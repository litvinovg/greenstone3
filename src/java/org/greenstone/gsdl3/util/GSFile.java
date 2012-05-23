/*
 *    GSFile.java
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

import java.io.File;
//import org.apache.soap.encoding.soapenc.Base64;
import org.apache.axis.encoding.Base64;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.ClassLoader; // to find files on the class path
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.apache.log4j.*;

/**
 * GSFile - utility class for Greenstone.
 * 
 * all file paths are created here also has file utility methods
 * 
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 * @see File
 */

public class GSFile
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSFile.class.getName());

	/** site config file path */
	static public String siteConfigFile(String site_home)
	{
		return site_home + File.separatorChar + "siteConfig.xml";

	}

	/** interface config file path */
	static public String interfaceConfigFile(String interface_home)
	{
		return interface_home + File.separatorChar + "interfaceConfig.xml";

	}

	/** collection directory path */
	static public String collectDir(String site_home)
	{
		return site_home + File.separatorChar + "collect";
	}

	/** collection config file path */
	static public String collectionConfigFile(String site_home, String collection_name)
	{
		return collectionConfigFile(collectionBaseDir(site_home, collection_name));
	}

	static public String collectionConfigFile(String collection_home)
	{
		return collectionEtcDir(collection_home) + File.separatorChar + "collectionConfig.xml";

	}

	/** collection init file path */
	static public String collectionInitFile(String site_home, String collection_name)
	{
		return site_home + File.separatorChar + "collect" + File.separatorChar + collection_name + File.separatorChar + "etc" + File.separatorChar + "collectionInit.xml";

	}

	/** collection build config file path */
	static public String collectionBuildConfigFile(String site_home, String collection_name)
	{
		return site_home + File.separatorChar + "collect" + File.separatorChar + collection_name + File.separatorChar + "index" + File.separatorChar + "buildConfig.xml";
	}

	/** collection build config file path */
	static public String collectionBuildConfigFileBuilding(String site_home, String collection_name)
	{
		return collectionBuildConfigFileBuilding(collectionBaseDir(site_home, collection_name));
	}

	static public String collectionBuildConfigFileBuilding(String collection_home)
	{
		return collection_home + File.separatorChar + "building" + File.separatorChar + "buildConfig.xml";
	}

	/** XML Transform directory path */
	static public String xmlTransformDir(String interface_home)
	{
		return interface_home + File.separatorChar + "transform";
	}

	/** collection base directory path */
	static public String collectionBaseDir(String site_home, String collection_name)
	{
		return site_home + File.separatorChar + "collect" + File.separatorChar + collection_name;
	}

	/** collection archive directory path */
	static public String collectionArchiveDir(String site_home, String collection_name)
	{
		return collectionArchiveDir(collectionBaseDir(site_home, collection_name));
	}

	static public String collectionArchiveDir(String collection_home)
	{
		return collection_home + File.separatorChar + "archives";
	}

	/** collection building directory path */
	static public String collectionBuildDir(String site_home, String collection_name)
	{
		return collectionBuildDir(collectionBaseDir(site_home, collection_name));
	}

	static public String collectionBuildDir(String collection_home)
	{
		return collection_home + File.separator + "building";
	}

	/** collection building directory path */
	static public String collectionEtcDir(String site_home, String collection_name)
	{
		return collectionEtcDir(collectionBaseDir(site_home, collection_name));
	}

	static public String collectionEtcDir(String collection_home)
	{
		return collection_home + File.separator + "etc";
	}

	/** collection building directory path */
	static public String collectionImportDir(String site_home, String collection_name)
	{
		return collectionImportDir(collectionBaseDir(site_home, collection_name));

	}

	static public String collectionImportDir(String collection_home)
	{
		return collection_home + File.separatorChar + "import";
	}

	/** collection building directory path */
	static public String collectionIndexDir(String site_home, String collection_name)
	{
		return collectionIndexDir(collectionBaseDir(site_home, collection_name));

	}

	static public String collectionIndexDir(String collection_home)
	{
		return collection_home + File.separatorChar + "index";
	}

	/** text path (for doc retrieval) relative to collectionBaseDir */
	static public String collectionTextPath(String index_stem)
	{
		return "index" + File.separatorChar + "text" + File.separatorChar + index_stem;
	}

	/** index path (for querying) relative to collectionBaseDir */
	static public String collectionIndexPath(String index_stem, String index_name)
	{
		return "index" + File.separatorChar + index_name + File.separatorChar + index_stem;
	}

	/** collection resources directory path */
	static public String collectionResourceDir(String site_home, String collection_name)
	{
		return collectionResourceDir(collectionBaseDir(site_home, collection_name));

	}

	static public String collectionResourceDir(String collection_home)
	{
		return collection_home + File.separatorChar + "resources";
	}

	/** absolute path for an associated file */
	static public String assocFileAbsolutePath(String site_home, String collection_name, String assoc_file_path, String filename)
	{
		return collectionBaseDir(site_home, collection_name) + File.separatorChar + "index" + File.separatorChar + "assoc" + File.separatorChar + assoc_file_path + File.separatorChar + filename;
	}

	static public String extHome(String gsdl3_home, String ext_name)
	{
		return gsdl3_home + File.separatorChar + "ext" + File.separatorChar + ext_name;
	}

	static public String siteHome(String gsdl3_home, String site_name)
	{
		return gsdl3_home + File.separatorChar + "sites" + File.separatorChar + site_name;
	}

	static public String interfaceHome(String gsdl3_home, String interface_name)
	{
		return gsdl3_home + File.separatorChar + "interfaces" + File.separatorChar + interface_name;
	}

	static public String interfaceStylesheetFile(String gsdl3_home, String interface_name, String filename)
	{
		return gsdl3_home + File.separatorChar + "interfaces" + File.separatorChar + interface_name + File.separatorChar + "transform" + File.separatorChar + filename;
	}

	static public String siteStylesheetFile(String site_home, String filename)
	{
		return site_home + File.separatorChar + "transform" + File.separatorChar + filename;
	}

	static public String collStylesheetFile(String site_home, String coll_name, String filename)
	{
		return collectionBaseDir(site_home, coll_name) + File.separatorChar + "transform" + File.separatorChar + filename;
	}

	/**
	 * returns the absolute path to a stylesheet. Stylesheets are looked for in
	 * the following places, in the following order: current collection, current
	 * site, current interface, base interfaces returns null if the file cannot
	 * be found
	 * 
	 * this is not so good because sites may be on a different computer
	 */
	static public String stylesheetFile(String gsdl3_home, String site_name, String collection, String interface_name, ArrayList<String> base_interfaces, String filename)
	{

		String site_home = siteHome(gsdl3_home, site_name);
		// try collection first
		File stylesheet = null;
		if (!collection.equals(""))
		{

			String coll_home = collectionBaseDir(site_home, collection);
			stylesheet = new File(coll_home + File.separatorChar + "transform" + File.separatorChar + filename);
			if (stylesheet.exists())
			{
				return stylesheet.getPath();
			}
		}

		// try site one next	
		stylesheet = new File(site_home + File.separatorChar + "transform" + File.separatorChar + filename);
		if (stylesheet.exists())
		{
			return stylesheet.getPath();
		}

		// try current interface
		String interface_home = interfaceHome(gsdl3_home, interface_name);
		stylesheet = new File(interface_home + File.separatorChar + "transform" + File.separatorChar + filename);
		if (stylesheet.exists())
		{
			return stylesheet.getPath();
		}
		// try base interface
		if (base_interfaces == null || base_interfaces.size() == 0)
		{
			return null; // no base interfaces to look for
		}
		for (int i = 0; i < base_interfaces.size(); i++)
		{
			interface_home = interfaceHome(gsdl3_home, base_interfaces.get(i));
			stylesheet = new File(interface_home + File.separatorChar + "transform" + File.separatorChar + filename);
			if (stylesheet.exists())
			{
				return stylesheet.getPath();
			}
		}

		// still can't find it and we have looked everywhere
		return null;
	}

	static public ArrayList<File> getStylesheetFiles(String gsdl3_home, String site_name, String collection, String interface_name, ArrayList<String> base_interfaces, String filename)
	{
		ArrayList<File> stylesheets = new ArrayList<File>();
		String site_home = siteHome(gsdl3_home, site_name);
		// try collection first
		File stylesheet = null;
		if (!collection.equals(""))
		{
			String coll_home = collectionBaseDir(site_home, collection);
			stylesheet = new File(coll_home + File.separatorChar + "transform" + File.separatorChar + filename);
			if (stylesheet.exists())
			{
				stylesheets.add(stylesheet);
			}
		}

		// try site one next	
		stylesheet = new File(site_home + File.separatorChar + "transform" + File.separatorChar + filename);
		if (stylesheet.exists())
		{
			stylesheets.add(stylesheet);
		}

		// try current interface
		String interface_home = interfaceHome(gsdl3_home, interface_name);
		stylesheet = new File(interface_home + File.separatorChar + "transform" + File.separatorChar + filename);
		if (stylesheet.exists())
		{
			stylesheets.add(stylesheet);
		}
		// try base interface
		if (base_interfaces != null && base_interfaces.size() != 0)
		{
			for (int i = 0; i < base_interfaces.size(); i++)
			{
				interface_home = interfaceHome(gsdl3_home, base_interfaces.get(i));
				stylesheet = new File(interface_home + File.separatorChar + "transform" + File.separatorChar + filename);
				if (stylesheet.exists())
				{
					stylesheets.add(stylesheet);
				}
			}
		}

		return stylesheets;
	}

	/** base directory for phind data */
	public static String phindBaseDir(String site_home, String coll_name, String phind_index)
	{
		return site_home + File.separatorChar + "collect" + File.separatorChar + coll_name + File.separatorChar + "index" + File.separatorChar + "phind" + phind_index;
	}

	/** the collection database file - */
	static public String collectionDatabaseFile(String site_home, String collection_name, String index_stem, String database_type)
	{

		String db_ext = null;
		if (database_type.equalsIgnoreCase("jdbm"))
		{
			db_ext = ".jdb";
		}
		else
		{
			// assume gdbm
			db_ext = ".gdb";
		}

		return site_home + File.separatorChar + "collect" + File.separatorChar + collection_name + File.separatorChar + "index" + File.separatorChar + "text" + File.separatorChar + index_stem + db_ext;

	}

	// some file utility methods

	/**
	 * read in a file and encode it using base64 encoded data returned as a
	 * String
	 */
	static public String base64EncodeFromFile(String in_filename)
	{
		byte[] data = null;
		try
		{
			data = readFile(in_filename);
		}
		catch (Exception e)
		{
			logger.error("couldn't read the file");
		}
		String encodedString = Base64.encode(data);
		return encodedString;

	}

	/** decode some base64 data, and write it to the specified file */
	static public boolean base64DecodeToFile(String data, String out_filename)
	{
		try
		{
			byte[] buffer = Base64.decode(data);
			writeFile(buffer, out_filename);

		}
		catch (Exception e)
		{
			logger.error("file opening/closing errors" + e.getMessage());
			return false;
		}
		return true;

	}

	/** read in a file to a byte array */
	public static byte[] readFile(String filename) throws IOException
	{
		File file = new File(filename);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		int bytes = (int) file.length();
		byte[] buffer = new byte[bytes];
		int readBytes = bis.read(buffer);
		bis.close();
		return buffer;
	}

	/** write a byte array to a file */
	public static void writeFile(byte[] buffer, String filename) throws IOException
	{
		File file = new File(filename);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		bos.write(buffer);
		bos.close();
	}

	public static boolean deleteFile(File f)
	{

		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			for (int i = 0; files != null && i < files.length; i++)
			{
				deleteFile(files[i]);
			}

		}
		// delete the file or directory
		return f.delete();
	}

	public static boolean copyFile(File source, File destination)
	{
		if (!source.isFile())
		{
			logger.error(source.getPath() + " is not a file!");
			return false;
		}
		try
		{
			destination.getParentFile().mkdirs();
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(destination);
			int value = 0;
			while ((value = in.read()) != -1)
			{
				out.write(value);
			}
			in.close();
			out.close();
		}
		catch (Exception e)
		{
			logger.error("something went wrong copying " + source.getPath() + " to " + destination.getPath());
			logger.error("Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Recursively moves the contents of source file to destination, maintaining
	 * paths.
	 * 
	 * @param source
	 *            A File representing the directory whose contents you wish to
	 *            move.
	 * @param destination
	 *            A File representing the directory you wish to move files to.
	 * @return true if successful
	 */
	public static boolean moveDirectory(File source, File destination)
	{

		// first try rename
		if (source.renameTo(destination))
		{
			return true;
		}

		// john T. said that this sometimes doesn't work, so you have to copy files manually

		// copied from gatherer Utility class
		File input[] = source.listFiles();
		for (int i = 0; i < input.length; i++)
		{
			File output = new File(destination, input[i].getName());
			if (input[i].isDirectory())
			{
				moveDirectory(input[i], output);
			}
			else
			{
				// Copy the file
				try
				{
					output.getParentFile().mkdirs();
					FileInputStream in = new FileInputStream(input[i]);
					FileOutputStream out = new FileOutputStream(output);

					FileChannel inC = in.getChannel();
					FileChannel outC = out.getChannel();

					System.err.println(inC.transferTo(0, inC.size(), outC));
					
					in.close();
					out.close();

					// Delete file
					input[i].delete();
				}
				catch (Exception e)
				{
					logger.error("exception: " + e.getMessage());
					return false;
				}

			}
		}
		return true;
	}

}
