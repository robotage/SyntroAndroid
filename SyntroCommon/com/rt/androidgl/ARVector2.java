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


public class ARVector2 {
	public static final String TAG = "ARVector2";
	
	public float[] vector = new float[2];
	
	public ARVector2() {
		vector[0] = 0;
		vector[1] = 0;
	}
	
	public ARVector2(float x, float y) {
		vector[0] = x;
		vector[1] = y;
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
	
	public void setX(float x) {
		vector[0] = x;
	}
	
	public void setY(float y) {
		vector[1] = y;
	}
	
	public void set(float x, float y) {
		vector[0] = x;
		vector[1] = y;
	}
	
	public void set(ARVector2 src) {
		vector[0] = src.x();
		vector[1] = src.y();
	}
	
	public float length() {
		return (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
	}
	
	public void normalize() {
		float length = length();
		if (length == 0)
			return;
		vector[0] /= length;
		vector[1] /= length;
	}
	
	public static float dotProduct(ARVector2 a, ARVector2 b) {
		return a.x() * b.x() + a.y() * b.y() ;
	}	
	
	public static void add(ARVector2 rv, ARVector2 a, ARVector2 b) {
		rv.setX(a.x() + b.x());
		rv.setY(a.y() + b.y());
	}
	
	public static void subtract(ARVector2 rv, ARVector2 a, ARVector2 b) {
		rv.setX(a.x() - b.x());
		rv.setY(a.y() - b.y());
	}
	
	public static ARVector2 add(ARVector2 a, ARVector2 b) {
		ARVector2 rv = new ARVector2();
		
		rv.setX(a.x() + b.x());
		rv.setY(a.y() + b.y());
		return rv;
	}
	
	public static ARVector2 subtract(ARVector2 a, ARVector2 b) {
		ARVector2 rv = new ARVector2();

		rv.setX(a.x() - b.x());
		rv.setY(a.y() - b.y());
		return rv;
	}
	
	public static ARVector2 scale(ARVector2 a, float scale) {
		ARVector2 rv = new ARVector2();
		
		rv.vector[0] = a.x() * scale;
		rv.vector[1] = a.y() * scale;
		return rv;
	}
	
	public static boolean fuzzyCompare(float a, float b) {
		float delta = a - b;
		
		return Math.abs(delta) < 0.000001f;
	}
}
