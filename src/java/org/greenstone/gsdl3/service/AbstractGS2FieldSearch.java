/*
 *    AbstractGS2FieldSearch.java
 *    Copyright (C) 2006 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *   the Free Software Foundation; either version 2 of the License, or
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
import org.w3c.dom.NodeList;

// java classes
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Vector;
import java.io.File;

import org.apache.log4j.*;

abstract public class AbstractGS2FieldSearch extends AbstractGS2TextSearch
{

	// extra services offered by mgpp collections
	protected static final String FIELD_QUERY_SERVICE = "FieldQuery";
	protected static final String ADVANCED_FIELD_QUERY_SERVICE = "AdvancedFieldQuery";

	// extra parameters used
	protected static final String LEVEL_PARAM = "level";
	protected static final String RANK_PARAM = "sortBy";
	protected static final String RANK_PARAM_RANK = "1";
	protected static final String RANK_PARAM_NONE = "0";
	protected static final String SIMPLE_FIELD_PARAM = "simpleField";
	protected static final String ADVANCED_FIELD_PARAM = "complexField";

	// more params for field query
	protected static final String FIELD_QUERY_PARAM = "fqv";
	protected static final String FIELD_STEM_PARAM = "fqs";
	protected static final String FIELD_CASE_PARAM = "fqc";
	protected static final String FIELD_ACCENT_PARAM = "fqa";
	protected static final String FIELD_FIELD_PARAM = "fqf";
	protected static final String FIELD_COMBINE_PARAM = "fqk";
	protected static final String FIELD_COMBINE_PARAM_AND = "0";
	protected static final String FIELD_COMBINE_PARAM_OR = "1";
	protected static final String FIELD_COMBINE_PARAM_NOT = "2";

	// some stuff for config files
	protected static final String SEARCH_TYPE_ELEM = "searchType";
	protected static final String SEARCH_TYPE_PLAIN = "plain";
	protected static final String SEARCH_TYPE_SIMPLE_FORM = "simpleform";
	protected static final String SEARCH_TYPE_ADVANCED_FORM = "advancedform";

	protected static final String DEFAULT_LEVEL_ELEM = "defaultLevel";
	protected static final String DEFAULT_DB_LEVEL_ELEM = "defaultDBLevel";
	protected static final String LEVEL_ELEM = "level";
	protected static final String FIELD_ATT = "field";

	protected static final int TEXT_QUERY = 0;
	protected static final int SIMPLE_QUERY = 1;
	protected static final int ADVANCED_QUERY = 2;

	protected String AND_OPERATOR = "&";
	protected String OR_OPERATOR = "|";
	protected String NOT_OPERATOR = "!";

	// the default level for searching
	protected String default_level = null;
	// default level for collection db
	protected String default_db_level = null;
	// which search services will we offer??
	protected boolean plain_search = false;
	protected boolean simple_form_search = false;
	protected boolean advanced_form_search = false;

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractGS2FieldSearch.class.getName());

	/** constructor */
	public AbstractGS2FieldSearch()
	{
	}

	public void cleanUp()
	{
		super.cleanUp();
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		// Get the default level out of <defaultLevel> (buildConfig.xml)
		Element def = (Element) GSXML.getChildByTagName(info, DEFAULT_LEVEL_ELEM);
		if (def != null)
		{
			this.default_level = def.getAttribute(GSXML.SHORTNAME_ATT);
		}
		if (this.default_level == null || this.default_level.equals(""))
		{
			logger.error("default level not specified!, assuming Doc");
			this.default_level = "Doc";
		}

		// Get the default DB level
		def = (Element) GSXML.getChildByTagName(info, DEFAULT_DB_LEVEL_ELEM);
		if (def != null)
		{
			this.default_db_level = def.getAttribute(GSXML.SHORTNAME_ATT);
		}
		if (this.default_db_level == null || this.default_db_level.equals(""))
		{
			logger.error("default database level (defaultDBLevel) not specified!, assuming Sec");
			this.default_db_level = "Sec";
		}

		// get stuff from from extra info (which is collectionConfig.xml)
		if (extra_info != null)
		{

			// the search element
			Element config_search = (Element) GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);

			NodeList search_types = config_search.getElementsByTagName(SEARCH_TYPE_ELEM);
			if (search_types == null)
			{
				// none specified, assume plain only
				this.plain_search = true;
			}
			else
			{
				for (int i = 0; i < search_types.getLength(); i++)
				{
					Element t = (Element) search_types.item(i);
					String type_name = t.getAttribute(GSXML.NAME_ATT);
					if (type_name.equals(SEARCH_TYPE_PLAIN))
					{
						this.plain_search = true;
					}
					else if (type_name.equals(SEARCH_TYPE_SIMPLE_FORM))
					{
						this.simple_form_search = true;
					}
					else if (type_name.equals(SEARCH_TYPE_ADVANCED_FORM))
					{
						this.advanced_form_search = true;
					}
				}
			}

			// AbstractGS2TextSearch has set up the TextQuery service, but we may not want it
			if (!this.plain_search)
			{
				// need to remove the TextQuery service
				Element tq_service = GSXML.getNamedElement(short_service_info, GSXML.SERVICE_ELEM, GSXML.NAME_ATT, QUERY_SERVICE);
				short_service_info.removeChild(tq_service);

			}

			Document owner = info.getOwnerDocument();

			NodeList levels = info.getElementsByTagName(GSXML.LEVEL_ELEM);

			for (int i = 0; i < levels.getLength(); i++)
			{
				Element lev = (Element) levels.item(i);
				String name = lev.getAttribute(GSXML.NAME_ATT);
				Element node_extra = GSXML.getNamedElement(config_search, GSXML.LEVEL_ELEM, GSXML.NAME_ATT, name);
				if (node_extra == null)
				{
					logger.error("haven't found extra info for level named " + name);
					continue;
				}

				// get the display elements if any - displayName
				NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
				if (display_names != null)
				{
					for (int j = 0; j < display_names.getLength(); j++)
					{
						Element e = (Element) display_names.item(j);
						lev.appendChild(owner.importNode(e, true));
					}
				}
			} // for each level
		}
		else
		{
			// for soem reason we don't have the collectionConfig file. assume plain only
			this.plain_search = true;
		}

		// the format info is the same for all services
		Element format_info = (Element) format_info_map.get(QUERY_SERVICE);

		// set up the extra services which are available for this collection
		if (this.simple_form_search)
		{
			// set up short_service_info_ - for now just has id and type - name will be added in on the fly
			Element fq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			fq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			fq_service.setAttribute(GSXML.NAME_ATT, FIELD_QUERY_SERVICE);
			this.short_service_info.appendChild(fq_service);

			if (format_info != null)
			{
				this.format_info_map.put(FIELD_QUERY_SERVICE, format_info);
			}
		}

		if (this.advanced_form_search)
		{
			Element afq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			afq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			afq_service.setAttribute(GSXML.NAME_ATT, ADVANCED_FIELD_QUERY_SERVICE);
			this.short_service_info.appendChild(afq_service);

			if (format_info != null)
			{
				this.format_info_map.put(ADVANCED_FIELD_QUERY_SERVICE, format_info);
			}
		}

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		// should we check that the service is actually on offer? presumably we wont get asked for services that we haven't advertised previously.

		if (!service_id.equals(FIELD_QUERY_SERVICE) && !service_id.equals(ADVANCED_FIELD_QUERY_SERVICE))
		{
			return super.getServiceDescription(service_id, lang, subset);
		}

		Element service = this.doc.createElement(GSXML.SERVICE_ELEM);
		service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		service.setAttribute(GSXML.NAME_ATT, service_id);
		if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
		{
			service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(service_id + ".name", lang)));
			service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(service_id + ".submit", lang)));
			service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(service_id + ".description", lang)));

		}
		if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
		{
			Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			service.appendChild(param_list);
			if (service_id.equals(FIELD_QUERY_SERVICE))
			{

				addCustomQueryParams(param_list, lang);
				createParameter(MAXDOCS_PARAM, param_list, lang);
				if (!default_index_subcollection.equals(""))
				{
					createParameter(INDEX_SUBCOLLECTION_PARAM, param_list, lang);
				}
				if (!default_index_language.equals(""))
				{
					createParameter(INDEX_LANGUAGE_PARAM, param_list, lang);
				}
				// create a multi param for the fields etc
				// text box, field
				Element multiparam = null;
				Element param = null;
				multiparam = GSXML.createParameterDescription(this.doc, SIMPLE_FIELD_PARAM, "", GSXML.PARAM_TYPE_MULTI, null, null, null);
				multiparam.setAttribute("occurs", "4");
				param_list.appendChild(multiparam);

				// the components

				createParameter(FIELD_QUERY_PARAM, multiparam, lang);
				createParameter(FIELD_FIELD_PARAM, multiparam, lang);

			}
			else
			{
				createParameter(LEVEL_PARAM, param_list, lang);
				createParameter(RANK_PARAM, param_list, lang);
				createParameter(MAXDOCS_PARAM, param_list, lang);
				if (!default_index_subcollection.equals(""))
				{
					createParameter(INDEX_SUBCOLLECTION_PARAM, param_list, lang);
				}
				if (!default_index_language.equals(""))
				{
					createParameter(INDEX_LANGUAGE_PARAM, param_list, lang);
				}

				// create a multi param for the fields etc
				// text box, stem, case, field

				Element multiparam = null;
				Element param = null;

				multiparam = GSXML.createParameterDescription(this.doc, ADVANCED_FIELD_PARAM, "", GSXML.PARAM_TYPE_MULTI, null, null, null);
				multiparam.setAttribute("occurs", "4");
				param_list.appendChild(multiparam);

				createParameter(FIELD_COMBINE_PARAM, multiparam, lang);
				createParameter(FIELD_QUERY_PARAM, multiparam, lang);
				if (this.does_case)
				{
					createParameter(FIELD_CASE_PARAM, multiparam, lang);
				}
				if (this.does_stem)
				{
					createParameter(FIELD_STEM_PARAM, multiparam, lang);
				}
				if (this.does_accent)
				{
					createParameter(FIELD_ACCENT_PARAM, multiparam, lang);
				}
				createParameter(FIELD_FIELD_PARAM, multiparam, lang);

			}
		}
		return service;

	}

	/** add in the level params to TextQuery */
	protected void addCustomQueryParams(Element param_list, String lang)
	{
		createParameter(LEVEL_PARAM, param_list, lang);
		super.addCustomQueryParams(param_list, lang);
	}

	/** create a param and add to the list */
	protected void createParameter(String name, Element param_list, String lang)
	{
		Element param = null;
		if (name.equals(LEVEL_PARAM))
		{
			ArrayList level_ids = new ArrayList();
			ArrayList level_names = new ArrayList();
			getLevelData(level_ids, level_names, lang);
			if (level_ids.size() > 1)
			{
				// the first one is the default
				//param = GSXML.createParameterDescription2(this.doc, LEVEL_PARAM, getTextString("param."+LEVEL_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, (String)level_ids.get(0), level_ids, level_names);
				param = GSXML.createParameterDescription2(this.doc, LEVEL_PARAM, getTextString("param." + LEVEL_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, this.default_level, level_ids, level_names);
			}
			else
			{
				// we need to set the level, but hidden, in case there is an invalid level saved
				//param = GSXML.createParameterDescription(this.doc, LEVEL_PARAM, "", GSXML.PARAM_TYPE_INVISIBLE, (String)level_ids.get(0), null, null);
				param = GSXML.createParameterDescription(this.doc, LEVEL_PARAM, "", GSXML.PARAM_TYPE_INVISIBLE, this.default_level, null, null);
			}
		}
		else if (name.equals(RANK_PARAM))
		{
			String[] vals1 = { RANK_PARAM_RANK, RANK_PARAM_NONE };
			String[] vals1_texts = { getTextString("param." + RANK_PARAM + "." + RANK_PARAM_RANK, lang), getTextString("param." + RANK_PARAM + "." + RANK_PARAM_NONE, lang) };

			param = GSXML.createParameterDescription(this.doc, RANK_PARAM, getTextString("param." + RANK_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, RANK_PARAM_RANK, vals1, vals1_texts);

		}
		else if (name.equals(FIELD_QUERY_PARAM))
		{
			param = GSXML.createParameterDescription(this.doc, FIELD_QUERY_PARAM, getTextString("param." + FIELD_QUERY_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);

		}
		else if (name.equals(FIELD_CASE_PARAM) || name.equals(FIELD_STEM_PARAM) || name.equals(FIELD_ACCENT_PARAM))
		{
			String[] bool_ops = { "0", "1" };
			String[] bool_texts = { getTextString("param.boolean.off", lang, "AbstractTextSearch"), getTextString("param.boolean.on", lang, "AbstractTextSearch") };
			param = GSXML.createParameterDescription(this.doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_BOOLEAN, BOOLEAN_PARAM_ON, bool_ops, bool_texts);

		}
		else if (name.equals(FIELD_FIELD_PARAM))
		{
			ArrayList fields = new ArrayList();
			ArrayList field_names = new ArrayList();
			getIndexData(fields, field_names, lang);
			// the field list -  read from config file

			// Fix for http://trac.greenstone.org/ticket/245 "java crash, index out of bounds"
			// org.greenstone.gsdl3.service.AbstractGS2FieldSearch.createParameter(AbstractGS2FieldSearch.java:362)
			// Changed from:
			// param = GSXML.createParameterDescription2(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, (String)fields.get(0), fields, field_names );
			String default_value = (fields.size() > 0) ? (String) fields.get(0) : null;
			// don't want to access element 0 if fields.size()==0, and
			// GSXML.createParameterDescription2 checks for default_value==null condition
			param = GSXML.createParameterDescription2(this.doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, default_value, fields, field_names);

		}
		else if (name.equals(FIELD_COMBINE_PARAM))
		{

			String[] vals = { FIELD_COMBINE_PARAM_AND, FIELD_COMBINE_PARAM_OR, FIELD_COMBINE_PARAM_NOT };
			String[] val_texts = { getTextString("param." + FIELD_COMBINE_PARAM + "." + FIELD_COMBINE_PARAM_AND, lang), getTextString("param." + FIELD_COMBINE_PARAM + "." + FIELD_COMBINE_PARAM_OR, lang), getTextString("param." + FIELD_COMBINE_PARAM + "." + FIELD_COMBINE_PARAM_NOT, lang) };

			param = GSXML.createParameterDescription(this.doc, FIELD_COMBINE_PARAM, "", GSXML.PARAM_TYPE_ENUM_SINGLE, FIELD_COMBINE_PARAM_AND, vals, val_texts);
			param.setAttribute(GSXML.PARAM_IGNORE_POS_ATT, "0");
		}

		if (param != null)
		{
			param_list.appendChild(param);
		}
		else
		{
			super.createParameter(name, param_list, lang);
		}
	}

	// should cache some of this
	protected void getLevelData(ArrayList level_ids, ArrayList level_names, String lang)
	{
		Element level_list = (Element) GSXML.getChildByTagName(this.config_info, LEVEL_ELEM + GSXML.LIST_MODIFIER);
		NodeList levels = level_list.getElementsByTagName(LEVEL_ELEM);
		for (int i = 0; i < levels.getLength(); i++)
		{
			Element level = (Element) levels.item(i);
			String shortname = level.getAttribute(GSXML.SHORTNAME_ATT);
			if (shortname.equals(""))
			{
				continue;
			}
			level_ids.add(shortname);
			String display_name = GSXML.getDisplayText(level, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			if (display_name.equals(""))
			{
				// we'll use the name, and the dictionary
				display_name = level.getAttribute(GSXML.NAME_ATT);
				if (display_name.equals(""))
				{
					display_name = shortname;
				}
				else
				{
					display_name = getTextString("level." + display_name, lang);
				}
			}
			level_names.add(display_name);
		}
	}

	// the following three functions are needed so the base class can 
	// call the process+SERVICE_NAME methods
	/** process a text query */
	protected Element processTextQuery(Element request)
	{
		return processAnyQuery(request, TEXT_QUERY);
	}

	/** process a field query */
	protected Element processFieldQuery(Element request)
	{
		return processAnyQuery(request, SIMPLE_QUERY);
	}

	/** process an advanced field query */
	protected Element processAdvancedFieldQuery(Element request)
	{
		return processAnyQuery(request, ADVANCED_QUERY);
	}

	/** process a query */
	protected Element processAnyQuery(Element request, int query_type)
	{
		String service_name = null;
		String empty_query_test_param = null;
		// set up the type specific bits
		switch (query_type)
		{
		case TEXT_QUERY:
			service_name = QUERY_SERVICE;
			empty_query_test_param = QUERY_PARAM;
			break;
		case SIMPLE_QUERY:
			service_name = FIELD_QUERY_SERVICE;
			empty_query_test_param = FIELD_QUERY_PARAM;
			break;
		case ADVANCED_QUERY:
			service_name = ADVANCED_FIELD_QUERY_SERVICE;
			empty_query_test_param = FIELD_QUERY_PARAM;
			break;
		default:
			// should never get here
			logger.error("wrong query type!!");
			return null;
		}

		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, service_name);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("TextQuery request had no paramList.");
			return result; // Return the empty result
		}

		// Process the request parameters
		HashMap params = GSXML.extractParams(param_list, false);

		// Make sure a query has been specified
		String query = (String) params.get(empty_query_test_param);
		if (query == null || query.equals(""))
		{
			return result; // Return the empty result
		}

		// If a field hasn't been specified, use the default - for textQuery
		String field = (String) params.get(INDEX_PARAM);
		if (field == null)
		{
			field = default_index;
		}

		// set up the appropriate query system
		if (!setUpQueryer(params))
		{
			return result;
		}

		// if field search, create the query string
		switch (query_type)
		{
		case TEXT_QUERY:
			query = addFieldInfo(query, field);
			break;
		case SIMPLE_QUERY:
			query = parseFieldQueryParams(params);
			break;
		case ADVANCED_QUERY:
			query = parseAdvancedFieldQueryParams(params);
			break;
		}

		// run the query
		Object query_result = runQuery(query);

		// build up the response

		// Create a metadata list to store information about the query results
		// should we be using metadataList? or something else?
		Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(metadata_list);

		// Add a metadata element specifying the number of matching documents
		long totalDocs = numDocsMatched(query_result);

		GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "" + totalDocs);

		// Create a document list to store the matching documents, and add them
		String[] docs = getDocIDs(query_result);
		String[] doc_ranks = getDocRanks(query_result);

		// add a metadata item to specify docs returned
		int docs_returned = docs.length;
		if (does_paging)
		{
			String maxdocs_str = (String) params.get(MAXDOCS_PARAM);
			if (maxdocs_str != null)
			{
				int maxdocs = Integer.parseInt(maxdocs_str);
				docs_returned = (maxdocs < (int) totalDocs ? maxdocs : (int) totalDocs);
			}
		}
		GSXML.addMetadata(this.doc, metadata_list, "numDocsReturned", "" + docs_returned);

		// add a metadata item to specify what actual query was done - eg if stuff was stripped out etc. and then we can use the query later, cos we don't know which parameter was the query
		GSXML.addMetadata(this.doc, metadata_list, "query", query);
		if (docs.length > 0)
		{
			Element document_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			result.appendChild(document_list);
			for (int d = 0; d < docs.length; d++)
			{
				String doc_id = internalNum2OID(docs[d]);
				Element doc_node = createDocNode(doc_id, doc_ranks[d]);
				document_list.appendChild(doc_node);
			}
		}

		// Create a term list to store the term information, and add it
		Element term_list = this.doc.createElement(GSXML.TERM_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(term_list);
		addTermInfo(term_list, params, query_result);

		return result;

	}

	/** methods to handle actually doing the query */
	/** do any initialisation of the query object */
	abstract protected boolean setUpQueryer(HashMap params);

	/** do the query */
	abstract protected Object runQuery(String query);

	/** get the total number of docs that match */
	abstract protected long numDocsMatched(Object query_result);

	/** get the list of doc ids */
	abstract protected String[] getDocIDs(Object query_result);

	/** get the list of doc ranks */
	abstract protected String[] getDocRanks(Object query_result);

	/** add in term info if available */
	abstract protected boolean addTermInfo(Element term_list, HashMap params, Object query_result);

	/**
	 * combines all the field params into a single query - for simple field
	 * query
	 */
	/** We assume the combination (AND/OR) is done by the match param */
	protected String parseFieldQueryParams(HashMap params)
	{

		StringBuffer final_query = new StringBuffer(256);
		String text_line = (String) params.get(FIELD_QUERY_PARAM);
		String[] texts = text_line.split(",", -1);
		String field_line = (String) params.get(FIELD_FIELD_PARAM);
		String[] fields = field_line.split(",", -1);

		for (int i = 0; i < texts.length; i++)
		{
			String q = texts[i].trim();
			if (!q.equals(""))
			{
				final_query.append(" " + addFieldInfo(q, fields[i]));
			}
		}

		return final_query.toString();
	}

	abstract protected String addFieldInfo(String query, String field);

	/**
	 * combines all the field params into a single query - for advanced field
	 * query
	 */
	protected String parseAdvancedFieldQueryParams(HashMap params)
	{

		StringBuffer final_query = new StringBuffer(256);
		String text_line = (String) params.get(FIELD_QUERY_PARAM);
		String[] texts = text_line.split(",", -1);
		String field_line = (String) params.get(FIELD_FIELD_PARAM);
		String[] fields = field_line.split(",", -1);
		String[] cases = null;
		String[] stems = null;
		String[] accents = null;
		if (does_case)
		{
			String case_line = (String) params.get(FIELD_CASE_PARAM);
			if (case_line != null)
				cases = case_line.split(",", -1);
		}
		if (does_stem)
		{
			String stem_line = (String) params.get(FIELD_STEM_PARAM);
			if (stem_line != null)
				stems = stem_line.split(",", -1);
		}
		if (does_accent)
		{
			String accent_line = (String) params.get(FIELD_ACCENT_PARAM);
			if (accent_line != null)
				accents = accent_line.split(",", -1);
		}
		String combine_line = (String) params.get(FIELD_COMBINE_PARAM);
		String[] combines = combine_line.split(",", -1);
		String combine = "";
		for (int i = 0; i < texts.length; i++)
		{
			if (i == 0)
			{// assume first one is blank
				combine = "";
			}
			else
			{
				String x = combines[i];
				if (x.equals(FIELD_COMBINE_PARAM_AND))
				{
					combine = AND_OPERATOR;
				}
				else if (x.equals(FIELD_COMBINE_PARAM_OR))
				{
					combine = OR_OPERATOR;
				}
				else if (x.equals(FIELD_COMBINE_PARAM_NOT))
				{
					combine = NOT_OPERATOR;
				}

			}

			String q = texts[i].trim();
			boolean modified = false;
			if (!q.equals(""))
			{
				String c = null;
				String s = null;
				String a = null;
				if (does_case)
				{
					modified = true;
					c = cases[i];
				}
				if (does_stem)
				{
					modified = true;
					s = stems[i];
				}
				if (does_accent)
				{
					modified = true;
					a = accents[i];
				}
				if (modified)
				{
					q = addStemOptions(q, s, c, a);
				}
				addQueryElem(final_query, q, fields[i], combine);
			}
		}
		return final_query.toString();
	}

	abstract protected void addQueryElem(StringBuffer final_query, String query, String field, String combine);

	abstract protected String addStemOptions(String query, String stem, String casef, String accent);

}
