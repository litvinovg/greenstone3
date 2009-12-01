/*
 * Copyright (C) 1997 Pharos IP Pty Ltd
 * $Id$
 * $Source$
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package au.com.pharos.test;

/** Utilities for automated testing.  This class contains only static
 * functions and should not be instantiated.
 *
 * @author Martin Pool
 * @version $Revision$ $Date$
 **/
// TODO: Somehow automatically generate sequence numbers, or perhaps use
// strings to report progress
public class Test {
    // Don't instantiate
    private Test() {}

    static int nPassed, nFailed;

    /** Register that a particular test executed successfully, and
     * output a success or failure message.
     *
     * @param number test sequence number
     **/
    static public void ok(int no) {
	ok(no, true);
    }

    /** Register the result of a test, and output a success or failure
     * message.
     *
     * @param number test sequence number
     * @param result whether the test was successful or not
     */
    static public void ok(int number, boolean passed) {
	if (passed) {
	    nPassed++;
	}
	else {
	    System.out.print("not ");
	    nFailed++;
	}
	System.out.print("ok " + number + "\n");
    }

    /**
     * Print a summary of test results.
     **/
    static public void summary() {
	System.out.print("test summary: " + nPassed + " passed, " +
			 nFailed + " failed");
    }
}
    
