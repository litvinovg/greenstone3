package org.greenstone.gsdl3.action;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeneralAction extends Action
{

	/** process a request */
	public Node process(Node message_node)
	{
		Element message = this.converter.nodeToElement(message_node);

		// the result
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// assume only one request
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
		logger.debug(" request=" + this.converter.getString(request));

		UserContext userContext = new UserContext(request);

		// get the param list
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		if (params.get("configChangeName") != null && params.get("configChangeValue") != null)
		{
			String optionName = (String) params.get("configChangeName");
			String optionValue = (String) params.get("configChangeValue");

			changeConfig(optionName, optionValue);
		}

		String service_name = (String) params.get(GSParams.SERVICE);
		String cluster_name = (String) params.get(GSParams.CLUSTER);
		String response_only_p = (String) params.get(GSParams.RESPONSE_ONLY);
		boolean response_only = false;
		if (response_only_p != null)
		{
			response_only = (response_only_p.equals("1") ? true : false);
		}
		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
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

		if (request_type.equals("r") || request_type.equals("s") || request_type.equals("ro"))
		{
			//do the request
			Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
			Element mr_query_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);

			if (request_type.equals("s"))
			{
				mr_query_request.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_STATUS);
			}

			mr_query_message.appendChild(mr_query_request);

			Element param_list = null;
			// add in the service params - except the ones only used by the action
			HashMap service_params = (HashMap) params.get("s1");
			if (service_params != null)
			{
				param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				GSXML.addParametersToList(this.doc, param_list, service_params);
				mr_query_request.appendChild(param_list);
			}

			Element userInformation = (Element) GSXML.getChildByTagName(request, GSXML.USER_INFORMATION_ELEM);
			if (userInformation != null)
			{
				mr_query_request.appendChild(this.doc.importNode(userInformation, true));
			}
			mr_query_request.setAttribute("remoteAddress", request.getAttribute("remoteAddress"));

			Element mr_query_response = (Element) this.mr.process(mr_query_message);
			Element result_response = (Element) GSXML.getChildByTagName(mr_query_response, GSXML.RESPONSE_ELEM);

			if (response_only)
			{
				// just send the reponse as is
				addSiteMetadata(result_response, userContext);
				addInterfaceOptions(result_response);
				return result_response;
			}
			if (result_response != null)
			{
				// else append the contents of the response to the page 
				GSXML.copyAllChildren(page_response, result_response);
			}
		}

		// another part of the page is the service description

		// request the service info for the selected service - should be cached
		Element mr_info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_info_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
		mr_info_message.appendChild(mr_info_request);
		Element mr_info_response = (Element) this.mr.process(mr_info_message);

		String path = GSXML.RESPONSE_ELEM;
		path = GSPath.appendLink(path, GSXML.SERVICE_ELEM);

		Node desNode = GSXML.getNodeByPath(mr_info_response, path);
		if (desNode != null)
		{
			page_response.appendChild((Element) this.doc.importNode(desNode, true));
		}

		addSiteMetadata(page_response, userContext);
		addInterfaceOptions(page_response);

		return result;
	}

	protected void changeConfig(String optionName, String optionValue)
	{
		if (this.config_params.get(optionName) != null)
		{
			this.config_params.put(optionName, optionValue);

			File interfaceConfigFile = new File(GSFile.interfaceConfigFile(GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get("interface_name"))));

			Document interfaceXML = null;
			try
			{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				interfaceXML = db.parse(interfaceConfigFile);
				Element topElement = interfaceXML.getDocumentElement();
				Element optionListElem = (Element) GSXML.getChildByTagName(topElement, "optionList");

				NodeList optionList = optionListElem.getElementsByTagName("option");

				for (int i = 0; i < optionList.getLength(); i++)
				{
					Element currentOption = (Element) optionList.item(i);
					if (currentOption.getAttribute(GSXML.NAME_ATT) != null && currentOption.getAttribute(GSXML.NAME_ATT).equals(optionName))
					{
						currentOption.setAttribute(GSXML.VALUE_ATT, optionValue);
					}
				}

				DOMSource source = new DOMSource(interfaceXML);
				Result xmlresult = new StreamResult(interfaceConfigFile);

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(source, xmlresult);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			logger.error("Could not set param \"" + optionName + "\" to \"" + optionValue + "\" because that option does not exist.");
		}
	}
}
