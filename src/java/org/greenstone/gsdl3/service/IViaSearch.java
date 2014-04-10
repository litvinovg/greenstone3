package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.util.Misc;
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList; 

//Java classes
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.Serializable;
import java.net.Authenticator;

import org.apache.log4j.*;

/**
 *
 * @author Katherine Don
 * @author Chi-Yu Huang
 */

public class IViaSearch
    extends AbstractTextSearch {
    

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.IViaSearch.class.getName());
 
    // have standard gs param names for hits per page, and start page
    // these need to be mapped to iVia params
    protected static final String IM_HITS_PARAM = "no_of_records_per_page";
    protected static final String IM_START_PAGE_PARAM = "start_page_no";

    protected String ivia_server_url = null;
    protected ArrayList<String> index_ids = null;
    public IViaSearch()
    {
    }
    
    //Configure IViaSearch Service
    public boolean configure(Element info, Element extra_info) 
    { 
	if (!super.configure(info, extra_info)){
	    return false;
	}

	Element server_elem = (Element)GSXML.getChildByTagName(info, "iViaServer");
	if (server_elem == null) {
	    logger.error("no iViaServer element found");
	    return false;
	}
	ivia_server_url = server_elem.getAttribute("url");
	if (ivia_server_url.equals("")) {
	    logger.error("no url for the iViaServer element");
	    return false;
	}
	does_paging = true;
	does_multi_index_search = true;
	this.default_index = ",kw,au,su,ti,de,fu,"; // all of them
	index_ids = new ArrayList<String>();
	index_ids.add("kw");
	index_ids.add("au");	
	index_ids.add("su");	
	index_ids.add("ti");
	index_ids.add("de");
	index_ids.add("fu");

	return true;
    }
    
    /** Process a text query - implemented by concrete subclasses */
    protected Element processTextQuery(Element request) {
      Document result_doc = XMLConverter.newDOM();
	// Create a new (empty) result message
	Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	Element doc_node_list = result_doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(doc_node_list);
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    return result;  // Return the empty result
	}

	// Process the request parameters
	HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

	// Make sure a query has been specified
	String query = (String) params.get(QUERY_PARAM);
	if (query == null || query.equals("")) {
	    return result;  // Return the empty result
	}

	// tidy whitespace
	query = query.replaceAll("\\s+", "+");
	String url_string = ivia_server_url+"/cgi-bin/canned_search?theme=gsdl3&query="+query; 

	// check for fields
	String fields = (String) params.get(INDEX_PARAM);
	fields = checkFieldParam(fields); // removes invalid fields
	if (!fields.equals("")) {
	    url_string += "&fields="+fields;
	}
	//check for hits per page
	String hits_per_page = (String) params.get(HITS_PER_PAGE_PARAM);
	if (hits_per_page != null && !hits_per_page.equals("")) {
	    url_string += "&"+IM_HITS_PARAM+"="+hits_per_page;
	}

	// check for start page
	String start_page = (String) params.get(START_PAGE_PARAM);
	if (start_page != null && !start_page.equals("")) {
	    url_string += "&"+IM_START_PAGE_PARAM+"="+start_page;
	}
	String results_num = null;
	String doc_ids = null;
	BufferedReader reader = null;
	try {
	    logger.debug("sending "+url_string);
	    reader = Misc.makeHttpConnection(url_string);
	    results_num = reader.readLine();
	    doc_ids = reader.readLine();
	} catch (java.net.MalformedURLException e) {
	    GSXML.addError(result, "Malformed URL: "+url_string);
	    return result;
	} catch (java.io.IOException e) {
	    GSXML.addError(result, "IOException during connection to "+url_string+": "+e.toString());
	    return result;
	}
	    
	if (results_num.startsWith("Resources: ") && doc_ids.startsWith("Ids: ")) {
	    results_num = results_num.substring(11);
	    doc_ids = doc_ids.substring(5).trim();

	} else {
	    logger.error("badly formatted results:");
	    StringBuffer result_string = new StringBuffer();
	    result_string.append("Error: badly formatted result from IVia server:\n ");
	    result_string.append(results_num);
	    result_string.append(doc_ids);
	    String line;
	    try {
		while((line = reader.readLine()) != null) {
		    result_string.append(line);
		}
	    } catch (Exception e) {
		result_string.append("Exception: "+e);
	    }
	    GSXML.addError(result, result_string.toString());
	    
	    return result;
	}
	
	// get the num docs and add to a metadata list
	Element metadata_list = result_doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER); 
	result.appendChild(metadata_list);
	
	// Add a metadata element specifying the number of matching documents
	long numdocs = Long.parseLong(results_num);
	GSXML.addMetadata(metadata_list, "numDocsMatched", ""+numdocs);	
	String [] ids = doc_ids.split(" ");
	
	for (int d=0; d<ids.length; d++) {
	    Element doc_node = result_doc.createElement(GSXML.DOC_NODE_ELEM);
	    doc_node.setAttribute(GSXML.NODE_ID_ATT, ids[d]);
	    doc_node_list.appendChild(doc_node);
	}
	return result;
    }
     
    protected String checkFieldParam(String fields) {
	
	if (fields == null) {
	    // return the default
	    return "";
	}
	StringBuffer new_fields = new StringBuffer();
	String [] ids = fields.split(",");
	for (int i=0; i<ids.length; i++) {
	    if(index_ids.contains(ids[i])) {
		new_fields.append(ids[i]);
		new_fields.append(",");
	    }
	}
	if (new_fields.length() == 0) {
	    return "";
	}
	return new_fields.toString();
    }
    /**
       An IVia server has a fixed list of fields to search (I think) so we can hard code them here rather than reading them in from a config file
    */
    protected void getIndexData(ArrayList<String> index_ids, ArrayList<String> index_names,String lang){
	index_ids.addAll(this.index_ids);
	index_names.add(getTextString("param."+INDEX_PARAM+".kw", lang)); 
	index_names.add(getTextString("param."+INDEX_PARAM+".au", lang));
	index_names.add(getTextString("param."+INDEX_PARAM+".su", lang));
	index_names.add(getTextString("param."+INDEX_PARAM+".ti", lang));
	index_names.add(getTextString("param."+INDEX_PARAM+".de", lang));
	index_names.add(getTextString("param."+INDEX_PARAM+".fu", lang));
    }
    
}
