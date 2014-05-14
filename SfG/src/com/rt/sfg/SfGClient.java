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

package com.rt.sfg;

import java.util.ArrayList;
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

public class SfGClient extends Endpoint {
	
	public static final int VIEWCLIENT_DIRECTORY_INTERVAL = 5 * SyntroDefs.SYNTRO_CLOCKS_PER_SEC;
	
	private long lastDirectoryTime;
	private ArrayList<String> sources = new ArrayList<String>();
	private SyntroAV av = new SyntroAV();
	
	private int videoPort;
	private int cameraPort;
	
	private byte[] latestCameraFrame = null;
	private long latestCameraFrameTimestamp;
	private SyntroAV cameraParams;
	
	private  Bitmap latestFrame = null;
	
	private Handler imageHandler = null;
	
	private boolean videoEnabled = true;
	
    private SyntroAudioWriter audioWriter = null;

    private int streamPort = -1;

	public SfGClient(WifiManager wifi, int instance, SyntroParams params) {
		super(wifi, instance, params);
	}
	
	public Handler getHandler() {
		return newStreamHandler;
	}

	public void setImageHandler(Handler handler) {
		imageHandler = handler;
	}
	
	synchronized public Bitmap getLatestFrame() {
		Bitmap lf = latestFrame;
		latestFrame = null;
		return lf;
	}
	
	synchronized private void setLatestFrame(Bitmap bitmap) {
		latestFrame = bitmap;
	}
	
	private void setLatestAudio(byte[] data, int offset, int length, int sampleRate, int sampleSize, int channels) {
		SyntroAudioData audioData = new SyntroAudioData();
		audioData.setAudioData(data,  offset,  length,  sampleRate,  sampleSize,  channels);
		
		if (audioWriter != null) {
			audioWriter.addAudioData(audioData);
		}

	}
	
	synchronized public void enableVideo(boolean enable) {
		videoEnabled = enable;
	}
	
	synchronized public void setCameraParams(SyntroAV params) {
		cameraParams = params;
	}

	synchronized public void sendCameraFrame(byte[] cameraFrame) {
		latestCameraFrame = cameraFrame;
		latestCameraFrameTimestamp = System.currentTimeMillis();
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
		videoPort = -1;
		cameraPort = clientAddService(SyntroDefs.SYNTRO_STREAMNAME_AVMUX, SyntroDefs.SERVICETYPE_MULTICAST, true, true);
	}
	
	protected void appClientExit() {
		if (videoPort != -1) {
			clientRemoveService(videoPort);
			videoPort = -1;
		}
		clientRemoveService(cameraPort);
		if (audioWriter != null)
			audioWriter.exitThread();
		audioWriter = null;
	}
	
	synchronized protected void appClientBackground() {
		long now = SystemClock.elapsedRealtime();
		
		if (clientIsConnected() && ((now - lastDirectoryTime) >= VIEWCLIENT_DIRECTORY_INTERVAL)) {
			clientRequestDirectory();
			lastDirectoryTime = now;
		}
		
		if ((latestCameraFrame != null) && clientIsConnected() && clientIsServiceActive(cameraPort)) {
			int totalLength = SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + SyntroAV.SYNTRO_RECORD_AVMUX_LENGTH +
					latestCameraFrame.length;
			SyntroMessage message = clientBuildMessage(cameraPort, totalLength);
			cameraParams.avmuxSize = 0;
			cameraParams.videoSize = latestCameraFrame.length;
			cameraParams.audioSize = 0;
			cameraParams.timestamp = latestCameraFrameTimestamp;
			cameraParams.buildAvmux(message, SyntroAV.SYNTRO_RECORDHEADER_PARAM_NORMAL);
			System.arraycopy(latestCameraFrame, 0, message.get(), SyntroDefs.SYNTRO_MESSAGE_LENGTH +
					SyntroDefs.SYNTRO_EHEAD_LENGTH + SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + SyntroAV.SYNTRO_RECORD_AVMUX_LENGTH,
					latestCameraFrame.length);
			clientSendMessage(cameraPort, message, SyntroDefs.SYNTROLINK_LOWPRI);
			latestCameraFrame = null;
		}
	}
	
	protected void appClientReceiveMulticast(int servicePort, SyntroMessage message)
	{	
		if (!videoEnabled)
			return;
		if (!av.crackAvmux(message))
			return;
		
		if ((latestFrame == null) && (av.videoSize > 0)) {
			setLatestFrame(BitmapFactory.decodeByteArray(message.get(), av.videoOffset, av.videoSize));
			if (imageHandler != null)
				imageHandler.sendEmptyMessage(0);
		}
		if (av.audioSize > 0) {
			setLatestAudio(message.get(), av.audioOffset, av.audioSize, av.audioSampleRate, av.audioSampleSize, av.audioChannels);
		}
		clientSendMulticastAck(servicePort, av.seqno);
	}
	
	protected void appClientReceiveDirectory(String[] dirList)
	{
		setSources(dirList);
	}
	
	protected void appClientReceiveMulticastAck(int servicePort, SyntroMessage message) {
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
}
