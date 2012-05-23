/*
 *    GSXSLT.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;

import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** various functions for manipulating Greenstone xslt */
public class GSXSLT
{
	public static void mergeStylesheets(Document main_xsl, Element extra_xsl, boolean overwrite)
	{
		mergeStylesheetsDebug(main_xsl, extra_xsl, overwrite, false, null, null);
	}

	/**
	 * takes a stylesheet Document, and adds in any child nodes from extra_xsl
	 * named templates overwrite any existing one, while match templates are
	 * just added to the end of teh stylesheet
	 */
	public static void mergeStylesheetsDebug(Document main_xsl, Element extra_xsl, boolean overwrite, boolean debug, String firstDocFileName, String secondDocFileName)
	{
		if (debug)
		{
			System.err.println("ADDING DEBUG ELEMENTS WITH FILE NAME " + firstDocFileName);
			insertDebugElements(main_xsl, firstDocFileName);
		}

		Element main = main_xsl.getDocumentElement();

		NodeList children = GSXML.getChildrenByTagName(extra_xsl, "xsl:variable");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// If the new xsl:import element is identical (in terms of href attr value) 
			// to any in the merged document, don't copy it over
			if (!isDuplicateElement(main, node, "xsl:variable", "name"))
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "import");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// If the new xsl:import element is identical (in terms of href attr value) 
			// to any in the merged document, don't copy it over
			if (!isDuplicateElement(main, node, "xsl:import", "href"))
			{
				// Import statements should be the first children of an xsl:stylesheet element
				// If firstchild is null, then this xsl:import element will be inserted at the "end"
				// Although Node.insertBefore() will first remove identical nodes before inserting, we check
				// only the href attribute to see if they're "identical" to any pre-existing <xsl:import>
				main.insertBefore(main_xsl.importNode(node, true), main.getFirstChild());
			}
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "include");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// If the new xsl:include element is identical (in terms of href attr value)
			// to any in the merged document, don't copy it over
			// Although Node.appendChild() will first remove identical nodes before appending, we check
			// only the href attribute to see if they're "identical" to any pre-existing <xsl:include>
			if (!isDuplicateElement(main, node, "xsl:include", "href"))
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// If the new xsl:output element is identical (in terms of the value for the method attr) 
			// to any in the merged document, don't copy it over
			if (!isDuplicateElement(main, node, "xsl:output", "method"))
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "template");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// remove any previous occurrences of xsl:template with the same value for name
			// or even the same value for match (should we use priorities for match?)

			if (overwrite)
			{
				removeDuplicateElementsFrom(main, node, "xsl:template", "name");
				removeDuplicateElementsFrom(main, node, "xsl:template", "match");
				main.appendChild(main_xsl.importNode(node, true));
			}
			else if (!isDuplicateElement(main, node, "xsl:template", "name"))
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		if (debug)
		{
			System.err.println("ADDING DEBUG ELEMENTS WITH FILE NAME " + secondDocFileName);
			insertDebugElements(main_xsl, secondDocFileName);
		}
	}

	protected static void insertDebugElements(Document doc, String fileName)
	{
		NodeList htmlTags = GSXML.getHTMLStructureElements(doc);
		System.err.println("HTML TAGS SIZE IS " + htmlTags.getLength());
		for (int i = 0; i < htmlTags.getLength(); i++)
		{
			Element current = (Element) htmlTags.item(i);
			if (current.getUserData("GSDEBUGFILENAME") == null)
			{
				Element xslParent = (Element) current.getParentNode();

				while (xslParent.getNamespaceURI() != "http://www.w3.org/1999/XSL/Transform" && !xslParent.getNodeName().startsWith("xsl:"))
				{
					xslParent = (Element) xslParent.getParentNode();
				}

				System.err.println("ADDING FILE NAME " + fileName);
				current.setUserData("GSDEBUGFILENAME", fileName, null);
				current.setUserData("GSDEBUGXML", xslParent.cloneNode(true), null);
			}
			else
			{
				System.err.println("ALREADY SET!");
			}
		}
	}

	public static void inlineImportAndIncludeFiles(Document doc, String pathExtra, int depth)
	{
		inlineImportAndIncludeFilesDebug(doc, pathExtra, depth, false, null);
	}

	public static void inlineImportAndIncludeFilesDebug(Document doc, String pathExtra, int depth, boolean debug, String docFileName)
	{
		XMLConverter converter = new XMLConverter();

		String path = pathExtra;
		if (path == null)
		{
			path = "";
		}

		NodeList importList = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "import");
		NodeList includeList = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "include");

		if (importList.getLength() == 0)
		{
			importList = doc.getElementsByTagName("xsl:import");
		}
		if (includeList.getLength() == 0)
		{
			includeList = doc.getElementsByTagName("xsl:include");
		}

		for (int i = 0; i < importList.getLength() + includeList.getLength(); i++)
		{
			Element current = (Element) ((i < importList.getLength()) ? importList.item(i) : includeList.item(i - importList.getLength()));
			String href = current.getAttribute("href");

			String filePath = GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), "oran") + File.separator + "transform" + File.separator + path.replace("/", File.separator) + href.replace("/", File.separator);

			try
			{
				Document inlineDoc = converter.getDOM(new File(filePath));

				String newPath = path;
				int lastSepIndex = href.lastIndexOf("/");
				if (lastSepIndex != -1)
				{
					newPath += href.substring(0, lastSepIndex + 1);
				}

				//Do this recursively
				inlineImportAndIncludeFilesDebug(inlineDoc, newPath, depth + 1, debug, filePath);
				GSXSLT.mergeStylesheetsDebug(doc, inlineDoc.getDocumentElement(), false, debug, docFileName, filePath);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return;
			}
		}

		while (importList.getLength() > 0)
		{
			Element importElem = (Element) importList.item(0);
			importElem.getParentNode().removeChild(importElem);
		}
		while (includeList.getLength() > 0)
		{
			Element includeElem = (Element) includeList.item(0);
			includeElem.getParentNode().removeChild(includeElem);
		}
	}

	public static void modifyConfigFormatForDebug(Document doc, String fileName)
	{
		NodeList templateNodes = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "template");
		if (templateNodes.getLength() == 0)
		{
			templateNodes = doc.getElementsByTagName("xsl:template");
		}

		String debugElementString = "";
		debugElementString += "<span class=\"configDebugSpan\" style=\"display:none;\">";
		debugElementString += "  \"filename\":\"" + fileName + "\",";
		debugElementString += "  \"xml\":\"<xsl:value-of select=\"util:xmlNodeToString(.)\"/>\""; //<xsl:copy><xsl:copy-of select=\"@*\"/></xsl:copy>
		debugElementString += "</span>";

		XMLConverter converter = new XMLConverter();
		Element debugElement = (Element) converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xslt:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xslt=\"output.xsl\" xmlns:gsf=\"http://www.greenstone.org/greenstone3/schema/ConfigFormat\">" + debugElementString + "</xslt:stylesheet>").getDocumentElement().getFirstChild();

		for (int i = 0; i < templateNodes.getLength(); i++)
		{
			Element currentTemplate = (Element) templateNodes.item(i);
			if (currentTemplate.getAttribute("match") != null && (currentTemplate.getAttribute("match").equals("gsf:metadata") || currentTemplate.getAttribute("match").equals("*") || currentTemplate.getAttribute("match").equals("format")))
			{
				continue;
			}

			if (currentTemplate.hasChildNodes())
			{
				currentTemplate.insertBefore(doc.importNode(debugElement.cloneNode(true), true), currentTemplate.getFirstChild());
			}
			else
			{
				currentTemplate.appendChild(doc.importNode(debugElement.cloneNode(true), true));
			}
		}
	}

	// In element main, tries to find any previous occurrence of elements with xsl-template-name=templateName, 
	// and whose named attribute (attributeName) has the same value as the same attribute in node.
	// If this is the case, such a previous occurrence is removed from element main, since
	// the new node will contain a more specific redefinition of this element.
	public static void removeDuplicateElementsFrom(Element main, Node node, String templateName, String attrName)
	{
		String attr = ((Element) node).getAttribute(attrName);
		if (!attr.equals(""))
		{
			Element old_template = GSXML.getNamedElement(main, templateName, attrName, attr);
			if (old_template != null)
			{
				main.removeChild(old_template);
			}
		}
	}

	// Call this method on elements like xsl:include, xsl:import and xsl:output
	// In element main, tries to find any previous occurrence of elements with xsl-element-name=xslName, 
	// and whose named attribute (attributeName) has the same value as the same attribute in node.
	// If this is the case, it returns true, since the element would a complete duplicate for our intents.
	public static boolean isDuplicateElement(Element main, Node node, String xslName, String attrName)
	{
		String attr = ((Element) node).getAttribute(attrName);
		if (!attr.equals(""))
		{
			Element old_element = GSXML.getNamedElement(main, xslName, attrName, attr);
			if (old_element != null)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * takes any import or include nodes, and creates absolute path names for
	 * the files
	 */
	public static void absoluteIncludePaths(Document stylesheet, String gsdl3_home, String site_name, String collection, String interface_name, ArrayList<String> base_interfaces)
	{
		Element base_node = stylesheet.getDocumentElement();
		if (base_node == null)
		{
			return;
		}
		Node child = base_node.getFirstChild();
		while (child != null)
		{
			String name = child.getNodeName();
			if (name.equals("xsl:import") || name.equals("xsl:include"))
			{
				((Element) child).setAttribute("href", GSFile.stylesheetFile(gsdl3_home, site_name, collection, interface_name, base_interfaces, ((Element) child).getAttribute("href")));
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * looks through a stylesheet for <xxx:template match='template_name'>
	 * inside this template it looks for any <xxx:value-of
	 * select='metadataList/metadata[@name=yyy]> elements, and extracts the
	 * metadata names into a Vector
	 */
	public static Vector<String> extractWantedMetadata(Document stylesheet, String template_name)
	{

		Vector<String> metadata = new Vector<String>();
		Element base_node = stylesheet.getDocumentElement();
		NodeList templates = base_node.getElementsByTagNameNS("*", "template");
		for (int i = 0; i < templates.getLength(); i++)
		{
			Element template = (Element) templates.item(i);
			String match_name = template.getAttribute("match");
			if (!match_name.equals(template_name))
			{
				continue; // we're only looking for specific templates
			}
			String mode = template.getAttribute("mode");
			if (!mode.equals(""))
			{
				continue; // we only want ones without modes - these are processing ones, not display ones
			}
			// we have one that we want to look through
			NodeList values = template.getElementsByTagNameNS("*", "value-of");
			for (int v = 0; v < values.getLength(); v++)
			{
				String select = ((Element) values.item(v)).getAttribute("select");
				if (select.startsWith("metadataList/metadata[@name="))
				{
					String[] bits = select.split("'|\"");
					// there should be two quotes in teh string, therefore 3 items, and the second one is teh one we want
					String name = bits[1];
					metadata.add(name);
				}
			}
		}
		return metadata;
	}

}
