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
  
  /** container Document to create XML Nodes for requests sent to message router
   *  Not used for response 
   */
  protected Document doc=null;
  
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
  protected NodeList collection_list = null;

  /** The identify response */
  protected Element identify_response = null;
  
  public OAIReceptionist() {
    this.converter = new XMLConverter();
    this.doc = this.converter.newDOM();

  }
  
  public void cleanUp() {}
  
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
    
    repository_id = getRepositoryId(); 
    collection_list = getOAICollectionList();

    //clear out expired resumption tokens stored in OAIResumptionToken.xml
    OAIXML.init();
    OAIXML.clearExpiredTokens();
    
    return true;
  }
  /** process using strings - just calls process using Elements */
  public String process(String xml_in) {
    
    Node message_node = this.converter.getDOM(xml_in);
    Node page = process(message_node);
    return this.converter.getString(page);
  }

  //Compose a message element used to send back to the OAIServer servlet. 
  //This method is only used within OAIReceptionist
  private Element getMessage(Element e) {
    Element msg = OAIXML.createElement(OAIXML.MESSAGE);
    msg.appendChild(OAIXML.getResponse(e));
    return msg;
  }
  /** process - produce xml data in response to a request
   * if something goes wrong, it returns null -
   */
  public Node process(Node message_node) {
    logger.error("OAIReceptionist received request");

    Element message = this.converter.nodeToElement(message_node);
    logger.error(this.converter.getString(message));

    // check that its a correct message tag
    if (!message.getTagName().equals(GSXML.MESSAGE_ELEM)) {
      logger.error(" Invalid message. GSDL message should start with <"+GSXML.MESSAGE_ELEM+">, instead it starts with:"+message.getTagName()+".");
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
   
    // get the request out of the message - assume that there is only one
    Element request = (Element)GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
    if (request == null) {
      logger.error(" message had no request!");
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    //At this stage, the value of 'to' attribute of the request must be the 'verb'
    //The only thing that the oai receptionist can be sure is that these verbs are valid, nothing else.
    String verb = request.getAttribute(GSXML.TO_ATT);
    if (verb.equals(OAIXML.IDENTIFY)) {
      return doIdentify();
    }
    if (verb.equals(OAIXML.LIST_METADATA_FORMATS)) {
      return doListMetadataFormats(message);
    }
    if (verb.equals(OAIXML.LIST_SETS)) {
      return doListSets(message);
    }
    if (verb.equals(OAIXML.GET_RECORD)) {
      return doGetRecord(message);
    }
    if (verb.equals(OAIXML.LIST_IDENTIFIERS)) {
      return doListIdentifiers(message);
    }
    if (verb.equals(OAIXML.LIST_RECORDS)) {
      return doListRecords(message);
    }
    return getMessage(OAIXML.createErrorElement("Unexpected things happened", ""));
    
  }
  /** send a request to the message router asking for a list of collections that support oai
   *  The type attribute must be changed from 'oaiService' to 'oaiSetList'
   */
  private NodeList getOAICollectionList() {
    Element message = this.doc.createElement(OAIXML.MESSAGE);
    Element request = this.doc.createElement(OAIXML.REQUEST);
    message.appendChild(request);
    request.setAttribute(OAIXML.TYPE, OAIXML.OAI_SET_LIST);
    request.setAttribute(OAIXML.TO, "");
    Node msg_node = mr.process(message);
    
    if (msg_node == null) {
      logger.error("returned msg_node from mr is null");
      return null;
    }
    Element resp = (Element)GSXML.getChildByTagName(msg_node, OAIXML.RESPONSE);
    Element coll_list = (Element)GSXML.getChildByTagName(resp, OAIXML.COLLECTION_LIST);
    if (coll_list == null) {
      logger.error("coll_list is null");
      return null;
    }
    //logger.info(GSXML.xmlNodeToString(coll_list));
    NodeList list = coll_list.getElementsByTagName(OAIXML.COLLECTION);
    int length = list.getLength();
    if (length == 0) {
      logger.error("length is 0");
      return null;
    }
    return list;
  }
  /**Exclusively called by doListSets()*/
  private void getSets(Element list_sets_elem, NodeList oai_coll, int start_point, int end_point) {
    for (int i=start_point; i<end_point; i++) {
      String coll_spec = ((Element)oai_coll.item(i)).getAttribute(OAIXML.NAME);
      String coll_name = coll_spec.substring(coll_spec.indexOf(":") + 1);
      Element set = OAIXML.createElement(OAIXML.SET);
      Element set_spec = OAIXML.createElement(OAIXML.SET_SPEC);
      GSXML.setNodeText(set_spec, coll_spec);
      set.appendChild(set_spec);
      Element set_name = OAIXML.createElement(OAIXML.SET_NAME);
      GSXML.setNodeText(set_name, coll_name);
      set.appendChild(set_name);
      list_sets_elem.appendChild(set);
    }
  }
  private int getResumeAfter() {
    Element resume_after = (Element)GSXML.getChildByTagName(oai_config, OAIXML.RESUME_AFTER);
    if(resume_after != null) return Integer.parseInt(GSXML.getNodeText(resume_after));
    return -1;
  }
  private String getRepositoryId() {
    Element ri = (Element)GSXML.getChildByTagName(oai_config, OAIXML.REPOSITORY_ID);
    if (ri != null) { 
      return GSXML.getNodeText(ri);
    }
    return "";
  }
  /** method to compose a set element
   */
  private Element doListSets(Element msg){
    logger.info("");
    // option: resumptionToken
    // exceptions: badArgument, badResumptionToken, noSetHierarchy
    Element list_sets_elem = OAIXML.createElement(OAIXML.LIST_SETS);

    int oai_coll_size = collection_list.getLength(); 
    if (oai_coll_size == 0) {
      return getMessage(list_sets_elem);
    }
    
    Element req = (Element)GSXML.getChildByTagName(msg, GSXML.REQUEST_ELEM);
    if (req == null) {
      logger.error("req is null");
      return null;
    }
    //params list only contains the parameters other than the verb
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    Element param = null;
    int smaller = (oai_coll_size>resume_after)? resume_after : oai_coll_size;
    if (params.getLength() > 1) {
      //Bad argument. Can't be more than one parameters for ListMetadataFormats verb
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    if(params.getLength() == 0) {
      //this is requesting a list of sets in the whole repository
      /** there is no resumeptionToken in the request, we check whether we need
       *  to send out resumeptionToken by comparing the total number of sets in this
       *  repository and the specified value of resumeAfter
       */
      if(resume_after < 0 || oai_coll_size <= resume_after) {
        //send the whole list of records
        //all data are sent on the first request. Therefore there should be
        //no resumeptionToken stored in OAIConfig.xml.
        //As long as the verb is 'ListSets', we ignore the rest of the parameters
        getSets(list_sets_elem, collection_list, 0, oai_coll_size);
        return getMessage(list_sets_elem);
      }
      
      //append required sets to list_sets_elem (may be a complete or incomplete list)
      getSets(list_sets_elem, collection_list, 0, smaller);
      
      if(oai_coll_size > resume_after) {
        //An incomplete list is sent; append a resumptionToken element
        Element token = createResumptionTokenElement(oai_coll_size, 0, resume_after, true);
        //store this token
        OAIXML.addToken(token);
        
        list_sets_elem.appendChild(token);
      }
      
      return getMessage(list_sets_elem);
    } 
    
    // The url should contain only one param called resumptionToken
    // This is requesting a subsequent part of a list, following a previously sent incomplete list
    param = (Element)params.item(0);
    String param_name = param.getAttribute(OAIXML.NAME);
    if (!param_name.equals(OAIXML.RESUMPTION_TOKEN)) {
      //Bad argument
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    //get the token
    String token = param.getAttribute(OAIXML.VALUE);
    //validate the token string (the string has already been decoded in OAIServer, e.g., 
    // replace %3A with ':')
    if(OAIXML.containsToken(token) == false) {
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_RESUMPTION_TOKEN, ""));
    }
    //take out the cursor value, which is the size of previously sent list
    int index = token.indexOf(":");
    int cursor = Integer.parseInt(token.substring(index + 1));
    Element token_elem = null;
    
    // are we sending the final part of a complete list?
    if(cursor + resume_after >= oai_coll_size) {
      //Yes, we are.
      //append required sets to list_sets_elem (list is complete)
      getSets(list_sets_elem, collection_list, cursor, oai_coll_size);
      //An incomplete list is sent; append a resumptionToken element
      token_elem = createResumptionTokenElement(oai_coll_size, cursor, -1, false);
      list_sets_elem.appendChild(token_elem); 
    } else {
      //No, we are not.
      //append required sets to list_sets_elem (list is incomplete)
      getSets(list_sets_elem, collection_list, cursor, cursor + resume_after);
      token_elem = createResumptionTokenElement(oai_coll_size, cursor, cursor + resume_after, true);
      //store this token
      OAIXML.addToken(token_elem);
      list_sets_elem.appendChild(token_elem);                        
    }
    return getMessage(list_sets_elem);
  }  
    private Element createResumptionTokenElement(int total_size, int cursor, int so_far_sent, boolean set_expiration, String metadata_prefix) {
    Element token = OAIXML.createElement(OAIXML.RESUMPTION_TOKEN);
    token.setAttribute(OAIXML.COMPLETE_LIST_SIZE, "" + total_size);
    token.setAttribute(OAIXML.CURSOR, "" + cursor);
    
    if(set_expiration) {
      /** read the resumptionTokenExpiration element in OAIConfig.xml and get the specified time value
       *  Use the time value plus the current system time to get the expiration date string.
       */
	String expiration_date = OAIXML.getTime(System.currentTimeMillis() + OAIXML.getTokenExpiration()); // in milliseconds
      token.setAttribute(OAIXML.EXPIRATION_DATE, expiration_date);
    }
    
    if(so_far_sent > 0) {
      //the format of resumptionToken is not defined by the OAI-PMH and should be
      //considered opaque by the harvester (in other words, strictly follow what the
      //data provider has to offer
      //Here, we make use of the uniqueness of the system time
	String tokenValue = OAIXML.GS3OAI + System.currentTimeMillis() + ":" + so_far_sent;	
	if(!metadata_prefix.equals("")) {
	    tokenValue = tokenValue + ":" + metadata_prefix;
	}
	GSXML.setNodeText(token, tokenValue);
    }
    return token;
  }
    
    private Element createResumptionTokenElement(int total_size, int cursor, int so_far_sent, boolean set_expiration) {
	return createResumptionTokenElement(total_size, cursor, so_far_sent, set_expiration, ""); // empty metadata_prefix
    }

  /** if the param_map contains strings other than those in valid_strs, return false;
   *  otherwise true.
   */
  private boolean isValidParam(HashMap<String, String> param_map, HashSet<String> valid_strs) {
    ArrayList<String> param_list = new ArrayList<String>(param_map.keySet());
    for(int i=0; i<param_list.size(); i++) {
      if (valid_strs.contains(param_list.get(i)) == false) {
        return false;
      }
    }
    return true;
  }
  private Element doListIdentifiers(Element msg) {
    // option: from, until, set, metadataPrefix, resumptionToken
    // exceptions: badArgument, badResumptionToken, cannotDisseminateFormat, noRecordMatch, and noSetHierarchy
    HashSet<String> valid_strs = new HashSet<String>();
    valid_strs.add(OAIXML.FROM);
    valid_strs.add(OAIXML.UNTIL);
    valid_strs.add(OAIXML.SET);
    valid_strs.add(OAIXML.METADATA_PREFIX);
    valid_strs.add(OAIXML.RESUMPTION_TOKEN);
        
    Element list_identifiers = OAIXML.createElement(OAIXML.LIST_IDENTIFIERS);
    Element req = (Element)GSXML.getChildByTagName(msg, GSXML.REQUEST_ELEM);
    if (req == null) {       logger.error("req is null");       return null;     }
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    String coll_name = "";
    String token = "";
    
    HashMap<String, String> param_map = OAIXML.getParamMap(params);    
    if (!isValidParam(param_map, valid_strs)) {
	logger.error("One of the params is invalid");
	return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    } 
    // param keys are valid, but if there are any date params, check they're of the right format
    String from = param_map.get(OAIXML.FROM);
    if(from != null) {	
	Date from_date = OAIXML.getDate(from);
	if(from_date == null) {
	    logger.error("invalid date: " + from);
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	}
    }
    String until = param_map.get(OAIXML.UNTIL);
    if(until != null) {	
	Date until_date = OAIXML.getDate(until);
	if(until_date == null) {
	    logger.error("invalid date: " + until);
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	} 
    }    
    if(from != null && until != null) { // check they are of the same date-time format (granularity)
	if(from.length() != until.length()) {
	    logger.error("The request has different granularities (date-time formats) for the From and Until date parameters.");
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	}
    }

    //ask the message router for a list of oai collections
    //NodeList oai_coll = collection_list; //getOAICollectionList();
    int oai_coll_size = collection_list.getLength();
    if (oai_coll_size == 0) {
      logger.info("returned oai collection list is empty");
      return getMessage(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }
    
    //Now we check if the optional argument 'set' has been specified in the params; if so,
    //whether the specified setSpec is supported by this repository 
    boolean request_set = param_map.containsKey(OAIXML.SET);
    if(request_set == true) {
      boolean set_supported = false;
      String set_spec_str = param_map.get(OAIXML.SET);
      // get the collection name
      //if setSpec is supported by this repository, it must be in the form: site_name:coll_name
      String[] strs = splitSetSpec(set_spec_str);
      coll_name = strs[1];

      for(int i=0; i<oai_coll_size; i++) {
        if(set_spec_str.equals(((Element)collection_list.item(i)).getAttribute(OAIXML.NAME))) {
          set_supported = true;
        }
      }
      if(set_supported == false) {
        logger.error("requested set is not found in this repository");
        return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
      }
    }
    
    //Is there a resumptionToken included which is requesting an incomplete list?
    if(param_map.containsKey(OAIXML.RESUMPTION_TOKEN)) {
        // validate resumptionToken
      token = param_map.get(OAIXML.RESUMPTION_TOKEN);
      logger.info("has resumptionToken" + token);
      if(OAIXML.containsToken(token) == false) {
        return getMessage(OAIXML.createErrorElement(OAIXML.BAD_RESUMPTION_TOKEN, ""));
      }
    }
    
    // Custom test that expects a metadataPrefix comes here at end so that the official params can
    // be tested first for errors and their error responses sent off. Required for OAI validation
    if (!param_map.containsKey(OAIXML.METADATA_PREFIX)) {
      logger.error("contains invalid params or no metadataPrefix");
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }    
    
    //Now that we got a prefix, check and see if it's supported by this repository
    String prefix_value = param_map.get(OAIXML.METADATA_PREFIX);
    if (containsMetadataPrefix(prefix_value) == false) {
      logger.error("requested prefix is not found in OAIConfig.xml");
      return getMessage(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }

    //Now that all validation has been done, I hope, we can send request to the message router
    Element result = null;
    String verb = req.getAttribute(OAIXML.TO); 
    NodeList param_list = req.getElementsByTagName(OAIXML.PARAM);
    ArrayList<Element> retain_param_list = new ArrayList<Element>();
    for (int j=0; j<param_list.getLength(); j++) {
      Element e = OAIXML.duplicateElement(msg.getOwnerDocument(), (Element)param_list.item(j), true);
      retain_param_list.add(e);
    }

    //re-organize the request element
    // reset the 'to' attribute
    if (request_set == false) {
      logger.info("requesting identifiers of all collections");
      for(int i=0; i<oai_coll_size; i++) {
        if(req == null) {
          req = msg.getOwnerDocument().createElement(GSXML.REQUEST_ELEM);
          msg.appendChild(req);
          for (int j=0; j<retain_param_list.size(); j++) {
            req.appendChild(retain_param_list.get(j));
          }
        }
        String full_name = ((Element)collection_list.item(i)).getAttribute(OAIXML.NAME);
        coll_name = full_name.substring(full_name.indexOf(":") + 1);
        req.setAttribute(OAIXML.TO, coll_name + "/" + verb);
        Node n = mr.process(msg);
	Element e = converter.nodeToElement(n);
        result = collectAll(result, e, verb, OAIXML.HEADER);
        
        //clear the content of the old request element
        msg.removeChild(req);
        req = null;
      }      
    } else {
      req.setAttribute(OAIXML.TO, coll_name + "/" + verb);
      Node result_node = mr.process(msg);
      result = converter.nodeToElement(result_node);
    }
    
    if (result == null) {
      logger.info("message router returns null");
      return getMessage(OAIXML.createErrorElement("Internal service returns null", ""));
    }
    Element res = (Element)GSXML.getChildByTagName(result, OAIXML.RESPONSE);
      if(res == null) {
        logger.info("response element in xml_result is null");
        return getMessage(OAIXML.createErrorElement("Internal service returns null", ""));
      }
    NodeList header_list = res.getElementsByTagName(OAIXML.HEADER);
    int num_headers = header_list.getLength();
    if(num_headers == 0) {
      logger.info("message router returns 0 headers.");
      return getMessage(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }      

    //The request coming in does not contain a token, but we have to check the resume_after value and see if we need to issue a resumption token and
    //      save the token as well.
    if (token.equals("") == true) {
      if(resume_after < 0 || num_headers <= resume_after) {
        //send the whole list of records
        return result;
      }
      
      //append required number of records (may be a complete or incomplete list)
      getRecords(list_identifiers, header_list, 0, resume_after);
      //An incomplete list is sent; append a resumptionToken element
      Element token_elem = createResumptionTokenElement(num_headers, 0, resume_after, true);
      //store this token
      OAIXML.addToken(token_elem);
       
      list_identifiers.appendChild(token_elem);
      return getMessage(list_identifiers);
    } 
        
    if (token.equals("") == false) {
      //get an appropriate number of records (partial list) according to the token
      //take out the cursor value, which is the size of previously sent list
      int index = token.indexOf(":");
      int cursor = Integer.parseInt(token.substring(index + 1));
      Element token_elem = null;
      
      // are we sending the final part of a complete list?
      if(cursor + resume_after >= num_headers) {
        //Yes, we are.
        //append required records to list_records (list is complete)
        getRecords(list_identifiers, header_list, cursor, num_headers);
        //An incomplete list is sent; append a resumptionToken element
        token_elem = createResumptionTokenElement(num_headers, cursor, -1, false);
        list_identifiers.appendChild(token_elem);
      } else {
        //No, we are not.
        //append required records to list_records (list is incomplete)
        getRecords(list_identifiers, header_list, cursor, cursor + resume_after);
        token_elem = createResumptionTokenElement(num_headers, cursor, cursor + resume_after, true);
        //store this token
        OAIXML.addToken(token_elem);
        list_identifiers.appendChild(token_elem);
      }      
      
      return getMessage(list_identifiers);
    }//end of if(!token.equals("")) 

    return result;
  }
  private Element doListRecords(Element msg){
    logger.info("");
    // option: from, until, set, metadataPrefix, and resumptionToken
    // exceptions: badArgument, badResumptionToken, cannotDisseminateFormat, noRecordMatch, and noSetHierarchy
    HashSet<String> valid_strs = new HashSet<String>();
    valid_strs.add(OAIXML.FROM);
    valid_strs.add(OAIXML.UNTIL);
    valid_strs.add(OAIXML.SET);
    valid_strs.add(OAIXML.METADATA_PREFIX);
    valid_strs.add(OAIXML.RESUMPTION_TOKEN);

    Element list_records = OAIXML.createElement(OAIXML.LIST_RECORDS);
    Element req = (Element)GSXML.getChildByTagName(msg, GSXML.REQUEST_ELEM);
    if (req == null) {       logger.error("req is null");       return null;     }
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);

    String coll_name = "";
    String token = "";
    
    if(params.getLength() == 0) {
      logger.error("must at least have the metadataPrefix parameter, can't be none");
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    
    HashMap<String, String> param_map = OAIXML.getParamMap(params);    
    if (!isValidParam(param_map, valid_strs)) {
	logger.error("One of the params is invalid");
	return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    // param keys are valid, but if there are any date params, check they're of the right format
    String from = param_map.get(OAIXML.FROM);
    if(from != null) {	
	Date from_date = OAIXML.getDate(from);
	if(from_date == null) {
	    logger.error("invalid date: " + from);
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	}
    }
    String until = param_map.get(OAIXML.UNTIL);
    Date until_date = null;
    if(until != null) {	
	until_date = OAIXML.getDate(until);
	if(until_date == null) {
	    logger.error("invalid date: " + until);
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	} 
    }
    if(from != null && until != null) { // check they are of the same date-time format (granularity)
	if(from.length() != until.length()) {
	    logger.error("The request has different granularities (date-time formats) for the From and Until date parameters.");
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	}
    }
    
    //ask the message router for a list of oai collections
    //NodeList oai_coll = getOAICollectionList();
    int oai_coll_size = collection_list.getLength();
    if (oai_coll_size == 0) {
      logger.info("returned oai collection list is empty");
      return getMessage(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }
    
    //Now we check if the optional argument 'set' has been specified in the params; if so,
    //whether the specified setSpec is supported by this repository 
    boolean request_set = param_map.containsKey(OAIXML.SET);
    if(request_set == true) {
      boolean set_supported = false;
      String set_spec_str = param_map.get(OAIXML.SET);
      // get the collection name
      //if setSpec is supported by this repository, it must be in the form: site_name:coll_name
      String[] strs = splitSetSpec(set_spec_str);
//    name_of_site = strs[0];
      coll_name = strs[1];
      //logger.info("param contains set: "+coll_name);

      for(int i=0; i<oai_coll_size; i++) {
        if(set_spec_str.equals(((Element)collection_list.item(i)).getAttribute(OAIXML.NAME))) {
          set_supported = true;
        }
      }
      if(set_supported == false) {
        logger.error("requested set is not found in this repository");
        return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
      }
    }
    
    //Is there a resumptionToken included which is requesting an incomplete list?
    if(param_map.containsKey(OAIXML.RESUMPTION_TOKEN)) {
        // validate resumptionToken
        //if (the token value is not found in the token xml file) {
        //  return getMessage(OAIXML.createErrorElement(OAIXML.BAD_RESUMPTION_TOKEN, ""));
        //} else {
        //   use the request to get a complete list of records from the message router
      //    and issue the subsequent part of that complete list according to the token.
      //    store a new token if necessary.
        //}      
      token = param_map.get(OAIXML.RESUMPTION_TOKEN);
      logger.info("has resumptionToken: " + token);
      if(OAIXML.containsToken(token) == false) {
        return getMessage(OAIXML.createErrorElement(OAIXML.BAD_RESUMPTION_TOKEN, ""));
      }
    }

    // Moved the additional custom test that mandates the metadataPrefix here, since official
    // errors should be caught first, so that their error responses can be sent off first
    // such that GS2's oaiserver will validate properly.
    if (!param_map.containsKey(OAIXML.METADATA_PREFIX)) {
	if(!token.equals("")) { // resumptiontoken
	    int lastIndex = token.lastIndexOf(":");
	    if(lastIndex != token.indexOf(":")) { // if a meta_prefix is suffixed to the usual token,
		// put that in the map and remove it from the end of the stored token
		String meta_prefix = token.substring(lastIndex+1);
		param_map.put(OAIXML.METADATA_PREFIX, meta_prefix);
		token = token.substring(0, lastIndex);
		param_map.put(OAIXML.RESUMPTION_TOKEN, token);

		// Add to request <param name="metadataPrefix" value="oai_dc"/>
		// need to add metaprefix as param to request, else a request 
		// for subsequent records when working with resumption tokens will fail
		Element paramEl = req.getOwnerDocument().createElement(OAIXML.PARAM);
		paramEl.setAttribute(OAIXML.NAME, OAIXML.METADATA_PREFIX);
		paramEl.setAttribute(OAIXML.VALUE, meta_prefix);
		req.appendChild(paramEl);
	    }
	} else { // no metadata_prefix

	    // it must have a metadataPrefix
	    /** Here I disagree with the OAI specification: even if a resumptionToken is 
	     *  included in the request, the metadataPrefix is a must argument. Otherwise
	     *  how would we know what metadataPrefix the harvester requested in his last request?
	     */
	    logger.error("no metadataPrefix");
	    return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
	}
    }
    
    //Now that we got a prefix, check and see if it's supported by this repository
    String prefix_value = param_map.get(OAIXML.METADATA_PREFIX);
    if (containsMetadataPrefix(prefix_value) == false) {
	logger.error("requested prefix is not found in OAIConfig.xml");
	return getMessage(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    

    //Now that all validation has been done, I hope, we can send request to the message router
    Element result = null;
    String verb = req.getAttribute(OAIXML.TO); 
    NodeList param_list = req.getElementsByTagName(OAIXML.PARAM);
    ArrayList<Element> retain_param_list = new ArrayList<Element>();
    for (int j=0; j<param_list.getLength(); j++) {
      Element e = OAIXML.duplicateElement(msg.getOwnerDocument(), (Element)param_list.item(j), true);
      retain_param_list.add(e);
    }

    //re-organize the request element
    // reset the 'to' attribute
    if (request_set == false) {
      //coll_name could be "", which means it's requesting all records of all collections
      //we send a request to each collection asking for its records
      for(int i=0; i<oai_coll_size; i++) {
        if(req == null) {
          req = msg.getOwnerDocument().createElement(GSXML.REQUEST_ELEM);
          msg.appendChild(req);
          for (int j=0; j<retain_param_list.size(); j++) {
            req.appendChild(retain_param_list.get(j));
          }
        }
        String full_name = ((Element)collection_list.item(i)).getAttribute(OAIXML.NAME);
        coll_name = full_name.substring(full_name.indexOf(":") + 1);
        req.setAttribute(OAIXML.TO, coll_name + "/" + verb);
        //logger.info(GSXML.xmlNodeToString(req));
        Node n = mr.process(msg);
	Element e = converter.nodeToElement(n);
        result = collectAll(result, e, verb, OAIXML.RECORD);

        //clear the content of the old request element
        msg.removeChild(req);
        req = null;
      }      
    } else {
      req.setAttribute(OAIXML.TO, coll_name + "/" + verb);

      Node result_node = mr.process(msg);
      result = converter.nodeToElement(result_node);
    }
    
    if (result == null) {
      logger.info("message router returns null");
      return getMessage(OAIXML.createErrorElement("Internal service returns null", ""));
    }
    Element res = (Element)GSXML.getChildByTagName(result, OAIXML.RESPONSE);
      if(res == null) {
        logger.info("response element in xml_result is null");
        return getMessage(OAIXML.createErrorElement("Internal service returns null", ""));
      }
    NodeList record_list = res.getElementsByTagName(OAIXML.RECORD);
    int num_records = record_list.getLength();
    if(num_records == 0) {
      logger.info("message router returns 0 records.");
      return getMessage(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }      

    //The request coming in does not contain a token, but we have to check the resume_after value and see if we need to issue a resumption token and
    //      save the token as well.
    if (token.equals("") == true) {
      if(resume_after < 0 || num_records <= resume_after) {
        //send the whole list of records
        return result;
      }
      
      //append required number of records (may be a complete or incomplete list)
      getRecords(list_records, record_list, 0, resume_after);
      //An incomplete list is sent; append a resumptionToken element
      Element token_elem = createResumptionTokenElement(num_records, 0, resume_after, true, param_map.get(OAIXML.METADATA_PREFIX));
      //store this token
      OAIXML.addToken(token_elem);
       
      list_records.appendChild(token_elem);
      return getMessage(list_records);
    } 
        
    if (token.equals("") == false) {
      //get an appropriate number of records (partial list) according to the token
      //take out the cursor value, which is the size of previously sent list
      int index = token.indexOf(":");
      int cursor = Integer.parseInt(token.substring(index + 1));
      Element token_elem = null;
      
      // are we sending the final part of a complete list?
      if(cursor + resume_after >= num_records) {
        //Yes, we are.
        //append required records to list_records (list is complete)
        getRecords(list_records, record_list, cursor, num_records);
        //An incomplete list is sent; append a resumptionToken element
	token_elem = createResumptionTokenElement(num_records, cursor, -1, false, param_map.get(OAIXML.METADATA_PREFIX));
	list_records.appendChild(token_elem);

      } else {
        //No, we are not.
        //append required records to list_records (list is incomplete)
        getRecords(list_records, record_list, cursor, cursor + resume_after);
        token_elem = createResumptionTokenElement(num_records, cursor, cursor + resume_after, true, param_map.get(OAIXML.METADATA_PREFIX));
        //store this token
        OAIXML.addToken(token_elem);
        list_records.appendChild(token_elem);
      }      
      
      return getMessage(list_records);
    }//end of if(!token.equals("")) 

    return result;//a backup return
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
    Element res_in_result = (Element)GSXML.getChildByTagName(result, OAIXML.RESPONSE);
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
  /** there are three possible exception conditions: bad argument, idDoesNotExist, and noMetadataFormats.
    * The first one is handled here, and the last two are processed by OAIPMH.
    */
  private Element doListMetadataFormats(Element msg) {
    //if the verb is ListMetadataFormats, there could be only one parameter: identifier
    //, or there is no parameter; otherwise it is an error
    //logger.info("" + this.converter.getString(msg));
    
    Element list_metadata_formats = OAIXML.createElement(OAIXML.LIST_METADATA_FORMATS);

    Element req = (Element)GSXML.getChildByTagName(msg, GSXML.REQUEST_ELEM);
    if (req == null) {       logger.error("");       return null;     }
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    Element param = null;
    if(params.getLength() == 0) {
      //this is requesting metadata formats for the whole repository
      //read the oaiConfig.xml file, return the metadata formats specified there.
      Element format_list = (Element)GSXML.getChildByTagName(oai_config, OAIXML.LIST_METADATA_FORMATS);
      if(format_list == null) {
	logger.error("OAIConfig.xml must contain the supported metadata formats");
	return getMessage(list_metadata_formats);
      }
      NodeList formats = format_list.getElementsByTagName(OAIXML.METADATA_FORMAT);
      for(int i=0; i<formats.getLength(); i++) {
	Element meta_fmt = OAIXML.createElement(OAIXML.METADATA_FORMAT);
	Element first_meta_format = (Element)formats.item(i);
	//the element also contains mappings, but we don't want them
	meta_fmt.appendChild(meta_fmt.getOwnerDocument().importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.METADATA_PREFIX), true));
	meta_fmt.appendChild(meta_fmt.getOwnerDocument().importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.SCHEMA), true));
	meta_fmt.appendChild(meta_fmt.getOwnerDocument().importNode(GSXML.getChildByTagName(first_meta_format, OAIXML.METADATA_NAMESPACE), true));
	list_metadata_formats.appendChild(meta_fmt);
        }
      return getMessage(list_metadata_formats);
      
      
    } 

    if (params.getLength() > 1) {
      //Bad argument. Can't be more than one parameters for ListMetadataFormats verb
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    } 
    
    // This is a request for the metadata of a particular item with an identifier
      /**the request xml is in the form: <request>
       *                                   <param name=.../>
       *                                 </request>
       *And there is a param element and one element only. (No paramList element in between).
       */
      param = (Element)params.item(0);
      String param_name = param.getAttribute(OAIXML.NAME);
      String identifier = "";
      if (!param_name.equals(OAIXML.IDENTIFIER)) {
        //Bad argument
        return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
      } else {
        identifier = param.getAttribute(OAIXML.VALUE);
        // the identifier is in the form: <site_name>:<coll_name>:<OID>
        // so it must contain at least two ':' characters
        String[] strs = identifier.split(":");
        if(strs == null || strs.length < 3) {
          // the OID may also contain ':'
          logger.error("identifier is not in the form site:coll:id" + identifier);
          return getMessage(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
        }
        
        // send request to message router
        // get the names
        strs = splitNames(identifier);
	if(strs == null || strs.length < 3) {
	    logger.error("identifier is not in the form site:coll:id" + identifier);
	    return getMessage(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
	}
        String name_of_site = strs[0];
        String coll_name = strs[1];
        String oid = strs[2];

        //re-organize the request element
        // reset the 'to' attribute
        String verb = req.getAttribute(OAIXML.TO);
        req.setAttribute(OAIXML.TO, coll_name + "/" + verb);
        // reset the identifier element
        param.setAttribute(OAIXML.NAME, OAIXML.OID);
        param.setAttribute(OAIXML.VALUE, oid);

        //Now send the request to the message router to process
	Node result_node = mr.process(msg);
	return converter.nodeToElement(result_node);
      }
  
    
  }


  private void appendParam(Element req, String name, String value) {    
        Element param = req.getOwnerDocument().createElement(OAIXML.PARAM);
        param.setAttribute(OAIXML.NAME, name);
        param.setAttribute(OAIXML.VALUE, value);
        req.appendChild(param);
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
      return getMessage(this.identify_response);
    }
    
    Element identify = OAIXML.createElement(OAIXML.IDENTIFY);
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
    Element earliestDatestamp_elem = OAIXML.createElement(OAIXML.EARLIEST_DATESTAMP);
    GSXML.setNodeText(earliestDatestamp_elem, earliestDatestamp_str);
    identify.appendChild(earliestDatestamp_elem);

    //do the deletedRecord
    copyNamedElementfromConfig(identify, OAIXML.DELETED_RECORD);
    //do the granularity
    copyNamedElementfromConfig(identify, OAIXML.GRANULARITY);
     
    // output the oai identifier
    Element description = OAIXML.createElement(OAIXML.DESCRIPTION);
    identify.appendChild(description);
    Element oaiIdentifier = OAIXML.createOAIIdentifierXML(repository_id, "lucene-jdbm-demo", "ec159e");
    description.appendChild(oaiIdentifier);

    // if there are any oaiInfo metadata, add them in too.
    Element info = (Element)GSXML.getChildByTagName(oai_config, OAIXML.OAI_INFO);
    if (info != null) {
      NodeList meta = GSXML.getChildrenByTagName(info, OAIXML.INFO_METADATA);
      if (meta != null && meta.getLength() > 0) {
	Element gsdl = OAIXML.createGSDLElement();
	description.appendChild(gsdl);
	for (int m = 0; m<meta.getLength(); m++) {
	  copyNode(gsdl, meta.item(m));
	}
	
      }
    }
    this.identify_response = identify;
    return getMessage(identify);
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
  /** split the identifier into <site + collection + OID> as an array 
      It has already been checked that the 'identifier' contains at least two ':'
   */
  private String[] splitNames(String identifier) {
    logger.info(identifier);
    String [] strs = new String[3];
    int first_colon = identifier.indexOf(":");
    if(first_colon == -1) {
	return null;
    }
    strs[0] = identifier.substring(0, first_colon);

    String sr = identifier.substring(first_colon + 1);
    int second_colon = sr.indexOf(":");
    //logger.error(first_colon + "    " + second_colon);
    strs[1] = sr.substring(0, second_colon);
    
    strs[2] = sr.substring(second_colon + 1);
    return strs;
  }
  /** validate if the specified metadata prefix value is supported by the repository
   *  by checking it in the OAIConfig.xml
   */
  private boolean containsMetadataPrefix(String prefix_value) {
    NodeList prefix_list = oai_config.getElementsByTagName(OAIXML.METADATA_PREFIX);
    
    for(int i=0; i<prefix_list.getLength(); i++) {
      if(prefix_value.equals(GSXML.getNodeText((Element)prefix_list.item(i)).trim() )) {
        return true;
      }
    }
    return false;
  }
  private Element doGetRecord(Element msg){
    logger.info("");
    /** arguments:
        identifier: required
        metadataPrefix: required
     *  Exceptions: badArgument; cannotDisseminateFormat; idDoesNotExist
     */ 
    Element get_record = OAIXML.createElement(OAIXML.GET_RECORD);

    HashSet<String> valid_strs = new HashSet<String>();
    valid_strs.add(OAIXML.IDENTIFIER);
    valid_strs.add(OAIXML.METADATA_PREFIX);

    Element req = (Element)GSXML.getChildByTagName(msg, GSXML.REQUEST_ELEM);
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    HashMap<String, String> param_map = OAIXML.getParamMap(params);    
    
    if(!isValidParam(param_map, valid_strs) ||
        params.getLength() == 0 ||
        param_map.containsKey(OAIXML.IDENTIFIER) == false ||
         param_map.containsKey(OAIXML.METADATA_PREFIX) == false ) {
      logger.error("must have the metadataPrefix/identifier parameter.");
      return getMessage(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    
    String prefix = param_map.get(OAIXML.METADATA_PREFIX);
    String identifier = param_map.get(OAIXML.IDENTIFIER);
    
    // verify the metadata prefix
    if (containsMetadataPrefix(prefix) == false) {
      logger.error("requested prefix is not found in OAIConfig.xml");
      return getMessage(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }

    // get the names
    String[] strs = splitNames(identifier);
    if(strs == null || strs.length < 3) {
      logger.error("identifier is not in the form site:coll:id" + identifier);
      return getMessage(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
    }    
    String name_of_site = strs[0];
    String coll_name = strs[1];
    String oid = strs[2];
    
    //re-organize the request element
    // reset the 'to' attribute
    String verb = req.getAttribute(OAIXML.TO);
    req.setAttribute(OAIXML.TO, coll_name + "/" + verb);
    // reset the identifier element
    Element param = GSXML.getNamedElement(req, OAIXML.PARAM, OAIXML.NAME, OAIXML.IDENTIFIER);
    if (param != null) {
      param.setAttribute(OAIXML.NAME, OAIXML.OID);
      param.setAttribute(OAIXML.VALUE, oid);
    }    

    //Now send the request to the message router to process
    Node result_node = mr.process(msg);
    return converter.nodeToElement(result_node);
  }

    // See OAIConfig.xml
    // dynamically works out what the earliestDateStamp is, since it varies by collection
    // returns this time in *milliseconds*.
    protected long getEarliestDateStamp(NodeList oai_coll) {
	//do the earliestDatestamp
	long earliestDatestamp = System.currentTimeMillis();	
	int oai_coll_size = oai_coll.getLength();
	if (oai_coll_size == 0) {
	    logger.info("returned oai collection list is empty. Setting repository earliestDatestamp to be 1970-01-01.");
	    earliestDatestamp = 0;
	}
	// the earliestDatestamp is now stored as a metadata element in the collection's buildConfig.xml file
	// we get the earliestDatestamp among the collections
	for(int i=0; i<oai_coll_size; i++) {
	    long coll_earliestDatestamp = Long.parseLong(((Element)oai_coll.item(i)).getAttribute(OAIXML.EARLIEST_DATESTAMP));
	    earliestDatestamp = (earliestDatestamp > coll_earliestDatestamp)? coll_earliestDatestamp : earliestDatestamp;
	}

	return earliestDatestamp*1000; // converting from seconds to milliseconds
    }
}
