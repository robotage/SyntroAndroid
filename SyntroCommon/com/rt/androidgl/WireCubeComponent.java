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

import android.opengl.GLES20;

public class WireCubeComponent extends Component {

	public void generate(float width, float height, float depth)
	{
		float w2 = width / 2.0f;
		float h2 = height / 2.0f;
		float d2 = depth / 2.0f;
		
		reset();
		
		startGeneration(24, false, false);

		setVertex(new ARVector3(-w2, h2, d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, d2)); nextVertex();
		setVertex(new ARVector3(-w2, -h2, d2)); nextVertex();
		setVertex(new ARVector3(w2, -h2, d2)); nextVertex();

		setVertex(new ARVector3(w2, -h2, d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, d2)); nextVertex();
		setVertex(new ARVector3(-w2, -h2, d2)); nextVertex();
		setVertex(new ARVector3(-w2, h2, d2)); nextVertex();

		setVertex(new ARVector3(-w2, h2, -d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, -d2)); nextVertex();
		setVertex(new ARVector3(-w2, -h2, -d2)); nextVertex();
		setVertex(new ARVector3(w2, -h2, -d2)); nextVertex();

		setVertex(new ARVector3(w2, -h2, -d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, -d2)); nextVertex();
		setVertex(new ARVector3(-w2, -h2, -d2)); nextVertex();
		setVertex(new ARVector3(-w2, h2, -d2)); nextVertex();

		setVertex(new ARVector3(-w2, -h2, -d2)); nextVertex();
		setVertex(new ARVector3(-w2, -h2, d2)); nextVertex();
		setVertex(new ARVector3(w2, -h2, -d2)); nextVertex();
		setVertex(new ARVector3(w2, -h2, d2)); nextVertex();

		setVertex(new ARVector3(-w2, h2, -d2)); nextVertex();
		setVertex(new ARVector3(-w2, h2, d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, -d2)); nextVertex();
		setVertex(new ARVector3(w2, h2, d2)); nextVertex();
		
//		startGeneration(4);
//		addVertex(new ARVector3(-0.1f, 0, 0));
//		addVertex(new ARVector3(0.1f, 0, 0));
//		addVertex(new ARVector3(0, -0.1f, 0));
//		addVertex(new ARVector3(0, 0.1f, 0));

		endGeneration();
		
	}

	public void draw()
	{
		draw(GLES20.GL_LINES);
	}

}
