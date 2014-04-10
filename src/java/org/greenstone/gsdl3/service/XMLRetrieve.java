/*
 *    ServiceRack.java
 *    Copyright (C) 2014 New Zealand Digital Library, http://www.nzdl.org
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
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

// General Java classes
import java.io.File;
import java.io.Serializable;
import java.util.Vector;
import java.util.HashMap;

import org.apache.log4j.*;

public class XMLRetrieve extends ServiceRack {

      static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.XMLRetrieve.class.getName());
    protected static final String CONTENT_SERVICE = "DocumentContentRetrieve";
    protected static final String METADATA_SERVICE = "DocumentMetadataRetrieve";
    protected static final String STRUCTURE_SERVICE = "DocumentStructureRetrieve";
    
    protected String toc_xsl_name = "";
    protected String document_encoding = "";
    protected String document_root_tag = "";
    
    protected Element collection_doc_list = null;
    
    protected boolean provide_content = true;
    protected boolean provide_structure = true;
    protected boolean provide_metadata = true;
    
  protected GSEntityResolver entity_resolver = null;

    public boolean configure(Element info, Element extra_info) {
	if (!super.configure(info, extra_info)){
	    return false;
	}
	logger.info("configuring XMLRetrieve...");
	// look for the parameters
	Element param_list = (Element)GSXML.getChildByTagName(info, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	HashMap<String, Serializable> params;
	String services_to_provide = "";
	if (param_list != null) {
	    params = GSXML.extractParams(param_list, false);
	    this.toc_xsl_name = (String)params.get("tocXSLT");
	    this.document_encoding = (String)params.get("documentEncoding");
	    this.document_root_tag = (String)params.get("documentRootTag");
	    services_to_provide = (String)params.get("provideServices");
	}
	if (this.toc_xsl_name == null || this.toc_xsl_name.equals("")) {
	    this.toc_xsl_name = "default_toc";
	}
	this.toc_xsl_name  = this.toc_xsl_name+".xsl";
	
	if (this.document_encoding == null || this.document_encoding.equals("")) {
	    this.document_encoding = "UTF-8";
	}
	
	if (services_to_provide != null && !services_to_provide.equals("")) {
	    if (services_to_provide.indexOf("content")==-1) {
		provide_content = false;
	    }
	    if (services_to_provide.indexOf("metadata")==-1) {
		provide_metadata = false;
	    }
	    if (services_to_provide.indexOf("structure")==-1) {
		provide_structure = false;
	    }
	    
	}
	
	// set up short_service_info_ - for now just has name and type
	Element retrieve_service;
	if (provide_content) {
	    retrieve_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
	    retrieve_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	    retrieve_service.setAttribute(GSXML.NAME_ATT, CONTENT_SERVICE);
	    this.short_service_info.appendChild(retrieve_service);
	}
	if (provide_metadata) {
	    retrieve_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
	    retrieve_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	    retrieve_service.setAttribute(GSXML.NAME_ATT, METADATA_SERVICE);
	    this.short_service_info.appendChild(retrieve_service);
	}
	if (provide_structure) {
	    retrieve_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
	    retrieve_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	    retrieve_service.setAttribute(GSXML.NAME_ATT, STRUCTURE_SERVICE);
	    this.short_service_info.appendChild(retrieve_service);
	}
	// find the doc list from the extra_info and keep it - should this be in collect.cfg or build.cfg??
	collection_doc_list = (Element)GSXML.getChildByTagName(extra_info, GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER);

	entity_resolver = new GSEntityResolver();
	entity_resolver.setClassLoader(this.class_loader);
	//this.converter.setEntityResolver(resolver);
	return true;
    }

    // this may get called but is not useful in the case of retrieve services
    protected Element getServiceDescription(Document doc, String service_id, String lang, String subset) {

	Element retrieve_service = doc.createElement(GSXML.SERVICE_ELEM);
	retrieve_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	retrieve_service.setAttribute(GSXML.NAME_ATT, service_id);
	return retrieve_service;
    }
    
    protected Element processDocumentContentRetrieve(Element request) {
      Document result_doc = XMLConverter.newDOM();
	Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, CONTENT_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	
	Element doc_list = (Element)GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	if (doc_list == null) {
	    return result;
	}
	Element result_doc_list = (Element)result_doc.importNode(doc_list, true);
	result.appendChild(result_doc_list);

	NodeList docs = result_doc_list.getElementsByTagName(GSXML.DOC_NODE_ELEM);
	for (int i=0; i<docs.getLength(); i++) {

	    Element doc = (Element)docs.item(i);
	    Element content = result_doc.createElement(GSXML.NODE_CONTENT_ELEM);
	    doc.appendChild(content);

	    String node_id = doc.getAttribute(GSXML.NODE_ID_ATT);
	    String doc_name = getWorkName(node_id);
	    
	    Element doc_elem = loadDocument(doc_name); // should perhaps cache the read in docs??
	    if (doc_elem == null) {
		continue;
	    } 

	    
	    // if we have asked for the whole doc, just append it
	    if (doc_name.equals(node_id)) {
		content.appendChild(result_doc.importNode(doc_elem, true));
		continue;
	    }
		   
	    // else we only want a sub section
	    
	    Element section = getSection(doc_elem, node_id);
	    if (section != null) {
		content.appendChild(result_doc.importNode(section, true));
	    }
	    
	} // for each doc

	return result;
	
    }
    
    protected Element processDocumentStructureRetrieve(Element request) {
      Document result_doc = XMLConverter.newDOM();
	Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, STRUCTURE_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	
	Element doc_list = (Element)GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	if (doc_list == null) {
	    logger.error("no documents specified in the request. ");
	    return result;
	}

	Element result_doc_list = (Element)result_doc.importNode(doc_list, true);
	result.appendChild(result_doc_list);
	// first look for the stylesheet in the collection
	File stylesheet = new File(GSFile.collStylesheetFile(this.site_home, this.cluster_name, this.toc_xsl_name));
	if (!stylesheet.exists()) {
	    // now try in the site
	    stylesheet = new File(GSFile.siteStylesheetFile(this.site_home, this.toc_xsl_name));
	}
	if (!stylesheet.exists()) {
	    logger.error("couldn't find the stylesheet file to produce the table of contents:"+stylesheet.getPath());
	    return result;
	}

	// for now, we dont have any params, and we always return the structure of the whole document
	
	XMLTransformer transformer = new XMLTransformer();
	NodeList docs = result_doc_list.getElementsByTagName(GSXML.DOC_NODE_ELEM);

	for (int i=0; i<docs.getLength(); i++) {

	    Element doc = (Element)docs.item(i);
	    
	    Element structure = result_doc.createElement(GSXML.NODE_STRUCTURE_ELEM);
	    doc.appendChild(structure);
	    String doc_name = doc.getAttribute(GSXML.NODE_ID_ATT);
	    // make sure we are at the top level
	    doc_name = getWorkName(doc_name);
	    
	    File doc_file = new File(GSFile.collectionIndexDir(this.site_home, this.cluster_name)+File.separator+"text"+File.separatorChar+doc_name+".xml");
	    
	    if (!doc_file.exists()) {
		logger.error("couldn't find file in coll "+this.cluster_name +", file "+doc_name+".xml");
	    } else {
		try {
		    Node toc = transformer.transform(stylesheet, doc_file);
		    structure.appendChild(result_doc.importNode(toc, true));
		} catch (Exception e) {
		    logger.error("couldn't transform the document to get the toc");
		}
	    }
	    
	}
	
	return result;
	
    }
    
    // this just extracts a bit of text from the section to use as the Title
    // this should be overwritten for any format that  has something more suitable
    protected Element processDocumentMetadataRetrieve(Element request) {
      Document result_doc = XMLConverter.newDOM();
	Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, METADATA_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	
	Element doc_list = (Element)GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	if (doc_list == null) {
	    logger.error("no documents in the request");
	    return result;
	}

	Element result_doc_list = (Element)result_doc.importNode(doc_list, true);
	result.appendChild(result_doc_list);

	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("no metadata in the request");
	    return result;
	}
	
	Vector<String> meta_name_list = new Vector<String>();
	boolean all_metadata = false;
	// Process the request parameters
	Element param = GSXML.getFirstElementChild(param_list);//(Element) param_list.getFirstChild();
	while (param != null) {
	    // Identify the metadata information desired
	    if (param.getAttribute(GSXML.NAME_ATT).equals("metadata")) {
		String metadata = GSXML.getValue(param);
		if (metadata.equals("all")) {
		    all_metadata = true;
		    break;
		}
		meta_name_list.add(metadata);
	    }
	    param = (Element) param.getNextSibling();
	}
		
	NodeList docs = result_doc_list.getElementsByTagName(GSXML.DOC_NODE_ELEM);
	for (int i=0; i<docs.getLength(); i++) {
	    Element doc = (Element)docs.item(i);
	    String node_id = doc.getAttribute(GSXML.NODE_ID_ATT);
	    String doc_name = getWorkName(node_id);
	    
	    Element metadata_list = getMetadata(result_doc, node_id, all_metadata, meta_name_list); 
	    doc.appendChild(metadata_list);
	}
	
	return result;
    }
    
    protected Element loadDocument(String doc_name) {
	// try to find the document
	File doc_file = new File(GSFile.collectionIndexDir(this.site_home, this.cluster_name)+File.separator+"text"+File.separatorChar+doc_name+".xml");
	    
	if (!doc_file.exists()) {
	    logger.info("couldn't find file in coll "+this.cluster_name +", file "+doc_name+".xml");
	    return null;
	}
	
	Document the_doc = null;
	try {
	  the_doc = this.converter.getDOM(doc_file, this.document_encoding, this.entity_resolver);
	} catch (Exception e) {
	    logger.error("couldn't create a DOM from file "+doc_file.getPath());
	    return null;
	}
	
	return the_doc.getDocumentElement();

    }

    
    protected Element getSection(Element doc_elem, String node_id) {
	String [] bits = node_id.split("\\.");
	if (bits.length > 4) {
	    logger.error("badly formatted node id ("+node_id +"), cant retrieve the section");
	    return null;
	}
	    
	String id="";
	String tagname = "";
	String scope = "";
	if (bits.length==2) {
	    tagname = bits[1];
	} else { 
	    scope = bits[1];
	    tagname = bits[2];
	    
	    if (bits.length == 4) {
		id = bits[3];
	    }
	}
	scope = translateScope(scope);
	Element top=null;
	if (!scope.equals("")) {
	    top = (Element)GSXML.getNodeByPath(doc_elem, scope);
	    if (top == null) {
		// something gone wrong
		return null;
	    }
	} else {
	    top = doc_elem;
	}
	
	NodeList elements = top.getElementsByTagName(tagname);
	if (elements.getLength() == 0) {
	    return null;
	}
	// no id, just return the first one
	if (id.equals("")) {
	    return (Element)elements.item(0);
	}
	// have an id, need to check and find the right one.
	for (int i=0; i<elements.getLength();i++) {
	    Element e = (Element)elements.item(i);
	    if (e.getAttribute("gs3:id").equals(id)) {
		return e;
	    }
	}
	return null;
	
    }

  protected Element getMetadata(Document result_doc, String node_id, boolean all, Vector<String> meta_name_list) {

	// our default strategy here is to only return Title and root:Title
	// ignore all others 
	// the title of a section is just a little bit of the text inside it.
	// the root_Title is the title from the doc info in the config file
	Element metadata_list = result_doc.createElement(GSXML.METADATA_ELEM+ GSXML.LIST_MODIFIER);
	String doc_name = getWorkName(node_id);
	boolean node_is_root = false;
	if (doc_name.equals(node_id)) {
	    node_is_root = true;
	}
	
	Element this_doc = GSXML.getNamedElement(this.collection_doc_list, GSXML.DOCUMENT_ELEM, GSXML.NAME_ATT, doc_name);
	Element doc_meta_list  = (Element) GSXML.getChildByTagName(this_doc, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	
	boolean get_section_title = false;
	
	if (all) {
	    if (node_is_root) {
	    return (Element)result_doc.importNode(doc_meta_list, true);
	    } else {
		get_section_title = true;
	    }
	    
	} else {
	    // have to process metadata one by one
	    for (int i=0; i<meta_name_list.size(); i++) {
		String meta_name = meta_name_list.elementAt(i);
		String actual_meta_name = meta_name;
		if (meta_name.startsWith("root_")) {
		    actual_meta_name = meta_name.substring(5);
		} else {
		    // its a section level one - check to see if doc is root
		    if (!node_is_root) {
			if (meta_name.equals("Title")) {
			    get_section_title = true;
			}
			continue; // move on to teh next metadata
		    }
		}
		
		// here, we look for the specific meta elem in doc_meta_list
		Element meta_item = GSXML.getNamedElement(doc_meta_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, actual_meta_name);
		if (meta_item != null) {
		    meta_item = (Element)result_doc.importNode(meta_item, true);
		    meta_item.setAttribute(GSXML.NAME_ATT, meta_name);
		    metadata_list.appendChild(meta_item);
		}
	    } // for each metadata
	}

	// now we have processed all teh doc metadata, just have section one to go, if needed
	if (get_section_title) {
	    
	    Element doc_elem = loadDocument(doc_name);
	    if (doc_elem != null) {
		Element section = getSection(doc_elem, node_id);
		if (section != null) {
		  Element title_meta = extractTitleMeta(result_doc, section);
		    if (title_meta != null) {
			metadata_list.appendChild(title_meta);
		    }
		}
	    }
		
	}
	return metadata_list;
    }
    
  protected Element extractTitleMeta(Document result_doc, Element section) {
	Element meta_elem = result_doc.createElement(GSXML.METADATA_ELEM);
	meta_elem.setAttribute(GSXML.NAME_ATT, "Title");

	String title = "dummy title";
	Text t = result_doc.createTextNode(title);
	meta_elem.appendChild(t);
	return meta_elem;

    }
    // some methods for handling nodeIDs - they may be different for different colls, so they can be overwritten

    // the full default nodeID looks like work.scope.tag.id
    // the shorter versions are work, work.tag, work.scope.tag
    protected String getWorkName(String node_id) {
	int pos = node_id.indexOf('.');
	if (pos == -1) {
	    return node_id;
	}
	return node_id.substring(0, pos);
    }
    
    // this assumes that the scope refers to a top level node - this may be overwritten if the scope bit in the id is a shorthand of some sort
    protected String translateScope(String scope) {
	if (this.document_root_tag != null) {
	    return GSPath.appendLink(this.document_root_tag, scope);
	}
	return scope;
    }
    
}

