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

import java.util.Arrays;

public class SyntroServiceLookup {
	
	//	local service state defs

	public static final int SYNTRO_LOCAL_SERVICE_STATE_INACTIVE = 0;// no subscribers
	public static final int SYNTRO_LOCAL_SERVICE_STATE_ACTIVE = 1;	// one or more subscribers
	
	public static final int SERVICE_LOOKUP_INTERVAL = (SyntroDefs.SYNTRO_CLOCKS_PER_SEC * 2); 	// while waiting
	public static final int SERVICE_REFRESH_INTERVAL = (SyntroDefs.SYNTRO_CLOCKS_PER_SEC * 4); 	// when registered
	public static final int SERVICE_REFRESH_TIMEOUT = (SERVICE_REFRESH_INTERVAL * 3);			// Refresh timeout period
	public static final int SERVICE_REMOVING_INTERVAL = (SyntroDefs.SYNTRO_CLOCKS_PER_SEC * 4); // when closing
	public static final int SERVICE_REMOVING_MAX_RETRIES = 2;			// number of times an endpoint retries closing a remote service

	//	remote service state defs

	public static final int SYNTRO_REMOTE_SERVICE_STATE_NOTINUSE = 0;	// indicates remote service port is not in use
	public static final int SYNTRO_REMOTE_SERVICE_STATE_LOOK = 1;		// requests a lookup on a service
	public static final int SYNTRO_REMOTE_SERVICE_STATE_LOOKING = 2;	// outstanding lookup
	public static final int SYNTRO_REMOTE_SERVICE_STATE_REGISTERED = 3;	// successfully registered
	public static final int SYNTRO_REMOTE_SERVICE_STATE_REMOVE = 4;		// request to remove a remote service registration
	public static final int SYNTRO_REMOTE_SERVICE_STATE_REMOVING = 5;	// remove request has been sent
	
	//	These fields contains the unpacked values
	
	public String servicePath;								// the path for the service
	public int serviceType;									// multicast or e2e
	public boolean local;									// true if a local service, false for remote
	public int localPort;									// local port
	public int remotePort;									// remote port
	public byte[] remoteUID = new byte[SyntroDefs.SYNTRO_UID_LEN]; //

	// 	state variables
	
	public boolean registered = false;						// true if registered
	public boolean inuse = false;							// true if in use
	public boolean enabled = false;							// true if enabled

	public int serviceData;									// the int value that the app client can set

	public boolean removingService;							// true if disabling due to service removal
	public long timeLastLookup;								// time of last lookup (for timeout purposes)
	public long timeLastLookupResponse;						// time of last lookup response (for timeout purposes)
	public int closingRetries;								// number of times a close has been retried
	public int	state;										// state of the service

	public int lastReceivedSeqNo;							// sequence number on last received multicast message
	public byte nextSendSeqNo;								// the number to use on the next sent multicast message
	public byte lastReceivedAck;							// the last ack received
	public long lastSendTime;								// time the last multicast frame was sent
	
	// 	This is the packed version
	
	public byte[] packet = new byte[SyntroDefs.SYNTRO_SERVICEREQ_LENGTH];
	
	public SyntroServiceLookup() {
		inuse = false;
	}
		
	public void init(int localPort, String servicePath, int serviceType, boolean local) {
		
		state = SYNTRO_LOCAL_SERVICE_STATE_INACTIVE;
		serviceData = -1;

		lastReceivedSeqNo = -1;
		nextSendSeqNo = 0;
		lastReceivedAck = 0;
		lastSendTime = 0;
		
		this.localPort = localPort;
		this.servicePath = new String(servicePath);
		this.serviceType = serviceType;
		this.local = local;
		
		Arrays.fill(packet, (byte)0);
		
		System.arraycopy(servicePath.getBytes(), 0, 
				packet, SyntroDefs.SYNTRO_SERVICEREQ_SERVPATH, servicePath.length());

		packet[SyntroDefs.SYNTRO_SERVICEREQ_SERVICETYPE] = (byte)serviceType;
		registered = false;
		packet[SyntroDefs.SYNTRO_SERVICEREQ_RESPONSE] = SyntroDefs.SERVICE_LOOKUP_FAIL;
		SyntroUtils.convertIntToUC2(localPort, packet, SyntroDefs.SYNTRO_SERVICEREQ_LOCALPORT); 
		inuse = true;
	}
	
}
