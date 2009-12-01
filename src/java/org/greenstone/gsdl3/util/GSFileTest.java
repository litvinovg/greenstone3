/*
 *    GSFileTest.java
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

import junit.framework.*; 
import java.io.File;

public class GSFileTest extends TestCase {

    /** what type of test - used by the testing framework */
    public static final String TEST_ALL_TEST_TYPE="UNIT";

    public GSFileTest(String name) {
	super(name);
    }

    /** test suite that dynamically runs all the tests */
    public static Test suite() {
	return new TestSuite(GSFileTest.class);
    }

    public void testBase64Conversion() {
	
	// start off with reverse conversion - decode then encode
	String data = "sdkfjho9w4385khrf9ow834rfia8ufd38rhf9812";
	File outfile;
	try {
	    outfile = File.createTempFile("b64convert_test", "");
	} catch (java.io.IOException e) {
	    fail("couldn't create a temporary file in default temp dir");
	    return;
	}
	String filename = outfile.getAbsolutePath();
	
	String newdata;
	if (GSFile.base64DecodeToFile(data, filename)) {
	    newdata = GSFile.base64EncodeFromFile(filename);

	    assertEquals("decoded/encoded string does not match the origianl",
			 data, newdata);
	    // clean up
	    outfile.delete();
	} else {
	    fail("couldn't decode the data string");
	}

	//try with a real file???
	
    }

}
