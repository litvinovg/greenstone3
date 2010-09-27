/*
 *    SimpleCollectionDatabase.java
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

import org.apache.log4j.*;

public class SimpleCollectionDatabase implements OID.OIDTranslatable {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.SimpleCollectionDatabase.class.getName());

  /* just read access, many readers can share a database */
  public final static int READ = FlatDatabaseWrapper.READ;
  /* read/write, exclusive access */
  public final static int WRITE = FlatDatabaseWrapper.WRITE;
  
  protected FlatDatabaseWrapper coll_db = null;
  
  public SimpleCollectionDatabase(String db_type) {

      // Access databaseWrapper through reflection (forName) so code
      // can be more dynamic as to the database backends that are
      // supported for this installation of Greenstone

      String dbwrap_name = db_type.toUpperCase() + "Wrapper";
      Class dbwrap_class = null;

      try {
	  String full_dbwrap_name = "org.greenstone.gsdl3.util."+dbwrap_name;
	  dbwrap_class = Class.forName(full_dbwrap_name);
      } 
      catch(ClassNotFoundException e) {
	  try {
	      //try the dbwrap_name alone in case the package name is
	      //already specified
	      dbwrap_class = Class.forName(dbwrap_name);
	  } 
	  catch(ClassNotFoundException ae) {
	      logger.error("Couldn't create SimpleCollectionDatabase of type "+db_type);
	      logger.info(ae.getMessage());
	  }
      }

      try {
	  this.coll_db = (FlatDatabaseWrapper)dbwrap_class.newInstance();
      }
      catch(Exception e) {
	      logger.error("Failed to call the constructor "+dbwrap_name+"()");
      }


  }
  
  public boolean databaseOK() {
      // Previously failed to open database
      // Most likely cause is that this installation of Greenstone3 has not 
      // been compiled with support for this database type
      return coll_db != null;
  }

  /** open the database filename, with mode mode - uses the FlatDatabaseWrapper modes */
  public boolean openDatabase(String filename, int mode){
    return this.coll_db.openDatabase(filename, mode);
  }
  
  /** close the database */
  public void closeDatabase() {
    this.coll_db.closeDatabase();
  }
  
  /** Returns a DBInfo structure of the key-value pairs associated with a 
    particular main key in the database */
  public DBInfo getInfo(String main_key) {
      //   logger.warn("All the entries of the db are:");
      //   this.coll_db.displayAllEntries();

      
    if (this.coll_db==null) {
	// Most likely cause is that this installation of Greenstone3 has not 
	// been compiled with support for this database type
	return null;
    }

    String key_info = this.coll_db.getValue(main_key);
    if (key_info == null || key_info.equals("")) {
      return null;
    }
    
    DBInfo info = new DBInfo();
    
    String [] lines = key_info.split("\n");
    String key;
    String value;
    for (int i=0; i<lines.length; i++) {
      logger.debug("line:"+lines[i]);
      int a = lines[i].indexOf('<');
      int b= lines[i].indexOf('>');
      if (a==-1 || b==-1) {
	logger.error("bad format in db");
      }
      else {
	key=lines[i].substring(a+1, b);
	value=lines[i].substring(b+1);
	logger.debug("key="+key+", val="+value);
	info.addInfo(key, value);

      }
    }
    return info;
    
  }
  
  /** converts a greenstone OID to internal docnum */
  public String OID2Docnum(String OID) {
    DBInfo info = getInfo(OID);
    if (info != null) {
      return info.getInfo("docnum");
    }
    return null;
  }
    
  /** converts a greenstone OID to an internal docnum, returning a Long 
   - convenience method*/
  public long OID2DocnumLong(String OID) {
    DBInfo info = getInfo(OID);
    if (info != null) {
      long real_num = Long.parseLong(info.getInfo("docnum"));
      return real_num;
    }
    return -1;
  }


  /** converts a docnum to greenstone OID */
  public String docnum2OID(String docnum) {
    DBInfo info = getInfo(docnum);
    if (info!=null){
      String oid = info.getInfo("section");
      return oid;
    } else{
      return null;
    }
  }

  /** converts a docnum to greenstone OID 
      - convenience method */
  public String docnum2OID(long docnum) {
    return docnum2OID(Long.toString(docnum));
  }

  /** converts an external id to greenstone OID */
  public String externalId2OID(String extid) {
    DBInfo info = getInfo(extid);
    if (info != null) {
      String oid = info.getInfo("section");
      return oid;
    }
    return null;
  }

  /** After OID.translateOID() is through, this method processes OID further 
   * to translate relative oids into proper oids:
   * .pr (parent), .rt (root) .fc (first child), .lc (last child),
   * .ns (next sibling), .ps (previous sibling) 
   * .np (next page), .pp (previous page) : links sections in the order that you'd read the document
   * a suffix is expected to be present so test before using 
   */
    public String processOID(String doc_id, String top, String suff, int sibling_num) {
	DBInfo info = getInfo(doc_id);
    if (info==null) {
      logger.info("info is null!!");
      return top;
    }
	
    String contains = info.getInfo("contains");
    if (contains.equals("")) {
      // something is wrong
      return top;
    }
    contains = contains.replaceAll("\"", doc_id);
    String [] children = contains.split(";");
    if (suff.equals("fc")) {
      return children[0];
    } else if (suff.equals("lc")) {
      return children[children.length-1];
    } else {
      if (suff.equals("ss")) {
	return children[sibling_num-1];
      }
      // find the position that we are at.
      int i=0;
      while (i<children.length) {
	if (children[i].equals(top)) {
	  break;
	}
	i++;
      }
	    
      if (suff.equals("ns")) {
	if (i==children.length-1) {
	  return children[i];
	}
	return children[i+1];
      } else if (suff.equals("ps")) {
	if (i==0) {
	  return children[i];
	}
	return children[i-1];
      }
    }
	
    return top;
    }  
}
