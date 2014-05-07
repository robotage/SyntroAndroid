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

import java.net.*;
import android.os.SystemClock;
import android.util.Log;

public class HelloThread implements Runnable {

	public static final String TRACE_TAG = "HelloThread: ";

	public static final int	SOCKET_HELLO = 8040;				// the general socket number for hellos
	public static final int HELLO_START_INSTANCE = 2;			// instances start here
	public static final int HELLO_SYNC_PATTERN = 0xffa55a11;	// the sync bytes

	public static final int	HELLO_SYNC_LEN = 4;					// 4 bytes in sync

	public static final int	HELLO_UP = 1;						// state in hello state message
	public static final int	HELLO_DOWN = 0;						// as above

	public static final int	HELLO_INTERVAL = (2 * SyntroDefs.SYNTRO_CLOCKS_PER_SEC); // send a hello every 2 seconds if beaconing
	
	// The elements of the hello packet

	// 4 byte sync code
	public static final int SYNTRO_HELLO_SYNC = 0;

	// device IP addr
	public static final int SYNTRO_HELLO_IPADDR = SYNTRO_HELLO_SYNC + HELLO_SYNC_LEN;

	// component UID
	public static final int SYNTRO_HELLO_UID = SYNTRO_HELLO_IPADDR + SyntroDefs.SYNTRO_IPADDR_LEN;

	// app name
	public static final int SYNTRO_HELLO_APPNAME = SYNTRO_HELLO_UID + SyntroDefs.SYNTRO_UID_LEN;

	// component type
	public static final int SYNTRO_HELLO_COMPTYPE = SYNTRO_HELLO_APPNAME + SyntroDefs.SYNTRO_MAX_APPNAME;

	// priority byte of SyntroControl wanted
	public static final int SYNTRO_HELLO_PRIORITY = SYNTRO_HELLO_COMPTYPE + SyntroDefs.SYNTRO_MAX_COMPTYPE;

	// unused byte
	public static final int SYNTRO_HELLO_UNUSED = SYNTRO_HELLO_PRIORITY + 1;

	// heartbeat send interval
	public static final int SYNTRO_HEARTBEAT_INTERVAL = SYNTRO_HELLO_UNUSED + 1;

	// total length
	public static final int SYNTRO_HELLO_LENGTH = SYNTRO_HEARTBEAT_INTERVAL + 2;
	
	//	variables
	
	boolean beaconing = false;									// enable to start sending beacons
	boolean running = true;
	
	int				instance;									// the instance of this app
	Thread			t;											// the thread object
	SyntroParams	params;										// the apps parameters
	long			hellocontroltimer;							// for timing out an active controller

	DatagramPacket	rxpacket;									// for received packets
	public byte[]	rxHello;									// this is the cracked version
	DatagramPacket	txhellopacket;								// this is the hello that we send
	public byte[]	txHello = new byte[SYNTRO_HELLO_LENGTH];	// this is used to construct the datagram


	public 	byte[] controlIPAddr = new byte[SyntroDefs.SYNTRO_IPADDR_LEN];	// this is the IP address of SyntroControl
	public 	boolean gotControl = false;			// true when hello has got a valid SyntroControl address

	
	public HelloThread(int instance, SyntroParams params) {
		this.instance = instance;
		this.params = params;
	
		t = new Thread(this, "HelloThread");
		t.start();
	}
	
	public void startBeaconing() {
		gotControl = false;
		beaconing = true;
	}
	
	public void exitThread() {
		gotControl = false;
		beaconing = false;
		running = false;
	}
	
	public void run() {
		
		DatagramSocket 	socket;									// the hello socket
		long			lastsenttime;							// when we last sent a hello
		
		Log.d(TRACE_TAG, "Hello run starting");
		
		//	Create socket
		
		try {
			socket = new DatagramSocket(SOCKET_HELLO + instance);	// listen on our socket
			Log.d(TRACE_TAG, "Opened Hello socket");
		} catch (Exception e) {
			Log.e(TRACE_TAG, "Hello failed to open socket");
			return;
		}
		
		//	Set timeout as basic no activity timer for this loop
		
		try {
			socket.setSoTimeout(1000);								
		} catch (Exception e) {
			Log.e(TRACE_TAG, "Failed to set timeout on hello socket");
		}
		
	//	Enable broadcast
		
		try {
			socket.setBroadcast(true);
		} catch (Exception e) {
			Log.e(TRACE_TAG, "Failed to set broadcast on hello socket");
		}
		rxpacket = new DatagramPacket(new byte[512], 512);
		try {
			txhellopacket = new DatagramPacket(new byte[512], 512, 
					Inet4Address.getByAddress(SyntroDefs.broadcastaddress), SOCKET_HELLO);
			buildTXHello();
		} catch (Exception e) {
			Log.e(TRACE_TAG, "failed to create txhellopacket");
			txhellopacket = null;
		}
		
		lastsenttime = SystemClock.elapsedRealtime() - HELLO_INTERVAL;	// init to force hello send immediately
		
		while (running)												// main loop
		{
			try {
				socket.receive(rxpacket);
				processRXPacket(rxpacket);
			} catch (Exception e) {
				
			}	
			
			// see if need to send a hello
			
			if (beaconing & ((SystemClock.elapsedRealtime() - lastsenttime) > HELLO_INTERVAL)) {
			//	do need to send a hello
				try {
					socket.send(txhellopacket);
					Log.d(TRACE_TAG, "Sent beacon");
				} catch (Exception e) {
					Log.e(TRACE_TAG, "failed to send beacon");
				}
				lastsenttime = SystemClock.elapsedRealtime();
			}
			
		}
		socket.close();
	}
	
	private void processRXPacket(DatagramPacket rxpacket) {

		try {
			rxHello = rxpacket.getData();
		} catch (Exception e) {
			Log.e(TRACE_TAG, "rx hello exception" + e.getMessage());
			return;
		}
		
		if (!beaconing)
			return;
		
		//	Read in the hello and we are still looking. Now check to see what to do
		
		if (SyntroUtils.convertUC4ToInt(rxHello, SYNTRO_HELLO_SYNC) != HELLO_SYNC_PATTERN) {
			Log.e(TRACE_TAG, "Hello with incorrect sync received");
			return;
		}
		
		if (params.controlname[0] != 0) {
			if (!SyntroUtils.nameMatch(params.controlname, 0, rxHello, SYNTRO_HELLO_APPNAME))
				return;
		}
						
		//	It's a valid hello from the correct SyntroControl!
		
		hellocontroltimer = SystemClock.elapsedRealtime();	// reset the clock
  			
		//	got a hello from a new SyntroControl - save the data and stop beaconing
		
		System.arraycopy(rxHello, SYNTRO_HELLO_IPADDR, controlIPAddr, 
				0, SyntroDefs.SYNTRO_IPADDR_LEN);
		
		gotControl = true;
		beaconing = false;
	}
	
	private void buildTXHello() {
		SyntroUtils.convertIntToUC4(HELLO_SYNC_PATTERN, txHello, SYNTRO_HELLO_SYNC);
		
		System.arraycopy(SyntroDefs.myipaddress, 0, txHello, SYNTRO_HELLO_IPADDR, 
				SyntroDefs.SYNTRO_IPADDR_LEN);
		
		System.arraycopy(SyntroDefs.myuid, 0, txHello, SYNTRO_HELLO_UID, 
				SyntroDefs.SYNTRO_UID_LEN);
		
		System.arraycopy(params.appName, 0, txHello, SYNTRO_HELLO_APPNAME, 
				SyntroDefs.SYNTRO_MAX_APPNAME);
		
		System.arraycopy(params.compType, 0, txHello, SYNTRO_HELLO_COMPTYPE, 
				SyntroDefs.SYNTRO_MAX_COMPTYPE);
		
		SyntroUtils.convertIntToUC2(SyntroDefs.SYNTRO_HEARTBEAT_INTERVAL / SyntroDefs.SYNTRO_CLOCKS_PER_SEC, 
				txHello, SYNTRO_HEARTBEAT_INTERVAL);
		
		System.arraycopy(txHello, 0, txhellopacket.getData(), 0, SYNTRO_HELLO_LENGTH);
		txhellopacket.setLength(SYNTRO_HELLO_LENGTH);
	}
}
