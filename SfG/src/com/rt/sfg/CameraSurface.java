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

import java.io.ByteArrayOutputStream;
import java.util.List;
import com.rt.syntro.lib.SyntroAV;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraSurface implements SurfaceHolder.Callback {
    private static final String TRACE_TAG = "CameraSurface";

    public static final int GLASS_WIDTH = 640;
    public static final int GLASS_HEIGHT = 360;
    
    private SurfaceHolder holder;
    
    private Camera camera;
    
    private SyntroAV avParams = new SyntroAV();
    
    private boolean enabled = false;
    
    private Camera.Parameters parameters;
   	
    private Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    SfGClient client;
    
    public CameraSurface(Context context, SfGClient client) {
    	this.client = client;
    	
    	avParams.audioChannels = 1;
    	avParams.audioSampleRate = 8000;
    	avParams.audioSampleSize = 16;
    	avParams.audioSubtype = SyntroAV.SYNTRO_RECORD_TYPE_AUDIO_PCM;
    	avParams.avmuxSubtype = SyntroAV.SYNTRO_RECORD_TYPE_AVMUX_MJPPCM;
    	avParams.videoSubtype = SyntroAV.SYNTRO_RECORD_TYPE_VIDEO_MJPEG;
    	avParams.videoFramerate = 10;
    	avParams.videoHeight = GLASS_HEIGHT;
    	avParams.videoWidth = GLASS_WIDTH;
  
    	client.setCameraParams(avParams);
    	
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(50);
        paintText.setStyle(Style.FILL);
 //       paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

    }
    
    public boolean isCameraEnabled() {
    	return enabled;
    }
    
    private boolean startCamera() {
    	if (enabled)
    		stopCamera();
    	
    	try {
    		camera = Camera.open(0);
    		parameters = camera.getParameters();
    					   					
//       	displayPictureSizes();
//    		displayPreviewSizes();
//    		displayFpsRanges();

    		parameters.setPictureSize(1280, 720);
    		parameters.setPreviewSize(GLASS_WIDTH, GLASS_HEIGHT);
    		parameters.setPreviewFpsRange(5000, 5000);
    					
    		camera.setParameters(parameters);
 
    		holder.setKeepScreenOn(true);
			camera.setPreviewDisplay(holder);
			     					
    		camera.setPreviewCallback(new Camera.PreviewCallback() {
    			public void onPreviewFrame(byte[] data, Camera camera) {
    				YuvImage image = new YuvImage(data, ImageFormat.NV21, 640, 360, null);
    				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    				image.compressToJpeg(new Rect(0, 0, GLASS_WIDTH, GLASS_HEIGHT), 70, outputStream);
    				client.sendCameraFrame(outputStream.toByteArray());
    			}
    		});
    					
    		camera.startPreview();
    		Log.d(TRACE_TAG, "Camera open");
    	} catch (Exception e) {
    		Log.e(TRACE_TAG, "Failed to open camera: " + e.getMessage());
    		if (camera != null)
    			camera.release();
    		camera = null;
    		return false;
    	}
    	enabled = true;
    	return true;
    }
    		
    private void stopCamera() {
    	if (!enabled)
    		return;
    	
    	enabled = false;
    	
    	if (camera == null)
    		return;
 
    	try {
    		camera.stopPreview();
    		camera.setPreviewCallback(null);
    		camera.setPreviewDisplay(null);
    		camera.release();
    	} catch (Exception e) {
    		Log.e(TRACE_TAG, "Camera shutdown exception " + e);
    	}
 		camera = null;
    	Log.i(TRACE_TAG, "Camera stopped");
    	
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TRACE_TAG, "Surface created");
        this.holder = holder;
        startCamera();
    }
        
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TRACE_TAG, "Surface destroyed");
        stopCamera();
        holder = null;
    }
    
    @SuppressWarnings("unused")
    private void displayPictureSizes() {
    	List<Camera.Size> list = parameters.getSupportedPictureSizes();
    	
    	for (int i = 0; i < list.size(); i++) {
    		Log.d(TRACE_TAG, "Picture size: " + list.get(i).width + " " + list.get(i).height);
    	}
    }
    
    @SuppressWarnings("unused")
    private void displayPreviewSizes() {
    	List<Camera.Size> list = parameters.getSupportedPreviewSizes();
    	
    	for (int i = 0; i < list.size(); i++) {
    		Log.d(TRACE_TAG, "Preview size: " + list.get(i).width + " " + list.get(i).height);
    	}
    }
    
    @SuppressWarnings("unused")
	private void displayFpsRanges() {
    	List<int[]> list = parameters.getSupportedPreviewFpsRange();
    	
    	for (int i = 0; i < list.size(); i++) {
    		Log.d(TRACE_TAG, "Preview fps: " + list.get(i)[0] + " " + list.get(i)[1]);
    	}
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}
}
