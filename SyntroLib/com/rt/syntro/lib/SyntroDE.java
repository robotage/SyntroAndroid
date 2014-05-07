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

public class SyntroDE {
	
	private String de;								// the directory entry string itself
	
	public SyntroDE() {
		de = "";
	}
	
	public void DESetup(String appName, String compType, String uidStr) {
		de = SyntroDefs.DETAG_CMP;
		DEAddValue(SyntroDefs.DETAG_UID, uidStr);
		DEAddValue(SyntroDefs.DETAG_APPNAME, appName);
		DEAddValue(SyntroDefs.DETAG_COMPTYPE, compType);
	}
	
	public void DEComplete() {
		de += SyntroDefs.DETAG_CMP_END;
	}
	
	public void DEAddValue(String tag, String value) {
		de += String.format("<%1$s>%2$s</%3$s>\n", tag, value, tag);
	}
	
	public int length() {
		return de.length();									
	}

	public byte[] get() {
		return de.getBytes();
	}
	
	public String getString() {
		return de;
	}
}
