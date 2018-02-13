package org.greenstone.gsdl3.action;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageAction extends Action
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.PageAction.class.getName());

	public static final String HOME_PAGE = "home";
	public static final String ABOUT_PAGE = "about";
	public static final String PREFS_PAGE = "pref";
	public static final String GLI4GS3_PAGE = "gli4gs3";

	public Node process(Node message_node)
	
	{
		Element message = GSXML.nodeToElement(message_node);
		Document doc = XMLConverter.newDOM();
	    
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		String collection = "";
		if (paramList != null)
		{
			HashMap<String, Serializable> params = GSXML.extractParams(paramList, false);
			if (params != null && params.get(GSParams.COLLECTION) != null)
			{
				collection = (String) params.get(GSParams.COLLECTION);
			}
		}

		// the page name is the subaction
		String page_name = request.getAttribute(GSXML.SUBACTION_ATT);
		if (page_name.equals(""))
		{ // if no page specified, assume home page
			page_name = HOME_PAGE;
		}
		Element result = doc.createElement(GSXML.MESSAGE_ELEM);
		Element response;
		if (page_name.equals(HOME_PAGE))
		{
		  response = homePage(request);
			//} else if (page_name.equals(ABOUT_PAGE)) {
		}
		else if (page_name.equals(ABOUT_PAGE) || page_name.equals(PREFS_PAGE))
		{
		  response = aboutPage(request);
			//}else if (page_name.equals(PREFS_PAGE)) {
			//response = prefsPage(request);
		}
		else if (page_name.equals(GLI4GS3_PAGE))
		{
		  response = gli4gs3Page(request);
		}
		else
		{ // unknown page
		  response = unknownPage(request);
		}

		Element formatMessage = doc.createElement(GSXML.MESSAGE_ELEM);
		Element formatRequest = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_FORMAT, collection, new UserContext(request));
		formatMessage.appendChild(formatRequest);
		Element formatResponseMessage = (Element) this.mr.process(formatMessage);
		Element formatResponse = (Element) GSXML.getChildByTagName(formatResponseMessage, GSXML.RESPONSE_ELEM);

		Element globalFormat = (Element) GSXML.getChildByTagName(formatResponse, GSXML.FORMAT_ELEM);
		if (globalFormat != null)
		{
		  response.appendChild(response.getOwnerDocument().importNode(globalFormat, true));
		}

		result.appendChild(doc.importNode(response, true));
		logger.debug("page action result: " + this.converter.getPrettyString(result));

		return result;
	}

  protected Element homePage(Element request)
	{
	  Document doc = XMLConverter.newDOM();
		

		UserContext userContext = new UserContext(request);
		// first, get the message router info
		Element info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element info_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		//Create param list
		Element param_list_element = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		info_request.appendChild(param_list_element);
		//Describe params without collectionlist. Collectionlist provided by CollectionGroup service
		GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM, GSXML.CLUSTER_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM, GSXML.SITE_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM, GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER);
		info_message.appendChild(info_request);
		//Send request to message router
		Element info_response_message = (Element) this.mr.process(info_message);
		//Check if it is not null
		if (info_response_message == null)
		{
			logger.error(" couldn't query the message router!");
			return null;
		}
		//Check if it is not null
		Element info_response = (Element) GSXML.getChildByTagName(info_response_message, GSXML.RESPONSE_ELEM);
		if (info_response == null)
		{
			logger.error("couldn't query the message router!");
			return null;
		}
		
		Element resp_service_list = (Element) GSXML.getChildByTagName(info_response, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		
		if (resp_service_list == null) {
			logger.error("No services available. Couldn't query the message router!");
			return null;
		}
		Element groupInfoService = GSXML.getNamedElement(resp_service_list, GSXML.SERVICE_ELEM, GSXML.TYPE_ATT,
				GSXML.SERVICE_TYPE_GROUPINFO);
		if (groupInfoService != null) {
			// Prepare request for CollectionGroup service to get current
			// collections and groups list
			Element group_info_message = doc.createElement(GSXML.MESSAGE_ELEM);
			Element group_info_request = GSXML.createBasicRequest(doc, GSXML.TO_ATT,
					groupInfoService.getAttribute(GSXML.NAME_ATT), userContext);
			group_info_message.appendChild(group_info_request);
			//Append group request if exists
			Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			if (paramList != null) {
				group_info_request.appendChild(doc.importNode(paramList, true));
			}
			Element group_info_response_message = (Element) this.mr.process(group_info_message);
			Element group_info_response = (Element) GSXML.getChildByTagName(group_info_response_message,
					GSXML.RESPONSE_ELEM);
			Element collection_list = (Element) GSXML.getChildByTagName(group_info_response,
					GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			// Add Collection List from CollectionGroup Service to response
			// from message router
			info_response = (Element) doc.importNode(info_response, true);
			if (collection_list != null) {
				info_response.appendChild(doc.importNode(collection_list, true));
			}
			Element group_list = (Element) GSXML.getChildByTagName(group_info_response,
					GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
			if (group_list != null) {
				info_response.appendChild(doc.importNode(group_list, true));
			}
			Element path_list = (Element) GSXML.getChildByTagName(group_info_response,
					GSXML.PATH_ELEM + GSXML.LIST_MODIFIER);
			if (path_list != null) {
				info_response.appendChild(doc.importNode(path_list, true));
			}
			// Send message to groupInfoType Services
		} else {
			// If no service with type SERVICE_TYPE_GROUPINFO could be provided
			// request message router for all available collections
			GSXML.addParameterToList(param_list_element, GSXML.SUBSET_PARAM,
					GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			info_response_message = (Element) this.mr.process(info_message);

			if (info_response_message == null) {
				logger.error(" couldn't query the message router!");
				return null;
			}
			info_response = (Element) GSXML.getChildByTagName(info_response_message, GSXML.RESPONSE_ELEM);
			if (info_response == null) {
				logger.error("couldn't query the message router!");
				return null;
			}
		}

		// second, get the metadata for each collection - we only want specific
		// elements but for now, we'll just get it all
		Element collection_list = (Element) GSXML.getChildByTagName(info_response, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		//logger.debug(GSXML.xmlNodeToString(collection_list));
		if (collection_list != null)
		{
			NodeList colls = GSXML.getChildrenByTagName(collection_list, GSXML.COLLECTION_ELEM);
			if (colls.getLength() > 0)
			{
				sendMultipleRequests(doc, colls, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
			}
		}

		// get metadata for any services
		Element service_list = (Element) GSXML.getChildByTagName(info_response, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		if (service_list != null)
		{
			NodeList services = GSXML.getChildrenByTagName(service_list, GSXML.SERVICE_ELEM);
			if (services.getLength() > 0)
			{
				sendMultipleRequests(doc, services, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
			}
		}

		// get metadata for service clusters
		Element cluster_list = (Element) GSXML.getChildByTagName(info_response, GSXML.CLUSTER_ELEM + GSXML.LIST_MODIFIER);
		if (cluster_list != null)
		{
			NodeList clusters = GSXML.getChildrenByTagName(cluster_list, GSXML.CLUSTER_ELEM);
			if (clusters.getLength() > 0)
			{
				sendMultipleRequests(doc, clusters, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);

			}
		}

		addSiteMetadata(info_response, userContext);
		addInterfaceOptions(info_response);
		// all the components have been merged into info_response
		return info_response;

	} // homePage

	protected Element aboutPage(Element request)
	{
	  Document doc = XMLConverter.newDOM();
		
		UserContext userContext = new UserContext(request);
		// extract the params from the cgi-request, 
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_paramList, false);

		String coll_name = (String) params.get(GSParams.COLLECTION);
		if (coll_name == null || coll_name.equals(""))
		{
			logger.error("about page requested with no collection or cluster specified!");
			// return an empty response
			Element response = doc.createElement(GSXML.RESPONSE_ELEM);
			addSiteMetadata(response, userContext);
			addInterfaceOptions(response);
			return response;
		}

		// get the collection or cluster description
		Element coll_about_message = doc.createElement(GSXML.MESSAGE_ELEM);

		Element coll_about_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, coll_name, userContext);
		coll_about_message.appendChild(coll_about_request);
		Element coll_about_response = (Element) this.mr.process(coll_about_message);

		// add collection type attribute to paramList
		String col_type = "";
		NodeList collect_elem = coll_about_response.getElementsByTagName(GSXML.COLLECTION_ELEM);
		if (collect_elem.getLength() != 0)
		{
			for (int i = 0; i < collect_elem.getLength(); i++)
			{
				Element e = (Element) collect_elem.item(i);
				col_type = e.getAttribute(GSXML.TYPE_ATT);
			}
		}
		else
		{
			logger.error(GSXML.COLLECTION_ELEM + " element is null");
		}

		// adding a ct param to paramlist. only needed for gs2 interface, not default
		NodeList paramList_list = request.getElementsByTagName(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList_list.getLength() != 0)
		{
			for (int i = 0; i < paramList_list.getLength(); i++)
			{
				Element e = (Element) paramList_list.item(i);
				Element ct = GSXML.createParameter(request.getOwnerDocument(), GSParams.COLLECTION_TYPE, col_type.equalsIgnoreCase("mg") ? "0" : "1");
				e.appendChild(ct);
			}
		}
		else
		{
			logger.info("paramList is null!!");
		}

		if (coll_about_response == null)
		{
			return null;
		}

		// second, get the info for each service - we only want display items 
		// but for now, we'll just get it all
		NodeList services = coll_about_response.getElementsByTagName(GSXML.SERVICE_ELEM);
		if (services.getLength() > 0)
		{
			sendMultipleRequests(doc, services, coll_name, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
		}

		Element response = (Element) GSXML.getChildByTagName(coll_about_response, GSXML.RESPONSE_ELEM);
		//add the site metadata
		addSiteMetadata(response, userContext);
		addInterfaceOptions(response);
		return response;
	}

	//protected Element prefsPage(Element request) {

	//	return null;
	//}

	/** if we dont know the page type, use this method */
	protected Element unknownPage(Element request)
	{
	  Document doc = XMLConverter.newDOM();
		
		UserContext userContext = new UserContext(request);
		String page_name = request.getAttribute(GSXML.SUBACTION_ATT);

		// extract the params from the cgi-request, 
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_paramList, false);

		String coll_name = (String) params.get(GSParams.COLLECTION);
		if (coll_name == null || coll_name.equals(""))
		{
			// just return an empty response
			Element response = doc.createElement(GSXML.RESPONSE_ELEM);
			addSiteMetadata(response, userContext);
			addInterfaceOptions(response);
			return response;
		}

		// else get the coll description - actually this is the same as for the about page - should we merge these two methods??

		// if there is a service specified should we get the service description instead??
		// get the collection or cluster description
		Element coll_about_message = doc.createElement(GSXML.MESSAGE_ELEM);

		Element coll_about_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, coll_name, userContext);
		coll_about_message.appendChild(coll_about_request);

		Element coll_about_response = (Element) this.mr.process(coll_about_message);

		Element response = (Element) GSXML.getChildByTagName(coll_about_response, GSXML.RESPONSE_ELEM);

		//add the site metadata
		addSiteMetadata(response, userContext);
		addInterfaceOptions(response);

		return response;

	}

	protected boolean sendMultipleRequests(Document doc, NodeList items, String path_prefix, String request_type, UserContext userContext)
	{
		// we will send all the requests in a single message
		Element message = doc.createElement(GSXML.MESSAGE_ELEM);
		for (int i = 0; i < items.getLength(); i++)
		{
			Element c = (Element) items.item(i);
			String path = c.getAttribute(GSXML.NAME_ATT);
			if (path_prefix != null)
			{
				path = GSPath.appendLink(path_prefix, path);
			}
			Element request = GSXML.createBasicRequest(doc, request_type, path, userContext);
			message.appendChild(request);
		}

		Element response_message = (Element) this.mr.process(message);

		NodeList responses = response_message.getElementsByTagName(GSXML.RESPONSE_ELEM);
		// check that have same number of responses as requests
		if (items.getLength() != responses.getLength())
		{
			logger.error("didn't get a response for each request - somethings gone wrong!");
			return false;
		}

		for (int i = 0; i < items.getLength(); i++)
		{
			Element c1 = (Element) items.item(i);
			Element c2 = (Element) GSXML.getChildByTagName((Element) responses.item(i), c1.getTagName());
			if (c1 != null && c2 != null && c1.getAttribute(GSXML.NAME_ATT).endsWith(c2.getAttribute(GSXML.NAME_ATT)))
			{
				//add the new data into the original element
				GSXML.mergeElements(c1, c2);
			}
			else
			{
				logger.debug(" response does not correspond to request!");
			}

		}

		return true;

	}

	protected Element gli4gs3Page(Element request)
	{
	  Document doc = XMLConverter.newDOM();
		
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		Element page_response = doc.createElement(GSXML.RESPONSE_ELEM);

		Element applet_elem = doc.createElement("Applet");
		page_response.appendChild(applet_elem);
		applet_elem.setAttribute("ARCHIVE", "SignedGatherer.jar"); // SignedGatherer.jar should be placed in web/applet. 
		applet_elem.setAttribute("CODE", "org.greenstone.gatherer.WebGatherer");
		applet_elem.setAttribute("CODEBASE", "applet"); // SignedGatherer.jar is in web/applet. But CODEBASE is the *URL* path to the (jar) file containing the main class, and is relative to documentroot "web".
		applet_elem.setAttribute("HEIGHT", "50");
		applet_elem.setAttribute("WIDTH", "380");
		
		Element gwcgi_param_elem = doc.createElement("PARAM");
		gwcgi_param_elem.setAttribute("name", "gwcgi");
		String library_name = GlobalProperties.getGSDL3WebAddress();
		gwcgi_param_elem.setAttribute("value", library_name);
		applet_elem.appendChild(gwcgi_param_elem);

		Element gsdl3_param_elem = doc.createElement("PARAM");
		gsdl3_param_elem.setAttribute("name", "gsdl3");
		gsdl3_param_elem.setAttribute("value", "true");
		applet_elem.appendChild(gsdl3_param_elem);
				
		// When an applet doesn't work in the browser, set the default display text to provide a link to the JNLP file to run with Java Web Start
		// The display text will be:
		// 		Applets don't seem to work in your browser. In place of the GLI Applet, try running its alternative <a href="applet/GLIapplet.jnlp">Java Web Start (JNLP) version</a>
		Node default_text = doc.createTextNode("Applets don't seem to work in your browser. In place of the GLI Applet, try running its alternative ");
		Element link_to_jnlp = doc.createElement("a");
		link_to_jnlp.setAttribute("href", "applet/GLIapplet.jnlp");
		Node anchor_text = doc.createTextNode("Java Web Start (JNLP) version");
		link_to_jnlp.appendChild(anchor_text);		
		applet_elem.appendChild(default_text);
		applet_elem.appendChild(link_to_jnlp);
		
		return page_response;
	}
}
