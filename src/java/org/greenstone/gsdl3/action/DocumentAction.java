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

import org.apache.log4j.*;

/** Action class for retrieving Documents via the message router */
public class DocumentAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.DocumentAction.class.getName());

	// this is used to specify that the sibling nodes of a selected one should be obtained
	public static final String SIBLING_ARG = "sib";
	public static final String GOTO_PAGE_ARG = "gp";
	public static final String ENRICH_DOC_ARG = "end";

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

		Element message = this.converter.nodeToElement(message_node);

		// the response
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// get the request - assume only one
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(cgi_paramList, false);

		// just in case there are some that need to get passed to the services
		HashMap service_params = (HashMap) params.get("s0");

		String has_rl = null;
		String has_href = null;
		has_href = (String) params.get("href");//for an external link : get the href URL if it is existing in the params list 
		has_rl = (String) params.get("rl");//for an external link : get the rl value if it is existing in the params list
		String collection = (String) params.get(GSParams.COLLECTION);
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		String document_name = (String) params.get(GSParams.DOCUMENT);
		if ((document_name == null || document_name.equals("")) && (has_href == null || has_href.equals("")))
		{
			logger.error("no document specified!");
			return result;
		}
		String document_type = (String) params.get(GSParams.DOCUMENT_TYPE);
		if (document_type == null)
		{
			document_type = "simple";
		}
		//whether to retrieve siblings or not
		boolean get_siblings = false;
		String sibs = (String) params.get(SIBLING_ARG);
		if (sibs != null && sibs.equals("1"))
		{
			get_siblings = true;
		}

		String sibling_num = (String) params.get(GOTO_PAGE_ARG);
		if (sibling_num != null && !sibling_num.equals(""))
		{
			// we have to modify the doc name
			document_name = document_name + "." + sibling_num + ".ss";
		}

		boolean expand_document = false;
		String ed_arg = (String) params.get(GSParams.EXPAND_DOCUMENT);
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
			String ec_arg = (String) params.get(GSParams.EXPAND_CONTENTS);
			if (ec_arg != null && ec_arg.equals("1"))
			{
				expand_contents = true;
			}
		}

		//append site metadata
		addSiteMetadata(page_response, lang, uid);

		// get the additional data needed for the page
		getBackgroundData(page_response, collection, lang, uid);
		Element format_elem = (Element) GSXML.getChildByTagName(page_response, GSXML.FORMAT_ELEM);

		// the_document is where all the doc info - structure and metadata etc
		// is added into, to be returned in the page
		Element the_document = this.doc.createElement(GSXML.DOCUMENT_ELEM);
		page_response.appendChild(the_document);

		// set the doctype from the cgi arg as an attribute
		the_document.setAttribute(GSXML.DOC_TYPE_ATT, document_type);

		// create a basic doc list containing the current node
		Element basic_doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		Element current_doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		basic_doc_list.appendChild(current_doc);
		if (document_name.length() != 0)
		{
			current_doc.setAttribute(GSXML.NODE_ID_ATT, document_name);
		}
		else if (has_href.length() != 0)
		{
			current_doc.setAttribute(GSXML.NODE_ID_ATT, has_href);
			current_doc.setAttribute("externalURL", has_rl);
		}

		// Create a parameter list to specify the required structure information
		Element ds_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (service_params != null)
		{
			GSXML.addParametersToList(this.doc, ds_param_list, service_params);
		}

		Element ds_param = null;
		boolean get_structure = false;
		boolean get_structure_info = false;
		if (document_type.equals("paged"))
		{
			get_structure_info = true;
			// get teh info needed for paged naviagtion
			ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "numSiblings");
			ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "numChildren");
			ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
			ds_param_list.appendChild(ds_param);
			ds_param.setAttribute(GSXML.NAME_ATT, "info");
			ds_param.setAttribute(GSXML.VALUE_ATT, "siblingPosition");

		}
		else if (document_type.equals("hierarchy"))
		{
			get_structure = true;
			if (expand_contents)
			{
				ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "entire");
			}
			else
			{
				// get the info needed for table of contents
				ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "ancestors");
				ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
				ds_param_list.appendChild(ds_param);
				ds_param.setAttribute(GSXML.NAME_ATT, "structure");
				ds_param.setAttribute(GSXML.VALUE_ATT, "children");
				if (get_siblings)
				{
					ds_param = this.doc.createElement(GSXML.PARAM_ELEM);
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
			Element ds_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
			String to = GSPath.appendLink(collection, "DocumentStructureRetrieve");// Hard-wired?
			Element ds_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
			ds_message.appendChild(ds_request);
			ds_request.appendChild(ds_param_list);

			// create a doc_node_list and put in the doc_node that we are interested in
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
				the_document.appendChild(this.doc.importNode(ds_response_struct_info, true));
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
					the_document.appendChild(this.doc.importNode(structs.item(i), true));
				}
			}
			else
			{
				// no structure nodes, so put in a dummy doc node
				Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
				if (document_name.length() != 0)
				{
					doc_node.setAttribute(GSXML.NODE_ID_ATT, document_name);
				}
				else if (has_href.length() != 0)
				{
					doc_node.setAttribute(GSXML.NODE_ID_ATT, has_href);
					doc_node.setAttribute("externalURL", has_rl);
				}
				the_document.appendChild(doc_node);
				has_dummy = true;
			}
		}
		else
		{ // a simple type - we dont have a dummy node for simple
			// should think about this more
			// no structure request, so just put in a dummy doc node
			Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
			if (document_name.length() != 0)
			{
				doc_node.setAttribute(GSXML.NODE_ID_ATT, document_name);
			}
			else if (has_href.length() != 0)
			{
				doc_node.setAttribute(GSXML.NODE_ID_ATT, has_href);
				doc_node.setAttribute("externalURL", has_rl);
			}
			the_document.appendChild(doc_node);
			has_dummy = true;
		}

		// Build a request to obtain some document metadata
		Element dm_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		String to = GSPath.appendLink(collection, "DocumentMetadataRetrieve"); // Hard-wired?
		Element dm_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		dm_message.appendChild(dm_request);
		// Create a parameter list to specify the required metadata information

		HashSet meta_names = new HashSet();
		meta_names.add("Title"); // the default
		if (format_elem != null)
		{
			extractMetadataNames(format_elem, meta_names);
		}

		Element dm_param_list = createMetadataParamList(meta_names);
		if (service_params != null)
		{
			GSXML.addParametersToList(this.doc, dm_param_list, service_params);
		}

		dm_request.appendChild(dm_param_list);

		// create the doc node list for the metadata request
		Element dm_doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		dm_request.appendChild(dm_doc_list);

		// Add each node from the structure response into the metadata request
		NodeList doc_nodes = the_document.getElementsByTagName(GSXML.DOC_NODE_ELEM);
		for (int i = 0; i < doc_nodes.getLength(); i++)
		{
			Element doc_node = (Element) doc_nodes.item(i);
			String doc_node_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);

			// Add the documentNode to the list
			Element dm_doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
			dm_doc_list.appendChild(dm_doc_node);
			dm_doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_node_id);
			dm_doc_node.setAttribute(GSXML.NODE_TYPE_ATT, doc_node.getAttribute(GSXML.NODE_TYPE_ATT));
		}

		// we also want a metadata request to the top level document to get
		// assocfilepath - this could be cached too
		Element doc_meta_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		dm_message.appendChild(doc_meta_request);
		Element doc_meta_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (service_params != null)
		{
			GSXML.addParametersToList(this.doc, doc_meta_param_list, service_params);
		}

		doc_meta_request.appendChild(doc_meta_param_list);
		Element doc_param = this.doc.createElement(GSXML.PARAM_ELEM);
		doc_meta_param_list.appendChild(doc_param);
		doc_param.setAttribute(GSXML.NAME_ATT, "metadata");
		doc_param.setAttribute(GSXML.VALUE_ATT, "assocfilepath");

		// create the doc node list for the metadata request
		Element doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		doc_meta_request.appendChild(doc_list);

		Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		// the node we want is the root document node
		if (document_name.length() != 0)
		{
			doc_node.setAttribute(GSXML.NODE_ID_ATT, document_name + ".rt");
		}
		else if (has_href.length() != 0)
		{
			doc_node.setAttribute(GSXML.NODE_ID_ATT, has_href + ".rt");
			doc_node.setAttribute("externalURL", has_rl);
		}
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
			GSXML.mergeMetadataLists(doc_nodes.item(i), dm_response_docs.item(i));
		}
		// get the top level doc metadata out
		Element doc_meta_response = (Element) dm_response_message.getElementsByTagName(GSXML.RESPONSE_ELEM).item(1);
		Element top_doc_node = (Element) GSXML.getNodeByPath(doc_meta_response, "documentNodeList/documentNode");
		GSXML.mergeMetadataLists(the_document, top_doc_node);

		// Build a request to obtain some document content
		Element dc_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		to = GSPath.appendLink(collection, "DocumentContentRetrieve"); // Hard-wired?
		Element dc_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		dc_message.appendChild(dc_request);

		// Create a parameter list to specify the request parameters - empty for now
		Element dc_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (service_params != null)
		{
			GSXML.addParametersToList(this.doc, dc_param_list, service_params);
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
		logger.debug("request = " + converter.getString(dc_message));
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
				Node content = GSXML.getChildByTagName((Element) dc_response_docs.item(i), "nodeContent");
				if (content != null)
				{
					if (highlight_query_terms)
					{
						content = highlightQueryTerms(request, (Element) content);
					}
					doc_nodes.item(i).appendChild(this.doc.importNode(content, true));
				}
				//GSXML.mergeMetadataLists(doc_nodes.item(i), dm_response_docs.item(i));
			}
		}
		else
		{
			//path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
			Element dc_response_doc = (Element) GSXML.getChildByTagName(dc_response_doc_list, GSXML.DOC_NODE_ELEM);
			Element dc_response_doc_content = (Element) GSXML.getChildByTagName(dc_response_doc, GSXML.NODE_CONTENT_ELEM);
			Element dc_response_doc_external = (Element) GSXML.getChildByTagName(dc_response_doc, "external");

			if (dc_response_doc_content == null)
			{
				// no content to add
				if (dc_response_doc_external != null)
				{
					String modified_doc_id = dc_response_doc.getAttribute(GSXML.NODE_ID_ATT);

					the_document.setAttribute("selectedNode", modified_doc_id);
					the_document.setAttribute("external", dc_response_doc_external.getAttribute("external_link"));
				}
				return result;
			}
			if (highlight_query_terms)
			{
				dc_response_doc.removeChild(dc_response_doc_content);

				dc_response_doc_content = highlightQueryTerms(request, dc_response_doc_content);
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
					Element enrich_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
					Element enrich_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, enrich_service, lang, uid);
					enrich_message.appendChild(enrich_request);
					// check for parameters
					HashMap e_service_params = (HashMap) params.get("s1");
					if (e_service_params != null)
					{
						Element enrich_pl = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
						GSXML.addParametersToList(this.doc, enrich_pl, e_service_params);
						enrich_request.appendChild(enrich_pl);
					}
					Element e_doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
					enrich_request.appendChild(e_doc_list);
					e_doc_list.appendChild(this.doc.importNode(dc_response_doc, true));

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
				dummy_node.appendChild(this.doc.importNode(dc_response_doc_content, true));
				// hack for simple type
				if (document_type.equals("simple"))
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
						dn.appendChild(this.doc.importNode(dc_response_doc_content, true));
						break;
					}
				}
			}
		}
		logger.debug("(DocumentAction) Page:\n" + this.converter.getPrettyString(result));
		return result;
	}

	/**
	 * tell the param class what its arguments are if an action has its own
	 * arguments, this should add them to the params object - particularly
	 * important for args that should not be saved
	 */
	public boolean getActionParameters(GSParams params)
	{
		params.addParameter(GOTO_PAGE_ARG, false);
		params.addParameter(ENRICH_DOC_ARG, false);
		return true;
	}

	/**
	 * this method gets the collection description, the format info, the list of
	 * enrich services, etc - stuff that is needed for the page, but is the same
	 * whatever the query is - should be cached
	 */
	protected boolean getBackgroundData(Element page_response, String collection, String lang, String uid)
	{

		// create a message to process - contains requests for the collection 
		// description, the format element, the enrich services on offer
		// these could all be cached
		Element info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		String path = GSPath.appendLink(collection, "DocumentContentRetrieve");
		// the format request - ignore for now, where does this request go to??
		Element format_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_FORMAT, path, lang, uid);
		info_message.appendChild(format_request);

		// the enrich_services request - only do this if provide_annotations is true

		if (provide_annotations)
		{
			Element enrich_services_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", lang, uid);
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
			logger.debug("doc action found a format statement");
			// set teh format type
			format_elem.setAttribute(GSXML.TYPE_ATT, "display");
			page_response.appendChild(this.doc.importNode(format_elem, true));
		}

		if (provide_annotations)
		{
			Element services_resp = (Element) responses.item(1);

			// a new message for the mr
			Element enrich_message = this.doc.createElement(GSXML.MESSAGE_ELEM);

			NodeList e_services = services_resp.getElementsByTagName(GSXML.SERVICE_ELEM);
			boolean service_found = false;
			for (int j = 0; j < e_services.getLength(); j++)
			{
				if (((Element) e_services.item(j)).getAttribute(GSXML.TYPE_ATT).equals("enrich"))
				{
					Element s = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, ((Element) e_services.item(j)).getAttribute(GSXML.NAME_ATT), lang, uid);
					enrich_message.appendChild(s);
					service_found = true;
				}
			}
			if (service_found)
			{
				Element enrich_response = (Element) this.mr.process(enrich_message);

				NodeList e_responses = enrich_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
				Element service_list = this.doc.createElement(GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
				for (int i = 0; i < e_responses.getLength(); i++)
				{
					Element e_resp = (Element) e_responses.item(i);
					Element e_service = (Element) this.doc.importNode(GSXML.getChildByTagName(e_resp, GSXML.SERVICE_ELEM), true);
					e_service.setAttribute(GSXML.NAME_ATT, e_resp.getAttribute(GSXML.FROM_ATT));
					service_list.appendChild(e_service);
				}
				page_response.appendChild(service_list);
			}
		} // if provide_annotations
		return true;

	}

	/**
	 * this involves a bit of a hack to get the equivalent query terms - has to
	 * requery the query service - uses the last selected service name. (if it
	 * ends in query). should this action do the query or should it send a
	 * message to the query action? but that will involve lots of extra stuff.
	 * also doesn't handle phrases properly - just highlights all the terms found
	 * in the text.
	 */
	protected Element highlightQueryTerms(Element request, Element dc_response_doc_content)
	{

		// do the query again to get term info 
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(cgi_param_list, false);

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
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		String to = GSPath.appendLink(collection, service_name);

		Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		mr_query_message.appendChild(mr_query_request);

		// paramList
		HashMap service_params = (HashMap) params.get("s1");

		Element query_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(this.doc, query_param_list, service_params);
		mr_query_request.appendChild(query_param_list);

		// do the query
		Element mr_query_response = (Element) this.mr.process(mr_query_message);

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

		HashSet query_term_variants = new HashSet();
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

		ArrayList phrase_query_term_variants_hierarchy = new ArrayList();

		Element query_element = GSXML.getNamedElement(metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "query");
		String performed_query = GSXML.getNodeText(query_element) + " ";

		ArrayList phrase_query_p_term_variants_list = new ArrayList();
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

					HashSet phrase_query_p_term_x_variants = new HashSet();

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
						phrase_query_p_term_variants_list = new ArrayList();
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

				phrase_query_p_term_variants_list = new ArrayList();
			}
		}

		System.err.println(query_term_variants + " *** " + phrase_query_term_variants_hierarchy);
		return highlightQueryTermsInternal(content, query_term_variants, phrase_query_term_variants_hierarchy);
	}

	/**
	 * Highlights query terms in a piece of text.
	 */
	private Element highlightQueryTermsInternal(String content, HashSet query_term_variants, ArrayList phrase_query_term_variants_hierarchy)
	{
		// Convert the content string to an array of characters for speed
		char[] content_characters = new char[content.length()];
		content.getChars(0, content.length(), content_characters, 0);

		// Now skim through the content, identifying word matches
		ArrayList word_matches = new ArrayList();
		int word_start = 0;
		boolean in_word = false;
		boolean preceding_word_matched = false;
		boolean inTag = false;
		for (int i = 0; i < content_characters.length; i++)
		{
			//We don't want to find words inside HTML tags
			if(content_characters[i] == '<')
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

		ArrayList highlight_start_positions = new ArrayList();
		ArrayList highlight_end_positions = new ArrayList();

		// Deal with phrases now
		ArrayList partial_phrase_matches = new ArrayList();
		for (int i = 0; i < word_matches.size(); i++)
		{
			WordMatch word_match = (WordMatch) word_matches.get(i);

			// See if any partial phrase matches are extended by this word
			if (word_match.preceding_word_matched)
			{
				for (int j = partial_phrase_matches.size() - 1; j >= 0; j--)
				{
					PartialPhraseMatch partial_phrase_match = (PartialPhraseMatch) partial_phrase_matches.remove(j);
					ArrayList phrase_query_p_term_variants_list = (ArrayList) phrase_query_term_variants_hierarchy.get(partial_phrase_match.query_phrase_number);
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
								int last_highlight_end = ((Integer) highlight_end_positions.get(last_highlight_index)).intValue();
								if (last_highlight_end > partial_phrase_match.start_position)
								{
									// There is an overlap, so remove the previous phrase match
									int last_highlight_start = ((Integer) highlight_start_positions.remove(last_highlight_index)).intValue();
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
				ArrayList phrase_query_p_term_variants_list = (ArrayList) phrase_query_term_variants_hierarchy.get(p);
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

		// Now add the annotation tags into the document at the correct points
		Element content_element = this.doc.createElement(GSXML.NODE_CONTENT_ELEM);

		int last_wrote = 0;
		for (int i = 0; i < highlight_start_positions.size(); i++)
		{
			int highlight_start = ((Integer) highlight_start_positions.get(i)).intValue();
			int highlight_end = ((Integer) highlight_end_positions.get(i)).intValue();

			// Print anything before the highlight range
			if (last_wrote < highlight_start)
			{
				String preceding_text = new String(content_characters, last_wrote, (highlight_start - last_wrote));
				content_element.appendChild(this.doc.createTextNode(preceding_text));
			}

			// Print the highlight text, annotated
			if (highlight_end > last_wrote)
			{
				String highlight_text = new String(content_characters, highlight_start, (highlight_end - highlight_start));
				Element annotation_element = GSXML.createTextElement(this.doc, "annotation", highlight_text);
				annotation_element.setAttribute("type", "query_term");
				content_element.appendChild(annotation_element);
				last_wrote = highlight_end;
			}
		}

		// Finish off any unwritten text
		if (last_wrote < content_characters.length)
		{
			String remaining_text = new String(content_characters, last_wrote, (content_characters.length - last_wrote));
			content_element.appendChild(this.doc.createTextNode(remaining_text));
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
