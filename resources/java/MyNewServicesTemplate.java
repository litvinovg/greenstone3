/*
 * MyNewServicesTemplate.java - a dummy class showing how to create new 
 * services for Greenstone3
 *
 * This class has two dummy services: TextQuery and MyDifferentService
 */

// This file needs to be put in org/greenstone/gsdl3/service
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;

import org.apache.log4j.*;

// change the class name (and the filename) to something more appropriate
public class MyNewServicesTemplate 
    extends ServiceRack {

    // add in a logger for error messages
    static Logger logger = Logger.getLogger("MyNewServicesTemplate");

    // the new service names
    protected static final String QUERY_SERVICE = "TextQuery";
    protected static final String DIFFERENT_SERVICE = "MyDifferentService";

    // initialize any custom variables
    public MyNewServicesTemplate() {

    }

    // clean up anything that we need to 
    public void cleanUp() {
	super.cleanUp();
    }
    
    // Configure the class based in info in buildConfig.xml and collectionConfig.xml
    // info is the <serviceRack name="MyNewServicesTemplate"/> element from
    // buildConfig.xml, and extra_info is the whole collectionConfig.xml file
    // in case its needed
    public boolean configure(Element info, Element extra_info) {

	if (!super.configure(info, extra_info)) {
	    return false;
	}

	logger.info("Configuring MyNewServicesTemplate...");

	// set up short_service_info - this currently is a list of services, 
	// with their names and service types
	// we have two services, a new textquery, and a new one of a new type
	Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE);
	this.short_service_info.appendChild(tq_service);

	Element diff_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	diff_service.setAttribute(GSXML.TYPE_ATT, "xxx");
	diff_service.setAttribute(GSXML.NAME_ATT, DIFFERENT_SERVICE);
	this.short_service_info.appendChild(diff_service);


	// Extract any relevant information from info and extra_info
	// This can be used to set up variables.

	// If there is any formatting information, add it in to format_info_map
	
	// Do this for all services as appropriate
	Element format = null; // find it from info/extra_info
	if (format != null) {
	    this.format_info_map.put(QUERY_SERVICE, this.doc.importNode(format, true));
	}

	return true;
	
    }

    // get the desription of a service. Could include parameter lists, displayText
    protected Element getServiceDescription(String service, String lang, String subset) {

	// check that we have been asked for the right service
	if (!service.equals(QUERY_SERVICE) && !service.equals(DIFFERENT_SERVICE)) {
	    return null;
	}

	if (service.equals(QUERY_SERVICE)) {
	    Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
	    tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE);
	    if (subset==null || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
		// add in any <displayText> elements
		// name, for example - get from properties file
		tq_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(QUERY_SERVICE+".name", lang) ));
	    }
	
	    if (subset==null || subset.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) {
		// add in a param list if this service has parameters
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		tq_service.appendChild(param_list);
		// create any params and append to param_list
	    }
	    return tq_service;
	} 

	if (service.equals(DIFFERENT_SERVICE)) {
	    Element diff_service = this.doc.createElement(GSXML.SERVICE_ELEM);
	    diff_service.setAttribute(GSXML.TYPE_ATT, "xxx");
	    diff_service.setAttribute(GSXML.NAME_ATT, DIFFERENT_SERVICE);
	    if (subset==null || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
		// add in any <displayText> elements
		// name, for example - get from properties file
		diff_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(DIFFERENT_SERVICE+".name", lang) ));
	    }
	
	    if (subset==null || subset.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) {
		// add in a param list if this service has parameters
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		diff_service.appendChild(param_list);
		// create any params and append to param_list
	    }

	    return diff_service;
	} 
	
	// not a valid service for this class
	return null;
	


    }

    /** This is the method that actually handles the TextQuery Service */
    protected Element processTextQuery(Element request) {

	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	// fill in the rest
	return result;
    }

    /** This is the method that actually handles the MyDifferentService service */
    protected Element processMyDifferentService(Element request) {

	Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, DIFFERENT_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	// fill in the rest
	return result;
    }
}



