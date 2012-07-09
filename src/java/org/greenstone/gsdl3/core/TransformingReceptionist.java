package org.greenstone.gsdl3.core;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.greenstone.gsdl3.action.Action;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.gsdl3.util.XMLTransformer;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * A receptionist that uses xslt to transform the page_data before returning it.
 * . Receives requests consisting of an xml representation of cgi args, and
 * returns the page of data - in html by default. The requests are processed by
 * the appropriate action class
 * 
 * @see Action
 */
public class TransformingReceptionist extends Receptionist
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.TransformingReceptionist.class.getName());

	/** The preprocess.xsl file is in a fixed location */
	static final String preprocess_xsl_filename = GlobalProperties.getGSDL3Home() + File.separatorChar + "interfaces" + File.separatorChar + "core" + File.separatorChar + "transform" + File.separatorChar + "preProcess.xsl";

	/** the list of xslt to use for actions */
	protected HashMap<String, String> xslt_map = null;

	/** a transformer class to transform xml using xslt */
	protected XMLTransformer transformer = null;

	protected TransformerFactory transformerFactory = null;
	protected DOMParser parser = null;

	boolean _debug = false;

	public TransformingReceptionist()
	{
		super();
		this.xslt_map = new HashMap<String, String>();
		this.transformer = new XMLTransformer();
		try
		{
			transformerFactory = org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
			this.converter = new XMLConverter();
			//transformerFactory.setURIResolver(new MyUriResolver()) ;

			parser = new DOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", false);
			// don't try and load external DTD - no need if we are not validating, and may cause connection errors if a proxy is not set up.
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// a performance test showed that having this on lead to increased 
			// memory use for small-medium docs, and not much gain for large 
			// docs.
			// http://www.sosnoski.com/opensrc/xmlbench/conclusions.html
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
			// setting a handler for when fatal errors, errors or warnings happen during xml parsing
			// call XMLConverter's getParseErrorMessage() to get the errorstring that can be rendered as web page
			this.parser.setErrorHandler(new XMLConverter.ParseErrorHandler());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** configures the receptionist - overwrite this to set up the xslt map */
	public boolean configure()
	{

		if (this.config_params == null)
		{
			logger.error(" config variables must be set before calling configure");
			return false;
		}
		if (this.mr == null)
		{
			logger.error(" message router must be set  before calling configure");
			return false;
		}

		// find the config file containing a list of actions
		File interface_config_file = new File(GSFile.interfaceConfigFile(GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.INTERFACE_NAME))));
		if (!interface_config_file.exists())
		{
			logger.error(" interface config file: " + interface_config_file.getPath() + " not found!");
			return false;
		}
		Document config_doc = this.converter.getDOM(interface_config_file, "utf-8");
		if (config_doc == null)
		{
			logger.error(" could not parse interface config file: " + interface_config_file.getPath());
			return false;
		}
		Element config_elem = config_doc.getDocumentElement();
		String base_interface = config_elem.getAttribute("baseInterface");
		setUpBaseInterface(base_interface);
		setUpInterfaceOptions(config_elem);

		Element action_list = (Element) GSXML.getChildByTagName(config_elem, GSXML.ACTION_ELEM + GSXML.LIST_MODIFIER);
		NodeList actions = action_list.getElementsByTagName(GSXML.ACTION_ELEM);

		for (int i = 0; i < actions.getLength(); i++)
		{
			Element action = (Element) actions.item(i);
			String class_name = action.getAttribute("class");
			String action_name = action.getAttribute("name");
			Action ac = null;
			try
			{
				ac = (Action) Class.forName("org.greenstone.gsdl3.action." + class_name).newInstance();
			}
			catch (Exception e)
			{
				logger.error(" couldn't load in action " + class_name);
				e.printStackTrace();
				continue;
			}
			ac.setConfigParams(this.config_params);
			ac.setMessageRouter(this.mr);
			ac.configure();
			ac.addActionParameters(this.params);
			this.action_map.put(action_name, ac);

			// now do the xslt map
			String xslt = action.getAttribute("xslt");
			if (!xslt.equals(""))
			{
				this.xslt_map.put(action_name, xslt);
			}
			NodeList subactions = action.getElementsByTagName(GSXML.SUBACTION_ELEM);
			for (int j = 0; j < subactions.getLength(); j++)
			{
				Element subaction = (Element) subactions.item(j);
				String subname = subaction.getAttribute(GSXML.NAME_ATT);
				String subxslt = subaction.getAttribute("xslt");

				String map_key = action_name + ":" + subname;
				logger.debug("adding in to xslt map, " + map_key + "->" + subxslt);
				this.xslt_map.put(map_key, subxslt);
			}
		}
		Element lang_list = (Element) GSXML.getChildByTagName(config_elem, "languageList");
		if (lang_list == null)
		{
			logger.error(" didn't find a language list in the config file!!");
		}
		else
		{
			this.language_list = (Element) this.doc.importNode(lang_list, true);
		}

		return true;
	}

	protected Node postProcessPage(Element page)
	{
		// might need to add some data to the page
		addExtraInfo(page);
		// transform the page using xslt
		Node transformed_page = transformPage(page);

		// if the user has specified they want only a part of the full page then subdivide it
		boolean subdivide = false;
		String excerptID = null;
		String excerptTag = null;
		Element request = (Element) GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (cgi_param_list != null)
		{
			HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);
			if ((excerptID = (String) params.get(GSParams.EXCERPT_ID)) != null)
			{
				subdivide = true;
			}
			if ((excerptTag = (String) params.get(GSParams.EXCERPT_TAG)) != null)
			{
				subdivide = true;
			}
		}

		if (subdivide)
		{
			Node subdivided_page = subdivide(transformed_page, excerptID, excerptTag);
			if (subdivided_page != null)
			{
				return subdivided_page;
			}
		}

		return transformed_page;
	}

	protected Node subdivide(Node transformed_page, String excerptID, String excerptTag)
	{
		if (excerptID != null)
		{
			Node selectedElement = getNodeByIdRecursive(transformed_page, excerptID);
			modifyNodesByTagRecursive(selectedElement, "a");
			return selectedElement;
		}
		else if (excerptTag != null)
		{
			/*
			 * // define a list
			 * 
			 * Node selectedElement =
			 * modifyNodesByTagRecursive(transformed_page, excerptTag);
			 */

			Node selectedElement = getNodeByTagRecursive(transformed_page, excerptTag);
			return selectedElement;

		}
		return transformed_page;
	}

	protected Node getNodeByIdRecursive(Node parent, String id)
	{
		if (parent.hasAttributes() && ((Element) parent).getAttribute("id").equals(id))
		{
			return parent;
		}

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node result = null;
			if ((result = getNodeByIdRecursive(children.item(i), id)) != null)
			{
				return result;
			}
		}
		return null;
	}

	protected Node getNodeByTagRecursive(Node parent, String tag)
	{
		if (parent.getNodeType() == Node.ELEMENT_NODE && ((Element) parent).getTagName().equals(tag))
		{
			return parent;
		}

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node result = null;
			if ((result = getNodeByTagRecursive(children.item(i), tag)) != null)
			{
				return result;
			}
		}
		return null;
	}

	protected Node modifyNodesByTagRecursive(Node parent, String tag)
	{
		if (parent == null || (parent.getNodeType() == Node.ELEMENT_NODE && ((Element) parent).getTagName().equals(tag)))
		{
			return parent;
		}

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node result = null;
			if ((result = modifyNodesByTagRecursive(children.item(i), tag)) != null)
			{
				//return result;
				//logger.error("Modify node value = "+result.getNodeValue()); //NamedItem("href"););
				//logger.error("BEFORE Modify node attribute = " + result.getAttributes().getNamedItem("href").getNodeValue());
				//String url = result.getAttributes().getNamedItem("href").getNodeValue();
				//url = url + "&excerptid=results";
				//result.getAttributes().getNamedItem("href").setNodeValue(url);
				//logger.error("AFTER Modify node attribute = " + result.getAttributes().getNamedItem("href").getNodeValue());
			}
		}
		return null;
	}

	/**
	 * overwrite this to add any extra info that might be needed in the page
	 * before transformation
	 */
	protected void addExtraInfo(Element page)
	{
	}

	/**
	 * transform the page using xslt we need to get any format element out of
	 * the page and add it to the xslt before transforming
	 */
	protected Node transformPage(Element page)
	{
		boolean allowsClientXSLT = (Boolean) config_params.get(GSConstants.ALLOW_CLIENT_SIDE_XSLT);
		//System.out.println("Client side transforms allowed? " + allowsClientXSLT);

		String currentInterface = (String) config_params.get(GSConstants.INTERFACE_NAME);

		Element request = (Element) GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
		String output = request.getAttribute(GSXML.OUTPUT_ATT);

		//System.out.println("Current output mode is: " + output + ", current interface name is: " + currentInterface);

		if (allowsClientXSLT)
		{
			if (!currentInterface.endsWith(GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX) && output.equals("html"))
			{
				System.out.println("output is html and we are not currently using a client side version, switching");
				// Switch the interface
				config_params.put(GSConstants.INTERFACE_NAME, currentInterface.concat(GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX));
			}
			else if ((currentInterface.endsWith(GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX) && !output.equals("html")) || output.equals("server"))
			{
				// The reverse needs to happen too
				config_params.put(GSConstants.INTERFACE_NAME, currentInterface.substring(0, currentInterface.length() - GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX.length()));
			}
		}
		else if (currentInterface.endsWith(GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX))
		{
			config_params.put(GSConstants.INTERFACE_NAME, currentInterface.substring(0, currentInterface.length() - GSConstants.CLIENT_SIDE_XSLT_INTERFACE_SUFFIX.length()));
		}

		// DocType defaults in case the skin doesn't have an "xsl:output" element
		String qualifiedName = "html";
		String publicID = "-//W3C//DTD HTML 4.01 Transitional//EN";
		String systemID = "http://www.w3.org/TR/html4/loose.dtd";

		// We need to create an empty document with a predefined DocType,
		// that will then be used for the transformation by the DOMResult
		Document docWithDoctype = converter.newDOM(qualifiedName, publicID, systemID);

		if (output.equals("xsltclient"))
		{
			// If you're just getting the client-side transform page, why bother with the rest of this?
			Element html = docWithDoctype.createElement("html");
			Element img = docWithDoctype.createElement("img");
			img.setAttribute("src", "interfaces/default/images/loading.gif"); // Make it dynamic
			img.setAttribute("alt", "Please wait...");
			Text title_text = docWithDoctype.createTextNode("Please wait..."); // Make this language dependent
			Element head = docWithDoctype.createElement("head");
			Element title = docWithDoctype.createElement("title");
			title.appendChild(title_text);
			Element body = docWithDoctype.createElement("body");
			Element script = docWithDoctype.createElement("script");
			Element jquery = docWithDoctype.createElement("script");
			jquery.setAttribute("src", "jquery.js");
			jquery.setAttribute("type", "text/javascript");
			Comment jquery_comment = docWithDoctype.createComment("jQuery");
			Comment script_comment = docWithDoctype.createComment("Filler for browser");
			script.setAttribute("src", "test.js");
			script.setAttribute("type", "text/javascript");
			Element pagevar = docWithDoctype.createElement("script");
			Element style = docWithDoctype.createElement("style");
			style.setAttribute("type", "text/css");
			Text style_text = docWithDoctype.createTextNode("body { text-align: center; padding: 50px; font: 14pt Arial, sans-serif; font-weight: bold; }");
			pagevar.setAttribute("type", "text/javascript");
			Text page_var_text = docWithDoctype.createTextNode("var placeholder = true;");

			html.appendChild(head);
			head.appendChild(title);
			head.appendChild(style);
			style.appendChild(style_text);
			html.appendChild(body);
			head.appendChild(pagevar);
			head.appendChild(jquery);
			head.appendChild(script);
			pagevar.appendChild(page_var_text);
			jquery.appendChild(jquery_comment);
			script.appendChild(script_comment);
			body.appendChild(img);
			docWithDoctype.appendChild(html);

			return (Node) docWithDoctype;
		}

		// Passing in the pretty string here means it needs to be generated even when not debugging; so use custom function to return blank when debug is off
		logger.debug("page before transforming:");
		logger.debug(this.converter.getPrettyStringLogger(page, logger));

		String action = request.getAttribute(GSXML.ACTION_ATT);
		String subaction = request.getAttribute(GSXML.SUBACTION_ATT);

		// we should choose how to transform the data based on output, eg diff
		// choice for html, and wml??
		// for now, if output=xml, we don't transform the page, we just return 
		// the page xml
		Document theXML = null;

		if (output.equals("xml") || output.equals("clientside"))
		{
			// Append some bits and pieces first...
			theXML = converter.newDOM();
			// Import into new document first!
			Node newPage = theXML.importNode(page, true);
			theXML.appendChild(newPage);
			Element root = theXML.createElement("xsltparams");
			newPage.appendChild(root);

			Element libname = theXML.createElement("param");
			libname.setAttribute("name", "library_name");
			Text libnametext = theXML.createTextNode((String) config_params.get(GSConstants.LIBRARY_NAME));
			libname.appendChild(libnametext);

			Element intname = theXML.createElement("param");
			intname.setAttribute("name", "interface_name");
			Text intnametext = theXML.createTextNode((String) config_params.get(GSConstants.INTERFACE_NAME));
			intname.appendChild(intnametext);

			Element siteName = theXML.createElement("param");
			siteName.setAttribute("name", "site_name");
			Text siteNameText = theXML.createTextNode((String) config_params.get(GSConstants.SITE_NAME));
			siteName.appendChild(siteNameText);

			Element filepath = theXML.createElement("param");
			filepath.setAttribute("name", "filepath");
			Text filepathtext = theXML.createTextNode(GlobalProperties.getGSDL3Home());
			filepath.appendChild(filepathtext);

			root.appendChild(libname);
			root.appendChild(intname);
			root.appendChild(siteName);
			root.appendChild(filepath);

			if (output.equals("xml"))
				return theXML.getDocumentElement();
		}

		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		String collection = "";
		String inlineTemplate = "";
		if (cgi_param_list != null)
		{
			// Don't waste time getting all the parameters
			HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);
			collection = (String) params.get(GSParams.COLLECTION);
			if (collection == null)
			{
				collection = "";
			}

			inlineTemplate = (String) params.get(GSParams.INLINE_TEMPLATE);
		}

		Document style_doc = getXSLTDocument(action, subaction, collection);
		if (style_doc == null)
		{
			String errorPage = this.converter.getParseErrorMessage();
			if (errorPage != null)
			{
				return XMLTransformer.constructErrorXHTMLPage("Cannot parse the xslt file\n" + errorPage);
			}
			return page;
		}

		// put the page into a document - this is necessary for xslt to get
		// the paths right if you have paths relative to the document root
		// eg /page.
		Document doc = this.converter.newDOM();
		doc.appendChild(doc.importNode(page, true));
		Element page_response = (Element) GSXML.getChildByTagName(page, GSXML.PAGE_RESPONSE_ELEM);
		Element format_elem = (Element) GSXML.getChildByTagName(page_response, GSXML.FORMAT_ELEM);

		if (output.equals("formatelem"))
		{
			return format_elem;
		}
		if (format_elem != null)
		{
			//page_response.removeChild(format_elem);
			logger.debug("format elem=" + this.converter.getPrettyStringLogger(format_elem, logger));
			// need to transform the format info
			String configStylesheet_file = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, "config_format.xsl");
			Document configStylesheet_doc = this.converter.getDOM(new File(configStylesheet_file));

			if (_debug)
			{
				GSXSLT.modifyConfigFormatForDebug(configStylesheet_doc, GSFile.collectionConfigFile(GSFile.collectDir(GSFile.siteHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME)) + File.separator + collection)));
			}

			if (configStylesheet_doc != null)
			{
				Document format_doc = this.converter.newDOM();
				format_doc.appendChild(format_doc.importNode(format_elem, true));
				Node result = this.transformer.transform(configStylesheet_doc, format_doc, config_params); // Needs addressing <-

				// Since we started creating documents with DocTypes, we can end up with 
				// Document objects here. But we will be working with an Element instead, 
				// so we grab the DocumentElement() of the Document object in such a case.
				Element new_format;
				if (result.getNodeType() == Node.DOCUMENT_NODE)
				{
					new_format = ((Document) result).getDocumentElement();
				}
				else
				{
					new_format = (Element) result;
				}
				logger.debug("new format elem=" + this.converter.getPrettyStringLogger(new_format, logger));
				if (output.equals("newformat"))
				{
					return new_format;
				}

				// add extracted GSF statements in to the main stylesheet
				if (_debug)
				{
					GSXSLT.mergeStylesheetsDebug(style_doc, new_format, true, true, "OTHER1", GSFile.collectionConfigFile(GSFile.collectDir(GSFile.siteHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME)) + File.separator + collection)));
				}
				else
				{
					GSXSLT.mergeStylesheets(style_doc, new_format, true);
				}
				//System.out.println("added extracted GSF statements into the main stylesheet") ;

				// add extracted GSF statements in to the debug test stylesheet
				//GSXSLT.mergeStylesheets(oldStyle_doc, new_format);
			}
			else
			{
				logger.error(" couldn't parse the config_format stylesheet, adding the format info as is");
				GSXSLT.mergeStylesheets(style_doc, format_elem, true);
				//GSXSLT.mergeStylesheets(oldStyle_doc, format_elem);
			}
			logger.debug("the converted stylesheet is:");
			logger.debug(this.converter.getPrettyStringLogger(style_doc.getDocumentElement(), logger));
		}

		//for debug purposes only
		Document oldStyle_doc = style_doc;
		Document preprocessingXsl;
		try
		{
			preprocessingXsl = getDoc(preprocess_xsl_filename);
			String errMsg = ((XMLConverter.ParseErrorHandler) parser.getErrorHandler()).getErrorMessage();
			if (errMsg != null)
			{
				return XMLTransformer.constructErrorXHTMLPage("error loading preprocess xslt file: " + preprocess_xsl_filename + "\n" + errMsg);
			}
		}
		catch (java.io.FileNotFoundException e)
		{
			return fileNotFoundErrorPage(e.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("error loading preprocess xslt");
			return XMLTransformer.constructErrorXHTMLPage("error loading preprocess xslt\n" + e.getMessage());
		}

		Document libraryXsl = null;
		try
		{
			libraryXsl = getDoc(this.getGSLibXSLFilename());
			String errMsg = ((XMLConverter.ParseErrorHandler) parser.getErrorHandler()).getErrorMessage();
			if (errMsg != null)
			{
				return XMLTransformer.constructErrorXHTMLPage("Error loading xslt file: " + this.getGSLibXSLFilename() + "\n" + errMsg);
			}
		}
		catch (java.io.FileNotFoundException e)
		{
			return fileNotFoundErrorPage(e.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("error loading gslib xslt");
			return XMLTransformer.constructErrorXHTMLPage("error loading gslib xslt\n" + e.getMessage());
		}

		//   Combine the skin file and library variables/templates into one document. 
		//   Please note: We dont just use xsl:import because the preprocessing stage  
		//   needs to know what's available in the library.

		Document skinAndLibraryXsl = null;
		Document skinAndLibraryDoc = converter.newDOM();

		// Applying the preprocessing XSLT - in its own block {} to allow use of non-unique variable names
		{

			skinAndLibraryXsl = converter.newDOM();
			Element root = skinAndLibraryXsl.createElement("skinAndLibraryXsl");
			skinAndLibraryXsl.appendChild(root);

			Element s = skinAndLibraryXsl.createElement("skinXsl");
			s.appendChild(skinAndLibraryXsl.importNode(style_doc.getDocumentElement(), true));
			root.appendChild(s);

			Element l = skinAndLibraryXsl.createElement("libraryXsl");
			Element libraryXsl_el = libraryXsl.getDocumentElement();
			l.appendChild(skinAndLibraryXsl.importNode(libraryXsl_el, true));
			root.appendChild(l);
			//System.out.println("Skin and Library XSL are now together") ;

			//System.out.println("Pre-processing the skin file...") ;

			//pre-process the skin style sheet
			//In other words, apply the preProcess.xsl to 'skinAndLibraryXsl' in order to
			//expand all GS-Lib statements into complete XSL statements and also to create
			//a valid xsl style sheet document.

			XMLTransformer preProcessor = new XMLTransformer();
			// Perform the transformation, by passing in:
			// preprocess-stylesheet, source-xsl (skinAndLibraryXsl), and the node that should 
			// be in the result (skinAndLibraryDoc)
			preProcessor.transform_withResultNode(preprocessingXsl, skinAndLibraryXsl, skinAndLibraryDoc);
			//System.out.println("GS-Lib statements are now expanded") ;
		}

		// there is a thing called a URIResolver which you can set for a
		// transformer or transformer factory. may be able to use this
		// instead of this absoluteIncludepaths hack

		//GSXSLT.absoluteIncludePaths(skinAndLibraryDoc, GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces);

		//Same but for the debug version when we want the do the transformation like we use to do
		//without any gslib elements.
		GSXSLT.absoluteIncludePaths(oldStyle_doc, GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces);

		//Send different stages of the skin xslt to the browser for debug purposes only
		//using &o=skindoc or &o=skinandlib etc...
		if (output.equals("skindoc"))
		{
			return converter.getDOM(getStringFromDocument(style_doc));
		}
		if (output.equals("skinandlib"))
		{
			return converter.getDOM(getStringFromDocument(skinAndLibraryXsl));
		}
		if (output.equals("skinandlibdoc") || output.equals("clientside"))
		{

			Node skinAndLib = converter.getDOM(getStringFromDocument(skinAndLibraryDoc));

			if (output.equals("skinandlibdoc"))
			{
				return skinAndLib;
			}
			else
			{
				// Send XML and skinandlibdoc down the line together
				Document finalDoc = converter.newDOM();
				Node finalDocSkin = finalDoc.importNode(skinAndLibraryDoc.getDocumentElement(), true);
				Node finalDocXML = finalDoc.importNode(theXML.getDocumentElement(), true);
				Element root = finalDoc.createElement("skinlibPlusXML");
				root.appendChild(finalDocSkin);
				root.appendChild(finalDocXML);
				finalDoc.appendChild(root);
				return (Node) finalDoc.getDocumentElement();
			}
		}
		if (output.equals("oldskindoc"))
		{
			return converter.getDOM(getStringFromDocument(oldStyle_doc));
		}

		// Try to get the system and public ID from the current skin xsl document
		// otherwise keep the default values.
		Element root = skinAndLibraryDoc.getDocumentElement();
		NodeList nodes = root.getElementsByTagName("xsl:output");
		// If there is at least one "xsl:output" command in the final xsl then...
		if (nodes.getLength() != 0)
		{
			// There should be only one element called xsl:output, 
			// but if this is not the case get the last one
			Element xsl_output = (Element) nodes.item(nodes.getLength() - 1);
			if (xsl_output != null)
			{
				// Qualified name will always be html even for xhtml pages
				//String attrValue = xsl_output.getAttribute("method");
				//qualifiedName = attrValue.equals("") ? qualifiedName : attrValue;

				String attrValue = xsl_output.getAttribute("doctype-system");
				systemID = attrValue.equals("") ? systemID : attrValue;

				attrValue = xsl_output.getAttribute("doctype-public");
				publicID = attrValue.equals("") ? publicID : attrValue;
			}
		}

		//System.out.println(converter.getPrettyString(docWithDoctype));
		//System.out.println("Doctype vals: " + qualifiedName + " " + publicID + " " + systemID) ;

		docWithDoctype = converter.newDOM(qualifiedName, publicID, systemID);

		//System.out.println("Generate final HTML from current skin") ;
		//Transformation of the XML message from the receptionist to HTML with doctype

		if (inlineTemplate != null)
		{
			try
			{
				Document inlineTemplateDoc = this.converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:java=\"http://xml.apache.org/xslt/java\" xmlns:util=\"xalan://org.greenstone.gsdl3.util.XSLTUtil\" xmlns:gsf=\"http://www.greenstone.org/greenstone3/schema/ConfigFormat\">" + inlineTemplate + "</xsl:stylesheet>", "UTF-8");

				if (_debug)
				{
					GSXSLT.mergeStylesheetsDebug(skinAndLibraryDoc, inlineTemplateDoc.getDocumentElement(), true, true, "OTHER2", "INLINE");
				}
				else
				{
					GSXSLT.mergeStylesheets(skinAndLibraryDoc, inlineTemplateDoc.getDocumentElement(), true);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		GSXSLT.inlineImportAndIncludeFiles(skinAndLibraryDoc, null);
		skinAndLibraryDoc = (Document) secondConfigFormatPass(collection, skinAndLibraryDoc, doc, new UserContext(request));

		if (_debug)
		{
			GSXML.addDebugSpanTags(skinAndLibraryDoc);
		}

		if (output.equals("xmlfinal"))
		{
			return doc;
		}

		if (output.equals("skinandlibdocfinal"))
		{
			return converter.getDOM(getStringFromDocument(skinAndLibraryDoc));
		}

		return this.transformer.transform(skinAndLibraryDoc, doc, config_params, docWithDoctype);

		// The line below will do the transformation like we use to do before having Skin++ implemented,
		// it will not contain any GS-Lib statements expanded, and the result will not contain any doctype.

		//return (Element)this.transformer.transform(style_doc, doc, config_params);  
		//return null; // For now - change later
	}

	protected Node secondConfigFormatPass(String collection, Document skinAndLibraryDoc, Document doc, UserContext userContext)
	{
		String to = GSPath.appendLink(collection, "DocumentMetadataRetrieve"); // Hard-wired?
		Element metaMessage = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element metaRequest = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		Element paramList = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element docNodeList = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);

		NodeList metaNodes = skinAndLibraryDoc.getElementsByTagName("gsf:metadata");

		for (int i = 0; i < metaNodes.getLength(); i++)
		{
			Element param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "metadata");
			param.setAttribute(GSXML.VALUE_ATT, ((Element) metaNodes.item(i)).getAttribute(GSXML.NAME_ATT));
			paramList.appendChild(param);
		}
		metaRequest.appendChild(paramList);

		NodeList docNodes = doc.getElementsByTagName("documentNode");
		for (int i = 0; i < docNodes.getLength(); i++)
		{
			Element docNode = this.doc.createElement(GSXML.DOC_NODE_ELEM);
			docNode.setAttribute(GSXML.NODE_ID_ATT, ((Element) docNodes.item(i)).getAttribute(GSXML.NODE_ID_ATT));
			docNode.setAttribute(GSXML.NODE_TYPE_ATT, ((Element) docNodes.item(i)).getAttribute(GSXML.NODE_TYPE_ATT));
			docNodeList.appendChild(docNode);
		}
		metaRequest.appendChild(docNodeList);

		metaMessage.appendChild(metaRequest);
		Element response = (Element) mr.process(metaMessage);

		NodeList metaDocNodes = response.getElementsByTagName(GSXML.DOC_NODE_ELEM);
		for (int i = 0; i < docNodes.getLength(); i++)
		{
			GSXML.mergeMetadataLists(docNodes.item(i), metaDocNodes.item(i));
		}

		String configStylesheet_file = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, "config_format.xsl");
		Document configStylesheet_doc = this.converter.getDOM(new File(configStylesheet_file));

		if (configStylesheet_doc != null)
		{
			return this.transformer.transform(configStylesheet_doc, skinAndLibraryDoc, config_params);
		}
		return skinAndLibraryDoc;
	}

	// method to convert Document to a proper XML string for debug purposes only
	protected String getStringFromDocument(Document doc)
	{
		String content = "";
		try
		{
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			content = writer.toString();
			System.out.println("Change the & to &Amp; for proper debug display");
			content = StringUtils.replace(content, "&", "&amp;");
			writer.flush();
		}
		catch (TransformerException ex)
		{
			ex.printStackTrace();
			return null;
		}
		return content;
	}

	protected synchronized Document getDoc(String docName) throws Exception
	{
		File xslt_file = new File(docName);

		FileReader reader = new FileReader(xslt_file);
		InputSource xml_source = new InputSource(reader);
		this.parser.parse(xml_source);
		Document doc = this.parser.getDocument();

		return doc;
	}

	protected Document getXSLTDocument(String action, String subaction, String collection)
	{
		String name = null;
		if (!subaction.equals(""))
		{
			String key = action + ":" + subaction;
			name = this.xslt_map.get(key);
		}
		// try the action by itself
		if (name == null)
		{
			name = this.xslt_map.get(action);
		}
		// now find the absolute path
		ArrayList<File> stylesheets = GSFile.getStylesheetFiles(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, name);
		if (stylesheets.size() == 0)
		{
			logger.error(" Can't find stylesheet for " + name);
			return null;
		}
		logger.debug("Stylesheet: " + name);

		Document finalDoc = this.converter.getDOM(stylesheets.get(stylesheets.size() - 1), "UTF-8");
		if (finalDoc == null)
		{
			return null;
		}

		for (int i = stylesheets.size() - 2; i >= 0; i--)
		{
			Document currentDoc = this.converter.getDOM(stylesheets.get(i), "UTF-8");
			if (currentDoc == null)
			{
				return null;
			}

			if (_debug)
			{
				GSXSLT.mergeStylesheetsDebug(finalDoc, currentDoc.getDocumentElement(), true, true, stylesheets.get(stylesheets.size() - 1).getAbsolutePath(), stylesheets.get(i).getAbsolutePath());
			}
			else
			{
				GSXSLT.mergeStylesheets(finalDoc, currentDoc.getDocumentElement(), true);
			}
		}

		return finalDoc;
	}

	// returns the path to the gslib.xsl file that is applicable for the current interface
	protected String getGSLibXSLFilename()
	{
		return GSFile.xmlTransformDir(GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.INTERFACE_NAME))) + File.separatorChar + "gslib.xsl";
	}

	// Call this when a FileNotFoundException could be thrown when loading an xsl (xml) file.
	// Returns an error xhtml page indicating which xsl (or other xml) file is missing.
	protected Document fileNotFoundErrorPage(String filenameMessage)
	{
		String errorMessage = "ERROR missing file: " + filenameMessage;
		Element errPage = XMLTransformer.constructErrorXHTMLPage(errorMessage);
		logger.error(errorMessage);
		return errPage.getOwnerDocument();
	}
}
