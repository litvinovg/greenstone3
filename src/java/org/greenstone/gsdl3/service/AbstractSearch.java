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

public abstract class AbstractSearch
    extends ServiceRack 
{

   static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.AbstractSearch.class.getName());

    
    // the search service
    protected static final String TEXT_QUERY_SERVICE = "TextQuery";

    // compulsory params
    protected static final String INDEX_PARAM = "index";
    protected static final String QUERY_PARAM = "query";

    // optional standard params - some of these have to be implemented
    protected static final String INDEX_SUBCOLLECTION_PARAM = "indexSubcollection";
    protected static final String INDEX_LANGUAGE_PARAM = "indexLanguage";
    protected static final String MAXDOCS_PARAM = "maxDocs";
    protected static final String HITS_PER_PAGE_PARAM = "hitsPerPage";
    protected static final String START_PAGE_PARAM = "startPage";

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

    /** can more than one index be searched at the smae time? */
    protected boolean does_multi_index_search = false;
    /** does this service support paging of results? */
    protected boolean does_paging = false;
    /** does this service support asking for a subset of results? */
    protected boolean does_chunking = false;
    /** the default document type - use if all documents are the same type
     */
    protected String default_document_type = null;
    /** the default index, or comma separated list if more than one is 
     * the default (with start and end commas, eg ,TI,SU,). 
     * Should be set by configure()
     */
    protected String default_index = "";

    protected String default_index_subcollection = "";

    protected String default_index_language = "";
   
    protected String default_max_docs = "100";
    
    protected String default_hits_per_page = "10";
 
    public AbstractSearch()
    {
    }

    /** sets up the short service info for TextQuery. If other services 
     * will be provided, should be added in the subclass configure
     * also looks for search format info, and document format info
     */
    public boolean configure(Element info, Element extra_info)
    {
	if (!super.configure(info, extra_info)){
	    return false;
	}

	logger.info("Configuring AbstractSearch...");
	
	this.config_info = info;

	// set up short_service_info_ - for now just has id and type. the name (lang dependent) will be added in if the list is requested.
	Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	tq_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	this.short_service_info.appendChild(tq_service);

	// add some format info to service map if there is any - look in extra info
	// first look in buildConfig
	Element format = (Element)GSXML.getChildByTagName(info, GSXML.FORMAT_ELEM);

	if (format==null) {
	    String path = GSPath.appendLink(GSXML.SEARCH_ELEM, GSXML.FORMAT_ELEM);

      //note by xiao: instead of retrieving the first 'format' element inside the 'search'
      // element, we are trying to find the real format element which has at least one
      // 'gsf:template' child element. (extra_info is collectionConfig.xml)
      //format = (Element) GSXML.getNodeByPath(extra_info, path);
      Element search_elem = (Element) GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);
      NodeList format_elems = null;
      if (search_elem != null) {
        format_elems = search_elem.getElementsByTagName(GSXML.FORMAT_ELEM);
      }
      for(int i=0; i<format_elems.getLength(); i++) {
          format = (Element)format_elems.item(i);
          if (format.getElementsByTagName("gsf:template").getLength() != 0) {
            break;
          }
      }
	}//end of if(format==null)
	//
	if (format != null) {
	    this.format_info_map.put(TEXT_QUERY_SERVICE, this.doc.importNode(format, true));
	}
	
	// look for document display format - for documentType
	String path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
	Element display_format = (Element)GSXML.getNodeByPath(extra_info, path);
	if (display_format != null) {
	    // check for docType option.
	    Element doc_type_opt = GSXML.getNamedElement(display_format, "gsf:option", GSXML.NAME_ATT, "documentType");
	    if (doc_type_opt != null) {
		String value = doc_type_opt.getAttribute(GSXML.VALUE_ATT);
		if (!value.equals("")) {
		    this.default_document_type = value;
		}
	    }
	}

	return true;
    }
    
    /** returns the description of the TextQuery service. If a subclass 
     * provides other services they need to provides their own descriptions */
    protected Element getServiceDescription(String service, String lang, String subset) 
    {
	if (!service.equals(TEXT_QUERY_SERVICE)) {
	    return null;
	}

	Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	tq_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	if (subset==null || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
	    tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getServiceName(TEXT_QUERY_SERVICE, lang) ));
	    tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getServiceSubmit(TEXT_QUERY_SERVICE, lang) ));
	    tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getServiceDescription(TEXT_QUERY_SERVICE, lang)));
	}
	if (subset==null || subset.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) {
	    Element param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	    addCustomQueryParams(param_list, lang);
	    addStandardQueryParams(param_list, lang);
	    tq_service.appendChild(param_list);
	}
	return tq_service;
	
    }

    // perhaps these should be changed to search down the class hierarchy for 
    // values - do can just put the info in the resource bundle to use it
    /** returns the default name for the TextQuery service */
    protected String getServiceName(String service_id, String lang) {
	return getTextString(service_id+".name", lang);
    }
    
    /** returns the default description for the TextQuery service */
    protected String getServiceDescription(String service_id, String lang) {
	return getTextString(service_id+".description", lang);
    }

    /** returns the default submit button text for the TextQuery service */
    protected String getServiceSubmit(String service_id, String lang) {
	return getTextString(service_id+".submit", lang);

    }
    /** adds the standard query params into the service description */
    protected void addStandardQueryParams(Element param_list, String lang) 
    {
      // this test is not so good. here we are using absence of default index
      // to determine whether we have indexes or not. But in other places,
      // absence of default index just means to use the first one as default.
	if (!default_index.equals("")){
	    createParameter(INDEX_PARAM, param_list, lang);
	}
	if (!default_index_subcollection.equals("")){
	    createParameter(INDEX_SUBCOLLECTION_PARAM,param_list, lang);
	}
	if (!default_index_language.equals("")){
	    createParameter(INDEX_LANGUAGE_PARAM,param_list, lang); 
	}
	if (does_chunking) {
	    createParameter(MAXDOCS_PARAM, param_list, lang);
	}
	if (does_paging) {
	    createParameter(HITS_PER_PAGE_PARAM, param_list, lang);
	    createParameter(START_PAGE_PARAM, param_list, lang);
	}
	createParameter(QUERY_PARAM, param_list, lang);
    }

    /** adds any service specific query params into the service
     *	default implementation: add nothing. subclasses may need to 
     * override this to add in their specific parameters
     */
    protected void addCustomQueryParams(Element param_list, String lang) 
    {
	// default behaviour, do nothing
    }

    protected void createParameter(String name, Element param_list, String lang) {
      createParameter(name, param_list, lang, null);
    }
    /** default implementations for the standard parameters plus some 
     * other common ones 
     * index, maxDocs, hitsPerPage, startPage, query, case, stem, 
    */
     protected void createParameter(String name, Element param_list, String lang, String default_value) {
	Element param = null;
	String param_default = default_value;
	if (name.equals(QUERY_PARAM)) {
	    param = GSXML.createParameterDescription(this.doc, QUERY_PARAM, getTextString("param."+QUERY_PARAM, lang), GSXML.PARAM_TYPE_STRING, param_default, null, null);
	    param_list.appendChild(param);
	} else if (name.equals(INDEX_PARAM)) {

	    // should we make these class fields?
	    ArrayList index_ids = new ArrayList();
	    ArrayList index_names = new ArrayList();
	    getIndexData(index_ids, index_names, lang);
	    String param_type = GSXML.PARAM_TYPE_ENUM_SINGLE;
	    if (does_multi_index_search) {
		param_type = GSXML.PARAM_TYPE_ENUM_MULTI;
	    }
	    if (param_default == null) {
	      param_default = this.default_index;
	    }
	    param = GSXML.createParameterDescription2(this.doc, INDEX_PARAM, getTextString("param."+INDEX_PARAM, lang), param_type, param_default, index_ids, index_names);
	    param_list.appendChild(param);
	}
        else if (name.equals(INDEX_SUBCOLLECTION_PARAM)){
	    Element index_sub_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_SUBCOLLECTION_ELEM+GSXML.LIST_MODIFIER);
	     if (index_sub_list == null) return;
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
	}
	else if(name.equals(INDEX_LANGUAGE_PARAM)){
	    Element index_lang_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_LANGUAGE_ELEM+GSXML.LIST_MODIFIER);
            if (index_lang_list == null) return; 
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
	}
        else if (name.equals(MAXDOCS_PARAM)) {
	  if (param_default == null) {
	    param_default = this.default_max_docs;
	  }

	    param = GSXML.createParameterDescription(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_INTEGER, param_default, null, null);
	    param_list.appendChild(param);
	}
	else if(name.equals(HITS_PER_PAGE_PARAM)){
	  if (param_default == null) {
	    param_default = this.default_hits_per_page;
	  }
	  
	     param = GSXML.createParameterDescription(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_INTEGER, param_default, null, null);
	    param_list.appendChild(param);
	}
	else if (name.equals(CASE_PARAM) || name.equals(STEM_PARAM) || name.equals(ACCENT_PARAM)) {
	  if (param_default == null) {
	    param_default = BOOLEAN_PARAM_OFF;
	  }
	    String[] bool_ops = {"0", "1"};
	    String[] bool_texts = {getTextString("param.boolean.off", lang),getTextString("param.boolean.on", lang)}; 
	    param = GSXML.createParameterDescription(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_BOOLEAN, param_default, bool_ops, bool_texts);
	    param_list.appendChild(param);
	} else if (name.equals(MATCH_PARAM)) {
	  if (param_default == null) {
	    param_default = MATCH_PARAM_SOME;
	  }
	  
	    String[] vals = {MATCH_PARAM_SOME, MATCH_PARAM_ALL };
	    String[] val_texts = {getTextString("param."+MATCH_PARAM+"."+MATCH_PARAM_SOME, lang), getTextString("param."+MATCH_PARAM+"."+MATCH_PARAM_ALL, lang)}; 
	    param = GSXML.createParameterDescription(this.doc, MATCH_PARAM, getTextString("param."+MATCH_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, param_default, vals, val_texts);
	    param_list.appendChild(param);
	} else if (name.equals(START_PAGE_PARAM)) {
	  if (param_default == null) {
	    param_default = "1";
	  }
	  
	    // start page - set to 1 for the search page
	    param = GSXML.createParameterDescription(this.doc, START_PAGE_PARAM, "", GSXML.PARAM_TYPE_INVISIBLE, param_default, null, null);
	    param_list.appendChild(param);
	}
	    
	    
    }
    /** create an element to go into the search results list. A node element
     * has the form
     * <docNode nodeId='xxx' nodeType='leaf' docType='hierarchy' rank='0.23'/>
     */
    protected Element createDocNode(String node_id, String rank) {
	Element node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
	node.setAttribute(GSXML.NODE_ID_ATT, node_id);
	node.setAttribute(GSXML.NODE_RANK_ATT, rank);
	String doc_type = null;
	if (default_document_type != null) {
	    doc_type = default_document_type;
	} else {
	    doc_type = getDocType(node_id);
	}
	node.setAttribute(GSXML.DOC_TYPE_ATT, doc_type);
	String node_type = getNodeType(node_id, doc_type);	
	node.setAttribute(GSXML.NODE_TYPE_ATT, node_type);
	return node;
    }

    /** returns the node type of the specified node.
	should be one of 
	GSXML.NODE_TYPE_LEAF, 
	GSXML.NODE_TYPE_INTERNAL, 
	GSXML.NODE_TYPE_ROOT
    */
    protected String getNodeType(String node_id, String doc_type) {
	if (doc_type.equals(GSXML.DOC_TYPE_SIMPLE)) {
	    return GSXML.NODE_TYPE_LEAF;
	}

	if (!hasParent(node_id)) {
	    return GSXML.NODE_TYPE_ROOT;
	}
	if (doc_type.equals(GSXML.DOC_TYPE_PAGED)) {
	     return GSXML.NODE_TYPE_LEAF;
	}
	if (!hasChildren(node_id)) {
	    return GSXML.NODE_TYPE_LEAF;
	}
	return GSXML.NODE_TYPE_INTERNAL;	
	
    }


    /** returns the document type of the doc that the specified node 
	belongs to. should be one of 
	GSXML.DOC_TYPE_SIMPLE, 
	GSXML.DOC_TYPE_PAGED, 
	GSXML.DOC_TYPE_HIERARCHY
	default implementation returns GSXML.DOC_TYPE_SIMPLE, over ride 
	if documents can be hierarchical
    */
    protected String getDocType(String node_id) {
	return GSXML.DOC_TYPE_SIMPLE;
    }
    
    /** returns true if the node has child nodes 
     * default implementation returns false, over ride if documents can be 
     * hierarchical 
     */
    protected boolean hasChildren(String node_id) {
	return false;
    }
    /** returns true if the node has a parent 
     * default implementation returns false, over ride if documents can be 
     * hierarchical*/
    protected boolean hasParent(String node_id) {
	return false;
    }

    /** do the actual query 
     * must be implemented by subclass */
    abstract protected Element processTextQuery(Element request); 
	
    /** get the details about the indexes available
     * must be implemented by subclass
     * there must be at least one index */
    abstract protected void getIndexData(ArrayList index_ids, ArrayList index_names, String lang);

    /** get the details about the indexexSubcollections available
     * might be implemented by subclass
     */
    protected void getIndexSubcollectionData(ArrayList index_ids, ArrayList index_names, String lang){}
    
    /** get the details about the indexes available
     * might be implemented by subclass
     */
    protected void getIndexLanguageData(ArrayList index_ids, ArrayList index_names, String lang){}
  

}

