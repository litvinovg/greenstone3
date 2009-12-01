package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList; 

import java.util.HashMap;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*; //Document;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;

import java.io.File;

import org.apache.log4j.*;

/**
 *
 */

public class LuceneSearch 
    extends AbstractSearch {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.LuceneSearch.class.getName());

    protected static final String INDEX_ELEM = "index";
    
    public boolean configure(Element info, Element extra_info) {
	if (!super.configure(info, extra_info)){
	    return false;
	}
	
	default_index = "idx";
	return true;
    }
    
    protected void getIndexData(ArrayList index_ids, ArrayList index_names, String lang) 
    {
	// the index info - read from config file - cache it??
	Element index_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_ELEM+GSXML.LIST_MODIFIER);
	if (index_list != null) {
	    NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
	    int len = indexes.getLength();
	    // now add even if there is only one
	    for (int i=0; i<len; i++) {
		Element index = (Element)indexes.item(i);
		index_ids.add(index.getAttribute(GSXML.NAME_ATT));
		index_names.add(GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en"));
		
	    }
	} else {
	    // there is only one index, so we assume the default
	    index_ids.add(this.default_index);
	    index_names.add("default index");
	}

    }

    /** Process a text query - implemented by concrete subclasses */
    protected Element processTextQuery(Element request) {

	// Create a new (empty) result message
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, TEXT_QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	Element doc_node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(doc_node_list);
	Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(metadata_list);
	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "0");
	    return result;  // Return the empty result
	}

	// Process the request parameters
	HashMap params = GSXML.extractParams(param_list, false);

	// Make sure a query has been specified
	String query_string = (String) params.get(QUERY_PARAM);
	if (query_string == null || query_string.equals("")) {
	    logger.error("TextQuery request had no query string.");
	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "0");
	    return result;  // Return the empty result
	}

	// Get the index
	String index = (String) params.get(INDEX_PARAM);
	if (index == null || index.equals("")) {
	    index = this.default_index; // assume the default
	}
        try {
	    String index_dir = GSFile.collectionIndexDir(this.site_home, this.cluster_name);
	    index_dir += File.separator+index;
	    Searcher searcher = new IndexSearcher(index_dir);
	    Analyzer analyzer = new StandardAnalyzer();
	    
	    Term term = new Term("content", query_string);
        
	    Query query = new TermQuery(term);

	    Hits hits = searcher.search(query);
	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", ""+hits.length());
	    
	    for (int i=0; i<hits.length(); i++) {
		org.apache.lucene.document.Document luc_doc = hits.doc(i);
		String node_id = luc_doc.get("nodeID");
		Element node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		node.setAttribute(GSXML.NODE_ID_ATT, node_id);
		doc_node_list.appendChild(node);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return result;
	    
    }

    
}
