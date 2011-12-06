/*
 *    XMLConverter.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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

// XML classes
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.*; // for new Documents

// other java classes
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.regex.*;

import org.apache.log4j.*;

// Apache Commons
import org.apache.commons.lang3.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * XMLConverter - utility class for greenstone
 * 
 * parses XML Strings into Documents, converts Nodes to Strings different
 * parsers have different behaviour - can experiment in here now we only use
 * xerces
 * 
 */
public class XMLConverter
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.XMLConverter.class.getName());

	protected EntityResolver resolver = null;
	
	/** xerces parser */
	protected DOMParser parser = null;

	private static boolean outputEscaping = true;

	/** the no-args constructor */
	public XMLConverter()
	{
		try
		{
			this.parser = new DOMParser();
			this.parser.setFeature("http://xml.org/sax/features/validation", false);
			// don't try and load external DTD - no need if we are not validating, and may cause connection errors if a proxy is not set up.
			this.parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// a performance test showed that having this on lead to increased 
			// memory use for small-medium docs, and not much gain for large 
			// docs.
			// http://www.sosnoski.com/opensrc/xmlbench/conclusions.html
			this.parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			// add an errorhandler to the parser which will store useful a error message on encountering fatal errors, errors and warnings when parsing
			// this errormessage can then be converted to xhtml and displayed in a browser.
			this.parser.setErrorHandler(new ParseErrorHandler());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
	}

	/** sets the entity resolver. pass in null to unset it */
	public void setEntityResolver(EntityResolver er)
	{
		this.resolver = er;
		this.parser.setEntityResolver(er);
	}

	/**
	 * Given a Node representing an Element or Document, will return the
	 * Element/docroot Element. Returns null if the Node was not an element.
	 */
	public static Element nodeToElement(Node node)
	{
		if (node == null)
		{
			return null;
		}
		short nodeType = node.getNodeType();

		if (nodeType == Node.DOCUMENT_NODE)
		{
			Document docNode = (Document) node;
			return docNode.getDocumentElement();
		}
		else if (nodeType == Node.ELEMENT_NODE)
		{
			return (Element) node;
		}
		else
		{
			String message = "Expecting Document or Element node type but got " + node.getNodeName() + "\nReturning null";
			System.err.println(message);
			logger.warn(message);
			return null;
		}
	}

	/** returns a DOM Document */
	public Document getDOM(String in)
	{

		try
		{
			Reader reader = new StringReader(in);
			InputSource xml_source = new InputSource(reader);

			DOMParser parser = new DOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			if(resolver != null)
			{
				parser.setEntityResolver(this.resolver);
			}
			parser.setErrorHandler(new ParseErrorHandler());
			parser.parse(xml_source);
			
			Document doc = parser.getDocument();

			return doc;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		return null;
	}

	/** returns a DOM Document */
	public Document getDOM(File in)
	{
		try
		{
			FileReader reader = new FileReader(in);
			InputSource xml_source = new InputSource(reader);
			
			DOMParser parser = new DOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			if(resolver != null)
			{
				parser.setEntityResolver(this.resolver);
			}
			parser.setErrorHandler(new ParseErrorHandler());
			parser.parse(xml_source);
			
			Document doc = parser.getDocument();
			return doc;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);

		}
		return null;
	}

	/** returns a DOM document */
	public Document getDOM(File in, String encoding)
	{
		try
		{

			InputStreamReader isr = new InputStreamReader(new FileInputStream(in), encoding);
			InputSource xml_source = new InputSource(isr);

			DOMParser parser = new DOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			if(resolver != null)
			{
				parser.setEntityResolver(this.resolver);
			}
			parser.setErrorHandler(new ParseErrorHandler());
			parser.parse(xml_source);
			
			Document doc = parser.getDocument();

			return doc;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		return null;
	}

	/** creates a new empty DOM Document */
	public static Document newDOM()
	{
		Document doc = new DocumentImpl();
		return doc;
	}

	/**
	 * This method's parameters represent the parts of the Doctype of this
	 * Document that is to be created. For more info see
	 * http://xerces.apache.org
	 * /xerces-j/apiDocs/org/apache/xerces/dom/DocumentTypeImpl
	 * .html#DocumentTypeImpl
	 * (org.apache.xerces.dom.CoreDocumentImpl,%20java.lang.String)
	 * 
	 * */
	public static Document newDOM(String qualifiedName, String publicID, String systemID)
	{
		// create empty DOM document
		DocumentImpl docImpl = new DocumentImpl();

		// Need to use the document to create the docType for it
		DocumentType myDocType = new DocumentTypeImpl(docImpl, qualifiedName, publicID, systemID);

		// Although we have created the docType using the document, we need to still 
		// put it into the empty document we just created 
		try
		{
			docImpl.appendChild(myDocType);
		}
		catch (Exception e)
		{
			System.out.println("Could not append docType because: " + e);
		}

		// return the document containing a DocType
		return docImpl;
	}

	/** returns the Node as a String */
	public static String getString(Node xmlNode)
	{
		outputEscaping = true;
		StringBuffer xmlRepresentation = new StringBuffer();
		getString(xmlNode, xmlRepresentation, 0, false);
		return xmlRepresentation.toString();
	}

	/**
	 * returns the node as a nicely formatted String - this introduces extra
	 * text nodes if the String is read back in as a DOM, so should only be used
	 * for printing
	 */
	public static String getPrettyString(Node xmlNode)
	{

		outputEscaping = true;
		StringBuffer xmlRepresentation = new StringBuffer();
		getString(xmlNode, xmlRepresentation, 0, true);
		return xmlRepresentation.toString();
	}

	/*
	 * For the purposes of logger.debug statements, where this is called and
	 * hence outputted, returns an empty string if debugging is not enabled
	 */
	public static String getPrettyStringLogger(Node xmlNode, Logger log)
	{

		if (log.isDebugEnabled())
			return getPrettyString(xmlNode);

		return "";

	}

	private static void getString(Node xmlNode, StringBuffer xmlRepresentation, int depth, boolean pretty)
	{

		if (xmlNode == null)
		{
			xmlRepresentation.append("<null>");
			return;
		}

		short nodeType = xmlNode.getNodeType();
		String nodeName = xmlNode.getNodeName();

		if (nodeType == Node.DOCUMENT_NODE)
		{
			Document xmlDocNode = (Document) xmlNode;

			//if (xmlDocNode.getDoctype() == null) {
			//System.err.println("Doctype is null.");
			//}
			//else {
			if (xmlDocNode.getDoctype() != null)
			{
				DocumentType dt = xmlDocNode.getDoctype();

				String name = dt.getName();
				String pid = dt.getPublicId();
				String sid = dt.getSystemId();

				// Use previously assigned name, not dt.getName() again
				String doctype_str = "<!DOCTYPE " + name + " PUBLIC \"" + pid + "\" \"" + sid + "\">\n";

				xmlRepresentation.append(doctype_str);
			}
			getString(xmlDocNode.getDocumentElement(), xmlRepresentation, depth, pretty);
			return;
		}
		// Handle Element nodes
		if (nodeType == Node.ELEMENT_NODE)
		{
			if (pretty)
			{
				xmlRepresentation.append("\n");
				for (int i = 0; i < depth; i++)
				{
					xmlRepresentation.append("  ");
				}
			}

			// Write opening tag
			xmlRepresentation.append("<");
			xmlRepresentation.append(nodeName);

			// Write the node attributes
			NamedNodeMap nodeAttributes = xmlNode.getAttributes();
			for (int i = 0; i < nodeAttributes.getLength(); i++)
			{
				Node attribute = nodeAttributes.item(i);
				xmlRepresentation.append(" ");
				xmlRepresentation.append(attribute.getNodeName());
				xmlRepresentation.append("=\"");
				xmlRepresentation.append(attribute.getNodeValue());
				xmlRepresentation.append("\"");
			}

			// If the node has no children, close the opening tag and return
			if (xmlNode.hasChildNodes() == false)
			{
				// This produces somewhat ugly output, but it is necessary to compensate
				// for display bugs in Netscape. Firstly, the space is needed before the
				// closing bracket otherwise Netscape will ignore some tags (<br/>, for
				// example). Also, a newline character would be expected after the tag,
				// but this causes problems with the display of links (the link text
				// will contain a newline character, which is displayed badly).
				xmlRepresentation.append(" />");
				return;
			}

			// Close the opening tag
			xmlRepresentation.append(">");

			// Apply recursively to the children of this node
			// hack for nodes next to text nodes - dont make them pretty
			// this is needed for text inside a <pre> element - any new lines 
			// or spaces around the span elements show up in the text
			NodeList children = xmlNode.getChildNodes();
			boolean do_pretty = pretty;
			for (int i = 0; i < children.getLength(); i++)
			{
				if (children.item(i).getNodeType() == Node.TEXT_NODE)
				{
					do_pretty = false; // if there is a text node amongst the children, do teh following nodes in non-pretty mode - hope this doesn't stuff up something else
				}
				getString(children.item(i), xmlRepresentation, depth + 1, do_pretty);
			}

			// Write closing tag
			if (pretty)
			{
				if (xmlRepresentation.charAt(xmlRepresentation.length() - 1) == '\n')
				{
					for (int i = 0; i < depth; i++)
						xmlRepresentation.append("  ");
				}
			}
			xmlRepresentation.append("</");
			xmlRepresentation.append(nodeName);
			xmlRepresentation.append(">");
			if (pretty)
			{
				xmlRepresentation.append("\n");
			}
		}

		// Handle Text nodes
		else if (nodeType == Node.TEXT_NODE)
		{
			String text = xmlNode.getNodeValue();

			// Perform output escaping, if required
			// Apache Commons replace method is far superior to String.replaceAll - very fast!
			if (outputEscaping)
			{

				text = StringUtils.replace(text, "&", "&amp;");
				text = StringUtils.replace(text, "<", "&lt;");
				text = StringUtils.replace(text, ">", "&gt;");
				text = StringUtils.replace(text, "'", "&apos;");
				text = StringUtils.replace(text, "\"", "&quot;");
			}

			// Remove any control-C characters
			text = StringUtils.replace(text, "" + (char) 3, "");

			xmlRepresentation.append(text);
		}

		// Handle Processing Instruction nodes
		else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE)
		{
			if (nodeName.equals("javax.xml.transform.disable-output-escaping"))
			{
				outputEscaping = false;
			}
			else if (nodeName.equals("javax.xml.transform.enable-output-escaping"))
			{
				outputEscaping = true;
			}
			else
			{
				logger.warn("Unhandled processing instruction " + nodeName);
			}
		}

		else if (nodeType == Node.COMMENT_NODE)
		{
			String text = xmlNode.getNodeValue();
			xmlRepresentation.append("<!-- ");
			xmlRepresentation.append(text);
			xmlRepresentation.append(" -->");
		}

		// A type of node that is not handled yet
		else
		{
			logger.warn("Unknown node type: " + nodeType + " " + getNodeTypeString(nodeType));
		}

		return;
	}

	protected static String getNodeTypeString(short node_type)
	{

		String type = "";
		switch (node_type)
		{
		case Node.ATTRIBUTE_NODE:
			type = "ATTRIBUTE_NODE";
			break;
		case Node.CDATA_SECTION_NODE:
			type = "CDATA_SECTION_NODE";
			break;
		case Node.COMMENT_NODE:
			type = "COMMENT_NODE";
			break;
		case Node.DOCUMENT_FRAGMENT_NODE:
			type = "DOCUMENT_FRAGMENT_NODE";
			break;
		case Node.DOCUMENT_NODE:
			type = "DOCUMENT_NODE";
			break;
		case Node.DOCUMENT_TYPE_NODE:
			type = "DOCUMENT_TYPE_NODE";
			break;
		case Node.ELEMENT_NODE:
			type = "ELEMENT_NODE";
			break;
		case Node.ENTITY_NODE:
			type = "ENTITY_NODE";
			break;
		case Node.ENTITY_REFERENCE_NODE:
			type = "ENTITY_REFERENCE_NODE";
			break;
		case Node.NOTATION_NODE:
			type = "NOTATION_NODE";
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			type = "PROCESSING_INSTRUCTION_NODE";
			break;
		case Node.TEXT_NODE:
			type = "TEXT_NODE";
			break;
		default:
			type = "UNKNOWN";
		}

		return type;
	}

	// returns null if there no error occurred during parsing, or else returns the error message
	public String getParseErrorMessage()
	{
		ParseErrorHandler errorHandler = (ParseErrorHandler) this.parser.getErrorHandler();
		return errorHandler.getErrorMessage();
	}

	// Errorhandler for SAXParseExceptions that are errors, fatal errors or warnings. This class can be used to 
	// register a handler for any fatal errors, errors and warnings that may occur when parsing an xml file. The
	// errors are printed both to the greenstone.log and to the tomcat console (System.err), and the error message
	// is stored in the errorMessage variable so that it can be retrieved and be used to generate an xhtml error page.
	static public class ParseErrorHandler implements ErrorHandler
	{
		protected String errorMessage = null;

		//  Receive notification of a recoverable error.
		public void error(SAXParseException exception)
		{
			handleError("Error:\n", exception);
		}

		//   Receive notification of a non-recoverable error.
		public void fatalError(SAXParseException exception)
		{
			handleError("Fatal Error:\n", exception);
		}

		// Receive notification of a warning.
		public void warning(SAXParseException exception)
		{
			handleError("Warning:\n", exception);
		}

		public String toString(SAXParseException e)
		{
			String msg = e.getMessage();
			msg += "\nOn line(column): " + e.getLineNumber() + "(" + e.getColumnNumber() + ")";
			msg += (e.getPublicId() != null) ? ("\npublic ID: " + e.getPublicId()) : "\nNo public ID";
			msg += (e.getSystemId() != null) ? ("\nsystem ID: " + e.getSystemId()) : "\nNo system ID";

			return msg;
		}

		// clears the errorPage variable after first call to this method
		public String getErrorMessage()
		{
			String errMsg = this.errorMessage;
			if (this.errorMessage != null)
			{
				this.errorMessage = null;
			}
			return errMsg;
		}

		// sets the errorMessage member variable to the data stored in the exception
		// and writes the errorMessage to the logger and tomcat's System.err
		protected void handleError(String errorType, SAXParseException exception)
		{
			this.errorMessage = errorType + toString(exception);
			System.err.println("\n****Error parsing xml:\n" + this.errorMessage + "\n****\n");
			logger.error(this.errorMessage);
		}
	}
}
