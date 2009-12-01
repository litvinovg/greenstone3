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

public class UserTermInfo{
    public String username_=null;
    public String password_=null;
    public String groups_=null;
    public String accountstatus_=null;
    public String comment_=null;
    
    public String toString(){
	String result="";
	result +="<username = "+username_+">";
	result +="<password = "+password_+">";
	result +="<groups = "+groups_+">";
	result +="<enable = "+accountstatus_+">";
	result +="<comment = "+comment_+">";
	return result;
    }
}
