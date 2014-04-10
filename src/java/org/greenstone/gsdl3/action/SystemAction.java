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

public class SystemAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.SystemAction.class.getName());

	String tempVal = "";

	/** process a request */
	public Node process(Node message_node)
	{
		Element message = GSXML.nodeToElement(message_node);
	    Document doc = message.getOwnerDocument();
	    
		// assume only one request
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		String subaction = request.getAttribute(GSXML.SUBACTION_ATT);
		UserContext userContext = new UserContext(request);
		// get the param list
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		Element result = doc.createElement(GSXML.MESSAGE_ELEM);

		String coll = (String) params.get(GSParams.SYSTEM_CLUSTER);

		String to = "";
		if (coll != null && !coll.equals(""))
		{
			to = coll;
		}
		else if(subaction.equals("authenticated-ping")) {
			to = "RemoteAuthentication"; // not "Authentication/RemoteAuthentication": MessageRouter knows to map the RemoteAuthentication service to the Authentication module
		}

		Element mr_request_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_SYSTEM, to, userContext);
		mr_request_message.appendChild(mr_request);

		Element system = doc.createElement(GSXML.SYSTEM_ELEM);
		mr_request.appendChild(system);

		// will need to change the following if can do more than one system request at once
		if (subaction.equals("c"))
		{ // configure
			system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_CONFIGURE);
			String info = (String) params.get(GSParams.SYSTEM_SUBSET);
			system.setAttribute(GSXML.SYSTEM_SUBSET_ATT, info);
		}
		else if (subaction.equals("ping")) { // can ping the server or a collection
			String name = (String) params.get(GSParams.SYSTEM_MODULE_NAME);
			
			if(name != null && !name.equals("")) { 
				// Pinging a collection (or module) with ?a=s&sa=ping&st=collection&sn=<colName>
				// is a collection-level (servicecluster/module level) ping
				
				String type = (String) params.get(GSParams.SYSTEM_MODULE_TYPE);
				if(type == null || type.equals("")) { 
					type = "collection"; // if the st=collection was omitted, assume collection
				}				
				// ping action set to moduleType=Collection and moduleName=colName
				system.setAttribute(GSXML.SYSTEM_MODULE_NAME_ATT, name);
				system.setAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT, type);
				system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_PING);
			} // else SYSTEM_MODULE_NAME given by the "sn" GSParam is null or empty 
			  // meaning server-level ping: ?a=s&sa=ping
			  
			system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_PING);
		}
		else if (subaction.equals("authenticated-ping")) { // can check whether a given username and password authenticates
		
			String username = (String) params.get(GSParams.UN);
			String password = (String) params.get(GSParams.PW);
			
			
			system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_AUTHENTICATED_PING);
			system.setAttribute(GSXML.USERNAME_ATT, username);
			system.setAttribute(GSXML.PASSWORD_ATT, password);
			
			if(params.containsKey("col")) {//params.containsKey(GSParams.COLLECTION)) {
				String collection = (String) params.get("col");//(String) params.get(GSParams.COLLECTION);
				system.setAttribute(GSXML.COLLECTION_ATT, collection);
			}
			
		}
		
		//else if (subaction.equals("is-persistent")){
		//	system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_ISPERSISTENT);
		//}
		else
		{
			String name = (String) params.get(GSParams.SYSTEM_MODULE_NAME);
			String type = (String) params.get(GSParams.SYSTEM_MODULE_TYPE);

			system.setAttribute(GSXML.SYSTEM_MODULE_NAME_ATT, name);
			system.setAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT, type);

			if (subaction.equals("d"))
			{ // delete
				system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_DEACTIVATE);

			}
			else if (subaction.equals("a"))
			{ // add
				system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_ACTIVATE);
			}
			else
			{
				// create the default response
				// for now just have an error
				logger.error("bad subaction type");
				Element page_response = doc.createElement(GSXML.RESPONSE_ELEM);
				result.appendChild(page_response);

				return result;
			}
		}

		Node response_message = this.mr.process(mr_request_message);

		Element response = GSXML.duplicateWithNewName(doc, (Element) GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM), GSXML.RESPONSE_ELEM, true);
		addSiteMetadata(response, userContext);
		addInterfaceOptions(response);

		result.appendChild(response);
		return result;

	}

}
