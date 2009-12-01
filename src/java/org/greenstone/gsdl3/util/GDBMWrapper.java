/*
 *    GDBMWrapper.java
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

import au.com.pharos.gdbm.GdbmFile;
import au.com.pharos.packing.*;
import au.com.pharos.gdbm.GdbmException;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.io.File;

/** java wrapper class for gdbm - uses Java-GDBM written by Martin Pool
 * replaces gdbmclass in the old version
 */

public class GDBMWrapper 
  implements FlatDatabaseWrapper {

  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GDBMWrapper.class.getName());
  
  /* GdbmFile modes:
     READER - read access, many readers may share the database
     WRITER - read/write access, exclusive access
     WRCREAT - read/write access, create db if doesn't exist
     NEWDB - read/write access, db should be replaced if exists
  */
    
    
    protected GdbmFile db_=null;
    
  /** open the database filename, with mode mode - uses the constants
      above, eg GdbmFile.WRITER */
  public boolean openDatabase(String filename, int mode) {
    // need to convert mode to GdbmFile mode
    if (mode == READ) { 
      mode = GdbmFile.READER;
    } else if (mode == WRITE) {
      mode = GdbmFile.WRITER;
    }	else {
      logger.error ("invalid mode, "+mode+ ", opening db for reading only");
      mode = GdbmFile.READER;
    }
    try {
	if (db_!=null) {
	    db_.close();
	}

	// The java version of the C++ code in common-src/src/lib/gdbmclass.cpp
	if(mode == GdbmFile.READER) {
	  // Looking to read in the database
	  // we now use gdb extension. Check for ldb/bdb in case of legacy collection
	  // if not (first time) then generate using txt2db
	    if (!new File(filename).exists()) {
	      logger.warn("Database file " + filename + " does not exist. Looking for ldb/bdb version");
	      int extension = filename.lastIndexOf('.');
	      String filename_head = filename.substring(0, extension);
	      filename = filename_head + ".ldb";
	      if (!new File(filename).exists()) {
		filename = filename_head + ".bdb";
		
		if (!new File(filename).exists()) {
		logger.warn("ldb/bdb version of database file " + filename + " does not exist. Looking for txtgz version of db file.");
		// put the filename back to gdb
		filename = filename_head + ".gdb";
		// need to generate architecture native GDBM file using txt2db
		
		// replace sought after gdbm filename ext with ".txt.gz"
		
		String txtgzFilename = filename_head + ".txt.gz";
		if(new File(txtgzFilename).exists()) {		    
		    // Test to make sure Perl is on the path
		    // On Linux, the output of the test goes to STDOUT so redirect it to STDERR
		    String cmdTest = "perl -v 2>&1";		    
		    //String cmdTest = "echo %PATH%";
		    int returnValue = Processing.runProcess(cmdTest);
		    if (returnValue != 0) {
			logger.error("Tried to find Perl. Return exit value of running " 
				     + cmdTest + ": " +  returnValue + ", (expected this to be 0)");
			logger.error("Check that Perl is set in your PATH environment variable.");
			//log.error("At present, PATH=" + System.getenv("PATH"));
		    }
		    
		    String cmd = "perl -S txtgz-to-gdbm.pl \"" + txtgzFilename + "\" \"" + filename + "\"";
		    returnValue = Processing.runProcess(cmd);
		    // For some reason, launching this command with gsdl_system() still returns 1
		    // even when it returns 0 when run from the command-line. We can check whether
		    // we succeeded by looking at whether the output database file was created.
		    if (returnValue != 0) {
			logger.warn("Warning, non-zero return value on running command \"" + cmd + "\": " + returnValue);
			if (!new File(filename).exists()) {
			    logger.error("Tried to run command \"" + cmd + "\", but it failed");
			}
		    }		    
		}
		}
	      }
	      
	    }
	}

	db_ = new GdbmFile(filename, mode);
    } catch ( GdbmException e) { // the database wasn't opened or created
	logger.error("couldn't open database "+filename);
	return false;
    } 
    db_.setKeyPacking(new StringPacking());
    db_.setValuePacking(new StringPacking());
    return true;
  }
  
  /** close the database associated with this wrapper */
  public void closeDatabase() {
    try {
      if (db_ != null) {
	db_.close();
	db_ = null;
      }
    } catch (GdbmException e) {
      // should never get here - close never actually throws an exception
      logger.error("error on close()");
    }
  }

  public String getValue(String key) {
    if (db_==null) {
      return null;
    }
    String s_info;
    try {
	try {
	    // The key is UTF8: do db lookup using the UTF8 version of key
	    s_info = (String)db_.fetch(key.getBytes("UTF-8"));
	} catch(UnsupportedEncodingException e) {
	    logger.warn("utf8 key for " + key 
			 + " unrecognised. Retrying with default encoding.");
	    // retry db lookup using default encoding of key instead of UTF8
	    s_info = (String)db_.fetch(key);
	}
    } catch (GdbmException e) {
      logger.error("couldn't get record");
      return null;
    }
    if (s_info==null) {
      // record not present
      logger.error("key "+key+" not present in db");
      return null;
    }
    return s_info;
  }

  /** sets the key value as info 
   * TODO - not implemented yet */
  public boolean setValue (String key, String value) {
    if (db_==null) {
      return false;
    }
    return false;
  }
  /** deletes the entry for key 
   * TODO - not implemented yet */
  public boolean deleteKey(String key) {
    if (db_==null) {
      return false;
    }
    return false;
  }

    /** returns a string of key-value entries that
     *  can be printed for debugging purposes. */
    public String displayAllEntries() {
	StringBuffer output = new StringBuffer();
	try{
	    java.util.Enumeration e = db_.keys();
	    while(e.hasMoreElements()) {
		Object key = e.nextElement();
		Object value = db_.fetch(key);

		output.append("key href: ");
		output.append((String)key);
		output.append("\tvalue ID: ");
		output.append((String)value);
		output.append("\n");
		//logger.warn("key: " + key + "\tvalue: " + value);

		String urlkey = java.net.URLEncoder.encode((String)key, "UTF8");
		output.append("URL encoded key: " + urlkey);
		//logger.warn("URL encoded key: " + urlkey);
	    }
	} catch(UnsupportedEncodingException e) {
	    logger.warn("Trouble converting key to UTF-8.");
	} catch(Exception e) {
	    logger.warn("Exception encountered when trying to displayAllEntries():" + e);
	}
	return output.toString();
    }
}
