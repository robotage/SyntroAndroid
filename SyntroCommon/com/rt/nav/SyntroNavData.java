
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

package com.rt.nav;

import android.util.Log;

import com.rt.syntro.lib.SyntroDefs;
import com.rt.syntro.lib.SyntroMessage;
import com.rt.syntro.lib.SyntroUtils;
import com.rt.androidgl.ARQuaternion;
import com.rt.androidgl.ARVector3;

public class SyntroNavData {
	
	public static final String TAG = "SyntroNavData";
	
	//	valid field masks
	
	public static final int SYNTRO_NAVDATA_VALID_FUSIONPOSE = 	0x0001;
	public static final int SYNTRO_NAVDATA_VALID_FUSIONQPOSE = 	0x0002;
	public static final int SYNTRO_NAVDATA_VALID_GYRO = 		0x0004;
	public static final int SYNTRO_NAVDATA_VALID_ACCEL = 		0x0008;
	public static final int SYNTRO_NAVDATA_VALID_COMPASS = 		0x0010;
	public static final int SYNTRO_NAVDATA_VALID_PRESSURE = 	0x0020;
	public static final int SYNTRO_NAVDATA_VALID_TEMPERATURE = 	0x0040;
	public static final int SYNTRO_NAVDATA_VALID_HUMIDITY = 	0x0080;

	// The NAVDATA fields
	
	public static final int SYNTRO_NAVDATA_TIMESTAMP = 0;		// timestamp with microsecond resolution
	public static final int SYNTRO_NAVDATA_VALID_FIELDS = 8;	// valid field bits
	public static final int SYNTRO_NAVDATA_SPACE = 10;			// spare bytes
	public static final int SYNTRO_NAVDATA_FUSION_POSE = 12;	// fusion pose as Euler angles in radians
	public static final int SYNTRO_NAVDATA_FUSION_QPOSE = 24;	// fusion pose as normalized quaternions
	public static final int SYNTRO_NAVDATA_GYRO = 40;			// gyro outputs in radians per second
	public static final int SYNTRO_NAVDATA_ACCEL = 52;			// accel outputs in gs
	public static final int SYNTRO_NAVDATA_COMPASS = 64;		// compass outputs in uT
	public static final int SYNTRO_NAVDATA_PRESSURE = 76;		// pressure in mbars
	public static final int SYNTRO_NAVDATA_TEMPERATURE = 80;	// temperature in degrees C
	public static final int SYNTRO_NAVDATA_HUMIDITY = 84;		// % RH
	
	public static final int SYNTRO_NAVDATA_LENGTH = 88;			// total length of data

	// cracked values
	
	public SyntroMessage message;							// the message itself
	public byte seqno;										// the sequence number
	public long timestamp;									// the record timestamp
	
	public int validFields;									// indicates which fields contain valid data
	public ARVector3 fusionPose = new ARVector3();			// the fused pose as Euler angles in radians
	public ARQuaternion fusionQPose = new ARQuaternion(); 	// the fused pose as a normalized quaternion
	public ARVector3 gyro = new ARVector3();                // the gyro outputs in radians per second
	public ARVector3 accel = new ARVector3();               // the accel outputs in gs
	public ARVector3 compass = new ARVector3();             // the compass outputs in uT
	public float pressure;                 					// pressure in mbars
	public float temperature;              					// temperature in degrees C
	public float humidity;                 					// %RH
	
	public boolean crackNavData(SyntroMessage message) {
		
		int ptr;
				
		this.message = message;
		if (message.getDataLength() < (SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH + SYNTRO_NAVDATA_LENGTH)) {
			Log.e(TAG, "Got nav packet but was too short " + message.getDataLength());
			return false;
		}
		
		try {
			//	Get stuff from Ehead
			
			ptr = SyntroDefs.SYNTRO_MESSAGE_LENGTH;			// start of ehead
			
			seqno = message.get()[ptr + SyntroDefs.SYNTRO_EHEAD_SEQ];
			
			// get stuff from record header
			
			ptr += SyntroDefs.SYNTRO_EHEAD_LENGTH;
					
			int type = SyntroUtils.convertUC2ToInt(message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TYPE);
			if (type != SyntroDefs.SYNTRO_RECORD_TYPE_NAV) {
				Log.e(TAG, "Record is not nav type " + type);
				return false;
			}
			
			timestamp = SyntroUtils.convertUC8ToLong(message.get(), ptr + SyntroDefs.SYNTRO_RECORD_HEADER_TIMESTAMP);

			ptr += SyntroDefs.SYNTRO_RECORD_HEADER_LENGTH;

			validFields = SyntroUtils.convertUC2ToInt(message.get(), ptr + SYNTRO_NAVDATA_VALID_FIELDS);
			
			for (int i = 0; i < 3; i++)
				fusionPose.vector[i] = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_FUSION_POSE + 4 * i);
			
			for (int i = 0; i < 4; i++)
				fusionQPose.vector[i] = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_FUSION_QPOSE + 4 * i);
			
			for (int i = 0; i < 3; i++)
				gyro.vector[i] = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_GYRO + 4 * i);

			for (int i = 0; i < 3; i++)
				accel.vector[i] = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_ACCEL + 4 * i);

			for (int i = 0; i < 3; i++)
				compass.vector[i] = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_COMPASS + 4 * i);
			
			pressure = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_PRESSURE);
			temperature = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_TEMPERATURE);
			humidity = SyntroUtils.convertUC4ToFloat(message.get(), ptr + SYNTRO_NAVDATA_HUMIDITY);

		} catch (Exception e) {
			Log.e(TAG, "Failed to crack nav data " + e);
			return false;
		}
		
		return true;
	}
}
