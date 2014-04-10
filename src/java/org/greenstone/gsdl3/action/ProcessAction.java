package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.io.File;
import java.io.Serializable;

public class ProcessAction extends Action
{

	/** process a request */
	public Node process(Node message_node)
	{
		Element message = GSXML.nodeToElement(message_node);
	    Document doc = message.getOwnerDocument();
	    
		// the result
		Element result = doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// assume only one request
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		// get the param list
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);
		String service_name = (String) params.get(GSParams.SERVICE);
		String cluster_name = (String) params.get(GSParams.CLUSTER);
		String response_only_p = (String) params.get(GSParams.RESPONSE_ONLY);
		boolean response_only = false;
		if (response_only_p != null)
		{
			response_only = (response_only_p.equals("1") ? true : false);
		}
		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
		UserContext userContext = new UserContext(request);
		// what is carried out depends on the request_type
		// if rt=d, then a describe request is done,
		// is rt=r, a request and then a describe request is done
		// if rt=s, a status request is done.

		// if ro=1, then this calls for a process only page - we do the request
		// (rt should be r or s) and just give the response straight back
		// without any page processing

		// where to send requests
		String to;
		if (cluster_name != null)
		{

			to = GSPath.appendLink(cluster_name, service_name);
		}
		else
		{
			to = service_name;
		}

		if (!request_type.equals("d"))
		{
			// if rt=s or rt=r, do the request

			Element mr_query_message = doc.createElement(GSXML.MESSAGE_ELEM);
			String request_type_att;
			Element param_list = null;
			if (request_type.equals("s"))
			{ // status
				request_type_att = GSXML.REQUEST_TYPE_STATUS;
				param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				Element param = doc.createElement(GSXML.PARAM_ELEM);
				param.setAttribute(GSXML.NAME_ATT, GSParams.PROCESS_ID);
				param.setAttribute(GSXML.VALUE_ATT, (String) params.get(GSParams.PROCESS_ID));
				param_list.appendChild(param);
			}
			else
			{
				request_type_att = GSXML.REQUEST_TYPE_PROCESS;
				// add in the service params - except the ones only used by the action
				HashMap service_params = (HashMap) params.get("s1");
				if (service_params != null)
				{
					param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
					GSXML.addParametersToList(param_list, service_params);
				}

			}
			Element mr_query_request = GSXML.createBasicRequest(doc, request_type_att, to, userContext);
			if (param_list != null)
			{
				mr_query_request.appendChild(param_list);
			}
			mr_query_message.appendChild(mr_query_request);

			Element mr_query_response = (Element) this.mr.process(mr_query_message);
			Element result_response = (Element) GSXML.getChildByTagName(mr_query_response, GSXML.RESPONSE_ELEM);

			if (response_only)
			{
				// just send the reponse as is
				return result_response;
			}

			// else append the contents of the response to the page - just the status elem for now
			Element status = (Element) GSXML.getChildByTagName(result_response, GSXML.STATUS_ELEM);
			page_response.appendChild(doc.importNode(status, true));
		}

		// another part of the page is the service description

		// request the service info for the selected service - should be cached
		Element mr_info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_info_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
		mr_info_message.appendChild(mr_info_request);
		Element mr_info_response = (Element) this.mr.process(mr_info_message);

		String path = GSXML.RESPONSE_ELEM;
		path = GSPath.appendLink(path, GSXML.SERVICE_ELEM);
		Element description = (Element) doc.importNode(GSXML.getNodeByPath(mr_info_response, path), true);

		page_response.appendChild(description);

		return result;
	}

	protected Element getServiceParamList(Element cgi_param_list)
	{
		Document doc = cgi_param_list.getOwnerDocument();
		
		Element new_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element param;
		NodeList cgi_params = cgi_param_list.getChildNodes();
		for (int i = 0; i < cgi_params.getLength(); i++)
		{
			Element p = (Element) cgi_params.item(i);
			String name = p.getAttribute(GSXML.NAME_ATT);
			if (name.equals(GSParams.SERVICE) || name.equals(GSParams.REQUEST_TYPE) || name.equals(GSParams.CLUSTER))
			{
				continue;
			}
			// else add it in to the list
			new_param_list.appendChild(doc.importNode(p, true));
		}
		return new_param_list;
	}
}
