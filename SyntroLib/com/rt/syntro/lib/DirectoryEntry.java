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

import java.util.ArrayList;

public class DirectoryEntry {
	private String raw = new String();
	private String uid = new String();
	private String name = new String();
	private String type = new String();
	private ArrayList<String> multicastServices = new ArrayList<String>();
	private ArrayList<String> e2eServices = new ArrayList<String>();
	
	public DirectoryEntry() {
		raw = new String();
	}
	
	public DirectoryEntry(String dirLine)
	{
		setLine(dirLine);
	}
		
	public void setLine(String dirLine)
	{
		uid= "";
		name = "";
		type = "";
		multicastServices.clear();
		e2eServices.clear();

		raw = dirLine;

		parseLine();
	}

	public boolean isValid()
	{
		if ((uid.length() > 0) && (name.length() > 0) && (type.length() > 0))
			return true;

		return false;
	}

	public String uid()
	{
		return uid;
	}

	public String appName()
	{
		return name;
	}

	public String componentType()
	{
		return type;
	}

	public ArrayList<String> multicastServices()
	{
		return multicastServices;
	}

	public ArrayList<String> e2eServices()
	{
		return e2eServices;
	}

	private void parseLine()
	{
		if (raw.length() == 0)
			return;

		uid = element(SyntroDefs.DETAG_UID);
		name = element(SyntroDefs.DETAG_APPNAME);
		type = element(SyntroDefs.DETAG_COMPTYPE);
		multicastServices = elements(SyntroDefs.DETAG_MSERVICE);
		e2eServices = elements(SyntroDefs.DETAG_ESERVICE);
	}

	private String element(String name)
	{
		String element = new String();

		String start= new String("<" + name + ">");
		String end = new String("</" + name + ">");

		int i = raw.indexOf(start);
		int j = raw.indexOf(end);

		if ((i >= 0) && (j > (i + start.length())))
			element = raw.substring(i + start.length(), j);

		return element;
	}

	ArrayList<String> elements(String name)
	{
		ArrayList<String> elements = new ArrayList<String>();
		int pos = 0;

		String start= new String("<" + name + ">");
		String end = new String("</" + name + ">");

		int i = raw.indexOf(start, pos);

		while (i >= 0) {
			int j = raw.indexOf(end, pos);

			if (j > i + start.length())
				elements.add(raw.substring(i + start.length(), j));

			pos = j + end.length();

			i = raw.indexOf(start, pos);
		}

		return elements;
	}

}
