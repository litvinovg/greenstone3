/*
 *    DBHelper.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import java.util.HashMap;

public class DBHelper
{
	protected static final HashMap<String, String> _extMap;
	static 
	{
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put("jdbm", ".jdb");
		extMap.put("gdbm", ".gdb");
		_extMap = extMap;
	}
	
	public static String getDBExtFromDBType(String dbtype)
	{
		return _extMap.get(dbtype);
	}
}