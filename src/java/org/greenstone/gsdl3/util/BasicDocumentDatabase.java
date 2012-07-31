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
import java.util.StringTokenizer;
import java.util.Iterator;
import java.io.File;

import org.apache.commons.lang3.StringUtils;
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
    
	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS, INFO_DOC_TYPE
	 */
  public String getStructureInfo(String doc_id, String info_type){
		String value = "";
		if (info_type.equals(INFO_NUM_SIBS))
		{
			String parent_id = OID.getParent(doc_id);
			if (parent_id.equals(doc_id))
			{
				value = "0";
			}
			else
			{
				value = String.valueOf(getNumChildren(parent_id));
			}
			return value;
		}

		if (info_type.equals(INFO_NUM_CHILDREN))
		{
			return String.valueOf(getNumChildren(doc_id));
		}

		if (info_type.equals(INFO_SIB_POS))
		{
			String parent_id = OID.getParent(doc_id);
			if (parent_id.equals(doc_id))
			{
				return "-1";
			}

			DBInfo info = this.coll_db.getInfo(parent_id);
			if (info == null)
			{
				return "-1";
			}

			String contains = info.getInfo("contains");
			contains = StringUtils.replace(contains, "\"", parent_id);
			String[] children = contains.split(";");
			for (int i = 0; i < children.length; i++)
			{
				String child_id = children[i];
				if (child_id.equals(doc_id))
				{
					return String.valueOf(i + 1); // make it from 1 to length

				}
			}

			return "-1";
		}
		if (info_type.equals(INFO_DOC_TYPE))
	
		{
		  return getDocType(doc_id);
		}
		return null;

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
    
  public int getNumChildren(String node_id) {
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return 0;
		}
		String contains = info.getInfo("contains");
		if (contains.equals(""))
		{
			return 0;
		}
		String[] children = contains.split(";");
		return children.length;


  }
  /** returns a list of the child ids in order, null if no children
   */
  public ArrayList<String> getChildrenIds(String node_id) {
		DBInfo info = this.coll_db.getInfo(node_id);
		if (info == null)
		{
			return null;
		}

		String contains = info.getInfo("contains");
		if (contains.equals(""))
		{
			return null;
		}
		ArrayList<String> children = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(contains, ";");
		while (st.hasMoreTokens())
		{
			String child_id = StringUtils.replace(st.nextToken(), "\"", node_id);
			children.add(child_id);
		}
		return children;


  }

    /** returns true if the node has a parent */
    public boolean hasParent(String node_id){
	String parent = OID.getParent(node_id);
	if (parent.equals(node_id)) {
	    return false;
	}
	return true;
    }
	/**
	 * returns the node id of the parent node, null if no parent 
	 */
  public String getParentId(String node_id) {
		String parent = OID.getParent(node_id);
		if (parent.equals(node_id))
		{
			return null;
		}
		return parent;

  }
    
  /** 
   * returns the node id of the root node of the document containing node_id
   */
  public String getRootId(String node_id) {
    return OID.getTop(node_id);
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


	/**
	 * adds all the children of doc_id to the doc element, and if
	 * recursive=true, adds all their children as well
	 */
	public void addDescendants(Element doc, String doc_id, boolean recursive)
	{
		ArrayList<String> child_ids = getChildrenIds(doc_id);
		if (child_ids == null)
			return;
		for (int i = 0; i < child_ids.size(); i++)
		{
			String child_id = child_ids.get(i);
			Element child_elem = createDocNode(child_id);
			doc.appendChild(child_elem);
			if (recursive && (!child_elem.getAttribute(GSXML.NODE_TYPE_ATT).equals(GSXML.NODE_TYPE_LEAF) || child_elem.getAttribute(GSXML.DOC_TYPE_ATT).equals(GSXML.DOC_TYPE_PAGED)))
			{
				addDescendants(child_elem, child_id, recursive);
			}
		}
	}

	/**
	 * adds all the siblings of current_id to the parent element. returns the
	 * new current element
	 */
	public Element addSiblings(Element parent_node, String parent_id, String current_id)
	{
		Element current_node = GSXML.getFirstElementChild(parent_node);//(Element)parent_node.getFirstChild();
		if (current_node == null)
		{
			// create a sensible error message
			logger.error(" there should be a first child.");
			return null;
		}
		// remove the current child,- will add it in later in its correct place
		parent_node.removeChild(current_node);

		// add in all the siblings,
		addDescendants(parent_node, parent_id, false);

		// find the node that is now the current node
		// this assumes that the new node that was created is the same as 
		// the old one that was removed - we may want to replace the new one 
		// with the old one.
		Element new_current = GSXML.getNamedElement(parent_node, current_node.getNodeName(), GSXML.NODE_ID_ATT, current_id);
		return new_current;
	}

  /** returns the list of sibling ids, including the specified node_id */
  public ArrayList<String> getSiblingIds(String node_id){
		String parent_id = getParentId(node_id);
		if (parent_id == null)
		{
			return null;
		}
		return getChildrenIds(parent_id);

  }

}


