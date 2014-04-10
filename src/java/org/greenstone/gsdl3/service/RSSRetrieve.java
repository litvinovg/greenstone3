package org.greenstone.gsdl3.service;


// Greenstone classes
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.collection.Collection;
import org.greenstone.util.GlobalProperties;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.ProcessingInstruction;

// General Java classes
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Vector;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.log4j.*;

public class RSSRetrieve extends ServiceRack {

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.RSSRetrieve.class.getName());
    protected static final String RSS_SERVICE = "RSSFeed";

    public boolean configure(Element info, Element extra_info) {
	if (!super.configure(info, extra_info)){
	    return false;
	}
	logger.info("configuring RSSRetrieve...");	
	
	// set up short_service_info_ - for now just has name and type
	Element rss_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
	rss_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	rss_service.setAttribute(GSXML.NAME_ATT, RSS_SERVICE);
	this.short_service_info.appendChild(rss_service);

	return true;
    }

    // this may get called but is not useful in the case of retrieve services
    protected Element getServiceDescription(Document doc, String service_id, String lang, String subset) {

	Element rss_service = doc.createElement(GSXML.SERVICE_ELEM);
	rss_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
	rss_service.setAttribute(GSXML.NAME_ATT, service_id);
	return rss_service;
    }
    
    // Sends off a collection 'describe' message and returns the <collection> element of the response.
    // This contains the collection meta from collectionConfig.xml. Used to construct header of RSS feed
    protected Element getCollMetadata(UserContext userContext) {
      
      Document msg_doc = XMLConverter.newDOM();
	Element mr_request_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);
	String to = this.cluster_name;
	Element meta_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
	mr_request_message.appendChild(meta_request);
	Element meta_response = (Element) this.router.process(mr_request_message);
	meta_response = (Element) GSXML.getChildByTagName(meta_response, GSXML.RESPONSE_ELEM);

	NodeList nl = meta_response.getElementsByTagName(GSXML.COLLECTION_ELEM);
	Element collectionEl = (Element) nl.item(0);
	return collectionEl;
    }


    /**
       Generates the RSS feed XML by creating the header and footer with the contents
       of the the collection's index/rss-items.rdf file embedded in the middle.       
       @return the RSS feed XML.
       @see http://cyber.law.harvard.edu/rss/rss.html
     */
    protected Element processRSSFeed(Element request) {

	// Ask the MessageRouter for this collection's colConfig metadata 
	// from which the RSS header values will be constructed
	UserContext userContext = new UserContext(request);
	Element collMeta = getCollMetadata(userContext);
	
	//logger.error("**** collection metadata:");
	//GSXML.elementToLogAsString(collMeta, true);

	// work out some commonly used variables such as lang and url_prefix
	String lang = request.getAttribute("lang");
	if(lang.equals("")) {
	    lang = "en";
	}

	// url_prefix is of the form http://domain/greenstone3/library/collection/_colname_/
	String url_prefix = GlobalProperties.getFullGSDL3WebAddress()+"/"+this.library_name+"/collection/"+this.cluster_name;


	// generate the header and footer
	Document rssDoc = XMLConverter.newDOM();
	
	Element rssNode = rssDoc.createElement("rss"); // rootnode
	rssNode.setAttribute("version", "2.0");

	String namespace_url = "http://www.w3.org/2000/xmlns/";
	rssNode.setAttributeNS(namespace_url, "xmlns:content", "http://purl.org/rss/1.0/modules/content/");
	rssNode.setAttributeNS(namespace_url, "xmlns:taxo", "http://purl.org/rss/1.0/modules/taxonomy/");
	rssNode.setAttributeNS(namespace_url, "xmlns:dc", "http://purl.org/dc/elements/1.1/");
	rssNode.setAttributeNS(namespace_url, "xmlns:syn", "http://purl.org/rss/1.0/modules/syndication/");
	rssNode.setAttributeNS(namespace_url, "xmlns:admin", "http://webns.net/mvcb/");
	rssDoc.appendChild(rssNode);

	// Setting the preproccessing header line (Utf-8) will be done in web/interfaces' rss.xsl
	//ProcessingInstruction procInstruction = doc.createProcessingInstruction("xml","version=\"1.0\"");
	//rssDoc.appendChild(procInstruction);
	
	Element channelNode = rssDoc.createElement("channel");
	rssNode.appendChild(channelNode);
	
	Element childNode = rssDoc.createElement("title");
	GSXML.setNodeText(childNode, this.cluster_name); //_collectionname_
	channelNode.appendChild(childNode);

	// _httppageabout_: of form http://domain/greenstone3/library/collection/_colname_/page/about
	childNode = rssDoc.createElement("link");	
	GSXML.setNodeText(childNode, url_prefix+"/page/about"); // _httppageabout_
	channelNode.appendChild(childNode);

	// get the description string for the requested language, else fallback on en description if present
	childNode = rssDoc.createElement("description");
	NodeList descriptions = GSXML.getNamedElements(collMeta, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, GSXML.DISPLAY_TEXT_DESCRIPTION);
	//Element descriptEl = GSXML.getNamedElement(collMeta, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, GSXML.DISPLAY_TEXT_DESCRIPTION);
	Element descriptEl = null;
	if(descriptions != null) {
	    for (int i = 0; i < descriptions.getLength(); i++) {
		Element e = (Element) descriptions.item(i);
		if(e.getAttribute("lang").equals(lang)) {
		    descriptEl = e;
		    break; // found the description for the requested language, finish loop
		} else if(e.getAttribute("lang").equals("en")) {
		    descriptEl = e; // at least found english fall-back description, continue loop
		}
	    }
	}
	String description = (descriptEl == null) ? "none" : GSXML.getNodeText(descriptEl);
	GSXML.setNodeText(childNode, description); //_collectionextra_
	channelNode.appendChild(childNode);

	childNode = rssDoc.createElement("language");
	GSXML.setNodeText(childNode, lang); //_cgiargl_
	channelNode.appendChild(childNode);


	// RSS specification: http://cyber.law.harvard.edu/rss/rss.html
	// pubDate is date of first publication of the item. Use collection.getEarliestDatestamp()
	// lastBuildDate is the date the item was last modified. Use collection.getLastmodified()

	//HashMap<String, ModuleInterface> module_map = this.router.getModuleMap();
	//Collection coll = (Collection)module_map.get(this.cluster_name);
	Collection coll = (Collection)serviceCluster;
	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	childNode = rssDoc.createElement("pubDate");
	Date date = new Date(coll.getEarliestDatestamp()); // "Thu, 23 Aug 1999 07:00:00 GMT"
	GSXML.setNodeText(childNode, dateFormat.format(date));
	channelNode.appendChild(childNode);

	childNode = rssDoc.createElement("lastBuildDate");
	date = new Date(coll.getLastmodified()); // "Thu, 23 Aug 1999 16:20:26 GMT"	
	GSXML.setNodeText(childNode, dateFormat.format(date));
	channelNode.appendChild(childNode);

	childNode = rssDoc.createElement("managingEditor");
	Element e = GSXML.getNamedElement(collMeta, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, "creator");
	String value = (e == null) ? "unknown" : GSXML.getNodeText(e);
	GSXML.setNodeText(childNode, value); //_creator_
	channelNode.appendChild(childNode);

	childNode = rssDoc.createElement("webMaster");
	e = GSXML.getNamedElement(collMeta, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, "maintainer");
	value = (e == null) ? "unknown" : GSXML.getNodeText(e);
	GSXML.setNodeText(childNode, value); //_maintainer_
	channelNode.appendChild(childNode);


	// <image> child of <channel> has title, url, link and description children
	Element collIcon = GSXML.getNamedElement(collMeta, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, "icon");
	if(collIcon != null) { // since there is a collection image, create an imageNode
	    
	    Node imageNode = rssDoc.createElement("image");
	    channelNode.appendChild(imageNode);	

	    childNode = rssDoc.createElement("title");
	    GSXML.setNodeText(childNode, this.cluster_name); //_collectionname_
	    imageNode.appendChild(childNode);

	    // need full URL for collection icon. Its name is in <displayItem name="icon_name.ext"/>
	    // URL is of the form domain/servlet/sites/localsite/collect/lucene-jdbm-demo/images/icon_name.ext
	    childNode = rssDoc.createElement("url");	    
	    String domain = GlobalProperties.getFullGSDL3WebAddress(); // remove servlet as it's included in site_http_address
	    domain = domain.substring(0, domain.lastIndexOf("/"));
	    String image_url = GSXML.getNodeText(collIcon); // name of image file
	    image_url = domain+"/"+this.site_http_address+"/"+"/collect/"+this.cluster_name+"/images/"+image_url;
	    GSXML.setNodeText(childNode, image_url); // _iconcollection_
	    imageNode.appendChild(childNode);

	    childNode = rssDoc.createElement("link");
	    GSXML.setNodeText(childNode, url_prefix+"/page/about"); // _httppageabout_
	    imageNode.appendChild(childNode);

	    childNode = rssDoc.createElement("description");
	    GSXML.setNodeText(childNode, description); //_collectionextra_
	    imageNode.appendChild(childNode);
	}


	// now add the contents of rss-items.rdf as a child of channel, 
	// passing in url_prefix for url resolution
	Element rss_raw_data = loadDocument("rss-items.rdf", url_prefix);
	if(rss_raw_data != null) {
	    NodeList rss_items = rss_raw_data.getElementsByTagName("item");
	    for(int i = 0; i < rss_items.getLength(); i++) {
		channelNode.appendChild(rssDoc.importNode(rss_items.item(i), true));
	    }
	}

	// generate the GS3 response message containing the RSS xml
	Element result = rssDoc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, RSS_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
	result.appendChild(rssNode); // body of <response> is simply the <rss> root element of the RSS feed
	return result;
	
    }

    // load contents of rss-items.rdf file into an XML doc in memory, after doing the url_prefix replacements
    protected Element loadDocument(String doc_name, String url_prefix) {
	String document_encoding = "UTF-8";

	// try to find the document
	File doc_file = new File(GSFile.collectionIndexDir(this.site_home, this.cluster_name)+File.separator+doc_name);
	    
	if (!doc_file.exists()) {
	    logger.info("couldn't find file in coll "+this.cluster_name +", file "+doc_name);
	    return null;
	}

	// the rss-items.rdf file has no root element, only multiple <item> elements (and subelements)
	// Without a root element, it can't be read into a DOM object. So we read it into a regular String,
	// then bookend the contents with temporary <rssrawdata></rssrawdata> elements to provide a root 
	// element and can read in that String as a DOM object.

	StringBuffer contents = new StringBuffer("<rssrawdata>\n");
	try {
	    BufferedReader in = new BufferedReader(new FileReader(doc_file));	    
	    String line = null;
	    while((line = in.readLine()) != null) {
		//line = line.replace("_httpcollection_", "/"+this.cluster_name);
		line = line.replace("_httpdomain__httpcollection_", url_prefix);
		contents.append(line);
	    }
	    contents.append("</rssrawdata>");
	    in.close(); // close the fileread handle

	} catch (Exception e) {
	    e.printStackTrace();
	    contents.append("couldn't read ");
	    contents.append(doc_file);
	    contents.append("\n</rssrawdata>");
	}


	Document the_doc = null;
	try {
	    the_doc = this.converter.getDOM(contents.toString()); // String input converted to DOM
	} catch (Exception e) {
	    logger.error("couldn't create a DOM from file "+doc_file.getPath());
	    return null;
	}
	
	return the_doc.getDocumentElement();

    }

    
}

