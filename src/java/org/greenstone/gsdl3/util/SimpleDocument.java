/*
 *    SimpleDocument.java
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

import org.apache.log4j.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SimpleDocument extends AbstractSimpleDocument {


    /** the default document type - use if all documents are the same type
     */
    protected String default_document_type = null;

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.SimpleDocument.class.getName());

    public SimpleDocument(Document doc, String default_document_type)
    {
	super(doc);

	this.default_document_type = default_document_type;
    }

    /** create an element to go into the search results list. A node element
     * has the form
     * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy' rank='0.23'/>
     */
    public Element createDocNode(String node_id, String rank) {
	// override to provide version that checks 'default_document_type'
 
	Element node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
	node.setAttribute(GSXML.NODE_ID_ATT, node_id);
	node.setAttribute(GSXML.NODE_RANK_ATT, rank);
	String doc_type = null;
	if (default_document_type != null) {
	    doc_type = default_document_type;
	} else {
	    doc_type = getDocType(node_id);
	}
	node.setAttribute(GSXML.DOC_TYPE_ATT, doc_type);
	String node_type = getNodeType(node_id, doc_type);	
	node.setAttribute(GSXML.NODE_TYPE_ATT, node_type);
	return node;
    }


    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_HIERARCHY
	default implementation returns GSXML.DOC_TYPE_SIMPLE, over ride 
	if documents can be hierarchical
    */
    public String getDocType(String node_id) {
	return GSXML.DOC_TYPE_SIMPLE;
    }
    
    /** returns true if the node has child nodes 
     * default implementation returns false, over ride if documents can be 
     * hierarchical 
     */
    public boolean hasChildren(String node_id) {
	return false;
    }

    /** returns true if the node has a parent 
     * default implementation returns false, over ride if documents can be 
     * hierarchical*/
    public boolean hasParent(String node_id) {
	return false;
    }
}