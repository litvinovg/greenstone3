/*
 *    GlobalProperties.java
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
package org.greenstone.util;

import java.util.Properties;
import java.io.File;
import java.io.InputStream;

import org.apache.log4j.*;

/**
 * holds some global properties for the application. Read from a properties file
 */
public class GlobalProperties
{

	static Logger logger = Logger.getLogger(org.greenstone.util.GlobalProperties.class.getName());
	private static Properties properties = null;
	private static String properties_filename = "global.properties";
	private static String gsdl3_home = null;
	private static String gsdl3_web_address = null;
        private static String full_gsdl3_web_address = null;

	// Note, that if the servlet is reloadable, then it is reloaded each time the file is changed.
	static
	{
		//load in the properties
		properties = new Properties();
		reload();
	}

	/** get the value of the property 'key'. returns null if not found */
	public static String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	/**
	 * get the value of the property 'key'. returns default_value if not found
	 */
	public static String getProperty(String key, String default_value)
	{
		return properties.getProperty(key, default_value);
	}

	/** some special ones */
	public static String getGSDL3Home()
	{
		return gsdl3_home;
	}

	public static String getGS2Build()
	{
		return gsdl3_home + File.separator + ".." + File.separator + "gs2build";
	}

	public static String getGSDL3WebAddress()
	{
		return gsdl3_web_address;
	}

        public static String getFullGSDL3WebAddress()
	{
		return full_gsdl3_web_address;
	}

	public static void reload()
	{
		try
		{

			InputStream in = Class.forName("org.greenstone.util.GlobalProperties").getClassLoader().getResourceAsStream(properties_filename);
			if (in != null)
			{
				logger.debug("loading global properties");
				properties.load(in);
				in.close();
			}
			else
			{
				logger.error("couldn't load global properties!");
			}
			gsdl3_home = properties.getProperty("gsdl3.home");
			// make sure the path separators are correct
			File gs3_file = new File(gsdl3_home);
			gsdl3_home = gs3_file.getPath();

			//build the gsdl3 web address, in a way resilient to errors and ommisions in global.properties, simplifying where possible
			//aiming for a string with no trailing slash, eg "http://localhost:8080/greenstone3" or "http://www.mygreenstonelibrary.com"
			String protocolSpecifier = null, hostSpecifier = null, portSpecifier = null, contextSpecifier = null;

			//protocol
			if (properties.getProperty("tomcat.protocol") == null || properties.getProperty("tomcat.protocol").equals(""))
			{
				protocolSpecifier = "http://";
			}
			else
			{
				if (properties.getProperty("tomcat.protocol").endsWith("://"))
				{
					protocolSpecifier = properties.getProperty("tomcat.protocol");
				}
				else
				{
					protocolSpecifier = properties.getProperty("tomcat.protocol") + "://";
				}
			}

			//hostname
			if (properties.getProperty("tomcat.server") == null)
			{
				hostSpecifier = "localhost";
			}
			else
			{
				hostSpecifier = properties.getProperty("tomcat.server");
				while (hostSpecifier.endsWith("/"))
				{
					hostSpecifier = hostSpecifier.substring(0, hostSpecifier.length() - 1);
				}
			}

			//port
			if (properties.getProperty("tomcat.port") == null || properties.getProperty("tomcat.port").equals("") || (protocolSpecifier.equals("http://") && properties.getProperty("tomcat.port").equals("80")) || (protocolSpecifier.equals("https://") && properties.getProperty("tomcat.port").equals("443")))
			{
				portSpecifier = "";
			}
			else
			{
				portSpecifier = ":" + properties.getProperty("tomcat.port");
			}

			//context path
			if (properties.getProperty("tomcat.context") == null || properties.getProperty("tomcat.context").equals("") || properties.getProperty("tomcat.context").equals("/"))
			{
				contextSpecifier = "";
			}
			else
			{
				contextSpecifier = properties.getProperty("tomcat.context");
				if (!contextSpecifier.startsWith("/"))
				{
					contextSpecifier = "/" + contextSpecifier;
				}
			}

			//string it all together
			full_gsdl3_web_address = protocolSpecifier + hostSpecifier + portSpecifier + contextSpecifier;
			gsdl3_web_address = contextSpecifier;
		}
		catch (Exception e)
		{
			logger.error("Exception trying to reload global.properties: " + e);
		}
	}
}
