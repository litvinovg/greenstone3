/*
 *    ServletRealmCheck.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Commandline script that is used by gliserver.pl to authenticate a username and password and 
 * return the user's groups, while the derby server is running. Because 2 JVM instances can't
 * access the same embedded derby server at the same time, gliserver can't call usersDB2txt.java.
 * If a collection parameter is additionally provided, this script will check the user's groups
 * to see if any of these allow the user to edit that collection.
 *
 * Run as java org.greenstone.gsdl3.util.ServletRealmCheck <GS3SRCHOME> <un> <pwd> [colname]
 *
 * GS3\src\java>"C:\Program Files\Java\jdk1.6.0_22\bin\java" 
 * 		-classpath "GS3\web\WEB-INF\lib\gsdl3.jar;GS3\web\WEB-INF\lib\derby.jar" 
 * 		org.greenstone.gsdl3.util.ServletRealmCheck "GS3" admin <pw> 2>&1
 *
 * Tries URL: http://hostname:8383/greenstone3/library?a=s&sa=authenticated-ping&excerptid=gs_content&un=admin&pw=<pw>[&col=demo] 
 * The &excerptid=gs_content in the URL will return just the <div id="gs_content" /> part of the 
 * page that we're interested in.
 *
 * Result: either prints out an error message ("Authentication failed...") or a positive result, 
 * which is the user's groups. For the admin user example: administrator,all-collections-editor.
 *
*/
public class ServletRealmCheck
{	
    public static void main(String[] args) {
	
	if (args.length < 3 || args.length > 4){
		System.out.println("Run with: <GSDL3SRCHOME> <un> <pwd> [collection-name]");	    
	    System.exit(0);
	}
	
	String gsdl3srchome = args[0];
	String username = args[1];
	String password = args[2];
	String collection = (args.length > 3) ? args[3] : null;		
	
	//System.err.println("gsdl3srchome: " + gsdl3srchome);
	//System.err.println("username: " + username);
	//System.err.println("password: " + password);
	//System.err.println("collection: " + collection);
	
	
	// Load the build.properties file, get the GS3 server URL and send authenticated-ping and print the return result
	
	//http://www.mkyong.com/java/java-properties-file-examples/
	Properties buildProps = new Properties();
	InputStream input = null;
 
	try {
		File buildPropsFile = new File(gsdl3srchome, "build.properties");
		input = new FileInputStream(buildPropsFile);
 
		// load a properties file
		buildProps.load(input);
 
		// get the property value and print it out
		String servername = buildProps.getProperty("tomcat.server");
		String port = buildProps.getProperty("tomcat.port");
		int portNum = Integer.parseInt(port);
		
		// Appending &excerptid=gs_content will get just the <div ... id="gs_content"/> from the final web page:
		String urlSuffix = "/greenstone3/library?a=s&sa=authenticated-ping&excerptid=gs_content&un="+username+"&pw="+password;
		if(collection != null) {
			urlSuffix = urlSuffix + "&col="+collection;
		}
		URL authenticationUrl = new URL("http", servername, portNum, urlSuffix);
		
		HttpURLConnection conn = (HttpURLConnection)authenticationUrl.openConnection();		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String result = "";
		String line = null;
		
		while((line = reader.readLine()) != null) {
			result += line;
		}
		
		//System.err.println("** Sent: " + authenticationUrl);
		//System.err.println("** Got result:\n" + result);
		
		// Parse out the content nested inside <div ... id="gs_content"> </div>
		int start = result.indexOf("id=\"gs_content\"");
		if(start != -1) {
			start = result.indexOf(">", start);
			int end = result.indexOf("<", start);
			result = result.substring(start+1, end);
			result = result.trim();
		}		
		
		// Now we finally have what we actually want to print out for the caller to use
		System.out.print(result); // don't add newline to end
		
	} catch (IOException ex) {
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
  }	
   
}
