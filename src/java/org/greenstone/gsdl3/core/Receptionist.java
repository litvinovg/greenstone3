package org.greenstone.gsdl3.core;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.action.*;
// XML classes
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 

// other java classes
import java.io.File;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.ArrayList;

import org.apache.log4j.*;

/** the most basic Receptionist, used for interface generation. 
 * Receives requests consisting
 * of an xml representation of cgi args, and returns the page of data. The requests are processed by the appropriate action class
 *
 * @see Action
 */
public class Receptionist implements ModuleInterface {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.Receptionist.class.getName());

    /** the set up variables */
    protected HashMap config_params = null;
    /** container Document to create XML Nodes */
    protected Document doc=null; 
   
    /** a converter class to parse XML and create Docs */
    protected XMLConverter converter=null;

    /** the message router that the Receptionist and Actions will talk to */
    protected ModuleInterface mr=null;

    /** the list of actions */
    protected HashMap action_map=null;
    
    /** the list of params */
    protected GSParams params=null;
    protected Element language_list = null;

    /** the list of interfaces this is based on */
    protected ArrayList base_interfaces = null;
    
    public Receptionist() {
	this.converter = new XMLConverter();
	this.doc = this.converter.newDOM();
	this.action_map= new HashMap();
    }
    
    public void cleanUp() {}
    public void setParams(GSParams params) {
	this.params = params;
    }
    public void setConfigParams(HashMap params) {
	this.config_params = params;
    }
    public HashMap getConfigParams() {
    	return this.config_params;
    }
    /** sets the message router  - it should already be created and 
     * configured before  being passed to the receptionist*/
    public void setMessageRouter(ModuleInterface m) {
       this.mr = m;
    }

    /** configures the receptionist */
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
	
	Document config_doc = this.converter.getDOM(interface_config_file);
	if (config_doc == null) {
	    logger.error(" could not parse interface config file: "+interface_config_file.getPath());
	    return false;
	}
	Element config_elem = config_doc.getDocumentElement();
	String base_interface = config_elem.getAttribute("baseInterface");
	setUpBaseInterface(base_interface);
	setUpInterfaceOptions(config_elem);

	// load up the actions
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
	}

	this.language_list = (Element)GSXML.getChildByTagName(config_elem, "languageList");
	if (language_list == null) {
	    logger.error(" didn't find a language list in the config file!!");
	}

	return true;
    }



    public String process(String xml_in) {

	Node message_node = this.converter.getDOM(xml_in);
	Node page = process(message_node);
	return this.converter.getString(page);
    }


    /** process - produce a page of data in response to a request 
     * if something goes wrong, it returns null - 
     * TODO:  return a suitable message to the user */
    public Node process(Node message_node) {

	Element message = this.converter.nodeToElement(message_node);

	// get the request out of the message - assume that there is only one
	Element request = (Element)GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
	if (request == null) {
	    logger.error(" message had no request!");
	    return null; 
	}
	// check the request type 
	String type = request.getAttribute(GSXML.TYPE_ATT); // returns "" if no att of this name
	if (!type.equals(GSXML.REQUEST_TYPE_PAGE)) { 
	    // now Receptionist forwards non-page requests straight to the MR, and returns the responses
	    logger.error(" request type is not '"+GSXML.REQUEST_TYPE_PAGE+"', but it is '"+type+"', so forwarding the message to the MR!");
	    // process the whole message - mr needs <message> tags, and 
	    // in this case, there may be more than one request in the message
	    return this.mr.process(message);
	}	
	// work out which action to pass to
	String action = request.getAttribute(GSXML.ACTION_ATT);
	if (action.equals("")) {
	    logger.error(" no action specified in the request!");
	    return null;
	}
	
	// find the  appropriate action	
	Action a = (Action)this.action_map.get(action);
	
	String action_name=null;
	if (a==null) { // not in the map yet
	    // try to load a new action
	    try {
		action_name = action.substring(0,1).toUpperCase()+action.substring(1)+"Action";
		Action ac = (Action)Class.forName("org.greenstone.gsdl3.action."+action_name).newInstance();
		ac.setConfigParams(this.config_params);
		ac.setMessageRouter(this.mr);
		ac.configure();
		ac.getActionParameters(this.params);
		this.action_map.put(action, ac);
		a = ac;
	    } catch (Exception e) {
		
		logger.error(" a new action ("+action_name+") was specified and it couldn't be created. Error message:"+e.getMessage());
		return null;
	    }
	}

	// transform the request in some way -- does nothing!
	preProcessRequest(request);
	// set up the page
	Element page = this.doc.createElement(GSXML.PAGE_ELEM);
	page.setAttribute(GSXML.LANG_ATT, request.getAttribute(GSXML.LANG_ATT));
	// just in case these namespaces end up in the page and we want to display the XML
	page.setAttribute("xmlns:gsf","http://www.greenstone.org/greenstone3/schema/ConfigFormat");
	page.setAttribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform");
	
	//logger.info(a+" mesa=" + this.converter.getPrettyString(message));
	// get the page data from the action
	Node action_response = a.process(message);

	boolean response_only=false;
	Element param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list != null) {
	    Element param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, "ro");
	    if (param != null) {
		String value = param.getAttribute("value");
		if (value.equals("1")) {
		    response_only = true;
		}
	    }
	}
	if (response_only) {
	    // only the response from the action is sent back
	    return action_response;
	}
	
	// the request is part of the page
	page.appendChild(GSXML.duplicateWithNewName(this.doc, request, GSXML.PAGE_REQUEST_ELEM, true));
	// add the response too
	Element page_response = GSXML.duplicateWithNewName(this.doc, (Element)GSXML.getChildByTagName(action_response, GSXML.RESPONSE_ELEM), GSXML.PAGE_RESPONSE_ELEM, true);
	page.appendChild(page_response);

	//logger.info(" raw page="+this.converter.getString(page));
	// transform the result in some way
	//Element resulting_page = postProcessPage(page);

	Node resulting_page = postProcessPage(page);

	logger.debug("receptionist returned response");
	logger.error("receptionist returned response");
	logger.debug(this.converter.getString(resulting_page));
	logger.error(this.converter.getString(resulting_page));
//    logger.info("receptionist returned response");
//    logger.info(this.converter.getString(resulting_page));

	return resulting_page;
	
    }

    protected boolean setUpBaseInterface(String base_interface) {
	if (base_interface== null || base_interface.equals("")) {
	    // there was no base interface, the list remains null
	    return true;
	}
	// foreach base interface
	while (!base_interface.equals("")) {
	    // find the base interface config file
	    File base_interface_config_file = new File(GSFile.interfaceConfigFile(GSFile.interfaceHome(GlobalProperties.getGSDL3Home(), base_interface)));
	    if (!base_interface_config_file.exists()) {
		logger.error(" base interface config file: "+base_interface_config_file.getPath()+" not found!");
		return false;
	    }
	    // the interface name is valid, add it to the list
	    if (base_interfaces == null) {
		base_interfaces = new ArrayList();
	    }
	    base_interfaces.add(base_interface);
	    // now see if this has a base interface
	    Document config_doc = this.converter.getDOM(base_interface_config_file);
	    if (config_doc == null) {
		logger.error(" could not parse base interface config file: "+base_interface_config_file.getPath());
		return false;
	    }
	    Element config_elem = config_doc.getDocumentElement();
	    base_interface = config_elem.getAttribute("baseInterface");
	}
	return true;
    }

    protected boolean setUpInterfaceOptions(Element config_elem) {
	Element option_list = (Element)GSXML.getChildByTagName(config_elem, "optionList");
	if (option_list != null) {
	    logger.debug("found an interface optionList");
	    // we set any options in the config params
	    NodeList options = option_list.getElementsByTagName("option");
	    for (int i=0; i<options.getLength(); i++) {
		Element option = (Element)options.item(i);
		String name = option.getAttribute(GSXML.NAME_ATT);
		String value = option.getAttribute(GSXML.VALUE_ATT);
		logger.debug("option: "+name+", "+value);
		if (!name.equals("") && !value.equals("")) {
		    this.config_params.put(name, value);
		}
	    }
	}

	return true;
    }

    protected void preProcessRequest(Element request) {
	return;
    }

    protected Node postProcessPage(Element page) {
	return page;
    }
    

}
