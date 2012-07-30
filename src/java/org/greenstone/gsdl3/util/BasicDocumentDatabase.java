/*
 *    BasicDocumentDatabase.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *   the Free Software Foundation; either version 2 of the License, or
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

// Greenstone classes
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.GSFile;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;

// java
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.io.File;

import org.apache.log4j.*;

public class BasicDocumentDatabase extends AbstractBasicDocument
{
    // collection database
    protected SimpleCollectionDatabase coll_db = null;

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.BasicDocumentDatabase.class.getName());


    /** constructor */
    public BasicDocumentDatabase(Document doc, 
				  String database_type, String site_home,
				  String cluster_name,  String index_stem)
    {
	super(doc);

	coll_db = new SimpleCollectionDatabase(database_type);
	if (!coll_db.databaseOK()) {
	  logger.error("Couldn't create the collection database of type "+database_type);
	}

	// Open database for querying
	String coll_db_file = GSFile.collectionDatabaseFile(site_home, cluster_name, index_stem, database_type);
	if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ)) {
	    logger.error("Could not open collection database!");
	    coll_db = null;
	}
    }

    public boolean isValid() 
    {
	return (coll_db != null);
    }
	
    public void cleanUp() {
	this.coll_db.closeDatabase();
    }


    public DBInfo getInfo(String main_key) {
	if (this.coll_db==null) {
	    // Most likely cause is that this installation of Greenstone3 has not 
	    // been compiled with support for this database type
	    return null;
	}

	return coll_db.getInfo(main_key);
    }


    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_HIERARCHY
    */
    public String getDocType(String node_id){
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return GSXML.DOC_TYPE_SIMPLE;
	}
	String doc_type = info.getInfo("doctype");
	if (!doc_type.equals("")&&!doc_type.equals("doc")) {
	    return doc_type;
	}

	String top_id = OID.getTop(node_id);
	boolean is_top = (top_id.equals(node_id) ? true : false);
	
	String children = info.getInfo("contains");
	boolean is_leaf = (children.equals("") ? true : false);

	if (is_top && is_leaf) { // a single section document
	    return GSXML.DOC_TYPE_SIMPLE;
	}

	// now we just check the top node
	if (!is_top) { // we need to look at the top info
	    info = this.coll_db.getInfo(top_id);
	}
	if (info == null) {
	    return GSXML.DOC_TYPE_HIERARCHY;
	}
 
	String childtype = info.getInfo("childtype");
	if (childtype.equals("Paged")) {
	    return GSXML.DOC_TYPE_PAGED;
	}
		if (childtype.equals("PagedHierarchy"))
		  {
		    return GSXML.DOC_TYPE_PAGED_HIERARCHY;
		  }
	return GSXML.DOC_TYPE_HIERARCHY;

    }
    
    /** returns true if the node has child nodes */
    public boolean hasChildren(String node_id){
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return false;
	}
	String contains = info.getInfo("contains");
	if (contains.equals("")) {
	    return false;
	}
	return true;
    }
    
    /** returns true if the node has a parent */
    public boolean hasParent(String node_id){
	String parent = OID.getParent(node_id);
	if (parent.equals(node_id)) {
	    return false;
	}
	return true;
    }
    
   /** convert indexer internal id to Greenstone oid */
    public String internalNum2OID(long docnum)
    {
	return this.coll_db.docnum2OID(docnum);
    }

    public String internalNum2OID(String docnum)
    {
	return this.coll_db.docnum2OID(docnum);
    }

}


