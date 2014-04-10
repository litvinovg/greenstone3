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
import org.greenstone.gsdl3.util.AbstractBasicDocument;
import org.greenstone.gsdl3.util.BasicDocument;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.XMLConverter;

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


	protected AbstractBasicDocument gs_doc = null;

	// means the id is not a greenstone id and needs translating
	//	protected static final String EXTID_PARAM = "ext";

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
			Element dsr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			dsr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dsr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_STRUCTURE_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dsr_service);
		}

		if (does_metadata)
		{
			Element dmr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			dmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dmr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dmr_service);
		}

		if (does_content)
		{
			Element dcr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			dcr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			dcr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
			this.short_service_info.appendChild(dcr_service);
		}

		// look for document display format
		String path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
		Element display_format = (Element) GSXML.getNodeByPath(extra_info, path);
		if (display_format != null)
		{
			this.format_info_map.put(DOCUMENT_CONTENT_RETRIEVE_SERVICE, this.desc_doc.importNode(display_format, true));
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
			Element replacement_elem = (Element) GSXML.getChildByTagName(extra_info, GSXML.REPLACE_ELEM + GSXML.LIST_MODIFIER);
			if (replacement_elem != null)
			{
				macro_resolver.addMacros(replacement_elem);
			}
			// look for any refs to global replace lists
			NodeList replace_refs_elems = extra_info.getElementsByTagName(GSXML.REPLACE_ELEM + GSXML.LIST_MODIFIER + GSXML.REF_MODIFIER);
			for (int i = 0; i < replace_refs_elems.getLength(); i++)
			{
				String id = ((Element) replace_refs_elems.item(i)).getAttribute("id");
				if (!id.equals(""))
				{
					Element replace_list = GSXML.getNamedElement(this.router.config_info, GSXML.REPLACE_ELEM + GSXML.LIST_MODIFIER, "id", id);
					if (replace_list != null)
					{
						macro_resolver.addMacros(replace_list);
					}
				}
			}
		}

		// Base line for document (might be overriden by sub-classes)
		gs_doc = new BasicDocument(this.default_document_type);

		return true;
	}

  protected Element getServiceDescription(Document doc, String service_id, String lang, String subset)
	{

		// these ones are probably never called, but put them here just in case
		Element service_elem = doc.createElement(GSXML.SERVICE_ELEM);
		service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		service_elem.setAttribute(GSXML.NAME_ATT, service_id);
		return service_elem;
	}

	protected Element processDocumentMetadataRetrieve(Element request)
	{

		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
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
			GSXML.addError(result, "DocumentMetadataRetrieve: missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// The metadata information required
		ArrayList<String> metadata_names_list = new ArrayList<String>();
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
				}
				metadata_names_list.add(metadata);
			}

			param = (Element) param.getNextSibling();
		}

		// check that there has been some metadata specified
		if (!all_metadata && metadata_names_list.size() == 0)
		{
			GSXML.addError(result, "DocumentMetadataRetrieve: no metadata names found in the " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// Get the documents
		Element request_node_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (request_node_list == null)
		{
			GSXML.addError(result, "DocumentMetadataRetrieve: missing " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// copy the request doc node list to the response
		Element response_node_list = (Element) result_doc.importNode(request_node_list, true);
		result.appendChild(response_node_list);

		// use the copied list so that we add the metadata into the copy
		// are we just adding metadata for the top level nodes? or can we accept a hierarchy here???
		NodeList doc_nodes = GSXML.getChildrenByTagName(response_node_list, GSXML.DOC_NODE_ELEM);
		if (doc_nodes.getLength() == 0)
		{
			GSXML.addError(result, "DocumentMetadataRetrieve: no " + GSXML.DOC_NODE_ELEM + " found in the " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// Whew, now we have checked (almost) all the syntax of the request, now we can process it.
		for (int i = 0; i < doc_nodes.getLength(); i++)
		{
			Element doc_node = (Element) doc_nodes.item(i);
			String node_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);
			boolean is_href_id = false;
			if (node_id.equals(""))
			{
				node_id = getGreenstoneIdFromHref(doc_node);
				if (node_id == null)
				{
					// **** TODO, is this good enough???
					doc_node.setAttribute("external_link", "true");
					continue;
				}

			}

			// may have modifiers .rt, .1.ss etc
			if (idNeedsTranslating(node_id))
			{
				node_id = translateId(node_id);
			}

			if (node_id == null)
			{
				continue;
			}
			try
			{
			  Element metadata_list = getMetadataList(result_doc, node_id, all_metadata, metadata_names_list, lang);
				if (metadata_list != null)
				{
					doc_node.appendChild(metadata_list);
				}
			}
			catch (GSException e)
			{
				GSXML.addError(result, e.getMessage(), e.getType());
				if (e.getType().equals(GSXML.ERROR_TYPE_SYSTEM))
				{
					// there is no point trying any others
					return result;
				}
			}

		} // for each doc node

		return result;
	}

	protected Element processDocumentStructureRetrieve(Element request)
	{

		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
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
			GSXML.addError(result, "DocumentStructureRetrieve: missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// get the documents of the request
		Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_doc_list == null)
		{
			GSXML.addError(result, "DocumentStructureRetrieve: missing " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// copy the doc_list to the response
		Element response_node_list = (Element) result_doc.importNode(query_doc_list, true);
		result.appendChild(response_node_list);

		// check that we have some doc nodes specified
		NodeList node_list = GSXML.getChildrenByTagName(response_node_list, GSXML.DOC_NODE_ELEM);
		if (node_list.getLength() == 0)
		{
			GSXML.addError(result, "DocumentStructureRetrieve: no " + GSXML.DOC_NODE_ELEM + " found in the " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		// the type of info required
		boolean want_structure = false;
		boolean want_info = false;

		ArrayList<String> info_types = new ArrayList<String>();
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

		// for each of the doc nodes, get the required info
		// these nodes are part of result_doc - can use that to create Elements
		for (int i = 0; i < node_list.getLength(); i++)
		{
			Element doc_node = (Element) node_list.item(i);

			String doc_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);
			boolean is_href_id = false;
			if (doc_id.equals(""))
			{
				doc_id = getGreenstoneIdFromHref(doc_node);
				if (doc_id == null)
				{
					// **** TODO, is this good enough???
					doc_node.setAttribute("external_link", "true");
					continue;
				}
				doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_id);
			}

			if (idNeedsTranslating(doc_id))
			{
				doc_id = translateId(doc_id);
				doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_id);
			}

			if (doc_id == null)
			{
				continue;
			}

			if (want_info)
			{
			  Element node_info_elem = result_doc.createElement("nodeStructureInfo");
				doc_node.appendChild(node_info_elem);

				for (int j = 0; j < info_types.size(); j++)
				{
					String info_type = info_types.get(j);
					String info_value = getStructureInfo(doc_id, info_type);
					if (info_value != null)
					{
						Element info_elem = result_doc.createElement("info");
						info_elem.setAttribute(GSXML.NAME_ATT, info_type);
						info_elem.setAttribute(GSXML.VALUE_ATT, info_value);
						node_info_elem.appendChild(info_elem);
					}
				}
			}

			if (want_structure)
			{
				// all structure info goes into a nodeStructure elem
				Element structure_elem = result_doc.createElement(GSXML.NODE_STRUCTURE_ELEM);
				doc_node.appendChild(structure_elem);

				if (want_entire_structure)
				{
					String root_id = getRootId(doc_id);
					Element root_node = createDocNode(result_doc, root_id); //, true, false);
					addDescendants(root_node, root_id, true);
					structure_elem.appendChild(root_node);
					continue; // with the next document, we dont need to do any more here
				}

				// Add the requested structure information
				Element base_node = createDocNode(result_doc, doc_id); //, false, false);

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
						Element parent_node = createDocNode(result_doc, parent_id);
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
					  Element parent_node = createDocNode(result_doc, parent_id);
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

		} // for each doc
		return result;
	}

	/** Retrieve the content of a document */
	protected Element processDocumentContentRetrieve(Element request)
	{
		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_CONTENT_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		if (!does_content)
		{
			// shouldn't get here
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		// Get the request content 
		Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_doc_list == null)
		{
			logger.error("Error: DocumentContentRetrieve request specified no doc nodes.\n");
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);

		// copy the request doc node list to the response
		Element response_node_list = (Element) result_doc.importNode(query_doc_list, true);
		result.appendChild(response_node_list);

		NodeList doc_nodes = GSXML.getChildrenByTagName(response_node_list, GSXML.DOC_NODE_ELEM);
		if (doc_nodes.getLength() == 0)
		{
			GSXML.addError(result, "DocumentContentRetrieve: no " + GSXML.DOC_NODE_ELEM + " found in the " + GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		//Element doc_list = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		//result.appendChild(doc_list);

		// set up the retrieval??

		// Get the documents
		//String[] doc_ids = GSXML.getAttributeValuesFromList(query_doc_list, GSXML.NODE_ID_ATT);
		//String[] is_externals = GSXML.getAttributeValuesFromList(query_doc_list, "externalURL");

		for (int i = 0; i < doc_nodes.getLength(); i++)
		{
			Element doc_node = (Element) doc_nodes.item(i);
			String node_id = doc_node.getAttribute(GSXML.NODE_ID_ATT);
			
			if (node_id.equals(""))
			{
				node_id = getGreenstoneIdFromHref(doc_node);
				if (node_id == null)
				{
					// **** TODO, is this good enough???
					doc_node.setAttribute("external_link", "true");
					continue;
				}
				doc_node.setAttribute(GSXML.NODE_ID_ATT, node_id);
			}

			// may have modifiers .rt, .1.ss etc
			if (idNeedsTranslating(node_id))
			{
				node_id = translateId(node_id);
			}

			if (node_id == null)
			{
				continue;
			}
			try
			{
			  Element node_content = getNodeContent(result_doc, node_id, lang);
				if (node_content != null)
				{
				  doc_node.appendChild(node_content);
				}
			}
			catch (GSException e)
			{
				GSXML.addError(result, e.getMessage());
				return result;
			}
		} // for each node
		return result;
	} // processDocumentContentRetrieve

	/**
	 * create an element to go into the structure. A node element has the form
	 * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy'/>
	 */
  protected Element createDocNode(Document doc, String node_id)
	{
	  return this.gs_doc.createDocNode(doc, node_id);
	}

	/**
	 * adds all the children of doc_id the the doc element, and if
	 * recursive=true, adds all their children as well
	 */
	protected void addDescendants(Element doc, String doc_id, boolean recursive)
	{
	  this.gs_doc.addDescendants(doc, doc_id, recursive);
	}

	/**
	 * adds all the siblings of current_id to the parent element. returns the
	 * new current element
	 */
	protected Element addSiblings(Element parent_node, String parent_id, String current_id)
	{
	  return this.gs_doc.addSiblings(parent_node, parent_id, current_id);
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
	protected ArrayList<String> getSiblingIds(String node_id)
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
	return this.gs_doc.getNodeType(node_id, doc_type);
	}

	/**
	 * if id ends in .fc, .pc etc, then translate it to the correct id 
	 * default implementation: just remove the suffix
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

	protected String getGreenstoneIdFromHref(Element doc_node)
	{
		String node_id = doc_node.getAttribute(GSXML.HREF_ID_ATT);
		node_id = translateExternalId(node_id);
		if (node_id == null)
		{
			return node_id;
		}
		// check for id modifiers
		String id_mods = doc_node.getAttribute(GSXML.ID_MOD_ATT);
		if (!id_mods.equals(""))
		{
			node_id = node_id + id_mods;
		}
		return node_id;
	}

	/**
	 * returns the document type of the doc that the specified node belongs to.
	 * should be one of GSXML.DOC_TYPE_SIMPLE, GSXML.DOC_TYPE_PAGED,
	 * GSXML.DOC_TYPE_HIERARCHY
	 */
	protected String getDocType(String node_id)
	{
		return this.gs_doc.getDocType(node_id);
	}

	/**
	 * returns the id of the root node of the document containing node node_id.
	 * may be the same as node_id 
	 */
	protected String getRootId(String node_id)
	{
		return  this.gs_doc.getRootId(node_id);
	}

	/**
	 * returns a list of the child ids in order, null if no children 
	 */
	protected ArrayList<String> getChildrenIds(String node_id)
	{
	  return this.gs_doc.getChildrenIds(node_id);
	}

	/**
	 * returns the node id of the parent node, null if no parent 
	 */
	protected String getParentId(String node_id)
	{
	  return this.gs_doc.getParentId(node_id);
	}

	/**
	 * get the metadata for the doc node doc_id returns a metadataList element:
	 * <metadataList><metadata name="xxx">value</metadata></metadataList>
	 */
  abstract protected Element getMetadataList(Document doc, String doc_id, boolean all_metadata, ArrayList<String> metadata_names, String lang) throws GSException;

	/**
	 * returns the content of a node should return a nodeContent element:
	 * <nodeContent>text content or other elements</nodeContent> can return
	 */
  abstract protected Element getNodeContent(Document doc, String doc_id, String lang) throws GSException;

	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS
	 */
  protected String getStructureInfo(String doc_id, String info_type) {

    return this.gs_doc.getStructureInfo(doc_id, info_type);
  }


}
