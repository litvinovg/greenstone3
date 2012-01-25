/*
 *    AbstractDocumentRetrieve.java
 *    a base class for retrieval services

 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
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
import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.GSConstants;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// General Java classes
import java.io.File;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.log4j.*;

/**
 * Abstract class for Document Retrieval Services
 * 
 * @author <a href="mailto:greenstone@cs.waikato.ac.nz">Katherine Don</a>
 */

public abstract class AbstractDocumentRetrieve extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractDocumentRetrieve.class.getName());

	// the services on offer
	protected static final String DOCUMENT_STRUCTURE_RETRIEVE_SERVICE = "DocumentStructureRetrieve";
	protected static final String DOCUMENT_METADATA_RETRIEVE_SERVICE = "DocumentMetadataRetrieve";
	protected static final String DOCUMENT_CONTENT_RETRIEVE_SERVICE = "DocumentContentRetrieve";

	protected static final String STRUCT_PARAM = "structure";
	protected static final String INFO_PARAM = "info";

	protected static final String STRUCT_ANCESTORS = "ancestors";
	protected static final String STRUCT_PARENT = "parent";
	protected static final String STRUCT_SIBS = "siblings";
	protected static final String STRUCT_CHILDREN = "children";
	protected static final String STRUCT_DESCENDS = "descendants";
	protected static final String STRUCT_ENTIRE = "entire";

	protected static final String INFO_NUM_SIBS = "numSiblings";
	protected static final String INFO_NUM_CHILDREN = "numChildren";
	protected static final String INFO_SIB_POS = "siblingPosition";

	// means the id is not a greenstone id and needs translating
	protected static final String EXTID_PARAM = "ext";

	protected Element config_info = null; // the xml from the config file

	protected String default_document_type = null;
	protected MacroResolver macro_resolver = null;

	/** does this class provide the service?? */
	protected boolean does_metadata = true;
	protected boolean does_content = true;
	protected boolean does_structure = true;

	/** constructor */
	public AbstractDocumentRetrieve()
	{
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring AbstractDocumentRetrieve...");
		this.config_info = info;

		// set up short_service_info_ - for now just has name and type
		if (does_structure)
		{
			Element dsr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			dsr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dsr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_STRUCTURE_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dsr_service);
		}

		if (does_metadata)
		{
			Element dmr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			dmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dmr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dmr_service);
		}

		if (does_content)
		{
			Element dcr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			dcr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dcr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dcr_service);
		}

		// look for document display format
		String path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
		Element display_format = (Element) GSXML.getNodeByPath(extra_info, path);
		if (display_format != null)
		{
			this.format_info_map.put(DOCUMENT_CONTENT_RETRIEVE_SERVICE, this.doc.importNode(display_format, true));
			// should we keep a copy?
			// check for docType option.
			Element doc_type_opt = GSXML.getNamedElement(display_format, "gsf:option", GSXML.NAME_ATT, "documentType");
			if (doc_type_opt != null)
			{
				String value = doc_type_opt.getAttribute(GSXML.VALUE_ATT);
				if (!value.equals(""))
				{
					this.default_document_type = value;
				}
			}
		}

		if (macro_resolver != null)
		{
			macro_resolver.setSiteDetails(this.site_http_address, this.cluster_name, this.getLibraryName());
			// set up the macro resolver
			Element replacement_elem = (Element) GSXML.getChildByTagName(extra_info, "replaceList");
			if (replacement_elem != null)
			{
				macro_resolver.addMacros(replacement_elem);
			}
			// look for any refs to global replace lists
			NodeList replace_refs_elems = extra_info.getElementsByTagName("replaceListRef");
			for (int i = 0; i < replace_refs_elems.getLength(); i++)
			{
				String id = ((Element) replace_refs_elems.item(i)).getAttribute("id");
				if (!id.equals(""))
				{
					Element replace_list = GSXML.getNamedElement(this.router.config_info, "replaceList", "id", id);
					if (replace_list != null)
					{
						macro_resolver.addMacros(replace_list);
					}
				}
			}
		}

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{

		// these ones are probably never called, but put them here just in case
		Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
		service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		service_elem.setAttribute(GSXML.NAME_ATT, service_id);
		return service_elem;
	}

	protected Element processDocumentMetadataRetrieve(Element request)
	{

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		String lang = request.getAttribute(GSXML.LANG_ATT);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		if (!does_metadata)
		{
			// shouldn't get here
			return result;
		}
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, "DocumentMetadataRetrieve: missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		boolean external_id = false;
		// The metadata information required
		ArrayList metadata_names_list = new ArrayList();
		boolean all_metadata = false;
		// Process the request parameters
		Element param = GSXML.getFirstElementChild(param_list);//(Element) param_list.getFirstChild();
		while (param != null)
		{
			// Identify the metadata information desired
			if (param.getAttribute(GSXML.NAME_ATT).equals("metadata"))
			{
				String metadata = GSXML.getValue(param);
				if (metadata.equals("all"))
				{
					all_metadata = true;
					break;
				}
				metadata_names_list.add(metadata);
			}
			else if (param.getAttribute(GSXML.NAME_ATT).equals(EXTID_PARAM) && GSXML.getValue(param).equals("1"))
			{
				external_id = true;
			}
			param = (Element) param.getNextSibling();
		}

		// check that there has been some metadata specified
		if (!all_metadata && metadata_names_list.size() == 0)
		{
			GSXML.addError(this.doc, result, "DocumentMetadataRetrieve: no metadata names found in the " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// Get the documents
		Element request_node_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (request_node_list == null)
		{
			GSXML.addError(this.doc, result, "DocumentMetadataRetrieve: missing " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// copy the request doc node list to the response
		Element response_node_list = (Element) this.doc.importNode(request_node_list, true);
		result.appendChild(response_node_list);

		// use the copied list so that we add the metadata into the copy
		// are we just adding metadata for the top level nodes? or can we accept a hierarchy here???
		NodeList request_nodes = GSXML.getChildrenByTagName(response_node_list, GSXML.DOC_NODE_ELEM);
		if (request_nodes.getLength() == 0)
		{
			GSXML.addError(this.doc, result, "DocumentMetadataRetrieve: no " + GSXML.DOC_NODE_ELEM + " found in the " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// Whew, now we have checked (almost) all the syntax of the request, now we can process it.
		for (int i = 0; i < request_nodes.getLength(); i++)
		{
			Element request_node = (Element) request_nodes.item(i);
			String node_id = request_node.getAttribute(GSXML.NODE_ID_ATT);

			boolean is_external_link = false;
			if (!node_id.startsWith("HASH") && !node_id.startsWith("D"))
			{
				if (node_id.endsWith(".rt"))
				{
					node_id = getHrefOID(node_id.substring(0, node_id.length() - 3));
					if (node_id != null)
					{
						node_id += ".rt";
					}
					else
					{
						is_external_link = true;
					}
				}
				else
				{
					node_id = getHrefOID(node_id);
					if (node_id == null)
					{
						is_external_link = true;
					}
				}
			}
			if (!is_external_link)
			{
				if (external_id)
				{
					// can we have .pr etc extensions with external ids?
					node_id = translateExternalId(node_id);
				}
				else if (idNeedsTranslating(node_id))
				{
					node_id = translateId(node_id);
				}
			}

			if (node_id == null)
			{
				continue;
			}
			if (!is_external_link)
			{
				try
				{
					Element metadata_list = getMetadataList(node_id, all_metadata, metadata_names_list);
					request_node.appendChild(metadata_list);
				}
				catch (GSException e)
				{
					GSXML.addError(this.doc, result, e.getMessage(), e.getType());
					if (e.getType().equals(GSXML.ERROR_TYPE_SYSTEM))
					{
						// there is no point trying any others
						return result;
					}
				}
			}
			else
			{
				request_node.setAttribute("external_link", request_node.getAttribute(GSXML.NODE_ID_ATT));
			}
		}
		return result;
	}

	protected Element processDocumentStructureRetrieve(Element request)
	{

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_STRUCTURE_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		if (!does_structure)
		{
			// shouldn't get here
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, "DocumentStructureRetrieve: missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// get the documents of the request
		Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_doc_list == null)
		{
			GSXML.addError(this.doc, result, "DocumentStructureRetrieve: missing " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// copy the doc_list to the response
		Element response_node_list = (Element) this.doc.importNode(query_doc_list, true);
		result.appendChild(response_node_list);

		// check that we have some doc nodes specified
		NodeList node_list = GSXML.getChildrenByTagName(response_node_list, GSXML.DOC_NODE_ELEM);
		if (node_list.getLength() == 0)
		{
			GSXML.addError(this.doc, result, "DocumentStructureRetrieve: no " + GSXML.DOC_NODE_ELEM + " found in the " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		Element extid_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, EXTID_PARAM);
		boolean external_id = false;
		if (extid_param != null && GSXML.getValue(extid_param).equals("1"))
		{
			external_id = true;
		}

		// the type of info required
		boolean want_structure = false;
		boolean want_info = false;

		ArrayList info_types = new ArrayList();
		// The document structure information desired
		boolean want_ancestors = false;
		boolean want_parent = false;
		boolean want_siblings = false;
		boolean want_children = false;
		boolean want_descendants = false;

		boolean want_entire_structure = false;
		// Process the request parameters
		NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
		for (int i = 0; i < params.getLength(); i++)
		{

			Element param = (Element) params.item(i);
			String p_name = param.getAttribute(GSXML.NAME_ATT);
			String p_value = GSXML.getValue(param);
			// Identify the structure information desired
			if (p_name.equals(STRUCT_PARAM))
			{
				want_structure = true;

				// This is NOT locale sensitive
				if (p_value.equals(STRUCT_ANCESTORS))
					want_ancestors = true;
				else if (p_value.equals(STRUCT_PARENT))
					want_parent = true;
				else if (p_value.equals(STRUCT_SIBS))
					want_siblings = true;
				else if (p_value.equals(STRUCT_CHILDREN))
					want_children = true;
				else if (p_value.equals(STRUCT_DESCENDS))
					want_descendants = true;
				else if (p_value.equals(STRUCT_ENTIRE))
					want_entire_structure = true;
				else
					logger.error("AbstractDocumentRetrieve Warning: Unknown value \"" + p_value + "\".");
			}
			else if (p_name.equals(INFO_PARAM))
			{
				want_info = true;
				info_types.add(p_value);
			}
		}

		// Make sure there is no repeated information
		if (want_ancestors)
			want_parent = false;
		if (want_descendants)
			want_children = false;

		for (int i = 0; i < node_list.getLength(); i++)
		{
			Element doc = (Element) node_list.item(i);

			String doc_id = doc.getAttribute(GSXML.NODE_ID_ATT);
			String is_external = doc.getAttribute("externalURL");

			boolean is_external_link = false;
			if (is_external.equals("0"))
			{
				is_external_link = true;
			}
			if (is_external.equals("1") && !doc_id.startsWith("HASH") && !is_external_link)
			{
				if (doc_id.endsWith(".rt"))
				{
					doc_id = getHrefOID(doc_id.substring(0, doc_id.length() - 3));
					if (doc_id != null)
					{
						doc_id += ".rt";
					}
					else
					{
						is_external_link = true;
					}
				}
				else
				{
					doc_id = getHrefOID(doc_id);
					if (doc_id == null)
					{
						is_external_link = true;
					}
				}
			}
			if (!is_external_link)
			{
				if (external_id)
				{
					doc_id = translateExternalId(doc_id);
					doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				}
				else if (idNeedsTranslating(doc_id))
				{
					doc_id = translateId(doc_id);
					doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				}

				if (doc_id == null)
				{
					continue;
				}

				if (want_info)
				{
					Element node_info_elem = this.doc.createElement("nodeStructureInfo");
					doc.appendChild(node_info_elem);

					for (int j = 0; j < info_types.size(); j++)
					{
						String info_type = (String) info_types.get(j);
						String info_value = getStructureInfo(doc_id, info_type);
						if (info_value != null)
						{
							Element info_elem = this.doc.createElement("info");
							info_elem.setAttribute(GSXML.NAME_ATT, info_type);
							info_elem.setAttribute(GSXML.VALUE_ATT, info_value);
							node_info_elem.appendChild(info_elem);
						}
					}
				}

				if (want_structure)
				{
					// all structure info goes into a nodeStructure elem
					Element structure_elem = this.doc.createElement(GSXML.NODE_STRUCTURE_ELEM);
					doc.appendChild(structure_elem);

					if (want_entire_structure)
					{
						String root_id = getRootId(doc_id);
						Element root_node = createDocNode(root_id); //, true, false);
						addDescendants(root_node, root_id, true);
						structure_elem.appendChild(root_node);
						continue; // with the next document, we dont need to do any more here
					}

					// Add the requested structure information
					Element base_node = createDocNode(doc_id); //, false, false);

					//Ancestors: continually add parent nodes until the root is reached
					Element top_node = base_node; // the top node so far
					if (want_ancestors)
					{
						String current_id = doc_id;
						while (true)
						{
							String parent_id = getParentId(current_id);
							//Element parent = getParent(current_id);
							if (parent_id == null)
								break; // no parent
							Element parent_node = createDocNode(parent_id);
							parent_node.appendChild(top_node);
							current_id = parent_id;//.getAttribute(GSXML.NODE_ID_ATT);
							top_node = parent_node;
						}
					}
					// Parent: get the parent of the selected node
					else if (want_parent)
					{
						String parent_id = getParentId(doc_id);
						if (parent_id != null)
						{
							Element parent_node = createDocNode(parent_id);
							parent_node.appendChild(base_node);
							top_node = parent_node;
						}
					}

					// now the top node is the root of the structure
					structure_elem.appendChild(top_node);

					//Siblings: get the other descendants of the selected node's parent
					if (want_siblings)
					{
						String parent_id = getParentId(doc_id);
						if (parent_id != null)
						{
							// if parent == current id, then we are at the top 
							// and can't get siblings
							Element parent_node = (Element) base_node.getParentNode(); // this may be the structure element if there has been no request for parents or ancestors

							// add siblings, - returns a pointer to the new current node 
							base_node = addSiblings(parent_node, parent_id, doc_id);
						}

					}

					// Children: get the descendants, but only one level deep
					if (want_children)
					{
						addDescendants(base_node, doc_id, false);
					}
					// Descendants: recursively get every descendant 
					else if (want_descendants)
					{
						addDescendants(base_node, doc_id, true);
					}
				} // if want structure

			}
			else
			{
				Element external_link_elem = this.doc.createElement("external");
				external_link_elem.setAttribute("external_link", doc.getAttribute(GSXML.NODE_ID_ATT));
				doc.appendChild(external_link_elem);
			}// if is_external_link
		} // for each doc
		return result;
	}

	/** Retrieve the content of a document */
	protected Element processDocumentContentRetrieve(Element request)
	{
		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		if (!does_content)
		{
			// shouldn't get here
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element extid_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, EXTID_PARAM);
		boolean external_id = false;
		if (extid_param != null && GSXML.getValue(extid_param).equals("1"))
		{
			external_id = true;
		}
		// Get the request content 
		Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_doc_list == null)
		{
			logger.error("Error: DocumentContentRetrieve request specified no doc nodes.\n");
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		Element doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(doc_list);

		// set up the retrieval??

		// Get the documents
		String[] doc_ids = GSXML.getAttributeValuesFromList(query_doc_list, GSXML.NODE_ID_ATT);
		String[] is_externals = GSXML.getAttributeValuesFromList(query_doc_list, "externalURL");

		for (int i = 0; i < doc_ids.length; i++)
		{
			String doc_id = doc_ids[i];
			String is_external = is_externals[i];

			boolean is_external_link = false;
			if (is_external.equals("0"))
			{
				is_external_link = true;
			}
			if (is_external.equals("1") && !doc_id.startsWith("HASH") && !is_external_link)
			{
				//if (!doc_id.startsWith("HASH")){
				if (doc_id.endsWith(".rt"))
				{
					String find_doc_id = getHrefOID(doc_id.substring(0, doc_id.length() - 3));
					if (find_doc_id != null)
					{
						doc_id = doc_id + ".rt";
					}
					else
					{
						is_external_link = true;
					}

				}
				else
				{
					String find_doc_id = getHrefOID(doc_id);
					if (find_doc_id == null)
					{
						is_external_link = true;
					}
					else
					{
						doc_id = find_doc_id;
					}
				}
			}

			if (!is_external_link)
			{
				// Create the document node
				Element doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
				doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				doc_list.appendChild(doc);

				if (external_id)
				{
					doc_id = translateExternalId(doc_id);
					doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				}
				else if (idNeedsTranslating(doc_id))
				{
					doc_id = translateId(doc_id);
					doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				}
				if (doc_id == null)
				{
					continue;
				}
				try
				{
					Element node_content = getNodeContent(doc_id, lang);
					doc.appendChild(node_content);
				}
				catch (GSException e)
				{
					GSXML.addError(this.doc, result, e.getMessage());
					return result;

				}
			}
			else
			{
				Element doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
				doc.setAttribute(GSXML.NODE_ID_ATT, doc_id);
				//doc.setAttribute("external_link", doc_id);
				Element external_link_elem = this.doc.createElement("external");
				external_link_elem.setAttribute("external_link", doc_id);
				doc.appendChild(external_link_elem);

				doc_list.appendChild(doc);
			}
		}
		return result;
	}

	/**
	 * create an element to go into the structure. A node element has the form
	 * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy'/>
	 */
	protected Element createDocNode(String node_id)
	{
		Element node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		node.setAttribute(GSXML.NODE_ID_ATT, node_id);

		String doc_type = null;
		if (default_document_type != null)
		{
			doc_type = default_document_type;
		}
		else
		{
			doc_type = getDocType(node_id);
		}
		node.setAttribute(GSXML.DOC_TYPE_ATT, doc_type);
		String node_type = getNodeType(node_id, doc_type);
		node.setAttribute(GSXML.NODE_TYPE_ATT, node_type);
		return node;
	}

	/**
	 * adds all the children of doc_id the the doc element, and if
	 * recursive=true, adds all their children as well
	 */
	protected void addDescendants(Element doc, String doc_id, boolean recursive)
	{
		ArrayList child_ids = getChildrenIds(doc_id);
		if (child_ids == null)
			return;
		for (int i = 0; i < child_ids.size(); i++)
		{
			String child_id = (String) child_ids.get(i);
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
	protected Element addSiblings(Element parent_node, String parent_id, String current_id)
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

	/**
	 * returns true if oid ends in .fc (firstchild), .lc (lastchild), .pr
	 * (parent), .ns (next sibling), .ps (prev sibling), .rt (root) .ss
	 * (specified sibling), false otherwise
	 */
	protected boolean idNeedsTranslating(String id)
	{
		return OID.needsTranslating(id);
	}

	/** returns the list of sibling ids, including the specified node_id */
	protected ArrayList getSiblingIds(String node_id)
	{
		String parent_id = getParentId(node_id);
		if (parent_id == null)
		{
			return null;
		}
		return getChildrenIds(parent_id);

	}

	/**
	 * returns the node type of the specified node. should be one of
	 * GSXML.NODE_TYPE_LEAF, GSXML.NODE_TYPE_INTERNAL, GSXML.NODE_TYPE_ROOT
	 */
	protected String getNodeType(String node_id, String doc_type)
	{
		if (doc_type.equals(GSXML.DOC_TYPE_SIMPLE))
		{
			return GSXML.NODE_TYPE_LEAF;
		}

		if (getParentId(node_id) == null)
		{
			return GSXML.NODE_TYPE_ROOT;
		}
		if (doc_type.equals(GSXML.DOC_TYPE_PAGED))
		{
			return GSXML.NODE_TYPE_LEAF;
		}
		if (getChildrenIds(node_id) == null)
		{
			return GSXML.NODE_TYPE_LEAF;
		}
		return GSXML.NODE_TYPE_INTERNAL;

	}

	/**
	 * if id ends in .fc, .pc etc, then translate it to the correct id default
	 * implementation: just remove the suffix
	 */
	protected String translateId(String id)
	{
		return id.substring(0, id.length());
	}

	/**
	 * if an id is not a greenstone id (an external id) then translate it to a
	 * greenstone one default implementation: return the id
	 */
	protected String translateExternalId(String id)
	{
		return id;
	}

	/**
	 * returns the document type of the doc that the specified node belongs to.
	 * should be one of GSXML.DOC_TYPE_SIMPLE, GSXML.DOC_TYPE_PAGED,
	 * GSXML.DOC_TYPE_HIERARCHY default implementation: return DOC_TYPE_SIMPLE
	 */
	protected String getDocType(String node_id)
	{
		return GSXML.DOC_TYPE_SIMPLE;
	}

	/**
	 * returns the id of the root node of the document containing node node_id.
	 * may be the same as node_id default implemntation: return node_id
	 */
	protected String getRootId(String node_id)
	{
		return node_id;
	}

	/**
	 * returns a list of the child ids in order, null if no children default
	 * implementation: return null
	 */
	protected ArrayList getChildrenIds(String node_id)
	{
		return null;
	}

	/**
	 * returns the node id of the parent node, null if no parent default
	 * implementation: return null
	 */
	protected String getParentId(String node_id)
	{
		return null;
	}

	/**
	 * get the metadata for the doc node doc_id returns a metadataList element:
	 * <metadataList><metadata name="xxx">value</metadata></metadataList>
	 */
	abstract protected Element getMetadataList(String doc_id, boolean all_metadata, ArrayList metadata_names) throws GSException;

	/**
	 * returns the content of a node should return a nodeContent element:
	 * <nodeContent>text content or other elements</nodeContent> can return
	 */
	abstract protected Element getNodeContent(String doc_id, String lang) throws GSException;

	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS
	 */
	abstract protected String getStructureInfo(String doc_id, String info_type);

	protected String getHrefOID(String href_url)
	{
		return null;
	}

}
