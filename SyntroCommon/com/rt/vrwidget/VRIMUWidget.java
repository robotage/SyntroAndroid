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

import com.rt.androidgl.ARVector3;
import com.rt.androidgl.AndroidGL;
import com.rt.androidgl.AndroidGLShader;
import com.rt.androidgl.CylinderComponent;
import com.rt.androidgl.DiskComponent;
import com.rt.androidgl.WireCubeComponent;

public class VRIMUWidget extends VRWidget {
	public static final String TAG = "VRIMUWidget";
			
	private CylinderComponent imuXCylinder = new CylinderComponent();
	private CylinderComponent imuYCylinder = new CylinderComponent();
	private CylinderComponent imuZCylinder = new CylinderComponent();
	private DiskComponent imuXTop = new DiskComponent();
	private DiskComponent imuYTop = new DiskComponent();
	private DiskComponent imuZTop = new DiskComponent();
	private WireCubeComponent cube = new WireCubeComponent();

	private float imuRadius;
	private float imuLength;

	public VRIMUWidget(AndroidGL agl) {
		super(agl, WIDGETTYPE_IMU);
		
	}
	
	public void VRWidgetInit()
	{
		imuLength = 4.0f;
		imuRadius = imuLength / 10.0f;
		
		imuXCylinder.generate(imuRadius / 100.0f, imuRadius, imuLength, 50, 1);
		imuXCylinder.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuXCylinder.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuXCylinder.setTextureID(agl.lookupTexture("RedShade"));

		imuXTop.generate(0, imuRadius, 50, 1);
		imuXTop.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuXTop.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuXTop.setTextureID(agl.lookupTexture("RedShade"));

		imuYCylinder.generate(imuRadius / 100.0f, imuRadius, imuLength, 50, 1);
		imuYCylinder.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuYCylinder.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuYCylinder.setTextureID(agl.lookupTexture("GreenShade"));

		imuYTop.generate(0, imuRadius, 50, 1);
		imuYTop.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuYTop.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuYTop.setTextureID(agl.lookupTexture("GreenShade"));

		imuZCylinder.generate(imuRadius / 100.0f, imuRadius, imuLength, 50, 1);
		imuZCylinder.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuZCylinder.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuZCylinder.setTextureID(agl.lookupTexture("BlueShade"));

		imuZTop.generate(0, imuRadius, 50, 1);
		imuZTop.setShader(agl.shaders[AndroidGLShader.SHADER_ADSTEXTURE]);
		imuZTop.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 3.0f);
		imuZTop.setTextureID(agl.lookupTexture("BlueShade"));

		cube.generate(imuLength / 2.0f, imuLength / 2.0f, imuLength / 2.0f);
		cube.setShader(agl.shaders[AndroidGLShader.SHADER_FLAT]);
		cube.setMaterial(new ARVector3(1.0f, 1.0f, 1.0f), new ARVector3(0.8f, 0.8f, 0.8f), 
				new ARVector3(1.0f, 1.0f, 1.0f), 6.0f);
		cube.setColor(1, 1, 0, 1);
		
		setRotationOrder(VRWidget.ROTATION_XZY);					// so that roll/pitch/yaw works
	}	

	public void VRWidgetRender()
	{
	    startWidgetRender();

	    //  do cube

	    startComponentRender(0, 0, 0, 0, 0, 0);
	    cube.draw();
	    endComponentRender(cube.getBoundMinus(), cube.getBoundPlus());

	    // do X IMU cylinder

	    startComponentRender(0, 0, 0, 0, 90, 0);
	    imuXCylinder.draw();
	    endComponentRender(imuXCylinder.getBoundMinus(), imuXCylinder.getBoundPlus());

	    // do X IMU top

	    startComponentRender(imuLength, 0, 0, 0, 90, 0);
	    imuXTop.draw();
	    endComponentRender(imuXTop.getBoundMinus(), imuXTop.getBoundPlus());

	    // do Y IMU cylinder

	    startComponentRender(0, 0, 0, 0, 0, 0);
	    imuYCylinder.draw();
	    endComponentRender(imuYCylinder.getBoundMinus(), imuYCylinder.getBoundPlus());

	    // do Y IMU top

	    startComponentRender(0, 0, imuLength, 0, 0, -90);
	    imuYTop.draw();
	    endComponentRender(imuYTop.getBoundMinus(), imuYTop.getBoundPlus());

	    // do Z IMU cylinder

	    startComponentRender(0, 0, 0, 90, 0, 0);
	    imuZCylinder.draw();
	    endComponentRender(imuZCylinder.getBoundMinus(), imuZCylinder.getBoundPlus());

	    // do Z IMU top

	    startComponentRender(0, -imuLength, 0, 90, 0, 0);
	    imuZTop.draw();
	    endComponentRender(imuZTop.getBoundMinus(), imuZTop.getBoundPlus());

	    endWidgetRender();
	}
}
