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

  // oai request parameters
  public static final String METADATA_PREFIX = "metadataPrefix";
  public static final String FROM = "from";
  public static final String UNTIL = "until";
  public static final String SET = "set";
  public static final String RESUMPTION_TOKEN = "resumptionToken";
  public static final String IDENTIFIER = "identifier";

  // Error element and code att
  public static final String ERROR = "error";
  public static final String CODE = "code";
  
  // OAI error codes
  public static final String BAD_ARGUMENT = "badArgument";
  public static final String BAD_RESUMPTION_TOKEN = "badResumptionToken";
  public static final String BAD_VERB = "badVerb";
  public static final String CANNOT_DISSEMINATE_FORMAT = "cannotDisseminateFormat";
  public static final String ID_DOES_NOT_EXIST = "idDoesNotExist";
  public static final String NO_METADATA_FORMATS = "noMetadataFormats";
  public static final String NO_RECORDS_MATCH = "noRecordsMatch";
  public static final String NO_SET_HIERARCHY = "noSetHierarchy";
  

  // words used to compose oai responses
  // many of these used in OAIConfig too

  // General
  public static final String OAI_PMH = "OAI-PMH";
  public static final String RESPONSE_DATE = "responseDate";
  public static final String REQUEST = "request";
  
  // Identify data
  public static final String ADMIN_EMAIL = "adminEmail";
  public static final String BASE_URL = "baseURL";
  public static final String COMPRESSION = "compression";
  public static final String DELETED_RECORD = "deletedRecord";
  public static final String DESCRIPTION = "description";
  public static final String EARLIEST_DATESTAMP = "earliestDatestamp";
  public static final String GRANULARITY = "granularity";
  public static final String PROTOCOL_VERSION = "protocolVersion";
  public static final String REPOSITORY_NAME = "repositoryName";
  public static final String OAI_IDENTIFIER = "oai-identifier";
  public static final String SCHEME = "scheme";
  public static final String REPOSITORY_IDENTIFIER = "repositoryIdentifier";
  public static final String DELIMITER = "delimiter";
  public static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

  // metadata formats
  public static final String METADATA_FORMAT = "metadataFormat";
  public static final String SCHEMA = "schema";
  public static final String METADATA_NAMESPACE = "metadataNamespace";
  public static final String OAI_DC = "oai_dc";
  public static final String DC = "dc";

  // record response data
  // SET_SPEC
  public static final String RECORD = "record";
  public static final String HEADER = "header";
  public static final String DATESTAMP = "datestamp";
  public static final String METADATA = "metadata";

  // list sets
  // SET, 
  public static final String SET_NAME = "setName";
  public static final String SET_SPEC = "setSpec";
  public static final String SET_DESCRIPTION = "setDescription";

  // resumption token element
  public static final String RESUMPTION_TOKEN_ELEM = "resumptionToken";
  public static final String EXPIRATION_DATE = "expirationDate";
  public static final String COMPLETE_LIST_SIZE = "completeListSize";
  public static final String CURSOR = "cursor";
  
  // extra elements/attributes from OAIConfig
  public static final String OAI_INFO = "oaiInfo";
  public static final String USE_STYLESHEET = "useOAIStylesheet";
  public static final String STYLESHEET = "OAIStylesheet";
  public static final String RESUME_AFTER = "resumeAfter";
  public static final String RESUMPTION_TOKEN_EXPIRATION = "resumptionTokenExpiration";
  public static final String OAI_SUPER_SET = "oaiSuperSet";
  public static final String MAPPING = "mapping";
  public static final String MAPPING_LIST = "mappingList";

  // code constants
   public static final String GS_OAI_RESOURCE_URL = "gs.OAIResourceURL";
   public static final String ILLEGAL_OAI_VERB = "Illegal OAI verb";
   public static final String LASTMODIFIED = "lastmodified";
  // // The node id in the collection database, which contains all the OIDs in the database
   public static final String BROWSELIST = "browselist";
   public static final String OAI_LASTMODIFIED = "oailastmodified";
   public static final String OAIPMH = "OAIPMH";
   public static final String OAI_SET_LIST = "oaiSetList";
   public static final String OAI_SERVICE_UNAVAILABLE = "OAI service unavailable";
   public static final String OID = "OID";
    
  //system-dependent file separator, maybe '/' or '\'
  public static final String FILE_SEPARATOR = File.separator;
  public static final String OAI_VERSION1 = "1.0";
  public static final String OAI_VERSION2 = "2.0";
  /*************************above are final values****************************/
    
    
  //initialized in getOAIConfigXML()
  public static Element oai_config_elem = null;
    
  //stores the date format "yyyy-MM-ddTHH:mm:ssZ"
  public static String granularity = "";

  // http://www.openarchives.org/OAI/openarchivesprotocol.html#DatestampsRequests 
  // specifies that all repositories must support YYYY-MM-DD (yyyy-MM-dd in Java)
  // this would be in addition to the other (optional) granularity of above that 
  // a repository may additionally choose to support.
  public static final String default_granularity = "yyyy-MM-dd";

  public static long token_expiration = 7200;
  /** which version of oai that this oaiserver supports; default is 2.0 
   *  initialized in getOAIConfigXML()
   */
  public static String oai_version = "2.0";
  public static String baseURL = "";
    
  /** Converter for parsing files and creating Elements */
  public static XMLConverter converter = new XMLConverter();
  
  public static String[] special_char = {"/", "?", "#", "=", "&", ":", ";", " ", "%", "+"};
  public static String[] escape_sequence = {"%2F", "%3F", "%23", "%3D", "%26", "%3A", "%3B", "%20", "%25", "%2B"};

  public static String getOAIVersion() {
    return oai_version;
  }
	
  public static String getBaseURL() {
    return baseURL;
  }
	
  /** Read in OAIConfig.xml (residing web/WEB-INF/classes/) and use it to configure the receptionist etc.
   *  the oai_version and baseURL variables are also set in here. 
   *  The init() method is also called in here. */ 
  public static Element getOAIConfigXML() {
     
    File oai_config_file = null;

    try {
      URL oai_config_url = Class.forName("org.greenstone.gsdl3.OAIServer").getClassLoader().getResource("OAIConfig.xml");
      if (oai_config_url == null) {
	logger.error("couldn't find OAIConfig.xml via class loader");
	return null;
      }
      oai_config_file = new File(oai_config_url.toURI());
      if (!oai_config_file.exists()) {
        logger.error(" oai config file: "+oai_config_file.getPath()+" not found!");
        return null;
      }
    } catch(Exception e) {
      logger.error("couldn't find OAIConfig.xml "+e.getMessage());
      return null;
    }

    Document oai_config_doc = converter.getDOM(oai_config_file, "utf-8");
    if (oai_config_doc != null) {
      oai_config_elem = oai_config_doc.getDocumentElement();
    } else {
      logger.error("Failed to parse oai config file OAIConfig.xml.");
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
  public static  Element createBasicResponse(Document doc, String verb, String[] pairs) {

    Element response = createResponseHeader(doc, verb);
      
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
   *  @return an oai error <message><response><error>
   */
  public static Element createErrorMessage(String error_code, String error_text) {
    Document doc = converter.newDOM();
    Element message = doc.createElement(GSXML.MESSAGE_ELEM);
    Element resp = doc.createElement(GSXML.RESPONSE_ELEM);
    message.appendChild(resp);
    Element error = createErrorElement(doc, error_code, error_text);
    resp.appendChild(error);
    return message;
  }
    
  /** @param error_code the value of the code attribute
   *  @param error_text the node text of the error element
   *  @return an oai error <response><error>
   */
  public static Element createErrorResponse(String error_code, String error_text) {
    Document doc = converter.newDOM();
    Element resp = doc.createElement(GSXML.RESPONSE_ELEM);
    Element error = createErrorElement(doc, error_code, error_text);
    resp.appendChild(error);
    return resp;
  }
    
  /** @param error_code the value of the code attribute
   *  @param error_text the node text of the error element
   *  @return an oai error <error>
   */
  public static Element createErrorElement(Document doc, String error_code, String error_text) {
    Element error = doc.createElement(ERROR);
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
  public static Element createResponseHeader(Document response_doc, String verb) {
    String tag_name = (oai_version.equals(OAI_VERSION2))? OAI_PMH : verb;
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
  public static Element getMetadataPrefixElement(Document doc, String tag_name, String version) {
    //examples of tag_name: dc, oai_dc:dc, etc.
    Element oai = doc.createElement(tag_name);
    if (version.equals(OAI_VERSION2)) {
      oai.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
      oai.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
      oai.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      oai.setAttribute("xsi:schemaLocation", "http://www.openarchives.org/OAI/2.0/oai_dc/ \n http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
    } else {
      oai.setAttribute("xmlns", "http://www.openarchives.com/OAI/1.1/");
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
    
  public static Element createOAIIdentifierXML(Document doc, String repository_id, String sample_collection, String sample_doc_id) {
    String xml = "<oai-identifier xmlns=\"http://www.openarchives.org/OAI/2.0/oai-identifier\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai-identifier\n http://www.openarchives.org/OAI/2.0/oai-identifier.xsd\">\n <scheme>oai</scheme>\n<repositoryIdentifier>" + repository_id + "</repositoryIdentifier>\n<delimiter>:</delimiter>\n<sampleIdentifier>oai:"+repository_id+":"+sample_collection+":"+sample_doc_id+"</sampleIdentifier>\n</oai-identifier>";

    Document xml_doc = converter.getDOM(xml);
    return (Element)doc.importNode(xml_doc.getDocumentElement(), true);
    

  }

  public static Element createGSDLElement(Document doc) {
    String xml = "<gsdl xmlns=\"http://www.greenstone.org/namespace/gsdl_oaiinfo/1.0/gsdl_oaiinfo\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n xsi:schemaLocation=\"http://www.greenstone.org/namespace/gsdl_oaiinfo/1.0/gsdl_oaiinfo\n   http://www.greenstone.org/namespace/gsdl_oaiinfo/1.0/gsdl_oaiinfo.xsd\"></gsdl>";
    Document xml_doc = converter.getDOM(xml);
    return (Element)doc.importNode(xml_doc.getDocumentElement(), true);
    

  }

  public static Element createSet(Document doc, String spec, String name, String description) {

    Element set_elem = doc.createElement(SET);
    Element set_spec = doc.createElement(SET_SPEC);
    GSXML.setNodeText(set_spec, spec);
    set_elem.appendChild(set_spec);
    Element set_name = doc.createElement(SET_NAME);
    GSXML.setNodeText(set_name, name);
    set_elem.appendChild(set_name);
    if (description != null) {
      Element set_description = doc.createElement(SET_DESCRIPTION);
      GSXML.setNodeText(set_description, description);
      set_elem.appendChild(set_description);
    }
    return set_elem;
    
  }

  /** returns the resumptionToken element to go into an OAI response */
  public static Element createResumptionTokenElement(Document doc, String token_name, int total_size, int cursor, long expiration_time) {
    Element token = doc.createElement(OAIXML.RESUMPTION_TOKEN);
    if (total_size != -1) {
      token.setAttribute(OAIXML.COMPLETE_LIST_SIZE, "" + total_size);
    }
    if (cursor != -1) {
      token.setAttribute(OAIXML.CURSOR, "" + cursor);
    }
    if(expiration_time !=-1) {
      token.setAttribute(OAIXML.EXPIRATION_DATE, getTime(expiration_time));
    }
   
    if (token != null) {
      GSXML.setNodeText(token, token_name);
    }
    return token;
  }
  
}






