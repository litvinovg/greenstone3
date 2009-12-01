/*
 *    GSHTML.java
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

/** GSHTML - provides some convenience methods for dealing
 * with html
 */
public class GSHTML {

    /** make a string html safe */
    public static String htmlSafe(String input) {

	StringBuffer filtered = new StringBuffer(input.length());
	char c;
	for (int i=0; i<input.length(); i++) {
	    c = input.charAt(i);
	    if (c == '<') {
		filtered.append("&lt;");
	    } else if (c == '>') {
		filtered.append("&gt;");
	    } else if (c == '"') {
		filtered.append("&quot;");
	    } else if (c == '&') {
		filtered.append("&amp;");
	    } else {
		filtered.append(c);
	    }
	}
	return(filtered.toString());
    }
    /** undo the html safe action */
    public static String htmlUnsafe(String input) {
	StringBuffer filtered = new StringBuffer(input.length());
	char c;
	for (int i=0; i<input.length(); i++) {
	    c = input.charAt(i);
	    if (c =='&') {
		int j=input.indexOf(';', i);
		String entity = input.substring(i, j);
		i=j;
		if (entity.equals("&amp")) {
		    filtered.append('&');
		} else if (entity.equals("&lt")){
		    filtered.append('<');
		} else if (entity.equals("&gt")){
		    filtered.append('>');
		} else if (entity.equals("&quot")) {
		    filtered.append('"');
		} // else just ignore it.
	    } else {
		filtered.append(c);
	    }

	}
	return(filtered.toString());
    }
    
    /** produce a default error page */
    public static String  errorPage(String error) {
	String page = "<html><head><Title>GSDL3 Error!</Title></head>\n"+
	    "<body><h1>Greenstone Error!</h1>"+
	    "<p/>"+error+"</body></html>";
	return page;
    }
	
}
