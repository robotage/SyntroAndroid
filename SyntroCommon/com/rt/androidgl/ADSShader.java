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

public class ADSShader extends AndroidGLShader {

	private AndroidGL agl;	
	
	protected final String vertexSource =
	        "attribute highp vec3 vertex;\n" +
	        "attribute mediump vec3 normal;\n" +
			"varying mediump vec3 lightI;\n" +
	        "uniform mediump mat4 MV;\n" +
			"uniform mediump mat4 MVP\n;" +
	 		"uniform mediump mat3 normalMatrix;\n" +
			"uniform mediump int lightCount;\n" +
			"uniform mediump vec4 posL[4];\n" +
			"uniform mediump vec3 ambientL[4];\n" +
			"uniform mediump vec3 diffuseL[4];\n" +
			"uniform mediump vec3 specularL[4];\n" +
			"uniform mediump vec3 ambientM;\n" +
			"uniform mediump vec3 diffuseM;\n" +
			"uniform mediump vec3 specularM;\n" +
			"uniform mediump float shininess;\n" +
	        "void main(void)\n" +
	        "{\n" +
			"    gl_Position = MVP * vec4(vertex, 1.0);\n" +
	 		"    vec3 tnorm = normalize(normalMatrix * normal);\n" +
			"    vec4 eyeCoords = MV * vec4(vertex, 1.0);\n" +
			"    vec3 v = normalize(-eyeCoords.xyz);\n" +
			"    lightI = vec3(0.0, 0.0, 0.0);\n" +
			"    for (int i = 0; i < lightCount; i++) {\n" +
			"        vec3 s = normalize(vec3(posL[i] - eyeCoords));\n" +
			"        float sDotN = max(dot(s, tnorm), 0.0);\n" +
			"        vec3 r = reflect(-s, tnorm);\n" +
			"        vec3 ambient = ambientL[i] * ambientM;\n" +
			"        vec3 diffuse = diffuseL[i] * diffuseM * sDotN;\n" +
			"        vec3 spec = vec3(0.0);\n" +
			"        if (sDotN > 0.0) spec = specularL[i] * specularM * pow(max(dot(r,v), 0.0), shininess);\n" +
			"        lightI += ambient + diffuse + spec;\n" +
			"    }\n" +
	       "}\n";
	
	
	protected final String fragmentSource = 
	        "varying mediump vec3 lightI;\n" +
	        "void main(void)\n" +
	        "{\n" +
			"    gl_FragColor = vec4(lightI, 1.0);\n" +
	        "}\n";
	
	public ADSShader(AndroidGL agl) {
		this.agl = agl;
		shaderType = AndroidGLShader.SHADER_ADS;
		compileProgram(vertexSource, fragmentSource);
		vertexIndex = GLES20.glGetAttribLocation(program, "vertex");
	    if (vertexIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for vertex");
	    }
	    
		normalIndex = GLES20.glGetAttribLocation(program, "normal");
	    if (vertexIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for normal");
	    }
	    
		MVPIndex = GLES20.glGetUniformLocation(program, "MVP");
	    if (MVPIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for MVP");
	    }
	    
		MVIndex = GLES20.glGetUniformLocation(program, "MV");
	    if (MVIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for MV");
	    }
	    
		NIndex = GLES20.glGetUniformLocation(program, "normalMatrix");
	    if (NIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for N");
	    }
	    
		lightCountIndex = GLES20.glGetUniformLocation(program, "lightCount");
	    if (lightCountIndex == -1) {
	    	throw new RuntimeException("Could not get attrib location for lightCount");
	    }
	    
		lightPosIndex = GLES20.glGetUniformLocation(program, "posL");
	    if (lightPosIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for posL");
	    }
	    
		lightAmbientIndex = GLES20.glGetUniformLocation(program, "ambientL");
	    if (lightAmbientIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for ambientL");
	    }
	    
		lightDiffuseIndex = GLES20.glGetUniformLocation(program, "diffuseL");
	    if (lightDiffuseIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for diffuseL");
	    }
	    
		lightSpecularIndex = GLES20.glGetUniformLocation(program, "specularL");
	    if (lightSpecularIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for specularL");
	    }
	    
		materialAmbientIndex = GLES20.glGetUniformLocation(program, "ambientM");
	    if (materialAmbientIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for ambientM");
	    }
	    
		materialDiffuseIndex = GLES20.glGetUniformLocation(program, "diffuseM");
	    if (materialDiffuseIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for diffuseM");
	    }
	    
		materialSpecularIndex = GLES20.glGetUniformLocation(program, "specularM");
	    if (materialSpecularIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for specularM");
	    }

	    materialShininessIndex = GLES20.glGetUniformLocation(program, "shininess");
	    if (materialSpecularIndex == -1) {
	    	throw new RuntimeException("Could not get uniform location for shininess");
	    }
	}
	
	public void load(FloatBuffer vertexBuffer, int vertexStride, int normalOffset, int textureOffset, ComponentMaterial material) {
		GLES20.glUseProgram(program);
		AndroidGL.checkGLError(TAG, "glUseProgram");
		
	    vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(vertexIndex, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		AndroidGL.checkGLError(TAG, "glVertexAttribPointer vertex");
	    GLES20.glEnableVertexAttribArray(vertexIndex);
		AndroidGL.checkGLError(TAG, "glEnableVertexAttribArray vertex");
		
		if (normalOffset == -1) {
			Log.e(TAG, "Attempt to paint material with no normals");
			return;
		}
		vertexBuffer.position(normalOffset);
		GLES20.glVertexAttribPointer(normalIndex, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		AndroidGL.checkGLError(TAG, "glVertexAttribPointer normals");
		GLES20.glEnableVertexAttribArray(normalIndex);
		AndroidGL.checkGLError(TAG, "glEnableVertexAttribArray normals");		
		
		
		GLES20.glUniformMatrix4fv(MVPIndex, 1, false, agl.modelViewProjectionMatrix, 0);
		AndroidGL.checkGLError(TAG, "glUniformMatrix4fv MVP");
		
		GLES20.glUniformMatrix4fv(MVIndex, 1, false, agl.modelViewMatrix, 0);
		AndroidGL.checkGLError(TAG, "glUniformMatrix4fv MV");

		GLES20.glUniformMatrix3fv(NIndex, 1, false, agl.normalMatrix, 0);
		AndroidGL.checkGLError(TAG, "glUniformMatrix3fv normalMatrix");
		
		GLES20.glUniform1i(lightCountIndex, agl.lightCount);
		AndroidGL.checkGLError(TAG, "glUniform1i lightCount");
		
		GLES20.glUniform4fv(lightPosIndex, 4, agl.lightPosition, 0);
		AndroidGL.checkGLError(TAG, "glUniform4fv lightPosition");
		
		GLES20.glUniform3fv(lightAmbientIndex, 4, agl.lightAmbient, 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv lightAmbient");
		
		GLES20.glUniform3fv(lightDiffuseIndex, 4, agl.lightDiffuse, 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv lightDiffuse");
		
		GLES20.glUniform3fv(lightSpecularIndex, 4, agl.lightSpecular, 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv lightSpecular");
		
		GLES20.glUniform3fv(materialAmbientIndex, 1, material.ambientReflectivity.value(), 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv materialAmbient");
		
		GLES20.glUniform3fv(materialDiffuseIndex, 1, material.diffuseReflectivity.value(), 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv materialDiffuse");
		
		GLES20.glUniform3fv(materialSpecularIndex, 1, material.specularReflectivity.value(), 0);
		AndroidGL.checkGLError(TAG, "glUniform3fv materialSpecular");
		
		GLES20.glUniform1f(materialShininessIndex, material.shininess);
		AndroidGL.checkGLError(TAG, "glUniform1f materialShininess");
		
	}	
}
