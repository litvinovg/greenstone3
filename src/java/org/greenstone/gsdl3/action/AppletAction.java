package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.*;

/** action class for handling applets */
public class AppletAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.AppletAction.class.getName());

	public Node process(Node message_node)
	{

		Element message = this.converter.nodeToElement(message_node);

		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// get the collection and service params
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		// request_type is display (d) or request (r)
		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
		if (!request_type.equals("d") && !request_type.equals("r"))
		{
			logger.error("AppletAction Error: the rt arg should be either d or r, instead it was " + request_type + "!");
			return result;
		}

		String collection = (String) params.get(GSParams.COLLECTION);
		boolean coll_specified = true;
		String service_name = (String) params.get(GSParams.SERVICE);
		UserContext userContext = new UserContext(request);
		String to = null;
		if (collection == null || collection.equals(""))
		{
			coll_specified = false;
			to = service_name;
		}
		else
		{
			to = GSPath.appendLink(collection, service_name);
		}

		if (request_type.equals("r"))
		{
			// we are processing stuff for the applet send a message to the service, type="query", and take out the something element, and return that as our result - the applet must take xml

			Element mr_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
			Element mr_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
			mr_message.appendChild(mr_request);
			// just append all the params for now - should filter out unneeded ones
			mr_request.appendChild(this.doc.importNode(cgi_param_list, true));

			// process the request
			Element mr_response = (Element) this.mr.process(mr_message);
			// get the applet data out and pass it back as is. 
			String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.APPLET_DATA_ELEM);
			Element applet_info = GSXML.getFirstElementChild(GSXML.getNodeByPath(mr_response, path));
			//Element applet_info = (Element)GSXML.getNodeByPath(mr_response, path).getFirstChild();
			return applet_info;

		}

		// get the applet description, and the collection info if a collection is specified

		Element mr_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element applet_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
		mr_message.appendChild(applet_request);

		Element mr_response = (Element) this.mr.process(mr_message);

		// add in the applet info
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.SERVICE_ELEM);
		//path = GSPath.appendLink(path, GSXML.APPLET_ELEM);
		Element service_elem = (Element) this.doc.importNode(GSXML.getNodeByPath(mr_response, path), true);
		Element applet_elem = (Element) GSXML.getChildByTagName(service_elem, GSXML.APPLET_ELEM);
		// must handle any params that have values that are not 
		// necessarily known by the service
		// should this be done here or by web receptionist??
		// cant really have an applet without web?
		editLocalParams(applet_elem, (String) config_params.get(GSConstants.LIBRARY_NAME), collection);
		page_response.appendChild(service_elem);

		//append site metadata
		addSiteMetadata(page_response, userContext);
		//addInterfaceOptions(page_response);
		return result;

	}

	/**
	 * this method looks through the PARAMs of the applet description for
	 * 'library' or 'collection'. If found, the params are set to the
	 * appropriate values should this be done here or in the receptionist?
	 */
	protected void editLocalParams(Element description, String library_name, String full_collection_name)
	{

		Node child = description.getFirstChild();
		while (child != null)
		{
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String param = child.getNodeName();
				if (param.equals("PARAM") || param.equals("param"))
				{
					String name = ((Element) child).getAttribute("NAME");
					if (name == null || name.equals(""))
					{
						// somethings wrong!!
					}
					else if (name.equals("library"))
					{
						((Element) child).setAttribute("VALUE", library_name);
					}
					else if (name.equals("collection"))
					{
						((Element) child).setAttribute("VALUE", full_collection_name);
					}

				}
			}
			child = child.getNextSibling();
		}
	}

}
