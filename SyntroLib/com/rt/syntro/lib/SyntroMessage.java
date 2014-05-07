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

public class SyntroMessage {
	private byte 	message[];								// the message itself
	private int		datalen;								// the length of the message data part
		
	public SyntroMessage(int cmd, int flags, int len) {
		message = new byte[SyntroDefs.SYNTRO_MESSAGE_LENGTH + len];
		message[SyntroDefs.SYNTRO_MESSAGE_CMD] = (byte)cmd;
		SyntroUtils.convertIntToUC4(SyntroDefs.SYNTRO_MESSAGE_LENGTH + len, message, SyntroDefs.SYNTRO_MESSAGE_TOTAL_LENGTH);
		message[SyntroDefs.SYNTRO_MESSAGE_FLAGS] = (byte)flags;
		
		datalen = len;
		
		int cksm = 0;
		for (int i = 0; i < SyntroDefs.SYNTRO_MESSAGE_LENGTH; i++) {
			cksm += ((int)message[i]) & 0xff;				// accumulate checksum
			cksm &= 0xff;
		}
		message[SyntroDefs.SYNTRO_MESSAGE_CKSM] =  (byte)(256 - cksm);
	}

	public SyntroMessage(byte[] hdr) {
		datalen = SyntroUtils.convertUC4ToInt(hdr, SyntroDefs.SYNTRO_MESSAGE_TOTAL_LENGTH) - SyntroDefs.SYNTRO_MESSAGE_LENGTH;
		if (datalen < 0)
			datalen = 0;
		message = new byte[SyntroDefs.SYNTRO_MESSAGE_LENGTH + datalen];
		System.arraycopy(hdr, 0, message, 0, SyntroDefs.SYNTRO_MESSAGE_LENGTH);
	}
	
	public void setHeaderData(int cmd, int flags) {
		message[SyntroDefs.SYNTRO_MESSAGE_CMD] = (byte)cmd;
		SyntroUtils.convertIntToUC4(message.length, message, SyntroDefs.SYNTRO_MESSAGE_TOTAL_LENGTH);
		message[SyntroDefs.SYNTRO_MESSAGE_FLAGS] = (byte)flags;
		int cksm = 0;
		for (int i = 0; i < SyntroDefs.SYNTRO_MESSAGE_LENGTH; i++) {
			cksm += ((int)message[i]) & 0xff;				// accumulate checksum
			cksm &= 0xff;
		}
		message[SyntroDefs.SYNTRO_MESSAGE_CKSM] =  (byte)(256 - cksm);		
	}
	
	public SyntroMessage(int len) {
		message = new byte[SyntroDefs.SYNTRO_MESSAGE_LENGTH + len];
		datalen = len;
	}


	public byte[] get() {
		return message;
	}
	
	public int length() {
		return message.length;
	}
	
	public int getDataLength() {
		return datalen;
	}
	
	public int getCmd() {
		return (int)message[SyntroDefs.SYNTRO_MESSAGE_CMD];
	}
	
	public static boolean checkChecksum(byte[] hdr) {
		int cksm = 0;
		
		for (int i = 0; i < SyntroDefs.SYNTRO_MESSAGE_LENGTH; i++) {
			cksm += (int)hdr[i] & 0xff;
			cksm &= 0xff;
		}
		return cksm == 0;
	}
	

}
