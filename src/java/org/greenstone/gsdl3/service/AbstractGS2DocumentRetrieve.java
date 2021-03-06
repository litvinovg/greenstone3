/*
 *    AbstractGS2DocumentRetrieve.java
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
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.BasicDocumentDatabase;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.GS2MacroResolver;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.DBInfo;
// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// General Java classes
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.log4j.*;

// Apache Commons
import org.apache.commons.lang3.*;

/**
 * Implements the generic retrieval and classifier services for GS2 collections.
 * 
 */

public abstract class AbstractGS2DocumentRetrieve extends AbstractDocumentRetrieve
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractGS2DocumentRetrieve.class.getName());

	//    protected static final String EXTLINK_PARAM = "ext"; here or in base??    
	protected String index_stem = null;

	protected SimpleCollectionDatabase coll_db = null;
  BasicDocumentDatabase gs_doc_db = null;
	/** constructor */
	protected AbstractGS2DocumentRetrieve()
	{
		this.macro_resolver = new GS2MacroResolver();
	}

	public void cleanUp()
	{
		super.cleanUp();
		this.coll_db.closeDatabase();
		this.gs_doc_db.cleanUp();
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring AbstractGS2DocumentRetrieve...");
		//this.config_info = info;

		// the index stem is either specified in the config file or is  the collection name
		Element index_stem_elem = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_STEM_ELEM);
		if (index_stem_elem != null)
		{
			this.index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (this.index_stem == null || this.index_stem.equals(""))
		{
			logger.error("AbstractGS2DocumentRetrieve.configure(): indexStem element not found, stem will default to collection name");
			this.index_stem = this.cluster_name;
		}

		// find out what kind of database we have
		Element database_type_elem = (Element) GSXML.getChildByTagName(info, GSXML.DATABASE_TYPE_ELEM);
		String database_type = null;
		if (database_type_elem != null)
		{
			database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (database_type == null || database_type.equals(""))
		{
			database_type = "gdbm"; // the default
		}
		coll_db = new SimpleCollectionDatabase(database_type);
		if (!coll_db.databaseOK())
		{
			logger.error("Couldn't create the collection database of type " + database_type);
			return false;
		}

		// Open database for querying
		String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, this.index_stem, database_type);
		if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ))
		{
			logger.error("Could not open collection database!");
			return false;
		}

		gs_doc_db = new BasicDocumentDatabase(database_type, this.site_home, this.cluster_name, this.index_stem);
		if (!gs_doc_db.isValid())
		{
			logger.error("Failed to open Document Database.");
			return false;
		}
		this.gs_doc = gs_doc_db;

		// we need to set the database for our GS2 macro resolver
		GS2MacroResolver gs2_macro_resolver = (GS2MacroResolver) this.macro_resolver;
		gs2_macro_resolver.setDB(this.coll_db);
		// set the class loader in case we have collection specific properties files
		gs2_macro_resolver.setClassLoader(this.class_loader);
		return true;
	}

	/** if id ends in .fc, .pc etc, then translate it to the correct id */
	protected String translateId(String node_id)
	{
		return OID.translateOID(this.coll_db, node_id); //return this.coll_db.translateOID(node_id);
	}

	/**
	 * if an id is not a greenstone id (an external id) then translate it to a
	 * greenstone one
	 */
	protected String translateExternalId(String node_id)
	{
		return this.coll_db.externalId2OID(node_id);
	}

	/**
	 * returns the id of the root node of the document containing node node_id.
	 * . may be the same as node_id
	 */
	protected String getRootId(String node_id)
	{
	  return this.gs_doc.getRootId(node_id);
	}



	/**
	 * get the metadata for the classifier node node_id returns a metadataList
	 * element: <metadataList><metadata
	 * name="xxx">value</metadata></metadataList>
	 */
  protected Element getMetadataList(Document doc, String node_id, boolean all_metadata, ArrayList<String> metadata_names, String lang) throws GSException
	{
		Element metadata_list = doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return null;
		}
		
		if (all_metadata) // this will get all metadata for current node
		{
			// return everything out of the database
			Set<String> keys = info.getKeys();
			Iterator<String> it = keys.iterator();
			while (it.hasNext())
			{
				String key = it.next();
				//String value = info.getInfo(key);
				Vector<String> values = info.getMultiInfo(key);
				for (int i = 0; i < values.size(); i++)
				{
				  GSXML.addMetadata(metadata_list, key, this.macro_resolver.resolve(values.elementAt(i), lang, MacroResolver.SCOPE_META, node_id));
				}
			}

		}
		// now we go through the list of names. If we have specified 
		// all_metadata, then here we only get the ones like 
		// parent_Title, that are not the current node.
		for (int i = 0; i < metadata_names.size(); i++)
		  {
		    String meta_name = metadata_names.get(i);
		    
		    if (!all_metadata || meta_name.indexOf(GSConstants.META_RELATION_SEP)!=-1) {
		    Vector <String> values = getMetadata(node_id, info, meta_name, lang);
		    if (values != null) {
		      for (int j = 0; j < values.size(); j++)
			{
			  // some of these may be parent/ancestor. does resolve need a different id???
			  GSXML.addMetadata(metadata_list, meta_name,  this.macro_resolver.resolve(values.elementAt(j), lang, MacroResolver.SCOPE_META, node_id));
			}
		    }
		    }
		  }
		
		return metadata_list;
	}

  protected Vector<String> getMetadata(String node_id, DBInfo info, String metadata, String lang) {

    DBInfo current_info = info;
    
    int index = metadata.indexOf(GSConstants.META_RELATION_SEP);
    if (index == -1) {
      // metadata is for this node
      return info.getMultiInfo(metadata);
    }
    // we need to get metadata for one or more different nodes
    // we have a relation root, parent, ancestors, siblings, children, descendents
    String relation = metadata.substring(0, index);
    String relation_id="";
    metadata = metadata.substring(index + 1);
    if (relation.equals("root")) {
      relation_id = OID.getTop(node_id);
      if (relation_id.equals(node_id)) {
	// use the current node info
	return info.getMultiInfo(metadata);
      } else {
	return getMetaValuesForOID(relation_id, metadata);
      }
    }
    if (relation.equals("parent")) {
      relation_id = OID.getParent(node_id);
      if (relation_id.equals(node_id)) {
	// no parent
	return null;
      }
      return getMetaValuesForOID(relation_id, metadata);
    }

    if (relation.equals("ancestors")) {
      if (OID.isTop(node_id)) {
	return null;
      }
      String current_id = node_id;
      relation_id = OID.getParent(current_id);
      Vector<String> values = new Vector<String>();
      while (!relation_id.equals(current_id)) {
	
	Vector<String> more_values = getMetaValuesForOID(relation_id, metadata);
	if (more_values != null) {
	  values.addAll(0, more_values);
	}
	current_id = relation_id;
	relation_id = OID.getParent(current_id);
      }
      return values;
    }
    if (relation.equals("siblings")) {
      String parent_id = OID.getParent(node_id);
      if (parent_id.equals(node_id)) {
	// no parent, therefore no siblings
	return null;
      }
      // siblings is the same as asking for children of the parent
      node_id = parent_id;
      relation = "children";
      current_info = this.coll_db.getInfo(parent_id);
      if (current_info == null) {
	return null;
      }
    }
    if (relation.equals("children")) {
      Vector<String> values = new Vector<String>();
      String contains = current_info.getInfo("contains");
      contains = StringUtils.replace(contains, "\"", node_id);
      String[] children = contains.split(";"); 
      for (int i = 0; i < children.length; i++) {
	
	String child_id = children[i];
	Vector<String> more_values = getMetaValuesForOID(child_id, metadata);
	if (more_values != null) {
	  values.addAll(more_values);
	}
      }
      return values;
    } 
    if (relation.equals("descendents")) {
      return null;
    }
    // unknown relation
    logger.error("asked for relation "+relation+" and don't understand it, so getting the full metadata name "+relation+GSConstants.META_RELATION_SEP+metadata);
    // we assume that maybe the metadata has an _ in the name, so get the original name
    return info.getMultiInfo(relation+GSConstants.META_RELATION_SEP+metadata);

  }
      

    protected Vector<String> getMetaValuesForOID(String oid, String metadata) {
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) {
	return null;
      }
      
      Vector<String> values = info.getMultiInfo(metadata);
      // lets look through the values and look for [xxx] things. We need to look up metadata for them.
      if (values == null) { return values; }
      
      for (int j = 0; j < values.size(); j++) {
	String val = values.elementAt(j);
	if (val.contains("[")) {
	  // look for metadata refs
	  String [] metas = StringUtils.substringsBetween(val, "[", "]");
	  for (int i=0; i<metas.length; i++) {
	    String meta = metas[i];
	    String meta_val = info.getInfo(meta);
	    if (!meta_val.equals("")) {
	      val = StringUtils.replace(val,"["+meta+"]",meta_val);
	    }
	  }
	  values.set(j,val);
	}
      }
      return values;
    }
	protected int getNumChildren(String node_id)
	{
	  return this.gs_doc.getNumChildren(node_id);
	}


	/**
	 * returns the content of a node should return a nodeContent element:
	 * <nodeContent>text content or other elements</nodeContent>
	 */
  abstract protected Element getNodeContent(Document doc, String doc_id, String lang) throws GSException;


	/**
	 * needs to get info from collection database - if the calling code gets it
	 * already it may pay to pass it in instead
	 */
	protected String resolveTextMacros(String doc_content, String doc_id, String lang)
	{
		// resolve any collection specific macros
		doc_content = macro_resolver.resolve(doc_content, lang, MacroResolver.SCOPE_TEXT, doc_id);
		return doc_content;
	}



}
