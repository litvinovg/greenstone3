package org.greenstone.gsdl3.service;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DebugService extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.DebugService.class.getName());

	/**********************************************************
	 * The list of services the utility service rack supports *
	 *********************************************************/
	protected static final String GET_TEMPLATE_FROM_XML_FILE = "GetXMLTemplateFromFile";
	protected static final String SAVE_TEMPLATE_TO_XML_FILE = "SaveXMLTemplateToFile";
	protected static final String GET_TEMPLATE_LIST_FROM_FILE = "GetTemplateListFromFile";
	protected static final String GET_XSLT_FILES_FOR_COLLECTION = "GetXSLTFilesForCollection";
	protected static final String RESOLVE_CALL_TEMPLATE = "ResolveCallTemplate";
	/*********************************************************/

	String[] services = { GET_TEMPLATE_FROM_XML_FILE, SAVE_TEMPLATE_TO_XML_FILE, GET_TEMPLATE_LIST_FROM_FILE, GET_XSLT_FILES_FOR_COLLECTION, RESOLVE_CALL_TEMPLATE };

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

	protected Element processResolveCallTemplate(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, RESOLVE_CALL_TEMPLATE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, RESOLVE_CALL_TEMPLATE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext context = new UserContext(request);
		boolean found = false;
		for (String group : context.getGroups())
		{
			if (group.equals("administrator"))
			{
				found = true;
			}
		}

		if (!found)
		{
			GSXML.addError(this.doc, result, "This user does not have the required permissions to perform this action.");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, RESOLVE_CALL_TEMPLATE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String interfaceName = (String) params.get("interfaceName");
		String siteName = (String) params.get("siteName");
		String collectionName = (String) params.get("collectionName");
		String fileName = (String) params.get("fileName");
		String nameToGet = (String) params.get("templateName");

		fileName = fileName.replace("\\", "/");

		Document xslDoc = GSXSLT.mergedXSLTDocumentCascade(fileName, siteName, collectionName, interfaceName, new ArrayList<String>(), true);

		int sepIndex = fileName.lastIndexOf("/");
		String pathExtra = null;
		if (sepIndex != -1)
		{
			pathExtra = fileName.substring(0, sepIndex + 1);
			fileName = fileName.substring(sepIndex + 1);
		}

		GSXSLT.inlineImportAndIncludeFilesDebug(xslDoc, pathExtra, true, fileName, siteName, collectionName, interfaceName, new ArrayList<String>());

		NodeList templateList = xslDoc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "template");
		for (int i = 0; i < templateList.getLength(); i++)
		{
			Element current = (Element) templateList.item(i);
			if (current.hasAttribute("name") && current.getAttribute("name").equals(nameToGet))
			{
				Element debugElement = (Element) current.getElementsByTagName("debug").item(0);

				Element requestedTemplate = this.doc.createElement("requestedTemplate");
				requestedTemplate.setTextContent(debugElement.getAttribute("filename"));
				result.appendChild(requestedTemplate);
			}
		}

		return result;
	}

	protected Element processGetXMLTemplateFromFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, GET_TEMPLATE_FROM_XML_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, GET_TEMPLATE_FROM_XML_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext context = new UserContext(request);
		boolean found = false;
		for (String group : context.getGroups())
		{
			if (group.equals("administrator"))
			{
				found = true;
			}
		}

		if (!found)
		{
			GSXML.addError(this.doc, result, "This user does not have the required permissions to perform this action.");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, GET_TEMPLATE_FROM_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String locationName = (String) params.get("locationName");
		String interfaceName = (String) params.get("interfaceName");
		String siteName = (String) params.get("siteName");
		String collectionName = (String) params.get("collectionName");
		String fileName = (String) params.get("fileName");
		String namespace = (String) params.get("namespace");
		String nodeName = (String) params.get("nodename");
		String nameToGet = (String) params.get("name");
		String matchToGet = (String) params.get("match");
		String xPath = (String) params.get("xpath");

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
			GSXML.addError(this.doc, result, "A valid namespace was not specified.");
			return result;
		}

		File xslFile = createFileFromLocation(locationName, fileName, interfaceName, siteName, collectionName);
		if (xslFile.exists())
		{
			XMLConverter converter = new XMLConverter();
			Document xslDoc = converter.getDOM(xslFile, "UTF-8");
			Element rootElem = xslDoc.getDocumentElement();

			if (xPath != null && xPath.length() > 0)
			{
				String[] pathSegments = xPath.split("/");
				for (int i = 1; i < pathSegments.length; i++)
				{
					String currentSegment = pathSegments[i];
					int count = 1;
					if (currentSegment.contains("["))
					{
						count = Integer.parseInt(currentSegment.substring(currentSegment.indexOf("[") + 1, currentSegment.indexOf("]")));
						currentSegment = currentSegment.substring(0, currentSegment.indexOf("["));
					}
					Node child = rootElem.getFirstChild();
					while (count > 0)
					{
						if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getNodeName().equals(currentSegment))
						{
							rootElem = (Element) child;
							count--;
						}
						child = child.getNextSibling();
					}
				}
			}

			NodeList templateElems = rootElem.getElementsByTagNameNS(fullNamespace, nodeName);

			if (nameToGet != null && nameToGet.length() != 0)
			{
				for (int i = 0; i < templateElems.getLength(); i++)
				{
					Element template = (Element) templateElems.item(i);
					if (template.getAttribute("name").equals(nameToGet))
					{
						fixAttributes(template);

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
						fixAttributes(template);

						Element requestedTemplate = this.doc.createElement("requestedMatchTemplate");
						requestedTemplate.appendChild(this.doc.importNode(template, true));
						result.appendChild(requestedTemplate);
					}
				}
			}
		}

		return result;
	}

	protected void fixAttributes(Element template)
	{
		NodeList nodes = template.getElementsByTagName("*");
		for (int j = 0; j < nodes.getLength(); j++)
		{
			Node current = nodes.item(j);
			NamedNodeMap attributes = current.getAttributes();
			for (int k = 0; k < attributes.getLength(); k++)
			{
				Node currentAttr = attributes.item(k);
				String value = currentAttr.getNodeValue();
				if (value.contains("&") || value.contains("<") || value.contains(">") || value.contains("\""))
				{
					currentAttr.setNodeValue(value.replace("&", "&amp;amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"));
				}
			}
		}
	}

	protected Element processSaveXMLTemplateToFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, SAVE_TEMPLATE_TO_XML_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext context = new UserContext(request);
		boolean foundGroup = false;
		for (String group : context.getGroups())
		{
			if (group.equals("administrator"))
			{
				foundGroup = true;
			}
		}

		if (!foundGroup)
		{
			GSXML.addError(this.doc, result, "This user does not have the required permissions to perform this action.");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String locationName = (String) params.get("locationName");
		String fileName = (String) params.get("fileName");
		String interfaceName = (String) params.get("interfaceName");
		String siteName = (String) params.get("siteName");
		String collectionName = (String) params.get("collectionName");
		String namespace = (String) params.get("namespace");
		String nodeName = (String) params.get("nodename");
		String nameToSave = (String) params.get("name");
		String matchToSave = (String) params.get("match");
		String xPath = (String) params.get("xpath");
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

		File xslFile = createFileFromLocation(locationName, fileName, interfaceName, siteName, collectionName);
		if (xslFile.exists())
		{
			XMLConverter converter = new XMLConverter();
			Document xslDoc = converter.getDOM(xslFile, "UTF-8");

			Element rootElem = xslDoc.getDocumentElement();

			if (xPath != null && xPath.length() > 0)
			{
				String[] pathSegments = xPath.split("/");
				for (int i = 1; i < pathSegments.length; i++)
				{
					String currentSegment = pathSegments[i];
					int count = 1;
					if (currentSegment.contains("["))
					{
						count = Integer.parseInt(currentSegment.substring(currentSegment.indexOf("[") + 1, currentSegment.indexOf("]")));
						currentSegment = currentSegment.substring(0, currentSegment.indexOf("["));
					}
					Node child = rootElem.getFirstChild();
					while (count > 0)
					{
						if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getNodeName().equals(currentSegment))
						{
							rootElem = (Element) child;
							count--;
						}
						child = child.getNextSibling();
					}
				}
			}

			NodeList templateElems = xslDoc.getElementsByTagNameNS(fullNamespace, nodeName);

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
						Element newTemplate = (Element) converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"" + GSXML.XSL_NAMESPACE + "\" xmlns:java=\"" + GSXML.JAVA_NAMESPACE + "\" xmlns:util=\"" + GSXML.UTIL_NAMESPACE + "\" xmlns:gsf=\"" + GSXML.GSF_NAMESPACE + "\">" + xml + "</xsl:stylesheet>", "UTF-8").getDocumentElement().getElementsByTagNameNS(fullNamespace, nodeName).item(0);
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
		else
		{
			GSXML.addError(this.doc, result, SAVE_TEMPLATE_TO_XML_FILE + "File: " + xslFile.getAbsolutePath() + " does not exist", GSXML.ERROR_TYPE_SYNTAX);
		}

		return result;
	}

	protected Element processGetTemplateListFromFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, GET_TEMPLATE_LIST_FROM_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, GET_TEMPLATE_LIST_FROM_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext context = new UserContext(request);
		boolean found = false;
		for (String group : context.getGroups())
		{
			if (group.equals("administrator"))
			{
				found = true;
			}
		}

		if (!found)
		{
			GSXML.addError(this.doc, result, "This user does not have the required permissions to perform this action.");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, GET_TEMPLATE_FROM_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String locationName = (String) params.get("locationName");
		String siteName = (String) params.get("siteName");
		String collectionName = (String) params.get("collectionName");
		String interfaceName = (String) params.get("interfaceName");
		String fileName = (String) params.get("fileName");

		fileName.replace("/", File.separator);

		if (locationName == null || locationName.length() == 0)
		{
			GSXML.addError(this.doc, result, "Parameter \"locationName\" was null or empty.");
			return result;
		}

		File xslFile = createFileFromLocation(locationName, fileName, interfaceName, siteName, collectionName);

		if (xslFile.exists())
		{
			XMLConverter converter = new XMLConverter();
			Document xslDoc = converter.getDOM(xslFile, "UTF-8");
			Element xslDocElem = xslDoc.getDocumentElement();

			if (locationName.equals("collectionConfig"))
			{
				GSXSLT.modifyCollectionConfigForDebug(xslDocElem);
			}

			Element templateList = this.doc.createElement("templateList");
			StringBuilder templateListString = new StringBuilder("[");

			NodeList xslTemplateElems = xslDocElem.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "template");
			NodeList gsfTemplateElems = xslDocElem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "template");
			for (int i = 0; i < xslTemplateElems.getLength() + gsfTemplateElems.getLength(); i++)
			{
				Element currentElem = (i < xslTemplateElems.getLength()) ? (Element) xslTemplateElems.item(i) : (Element) gsfTemplateElems.item(i - xslTemplateElems.getLength());
				if (!currentElem.hasAttribute(GSXML.NAME_ATT) && !currentElem.hasAttribute(GSXML.MATCH_ATT))
				{
					continue;
				}

				templateListString.append("{");
				templateListString.append("\"namespace\":\"" + ((i < xslTemplateElems.getLength()) ? "xsl" : "gsf") + "\",");
				if (currentElem.hasAttribute(GSXML.NAME_ATT))
				{
					templateListString.append("\"name\":\"" + currentElem.getAttribute(GSXML.NAME_ATT) + "\",");
				}
				else if (currentElem.hasAttribute(GSXML.MATCH_ATT))
				{
					templateListString.append("\"match\":\"" + currentElem.getAttribute(GSXML.MATCH_ATT) + "\",");
				}

				if (currentElem.getUserData("xpath") != null)
				{
					templateListString.append("\"xpath\":\"" + currentElem.getUserData("xpath") + "\",");
				}
				templateListString.deleteCharAt(templateListString.length() - 1);
				templateListString.append("},");
			}

			templateListString.deleteCharAt(templateListString.length() - 1);
			templateListString.append("]");

			templateList.setTextContent(templateListString.toString());
			result.appendChild(templateList);
		}
		else
		{
			GSXML.addError(this.doc, result, "File: " + xslFile.getAbsolutePath() + " does not exist");
			return result;
		}

		return result;
	}

	protected Element processGetXSLTFilesForCollection(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, GET_XSLT_FILES_FOR_COLLECTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, GET_XSLT_FILES_FOR_COLLECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext context = new UserContext(request);
		boolean found = false;
		for (String group : context.getGroups())
		{
			if (group.equals("administrator"))
			{
				found = true;
			}
		}

		if (!found)
		{
			GSXML.addError(this.doc, result, "This user does not have the required permissions to perform this action.");
			return result;
		}

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		if (param_list == null)
		{
			GSXML.addError(this.doc, result, GET_TEMPLATE_FROM_XML_FILE + ": No param list specified", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String interfaceName = (String) params.get("interfaceName");
		String siteName = (String) params.get("siteName");
		String collectionName = (String) params.get("collectionName");

		Element fileList = this.doc.createElement("fileListJSON");
		StringBuilder fileListString = new StringBuilder("[");

		String[] placesToCheck = new String[] { GlobalProperties.getGSDL3Home() + File.separator + "interfaces" + File.separator + interfaceName + File.separator + "transform", //INTERFACE FILE PATH
		GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "transform", //SITE FILE PATH
		GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "collect" + File.separator + collectionName + File.separator + "transform", //COLLECTION FILE PATH
		GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "collect" + File.separator + collectionName + File.separator + "etc" //COLLECTION CONFIG FILE PATH
		};

		String[] shortPlaceNames = new String[] { "interface", "site", "collection", "collectionConfig" };

		for (int i = 0; i < placesToCheck.length; i++)
		{
			ArrayList<File> xslFiles = getXSLTFilesFromDirectoryRecursive(new File(placesToCheck[i]));
			for (File f : xslFiles)
			{
				fileListString.append("{\"location\":\"" + shortPlaceNames[i] + "\",\"path\":\"" + f.getAbsolutePath().replace(placesToCheck[i] + File.separator, "") + "\"},");
			}
		}
		fileListString.deleteCharAt(fileListString.length() - 1); //Remove the last , character
		fileListString.append("]");

		fileList.setTextContent(fileListString.toString());
		result.appendChild(fileList);

		return result;
	}

	protected File createFileFromLocation(String location, String filename, String interfaceName, String siteName, String collectionName)
	{
		String filePath = "";
		if (location.equals("interface"))
		{
			filePath = GlobalProperties.getGSDL3Home() + File.separator + "interfaces" + File.separator + interfaceName + File.separator + "transform" + File.separator + filename;
		}
		else if (location.equals("site"))
		{
			filePath = GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "transform" + File.separator + filename;
		}
		else if (location.equals("collection"))
		{
			filePath = GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "collect" + File.separator + collectionName + File.separator + "transform" + File.separator + filename;
		}
		else if (location.equals("collectionConfig"))
		{
			filePath = GlobalProperties.getGSDL3Home() + File.separator + "sites" + File.separator + siteName + File.separator + "collect" + File.separator + collectionName + File.separator + "etc" + File.separator + filename;
		}

		return new File(filePath);
	}

	protected ArrayList<File> getXSLTFilesFromDirectoryRecursive(File dir)
	{
		ArrayList<File> result = new ArrayList<File>();

		if (dir != null && dir.exists() && dir.isDirectory())
		{
			for (File f : dir.listFiles())
			{
				if (f.isDirectory())
				{
					result.addAll(getXSLTFilesFromDirectoryRecursive(f));
				}
				else if (f.getName().endsWith(".xsl") || f.getName().endsWith(".xml"))
				{
					result.add(f);
				}
			}
		}

		return result;
	}
}