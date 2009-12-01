/*
 *    GS2LuceneSearch.java
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
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Document; 
// java classes
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Vector;

// Logging
import org.apache.log4j.Logger;

import org.greenstone.LuceneWrapper.GS2LuceneQuery;
import org.greenstone.LuceneWrapper.LuceneQueryResult;

public class GS2LuceneSearch
    extends AbstractGS2FieldSearch
{
    protected static final String RANK_PARAM_RANK_VALUE = "rank";

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2LuceneSearch.class.getName());

    private GS2LuceneQuery lucene_src=null;    
    
    public GS2LuceneSearch()
    {
	this.lucene_src = new GS2LuceneQuery();
	// Lucene uses double operators, not single
	AND_OPERATOR = "&&";
	OR_OPERATOR = "||";

	does_paging = true;
	does_chunking = true;
    }
    
    public void cleanUp() {
	super.cleanUp();
	this.lucene_src.cleanUp();
    }
    
    /** configure this service */
    public boolean configure(Element info, Element extra_info)
    {
	if (!super.configure(info, extra_info)){
	    return false;
	}
	
	// Lucene doesn't do case folding or stemming or accent folding at the 
	// moment
	does_case = false;
	does_stem = false;
	does_accent = false;

	return true;
    }
    
    /** add in the lucene specific params to TextQuery */
    protected void addCustomQueryParams(Element param_list, String lang) 
    {
	super.addCustomQueryParams(param_list, lang);
	/** lucenes rank param is based on index fields, not ranked/not */
	createParameter(RANK_PARAM, param_list, lang);

    }
    
    /** create a param and add to the list */
    /** we override this to do a special rank param */
    protected void createParameter(String name, Element param_list, String lang) 
    {
	Element param = null;
	if (name.equals(RANK_PARAM)) {
	    // get the fields
	    ArrayList fields = new ArrayList();
	    fields.add(RANK_PARAM_RANK_VALUE);
	    ArrayList field_names = new ArrayList();
	    field_names.add(getTextString("param.sortBy.rank", lang)); 
	    getSortByIndexData(fields, field_names, lang);
	    
	    param = GSXML.createParameterDescription2(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, (String)fields.get(0), fields, field_names );
	}
	if (param != null) {
	    param_list.appendChild(param);
	} else {
	    super.createParameter(name, param_list, lang);
	}
    }
    
    protected void getSortByIndexData(ArrayList index_ids, ArrayList index_names, String lang) {
	// the index info -
	Element index_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_ELEM+GSXML.LIST_MODIFIER);
	NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
	int len = indexes.getLength();
	// now add even if there is only one
	for (int i=0; i<len; i++) {
	    Element index = (Element)indexes.item(i);
	    String shortname = index.getAttribute(GSXML.SHORTNAME_ATT);
	    if (shortname.equals("") || shortname.equals("ZZ") || shortname.equals("TX")) {
		continue;
	    }
	    index_ids.add("by"+shortname);
	    String display_name = GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en");
	    if (display_name.equals("")) {
		display_name = index.getAttribute(GSXML.NAME_ATT);
		if (display_name.equals("")) {
		    display_name = shortname;
		}
	    }
	    index_names.add(display_name);
		
	}
	
    }

    /** methods to handle actually doing the query */

    /** do any initialisation of the query object */
    protected boolean setUpQueryer(HashMap params) {
       	String indexdir = GSFile.collectionBaseDir(this.site_home, this.cluster_name) + File.separatorChar + "index"+File.separatorChar;
	
	String index = "didx";
	String physical_index_language_name=null;
	String physical_sub_index_name=null;
	int maxdocs = 100;
	int hits_per_page = 20;
	int start_page = 1;
	// set up the query params
    	Set entries = params.entrySet();
    	Iterator i = entries.iterator();
    	while (i.hasNext()) {
    	    Map.Entry m = (Map.Entry)i.next();
    	    String name = (String)m.getKey();
    	    String value = (String)m.getValue();
    	    
    	    if (name.equals(MAXDOCS_PARAM)&& !value.equals("")) {
		maxdocs = Integer.parseInt(value);
	    } else if (name.equals(HITS_PER_PAGE_PARAM)) {
		hits_per_page = Integer.parseInt(value);
	    } else if (name.equals(START_PAGE_PARAM)) {
		start_page = Integer.parseInt(value);
		
    	    } else if (name.equals(MATCH_PARAM)) {
    		if (value.equals(MATCH_PARAM_ALL)) {
		    this.lucene_src.setDefaultConjunctionOperator("AND");
    		} else{
		    this.lucene_src.setDefaultConjunctionOperator("OR");
    		}
    	    } else if (name.equals(RANK_PARAM)) {
		if (value.equals(RANK_PARAM_RANK_VALUE)) {
		    value = null;
		}
		this.lucene_src.setSortField(value);
    	    } else if (name.equals(LEVEL_PARAM)) {
		if (value.toUpperCase().equals("SEC")){
		    index = "sidx";
		}
		else {
		    index = "didx";
		}
	    } else if (name.equals(INDEX_SUBCOLLECTION_PARAM)) {
		physical_sub_index_name=value;
	    } else if (name.equals(INDEX_LANGUAGE_PARAM)){
		physical_index_language_name=value;
	    }  // ignore any others
    	}
	// set up start and end results if necessary
	int start_results = 1;
	if (start_page != 1) {
	    start_results = ((start_page-1) * hits_per_page) + 1;
	}
	int end_results = hits_per_page * start_page;
	this.lucene_src.setStartResults(start_results);
	this.lucene_src.setEndResults(end_results);
	

	if (index.equals("sidx") || index.equals("didx")){
	    if (physical_sub_index_name!=null) {
		index+=physical_sub_index_name;
	    }   
	    if (physical_index_language_name!=null){
		index+=physical_index_language_name;
	    }
	}

	this.lucene_src.setIndexDir(indexdir+index);
	this.lucene_src.initialise();
	return true;
    }
    /** do the query */
    protected Object runQuery(String query) {
	try {
	    LuceneQueryResult lqr=this.lucene_src.runQuery(query);
	    return lqr;
	} catch (Exception e) {
	    logger.error ("exception happened in run query: ", e);
	}
	
	return null;
    }
    /** get the total number of docs that match */
    protected long numDocsMatched(Object query_result) {
    	return ((LuceneQueryResult)query_result).getTotalDocs();
    	
    }
    /** get the list of doc ids */
    protected String [] getDocIDs(Object query_result) {
    	Vector docs = ((LuceneQueryResult)query_result).getDocs();
    	String [] doc_nums = new String [docs.size()];
    	for (int d = 0; d < docs.size(); d++) {
	    String doc_num = ((LuceneQueryResult.DocInfo) docs.elementAt(d)).id_;
    	    doc_nums[d] = doc_num;
    	}
    	return doc_nums;
    }
    /** get the list of doc ranks */
    protected String [] getDocRanks(Object query_result) {
    	Vector docs = ((LuceneQueryResult)query_result).getDocs();
    	String [] doc_ranks = new String [docs.size()];
    	for (int d = 0; d < docs.size(); d++) {
    	    doc_ranks[d] = Float.toString(((LuceneQueryResult.DocInfo) docs.elementAt(d)).rank_);
    	}
    	return doc_ranks;
    }
    /** add in term info if available */
    protected boolean addTermInfo(Element term_list, HashMap params,
				  Object query_result) {
    	String query_level = (String)params.get(LEVEL_PARAM); // the current query level
	
    	Vector terms = ((LuceneQueryResult)query_result).getTerms();
    	for (int t = 0; t < terms.size(); t++) {
    	    LuceneQueryResult.TermInfo term_info = (LuceneQueryResult.TermInfo) terms.get(t);
    	    
    	    Element term_elem = this.doc.createElement(GSXML.TERM_ELEM);
    	    term_elem.setAttribute(GSXML.NAME_ATT, term_info.term_);
    	    term_elem.setAttribute(FREQ_ATT, "" + term_info.term_freq_);
    	    term_elem.setAttribute(NUM_DOCS_MATCH_ATT, "" + term_info.match_docs_);
   	    term_elem.setAttribute(FIELD_ATT, term_info.field_);
    	    term_list.appendChild(term_elem);
    	}
    	return true;
    }
    
    protected String addFieldInfo(String query, String field) {
	if (field.equals("") || field.equals("ZZ")) {
	    return query;
	}
	return field+":("+query+")";
    }
    
    protected void addQueryElem(StringBuffer s, String q, String f, String c) {
	
	String combine="";
	if (s.length()>0) {
	    combine = " "+c+" ";
	}
	s.append(combine + addFieldInfo(q,f));
    }
    
    /** Lucene doesn't use these options at the moment */
    protected String addStemOptions(String query, String stem,
				    String casef, String accent) {
	return query;
    }

    /** Lucene no longer uses internal ids. It just uses hash ids. So we need 
	to override these methods so no conversion is done. */
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
