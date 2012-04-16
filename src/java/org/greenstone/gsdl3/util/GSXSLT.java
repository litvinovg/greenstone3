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

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.util.Vector;
import java.util.ArrayList;

/** various functions for manipulating Greenstone xslt */
public class GSXSLT
{
	/**
	 * takes a stylesheet Document, and adds in any child nodes from extra_xsl
	 * named templates overwrite any existing one, while match templates are
	 * just added to the end of teh stylesheet
	 */
	public static void mergeStylesheets(Document main_xsl, Element extra_xsl)
	{
		Element main = main_xsl.getDocumentElement();

		NodeList children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "include");
		for (int i = 0; i < children.getLength(); i++) {
		    	Node node = children.item(i);
			// remove any previous occurrences of xsl:include with the same href value
			removeDuplicateElementsFrom(main, node, "xsl:include", "href");
			main.appendChild(main_xsl.importNode(node, true));
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "import");
		for (int i = 0; i < children.getLength(); i++) {
		    	Node node = children.item(i);
			// remove any previous occurrences of xsl:output with the same method value
			removeDuplicateElementsFrom(main, node, "xsl:import", "href");
			main.appendChild(main_xsl.importNode(node, true));
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "output");
		for (int i = 0; i < children.getLength(); i++) {
		    	Node node = children.item(i);
			// remove any previous occurrences of xsl:output with the same method value
			removeDuplicateElementsFrom(main, node, "xsl:output", "method");			
			main.appendChild(main_xsl.importNode(node, true));
		}

		children = extra_xsl.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform", "template");
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			// remove any previous occurrences of xsl:template with the same value for name
			// or even the same value for match (should we use priorities for match?)
			removeDuplicateElementsFrom(main, node, "xsl:template", "name");
			removeDuplicateElementsFrom(main, node, "xsl:template", "match");
			main.appendChild(main_xsl.importNode(node, true));
		}
	}

    // In element main, tries to find if any previous occurrence of elements with template=templateName, 
    // and whose named attribute (attributeName) has the same value as the same attribute in node.
    // If this is the case, such a previous occurrence is removed it from element main
    public static void removeDuplicateElementsFrom(Element main, Node node, String templateName, String attrName) {
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



	/**
	 * takes any import or include nodes, and creates absolute path names for
	 * the files
	 */
	public static void absoluteIncludePaths(Document stylesheet, String gsdl3_home, String site_name, String collection, String interface_name, ArrayList base_interfaces)
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
	public static Vector extractWantedMetadata(Document stylesheet, String template_name)
	{

		Vector metadata = new Vector();
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
