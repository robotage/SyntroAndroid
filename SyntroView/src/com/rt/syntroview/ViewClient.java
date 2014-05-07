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

package com.rt.syntroview;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.rt.syntro.lib.DirectoryEntry;
import com.rt.syntro.lib.Endpoint;
import com.rt.syntro.lib.SyntroAV;
import com.rt.syntro.lib.SyntroAudioData;
import com.rt.syntro.lib.SyntroAudioWriter;
import com.rt.syntro.lib.SyntroDefs;
import com.rt.syntro.lib.SyntroMessage;
import com.rt.syntro.lib.SyntroParams;
import com.rt.syntro.lib.SyntroUtils;

public class ViewClient extends Endpoint {
	
	public static final String TRACE_TAG = "ViewClient";

	//	message codes
	
	public static final int VIEWCLIENT_NEWFRAME = 0;		
	public static final int VIEWCLIENT_NEWDIRECTORY = 1;		
	public static final int VIEWCLIENT_LINKCLOSED = 2;		
	public static final int VIEWCLIENT_LINKCONNECTED = 3;		

	public static final int VIEWCLIENT_DIRECTORY_INTERVAL = 5 * SyntroDefs.SYNTRO_CLOCKS_PER_SEC;
	
	private Handler parentHandler;
	private  Bitmap latestFrame = null;
	private long lastDirectoryTime;
	private ArrayList<String> sources = new ArrayList<String>();
	private SyntroAV av = new SyntroAV();
	
    private SyntroAudioWriter audioWriter = null;
    
    private int streamPort = -1;

	private LinkedBlockingQueue<SyntroAudioData> audioQueue = new LinkedBlockingQueue<SyntroAudioData>();
	
	public ViewClient(WifiManager wifi, int instance, SyntroParams params, Handler handler) {
		super(wifi, instance, params);
		this.parentHandler = handler;
	}
	public Handler getHandler() {
		return newStreamHandler;
	}
	
	synchronized Bitmap getLatestFrame() {
		return latestFrame;
	}
	
	synchronized void setLatestFrame(Bitmap bitmap) {
		latestFrame = bitmap;
	}
	
	synchronized SyntroAudioData getLatestAudio() {
		return audioQueue.poll();
	}
	
	private void setLatestAudio(byte[] data, int offset, int length, int sampleRate, int sampleSize, int channels) {
		SyntroAudioData audioData = new SyntroAudioData();
		audioData.setAudioData(data,  offset,  length,  sampleRate,  sampleSize,  channels);
		
		if (audioWriter != null) {
			audioWriter.addAudioData(audioData);
		}

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
		 
				if (serviceSourceName[1].equals(SyntroDefs.SYNTRO_STREAMNAME_AVMUX))
					sources.add(serviceSourceName[0]);
			}		
		}
//		Log.i(TRACE_TAG, "Sources");
//		for (int i = 0; i < sources.size(); i++) {
//			Log.i(TRACE_TAG, sources.get(i));
//		}
	}
	
	protected void appClientInit() {
		audioWriter = new SyntroAudioWriter();
		lastDirectoryTime = SystemClock.elapsedRealtime();
	}
	
	protected void appClientExit() {
		if (audioWriter != null)
			audioWriter.exitThread();
		audioWriter = null;
	}
	

	protected void appClientBackground() {
		long now = SystemClock.elapsedRealtime();
		
		if (clientIsConnected() && ((now - lastDirectoryTime) >= VIEWCLIENT_DIRECTORY_INTERVAL)) {
			clientRequestDirectory();
			lastDirectoryTime = now;
		}
	}
	
	protected void appClientReceiveMulticast(int servicePort, SyntroMessage message)
	{		
		if (!av.crackAvmux(message))
			return;
		
		if (av.videoSize > 0) {
			setLatestFrame(BitmapFactory.decodeByteArray(message.get(), av.videoOffset, av.videoSize));
			parentHandler.sendEmptyMessage(VIEWCLIENT_NEWFRAME);
		}
		if (av.audioSize > 0) {
			setLatestAudio(message.get(), av.audioOffset, av.audioSize, av.audioSampleRate, av.audioSampleSize, av.audioChannels);
		}
		clientSendMulticastAck(servicePort, av.seqno);
	}

	
	protected void appClientReceiveDirectory(String[] dirList)
	{
		setSources(dirList);
		parentHandler.sendEmptyMessage(VIEWCLIENT_NEWDIRECTORY);
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
	 							SyntroDefs.SYNTRO_STREAMNAME_AVMUX), 
	 							SyntroDefs.SERVICETYPE_MULTICAST, false, true);
	 		}
	 	}
	};
	
	protected void appClientConnected() {
		parentHandler.sendEmptyMessage(VIEWCLIENT_LINKCONNECTED);
	}
	
	
	protected void appClientClosed() {
		parentHandler.sendEmptyMessage(VIEWCLIENT_LINKCLOSED);
	}
}
