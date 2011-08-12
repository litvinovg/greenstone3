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
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSPath;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;

// java classes
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.*;

/** Partially implements a generic search service
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 */

public abstract class AbstractTextSearch
    extends AbstractSearch 
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
    protected static final String ACCENT_PARAM="accent";

    protected static final String BOOLEAN_PARAM_ON = "1";
    protected static final String BOOLEAN_PARAM_OFF = "0";
    protected static final String MATCH_PARAM = "matchMode";
    protected static final String MATCH_PARAM_ALL = "all";
    protected static final String MATCH_PARAM_SOME = "some";

    protected String default_index_subcollection = "";

    protected String default_index_language = "";
   
    public AbstractTextSearch()
    {
	// the search service
	QUERY_SERVICE = "TextQuery";
    }


    /** adds the standard query params into the service description */
    protected void addStandardQueryParams(Element param_list, String lang) 
    {
	if (!default_index_subcollection.equals("")){
	    createParameter(INDEX_SUBCOLLECTION_PARAM,param_list, lang);
	}
	if (!default_index_language.equals("")){
	    createParameter(INDEX_LANGUAGE_PARAM,param_list, lang); 
	}

	super.addStandardQueryParams(param_list,lang);
    }

    /** Top up createParameterChain with TextQuery specific params:
     *    case, stem ...
     */
     protected boolean createParameterChain(String name, Element param_list, String lang, String default_value) 
    {
	Element param = null;
	String param_default = default_value;

	if (super.createParameterChain(name,param_list,lang,default_value)) {
	    // found a match, so can stop here
	    return true;
	}
	// otherwise look to see if it is a text specific parameter
	else if (name.equals(INDEX_SUBCOLLECTION_PARAM)){
	    Element index_sub_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_SUBCOLLECTION_ELEM+GSXML.LIST_MODIFIER);
	    if (index_sub_list == null) return true; // processed, just not a very interesting result
	    ArrayList index_sub_ids = new ArrayList();
	    ArrayList index_sub_names = new ArrayList();
	    getIndexSubcollectionData(index_sub_ids, index_sub_names, lang);
	    String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
	    if (does_multi_index_search) {
		param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
	    }
	    if (param_default == null) {
	      param_default = this.default_index_subcollection;
	    }
	    param = GSXML.createParameterDescription2(this.doc, INDEX_SUBCOLLECTION_PARAM, getTextString("param."+INDEX_SUBCOLLECTION_PARAM, lang), param_type, param_default, index_sub_ids, index_sub_names);
	    param_list.appendChild(param);
	    return true;
	}
	else if(name.equals(INDEX_LANGUAGE_PARAM)){
	    Element index_lang_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_LANGUAGE_ELEM+GSXML.LIST_MODIFIER);
            if (index_lang_list == null) return true; // processed, just not a very interesting result
	    ArrayList index_lang_ids = new ArrayList();
	    ArrayList index_lang_names = new ArrayList();
	    getIndexLanguageData(index_lang_ids, index_lang_names, lang);
	    String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
	    if (does_multi_index_search) {
		param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
	    }
	    if (param_default == null) {
	      param_default = this.default_index_language;
	    }
	    param = GSXML.createParameterDescription2(this.doc, INDEX_LANGUAGE_PARAM, getTextString("param."+INDEX_LANGUAGE_PARAM, lang), param_type, param_default, index_lang_ids, index_lang_names);
	    param_list.appendChild(param);
	    return true;
	}

	// Get to there then none of the above params matched
	// => return false so the chain can continue
	return false;
	    
    }

    /** do the actual query 
     * must be implemented by subclass */
    abstract protected Element processTextQuery(Element request); 
	
    /** get the details about the indexexSubcollections available
     * might be implemented by subclass
     */
    protected void getIndexSubcollectionData(ArrayList index_ids, ArrayList index_names, String lang){}
    
    /** get the details about the indexes available
     * might be implemented by subclass
     */
    protected void getIndexLanguageData(ArrayList index_ids, ArrayList index_names, String lang){}
  

}

