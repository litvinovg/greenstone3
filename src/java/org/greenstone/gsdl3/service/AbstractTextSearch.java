/*
 *    AbstractTextSearch.java
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
import org.greenstone.gsdl3.util.GSXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Partially implements a generic search service
 * 
 */

public abstract class AbstractTextSearch extends AbstractSearch
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractTextSearch.class.getName());

	// optional standard params - some of these have to be implemented
	protected static final String INDEX_SUBCOLLECTION_PARAM = "indexSubcollection";
	protected static final String INDEX_LANGUAGE_PARAM = "indexLanguage";

	protected static final String INDEX_SUBCOLLECTION_ELEM = "indexSubcollection";
	protected static final String INDEX_LANGUAGE_ELEM = "indexLanguage";

	// some other common params that may be used
	protected static final String CASE_PARAM = "case";
	protected static final String STEM_PARAM = "stem";
	protected static final String ACCENT_PARAM = "accent";

	protected static final String BOOLEAN_PARAM_ON = "1";
	protected static final String BOOLEAN_PARAM_OFF = "0";
	protected static final String MATCH_PARAM = "matchMode";
	protected static final String MATCH_PARAM_ALL = "all";
	protected static final String MATCH_PARAM_SOME = "some";

	protected String default_index_subcollection = "";

	protected String default_index_language = "";

	public AbstractTextSearch()
	{
	  super();
		// the search service
		QUERY_SERVICE = "TextQuery";
		paramDefaults.put(CASE_PARAM, BOOLEAN_PARAM_ON);
		paramDefaults.put(STEM_PARAM, BOOLEAN_PARAM_OFF);
		paramDefaults.put(ACCENT_PARAM, BOOLEAN_PARAM_ON);
		paramDefaults.put(MATCH_PARAM, MATCH_PARAM_SOME);
	}

	/** adds the standard query params into the service description */
	protected void addStandardQueryParams(Element param_list, String lang)
	{
		if (!default_index_subcollection.equals(""))
		{
			createParameter(INDEX_SUBCOLLECTION_PARAM, param_list, lang);
		}
		if (!default_index_language.equals(""))
		{
			createParameter(INDEX_LANGUAGE_PARAM, param_list, lang);
		}

		super.addStandardQueryParams(param_list, lang);
	}

	/**
	 * Top up createParameterChain with TextQuery specific params: case, stem
	 * ...
	 */
	protected boolean createParameterChain(String name, Element param_list, String lang, String default_value)
	{
	  Document doc = param_list.getOwnerDocument();
		Element param = null;
		String param_default = default_value;
		if (default_value == null) {
		  // have we got a stored up default? will be null if not there
		  param_default = paramDefaults.get(name);
		}

		if (super.createParameterChain(name, param_list, lang, default_value))
		{
			// found a match, so can stop here
			return true;
		}
		// otherwise look to see if it is a text specific parameter
		if (name.equals(INDEX_SUBCOLLECTION_PARAM))
		{
			Element index_sub_list = (Element) GSXML.getChildByTagName(this.config_info, INDEX_SUBCOLLECTION_ELEM + GSXML.LIST_MODIFIER);
			if (index_sub_list == null)
				return true; // processed, just not a very interesting result
			ArrayList<String> index_sub_ids = new ArrayList<String>();
			ArrayList<String> index_sub_names = new ArrayList<String>();
			getIndexSubcollectionData(index_sub_ids, index_sub_names, lang);
			String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
			if (does_multi_index_search)
			{
				param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
			}
			if (param_default == null)
			{
				param_default = this.default_index_subcollection;
			}
			param = GSXML.createParameterDescription2(doc, INDEX_SUBCOLLECTION_PARAM, getTextString("param." + INDEX_SUBCOLLECTION_PARAM, lang), param_type, param_default, index_sub_ids, index_sub_names);
			param_list.appendChild(param);
			return true;
		}
		else if (name.equals(INDEX_LANGUAGE_PARAM))
		{
			Element index_lang_list = (Element) GSXML.getChildByTagName(this.config_info, INDEX_LANGUAGE_ELEM + GSXML.LIST_MODIFIER);
			if (index_lang_list == null)
				return true; // processed, just not a very interesting result
			ArrayList<String> index_lang_ids = new ArrayList<String>();
			ArrayList<String> index_lang_names = new ArrayList<String>();
			getIndexLanguageData(index_lang_ids, index_lang_names, lang);
			String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
			if (does_multi_index_search)
			{
				param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
			}
			if (param_default == null)
			{
				param_default = this.default_index_language;
			}
			param = GSXML.createParameterDescription2(doc, INDEX_LANGUAGE_PARAM, getTextString("param." + INDEX_LANGUAGE_PARAM, lang), param_type, param_default, index_lang_ids, index_lang_names);
			param_list.appendChild(param);
			return true;
		}
	else if (name.equals(CASE_PARAM) || name.equals(STEM_PARAM) || name.equals(ACCENT_PARAM)) {
	    String[] bool_ops = {"0", "1"};
	    String[] bool_texts = {getTextString("param.boolean.off", lang),getTextString("param.boolean.on", lang)}; 
	    param = GSXML.createParameterDescription(doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_BOOLEAN, param_default, bool_ops, bool_texts);
	    param_list.appendChild(param);
	    return true;
	} else if (name.equals(MATCH_PARAM)) {
	  
	    String[] vals = {MATCH_PARAM_SOME, MATCH_PARAM_ALL };
	    String[] val_texts = {getTextString("param."+MATCH_PARAM+"."+MATCH_PARAM_SOME, lang), getTextString("param."+MATCH_PARAM+"."+MATCH_PARAM_ALL, lang)}; 
	    param = GSXML.createParameterDescription(doc, MATCH_PARAM, getTextString("param."+MATCH_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, param_default, vals, val_texts);
	    param_list.appendChild(param);
	    return true;
	}
		// Get to here then none of the above params matched
		// => return false so the chain can continue
		return false;

	}

	/**
	 * do the actual query must be implemented by subclass
	 */
	abstract protected Element processTextQuery(Element request);

	/**
	 * get the details about the indexexSubcollections available might be
	 * implemented by subclass
	 */
	protected void getIndexSubcollectionData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang)
	{
	}

	/**
	 * get the details about the indexes available might be implemented by
	 * subclass
	 */
	protected void getIndexLanguageData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang)
	{
	}

}
