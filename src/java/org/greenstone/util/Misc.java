 /*
 *    Misc.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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

package org.greenstone.util;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/** contains miscellaneous functions */
public class Misc {

    // Can initialise static final vars on declaration or in static initialisation code block
    // http://stackoverflow.com/questions/2339932/java-can-final-variables-be-initialized-in-static-initialization-block
    // Initialise object member final vars on declaration or in constructors
    public static final String NEWLINE;
    
    // Before Java 7, no System.lineSeparator() or System.getProperty("line.separator")
    // And on local linux, am compiling with JDK 6, so need this.
    // http://stackoverflow.com/questions/207947/how-do-i-get-a-platform-dependent-new-line-character

    static {
	// http://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
	// https://www.tutorialspoint.com/java/lang/package_getspecificationversion.htm
	
	double java_version = Double.parseDouble(System.getProperty("java.specification.version"));
	if(java_version >= 1.7) { // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
	    NEWLINE = System.getProperty("line.separator");
	} else {
	    NEWLINE = isWindows() ? "\r\n" : "\n";
	}	
    }

    public static void printHash(HashMap map) {
	Set entries = map.entrySet();
	Iterator i = entries.iterator();
	while (i.hasNext()) {
	    Map.Entry m = (Map.Entry)i.next();
	    String name = (String)m.getKey();
	    String value = (String)m.getValue();
	}  
    }
    
    /** Method to determine if the host system is Windows based.
     * @return a boolean which is true if the platform is Windows, 
     * false otherwise
     */
    public static boolean isWindows() {
	Properties props = System.getProperties();
	String os_name = props.getProperty("os.name","");
	if(os_name.startsWith("Windows")) {
	    return true;
	}
	return false;
    }
    
    public static boolean isWindows9x() {
        Properties props = System.getProperties();
        String os_name = props.getProperty("os.name","");
        if(os_name.startsWith("Windows") && os_name.indexOf("9") != -1) {
            return true;
        }
        return false;
    }

    /** Method to determine if the host system is MacOS based.
     * @return a boolean which is true if the platform is MacOS, false otherwise
     */
    public static boolean isMac() {
        Properties props = System.getProperties();
        String os_name = props.getProperty("os.name","");
        if(os_name.startsWith("Mac OS")) {
            return true;
        }
        return false;
    }

    public static String getGsdlOS() {
		if (isWindows()) {
			return "windows";
		}
        if (isMac()) {
			return "darwin";
		}
		return "linux";
    }
    
    public static boolean isBigEndian() {

	if (System.getProperty("sun.cpu.endian").equals("big")) {
	    return true;
	}
	return false;

    }

    public static BufferedReader makeHttpConnection(String url_string)
	throws java.net.MalformedURLException, java.io.IOException {
	BufferedReader reader = null;
	URL url = new URL(url_string);
	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	InputStream input = connection.getInputStream();
	reader = new BufferedReader(new InputStreamReader(input));  
	return reader;
    }

    public static void main(String [] args) {
	isBigEndian();
    }

}

