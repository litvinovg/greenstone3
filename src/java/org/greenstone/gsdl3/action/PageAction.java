package org.greenstone.gsdl3.action;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
//XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.io.File;

import org.apache.log4j.*;

public class PageAction extends Action
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.PageAction.class.getName());

	public static final String HOME_PAGE = "home";
	public static final String ABOUT_PAGE = "about";
	public static final String PREFS_PAGE = "pref";
	public static final String GLI4GS3_PAGE = "gli4gs3";

	public Node process(Node message_node)
	{
		Element message = this.converter.nodeToElement(message_node);

		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		// the page name is the subaction
		String page_name = request.getAttribute(GSXML.SUBACTION_ATT);
		if (page_name.equals(""))
		{ // if no page specified, assume home page
			page_name = HOME_PAGE;
		}
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
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

			logger.error("unknown page specified!");
			response = unknownPage(request);
		}

		result.appendChild(this.doc.importNode(response, true));
		logger.debug("page action result: " + this.converter.getPrettyString(result));
		return result;
	}

	protected Element homePage(Element request)
	{
		UserContext userContext = new UserContext(request);
		// first, get the message router info
		Element info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element coll_list_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		info_message.appendChild(coll_list_request);
		Element info_response_message = (Element) this.mr.process(info_message);
		if (info_response_message == null)
		{
			logger.error(" couldn't query the message router!");
			return null;
		}
		Element info_response = (Element) GSXML.getChildByTagName(info_response_message, GSXML.RESPONSE_ELEM);
		if (info_response == null)
		{
			logger.error("couldn't query the message router!");
			return null;
		}

		// second, get the metadata for each collection - we only want specific
		// elements but for now, we'll just get it all
		Element collection_list = (Element) GSXML.getChildByTagName(info_response, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		logger.debug(GSXML.xmlNodeToString(collection_list, false));
		if (collection_list != null)
		{
			NodeList colls = GSXML.getChildrenByTagName(collection_list, GSXML.COLLECTION_ELEM);
			if (colls.getLength() > 0)
			{
				sendMultipleRequests(colls, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
			}
		}

		// get metadata for any services
		Element service_list = (Element) GSXML.getChildByTagName(info_response, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		if (service_list != null)
		{
			NodeList services = GSXML.getChildrenByTagName(service_list, GSXML.SERVICE_ELEM);
			if (services.getLength() > 0)
			{
				sendMultipleRequests(services, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
			}
		}

		// get metadata for service clusters
		Element cluster_list = (Element) GSXML.getChildByTagName(info_response, GSXML.CLUSTER_ELEM + GSXML.LIST_MODIFIER);
		if (cluster_list != null)
		{
			NodeList clusters = GSXML.getChildrenByTagName(cluster_list, GSXML.CLUSTER_ELEM);
			if (clusters.getLength() > 0)
			{
				sendMultipleRequests(clusters, null, GSXML.REQUEST_TYPE_DESCRIBE, userContext);

			}
		}

		// all the components have been merged into info_response
		return info_response;

	} // homePage

	protected Element aboutPage(Element request)
	{
		UserContext userContext = new UserContext(request);
		// extract the params from the cgi-request, 
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(cgi_paramList, false);

		String coll_name = (String) params.get(GSParams.COLLECTION);
		if (coll_name == null || coll_name.equals(""))
		{
			logger.error("about page requested with no collection or cluster specified!");
			// return an empty response
			Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
			addSiteMetadata(response, userContext);
			addInterfaceOptions(response);
			return response;
		}

		// get the collection or cluster description
		Element coll_about_message = this.doc.createElement(GSXML.MESSAGE_ELEM);

		Element coll_about_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, coll_name, userContext);
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

		NodeList paramList = request.getElementsByTagName(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList.getLength() != 0)
		{
			for (int i = 0; i < paramList.getLength(); i++)
			{
				Element e = (Element) paramList.item(i);
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
			sendMultipleRequests(services, coll_name, GSXML.REQUEST_TYPE_DESCRIBE, userContext);
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
		UserContext userContext = new UserContext(request);
		String page_name = request.getAttribute(GSXML.SUBACTION_ATT);

		// extract the params from the cgi-request, 
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(cgi_paramList, false);

		String coll_name = (String) params.get(GSParams.COLLECTION);
		if (coll_name == null || coll_name.equals(""))
		{
			// just return an empty response
			Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
			addSiteMetadata(response, userContext);
			addInterfaceOptions(response);
			return response;
		}

		// else get the coll description - actually this is the same as for the about page - should we merge these two methods??

		// if there is a service specified should we get the service description instead??
		// get the collection or cluster description
		Element coll_about_message = this.doc.createElement(GSXML.MESSAGE_ELEM);

		Element coll_about_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, coll_name, userContext);
		coll_about_message.appendChild(coll_about_request);

		Element coll_about_response = (Element) this.mr.process(coll_about_message);

		Element response = (Element) GSXML.getChildByTagName(coll_about_response, GSXML.RESPONSE_ELEM);

		//add the site metadata
		addSiteMetadata(response, userContext);
		addInterfaceOptions(response);

		return response;

	}

	protected boolean sendMultipleRequests(NodeList items, String path_prefix, String request_type, UserContext userContext)
	{
		// we will send all the requests in a single message
		Element message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		for (int i = 0; i < items.getLength(); i++)
		{
			Element c = (Element) items.item(i);
			String path = c.getAttribute(GSXML.NAME_ATT);
			if (path_prefix != null)
			{
				path = GSPath.appendLink(path_prefix, path);
			}
			Element request = GSXML.createBasicRequest(this.doc, request_type, path, userContext);
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
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

		Element applet_elem = this.doc.createElement("Applet");
		page_response.appendChild(applet_elem);
		applet_elem.setAttribute("ARCHIVE", "SignedGatherer.jar");
		applet_elem.setAttribute("CODE", "org.greenstone.gatherer.GathererApplet");
		applet_elem.setAttribute("CODEBASE", "applet");
		applet_elem.setAttribute("HEIGHT", "50");
		applet_elem.setAttribute("WIDTH", "380");
		Element gwcgi_param_elem = this.doc.createElement("PARAM");
		gwcgi_param_elem.setAttribute("name", "gwcgi");
		String library_name = GlobalProperties.getGSDL3WebAddress();
		gwcgi_param_elem.setAttribute("value", library_name);
		applet_elem.appendChild(gwcgi_param_elem);

		Element gsdl3_param_elem = this.doc.createElement("PARAM");
		gsdl3_param_elem.setAttribute("name", "gsdl3");
		gsdl3_param_elem.setAttribute("value", "true");
		applet_elem.appendChild(gsdl3_param_elem);

		return page_response;
	}
}
