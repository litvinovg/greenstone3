package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.util.Misc;
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList; 

import java.util.HashMap;
import java.util.ArrayList;
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
 * @author Katherine Don
 * @version $Revision$
 * Modified by  <a href="mailto:chi@cs.waikato.ac.nz">Chi-Yu Huang</a>
 */

public class IViaRetrieve 
    extends AbstractDocumentRetrieve {

      static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.IViaRetrieve.class.getName());
    

    protected String ivia_server_url = null;
    
    public IViaRetrieve() {
	does_structure = false;
    }
    
    //Configure IViaRetrieve Service
    public boolean configure(Element info, Element extra_info) 
    {
	if (!super.configure(info, extra_info)) {
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
	return true;
	
    }
        
    /** gets a document by sending a request to iVia, then processes it and creates a documentNode around the text */
    protected Element getNodeContent(String doc_id, String lang) 
	throws GSException {

	String url_string = ivia_server_url+"/cgi-bin/view_record?theme=gsdl3&record_id="+doc_id;

	StringBuffer buffer = new StringBuffer();
	try {
	    BufferedReader reader = Misc.makeHttpConnection(url_string);
    	    String line;
	    while((line = reader.readLine())!= null) {
		buffer.append(line);
	    }
   	} catch (java.net.MalformedURLException e) {
	    throw new GSException("Malformed URL: "+url_string, GSXML.ERROR_TYPE_SYSTEM);
	} catch (java.io.IOException e) {
	    throw new GSException("IOException during connection to "+url_string+": "+e.toString(),GSXML.ERROR_TYPE_SYSTEM);
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
	    
	Document content_doc = this.converter.getDOM(processed_content.toString());
	if (content_doc == null) {
	    logger.error("Couldn't parse node content");
	    logger.error(processed_content.toString());
	    return null;
	}
	
	Element content_element = content_doc.getDocumentElement();

	return (Element)this.doc.importNode(content_element,true);
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

    protected String translateId(String oid){
	int p = oid.lastIndexOf('.');
	if (p != oid.length()-3) {
	    logger.info("translateoid error: '.' is not the third to last char!!");
	    return oid;
	}
	String top = oid.substring(0, p);
	return top;
    }

    protected String translateExternalId(String id){
	return id;
    }

    protected String getDocType(String node_id){
    	return GSXML.DOC_TYPE_SIMPLE;
    }
    protected String getRootId(String node_id){
   	return node_id; 
    }

    protected ArrayList<String> getChildrenIds(String node_id){
	return null; 
    }
    
    protected String getParentId(String node_id){
	return null;
    }
    
    protected Element getMetadataList (String doc_id,
				       boolean all_metadata,
				       ArrayList<String> metadata_names,
				       String lang) 
	throws GSException {
	
	Element meta_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);

	// do the query to the iVia server
	StringBuffer field_list= new StringBuffer();
	boolean metadata_found = false;

	for (int i=0; i<metadata_names.size();i++){
	    if (isAcceptableMetadata(metadata_names.get(i))){
		metadata_found = true;
		field_list.append(metadata_names.get(i));
		field_list.append(",");
	    }
	}
	if (!metadata_found){
	    return meta_list;
	}
	
	String url_string = ivia_server_url+"/cgi-bin/view_record_set?theme=gsdl3&record_id_list="+doc_id+"&field_list="+field_list.toString(); 
	try {
	    BufferedReader reader = Misc.makeHttpConnection(url_string);
	    String line;
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
	} catch (java.net.MalformedURLException e) {
	    throw new GSException("Malformed URL: "+url_string, GSXML.ERROR_TYPE_SYSTEM);
	} catch (java.io.IOException e) {
	    throw new GSException("IOException: "+e.toString(), GSXML.ERROR_TYPE_SYSTEM);
	}
	return meta_list;
    }
    
    protected String getStructureInfo(String doc_id, String info_type){
	return "";	
    }
}
