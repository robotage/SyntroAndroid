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

import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class FlatShader extends AndroidGLShader {


	protected final String vertexSource =
	        "attribute highp vec4 vertex;\n" +
	        "uniform mediump mat4 MVP;\n" +
	        "void main(void) {\n" +
	        "    gl_Position = MVP * vertex;\n" +
	        "}\n";
	
	
	protected final String fragmentSource = 
	        "uniform mediump vec4 color;\n" +
	        "void main(void) {\n" +
	        "    gl_FragColor = color;\n" +
	        "}\n";

	private int colorIndex;
	private AndroidGL agl;
	
	public FlatShader(AndroidGL agl) {
		this.agl = agl;
		shaderType = AndroidGLShader.SHADER_FLAT;
		compileProgram(vertexSource, fragmentSource);
		vertexIndex = GLES20.glGetAttribLocation(program, "vertex");
	    if (vertexIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for vertex");
	    }

	    MVPIndex = GLES20.glGetUniformLocation(program, "MVP");
	    if (MVPIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for matrix");
	    }

	    colorIndex = GLES20.glGetUniformLocation(program, "color");
	    if (colorIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for color");
	    }
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, float red, float green, float blue, float alpha) {
		GLES20.glUseProgram(program);
		AndroidGL.checkGLError(TAG, "glUseProgram");
		
		GLES20.glEnableVertexAttribArray(vertexIndex);
		AndroidGL.checkGLError(TAG, "glEnableVertexAttribArray");

		GLES20.glVertexAttribPointer(vertexIndex, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		AndroidGL.checkGLError(TAG, "glVertexAttribPointer");
		
		GLES20.glUniformMatrix4fv(MVPIndex, 1, false, agl.modelViewProjectionMatrix, 0);
		AndroidGL.checkGLError(TAG, "glUniformMatrix4fv");
		
		GLES20.glUniform4f(colorIndex, red, green, blue, alpha);
		AndroidGL.checkGLError(TAG, "glUniform4f");
	}
}
