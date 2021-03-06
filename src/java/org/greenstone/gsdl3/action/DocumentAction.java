/*
 *    DocumentAction.java
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
package org.greenstone.gsdl3.action;

// Greenstone classes
import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
import org.greenstone.util.GlobalProperties;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;

// General Java classes
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.*;

/** Action class for retrieving Documents via the message router */
public class DocumentAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.DocumentAction.class.getName());

	// this is used to specify that the sibling nodes of a selected one should be obtained
	public static final String SIBLING_ARG = "sib";
	public static final String GOTO_PAGE_ARG = "gp";
	public static final String ENRICH_DOC_ARG = "end";
	public static final String EXPAND_DOCUMENT_ARG = "ed";
	public static final String EXPAND_CONTENTS_ARG = "ec";
	public static final String REALISTIC_BOOK_ARG = "book";
        public static final String NO_TEXT_ARG = "noText";
        public static final String DOC_EDIT_ARG = "docEdit";
  
	/**
	 * if this is set to true, when a document is displayed, any annotation type
	 * services (enrich) will be offered to the user as well
	 */
	protected boolean provide_annotations = false;

	protected boolean highlight_query_terms = false;

	public boolean configure()
	{
		super.configure();
		String highlight = (String) config_params.get("highlightQueryTerms");
		if (highlight != null && highlight.equals("true"))
		{
			highlight_query_terms = true;
		}
		String annotate = (String) config_params.get("displayAnnotationService");
		if (annotate != null && annotate.equals("true"))
		{
			provide_annotations = true;
		}
		return true;
	}

	public Node process(Node message_node)
	{
		// for now, no subaction eventually we may want to have subactions such as text assoc or something ?

		Element message = GSXML.nodeToElement(message_node);
		Document doc = XMLConverter.newDOM(); //message.getOwnerDocument();
		
		// the response
		Element result = doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// get the request - assume only one
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_paramList, false);

		// just in case there are some that need to get passed to the services
		HashMap service_params = (HashMap) params.get("s0");

		String collection = (String) params.get(GSParams.COLLECTION);
		String document_id = (String) params.get(GSParams.DOCUMENT);
		if (document_id != null && document_id.equals(""))
		{
			document_id = null;
		}
		String href = (String) params.get(GSParams.HREF);//for an external link : get the href URL if it is existing in the params list 
		if (href != null && href.equals(""))
		{
			href = null;
		}
		String rl = (String) params.get(GSParams.RELATIVE_LINK);//for an external link : get the rl value if it is existing in the params list
		if (document_id == null && href == null)
		{
			logger.error("no document specified!");
			return result;
		}
		if (rl != null && rl.equals("0"))
		{
			// this is a true external link, we should have been directed to a different page or action
			logger.error("rl value was 0, shouldn't get here");
			return result;
		}

		UserContext userContext = new UserContext(request);

		//append site metadata
		addSiteMetadata(page_response, userContext);
		addInterfaceOptions(page_response);

		// get the additional data needed for the page
		getBackgroundData(page_response, collection, userContext);
		Element format_elem = (Element) GSXML.getChildByTagName(page_response, GSXML.FORMAT_ELEM);

		if (format_elem != null) {
		  // lets look for param defaults set in config file
		  NodeList param_defaults = format_elem.getElementsByTagName("paramDefault");
		  for (int i=0; i<param_defaults.getLength(); i++) {
		    Element p = (Element)param_defaults.item(i);
		    String name = p.getAttribute(GSXML.NAME_ATT);
		    if (params.get(name) ==null) {
		      // wasn't set from interface
		      String value = p.getAttribute(GSXML.VALUE_ATT);
		      params.put(name, value );
		      // also add into request param xml so that xslt knows it too
		      GSXML.addParameterToList(cgi_paramList, name, value);
		    }
		  }
		}

		String document_type = (String) params.get(GSParams.DOCUMENT_TYPE);
		if (document_type != null && document_type.equals(""))
		{
			//document_type = "hierarchy";
			document_type = null; // we'll get it later if not already specified
		}
		// what if it is null here?? Anu to check...


		boolean editing_document = false;
		String doc_edit = (String) params.get(DOC_EDIT_ARG);
		if (doc_edit != null && doc_edit.equals("1")) {
		  editing_document = true;
		}

		// are we editing mode? just get the archive document, convert to our internal doc format, and return it
		if (editing_document) {

		  // call get archive doc
		  Element dx_message = doc.createElement(GSXML.MESSAGE_ELEM);
		  String to = "DocXMLGetSection";
		  Element dx_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		  dx_message.appendChild(dx_request);
		  Element dx_section = doc.createElement(GSXML.DOCXML_SECTION_ELEM);
		  dx_section.setAttribute(GSXML.NODE_ID_ATT, document_id);
		  dx_section.setAttribute(GSXML.COLLECTION_ATT, collection);
		  dx_request.appendChild(dx_section);

		  Element dx_response_message = (Element) this.mr.process(dx_message);
		  if (processErrorElements(dx_response_message, page_response))
		    {
		      return result;
		    }

		  // get the section out
		  String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOCXML_SECTION_ELEM);
		  Element section = (Element) GSXML.getNodeByPath(dx_response_message, path);
		  if (section == null) {
		    logger.error("no archive doc returned for "+document_id);
		    return result;
		  }
		  // convert the archive format into the internal format that the page response requires

		  // work out doctype
		  // NOTE: this will be coming from collection database in index
		  // the archive file doesn't store this. So we have to assume
		  // that the doc type will not be changing with any
		  // modifications happening to archives.
		  
		  // if doc type is null, then we need to work it out.
		  // create a basic doc list containing the current node

		  if (document_type == null) {
		    Element basic_doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		    Element current_doc = doc.createElement(GSXML.DOC_NODE_ELEM);
		    basic_doc_list.appendChild(current_doc);
		    current_doc.setAttribute(GSXML.NODE_ID_ATT, document_id);
		    basic_doc_list.appendChild(current_doc);
		    document_type = getDocumentType(basic_doc_list, collection, userContext, page_response);
		  }
		  
		  if (document_type == null) {
		      logger.debug("@@@ doctype is null, setting to simple");
		      document_type = GSXML.DOC_TYPE_SIMPLE;
		  }		  

		  Element doc_elem = doc.createElement(GSXML.DOCUMENT_ELEM);	 
		  doc_elem.setAttribute(GSXML.DOC_TYPE_ATT, document_type);
		  page_response.appendChild(doc_elem);

		  Element transformed_section = transformArchiveToDocument(section);
		  if (document_type ==  GSXML.DOC_TYPE_SIMPLE) {
		    // simple doc, only returning a single document node, which is the top level section.
		    doc_elem.setAttribute(GSXML.NODE_ID_ATT, document_id);
		    GSXML.mergeElements(doc_elem, transformed_section);
		    return result;
		  }

		  // multi sectioned document.
		  transformed_section.setAttribute(GSXML.NODE_ID_ATT, document_id);		  
		  // In docEdit mode, we obtain the text from archives, from doc.xml
		  // Now the transformation has replaced <Section> with <documentNode>
		  // Need to add nodeID, nodeType and docType attributes to each docNode
		  // as doc.xml doesn't store that.
		  insertDocNodeAttributes(transformed_section, document_type, null);		  
		  doc_elem.appendChild(doc.importNode(transformed_section, true));
		  logger.debug("dx result = "+XMLConverter.getPrettyString(result));

		  return result;
		}
		
		//whether to retrieve siblings or not
		boolean get_siblings = false;
		String sibs = (String) params.get(SIBLING_ARG);
		if (sibs != null && sibs.equals("1"))
		{
			get_siblings = true;
		}

		String doc_id_modifier = "";
		String sibling_num = (String) params.get(GOTO_PAGE_ARG);
		if (sibling_num != null && !sibling_num.equals(""))
		{
			// we have to modify the doc name
			doc_id_modifier = "." + sibling_num + ".ss";
		}

		boolean expand_document = false;
		String ed_arg = (String) params.get(EXPAND_DOCUMENT_ARG);
		if (ed_arg != null && ed_arg.equals("1"))
		{
			expand_document = true;
		}

		boolean expand_contents = false;
		if (expand_document)
		{ // we always expand the contents with the text
			expand_contents = true;
		}
		else
		{
			String ec_arg = (String) params.get(EXPAND_CONTENTS_ARG);
			if (ec_arg != null && ec_arg.equals("1"))
			{
				expand_contents = true;
			}
		}

		// do we want text content? Not if no_text=1.
		// expand_document overrides this. - should it??
		boolean get_text = true;
		String nt_arg = (String) params.get(NO_TEXT_ARG);
		
		if (!expand_document && nt_arg!=null && nt_arg.equals("1")) {
		  logger.debug("SETTING GET TEXT TO FALSE");
		  get_text = false;
		} else {
		  logger.debug("GET TEXT REMAINS TRUE");
		}

		// the_document is where all the doc info - structure and metadata etc
		// is added into, to be returned in the page
		Element the_document = doc.createElement(GSXML.DOCUMENT_ELEM);
		page_response.appendChild(the_document);

		// create a basic doc list containing the current node
		Element basic_doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		Element current_doc = doc.createElement(GSXML.DOC_NODE_ELEM);
		basic_doc_list.appendChild(current_doc);
		if (document_id != null)
		{
			current_doc.setAttribute(GSXML.NODE_ID_ATT, document_id + doc_id_modifier);
		}
		else
		{
			current_doc.setAttribute(GSXML.HREF_ID_ATT, href);
			// do we need this??
			current_doc.setAttribute(GSXML.ID_MOD_ATT, doc_id_modifier);
		}
		
		if (document_type == null)
		{
			document_type = getDocumentType(basic_doc_list, collection, userContext, page_response);
		}
		if (document_type == null)
		{
		    logger.debug("##### doctype is null, setting to simple");
		    document_type = GSXML.DOC_TYPE_SIMPLE;
		}
		
		the_document.setAttribute(GSXML.DOC_TYPE_ATT, document_type);
		
		// Create a parameter list to specify the required structure information
		Element ds_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (service_params != null)
		{
			GSXML.addParametersToList(ds_param_list, service_params);
		}

		Element ds_param = null;
		boolean get_structure = false;
		boolean get_structure_info = false;
		if (document_type.equals(GSXML.DOC_TYPE_PAGED))
		{
			get_structure_info = true;

			if (expand_contents)
			{
				ds_param = doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "entire");
			}

			// get the info needed for paged naviagtion
			ds_param = doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "numSiblings");
			ds_param = doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "numChildren");
			ds_param = doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "siblingPosition");

			if (get_siblings)
			{
				ds_param = doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "siblings");
			}

		}
		else if (document_type.equals(GSXML.DOC_TYPE_HIERARCHY) || document_type.equals(GSXML.DOC_TYPE_PAGED_HIERARCHY))
		{
			get_structure = true;
			if (expand_contents)
			{
				ds_param = doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "entire");
			}
			else
			{
				// get the info needed for table of contents
				ds_param = doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "ancestors");
				ds_param = doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "children");
				if (get_siblings)
				{
					ds_param = doc.createElement(GSXML.PARAM_ELEM);
					ds_param_list.appendChild(ds_param);
					ds_param.setAttribute(GSXML.NAME_ATT, "structure");
					ds_param.setAttribute(GSXML.VALUE_ATT, "siblings");
				}
			}
		}
		else
		{
		  // we dont need any structure
		}

		boolean has_dummy = false;
		if (get_structure || get_structure_info)
		{

			// Build a request to obtain the document structure
			Element ds_message = doc.createElement(GSXML.MESSAGE_ELEM);
			String to = GSPath.appendLink(collection, "DocumentStructureRetrieve");// Hard-wired?
			Element ds_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
			ds_message.appendChild(ds_request);
			ds_request.appendChild(ds_param_list);

			// add the node list we created earlier
			ds_request.appendChild(basic_doc_list);

			// Process the document structure retrieve message
			Element ds_response_message = (Element) this.mr.process(ds_message);
			if (processErrorElements(ds_response_message, page_response))
			{
				return result;
			}

			// get the info and print out
			String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
			path = GSPath.appendLink(path, "nodeStructureInfo");
			Element ds_response_struct_info = (Element) GSXML.getNodeByPath(ds_response_message, path);
			// get the doc_node bit 
			if (ds_response_struct_info != null)
			{
				the_document.appendChild(doc.importNode(ds_response_struct_info, true));
			}
			path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
			path = GSPath.appendLink(path, GSXML.NODE_STRUCTURE_ELEM);
			Element ds_response_structure = (Element) GSXML.getNodeByPath(ds_response_message, path);

			if (ds_response_structure != null)
			{
				// add the contents of the structure bit into the_document
				NodeList structs = ds_response_structure.getChildNodes();
				for (int i = 0; i < structs.getLength(); i++)
				{
					the_document.appendChild(doc.importNode(structs.item(i), true));
				}
			}
			else
			{
				// no structure nodes, so put in a dummy doc node
				Element doc_node = doc.createElement(GSXML.DOC_NODE_ELEM);
				if (document_id != null)
				{
					doc_node.setAttribute(GSXML.NODE_ID_ATT, document_id);
				}
				else
				{
					doc_node.setAttribute(GSXML.HREF_ID_ATT, href);

				}
				the_document.appendChild(doc_node);
				has_dummy = true;
			}
		}
		else
		{ // a simple type - we dont have a dummy node for simple
			// should think about this more
			// no structure request, so just put in a dummy doc node
			Element doc_node = doc.createElement(GSXML.DOC_NODE_ELEM);
			if (document_id != null)
			{
				doc_node.setAttribute(GSXML.NODE_ID_ATT, document_id);
			}
			else
			{
				doc_node.setAttribute(GSXML.HREF_ID_ATT, href);
			}
			the_document.appendChild(doc_node);
			has_dummy = true;
		}

		// Build a request to obtain some document metadata
		Element dm_message = doc.createElement(GSXML.MESSAGE_ELEM);
		String to = GSPath.appendLink(collection, "DocumentMetadataRetrieve"); // Hard-wired?
		Element dm_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		dm_message.appendChild(dm_request);
		// Create a parameter list to specify the required metadata information

		HashSet<String> meta_names = new HashSet<String>();
		meta_names.add("Title"); // the default
		if (format_elem != null)
		{
			getRequiredMetadataNames(format_elem, meta_names);
		}

		Element extraMetaListElem = (Element) GSXML.getChildByTagName(request, GSXML.EXTRA_METADATA + GSXML.LIST_MODIFIER);
		if (extraMetaListElem != null)
		{
			NodeList extraMetaList = extraMetaListElem.getElementsByTagName(GSXML.EXTRA_METADATA);
			for (int i = 0; i < extraMetaList.getLength(); i++)
			{
				meta_names.add(((Element) extraMetaList.item(i)).getAttribute(GSXML.NAME_ATT));
			}
		}

		Element dm_param_list = createMetadataParamList(doc,meta_names);
		if (service_params != null)
		{
			GSXML.addParametersToList(dm_param_list, service_params);
		}

		dm_request.appendChild(dm_param_list);

		// create the doc node list for the metadata request
		Element dm_doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		dm_request.appendChild(dm_doc_list);

		// Add each node from the structure response into the metadata request
		NodeList doc_nodes = the_document.getElementsByTagName(GSXML.DOC_NODE_ELEM);
		for (int i = 0; i < doc_nodes.getLength(); i++)
		{
			Element doc_node = (Element) doc_nodes.item(i);
			String doc_node_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);

			// Add the documentNode to the list
			Element dm_doc_node = doc.createElement(GSXML.DOC_NODE_ELEM);
			if (needSectionContent(params)) {
				if (doc_node_id.equals(document_id)) {
					dm_doc_list.appendChild(dm_doc_node);
				} 
			} else {
				dm_doc_list.appendChild(dm_doc_node);
			}
			//dm_doc_list.appendChild(dm_doc_node);
			dm_doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_node_id);
			dm_doc_node.setAttribute(GSXML.NODE_TYPE_ATT, doc_node.getAttribute(GSXML.NODE_TYPE_ATT));
			if (document_id == null){
				dm_doc_node.setAttribute(GSXML.HREF_ID_ATT, href );
				}

		}
		// we also want a metadata request to the top level document to get
		// assocfilepath - this could be cached too
		Element doc_meta_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		dm_message.appendChild(doc_meta_request);
		Element doc_meta_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (service_params != null)
		{
			GSXML.addParametersToList(doc_meta_param_list, service_params);
		}

		doc_meta_request.appendChild(doc_meta_param_list);
		Element doc_param = doc.createElement(GSXML.PARAM_ELEM);
		doc_meta_param_list.appendChild(doc_param);
		doc_param.setAttribute(GSXML.NAME_ATT, "metadata");
		doc_param.setAttribute(GSXML.VALUE_ATT, "assocfilepath");

		// create the doc node list for the metadata request
		Element doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		doc_meta_request.appendChild(doc_list);

		Element doc_node = doc.createElement(GSXML.DOC_NODE_ELEM);
		// the node we want is the root document node
		if (document_id != null)
		{
			doc_node.setAttribute(GSXML.NODE_ID_ATT, document_id + ".rt");
		}
		/*else
		{
			doc_node.setAttribute(GSXML.HREF_ID_ATT, href);// + ".rt");
			// can we assume that href is always a top level doc??
			//doc_node.setAttribute(GSXML.ID_MOD_ATT, ".rt");
			//doc_node.setAttribute("externalURL", has_rl);
		}*/
		doc_list.appendChild(doc_node);

		Element dm_response_message = (Element) this.mr.process(dm_message);
		if (processErrorElements(dm_response_message, page_response))
		{
			return result;
		}

		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		Element dm_response_doc_list = (Element) GSXML.getNodeByPath(dm_response_message, path);

		// Merge the metadata with the structure information
		NodeList dm_response_docs = dm_response_doc_list.getChildNodes();
		for (int i = 0; i < doc_nodes.getLength(); i++)
		{
			String node_idd = ((Element)doc_nodes.item(i)).getAttribute(GSXML.NODE_ID_ATT);
			Node dcNode = GSXML.getNamedElement(dm_response_doc_list, "documentNode", GSXML.NODE_ID_ATT, node_idd);
			GSXML.mergeMetadataLists(doc_nodes.item(i), dcNode);
		}
		// get the top level doc metadata out
		Element doc_meta_response = (Element) dm_response_message.getElementsByTagName(GSXML.RESPONSE_ELEM).item(1);
		Element top_doc_node = (Element) GSXML.getNodeByPath(doc_meta_response, "documentNodeList/documentNode");
		GSXML.mergeMetadataLists(the_document, top_doc_node);

		// do we want doc text content? If not, we are done.
		if (!get_text) {
		  // don't get text
		  return result;
		}

		// Build a request to obtain some document content
		Element dc_message = doc.createElement(GSXML.MESSAGE_ELEM);
		to = GSPath.appendLink(collection, "DocumentContentRetrieve"); // Hard-wired?
		Element dc_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		dc_message.appendChild(dc_request);

		// Create a parameter list to specify the request parameters - empty for now
		Element dc_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (service_params != null)
		{
			GSXML.addParametersToList(dc_param_list, service_params);
		}

		dc_request.appendChild(dc_param_list);

		// get the content
		// the doc list for the content request is the same as the one for the structure request unless we want the whole document, in which case its the same as for the metadata request.
		if (expand_document)
		{
			dc_request.appendChild(dm_doc_list);
		}
		else
		{
			dc_request.appendChild(basic_doc_list);
		}
		Element dc_response_message = (Element) this.mr.process(dc_message);

		if (processErrorElements(dc_response_message, page_response))
		{
			return result;
			
		}
		Element dc_response_doc_list = (Element) GSXML.getNodeByPath(dc_response_message, path);

		if (expand_document)
		{
			// Merge the content with the structure information
			NodeList dc_response_docs = dc_response_doc_list.getChildNodes();
			for (int i = 0; i < doc_nodes.getLength(); i++)
			{
				String node_id = ((Element)doc_nodes.item(i)).getAttribute(GSXML.NODE_ID_ATT);
				//Node content = GSXML.getChildByTagName((Element) dc_response_docs.item(i), GSXML.NODE_CONTENT_ELEM);
				Node docNode = GSXML.getNamedElement(dc_response_doc_list, "documentNode", GSXML.NODE_ID_ATT, node_id);
				Node content = GSXML.getChildByTagName(docNode, GSXML.NODE_CONTENT_ELEM);
				if (content != null)
				{
					if (highlight_query_terms)
					{
					  
					  content = highlightQueryTerms(request, node_id, (Element) content);
					}
					
					doc_nodes.item(i).appendChild(doc.importNode(content, true));
				}
				//GSXML.mergeMetadataLists(doc_nodes.item(i), dm_response_docs.item(i));
			}
			if (has_dummy && document_type.equals(GSXML.DOC_TYPE_SIMPLE)) {
			  Element dummy_node = (Element) doc_nodes.item(0);
			  the_document.removeChild(dummy_node);
			  the_document.setAttribute(GSXML.NODE_ID_ATT, dummy_node.getAttribute(GSXML.NODE_ID_ATT));
			  NodeList dummy_children = dummy_node.getChildNodes();
			  for (int i = dummy_children.getLength() - 1; i >= 0; i--)
			    {
			      // special case as we don't want more than one metadata list
			      if (dummy_children.item(i).getNodeName().equals(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER))
				{
				  GSXML.mergeMetadataFromList(the_document, dummy_children.item(i));
				}
			      else
				{
				  the_document.appendChild(dummy_children.item(i));
				}
			    }
			}
		}
		else
		{
			//path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
			Element dc_response_doc = (Element) GSXML.getChildByTagName(dc_response_doc_list, GSXML.DOC_NODE_ELEM);
			Element dc_response_doc_content = (Element) GSXML.getChildByTagName(dc_response_doc, GSXML.NODE_CONTENT_ELEM);
			//Element dc_response_doc_external = (Element) GSXML.getChildByTagName(dc_response_doc, "external");

			if (dc_response_doc_content == null)
			{
				// no content to add
				if (dc_response_doc.getAttribute("external").equals("true"))
				{

					//if (dc_response_doc_external != null)
					//{
					String href_id = dc_response_doc.getAttribute(GSXML.HREF_ID_ATT);

					the_document.setAttribute("selectedNode", href_id);
					the_document.setAttribute("external", href_id);
				}
				return result;
			}
			if (highlight_query_terms)
			{
				dc_response_doc.removeChild(dc_response_doc_content);

				dc_response_doc_content = highlightQueryTerms(request, null, dc_response_doc_content);
				dc_response_doc.appendChild(dc_response_doc.getOwnerDocument().importNode(dc_response_doc_content, true));
			}

			if (provide_annotations)
			{
				String service_selected = (String) params.get(ENRICH_DOC_ARG);
				if (service_selected != null && service_selected.equals("1"))
				{
					// now we can modifiy the response doc if needed
					String enrich_service = (String) params.get(GSParams.SERVICE);
					// send a message to the service
					Element enrich_message = doc.createElement(GSXML.MESSAGE_ELEM);
					Element enrich_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, enrich_service, userContext);
					enrich_message.appendChild(enrich_request);
					// check for parameters
					HashMap e_service_params = (HashMap) params.get("s1");
					if (e_service_params != null)
					{
						Element enrich_pl = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
						GSXML.addParametersToList(enrich_pl, e_service_params);
						enrich_request.appendChild(enrich_pl);
					}
					Element e_doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
					enrich_request.appendChild(e_doc_list);
					e_doc_list.appendChild(doc.importNode(dc_response_doc, true));

					Node enrich_response = this.mr.process(enrich_message);

					String[] links = { GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.DOC_NODE_ELEM, GSXML.NODE_CONTENT_ELEM };
					path = GSPath.createPath(links);
					dc_response_doc_content = (Element) GSXML.getNodeByPath(enrich_response, path);

				}
			} // if provide_annotations

			// use the returned id rather than the sent one cos there may have
			// been modifiers such as .pr that are removed.
			String modified_doc_id = dc_response_doc.getAttribute(GSXML.NODE_ID_ATT);
			the_document.setAttribute("selectedNode", modified_doc_id);
			if (has_dummy)
			{
				// change the id if necessary and add the content
				Element dummy_node = (Element) doc_nodes.item(0);

				dummy_node.setAttribute(GSXML.NODE_ID_ATT, modified_doc_id);
				dummy_node.appendChild(doc.importNode(dc_response_doc_content, true));
				// hack for simple type
				if (document_type.equals(GSXML.DOC_TYPE_SIMPLE))
				{
					// we dont want the internal docNode, just want the content and metadata in the document
					// rethink this!!
					the_document.removeChild(dummy_node);

					NodeList dummy_children = dummy_node.getChildNodes();
					//for (int i=0; i<dummy_children.getLength(); i++) {
					for (int i = dummy_children.getLength() - 1; i >= 0; i--)
					{
						// special case as we don't want more than one metadata list
						if (dummy_children.item(i).getNodeName().equals(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER))
						{
							GSXML.mergeMetadataFromList(the_document, dummy_children.item(i));
						}
						else
						{
							the_document.appendChild(dummy_children.item(i));
						}
					}
				}

				the_document.setAttribute(GSXML.NODE_ID_ATT, modified_doc_id);
			}
			else
			{
				// Merge the document content with the metadata and structure information
				for (int i = 0; i < doc_nodes.getLength(); i++)
				{
					Node dn = doc_nodes.item(i);
					String dn_id = ((Element) dn).getAttribute(GSXML.NODE_ID_ATT);
					if (dn_id.equals(modified_doc_id))
					{
						dn.appendChild(doc.importNode(dc_response_doc_content, true));
						break;
					}
				}
			}
		}
		//logger.debug("(DocumentAction) Page:\n" + GSXML.xmlNodeToString(result));
		return result;
	}

	/**
	 * tell the param class what its arguments are if an action has its own
	 * arguments, this should add them to the params object - particularly
	 * important for args that should not be saved
	 */
	public boolean addActionParameters(GSParams params)
	{
		params.addParameter(GOTO_PAGE_ARG, false);
		params.addParameter(ENRICH_DOC_ARG, false);
		params.addParameter(EXPAND_DOCUMENT_ARG, false);
		params.addParameter(EXPAND_CONTENTS_ARG, false);
		params.addParameter(REALISTIC_BOOK_ARG, false);

		return true;
	}
	
	private boolean needSectionContent(HashMap<String, Serializable> params) {
		String document_id = (String) params.get(GSParams.DOCUMENT);
		String ilt = (String) params.get(GSParams.INLINE_TEMPLATE);
		String iltPrefix = "<xsl:template match=\"/\"><text><xsl:for-each select=\"/page/pageResponse/document//documentNode[@nodeID =";
		if (ilt != null && ilt.startsWith(iltPrefix) && document_id != null) {
			return true;
		}
	
		return false;
	}
	/**
	 * this method gets the collection description, the format info, the list of
	 * enrich services, etc - stuff that is needed for the page, but is the same
	 * whatever the query is - should be cached
	 */
	protected boolean getBackgroundData(Element page_response, String collection, UserContext userContext)
	{
		Document doc = page_response.getOwnerDocument();
		
		// create a message to process - contains requests for the collection 
		// description, the format element, the enrich services on offer
		// these could all be cached
		Element info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		String path = GSPath.appendLink(collection, "DocumentContentRetrieve");
		// the format request - ignore for now, where does this request go to??
		Element format_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_FORMAT, path, userContext);
		info_message.appendChild(format_request);

		// the enrich_services request - only do this if provide_annotations is true

		if (provide_annotations)
		{
			Element enrich_services_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
			enrich_services_request.setAttribute(GSXML.INFO_ATT, "serviceList");
			info_message.appendChild(enrich_services_request);
		}

		Element info_response = (Element) this.mr.process(info_message);

		// the collection is the first response
		NodeList responses = info_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		Element format_resp = (Element) responses.item(0);

		Element format_elem = (Element) GSXML.getChildByTagName(format_resp, GSXML.FORMAT_ELEM);
		if (format_elem != null)
		{
			Element global_format_elem = (Element) GSXML.getChildByTagName(format_resp, GSXML.GLOBAL_FORMAT_ELEM);
			if (global_format_elem != null)
			{
				GSXSLT.mergeFormatElements(format_elem, global_format_elem, false);
			}

			// set the format type
			format_elem.setAttribute(GSXML.TYPE_ATT, "display");
			page_response.appendChild(doc.importNode(format_elem, true));
		}

		if (provide_annotations)
		{
			Element services_resp = (Element) responses.item(1);

			// a new message for the mr
			Element enrich_message = doc.createElement(GSXML.MESSAGE_ELEM);
			NodeList e_services = services_resp.getElementsByTagName(GSXML.SERVICE_ELEM);
			boolean service_found = false;
			for (int j = 0; j < e_services.getLength(); j++)
			{
				if (((Element) e_services.item(j)).getAttribute(GSXML.TYPE_ATT).equals("enrich"))
				{
					Element s = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, ((Element) e_services.item(j)).getAttribute(GSXML.NAME_ATT), userContext);
					enrich_message.appendChild(s);
					service_found = true;
				}
			}
			if (service_found)
			{
				Element enrich_response = (Element) this.mr.process(enrich_message);

				NodeList e_responses = enrich_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
				Element service_list = doc.createElement(GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
				for (int i = 0; i < e_responses.getLength(); i++)
				{
					Element e_resp = (Element) e_responses.item(i);
					Element e_service = (Element) doc.importNode(GSXML.getChildByTagName(e_resp, GSXML.SERVICE_ELEM), true);
					e_service.setAttribute(GSXML.NAME_ATT, e_resp.getAttribute(GSXML.FROM_ATT));
					service_list.appendChild(e_service);
				}
				page_response.appendChild(service_list);
			}
		} // if provide_annotations
		return true;

	}

	protected String getDocumentType(Element basic_doc_list, String collection, UserContext userContext, Element page_response)
	{
		Document doc = basic_doc_list.getOwnerDocument();
		
		Element ds_message = doc.createElement(GSXML.MESSAGE_ELEM);
		String to = GSPath.appendLink(collection, "DocumentStructureRetrieve");// Hard-wired?
		Element ds_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		ds_message.appendChild(ds_request);

		// Create a parameter list to specify the required structure information
		Element ds_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element ds_param = doc.createElement(GSXML.PARAM_ELEM);
		ds_param_list.appendChild(ds_param);
		ds_param.setAttribute(GSXML.NAME_ATT, "info");
		ds_param.setAttribute(GSXML.VALUE_ATT, "documentType");

		ds_request.appendChild(ds_param_list);

		// add the node list we created earlier
		ds_request.appendChild(basic_doc_list);

		// Process the document structure retrieve message
		Element ds_response_message = (Element) this.mr.process(ds_message);
		if (processErrorElements(ds_response_message, page_response))
		{
			return null;
		}

		String[] links = { GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.DOC_NODE_ELEM, "nodeStructureInfo" };
		String path = GSPath.createPath(links);
		Element info_elem = (Element) GSXML.getNodeByPath(ds_response_message, path);
		if (info_elem == null) {
		    return null;
		}
		Element doctype_elem = GSXML.getNamedElement(info_elem, "info", "name", "documentType");
		if (doctype_elem != null)
		{
			String doc_type = doctype_elem.getAttribute("value");
			return doc_type;
		}
		return null;
	}

    // Recursive method to set the docType, nodeType and nodeID attributes of each docNode
    // The docType remains constant as in parameter document_type
    // The nodeID for the first (root) docNode is already set. For all children, the rootNode id
    // is updated to be <parent-id>.<num-child>, where the first parent-id is rootNode id. 
    // The nodeType is root if rootNode, internal if there are children and leaf if no children
    protected void insertDocNodeAttributes(Element docNode, String document_type, String id) {

	boolean isRoot =  false;
	if(id == null) { // rootNode, get the root nodeID to work with recursively
	    id = docNode.getAttribute(GSXML.NODE_ID_ATT);
	    isRoot = true;
	} else { // for all but the root node, need to still set the nodeID
	    docNode.setAttribute(GSXML.NODE_ID_ATT, id);
	}
	
	docNode.setAttribute(GSXML.DOC_TYPE_ATT, document_type);
	
	NodeList docNodes = GSXML.getChildrenByTagName(docNode, GSXML.DOC_NODE_ELEM);
	if(docNodes.getLength() > 0) {	    
	    docNode.setAttribute(GSXML.NODE_TYPE_ATT, GSXML.NODE_TYPE_INTERNAL);
	    for(int i = 0; i < docNodes.getLength(); i++) {
		Element childDocNode = (Element)docNodes.item(i);
		
		// work out the child docNode's nodeID based on current id
		String nodeID = id + "." + (i+1);		
		insertDocNodeAttributes(childDocNode, document_type, nodeID); //recursion step
	    }
	} else {
	    docNode.setAttribute(GSXML.NODE_TYPE_ATT, GSXML.NODE_TYPE_LEAF);
	}

	// rootNode's nodeType is a special case: it's "root", not "leaf" or "internal"
	if(isRoot) docNode.setAttribute(GSXML.NODE_TYPE_ATT, GSXML.NODE_TYPE_ROOT);
	
    }

  /** run the XSLT transform which converts from doc.xml format to our internal document format */
    protected Element transformArchiveToDocument(Element section) {
    
      String stylesheet_filename = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), "", (String) this.config_params.get(GSConstants.INTERFACE_NAME), (ArrayList<String>) this.config_params.get(GSConstants.BASE_INTERFACES), "archive2document.xsl");
      if (stylesheet_filename == null) {
	logger.error("Couldn't find stylesheet archive2document.xsl");
	return section;
      }
         
    Document stylesheet_doc = XMLConverter.getDOM(new File(stylesheet_filename));
    if (stylesheet_doc == null) {
      logger.error("Couldn't load in stylesheet "+stylesheet_filename);
      return section;
    }

    Document section_doc = XMLConverter.newDOM();
    section_doc.appendChild(section_doc.importNode(section, true));
    Node result = this.transformer.transform(stylesheet_doc, section_doc);
    logger.debug("transform result = "+XMLConverter.getPrettyString(result));

    Element new_element;
    if (result.getNodeType() == Node.DOCUMENT_NODE) {
	new_element = ((Document) result).getDocumentElement();
    } else {
	new_element = (Element) result;
    }

    
    return new_element;

  }


	/**
	 * this involves a bit of a hack to get the equivalent query terms - has to
	 * requery the query service - uses the last selected service name. (if it
	 * ends in query). should this action do the query or should it send a
	 * message to the query action? but that will involve lots of extra stuff.
	 * also doesn't handle phrases properly - just highlights all the terms
	 * found in the text.
	 */
  protected Element highlightQueryTerms(Element request, String current_node_id, Element dc_response_doc_content)
	{
		Document doc = request.getOwnerDocument();
		
		// do the query again to get term info 
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		HashMap previous_params = (HashMap) params.get("p");
		if (previous_params == null)
		{
			return dc_response_doc_content;
		}
		String service_name = (String) previous_params.get(GSParams.SERVICE);
		if (service_name == null || !service_name.endsWith("Query"))
		{ // hack for now - we only do highlighting if we were in a query last - ie not if we were in a browse thingy
			logger.debug("invalid service, not doing highlighting");
			return dc_response_doc_content;
		}
		String collection = (String) params.get(GSParams.COLLECTION);
		UserContext userContext = new UserContext(request);
		String to = GSPath.appendLink(collection, service_name);

		Element mr_query_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		mr_query_message.appendChild(mr_query_request);

		// paramList
		HashMap service_params = (HashMap) params.get("s1");

		Element query_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(query_param_list, service_params);
		if (current_node_id != null) {
		  GSXML.addParameterToList(query_param_list, "hldocOID", current_node_id);
		} else {
		  GSXML.addParameterToList(query_param_list, "hldocOID", (String) params.get(GSParams.DOCUMENT));
		}
		mr_query_request.appendChild(query_param_list);
		// do the query
		Element mr_query_response = (Element) this.mr.process(mr_query_message);
		String pathNode = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.NODE_CONTENT_ELEM);
		Element highlighted_Node = (Element) GSXML.getNodeByPath(mr_query_response, pathNode);
		// For SOLR, the above query may come back with a nodeContent element, which is the hldocOID section content, with search terms marked up. We send it back to the documnetContentRetrieve service so that resolveTextMacros can be applied, and it can be properly encased in documentNode etc elements
		if (highlighted_Node != null)
		{
			// Build a request to process highlighted text
			
			Element hl_message = doc.createElement(GSXML.MESSAGE_ELEM);
			to = GSPath.appendLink(collection, "DocumentContentRetrieve");
			Element dc_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
			hl_message.appendChild(dc_request);

			// Create a parameter list to specify the request parameters - empty for now
			Element dc_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			dc_request.appendChild(dc_param_list);

			// get the content
			Element doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			dc_request.appendChild(doc_list);
			Element current_doc = doc.createElement(GSXML.DOC_NODE_ELEM);
			doc_list.appendChild(current_doc);
			current_doc.setAttribute(GSXML.NODE_ID_ATT, (String) params.get(GSParams.DOCUMENT));
			//Append highlighted content to request for processing
			dc_request.appendChild(doc.importNode(highlighted_Node, true));
			Element hl_response_message = (Element) this.mr.process(hl_message);
		
			//Get results
			NodeList contentList = hl_response_message.getElementsByTagName(GSXML.NODE_CONTENT_ELEM);
			Element content = (Element) contentList.item(0);	
			return content;
		}
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.TERM_ELEM + GSXML.LIST_MODIFIER);
		Element query_term_list_element = (Element) GSXML.getNodeByPath(mr_query_response, path);
		if (query_term_list_element == null)
		{
			// no term info
			logger.error("No query term information.\n");
			return dc_response_doc_content;
		}

		String content = GSXML.getNodeText(dc_response_doc_content);

		String metadata_path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		Element metadata_list = (Element) GSXML.getNodeByPath(mr_query_response, metadata_path);

		HashSet<String> query_term_variants = new HashSet<String>();
		NodeList equivalent_terms_nodelist = query_term_list_element.getElementsByTagName("equivTermList");
		if (equivalent_terms_nodelist == null || equivalent_terms_nodelist.getLength() == 0)
		{
			NodeList terms_nodelist = query_term_list_element.getElementsByTagName("term");
			if (terms_nodelist != null && terms_nodelist.getLength() > 0)
			{
				for (int i = 0; i < terms_nodelist.getLength(); i++)
				{
					String termValue = ((Element) terms_nodelist.item(i)).getAttribute("name");
					String termValueU = null;
					String termValueL = null;

					if (termValue.length() > 1)
					{
						termValueU = termValue.substring(0, 1).toUpperCase() + termValue.substring(1);
						termValueL = termValue.substring(0, 1).toLowerCase() + termValue.substring(1);
					}
					else
					{
						termValueU = termValue.substring(0, 1).toUpperCase();
						termValueL = termValue.substring(0, 1).toLowerCase();
					}

					query_term_variants.add(termValueU);
					query_term_variants.add(termValueL);
				}
			}
		}
		else
		{
			for (int i = 0; i < equivalent_terms_nodelist.getLength(); i++)
			{
				Element equivalent_terms_element = (Element) equivalent_terms_nodelist.item(i);
				String[] equivalent_terms = GSXML.getAttributeValuesFromList(equivalent_terms_element, GSXML.NAME_ATT);
				for (int j = 0; j < equivalent_terms.length; j++)
				{
					query_term_variants.add(equivalent_terms[j]);
				}
			}
		}

		ArrayList<ArrayList<HashSet<String>>> phrase_query_term_variants_hierarchy = new ArrayList<ArrayList<HashSet<String>>>();

		Element query_element = GSXML.getNamedElement(metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "query");
		String performed_query = GSXML.getNodeText(query_element) + " ";

		ArrayList<HashSet<String>> phrase_query_p_term_variants_list = new ArrayList<HashSet<String>>();
		int term_start = 0;
		boolean in_term = false;
		boolean in_phrase = false;
		for (int i = 0; i < performed_query.length(); i++)
		{
			char character = performed_query.charAt(i);
			boolean is_character_letter_or_digit = Character.isLetterOrDigit(character);

			// Has a query term just started?
			if (in_term == false && is_character_letter_or_digit == true)
			{
				in_term = true;
				term_start = i;
			}

			// Or has a term just finished?
			else if (in_term == true && is_character_letter_or_digit == false)
			{
				in_term = false;
				String term = performed_query.substring(term_start, i);

				Element term_element = GSXML.getNamedElement(query_term_list_element, GSXML.TERM_ELEM, GSXML.NAME_ATT, term);
				if (term_element != null)
				{

					HashSet<String> phrase_query_p_term_x_variants = new HashSet<String>();

					NodeList term_equivalent_terms_nodelist = term_element.getElementsByTagName("equivTermList");
					if (term_equivalent_terms_nodelist == null || term_equivalent_terms_nodelist.getLength() == 0)
					{
						String termValueU = null;
						String termValueL = null;

						if (term.length() > 1)
						{
							termValueU = term.substring(0, 1).toUpperCase() + term.substring(1);
							termValueL = term.substring(0, 1).toLowerCase() + term.substring(1);
						}
						else
						{
							termValueU = term.substring(0, 1).toUpperCase();
							termValueL = term.substring(0, 1).toLowerCase();
						}

						phrase_query_p_term_x_variants.add(termValueU);
						phrase_query_p_term_x_variants.add(termValueL);
					}
					else
					{
						for (int j = 0; j < term_equivalent_terms_nodelist.getLength(); j++)
						{
							Element term_equivalent_terms_element = (Element) term_equivalent_terms_nodelist.item(j);
							String[] term_equivalent_terms = GSXML.getAttributeValuesFromList(term_equivalent_terms_element, GSXML.NAME_ATT);
							for (int k = 0; k < term_equivalent_terms.length; k++)
							{
								phrase_query_p_term_x_variants.add(term_equivalent_terms[k]);
							}
						}
					}
					phrase_query_p_term_variants_list.add(phrase_query_p_term_x_variants);

					if (in_phrase == false)
					{
						phrase_query_term_variants_hierarchy.add(phrase_query_p_term_variants_list);
						phrase_query_p_term_variants_list = new ArrayList<HashSet<String>>();
					}
				}
			}
			// Watch for phrases (surrounded by quotes)
			if (character == '\"')
			{
				// Has a phrase just started?
				if (in_phrase == false)
				{
					in_phrase = true;
				}
				// Or has a phrase just finished?
				else if (in_phrase == true)
				{
					in_phrase = false;
					phrase_query_term_variants_hierarchy.add(phrase_query_p_term_variants_list);
				}

				phrase_query_p_term_variants_list = new ArrayList<HashSet<String>>();
			}
		}

		return highlightQueryTermsInternal(doc, content, query_term_variants, phrase_query_term_variants_hierarchy);
	}

	/**
	 * Highlights query terms in a piece of text.
	 */
	private Element highlightQueryTermsInternal(Document doc, String content, HashSet<String> query_term_variants, ArrayList<ArrayList<HashSet<String>>> phrase_query_term_variants_hierarchy)
	{
		// Convert the content string to an array of characters for speed
		char[] content_characters = new char[content.length()];
		content.getChars(0, content.length(), content_characters, 0);

		// Now skim through the content, identifying word matches
		ArrayList<WordMatch> word_matches = new ArrayList<WordMatch>();
		int word_start = 0;
		boolean in_word = false;
		boolean preceding_word_matched = false;
		boolean inTag = false;
		for (int i = 0; i < content_characters.length; i++)
		{
			//We don't want to find words inside HTML tags
			if (content_characters[i] == '<')
			{
				inTag = true;
				continue;
			}
			else if (inTag && content_characters[i] == '>')
			{
				inTag = false;
			}
			else if (inTag)
			{
				continue;
			}

			boolean is_character_letter_or_digit = Character.isLetterOrDigit(content_characters[i]);

			// Has a word just started?
			if (in_word == false && is_character_letter_or_digit == true)
			{
				in_word = true;
				word_start = i;
			}

			// Or has a word just finished?
			else if (in_word == true && is_character_letter_or_digit == false)
			{
				in_word = false;

				// Check if the word matches any of the query term equivalents
				String word = new String(content_characters, word_start, (i - word_start));
				if (query_term_variants.contains(word))
				{
					// We have found a matching word, so remember its location
					word_matches.add(new WordMatch(word, word_start, i, preceding_word_matched));
					preceding_word_matched = true;
				}
				else
				{
					preceding_word_matched = false;
				}
			}
		}

		// Don't forget the last word...
		if (in_word == true)
		{
			// Check if the word matches any of the query term equivalents
			String word = new String(content_characters, word_start, (content_characters.length - word_start));
			if (query_term_variants.contains(word))
			{
				// We have found a matching word, so remember its location
				word_matches.add(new WordMatch(word, word_start, content_characters.length, preceding_word_matched));
			}
		}

		ArrayList<Integer> highlight_start_positions = new ArrayList<Integer>();
		ArrayList<Integer> highlight_end_positions = new ArrayList<Integer>();

		// Deal with phrases now
		ArrayList<PartialPhraseMatch> partial_phrase_matches = new ArrayList<PartialPhraseMatch>();
		for (int i = 0; i < word_matches.size(); i++)
		{
			WordMatch word_match = word_matches.get(i);

			// See if any partial phrase matches are extended by this word
			if (word_match.preceding_word_matched)
			{
				for (int j = partial_phrase_matches.size() - 1; j >= 0; j--)
				{
					PartialPhraseMatch partial_phrase_match = partial_phrase_matches.remove(j);
					ArrayList phrase_query_p_term_variants_list = phrase_query_term_variants_hierarchy.get(partial_phrase_match.query_phrase_number);
					HashSet phrase_query_p_term_x_variants = (HashSet) phrase_query_p_term_variants_list.get(partial_phrase_match.num_words_matched);
					if (phrase_query_p_term_x_variants.contains(word_match.word))
					{
						partial_phrase_match.num_words_matched++;

						// Has a complete phrase match occurred?
						if (partial_phrase_match.num_words_matched == phrase_query_p_term_variants_list.size())
						{
							// Check for overlaps by looking at the previous highlight range
							if (!highlight_end_positions.isEmpty())
							{
								int last_highlight_index = highlight_end_positions.size() - 1;
								int last_highlight_end = highlight_end_positions.get(last_highlight_index).intValue();
								if (last_highlight_end > partial_phrase_match.start_position)
								{
									// There is an overlap, so remove the previous phrase match
									int last_highlight_start = highlight_start_positions.remove(last_highlight_index).intValue();
									highlight_end_positions.remove(last_highlight_index);
									partial_phrase_match.start_position = last_highlight_start;
								}
							}

							highlight_start_positions.add(new Integer(partial_phrase_match.start_position));
							highlight_end_positions.add(new Integer(word_match.end_position));
						}
						// No, but add the partial match back into the list for next time
						else
						{
							partial_phrase_matches.add(partial_phrase_match);
						}
					}
				}
			}
			else
			{
				partial_phrase_matches.clear();
			}

			// See if this word is at the start of any of the phrases
			for (int p = 0; p < phrase_query_term_variants_hierarchy.size(); p++)
			{
				ArrayList phrase_query_p_term_variants_list = phrase_query_term_variants_hierarchy.get(p);
				if (phrase_query_p_term_variants_list.size()>0) {
				HashSet phrase_query_p_term_1_variants = (HashSet) phrase_query_p_term_variants_list.get(0);
				if (phrase_query_p_term_1_variants.contains(word_match.word))
				{
					// If this phrase is just one word long, we have a complete match
					if (phrase_query_p_term_variants_list.size() == 1)
					{
						highlight_start_positions.add(new Integer(word_match.start_position));
						highlight_end_positions.add(new Integer(word_match.end_position));
					}
					// Otherwise we have the start of a potential phrase match
					else
					{
						partial_phrase_matches.add(new PartialPhraseMatch(word_match.start_position, p));
					}
				}
				}
			}
		}

		// Now add the annotation tags into the document at the correct points
		Element content_element = doc.createElement(GSXML.NODE_CONTENT_ELEM);

		int last_wrote = 0;
		for (int i = 0; i < highlight_start_positions.size(); i++)
		{
			int highlight_start = highlight_start_positions.get(i).intValue();
			int highlight_end = highlight_end_positions.get(i).intValue();

			// Print anything before the highlight range
			if (last_wrote < highlight_start)
			{
				String preceding_text = new String(content_characters, last_wrote, (highlight_start - last_wrote));
				content_element.appendChild(doc.createTextNode(preceding_text));
			}

			// Print the highlight text, annotated
			if (highlight_end > last_wrote)
			{
				String highlight_text = new String(content_characters, highlight_start, (highlight_end - highlight_start));
				Element annotation_element = GSXML.createTextElement(doc, "annotation", highlight_text);
				annotation_element.setAttribute("type", "query_term");
				content_element.appendChild(annotation_element);
				last_wrote = highlight_end;
			}
		}

		// Finish off any unwritten text
		if (last_wrote < content_characters.length)
		{
			String remaining_text = new String(content_characters, last_wrote, (content_characters.length - last_wrote));
			content_element.appendChild(doc.createTextNode(remaining_text));
		}
		return content_element;
	}

	static private class WordMatch
	{
		public String word;
		public int start_position;
		public int end_position;
		public boolean preceding_word_matched;

		public WordMatch(String word, int start_position, int end_position, boolean preceding_word_matched)
		{
			this.word = word;
			this.start_position = start_position;
			this.end_position = end_position;
			this.preceding_word_matched = preceding_word_matched;
		}
	}

	static private class PartialPhraseMatch
	{
		public int start_position;
		public int query_phrase_number;
		public int num_words_matched;

		public PartialPhraseMatch(int start_position, int query_phrase_number)
		{
			this.start_position = start_position;
			this.query_phrase_number = query_phrase_number;
			this.num_words_matched = 1;
		}
	}
}
