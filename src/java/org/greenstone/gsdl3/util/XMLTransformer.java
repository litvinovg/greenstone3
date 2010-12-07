/*
 *    XMLTransformer.java
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMResult;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// other java classes
import java.io.StringReader;
import java.io.StringWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import org.apache.xml.utils.DefaultErrorHandler;

import org.apache.log4j.*;

/** XMLTransformer - utility class for greenstone
 *
 * transforms xml using xslt
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */
public class XMLTransformer {

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.XMLTransformer.class.getName());
	
  /** The transformer factory we're using */
    TransformerFactory t_factory=null;
  
  /**
   * The no-arguments constructor. 
   *
   * Any exceptions thrown are caught internally
   * 
   * @see javax.xml.transform.TransformerFactory
   */
    public XMLTransformer() {
	// http://download.oracle.com/docs/cd/E17476_01/javase/1.5.0/docs/api/index.html?javax/xml/transform/TransformerFactory.html states that
	// TransformerFactory.newInstance() looks in jar files for a Factory specified in META-INF/services/javax.xml.transform.TransformerFactory, 
	// else it will use the "platform default"
	// In this case: xalan.jar's META-INF/services/javax.xml.transform.TransformerFactory contains org.apache.xalan.processor.TransformerFactoryImpl
	// as required.

	// This means we no longer have to do a System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
	// followed by a this.t_factory = org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
	// The System.setProperty step to force the TransformerFactory implementation that gets used, conflicts with
	// Fedora (visiting the Greenstone server pages breaks the Greenstone-tomcat hosted Fedora pages) as Fedora 
	// does not include the xalan.jar and therefore can't then find the xalan TransformerFactory explicitly set.

	// Gone back to forcing use of xalan transformer, since other jars like crimson.jar, which may be on some
	// classpaths, could be be chosen as the TransformerFactory implementation over xalan. This is what used to
	// give problems before. Instead, have placed copies of the jars that Fedora needs (xalan.jar and serializer.jar 
	// and the related xsltc.jar which it may need) into packages/tomcat/lib so that it's on the server's classpath
	// and will be found by Fedora.

	// make sure we are using the xalan transformer
	System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
	try {
	    this.t_factory = org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
	    //this.t_factory = TransformerFactory.newInstance();
	} catch (Exception e) {
	    logger.error("exception creating t_factory "+e.getMessage());
	}
    }

 
	
  /**
   * Transform an XML document using a XSLT stylesheet
   * 
   * @param stylesheet a filename for an XSLT stylesheet
   * @param xml_in the XML to be transformed
   * @return the transformed XML
   */
    public String transform(String stylesheet, String xml_in) {
	
	try {
	    // Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
	    Transformer transformer = this.t_factory.newTransformer(new StreamSource(stylesheet));
		transformer.setErrorListener(new TransformErrorListener());

	    // Use the Transformer to transform an XML Source and send the output to a Result object.
	    StringWriter output = new StringWriter();

	    transformer.transform(new StreamSource(new StringReader(xml_in)), new StreamResult(output));
	    return output.toString();
	} catch (TransformerConfigurationException e) {
	    logger.error("couldn't create transformer object: "+e.getMessageAndLocation());
	    logger.error(e.getLocationAsString());  
	    return "";
	} catch (TransformerException e) {
	    logger.error("couldn't transform the source: " + e.getMessageAndLocation());
	    return "";
	}	
    }

    public String transformToString(Document stylesheet, Document source) {
	return transformToString(stylesheet, source, null);
    }
    
    public String transformToString(Document stylesheet, Document source, HashMap parameters) {
	
	try {
	    // Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
	    Transformer transformer = this.t_factory.newTransformer(new DOMSource(stylesheet));
		transformer.setErrorListener(new TransformErrorListener());
	    if (parameters != null) {
		Set params = parameters.entrySet();
		Iterator i = params.iterator();
		while (i.hasNext()) {
		    Map.Entry m = (Map.Entry)i.next();
		    transformer.setParameter((String)m.getKey(), m.getValue());
		}
	    }
	    //transformer.setParameter("page_lang", source.getDocumentElement().getAttribute(GSXML.LANG_ATT));

	    
	    // Use the Transformer to transform an XML Source and send the output to a Result object.
	    StringWriter output = new StringWriter();
	    
	    transformer.transform(new DOMSource(source), new StreamResult(output));
	    return output.toString();
	} catch (TransformerConfigurationException e) {
		logger.error("couldn't create transformer object: "+e.getMessageAndLocation());
	    logger.error(e.getLocationAsString());  
	    return "";
	} catch (TransformerException e) {
		logger.error("couldn't transform the source: " + e.getMessageAndLocation());
	    return "";
	}
    }

    public Node transform(Document stylesheet, Document source) {
	return transform(stylesheet, source, null, null);
    }

    public Node transform(Document stylesheet, Document source, HashMap parameters, Document docDocType) {
		try {
			// Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
			Transformer transformer = this.t_factory.newTransformer(new DOMSource(stylesheet));
			transformer.setErrorListener(new TransformErrorListener());
			if (parameters != null) {
				Set params = parameters.entrySet();
				Iterator i = params.iterator();
				while (i.hasNext()) {
					Map.Entry m = (Map.Entry)i.next();
					transformer.setParameter((String)m.getKey(), m.getValue());
				}
			}

			// When we transform the DOMResult, we need to make sure the result of
			// the transformation has a DocType. For that to happen, we need to create
			// the DOMResult using a Document with a predefined docType.
			// If we don't have a DocType then do the transformation with a DOMResult
			// that does not contain any doctype (like we use to do before).
			DOMResult result = docDocType == null ? new DOMResult() : new DOMResult(docDocType);
			transformer.transform(new DOMSource(source), result);
			return result.getNode(); // pass the entire document
		} 
		catch (TransformerConfigurationException e) {
			return transformError("XMLTransformer.transform(Doc, Doc, HashMap, Doc)"
				+ "\ncouldn't create transformer object", e);
		} 
		catch (TransformerException e) {
			return transformError("XMLTransformer.transform(Doc, Doc, HashMap, Doc)"
				+ "\ncouldn't transform the source", e);
		}
    }

    public Node transform(File stylesheet, File source) {
	try {
	    Transformer transformer = this.t_factory.newTransformer(new StreamSource(stylesheet));
		transformer.setErrorListener(new TransformErrorListener());
	    DOMResult result = new DOMResult();
	    transformer.transform(new StreamSource(source), result);
	    return result.getNode().getFirstChild();
	} catch (TransformerConfigurationException e) {
			return transformError("XMLTransformer.transform(File, File)"
				+ "\ncouldn't create transformer object for files\n" 
				+ stylesheet + "\n" + source, e);
	} 
	catch (TransformerException e) {
		return transformError("XMLTransformer.transform(File, File)"
				+ "\ncouldn't transform the source for files\n" 
				+ stylesheet + "\n" + source, e);
	}	
 }
 
    public Node transform(File stylesheet, File source, Document docDocType) {
    	try {
    	    Transformer transformer = this.t_factory.newTransformer(new StreamSource(stylesheet));
			transformer.setErrorListener(new TransformErrorListener());
    	    DOMResult result = new DOMResult(docDocType);
    	    transformer.transform(new StreamSource(source), result);
    	    return result.getNode().getFirstChild();
    	} catch (TransformerConfigurationException e) {
			return transformError("XMLTransformer.transform(File, File, Doc)"
				+ "\ncouldn't create transformer object for files\n" 
				+ stylesheet + "\n" + source, e);
		} 
		catch (TransformerException e) {
			return transformError("XMLTransformer.transform(File, File, Doc)"
				+ "\ncouldn't transform the source for files\n" 
				+ stylesheet + "\n" + source, e);
		}	
    }
    
	// Given a heading string on the sort of transformation error that occurred and the exception object itself, 
	// this method prints the exception to the tomcat window (system.err) and the greenstone log and then returns
	// an xhtml error page that is constructed from it.
   	protected Node transformError(String heading, TransformerException e) {
		String message = heading + "\n" + e.getMessage();
		logger.error(heading + ": " + e.getMessage());
		
		String location = e.getLocationAsString();		
		if(location != null) {
			logger.error(location);
			message = message + "\n" + location;
		}
		System.err.println("****\n" + message + "\n****");
		return constructErrorXHTMLPage(message);
	}
    
	// Given an error message, splits it into separate lines based on any newlines present and generates an xhtml page
	// (xml Element)  with paragraphs for each line. This is then returned so that it can be displayed in the browser.
    public static Element constructErrorXHTMLPage(String message) {
    	try{
			String[] lines = message.split("\n");
		
    		Document xhtmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    		// <html></html>
    		Node htmlNode = xhtmlDoc.createElement("html");
    		xhtmlDoc.appendChild(htmlNode);
    		// <head></head>
    		Node headNode = xhtmlDoc.createElement("head");
    		htmlNode.appendChild(headNode);
    		// <title></title>
    		Node titleNode = xhtmlDoc.createElement("title");
    		headNode.appendChild(titleNode);
    		Node titleString = xhtmlDoc.createTextNode("Error occurred");
    		titleNode.appendChild(titleString);
    		
    		// <body></body>
    		Node bodyNode = xhtmlDoc.createElement("body");
    		htmlNode.appendChild(bodyNode);
    		
    		// finally put the message in the body
    		Node h1Node = xhtmlDoc.createElement("h1");
    		bodyNode.appendChild(h1Node);
    		Node headingString = xhtmlDoc.createTextNode("The following error occurred:");
    		h1Node.appendChild(headingString);
    		
    		//Node textNode = xhtmlDoc.createTextNode(message);
    		//bodyNode.appendChild(textNode);
    		
			for (int i = 0; i < lines.length; i++) {
				Node pNode = xhtmlDoc.createElement("p");
				Node textNode = xhtmlDoc.createTextNode(lines[i]);
				pNode.appendChild(textNode);
				bodyNode.appendChild(pNode);
			}
			
    		return xhtmlDoc.getDocumentElement();
    		
    	}catch(Exception e) {
    		String errmsg = "Exception trying to construct error xhtml page from message: " + message
    			+ "\n" + e.getMessage();
    		System.err.println(errmsg);
    		logger.error(errmsg);
    		return null;
    	}
    }
    
	// ErrorListener class that can be used to register a handler for any fatal errors, errors and warnings that may
	// occur when transforming an xml file with an xslt stylesheet. The errors are printed both to the greenstone.log and 
	// to the tomcat console (System.err), and the error message is stored in the errorMessage variable so that it can
	// be retrieved and be used to generate an xhtml error page.
	static public class TransformErrorListener implements ErrorListener {
		protected String errorMessage = null;
	
		//  Receive notification of a recoverable error.
		public void error(TransformerException exception) {
			handleError("Error:\n", exception);
		}
        //   Receive notification of a non-recoverable error.
		public void fatalError(TransformerException exception) {
			handleError("Fatal Error:\n", exception);
		}
		// Receive notification of a warning.
        public void warning(TransformerException exception) {
			handleError("Warning:\n", exception);
        }
		
		public String toString(TransformerException e) {
			String location = e.getLocationAsString();
			if(location == null) {
				return e.getMessage();
			}
			return e.getMessage() + "\n" + location;
		}
		
		// clears the errorPage variable after first call to this method
		public String getErrorMessage() {
			String errMsg = this.errorMessage;
			if(this.errorMessage != null) {
				this.errorMessage = null;
			}
			return errMsg;
		}
		
		// sets the errorMessage member variable to the data stored in the exception
		// and writes the errorMessage to the logger and tomcat's System.err
		protected void handleError(String errorType, TransformerException exception) {
			this.errorMessage = errorType + toString(exception); 
			System.err.println("\n****Error transforming xml:\n" + this.errorMessage + "\n****\n");
			logger.error(this.errorMessage);
		}
	}
}
