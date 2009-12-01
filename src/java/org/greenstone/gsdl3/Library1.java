/*
 *    Library1.java
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
package org.greenstone.gsdl3;

import org.greenstone.gsdl3.core.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/** sample command line interface to gsdl3 site
 *
 * enter XML queries at the prompt
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */
final public class Library1 {

    public static void main(String args[]) {

	if (args.length != 1) {
	    System.out.println("Usage: Library1 <sitename>");
	    System.exit(1);
	}
	String site_name = args[0];

	MessageRouter mr = new MessageRouter();
	mr.setSiteName(site_name);
	mr.configure();
	
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String query=null;
	String result=null;
	while (true) {
	    System.out.println("Please enter an XML query all on one line, or 'exit' to quit");
	    try {
		query = br.readLine();
	    } catch (Exception e) {
		System.err.println("Library1 exception:"+e.getMessage());
	    }
	    if (query.startsWith("exit")) {
		System.exit(1);
	    }
	    System.out.println("\nProcessing query");
	    
	    result = mr.process(query);

	    System.out.println("Result:\n"+result+"\n");


	}
       
    }
}
