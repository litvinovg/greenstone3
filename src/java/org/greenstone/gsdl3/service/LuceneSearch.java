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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.QueryParser;
//import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import org.greenstone.LuceneWrapper3.GS2Analyzer;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.*;

/**
 *
 */

public class LuceneSearch 
    extends AbstractTextSearch {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.LuceneSearch.class.getName());

    protected static final String INDEX_ELEM = "index";

    protected ArrayList<String> index_ids;

    public LuceneSearch()
    {
	index_ids = new ArrayList<String>();
    }
    
    public boolean configure(Element info, Element extra_info) {
	if (!super.configure(info, extra_info)){
	    return false;
	}
	
	default_index = "idx";

	// cache index info read from config file
	Element index_list 
	    = (Element)GSXML.getChildByTagName(this.config_info, 
					       INDEX_ELEM+GSXML.LIST_MODIFIER);
	if (index_list != null) {
	    NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
	    int len = indexes.getLength();
	    // now add even if there is only one
	    for (int i=0; i<len; i++) {
		Element index = (Element)indexes.item(i);
		index_ids.add(index.getAttribute(GSXML.NAME_ATT));		
	    }
	} else {
	    // there is only one index, so we assume the default
	    index_ids.add(this.default_index);
	}

	return true;
    }
    
    protected void getIndexData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang) 
    {
	// copying exercise for index_ids, 
	for (int i=0; i<this.index_ids.size(); i++) {
	    index_ids.add(this.index_ids.get(i));
	}

	// But need to work out display name from scratch as this uses
	// the 'lang' parameter

	Element index_list 
	    = (Element)GSXML.getChildByTagName(this.config_info,
					       INDEX_ELEM+GSXML.LIST_MODIFIER);
	if (index_list != null) {
	    NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
	    int len = indexes.getLength();
	    // now add even if there is only one
	    for (int i=0; i<len; i++) {
		Element index = (Element)indexes.item(i);
		index_names.add(GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en"));
		
	    }
	} else {
	    // there is only one index, so we assume the default
	    index_names.add("default index");
	}
    }


    protected void initResultElement(Element result, Element doc_node_list, Element metadata_list)
    {

	// Create a new (empty) result message
	result.setAttribute(GSXML.FROM_ATT, QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	result.appendChild(doc_node_list);
	result.appendChild(metadata_list);
    }

    protected boolean hasParamList(Element request, Element metadata_list)
    {
	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "0");
	    return false; // signal that an empty result should be return
	}

	return true;
    }

    protected boolean hasQueryString(Element param_list, Element metadata_list)
    {

	// Process the request parameters to make sure a query has been specified
	HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
	String query_string = (String) params.get(QUERY_PARAM);

	if (query_string == null || query_string.equals("")) {
	    logger.error("TextQuery request had no query string.");
	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "0");
	    return false;  // signal that an empty result should be return
	}

	return true;
    }



    /** Process a text query - implemented by concrete subclasses */
    protected Element processTextQuery(Element request) {

	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	Element doc_node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	initResultElement(result,doc_node_list,metadata_list);

	if (!hasParamList(request,metadata_list)) {
	    return result;
	}

	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (!hasQueryString(param_list,metadata_list)) {
	    return result;
	}

	HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
	String query_string = (String) params.get(QUERY_PARAM);

	// Get the index
	String index = (String) params.get(INDEX_PARAM);
	if (index == null || index.equals("")) {
	    index = this.default_index; // assume the default
	}
	
	try {
	    String index_dir = GSFile.collectionIndexDir(this.site_home, this.cluster_name);
	    index_dir += File.separator+index;
	    Directory index_dir_dir = FSDirectory.open(new File(index_dir));
	    Searcher searcher = new IndexSearcher(index_dir_dir);
	    Analyzer analyzer = new GS2Analyzer();
	    
	    Term term = new Term("content", query_string);
	    
	    Query query = new TermQuery(term);
	    
	    TopDocs hits = searcher.search(query, Integer.MAX_VALUE);

	    GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", ""+hits.scoreDocs.length);
	    
    	    IndexReader reader = ((IndexSearcher) searcher).getIndexReader();

	    for (int i=0; i<hits.scoreDocs.length; i++) {
		int lucene_doc_num = hits.scoreDocs[i].doc;
		org.apache.lucene.document.Document luc_doc = reader.document(lucene_doc_num);
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
