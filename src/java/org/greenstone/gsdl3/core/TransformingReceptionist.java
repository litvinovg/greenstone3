package org.greenstone.gsdl3.core;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.action.*;
// XML classes
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

// other java classes
import java.io.File;
import java.io.StringWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Enumeration;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.apache.log4j.*;
import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.DOMParser;

/** A receptionist that uses xslt to transform the page_data before returning it. . Receives requests consisting
 * of an xml representation of cgi args, and returns the page of data - in 
 * html by default. The requests are processed by the appropriate action class
 *
 * @see Action
 */
public class TransformingReceptionist extends Receptionist{
    
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.TransformingReceptionist.class.getName());
	
  /** The preprocess.xsl file is in a fixed location */
  static final String preprocess_xsl_filename = GlobalProperties.getGSDL3Home() + File.separatorChar 
    + "ui" + File.separatorChar + "xslt" + File.separatorChar + "preProcess.xsl";	
		
  /** the list of xslt to use for actions */
  protected HashMap xslt_map = null;
    
  /** a transformer class to transform xml using xslt */
  protected XMLTransformer transformer=null;

  protected TransformerFactory transformerFactory=null;
  protected DOMParser parser = null;
  public TransformingReceptionist() {
    super();
    this.xslt_map = new HashMap();
    this.transformer = new XMLTransformer();
    try {
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
    catch (Exception e) {
      e.printStackTrace();
    }

  }
    
  /** configures the receptionist - overwrite this to set up the xslt map*/
  public boolean configure() {
	
    if (this.config_params==null) {
      logger.error(" config variables must be set before calling configure");
      return false;
    }
    if (this.mr==null) {	   
      logger.error(" message router must be set  before calling configure");
      return false;
    }
		
    // find the config file containing a list of actions
    File interface_config_file = new File(GSFile.interfaceConfigFile(GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), (String)this.config_params.get(GSConstants.INTERFACE_NAME))));
    if (!interface_config_file.exists()) {
      logger.error(" interface config file: "+interface_config_file.getPath()+" not found!");
      return false;
    }
    Document config_doc = this.converter.getDOM(interface_config_file, "utf-8");
    if (config_doc == null) {
      logger.error(" could not parse interface config file: "+interface_config_file.getPath());
      return false;
    }
    Element config_elem = config_doc.getDocumentElement();
    String base_interface = config_elem.getAttribute("baseInterface");
    setUpBaseInterface(base_interface);
    setUpInterfaceOptions(config_elem);

    Element action_list = (Element)GSXML.getChildByTagName(config_elem, GSXML.ACTION_ELEM+GSXML.LIST_MODIFIER);
    NodeList actions = action_list.getElementsByTagName(GSXML.ACTION_ELEM);

    for (int i=0; i<actions.getLength(); i++) {
      Element action = (Element) actions.item(i);
      String class_name = action.getAttribute("class");
      String action_name = action.getAttribute("name");
      Action ac = null;
      try {
	ac = (Action)Class.forName("org.greenstone.gsdl3.action."+class_name).newInstance();
      } catch (Exception e) {
	logger.error(" couldn't load in action "+class_name);
	e.printStackTrace();
	continue;
      }
      ac.setConfigParams(this.config_params);
      ac.setMessageRouter(this.mr);
      ac.configure();
      ac.getActionParameters(this.params);
      this.action_map.put(action_name, ac);

      // now do the xslt map
      String xslt = action.getAttribute("xslt");
      if (!xslt.equals("")) {
	this.xslt_map.put(action_name, xslt);
      }
      NodeList subactions = action.getElementsByTagName(GSXML.SUBACTION_ELEM);
      for (int j=0; j<subactions.getLength(); j++) {
	Element subaction = (Element)subactions.item(j);
	String subname = subaction.getAttribute(GSXML.NAME_ATT);
	String subxslt = subaction.getAttribute("xslt");
				
	String map_key = action_name+":"+subname;
	logger.debug("adding in to xslt map, "+map_key+"->"+subxslt);
	this.xslt_map.put(map_key, subxslt);
      }
    }
    Element lang_list = (Element)GSXML.getChildByTagName(config_elem, "languageList");
    if (lang_list == null) {
      logger.error(" didn't find a language list in the config file!!");
    } else {
      this.language_list = (Element) this.doc.importNode(lang_list, true);
    }

    return true;
  }


  protected Node postProcessPage(Element page) {
    // might need to add some data to the page
    addExtraInfo(page);
    // transform the page using xslt
    Node transformed_page = transformPage(page);
	
	// if the user has specified they want only a part of the full page then subdivide it
	boolean subdivide = false;
	String excerptID = null;
	String excerptTag = null;
	Element request = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
    Element cgi_param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
    if (cgi_param_list != null) {
		  HashMap params = GSXML.extractParams(cgi_param_list, false);
		  if((excerptID = (String)params.get(GSParams.EXCERPT_ID)) != null)
		  {
			subdivide = true;
		  }
		  if((excerptTag = (String)params.get(GSParams.EXCERPT_TAG)) != null)
		  {
			subdivide = true;
		  }
	}

	if(subdivide)
	{
		Node subdivided_page = subdivide(transformed_page, excerptID, excerptTag);
		if(subdivided_page != null)
		{
			return subdivided_page;
		}
	}
		
    return transformed_page; 
  }
  
  protected Node subdivide(Node transformed_page, String excerptID, String excerptTag)
  {
	if(excerptID != null)
	{
		Node selectedElement = getNodeByIdRecursive(transformed_page, excerptID);
        modifyNodesByTagRecursive(selectedElement, "a");
		return selectedElement;
	}
	else if(excerptTag != null)
	{
        // define a list
        
		Node selectedElement = modifyNodesByTagRecursive(transformed_page, excerptTag);
		return selectedElement;
	}
	return transformed_page;
  }
  
  protected Node getNodeByIdRecursive(Node parent, String id)
  {
	if(parent.hasAttributes() && ((Element)parent).getAttribute("id").equals(id))
	{
		return parent;
	}
	
	NodeList children = parent.getChildNodes();
	for(int i = 0; i < children.getLength(); i++)
	{
		Node result = null;
		if((result = getNodeByIdRecursive(children.item(i), id)) != null)
		{
			return result;
		}
	}
	return null;
  }
  
  protected Node getNodeByTagRecursive(Node parent, String tag)
  {
	if(parent.getNodeType() == Node.ELEMENT_NODE && ((Element)parent).getTagName().equals(tag))
	{
		return parent;
	}
	
	NodeList children = parent.getChildNodes();
	for(int i = 0; i < children.getLength(); i++)
	{
		Node result = null;
		if((result = getNodeByTagRecursive(children.item(i), tag)) != null)
		{
			return result;
		}
	}
	return null;
  }
 
  protected Node modifyNodesByTagRecursive(Node parent, String tag)
  {
    if(parent.getNodeType() == Node.ELEMENT_NODE && ((Element)parent).getTagName().equals(tag))
    {
        return parent;
    }
    
    NodeList children = parent.getChildNodes();
    for(int i = 0; i < children.getLength(); i++)
    {
        Node result = null;
        if((result = modifyNodesByTagRecursive(children.item(i), tag)) != null)
        {
            //return result;
            //logger.error("Modify node value = "+result.getNodeValue()); //NamedItem("href"););
            logger.error("BEFORE Modify node attribute = "+result.getAttributes().getNamedItem("href").getNodeValue());
            String url = result.getAttributes().getNamedItem("href").getNodeValue();
            url = url + "&excerptid=gs_content";
            result.getAttributes().getNamedItem("href").setNodeValue(url);
            logger.error("AFTER Modify node attribute = "+result.getAttributes().getNamedItem("href").getNodeValue());
            
        }
    }
    return null;
  }

  /** overwrite this to add any extra info that might be needed in the page before transformation */
  protected void addExtraInfo(Element page) {}

  /** transform the page using xslt
   * we need to get any format element out of the page and add it to the xslt
   * before transforming */
  protected Node transformPage(Element page) {

    logger.debug("page before transforming:");
    logger.debug(this.converter.getPrettyString(page));

    Element request = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
    String action = request.getAttribute(GSXML.ACTION_ATT);
    String subaction = request.getAttribute(GSXML.SUBACTION_ATT);
		
    String output = request.getAttribute(GSXML.OUTPUT_ATT);
    // we should choose how to transform the data based on output, eg diff
    // choice for html, and wml??
    // for now, if output=xml, we don't transform the page, we just return 
    // the page xml
    if (output.equals("xml")) {
      return page;
    }
		

    Element cgi_param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
    String collection = "";
    if (cgi_param_list != null) {
      HashMap params = GSXML.extractParams(cgi_param_list, false);
      collection = (String)params.get(GSParams.COLLECTION);
      if (collection == null) collection = "";
    }
		
    String xslt_file = getXSLTFileName(action, subaction, collection);
    if (xslt_file==null) {
      // returning file not found error page to indicate which file is missing
      return fileNotFoundErrorPage(xslt_file);
    }
		
    Document style_doc = this.converter.getDOM(new File(xslt_file), "UTF-8");
    String errorPage = this.converter.getParseErrorMessage();
    if(errorPage != null) {
      return XMLTransformer.constructErrorXHTMLPage(
						    "Cannot parse the xslt file: " + xslt_file + "\n" + errorPage);
    }
    if (style_doc == null) {
      logger.error(" cant parse the xslt file needed, so returning the original page!");
      return page;
    }
		
		
    // put the page into a document - this is necessary for xslt to get
    // the paths right if you have paths relative to the document root
    // eg /page.
    Document doc = this.converter.newDOM();
    doc.appendChild(doc.importNode(page, true));
    Element page_response = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_RESPONSE_ELEM);
    Element format_elem = (Element)GSXML.getChildByTagName(page_response, GSXML.FORMAT_ELEM);
    if (output.equals("formatelem")) {
      return format_elem;
    }
    if (format_elem != null) {
      //page_response.removeChild(format_elem);
      logger.debug("format elem="+this.converter.getPrettyString(format_elem));
      // need to transform the format info
      String configStylesheet_file = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String)this.config_params.get(GSConstants.SITE_NAME), collection, (String)this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces,   "config_format.xsl");
      Document configStylesheet_doc = this.converter.getDOM(new File(configStylesheet_file));
      if (configStylesheet_doc != null) {
	Document format_doc = this.converter.newDOM();
	format_doc.appendChild(format_doc.importNode(format_elem, true));
	Node result = this.transformer.transform(configStylesheet_doc, format_doc);
				
	// Since we started creating documents with DocTypes, we can end up with 
	// Document objects here. But we will be working with an Element instead, 
	// so we grab the DocumentElement() of the Document object in such a case.
	Element new_format;
	if(result.getNodeType() == Node.DOCUMENT_NODE) {
	  new_format = ((Document)result).getDocumentElement();
	} else {
	  new_format = (Element)result;
	}
	logger.debug("new format elem="+this.converter.getPrettyString(new_format));
	if (output.equals("newformat")) {
	  return new_format;
	}
				
	// add extracted GSF statements in to the main stylesheet
	GSXSLT.mergeStylesheets(style_doc, new_format);
	//System.out.println("added extracted GSF statements into the main stylesheet") ;
				
	// add extracted GSF statements in to the debug test stylesheet
	//GSXSLT.mergeStylesheets(oldStyle_doc, new_format);
      } else {
	logger.error(" couldn't parse the config_format stylesheet, adding the format info as is");
	GSXSLT.mergeStylesheets(style_doc, format_elem);
	//GSXSLT.mergeStylesheets(oldStyle_doc, format_elem);
      }
      logger.debug("the converted stylesheet is:");
      logger.debug(this.converter.getPrettyString(style_doc.getDocumentElement()));
    }
		
    //for debug purposes only
    Document oldStyle_doc = style_doc;


    Document preprocessingXsl  ;
    try {
      preprocessingXsl = getPreprocessDoc();
      String errMsg = ((XMLConverter.ParseErrorHandler)parser.getErrorHandler()).getErrorMessage();
      if(errMsg != null) {
	return XMLTransformer.constructErrorXHTMLPage("error loading preprocess xslt file: " 
						      + preprocess_xsl_filename + "\n" + errMsg);
      } 
    } catch (java.io.FileNotFoundException e) {
      return fileNotFoundErrorPage(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace() ;
      System.out.println("error loading preprocess xslt") ;
      return XMLTransformer.constructErrorXHTMLPage("error loading preprocess xslt\n" + e.getMessage());
    }

    Document libraryXsl = null;
    try {
      libraryXsl = getLibraryDoc() ;
      String errMsg = ((XMLConverter.ParseErrorHandler)parser.getErrorHandler()).getErrorMessage();
      if(errMsg != null) {
	return XMLTransformer.constructErrorXHTMLPage("Error loading xslt file: " 
						      + this.getLibraryXSLFilename() + "\n" + errMsg);
      } 
    } catch (java.io.FileNotFoundException e) {
      return fileNotFoundErrorPage(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace() ;
      System.out.println("error loading library xslt") ;
      return XMLTransformer.constructErrorXHTMLPage("error loading library xslt\n" + e.getMessage()) ;
    }
		
    //   Combine the skin file and library variables/templates into one document. 
    //   Please note: We dont just use xsl:import because the preprocessing stage  
    //   needs to know what's available in the library.

    Document skinAndLibraryXsl = null ;
    Document skinAndLibraryDoc = converter.newDOM();
    try {
			
      skinAndLibraryXsl = converter.newDOM();
      Element root = skinAndLibraryXsl.createElement("skinAndLibraryXsl") ;
      skinAndLibraryXsl.appendChild(root) ;
			
      Element s = skinAndLibraryXsl.createElement("skinXsl") ;
      s.appendChild(skinAndLibraryXsl.importNode(style_doc.getDocumentElement(), true)) ;
      root.appendChild(s) ;
			
      Element l = skinAndLibraryXsl.createElement("libraryXsl") ;
      Element libraryXsl_el = libraryXsl.getDocumentElement();
      l.appendChild(skinAndLibraryXsl.importNode(libraryXsl_el, true)) ;
      root.appendChild(l) ;
      //System.out.println("Skin and Library XSL are now together") ;
			
			
      //System.out.println("Pre-processing the skin file...") ;
			
      //pre-process the skin style sheet
      //In other words, apply the preProcess.xsl to 'skinAndLibraryXsl' in order to
      //expand all GS-Lib statements into complete XSL statements and also to create
      //a valid  xsl style sheet document.

      Transformer preProcessor = transformerFactory.newTransformer(new DOMSource(preprocessingXsl));
      preProcessor.setErrorListener(new XMLTransformer.TransformErrorListener());
      DOMResult result = new DOMResult();
      result.setNode(skinAndLibraryDoc);
      preProcessor.transform(new DOMSource(skinAndLibraryXsl), result);
      //System.out.println("GS-Lib statements are now expanded") ;		
			
    }	
    catch (TransformerException e) {
      e.printStackTrace() ;
      System.out.println("TransformerException while preprocessing the skin xslt") ;
      return XMLTransformer.constructErrorXHTMLPage(e.getMessage()) ;
    }
    catch (Exception e) {
      e.printStackTrace() ;
      System.out.println("Error while preprocessing the skin xslt") ;
      return XMLTransformer.constructErrorXHTMLPage(e.getMessage()) ;
    }
		
    //The following code is to be uncommented if we need to append the extracted GSF statements
    //after having extracted the GSLib elements. In case of a problem during postprocessing.
    /*
    // put the page into a document - this is necessary for xslt to get
    // the paths right if you have paths relative to the document root
    // eg /page.
    Document doc = this.converter.newDOM();
    doc.appendChild(doc.importNode(page, true));
    Element page_response = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_RESPONSE_ELEM);
    Element format_elem = (Element)GSXML.getChildByTagName(page_response, GSXML.FORMAT_ELEM);
    if (output.equals("formatelem")) {
    return format_elem;
    }
    if (format_elem != null) {
    //page_response.removeChild(format_elem);
    logger.debug("format elem="+this.converter.getPrettyString(format_elem));
    // need to transform the format info
    String configStylesheet_file = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String)this.config_params.get(GSConstants.SITE_NAME), collection, (String)this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces,   "config_format.xsl");
    Document configStylesheet_doc = this.converter.getDOM(new File(configStylesheet_file));
    if (configStylesheet_doc != null) {
    Document format_doc = this.converter.newDOM();
    format_doc.appendChild(format_doc.importNode(format_elem, true));
    Node result = this.transformer.transform(configStylesheet_doc, format_doc);
				
    // Since we started creating documents with DocTypes, we can end up with 
    // Document objects here. But we will be working with an Element instead, 
    // so we grab the DocumentElement() of the Document object in such a case.
    Element new_format;
    if(result.getNodeType() == Node.DOCUMENT_NODE) {
    new_format = ((Document)result).getDocumentElement();
    } else {
    new_format = (Element)result;
    }
    logger.debug("new format elem="+this.converter.getPrettyString(new_format));
    if (output.equals("newformat")) {
    return new_format;
    }
				
    // add extracted GSF statements in to the main stylesheet
    GSXSLT.mergeStylesheets(skinAndLibraryDoc, new_format);
    //System.out.println("added extracted GSF statements into the main stylesheet") ;
				
    // add extracted GSF statements in to the debug test stylesheet
    //GSXSLT.mergeStylesheets(oldStyle_doc, new_format);
    } else {
    logger.error(" couldn't parse the config_format stylesheet, adding the format info as is");
    GSXSLT.mergeStylesheets(skinAndLibraryDoc, format_elem);
    //	GSXSLT.mergeStylesheets(oldStyle_doc, format_elem);
    }
    logger.debug("the converted stylesheet is:");
    logger.debug(this.converter.getPrettyString(skinAndLibraryDoc.getDocumentElement()));
    }
    */
	
    // there is a thing called a URIResolver which you can set for a
    // transformer or transformer factory. may be able to use this
    // instead of this absoluteIncludepaths hack

    GSXSLT.absoluteIncludePaths(skinAndLibraryDoc, GlobalProperties.getGSDL3Home(), 
				(String)this.config_params.get(GSConstants.SITE_NAME), 
				collection, (String)this.config_params.get(GSConstants.INTERFACE_NAME), 
				base_interfaces);


    //Same but for the debug version when we want the do the transformation like we use to do
    //without any gslib elements.
    GSXSLT.absoluteIncludePaths(oldStyle_doc, GlobalProperties.getGSDL3Home(), 
				(String)this.config_params.get(GSConstants.SITE_NAME), 
				collection, (String)this.config_params.get(GSConstants.INTERFACE_NAME), 
				base_interfaces);
				
    //Send different stages of the skin xslt to the browser for debug purposes only
    //using &o=skindoc or &o=skinandlib etc...
    if (output.equals("skindoc")) {
      return converter.getDOM(getStringFromDocument(style_doc));
    }
    if (output.equals("skinandlib")) {
      return converter.getDOM(getStringFromDocument(skinAndLibraryXsl));
    }
    if (output.equals("skinandlibdoc")) {
      return converter.getDOM(getStringFromDocument(skinAndLibraryDoc));
    }
    if (output.equals("oldskindoc")) {
      return converter.getDOM(getStringFromDocument(oldStyle_doc));
    }

    // DocType defaults in case the skin doesn't have an "xsl:output" element
    String qualifiedName = "html";
    String publicID = "-//W3C//DTD HTML 4.01 Transitional//EN";
    String systemID = "http://www.w3.org/TR/html4/loose.dtd";
		
    // Try to get the system and public ID from the current skin xsl document
    // otherwise keep the default values.
    Element root = skinAndLibraryDoc.getDocumentElement();
    NodeList nodes = root.getElementsByTagName("xsl:output");
    // If there is at least one "xsl:output" command in the final xsl then...
    if(nodes.getLength() != 0) {
      // There should be only one element called xsl:output, 
      // but if this is not the case get the last one
      Element xsl_output = (Element)nodes.item(nodes.getLength()-1);
      if (xsl_output != null) {
	// Qualified name will always be html even for xhtml pages
	//String attrValue = xsl_output.getAttribute("method");
	//qualifiedName = attrValue.equals("") ? qualifiedName : attrValue;
				
	String attrValue = xsl_output.getAttribute("doctype-system");
	systemID = attrValue.equals("") ? systemID : attrValue;
				
	attrValue = xsl_output.getAttribute("doctype-public");
	publicID = attrValue.equals("") ? publicID : attrValue;
      }
    }
		
    // We need to create an empty document with a predefined DocType,
    // that will then be used for the transformation by the DOMResult
    Document docWithDoctype = converter.newDOM(qualifiedName, publicID, systemID);
		
    //System.out.println(converter.getPrettyString(docWithDoctype));
    //System.out.println("Doctype vals: " + qualifiedName + " " + publicID + " " + systemID) ;
		
		
    //System.out.println("Generate final HTML from current skin") ;
    //Transformation of the XML message from the receptionist to HTML with doctype
    return this.transformer.transform(skinAndLibraryDoc, doc, config_params, docWithDoctype);
		
		
    // The line below will do the transformation like we use to do before having Skin++ implemented,
    // it will not contain any GS-Lib statements expanded, and the result will not contain any doctype.

    //return (Element)this.transformer.transform(style_doc, doc, config_params);  

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
	System.out.println("Change the & to &Amp; for proper debug dispay") ;
	content = content.replaceAll("&", "&amp;");
	writer.flush();
      }
    catch(TransformerException ex)
      {
	ex.printStackTrace();
	return null;
      }
    return content;
  } 


  protected Document getPreprocessDoc() throws Exception {

    File xslt_file = new File(preprocess_xsl_filename) ;

    FileReader reader = new FileReader(xslt_file);
    InputSource xml_source = new InputSource(reader);
    this.parser.parse(xml_source);
    Document doc = this.parser.getDocument();

    return doc ;		
  }

  protected Document getLibraryDoc() throws Exception {
    Document doc = null;
    File xslt_file = new File(this.getLibraryXSLFilename()) ;

    FileReader reader = new FileReader(xslt_file);
    InputSource xml_source = new InputSource(reader);
    this.parser.parse(xml_source);
		
    doc = this.parser.getDocument();
    return doc ;		
  }   

  protected String getXSLTFileName(String action, String subaction,
				   String collection) {

    String name = null;
    if (!subaction.equals("")) {
      String key = action+":"+subaction;
      name = (String) this.xslt_map.get(key);
    }
    // try the action by itself
    if (name==null) {
      name = (String) this.xslt_map.get(action);
    }
    // now find the absolute path
    String stylesheet = GSFile.stylesheetFile(GlobalProperties.getGSDL3Home(), (String)this.config_params.get(GSConstants.SITE_NAME), collection, (String)this.config_params.get(GSConstants.INTERFACE_NAME), base_interfaces, name);
    if (stylesheet==null) {
      logger.info(" cant find stylesheet for "+name);
    }
    return stylesheet;
  }

  // returns the library.xsl path of the library file that is applicable for the current interface
  protected String getLibraryXSLFilename() {
    return GSFile.xmlTransformDir(GSFile.interfaceHome(
						       GlobalProperties.getGSDL3Home(), (String)this.config_params.get(GSConstants.INTERFACE_NAME)))
      + File.separatorChar + "library.xsl";
  }
	
  // Call this when a FileNotFoundException could be thrown when loading an xsl (xml) file.
  // Returns an error xhtml page indicating which xsl (or other xml) file is missing.
  protected Document fileNotFoundErrorPage(String filenameMessage) {
    String errorMessage = "ERROR missing file: " + filenameMessage;
    Element errPage = XMLTransformer.constructErrorXHTMLPage(errorMessage);
    logger.error(errorMessage);
    return errPage.getOwnerDocument();
  }
}
