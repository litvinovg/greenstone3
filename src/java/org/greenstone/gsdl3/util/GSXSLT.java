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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/** various functions for manipulating Greenstone xslt */
public class GSXSLT
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSXSLT.class.getName());

	public static void mergeStylesheets(Document main_xsl, Element extra_xsl, boolean overwrite)
	{
		mergeStylesheetsDebug(main_xsl, extra_xsl, overwrite, false, null, null);
	}

	/**
	 * takes a stylesheet Document, and adds in any child nodes from extra_xsl
	 * named templates overwrite any existing one, while match templates are
	 * just added to the end of the stylesheet
	 * 
	 * elements are added in following order, and added to preserve original
	 * order with imported ones coming after existing ones import, include,
	 * output, variable, template
	 */
	public static void mergeStylesheetsDebug(Document main_xsl, Element extra_xsl, boolean overwrite, boolean debug, String firstDocFileName, String secondDocFileName)
	{
		if (debug)
		{
			insertDebugElements(main_xsl, firstDocFileName);
		}

		Element main = main_xsl.getDocumentElement();
		Node insertion_point = null;
		Element last_import = GSXML.getLastElementByTagNameNS(main, GSXML.XSL_NAMESPACE, "import");
		if (last_import != null)
		{
			insertion_point = last_import.getNextSibling();
		}
		else
		{
			insertion_point = main.getFirstChild();
		}

		// imports
		NodeList children = extra_xsl.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "import");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// If the new xsl:import element is identical (in terms of href attr value) 
			// to any in the merged document, don't copy it over
			if (GSXML.getNamedElementNS(main, GSXML.XSL_NAMESPACE, "import", "href", node.getAttribute("href")) == null)
			{
				// Import statements should be the first children of an xsl:stylesheet element
				// If firstchild is null, then this xsl:import element will be inserted at the "end"
				// Although Node.insertBefore() will first remove identical nodes before inserting, we check
				// only the href attribute to see if they're "identical" to any pre-existing <xsl:import>
				//main.insertBefore(main_xsl.importNode(node, true), main.getFirstChild());
				main.insertBefore(main_xsl.importNode(node, true), insertion_point);
			}
		}

		// do we have a new insertion point??
		Element last_include = GSXML.getLastElementByTagNameNS(main, GSXML.XSL_NAMESPACE, "include");
		if (last_include != null)
		{
			insertion_point = last_include.getNextSibling();
		}

		// includes
		children = extra_xsl.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "include");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// If the new xsl:include element is identical (in terms of href attr value)
			// to any in the merged document, don't copy it over
			// Although Node.appendChild() will first remove identical nodes before appending, we check
			// only the href attribute to see if they're "identical" to any pre-existing <xsl:include>
			if (GSXML.getNamedElementNS(main, GSXML.XSL_NAMESPACE, "include", "href", node.getAttribute("href")) == null)
			{
				//main.appendChild(main_xsl.importNode(node, true));
				main.insertBefore(main_xsl.importNode(node, true), insertion_point);
			}
		} // for each include

		if (main.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "output").getLength() == 0)
		{
			// outputs
			children = extra_xsl.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "output");
			for (int i = 0; i < children.getLength(); i++)
			{
				Element node = (Element) children.item(i);
				// If the new xsl:output element is identical (in terms of the value for the method attr) 
				// to any in the merged document, don't copy it over

				main.insertBefore(main_xsl.importNode(node, true), insertion_point);
			}
		}

		// variables - only top level ones!!
		// append to end of document
		children = GSXML.getChildrenByTagNameNS(extra_xsl, GSXML.XSL_NAMESPACE, "variable");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// If the new xsl:import element is identical (in terms of href attr value) 
			// to any in the merged document, don't copy it over
			if (GSXML.getNamedElementNS(main, GSXML.XSL_NAMESPACE, "variable", "name", node.getAttribute("name")) == null)
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		// params - only top level ones!!
		// append to end of document
		children = GSXML.getChildrenByTagNameNS(extra_xsl, GSXML.XSL_NAMESPACE, "param");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// If the new xsl:import element is identical (in terms of href attr value) 
			// to any in the merged document, don't copy it over
			if (GSXML.getNamedElementNS(main, GSXML.XSL_NAMESPACE, "param", "name", node.getAttribute("name")) == null)
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		// key -- xsl:key elements need to be defined at the top level
		children = GSXML.getChildrenByTagNameNS(extra_xsl, GSXML.XSL_NAMESPACE, "key");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// If the new xsl:key element is identical (in terms of name attr value) 
			// to any in the merged document, don't copy it over
			if (GSXML.getNamedElementNS(main, GSXML.XSL_NAMESPACE, "key", "name", node.getAttribute("name")) == null)
			{
				main.appendChild(main_xsl.importNode(node, true));
			}
		}

		// templates
		// append to end of document
		children = extra_xsl.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "template");
		for (int i = 0; i < children.getLength(); i++)
		{
			Element node = (Element) children.item(i);
			// remove any previous occurrences of xsl:template with the same value for name or match 
			String template_match = node.getAttribute("match");
			String template_name = node.getAttribute("name");
			String template_mode = node.getAttribute("mode");

			if (overwrite)
			{
				// if we have a name attribute, remove any other similarly named template
				GSXML.removeElementsWithAttributesNS(main, GSXML.XSL_NAMESPACE, "template", new String[] { "name", "match", "mode" }, new String[] { template_name, template_match, template_mode });

				// now add our good template in
				main.appendChild(main_xsl.importNode(node, true));
			}
			else
			{
				// if overwrite is false, then we only add in templates if they don't match something else.
				// In this case (eg from expanding imported stylesheets)
				// there can't be any duplicate named templates, so just look for matches
				// we already have the one with highest import precedence (from the top most level) so don't add any more in
				if (GSXML.getElementsWithAttributesNS(main, GSXML.XSL_NAMESPACE, "template", new String[] { "name", "match", "mode" }, new String[] { template_name, template_match, template_mode }).getLength() == 0)
				{
					main.appendChild(main_xsl.importNode(node, true));
				}
			}
		}

		if (debug)
		{
			insertDebugElements(main_xsl, secondDocFileName);
		}
	}

	public static void insertDebugElements(Document doc, String filename)
	{
		NodeList xslTemplates = doc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "template");
		NodeList gsfTemplates = doc.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "template");

		for (int i = 0; i < xslTemplates.getLength() + gsfTemplates.getLength(); i++)
		{
			boolean gsf = (i >= xslTemplates.getLength());
			Element currentTemplate = (Element) (!gsf ? xslTemplates.item(i) : gsfTemplates.item(i - xslTemplates.getLength()));

			NodeList childNodes = currentTemplate.getChildNodes();
			boolean debugInformationAlreadyExists = false;
			for (int j = 0; j < childNodes.getLength(); j++)
			{
				Node current = childNodes.item(j);
				if (current instanceof Element && ((Element) current).getNodeName().equals("debug") && (!((Element) current).getAttribute("nodename").startsWith("gsf:") || ((Element) current).getAttribute("nodename").equals("gsf:template")))
				{
					debugInformationAlreadyExists = true;
					break;
				}
			}

			if (debugInformationAlreadyExists)
			{
				continue;
			}

			Element debugElement = doc.createElement("debug");
			debugElement.setAttribute("filename", filename);
			debugElement.setAttribute("nodename", gsf ? "gsf:template" : "xsl:template");

			if (currentTemplate.getAttribute("match").length() > 0)
			{
				debugElement.setAttribute("match", currentTemplate.getAttribute("match"));
			}
			if (currentTemplate.getAttribute("name").length() > 0)
			{
				debugElement.setAttribute("name", currentTemplate.getAttribute("name"));
			}

			if (currentTemplate.getUserData("xpath") != null)
			{
				debugElement.setAttribute("xpath", (String) currentTemplate.getUserData("xpath"));
			}

			if (childNodes.getLength() > 0)
			{
				int paramCount = 0;
				while (childNodes.getLength() > paramCount)
				{
					Node currentNode = childNodes.item(paramCount);
					if (currentNode instanceof Element)
					{
						if (((Element) currentNode).getNodeName().equals("xsl:param") || ((Element) currentNode).getNodeName().equals("xslt:param") || (((Element) currentNode).getNodeName().equals("param") && ((Element) currentNode).getNamespaceURI().equals(GSXML.XSL_NAMESPACE)))
						{
							paramCount++;
						}
						else
						{
							debugElement.appendChild(currentNode);
						}
					}
					else
					{
						debugElement.appendChild(currentNode);
					}
				}
				currentTemplate.appendChild(debugElement);
			}
			else
			{
				currentTemplate.appendChild(debugElement);
			}

			Element textElement = doc.createElementNS(GSXML.XSL_NAMESPACE, "text");
			textElement.appendChild(doc.createTextNode(" "));
			debugElement.appendChild(textElement);
		}
	}

	public static void inlineImportAndIncludeFiles(Document doc, String pathExtra, String site, String collection, String interface_name, ArrayList<String> base_interfaces)
	{
		inlineImportAndIncludeFilesDebug(doc, pathExtra, false, null, site, collection, interface_name, base_interfaces);
	}

	public static void inlineImportAndIncludeFilesDebug(Document doc, String pathExtra, boolean debug, String docFileName, String site, String collection, String interface_name, ArrayList<String> base_interfaces)
	{
		String path = (pathExtra == null) ? "" : pathExtra;

		NodeList importList = doc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "import");
		NodeList includeList = doc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "include");

		for (int i = 0; i < importList.getLength() + includeList.getLength(); i++)
		{
			Element current = (Element) ((i < importList.getLength()) ? importList.item(i) : includeList.item(i - importList.getLength()));
			String href = current.getAttribute("href");

			try
			{
				//Document inlineDoc = converter.getDOM(new File(filePath), "UTF-8");
				Document inlineDoc = mergedXSLTDocumentCascade(path + href, site, collection, interface_name, base_interfaces, debug);
				String newPath = path;
				int lastSepIndex = href.lastIndexOf("/");
				if (lastSepIndex != -1)
				{
					newPath += href.substring(0, lastSepIndex + 1);
				}

				//Do this recursively
				inlineImportAndIncludeFilesDebug(inlineDoc, newPath, debug, "merged " + href/* filePath */, site, collection, interface_name, base_interfaces);
				GSXSLT.mergeStylesheetsDebug(doc, inlineDoc.getDocumentElement(), false, debug, docFileName, /* filePath */"merged " + href);
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

	public static Document mergedXSLTDocumentCascade(String xslt_filename, String site, String collection, String this_interface, ArrayList<String> base_interfaces, boolean debug)
	{
		XMLConverter converter = new XMLConverter();
		// find the list of stylesheets with this name
		ArrayList<File> stylesheets = GSFile.getStylesheetFiles(GlobalProperties.getGSDL3Home(), site, collection, this_interface, base_interfaces, xslt_filename);
		if (stylesheets.size() == 0)
		{
			logger.error(" Can't find stylesheet for " + xslt_filename);
			return null;
		}
		logger.debug("Stylesheet: " + xslt_filename);

		Document finalDoc = converter.getDOM(stylesheets.get(stylesheets.size() - 1), "UTF-8");
		if (finalDoc == null)
		{
			return null;
		}

		for (int i = stylesheets.size() - 2; i >= 0; i--)
		{
			Document currentDoc = converter.getDOM(stylesheets.get(i), "UTF-8");
			if (currentDoc == null)
			{
				return null;
			}

			if (debug)
			{
				GSXSLT.mergeStylesheetsDebug(finalDoc, currentDoc.getDocumentElement(), true, true, stylesheets.get(stylesheets.size() - 1).getAbsolutePath(), stylesheets.get(i).getAbsolutePath());
			}
			else
			{
				GSXSLT.mergeStylesheets(finalDoc, currentDoc.getDocumentElement(), true);
			}
		}

		if (stylesheets.size() == 1 && debug)
		{
			insertDebugElements(finalDoc, stylesheets.get(0).getAbsolutePath());
		}

		return finalDoc;
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


  public static void findExtraMetadataNames(Element xsl_elem, HashSet<String> meta_names) {

    // gsf:metadata and gsf:foreach-metadata
    NodeList metadata_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "metadata"); 
    NodeList foreach_metadata_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "foreach-metadata");
    int num_meta_nodes = metadata_nodes.getLength();
    int total_nodes = num_meta_nodes +foreach_metadata_nodes.getLength();
    for (int i = 0; i < total_nodes; i++) {
      Element current;
      if (i<num_meta_nodes) {
	current = (Element) metadata_nodes.item(i);
      } else {
	current = (Element) foreach_metadata_nodes.item(i-num_meta_nodes);
      }
      String full_name = current.getAttribute("name");
      String select = current.getAttribute("select");
      
      String [] names = full_name.split(",");
      for(int j=0; j<names.length; j++) {
	
	String name = names[j];
	if (!name.equals("")) {
	  if (!select.equals("")) {
	    name = select + GSConstants.META_RELATION_SEP + name;
	  }
	  meta_names.add(name);
	}
      }
    }
    
    // gsf:link
    boolean getEquivLinkMeta = false;
    NodeList link_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "link");
    for (int i = 0; i < link_nodes.getLength(); i++) {
      
      Element elem = (Element) link_nodes.item(i);
      String type = elem.getAttribute("type");
      if (type.equals("source"))
	{
	  meta_names.add("root_assocfilepath");
	  meta_names.add("srclinkFile");
	}
      else if (type.equals("web"))
	{
	  meta_names.add("weblink");
	  meta_names.add("webicon");
	  meta_names.add("/weblink");
	}
      else if (type.equals("equivdoc"))
	{
	  getEquivLinkMeta = true; // equivalent to gsf:equivlinkgs3
	}
    }
    // gsf:equivlinkgs3
    link_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "equivlinkgs3");
    if (getEquivLinkMeta || link_nodes.getLength() > 0) {
      
      String[] equivlink_metanames = { "equivDocIcon", "equivDocLink", "/equivDocLink" };

      for (int i = 0; i < equivlink_metanames.length; i++)
	{
	  StringBuffer metadata = new StringBuffer();
	  metadata.append("all"); // this means the attr multiple = true;
	  metadata.append(GSConstants.META_RELATION_SEP);
	  
	  metadata.append(GSConstants.META_SEPARATOR_SEP);
	  metadata.append(','); // attr separator = ","
	  metadata.append(GSConstants.META_SEPARATOR_SEP);
	  metadata.append(GSConstants.META_RELATION_SEP);
	  
	  // the name of the metadata we're retrieving
	  metadata.append(equivlink_metanames[i]);
	  meta_names.add(metadata.toString());
	}
    }
    
    // gsf:icon
    NodeList icon_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "icon");
    for (int i = 0; i < icon_nodes.getLength(); i++) {
      Element current = (Element) icon_nodes.item(i);
      String type = current.getAttribute(GSXML.TYPE_ATT);
      if (type == null || type.length() == 0) {
	continue;
      }
      if (type.equals("web")) {
	meta_names.add("webicon");
	break; // this is the only one we are looking for at the moment
      }
    }
    
    // gsf:image
    NodeList image_nodes = xsl_elem.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "image");
    for (int i = 0; i < image_nodes.getLength(); i++) {
      Element current = (Element) image_nodes.item(i);
      String type = current.getAttribute(GSXML.TYPE_ATT);
      if (type == null || type.length() == 0) {
	continue;
      }
      
      if (type.equals("source")) {
	
	String[] standardSourceMeta = new String[] { "SourceFile", "ImageHeight", "ImageWidth", "ImageType", "srcicon" };
	for (String meta : standardSourceMeta) {
	  meta_names.add(meta);
	}
	
      }
      else if (type.equals("screen")) {
	
	String[] standardScreenMeta = new String[] { "Screen", "ScreenHeight", "ScreenWidth", "ScreenType", "screenicon" };
	for (String meta : standardScreenMeta) {
	  meta_names.add(meta);
	}
      }
      else if (type.equals("thumb")) {
	String[] standardThumbMeta = new String[] { "Thumb", "ThumbHeight", "ThumbWidth", "ThumbType", "thumbicon" };
	for (String meta : standardThumbMeta) {
	  meta_names.add(meta);
	}
      }
      else if (type.equals("cover")) {
	meta_names.add("hascover");
	logger.error("adding hascover");
      }
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

	public static void mergeFormatElements(Element mainFormat, Element secondaryFormat, boolean overwrite)
	{
		NodeList xslChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.XSL_NAMESPACE, "variable");
		NodeList gsfChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.GSF_NAMESPACE, "variable");
		for (int i = 0; i < xslChildren.getLength() + gsfChildren.getLength(); i++)
		{
			Element node = (Element) ((i < xslChildren.getLength()) ? xslChildren.item(i) : gsfChildren.item(i - xslChildren.getLength()));
			if (GSXML.getNamedElementNS(mainFormat, "http://www.w3.org/1999/XSL/Transform", "variable", "name", node.getAttribute("name")) == null && GSXML.getNamedElementNS(mainFormat, "http://www.greenstone.org/greenstone3/schema/ConfigFormat", "variable", "name", node.getAttribute("name")) == null)
			{
				mainFormat.appendChild(node);
			}
		}

		xslChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.XSL_NAMESPACE, "param");
		gsfChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.GSF_NAMESPACE, "param");
		for (int i = 0; i < xslChildren.getLength() + gsfChildren.getLength(); i++)
		{
			Element node = (Element) ((i < xslChildren.getLength()) ? xslChildren.item(i) : gsfChildren.item(i - xslChildren.getLength()));
			if (GSXML.getNamedElementNS(mainFormat, "http://www.w3.org/1999/XSL/Transform", "param", "name", node.getAttribute("name")) == null && GSXML.getNamedElementNS(mainFormat, "http://www.greenstone.org/greenstone3/schema/ConfigFormat", "param", "name", node.getAttribute("name")) == null)
			{
				mainFormat.appendChild(node);
			}
		}

		xslChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.XSL_NAMESPACE, "template");
		gsfChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.GSF_NAMESPACE, "template");
		for (int i = 0; i < xslChildren.getLength() + gsfChildren.getLength(); i++)
		{
			Element node = (Element) ((i < xslChildren.getLength()) ? xslChildren.item(i) : gsfChildren.item(i - xslChildren.getLength()));
			// remove any previous occurrences of xsl:template with the same value for name or match 
			String template_match = node.getAttribute("match");
			String template_name = node.getAttribute("name");
			String template_mode = node.getAttribute("mode");

			String[] attributeNames = new String[] { "name", "match", "mode" };
			String[] attributeValues = new String[] { template_name, template_match, template_mode };

			if (overwrite)
			{
				// if we have a name attribute, remove any other similarly named template
				GSXML.removeElementsWithAttributesNS(mainFormat, GSXML.XSL_NAMESPACE, "template", attributeNames, attributeValues);
				GSXML.removeElementsWithAttributesNS(mainFormat, GSXML.GSF_NAMESPACE, "template", attributeNames, attributeValues);

				// now add our good template in
				mainFormat.appendChild(node);
			}
			else
			{
				// if overwrite is false, then we only add in templates if they don't match something else.
				// In this case (eg from expanding imported stylesheets)
				// there can't be any duplicate named templates, so just look for matches
				// we already have the one with highest import precedence (from the top most level) so don't add any more in
				if (GSXML.getElementsWithAttributesNS(mainFormat, "http://www.w3.org/1999/XSL/Transform", "template", attributeNames, attributeValues).getLength() == 0 && GSXML.getElementsWithAttributesNS(mainFormat, "http://www.greenstone.org/greenstone3/schema/ConfigFormat", "template", attributeNames, attributeValues).getLength() == 0)
				{
					mainFormat.appendChild(node);
				}
			}
		}

		gsfChildren = GSXML.getChildrenByTagNameNS(secondaryFormat, GSXML.GSF_NAMESPACE, "option");
		for (int i = 0; i < gsfChildren.getLength(); i++)
		{
			Element node = (Element) gsfChildren.item(i);
			if (GSXML.getNamedElementNS(mainFormat, GSXML.GSF_NAMESPACE, "option", "name", node.getAttribute("name")) == null)
			{
				mainFormat.appendChild(node);
			}
		}
	}

	public static void fixTables(Document doc)
	{
		NodeList debugElements = doc.getElementsByTagName("debug");

		HashMap<Element, ArrayList<Element>> tracker = new HashMap<Element, ArrayList<Element>>();
		for (int i = 0; i < debugElements.getLength(); i++)
		{
			Element currentElement = (Element) debugElements.item(i);

			boolean hasChildElements = false;
			NodeList children = currentElement.getChildNodes();
			for (int j = 0; j < children.getLength(); j++)
			{
				Node current = children.item(j);
				if (current instanceof Element)
				{
					hasChildElements = true;
				}
			}

			if (hasChildElements && currentElement.getParentNode() != null && currentElement.getParentNode() instanceof Element)
			{
				Element parent = findNonDebugParent(currentElement);
				if (parent == null)
				{
					continue;
				}

				if (parent.getNodeName().toLowerCase().equals("table") || parent.getNodeName().toLowerCase().equals("tr"))
				{
					if (tracker.get(parent) == null)
					{
						ArrayList<Element> debugElems = new ArrayList<Element>();
						debugElems.add(currentElement);
						tracker.put(parent, debugElems);
					}
					else
					{
						ArrayList<Element> debugElems = tracker.get(parent);
						debugElems.add(currentElement);
					}
				}
			}
		}

		for (Element tableElem : tracker.keySet())
		{
			ArrayList<Element> debugElems = tracker.get(tableElem);
			ArrayList<String> attrNames = new ArrayList<String>();

			for (Element debugElem : debugElems)
			{
				NamedNodeMap attributes = debugElem.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++)
				{
					attrNames.add(attributes.item(i).getNodeName());
				}
			}

			for (String name : attrNames)
			{
				String attrValueString = "[";
				for (int i = debugElems.size() - 1; i >= 0; i--)
				{
					Element current = debugElems.get(i);
					attrValueString += "\'" + current.getAttribute(name).replace("\\", "\\\\").replace("'", "\\'") + "\'";
					if (i != 0)
					{
						attrValueString += ",";
					}
				}
				attrValueString += "]";

				tableElem.setAttribute(name, attrValueString);
			}
			tableElem.setAttribute("debug", "true");
			tableElem.setAttribute("debugSize", "" + debugElems.size());
		}
	}

	private static Element findNonDebugParent(Element elem)
	{
		Node parent = elem.getParentNode();
		while (parent instanceof Element && parent.getNodeName().equals("debug"))
		{
			parent = parent.getParentNode();
		}

		if (parent instanceof Element)
		{
			return (Element) parent;
		}
		return null;
	}

	public static void modifyCollectionConfigForDebug(Element coll_config_xml)
	{
		NodeList xslTemplates = coll_config_xml.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "template");
		NodeList gsfTemplates = coll_config_xml.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "template");

		for (int i = 0; i < xslTemplates.getLength() + gsfTemplates.getLength(); i++)
		{
			Element currentTemplate = (Element) ((i < xslTemplates.getLength()) ? xslTemplates.item(i) : gsfTemplates.item(i - xslTemplates.getLength()));
			Element temp = currentTemplate;
			String xPath = "";
			while (!temp.getNodeName().toLowerCase().equals("collectionconfig"))
			{
				temp = (Element) temp.getParentNode();
				String nodeName = temp.getNodeName();

				int count = 1;
				Node counter = temp.getPreviousSibling();
				while (counter != null)
				{
					if (counter.getNodeType() == Node.ELEMENT_NODE && ((Element) counter).getNodeName().equals(nodeName))
					{
						count++;
					}
					counter = counter.getPreviousSibling();
				}
				xPath = nodeName + ((count > 1) ? ("[" + count + "]") : "") + "/" + xPath;
			}

			xPath = xPath.substring(0, xPath.length() - 1);
			currentTemplate.setUserData("xpath", xPath, new DataTransferHandler());
		}
	}

	static class DataTransferHandler implements UserDataHandler
	{
		public void handle(short operation, String key, Object data, Node src, Node dst)
		{
			if (operation == NODE_IMPORTED || operation == NODE_CLONED)
			{
				//Thread.dumpStack();
				dst.setUserData(key, data, new DataTransferHandler());
			}
		}
	}
}
