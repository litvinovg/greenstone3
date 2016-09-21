/*
 *    CustomClassLoader.java
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
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;

/**
 * Looks for classes/resources in the specified resources_dir directory
 */
public class CustomClassLoader extends ClassLoader
{

	String base_dir = null;

  public CustomClassLoader(ClassLoader parent, String resources_dir)
	{
		super(parent);
		this.base_dir = resources_dir; 
	}
  
  // Resource Bundle loading with a class loader will call getResource(). 
  // THe Java implementation of getResource tries using the parent class 
  // loader to find the resource (or the system class loader if no parent). 
  // If the resource not found, then it calls the local findResource method. 
  // Ie parent:child ordering.
  // Because we want local files to override default ones, we need 
  // child:parent ordering, so change getResource to look locally first.
  //public URL findResource(String name)
	public URL getResource(String name)
	{
		File resource_path = new File(this.base_dir, name);
		try
		{
			if (resource_path.exists())
			{
				return resource_path.toURI().toURL();
			}
		}
		catch (Exception e)
		{
		}

		return super.getResource(name);
	}
}
