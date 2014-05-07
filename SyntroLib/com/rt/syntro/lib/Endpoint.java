//
//  Copyright (c) 2014 richards-tech.
//
//  This file is part of SyntroNet
//
//  SyntroNet is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  SyntroNet is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with SyntroNet.  If not, see <http://www.gnu.org/licenses/>.
//

package com.rt.syntro.lib;

import java.io.IOException;
import java.net.InetAddress;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

public class Endpoint implements Runnable {
	
	public static final String TRACE_TAG = "Endpoint: ";
	
	private static final int SERVICE_BACKGROUND_INTERVAL = 1000;	// once per second
	
	// state codes
	public static final int SLSTATE_INIT = 0;			// the idle state - waiting for a SyntroControl
	public static final int SLSTATE_WFCONTROL = 1;		// beaconing now
	public static final int SLSTATE_WFCONNECT = 2;		// got a SyntroControl hello and connecting
	public static final int SLSTATE_CONNECTED = 3;		// now connected and sending status messages
	
	//	Timeouts
	
	private static final int CONNECT_TIMEOUT = 5000;		// 5 second timer for connects
	private static final int CONNECT_RETRY_INTERVAL = 1000;	// try to connect every second
	private static final int MULTICAST_TIMEOUT = (10 * SyntroDefs.SYNTRO_CLOCKS_PER_SEC); // 10 second timeout for unacked multicast send

	public int state = SLSTATE_INIT;		// this is the state variable
	
	//	variables
	
	Thread			t;									// the thread object
	Socket			socket;								// the link socket
	long			connectretrytimer;					// to avoid continuous connects when they are failing
	long			heartbeatTimer;						// for regular heartbeats
	long			DETimer;							// for DE updates
	long			watchdogtimer;						// for timing out the connection if don't receive heartbeats
	long			servicebackgroundtimer;				// for sending out service lookup requests
	
	//	some vars to handle receive

	SyntroMessage	rxsm = null;						// this is for our received message
	byte			hdr[] = new byte[SyntroDefs.SYNTRO_MESSAGE_LENGTH];	// read received header into this array initially
	int 			hdrindex = 0;						// this is the pointer into the temporary header
	int				dataindex;							// pointer into received data
	byte[]			data;								// where the received data goes
	int				dataleft;							// number of data byte remaining to be received
	
	SyntroServiceLookup	services[] = new SyntroServiceLookup[SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT]; // the requested service array
	int				reqservices = 0;					// number of requested services
	int				nextservice = 0;					// the next service to be processed by the service background
	
	HelloThread helloThread;
	
	SyntroDE de;
	SyntroParams params;
	
	int instance;
	boolean shouldRun;
	
	public Endpoint(WifiManager wifi, int instance, SyntroParams params) {
		this.params = params;
		this.instance = instance;
		socket = null;
		state = SLSTATE_INIT;
		
		try {
			setNetworkAddresses(wifi, instance);
		} catch (Exception e) {
			Log.e(TRACE_TAG, "failed to get broadcast address");
		}
						
		shouldRun = true;
		t = new Thread(this, "Endpoint");
		t.start();
	}
	
	public void exitThread() {
		appClientExit();
		shouldRun = false;
	}

	public void run() {
		InetSocketAddress remoteaddr;
		
		de = new SyntroDE();
		
		for (int i = 0; i < SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT; i++) {
			services[i] = new SyntroServiceLookup();
		}
		resetState();

		helloThread = new HelloThread(instance, params);
		appClientInit();
		
		while (shouldRun) {
			switch (state) {
				case SLSTATE_INIT:
					helloThread.startBeaconing();
					state = SLSTATE_WFCONTROL;
					break;
					
				case SLSTATE_WFCONTROL:
					if (!helloThread.gotControl)
						break;							// still haven't got a SyntroControl
					
					//	a SyntroControl is available - connect to it if it's time.
					
					if ((SystemClock.elapsedRealtime() - connectretrytimer) < CONNECT_RETRY_INTERVAL)
						break;
					
					connectretrytimer = SystemClock.elapsedRealtime();
					
					try {
						 remoteaddr = new InetSocketAddress(
								 InetAddress.getByAddress(helloThread.controlIPAddr), 
								 SyntroDefs.SYNTRO_PRIMARY_SOCKET_LOCAL);
					} catch (Exception e) {
						Log.e(TRACE_TAG, "Error on remoteaddr build");
						break;
					}
					state = SLSTATE_WFCONNECT;			// indicate waiting for connection (HelloThread uses this)
					socket = new Socket();
					try {
						socket.setSoTimeout(0);			// no timeout
						socket.setSendBufferSize(SyntroDefs.SYNTRO_MESSAGE_MAX * 3);
						socket.setReceiveBufferSize(SyntroDefs.SYNTRO_MESSAGE_MAX * 3);
						} catch (Exception e) {
						Log.e(TRACE_TAG, "failed to set socket buffer size " + e);
					}
					try {
						socket.connect(remoteaddr, CONNECT_TIMEOUT);
					} catch (Exception e) {
						Log.e(TRACE_TAG, "Error on connect");
						resetState();
						break;
					}
					if (socket.isConnected()) {
						state = SLSTATE_CONNECTED;
						Log.d(TRACE_TAG, "Connect to SyntroControl " + 
								SyntroUtils.displayIPAddr(helloThread.controlIPAddr, 0));
						buildDE();
						forceDE();
						
						watchdogtimer = SystemClock.elapsedRealtime();
						servicebackgroundtimer = SystemClock.elapsedRealtime();
						nextservice = 0;
						appClientConnected();
					}
					break;
									
				case SLSTATE_CONNECTED:
					if (!socket.isConnected()) {
						Log.e(TRACE_TAG, "no longer connected to SyntroControl");
						resetState();
						break;							// try to set up connection again
					}
					if ((SystemClock.elapsedRealtime() - heartbeatTimer) > SyntroDefs.SYNTRO_HEARTBEAT_INTERVAL) {
						if (!sendHeartBeat()) {
							resetState();
							break;
						}
					}
					if ((SystemClock.elapsedRealtime() - watchdogtimer) > SyntroDefs.SYNTRO_HEARTBEAT_TIMEOUT) {
						Log.e(TRACE_TAG, "timed out control connection.");
						resetState();
						break;
					}
					processReceive();
					serviceBackground();
					appClientBackground();
					break;
						
					
			}
			
			Thread.yield();	
		}
		Log.d(TRACE_TAG, "Endpoint exiting");
		helloThread.exitThread();
	}
	
	private boolean sendHeartBeat() {
		
		if ((SystemClock.elapsedRealtime() - DETimer) > SyntroDefs.SYNTRO_DE_INTERVAL) {
			DETimer = SystemClock.elapsedRealtime();
			
			SyntroMessage sm = new SyntroMessage(SyntroDefs.SYNTROMSG_HEARTBEAT, 
					SyntroDefs.SYNTROLINK_MEDHIGHPRI, 
					HelloThread.SYNTRO_HELLO_LENGTH + de.length() + 1);
			try {
				System.arraycopy(helloThread.txHello, 0, sm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH, HelloThread.SYNTRO_HELLO_LENGTH);
				System.arraycopy(de.get(), 0, sm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + HelloThread.SYNTRO_HELLO_LENGTH, 
						de.length());
				sendMessage(sm);
			} catch (Exception e) {
				Log.e(TRACE_TAG, "failed to generate heartbeat/DE message " + e);
				return false;
			}			
		} else {
			SyntroMessage sm = new SyntroMessage(SyntroDefs.SYNTROMSG_HEARTBEAT, 
					SyntroDefs.SYNTROLINK_MEDHIGHPRI, 
					HelloThread.SYNTRO_HELLO_LENGTH);
			try {
				System.arraycopy(helloThread.txHello, 0, sm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH, HelloThread.SYNTRO_HELLO_LENGTH);
				sendMessage(sm);
			} catch (Exception e) {
				Log.e(TRACE_TAG, "failed to generate heartbeat message" + e);
				return false;
			}
		}
		heartbeatTimer = SystemClock.elapsedRealtime();
		return true;
	}
	
	private void processReceive() {
		int				bytesread;
		
		if (state != SLSTATE_CONNECTED)
			return;
		
		try {
			while (socket.getInputStream().available() > 0) {
				if (hdrindex < SyntroDefs.SYNTRO_MESSAGE_LENGTH) {			// getting header
					try {
						bytesread = socket.getInputStream().read(hdr, hdrindex, dataleft);
					} catch (Exception e) {
						Log.e(TRACE_TAG, "error while getting header " + e);
						return;
					}
					if (bytesread == -1) {								// the connection has gone!
						Log.i(TRACE_TAG, "control connection dropped.");
						resetState();
						return;
					}
			
					dataleft -= bytesread;
					hdrindex += bytesread;
			
					if (hdrindex < SyntroDefs.SYNTRO_MESSAGE_LENGTH)
						continue;										// not got a complete header yet
				
					// 	got complete header
				
					if (!SyntroMessage.checkChecksum(hdr)) {
						Log.e(TRACE_TAG, "message header with checksum error");
						hdrindex = 0;									// reset and try again
						dataleft = SyntroDefs.SYNTRO_MESSAGE_LENGTH;
						continue;
					}
					if (SyntroUtils.convertUC4ToInt(hdr, SyntroDefs.SYNTRO_MESSAGE_TOTAL_LENGTH)
							>= SyntroDefs.SYNTRO_MESSAGE_MAX) {
						Log.e(TRACE_TAG, "received message that's too long " + 
							SyntroUtils.convertUC4ToInt(hdr, SyntroDefs.SYNTRO_MESSAGE_TOTAL_LENGTH));
						hdrindex = 0;
						dataleft = SyntroDefs.SYNTRO_MESSAGE_LENGTH;
						continue;
					}
			
					//	now allocate a new SyntroMessage with this information and process

					rxsm = new SyntroMessage(hdr);
					data = rxsm.get();									// get the pointer
					dataindex = SyntroDefs.SYNTRO_MESSAGE_LENGTH;		// where the data goes
					dataleft = rxsm.getDataLength();					// number of bytes required
					if (dataleft > 0)
						continue;										// if there are more bytes to get
				} else {
					try {
						bytesread = socket.getInputStream().read(data, dataindex, dataleft);
					} catch (Exception e) {
						Log.e(TRACE_TAG, "error while reading data part of message " + e);
						return;
					}
					if (bytesread == -1) {								// the connection has gone!
						Log.e(TRACE_TAG, "control connection dropped.");
						resetState();
						return;
					}
			
					dataleft -= bytesread;
					dataindex += bytesread;
			
					if (dataleft > 0)
						continue;
				}
				
				// getting here means we have a complete message
				
				hdrindex = 0;										// set up for next one
				dataleft = SyntroDefs.SYNTRO_MESSAGE_LENGTH;
				
				switch (rxsm.getCmd())
				{
					case SyntroDefs.SYNTROMSG_HEARTBEAT:
						if (rxsm.getDataLength() != HelloThread.SYNTRO_HELLO_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length HB " + rxsm.getDataLength());
							break;
						}
						watchdogtimer = SystemClock.elapsedRealtime();
						appClientHeartbeat(rxsm);
						break;

					case SyntroDefs.SYNTROMSG_SERVICE_LOOKUP_RESPONSE:
						if (rxsm.getDataLength() != SyntroDefs.SYNTRO_SERVICEREQ_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length service lookup response " + rxsm.getDataLength());
							break;
						}
						processLookupResponse(rxsm);
						break;
							
					case SyntroDefs.SYNTROMSG_SERVICE_ACTIVATE:
						if (rxsm.getDataLength() != SyntroDefs.SYNTRO_SERVICEACTIVATE_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length service activate " + rxsm.getDataLength());
							break;
						}
						processServiceActivate(rxsm);
						break;
							
					case SyntroDefs.SYNTROMSG_E2E:
						if (rxsm.getDataLength() < SyntroDefs.SYNTRO_EHEAD_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length E2E " + rxsm.getDataLength());
							break;
						}
						processE2E(rxsm);
						break;
						
					case SyntroDefs.SYNTROMSG_MULTICAST_MESSAGE:
						if (rxsm.getDataLength() < SyntroDefs.SYNTRO_EHEAD_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length multicast " + rxsm.getDataLength());
							break;
						}
						processMulticast(rxsm);
						break;


					case SyntroDefs.SYNTROMSG_MULTICAST_ACK:
						if (rxsm.getDataLength() < SyntroDefs.SYNTRO_EHEAD_LENGTH)
						{
							Log.e(TRACE_TAG, "got incorrect length multicast ack " + rxsm.getDataLength());
							break;
						}
						processMulticastAck(rxsm);
						break;
						
					case SyntroDefs.SYNTROMSG_DIRECTORY_RESPONSE:
						processDirectoryResponse(rxsm);
						break;

					default:
						Log.e(TRACE_TAG, "Unexpected message " + rxsm.getCmd());
						break;
				}
				return;
			}
		} catch (Exception e) {
			Log.e(TRACE_TAG, "error while processing received data " + e);
		}
	}
	
	private void processMulticast(SyntroMessage rxsm)
	{
		int servicePort;
		SyntroServiceLookup	service;

		servicePort = SyntroUtils.convertUC2ToInt(rxsm.get(), 
				SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_PORT);
		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Received multicast on out of range port " + servicePort);
			return;
		}
		service = services[servicePort];

		if (!service.inuse) {
			Log.e(TRACE_TAG, "Received multicast on not in use port " + servicePort);
			return;
		}

		if (service.serviceType != SyntroDefs.SERVICETYPE_MULTICAST) {
			Log.e(TRACE_TAG, "Multicast data received on port " + servicePort + " that is not a multicast service port");
			return;								
		}

		if (service.lastReceivedSeqNo == rxsm.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ])
			Log.e(TRACE_TAG, "Received duplicate multicast seq number " +  service.lastReceivedSeqNo +
					" source " + SyntroUtils.displayUID(rxsm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SRC_UID) +
					" dest " + SyntroUtils.displayUID(rxsm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_UID));
		
		service.lastReceivedSeqNo = rxsm.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ];

		appClientReceiveMulticast(servicePort, rxsm);
	}

	
	private void processMulticastAck(SyntroMessage rxsm)
	{
		int servicePort;
		SyntroServiceLookup	service;

		servicePort = SyntroUtils.convertUC2ToInt(rxsm.get(), 
				SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_PORT);
		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Received multicast ack on out of range port " + servicePort);
			return;
		}
		service = services[servicePort];

		if (!service.inuse) {
			Log.e(TRACE_TAG, "Received multicast ack on not in use port " + servicePort);
			return;
		}

		if (service.serviceType != SyntroDefs.SERVICETYPE_MULTICAST) {
			Log.e(TRACE_TAG, "Multicast ack received on port " + servicePort + " that is not a multicast service port");
			return;								
		}

		service.lastReceivedAck = rxsm.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ];

		appClientReceiveMulticastAck(servicePort, rxsm);
	}

	private void processE2E(SyntroMessage rxsm)
	{
		int servicePort;
		SyntroServiceLookup	service;

		servicePort = SyntroUtils.convertUC2ToInt(rxsm.get(), 
				SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_PORT);
		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Received E2E on out of range port " + servicePort);
			return;
		}
		service = services[servicePort];

		if (!service.inuse) {
			Log.e(TRACE_TAG, "Received E2E on not in use port " + servicePort);
			return;
		}

		if (service.serviceType != SyntroDefs.SERVICETYPE_E2E) {
			Log.e(TRACE_TAG, "E2E data received on port " + servicePort + " that is not a E2E service port");
			return;								
		}

		appClientReceiveE2E(servicePort, rxsm);
	}

	private void processDirectoryResponse(SyntroMessage rsxm)
	{
		byte[] rawBytes =new byte[rxsm.getDataLength()];
		System.arraycopy(rxsm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH, rawBytes, 0, rawBytes.length);
		
		String rawString = new String(rawBytes);
		
		String[] stringList = rawString.split("\00");
				
		appClientReceiveDirectory(stringList);
	}

	
	private void processServiceActivate(SyntroMessage rxsm) {
			int servicePort;
			SyntroServiceLookup	service;

			servicePort = SyntroUtils.convertUC2ToInt(rxsm.get(), 
					SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_SERVICEACTIVATE_LOCALPORT);
			if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
				Log.e(TRACE_TAG, "Received service activate on out of range port " + servicePort);
				return;
			}
			service = services[servicePort];

			if (!service.inuse) {
				Log.e(TRACE_TAG, "Received service activate on not in use port " + servicePort);
				return;
			}
			if (!service.local) {
				Log.e(TRACE_TAG, "Received service activate on remote service port " + servicePort);
				return;
			}
			if (!service.enabled) {
				return;
			}
			service.remotePort = SyntroUtils.convertUC2ToInt(rxsm.get(), 
					SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_SERVICEACTIVATE_CONTROLPORT);
			service.state = SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_ACTIVE;
			service.timeLastLookup = SystemClock.elapsedRealtime();
			Log.d(TRACE_TAG, "Received service activate for port " + servicePort + 
					" to SyntroControl port " + service.remotePort);
	}
	
	private void processLookupResponse(SyntroMessage sm) {
		SyntroServiceLookup ssl = new SyntroServiceLookup();
		try {
			System.arraycopy(sm.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH,
					ssl.packet, 0, SyntroDefs.SYNTRO_SERVICEREQ_LENGTH);	// crack the response
		} catch (Exception e) {
			Log.e(TRACE_TAG, "failed to crack service lookup repsonse");
		}
		
		int index = SyntroUtils.convertUC2ToInt(ssl.packet, SyntroDefs.SYNTRO_SERVICEREQ_LOCALPORT);									// get the index in tot he service array
		if (index >= reqservices) {
			Log.e(TRACE_TAG, "received SSL with invalid lport");
			return;
		}
		
		SyntroServiceLookup srv = services[index];
		
		if (ssl.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] == SyntroDefs.SERVICE_LOOKUP_FAIL) {
			if (srv.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] == SyntroDefs.SERVICE_LOOKUP_SUCCEED) {
				Log.e(TRACE_TAG, "service is no longer available " + 
						SyntroUtils.displayName(srv.packet, SyntroDefs.SYNTRO_SERVICEREQ_SERVPATH));
			} else {
				Log.d(TRACE_TAG, "service is unavailable " + 
						SyntroUtils.displayName(srv.packet, SyntroDefs.SYNTRO_SERVICEREQ_SERVPATH));
			}
			srv.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] = SyntroDefs.SERVICE_LOOKUP_FAIL;
			srv.registered = false;
			return;
		}
		
		if (srv.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] == SyntroDefs.SERVICE_LOOKUP_SUCCEED) {
			if (Arrays.equals(srv.packet, ssl.packet)) {
				// just reconfirming existing entry 
				return;								// nothing to do
			}		
		}
		System.arraycopy(ssl.packet, 0, srv.packet, 0, SyntroDefs.SYNTRO_SERVICEREQ_LENGTH);
		srv.registered = true;
		srv.remotePort = SyntroUtils.convertUC2ToInt(srv.packet, SyntroDefs.SYNTRO_SERVICEREQ_REMOTEPORT);
		System.arraycopy(srv.packet, SyntroDefs.SYNTRO_SERVICEREQ_UID, srv.remoteUID, 0, SyntroDefs.SYNTRO_UID_LEN);
		Log.i(TRACE_TAG, "Service " + srv.servicePath + " mapped to UID " + SyntroUtils.displayUID(srv.remoteUID, 0) + 
				" port " + srv.remotePort);
	}

	private boolean sendMessage(SyntroMessage msg)
	{
		if (state != SLSTATE_CONNECTED)
			return false;

		if (!socket.isConnected())
			return false;
				
		try {
			socket.getOutputStream().write(msg.get());
			socket.getOutputStream().flush();
		} catch (Exception e) {
			Log.e(TRACE_TAG, "socket write failed" + e);
			return false;
		}
		return true;
	}
	
	private void resetState() {

		int		i;
		
		if (socket != null) {
			if (socket.isConnected()) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (Exception e) {
					Log.e(TRACE_TAG, "socket close failed " + e);
				}
			}
			socket = null;
		}
			
		if (state == SLSTATE_CONNECTED)
			appClientClosed();
		
		state = SLSTATE_INIT;
		hdrindex = 0;
		dataleft = SyntroDefs.SYNTRO_MESSAGE_LENGTH;		// setup for new header
		
		for (i = 0; i < services.length; i++) {
			services[i].registered = false;
			services[i].packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] = SyntroDefs.SERVICE_LOOKUP_FAIL;
		}
		connectretrytimer = SystemClock.elapsedRealtime() - CONNECT_RETRY_INTERVAL;;
	}
	
	synchronized private void serviceBackground() {
		SyntroServiceLookup service;
		int servicePort;
		long now = SystemClock.elapsedRealtime();
		
		if ((SystemClock.elapsedRealtime() - servicebackgroundtimer) < SERVICE_BACKGROUND_INTERVAL) {
			return;
		}
		servicebackgroundtimer = SystemClock.elapsedRealtime();
		for (servicePort = 0; servicePort < reqservices; servicePort++) {
			service = services[servicePort];
			if (!service.inuse)
				continue;										// not being used
			if (!service.enabled)
				continue;
			if (service.local) {								// local service background
				if (service.state != SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_ACTIVE)
					continue;									// no current activation anyway

				if ((now - service.timeLastLookup) >= SyntroServiceLookup.SERVICE_REFRESH_TIMEOUT) {
					service.state = SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_INACTIVE;	// indicate inactive and no data should be sent
					Log.e(TRACE_TAG, "Timed out activation on local service " + service.servicePath +
							" port " + servicePort);
				}
			} else {											// remote service background
				switch (service.state) {
					case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK:			// need to start looking up
						service.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] = SyntroDefs.SERVICE_LOOKUP_FAIL; // indicate we know nothing
						sendRemoteServiceLookup(service);
						service.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOKING;
						break;

					case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOKING:
						if ((now - service.timeLastLookup >= SyntroServiceLookup.SERVICE_LOOKUP_INTERVAL))
							sendRemoteServiceLookup(service);	// try again
						break;

					case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED:
						if ((now - service.timeLastLookupResponse >= SyntroServiceLookup.SERVICE_REFRESH_TIMEOUT)) {
							Log.e(TRACE_TAG, "Refresh timeout on service " + service.servicePath + 
									" port " + servicePort);
							service.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK;	// go back to looking
							break;
						}
						if ((now - service.timeLastLookup >= SyntroServiceLookup.SERVICE_REFRESH_INTERVAL))
							sendRemoteServiceLookup(service);	// do a refresh
						break;

					case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVE:
						service.packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] = SyntroDefs.SERVICE_LOOKUP_REMOVE; // request the remove
						sendRemoteServiceLookup(service);
						service.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVING;
						service.closingRetries = 0;
						break;

					case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVING:
						if ((now - service.timeLastLookup >= SyntroServiceLookup.SERVICE_REMOVING_INTERVAL)) {
							if (++service.closingRetries == SyntroServiceLookup.SERVICE_REMOVING_MAX_RETRIES) {
								Log.e(TRACE_TAG, "Timed out attempt to remove remote registration for service " + 
							service.servicePath + " on port " + servicePort);
								if (service.removingService) {
									service.inuse = false;
									service.removingService = false;
								}
								service.enabled = false;
								break;
							}
							sendRemoteServiceLookup(service);
							break;
						}
						break;

					default:
						Log.e(TRACE_TAG, "Illegal state " + service.state + " on remote service port " +
									servicePort);
						service.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK;	// go back to looking
						break;
				}
			}
		}

	}
	
	private void sendRemoteServiceLookup(SyntroServiceLookup remoteService)
	{
		if (remoteService.local) {
			Log.e(TRACE_TAG, "send remote service lookup on local service port");
			return;
		}
		SyntroMessage message = new SyntroMessage(SyntroDefs.SYNTRO_SERVICEREQ_LENGTH);
		System.arraycopy(remoteService.packet, 0, message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH, SyntroDefs.SYNTRO_SERVICEREQ_LENGTH);
		message.setHeaderData(SyntroDefs.SYNTROMSG_SERVICE_LOOKUP_REQUEST, SyntroDefs.SYNTROLINK_MEDHIGHPRI);
		sendMessage(message);
		remoteService.timeLastLookup = SystemClock.elapsedRealtime();
	}

	
	private void setNetworkAddresses(WifiManager wifi, int instance) throws IOException {
    	DhcpInfo dhcp = wifi.getDhcpInfo();
       	int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
       	int myip = dhcp.ipAddress;
    	
    	byte[] baddr = new byte[SyntroDefs.SYNTRO_IPADDR_LEN];
    	byte[] maddr = new byte[SyntroDefs.SYNTRO_IPADDR_LEN];
    	
    	for (int i = 0; i < SyntroDefs.SYNTRO_IPADDR_LEN; i++) {
       		baddr[i] = (byte) ((broadcast >> i * 8) & 0xFF);
       		maddr[i] = (byte) ((myip >> i * 8) & 0xFF);
       	}
    	
    	System.arraycopy(baddr, 0, SyntroDefs.broadcastaddress, 0, SyntroDefs.SYNTRO_IPADDR_LEN);
    	System.arraycopy(maddr, 0, SyntroDefs.myipaddress, 0, SyntroDefs.SYNTRO_IPADDR_LEN);
    	
    	WifiInfo wifiinfo = wifi.getConnectionInfo();
    	System.arraycopy(SyntroUtils.MACAddrStringToMACAddr(wifiinfo.getMacAddress()), 0, 
    			SyntroDefs.mymacaddress, 0, SyntroDefs.SYNTRO_MACADDR_LEN);
    	
    	System.arraycopy(SyntroUtils.MACAddrStringToMACAddr(wifiinfo.getMacAddress()), 0, 
    			SyntroDefs.myuid, 0, SyntroDefs.SYNTRO_MACADDR_LEN);
		SyntroDefs.myuid[SyntroDefs.SYNTRO_MACADDR_LEN] = 0;
		SyntroDefs.myuid[SyntroDefs.SYNTRO_MACADDR_LEN + 1] = (byte)instance;
		
		Log.d(TRACE_TAG, "My IP: " + SyntroUtils.displayIPAddr(SyntroDefs.myipaddress, 0));
		Log.d(TRACE_TAG, "My bcast: " + SyntroUtils.displayIPAddr(SyntroDefs.broadcastaddress, 0));
		Log.d(TRACE_TAG, "My UID: " + SyntroUtils.displayUID(SyntroDefs.myuid, 0));

    }
	
	private void buildDE() {
		de.DESetup(SyntroUtils.displayName(params.appName, 0), 
				SyntroUtils.displayName(params.compType, 0), 
				SyntroUtils.displayUID(helloThread.txHello, HelloThread.SYNTRO_HELLO_UID));
		
		// find highest in use service index
		int highest = 0;
		
		for (int i = 0; i < SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT; i++) {
			if (services[i].inuse)
				highest = i;
		}
		
		for (int i = 0; i <= highest ; i++) {
			if (services[i].inuse && services[i].local) {
				if (services[i].serviceType == SyntroDefs.SERVICETYPE_MULTICAST)
					de.DEAddValue(SyntroDefs.DETAG_MSERVICE, services[i].servicePath);
				else
					de.DEAddValue(SyntroDefs.DETAG_ESERVICE, services[i].servicePath);
			} else {
				de.DEAddValue(SyntroDefs.DETAG_NOSERVICE, "");
			}
		}

		de.DEComplete();
	}
	
	private void forceDE()
	{
		heartbeatTimer = SystemClock.elapsedRealtime() - SyntroDefs.SYNTRO_HEARTBEAT_INTERVAL;
		DETimer = SystemClock.elapsedRealtime() - SyntroDefs.SYNTRO_DE_INTERVAL;
	}
	
	//------------------------------------------------------
	//
	//	Client level functions
	
	protected void clientRequestDirectory() {
		SyntroMessage message = new SyntroMessage(0);
		message.setHeaderData(SyntroDefs.SYNTROMSG_DIRECTORY_REQUEST, SyntroDefs.SYNTROLINK_LOWPRI);
		sendMessage(message);
	}
	
	protected int clientAddService(String srvpath, int srvtype, boolean local, boolean enabled) {
		if (reqservices == SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT) {
			Log.e(TRACE_TAG, "too many services");
			return -1;
		}
		
		if (srvpath.length() >= SyntroDefs.SYNTRO_MAX_SERVPATH) {
			Log.e(TRACE_TAG, "Service path too long " + srvpath);
			return -1;
		}
		
		SyntroServiceLookup srv = services[reqservices];
		
		srv.init(reqservices, srvpath, srvtype, local);
		
		srv.enabled = enabled;
		srv.removingService = false;
		srv.state = SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_INACTIVE;
		srv.serviceData = -1;
		if (!local) {
			srv.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK;
			srv.timeLastLookup = SystemClock.elapsedRealtime();
		}
		buildDE();
		return reqservices++;
	}
	
	protected boolean clientEnableService(int servicePort)
	{
		SyntroServiceLookup srv;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Request to enable service on out of range port " + servicePort);
			return false;
		}
		srv = services[servicePort];
		if (!srv.inuse) {
			Log.e(TRACE_TAG, "Attempt to enable service on not in use port " + servicePort);
			return false;
		}
		if (srv.removingService) {
			Log.e(TRACE_TAG, "Attempt to enable service on port %1 that is being removed " + servicePort);
			return false;
		}
		srv.enabled = true;
		if (srv.local) {
			srv.state = SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_INACTIVE;
			buildDE();
			return true;
		} else {
			srv.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK;
			srv.timeLastLookup = SystemClock.elapsedRealtime();
			return true;
		}
	}
	
	protected boolean clientIsServiceActive(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Request to status service on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Attempt to status service on not in use port " + servicePort);
			return false;
		}
		if (!service.enabled)
			return false;
		if (service.local) {
			return service.state == SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_ACTIVE;
		} else {
			return service.state == SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED;
		}
	}
	
	protected boolean clientIsServiceEnabled(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Request to get enable status on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Attempt to enable status on not in use port " + servicePort);
			return false;
		}
		if (service.local) {
			return service.enabled;
		} else {
			if (!service.enabled)
				return false;
			if (service.state == SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVE)
				return false;
			if (service.state == SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVING)
				return false;
			return true;
		}
	}
	
	protected boolean clientDisableService(int servicePort) 
	{
		return disableService(servicePort);
	}
	
	private boolean disableService(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Request to disable service on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Attempt to disable service on not in use port " + servicePort);
			return false;
		}
		if (service.local) {
			forceDE();
			service.enabled = false;
			return true;
		} else {
			switch (service.state) {
				case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVE:
				case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVING:
					Log.e(TRACE_TAG, "Attempt to disable service on port that's already being disabled " + servicePort);
					return false;

				case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOK:
					if (service.removingService) {
						service.inuse = false;					// being removed
						service.removingService = false;
					}
					service.enabled = false;
					return true;

				case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_LOOKING:
				case SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED:
					service.state = SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REMOVE; // indicate we want to remove whatever happens
					return true;

				default:
					Log.e(TRACE_TAG, "Disable service on port " + servicePort + " with state " + service.state);
					service.enabled = false;					// just disable then
					if (service.removingService) {
						service.inuse = false;
						service.removingService = false;
					}
					return false;
			}
		}
	}
	
	protected boolean clientRemoveService(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried to remove a service on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to remove a service on not in use port " + servicePort);
			return false;
		}
		if (!service.enabled) {
			service.inuse = false;								// if not enabled, just mark as not in use
			return true;
		}
		if (service.local) {
			service.enabled = false;
			service.inuse = false;
			buildDE();
			forceDE();
			return true;
		} else {
			service.removingService = true;					
			return disableService(servicePort);
		}
	}
	
	protected String clientGetServicePath(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried get service path on out of range port " + servicePort);
			return "";
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to get service path on not in use port " + servicePort);
			return "";
		}
		return service.servicePath;
	}
	
	protected int clientGetServiceType(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried get service type on out of range port " + servicePort);
			return -1;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to get service type on not in use port " + servicePort);
			return -1;
		}
		return service.serviceType;
	}	
	
	protected boolean clientIsServiceLocal(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried get service local on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to get service local on not in use port " + servicePort);
			return false;
		}
		return service.local;
	}
	
	protected int clientGetServiceRemotePort(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried get remote port on out of range port " + servicePort);
			return -1;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to get remote port on not in use port " + servicePort);
			return -1;
		}

		if (!service.enabled) {
			Log.e(TRACE_TAG, "Tried to get remote port for service on disabled port " + servicePort);
			return -1;
		}
		if (service.local) {
			if (service.state != SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_ACTIVE) {
				Log.e(TRACE_TAG, "Tried to get dest port for inactive service port " + servicePort);
				return -1;
			}
			return service.remotePort;
		} else {
			if (service.state != SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED) {
				Log.e(TRACE_TAG, "Tried to get dest port for inactive service port " + servicePort + 
						" in state " + service.state);
				return -1;
			}
			return service.remotePort;
		}
	}
	
	protected int clientGetRemoteServiceState(int servicePort)
	{		
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Request for service state on out of range port " + servicePort);
			return -1;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			return SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_NOTINUSE;								
		}
		if (service.local) {
			return SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_NOTINUSE;								
		}
		if (!service.enabled) {
			return SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_NOTINUSE;								
		}
		return service.state;
	}
	
	protected byte[] clientGetRemoteServiceUID(int servicePort)
	{
		SyntroServiceLookup service;
		byte[] uid = new byte[SyntroDefs.SYNTRO_UID_LEN];

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Tried get remote port on out of range port " + servicePort);
			return uid;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Tried to get remote port on not in use port " + servicePort);
			return uid;
		}

		if (service.local) {
			Log.e(TRACE_TAG, "GetRemoteServiceUID on port that is a local service port " + servicePort);
			return uid;								
		}
		if (service.state != SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED) {
			Log.e(TRACE_TAG, "GetRemoteServiceUID on port " + servicePort + " in state " + service.state);
			return uid;
		}

		return service.remoteUID;
	}
	
	protected boolean clientIsConnected()
	{
		return state == SLSTATE_CONNECTED;
	}
	
	protected void clientSetServiceData(int servicePort, int value)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Set service data on out of range port " + servicePort);
			return;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Set service data on not in use port " + servicePort);
			return;
		}

		service.serviceData = value;
	}
	
	protected int clientGetServiceData(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "Get service data on out of range port " + servicePort);
			return -1;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "Get service data on not in use port " + servicePort);
			return -1;
		}

		return service.serviceData;
	}
	
	protected boolean clientClearToSend(int servicePort)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "clientClearToSend on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "clientClearToSend on not in use port " + servicePort);
			return false;
		}

		if (!service.enabled) {
			Log.e(TRACE_TAG, "clientClearToSend on not enabled port " + servicePort);
			return false;
		}

		// within the send/ack window ?
		if (SyntroUtils.isSendOK(service.nextSendSeqNo, service.lastReceivedAck)) {
			return true;
		}

		// if we haven't timed out, wait some more
		if ((SystemClock.elapsedRealtime() - service.lastSendTime) <  MULTICAST_TIMEOUT) {
			return false;
		}

		// we timed out, reset our sequence numbers
		service.lastReceivedAck = service.nextSendSeqNo;
		return true;
	}
	
	protected SyntroMessage clientBuildMessage(int servicePort, int length)
	{
		SyntroServiceLookup service;
		SyntroMessage message;
		byte[] remoteUID;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "clientBuildMessage on out of range port " + servicePort);
			return null;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "clientBuildMessage on not in use port " + servicePort);
			return null;
		}

		if (!service.enabled) {
			Log.e(TRACE_TAG, "clientBuildMessage on not enabled port " + servicePort);
			return null;
		}

		if (service.serviceType == SyntroDefs.SERVICETYPE_MULTICAST) {
			remoteUID = SyntroDefs.myuid;
		} else {
			if (service.local) {
				Log.e(TRACE_TAG, "clientBuildMessage on local service E2E port " + servicePort);
				return null;
			}

			remoteUID = service.remoteUID;										 	
		}
		message = new SyntroMessage(length + SyntroDefs.SYNTRO_EHEAD_LENGTH);
		
		System.arraycopy(remoteUID, 0,
				message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_UID,
				SyntroDefs.SYNTRO_UID_LEN);
		
		System.arraycopy(SyntroDefs.myuid, 0,
				message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SRC_UID,
				SyntroDefs.SYNTRO_UID_LEN);
		
		SyntroUtils.convertIntToUC2(service.remotePort, 
				message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_PORT);
		
		SyntroUtils.convertIntToUC2(servicePort, 
				message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SRC_PORT);
		
		
		message.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ] = 0;								 
		return message;
	}

	protected boolean clientSendMessage(int servicePort, SyntroMessage message, int priority)
	{
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "clientSendMessage on out of range port " + servicePort);
			return false;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "clientSendMessage on not in use port " + servicePort);
			return false;
		}

		if (!service.enabled) {
			Log.e(TRACE_TAG, "clientSendMessage on not enabled port " + servicePort);
			return false;
		}

		if (service.serviceType == SyntroDefs.SERVICETYPE_MULTICAST) {
			if (!service.local) {
				Log.e(TRACE_TAG, "Tried to send multicast message on E2E service port " + servicePort);
				return false;
			}
			if (service.state != SyntroServiceLookup.SYNTRO_LOCAL_SERVICE_STATE_ACTIVE) {
				Log.e(TRACE_TAG, "Tried to send multicast message on inactive port " + servicePort);
				return false;
			}
			message.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ] = service.nextSendSeqNo++;
			message.setHeaderData(SyntroDefs.SYNTROMSG_MULTICAST_MESSAGE, priority);
			sendMessage(message);
		} else {
			if (!service.local && (service.state != SyntroServiceLookup.SYNTRO_REMOTE_SERVICE_STATE_REGISTERED)) {
				Log.e(TRACE_TAG, "Tried to send E2E message on remote service without successful lookup on port " + servicePort);
				return false;
			}
			message.setHeaderData(SyntroDefs.SYNTROMSG_E2E, priority);
			sendMessage(message);
		}
		service.lastSendTime = SystemClock.elapsedRealtime();
		return true;
	}

	protected void clientSendMulticastAck(int servicePort, int seqno) {
		
		SyntroServiceLookup service;

		if ((servicePort < 0) || (servicePort >= SyntroDefs.SYNTRO_MAX_SERVICESPERCOMPONENT)) {
			Log.e(TRACE_TAG, "sendMulticastAck on out of range port " + servicePort);
			return;
		}
		service = services[servicePort];
		
		if (!service.inuse) {
			Log.e(TRACE_TAG, "sendMulticastAck on not in use port " + servicePort);
			return;
		}

		if (!service.enabled) {
			Log.e(TRACE_TAG, "sendMulticastAck on not enabled port " + servicePort);
			return;
		}
		
		SyntroMessage mam = new SyntroMessage(SyntroDefs.SYNTROMSG_MULTICAST_ACK, 
				SyntroDefs.SYNTROLINK_HIGHPRI, SyntroDefs.SYNTRO_EHEAD_LENGTH);
		
		System.arraycopy(service.remoteUID, 0,
				mam.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_UID,
				SyntroDefs.SYNTRO_UID_LEN);
		
		System.arraycopy(SyntroDefs.myuid, 0,
				mam.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SRC_UID,
				SyntroDefs.SYNTRO_UID_LEN);
		
		SyntroUtils.convertIntToUC2(service.remotePort, 
				mam.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_DST_PORT);
		
		SyntroUtils.convertIntToUC2(servicePort, 
				mam.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SRC_PORT);
		

		mam.get()[SyntroDefs.SYNTRO_MESSAGE_LENGTH + SyntroDefs.SYNTRO_EHEAD_SEQ] = (byte)(seqno + 1);

		sendMessage(mam);
	}
	
	//------------------------------------------------------
	//
	//	Default client overrides
	
	protected void appClientInit(){
	}
	
	protected void appClientExit() {
	}
	
	protected void appClientConnected() {
	}
	
	protected void appClientBackground() {
	}
	
	protected void appClientClosed() {
	}
	
	protected void appClientHeartbeat(SyntroMessage heartbeat)
	{
	}

	protected void appClientReceiveMulticast(int servicePort, SyntroMessage message)
	{
		Log.e(TRACE_TAG, "Received unexpected multicast on port " + servicePort);
	}
	
	protected void appClientReceiveMulticastAck(int servicePort, SyntroMessage message)
	{
		Log.e(TRACE_TAG, "Received unexpected multicast ack on port " + servicePort);
	}
	
	protected void appClientReceiveE2E(int servicePort, SyntroMessage message)
	{
		Log.e(TRACE_TAG, "Received unexpected E2E on port " + servicePort);
	}
	
	protected void appClientReceiveDirectory(String[] dirList)
	{
		Log.e(TRACE_TAG, "Received unexpected directory");
	}

}

