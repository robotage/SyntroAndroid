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

public class SyntroParams {
	public final byte[] controlname = new byte[SyntroDefs.SYNTRO_MAX_APPNAME];	// the name of the SyntroControl to use (blank if any
	public final byte[] appType = new byte[SyntroDefs.SYNTRO_MAX_APPTYPE];		// the type of this app
	public final byte[] appName = new byte[SyntroDefs.SYNTRO_MAX_APPNAME];		// the name of this app
	public final byte[] compType = new byte[SyntroDefs.SYNTRO_MAX_COMPTYPE];	// the type of this component
}
