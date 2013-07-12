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

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * holds some global properties for the application. Read from a properties file
 */
public class GlobalProperties
{

	static Logger logger = Logger.getLogger(org.greenstone.util.GlobalProperties.class.getName());
	private static Properties properties = new Properties();
	private static String properties_filename = "global.properties";
	private static String gsdl3_home = null;
	private static String gsdl3_writablehome = null;
	private static String gsdl3_web_address = null;
	private static String full_gsdl3_web_address = null;

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

	public static String getGSDL3WritableHome()
	{
		return gsdl3_writablehome;
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

	public static void loadGlobalProperties(String optionalGS3Home)
	{
		try
		{
			InputStream in = Class.forName("org.greenstone.util.GlobalProperties").getClassLoader().getResourceAsStream(properties_filename);
			if (in != null)
			{
				logger.debug("Loading global properties");
				properties.load(in);
				in.close();
			}
			else
			{
				logger.error("Couldn't load global properties: " + properties_filename);
			}

			gsdl3_home = properties.getProperty("gsdl3.home");
			if ((gsdl3_home == null || gsdl3_home.length() == 0) 
			    && optionalGS3Home != null && optionalGS3Home.length() > 0)
			{
				gsdl3_home = optionalGS3Home;
			}

			// if gsdl3_home is still null, fall back to default: gsdl3srchome/web
			if (gsdl3_home == null) { 
			    gsdl3_home = System.getenv("GSDL3SRCHOME") + File.separator + "web";
			    logger.warn("** Note: falling back to using GSDL3SRCHOME to set gsdl3.home to: " + gsdl3_home);
			}

			// make sure the path separators are correct
			// gsdl3_home may be null, e.g., when we are loading properties from Server3
			if (gsdl3_home != null)
			{
				File gs3_file = new File(gsdl3_home);
				gsdl3_home = gs3_file.getPath();
			}

			gsdl3_writablehome = properties.getProperty("gsdl3.writablehome");
			// if gsdl3_writablehome is null, then defaults to gsdl3_home
			if (gsdl3_writablehome == null) { 
			    gsdl3_writablehome = gsdl3_home;
			}

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
			e.printStackTrace();
		}
	}
}
