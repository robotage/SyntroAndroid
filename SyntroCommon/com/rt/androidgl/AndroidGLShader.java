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

public class AndroidGLShader {
	public static String TAG = "AndroidGLShader";
	
	public static final int SHADER_FLAT = 0;
	public static final int SHADER_TEXTURE = 1;
	public static final int SHADER_ADS = 2;
	public static final int SHADER_ADSTEXTURE = 3;
	public static final int SHADER_COUNT = 4;

	public int vertexShader;
	public int fragmentShader;
	int program;
	public int shaderType = SHADER_FLAT;
	
	// possible shader uniform and attribute locations
	
	public int vertexIndex;									// vertex array index
	public int textureCoordIndex;							// texture coord index
	public int normalIndex;									// normal array index
	public int MVPIndex;									// model view projection matrix
	public int MVIndex;										// model view matrix
	public int NIndex;										// normal matrix
	public int lightCountIndex;								// number of lights in use index
	public int lightPosIndex;								// position of lights index
	public int lightAmbientIndex;							// ambient light values index
	public int lightDiffuseIndex;							// diffuse light values index
	public int lightSpecularIndex;							// specular light values index
	public int materialAmbientIndex;						// ambient material value index
	public int materialDiffuseIndex;						// diffuse material value index
	public int materialSpecularIndex;						// specular material value index
    public int materialShininessIndex;						// shininess of material
    
	public int getType() {
		return shaderType;
	}
	
	public void compileProgram(String vertexSource, String fragmentSource) {
		program = 0;
		vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0)
			return;
		fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (fragmentShader == 0)
			return;
		program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "shader link failed: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
		} else {
			Log.e(TAG, "failed to compile shader " + shaderType);
		}
	}
	
	private int compileShader(int shaderPart, String source) {
		int shader = GLES20.glCreateShader(shaderPart);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e(TAG, "failed to compile shader " + Integer.toString(shaderPart, 16) + ":");
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
			} else {
				Log.d(TAG, "shader " + shaderPart + " compiled");
			}
		}
		return shader;
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, int normalOffset, int textureOffset, int textureID) {
		System.out.println("No shader load");
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, float red, float green, float blue, float alpha) {
		System.out.println("No shader load");
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, int normalOffset, int textureOffset, ComponentMaterial material) {
		System.out.println("No shader load");
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, int normalOffset, int textureOffset, int textureID, ComponentMaterial material) {
		System.out.println("No shader load");
	}
}
