/*
 *    SharedSoleneGS2FieldSearch.java -- shared base code for Solr and Lucene
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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.greenstone.LuceneWrapper4.SharedSoleneQuery;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Shared code for Solr and Lucene GS2FieldSearch

public abstract class SharedSoleneGS2FieldSearch extends AbstractGS2FieldSearch
{

  protected static String RANK_PARAM_RANK = "rank";
  protected static String RANK_PARAM_NONE = "none";

  protected static final String SORT_ELEM = "sort";
  protected static final String SORT_ORDER_PARAM = "sortOrder";
  protected static final String SORT_ORDER_DESCENDING = "1";
  protected static final String SORT_ORDER_ASCENDING = "0";

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.SharedSoleneGS2FieldSearch.class.getName());

	protected SharedSoleneQuery solene_src = null;

  
	public SharedSoleneGS2FieldSearch()
	{
	  super();
		// Lucene/Solr uses double operators, not single
		AND_OPERATOR = "&&";
		OR_OPERATOR = "||";

		does_paging = true;
		does_chunking = true;
		paramDefaults.put(SORT_ORDER_PARAM, SORT_ORDER_DESCENDING);
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		// the search element
		Element config_search = (Element) GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);
		Document owner = info.getOwnerDocument();
		// find the sort fields in serviceRack xml, and add in the deisplayItems if any
		NodeList sort_nodes = info.getElementsByTagName(SORT_ELEM);

		for (int i = 0; i < sort_nodes.getLength(); i++)
		  {
		    Element sort = (Element) sort_nodes.item(i);
		    String name = sort.getAttribute(GSXML.NAME_ATT);
		    Element node_extra = GSXML.getNamedElement(config_search, SORT_ELEM, GSXML.NAME_ATT, name);
		    if (node_extra == null)
		      {
			logger.error("haven't found extra info for sort field named " + name);
			continue;
		      }
		    
		    // get the display elements if any - displayName
		    NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
		    if (display_names != null)
		      {
			for (int j = 0; j < display_names.getLength(); j++)
			  {
			    Element e = (Element) display_names.item(j);
			    sort.appendChild(owner.importNode(e, true));
			  }
		      }
		  } // for each sortfield
		// Lucene/Solr doesn't do case folding or stemming or accent folding at the 
		// moment
		does_case = false;
		does_stem = false;
		does_accent = false;

		return true;
	}

	/** add in the Lucene/Solr specific params to TextQuery */
	protected void addCustomQueryParams(Element param_list, String lang)
	{
		super.addCustomQueryParams(param_list, lang);
		/** Lucene's/Solr's rank (sort) param is based on sort fields, not ranked/not */
		createParameter(RANK_PARAM, param_list, lang);
		createParameter(SORT_ORDER_PARAM, param_list, lang);
	}

	/** create a param and add to the list */
	/** we override this to do a special rank param */
  protected void createParameter(String name, Element param_list, String lang)
	{
	  Document doc = param_list.getOwnerDocument();
		Element param = null;
		String param_default = paramDefaults.get(name);
		if (name.equals(RANK_PARAM))
		{
			// get the fields
			ArrayList<String> fields = new ArrayList<String>();
			ArrayList<String> field_names = new ArrayList<String>();
			if (!getSortData(fields, field_names, lang)) {
			  fields.add(RANK_PARAM_RANK);
			  fields.add(RANK_PARAM_NONE);
			  field_names.add(getTextString("param." + RANK_PARAM + "." + RANK_PARAM_RANK, lang));
			  field_names.add(getTextString("param." + RANK_PARAM + "." + RANK_PARAM_NONE, lang));
			}
			
			param = GSXML.createParameterDescription2(doc, name, getTextString("param." + name, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, fields.get(0), fields, field_names);
			
		} else if (name.equals(SORT_ORDER_PARAM)) {
	    String[] vals = { SORT_ORDER_ASCENDING, SORT_ORDER_DESCENDING };
	    String[] vals_texts = { getTextString("param." + SORT_ORDER_PARAM + "." + SORT_ORDER_ASCENDING, lang), getTextString("param." + SORT_ORDER_PARAM + "." + SORT_ORDER_DESCENDING, lang) };

	    param = GSXML.createParameterDescription(doc, SORT_ORDER_PARAM, getTextString("param." + SORT_ORDER_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, SORT_ORDER_DESCENDING, vals, vals_texts);
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

  protected boolean getSortData(ArrayList<String> sort_ids, ArrayList<String> sort_names, String lang) {

    	Element sort_list = (Element) GSXML.getChildByTagName(this.config_info, SORT_ELEM + GSXML.LIST_MODIFIER);
	if (sort_list == null) return false;
	NodeList sorts = sort_list.getElementsByTagName(SORT_ELEM);
	int len = sorts.getLength();
	if (len == 0) return false;
	for (int i = 0; i < len; i++)
	  {
	    Element sort = (Element) sorts.item(i);
	    String shortname = sort.getAttribute(GSXML.SHORTNAME_ATT);
	    sort_ids.add(shortname);
	    String display_name = GSXML.getDisplayText(sort, GSXML.DISPLAY_TEXT_NAME, lang, "en");
	    if (display_name.equals(""))
	      {
		display_name = sort.getAttribute(GSXML.NAME_ATT);
		if (display_name.equals(""))
		  {
		    display_name = shortname;
		  }
	      }
	    sort_names.add(display_name);
	    
	  }
	return true;
  }

	protected String addFieldInfo(String query, String field)
	{
		// currently, allfields (ZZ) is stored as a extra field for Lucene
		if (field.equals(""))
		{ // || field.equals("ZZ")) {
			return query;
		}
		return field + ":(" + query + ")";
	}

	protected void addQueryElem(StringBuffer s, String q, String f, String c)
	{

		String combine = "";
		if (s.length() > 0)
		{
			combine = " " + c + " ";
		}
		s.append(combine + addFieldInfo(q, f));
	}

	/** Lucene/Solr doesn't use these options at the moment */
	protected String addStemOptions(String query, String stem, String casef, String accent)
	{
		return query;
	}

	/**
	 * Lucene/Solr does not use internal ids. It just uses hash ids. So we need
	 * to override these methods so no conversion is done.
	 */
	/** convert indexer internal id to Greenstone oid */
	protected String internalNum2OID(long docnum)
	{
		return Long.toString(docnum);
	}

	protected String internalNum2OID(String docnum)
	{
		return docnum;

	}
}
