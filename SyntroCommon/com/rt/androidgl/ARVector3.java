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

import android.opengl.Matrix;

public class ARVector3 {
	public static final String TAG = "ARVector3";
	
	public float[] vector = new float[3];
	
	public ARVector3() {
		vector[0] = 0;
		vector[1] = 0;
		vector[2] = 0;
	}
	
	public ARVector3(float x, float y, float z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}
	
	public ARVector3(ARVector3 vec) {
		vector[0] = vec.x();
		vector[1] = vec.y();
		vector[2] = vec.z();
	}
	
	public float[] value() {
		return vector;
	}
	
	public float x() {
		return vector[0];
	}
	
	public float y() {
		return vector[1];
	}
	
	public float z() {
		return vector[2];
	}
	
	public void setX(float x) {
		vector[0] = x;
	}
	
	public void setY(float y) {
		vector[1] = y;
	}
	
	public void setZ(float z) {
		vector[2] = z;
	}
	
	public void set(float x, float y, float z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}
	
	public void set(ARVector3 src) {
		vector[0] = src.x();
		vector[1] = src.y();
		vector[2] = src.z();
	}
	
	public float length() {
		return Matrix.length(vector[0], vector[1], vector[2]);
	}
	
	public void normalize() {
		float length = length();
		if (length == 0)
			return;
		vector[0] /= length;
		vector[1] /= length;
		vector[2] /= length;
	}
	
	public static float dotProduct(ARVector3 a, ARVector3 b) {
		return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
	}	
	
	public static void crossProduct(ARVector3 rv, ARVector3 a, ARVector3 b) {
		
		rv.setX(a.y() * b.z() - a.z() * b.y());
		rv.setY(a.z() * b.x() - a.x() * b.z());
		rv.setZ(a.x() * b.y() - a.y() * b.x());	
	}
	
	public static ARVector3 crossProduct(ARVector3 a, ARVector3 b) {
		ARVector3 rv = new ARVector3();
		
		rv.setX(a.y() * b.z() - a.z() * b.y());
		rv.setY(a.z() * b.x() - a.x() * b.z());
		rv.setZ(a.x() * b.y() - a.y() * b.x());	
		
		return rv;
	}	
	
	public static void add(ARVector3 rv, ARVector3 a, ARVector3 b) {
		rv.setX(a.x() + b.x());
		rv.setY(a.y() + b.y());
		rv.setZ(a.z() + b.z());
	}
	
	public static void subtract(ARVector3 rv, ARVector3 a, ARVector3 b) {
		rv.setX(a.x() - b.x());
		rv.setY(a.y() - b.y());
		rv.setZ(a.z() - b.z());
	}
	
	public static ARVector3 add(ARVector3 a, ARVector3 b) {
		ARVector3 rv = new ARVector3();
		
		rv.setX(a.x() + b.x());
		rv.setY(a.y() + b.y());
		rv.setZ(a.z() + b.z());
		
		return rv;
	}
	
	public static ARVector3 subtract(ARVector3 a, ARVector3 b) {
		ARVector3 rv = new ARVector3();

		rv.setX(a.x() - b.x());
		rv.setY(a.y() - b.y());
		rv.setZ(a.z() - b.z());
		
		return rv;
	}
	
	public static ARVector3 scale(ARVector3 a, float scale) {
		ARVector3 rv = new ARVector3();
		
		rv.vector[0] = a.x() * scale;
		rv.vector[1] = a.y() * scale;
		rv.vector[2] = a.z() * scale;
		
		return rv;
	}
	
	public ARVector3 map(float[] m) {
		ARVector3 rv = new ARVector3();
		float x, y, z, w;
		
		x = m[0] * vector[0] + m[4] * vector[1] + m[8] * vector[2] + m[12];
		y = m[1] * vector[0] + m[5] * vector[1] + m[9] * vector[2] + m[13];
		z = m[2] * vector[0] + m[6] * vector[1] + m[10] * vector[2] + m[14];
		w = m[3] * vector[0] + m[7] * vector[1] + m[11] * vector[2] + m[15];
		if (w != 1.0f) {
			x /= w;
			y /= w;
			z /= w;
		}
		rv.setX(x);
		rv.setY(y);
		rv.setZ(z);
		return rv;
	}
	
	public static boolean fuzzyCompare(float a, float b) {
		float delta = a - b;
		
		return Math.abs(delta) < 0.000001f;
	}
}
