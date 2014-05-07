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

package com.rt.syntronavview;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.rt.syntro.lib.DirectoryEntry;
import com.rt.syntro.lib.Endpoint;
import com.rt.syntro.lib.SyntroDefs;
import com.rt.syntro.lib.SyntroMessage;
import com.rt.syntro.lib.SyntroParams;
import com.rt.syntro.lib.SyntroUtils;
import com.rt.nav.SyntroNavData;

public class NavViewClient extends Endpoint {
	
	public static final String TAG = "NavViewClient";
	
	//	message codes
	
	public static final int NAVVIEWCLIENT_NEWDATA = 0;		
	public static final int NAVVIEWCLIENT_NEWDIRECTORY = 1;		
	public static final int NAVVIEWCLIENT_LINKCLOSED = 2;		
	public static final int NAVVIEWCLIENT_LINKCONNECTED = 3;		
	
	public static final int NAVVIEWCLIENT_DIRECTORY_INTERVAL = 5 * SyntroDefs.SYNTRO_CLOCKS_PER_SEC;
	
	private Handler parentHandler;
	private  Bitmap latestFrame = null;
	private long lastDirectoryTime;
	private ArrayList<String> sources = new ArrayList<String>();
	private SyntroNavData navData = new SyntroNavData();
	private boolean waitingForDataAck = false;
	
    private int streamPort = -1;

	public NavViewClient(WifiManager wifi, int instance, SyntroParams params, 
				Handler parentHandler) {
		super(wifi, instance, params);
		this.parentHandler = parentHandler;
	}
	public Handler getHandler() {
		return newStreamHandler;
	}
	
	synchronized public SyntroNavData getNavData() {
		waitingForDataAck = false;
		return navData;
	}
	
	synchronized Bitmap getLatestFrame() {
		return latestFrame;
	}
	
	synchronized void setLatestFrame(Bitmap bitmap) {
		latestFrame = bitmap;
	}
	
	@SuppressWarnings("unchecked")
	synchronized public ArrayList<String> getSources() {
		return (ArrayList<String>)sources.clone();
	}
	
	synchronized private void setSources(String[] dirList)
	{
		DirectoryEntry de = new DirectoryEntry();
		String servicePath = new String();
		String[] serviceSourceName = new String[2];

		sources = new ArrayList<String>();
				
		for (int entry = 0; entry < dirList.length; entry++) {
			de.setLine(dirList[entry]);

			if (!de.isValid())
				continue;
			
			ArrayList<String> services = de.multicastServices();

			for (int i = 0; i < services.size(); i++) {
				servicePath = de.appName() + SyntroDefs.SYNTRO_SERVICEPATH_SEP + services.get(i);

				SyntroUtils.removeStreamNameFromPath(servicePath, serviceSourceName);
		 
				if (serviceSourceName[1].equals(SyntroDefs.SYNTRO_STREAMNAME_NAV))
					sources.add(serviceSourceName[0]);
			}		
		}
//		Log.i(TAG, "Sources");
//		for (int i = 0; i < sources.size(); i++) {
//			Log.i(TAG, sources.get(i));
//		}
	}
	
	protected void appClientInit() {
		lastDirectoryTime = SystemClock.elapsedRealtime();
	}
	
	protected void appClientExit() {
	}
	

	protected void appClientBackground() {
		long now = SystemClock.elapsedRealtime();
		
		if (clientIsConnected() && ((now - lastDirectoryTime) >= NAVVIEWCLIENT_DIRECTORY_INTERVAL)) {
			clientRequestDirectory();
			lastDirectoryTime = now;
		}
	}
	
	synchronized protected void appClientReceiveMulticast(int servicePort, SyntroMessage message)
	{		
		if (!navData.crackNavData(message))
			return;
		if (!waitingForDataAck) {
			parentHandler.sendEmptyMessage(NAVVIEWCLIENT_NEWDATA);
			waitingForDataAck = true;
		}
		
		clientSendMulticastAck(servicePort, navData.seqno);
	}

	
	protected void appClientReceiveDirectory(String[] dirList)
	{
		setSources(dirList);
		parentHandler.sendEmptyMessage(NAVVIEWCLIENT_NEWDIRECTORY);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler newStreamHandler = new Handler() {
	    	
	 	@Override
	   	public void handleMessage(Message msg) {
	 		if (streamPort != -1) {
	 			clientRemoveService(streamPort);
	 			streamPort = -1;
	 		}
	 		String streamName = msg.getData().getString("");
	 		Log.d(TRACE_TAG, "New stream name " + streamName);
	 		if (streamName.length() > 0) {
	 			streamPort = clientAddService(
	 					SyntroUtils.insertStreamNameInPath(streamName, 
	 							SyntroDefs.SYNTRO_STREAMNAME_NAV), 
	 							SyntroDefs.SERVICETYPE_MULTICAST, false, true);
	 		}
	 	}
	};
	
	protected void appClientConnected() {
		parentHandler.sendEmptyMessage(NAVVIEWCLIENT_LINKCONNECTED);
	}
	
	
	protected void appClientClosed() {
		parentHandler.sendEmptyMessage(NAVVIEWCLIENT_LINKCLOSED);
	}
}
