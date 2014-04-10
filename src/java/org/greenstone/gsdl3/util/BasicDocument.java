/*
 *    BasicDocument.java
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

public class BasicDocument extends AbstractBasicDocument {


    /** the default document type - use if all documents are the same type
     */
    protected String default_document_type = null;

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.BasicDocument.class.getName());

  public BasicDocument(String default_document_type)
    {
	super();

	this.default_document_type = default_document_type;
    }

	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS, INFO_DOC_TYPE
	 */
  public String getStructureInfo(String doc_id, String info_type) {
    if (info_type.equals(INFO_NUM_SIBS)) {
      return "0";
    }
    if (info_type.equals(INFO_NUM_CHILDREN)) {
      return "0";
    }
    if (info_type.equals(INFO_SIB_POS)) {
      return "-1";
    }
    if (info_type.equals(INFO_DOC_TYPE)) {
      return getDocType(doc_id);
    }
    return null;
  }
    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_HIERARCHY
	GSXML.DOC_TYPE_PAGEDHIERARCHY
	default implementation returns GSXML.DOC_TYPE_SIMPLE, over ride 
	if documents can be hierarchical
    */
    public String getDocType(String node_id) {
      if (default_document_type != null) {
	return default_document_type;
      }
      return GSXML.DOC_TYPE_SIMPLE;
    }
    
    /** returns true if the node has child nodes 
     * default implementation returns false, over ride if documents can be 
     * hierarchical 
     */
    public boolean hasChildren(String node_id) {
	return false;
    }
  public int getNumChildren(String node_id) {
    return 0;
  }
  /** returns a list of the child ids in order, null if no children
   * default implementation: return null
   */
  public ArrayList<String> getChildrenIds(String node_id) {
    return null;
  }
    /** returns true if the node has a parent 
     * default implementation returns false, over ride if documents can be 
     * hierarchical*/
    public boolean hasParent(String node_id) {
	return false;
    }

	/**
	 * returns the node id of the parent node, null if no parent 
	 * default implementation return null
	 */
  public String getParentId(String node_id) {
    return null;
  }

  /** 
   * returns the node id of the root node of the document containing node_id
   * default implementation: return node_id
   */
  public String getRootId(String node_id) {
    return node_id;
  }
	/**
	 * adds all the children of doc_id to the doc element, and if
	 * recursive=true, adds all their children as well
	 * default implementation: do nothing
	 */
  public void addDescendants(Element doc, String doc_id, boolean recursive) {
    return;
  }

	/**
	 * adds all the siblings of current_id to the parent element. returns the
	 * new current element
	 */
  public Element addSiblings(Element parent_node, String parent_id, String current_id)
  {
    return null;
  }

  /** returns the list of sibling ids, including the specified node_id */
  public ArrayList<String> getSiblingIds(String node_id) {
    return null;
  }

 
}