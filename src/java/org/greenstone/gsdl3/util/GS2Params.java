/*
 *    GS2Params.java
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

public class GS2Params extends GSParams {

    public GS2Params() {
	super();

	// now add in all the service ones we will be using
	addParameter("stem",  true);	
	addParameter("case",  true);
	addParameter("accent", true);
	addParameter("index", true);
	addParameter("matchMode",  true);
	addParameter("maxDocs",  true);
	addParameter("query", true);
	addParameter("level",  true);
	addParameter("qb",  true); // queryBox
	addParameter("qm",  true); // queryMode
	addParameter("qt",  true); // query type
	addParameter("qfm",  true); // query form mode
	addParameter("qfn",  true); // query form number of boxes
	addParameter("ct",  true); // collection type - mg or mgpp
	
    }

}
