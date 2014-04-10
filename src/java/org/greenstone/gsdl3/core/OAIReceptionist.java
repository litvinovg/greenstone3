/*
 *    OAIReceptionist.java
 *    Copyright (C) 2012 New Zealand Digital Library, http://www.nzdl.org
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

package org.greenstone.gsdl3.core;

import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.action.*;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// other java classes
import java.io.File;
import java.util.*;

import org.apache.log4j.*;

/** a Receptionist, used for oai metadata response xml generation.
 * This receptionist talks to the message router directly, 
 * instead of via any action, hence no action map is needed.
 * @see the basic Receptionist
 */
public class OAIReceptionist implements ModuleInterface {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.OAIReceptionist.class.getName());
  
  /** Instead of a config_params object, only a site_name is needed by oai receptionist. */
  protected String site_name = null;
  /** The unique  repository identifier */
  protected String repository_id = null;
    
  /** a converter class to parse XML and create Docs */
  protected XMLConverter converter=null;
  
  /** the configure file of this receptionist passed from the oai servlet. */
  protected Element oai_config = null;

  /** contained in the OAIConfig.xml deciding whether the resumptionToken should be in use */
  protected int resume_after = -1 ;
  
  /** the message router that the Receptionist and Actions will talk to */
  protected ModuleInterface mr = null;
  
  // Some of the data/responses will not change while the servlet is running, so
  // we can cache them
  
  /** A list of all the collections available to this OAI server */
  protected Element collection_list = null;
  /** a vector of the names, for convenience */
  protected Vector<String> collection_name_list = null;
  /** If this is true, then there are no OAI enabled collections, so can always return noRecordsMatch (after validating the request params) */
  protected boolean noRecordsMatch = false;
      
  /** A set of all known 'sets' */
  protected HashSet<String> set_set = null;

  protected boolean has_super_colls = false;
  /** a hash of super set-> collection list */
  protected HashMap<String, Vector<String>> super_coll_map = null;
  /** store the super coll elements for convenience */
  HashMap<String, Element> super_coll_data = null;
  /** The identify response */
  protected Element identify_response = null;
  /** The list set response */
  protected Element listsets_response = null;
  /** the list metadata formats response */
  protected Element listmetadataformats_response = null;

  public OAIReceptionist() {
    this.converter = new XMLConverter();
  }
  
  public void cleanUp() {
    if (this.mr != null) {
      
      this.mr.cleanUp();
    }
    OAIResumptionToken.saveTokensToFile();
  }
  
  public void setSiteName(String site_name) {
    this.site_name = site_name;
  }
  /** sets the message router  - it should already be created and
   * configured in the init() of a servlet (OAIServer, for example) before being passed to the receptionist*/
  public void setMessageRouter(ModuleInterface mr) {
    this.mr = mr;
  }
  
  /** configures the receptionist */
  public boolean configure(Element config) {
    
    if (this.mr==null) {
      logger.error(" message routers must be set  before calling oai configure");
      return false;
    }
    if (config == null) {
      logger.error(" oai configure file is null");
      return false;
    }
    oai_config = config;
    resume_after = getResumeAfter();
    
    repository_id = getRepositoryIdentifier(); 
    configureSuperSetInfo();
    if (!configureSetInfo()) {
      // there are no sets
      logger.error("No sets (collections) available for OAI");
      return false;
    }

    //clear out expired resumption tokens stored in OAIResumptionToken.xml
    OAIResumptionToken.init();
    OAIResumptionToken.clearExpiredTokens();
    
    return true;
  }

  // assuming that sets are static. If collections change then the servlet 
  // should be restarted.
  private boolean configureSuperSetInfo() {
    // do we have any super colls listed in web/WEB-INF/classes/OAIConfig.xml?
    // Will be like 
    // <oaiSuperSet>
    //   <SetSpec>xxx</SetSpec>
    //   <setName>xxx</SetName>
    //   <SetDescription>xxx</setDescription>
    // </oaiSuperSet>
    // The super set is listed in OAIConfig, and collections themselves state
    // whether they are part of the super set or not.
    NodeList super_coll_list = this.oai_config.getElementsByTagName(OAIXML.OAI_SUPER_SET);
    this.super_coll_data = new HashMap<String, Element>();
    if (super_coll_list.getLength() > 0) {
      this.has_super_colls = true;
      for (int i=0; i<super_coll_list.getLength(); i++) {
	Element super_coll = (Element)super_coll_list.item(i);
	Element set_spec = (Element)GSXML.getChildByTagName(super_coll, OAIXML.SET_SPEC);
	if (set_spec != null) {
	  String name = GSXML.getNodeText(set_spec);
	  if (!name.equals("")) {
	    this.super_coll_data.put(name, super_coll);
	    logger.error("adding in super coll "+name);
	  }
	}
      }
    
      if (this.super_coll_data.size()==0) {
	this.has_super_colls = false;
      }
    }
    if (this.has_super_colls == true) {
      this.super_coll_map = new HashMap<String, Vector<String>>();
    }
    return true;
    
  }
  private boolean configureSetInfo() {
    this.set_set = new HashSet<String>();

    // First, we get a list of all the OAI enabled collections
    // We get this by sending a listSets request to the MR
    Document doc = XMLConverter.newDOM();
    Element message = doc.createElement(GSXML.MESSAGE_ELEM);
    
    Element request = GSXML.createBasicRequest(doc, OAIXML.OAI_SET_LIST, "", null);
    message.appendChild(request);
    Node msg_node = mr.process(message);
    
    if (msg_node == null) {
      logger.error("returned msg_node from mr is null");
      return false;
    }
    Element resp = (Element)GSXML.getChildByTagName(msg_node, GSXML.RESPONSE_ELEM);
    Element coll_list = (Element)GSXML.getChildByTagName(resp, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
    if (coll_list == null) {
      logger.error("coll_list is null");
      return false;
    }

    this.collection_list = (Element)doc.importNode(coll_list, true);

    // go through and store a list of collection names for convenience
    // also create a 'to' attribute
    Node child = this.collection_list.getFirstChild();
    if (child == null) {
      logger.error("collection list has no children");
      noRecordsMatch = true;
      return false;
    }
    
    this.collection_name_list = new Vector<String>();
    StringBuffer to = new StringBuffer();
    boolean first = true;
    while (child != null) {
      if (child.getNodeName().equals(GSXML.COLLECTION_ELEM)) {
	String coll_id =((Element) child).getAttribute(GSXML.NAME_ATT);
	this.collection_name_list.add(coll_id);
	if (!first) {
	  to.append(',');
	}
	first = false;
	to.append(coll_id+"/"+OAIXML.LIST_SETS);
      }
      child = child.getNextSibling();
    }
    if (first) {
      // we haven't found any collections
      logger.error("found no collection elements in collectionList");
      noRecordsMatch = true;
      return false;
    }
    Document listsets_doc = XMLConverter.newDOM();
    Element listsets_element = listsets_doc.createElement(OAIXML.LIST_SETS);
    this.listsets_response = getMessage(listsets_doc, listsets_element);
    
    // Now, for each collection, get a list of all its sets
    // might include subsets (classifiers) or super colls
    // We'll reuse the first message, changing its type and to atts
    request.setAttribute(GSXML.TYPE_ATT, "");
    request.setAttribute(GSXML.TO_ATT, to.toString());
    // send to MR
    msg_node = mr.process(message);
    logger.error(this.converter.getPrettyString(msg_node));
    NodeList response_list =  ((Element)msg_node).getElementsByTagName(GSXML.RESPONSE_ELEM);
    for (int c=0; c<response_list.getLength(); c++) {
      // for each collection's response
      Element response = (Element)response_list.item(c);
      String coll_name = GSPath.getFirstLink(response.getAttribute(GSXML.FROM_ATT));
      logger.error("coll from response "+coll_name);
      NodeList set_list = response.getElementsByTagName(OAIXML.SET);
      for (int j=0; j<set_list.getLength(); j++) {
	// now check if it a super collection
	Element set = (Element)set_list.item(j);
	String set_spec = GSXML.getNodeText((Element)GSXML.getChildByTagName(set, OAIXML.SET_SPEC));
	logger.error("set spec = "+set_spec);
	// this may change if we add site name back in
	// setSpecs will be collname or collname:subset or supercollname
	if (set_spec.indexOf(":")==-1 && ! set_spec.equals(coll_name)) {
	  // it must be a super coll spec
	  logger.error("found super coll, "+set_spec);
	  // check that it is a valid one from config
	  if (this.has_super_colls == true && this.super_coll_data.containsKey(set_spec)) {
	    Vector <String> subcolls = this.super_coll_map.get(set_spec);
	    if (subcolls == null) {
	      logger.error("its new!!");
	      // not in there yet
	      subcolls = new Vector<String>();
	      this.set_set.add(set_spec);
	      this.super_coll_map.put(set_spec, subcolls);
	      // the first time a supercoll is mentioned, add into the set list
	      logger.error("finding the set info "+this.converter.getPrettyString(this.super_coll_data.get(set_spec)));
	      listsets_element.appendChild(GSXML.duplicateWithNewName(listsets_doc, this.super_coll_data.get(set_spec), OAIXML.SET, true));
	    }
	    // add this collection to the list for the super coll
	    subcolls.add(coll_name);
	  }
	} else { // its either the coll itself or a subcoll
	  // add in the set
	  listsets_element.appendChild(listsets_doc.importNode(set, true));
	  this.set_set.add(set_spec);
	}
      } // for each set in the collection
    } // for each OAI enabled collection
    return true;
  }

  protected void resetMessageRouter() {
    // we just need to send a configure request to MR
    Document doc = XMLConverter.newDOM();
    Element mr_request_message = doc.createElement(GSXML.MESSAGE_ELEM);
    Element mr_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_SYSTEM, "", null);
    mr_request_message.appendChild(mr_request);
    
    Element system = doc.createElement(GSXML.SYSTEM_ELEM);
    mr_request.appendChild(system);
    system.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_CONFIGURE);

    Element response = (Element) this.mr.process(mr_request_message);
    logger.error("configure response = "+this.converter.getPrettyString(response));
  }
  /** process using strings - just calls process using Elements */
  public String process(String xml_in) {
    
    Node message_node = this.converter.getDOM(xml_in);
    Node page = process(message_node);
    return this.converter.getString(page);
  }

  //Compose a message/response element used to send back to the OAIServer servlet. 
  //This method is only used within OAIReceptionist
  private Element getMessage(Document doc, Element e) {
    Element msg = doc.createElement(GSXML.MESSAGE_ELEM);
    Element response = doc.createElement(GSXML.RESPONSE_ELEM);
    msg.appendChild(response);
    response.appendChild(e);
    return msg;
  }

  /** process - produce xml data in response to a request
   * if something goes wrong, it returns null -
   */
  public Node process(Node message_node) {
    logger.error("OAIReceptionist received request");

    Element message = GSXML.nodeToElement(message_node);
    logger.error(this.converter.getString(message));

    // check that its a correct message tag
    if (!message.getTagName().equals(GSXML.MESSAGE_ELEM)) {
      logger.error(" Invalid message. GSDL message should start with <"+GSXML.MESSAGE_ELEM+">, instead it starts with:"+message.getTagName()+".");
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "Internal messaging error");
    }
   
    // get the request out of the message - assume that there is only one
    Element request = (Element)GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
    if (request == null) {
      logger.error(" message had no request!");
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "Internal messaging error");
    }

    // special case, reset=true for reloading the MR and recept data
    String reset = request.getAttribute("reset");
    if (!reset.equals("")) {
      resetMessageRouter();
      configureSetInfo();
      return OAIXML.createResetResponse(true);
    }

    
    //At this stage, the value of 'to' attribute of the request must be the 'verb'
    //The only thing that the oai receptionist can be sure is that these verbs are valid, nothing else.
    String verb = request.getAttribute(GSXML.TO_ATT);
    if (verb.equals(OAIXML.IDENTIFY)) {
      return doIdentify();
    }
    if (verb.equals(OAIXML.LIST_METADATA_FORMATS)) {
      return doListMetadataFormats(request);
    }
    if (verb.equals(OAIXML.LIST_SETS)) {
      // we have composed the list sets response on init
      // Note this means that list sets never uses resumption tokens
      return this.listsets_response; 
    }
    if (verb.equals(OAIXML.GET_RECORD)) {
      return doGetRecord(request);
    }
    if (verb.equals(OAIXML.LIST_IDENTIFIERS)) {
      return doListIdentifiersOrRecords(request,OAIXML.LIST_IDENTIFIERS , OAIXML.HEADER);
    }
    if (verb.equals(OAIXML.LIST_RECORDS)) {
      return doListIdentifiersOrRecords(request, OAIXML.LIST_RECORDS, OAIXML.RECORD);
    }
    // should never get here as verbs were checked in OAIServer
    return OAIXML.createErrorMessage(OAIXML.BAD_VERB, "Unexpected things happened");
    
  }


  private int getResumeAfter() {
    Element resume_after = (Element)GSXML.getChildByTagName(oai_config, OAIXML.RESUME_AFTER);
    if(resume_after != null) return Integer.parseInt(GSXML.getNodeText(resume_after));
    return -1;
  }
  private String getRepositoryIdentifier() {
    Element ri = (Element)GSXML.getChildByTagName(oai_config, OAIXML.REPOSITORY_IDENTIFIER);
    if (ri != null) { 
      return GSXML.getNodeText(ri);
    }
    return "";
  }


  /** if the param_map contains strings other than those in valid_strs, return false;
   *  otherwise true.
   */
  private boolean areAllParamsValid(HashMap<String, String> param_map, HashSet<String> valid_strs) {
    ArrayList<String> param_list = new ArrayList<String>(param_map.keySet());
    for(int i=0; i<param_list.size(); i++) {
      logger.error("param, key =  "+param_list.get(i)+", value = "+param_map.get(param_list.get(i)));
      if (valid_strs.contains(param_list.get(i)) == false) {
        return false;
      }
    }
    return true;
  }

  private Element doListIdentifiersOrRecords(Element req, String verb, String record_type) {
    // options: from, until, set, metadataPrefix, resumptionToken
    // exceptions: badArgument, badResumptionToken, cannotDisseminateFormat, noRecordMatch, and noSetHierarchy
    HashSet<String> valid_strs = new HashSet<String>();
    valid_strs.add(OAIXML.FROM);
    valid_strs.add(OAIXML.UNTIL);
    valid_strs.add(OAIXML.SET);
    valid_strs.add(OAIXML.METADATA_PREFIX);
    valid_strs.add(OAIXML.RESUMPTION_TOKEN);
    
    Document result_doc = XMLConverter.newDOM();
    Element result_element = result_doc.createElement(verb);
    boolean result_token_needed = false; // does this result need to include a
    // resumption token

    NodeList params = GSXML.getChildrenByTagName(req, GSXML.PARAM_ELEM);

    HashMap<String, String> param_map = GSXML.getParamMap(params); 
    
    // are all the params valid?
    if (!areAllParamsValid(param_map, valid_strs)) {
      logger.error("One of the params is invalid");
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "There was an invalid parameter");
      // TODO, need to tell the user which one was invalid ??
    }     
    
    // Do we have a resumption token??
    String token = null;
    String from = null;
    String until = null;
    boolean set_requested = false;
    String set_spec_str = null;
    String prefix_value = null;
    int cursor = 0;
    int current_cursor = 0;
    String current_set = null;
    long initial_time = 0;
    
    int total_size = -1; // we are only going to set this in resumption 
    // token if it is easy to work out, i.e. not sending extra requests to
    // MR just to calculate total size

    if(param_map.containsKey(OAIXML.RESUMPTION_TOKEN)) {
      // Is it an error to have other arguments? Do we need to check to make sure that resumptionToken is the only arg??
      // validate resumptionToken
      token = param_map.get(OAIXML.RESUMPTION_TOKEN);
      logger.info("has resumptionToken " + token);
      if(OAIResumptionToken.isValidToken(token) == false) {
	logger.error("token is not valid");
        return OAIXML.createErrorMessage(OAIXML.BAD_RESUMPTION_TOKEN, "");
      }
      result_token_needed = true; // we always need to send a token back if we have started with one. It may be empty if we are returning the end of the list
      // initialise the request params from the stored token data
      HashMap<String, String> token_data = OAIResumptionToken.getTokenData(token);
      from = token_data.get(OAIXML.FROM);
      until = token_data.get(OAIXML.UNTIL);
      set_spec_str = token_data.get(OAIXML.SET);
      if (set_spec_str != null) {
	set_requested = true;
      }
      prefix_value = token_data.get(OAIXML.METADATA_PREFIX);
      current_set = token_data.get(OAIResumptionToken.CURRENT_SET);
      try {
	cursor = Integer.parseInt(token_data.get(OAIXML.CURSOR));
	cursor = cursor + resume_after; // increment cursor
	current_cursor = Integer.parseInt(token_data.get(OAIResumptionToken.CURRENT_CURSOR));
	initial_time = Long.parseLong(token_data.get(OAIResumptionToken.INITIAL_TIME));
      } catch (NumberFormatException e) {
	logger.error("tried to parse int from cursor data and failed");
      }
      
      // check that the collections/sets haven't changed since the token was issued
      if (collectionsChangedSinceTime(set_spec_str, initial_time)) {
	logger.error("one of the collections in set "+set_spec_str+" has changed since token issued. Expiring the token");
	OAIResumptionToken.expireToken(token);
	return OAIXML.createErrorMessage(OAIXML.BAD_RESUMPTION_TOKEN, "Repository data has changed since this token was issued. Resend original request");
      }
    }
    else {
      // no resumption token, lets check the other params
      // there must be a metadataPrefix
      if (!param_map.containsKey(OAIXML.METADATA_PREFIX)) {
	logger.error("metadataPrefix param required");
	return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "metadataPrefix param required");
      }

      //if there are any date params, check they're of the right format
      from = param_map.get(OAIXML.FROM);
      if(from != null) {	
	Date from_date = OAIXML.getDate(from);
	if(from_date == null) {
	  logger.error("invalid date: " + from);
	  return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "invalid format for "+ OAIXML.FROM);
	}
      }
      until = param_map.get(OAIXML.UNTIL);
      if(until != null) {	
	Date until_date = OAIXML.getDate(until);
	if(until_date == null) {
	  logger.error("invalid date: " + until);
	  return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "invalid format for "+ OAIXML.UNTIL);
	} 
      }    
      if(from != null && until != null) { // check they are of the same date-time format (granularity)
	if(from.length() != until.length()) {
	  logger.error("The request has different granularities (date-time formats) for the From and Until date parameters.");
	  return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "The request has different granularities (date-time formats) for the From and Until date parameters.");
	}
      }
  
      // check the set arg is a set we know about
      set_requested = param_map.containsKey(OAIXML.SET);
      set_spec_str = null;
      if(set_requested == true) {
	set_spec_str = param_map.get(OAIXML.SET);
	if (!this.set_set.contains(set_spec_str)) {
	  // the set is not one we know about
	  logger.error("requested set is not found in this repository");
	  return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "invalid set parameter");	
	  
	}
      }
      // Is the metadataPrefix arg one this repository supports?
      prefix_value = param_map.get(OAIXML.METADATA_PREFIX);
      if (repositorySupportsMetadataPrefix(prefix_value) == false) {
	logger.error("requested metadataPrefix is not found in OAIConfig.xml");
	return OAIXML.createErrorMessage(OAIXML.CANNOT_DISSEMINATE_FORMAT, "metadata format "+prefix_value+" not supported by this repository");
      }
      
    } // else no resumption token, check other params    
    
    // Whew. Now we have validated the params, we can work on doing the actual
    // request


    Document doc = XMLConverter.newDOM();
    Element mr_msg = doc.createElement(GSXML.MESSAGE_ELEM);
    Element mr_req = doc.createElement(GSXML.REQUEST_ELEM);
    // TODO does this need a type???
    mr_msg.appendChild(mr_req);

    // copy in the from/until params if there
    if (from != null) {
      mr_req.appendChild(GSXML.createParameter(doc, OAIXML.FROM, from));
    }
    if (until != null) {
      mr_req.appendChild(GSXML.createParameter(doc, OAIXML.UNTIL, until));
    }
    // add metadataPrefix
    mr_req.appendChild(GSXML.createParameter(doc, OAIXML.METADATA_PREFIX, prefix_value));
	
    // do we have a set???
    // if no set, we send to all collections in the collection list
    // if super set, we send to all collections in super set list
    // if a single collection, send to it
    // if a subset, send to the collection
    Vector<String> current_coll_list = getCollectionListForSet(set_spec_str);
    boolean single_collection = false;
    if (current_coll_list.size() == 1) {
      single_collection = true;
    }
    if (set_spec_str != null && set_spec_str.indexOf(":") != -1) {
      // we have a subset - add the set param back in
      mr_req.appendChild(GSXML.createParameter(doc, OAIXML.SET, set_spec_str));
    }

    int num_collected_records = 0;
    int start_point = current_cursor; // may not be 0 if we are using a resumption token
    String resumption_collection = "";
    boolean empty_result_token = false; // if we are sending the last part of a list, then the token value will be empty
    
    // iterate through the list of collections and send the request to each

    int start_coll=0;
    if (current_set != null) {
      // we are resuming a previous request, need to locate the first collection
      for (int i=0; i<current_coll_list.size(); i++) {
	if (current_set.equals(current_coll_list.get(i))) {
	  start_coll = i;
	  break;
	}
      }
    }
    
    for (int i=start_coll; i<current_coll_list.size(); i++) {
      String current_coll = current_coll_list.get(i);
      mr_req.setAttribute(GSXML.TO_ATT, current_coll+"/"+verb);

      Element result = (Element)mr.process(mr_msg);
      logger.error(verb+ " result for coll "+current_coll);
      logger.error(this.converter.getPrettyString(result));
      if (result == null) {
	logger.info("message router returns null");
	// do what??? carry on? fail??
	return OAIXML.createErrorMessage("Internal service returns null", "");
      }
      Element res = (Element)GSXML.getChildByTagName(result, GSXML.RESPONSE_ELEM);
      if(res == null) {
        logger.info("response element in xml_result is null");
        return OAIXML.createErrorMessage("Internal service returns null", "");
      }
      NodeList record_list = res.getElementsByTagName(record_type);
      int num_records = record_list.getLength();
      if(num_records == 0) {
	logger.info("message router returns 0 records for coll "+current_coll);
	continue; // try the next collection
      }  
      if (single_collection) {
	total_size = num_records;
      }
      int records_to_add = (resume_after > 0 ? resume_after - num_collected_records : num_records);
      if (records_to_add > (num_records-start_point)) {
	records_to_add = num_records-start_point;
      }
      addRecordsToList(result_doc, result_element, record_list, start_point, records_to_add);
      num_collected_records += records_to_add;
      
      // do we need to stop here, and do we need to issue a resumption token?
      if (resume_after > 0 && num_collected_records == resume_after) {
	// we have finished collecting records at the moment.
	// but are we conincidentally at the end? or are there more to go?
	if (records_to_add < (num_records - start_point)) {
	  // we have added less than this collection had
	  start_point += records_to_add;
	  resumption_collection = current_coll;
	  result_token_needed = true;
	}
	else {
	  // we added all this collection had to offer
	  // is there another collection in the list??
	  if (i<current_coll_list.size()-1) {
	    result_token_needed = true;
	    start_point = 0;
	    resumption_collection = current_coll_list.get(i+1);
	  } 
	  else {
	    // we have finished one collection and there are no more collection
	    // if we need to send a resumption token (in this case, only because we started with one, then it will be empty
	    logger.error("at end of list, need empty result token");
	    empty_result_token = true;
	  }
	}
	break;
      }
      start_point = 0; // only the first one will have start non-zero, if we
      // have a resumption token
      
    } // for each collection

    if (num_collected_records ==0) {
      // there were no matching results
      return OAIXML.createErrorMessage(OAIXML.NO_RECORDS_MATCH, "");
    }

    if (num_collected_records < resume_after) {
      // we have been through all collections, and there are no more
      // if we need a result token - only because we started with one, so we need to send an empty one, then make sure everyone knows we are just sending an empty one
      if (result_token_needed) {
	empty_result_token = true;
      }
    }
    
    if (result_token_needed) {
      // we need a resumption token
      if (empty_result_token) {
	logger.error("have empty result token");
	token = "";
      } else {
	if (token != null) {
	  // we had a token for this request, we can just update it
	  token = OAIResumptionToken.updateToken(token, ""+cursor, resumption_collection, ""+start_point);
	} else {
	  // we are generating a new one
	  token = OAIResumptionToken.createAndStoreResumptionToken(set_spec_str, prefix_value, from, until, ""+cursor, resumption_collection, ""+start_point );
	}
      }

      // result token XML
      long expiration_date = -1;
      if (empty_result_token) {
	// we know how many records in total as we have sent them all
	total_size = cursor+num_collected_records;
      } else {
	// non-empty token, set the expiration date
	expiration_date = OAIResumptionToken.getExpirationDate(token);
      }
      Element token_elem = OAIXML.createResumptionTokenElement(result_doc, token, total_size, cursor, expiration_date); 
      // OAIXML.addToken(token_elem); // store it
      result_element.appendChild(token_elem); // add to the result
    }
  

    return getMessage(result_doc, result_element);
  }

  private Vector<String> getCollectionListForSet(String set) {
    if (set == null) {
      // no set requested, need the complete collection list
      return this.collection_name_list;
    }
    if (has_super_colls && super_coll_map.containsKey(set)) {
      return super_coll_map.get(set);
    }

    Vector<String> coll_list = new Vector<String>();
    if (set.indexOf(":") != -1) {
      String col_name = set.substring(0, set.indexOf(":"));
      coll_list.add(col_name);
    }
    else {
      coll_list.add(set);
    }
    return coll_list;
  }
  private void addRecordsToList(Document doc, Element result_element, NodeList
				record_list, int start_point, int num_records) {
    int end_point = start_point + num_records;
    for (int i=start_point; i<end_point; i++) {
      result_element.appendChild(doc.importNode(record_list.item(i), true));
    }
  }


  // method exclusively used by doListRecords/doListIdentifiers
  private void getRecords(Element verb_elem, NodeList list, int start_point, int end_point) {
    for (int i=start_point; i<end_point; i++) {
      verb_elem.appendChild(verb_elem.getOwnerDocument().importNode(list.item(i), true));
    }
  }
  private Element collectAll(Element result, Element msg, String verb, String elem_name) {
    if(result == null) {
      //in the first round, result is null
      return msg;
    }
    Element res_in_result = (Element)GSXML.getChildByTagName(result, GSXML.RESPONSE_ELEM);
    if(res_in_result == null) { // return the results of all other collections accumulated so far
      return msg;
    }
    Element verb_elem = (Element)GSXML.getChildByTagName(res_in_result, verb);
    if(msg == null) {
      return result;
    }

    //e.g., get all <record> elements from the returned message. There may be none of
    //such element, for example, the collection service returned an error message
    NodeList elem_list = msg.getElementsByTagName(elem_name);
    
    for (int i=0; i<elem_list.getLength(); i++) {
      verb_elem.appendChild(res_in_result.getOwnerDocument().importNode(elem_list.item(i), true));
    }
    return result;
  }

  
  /** there are three possible exception conditions: bad argument, idDoesNotExist, and noMetadataFormat.
   * The first one is handled here, and the last two are processed by OAIPMH.
   */
  private Element doListMetadataFormats(Element req) {
    //if the verb is ListMetadataFormats, there could be only one parameter: identifier
    //, or there is no parameter; otherwise it is an error
    //logger.info("" + this.converter.getString(msg));
    
    NodeList params = GSXML.getChildrenByTagName(req, GSXML.PARAM_ELEM);
    Element param = null;
    Document lmf_doc = XMLConverter.newDOM();
    if(params.getLength() == 0) {
      //this is requesting metadata formats for the whole repository
      //read the oaiConfig.xml file, return the metadata formats specified there.
      if (this.listmetadataformats_response != null) {
	// we have already created it
	return this.listmetadataformats_response;
      }

      Element list_metadata_formats = lmf_doc.createElement(OAIXML.LIST_METADATA_FORMATS);
      
      Element format_list = (Element)GSXML.getChildByTagName(oai_config, OAIXML.LIST_METADATA_FORMATS);
      if(format_list == null) {
	logger.error("OAIConfig.xml must contain the supported metadata formats");
	// TODO this is internal error, what to do???
	return getMessage(lmf_doc, list_metadata_formats);
      }
      NodeList formats = format_list.getElementsByTagName(OAIXML.METADATA_FORMAT);
      for(int i=0; i<formats.getLength(); i++) {
	Element meta_fmt = lmf_doc.createElement(OAIXML.METADATA_FORMAT);
	Element first_meta_format = (Element)formats.item(i);
	//the element also contains mappings, but we don't want them
	meta_fmt.appendChild(lmf_doc.importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.METADATA_PREFIX), true));
	meta_fmt.appendChild(lmf_doc.importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.SCHEMA), true));
	meta_fmt.appendChild(lmf_doc.importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.METADATA_NAMESPACE), true));
	list_metadata_formats.appendChild(meta_fmt);
      }
      return getMessage(lmf_doc, list_metadata_formats);
      
      
    } 

    if (params.getLength() > 1) {
      //Bad argument. Can't be more than one parameters for ListMetadataFormats verb
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "");
    } 
    
    // This is a request for the metadata of a particular item with an identifier
    /**the request xml is in the form: <request>
     *                                   <param name=.../>
     *                                 </request>
     *And there is a param element and one element only. (No paramList element in between).
     */
    param = (Element)params.item(0);
    String param_name = param.getAttribute(GSXML.NAME_ATT);
    String identifier = "";
    if (!param_name.equals(OAIXML.IDENTIFIER)) {
      //Bad argument
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "");
    } 
      
    identifier = param.getAttribute(GSXML.VALUE_ATT);
    // the identifier is in the form: <coll_name>:<OID>
    // so it must contain at least two ':' characters
    String[] strs = identifier.split(":");
    if(strs == null || strs.length < 2) {
      // the OID may also contain ':'
      logger.error("identifier is not in the form coll:id" + identifier);
      return OAIXML.createErrorMessage(OAIXML.ID_DOES_NOT_EXIST, "");
    }
        
    // send request to message router
    // get the names
    strs = splitNames(identifier);
    if(strs == null || strs.length < 2) {
      logger.error("identifier is not in the form coll:id" + identifier);
      return OAIXML.createErrorMessage(OAIXML.ID_DOES_NOT_EXIST, "");
    }
    //String name_of_site = strs[0];
    String coll_name = strs[0];
    String oid = strs[1];

    //re-organize the request element
    // reset the 'to' attribute
    String verb = req.getAttribute(GSXML.TO_ATT);
    req.setAttribute(GSXML.TO_ATT, coll_name + "/" + verb);
    // reset the identifier element
    param.setAttribute(GSXML.NAME_ATT, OAIXML.OID);
    param.setAttribute(GSXML.VALUE_ATT, oid);

    // TODO is this the best way to do this???? should we create a new request???
    Element message = req.getOwnerDocument().createElement(GSXML.MESSAGE_ELEM);
    message.appendChild(req);
    //Now send the request to the message router to process
    Node result_node = mr.process(message);
    return GSXML.nodeToElement(result_node);
  }
  
    


  private void copyNamedElementfromConfig(Element to_elem, String element_name) {
    Element original_element = (Element)GSXML.getChildByTagName(oai_config, element_name);
    if(original_element != null) {
      copyNode(to_elem, original_element);
    }
  }

  private void copyNode(Element to_elem, Node original_element) {
    to_elem.appendChild(to_elem.getOwnerDocument().importNode(original_element, true));

  }

  private Element doIdentify() {
    //The validation for this verb has been done in OAIServer.validate(). So no bother here.
    logger.info("");
    if (this.identify_response != null) {
      // we have already created it
      return getMessage(this.identify_response.getOwnerDocument(), this.identify_response);
    }
    Document doc = XMLConverter.newDOM();
    Element identify = doc.createElement(OAIXML.IDENTIFY);
    //do the repository name
    copyNamedElementfromConfig(identify, OAIXML.REPOSITORY_NAME);
    //do the baseurl
    copyNamedElementfromConfig(identify, OAIXML.BASE_URL);
    //do the protocol version
    copyNamedElementfromConfig(identify, OAIXML.PROTOCOL_VERSION);
        
    //There can be more than one admin email according to the OAI specification
    NodeList admin_emails = GSXML.getChildrenByTagName(oai_config, OAIXML.ADMIN_EMAIL);
    int num_admin = 0;
    Element from_admin_email = null;  
    if (admin_emails != null) {
      num_admin = admin_emails.getLength();
    }
    for (int i=0; i<num_admin; i++) {
      copyNode(identify, admin_emails.item(i));
    }

    //do the earliestDatestamp
    //send request to mr to search through the earliest datestamp amongst all oai collections in the repository.
    //ask the message router for a list of oai collections
    //NodeList oai_coll = getOAICollectionList();
    long earliestDatestamp = getEarliestDateStamp(collection_list);
    String earliestDatestamp_str = OAIXML.getTime(earliestDatestamp);
    Element earliestDatestamp_elem = doc.createElement(OAIXML.EARLIEST_DATESTAMP);
    GSXML.setNodeText(earliestDatestamp_elem, earliestDatestamp_str);
    identify.appendChild(earliestDatestamp_elem);

    //do the deletedRecord
    copyNamedElementfromConfig(identify, OAIXML.DELETED_RECORD);
    //do the granularity
    copyNamedElementfromConfig(identify, OAIXML.GRANULARITY);
     
    // output the oai identifier
    Element description = doc.createElement(OAIXML.DESCRIPTION);
    identify.appendChild(description);
    // TODO, make this a valid id
    Element oaiIdentifier = OAIXML.createOAIIdentifierXML(doc, repository_id, "lucene-jdbm-demo", "ec159e");
    description.appendChild(oaiIdentifier);

    // if there are any oaiInfo metadata, add them in too.
    Element info = (Element)GSXML.getChildByTagName(oai_config, OAIXML.OAI_INFO);
    if (info != null) {
      NodeList meta = GSXML.getChildrenByTagName(info, OAIXML.METADATA);
      if (meta != null && meta.getLength() > 0) {
	Element gsdl = OAIXML.createGSDLElement(doc);
	description.appendChild(gsdl);
	for (int m = 0; m<meta.getLength(); m++) {
	  copyNode(gsdl, meta.item(m));
	}
	
      }
    }
    this.identify_response = identify;
    return getMessage(doc, identify);
  }
  //split setSpec (site_name:coll_name) into an array of strings
  //It has already been checked that the set_spec contains at least one ':'
  private String[] splitSetSpec(String set_spec) {
    logger.info(set_spec);
    String[] strs = new String[2];
    int colon_index = set_spec.indexOf(":");
    strs[0] = set_spec.substring(0, colon_index);
    strs[1] = set_spec.substring(colon_index + 1);
    return strs;
  }
  /** split the identifier into <collection + OID> as an array 
      It has already been checked that the 'identifier' contains at least one ':'
  */
  private String[] splitNames(String identifier) {
    logger.info(identifier);
    String [] strs = new String[2];
    int first_colon = identifier.indexOf(":");
    if(first_colon == -1) {
      return null;
    }
    strs[0] = identifier.substring(0, first_colon);
    strs[1] = identifier.substring(first_colon + 1);
    return strs;
  }
  /** validate if the specified metadata prefix value is supported by the repository
   *  by checking it in the OAIConfig.xml
   */
  private boolean repositorySupportsMetadataPrefix(String prefix_value) {
    NodeList prefix_list = oai_config.getElementsByTagName(OAIXML.METADATA_PREFIX);
    
    for(int i=0; i<prefix_list.getLength(); i++) {
      if(prefix_value.equals(GSXML.getNodeText((Element)prefix_list.item(i)).trim() )) {
        return true;
      }
    }
    return false;
  }
  private Element doGetRecord(Element req){
    logger.info("");
    /** arguments:
        identifier: required
        metadataPrefix: required
	*  Exceptions: badArgument; cannotDisseminateFormat; idDoesNotExist
	*/ 
    Document doc = XMLConverter.newDOM();
    Element get_record = doc.createElement(OAIXML.GET_RECORD);

    HashSet<String> valid_strs = new HashSet<String>();
    valid_strs.add(OAIXML.IDENTIFIER);
    valid_strs.add(OAIXML.METADATA_PREFIX);

    NodeList params = GSXML.getChildrenByTagName(req, GSXML.PARAM_ELEM);
    HashMap<String, String> param_map = GSXML.getParamMap(params);    
    
    if(!areAllParamsValid(param_map, valid_strs) ||
       params.getLength() == 0 ||
       param_map.containsKey(OAIXML.IDENTIFIER) == false ||
       param_map.containsKey(OAIXML.METADATA_PREFIX) == false ) {
      logger.error("must have the metadataPrefix/identifier parameter.");
      return OAIXML.createErrorMessage(OAIXML.BAD_ARGUMENT, "");
    }
    
    String prefix = param_map.get(OAIXML.METADATA_PREFIX);
    String identifier = param_map.get(OAIXML.IDENTIFIER);
    
    // verify the metadata prefix
    if (repositorySupportsMetadataPrefix(prefix) == false) {
      logger.error("requested prefix is not found in OAIConfig.xml");
      return OAIXML.createErrorMessage(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }

    // get the names
    String[] strs = splitNames(identifier);
    if(strs == null || strs.length < 2) {
      logger.error("identifier is not in the form coll:id" + identifier);
      return OAIXML.createErrorMessage(OAIXML.ID_DOES_NOT_EXIST, "");
    }    
    //String name_of_site = strs[0];
    String coll_name = strs[0];
    String oid = strs[1];
    
    //re-organize the request element
    // reset the 'to' attribute
    String verb = req.getAttribute(GSXML.TO_ATT);
    req.setAttribute(GSXML.TO_ATT, coll_name + "/" + verb);
    // reset the identifier element
    Element param = GSXML.getNamedElement(req, GSXML.PARAM_ELEM, GSXML.NAME_ATT, OAIXML.IDENTIFIER);
    if (param != null) {
      param.setAttribute(GSXML.NAME_ATT, OAIXML.OID);
      param.setAttribute(GSXML.VALUE_ATT, oid);
    }    

    //Now send the request to the message router to process
    Element msg = doc.createElement(GSXML.MESSAGE_ELEM);
    msg.appendChild(doc.importNode(req, true));
    Node result_node = mr.process(msg);
    return GSXML.nodeToElement(result_node);
  }

  // See OAIConfig.xml
  // dynamically works out what the earliestDateStamp is, since it varies by collection
  // returns this time in *milliseconds*.
  protected long getEarliestDateStamp(Element oai_coll_list) {
    // config earliest datstamp
    long config_datestamp = 0;
    Element config_datestamp_elem = (Element)GSXML.getChildByTagName(this.oai_config, OAIXML.EARLIEST_DATESTAMP);
    if (config_datestamp_elem != null) {
      String datest = GSXML.getNodeText(config_datestamp_elem);
      config_datestamp = OAIXML.getTime(datest);
      if (config_datestamp == -1) {
	config_datestamp = 0;
      }
    }
    //do the earliestDatestamp
    long current_time = System.currentTimeMillis();
    long earliestDatestamp = current_time;
    NodeList oai_coll = oai_coll_list.getElementsByTagName(GSXML.COLLECTION_ELEM);
    int oai_coll_size = oai_coll.getLength();
    if (oai_coll_size == 0) {
      logger.info("returned oai collection list is empty. Setting repository earliestDatestamp to be the earliest datestamp from OAIConfig.xml, or 1970-01-01 if not specified.");
      return config_datestamp;
    }
    // the earliestDatestamp is now stored as a metadata element in the collection's buildConfig.xml file
    // we get the earliestDatestamp among the collections
    for(int i=0; i<oai_coll_size; i++) {
      long coll_earliestDatestamp = Long.parseLong(((Element)oai_coll.item(i)).getAttribute(OAIXML.EARLIEST_DATESTAMP));
      if (coll_earliestDatestamp == 0) {
	// try last modified
	coll_earliestDatestamp = Long.parseLong(((Element)oai_coll.item(i)).getAttribute(OAIXML.LAST_MODIFIED));
      }
      if (coll_earliestDatestamp > 0) {
	earliestDatestamp = (earliestDatestamp > coll_earliestDatestamp)? coll_earliestDatestamp : earliestDatestamp;
      }
    }
    if (earliestDatestamp == current_time) {
      logger.info("no collection had a real datestamp, using value from OAIConfig");
      return config_datestamp;
    }
    return earliestDatestamp; 
  }

  private boolean collectionsChangedSinceTime(String set_spec_str, long initial_time) {

    // we need to look though all collections in the set to see if any have last modified dates > initial_time
    Vector<String> set_coll_list = getCollectionListForSet(set_spec_str);

    Node child = this.collection_list.getFirstChild();
    while (child != null) {
      if (child.getNodeName().equals(GSXML.COLLECTION_ELEM)) {
	String coll_id =((Element) child).getAttribute(GSXML.NAME_ATT);
	if (set_coll_list.contains(coll_id)) {
	  long last_modified = Long.parseLong(((Element)child).getAttribute(OAIXML.LAST_MODIFIED));
	  if (initial_time < last_modified) {
	    return true;
	  }
	}
      }
      child = child.getNextSibling();
    }
    return false;
 
  }

}

  
