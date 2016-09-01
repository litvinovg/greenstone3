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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XMLTransformer - utility class for greenstone
 * 
 * transforms xml using xslt
 * 
 * @author Katherine Don
 * @version $Revision$
 */
public class XMLTransformer
{
	private static int debugFileCount = 0; // for unique filenames when debugging XML transformations with physical files

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.XMLTransformer.class.getName());

	/** The transformer factory we're using */
	TransformerFactory t_factory = null;

	/**
	 * The no-arguments constructor.
	 * 
	 * Any exceptions thrown are caught internally
	 * 
	 * @see javax.xml.transform.TransformerFactory
	 */
	public XMLTransformer()
	{
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
		try
		{
			this.t_factory = org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
			//this.t_factory = TransformerFactory.newInstance();
			this.t_factory.setErrorListener(new TransformErrorListener()); // handle errors in the xml Source used to instantiate transformers
		}
		catch (Exception e)
		{
			logger.error("exception creating t_factory " + e.getMessage());
		}
	}

	/**
	 * Transform an XML document using a XSLT stylesheet
	 * 
	 * @param stylesheet
	 *            a filename for an XSLT stylesheet
	 * @param xml_in
	 *            the XML to be transformed
	 * @return the transformed XML
	 */
	public String transform(String stylesheet, String xml_in)
	{

		try
		{
			TransformErrorListener transformerErrorListener = (TransformErrorListener) this.t_factory.getErrorListener();
			transformerErrorListener.setStylesheet(stylesheet);
			// Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
			Transformer transformer = this.t_factory.newTransformer(new StreamSource(stylesheet));

			// Use the Transformer to transform an XML Source and send the output to a Result object.
			StringWriter output = new StringWriter();
			StreamSource streamSource = new StreamSource(new StringReader(xml_in));
			transformer.setErrorListener(new TransformErrorListener(stylesheet, streamSource));
			transformer.transform(streamSource, new StreamResult(output));
			return output.toString();
		}
		catch (TransformerConfigurationException e)
		{
			logger.error("couldn't create transformer object: " + e.getMessageAndLocation());
			logger.error(e.getLocationAsString());
			return "";
		}
		catch (TransformerException e)
		{
			logger.error("couldn't transform the source: " + e.getMessageAndLocation());
			return "";
		}
	}

	public String transformToString(Document stylesheet, Document source)
	{
		return transformToString(stylesheet, source, null);
	}

	public String transformToString(Document stylesheet, Document source, HashMap parameters)
	{

		try
		{
			TransformErrorListener transformerErrorListener = (TransformErrorListener) this.t_factory.getErrorListener();
			transformerErrorListener.setStylesheet(stylesheet);
			// Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
			Transformer transformer = this.t_factory.newTransformer(new DOMSource(stylesheet));
			if (parameters != null)
			{
				Set params = parameters.entrySet();
				Iterator i = params.iterator();
				while (i.hasNext())
				{
					Map.Entry m = (Map.Entry) i.next();
					transformer.setParameter((String) m.getKey(), m.getValue());
				}
			}
			//transformer.setParameter("page_lang", source.getDocumentElement().getAttribute(GSXML.LANG_ATT));

			// Use the Transformer to transform an XML Source and send the output to a Result object.
			StringWriter output = new StringWriter();
			DOMSource domSource = new DOMSource(source);

			transformer.setErrorListener(new TransformErrorListener(stylesheet, domSource));
			transformer.transform(domSource, new StreamResult(output));
			return output.toString();
		}
		catch (TransformerConfigurationException e)
		{
			logger.error("couldn't create transformer object: " + e.getMessageAndLocation());
			logger.error(e.getLocationAsString());
			return "";
		}
		catch (TransformerException e)
		{
			logger.error("couldn't transform the source: " + e.getMessageAndLocation());
			return "";
		}
	}

	/**
	 * Transform an XML document using a XSLT stylesheet, but using a DOMResult
	 * whose node should be set to the Document donated by resultNode
	 */
	public Node transform_withResultNode(Document stylesheet, Document source, Document resultNode)
	{
		return transform(stylesheet, source, null, null, resultNode);
	}

	public Node transform(Document stylesheet, Document source)
	{
		return transform(stylesheet, source, null, null, null);
	}

	public Node transform(Document stylesheet, Document source, HashMap<String, Object> parameters)
	{
		return transform(stylesheet, source, parameters, null, null);
	}

	public Node transform(Document stylesheet, Document source, HashMap<String, Object> parameters, Document docDocType)
	{
		return transform(stylesheet, source, parameters, docDocType, null);
	}

	// This method will now set the docType in the new document created and returned, if any are specified in the
	// (merged) stylesheet being applied. The docDocType parameter is therefore no longer necessary nor used by default.
	protected Node transform(Document stylesheet, Document source, HashMap<String, Object> parameters, Document docDocType, Document resultNode)
	{
		try
		{
			// Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
			TransformErrorListener transformerErrorListener = (TransformErrorListener) this.t_factory.getErrorListener();
			transformerErrorListener.setStylesheet(stylesheet);
			Transformer transformer = this.t_factory.newTransformer(new DOMSource(stylesheet));
			//logger.info("XMLTransformer transformer is " + transformer); //done in ErrorListener

			if (parameters != null)
			{
				Set params = parameters.entrySet();
				Iterator i = params.iterator();
				while (i.hasNext())
				{
					Map.Entry m = (Map.Entry) i.next();
					transformer.setParameter((String) m.getKey(), m.getValue());
				}
			}

			// When we transform the DOMResult, we need to make sure the result of
			// the transformation has a DocType. For that to happen, we need to create
			// the DOMResult using a Document with a predefined docType.

			// When the DOCType is not explicitly specified (default case), the docDocType variable is null
			// In such a case, the transformer will work out the docType and output method and the rest 
			// from the stylesheet. Better to let the transformer work this out than GS manually aggregating 
			// all xsls being applied and the GS code deciding on which output method and doctype to use.

			//DOMResult result = docDocType == null ? new DOMResult() : new DOMResult(docDocType);
			DOMResult result = null;

			Properties props = transformer.getOutputProperties();
			if(docDocType == null) { // default case
			
			    String outputMethod = props.getProperty(OutputKeys.METHOD);
			    if(outputMethod.equals("html")) {				
				String doctype_public = props.getProperty(OutputKeys.DOCTYPE_PUBLIC);
				String doctype_system = props.getProperty(OutputKeys.DOCTYPE_SYSTEM);
				
				if(doctype_public == null) {
				  //doctype_public = ""; // or default to PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"?
				  doctype_public = "-//W3C//DTD HTML 4.01 Transitional//EN";
				}
				if(doctype_system == null) {
				  //doctype_system = ""; // or default to "http://www.w3.org/TR/html4/loose.dtd"?
				  doctype_system = "http://www.w3.org/TR/html4/loose.dtd";
				}

				Document docDocTypeFromTransformer = XMLConverter.newDOM(outputMethod, doctype_public, doctype_system);
				result = new DOMResult(docDocTypeFromTransformer);
			    } 
			    // if output method=xml, the <?xml ?> processing method goes missing hereafter, although it
			    // still exists in OutputKeys' VERSION, ENCODING and OMIT_XML_DECLARATION props at this point
					    
			} else { // if document with doctype was already specified (no longer the default case)
			    result = new DOMResult(docDocType);
			}
			// At this point if we haven't initialised result yet, set it to an empty DOMResult
			if(result == null) {
			    result = new DOMResult();
			}


			if (resultNode != null)
			{
				result.setNode(resultNode);
			}
			DOMSource domSource = new DOMSource(source);
			transformer.setErrorListener(new TransformErrorListener(stylesheet, domSource));
			transformer.transform(domSource, result);
			return result.getNode(); // pass the entire document
		}
		catch (TransformerConfigurationException e)
		{
			return transformError("XMLTransformer.transform(Doc, Doc, HashMap, Doc)" + "\ncouldn't create transformer object", e);
		}
		catch (TransformerException e)
		{
			return transformError("XMLTransformer.transform(Doc, Doc, HashMap, Doc)" + "\ncouldn't transform the source", e);
		}
	}

    /**
      When transforming an XML with an XSLT from the command-line, there's two problems if calling 
      XMLTransformer.transform(File xslt, File xml):

      1. Need to run the transformation process from the location of the stylesheet, else it can't find
      the stylesheet in order to load it. This is resolved by setting the SystemID for the stylesheet.

      2. The XSLT stylesheet has &lt;xsl:import&gt; statements importing further XSLTs which are furthermore 
      specified by relative paths, requiring that the XSLT (and perhaps also XML) input file is located
      in the folder from which the relative paths of the import statements are specified. This is resolved
      by working out the absolute path of each imported XSLT file and setting their SystemIDs too.

      Without solving these problems, things will only work if we 1. run the transformation from the 
      web/interfaces/default/transform folder and 2. need to have the input XSLT file (and XML?)
      placed in the web/interfacces/default/transform folder too

      Both parts of the solution are described in:
      http://stackoverflow.com/questions/3699860/resolving-relative-paths-when-loading-xslt-filse

      After the systemID on the XSLT input file is set, we can run the command from the toplevel GS3 
      folder.
      After resolving the URIs of the XSLT files imported by the input XSLT, by setting their systemIDs,
      the input XSLT (and XML) file need not be located in web/interfaces/default/transform anymore.
     */
    public Node transform(File stylesheet, File source, String interfaceName, Document docDocType)
	{
		try
		{
			TransformErrorListener transformerErrorListener = (TransformErrorListener) this.t_factory.getErrorListener();
			transformerErrorListener.setStylesheet(stylesheet);

			// http://stackoverflow.com/questions/3699860/resolving-relative-paths-when-loading-xslt-files
			Source xsltSource = new StreamSource(new InputStreamReader(new FileInputStream(stylesheet), "UTF-8"));
			URI systemID = stylesheet.toURI();
			try {			    
			    URL url = systemID.toURL();			
			    xsltSource.setSystemId(url.toExternalForm());
			} catch (MalformedURLException mue) {
			    logger.error("Warning: Unable to set systemID for stylesheet to " + systemID);
			    logger.error("Got exception: " + mue.getMessage(), mue);
			}

			this.t_factory.setURIResolver(new ClasspathResourceURIResolver(interfaceName));			
			Transformer transformer = this.t_factory.newTransformer(xsltSource);
			// Alternative way of instantiating a newTransformer():
			//Templates cachedXSLT = this.t_factory.newTemplates(xsltSource);
			//Transformer transformer = cachedXSLT.newTransformer();

			DOMResult result = (docDocType == null) ? new DOMResult() : new DOMResult(docDocType);
			StreamSource streamSource = new StreamSource(new InputStreamReader(new FileInputStream(source), "UTF-8"));

			transformer.setErrorListener(new TransformErrorListener(stylesheet, streamSource));

			transformer.transform(streamSource, result);
			return result.getNode().getFirstChild();
		}
		catch (TransformerConfigurationException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't create transformer object for files\n" + stylesheet + "\n" + source, e);
		}
		catch (TransformerException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't transform the source for files\n" + stylesheet + "\n" + source, e);
		}
		catch (UnsupportedEncodingException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't read file due to an unsupported encoding\n" + stylesheet + "\n" + source, e);
		}
		catch (FileNotFoundException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't find the file specified\n" + stylesheet + "\n" + source, e);
		}
	}

    /** 
     * Class for resolving the relative paths used for &lt;xsl:import&gt;s 
     * when transforming XML with an XSLT
    */
    class ClasspathResourceURIResolver implements URIResolver {
	String interface_name;

	public ClasspathResourceURIResolver(String interfaceName) {
	    interface_name = interfaceName;	    
	}

	/** 
	 * Override the URIResolver.resolve() method to turn relative paths of imported xslt files into
	 * absolute paths and set these absolute paths as their SystemIDs when loading them into memory.
	 * @see http://stackoverflow.com/questions/3699860/resolving-relative-paths-when-loading-xslt-files
	 */
	
	@Override
	    public Source resolve(String href, String base) throws TransformerException {
	    
	    //System.err.println("href: " + href); // e.g. href: layouts/main.xsl
	    //System.err.println("base: " + base); // e.g for *toplevel* base: file:/path/to/gs3-svn/CL1.xslt


	    // 1. for any xsl imported by the original stylesheet, try to work out its absolute path location
	    // by concatenating the parent folder of the base stylesheet file with the href of the file it
	    // imports (which is a path relative to the base file). This need not hold true for the very 1st
	    // base stylesheet: it may be located somewhere entirely different from the greenstone XSLT
	    // files it refers to. This is the case when running transforms on the commandline for testing

	    File importXSLfile = new File(href); // assume the xsl imported has an absolute path

	    if(!importXSLfile.isAbsolute()) { // imported xsl specifies a path relative to parent of base
		try {
		    URI baseURI = new URI(base);
		    importXSLfile = new File(new File(baseURI).getParent(), href);

		    if(!importXSLfile.exists()) { // if the imported file does not exist, it's not
			// relative to the base (the stylesheet file importing it). So look in the
			// interface's transform subfolder for the xsl file to be imported
			importXSLfile = new File(GSFile.interfaceStylesheetFile(GlobalProperties.getGSDL3Home(), interface_name, href));
		    }
		} catch(URISyntaxException use) {
		    importXSLfile = new File(href); // try
		}
	    }
	    
	    String importXSLfilepath = ""; // for printing the expected file path on error
	    try {
		// path of import XSL file after resolving any .. and . dir paths
		importXSLfilepath = importXSLfile.getCanonicalPath();
	    } catch(IOException ioe) { // resort to using the absolute path
		importXSLfilepath = importXSLfile.getAbsolutePath();
	    }

	    // 2. now we know where the XSL file being imported lives, so set the systemID to its
	    // absolute path when loading it into a Source object
	    URI systemID = importXSLfile.toURI();
	    Source importXSLsrc = null;
	    try {
		importXSLsrc = new StreamSource(new InputStreamReader(new FileInputStream(importXSLfile), "UTF-8"));		
		URL url = systemID.toURL();			
		importXSLsrc.setSystemId(url.toExternalForm());
	    } catch (MalformedURLException mue) {
		logger.error("Warning: Unable to set systemID for imported stylesheet to " + systemID);
		logger.error("Got exception: " + mue.getMessage(), mue);
	    } catch(FileNotFoundException fne) {
		logger.error("ERROR: importXSLsrc file does not exist " + importXSLfilepath);
		logger.error("\tError msg: " + fne.getMessage(), fne);
	    } catch(UnsupportedEncodingException uee) {
		logger.error("ERROR: could not resolve relative path of import XSL file " + importXSLfilepath
			     + " because of encoding issues: " + uee.getMessage(), uee);
	    }	    
	    return importXSLsrc;
	    //return new StreamSource(this.getClass().getClassLoader().getResourceAsStream(href)); // won't work
	}	
    }

	public Node transform(File stylesheet, File source)
	{
		return transform(stylesheet, source, null);
	}

	// debugAsFile is only to be set to true when either the stylesheet or source parameters 
	// are not objects of type File. The debugAsFile variable is passed into the 
	// TransformErrorListener. When set to true, the TransformErrorListener will itself create
	// two files containing the stylesheet and source XML, and try to transform the new source
	// file with the stylesheet file for debugging purposes.
	protected Node transform(File stylesheet, File source, Document docDocType)
	{
		try
		{
			TransformErrorListener transformerErrorListener = (TransformErrorListener) this.t_factory.getErrorListener();
			transformerErrorListener.setStylesheet(stylesheet);
			Transformer transformer = this.t_factory.newTransformer(new StreamSource(new InputStreamReader(new FileInputStream(stylesheet), "UTF-8")));
			DOMResult result = (docDocType == null) ? new DOMResult() : new DOMResult(docDocType);
			StreamSource streamSource = new StreamSource(new InputStreamReader(new FileInputStream(source), "UTF-8"));

			transformer.setErrorListener(new TransformErrorListener(stylesheet, streamSource));

			transformer.transform(streamSource, result);
			return result.getNode().getFirstChild();
		}
		catch (TransformerConfigurationException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't create transformer object for files\n" + stylesheet + "\n" + source, e);
		}
		catch (TransformerException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't transform the source for files\n" + stylesheet + "\n" + source, e);
		}
		catch (UnsupportedEncodingException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't read file due to an unsupported encoding\n" + stylesheet + "\n" + source, e);
		}
		catch (FileNotFoundException e)
		{
			return transformError("XMLTransformer.transform(File, File)" + "\ncouldn't find the file specified\n" + stylesheet + "\n" + source, e);
		}
	}

	// Given a heading string on the sort of transformation error that occurred and the exception object itself, 
	// this method prints the exception to the tomcat window (system.err) and the greenstone log and then returns
	// an xhtml error page that is constructed from it.
	protected Node transformError(String heading, Exception e)
	{
		String message = heading + "\n" + e.getMessage();
		logger.error(heading + ": " + e.getMessage());

		if (e instanceof TransformerException)
		{
			String location = ((TransformerException) e).getLocationAsString();
			if (location != null)
			{
				logger.error(location);
				message = message + "\n" + location;
			}
		}
		System.err.println("****\n" + message + "\n****");
		return constructErrorXHTMLPage(message);
	}

	// Given an error message, splits it into separate lines based on any newlines present and generates an xhtml page
	// (xml Element)  with paragraphs for each line. This is then returned so that it can be displayed in the browser.
	public static Element constructErrorXHTMLPage(String message)
	{
		try
		{
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

			for (int i = 0; i < lines.length; i++)
			{
				Node pNode = xhtmlDoc.createElement("p");
				Node textNode = xhtmlDoc.createTextNode(lines[i]);
				pNode.appendChild(textNode);
				bodyNode.appendChild(pNode);
			}

			return xhtmlDoc.getDocumentElement();

		}
		catch (Exception e)
		{
			String errmsg = "Exception trying to construct error xhtml page from message: " + message + "\n" + e.getMessage();
			System.err.println(errmsg);
			logger.error(errmsg);
			return null;
		}
	}

	// ErrorListener class for both Transformer objects and TransformerFactory objects.
	// This class can be used to register a handler for any fatal errors, errors and warnings that
	// may occur when either transforming an xml file with an xslt stylesheet using the XMLTransformer,
	// or when instantiating a Transformer object using the XMLTransformer's TransformerFactory member var.
	// The latter case occurs when the xml Source used to instantiate a Transformer from a TransformerFactory
	// is invalid in some manner, which results in a null Transformer object. However, as no 
	// TransformerConfigurationException or TransformerException are thrown in this case, the errors 
	// would have not been noticed until things go wrong later when trying to use the (null) Transformer.
	//
	// The errors caught by this ErrorListener class are printed both to the greenstone.log and to the 
	// tomcat console (System.err), and the error message is stored in the errorMessage variable so that 
	// it can be retrieved and be used to generate an xhtml error page.
	public class TransformErrorListener implements ErrorListener
	{
		protected String errorMessage = null;
		protected String stylesheet = null;
		protected Source source = null; // can be DOMSource or StreamSource
		protected boolean debugAsFile = true; // true if xslt or source are not real physical files

		// *********** METHODS TO BE CALLED WHEN SETTING AN ERROR LISTENER ON TRANSFORMERFACTORY OBJECTS
		// The default constructor is only for when setting an ErrorListener on TransformerFactory objects
		public TransformErrorListener()
		{
			this.stylesheet = null;
			this.source = null;
			XMLTransformer.debugFileCount++;
		}

		public void setStylesheet(Document xslt)
		{
			this.debugAsFile = true;
			this.stylesheet = GSXML.elementToString(xslt.getDocumentElement(), true);
			this.source = null;
		}

		public void setStylesheet(String xslt)
		{
			this.debugAsFile = true;
			this.stylesheet = xslt;
			this.source = null;
		}

		public void setStylesheet(File xslt)
		{
			this.debugAsFile = false; // if this constructor is called, we're dealing with physical files for both xslt and source			
			this.stylesheet = xslt.getAbsolutePath();
			this.source = null;
		}

		// *********** METHODS TO BE CALLED WHEN SETTING AN ERROR LISTENER ON TRANSFORMERFACTORY OBJECTS
		// When setting an ErrorListener on Transformer object, the ErrorListener takes a Stylesheet xslt and a Source		
		public TransformErrorListener(String xslt, Source source)
		{
			this.stylesheet = xslt;
			this.source = source;
			XMLTransformer.debugFileCount++;
		}

		public TransformErrorListener(Document xslt, Source source)
		{
			this.stylesheet = GSXML.elementToString(xslt.getDocumentElement(), true);
			this.source = source;
			XMLTransformer.debugFileCount++;
		}

		public TransformErrorListener(File xslt, Source source)
		{
			this.debugAsFile = false; // if this constructor is called, we're dealing with physical files for both xslt and source
			this.source = source;
			this.stylesheet = xslt.getAbsolutePath(); // not necessary to get the string from the file
			// all we were going to do with it *on error* was write it out to a file anyway		
		}

		// *********** METHODS CALLED AUTOMATICALLY ON ERROR

		//  Receive notification of a recoverable error.
		public void error(TransformerException exception)
		{
			handleError("Error:\n", exception);
		}

		// Receive notification of a non-recoverable error.
		public void fatalError(TransformerException exception)
		{
			handleError("Fatal Error:\n", exception);
		}

		// Receive notification of a warning.
		public void warning(TransformerException exception)
		{
			handleError("Warning:\n", exception);
		}

		public String toString(TransformerException e)
		{
			String msg = "Exception encountered was:\n\t";
			String location = e.getLocationAsString();
			if (location != null)
			{
				msg = msg + "Location: " + location + "\n\t";
			}

			return msg + "Message: " + e.getMessage();
		}

		// clears the errorPage variable after the first call to this method
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
		protected void handleError(String errorType, TransformerException exception)
		{

			this.errorMessage = errorType + toString(exception);

			// If either the stylesheet or the source to be transformed with it were not files,
			// so that the transformation was performed in-memory, then the "location" information
			// during the error handling (if any) wouldn't have been helpful.
			// To allow proper debugging, we write both stylesheet and source out as physical files
			// and perform the same transformation again, so that when a transformation error does 
			// occur, the files are not in-memory but can be viewed, and any location information
			// for the error given by the ErrorListener will be sensible (instead of the unhelpful
			// "line#0 column#0 in file://somewhere/dummy.xsl").
			// Note that if the stylesheet and the source it is to transform were both physical 
			// files to start off with, we will not need to perform the same transformation again
			// since the error reporting would have provided accurate locations for those.
			if (debugAsFile)
			{
			    System.err.println("\n****Original error transforming xml:\n" + this.errorMessage + "\n****\n"); 
			    System.err.println("***** About to perform the transform again with actual files\n******\n");
			    logger.error("Original transformation error: " + this.errorMessage);
			    logger.error("**** About to perform transform again with actual files");


				performTransformWithPhysicalFiles(); // will give accurate line numbers

				// No need to print out the current error message (seen in the Else statement below), 
				// as the recursive call to XMLTransformer.transform(File, File, false) in method
				// performTransformWithPhysicalFiles() will do this for us.
			}
			else
			{
				// printing out the error message
				// since !debugAsFile, we are dealing with physical files, 
				// variable stylesheet would have stored the filename instead of contents
				this.errorMessage = this.errorMessage + "\nstylesheet filename: " + stylesheet;

				this.errorMessage += "\nException CAUSE:\n" + exception.getCause();
				System.err.println("\n****Error transforming xml:\n" + this.errorMessage + "\n****\n");
				//System.err.println("Stylesheet was:\n + this.stylesheet + "************END STYLESHEET***********\n\n");		    

				logger.error(this.errorMessage);

				// now print out the source to a file, and run the stylesheet on it using a transform()
				// then any error will be referring to one of these two input files.
			}
		}

		// This method will redo the transformation that went wrong with *real* files: 
		// it writes out the stylesheet and source XML to files first, then performs the transformation
		// to get the actual line location of where things went wrong (instead of "line#0 column#0 in dummy.xsl")
		protected void performTransformWithPhysicalFiles()
		{
			File webLogsTmpFolder = new File(GlobalProperties.getGSDL3Home() + File.separator + "logs" + File.separator + "tmp");
			if (!webLogsTmpFolder.exists())
			{
				webLogsTmpFolder.mkdirs(); // create any necessary folders
			}
			File styleFile = new File(webLogsTmpFolder + File.separator + "stylesheet" + XMLTransformer.debugFileCount + ".xml");
			File sourceFile = new File(webLogsTmpFolder + File.separator + "source" + XMLTransformer.debugFileCount + ".xml");
			try
			{
				// write stylesheet to a file called stylesheet_systemID in tmp
				FileWriter styleSheetWriter = new FileWriter(styleFile);
				styleSheetWriter.write(stylesheet, 0, stylesheet.length());
				styleSheetWriter.flush();
				styleSheetWriter.close();
			}
			catch (Exception e)
			{
				System.err.println("*** Exception when trying to write out stylesheet to " + styleFile.getAbsolutePath());
			}

			if (this.source != null)
			{ // ErrorListener was set on a Transformer object
				try
				{
					FileWriter srcWriter = new FileWriter(sourceFile);
					String contents = "";
					if (source instanceof DOMSource)
					{
						DOMSource domSource = (DOMSource) source;
						Document doc = (Document) domSource.getNode();
						contents = GSXML.elementToString(doc.getDocumentElement(), true);
						//contents = GSXML.xmlNodeToXMLString(domSource.getNode());
					}
					else if (source instanceof StreamSource)
					{
						StreamSource streamSource = (StreamSource) source;
						BufferedReader reader = new BufferedReader(streamSource.getReader());
						String line = "";
						while ((line = reader.readLine()) != null)
						{
							contents = contents + line + "\n";
						}
					}
					srcWriter.write(contents, 0, contents.length());
					srcWriter.flush();
					srcWriter.close();
				}
				catch (Exception e)
				{
					System.err.println("*** Exception when trying to write out stylesheet to " + sourceFile.getAbsolutePath());
				}
			}

			System.err.println("*****************************************");
			System.err.println("Look for stylesheet in: " + styleFile.getAbsolutePath());
			if (this.source != null)
			{ // ErrorListener was set on a Transformer object
				System.err.println("Look for source XML in: " + sourceFile.getAbsolutePath());
			}

			// now perform the transform again, which will assign another TransformErrorListener
			// but since debuggingAsFile is turned off, we won't recurse into this section of
			// handling the error again
			if (this.source != null)
			{ // ErrorListener was set on a Transformer object
				XMLTransformer.this.transform(styleFile, sourceFile); // calls the File, File version, so debugAsFile will be false		
			}
			else
			{ // ErrorListener was set on a TransformerFactory object

				// The recursive step in this case is to perform the instantiation 
				// of the Transformer object again.
				// Only one TransformerFactory object per XMLTransformer, 
				// and only one TransformerHandler object set on any TransformerFactory
				// But the stylesheet used to create a Transformer from that TransformerFactory
				// object changes each time, by calls to setStylesheet(), 
				// Therefore, the debugAsFile state for the single TransformerFactory's 
				// TransformerHandler changes each time also.

				try
				{
					debugAsFile = false;
					this.stylesheet = styleFile.getAbsolutePath();
					//TransformErrorListener transformerErrorListener = (TransformErrorListener)XMLTransformer.this.t_factory.getErrorListener();
					//transformerErrorListener.setStylesheet(styleFile);
					Transformer transformer = XMLTransformer.this.t_factory.newTransformer(new StreamSource(styleFile));
					if (transformer == null)
					{
						String msg = "XMLTransformer transformer is " + transformer;
						logger.info(msg);
						System.out.println(msg + "\n****\n");
					}
				}
				catch (TransformerConfigurationException e)
				{
					String message = "Couldn't create transformer object: " + e.getMessageAndLocation();
					logger.error(message);
					logger.error(e.getLocationAsString());
					System.out.println(message);
				}
			}
		}
	}
}
