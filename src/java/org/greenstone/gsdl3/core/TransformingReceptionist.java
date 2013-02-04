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
	protected static final int CONFIG_PASS = 1;
	protected static final int TEXT_PASS = 2;

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.TransformingReceptionist.class.getName());

	/** The preprocess.xsl file is in a fixed location */
	static final String preprocess_xsl_filename = GlobalProperties.getGSDL3Home() + File.separatorChar + "interfaces" + File.separatorChar + "core" + File.separatorChar + "transform" + File.separatorChar + "preProcess.xsl";

	/** the list of xslt to use for actions */
	protected HashMap<String, String> xslt_map = null;

	/** a transformer class to transform xml using xslt */
	protected XMLTransformer transformer = null;

	protected TransformerFactory transformerFactory = null;
	protected DOMParser parser = null;

	protected HashMap<String, ArrayList<String>> _metadataRequiredMap = new HashMap<String, ArrayList<String>>();

	boolean _debug = true;

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

		getRequiredMetadataNamesFromXSLFiles();

		return true;
	}

	protected void getRequiredMetadataNamesFromXSLFiles()
	{
		ArrayList<File> xslFiles = GSFile.getAllXSLFiles((String) this.config_params.get(GSConstants.INTERFACE_NAME), (String) this.config_params.get(GSConstants.SITE_NAME));

		HashMap<String, ArrayList<String>> includes = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<File>> files = new HashMap<String, ArrayList<File>>();
		HashMap<String, ArrayList<String>> metaNames = new HashMap<String, ArrayList<String>>();

		//First exploratory pass
		for (File currentFile : xslFiles)
		{
			Document currentDoc = this.converter.getDOM(currentFile);
			if (currentDoc == null) {
			    // Can happen if an editor creates an auto-save temporary file 
			    // (such as #header.xsl#) that is not well formed XML
			    continue;
			}

			NodeList metadataElems = currentDoc.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "metadata"); //gsf:metadata
			NodeList foreachMetadataElems = currentDoc.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "foreach-metadata"); //gsf:foreach-metadata
			NodeList imageElems = currentDoc.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "image"); //gsf:image
			NodeList includeElems = currentDoc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "include");
			NodeList importElems = currentDoc.getElementsByTagNameNS(GSXML.XSL_NAMESPACE, "import");

			ArrayList<String> names = new ArrayList<String>();
			for (int i = 0; i < metadataElems.getLength(); i++)
			{
				Element current = (Element) metadataElems.item(i);
				String name = current.getAttribute(GSXML.NAME_ATT);
				if (name != null && name.length() > 0 && !names.contains(name))
				{
					names.add(name);
				}
			}

			for (int i = 0; i < foreachMetadataElems.getLength(); i++)
			{
				Element current = (Element) foreachMetadataElems.item(i);
				String name = current.getAttribute(GSXML.NAME_ATT);
				if (name != null && name.length() > 0 && !names.contains(name))
				{
					names.add(name);
				}
			}

			for (int i = 0; i < imageElems.getLength(); i++)
			{
				Element current = (Element) imageElems.item(i);
				String type = current.getAttribute(GSXML.TYPE_ATT);
				if (type == null || type.length() == 0)
				{
					continue;
				}

				if (type.equals("source"))
				{
					String[] standardSourceMeta = new String[] { "SourceFile", "ImageHeight", "ImageWidth", "ImageType", "srcicon" };
					for (String meta : standardSourceMeta)
					{
						if (!names.contains(meta))
						{
							names.add(meta);
						}
					}
				}
				else if (type.equals("screen"))
				{
					String[] standardScreenMeta = new String[] { "Screen", "ScreenHeight", "ScreenWidth", "ScreenType", "screenicon" };
					for (String meta : standardScreenMeta)
					{
						if (!names.contains(meta))
						{
							names.add(meta);
						}
					}
				}
				else if (type.equals("thumb"))
				{
					String[] standardThumbMeta = new String[] { "Thumb", "ThumbHeight", "ThumbWidth", "ThumbType", "thumbicon" };
					for (String meta : standardThumbMeta)
					{
						if (!names.contains(meta))
						{
							names.add(meta);
						}
					}
				}
			}

			metaNames.put(currentFile.getAbsolutePath(), names);

			ArrayList<String> includeAndImportList = new ArrayList<String>();
			for (int i = 0; i < includeElems.getLength(); i++)
			{
				includeAndImportList.add(((Element) includeElems.item(i)).getAttribute(GSXML.HREF_ATT));
			}
			for (int i = 0; i < importElems.getLength(); i++)
			{
				includeAndImportList.add(((Element) importElems.item(i)).getAttribute(GSXML.HREF_ATT));
			}
			includes.put(currentFile.getAbsolutePath(), includeAndImportList);

			String filename = currentFile.getName();
			if (files.get(filename) == null)
			{
				ArrayList<File> fileList = new ArrayList<File>();
				fileList.add(currentFile);
				files.put(currentFile.getName(), fileList);
			}
			else
			{
				ArrayList<File> fileList = files.get(filename);
				fileList.add(currentFile);
			}
		}

		//Second pass
		for (File currentFile : xslFiles)
		{
			ArrayList<File> filesToGet = new ArrayList<File>();
			filesToGet.add(currentFile);

			ArrayList<String> fullNameList = new ArrayList<String>();

			while (filesToGet.size() > 0)
			{
				File currentFileTemp = filesToGet.remove(0);

				//Add the names from this file
				ArrayList<String> currentNames = metaNames.get(currentFileTemp.getAbsolutePath());
				fullNameList.addAll(currentNames);

				ArrayList<String> includedHrefs = includes.get(currentFileTemp.getAbsolutePath());

				for (String href : includedHrefs)
				{
					int lastSepIndex = href.lastIndexOf("/");
					if (lastSepIndex != -1)
					{
						href = href.substring(lastSepIndex + 1);
					}

					ArrayList<File> filesToAdd = files.get(href);
					if (filesToAdd != null)
					{
						filesToGet.addAll(filesToAdd);
					}
				}
			}

			_metadataRequiredMap.put(currentFile.getAbsolutePath(), fullNameList);
		}
	}

	protected void preProcessRequest(Element request)
	{
		String action = request.getAttribute(GSXML.ACTION_ATT);
		String subaction = request.getAttribute(GSXML.SUBACTION_ATT);

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

		String stylesheetFile = GSFile.interfaceStylesheetFile(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.INTERFACE_NAME), name);
		stylesheetFile = stylesheetFile.replace("/", File.separator);

		ArrayList<String> requiredMetadata = _metadataRequiredMap.get(stylesheetFile);

		if (requiredMetadata != null)
		{
			Element extraMetadataList = this.doc.createElement(GSXML.EXTRA_METADATA + GSXML.LIST_MODIFIER);

			for (String metadataString : requiredMetadata)
			{
				Element metadataElem = this.doc.createElement(GSXML.EXTRA_METADATA);
				metadataElem.setAttribute(GSXML.NAME_ATT, metadataString);
				extraMetadataList.appendChild(metadataElem);
			}
			request.appendChild(request.getOwnerDocument().importNode(extraMetadataList, true));
		}
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
				//TODO: DO SOMETHING HERE?
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
		_debug = false;

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

		if (output.equals("xml") || (output.equals("json")) || output.equals("clientside"))
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

			if ((output.equals("xml")) || output.equals("json")) {
			    // in the case of "json", calling method responsible for converting to JSON-string
			    return theXML.getDocumentElement();
			}
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

			if (params.get(GSParams.DEBUG) != null && (((String) params.get(GSParams.DEBUG)).equals("on") || ((String) params.get(GSParams.DEBUG)).equals("1")))
			{
				_debug = true;
			}
		}

		config_params.put("collName", collection);

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

		NodeList pageElems = doc.getElementsByTagName("page");
		if (pageElems.getLength() > 0)
		{
			Element pageElem = (Element) pageElems.item(0);
			String langAtt = pageElem.getAttribute(GSXML.LANG_ATT);

			if (langAtt != null && langAtt.length() > 0)
			{
				config_params.put("lang", langAtt);
			}
		}

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

			if (configStylesheet_doc != null)
			{
				Document format_doc = this.converter.newDOM();
				format_doc.appendChild(format_doc.importNode(format_elem, true));

				if (_debug)
				{
					String siteHome = GSFile.siteHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME));
					GSXSLT.insertDebugElements(format_doc, GSFile.collectionConfigFile(siteHome, collection));
				}

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
					String siteHome = GSFile.siteHome(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME));
					GSXSLT.mergeStylesheetsDebug(style_doc, new_format, true, true, "OTHER1", GSFile.collectionConfigFile(siteHome, collection));
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
			String gsLibFile = this.getGSLibXSLFilename();
			if (new File(gsLibFile).exists())
			{
				libraryXsl = getDoc(gsLibFile);
				String errMsg = ((XMLConverter.ParseErrorHandler) parser.getErrorHandler()).getErrorMessage();
				if (errMsg != null)
				{
					return XMLTransformer.constructErrorXHTMLPage("Error loading xslt file: " + this.getGSLibXSLFilename() + "\n" + errMsg);
				}
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
			if (libraryXsl != null)
			{
				Element libraryXsl_el = libraryXsl.getDocumentElement();
				l.appendChild(skinAndLibraryXsl.importNode(libraryXsl_el, true));
			}
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
				Document inlineTemplateDoc = this.converter.getDOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"" + GSXML.XSL_NAMESPACE + "\" xmlns:java=\"" + GSXML.JAVA_NAMESPACE + "\" xmlns:util=\"" + GSXML.UTIL_NAMESPACE + "\" xmlns:gsf=\"" + GSXML.GSF_NAMESPACE + "\">" + inlineTemplate + "</xsl:stylesheet>", "UTF-8");

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

		if (_debug)
		{
			GSXSLT.inlineImportAndIncludeFilesDebug(skinAndLibraryDoc, null, _debug, this.getGSLibXSLFilename(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces);
		}
		else
		{
			GSXSLT.inlineImportAndIncludeFiles(skinAndLibraryDoc, null, (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces);
		}
		skinAndLibraryDoc = (Document) performFormatPass(collection, skinAndLibraryDoc, doc, new UserContext(request), TEXT_PASS);
		skinAndLibraryDoc = (Document) performFormatPass(collection, skinAndLibraryDoc, doc, new UserContext(request), CONFIG_PASS);

		if (output.equals("xmlfinal"))
		{
			return doc;
		}

		if (output.equals("skinandlibdocfinal"))
		{
			return converter.getDOM(getStringFromDocument(skinAndLibraryDoc));
		}

		Node finalResult = this.transformer.transform(skinAndLibraryDoc, doc, config_params, docWithDoctype);

		if (_debug)
		{
			GSXSLT.fixTables((Document) finalResult);
		}

		return finalResult;

		// The line below will do the transformation like we use to do before having Skin++ implemented,
		// it will not contain any GS-Lib statements expanded, and the result will not contain any doctype.

		//return (Element)this.transformer.transform(style_doc, doc, config_params);  
		//return null; // For now - change later
	}

	protected Node performFormatPass(String collection, Document skinAndLibraryDoc, Document doc, UserContext userContext, int pass)
	{
		String formatFile;
		if (pass == CONFIG_PASS)
		{
			formatFile = "config_format.xsl";
		}
		else
		{
			formatFile = "text_fragment_format.xsl";
		}
		String configStylesheet_file = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, formatFile);
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
		if (name == null)
		{
			// so we can reandomly create any named page
			if (action.equals("p") && !subaction.equals(""))
			{
				// TODO: pages/ won't work for interface other than default!!
				name = "pages/" + subaction + ".xsl";
			}

		}
		Document finalDoc = GSXSLT.mergedXSLTDocumentCascade(name, (String) this.config_params.get(GSConstants.SITE_NAME), collection, (String) this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, _debug);
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
