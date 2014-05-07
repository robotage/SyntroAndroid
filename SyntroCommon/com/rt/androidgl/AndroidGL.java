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

package com.rt.androidgl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class AndroidGL {
	public static final String TAG = "AndroidGL";
	
	public static final float PI = 3.1415926535f;
	public static final float DEGREE_TO_RAD = (PI / 180.0f);
	public static final float RAD_TO_DEGREE = (180.0f / PI);
	
	public static final int MAX_LIGHTS = 4;
	
	// Application variables
	
	public Context context;									// the application context
	
	//	Viewport related variables
	
	public float[] modelViewMatrix = new float[16];			// the model view matrix
	public float[] projectionMatrix = new float[16];		// the projection matrix
	public float[] modelViewProjectionMatrix = new float[16]; // the model view projection matrix
	public float[] normalMatrix44 = new float[16];		    // the normal matrix (4 x 4)
	public float[] normalMatrix = new float[9];			    // the normal matrix (3 x 3)
	public float viewportAspect;							// viewport aspect ratio
	public float viewportFieldOfView;						// angular field of view in x direction
	public float tanViewportFOV;							// tangent of the field of view
	public float width;										// width of the viewport
	public float height;									// height of the viewport
	public float halfWidth;									// half the viewport width;
	public float halfHeight;								// half the viewport height
	public float nearPlane;									// where the near plane is
	public float farPlane;									// where the far plane is
	
	//	Light source related variables
	
	public int lightCount;									// number of active light sources
	public float[] lightPosition = new float[4 * MAX_LIGHTS];
	public float[] lightAmbient = new float[3 * MAX_LIGHTS];
	public float[] lightDiffuse = new float[3 * MAX_LIGHTS];
	public float[] lightSpecular = new float[3 * MAX_LIGHTS];
	
	//	AndroidGLShader related variables
	
	public AndroidGLShader[] shaders = new AndroidGLShader[AndroidGLShader.SHADER_COUNT];	
	private ArrayList<VRTexture> textureList = new ArrayList<VRTexture>();	// the texture list

	
	
	public AndroidGL(Context context) {
		lightCount = 0;
		shaders[AndroidGLShader.SHADER_FLAT] = new FlatShader(this);
		shaders[AndroidGLShader.SHADER_ADS] = new ADSShader(this);
		shaders[AndroidGLShader.SHADER_TEXTURE] = new TextureShader(this);
		shaders[AndroidGLShader.SHADER_ADSTEXTURE] = new ADSTextureShader(this);
		nearPlane = 1.0f;
		farPlane = 1000.0f;
		viewportFieldOfView = 45 * DEGREE_TO_RAD;
		tanViewportFOV = (float) Math.tan(viewportFieldOfView / 2.0f);
		this.context = context;
    }

	public int addTexture(int textureResourceID, String name) {
		VRTexture vrt = new VRTexture();
		
		vrt.textureName = name;
		
		Resources res = context.getResources();
		InputStream textureStream = res.openRawResource(textureResourceID);
		Bitmap textureBitmap;
		try {
			textureBitmap = BitmapFactory.decodeStream(textureStream);
		} finally {
			try {
				textureStream.close();
			} catch (IOException e) {
				
			}
		}

		int[] tid = new int[1];
		GLES20.glGenTextures(1, tid, 0);
		vrt.textureID = tid[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vrt.textureID);
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
		textureBitmap.recycle();
		textureList.add(vrt);
		return vrt.textureID;
	}
	
	public int addTexture(Bitmap bitmap, String name) {
		VRTexture vrt = new VRTexture();
		
		vrt.textureName = name;
		
		int[] tid = new int[1];
		GLES20.glGenTextures(1, tid, 0);
		vrt.textureID = tid[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vrt.textureID);
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		textureList.add(vrt);
		return vrt.textureID;
	}
	
	public void replaceTexture(Bitmap bitmap, int textureID) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);		
	}
	
	public int lookupTexture(String textureName) {
		for (int i = 0; i < textureList.size(); i++) {
			if (textureList.get(i).textureName.equals(textureName)) {
				return textureList.get(i).textureID;
			}
		}
		return -1;
	}
	
	public void addLight(ARVector3 position, ARVector3 ambient, ARVector3 diffuse, ARVector3 specular) {
		int count;
		
		if ((count = lightCount) == MAX_LIGHTS) {
			Log.e(TAG, "Too many lights");
			return;
		}
		lightPosition[count * 3 + 0] = position.x();
		lightPosition[count * 3 + 1] = position.y();
		lightPosition[count * 3 + 2] = position.z();
		lightPosition[count * 3 + 3] = 1.0f;
		
		lightAmbient[count * 3 + 0] = ambient.x();
		lightAmbient[count * 3 + 1] = ambient.y();
		lightAmbient[count * 3 + 2] = ambient.z();
		
		lightDiffuse[count * 3 + 0] = diffuse.x();
		lightDiffuse[count * 3 + 1] = diffuse.y();
		lightDiffuse[count * 3 + 2] = diffuse.z();
		
		lightSpecular[count * 3 + 0] = specular.x();
		lightSpecular[count * 3 + 1] = specular.y();
		lightSpecular[count * 3 + 2] = specular.z();
		lightCount++;
	}
	
	public void setViewport(int width, int height) {
		this.width = width;
		this.height = height;
		if (height < width) {
			farPlane = width / (2.0f * tanViewportFOV);
		} else {
			farPlane = height / (2.0f * tanViewportFOV);
		}
		nearPlane = 1.0f;
		halfWidth = width / 2.0f;
		halfHeight = height / 2.0f;
		viewportAspect = (float)width / (float)height;
	    GLES20.glViewport(0, 0, width, height);
	    Matrix.setIdentityM(projectionMatrix, 0);
	    Matrix.setIdentityM(modelViewMatrix, 0);

	    if (width < height)
	    	Matrix.frustumM(projectionMatrix, 0, -tanViewportFOV * viewportAspect, tanViewportFOV * viewportAspect, 
		    		-tanViewportFOV, tanViewportFOV, nearPlane, farPlane);
	    else
	    	Matrix.frustumM(projectionMatrix, 0, -tanViewportFOV, tanViewportFOV, 
	    		-tanViewportFOV / viewportAspect, tanViewportFOV / viewportAspect, nearPlane, farPlane);
	}
	
	public boolean rayRectangleIntersection(ARVector3 intersection, ARVector3 ray0, ARVector3 ray1, 
			ARVector3 plane0, ARVector3 plane1, ARVector3 plane2, ARVector3 plane3,	boolean checkInside)
	{
		ARVector3 normal = new ARVector3();
		ARVector3 test = new ARVector3();
		float dist0, dist1;

		//	Compute normal to plane

		normal = ARVector3.crossProduct(ARVector3.subtract(plane1, plane0), ARVector3.subtract(plane2, plane0));
		normal.normalize();

		// find distance from points defining line to plane

		dist0 = ARVector3.dotProduct(ARVector3.subtract(ray0, plane0), normal);
		dist1 = ARVector3.dotProduct(ARVector3.subtract(ray1, plane0), normal);

		if (ARVector3.fuzzyCompare(dist0, dist1))
				return false;								// line and plane are parallel
		intersection.set(ARVector3.scale(ARVector3.add(ray0, ARVector3.subtract(ray1, ray0)), (-dist0 / (dist1 - dist0))));

		if (!checkInside)
			return true;

		// check if intersection point lies within the rectangle

		test = ARVector3.crossProduct(normal, ARVector3.subtract(plane1, plane0));
		if (ARVector3.dotProduct(test, ARVector3.subtract(intersection, plane0)) < 0.0f)
			return false;

		test = ARVector3.crossProduct(normal, ARVector3.subtract(plane2, plane1));
		if (ARVector3.dotProduct(test, ARVector3.subtract(intersection, plane1)) < 0.0f)
			return false;

		test = ARVector3.crossProduct(normal, ARVector3.subtract(plane3, plane2));
		if (ARVector3.dotProduct(test, ARVector3.subtract(intersection, plane2)) < 0.0f)
			return false;

		test = ARVector3.crossProduct(normal, ARVector3.subtract(plane0, plane3));
		if (ARVector3.dotProduct(test, ARVector3.subtract(intersection, plane3)) < 0.0f)
			return false;

		return true;
	}

	public static void checkGLError(String tag, String function) {
		int errCode;
		
		while ((errCode = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(tag, function + ": OpenGL error " + errCode);
			throw new RuntimeException(function + ": OpenGL error " + errCode);
		}
	}
}
