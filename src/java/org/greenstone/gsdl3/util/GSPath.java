/*
 *    GSPath.java
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
package org.greenstone.gsdl3.util;

/**
 * GSPath - utility class for greenstone
 *
 * modifies and examines message address paths and names
 *
 * @author Katherine Don
 * @version $Revision$
 *
 */

public class GSPath {

    /** adds new_link to the end of path
     *
     * @return the new path
     */
    static public String appendLink(String path, String new_link) {
	if (path.equals("")) {
	    return new_link;
	}
	return path +"/"+new_link;
    }

    /** adds new_link to the front of path
     *
     * @return the new path
     */
    static public String prependLink(String path, String new_link) {
	if (path.equals("")) {
	    return new_link;
	}
	return  new_link+"/"+path;
    }

    /** replaces the first link in the path with the specified name
     */
    static public String replaceFirstLink(String path, String new_first_link) {
	path = removeFirstLink(path);
	path = prependLink(path, new_first_link);
	return path;

    }
    /** returns the first name in the path
     *
     */
    static public String getFirstLink(String path) {

	int i = path.indexOf("/");
	if (i==0) { // have a '/' on the front
	    path = path.substring(1);
	    i= path.indexOf("/");
	}
	if (i==-1) { // '/' not found
	    return path;
	}
	
	return path.substring(0,i);
    }

    /** removes the first name in the path
     *
     * @return the modified path
     */
    static public String removeFirstLink(String path) {
	int i = path.indexOf("/");
	if (i==0) { // have a '/' on the front
	    path = path.substring(1);
	    i= path.indexOf("/");
	}
	if (i==-1) { // '/' not found - remove the whole path
	    return "";
	}
	return path.substring(i+1); // remove the '/' char
    }

    /** removes the last name in the path
     *
     * @return the modified path
     */
    static public String removeLastLink(String path) {
	int i = path.lastIndexOf("/");
	if (i==-1) { // '/' not found - remove the whole path
	    return "";
	}
	return path.substring(0, i); // remove the '/' char
    }
    
    static public String getLastLink(String path) {
	int i = path.lastIndexOf("/");
	if (i==-1) { // '/' not found - return the whole path
	    return path;
	}
	return path.substring(i+1); // remove the last link
    }

    
    static public String createPath(String [] links) {
	String path = links[0];
	for (int i=1; i<links.length; i++) {
	    path = appendLink(path, links[i]);
	}
	return path;
    }

    static public int getIndex(String link) {
	int i = link.indexOf("[");
	if (i==-1) {
	    return -1;
	}
	int j = link.indexOf("]");
	String num = link.substring(i+1,j);
	try {
	    int index = Integer.parseInt(num);
	    return index;
	} catch (Exception e) {
	    return -1;
	}
    }

    static public String removeIndex(String link) {
	int i = link.indexOf("[");
	if (i==-1) {
	    return link;
	}
	return link.substring(0, i);
    }

}

    
