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
 * @author Katherine Don
 * @author Michael Dewsnip
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

		gs_doc_db = new BasicDocumentDatabase(this.doc, database_type, this.site_home, this.cluster_name, this.index_stem);
		if (!gs_doc_db.isValid())
		{
			logger.error("Failed to open Document Database.");
			return false;
		}
		this.gs_doc = gs_doc_db;

		// we need to set the database for our GS2 macro resolver
		GS2MacroResolver gs2_macro_resolver = (GS2MacroResolver) this.macro_resolver;
		gs2_macro_resolver.setDB(this.coll_db);

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
	// assumes only one value per metadata
	protected Element getMetadataList(String node_id, boolean all_metadata, ArrayList<String> metadata_names) throws GSException
	{
		Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return null;
		}
		String lang = "en"; // why do we need this??
		if (all_metadata)
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
					GSXML.addMetadata(this.doc, metadata_list, key, this.macro_resolver.resolve(values.elementAt(i), lang, MacroResolver.SCOPE_META, node_id));
				}
			}

		}
		else
		{
			for (int i = 0; i < metadata_names.size(); i++)
			{
				String meta_name = metadata_names.get(i);
				String value = getMetadata(node_id, info, meta_name, lang);
				GSXML.addMetadata(this.doc, metadata_list, meta_name, value);
			}
		}
		return metadata_list;
	}


	protected int getNumChildren(String node_id)
	{
	  return this.gs_doc.getNumChildren(node_id);
	}


	/**
	 * returns the content of a node should return a nodeContent element:
	 * <nodeContent>text content or other elements</nodeContent>
	 */
	abstract protected Element getNodeContent(String doc_id, String lang) throws GSException;

	protected String getMetadata(String node_id, DBInfo info, String metadata, String lang)
	{
		String pos = "";
		String relation = "";
		String separator = ", ";
		int index = metadata.indexOf(GSConstants.META_RELATION_SEP);
		if (index == -1)
		{
			Vector<String> values = info.getMultiInfo(metadata);
			if (values != null)
			{
				// just a plain meta entry eg dc.Title
				StringBuffer result = new StringBuffer();
				boolean first = true;
				for (int i = 0; i < values.size(); i++)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						result.append(separator);
					}
					result.append(this.macro_resolver.resolve(values.elementAt(i), lang, MacroResolver.SCOPE_META, node_id));
				}
				return result.toString();
			}
			else
			{
				String result = info.getInfo(metadata);
				return this.macro_resolver.resolve(result, lang, MacroResolver.SCOPE_META, node_id);
			}
		}

		String temp = metadata.substring(0, index);		
		metadata = metadata.substring(index + 1);
		// check for pos on the front, indicating which piece of meta the user wants
		// pos can be "first", "last" or the position value of the requested piece of metadata 
		if (temp.startsWith(GSConstants.META_POS) || temp.equals("all"))
		{
		    if (temp.startsWith(GSConstants.META_POS)) {
			temp = temp.substring(GSConstants.META_POS.length());
			pos = temp;
		    }
			
			index = metadata.indexOf(GSConstants.META_RELATION_SEP);
			if (index == -1)
			{
				temp = "";
			}
			else
			{
				temp = metadata.substring(0, index);
				metadata = metadata.substring(index + 1);
			}
		}

		// now check for relational info
		if (temp.equals("parent") || temp.equals("root") || temp.equals("ancestors")
		     || temp.equals("siblings") || temp.equals("children") || temp.equals("descendants"))
		{ // "current" "siblings" "children" "descendants"
			// gets all siblings by default
			relation = temp;
			index = metadata.indexOf(GSConstants.META_RELATION_SEP);
			if (index == -1)
			{
				temp = "";
			}
			else
			{
				temp = metadata.substring(0, index);
				metadata = metadata.substring(index + 1);
			}
		}

		// now look for separator info
		if (temp.startsWith(GSConstants.META_SEPARATOR_SEP) && temp.endsWith(GSConstants.META_SEPARATOR_SEP))
		{
			separator = temp.substring(1, temp.length() - 1);

		}

		String relation_id = node_id;
		if (relation.equals("parent") || relation.equals("ancestors"))
		{
			relation_id = OID.getParent(node_id);
			// parent or ancestor does not include self
			if (relation_id.equals(node_id))
			{
				return "";
			}
		}
		else if (relation.equals("root"))
		{
			relation_id = OID.getTop(node_id);
		}

		// now we either have a single node, or we have ancestors	
		DBInfo relation_info;
		if (relation_id.equals(node_id))
		{
			relation_info = info;
		}
		else
		{
			relation_info = this.coll_db.getInfo(relation_id);
		}
		if (relation_info == null)
		{
			return "";
		}

		StringBuffer result = new StringBuffer();
		
		Vector<String> values = relation_info.getMultiInfo(metadata);

		if (!pos.equals("")) // if a particular position was specified, so not multiple values for the metadata
		{
			String meta = "";
			if (values != null) {
				if(pos.equals(GSConstants.META_FIRST)) {				
					meta = values.firstElement();
				} else if(pos.equals(GSConstants.META_LAST)) {
					meta = values.lastElement();
				} else {
					int position = Integer.parseInt(pos);
					if(position < values.size()) {			
						meta = values.elementAt(position);
					}
				}				
			} // else ""
						
			result.append(this.macro_resolver.resolve(meta, lang, MacroResolver.SCOPE_META, relation_id));			
		}
		else
		{
			if (values != null)
			{
				boolean first = true;
				for (int i = 0; i < values.size(); i++)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						result.append(separator);
					}
					result.append(this.macro_resolver.resolve(values.elementAt(i), lang, MacroResolver.SCOPE_META, relation_id));
				}
			}
			logger.info(result);
		}
		// if not ancestors, then this is all we do
		if (!relation.equals("ancestors"))
		{
			return result.toString();
		}

		// now do the ancestors 
		String current_id = relation_id;
		relation_id = OID.getParent(current_id);
		while (!relation_id.equals(current_id))
		{
			relation_info = this.coll_db.getInfo(relation_id);
			if (relation_info == null)
				return result.toString();
			
			values = relation_info.getMultiInfo(metadata);
			if (!pos.equals("")) // if a particular position was specified, so not multiple values for the metadata
			{
				String meta = "";
				if (values != null) {
					if(pos.equals(GSConstants.META_FIRST)) {
						meta = values.firstElement();
					} else if(pos.equals(GSConstants.META_LAST)) {
						meta = values.lastElement();
					} else {
						int position = Integer.parseInt(pos);
						if(position < values.size()) {			
							meta = values.elementAt(position);
						}
					}				
				} // else ""
				
				result.insert(0, separator);
				result.insert(0, this.macro_resolver.resolve(meta, lang, MacroResolver.SCOPE_META, relation_id));							
			}
			else
			{
				if (values != null)
				{
					for (int i = values.size() - 1; i >= 0; i--)
					{
						result.insert(0, separator);
						result.insert(0, this.macro_resolver.resolve(values.elementAt(i), lang, MacroResolver.SCOPE_META, relation_id));
					}
				}

			}
			current_id = relation_id;
			relation_id = OID.getParent(current_id);
		}
		return result.toString();
	}

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
