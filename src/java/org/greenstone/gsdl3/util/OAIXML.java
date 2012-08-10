/*
 *    OAIXML.java
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

import org.greenstone.util.GlobalProperties;

import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

// JAXP
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// import file Logger.java
import org.apache.log4j.*;

/** these constants are used for the OAI service */
public class OAIXML {
  
    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSXML.class.getName());

    // the leading keyword of oai protocol
    public static final String VERB = "verb";

    // six valid oai verbs
    public static final String GET_RECORD = "GetRecord";
    public static final String LIST_RECORDS = "ListRecords";
    public static final String LIST_IDENTIFIERS = "ListIdentifiers";
    public static final String LIST_SETS = "ListSets";
    public static final String LIST_METADATA_FORMATS = "ListMetadataFormats";
    public static final String IDENTIFY = "Identify";

    // other valid oai parameters
    public static final String OAI_METADATAFORMAT = "OAIMetadataFormat";
    public static final String METADATA_NAMESPACE = "metadataNamespace";
    public static final String OAI_DC = "oai_dc";
    public static final String DC = "dc";
    public static final String METADATA_PREFIX = "metadataPrefix";
    public static final String FROM = "from";
    public static final String UNTIL = "until";
    public static final String SET = "set";
    public static final String RESUMPTION_TOKEN = "resumptionToken";
    public static final String RESUMPTION_TOKEN_EXPIRATION = "resumptionTokenExpiration";
    public static final String IDENTIFIER = "identifier";

    public static final String USE_STYLESHEET = "useOAIStylesheet";
    public static final String STYLESHEET = "OAIStylesheet";
    // words used to compose oai responses
    public static final String ADMIN_EMAIL = "adminEmail";
    public static final String BAD_ARGUMENT = "badArgument";
    public static final String BAD_RESUMPTION_TOKEN = "badResumptionToken";
    public static final String BAD_VERB = "badVerb";
    public static final String BASE_URL = "baseURL";
    public static final String CANNOT_DISSEMINATE_FORMAT = "cannotDisseminateFormat";
    public static final String CODE = "code";
    public static final String COLLECTION = "collection";
    public static final String COLLECTION_LIST = "collectionList";
    public static final String COMPLETE_LIST_SIZE = "completeListSize";
    public static final String COMPRESSION = "compression";
    public static final String CURSOR = "cursor";
    public static final String DATESTAMP = "datestamp";
    public static final String DELETED_RECORD = "deletedRecord";
    public static final String DESCRIPTION = "description";
    public static final String EARLIEST_DATESTAMP = "earliestDatestamp";
    public static final String ERROR = "error";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String GRANULARITY = "granularity";
    public static final String GS3OAI = "GS3OAI";
	public static final String GS_OAI_RESOURCE_URL = "gs.OAIResourceURL";
    public static final String HAS_OAI = "hasOAI";
    public static final String HEADER = "header";
    public static final String ILLEGAL_OAI_VERB = "Illegal OAI verb";
    public static final String INDEX_STEM = "indexStem";
    public static final String LASTMODIFIED = "lastmodified";
    public static final String MAPPING = "mapping";
    public static final String MAPPING_LIST = "mappingList";
    public static final String MESSAGE = "message";
    public static final String METADATA = "metadata";
    public static final String METADATA_FORMAT = "metadataFormat";
    public static final String NAME = "name";
    public static final String NO_RECORDS_MATCH = "noRecordsMatch";
    public static final String OAI = "OAI";
    public static final String OAI_DASH_PMH = "OAI-PMH";
    public static final String OAI_LASTMODIFIED = "oailastmodified";
    public static final String OAIPMH = "OAIPMH";
    public static final String OAI_RESUMPTION_TOKENS = "OAIResumptionTokens";
    public static final String OAI_SERVICE = "oaiService";
    public static final String OAI_SET_LIST = "oaiSetList";
    public static final String OAI_SERVICE_UNAVAILABLE = "OAI service unavailable";
    public static final String OID = "OID";
    public static final String PARAM = "param";
    public static final String PARAM_LIST = "paramList";
    public static final String PROTOCOL_VERSION = "protocolVersion";
    public static final String RECORD = "record";
    public static final String REQUEST = "request";
    public static final String REPOSITORY_NAME = "repositoryName";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_DATE = "responseDate";
    public static final String RESUME_AFTER = "resumeAfter";
    public static final String SCHEMA = "schema";
    public static final String SERVICE = "service";
    public static final String SERVICE_UNAVAILABLE = "service unavailable";
    public static final String SET_SPEC = "setSpec";
    public static final String SET_NAME = "setName";
    public static final String SET_DESCRIPTION = "setDescription";
    public static final String SITE = "site";
    public static final String TO = "to";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    
    //Two error and exception conditions for the verb 'ListMetadataFormats'
    public static final String ID_DOES_NOT_EXIST = "idDoesNotExist";
    public static final String NO_METADATA_FORMATS = "noMetadataFormats";
    
    // The node id in the collection database, which contains all the OIDs in the database
    public static final String BROWSELIST = "browselist";
        
    //system-dependent file separator, maybe '/' or '\'
    public static final String FILE_SEPARATOR = File.separator;
    public static final String OAI_VERSION1 = "1.0";
    public static final String OAI_VERSION2 = "2.0";
    /*************************above are final values****************************/
    
    public static Element resumption_token_elem = null;
    //used when saving the token file
    public static File resumption_token_file = null;
    //public static ArrayList token_list = new ArrayList();
    
    //initialized in getOAIConfigXML()
    public static Element oai_config_elem = null;
    
    //stores the date format "yyyy-MM-ddTHH:mm:ssZ"
    public static String granularity = "";

    // http://www.openarchives.org/OAI/openarchivesprotocol.html#DatestampsRequests 
    // specifies that all repositories must support YYYY-MM-DD (yyyy-MM-dd in Java)
    // this would be in addition to the other (optional) granularity of above that 
    // a repository may additionally choose to support.
    public static final String default_granularity = "yyyy-MM-dd";

    //this value is overriden in getOAIConfigXML()
    public static long token_expiration = 7200;
    
    /** which version of oai that this oaiserver supports; default is 2.0 
     *  initialized in getOAIConfigXML()
     */
    public static String oai_version = "2.0";
	public static String baseURL = "";
    
    /**response owner document */
    public static Document response_doc = new XMLConverter().newDOM(); 
    
    public static String[] special_char = {"/", "?", "#", "=", "&", ":", ";", " ", "%", "+"};
    public static String[] escape_sequence = {"%2F", "%3F", "%23", "%3D", "%26", "%3A", "%3B", "%20", "%25", "%2B"};
//    /** key=special character; value=escaped sequence */
//    public static HashMap encode_map = new HashMap();
//    /** key=escaped sequence; value=special character */
//    public static HashMap decode_map = new HashMap();

    public static void init() {
      resumption_token_elem = getOAIResumptionTokenXML();
    }
    public static String getOAIVersion() {
      return oai_version;
    }
	
	public static String getBaseURL() {
		return baseURL;
	}
	
    public static Element createElement(String tag_name) {
      return response_doc.createElement(tag_name);
    }
    /**Compose a response element used when OAIPMH service sending responses thru 
     * ServiceCluster and MessageRouter, as they automatically wrap a message element
     * on this response element
     */
    public static Element getResponse(Element core_msg) {
      Element res = createElement(RESPONSE);
      res.appendChild(response_doc.importNode(core_msg, true));
      return res;
    }
    /** Read in OAIResumptionToken.xml (residing web/WEB-INF/classes/) */ 
    public static Element getOAIResumptionTokenXML() {     
      
      // The system environment variable $GSDL3HOME(ends ../web) does not contain the file separator 
      resumption_token_file = new File(GlobalProperties.getGSDL3Home() + FILE_SEPARATOR +
          "WEB-INF" + FILE_SEPARATOR + "classes" +FILE_SEPARATOR + "OAIResumptionToken.xml");
      if (resumption_token_file.exists()) {
        Document token_doc = parseXMLFile(resumption_token_file);
        if (token_doc != null) {
          resumption_token_elem = token_doc.getDocumentElement();
        } else {
          logger.error("Fail to parse resumption token file OAIReceptionToken.xml.");
          return null;
        }
        //remove all expired tokens
        clearExpiredTokens();
        return resumption_token_elem;  
      }
      //if resumption_token_file does not exist        
      logger.info("resumption token file: "+ resumption_token_file.getPath()+" not found! create an empty one.");
      resumption_token_elem = createElement(OAI_RESUMPTION_TOKENS);
      saveOAIResumptionTokenXML(resumption_token_elem);
      return resumption_token_elem;
    }
    public static void saveOAIResumptionTokenXML(Element token_elem) {     
      if(writeXMLFile(resumption_token_file, token_elem.getOwnerDocument()) == false) {
        logger.error("Fail to save the resumption token file");
      }
    }
    public static void clearExpiredTokens() {
      boolean token_deleted = false;
      NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, RESUMPTION_TOKEN);
      for (int i=0; i<tokens.getLength(); i++) {
        Element token_elem = (Element)tokens.item(i);
        String expire_str = token_elem.getAttribute(EXPIRATION_DATE);
        long datestamp = getTime(expire_str); // expire_str is in milliseconds
        if(datestamp < System.currentTimeMillis()) {
          resumption_token_elem.removeChild(token_elem);
          token_elem = null;
          token_deleted = true;
        }
      } 
      
      if(token_deleted) {
        saveOAIResumptionTokenXML(resumption_token_elem);
      }
    }
    public static boolean containsToken(String token) {
      NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, OAIXML.RESUMPTION_TOKEN);
      for (int i=0; i<tokens.getLength(); i++) {
        if(token.equals(GSXML.getNodeText((Element)tokens.item(i)).trim() ))
          return true;
      }
      return false;
    }
    public static void addToken(Element token) {
      Document doc = resumption_token_elem.getOwnerDocument();
      resumption_token_elem.appendChild(duplicateElement(doc, token, true));
      saveOAIResumptionTokenXML(resumption_token_elem);
    }
    public static void addToken(String token) {
      Element te = resumption_token_elem.getOwnerDocument().createElement(OAIXML.RESUMPTION_TOKEN);
      //add expiration att
      resumption_token_elem.appendChild(te);
      saveOAIResumptionTokenXML(resumption_token_elem);
    }
    public static boolean removeToken(String token) {
      NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, OAIXML.RESUMPTION_TOKEN);
      int num_tokens = tokens.getLength();
      for (int i=0; i<num_tokens; i++) {
          Element e = (Element)(tokens.item(i));
          if(token.equals(GSXML.getNodeText(e))) {
            resumption_token_elem.removeChild(e);
            saveOAIResumptionTokenXML(resumption_token_elem);
            return true;
          }
      }
      return false;      
    }
    /** Read in OAIConfig.xml (residing web/WEB-INF/classes/) and use it to configure the receptionist etc.
     *  the oai_version and baseURL variables are also set in here. 
     *  The init() method is also called in here. */ 
    public static Element getOAIConfigXML() {
      init();
     
      // The system environment variable $GSDL3HOME(ends ../web) does not contain the file separator 
      File oai_config_file = new File(GlobalProperties.getGSDL3Home() + FILE_SEPARATOR +
          "WEB-INF" + FILE_SEPARATOR + "classes" +FILE_SEPARATOR + "OAIConfig.xml");
      if (!oai_config_file.exists()) {
        logger.error(" oai config file: "+oai_config_file.getPath()+" not found!");
        return null;
      }
      Document oai_config_doc = parseXMLFile(oai_config_file);
      if (oai_config_doc != null) {
        oai_config_elem = oai_config_doc.getDocumentElement();
      } else {
        logger.error("Fail to parse oai config file OAIConfig.xml.");
        return null;
      }
      
      //initialize oai_version
      Element protocol_version = (Element)GSXML.getChildByTagName(oai_config_elem, PROTOCOL_VERSION);
      oai_version = GSXML.getNodeText(protocol_version).trim();

	  // initialize baseURL
	  Element base_url_elem = (Element)GSXML.getChildByTagName(oai_config_elem, BASE_URL);
      baseURL = GSXML.getNodeText(base_url_elem);
	  
      //initialize token_expiration
      Element expiration = (Element)GSXML.getChildByTagName(oai_config_elem, RESUMPTION_TOKEN_EXPIRATION);
      String expire_str = GSXML.getNodeText(expiration).trim();
      if (expiration != null && !expire_str.equals("")) {
        token_expiration = Long.parseLong(expire_str);
      }
      
      // read granularity from the config file
      Element granu_elem = (Element)GSXML.getChildByTagName(oai_config_elem, GRANULARITY);
      //initialize the granu_str which might be used by other methods (eg, getDate())
      granularity = GSXML.getNodeText(granu_elem).trim();

      //change "yyyy-MM-ddTHH:mm:ssZ" to "yyyy-MM-dd'T'HH:mm:ss'Z'"
      granularity = granularity.replaceAll("T", "'T'");
      granularity = granularity.replaceAll("Z", "'Z'");
      granularity = granularity.replaceAll("YYYY", "yyyy").replaceAll("DD", "dd").replaceAll("hh", "HH");
      return oai_config_elem;
    }

  public static String[] getMetadataMapping(Element metadata_format) {

    if (metadata_format == null) {
      return null;
    }
    NodeList mappings = metadata_format.getElementsByTagName(MAPPING);
    int size = mappings.getLength();
    if (size == 0) {
      logger.info("No metadata mappings are provided in OAIConfig.xml."); 
      return null;
    }
    String[] names = new String[size];
    for (int i=0; i<size; i++) {
      names[i] = GSXML.getNodeText((Element)mappings.item(i)).trim();
    }
    return names;      
    
  }
  
    public static String[] getGlobalMetadataMapping(String prefix) {
      Element list_meta_formats = (Element)GSXML.getChildByTagName(oai_config_elem, LIST_METADATA_FORMATS);
      if(list_meta_formats == null) {
        return null;
      }
      Element metadata_format = GSXML.getNamedElement(list_meta_formats, METADATA_FORMAT, METADATA_PREFIX, prefix);
      if(metadata_format == null) {
        return null;
      }
      return getMetadataMapping(metadata_format);
    }

    
    public static long getTokenExpiration() {
	return token_expiration*1000; // in milliseconds
    }

    /** TODO: returns a basic response for appropriate oai version
     *  
     */
    public static  Element createBasicResponse(String verb, String[] pairs) {

      Element response = createResponseHeader(verb);
      
      //set the responseDate and request elements accordingly
      Element request_elem = (Element)GSXML.getChildByTagName(response, REQUEST);
      if (verb.equals("")) {
        request_elem.setAttribute(VERB, verb);
      }
      int num_pairs = (pairs==null)? 0 : pairs.length;
      for (int i=num_pairs - 1; i>=0; i--) {
        int index = pairs[i].indexOf("=");
        if (index != -1) {
          String[] strs = pairs[i].split("=");
          if(strs != null && strs.length == 2) {
            request_elem.setAttribute(strs[0], oaiDecode(strs[1]));
          }
        }
      }//end of for()
      
	  GSXML.setNodeText(request_elem, baseURL);
      
      Node resp_date = GSXML.getChildByTagName(response, RESPONSE_DATE);
      if (resp_date != null) {
        GSXML.setNodeText((Element)resp_date, getCurrentUTCTime());
      }

      return response;
    }
    /** @param error_code the value of the code attribute
     *  @param error_text the node text of the error element
     *  @return an oai error element 
     *  Used by receptionist
     */
    public static Element createErrorElement(String error_code, String error_text) {
      Element error = createElement(ERROR);
      error.setAttribute(CODE, error_code);
      GSXML.setNodeText(error, error_text);
      return error;
    }

    /** convert the escaped sequences (eg, '%3A') of those special characters back to their
     *  original form (eg, ':'). 
     */
    public static String oaiDecode(String escaped_str) {
      logger.info("oaiDecode() " +escaped_str);
      for (int i=0; i<special_char.length; i++) {
        if (escaped_str.indexOf(escape_sequence[i]) != -1) {
          escaped_str = escaped_str.replaceAll(escape_sequence[i], special_char[i]);
        }
      }
      //escaped_str = escaped_str.replaceAll("%3A", ":");
      return escaped_str;        
    }
    /** convert those special characters (eg, ':') to their
     *  escaped sequences (eg, '%3A'). 
     */
    public static String oaiEncode(String original_str) {
      logger.info("oaiEncode() " + original_str);      
      for (int i=0; i<special_char.length; i++) {
        if (original_str.indexOf(special_char[i]) != -1) {
          original_str = original_str.replaceAll(special_char[i], escape_sequence[i]);
        }
      }
      //original_str = original_str.replaceAll(":", "%3A");
      return original_str;  
    }
    /** convert YYYY-MM_DDThh:mm:ssZ to yyyy-MM-ddTHH:mm:ssZ
     */
    public static String convertToJava(String oai_format) {
      oai_format = oai_format.replaceAll("YYYY", "yyyy").replaceAll("DD", "dd").replaceAll("hh", "HH");
      return oai_format;
    }
    /** convert yyyy-MM-ddTHH:mm:ssZ to YYYY-MM_DDThh:mm:ssZ
     */
    public static String convertToOAI(String java_format) {
      java_format = java_format.replaceAll("yyyy", "YYYY").replaceAll("dd", "DD").replaceAll("HH", "hh");
      return java_format;      
    }
    public static String getCurrentUTCTime() {
      Date current_utc = new Date(System.currentTimeMillis());
      //granularity is in the form: yyyy-MM-dd'T'HH:mm:ss'Z '
      DateFormat formatter = new SimpleDateFormat(granularity);
      return formatter.format(current_utc);
    }
    /** get a Date object from a Date format pattern string
     *
     * @param pattern - in the form: 2007-06-14T16:48:25Z, for example.
     * @return a Date object - null if the pattern is not in the specified form
     */

    public static Date getDate(String pattern) {
      if (pattern == null || pattern.equals("")) {
        return null;
      }
      Date date = null;
//      String str = pattern.replaceAll("T", " ");
//      str = str.replaceAll("Z", "");
      SimpleDateFormat sdf = null;
      try {
        sdf = new SimpleDateFormat(granularity);
        date = sdf.parse(pattern);
      } catch(Exception e) {
	  if(!default_granularity.equals(granularity)) { // try validating against default granularity
	      try {
		  date = null;
		  sdf = null;
		  sdf = new SimpleDateFormat(default_granularity);
		  date = sdf.parse(pattern);
	      } catch(Exception ex) {
		  logger.error("invalid date format: " + pattern);
		  return null;
	      }
	  } else {
	      logger.error("invalid date format: " + pattern);
	      return null;
	  }
      }
      return date;
    }
    /** get the million second value from a string representing time in a pattern
     * (eg, 2007-06-14T16:48:25Z)
     */
    public static long getTime(String pattern) {
      if (pattern == null || pattern.equals("")) {
        return -1;
      }
      Date date = null;
      SimpleDateFormat sdf = null;
      try {
        //granularity is a global variable in the form: yyyy-MM-ddTHH:mm:ssZ 
        sdf = new SimpleDateFormat(granularity);
        date = sdf.parse(pattern);
      } catch(Exception e) {
	  if(!default_granularity.equals(granularity)) { // try validating against default granularity
	      try {
		  date = null;
		  sdf = null;
		  sdf = new SimpleDateFormat(default_granularity);
		  date = sdf.parse(pattern);
	      } catch(Exception ex) {
		  logger.error("invalid date format: " + pattern);
		  return -1;
	      }
	  } else {
	      logger.error("invalid date format: " + pattern);
	      return -1;
	  }        
      }
      return date.getTime();
    }    
    /** get the string representation of a time from a long value(long type)
     */
    public static String getTime(long milliseconds) {
      Date date = new Date(milliseconds);
      SimpleDateFormat sdf = new SimpleDateFormat(granularity);
      return sdf.format(date);
    }    
    public static Element createResponseHeader(String verb) {
      String tag_name = (oai_version.equals(OAI_VERSION2))? OAI_DASH_PMH : verb;
      Element oai = response_doc.createElement(tag_name);
      Element resp_date = response_doc.createElement(RESPONSE_DATE);
      Element req = response_doc.createElement(REQUEST);
      oai.appendChild(resp_date);
      oai.appendChild(req);

      if(oai_version.equals(OAI_VERSION2)) {
        oai.setAttribute("xmlns", "http://www.openarchives.org/OAI/2.0/");
        oai.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        oai.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/2.0/ \n http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
      } else {
        oai.setAttribute("xmlns", "http://www.openarchives.com/OAI/1.1/OAI_" + verb);
        oai.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        oai.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/1.1/OAI_" + verb + "\n http://www.openarchives.org/OAI/1.1/OAI_" + verb + ".xsd");
      }
      return oai;
    }
    public static Element getMetadataPrefixElement(String tag_name, String version) {
      //examples of tag_name: dc, oai_dc:dc, etc.
      Element oai = response_doc.createElement(tag_name);
      if (version.equals(OAI_VERSION2)) {
        oai.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        oai.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        oai.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        oai.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/2.0/oai_dc/ \n http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
      } else {
        oai.setAttribute("xmlns", "ttp://www.openarchives.com/OAI/1.1/");
        oai.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        oai.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/1.1/" + tag_name + ".xsd");        
      }
      
      return oai;
    }
    public static HashMap<String, Node> getChildrenMapByTagName(Node n, String tag_name) {
	
	HashMap<String, Node> map= new HashMap<String, Node>();
	Node child = n.getFirstChild();
	while (child!=null) {
	    String name = child.getNodeName();
      if(name.equals(tag_name)) {
	      map.put(name, child);
      }
	    child = child.getNextSibling();
	}
	return map;
    }
    
    /** Duplicates an element */
    public static Element duplicateElement (Document owner, Element element, boolean with_attributes) {
        return duplicateElementNS (owner, element, null, with_attributes);
    }
    
    /** Duplicates an element */
    public static Element duplicateElementNS (Document owner,
    Element element,
    String namespace_uri,
    boolean with_attributes) {
        Element duplicate;
        if (namespace_uri == null) {
            duplicate = owner.createElement (element.getTagName ());
        } else {
            duplicate = owner.createElementNS (namespace_uri, element.getTagName ());
        }
        // Copy element attributes
        if (with_attributes) {
            NamedNodeMap attributes = element.getAttributes ();
            for (int i = 0; i < attributes.getLength (); i++) {
                Node attribute = attributes.item (i);
                duplicate.setAttribute (attribute.getNodeName (), attribute.getNodeValue ());
            }
        }
        
        // Copy element children
        NodeList children = element.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++) {
            Node child = children.item (i);
            duplicate.appendChild (owner.importNode (child, true));
        }
        
        return duplicate;
    }
    
    public static void copyElement(Element to, Element from, String elem_name) {
      
      Document to_doc = to.getOwnerDocument();
      Node child = from.getFirstChild();
      while (child != null) {
        if (child.getNodeName().equals(elem_name)) {
          to.appendChild(to_doc.importNode(child, true));
          return;
        }
        child = child.getNextSibling();
      }
    }
    public static HashMap<String, String> getParamMap(NodeList params) {
      HashMap<String, String> map = new HashMap<String, String>();
      for(int i=0; i<params.getLength(); i++) {
        Element param = (Element)params.item(i);
        String param_name = param.getAttribute(OAIXML.NAME);
        String param_value = param.getAttribute(OAIXML.VALUE);
        map.put(param_name, param_value);
      }
      return map;
    }
    /** Parse an XML document from a given file */
    static public Document parseXMLFile (File xml_file) {
        // No file? No point trying!
        if (xml_file.exists () == false) {
            return null;
        }
        Document doc = null;
        try {
            doc = parseXML (new FileInputStream (xml_file));
        }
        catch (Exception exception) {
            logger.error(exception.toString());
            return null;
        }
        return doc;
    }
    
    
    /** Parse an XML document from a given input stream */
    static public Document parseXML (InputStream xml_input_stream) {
        Document document = null;
        
        try {
            InputStreamReader isr = new InputStreamReader (xml_input_stream, "UTF-8");
            Reader xml_reader = new BufferedReader (isr);
            document = parseXML (xml_reader);
            isr.close ();
            xml_input_stream.close ();
        }
        catch (Exception exception) {
            logger.error(exception.toString());
        }
        
        return document;
    }
    
    /** Parse an XML document from a given reader */
    static public Document parseXML (Reader xml_reader) {
        Document document = null;
        
        try {
            InputSource isc       = new InputSource (xml_reader);
            DOMParser parser      = new DOMParser ();
            parser.setFeature ("http://xml.org/sax/features/validation", false);
            parser.setFeature ("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // May or may not be ignored, the documentation for Xerces is contradictory. If it works then parsing -should- be faster.
            parser.setFeature ("http://apache.org/xml/features/dom/defer-node-expansion", true);
            parser.setFeature ("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            parser.parse (isc);
            document = parser.getDocument ();
        }
        catch (SAXException exception) {
            System.err.println ("SAX exception: " + exception.getMessage ());
            logger.error(exception.toString());
        }
        catch (Exception exception) {
            logger.error(exception.toString());
        }
        
        return document;
    }
    /** Write an XML document to a given file */
    static public boolean writeXMLFile (File xml_file, Document document) {
        try {
            OutputStream os = new FileOutputStream (xml_file);
            // Create an output format for our document.
            OutputFormat f = new OutputFormat (document);
            f.setEncoding ("UTF-8");
            f.setIndenting (true);
            f.setLineWidth (0); // Why isn't this working!
            f.setPreserveSpace (false);
            // Create the necessary writer stream for serialization.
            OutputStreamWriter osw = new OutputStreamWriter (os, "UTF-8");
            Writer w               = new BufferedWriter (osw);
            // Generate a new serializer from the above.
            XMLSerializer s        = new XMLSerializer (w, f);
            s.asDOMSerializer ();
            // Finally serialize the document to file.
            s.serialize (document);
            // And close.
            os.close ();
            return true;
        }
        catch (Exception exception) {
            logger.error(exception.toString());
            return false;
        }
    }
    

}






