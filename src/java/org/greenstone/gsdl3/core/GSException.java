/*
 *    GSException.java
 *    a new exception for Greenstone

 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.core;

import org.greenstone.gsdl3.util.GSXML;

public class GSException
    extends Exception {

    private String exception_type = null;
    // need some better names for these
    //public static int PERMANENT = 0;
    //public static int TEMPORARY = 1;
    //public static int OTHER = 2;

    public GSException(String message) {
	
	super(message);
	this.exception_type = GSXML.ERROR_TYPE_OTHER;
    }
    public GSException(String message, String type) {

	super(message);
	this.exception_type = type;
    }

    public String getType() {
	return exception_type;
    }
	

}
