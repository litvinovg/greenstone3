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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.BasicDocumentDatabase;
import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.GS2MacroResolver;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Greenstone 2 collection classifier service
 * 
 */

public class GS2Browse extends AbstractBrowse
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2Browse.class.getName());

	protected SimpleCollectionDatabase coll_db = null;
  BasicDocumentDatabase gs_doc_db = null;
	public GS2Browse()
	{
	  this.macro_resolver = new GS2MacroResolver();
	}

	public void cleanUp()
	{
		super.cleanUp();
		this.coll_db.closeDatabase();
		this.gs_doc_db.cleanUp();
	}

	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring GS2Browse...");
		// the index stem is either specified in the config file or is  the collection name
		Element index_stem_elem = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_STEM_ELEM);
		String index_stem = null;
		if (index_stem_elem != null)
		{
			index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (index_stem == null || index_stem.equals(""))
		{
			index_stem = this.cluster_name;
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

		// do we still need this????
		coll_db = new SimpleCollectionDatabase(database_type);
		if (!coll_db.databaseOK())
		{
			logger.error("Couldn't create the collection database of type " + database_type);
			return false;
		}

		// Open database for querying
		String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, index_stem, database_type);
		if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ))
		{
			logger.error("Could not open collection database!");
			return false;
		}
		
		
		gs_doc_db = new BasicDocumentDatabase(database_type, this.site_home, this.cluster_name, index_stem);
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

	protected String getChildType(String node_id)
	{
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return null;
		}
		return info.getInfo("childtype");
	}

  /** the type of a node is the same as teh child type of its parent */
  protected String getThisType(String node_id) {
    String parent_id = OID.getParent(node_id);
    if (parent_id.equals(node_id)) {
      return null; // no parent so doesn't have a thistype
    }
    DBInfo info = this.coll_db.getInfo(parent_id);
    if (info == null)
      {
	return null;
      }
    return info.getInfo("childtype");
  }


	protected String getMetadata(String node_id, String key)
	{
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return "";
		}

		Set<String> keys = info.getKeys();
		Iterator<String> it = keys.iterator();
		while (it.hasNext())
		{
			String key_in = it.next();
			String value = info.getInfo(key);
			if (key_in.equals(key))
			{
				return value;
			}
		}

		return "";

	}

	/**
	 * get the metadata for the classifier node node_id returns a metadataList
	 * element: <metadataList><metadata
	 * name="xxx">value</metadata></metadataList> if all_metadata is true,
	 * returns all available metadata, otherwise just returns requested metadata
	 */
	// assumes only one value per metadata
  // does no macro resolving. assumes classifier metadata will not have macros.
  protected Element getMetadataList(Document doc, String node_id, boolean all_metadata, ArrayList<String> metadata_names)
	{
		String lang = "en";
		Element metadata_list = doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return null;
		}
		if (all_metadata)
		{
			// return everything out of the database
			Set<String> keys = info.getKeys();
			Iterator<String> it = keys.iterator();
			while (it.hasNext())
			{
				String key = it.next();
				String value = info.getInfo(key);
				GSXML.addMetadata(metadata_list, key, this.macro_resolver.resolve(value, lang, MacroResolver.SCOPE_META, node_id));
			}

		}
		else
		{
			for (int i = 0; i < metadata_names.size(); i++)
			{
				String meta_name = metadata_names.get(i);
				String value = (String) info.getInfo(meta_name);
				GSXML.addMetadata(metadata_list, meta_name, this.macro_resolver.resolve(value, lang, MacroResolver.SCOPE_META, node_id));
			}
		}
		return metadata_list;
	}


	protected int getNumChildren(String node_id)
	{
	  return this.gs_doc.getNumChildren(node_id);
	}

	/**
	 * returns true if the id refers to a document (rather than a classifier
	 * node)
	 */
	protected boolean isDocumentId(String node_id)
	{
		if (node_id.startsWith("CL"))
		{
			return false;
		}
		return true;
	}

}
