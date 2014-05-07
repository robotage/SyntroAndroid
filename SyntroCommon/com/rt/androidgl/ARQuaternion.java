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

public class ARQuaternion {
	public static final String TAG = "ARQuaternion";
	
	public float[] vector = new float[4];
	
	public ARQuaternion() {
		vector[0] = 0;
		vector[1] = 0;
		vector[2] = 0;
		vector[3] = 0;
	}
	
	public ARQuaternion(float w, float x, float y, float z) {
		vector[0] = w;
		vector[1] = x;
		vector[2] = y;
		vector[3] = z;
	}
	
	public ARQuaternion(ARQuaternion quat) {
		vector[0] = quat.x();
		vector[1] = quat.x();
		vector[2] = quat.y();
		vector[3] = quat.z();
	}
	
	public float[] value() {
		return vector;
	}
	
	public float w() {
		return vector[0];
	}
	
	public float x() {
		return vector[1];
	}
	
	public float y() {
		return vector[2];
	}
	
	public float z() {
		return vector[3];
	}
	
	public void setW(float w) {
		vector[0] = w;
	}
	
	public void setX(float x) {
		vector[1] = x;
	}
	
	public void setY(float y) {
		vector[2] = y;
	}
	
	public void setZ(float z) {
		vector[3] = z;
	}
	
	public void set(float w, float x, float y, float z) {
		vector[0] = w;
		vector[1] = x;
		vector[2] = y;
		vector[3] = z;
	}
	
	public void set(ARQuaternion src) {
		vector[0] = src.w();
		vector[1] = src.x();
		vector[2] = src.y();
		vector[3] = src.z();
	}
	
	public float norm() {
		return (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2] + vector[3] * vector[3]);
	}
	
	public void normalize() {
		float length = norm();
		if (length == 0)
			return;
		vector[0] /= length;
		vector[1] /= length;
		vector[2] /= length;
		vector[3] /= length;
	}
	
	public void scale(float scaleFactor) {
		vector[0] *= scaleFactor;
		vector[1] *= scaleFactor;
		vector[2] *= scaleFactor;
		vector[3] *= scaleFactor;
	}
	
	public void add(ARQuaternion quat) {
		vector[0] += quat.w();
		vector[1] += quat.x();
		vector[2] += quat.y();
		vector[3] += quat.z();
	}
	
	public ARQuaternion conjugate() {
		ARQuaternion rv = new ARQuaternion(vector[0], -vector[1], -vector[2], -vector[3]);
		return rv;
	}
	
	public static ARQuaternion multiply(ARQuaternion a, ARQuaternion b) {
		ARQuaternion rv = new ARQuaternion();
		
		ARVector3 va = new ARVector3(a.x(), a.y(), a.z());
		ARVector3 vb = new ARVector3(b.x(), b.y(), b.z());
		
		float dotAB = ARVector3.dotProduct(va, vb);
		ARVector3 crossAB = ARVector3.crossProduct(va, vb);
		
		rv.setW(a.w() * b.w() - dotAB);
		rv.setX(a.w() * vb.x() + b.w() * va.x() + crossAB.x());
		rv.setY(a.w() * vb.y() + b.w() * va.y() + crossAB.y());
		rv.setZ(a.w() * vb.z() + b.w() * va.z() + crossAB.z());
		
		return rv;
	}
}
	
