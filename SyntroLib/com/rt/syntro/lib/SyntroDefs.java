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

//	SyntroDefs.java - Definitions for the Syntro system
//
//
//	This file is part of the Syntro software system.
//
//	Copyright (c) 2011 Silicon Genome, LLC.
//
//	Redistribution and use in source and binary forms, with or without
//	modification, are permitted provided that the following conditions
//	are met:
//
//	1. Redistributions of source code must retain the above copyright
//	notice, this list of conditions and the following disclaimer.
//
//	2. Redistributions in binary form must reproduce the above copyright
//	notice, this list of conditions and the following disclaimer in the
//	documentation and/or other materials provided with the distribution.
//
//	3. The name of the Silicon Genome, LLC may not be used to endorse or 
//	promote products derived from this software without specific prior
//		written permission.
//
//		 SOFTWARE IS PROVIDED BY THE SILICON GENOME, LLC ``AS IS'' AND
//	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//		 DISCLAIMED.  IN NO EVENT SHALL SILICON GENOME, LLC BE LIABLE
//	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//	DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//	OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//	HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//	LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//	OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//	SUCH DAMAGE.
//

import java.lang.String;

public class SyntroDefs {
	public static final	String PRODUCT_VERSION = "1.1.0";

	public static final String DEBUG_TAG = "SyntroDefs";
	
	//	Time defs
	
	public static final int SYNTRO_CLOCKS_PER_SEC = 1000;			// 1000 ticks per second

	//The defines below are the most critical Syntro system definitions. Be very careful about
	//changing these as unexpected results may occur. Some values are assuemd in SyntroConfig
	//dialogs - these would also need to be updated if the corresponding values here are
	//changed. For example, SYNTRO_MAX_COMPONENTS_PER_DEVICE is used to define the size
	//of the SyntroExec parameter file. Many things would break if this was changed!

	public static final	int SYNTRO_MAX_COMPONENTSPERDEVICE = 32;	// maximum number of components assigned to a single device
	public static final	int SYNTRO_MAX_SERVICESPERCOMPONENT	= 128;	// max number of services in a component

	//These defs are for a string version of the SYNTRO_UID

	public static final int SYNTRO_UID_LEN = 8;						// 8 byte in a UID
	public static final	int SYNTRO_UIDSTR_LEN = (SYNTRO_UID_LEN * 2 + 1);	// length of string version as hex pairs plus 0

	
	//IP and MAC related definitions
	
	public static final	int SYNTRO_IPSTR_LEN = 16;					// size to use for IP string buffers (xxx.xxx.xxx.xxx plus 0)
	public static final	int SYNTRO_IPADDR_LEN = 4;					// 4 byte IP address
	public static final	int SYNTRO_MACADDR_LEN = 6;					// length of a MAC address
	public static final	int SYNTRO_MACADDRSTR_LEN = (SYNTRO_MACADDR_LEN*2+1);	// the hex string version

	public static final byte[] broadcastaddress = new byte[SYNTRO_IPADDR_LEN];	// this is the broadcast address for this network
	public static final byte[] myipaddress = new byte[SYNTRO_IPADDR_LEN];	// my IP address
	public static final byte[] mymacaddress = new byte[SYNTRO_MACADDR_LEN];	// my mac address
	public static final byte[] myuid = new byte[SYNTRO_UID_LEN];	// my UID
	
	//	Service Path Syntax
	//
	//	When a component wishes to communicate with a service on another component, it needs to locate it using
	//	a service path string. This is mapped by SyntroControl into a UID and port number. 
	//	The service string can take various forms (note that case is important):
	//
	//	Form 1 -	Service Name. In this case, the service path is simply the service name. SyntroControl will
	//						look up the name and stop at the first service with a matching name. for example, if the
	//						service name is "video", the service path would be "video".
	//
	//	Form 2 -	Component name and service name. The service path is the component name, then "/" and
	//						then the service name. SyntroControl will only match the service name against
	//						the component with the specified component name. For example, if the service name is "video"
	//						and the component name is "WebCam", the service path would be "WebCam/video". "*" is a wildcard
	//						so that "*/video" is equivalent to "video".
	//
	//	Form 3 -	Region name, component name and service name. The service path consists of a region name, then
	//						a "/", then the component name, then a "/" and then the service name. As an example, if the 
	//						region name is "Robot1", the component name "WebCam" and the service name "video", the
	//						service path would be "Robot1/WebCam/video". Again, "*" is a wildcard for regions and
	//						components so that "Robot1/*/video" would match the first service called "video" found in region
	//						"Robot1".

	public static final	char SYNTRO_SERVICEPATH_SEP = '/';				// the path element separator character
	public static final	char SYNTRO_STREAM_TYPE_SEP = ':';				// the stream type separator character

	//Syntro constants

	public static final	int SYNTRO_HEARTBEAT_INTERVAL = (2 * SYNTRO_CLOCKS_PER_SEC);// heartbeat every two seconds
	public static final	int SYNTRO_HEARTBEAT_TIMEOUT = (5 * SYNTRO_CLOCKS_PER_SEC);	// 5 second timeout
	public static final int SYNTRO_DE_INTERVAL = (10 * SYNTRO_CLOCKS_PER_SEC);		// 10 seconds between directory updates sent

	public static final	int SYNTRO_SERVICE_LOOKUP_INTERVAL = (5 * SYNTRO_CLOCKS_PER_SEC);// 5 seconds interval between service lookup requests

	public static final	int SYNTRO_PRIMARY_SOCKET_LOCAL = 1661;			// socket for the primary Controller
	public static final	int SYNTRO_BACKUP_SOCKET_LOCAL = 1662;			// socket for the backup Controller

	//Component Directory Entry Tag Defs

	public static final String DETAG_CMP = "<CMP>";						// component DE start
	public static final String DETAG_CMP_END = "</CMP>";				// component DE end
	public static final	String DETAG_APPNAME = "NAM";					// the app name
	public static final	String DETAG_COMPTYPE = "TYP";					// a string identifying the component
	public static final	String DETAG_UID = "UID";						// the UID
	public static final	String DETAG_MSERVICE = "MSV";					// a string identifying a multicast service
	public static final	String DETAG_ESERVICE = "ESV";					// a string identifying an E2E service
	public static final	String DETAG_NOSERVICE = "NSV";					// an empty service slot

	//E2E service code for parameter deployment

	public static final	String DE_E2ESERVICE_PARAMS = "Params";			// parameter deployment service

	//Service type codes

	public static final	int SERVICETYPE_MULTICAST = 0;					// a multicast service
	public static final	int SERVICETYPE_E2E = 1;						// an end to end service

	//-------------------------------------------------------------------------------------------
	//	Syntro message types
	//

	//	HEARTBEAT
	//	This message which is just the SYNTROMSG itself is sent regular by both parties
	//	in a SyntroLink. It's used to ensure correct operation of the link and to allow
	//	the link to be re-setup if necessary. The message itself is a Hello data structure -
	//	the same as is sent on the Hello system.
	//
	//	The HELLO structure that forms the message may also be followed by a properly
	//	formatted directory entry (DE) as described above. If there is nothing present,
	//	this means that DE for the component hasn't changed. Otherwise, the DE is used by the
	//	receiving SyntroControl as the new DE for the component.

	public static final int SYNTROMSG_HEARTBEAT = 1;

	//	SERVICE_LOOKUP_REQUEST
	//	This message is sent by a Component to the SyntroControl in order to request
	//	a service lookup.

	public static final int SYNTROMSG_SERVICE_LOOKUP_REQUEST = 2;

	//	SERVICE_LOOKUP_RESPONSE
	//	This message is sent back to a component with the results of the lookup.
	//	The relevant fields are filled in the SYNTRO_SERVICE_LOOKUP structure.

	public static final int SYNTROMSG_SERVICE_LOOKUP_RESPONSE = 3;

	//	DIRECTORY_REQUEST
	//	An application can request a copy of the directory using this message.
	//	There are no parameters or data - the message is just a SYNTRO_MESSAGE

	public static final int SYNTROMSG_DIRECTORY_REQUEST = 4;

	//	DIRECTORY_RESPONSE
	//	This message is sent to an application in response to a request.
	//	The message consists of a SYNTRO_DIRECTORY_RESPONSE structure.

	public static final int SYNTROMSG_DIRECTORY_RESPONSE = 5;

	//	SERVICE_ACTIVATE
	//	This message is sent by a SyntroControl to an Endpoint multicast service when the Endpoint
	//	multicast should start generating data as someone has subscribed to its service.

	public static final int SYNTROMSG_SERVICE_ACTIVATE = 6;

	//	MULTICAST_FRAME
	//	Multicast frames are sent using this message. The data is the parameter

	public static final int SYNTROMSG_MULTICAST_MESSAGE = 16;

	//	MULTICAST_ACK
	//	This message is sent to acknowledge a multicast and request the next

	public static final int SYNTROMSG_MULTICAST_ACK = 17;

//	E2E - Endpoint to Endpoint message

	public static final int SYNTROMSG_E2E = 18;

	public static final int SYNTROMSG_MAX = 18;				// highest legal message value

	//	SYNTRO_MESSAGE - the structure that defines the object transferred across
	//	the SyntroLink - the component to component header
	
	// the command byte
	public static final int	SYNTRO_MESSAGE_CMD = 0;		
	
	// the length of the message (includes all bytes in the message) - UC4
	public static final int	SYNTRO_MESSAGE_TOTAL_LENGTH = 1;
	
	// the flags byte
	public static final int	SYNTRO_MESSAGE_FLAGS = 5;
	
	// a spare byte
	public static final int	SYNTRO_MESSAGE_SPARE = 6;
	
	// the checksum byte
	public static final int	SYNTRO_MESSAGE_CKSM = 7;

	public static final int	SYNTRO_MESSAGE_LENGTH = 8;	// number of bytes in message header
	
	//	SYNTRO_EHEAD - Endpoint header
	//	
	//	This is used to send messages between specific services within components.
	//	seq is used to control the acknowledgement window. It starts off at zero
	//	and increments with each new message. Acknowledgements indicate the next acceptable send
	//	seq and so open the window again.

	public final static int	SYNTRO_MAX_WINDOW = 4;			// the maximum number of outstanding messages

	public final static int SYNTRO_EHEAD_SRC_UID = 0;
	public final static int SYNTRO_EHEAD_DST_UID = SYNTRO_EHEAD_SRC_UID + SYNTRO_UID_LEN;
	public final static int SYNTRO_EHEAD_SRC_PORT = SYNTRO_EHEAD_DST_UID + SYNTRO_UID_LEN;
	public final static int SYNTRO_EHEAD_DST_PORT = SYNTRO_EHEAD_SRC_PORT + 2;
	public final static int SYNTRO_EHEAD_SEQ = SYNTRO_EHEAD_DST_PORT + 2;
	public final static int SYNTRO_EHEAD_PAR0 = SYNTRO_EHEAD_SEQ + 1;
	public final static int SYNTRO_EHEAD_PAR1 = SYNTRO_EHEAD_PAR0 + 1;
	public final static int SYNTRO_EHEAD_PAR2 = SYNTRO_EHEAD_PAR1 + 1;
	public final static int SYNTRO_EHEAD_LENGTH = SYNTRO_EHEAD_PAR2 + 1;
	
	//	Syntro message size maximums

	public static final	int SYNTRO_MESSAGE_MAX = 0x80000;				// pretty big! Needed for HD MJPEG video

	public static final	int SYNTROARM_MESSAGE_MAX = 1460;				// max size for ARM processors

	public static final	int SP_SYNTRO_MESSAGE_MAX = 192;				// maximum for small processors (not including SYNTRO_MESSAGE header or SYNTRO_EHEAD header)

	public static final int SYNTRO_PARAM_MAX = 128;						// parameter chunk size for config deployment

	//	SYNTROMESSAGE nFlags masks

	public static final	int SYNTROLINK_PRI = 0x03;						// bits 0 and 1 are priority bits

	public static final	int SYNTROLINK_EOM = 0x80;						// bit 7 set if the last in the sequence

	public static final	int SYNTROLINK_PRIORITIES = 4;					// four priority levels

	public static final	int SYNTROLINK_HIGHPRI = 0;						// highest priority - typically for real time control data
	public static final	int SYNTROLINK_MEDHIGHPRI = 1;
	public static final	int SYNTROLINK_MEDPRI = 2;
	public static final	int SYNTROLINK_LOWPRI = 3;						// lowest priority - typically for multicast information

	//	Config and Directory related definitions

	public static final	int SYNTRO_MAX_TAG = 256;						// maximum length of tag string (including 0 at end)
	public static final	int SYNTRO_MAX_NONTAG = 1024;					// max length of value (including 0 at end)

	public static final	int SP_SYNTRO_MAX_TAG = 32;						// maximum length of tag string for small processors
	public static final	int SP_SYNTRO_MAX_NONTAG = 32;					// max length of value string for small processors

	public static final int SYNTRO_MAX_NAME = 32;
	public static final	int SYNTRO_MAX_APPNAME = SYNTRO_MAX_NAME;		// max length of a zero-terminated app name
	public static final	int SYNTRO_MAX_APPTYPE = SYNTRO_MAX_NAME;		// max length of a zero-terminated app type
	public static final	int SYNTRO_MAX_COMPTYPE = SYNTRO_MAX_NAME;		// max length of a zero-terminated component type
	public static final	int SYNTRO_MAX_SERVNAME = SYNTRO_MAX_NAME;		// max length of a service name
	public static final	int SYNTRO_MAX_SERVPATH = 128;					// this is max size of the NULL terminated path for service paths

	//	SyntroCore Component Type defs

	public static final	String COMPNAME_CONTROL = "Control";			// the component name of SyntroControl
	public static final	String COMPNAME_EXEC = "Exec";					// the component name for SyntroExec

	public static final	String TAG_COMPNAME = "COMPNAME";				// standard component name field for configs
	public static final	String TAG_CONTROLNAME = "CONTROLNAME";			// standard control name field for configs

	//	Directory lookup codes
	
	public static final	int SERVICE_LOOKUP_FAIL = 0;					// not found
	public static final	int SERVICE_LOOKUP_SUCCEED = 1;					// found and response fields filled in
	public static final	int SERVICE_LOOKUP_REMOVE = 2;					// used to remove a lookup

	// The service request structure

	// the returned UID of the service
	public static int SYNTRO_SERVICEREQ_UID = 0;
	
	// the returned ID for this entry (to detect restarts requiring re-regs)
	public static int SYNTRO_SERVICEREQ_ID = SYNTRO_SERVICEREQ_UID + SYNTRO_UID_LEN;
	
	// the service path string to be looked up
	public static final int SYNTRO_SERVICEREQ_SERVPATH = SYNTRO_SERVICEREQ_ID + 4;

	// the returned port to use for the service - the remote index for the service
	public static final int SYNTRO_SERVICEREQ_REMOTEPORT = SYNTRO_SERVICEREQ_SERVPATH + SYNTRO_MAX_SERVPATH;
	
	// the returned component index on SyntroControl (for fast refreshes)
	public static final int SYNTRO_SERVICEREQ_COMPONENTINDEX = SYNTRO_SERVICEREQ_REMOTEPORT + 2;
	
	// the port number of the requestor - the local index for the service
	public static final int SYNTRO_SERVICEREQ_LOCALPORT = SYNTRO_SERVICEREQ_COMPONENTINDEX + 2;
	
	// the service type requested
	public static final int SYNTRO_SERVICEREQ_SERVICETYPE = SYNTRO_SERVICEREQ_LOCALPORT + 2;
	
	// the response code
	public static final int SYNTRO_SERVICEREQ_RESPONSE = SYNTRO_SERVICEREQ_SERVICETYPE + 1;

	// total length
	public static final int SYNTRO_SERVICEREQ_LENGTH = SYNTRO_SERVICEREQ_RESPONSE + 1;
		
	//	Reg request codes
	
	public static final	int REG_REQUEST_FAIL = 0;						// fail code in the request
	public static final	int REG_REQUEST_SUCCEED = 1;					// if it succeeded
	
	// The service activate structure
	
	// the target service port
	public static final int SYNTRO_SERVICEACTIVATE_LOCALPORT = 0;
	
	// the returned component index
	public static final int SYNTRO_SERVICEACTIVATE_INDEX = 2;
	
	// the port to use to send stuff to SyntroControl
	public static final int SYNTRO_SERVICEACTIVATE_CONTROLPORT = 4;
	
	// the response code
	public static final int SYNTRO_SERVICEACTIVATE_RESPONSE = 6;
	
	// the length
	public static final int SYNTRO_SERVICEACTIVATE_LENGTH = 7;
	
//	Standard multicast stream names

	public static final String SYNTRO_STREAMNAME_AVMUX = "avmux";
	public static final String SYNTRO_STREAMNAME_AVMUXLR = "avmux:lr";
	public static final String SYNTRO_STREAMNAME_AVMUXMOBILE = "avmuxmobile";
	public static final String SYNTRO_STREAMNAME_THUMBNAIL = "thumbnail";
	public static final String SYNTRO_STREAMNAME_VIDEO = "video";
	public static final String SYNTRO_STREAMNAME_VIDEOLR = "video:lr";
	public static final String SYNTRO_STREAMNAME_AUDIO = "audio";
	public static final String SYNTRO_STREAMNAME_NAV = "nav";
	public static final String SYNTRO_STREAMNAME_LOG = "log";
	public static final String SYNTRO_STREAMNAME_SENSOR = "sensor";
	public static final String SYNTRO_STREAMNAME_TEMPERATURE = "temperature";
	public static final String SYNTRO_STREAMNAME_HUMIDITY = "humidity";
	public static final String SYNTRO_STREAMNAME_LIGHT = "light";
	public static final String SYNTRO_STREAMNAME_MOTION = "motion";
	public static final String SYNTRO_STREAMNAME_AIRQUALITY = "airquality";
	public static final String SYNTRO_STREAMNAME_PRESSURE = "pressure";
	public static final String SYNTRO_STREAMNAME_ACCELEROMETER = "accelerometer";
	public static final String SYNTRO_STREAMNAME_ZIGBEE_MULTICAST = "zbmc";
	public static final String SYNTRO_STREAMNAME_HOMEAUTOMATION = "ha";
	
	// Syntro record header
	
	public static final int SYNTRO_RECORD_HEADER_TYPE = 0;		// type of record
	public static final int SYNTRO_RECORD_HEADER_SUBTYPE = 2;	// a subtype for the record
	public static final int SYNTRO_RECORD_HEADER_HEADERLENGTH = 4;	// total length of specific header
	public static final int SYNTRO_RECORD_HEADER_PARAM = 6;		// a parameter
	public static final int SYNTRO_RECORD_HEADER_PARAM1 = 8;	// a parameter
	public static final int SYNTRO_RECORD_HEADER_PARAM2 = 10;	// a parameter
	public static final int SYNTRO_RECORD_HEADER_RECORDINDEX = 12; // unique index of record
	public static final int SYNTRO_RECORD_HEADER_TIMESTAMP = 16;// record timestamp
	public static final int SYNTRO_RECORD_HEADER_LENGTH = 24;	// total length of record header
	
	// Record header type codes
	
	public static final int SYNTRO_RECORD_TYPE_VIDEO = 0;		// a video record
	public static final int SYNTRO_RECORD_TYPE_AUDIO = 1;		// an audio record
	public static final int SYNTRO_RECORD_TYPE_NAV = 2;			// navigation data
	public static final int SYNTRO_RECORD_TYPE_LOG = 3;			// log data
	public static final int SYNTRO_RECORD_TYPE_SENSOR = 4;		// multiplexed sensor data
	public static final int SYNTRO_RECORD_TYPE_TEMPERATURE = 5;	// temperature sensor
	public static final int  SYNTRO_RECORD_TYPE_HUMIDITY = 6;	// humidity sensor
	public static final int SYNTRO_RECORD_TYPE_LIGHT = 7;		// light sensor
	public static final int SYNTRO_RECORD_TYPE_MOTION = 8;		// motion detection events
	public static final int SYNTRO_RECORD_TYPE_AIRQUALITY = 9;	// air quality sensor
	public static final int SYNTRO_RECORD_TYPE_PRESSURE = 10;	// air pressure sensor
	public static final int SYNTRO_RECORD_TYPE_ZIGBEE = 11;		// zigbee multicast data
	public static final int SYNTRO_RECORD_TYPE_AVMUX = 12;		// an avmux stream record

	public static final int SYNTRO_RECORD_TYPE_USER = 0x8000;	// user defined codes start here

	
}





