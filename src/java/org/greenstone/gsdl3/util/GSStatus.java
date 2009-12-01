/*
 *    GSStatus.java
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

public class GSStatus {

    // responses for initial request
    public static final int SUCCESS = 1; // request succeeded
    public static final int ACCEPTED = 2; // request accepted but not completed
    public static final int ERROR = 3; // error and process stopped

    // responses for status requests
    public static final int CONTINUING = 10; // request still continuing
    public static final int COMPLETED = 11; // request finished
    public static final int HALTED = 12; // process stopped
	    
    // other
    public static final int INFO = 20; // just an info message that doesnt mean anything

    /** returns true if teh code indicates that a process is no longer running 
     */
    public static boolean isCompleted(int code) {
	if (code == SUCCESS || code == ERROR || code == COMPLETED || code == HALTED) {
	    return true;
	} 
	return false;
    }

    // there may be other types of error codes
    public static boolean isError(int code) {
	if (code == ERROR) {
	    return true;
	}
	return false;
    }

}

