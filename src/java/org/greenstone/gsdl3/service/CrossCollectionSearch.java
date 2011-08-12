/*
 *    CrossCollectionSearch.java
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

import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;

import org.apache.log4j.*;

/**
 *
 * @author <a href="mailto:greenstone@cs.waikato.ac.nz">Katherine Don</a>
 */

public class CrossCollectionSearch
    extends ServiceRack {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.CrossCollectionSearch.class.getName());
    protected static final String QUERY_PARAM = "query";
    protected static final String COLLECTION_PARAM = "collection";

    // the services on offer - these proxy the actual collection ones
    protected static final String TEXT_QUERY_SERVICE = "TextQuery";
    protected static final String ADV_QUERY_SERVICE = "AdvTextQuery";
    protected static final String DOCUMENT_METADATA_RETRIEVE_SERVICE = "DocumentMetadataRetrieve";
    
    protected String [] coll_ids_list = null;
    protected String [] coll_ids_list_no_all = null;
    protected String [] coll_names_list = null;

    
    /** constructor */
    public CrossCollectionSearch()
    {
    }

    public boolean configure(Element info, Element extra_info) 
    {
	// any parameters? colls to include??
	logger.info("Configuring CrossCollectionSearch...");
	// query service
	Element ccs_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	ccs_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	ccs_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	this.short_service_info.appendChild(ccs_service);

	// metadata service
	Element dmr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	dmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	dmr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
	this.short_service_info.appendChild(dmr_service);

	// get any format info
	Element format_info = (Element)GSXML.getChildByTagName(info, GSXML.FORMAT_ELEM);
	if (format_info != null) {
	    this.format_info_map.put(TEXT_QUERY_SERVICE, this.doc.importNode(format_info, true));
	} else {
	    // add in a default format statement
	    String format_string = "<format xmlns:gsf='http://www.greenstone.org/greenstone3/schema/ConfigFormat' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'><gsf:template match='documentNode'><td><a><xsl:attribute name='href'>?a=d&amp;c=<xsl:value-of select='@collection'/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test=\"@nodeType='leaf'\">&amp;sib=1</xsl:if>&amp;dt=<xsl:value-of select='@docType'/>&amp;p.a=q&amp;p.s="+TEXT_QUERY_SERVICE+"&amp;p.c=";
	    if (this.cluster_name!=null) {
		format_string += this.cluster_name;
	    }
	    format_string += "</xsl:attribute><gsf:icon/></a></td><td><gsf:metadata name='Title'/> (<xsl:value-of select='@collection'/>) </td></gsf:template></format>";
	    this.format_info_map.put(TEXT_QUERY_SERVICE, this.doc.importNode(this.converter.getDOM(format_string).getDocumentElement(), true));
	}
	return true;
    }

    protected Element getServiceDescription(String service, String lang, String subset) 
    {
	if (service.equals(TEXT_QUERY_SERVICE)) {

	    Element ccs_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    ccs_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	    ccs_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	    
	    // display info
	    if (subset==null || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
		ccs_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(TEXT_QUERY_SERVICE+".name", lang)));
		ccs_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(TEXT_QUERY_SERVICE+".submit", lang)));
		ccs_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(TEXT_QUERY_SERVICE+".description", lang)));
	    }
	    // param info
	    if (subset==null || subset.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) {
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		// collection list
		if (coll_ids_list ==null ) {
		    initCollectionList();
		}
		Element param = GSXML.createParameterDescription(this.doc, COLLECTION_PARAM, getTextString("param."+COLLECTION_PARAM, lang), GSXML.PARAM_TYPE_ENUM_MULTI, "all", coll_ids_list, coll_names_list);
		param_list.appendChild(param);
		// query param
		param = GSXML.createParameterDescription(this.doc, QUERY_PARAM, getTextString("param."+QUERY_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
		param_list.appendChild(param);
		ccs_service.appendChild(param_list);
	    }
	    
	    logger.debug("service description="+this.converter.getPrettyString(ccs_service));
	    return ccs_service;
	}
	// these ones are probably never called, but put them here just in case
	Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
	service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	service_elem.setAttribute(GSXML.NAME_ATT, service);
	return service_elem;
    

    }

    
    protected Element processTextQuery(Element request) {
	// Create a new (empty) result message
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, TEXT_QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	String lang = request.getAttribute(GSXML.LANG_ATT);
	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    return result;  // Return the empty result
	}

	// get the collection list
	String [] colls_list = coll_ids_list_no_all;
	Element coll_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT,  COLLECTION_PARAM);
	if (coll_param != null) {
	    String coll_list = GSXML.getValue(coll_param);
	    if (!coll_list.equals("all") &&  !coll_list.equals("")) {
		colls_list = coll_list.split(",");
	    }
	}
	
	Element query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	// we are sending the same request to each collection - build up the to
	// attribute for the request
	StringBuffer to_att = new StringBuffer();
	for (int i=0; i<colls_list.length; i++) {
	    if (i>0) {
		to_att.append(",");
	    }
	    to_att.append(GSPath.appendLink(colls_list[i], "TextQuery"));
	    
	}
	// send the query to all colls
	Element query_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to_att.toString(), lang, "");
	query_message.appendChild(query_request);
	// should we add params individually?
	Element new_param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	query_request.appendChild(new_param_list);
	new_param_list.appendChild(this.doc.importNode(GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, QUERY_PARAM), true));
	Element query_result = (Element)this.router.process(query_message);
	
	// gather up the data from each response
	int numDocsMatched = 0;
	int numDocsReturned = 0;

	//term info??

	NodeList metadata = query_result.getElementsByTagName(GSXML.METADATA_ELEM);
	for (int j=0; j<metadata.getLength(); j++) {
	    Element meta = (Element)metadata.item(j);
	    if (meta.getAttribute(GSXML.NAME_ATT).equals("numDocsReturned")) {
		numDocsReturned += Integer.parseInt(GSXML.getValue(meta));
	    } else if (meta.getAttribute(GSXML.NAME_ATT).equals("numDocsMatched")) {
		numDocsMatched += Integer.parseInt(GSXML.getValue(meta));
	    }
	}
	
	Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(metadata_list);
	GSXML.addMetadata(this.doc, metadata_list, "numDocsReturned", ""+numDocsReturned);
	//GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", ""+numDocsMatched);

	Element doc_node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(doc_node_list);
	
	NodeList responses = query_result.getElementsByTagName(GSXML.RESPONSE_ELEM);
	
	for (int k=0; k<responses.getLength(); k++) {
	    String coll_name = GSPath.removeLastLink(((Element)responses.item(k)).getAttribute(GSXML.FROM_ATT));
	    NodeList nodes = ((Element)responses.item(k)).getElementsByTagName(GSXML.DOC_NODE_ELEM);
	    if (nodes==null || nodes.getLength()==0) continue;
	    Element last_node = null;
	    Element this_node = null;
	    for (int n=0; n<nodes.getLength(); n++) {
		this_node = (Element)nodes.item(n);
		this_node.setAttribute("collection", coll_name);
		if (k==0) {

		    doc_node_list.appendChild(this.doc.importNode(this_node, true));
		} else {
		    if (last_node==null) {
			last_node = (Element)GSXML.getChildByTagName(doc_node_list, GSXML.DOC_NODE_ELEM);
		    }
		    last_node = GSXML.insertIntoOrderedList(doc_node_list, GSXML.DOC_NODE_ELEM, last_node, this_node, "rank", true);
		}
		
	    }
	}
	return result;
    }

//     protected Element processAdvTextQuery(Element request) 
//     {
	
//     }
    protected boolean initCollectionList() {
	String lang="en";
	String uid = "";
	// first, get the message router info
	Element coll_list_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Element coll_list_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", lang, ""); // uid
	coll_list_message.appendChild(coll_list_request);
	logger.debug("coll list request = "+this.converter.getPrettyString(coll_list_request));
	Element coll_list_response = (Element)this.router.process(coll_list_message);
	if (coll_list_response==null) {
	    logger.error("couldn't query the message router!");
	    return false;
	}
	logger.debug("coll list response = "+this.converter.getPrettyString(coll_list_response));
	// second, get some info from each collection. we want the coll name 
	// and whether its got a text query service 
	
	NodeList colls = coll_list_response.getElementsByTagName(GSXML.COLLECTION_ELEM);
	// we will send all the requests in a single message
	Element metadata_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	for (int i=0; i<colls.getLength(); i++) {
	    Element c = (Element)colls.item(i);
	    String name = c.getAttribute(GSXML.NAME_ATT);	    
	    Element metadata_request = GSXML.createBasicRequest(this.doc,  GSXML.REQUEST_TYPE_DESCRIBE, name, lang, uid);
	    metadata_message.appendChild(metadata_request);
	}
	logger.debug("metadata request = "+this.converter.getPrettyString(metadata_message));
	Element metadata_response = (Element)this.router.process(metadata_message);
	logger.debug("metadata response = "+this.converter.getPrettyString(metadata_response));
	NodeList coll_responses = metadata_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
	ArrayList valid_colls = new ArrayList();
	ArrayList valid_coll_names = new ArrayList();
	for (int i=0; i<coll_responses.getLength(); i++) {
	    Element response = (Element)coll_responses.item(i);
	    Element coll = (Element)GSXML.getChildByTagName(response, GSXML.COLLECTION_ELEM);
	    Element service_list = (Element)GSXML.getChildByTagName(coll, GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER);
	    if (service_list==null) continue;
	    Element query_service = GSXML.getNamedElement(service_list, GSXML.SERVICE_ELEM, GSXML.NAME_ATT, TEXT_QUERY_SERVICE); // should be AbstractTextSearch.TEXT_QUERY_SERVICE
	    if (query_service == null) continue;
	    // use the name of the response in case we are talking to a remote collection, not the name of the collection.
	    String coll_id = response.getAttribute(GSXML.FROM_ATT);
	    String coll_name = coll_id+": "+GSXML.getDisplayText(coll, GSXML.DISPLAY_TEXT_NAME, "en", "en"); // just use english for now until we do some caching or something
	    valid_colls.add(coll_id);
	    valid_coll_names.add(coll_name);
	}

	if (valid_colls.size()==0) {
	    return false;
	}
	// ids no all has the list without 'all' option.
	this.coll_ids_list_no_all = new String[1];
	this.coll_ids_list_no_all = (String []) valid_colls.toArray(coll_ids_list_no_all);
	
	valid_colls.add(0, "all");
	valid_coll_names.add(0, getTextString("param."+COLLECTION_PARAM+".all", "en" ));
	this.coll_ids_list = new String[1];
	this.coll_names_list = new String[1];
	this.coll_ids_list = (String []) valid_colls.toArray(coll_ids_list);
	this.coll_names_list = (String []) valid_coll_names.toArray(coll_names_list);
	return true;
    }

    protected Element processDocumentMetadataRetrieve(Element request) {
	// Create a new (empty) result message
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	String lang = request.getAttribute(GSXML.LANG_ATT);
	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("DocumentMetadataRetrieve request had no paramList.");
	    return result;  // Return the empty result
	}

	NodeList query_doc_list = request.getElementsByTagName(GSXML.DOC_NODE_ELEM);
	if (query_doc_list.getLength()==0) {
	   logger.error("DocumentMetadataRetrieve request had no documentNodes.");
	    return result;  // Return the empty result
	} 
	
	// the resulting doc node list
	Element result_node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(result_node_list);

	// get all the metadata params
	Element new_param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	Element param = GSXML.createParameter(this.doc, "metadata", "Title");
	new_param_list.appendChild(param);
	
	// organise the nodes into collection lists
	HashMap coll_map = new HashMap();
	
	for (int i=0; i<query_doc_list.getLength(); i++) {
	    Element doc_node = (Element)query_doc_list.item(i);
	    String coll_name = doc_node.getAttribute("collection");
	    Element coll_items = (Element)coll_map.get(coll_name);
	    if (coll_items==null) {
		coll_items=this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
		coll_map.put(coll_name, coll_items);
	    }
	    coll_items.appendChild(this.doc.importNode(doc_node, true));
	}

	// create teh individual requests
	Element meta_request_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Set mapping_set = coll_map.entrySet();
	Iterator iter = mapping_set.iterator();

	while (iter.hasNext()) {
	    Map.Entry e = (Map.Entry)iter.next();
	    String cname = (String)e.getKey();
	    Element doc_nodes = (Element)e.getValue();
	    Element meta_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, GSPath.appendLink(cname, DOCUMENT_METADATA_RETRIEVE_SERVICE), lang, "");
	    meta_request.appendChild(doc_nodes);
	    meta_request.appendChild(new_param_list.cloneNode(true));
	    meta_request_message.appendChild(meta_request);
	    
	}

	Node meta_result_node = this.router.process(meta_request_message);
	Element meta_result = this.converter.nodeToElement(meta_result_node);
	
	// now need to put the doc nodes back in the right order
	// go through the original list again. keep an element pointer to
	// the next element in each collections list
	NodeList meta_responses = meta_result.getElementsByTagName(GSXML.RESPONSE_ELEM);
	for (int i=0; i<meta_responses.getLength(); i++) {
	    String collname = GSPath.removeLastLink(((Element)meta_responses.item(i)).getAttribute(GSXML.FROM_ATT));
	    Element first_elem = (Element)GSXML.getNodeByPath(meta_responses.item(i), "documentNodeList/documentNode");
	    coll_map.put(collname, first_elem);
	}

	for (int i=0; i<query_doc_list.getLength(); i++) {
	    Element doc_node = (Element)query_doc_list.item(i);
	    Element new_node = (Element)this.doc.importNode(doc_node, false);
	    result_node_list.appendChild(new_node);
	    String coll_name = doc_node.getAttribute("collection");
	
	    Element meta_elem = (Element)coll_map.get(coll_name);
	    GSXML.mergeMetadataLists(new_node, meta_elem);
	    coll_map.put(coll_name, meta_elem.getNextSibling());
	}
	return result;
    }
}
 
