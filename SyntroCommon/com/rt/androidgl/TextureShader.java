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
import android.util.Log;

public class TextureShader extends AndroidGLShader {
	private AndroidGL agl;	
	
	protected final String vertexSource =
		    "attribute highp vec3 vertex;\n" +
	        "attribute mediump vec2 texCoord;\n" +
	        "varying mediump vec2 texc;\n" +
	        "uniform mediump mat4 MVP;\n" +
	        "void main(void)\n" +
	        "{\n" +
			"    gl_Position = MVP * vec4(vertex, 1.0);\n" +
	        "    texc = texCoord;\n" +
	        "}\n";
	
	
	protected final String fragmentSource = 
	        "uniform sampler2D texture;\n" +
	        "varying mediump vec2 texc;\n" +
	        "void main(void)\n" +
	        "{\n" +
	        "    gl_FragColor = texture2D(texture, texc.st);\n" +
	        "}\n";
	
	public TextureShader(AndroidGL agl) {
		this.agl = agl;
		shaderType = AndroidGLShader.SHADER_TEXTURE;
		compileProgram(vertexSource, fragmentSource);
		vertexIndex = GLES20.glGetAttribLocation(program, "vertex");
	    if (vertexIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for vertex");
	    }
	    
		textureCoordIndex = GLES20.glGetAttribLocation(program, "texCoord");
	    if (textureCoordIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for texture coord");
	    }
	    
		MVPIndex = GLES20.glGetUniformLocation(program, "MVP");
	    if (MVPIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for MVP");
	    }
	    
	}	
	
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, int normalOffset, int textureOffset, int textureID) {
		GLES20.glUseProgram(program);
		AndroidGL.checkGLError(TAG, "glUseProgram");
		
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

	    vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(vertexIndex, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		AndroidGL.checkGLError(TAG, "glVertexAttribPointer vertex");
	    GLES20.glEnableVertexAttribArray(vertexIndex);
		AndroidGL.checkGLError(TAG, "glEnableVertexAttribArray vertex");
		
		if (textureOffset == -1) {
			Log.e(TAG, "Attempt to paint texture with no texture coords");
			return;
		}
		vertexBuffer.position(textureOffset);
		GLES20.glVertexAttribPointer(textureCoordIndex, 2, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		AndroidGL.checkGLError(TAG, "glVertexAttribPointer textureCoord");
		GLES20.glEnableVertexAttribArray(textureCoordIndex);
		AndroidGL.checkGLError(TAG, "glEnableVertexAttribArray textureCoords");
		
		GLES20.glUniformMatrix4fv(MVPIndex, 1, false, agl.modelViewProjectionMatrix, 0);
		AndroidGL.checkGLError(TAG, "glUniformMatrix4fv MVP");
		
	    

	}		
}
