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

package com.rt.syntro.lib;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;


import android.util.Log;

public class SyntroUtils {
	public static final String TRACE_TAG = "SyntroUtils: ";
	
	public static String displayIPAddr(byte addr[], int offset) {
		return String.format(Locale.getDefault(), "%1$d.%2$d.%3$d.%4$d", 
				((int)addr[offset + 0] & 0xff), 
				((int)addr[offset + 1] & 0xff), 
				((int)addr[offset + 2] & 0xff), 
				((int)addr[offset + 3] & 0xff));
	}
	
	public static byte[] MACAddrStringToMACAddr(String str) {
		String[] bytestrings = str.split(":");
		byte[] data = new byte[SyntroDefs.SYNTRO_MACADDR_LEN];
		
		for (int i = 0; i < data.length; i++) {
			BigInteger temp = new BigInteger(bytestrings[i], 16);
			byte[] raw = temp.toByteArray();
			data[i] = raw[raw.length - 1];
		}
		return data;
	}
	
	public static int convertLongToUC8(long val, byte[] b, int offset) {
		b[offset++] = (byte)((val >> 56) & 0xff);
		b[offset++] = (byte)((val >> 48) & 0xff);
		b[offset++] = (byte)((val >> 40) & 0xff);
		b[offset++] = (byte)((val >> 32) & 0xff);
		b[offset++] = (byte)((val >> 24) & 0xff);
		b[offset++] = (byte)((val >> 16) & 0xff);
		b[offset++] = (byte)((val >> 8) & 0xff);
		b[offset++] = (byte)(val & 0xff);
		return offset;
	}
	
	public static long convertUC8ToLong(byte[] b, int offset) {
		long val;
		
		val =  ((long)(b[offset + 7])) & 0xff;
		val += (((long)(b[offset + 6])) & 0xff) << 8;
		val += (((long)(b[offset + 5])) & 0xff) << 16;
		val += (((long)(b[offset + 4])) & 0xff) << 24;
		val += (((long)(b[offset + 3])) & 0xff) << 32;
		val += (((long)(b[offset + 2])) & 0xff) << 40;
		val += (((long)(b[offset + 1])) & 0xff) << 48;
		val += (((long)(b[offset + 0])) & 0xff) << 56;
		return val;
	}

	public static int convertIntToUC4(int val, byte[] b, int offset) {
		b[offset++] = (byte)((val >> 24) & 0xff);
		b[offset++] = (byte)((val >> 16) & 0xff);
		b[offset++] = (byte)((val >> 8) & 0xff);
		b[offset++] = (byte)(val & 0xff);
		return offset;
	}
	
	public static int convertUC4ToInt(byte[] b, int offset) {
		int val;
		
		val =  ((int)(b[offset + 3])) & 0xff;
		val += (((int)(b[offset + 2])) & 0xff) << 8;
		val += (((int)(b[offset + 1])) & 0xff) << 16;
		val += (((int)(b[offset + 0])) & 0xff) << 24;
		return val;
	}
	
	public static float convertUC4ToFloat(byte[] b, int offset) {
		float val = ByteBuffer.wrap(b, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();	
		return val;
	}
	
	public static int convertIntToUC2(int val, byte[] b, int offset) {
		b[offset++] = (byte)((val >> 8) & 0xff);
		b[offset++] = (byte)(val & 0xff);
		return offset;
	}

	public static int convertUC2ToInt(byte[] b, int offset) {
		int val;
		
		val =  ((int)(b[offset + 1])) & 0xff;
		val += (((int)(b[offset + 0])) & 0xff) << 8;
		return val;
	}

	public static String displayUID(byte[] uid, int offset) {
		String uidstr = new String();
		try {
			for (int i = 0; i < SyntroDefs.SYNTRO_UID_LEN; i++)
				uidstr += String.format("%1$x", uid[offset + i]); 
		} catch (Exception e) {
			Log.e(TRACE_TAG, "error in uid getString " + e);
			return "uiderror";
		}
				
		return uidstr;
	}
	
	public static boolean nameMatch(byte[] a, int aOffset, byte[] b, int bOffset) {
		try {
			for (int i = 0; i < SyntroDefs.SYNTRO_MAX_NAME; i++) {
				if (a[aOffset + i] != b[bOffset + i])
					return false;
				if (a[aOffset + i] == 0)
					return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static String displayName(byte[]a, int offset) {
		int length;
		
		for (length = 0; length < SyntroDefs.SYNTRO_MAX_NAME - 1; length++)
			if (a[offset + length] == 0)
				break;
		String str = new String(a, offset, length);
		return str;
	}
	
	public static void setName(String name, byte[] a, int offset) {
		byte[] b = name.getBytes();
		
		if (b.length >= SyntroDefs.SYNTRO_MAX_NAME) {
			Log.e(TRACE_TAG, "Tried to set name too long for field " + name);
			return;
		}
		System.arraycopy(b, 0, a, offset, b.length);
		a[offset + b.length] = 0;
	}
	
	public static boolean isSendOK(byte seq, byte ack)
	{
		return (seq - ack) < SyntroDefs.SYNTRO_MAX_WINDOW;
	}
	
	public static void removeStreamNameFromPath(String servicePath, String[] serviceSourceName)
	{
		int start, end;

		start = servicePath.indexOf(SyntroDefs.SYNTRO_SERVICEPATH_SEP);	// find the "/"
		if (start == -1) {										// not found
			serviceSourceName[0] = servicePath;
			serviceSourceName[1] = "";
			return;
		}
		end = servicePath.indexOf(SyntroDefs.SYNTRO_STREAM_TYPE_SEP);	// find the ":" if there is one

		if (end == -1) {										// there isn't
			serviceSourceName[0] = servicePath.substring(0, start);
			serviceSourceName[1] = servicePath.substring(start + 1);
			return;
		}

	 // We have all parts present

		serviceSourceName[0] = servicePath.substring(0, start) + servicePath.substring(end);
		serviceSourceName[1] = servicePath.substring(start + 1, end);
	}
	
	public static String insertStreamNameInPath(String streamSource, String streamName)
	{
		 int index;
		 String result;

		 index = streamSource.indexOf(SyntroDefs.SYNTRO_STREAM_TYPE_SEP);
		 if (index == -1) {
			 // there is no extension - just add stream name to the end
			 result = streamSource + "/" + streamName;
		 } else {
			 // there is an extension - insert stream name before extension
			 String sep = new String("") + SyntroDefs.SYNTRO_STREAM_TYPE_SEP;
			 result = streamSource.replace(sep, 
					 new String("/") + streamName + SyntroDefs.SYNTRO_STREAM_TYPE_SEP);
		 }
		 Log.d(TRACE_TAG, "Processed stream name " + result);
		 return result;
	}

}
