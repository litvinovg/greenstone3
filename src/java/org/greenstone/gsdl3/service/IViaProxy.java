package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList; 

import java.util.HashMap;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.net.Authenticator;
import java.net.MalformedURLException;

import org.apache.log4j.*;

/**
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */

public class IViaProxy 
    extends ServiceRack {
   
     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.IViaProxy.class.getName());
    
    // the services on offer
    // these strings must match what is found in the properties file
    protected static final String TEXT_QUERY_SERVICE = "TextQuery";
    protected static final String DOC_CONTENT_SERVICE = "DocumentContentRetrieve";
    protected static final String DOC_META_SERVICE = "DocumentMetadataRetrieve";
    protected static final String QUERY_PARAM = "query";
    protected static final String FIELD_PARAM = "fields";
    // have standard gs param names for hits per page, and start page
    // these need to be mapped to iVia params
    protected static final String GS_HITS_PARAM = "hitsPerPage";
    protected static final String IM_HITS_PARAM = "no_of_records_per_page";
    protected static final String GS_START_PAGE_PARAM = "startPage";
    protected static final String IM_START_PAGE_PARAM = "start_page_no";

    protected String ivia_server_url = null;
    
    public boolean configure(Element info, Element extra_info) {
	
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
	Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	tq_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	this.short_service_info.appendChild(tq_service);

	Element dc_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	dc_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	dc_service.setAttribute(GSXML.NAME_ATT, DOC_CONTENT_SERVICE);
	this.short_service_info.appendChild(dc_service);

	Element dm_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	dm_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	dm_service.setAttribute(GSXML.NAME_ATT, DOC_META_SERVICE);
	this.short_service_info.appendChild(dm_service);
	
	// 
	// add some format info to service map if there is any
	String path = GSPath.appendLink(GSXML.SEARCH_ELEM, GSXML.FORMAT_ELEM);
	Element format = (Element) GSXML.getNodeByPath(extra_info, path);
	if (format != null) {
	    this.format_info_map.put(TEXT_QUERY_SERVICE, this.doc.importNode(format, true));
	}

	
	// look for document display format
	path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
	Element display_format = (Element)GSXML.getNodeByPath(extra_info, path);
	if (display_format != null) {
	    this.format_info_map.put(DOC_CONTENT_SERVICE, this.doc.importNode(display_format, true));
	    // shoudl we make a copy?
	}

	return true;
	
    }
    
    protected Element getServiceDescription(String service, String lang, String subset) {
	
	if (service.equals(TEXT_QUERY_SERVICE)) {
	    Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	    tq_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
	    if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER)) {
		tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(TEXT_QUERY_SERVICE+".name", lang)));
		tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(TEXT_QUERY_SERVICE+".submit", lang)));
		tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(TEXT_QUERY_SERVICE+".description", lang)));
	    } 
	    if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER)) {
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		tq_service.appendChild(param_list);
		Element param = GSXML.createParameterDescription(this.doc, QUERY_PARAM, getTextString("param."+QUERY_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
		param_list.appendChild(param);
		String [] field_ids = {"kw", "au", "su", "ti", "de", "fu"};
		String [] field_names = {
		    getTextString("param."+FIELD_PARAM+".kw", lang), 
		    getTextString("param."+FIELD_PARAM+".au", lang), 
		    getTextString("param."+FIELD_PARAM+".su", lang), 
		    getTextString("param."+FIELD_PARAM+".ti", lang), 
		    getTextString("param."+FIELD_PARAM+".de", lang), 
		    getTextString("param."+FIELD_PARAM+".fu", lang) };
		
		param = GSXML.createParameterDescription(this.doc, FIELD_PARAM, getTextString("param."+FIELD_PARAM, lang), GSXML.PARAM_TYPE_ENUM_MULTI, "kw,au,su,ti,de,fu", field_ids, field_names);
		param_list.appendChild(param);
		
		
		String [] hits_options = {"10", "30", "50"};
		param = GSXML.createParameterDescription(this.doc, GS_HITS_PARAM, getTextString("param."+GS_HITS_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, "10", hits_options, hits_options);
		param_list.appendChild(param);
		
		param = GSXML.createParameterDescription(this.doc, GS_START_PAGE_PARAM, "", GSXML.PARAM_TYPE_INVISIBLE, "1", null, null);
		param_list.appendChild(param);
	    }
	    return tq_service;
	} 
	if (service.equals(DOC_META_SERVICE)) {
	    Element dm_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    dm_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	    dm_service.setAttribute(GSXML.NAME_ATT, DOC_META_SERVICE);
	    return dm_service;

	}
	if (service.equals(DOC_CONTENT_SERVICE)) {
	    Element dc_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    dc_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	    dc_service.setAttribute(GSXML.NAME_ATT, DOC_CONTENT_SERVICE);
	    return dc_service;
	    
	    
	}
	return null;
    }
   
    /** Process a text query - implemented by concrete subclasses */
    protected Element processTextQuery(Element request) {

	// Create a new (empty) result message
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, TEXT_QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	Element doc_node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(doc_node_list);
       

	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    return result;  // Return the empty result
	}

	// Process the request parameters
	HashMap params = GSXML.extractParams(param_list, false);

	// Make sure a query has been specified
	String query = (String) params.get(QUERY_PARAM);
	if (query == null || query.equals("")) {
	    return result;  // Return the empty result
	}
	// tidy whitespace
	query = query.replaceAll("\\s+", "+");
	String url_string = ivia_server_url+"/cgi-bin/canned_search?theme=gsdl3&query="+query; 

	// check for fields
	String fields = (String) params.get(FIELD_PARAM);
	if (fields != null && !fields.equals("")) {
	    url_string += "&fields="+fields;
	}

	//check for hits per page
	String hits_per_page = (String) params.get(GS_HITS_PARAM);
	if (hits_per_page != null && !hits_per_page.equals("")) {
	    url_string += "&"+IM_HITS_PARAM+"="+hits_per_page;
	}

	// check for start page
	String start_page = (String) params.get(GS_START_PAGE_PARAM);
	if (start_page != null && !start_page.equals("")) {
	    url_string += "&"+IM_START_PAGE_PARAM+"="+start_page;
	}
	String results_num = null;
	String doc_ids = null;
	try {
	    logger.debug("IViaProxy, sending "+url_string);
	    BufferedReader reader = makeConnection(url_string);
	    results_num = reader.readLine();
	    doc_ids = reader.readLine();
	    
	} catch (Exception e) {
	    logger.error("exception happened during query");
	    e.printStackTrace();
	    return result;
	}
	    
	if (results_num.startsWith("Resources: ")) {
	    results_num = results_num.substring(11);
	} else {
	    logger.error("badly formatted results line: "+results_num);
	    return result;
	}
	if (doc_ids.startsWith("Ids: ")) {
	    doc_ids = doc_ids.substring(5).trim();
	} else {
	    logger.error("badly formatted docs line: "+doc_ids);
	    return result;
	}
	
	// get the num docs and add to a metadata list
	Element metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER); 
	result.appendChild(metadata_list);
	
	// Add a metadata element specifying the number of matching documents
	long numdocs = Long.parseLong(results_num);
	GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", ""+numdocs);	
	String [] ids = doc_ids.split(" ");
	
	for (int d=0; d<ids.length; d++) {
	    Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
	    doc_node.setAttribute(GSXML.NODE_ID_ATT, ids[d]);
	    doc_node_list.appendChild(doc_node);
	}
	logger.debug("IViaProxy result:");
	logger.debug(this.converter.getString(result));
	return result;
	
    }
     
    protected Element processDocumentMetadataRetrieve(Element request) {
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, DOC_META_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("missing paramList.\n");
	    return result;  // Return the empty result
	}

	// The metadata information required
	StringBuffer field_list = new StringBuffer();
	Element param = GSXML.getFirstElementChild(param_list);//(Element) param_list.getFirstChild();
	while (param != null) {
	    // Identify the metadata information desired
	    if (param.getAttribute(GSXML.NAME_ATT).equals("metadata")) {
		String metadata = GSXML.getValue(param);
		if (isAcceptableMetadata(metadata)) {
		    field_list.append(metadata);
		    field_list.append(",");	
		}
	    }
	    param = (Element) param.getNextSibling();
	}
	
	if (field_list.length()==0) {
	    logger.error("no metadata specified.\n"); 
	    return result;
	}
	    
	// Get the documents
	Element request_node_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	if (request_node_list == null) {
	    logger.error("DocumentMetadataRetrieve request had no "+GSXML.DOC_NODE_ELEM+"List.\n");
 	    return result;
 	}
	
	StringBuffer record_id_list = new StringBuffer();
	
	NodeList request_nodes = request_node_list.getChildNodes();
	for (int i = 0; i < request_nodes.getLength(); i++) {
	    Element request_node = (Element) request_nodes.item(i);
	    String node_id = request_node.getAttribute(GSXML.NODE_ID_ATT);
	    record_id_list.append(node_id);
	    record_id_list.append(",");
	}

	// do the query to the iVia server
	String url_string = ivia_server_url+"/cgi-bin/view_record_set?theme=gsdl3&record_id_list="+record_id_list.toString()+"&field_list="+field_list.toString(); 

	Element node_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(node_list);
	try {
	    BufferedReader reader = makeConnection(url_string);
	    String line;
	    while ((line = reader.readLine()) != null) {
		if (!line.startsWith("Record:")) {
		    continue;
		}
		// the first line is the record
		line=line.substring(8);
		Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		doc_node.setAttribute(GSXML.NODE_ID_ATT, line);
		Element meta_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
		doc_node.appendChild(meta_list);
		while  ((line = reader.readLine()) != null) {
		    //metadata entry
		    int col_pos = line.indexOf(':');
		    if (col_pos == -1) {
			// end of the metadata for this doc
			break; 
		    }
		    String name = line.substring(0,col_pos);
		    String value = line.substring(col_pos+2); // includes a space
		    GSXML.addMetadata(this.doc, meta_list, name, value);
		}
		node_list.appendChild(doc_node);
		
	    }
	} catch (Exception e) {
	    logger.error("exception happened");
	    e.printStackTrace();
	}
	logger.debug("IViaProxy: returning result: ");
	logger.debug(this.converter.getPrettyString(result));
	return result;
 
    }

    protected Element processDocumentContentRetrieve(Element request) {
	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, DOC_CONTENT_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	
	// Get the request doc_list
	Element query_doc_list = (Element) GSXML.getChildByTagName(request, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	if (query_doc_list == null) {
	    logger.error("DocumentContentRetrieve request specified no doc nodes.\n");
	    return result;
	}

	Element doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(doc_list);
	
	// Get the documents
	String[] doc_ids = GSXML.getAttributeValuesFromList(query_doc_list, 
						     GSXML.NODE_ID_ATT);
	for (int i = 0; i < doc_ids.length; i++) {
	    String doc_id = doc_ids[i];
	    Element doc_node = getDocument(doc_id); 
	    doc_list.appendChild(doc_node);
	}
	return result;

    }


    /** gets a document by sending a request to iVia, then processes it and creates a documentNode around the text */
    protected Element getDocument(String doc_id) {

	String url_string = ivia_server_url+"/cgi-bin/view_record?theme=gsdl3&record_id="+doc_id;
	StringBuffer buffer = new StringBuffer();
	try {
	    BufferedReader reader = makeConnection(url_string);
    
	    String line;
	    while((line = reader.readLine())!= null) {
		buffer.append(line);
	    }

	    
	} catch (Exception e) {
	    logger.error("exception happened");
	    e.printStackTrace();
	}
	
	String node_content = buffer.toString(); 
	String escaped_content = GSXML.xmlSafe(node_content);
	
	StringBuffer processed_content = new StringBuffer(escaped_content.length());
	processed_content.append("<nodeContent>");
	int pos = 0;
	int lastpos = 0;
	while ((pos = escaped_content.indexOf("&lt;a ", lastpos))!= -1) {
	    processed_content.append(escaped_content.substring(lastpos, pos));
	    int endpos = escaped_content.indexOf("&lt;/a&gt;", pos);
	    if (endpos == -1) {
		break;
	    }
	    String link = escaped_content.substring(pos, endpos+10);
	    link = convertLink(link);
	    processed_content.append(link);
	    lastpos = endpos+10;
	}
	processed_content.append(escaped_content.substring(lastpos)); // get the last bit
	processed_content.append("</nodeContent>");
	    
	Element doc_node = this.doc.createElement(GSXML.DOC_NODE_ELEM);
	doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_id);
	
	Document content_doc = this.converter.getDOM(processed_content.toString());
	if (content_doc != null) {
	    Element content_element = content_doc.getDocumentElement();
	    doc_node.appendChild(this.doc.importNode(content_element, true));
	} else {
	    logger.error("Couldn't parse the node content");
	}
	return doc_node;
	
    }

    /** converts a url from an <a> element into a greenstone suitable one */
    protected String convertLink(String aref) {
	
	if (aref.indexOf("href=&quot;http") != -1) {
	    return aref; // an external link
	}
	String type = "other";
	if (aref.indexOf("/cgi-bin/canned_search")!=-1) {
	    type="query";
	} else if (aref.indexOf("/cgi-bin/click_through") != -1) {
	    type = "external";
	} else if (aref.indexOf("/cgi-bin/view_record") != -1) {
	    type="document";
	} 
	
	int href_start = aref.indexOf("href=&quot;")+11;
	int href_end = aref.indexOf("&gt;", href_start);
	String href = aref.substring(href_start, href_end);
	String link_content = aref.substring(href_end+4, aref.length()-10);
	
	if (type.equals("external")) {
	    // the external link is everything after the http at the end.
	    String address = href.substring(href.lastIndexOf("http"));
	    address = address.replaceAll("%3[aA]", ":");
	    address = address.replaceAll("%2[fF]", "/");

	    return "&lt;a href=\""+address+"\"&gt;"+link_content+"&lt;/a&gt;";
	}
	if (type.equals("other")) {
	    return "other type of link ("+link_content+")";
	}
	StringBuffer result = new StringBuffer();
	result.append("<link type='");
	result.append(type);
	result.append("'");
	if (type.equals("query")) {
	    result.append(" service='TextQuery'");
	}
	result.append(">");
	// add in the parameters
	href = href.substring(href.indexOf("?")+1);
	String [] params = href.split("&amp;");
	for (int i=0; i<params.length; i++) {
	    String param = params[i];
	    int eq_pos = param.indexOf("=");
	    if (eq_pos != -1) {
		
		result.append("<param name='"+param.substring(0, eq_pos)+"' value='"+param.substring(eq_pos+1)+"'/>");
	    }
	}
	result.append(link_content);
	result.append("</link>");

	return result.toString();
	    
    }

    // iVia craps out if we ask for a metadata which is not valid. So need
    // to make sure we only ask for acceptable fields.
    protected boolean isAcceptableMetadata(String meta) {
	String valid_metadata = ",title,url,ivia_description,keywords,subjects,";
	if (valid_metadata.indexOf(","+meta+",")!=-1) {
	    return true;
	}
	return false;
    }
    protected BufferedReader makeConnection(String url_string) {
	BufferedReader reader = null;
	try {
	    URL url = new URL(url_string);
	    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	    InputStream input = connection.getInputStream();
	    reader = new BufferedReader(new InputStreamReader(input));  
	} catch (java.net.MalformedURLException e) {

	    logger.error("Malformed URL: "+url_string);
	} catch (java.io.IOException e) {
	    logger.error("An error occurred during IO to url "+url_string);
	}
	return reader;
    }

}
