package org.greenstone.gsdl3.service;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DebugService extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.DebugService.class.getName());

	/**********************************************************
	 * The list of services the utility service rack supports *
	 *********************************************************/
	protected static final String RETRIEVE_TEMPLATE_FROM_XML_FILE = "RetrieveXMLTemplateFromFile";
	protected static final String SAVE_TEMPLATE_TO_XML_FILE = "SaveXMLTemplateToFile";
	/*********************************************************/

	String[] services = { RETRIEVE_TEMPLATE_FROM_XML_FILE, SAVE_TEMPLATE_TO_XML_FILE };

	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring DebugServices...");
		this.config_info = info;

		for (int i = 0; i < services.length; i++)
		{
			Element service = this.doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service.setAttribute(GSXML.NAME_ATT, services[i]);
			this.short_service_info.appendChild(service);
		}

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		for (int i = 0; i < services.length; i++)
		{
			if (service_id.equals(services[i]))
			{
				Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
				service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
				service_elem.setAttribute(GSXML.NAME_ATT, services[i]);
				return service_elem;
			}
		}

		return null;
	}

	protected Element processRetrieveXMLTemplateFromFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, RETRIEVE_TEMPLATE_FROM_XML_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, RETRIEVE_TEMPLATE_FROM_XML_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, RETRIEVE_TEMPLATE_FROM_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String filepath = (String) params.get("filePath");
		String namespace = (String) params.get("namespace");
		String nodeName = (String) params.get("nodename");
		String nameToGet = (String) params.get("name");
		String matchToGet = (String) params.get("match");

		String fullNamespace;
		if (namespace.toLowerCase().equals("gsf"))
		{
			fullNamespace = GSXML.GSF_NAMESPACE;
		}
		else if (namespace.toLowerCase().equals("xsl"))
		{
			fullNamespace = GSXML.XSL_NAMESPACE;
		}
		else
		{
			return result;
		}

		File xslFile = new File(filepath);
		if (xslFile.exists())
		{
			XMLConverter converter = new XMLConverter();
			Document xslDoc = converter.getDOM(xslFile, "UTF-8");

			NodeList templateElems = xslDoc.getElementsByTagNameNS(fullNamespace, nodeName);

			if (nameToGet != null && nameToGet.length() != 0)
			{
				for (int i = 0; i < templateElems.getLength(); i++)
				{
					Element template = (Element) templateElems.item(i);
					if (template.getAttribute("name").equals(nameToGet))
					{
						Element requestedTemplate = this.doc.createElement("requestedNameTemplate");
						requestedTemplate.appendChild(this.doc.importNode(template, true));
						result.appendChild(requestedTemplate);
					}
				}
			}

			//Maybe should look for highest priority
			if (matchToGet != null && matchToGet.length() != 0)
			{
				for (int i = 0; i < templateElems.getLength(); i++)
				{
					Element template = (Element) templateElems.item(i);
					if (template.getAttribute("match").equals(matchToGet))
					{
						Element requestedTemplate = this.doc.createElement("requestedMatchTemplate");
						requestedTemplate.appendChild(this.doc.importNode(template, true));
						result.appendChild(requestedTemplate);
					}
				}
			}
		}

		return result;
	}

	protected Element processSaveXMLTemplateToFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, SAVE_TEMPLATE_TO_XML_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String filepath = (String) params.get("filePath");
		String namespace = (String) params.get("namespace");
		String nodeName = (String) params.get("nodename");
		String nameToSave = (String) params.get("name");
		String matchToSave = (String) params.get("match");
		String xml = (String) params.get("xml");

		String fullNamespace;
		if (namespace.toLowerCase().equals("gsf"))
		{
			fullNamespace = GSXML.GSF_NAMESPACE;
		}
		else if (namespace.toLowerCase().equals("xsl"))
		{
			fullNamespace = GSXML.XSL_NAMESPACE;
		}
		else
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": The specified namespace was not valid", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		File xslFile = new File(filepath);
		if (xslFile.exists())
		{
			XMLConverter converter = new XMLConverter();
			Document xslDoc = converter.getDOM(xslFile, "UTF-8");

			NodeList templateElems = xslDoc.getElementsByTagNameNS(fullNamespace, nodeName);

			/*
			 * NodeList textElems =
			 * xslDoc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "text"); for
			 * (int i = 0; i < textElems.getLength(); i++) {
			 * System.err.println("\n\n" + i + "\n\n" +
			 * GSXML.xmlNodeToString(textElems.item(i))); }
			 */

			boolean found = false;
			if (nameToSave != null && nameToSave.length() != 0)
			{
				for (int i = 0; i < templateElems.getLength(); i++)
				{
					Element template = (Element) templateElems.item(i);
					if (template.getAttribute("name").equals(nameToSave))
					{
						try
						{
							System.err.println(xml);
							Element newTemplate = (Element) converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"" + GSXML.XSL_NAMESPACE + "\" xmlns:java=\"" + GSXML.JAVA_NAMESPACE + "\" xmlns:util=\"" + GSXML.UTIL_NAMESPACE + "\" xmlns:gsf=\"" + GSXML.GSF_NAMESPACE + "\">" + xml + "</xsl:stylesheet>", "UTF-8").getDocumentElement().getElementsByTagNameNS(fullNamespace, nodeName).item(0);
							template.getParentNode().replaceChild(xslDoc.importNode(newTemplate, true), template);
							found = true;
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
			//Maybe should look for highest priority match
			if (matchToSave != null && matchToSave.length() != 0)
			{
				for (int i = 0; i < templateElems.getLength(); i++)
				{
					Element template = (Element) templateElems.item(i);
					if (template.getAttribute("match").equals(matchToSave))
					{

						Element newTemplate = (Element) converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"" + GSXML.XSL_NAMESPACE + "\" xmlns:java=\"" + GSXML.JAVA_NAMESPACE + "\" xmlns:util=\"" + GSXML.UTIL_NAMESPACE + "\" xmlns:gsf=\"" + GSXML.GSF_NAMESPACE + "\">" + xml + "</xsl:stylesheet>", "UTF-8").getDocumentElement().getFirstChild();
						template.getParentNode().replaceChild(xslDoc.importNode(newTemplate, true), template);
						found = true;
					}
				}
			}

			if (!found)
			{
				GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": Could not save as the specified template could not be found", GSXML.ERROR_TYPE_SYNTAX);
			}
			else
			{

				try
				{
					Transformer transformer = TransformerFactory.newInstance().newTransformer();

					//initialize StreamResult with File object to save to file
					StreamResult sresult = new StreamResult(new FileWriter(xslFile));
					DOMSource source = new DOMSource(xslDoc);
					transformer.transform(source, sresult);
				}
				catch (Exception ex)
				{
					GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": There was an error writing out the XML file", GSXML.ERROR_TYPE_SYNTAX);
				}

			}
		}

		return result;
	}
}