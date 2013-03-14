package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.util.*;

// XML classes
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

		Element message = this.converter.nodeToElement(message_node);

		// assume only one request
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		UserContext userContext = new UserContext(request);
		// get the param list
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		String service_name = "RSSFeed"; // RSSFeed service of RSSRetrieve.java
		String collection = (String) params.get(GSParams.COLLECTION);
		String to = GSPath.prependLink(service_name, collection); // collection/RSSFeed
		
		// the first part of the response is the service description 
		// for now get this again from the service. 
		// this should be cached somehow later on. 
    
		Element mr_request_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element rss_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		mr_request_message.appendChild(rss_request);

		Element rss_response = (Element) this.mr.process(mr_request_message);
		rss_response = (Element) GSXML.getChildByTagName(rss_response, GSXML.RESPONSE_ELEM); // just the response tag
		// NEED ERROR PROCESSING ?

		// siteMeta and interfaceOptions are unnecessary, as rss.xsl is going to remove it anyway
		// but may be handy when doing o=xml to view the original xml as it came through from the GS server
		addSiteMetadata(rss_response, userContext);
		addInterfaceOptions(rss_response);

		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		result.appendChild(this.doc.importNode(rss_response, true));
		return result;

	}

}
