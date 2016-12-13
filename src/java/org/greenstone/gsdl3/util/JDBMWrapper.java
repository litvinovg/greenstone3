/*
 *    JDBMWrapper.java
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

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.Properties;
import java.util.ArrayList;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class JDBMWrapper implements FlatDatabaseWrapper
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.JDBMWrapper.class.getName());

	static String TNAME = "greenstone";

	RecordManager recman_ = null;
	HTree hashtable_ = null;

	String db_filename_;

	static private PrintWriter utf8out = null;

	static
	{
		try
		{
			OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8");
			utf8out = new PrintWriter(osw, true);
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println(e);
		}
		// Register this wrapper with DBHelper
		DBHelper.registerDBTypeExt("jdbm",".jdb");
	}

	/** open database named filename, using mode */
	public boolean openDatabase(String db_filename, int mode)
	{
	        String db_filename_with_ext = db_filename;

		if (db_filename.endsWith(".jdb"))
		{
			// remove file extension as JDBM does not expect it
			db_filename = db_filename.substring(0, db_filename.length() - 4);
		}

		// Map the mode value into equivalent JDBM 'must_exist' value
		// currently this is very simple as there is only READ and WRITE
		// (no WRITER_CREATE)
		// => assume the database must exist
		boolean must_exist = true; // default

		if (recman_ != null)
		{
			String message = "openDatabase() called when the class already has a database open\n";
			message += "  Use closeDatabase before opening the next one.\n";
			message += "  Existing database file: " + db_filename_ + "\n";
			message += "  New database file:      " + db_filename + "\n";
			logger.warn(message);
			// consider closing it automatically?
		}

		try
		{
			// create or open a record manager
			Properties props = new Properties();
			recman_ = RecordManagerFactory.createRecordManager(db_filename, props);
			
			// load existing table (if exists) otherwise create new one
			long recid = recman_.getNamedObject(TNAME);

			if (recid != 0)
			{
				logger.info("# Loading existing database table '" + TNAME + "' ...");
				hashtable_ = HTree.load(recman_, recid);
			}
			else
			{
				if (must_exist)
				{
					recman_.close();
					recman_ = null;
					db_filename_ = null;

					logger.error("Database table '" + TNAME + "' does not exist.");
					throw new IOException();
				}
				else
				{
					logger.info("# No database table '" + TNAME + "' to set.  Creating new one");
					hashtable_ = HTree.createInstance(recman_);
					recman_.setNamedObject(TNAME, hashtable_.getRecid());
				}
			}
		}
		catch (IOException e)
		{
		    e.printStackTrace();

			logger.error("couldn't open database " + db_filename_with_ext);
			return false;
		}

		db_filename_ = db_filename;

		return true;
	}

	/** close the database associated with this wrapper */
	public void closeDatabase()
	{
		try
		{
			if (recman_ != null)
			{
				recman_.close();
				recman_ = null;
				db_filename_ = null;
			}
		}
		catch (IOException e)
		{
			logger.error("Failed to close JDBM database");
		}
	}

	/** returns the value associated with the key */
	public String getValue(String key)
	{
		if (hashtable_ == null) {
			logger.error("Database was not opened or not exist!");
			return null;
		}
		
		String val;

		try
		{
			val = (String) hashtable_.get(key);
			
			recman_.commit();
		}
		catch (IOException e)
		{
			logger.error("Failed get key " + key + "from JDBM database");
			return null;
		}
		
		return val;
	}
	
	/**
	 * Sets the given key to the given value in the database
	 */
	public boolean setValue(String key, String value)
	{
		try
		{
			hashtable_.put(key, value);
			recman_.commit();
		}
		catch (Exception ex)
		{
			logger.error("Failed to set " + key + " = " + value + " in the JDBM database");
			return false;
		}
		return true;
	}

	/**
	 * Deletes the given key (and corresponding value) from the database
	 */
	public boolean deleteKey(String key)
	{
		try
		{
			hashtable_.remove(key);
			recman_.commit();
		}
		catch (Exception ex)
		{
			logger.error("Failed to delete key " + key + " in the JDBM database");
			return false;
		}
		return true;
	}

    /** Returns all the keys of the database as Strings */
    public ArrayList<String> getAllEntryKeys() {
	
	ArrayList<String> keys = new ArrayList<String>();
	
	try {
	    FastIterator iter = hashtable_.keys();
	    
	    String key = (String) iter.next();
	    
	    while (key != null) {
		keys.add(key);
		key = (String) iter.next();
	    }
	    
	    recman_.commit();
	} catch (IOException e) {
	    logger.error("Failed to get all keys from JDBM database");
	    return null;
	}
	
	return keys;
    }
    

	/**
	 * returns a string of key-value entries that can be printed for debugging
	 * purposes
	 */
	public String displayAllEntries()
	{

		StringBuffer keys = new StringBuffer();

		try
		{
			FastIterator iter = hashtable_.keys();
			String key = (String) iter.next();

			String nl = System.getProperty("line.separator");

			while (key != null)
			{
				String val = (String) hashtable_.get(key);
				keys.append("[" + key + "]" + nl);
				keys.append(val + nl);
				// 70 hypens
				keys.append("----------------------------------------------------------------------" + nl);
				key = (String) iter.next();
			}

			recman_.commit();
		}
		catch (IOException e)
		{
			logger.error("Failed to get all keys and values from JDBM database");
			return null;
		}

		return keys.toString();
	}
}
