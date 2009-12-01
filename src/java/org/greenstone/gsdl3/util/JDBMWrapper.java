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

public class JDBMWrapper
  implements FlatDatabaseWrapper {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.JDBMWrapper.class.getName());

  /** open database named filename, using mode */
  public boolean openDatabase(String filename, int mode) {
    return false;
  }


  /** close the database associated with this wrapper */
  public void closeDatabase() {

  }

  /** returns the value associated with the key */
  public String getValue(String key) {
    return null;
  }

  /** returns a string of key-value entries that can be 
   *	printed for debugging purposes*/
  public String displayAllEntries() {
	return "No entries.";
  }
}
