/*
 *    GSEntityResolver.java
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


import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import java.io.File;
import java.net.URL;

import org.apache.log4j.*;

// uses a class loader to find entities
// The class loader to use can be set by setClassLoader(), otherwise it will use the class loader that loaded itself. For the Tomcat webapp, this will be the webapp class loader, not the system class loader. the webapp classloader knows about the classes in the WEB-INF/classes dir, the system classloader knows about the ones on the classpath. The system class loader is a parent to web app classloader, so this will be used as well.

public class GSEntityResolver implements EntityResolver {

    ClassLoader class_loader = null;
    File baseFilepath = null;
    
     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSEntityResolver.class.getName());

    public GSEntityResolver() {}

    public GSEntityResolver(File baseFilepath) {
	this.baseFilepath = baseFilepath;
    }

    /* Methods with the ClassLoader parameter are unused at present */
    public GSEntityResolver(ClassLoader loader) {
	this.class_loader = loader;
    }

    public void setClassLoader(ClassLoader loader) {
	this.class_loader = loader;
    }
    
    public InputSource resolveEntity (String public_id, String system_id) {
	
	logger.debug("entity resolver for "+system_id);
	String temp_id = system_id;
	if (temp_id.startsWith("file://")) {
	    File f = new File(system_id);
	    if (f.exists()) {
		return new InputSource(system_id);
	    } else {
		temp_id = f.getName(); //temp_id = temp_id.substring(temp_id.lastIndexOf("/")+1);
	    }
	} else { // not a file
	    if (temp_id.indexOf("/")!= -1) {
		temp_id = temp_id.substring(temp_id.lastIndexOf("/")+1);
	    }
	}
	
	// use the baseFilepath, if one was provided
	if(this.baseFilepath != null) {
	    return new InputSource("file://" + this.baseFilepath + File.separator + temp_id);
	}

	// try using a class loader
	if (this.class_loader==null) {
	    this.class_loader = this.getClass().getClassLoader();
	}
	URL url = class_loader.getResource(temp_id);
	if (url == null) {
	    return null;
	}
	return new InputSource("file://"+url.getFile());
    }
}
