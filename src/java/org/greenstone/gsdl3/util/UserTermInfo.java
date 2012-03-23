/*
 *    UserTermInfo.java
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

public class UserTermInfo
{
	public String username = null;
	public String password = null;
	public String groups = null;
	public String accountstatus = null;
	public String comment = null;
	public String email = null;

	public String toString()
	{
		String result = "";
		result += "<username = " + username + ">";
		result += "<password = " + password + ">";
		result += "<groups = " + groups + ">";
		result += "<enable = " + accountstatus + ">";
		result += "<comment = " + comment + ">";
		return result;
	}
}
