/*
 *    GS2Browse.java
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
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.GS2MacroResolver;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.DBInfo;
// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;

// General Java classes
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Iterator;

import org.apache.log4j.*;

/** Greenstone 2 collection classifier service
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @author <a href="mailto:mdewsnip@cs.waikato.ac.nz">Michael Dewsnip</a>
 */

public class GS2Browse 
    extends AbstractBrowse 
{

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2Browse.class.getName());

    protected SimpleCollectionDatabase coll_db = null;

    public GS2Browse() 
    {
    }

    public void cleanUp() {
	super.cleanUp();
	this.coll_db.closeDatabase();
    }

    public boolean configure(Element info, Element extra_info)
    {
	if (!super.configure(info, extra_info)){
	    return false;
	}

	logger.info("Configuring GS2Browse...");
	// the index stem is either specified in the config file or is  the collection name
	Element index_stem_elem = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_STEM_ELEM);
	String index_stem = null;
	if (index_stem_elem != null) {
	    index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
	}
	if (index_stem == null || index_stem.equals("")) {
	    index_stem = this.cluster_name;
	}
	
	// find out what kind of database we have
	Element database_type_elem = (Element) GSXML.getChildByTagName(info, GSXML.DATABASE_TYPE_ELEM);
	String database_type = null;
	if (database_type_elem != null) {
	  database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
	}

	if (database_type == null || database_type.equals("")) {
	  database_type = "gdbm"; // the default
	}
	coll_db = new SimpleCollectionDatabase(database_type);
	if (!coll_db.databaseOK()) {
	  logger.error("Couldn't create the collection database of type "+database_type);
	  return false;
	}
	
	// Open database for querying
	String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, index_stem, database_type);
	if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ)) {
	    logger.error("Could not open collection database!");
	    return false;
	}
	this.macro_resolver = new GS2MacroResolver(this.coll_db);
	return true;
    }

    /** if id ends in .fc, .pc etc, then translate it to the correct id */
    protected String translateId(String node_id) {
	return OID.translateOID(this.coll_db, node_id); //return this.coll_db.translateOID(node_id);
    }

    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_HIERARCHY
    */
    protected String getDocType(String node_id) {
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
	return GSXML.DOC_TYPE_HIERARCHY;

    }

    /** returns the id of the root node of the document containing node node_id. . may be the same as node_id */
    protected String getRootId(String node_id) {
	return OID.getTop(node_id);
    }
    /** returns a list of the child ids in order, null if no children */
    protected ArrayList<String> getChildrenIds(String node_id) {
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return null;
	}

	ArrayList<String> children = new ArrayList<String>();
	
	String contains = info.getInfo("contains");
	StringTokenizer st = new StringTokenizer(contains, ";");
	while (st.hasMoreTokens()) {
	    String child_id = st.nextToken().replaceAll("\"", node_id);
	    children.add(child_id);
	}
	return children;

    }
    /** returns the node id of the parent node, null if no parent */
    protected String getParentId(String node_id){
	String parent = OID.getParent(node_id);
	if (parent.equals(node_id)) {
	    return null;
	}
	return parent;
    }

    protected String getMetadata(String node_id, String key){
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return "";
	}

	Set<String> keys = info.getKeys();
	Iterator<String> it = keys.iterator();
	while(it.hasNext()) {
	    String key_in = it.next();
	    String value = info.getInfo(key);
	    if (key_in.equals(key)){
		return value;
	    }
	}
	
	return "";

    }

    /** get the metadata for the classifier node node_id
     * returns a metadataList element:
     * <metadataList><metadata name="xxx">value</metadata></metadataList>
     * if all_metadata is true, returns all available metadata, otherwise just
     * returns requested metadata
     */
    // assumes only one value per metadata
    protected Element getMetadataList(String node_id, boolean all_metadata, 
				      ArrayList<String> metadata_names) {
	String lang = "en";
	Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return null;
	}
	if (all_metadata) {
	    // return everything out of the database
	    Set<String> keys = info.getKeys();
	    Iterator<String> it = keys.iterator();
	    while(it.hasNext()) {
		String key = it.next();
		String value = info.getInfo(key);
		GSXML.addMetadata(this.doc, metadata_list, key, this.macro_resolver.resolve(value, lang, GS2MacroResolver.SCOPE_META, node_id));
	    }
	    
	} else {
	    for (int i=0; i<metadata_names.size(); i++) {
		String meta_name = metadata_names.get(i);
		String value = (String)info.getInfo(meta_name);
		GSXML.addMetadata(this.doc, metadata_list, meta_name, value);
	    }
	}
	return metadata_list;
    }

    /** returns the structural information asked for.
     * info_type may be one of
     * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS
     */
    protected String getStructureInfo(String doc_id, String info_type) {
	String value="";
	if (info_type.equals(INFO_NUM_SIBS)) {
	    String parent_id = OID.getParent(doc_id);
	    if (parent_id.equals(doc_id)) {
		value="0";
	    } else {
		value = String.valueOf(getNumChildren(parent_id));
	    }
	    return value;
	}
	
	if (info_type.equals(INFO_NUM_CHILDREN)) {
	    return String.valueOf(getNumChildren(doc_id));
	}

	
	if (info_type.equals(INFO_SIB_POS)) {
	    String parent_id = OID.getParent(doc_id);
	    if (parent_id.equals(doc_id)) {
		return "-1";
	    }
	    
	    DBInfo info = this.coll_db.getInfo(parent_id);
	    if (info==null) {
		return "-1";
	    }
	    
	    String contains = info.getInfo("contains");
	    contains = contains.replaceAll("\"", parent_id);
	    String [] children = contains.split(";");
	    for (int i=0;i<children.length;i++) {
		String child_id = children[i];
		if (child_id.equals(doc_id)) {
		    return String.valueOf(i+1); // make it from 1 to length
			   
		}
	    }
	    
	    return "-1";
	} else {
	    return null;
	}

    }

    protected int getNumChildren(String node_id) {
	DBInfo info = this.coll_db.getInfo(node_id);
	if (info == null) {
	    return 0;
	}
	String contains = info.getInfo("contains");
	if (contains.equals("")) {
	    return 0;
	}
	String [] children = contains.split(";");
	return children.length;
    }
    
    /** returns true if the id refers to a document (rather than 
     * a classifier node)
     */
    protected boolean isDocumentId(String node_id){
	if (node_id.startsWith("CL")) {
	    return false;
	}
	return true;
    }

}
