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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.rt.androidgl.AndroidGL;
import com.rt.androidgl.AndroidGLShader;
import com.rt.androidgl.PlaneComponent;

public class VRPlaneWidget extends VRWidget {
	
	private PlaneComponent plane = new PlaneComponent();
	private Bitmap bitmap;
	private Canvas canvas;
	private int bitmapId;
	private boolean bitmapChanged = false;
	
	public VRPlaneWidget(AndroidGL agl) {
		super(agl, WIDGETTYPE_PLANE);
	}
	
	public void VRWidgetInit() {
		plane.generate(width, height);
		plane.setShader(agl.shaders[AndroidGLShader.SHADER_TEXTURE]);
		plane.setColor(0, 1, 0, 1);
		float aspect = width / height;
		bitmap = Bitmap.createBitmap((int)(aspect * 400.0f), 400, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		bitmapId = agl.addTexture(bitmap, "planetexture");
		plane.setTextureID(bitmapId);
	}
	
	public void VRWidgetRender() {
		if (bitmapChanged) {
			agl.replaceTexture(bitmap, bitmapId);
			bitmapChanged = false;
		}
	
		startWidgetRender();

		startComponentRender(0, 0, 0, 0, 0, 0);
		
		plane.draw();
		
		endComponentRender(plane.getBoundMinus(), plane.getBoundPlus());

		endWidgetRender();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void updateBitmap() {
		bitmapChanged = true;
	}
}
