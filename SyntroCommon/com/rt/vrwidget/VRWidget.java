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

package com.rt.vrwidget;

import java.util.Stack;

import android.opengl.Matrix;
import android.util.Log;

import com.rt.androidgl.ARVector3;
import com.rt.androidgl.AndroidGL;
import com.rt.androidgl.AndroidGLShader;
import com.rt.androidgl.WireCubeComponent;

public class VRWidget {
	
	public static final String TAG = "VRWidget";
	
	//	Widget types
	
	public final static int WIDGETTYPE_WIDGET = 0;
	public final static int WIDGETTYPE_POINTER = 1;
	public final static int WIDGETTYPE_SELECTOR = 2;
	public final static int WIDGETTYPE_STATUS = 3;
	public final static int WIDGETTYPE_PLANE = 4;
	public final static int WIDGETTYPE_WEB = 5;
	public final static int WIDGETTYPE_WEBPAGE = 6;
	public final static int WIDGETTYPE_MAP = 7;
	public final static int WIDGETTYPE_COMPASS = 8;
	public final static int WIDGETTYPE_IMU = 9;
	
	//	Default used for the default render

	public final static float DEFAULT_X = 0.0f;
	public final static float DEFAULT_Y = 0.0f;
	public final static float DEFAULT_Z = -20.0f;

	public final static float DEFAULT_RADIUS = 1.0f;

	public final static float DEFAULT_WIDTH = 5.0f;
	public final static float DEFAULT_HEIGHT = 5.0f;
	public final static float DEFAULT_DEPTH = 5.0f;

	public final static float BOUNDBOX_BORDER = 0.1f;	
	
	//	Defines for rotation orders
	
	public static final int ROTATION_XYZ = 0;
	public static final int ROTATION_XZY = 1;
	public static final int ROTATION_YXZ = 2;
	public static final int ROTATION_YZX = 3;
	public static final int ROTATION_ZXY = 4;
	public static final int ROTATION_ZYX = 5;
	
	
	protected AndroidGL agl;								// the AndroidGL context
	
	protected boolean selected; 
	protected int widgetType;
	
	protected float width;
	protected float height;
	protected float depth;
	protected ARVector3 center = new ARVector3();
	protected ARVector3 rotation = new ARVector3();
	
	protected boolean enableMoveX;
	protected boolean enableMoveY;
	protected boolean enableMoveZ;
	protected boolean enableRotX;
	protected boolean enableRotY;
	protected boolean enableRotZ;
	
	protected boolean drawSelectBox;
	protected int rotationOrder;
	
	private Stack<float[]> modelMatrixStack = new Stack<float[]>();
	private Stack<float[]> offsetMatrixStack = new Stack<float[]>();
	
	private ARVector3 boundMinus = new ARVector3();
	private ARVector3 boundPlus = new ARVector3();
	private ARVector3[] viewportBoundBox = new ARVector3[8];
	
	private float[] offsetMatrix = new float[16];
	private float[] modelMatrix = new float[16];
	private float[] inverseModelMatrix = new float[16];
	
	//	Constructor
	
	public VRWidget(AndroidGL agl, int widgetType) {
		this.widgetType = widgetType;
		this.agl = agl;
		selected = false;
		center.setX(DEFAULT_X);
		center.setY(DEFAULT_Y);
		center.setZ(DEFAULT_Z / agl.tanViewportFOV);
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		depth = DEFAULT_DEPTH;
		for (int i = 0; i < 8; i++)
			viewportBoundBox[i] = new ARVector3();
		setModelMatrix();
		enableMoveX = true;
		enableMoveY = true;
		enableMoveZ = true;
		enableRotX = true;
		enableRotY = true;
		enableRotZ = true;
		drawSelectBox = true;
		rotationOrder = ROTATION_ZYX;
	}
	
	//	Functions that would normally be overridden
	
	public void VRWidgetInit() {
		Log.e(TAG, "No VRWidgetInit");
	}

	public void VRWidgetRender() {
		Log.e(TAG, "No VRWidgetRender");
	}
	

	public void VRHandleSingleClick(int x, int y, int button) {
		
	}
	
	//	Utility functions
	
	public int getWidgetType() {
		return widgetType;
	}
	
	public void setCenter(ARVector3 center) {
		conditionalMove(center);
	}
	
	public void setCenter(float x, float y, float z) {
		ARVector3 newCenter = new ARVector3(x, y, z);
		conditionalMove(newCenter);
	}

	public ARVector3 getCenter() {
		return center;
	}
	
	public void setSize(float sx, float sy, float sz) {
		width = sx;
		height = sy;
		depth = sz;
	}
	
	public void moveCenter(float dx, float dy, float dz) {
		ARVector3 newCenter = new ARVector3();
		
		newCenter.setX(center.x() + dx); 
		newCenter.setY(center.y() + dy); 
		newCenter.setZ(center.z() + dz); 
		conditionalMove(newCenter);
	}
	
	public void moveCenterTransform(ARVector3 intersection, ARVector3 newIntersection) {
		ARVector3 newCenter = new ARVector3();
		ARVector3 mappedIntersection = new ARVector3();
		ARVector3 mappedNewIntersection = new ARVector3();

		mappedIntersection = intersection.map(modelMatrix);
		mappedNewIntersection = newIntersection.map(modelMatrix);

		newCenter = ARVector3.add(center, ARVector3.subtract(mappedNewIntersection, mappedIntersection));
		
		conditionalMove(newCenter);
		
//		qDebug() << "MCT " << mappedIntersection.x() << " -> " << mappedNewIntersection.x() << " "
//			<< mappedIntersection.y() << " -> " << mappedNewIntersection.y() << " "
//			<< mappedIntersection.z() << " -> " << mappedNewIntersection.z() << " ";
		
		moveTowardOrigin(mappedNewIntersection.length() - mappedIntersection.length());
	}

	public void setMoveMask(boolean enableX, boolean enableY, boolean enableZ) {
		enableMoveX = enableX;
		enableMoveY = enableY;
		enableMoveZ = enableZ;
	}
	
	
	public void moveTowardOrigin(float distance) {
		ARVector3 origin = new ARVector3();
		origin.setX(-center.x());
		origin.setY(-center.y());
		origin.setZ(-center.z());
		origin.normalize();
		origin.setX(origin.x() * distance);
		origin.setY(origin.y() * distance);
		origin.setZ(origin.z() * distance);
		ARVector3 newCenter = ARVector3.add(center, origin);
		conditionalMove(newCenter);	
	}
	
	public void setRotationOrder(int order) {
		rotationOrder = order;
	}

	public void setRotation(float rotX, float rotY, float rotZ) {
		ARVector3 newRotation = new ARVector3(rotX, rotY, rotZ);
		conditionalRotate(newRotation);
	}
	
	public void setRotation(ARVector3 rotation) {
		conditionalRotate(rotation);
	}
	
	public ARVector3 getRotation() {
		return rotation;
	}

	public void moveRotation(float dx, float dy, float dz) {
		ARVector3 newRotation = new ARVector3();
		newRotation.setX(rotation.x() + dx);
		newRotation.setY(rotation.y() + dy);
		newRotation.setZ(rotation.z() + dz);
		conditionalRotate(newRotation);
	}
	
	public void setRotationMask(boolean enableX, boolean enableY, boolean enableZ) {
		enableRotX = enableX;
		enableRotY = enableY;
		enableRotZ = enableZ;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
	//	utility functions for subclasses
	
	protected void startWidgetRender() {					// called at start of render function
		pushModelMatrix();
        Matrix.multiplyMM(agl.modelViewMatrix, 0, modelMatrix, 0, agl.modelViewMatrix, 0);
        boundMinus.set(1000.0f, 1000.0f, 1000.0f);;
        boundPlus.set(-1000.0f, -1000.0f, -1000.0f);;

        Matrix.setIdentityM(offsetMatrix, 0);
	}

	protected void endWidgetRender() {						// called at end of render function
		float border = 0.1f;
		
		if (selected && drawSelectBox) {
			WireCubeComponent box = new WireCubeComponent();

			box.generate(	boundPlus.x() - boundMinus.x() + border,
							boundPlus.y() - boundMinus.y() + border,
							boundPlus.z() - boundMinus.z() + border); 
		    Matrix.translateM(agl.modelViewMatrix, 0, 
		    		(boundPlus.x() + boundMinus.x()) / 2.0f, 
		    		(boundPlus.y() + boundMinus.y()) / 2.0f, 
		    		(boundPlus.z() + boundMinus.z()) / 2.0f);

			box.setShader(agl.shaders[AndroidGLShader.SHADER_FLAT]);
			box.setColor(1.0f, 0.0f, 0.0f, 1.0f);
	        Matrix.multiplyMM(agl.modelViewProjectionMatrix, 0, agl.projectionMatrix, 0, agl.modelViewMatrix, 0);
			box.draw();
		}
//		ARCustomSelectBox();
		popModelMatrix();
		if (modelMatrixStack.size() != 0) {
			Log.e(TAG, "Widget type " + widgetType + " leaving with non empty model stack");
			return;
		}
		if (offsetMatrixStack.size() != 0) {
			Log.e(TAG, "Widget type " + widgetType + " leaving with non empty offset stack");
			return;
		}	
	}

	protected void startCompositeRender(float offsetX, float offsetY, float offsetZ,
										float rotOffsetX, float rotOffsetY, float rotOffsetZ) { // render a composite component
		startComponentRender(offsetX, offsetY, offsetZ, rotOffsetX, rotOffsetY, rotOffsetZ);
	}

	protected void endCompositeRender() {					// end a composite component
		popOffsetMatrix();
		popModelMatrix();
	}

	protected void startComponentRender(float offsetX, float offsetY, float offsetZ,
										float rotOffsetX, float rotOffsetY, float rotOffsetZ) { // render a component
		pushModelMatrix();
	 	pushOffsetMatrix();
	    Matrix.translateM(agl.modelViewMatrix, 0, offsetX, offsetY, offsetZ);
	    Matrix.translateM(offsetMatrix, 0, offsetX, offsetY, offsetZ);

		switch (rotationOrder) {
			case ROTATION_XYZ:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				break;

			case ROTATION_XZY:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				break;

			case ROTATION_YXZ:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				break;

			case ROTATION_YZX:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				break;

			case ROTATION_ZXY:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				break;

			case ROTATION_ZYX:
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(agl.modelViewMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetX, 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetY, 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(offsetMatrix, 0, rotOffsetZ, 0.0f, 0.0f, 1.0f);
				break;
		}

        Matrix.multiplyMM(agl.modelViewProjectionMatrix, 0, agl.projectionMatrix, 0, agl.modelViewMatrix, 0);
        
        float[] mat1 = new float[16];
        
        Matrix.invertM(mat1, 0, agl.modelViewMatrix, 0);
        Matrix.transposeM(agl.normalMatrix44, 0, mat1, 0);
		agl.normalMatrix[0] = agl.normalMatrix44[0];
		agl.normalMatrix[1] = agl.normalMatrix44[1];
		agl.normalMatrix[2] = agl.normalMatrix44[2];
		agl.normalMatrix[3] = agl.normalMatrix44[4];
		agl.normalMatrix[4] = agl.normalMatrix44[5];
		agl.normalMatrix[5] = agl.normalMatrix44[6];
		agl.normalMatrix[6] = agl.normalMatrix44[8];
		agl.normalMatrix[7] = agl.normalMatrix44[9];
		agl.normalMatrix[8] = agl.normalMatrix44[10];
    }

	protected void endComponentRender(ARVector3 bMinus, ARVector3 bPlus) {					// end a component
		ARVector3 mappedMinus;
		ARVector3 mappedPlus;

		mappedMinus = bMinus.map(offsetMatrix);
		mappedPlus = bPlus.map(offsetMatrix);

		for (int i = 0; i < 3; i++) {
			if (mappedMinus.value()[i] < boundMinus.value()[i])
				boundMinus.value()[i] = mappedMinus.value()[i];
		}
		for (int i = 0; i < 3; i++) {
			if (mappedPlus.value()[i] > boundPlus.value()[i])
				boundPlus.value()[i] = mappedPlus.value()[i];
		}
//		Log.d("", "bp = " + boundPlus.x() + "," + boundPlus.y() + "," + boundPlus.z());
//		Log.d("", "bm = " + boundMinus.x() + "," + boundMinus.y() + "," + boundMinus.z());
		popOffsetMatrix();
		popModelMatrix();	
	}

	protected void pushModelMatrix() {						// put model matrix on stack
		float[] mat = new float[16];
		System.arraycopy(agl.modelViewMatrix, 0, mat, 0, 16);
		modelMatrixStack.push(mat);
	}
	
	protected void popModelMatrix() {						// take model matrix from stack
		if (modelMatrixStack.size() == 0) {
			Log.e(TAG, "Widget " + widgetType + " tried to pop empty model stack");
			return;
		}
		agl.modelViewMatrix = modelMatrixStack.pop();
	}
	
	
	protected void pushOffsetMatrix() {						// put offset matrix on stack
		float[] mat = new float[16];
		System.arraycopy(offsetMatrix, 0, mat, 0, 16);
		offsetMatrixStack.push(mat);
	}
	
	protected void popOffsetMatrix() {						// take offset matrix from stack
		if (offsetMatrixStack.size() == 0) {
			Log.e(TAG, "Widget " + widgetType + " tried to pop empty offset stack");
			return;
		}
		offsetMatrix = offsetMatrixStack.pop();
	}
	
	//	Private functions
	
	private void updateViewportBox() {						// update the bounding box
		// bottom left front
		viewportBoundBox[0] = new ARVector3(boundMinus.x() - BOUNDBOX_BORDER, 
											boundMinus.y() - BOUNDBOX_BORDER, 
											boundPlus.z() + BOUNDBOX_BORDER);
		viewportBoundBox[0] = viewportBoundBox[0].map(modelMatrix);

		// top left front
		viewportBoundBox[1] = new ARVector3(boundMinus.x() - BOUNDBOX_BORDER, 
											boundPlus.y() + BOUNDBOX_BORDER, 
											boundPlus.z() + BOUNDBOX_BORDER);
		
		viewportBoundBox[1] = viewportBoundBox[1].map(modelMatrix);

		// top right front
		viewportBoundBox[2] = new ARVector3(boundPlus.x() + BOUNDBOX_BORDER, 
											boundPlus.y() + BOUNDBOX_BORDER, 
											boundPlus.z() + BOUNDBOX_BORDER);
		viewportBoundBox[2] = viewportBoundBox[2].map(modelMatrix);

		// bottom right front
		viewportBoundBox[3] = new ARVector3(boundPlus.x() + BOUNDBOX_BORDER, 
											boundMinus.y() - BOUNDBOX_BORDER, 
											boundPlus.z() + BOUNDBOX_BORDER);
		viewportBoundBox[3] = viewportBoundBox[3].map(modelMatrix);

		// bottom left back
		viewportBoundBox[4] = new ARVector3(boundMinus.x() - BOUNDBOX_BORDER, 
											boundMinus.y() - BOUNDBOX_BORDER, 
											boundMinus.z() - BOUNDBOX_BORDER);
		viewportBoundBox[4] = viewportBoundBox[4].map(modelMatrix);

		// top left back
		viewportBoundBox[5] = new ARVector3(boundMinus.x() - BOUNDBOX_BORDER, 
											boundPlus.y() + BOUNDBOX_BORDER, 
											boundMinus.z() - BOUNDBOX_BORDER);
		viewportBoundBox[5] = viewportBoundBox[5].map(modelMatrix);

		// top right back
		viewportBoundBox[6] = new ARVector3(boundPlus.x() + BOUNDBOX_BORDER, 
											boundPlus.y() + BOUNDBOX_BORDER, 
											boundMinus.z() - BOUNDBOX_BORDER);
		viewportBoundBox[6] = viewportBoundBox[6].map(modelMatrix);

		// bottom right back
		viewportBoundBox[7] = new ARVector3(boundPlus.x() + BOUNDBOX_BORDER, 
											boundMinus.y() - BOUNDBOX_BORDER, 
											boundMinus.z() - BOUNDBOX_BORDER);	
		viewportBoundBox[7] = viewportBoundBox[7].map(modelMatrix);
	}
	
	private void conditionalMove(ARVector3 newCenter) {		// move if stays in view
		ARVector3 oldCenter = new ARVector3();
		boolean visible = true;

		oldCenter = center;
		if (enableMoveX)
			center.value()[0] = newCenter.value()[0];
		if (enableMoveY)
			center.value()[1] = newCenter.value()[1];
		if (enableMoveZ)
			center.value()[2] = newCenter.value()[2];
		setModelMatrix();
		updateViewportBox();

		for (int i = 0; i < 8; i++) {
			if ((viewportBoundBox[i].z() >= -agl.nearPlane) ||
				(viewportBoundBox[i].z() <= -agl.farPlane)) {
					visible = false;
					break;
			}
		}
		if (!visible) {
			center = oldCenter;
			setModelMatrix();
		}	
	}
	
	private void conditionalRotate(ARVector3 newRotation) {	// rotate if stays in view
		ARVector3 oldRotation = new ARVector3();
		boolean visible = true;

		oldRotation = rotation;
		if (enableRotX)
			rotation.value()[0] = newRotation.value()[0];
		if (enableRotY)
			rotation.value()[1] = newRotation.value()[1];
		if (enableRotZ)
			rotation.value()[2] = newRotation.value()[2];
		setModelMatrix();
		updateViewportBox();

		for (int i = 0; i < 8; i++) {
			if ((viewportBoundBox[i].z() >= -agl.nearPlane) ||
				(viewportBoundBox[i].z() <= -agl.farPlane)) {
					visible = false;
					break;
			}
		}
		if (!visible) {
			rotation = oldRotation;
			setModelMatrix();
		}
	}
	
	private void setModelMatrix() {
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.setIdentityM(inverseModelMatrix, 0);

	    Matrix.translateM(modelMatrix, 0, center.x(), center.y(), center.z());
		
		switch (rotationOrder) {
			case ROTATION_XYZ:
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);
				
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);

				break;

			case ROTATION_XZY:
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);

				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);

				break;

			case ROTATION_YXZ:
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);

				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);

				break;

			case ROTATION_YZX:
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);

				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);

				break;

			case ROTATION_ZXY:
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);

				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);
				break;

			case ROTATION_ZYX:
				Matrix.rotateM(modelMatrix, 0, rotation.x(), 1.0f, 0.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(modelMatrix, 0, rotation.z(), 0.0f, 0.0f, 1.0f);

				Matrix.rotateM(inverseModelMatrix, 0, -rotation.z(), 0.0f, 0.0f, 1.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.y(), 0.0f, 1.0f, 0.0f);
				Matrix.rotateM(inverseModelMatrix, 0, -rotation.x(), 1.0f, 0.0f, 0.0f);

				break;
		}
	    Matrix.translateM(inverseModelMatrix, 0, -center.x(), -center.y(), -center.z());
	}
	
	public int boundingBoxIntersection(ARVector3 intersection, ARVector3 ray0, ARVector3 ray1)
	{
		ARVector3 bestIntersection = new ARVector3();
		int bestFace = -1;
		float bestDistance = 100000000000.0f;
		float length;
		int rectIndex;
		
		updateViewportBox();

		// Nearest face

		rectIndex = 0;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[0], viewportBoundBox[1], viewportBoundBox[2], viewportBoundBox[3], true)) {
			bestIntersection.set(intersection);
			bestDistance = bestIntersection.length() ;
			bestFace = rectIndex;
		}

		// Furthest face
		
		rectIndex++;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[4], viewportBoundBox[5], viewportBoundBox[6], viewportBoundBox[7], true)) {
			if ((length = intersection.length()) < bestDistance) {
				bestIntersection.set(intersection);
				bestDistance = length;
				bestFace = rectIndex;
			}
		}

		// Top face
		rectIndex++;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[1], viewportBoundBox[5], viewportBoundBox[6], viewportBoundBox[2], true)) {
			if ((length = intersection.length()) < bestDistance) {
				bestIntersection.set(intersection);
				bestDistance = length;
				bestFace = rectIndex;
			}
		}

		// Bottom face
		rectIndex++;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[0], viewportBoundBox[4], viewportBoundBox[7], viewportBoundBox[3], true)) {
			if ((length = intersection.length()) < bestDistance) {
				bestIntersection.set(intersection);
				bestDistance = length;
				bestFace = rectIndex;
			}
		}

		// Left side face
		rectIndex++;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[0], viewportBoundBox[4], viewportBoundBox[5], viewportBoundBox[1], true)) {
			if ((length = intersection.length()) < bestDistance) {
				bestIntersection.set(intersection);
				bestDistance = length;
				bestFace = rectIndex;
			}
		}

		// Right side face
		rectIndex++;
		if (agl.rayRectangleIntersection(intersection, ray0, ray1, 
				viewportBoundBox[3], viewportBoundBox[2], viewportBoundBox[6], viewportBoundBox[7], true)) {
			if ((length = intersection.length()) < bestDistance) {
				bestIntersection.set(intersection);
				bestDistance = length;
				bestFace = rectIndex;
			}
		}

		if (bestFace == -1)
			return -1;

		intersection.set(bestIntersection.map(inverseModelMatrix));
		rectIndex = bestFace;
		Log.d(TAG, "Hit face " + rectIndex + " raw inter " + bestIntersection.x() + " " + 
				bestIntersection.y() + " " + bestIntersection.z());
		return bestFace;
	}

	public boolean boundingRectIntersection(ARVector3 intersection, int rectIndex, ARVector3 ray0, ARVector3 ray1) {
		boolean success;

		updateViewportBox();

		switch (rectIndex) {
			// Nearest face
			case 0:	
				success = agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[0], viewportBoundBox[1], viewportBoundBox[2], viewportBoundBox[3], false);
				break;

			// Furthest face
			case 1:
				success = agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[4], viewportBoundBox[5], viewportBoundBox[6], viewportBoundBox[7], false);
				break;

			// Top face
			case 2:
				success = agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[1], viewportBoundBox[5], viewportBoundBox[6], viewportBoundBox[2], false);
				break;

			// Bottom face
			case 3:
				success = agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[0], viewportBoundBox[4], viewportBoundBox[7], viewportBoundBox[3], false);
				break;

			// Left side face
			case 4:
				success = agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[0], viewportBoundBox[4], viewportBoundBox[5], viewportBoundBox[1], false);
				break;

			// Right side face
			case 5:
				success =agl.rayRectangleIntersection(intersection, ray0, ray1, 
						viewportBoundBox[3], viewportBoundBox[2], viewportBoundBox[6], viewportBoundBox[7], false);
				break;

			default:
				success = false;
		}
		if (success) {
			//	qDebug() << "raw inter " << intersection.x() << " " << intersection.y() << " " << intersection.z();
			intersection.set(intersection.map(inverseModelMatrix));
		}
		return success;
	}
}


