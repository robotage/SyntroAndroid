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

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.rt.syntro.lib.SyntroParams;
import com.rt.syntro.lib.SyntroUtils;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;


public class SfGService extends Service {

    private static final String TRACE_TAG = "SfGService";
    private static final String LIVE_CARD_TAG = "syntro";

    private VideoSurface videoSurface;
    private CameraSurface cameraSurface;

    private LiveCard videoLiveCard;
    private LiveCard cameraLiveCard;
    
    private PowerManager pm;
    private PowerManager.WakeLock wl;
       
    SfGClient client = null;
    
    SfGService myThis;

    public class SfGBinder extends Binder {
        public VideoView getView() {
            return videoSurface.getVideoView();
        }
        
        public CameraSurface getCameraSurface() {
            return cameraSurface;
        }
          
        public ArrayList<String> getSources() {
        	return client.getSources();
        }
        
        public void newSource(String source) {
    	    Message msg = Message.obtain();
    		Bundle bundle = new Bundle();
    		bundle.putString("", source);
    		msg.setData(bundle);
    		client.getHandler().sendMessage(msg);     	
        }
    }
    private final SfGBinder binder = new SfGBinder();
   
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate() {
        super.onCreate();
        myThis = this;
        client = null;
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TAG");
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.d(TRACE_TAG, "onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//    	android.os.Debug.waitForDebugger();
        
    	if (client == null) {
    		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    		SyntroParams params = new SyntroParams();
    		SyntroUtils.setName("", params.controlname, 0);
    		SyntroUtils.setName("SfG", params.appType, 0);
    		SyntroUtils.setName("Glass", params.appName, 0);
    		SyntroUtils.setName("View", params.compType, 0);
      		client = new SfGClient(wifi, 2, params);
    	}

      	if (cameraLiveCard == null) {
            Log.d(TRACE_TAG, "Publishing Camera LiveCard");
            cameraLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            cameraSurface = new CameraSurface(this, client);
            cameraLiveCard.setDirectRenderingEnabled(true);
            cameraLiveCard.getSurfaceHolder().addCallback(cameraSurface);
            Intent cameraMenuIntent = new Intent(this, CameraMenu.class);
            cameraMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            cameraLiveCard.setAction(PendingIntent.getActivity(this, 0, cameraMenuIntent, 0));
            cameraLiveCard.attach(this);
            cameraLiveCard.publish(PublishMode.REVEAL);
            
            
            videoSurface = new VideoSurface(this, client);

          	client.setImageHandler(videoSurface.getVideoView().getImageHandler());
       
            Log.d(TRACE_TAG, "Publishing Video LiveCard");
            videoLiveCard = new LiveCard(this, "wibble");
            videoLiveCard.setDirectRenderingEnabled(true);
            videoLiveCard.getSurfaceHolder().addCallback(videoSurface);
            Intent videoMenuIntent = new Intent(this, VideoMenu.class);
            videoMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            videoLiveCard.setAction(PendingIntent.getActivity(this, 0, videoMenuIntent, 0));
            videoLiveCard.attach(this);
            videoLiveCard.publish(PublishMode.REVEAL);

            Log.d(TRACE_TAG, "Done publishing LiveCard");
        } 
        wl.acquire();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	wl.release();
        if (client != null) {
        	client.exitThread();
        	client = null;
        }
        
        if (cameraLiveCard != null && cameraLiveCard.isPublished()) {
            Log.d(TRACE_TAG, "Unpublishing Camera LiveCard");
            cameraLiveCard.unpublish();
            if (cameraSurface != null) {
                cameraLiveCard.getSurfaceHolder().removeCallback(cameraSurface);
            }
            cameraLiveCard = null;
        }
        
        if (videoLiveCard != null && videoLiveCard.isPublished()) {
            Log.d(TRACE_TAG, "Unpublishing Video LiveCard");
            videoLiveCard.unpublish();
            if (videoSurface != null) {
                videoLiveCard.getSurfaceHolder().removeCallback(videoSurface);
            }
            videoLiveCard = null;
        }
        
       super.onDestroy();
    }
}
