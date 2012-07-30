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

    /** returns true if the node has a parent 
     * default implementation returns false, over ride if documents can be 
     * hierarchical*/
    public boolean hasParent(String node_id) {
	return false;
    }
}