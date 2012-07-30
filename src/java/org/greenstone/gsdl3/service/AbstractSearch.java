/*
 *    AbstractSearch.java
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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.AbstractBasicDocument;
import org.greenstone.gsdl3.util.BasicDocument;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Partially implements a generic search service
 * 
 */

public abstract class AbstractSearch extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractSearch.class.getName());

	// the search service
	protected String QUERY_SERVICE = null; // set by subclass 

	// compulsory params
	protected static final String INDEX_PARAM = "index";
	protected static final String QUERY_PARAM = "query";
	protected static final String RAW_PARAM = "rawquery";

	// optional standard params - some of these have to be implemented
	protected static final String MAXDOCS_PARAM = "maxDocs";
	protected static final String HITS_PER_PAGE_PARAM = "hitsPerPage";
	protected static final String START_PAGE_PARAM = "startPage";

	protected AbstractBasicDocument gs_doc = null;

	/** can more than one index be searched at the same time? */
	protected boolean does_multi_index_search = false;
	/** does this service support paging of results? */
	protected boolean does_paging = false;
	/** does this service support asking for a subset of results? */
	protected boolean does_chunking = false;
	/** does this service support faceting search results */
	protected boolean does_faceting = false;
	/**
	 * the default document type - use if all documents are the same type
	 */
	protected String default_document_type = null;
	/**
	 * the default index, or comma separated list if more than one is the
	 * default (with start and end commas, eg ,TI,SU,). Should be set by
	 * configure()
	 */
	protected String default_index = "";

	protected String default_max_docs = "100";

	protected String default_hits_per_page = "10";

	public AbstractSearch()
	{
	}

	/**
	 * Sets up the short service info for service by QUERY_SERVICE (e.g.
	 * TextQuery or AudioQuery) If other services will be provided, should be
	 * added in the subclass configure also looks for search format info, and
	 * document format info
	 */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring AbstractSearch...");

		this.config_info = info;

		// set up short_service_info_ 
		// => for now just has id and type. the name (lang dependent)
		//    will be added in if the list is requested.

		Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE);
		this.short_service_info.appendChild(tq_service);

		// add some format info to service map if there is any 
		// => lookin extra info first look in buildConfig

		Element format = (Element) GSXML.getChildByTagName(info, GSXML.FORMAT_ELEM);

		if (format == null)
		{
			String path = GSPath.appendLink(GSXML.SEARCH_ELEM, GSXML.FORMAT_ELEM);

			// Note by xiao: instead of retrieving the first 'format'
			//   element inside the 'search' element, we are trying to
			//   find the real format element which has at least one
			//   'gsf:template' child element. (extra_info is
			//   collectionConfig.xml)

			//format = (Element) GSXML.getNodeByPath(extra_info, path);

			Element search_elem = (Element) GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);
			NodeList format_elems = null;
			if (search_elem != null)
			{
				format_elems = search_elem.getElementsByTagName(GSXML.FORMAT_ELEM);
			}
			for (int i = 0; i < format_elems.getLength(); i++)
			{
				format = (Element) format_elems.item(i);
				if (format.getElementsByTagName("gsf:template").getLength() != 0)
				{
					break;
				}
			}
		}//end of if(format==null)
			//
		if (format != null)
		{
			this.format_info_map.put(QUERY_SERVICE, this.doc.importNode(format, true));
		}

		// look for document display format - for documentType
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

		// Base line for document (might be overriden by sub-classes)
		gs_doc = new BasicDocument(this.doc, this.default_document_type);

		return true;
	}

	/**
	 * returns a basic description for QUERY_SERVICE. If a subclass provides
	 * other services they need to provide their own descriptions
	 */
	protected Element getServiceDescription(String service, String lang, String subset)
	{
		if (!service.equals(QUERY_SERVICE))
		{
			return null;
		}

		Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE);
		if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
		{
			tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getServiceName(QUERY_SERVICE, lang)));
			tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getServiceSubmit(QUERY_SERVICE, lang)));
			tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getServiceDescription(QUERY_SERVICE, lang)));
		}
		if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
		{
			Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			addCustomQueryParams(param_list, lang);
			addStandardQueryParams(param_list, lang);
			tq_service.appendChild(param_list);
		}
		return tq_service;

	}

	// perhaps these should be changed to search down the class hierarchy for 
	// values - do can just put the info in the resource bundle to use it
	/** returns the default name for the TextQuery service */
	protected String getServiceName(String service_id, String lang)
	{
		return getTextString(service_id + ".name", lang);
	}

	/** returns the default description for the TextQuery service */
	protected String getServiceDescription(String service_id, String lang)
	{
		return getTextString(service_id + ".description", lang);
	}

	/** returns the default submit button text for the TextQuery service */
	protected String getServiceSubmit(String service_id, String lang)
	{
		return getTextString(service_id + ".submit", lang);

	}

	/** adds the standard query params into the service description */
	protected void addStandardQueryParams(Element param_list, String lang)
	{
		// this test is not so good. here we are using absence of default index
		// to determine whether we have indexes or not. But in other places,
		// absence of default index just means to use the first one as default.
		if (!default_index.equals(""))
		{
			createParameter(INDEX_PARAM, param_list, lang);
		}
		if (does_chunking)
		{
			createParameter(MAXDOCS_PARAM, param_list, lang);
		}
		if (does_paging)
		{
			createParameter(HITS_PER_PAGE_PARAM, param_list, lang);
			createParameter(START_PAGE_PARAM, param_list, lang);
		}
		createParameter(QUERY_PARAM, param_list, lang);
	}

	/**
	 * adds any service specific query params into the service default
	 * implementation: add nothing. subclasses may need to override this to add
	 * in their specific parameters
	 */
	protected void addCustomQueryParams(Element param_list, String lang)
	{
		// default behaviour, do nothing
	}

	protected void createParameter(String name, Element param_list, String lang)
	{
		createParameter(name, param_list, lang, null);
	}

	protected void createParameter(String name, Element param_list, String lang, String default_value)
	{
		// at this level, not interested in boolean return type
		createParameterChain(name, param_list, lang, default_value);
	}

	/**
	 * default implementations for the standard parameters plus some other
	 * common ones index, maxDocs, hitsPerPage, startPage
	 */

	protected boolean createParameterChain(String name, Element param_list, String lang, String default_value)
	{
		Element param = null;
		String param_default = default_value;

		if (name.equals(QUERY_PARAM) || name.equals(RAW_PARAM))
		{
			param = GSXML.createParameterDescription(this.doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_STRING, param_default, null, null);
			param_list.appendChild(param);
			return true;
		}
		else if (name.equals(INDEX_PARAM))
		{
			// should we make these class fields?
			ArrayList<String> index_ids = new ArrayList<String>();
			ArrayList<String> index_names = new ArrayList<String>();
			getIndexData(index_ids, index_names, lang);
			String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
			if (does_multi_index_search)
			{
				param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
			}
			if (param_default == null)
			{
				param_default = this.default_index;
			}
			param = GSXML.createParameterDescription2(this.doc, INDEX_PARAM, getTextString("param." + INDEX_PARAM, lang), param_type, param_default, index_ids, index_names);
			param_list.appendChild(param);
			return true;
		}
		else if (name.equals(MAXDOCS_PARAM))
		{
			if (param_default == null)
			{
				param_default = this.default_max_docs;
			}

			param = GSXML.createParameterDescription(this.doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_INTEGER, param_default, null, null);
			param_list.appendChild(param);
			return true;
		}
		else if (name.equals(HITS_PER_PAGE_PARAM))
		{
			if (param_default == null)
			{
				param_default = this.default_hits_per_page;
			}

			param = GSXML.createParameterDescription(this.doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_INTEGER, param_default, null, null);
			param_list.appendChild(param);
			return true;
		}
		else if (name.equals(START_PAGE_PARAM))
		{
			if (param_default == null)
			{
				param_default = "1";
			}

			// start page - set to 1 for the search page
			param = GSXML.createParameterDescription(this.doc, START_PAGE_PARAM, "", GSXML.PARAM_TYPE_INVISIBLE, param_default, null, null);
			param_list.appendChild(param);
			return true;
		}

		// Get to there then none of the above params matched
		// => return false so the chain can continue
		return false;
	}

	/**
	 * create an element to go into the search results list. A node element has
	 * the form <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy'
	 * rank='0.23'/>
	 */
	protected Element createDocNode(String node_id, String rank)
	{
		return this.gs_doc.createDocNode(node_id, rank);
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
	 * returns the node type of the specified node. should be one of
	 * GSXML.NODE_TYPE_LEAF, GSXML.NODE_TYPE_INTERNAL, GSXML.NODE_TYPE_ROOT
	 */
	protected String getNodeType(String node_id, String doc_type)
	{
		return this.gs_doc.getNodeType(node_id, doc_type);
	}

	/** returns true if the node has child nodes */
	protected boolean hasChildren(String node_id)
	{
		return this.gs_doc.hasChildren(node_id);
	}

	/** returns true if the node has a parent */
	protected boolean hasParent(String node_id)
	{
		return this.gs_doc.hasParent(node_id);
	}

	/**
	 * get the details about the indexes available must be implemented by
	 * subclass there must be at least one index
	 */
	abstract protected void getIndexData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang);

}
