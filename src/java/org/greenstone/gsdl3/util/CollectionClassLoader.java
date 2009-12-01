/*
 *    CollectionClassLoader.java
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
package org.greenstone.gsdl3.util;

import java.net.URL;
import java.io.File;

/** Looks for classes/resources in the collection resources directory 
 */
public class CollectionClassLoader
    extends ClassLoader {

    String base_dir = null;
    public CollectionClassLoader(ClassLoader parent, String site_home, String collection_name) {
	super(parent);
	this.base_dir = GSFile.collectionResourceDir(site_home, collection_name);
    } 

    public URL findResource(String name) {
	File resource_path = new File(this.base_dir, name);
	try {
	    if (resource_path.exists()) {
		return new URL("file://"+resource_path.getAbsolutePath());
	    }
	} catch (Exception e) {};

	return super.findResource(name);
    }
}
