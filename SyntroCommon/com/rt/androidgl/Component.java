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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class Component {
	
	public static final String TAG = "Component";
	
	public static final int COMPONENT_SIZEOF_FLOAT = 4;
	public static final int COMPONENT_SIZEOF_INT = 4;
	
	//	offsets of data in the vertex buffer given to the shader
	
	protected AndroidGLShader shader = null;
	protected int textureID = -1;
	protected float[] color = new float[4];
	ComponentMaterial material = null;
	
    protected FloatBuffer vertexBuffer;
    protected IntBuffer indexBuffer;
  	
    protected int[] indices = null;
    int indexIndex;
    
	protected float[] vertices = null;
	int vertexIndex;
	int vertexCount;
	
	private int normalOffset;
	private int textureOffset;
	private int vertexLength;	
	
	ARVector3 boundMinus = new ARVector3();
	ARVector3 boundPlus = new ARVector3();
	
	
	public void setShader(AndroidGLShader shader) {
		this.shader = shader;
	}
	
	protected void reset() {
		textureID = -1;
		vertexCount = 0;
		vertices = null;
		indices = null;
		indexIndex = 0;
		boundMinus = new ARVector3(10000.0f, 10000.0f, 10000.0f);
		boundPlus = new ARVector3(-10000.0f, -10000.0f, -10000.0f);
	}
	
	public void setMaterial(ComponentMaterial material) {
		this.material = material;
	}
	
	public void setColor(float red, float green, float blue, float alpha) {
		color[0] = red;
		color[1] = green;
		color[2] = blue;
		color[3] = alpha;
	}
	
	public void setMaterial(ARVector3 ambient, ARVector3 diffuse, ARVector3 specular, float shininess) {
		material = new ComponentMaterial();
		material.ambientReflectivity = ambient;
		material.diffuseReflectivity = diffuse;
		material.specularReflectivity = specular;
		material.shininess = shininess;
	}
	
	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}
	
	
	protected void addIndex(short index) {
		if (index >= vertexCount) {
			Log.e(TAG, "Too many indices");
			return;
		}
		indices[indexIndex++] = index;
	}
	
	protected void nextVertex() {
		vertexIndex += vertexLength;
		if (vertexIndex > vertexCount * vertexLength) {
			Log.e(TAG, "Too many vertices)");
			return;
		}
	}
	
	protected void setVertex(ARVector3 vertex) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setVertex: too many vertices)");
			return;
		}
		vertices[vertexIndex + 0] = vertex.x();
		vertices[vertexIndex + 1] = vertex.y();
		vertices[vertexIndex + 2] = vertex.z();
		updateBoundingBox(vertex.x(), vertex.y(), vertex.z());
	}
	
	protected void setVertex(float x, float y, float z) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setVertex: too many vertices)");
			return;
		}
		vertices[vertexIndex + 0] = x;
		vertices[vertexIndex + 1] = y;
		vertices[vertexIndex + 2] = z;
		updateBoundingBox(x, y, z);
	}
	
	protected void setNormal(ARVector3 vertex) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setNormal: too many vertices)");
			return;
		}
		vertices[vertexIndex + normalOffset + 0] = vertex.x();
		vertices[vertexIndex + normalOffset + 1] = vertex.y();
		vertices[vertexIndex + normalOffset + 2] = vertex.z();
	}
	
	protected void setNormal(float x, float y, float z) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setNormal: too many vertices)");
			return;
		}
		vertices[vertexIndex + normalOffset + 0] = x;
		vertices[vertexIndex + normalOffset + 1] = y;
		vertices[vertexIndex + normalOffset + 2] = z;
	}
	
	protected void setTextureCoord(ARVector2 tex) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setTextureCoord: too many vertices)");
			return;
		}
		vertices[vertexIndex + textureOffset + 0] = tex.x();
		vertices[vertexIndex + textureOffset + 1] = tex.y();
	}
	
	protected void setTextureCoord(float u, float v) {
		if (vertexIndex >= vertexCount * vertexLength) {
			Log.e(TAG, "setTextureCoord: too many vertices)");
			return;
		}
		vertices[vertexIndex + textureOffset + 0] = u;
		vertices[vertexIndex + textureOffset + 1] = v;
	}
	
	
	protected void addTriangle(ARVector3 verts[], ARVector3 norms[], ARVector2 texCoords[])
	{
	    // Search for match - triangle consists of three verts
	    for(int iVertex = 0; iVertex < 3; iVertex++){
	        int vi;

	        for(vi = 0; vi < vertexIndex; vi += vertexLength) {
				if (!ARVector3.fuzzyCompare(vertices[vi + 0], verts[iVertex].x()))
					continue;
				if (!ARVector3.fuzzyCompare(vertices[vi + 1], verts[iVertex].y()))
					continue;
				if (!ARVector3.fuzzyCompare(vertices[vi + 2], verts[iVertex].z()))
					continue;

				if (!ARVector3.fuzzyCompare(vertices[vi + normalOffset + 0], norms[iVertex].x()))
					continue;
				if (!ARVector3.fuzzyCompare(vertices[vi + normalOffset + 1], norms[iVertex].y()))
					continue;
				if (!ARVector3.fuzzyCompare(vertices[vi + normalOffset + 2], norms[iVertex].z()))
					continue;

				if (!ARVector2.fuzzyCompare(vertices[vi + textureOffset + 0], texCoords[iVertex].x()))
					continue;
				if (!ARVector2.fuzzyCompare(vertices[vi + textureOffset + 1], texCoords[iVertex].y()))
					continue;

	 			// Then add the index only
				addIndex((short)(vi / vertexLength));
					break;
	        }
	            
	        // No match for this vertex, add to end of list
	        if ( vi == vertexIndex) {
				addIndex((short)(vi / vertexLength));
				setVertex(verts[iVertex]);
				setNormal(norms[iVertex]);
				setTextureCoord(texCoords[iVertex]);
				nextVertex();
	        }   
	    }
	}
	
	
	protected void startGeneration(int vertexCount, boolean normals, boolean textures) {
		vertexIndex = 0;
		this.vertexCount = vertexCount;
		vertexLength = 3;
		normalOffset = -1;
		textureOffset = -1;
		if (normals) {
			normalOffset = vertexLength;
			vertexLength += 3;
		}
		if (textures) {
			textureOffset = vertexLength;
			vertexLength += 2;
		}
		vertices = new float[vertexCount * vertexLength];
		indices = new int[vertexCount];
	}
	
	protected void endGeneration() {
    	vertexBuffer = ByteBuffer.allocateDirect(vertexCount * vertexLength * COMPONENT_SIZEOF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	vertexBuffer.put(vertices);
    	vertexBuffer.position(0);
    
	    if (indexIndex > 0) {
	    	indexBuffer = ByteBuffer.allocateDirect(indexIndex * COMPONENT_SIZEOF_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
	    	indexBuffer.put(indices);
	    	indexBuffer.position(0);
	    }
	}
	
	public ARVector3 getBoundPlus() {
		return boundPlus;
	}
	
	public ARVector3 getBoundMinus() {
		return boundMinus;
	}
	
	
	protected void draw(int drawMode)
	{
		if (shader == null) {
			Log.e(TAG, "Tried to draw with null shader");
			return;
		}

	//	Log.d(TAG, "loading shader");
		switch (shader.getType()) {
			case AndroidGLShader.SHADER_FLAT:
				shader.load(vertexBuffer, vertexLength * COMPONENT_SIZEOF_FLOAT, color[0], color[1], color[2], color[3]);
				break;

			case AndroidGLShader.SHADER_TEXTURE:
				shader.load(vertexBuffer, vertexLength * COMPONENT_SIZEOF_FLOAT, normalOffset, textureOffset, textureID);
				break;

			case AndroidGLShader.SHADER_ADS:
				shader.load(vertexBuffer, vertexLength * COMPONENT_SIZEOF_FLOAT, normalOffset, textureOffset, material);
				break;

			case AndroidGLShader.SHADER_ADSTEXTURE:
				shader.load(vertexBuffer, vertexLength * COMPONENT_SIZEOF_FLOAT, normalOffset, textureOffset, textureID, material);
				break;
			
			default:
				Log.e(TAG, "Invalid shader type " + shader.getType());
				return;

		}

		switch (drawMode) {
			case GLES20.GL_TRIANGLES:
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexIndex, GLES20.GL_UNSIGNED_INT, indexBuffer);
				break;

			case GLES20.GL_TRIANGLE_FAN:
				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
				break;

			case GLES20.GL_LINES:
				GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);
				break;

			default:
				Log.e(TAG, "Invalid draw mode " + drawMode);
				break;
		}
		AndroidGL.checkGLError(TAG, "glDrawXXX");
//		Log.d(TAG, "shader complete");
	}
	
	protected void updateBoundingBox(float x, float y, float z)
	{
		if (x < boundMinus.x())
			boundMinus.setX(x);

		if (y < boundMinus.y())
			boundMinus.setY(y);

		if (z < boundMinus.z())
			boundMinus.setZ(z);

		if (x > boundPlus.x())
			boundPlus.setX(x);

		if (y > boundPlus.y())
			boundPlus.setY(y);

		if (z > boundPlus.z())
			boundPlus.setZ(z);
	}
}
