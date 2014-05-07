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

public class PlaneComponent extends Component {

	public void generate(float width, float height)
	{
	    float[][] coords = {{+0.5f, +0.5f}, {-0.5f, +0.5f}, {-0.5f, -0.5f}, {+0.5f, -0.5f}};

		reset();

		startGeneration(4, false, true);
		
		for (int vert = 0; vert < 4; vert++) {
	        setVertex(width * coords[vert][0], height * coords[vert][1], 0);
			setTextureCoord((vert == 0) || (vert == 3) ? 1 : 0, (vert == 0) || (vert == 1) ? 0 : 1);
			nextVertex();
	   }
		
		endGeneration();
	}

	public void draw()
	{
		draw(GLES20.GL_TRIANGLE_FAN);
	}

	
}
