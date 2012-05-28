/*
 *    AbstractBrowse.java
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
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.MacroResolver;
import org.greenstone.gsdl3.util.OID;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// General Java classes
import java.util.ArrayList;

import org.apache.log4j.*;

/**
 * Partially implements a generic classifier service
 * 
 */
public abstract class AbstractBrowse extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractBrowse.class.getName());

	// the browsing services
	private static final String CLASSIFIER_SERVICE = "ClassifierBrowse";
	private static final String CLASSIFIER_METADATA_SERVICE = "ClassifierBrowseMetadataRetrieve";

	// do we want to keep info request?

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

	protected Element config_info = null; // the xml from the config file

	protected MacroResolver macro_resolver = null;

	protected String default_document_type = null;

	/** constructor */
	protected AbstractBrowse()
	{
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring AbstractBrowse...");
		this.config_info = info;
		if (macro_resolver != null)
		{
			macro_resolver.setSiteDetails(this.site_http_address, this.cluster_name, this.library_name);
			// set up the macro resolver
			Element replacement_elem = (Element) GSXML.getChildByTagName(extra_info, GSXML.REPLACE_ELEM + GSXML.LIST_MODIFIER);
			if (replacement_elem != null)
			{
				macro_resolver.addMacros(replacement_elem);
			}

		}

		// check that there are classifiers specified
		Element class_list = (Element) GSXML.getChildByTagName(info, GSXML.CLASSIFIER_ELEM + GSXML.LIST_MODIFIER);
		if (class_list == null)
		{
			// no classifiers specified
			return false;
		}

		// get the display and format elements from the coll config file for
		// the classifiers
		extractExtraClassifierInfo(info, extra_info);

		// short_service_info_ - the browse one
		Element cb_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		cb_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_BROWSE);
		cb_service.setAttribute(GSXML.NAME_ATT, CLASSIFIER_SERVICE);
		this.short_service_info.appendChild(cb_service);

		// metadata retrieval for the browsing  
		Element cbmr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		cbmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		cbmr_service.setAttribute(GSXML.NAME_ATT, CLASSIFIER_METADATA_SERVICE);
		this.short_service_info.appendChild(cbmr_service);

		// the format info
		Element cb_format_info = this.doc.createElement(GSXML.FORMAT_ELEM);
		boolean format_found = false;

		// try the default format first
		Element def_format = (Element) GSXML.getChildByTagName(info, GSXML.FORMAT_ELEM);
		if (def_format != null)
		{
			cb_format_info.appendChild(GSXML.duplicateWithNewName(this.doc, def_format, GSXML.DEFAULT_ELEM, true));
			format_found = true;
		}

		// add in to the description a simplified list of classifiers
		NodeList classifiers = class_list.getElementsByTagName(GSXML.CLASSIFIER_ELEM);
		for (int i = 0; i < classifiers.getLength(); i++)
		{
			Element cl = (Element) classifiers.item(i);
			Element new_cl = (Element) this.doc.importNode(cl, false); // just import this node, not the children

			// get the format info out, and put inside a classifier element
			Element format_cl = (Element) new_cl.cloneNode(false);
			Element format = (Element) GSXML.getChildByTagName(cl, GSXML.FORMAT_ELEM);
			if (format != null)
			{

				//copy all the children
				NodeList elems = format.getChildNodes();
				for (int j = 0; j < elems.getLength(); j++)
				{
					format_cl.appendChild(this.doc.importNode(elems.item(j), true));
				}
				cb_format_info.appendChild(format_cl);
				format_found = true;
			}

		}

		if (format_found)
		{
			this.format_info_map.put(CLASSIFIER_SERVICE, cb_format_info);
		}

		// look for document display format - is there a default doc type??
		String path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
		Element display_format = (Element) GSXML.getNodeByPath(extra_info, path);
		if (display_format != null)
		{
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

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{

		if (service_id.equals(CLASSIFIER_SERVICE))
		{

			Element class_list = (Element) GSXML.getChildByTagName(this.config_info, GSXML.CLASSIFIER_ELEM + GSXML.LIST_MODIFIER);
			if (class_list == null)
			{
				// no classifiers specified
				return null;
			}

			Element cb_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			cb_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_BROWSE);
			cb_service.setAttribute(GSXML.NAME_ATT, CLASSIFIER_SERVICE);
			cb_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(CLASSIFIER_SERVICE + ".name", lang)));
			cb_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(CLASSIFIER_SERVICE + ".description", lang)));

			Element cl_list = this.doc.createElement(GSXML.CLASSIFIER_ELEM + GSXML.LIST_MODIFIER);
			cb_service.appendChild(cl_list);
			NodeList classifiers = class_list.getElementsByTagName(GSXML.CLASSIFIER_ELEM);
			for (int i = 0; i < classifiers.getLength(); i++)
			{
				Element cl = (Element) classifiers.item(i);
				Element new_cl = (Element) this.doc.importNode(cl, false); // just import this node, not the children

				//String content = cl.getAttribute(GSXML.CLASSIFIER_CONTENT_ATT);

				//get the classify title  from the database
				String class_id = cl.getAttribute(GSXML.NAME_ATT);
				String content = getMetadata(class_id, "Title");

				cl_list.appendChild(new_cl);
				String text = GSXML.getDisplayText(cl, GSXML.DISPLAY_TEXT_NAME, lang, "en");
				if (text == null || text.equals(""))
				{
					// no display element was specified, use the metadata name
					// for now this looks in the class properties file
					// this needs to use a general metadata thing instead
					text = getMetadataNameText(content + ".buttonname", lang);
				}
				if (text == null)
				{
					text = content;
				}

				Element cl_name = GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, text);
				new_cl.appendChild(cl_name);

				// description

				String meta_name = getMetadataNameText(content, lang);
				if (meta_name == null)
				{
					meta_name = content;
				}
				String[] array = { meta_name };
				String description = getTextString("ClassifierBrowse.classifier_help", lang, array);
				Element cl_desc = GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, description);
				new_cl.appendChild(cl_desc);

			}
			return cb_service;
		}

		// these ones are probably never called, but put them here just in case

		if (service_id.equals(CLASSIFIER_METADATA_SERVICE))
		{

			Element cbmr_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			cbmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			cbmr_service.setAttribute(GSXML.NAME_ATT, CLASSIFIER_METADATA_SERVICE);
			return cbmr_service;
		}

		return null;
	}

	/**
	 * this looks for any classifier specific display or format info from
	 * extra_info and adds it in to the correct place in info
	 */
	static boolean extractExtraClassifierInfo(Element info, Element extra_info)
	{

		if (extra_info == null)
		{
			return false;
		}

		Document owner = info.getOwnerDocument();
		// so far we have display and format elements that we need for classifiers
		NodeList classifiers = info.getElementsByTagName(GSXML.CLASSIFIER_ELEM);
		Element config_browse = (Element) GSXML.getChildByTagName(extra_info, GSXML.BROWSE_ELEM);

		for (int i = 0; i < classifiers.getLength(); i++)
		{
			Element cl = (Element) classifiers.item(i);
			String name = cl.getAttribute(GSXML.NAME_ATT);

			//Element node_extra = GSXML.getNamedElement(config_browse,
			//					       GSXML.CLASSIFIER_ELEM,
			//					       GSXML.NAME_ATT,
			//					       name);
			//now use the position to get the node - CL1 
			// assumes the same number of classifiers in collectionCOnfig as in buildConfig
			//            int position = Integer.parseInt(name.substring(2));

			Element node_extra = null;
			NodeList cls = config_browse.getElementsByTagName(GSXML.CLASSIFIER_ELEM);
			//if (position >0 && position <= cls.getLength()) {
			//    node_extra  = (Element) cls.item((position -1)); 
			//}
			if (i < cls.getLength())
			{
				node_extra = (Element) cls.item(i);
			}

			if (node_extra == null)
			{
				logger.error("GS2REtrieve: haven't found extra info for classifier named " + name);
				continue;
			}

			// get the display elements if any - displayName
			NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
			if (display_names != null)
			{
				Element display = owner.createElement(GSXML.DISPLAY_ELEM);
				for (int j = 0; j < display_names.getLength(); j++)
				{
					Element e = (Element) display_names.item(j);
					cl.appendChild(owner.importNode(e, true));

				}
			}

			// get the format element if any 
			Element format = (Element) GSXML.getChildByTagName(node_extra, GSXML.FORMAT_ELEM);
			if (format == null)
			{ // try a generic one that applies to all classifiers
				format = (Element) GSXML.getChildByTagName(extra_info, GSXML.FORMAT_ELEM);
			}
			if (format != null)
			{ // append to index info
				cl.appendChild(owner.importNode(format, true));
			}
		} // for each classifier

		// now check for default format info
		Element default_format = (Element) GSXML.getChildByTagName(config_browse, GSXML.FORMAT_ELEM);
		if (default_format != null)
		{ // append to  info
			info.appendChild(owner.importNode(default_format, true));
		}

		return true;
	}

	protected Element processClassifierBrowse(Element request)
	{

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, CLASSIFIER_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		Element query_node_list = (Element) GSXML.getChildByTagName(request, GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (query_node_list == null)
		{
			logger.error(" ClassifierBrowse request specified no doc nodes.\n");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error(" ClassifierBrowse request had no paramList.");
			return result; // Return the empty result
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

		// the node list to hold the results
		Element node_list = this.doc.createElement(GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(node_list);

		// Get the nodes
		String[] node_ids = GSXML.getAttributeValuesFromList(query_node_list, GSXML.NODE_ID_ATT);
		for (int i = 0; i < node_ids.length; i++)
		{
			// Add the document to the list
			Element node = this.doc.createElement(GSXML.CLASS_NODE_ELEM);
			node_list.appendChild(node);

			String node_id = node_ids[i];
			node.setAttribute(GSXML.NODE_ID_ATT, node_id);

			if (idNeedsTranslating(node_id))
			{
				node_id = translateId(node_id);
				if (node_id == null)
				{
					continue;
				}
				node.setAttribute(GSXML.NODE_ID_ATT, node_id);
			}

			if (want_info)
			{
				Element node_info_elem = this.doc.createElement("nodeStructureInfo");
				node.appendChild(node_info_elem);

				for (int j = 0; j < info_types.size(); j++)
				{
					String info_type = info_types.get(j);
					String info_value = getStructureInfo(node_id, info_type);
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
				node.appendChild(structure_elem);

				if (want_entire_structure)
				{
					String root_id = getRootId(node_id);
					Element root_node = createClassifierNode(root_id); //, true, false);
					addDescendants(root_node, root_id, true);
					structure_elem.appendChild(root_node);
					continue; // with the next document, we dont need to do any more here
				}

				// Add the requested structure information
				Element base_node = createClassifierNode(node_id); //, false, false);

				//Ancestors: continually add parent nodes until the root is reached
				Element top_node = base_node; // the top node so far
				if (want_ancestors)
				{
					String current_id = node_id;
					while (true)
					{
						String parent_id = getParentId(current_id);
						//Element parent = getParent(current_id);
						if (parent_id == null)
							break; // no parent
						Element parent_node = createClassifierNode(parent_id);
						parent_node.appendChild(top_node);
						current_id = parent_id;//.getAttribute(GSXML.NODE_ID_ATT);
						top_node = parent_node;
					}
				}
				// Parent: get the parent of the selected node
				else if (want_parent)
				{
					String parent_id = getParentId(node_id);
					if (parent_id != null)
					{
						Element parent_node = createClassifierNode(parent_id);
						parent_node.appendChild(base_node);
						top_node = parent_node;
					}
				}

				// now the top node is the root of the structure
				structure_elem.appendChild(top_node);

				//Siblings: get the other descendants of the selected node's parent
				if (want_siblings)
				{

					Element parent_node = (Element) base_node.getParentNode(); // this may be the structure element if there has been no request for parents or ancestors
					String parent_id = getParentId(node_id);
					// add siblings, - returns a pointer to the new current node 
					base_node = addSiblings(parent_node, parent_id, node_id);
				}

				// Children: get the descendants, but only one level deep
				if (want_children)
				{
					addDescendants(base_node, node_id, false);
				}
				// Descendants: recursively get every descendant 
				else if (want_descendants)
				{
					addDescendants(base_node, node_id, true);
				}

				NodeList classifierElements = result.getElementsByTagName(GSXML.CLASS_NODE_ELEM);
				for (int j = 0; j < classifierElements.getLength(); j++)
				{
					Element current = (Element) classifierElements.item(j);
					Node parentNode = current.getParentNode();

					if (parentNode == null)
					{
						continue;
					}

					Element parent = (Element) parentNode;
					String childType = parent.getAttribute(GSXML.CHILD_TYPE_ATT);
					if (childType == null || childType.length() == 0)
					{
						continue;
					}

					current.setAttribute(GSXML.CLASSIFIER_STYLE_ATT, childType);
				}
			} // if want structure
		} // for each doc
		return result;
	}

	protected Element processClassifierBrowseMetadataRetrieve(Element request)
	{

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		result.setAttribute(GSXML.FROM_ATT, CLASSIFIER_METADATA_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("AbstractBrowse, ClassifierBrowseMetadataRetrieve Error: missing paramList.\n");
			return result; // Return the empty result
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
					break;
				}
				metadata_names_list.add(metadata);
			}
			param = (Element) param.getNextSibling();
		}

		Element node_list = this.doc.createElement(GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(node_list);

		// Get the nodes
		Element request_node_list = (Element) GSXML.getChildByTagName(request, GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		if (request_node_list == null)
		{
			logger.error(" ClassifierBrowseMetadataRetrieve request had no " + GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
			return result;
		}

		NodeList request_nodes = request_node_list.getChildNodes();
		for (int i = 0; i < request_nodes.getLength(); i++)
		{
			Element request_node = (Element) request_nodes.item(i);
			String node_id = request_node.getAttribute(GSXML.NODE_ID_ATT);

			// Add the document to the results list
			Element new_node = (Element) this.doc.importNode(request_node, false);
			node_list.appendChild(new_node);

			if (idNeedsTranslating(node_id))
			{
				node_id = translateId(node_id);
			}
			if (node_id == null)
			{
				continue;
			}

			Element metadata_list = getMetadataList(node_id, all_metadata, metadata_names_list);
			new_node.appendChild(metadata_list);
		}

		return result;
	}

	/** Creates a classifier node */
	protected Element createClassifierNode(String node_id)
	{
		Element node = this.doc.createElement(GSXML.CLASS_NODE_ELEM);
		node.setAttribute(GSXML.NODE_ID_ATT, node_id);
		node.setAttribute(GSXML.CHILD_TYPE_ATT, getChildType(node_id));
		return node;
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
	 * adds all the children of doc_id the the doc element, and if
	 * recursive=true, adds all their children as well
	 */
	protected void addDescendants(Element node, String node_id, boolean recursive)
	{
		ArrayList<String> child_ids = getChildrenIds(node_id);
		if (child_ids == null)
			return;
		for (int i = 0; i < child_ids.size(); i++)
		{
			String child_id = child_ids.get(i);
			Element child_elem;
			if (isDocumentId(child_id))
			{
				child_elem = createDocNode(child_id);
			}
			else
			{
				child_elem = createClassifierNode(child_id);
			}
			node.appendChild(child_elem);
			if (recursive)
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
	protected boolean idNeedsTranslating(String node_id)
	{
		return OID.needsTranslating(node_id);
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

	/** if id ends in .fc, .pc etc, then translate it to the correct id */
	abstract protected String translateId(String node_id);

	/** Gets the type of list a classifier is (e.g. VList or HList) */
	abstract protected String getChildType(String node_id);

	/**
	 * returns the document type of the doc that the specified node belongs to.
	 * should be one of GSXML.DOC_TYPE_SIMPLE, GSXML.DOC_TYPE_PAGED,
	 * GSXML.DOC_TYPE_HIERARCHY
	 */
	abstract protected String getDocType(String node_id);

	/**
	 * returns the id of the root node of the document containing node node_id.
	 * . may be the same as node_id
	 */
	abstract protected String getRootId(String node_id);

	/** returns a list of the child ids in order, null if no children */
	abstract protected ArrayList<String> getChildrenIds(String node_id);

	/** returns the node id of the parent node, null if no parent */
	abstract protected String getParentId(String node_id);

	/**
	 * returns true if the id refers to a document (rather than a classifier
	 * node)
	 */
	abstract protected boolean isDocumentId(String node_id);

	/**
	 * get the metadata for the classifier node node_id returns a metadataList
	 * element: <metadataList><metadata
	 * name="xxx">value</metadata></metadataList> if all_metadata is true,
	 * returns all available metadata, otherwise just returns requested metadata
	 */
	abstract protected Element getMetadataList(String node_id, boolean all_metadata, ArrayList<String> metadata_names);

	/**
	 * get the particular metadata (identified by the metadata name) for the
	 * classifier node node_id
	 * 
	 */
	abstract protected String getMetadata(String node_id, String metadata_name);

	/**
	 * returns the structural information asked for. info_type may be one of
	 * INFO_NUM_SIBS, INFO_NUM_CHILDREN, INFO_SIB_POS
	 */
	abstract protected String getStructureInfo(String node_id, String info_type);

}
