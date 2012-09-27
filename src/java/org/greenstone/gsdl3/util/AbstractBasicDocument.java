/*
 *    AbstractBasicDocument.java
 *    Copyright (C) 2011 New Zealand Digital Library, http://www.nzdl.org
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

import org.apache.log4j.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractBasicDocument {

  /** info types */
	public static final String INFO_NUM_SIBS = "numSiblings";
	public static final String INFO_NUM_CHILDREN = "numChildren";
	public static final String INFO_SIB_POS = "siblingPosition";
	public static final String INFO_DOC_TYPE = "documentType";

    /** XML element for describe requests - the container doc */
    protected Document doc = null; // typically a shared reference to the one in ServiceRack

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.AbstractBasicDocument.class.getName());

    public AbstractBasicDocument(Document doc) 
    {
	this.doc = doc;
    }

    /** create an element to go into a results list. A node element
     * has the form
     * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy'/>
     */
  public Element createDocNode(String node_id) {
    return createDocNode(node_id, null);
  }
    /** create an element to go into a results list. A node element
     * has the form
     * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy' [rank='0.23']/>
     */
    public Element createDocNode(String node_id, String rank) {
	Element node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
	node.setAttribute(GSXML.NODE_ID_ATT, node_id);
	if (rank != null) {
	  node.setAttribute(GSXML.NODE_RANK_ATT, rank);
	}
	String doc_type = getDocType(node_id);
	node.setAttribute(GSXML.DOC_TYPE_ATT, doc_type);
	String node_type = getNodeType(node_id, doc_type);	
	node.setAttribute(GSXML.NODE_TYPE_ATT, node_type);
	return node;
    }

	/**
	 * adds all the children of doc_id to the doc element, and if
	 * recursive=true, adds all their children as well
	 */
  abstract public void addDescendants(Element doc, String doc_id, boolean recursive);
  

	/**
	 * adds all the siblings of current_id to the parent element. returns the
	 * new current element
	 */
  abstract public Element addSiblings(Element parent_node, String parent_id, String current_id);

    /** returns the node type of the specified node.
	should be one of 
	GSXML.NODE_TYPE_LEAF, 
	GSXML.NODE_TYPE_INTERNAL, 
	GSXML.NODE_TYPE_ROOT
    */
    public String getNodeType(String node_id, String doc_type) {
	if (doc_type.equals(GSXML.DOC_TYPE_SIMPLE)) {
	    return GSXML.NODE_TYPE_LEAF;
	}

	if (!hasParent(node_id)) {
	    return GSXML.NODE_TYPE_ROOT;
	}
	if (doc_type.equals(GSXML.DOC_TYPE_PAGED)) {
	     return GSXML.NODE_TYPE_LEAF;
	}
	if (!hasChildren(node_id)) {
	    return GSXML.NODE_TYPE_LEAF;
	}
	return GSXML.NODE_TYPE_INTERNAL;	
	
    }

    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_PAGEDHIERARCHY, 
	GSXML.DOC_TYPE_HIERARCHY
    */
    abstract public String getDocType(String node_id);
    
    /** returns true if the node has child nodes 
     */
    abstract public boolean hasChildren(String node_id);
    
	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS, INFO_DOC_TYPE
	 */
	abstract public String getStructureInfo(String doc_id, String info_type);

  abstract public int getNumChildren(String node_id) ;
  /** returns a list of the child ids in order, null if no children
   */
  abstract public ArrayList<String> getChildrenIds(String node_id);
    /** returns true if the node has a parent 
     */
    abstract public boolean hasParent(String node_id);

	/**
	 * returns the node id of the parent node, null if no parent 
	 */
  abstract public String getParentId(String node_id);

  /** 
   * returns the node id of the root node of the document containing node_id
   */
  abstract public String getRootId(String node_id);

  /** returns the list of sibling ids, including the specified node_id */
  abstract public ArrayList<String> getSiblingIds(String node_id);
}