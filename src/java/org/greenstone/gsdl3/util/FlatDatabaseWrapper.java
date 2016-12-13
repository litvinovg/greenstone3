/*
 *    FlatDatabaseWrapper.java
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

import java.util.ArrayList;

public interface FlatDatabaseWrapper {

  /* just read access, many readers can share a database */
  public final static int READ = 0;
  /* read/write, exclusive access */
  public final static int WRITE = 1;

  // do we want the other modes? 
  // -- read/write - create if doesn't exist 
  // -- read/write - create a new database
  /** open database named filename, using mode */
  public boolean openDatabase(String filename, int mode);

  /** close the database associated with this wrapper */
  public void closeDatabase();

  /** returns the value associated with the key */
  public String getValue(String key);
  
  /** sets the given key to the given value in the database */
  public boolean setValue(String key, String value);
  
  /** deletes the given key from the database */
  public boolean deleteKey(String key);

  /** Returns all the keys of the database as String */
  public ArrayList<String> getAllEntryKeys();

  /** returns a string of key-value entries that can be 
   *	printed for debugging purposes*/
  public String displayAllEntries();
}

