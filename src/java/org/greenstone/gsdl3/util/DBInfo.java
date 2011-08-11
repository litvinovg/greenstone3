/*
 *    DBInfo.java
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

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;

/**
 * class to hold info from a gdbm (or equiv) query maps a String key to a list
 * of Strings (values) at the moment, the user must know if something has a
 * single value or not
 */
public class DBInfo
{
	protected HashMap info_map_;

	public DBInfo()
	{
		info_map_ = new HashMap();
	}

	// methods for keys that can have a single value

	/** set the value for a key - replaces any existing value */
	public void setInfo(String key, String value)
	{
		Vector v = new Vector();
		v.add(value);
		info_map_.put(key, v);
	}

	/**
	 * get the value - used for keys that have one value, otherwise just returns
	 * the first one
	 */
	public String getInfo(String key)
	{
		Vector items = (Vector) info_map_.get(key);
		if (items == null)
		{
			return "";
		}
		return (String) items.firstElement();
	}

	// methods for keys that can have multiple values

	/** add a value to a key - for keys that can have multiple values */
	public void addInfo(String key, String value)
	{
		Vector v = (Vector) info_map_.get(key);
		if (v == null)
		{
			v = new Vector();
		}
		v.add(value);
		info_map_.put(key, v);
	}

	/**
	 * return a vector of all values associated with a key
	 * 
	 * @return Vector of Strings
	 */
	public Vector getMultiInfo(String key)
	{
		return (Vector) info_map_.get(key);
	}

	/** returns a list of all the keys in the info */
	public Set getKeys()
	{
		return info_map_.keySet();
	}

	/** returns the Info as a string */
	public String toString()
	{
		return info_map_.toString();

	}
}
