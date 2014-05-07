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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.rt.androidgl.ARVector3;
import com.rt.androidgl.AndroidGL;
import com.rt.nav.SyntroNavData;
import com.rt.vrwidget.VRIMUWidget;
import com.rt.vrwidget.VRPlaneWidget;
import com.rt.vrwidget.VRWidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class NavViewRenderer implements GLSurfaceView.Renderer {
	public static String TAG = "NavViewRenderer";
	
	private static final int textSize = 40;
	private static final int lineHeight = 80;
	// Message codes
	
	public static final int VRRENDERER_MESSAGE_IMU_UPDATE 	= 0;
	
	public AndroidGL agl;

	private VRIMUWidget imuWidget;
	private VRPlaneWidget statusWidget;
	
	private GLSurfaceView display;
	
	private Context context;
	private Paint paint = new Paint();
	private Canvas statusCanvas;
	private boolean running = false;
	
	ARVector3 IMUPose = new ARVector3();

	public NavViewRenderer(Context context, GLSurfaceView glDisplay) {
		display = glDisplay;
		this.context = context;
		
		paint = new Paint();
		paint.setColor(0xffffff00);
		paint.setTextSize(textSize);
		paint.setStyle(Paint.Style.FILL);
    }
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d(TAG,"onSurfaceCreated");
		agl = new AndroidGL(context);

		agl.addLight(new ARVector3(20.0f, 5.0f, 0.0f), new ARVector3(0.2f, 0.2f, 0.2f),
				new ARVector3(0.2f, 0.2f, 0.2f), new ARVector3(0.4f, 0.4f, 0.4f));

		agl.addLight(new ARVector3(-20.0f, 5.0f, 0.0f), new ARVector3(0.0f, 0.0f, 0.0f),
				new ARVector3(0.2f, 0.2f, 0.2f), new ARVector3(0.8f, 0.8f, 0.8f));

		agl.addLight(new ARVector3(0.0f, 0.0f, 0.0f), new ARVector3(0.0f, 0.0f, 0.0f),
				new ARVector3(0.2f, 0.2f, 0.2f), new ARVector3(0.5f, 0.5f, 0.5f));
	
		agl.addTexture(R.raw.redshade, "RedShade");
		agl.addTexture(R.raw.greenshade, "GreenShade");
		agl.addTexture(R.raw.blueshade, "BlueShade");
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		agl.setViewport(size.x, size.y);
		
		imuWidget = new VRIMUWidget(agl);
		imuWidget.setRotation(new ARVector3(10.0f, 10.0f, 10.0f));
		imuWidget.VRWidgetInit();
		
		statusWidget = new VRPlaneWidget(agl);
		statusWidget.setSize(4, 4, 1);
		statusWidget.setCenter(0, 4, VRWidget.DEFAULT_Z);
		statusWidget.VRWidgetInit();
		statusCanvas = statusWidget.getCanvas();

		statusCanvas.drawARGB(255, 0, 0, 40);
		statusCanvas.drawText("Waiting for data...", 20, lineHeight, paint);
		statusWidget.updateBitmap();
		
    	Log.d(TAG, "leaving onSurfaceCreated");
    	running = true;
	}
		
	@Override
	synchronized public void onDrawFrame(GL10 gl) {
		
		
        GLES20.glClearColor(0.2f, 0.0f, 0.4f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
  //      GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        imuWidget.VRWidgetRender();
        statusWidget.VRWidgetRender();
 	}
	
	@Override
	synchronized public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG,"onSurfaceChanged");

		agl.setViewport(width, height);
		
		if (height > width) {
			imuWidget.setCenter(0, -3, VRWidget.DEFAULT_Z);
			statusWidget.setCenter(0, 4, VRWidget.DEFAULT_Z);
		} else {
			imuWidget.setCenter(3, 0, VRWidget.DEFAULT_Z);
			statusWidget.setCenter(-4, 0, VRWidget.DEFAULT_Z);
		}

		Log.d(TAG, "leaving onSurfaceChanged");
	}

	@SuppressLint("DefaultLocale")
	public synchronized void newData(SyntroNavData data) {
		String string;
		int ypos = lineHeight;
		
		if (!running)
			return;
		
		updateIMUWidget(data.fusionPose);
		
		statusCanvas.drawARGB(255, 0, 0, 80);
		statusCanvas.drawText("Fusion pose:", 20, ypos, paint);
		ypos += lineHeight;
		string = String.format("Roll   = %7.4f", data.fusionPose.vector[0] * AndroidGL.RAD_TO_DEGREE);
		statusCanvas.drawText(string, 50, ypos, paint);
		ypos += lineHeight;
		string = String.format("Pitch = %7.4f", data.fusionPose.vector[1] * AndroidGL.RAD_TO_DEGREE);
		statusCanvas.drawText(string, 50, ypos, paint);
		ypos += lineHeight;
		string = String.format("Yaw   = %7.4f", data.fusionPose.vector[2] * AndroidGL.RAD_TO_DEGREE);
		statusCanvas.drawText(string, 50, ypos, paint);

		statusWidget.updateBitmap();
	}
	
	private void updateIMUWidget(ARVector3 pose) {
		imuWidget.setRotation(AndroidGL.RAD_TO_DEGREE * pose.x(), 
				-AndroidGL.RAD_TO_DEGREE * (pose.z()),
				AndroidGL.RAD_TO_DEGREE * pose.y());
		display.requestRender();
	}
}
