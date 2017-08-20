package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

// other java stuff
import java.util.*;
import java.io.Serializable;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.log4j.*;

public class RSSAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.RSSAction.class.getName());

	/** process a request */
	public Node process(Node message_node)
	{
		
		Element message = GSXML.nodeToElement(message_node);
	    Document doc = message.getOwnerDocument();
	    
		// assume only one request
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		UserContext userContext = new UserContext(request);
		// get the param list
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		String service_name = "RSSFeed"; // RSSFeed service of RSSRetrieve.java
		String collection = (String) params.get(GSParams.COLLECTION);
		String to = GSPath.prependLink(service_name, collection); // collection/RSSFeed
		// Get baseUrl for links in RSS Feed
		String baseUrl = request.getAttribute("baseURL");
		
		
		// the first part of the response is the service description 
		// for now get this again from the service. 
		// this should be cached somehow later on. 
    
		Element mr_request_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element rss_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		//Pass baseURL to processRSSFeed
		rss_request.setAttribute("baseURL", baseUrl);
		mr_request_message.appendChild(rss_request);
		
		Element rss_response = (Element) this.mr.process(mr_request_message);
		rss_response = (Element) GSXML.getChildByTagName(rss_response, GSXML.RESPONSE_ELEM); // just the response tag
		if (rss_response == null) {
		  //RSS service not available
		  rss_response = doc.createElement(GSXML.RESPONSE_ELEM);
		  
		  GSXML.addError(rss_response, "RSS service not available for this collection");
		}
		// NEED ERROR PROCESSING ?

		// siteMeta and interfaceOptions are unnecessary, as rss.xsl is going to remove it anyway
		// but may be handy when doing o=xml to view the original xml as it came through from the GS server
		addSiteMetadata(rss_response, userContext);
		addInterfaceOptions(rss_response);

		Element result = doc.createElement(GSXML.MESSAGE_ELEM);
		result.appendChild(doc.importNode(rss_response, true));
		return result;

	}

}
