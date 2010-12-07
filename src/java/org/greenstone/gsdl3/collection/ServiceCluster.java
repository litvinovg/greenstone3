/*
 *    ServiceCluster.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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
// leave the package name as is for now - should be changed to something better
// cluster? groups?
package org.greenstone.gsdl3.collection;


import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.core.*;
import org.greenstone.gsdl3.service.*;

// java XML classes we're using
import org.w3c.dom.Document; 
import org.w3c.dom.Node; 
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList; 

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.*;

/* ServiceCluster - a groups of services that are related in some way
 * Implements ModuleInterface. Contains a list of services provided by the cluster, along with metadata about the cluster itself.
 * a collection is a special type of cluster
 *  @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 *  @version $Revision$
 *  @see ModuleInterface
 */
public class ServiceCluster  
    implements ModuleInterface {

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.collection.ServiceCluster.class.getName()); 

    protected static final String CONFIG_ENCODING = "utf-8";

    protected static final String DEFAULT_LANG = "en"; // hack for now, should be read from the coll cfg file? or site cfg file for cluster
    
    /** base directory for the site that this cluster belongs to*/
    protected String site_home = null;
    /** http address of the site that this cluster belongs to */
    protected String site_http_address = null;
    /** The name of the cluster - for a collection, this is the collection name*/
    protected String cluster_name = null;
    /** collection type : mg or mgpp */
    protected String col_type = "";

    /** a reference to the message router */
    protected MessageRouter router = null;
    /** The map of services.
     *
     * Maps Services to ServiceRack objects
     * @see ServiceRack
     *
     */
    protected HashMap service_map=null;
    /** maps pseudo service names to real service names - needed if we have two services with the same name for one collection */
    protected HashMap service_name_map=null;
    
    /** XML converter for String to DOM and vice versa */
    protected XMLConverter converter=null;

    /** container doc for description elements */
    protected Document doc = null;
    /** list of services */
    protected Element service_list = null;
    /** list of metadata - all metadata, regardless of language goes in here */
    protected Element metadata_list = null;
    /** language specific stuff */
    //protected Element lang_specific_metadata_list = null;
    protected Element display_item_list = null;
    /** the element that will have any descriptions passed back in */
    protected Element description = null;

    /** list of plugin */
    protected Element plugin_item_list = null;
    
    public void setSiteHome(String home) {
	this.site_home = home;
    }

    public void setSiteAddress(String address) {
	this.site_http_address = address;
    }

    public void cleanUp() {
	Iterator i = this.service_map.values().iterator();
	while (i.hasNext()) {
	    ServiceRack s = (ServiceRack)i.next();
	    s.cleanUp();
	}
    }
    public void setClusterName(String name) {
	this.cluster_name = name;	
	this.description.setAttribute(GSXML.NAME_ATT, name);
    }

    public void setMessageRouter(MessageRouter m) {
	this.router = m;
    }

    public ServiceCluster() {
	this.service_map = new HashMap();
	this.service_name_map = new HashMap();
	this.converter = new XMLConverter();
	this.doc = this.converter.newDOM();
	this.description = this.doc.createElement(GSXML.CLUSTER_ELEM);
	this.display_item_list = this.doc.createElement(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER);
	this.metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	this.plugin_item_list = this.doc.createElement(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
    }

    /**
     * Configures the cluster.
     *
     * gsdlHome and clusterName must be set before configure is called.
     *
     * reads the site configuration file, and configures itself
     * this calls configure(Element) with the XML element node from the config
     * file.
     * configure(Element) should be used if the config file has already been 
     * parsed.  This method will work with any subclass.
     * 
     * @return true if configure successful, false otherwise.
     */
    public boolean configure() {

	if (this.site_home == null || this.cluster_name== null) {
	    logger.error("site_home and cluster_name must be set before configure called!");
	    return false;
	}
	logger.info("configuring service cluster");
	// read the site configuration file
	File config_file = new File(GSFile.siteConfigFile(this.site_home));

	if (!config_file.exists()) {
	    logger.error("couldn't configure cluster: "+this.cluster_name +", "+config_file+" does not exist");
	    return false;
	}

	Document doc = this.converter.getDOM(config_file, CONFIG_ENCODING);
	if (doc == null) {
	    logger.error("couldn't parse config file "+config_file.getPath());
	    return false;
	}
	
	// get the appropriate service cluster element
	Element cluster_list = (Element)GSXML.getChildByTagName(doc.getDocumentElement(), GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER);
	Element sc = GSXML.getNamedElement(cluster_list, GSXML.CLUSTER_ELEM,
					   GSXML.NAME_ATT, this.cluster_name);
	
	return this.configure(sc);
    }
    
    
    public boolean configure(Element service_cluster_info) {
    
	// get the metadata - for now just add it to the list
	Element meta_list = (Element) GSXML.getChildByTagName(service_cluster_info, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	if (meta_list !=null) {
	    if (!addMetadata(meta_list)) {
		
		logger.error(" couldn't configure the metadata");
	    }
	}
	
	// get the display info
	Element display_list = (Element) GSXML.getChildByTagName(service_cluster_info, GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER);
	if (display_list !=null) {
	    if (!addDisplayItems(display_list)) {
		
		logger.error("couldn't configure the display items");
	    }
	}
	
	//get the plugin info
	Element import_list = (Element) GSXML.getChildByTagName(service_cluster_info, GSXML.IMPORT_ELEM);
	if (import_list != null)
	{
		Element plugin_list = (Element) GSXML.getChildByTagName(service_cluster_info, GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
		if (plugin_list !=null) {
	   	 	if (!addPlugins(plugin_list)) {
		
				logger.error("couldn't configure the plugins");
	    		}
		}
	}	
	
	// do the service racks
	// empty the service map in case this is a reconfigure
	clearServices();
	Element service_rack_list = (Element)GSXML.getChildByTagName(service_cluster_info, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
	if (service_rack_list == null) {
	    // is this an error? could you ever have a service cluster
	    // without service racks???
	    logger.error("cluster has no service racks!!");
	} else {
	    
	    if (!configureServiceRackList(service_rack_list, null)) {
		logger.error("couldn't configure the  service racks!!");
		return false;
	    }
	}

	return true;
    }
    
    /** adds metadata from a metadataList into the metadata_list xml 
     */
    protected boolean addMetadata(Element metadata_list) {
	if (metadata_list == null) return false;
	NodeList metanodes = metadata_list.getElementsByTagName(GSXML.METADATA_ELEM);
	if (metanodes.getLength()>0) { 	
	    for(int k=0; k<metanodes.getLength(); k++) {
		this.metadata_list.appendChild(this.doc.importNode(metanodes.item(k), true));
	    }
	}
	
	return true;
    }

    protected boolean addDisplayItems(Element display_list) {
	
	if (display_list==null) return false;
	NodeList displaynodes = display_list.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
	if (displaynodes.getLength()>0) { 	
	    for(int k=0; k<displaynodes.getLength(); k++) {
		Element d = (Element) displaynodes.item(k);
		String lang = d.getAttribute(GSXML.LANG_ATT);
		if (lang==null||lang.equals("")) {
		    //set the lang to teh default
		    d.setAttribute(GSXML.LANG_ATT, DEFAULT_LANG);
		}
		String name = d.getAttribute(GSXML.NAME_ATT);
		Element this_item = GSXML.getNamedElement(this.display_item_list, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, name);
		if (this_item==null) {
		    this_item = this.doc.createElement(GSXML.DISPLAY_TEXT_ELEM);
		    this_item.setAttribute(GSXML.NAME_ATT, name);
		    this.display_item_list.appendChild(this_item);
		}
		
		this_item.appendChild(this.doc.importNode(d, true));
	    }
	}
	
	return true;
    }
    
    protected boolean addPlugins(Element plugin_list) {
    	if (plugin_list == null) return false;
	NodeList pluginNodes = plugin_list.getElementsByTagName(GSXML.PLUGIN_ELEM);
	if (pluginNodes.getLength() > 0) {
		for (int k = 0; k < pluginNodes.getLength(); k++)
		{
			this.plugin_item_list.appendChild(this.doc.importNode(pluginNodes.item(k), true));
		}
	}
	
	return true;
    }

    
  protected void clearServices() {
    service_map.clear();
    this.service_list = this.doc.createElement(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER);
  }
    /** creates and configures all the services - extra_info is some more xml 
that is passed to teh service  - eg used for coll config files for Collection
    */
    protected boolean configureServiceRackList(Element service_rack_list, 
					   Element extra_info) {

	// create all the services
	NodeList nodes = service_rack_list.getElementsByTagName(GSXML.SERVICE_CLASS_ELEM);
	if (nodes.getLength()==0) {
	    logger.error("ServiceCluster configuration error: cluster "+this.cluster_name+" has no service modules!");
	    return false;
	}

	for(int i=0; i<nodes.getLength(); i++) {
	
	    // the xml request to send to the serviceRack to query what
	    // services it provides
	    Element message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	    Element request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", "", "");
	    message.appendChild(request);

	    Element n = (Element)nodes.item(i);
	    String servicetype = n.getAttribute(GSXML.NAME_ATT);
	    
	    ServiceRack s = null;
	    
	    try {
		// try for a default service in standard package
		s = (ServiceRack)Class.forName("org.greenstone.gsdl3.service."+servicetype).newInstance();
		
	    } catch (Exception e) {}
	    if (s == null) {
		try {
		    // name as is, in case package is already specified
		    s = (ServiceRack)Class.forName(servicetype).newInstance();
		} catch (Exception e) {}
	    }

	    if (s == null) {
		logger.error("Couldn't get an instance of class "+servicetype+", or org.greenstone.gsdl3.service."+servicetype);
		continue;
	    }

	    
	    s.setSiteHome(this.site_home);
	    s.setSiteAddress(this.site_http_address);
	    s.setClusterName(this.cluster_name);
	    s.setMessageRouter(this.router);
	    // pass the xml node to the service for configuration
	    if (s.configure(n, extra_info)) {
		
		// find out the supported service types for this service module
		Node types = s.process(message);
		NodeList typenodes = ((Element)types).getElementsByTagName(GSXML.SERVICE_ELEM);	   
		
		for (int j=0; j<typenodes.getLength();j++) {
		    String service = ((Element) typenodes.item(j)).getAttribute(GSXML.NAME_ATT);	   
		    if (service_map.get(service)!=null) {
			char extra = '0';
			String new_service = service+extra;	   
			
			while (service_map.get(new_service)!=null) {
			    extra++;
			    new_service = service+extra;
			}
			this.service_name_map.put(new_service, service);
			service=new_service;
			((Element) typenodes.item(j)).setAttribute(GSXML.NAME_ATT, service);
		    }
		    this.service_map.put(service, s);
		    // also add info to the ServiceInfo XML element
		    this.service_list.appendChild(this.doc.importNode(typenodes.item(j), true));
		}
	    }
	} 
	
	return true;
	
	
    }


    /**
     * Process an XML document - uses Strings 
     *  just calls process(Node).
     *
     * @param in the Document to process - a string
     * @return the resultant document as a string - contains any error messages
     * @see String
     */
    public String process(String in) {

	Document doc = this.converter.getDOM(in);
	
	Node res = process(doc);
	return this.converter.getString(res);
	
    }
    
    /** process XML as Node
     *
     */
    public Node process(Node message_node) {

	Element message = this.converter.nodeToElement(message_node);

	NodeList requests = message.getElementsByTagName(GSXML.REQUEST_ELEM);
	Document mess_doc = message.getOwnerDocument();
	Element mainResult = this.doc.createElement(GSXML.MESSAGE_ELEM);
	if (requests.getLength()==0) {
	    logger.error("no requests for cluster:"+this.cluster_name);
	    // no requests
	    return mainResult; // for now
	}
	for (int i=0; i<requests.getLength(); i++) {
	    Element request = (Element)requests.item(i);
	    String to = request.getAttribute(GSXML.TO_ATT);

	    // the cluster name should be first, check, then remove
	    String clustername = GSPath.getFirstLink(to);
	    if (!clustername.equals(this.cluster_name)){ 
		logger.error("cluster name wrong! was "+clustername+" should have been "+this.cluster_name);
		continue; // ignore this request
	    }
	    to = GSPath.removeFirstLink(to);
	    request.setAttribute(GSXML.TO_ATT, to);
	   
	    if (to.equals("")) { // this command is for me
		Element response = processMessage(request);
		mainResult.appendChild(response);
		
	    } else { // the request is for one of my services
		String service = GSPath.getFirstLink(to);
		
		if (!this.service_map.containsKey(service)) {
		    logger.error("non-existant service, "+service+", specified!");
		    continue;
		}
		String real_service = service;
		if (this.service_name_map.containsKey(service)) {
		    real_service = (String)this.service_name_map.get(service);
		    // need to change the to att in the request - give the real service name
		    to = request.getAttribute(GSXML.TO_ATT);
		    String old_to = to;
		    to = GSPath.replaceFirstLink(to, real_service);
		    request.setAttribute(GSXML.TO_ATT, to);
		}
		// have to pass the request to the service
		Element single_message = mess_doc.createElement(GSXML.MESSAGE_ELEM);
		single_message.appendChild(request);
		Node response_message = ((ModuleInterface)this.service_map.get(service)).process(single_message);
		if (response_message != null) {
		    Element response = (Element) GSXML.getChildByTagName(response_message, GSXML.RESPONSE_ELEM);
		    String from = response.getAttribute(GSXML.FROM_ATT);
		    if (!real_service.equals(service)) {
			// replace the real service name with the pseudo service name
			from = GSPath.replaceFirstLink(from, service);
			// also need to do it in the service itself
			// shoudl this be done here??
			Element service_elem = (Element) GSXML.getChildByTagName(response, GSXML.SERVICE_ELEM);
			if (service_elem!= null) {
			    service_elem.setAttribute(GSXML.NAME_ATT, service);
			}
		    }
		    from = GSPath.prependLink(from, this.cluster_name);
		    response.setAttribute(GSXML.FROM_ATT, from);
		    mainResult.appendChild(this.doc.importNode(response, true));
		}
		  
	    } // else
		
	   
	} // for each request
	return mainResult;
    }

    /** handles requests made to the ServiceCluster itself 
     *
     * @param req - the request Element- <request>
     * @return the result Element - should be <response>
     */
    protected Element processMessage(Element request) {

	Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
	response.setAttribute(GSXML.FROM_ATT, this.cluster_name);
	String type = request.getAttribute(GSXML.TYPE_ATT);
	String lang = request.getAttribute(GSXML.LANG_ATT);
	response.setAttribute(GSXML.TYPE_ATT, type);
	
	if (type.equals(GSXML.REQUEST_TYPE_DESCRIBE)) {
	    // create the collection element
	    Element description = (Element)this.description.cloneNode(false);
	    // set collection type : mg or mgpp
	    description.setAttribute(GSXML.TYPE_ATT, col_type);

	    response.appendChild(description);
	    // check the param list
	    Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	    if (param_list == null) {	    	    
		addAllDisplayInfo(description, lang);				
		description.appendChild(this.service_list);
		description.appendChild(this.metadata_list);
		description.appendChild(this.plugin_item_list);
		return response;
	    }
	    
	    // go through the param list and see what components are wanted
	    NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
	    for (int i=0; i<params.getLength(); i++) {	    
		
		Element param = (Element)params.item(i);				
		// Identify the structure information desired
		if (param.getAttribute(GSXML.NAME_ATT).equals(GSXML.SUBSET_PARAM)) {
		    String info = param.getAttribute(GSXML.VALUE_ATT);
		    if (info.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(this.service_list);
		    } else if (info.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(metadata_list);
		    } else if (info.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
			addAllDisplayInfo(description, lang);
		    } else if (info.equals(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER)) {
		    	description.appendChild(plugin_item_list);
		    }
		}
	    }
	    return response;
	}

    if (type.equals(GSXML.REQUEST_TYPE_FORMAT_STRING)) {
        logger.error("Received format string request");
        String service = request.getAttribute("service");
        logger.error("Service is " + service);
        String classifier = null;
        if(service.equals("ClassifierBrowse"))
        {
            classifier = request.getAttribute("classifier");
            logger.error("Classifier is " + classifier);
        }
        Element format_element = (Element) GSXML.getChildByTagName(request, GSXML.FORMAT_STRING_ELEM);
        String format_string = GSXML.getNodeText(format_element);
        logger.error("Format string: " + format_string);
        logger.error("Config file location = " + GSFile.collectionConfigFile(this.site_home, this.cluster_name));
	}
	if (type.equals(GSXML.REQUEST_TYPE_SYSTEM)) {
	    response = processSystemRequest(request);
	} else { // unknown type
	    logger.error("cant handle request of type "+ type);
	    
	}
	return response;
    }

    protected Element processSystemRequest(Element request) {

	Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
	response.setAttribute(GSXML.FROM_ATT, this.cluster_name);
	response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_SYSTEM);

	// a list of system requests - should put any error messages
	// or success messages into response
	NodeList commands = request.getElementsByTagName(GSXML.SYSTEM_ELEM);
	String message=null;	
	for (int i=0; i<commands.getLength(); i++) {
	    // all the commands should be Elements
	    Element elem = (Element)commands.item(i);
	    String action = elem.getAttribute(GSXML.TYPE_ATT);
	    if (action.equals(GSXML.SYSTEM_TYPE_CONFIGURE)) {
		String subset = elem.getAttribute(GSXML.SYSTEM_SUBSET_ATT);
		if (subset.equals("")) {
		    // need to reconfigure the service cluster
		    
		    if (this.configure()) {
			Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, this.cluster_name + " reconfigured");
			response.appendChild(s);
			
		    } else {
			Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, this.cluster_name + " could not be reconfigured");
			response.appendChild(s); 
		    }
		} else if (this.configureSubset(subset)) {
		    Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, this.cluster_name + " "+subset+" reconfigured");
		    response.appendChild(s);
		} else {
		    Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, this.cluster_name + " "+subset + " could not be reconfigured");
		    response.appendChild(s); 
		}
		continue;
	    } // configure action
	    
	    String module_name = elem.getAttribute(GSXML.SYSTEM_MODULE_NAME_ATT);
	    String module_type = elem.getAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT);
	    if (action.equals(GSXML.SYSTEM_TYPE_ACTIVATE)) {
		Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, "activate action not yet implemented - does it even make sense in this context??");
		response.appendChild(s);
	    } else if (action.equals(GSXML.SYSTEM_TYPE_DEACTIVATE)) {
		if (module_type.equals(GSXML.SERVICE_ELEM)) {
		    // deactivate the service
		    // remove from service_map
		    this.service_map.remove(module_name);
		    Element service_elem = GSXML.getNamedElement(this.service_list, GSXML.SERVICE_ELEM, GSXML.NAME_ATT, module_name);
		    service_list.removeChild(service_elem);		    
		    message = module_type+": "+module_name+" deactivated";
		} else {
		    message = "can't deactivate "+module_type+" type modules!";}
		Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, message);
		response.appendChild(s);
	    } else {
		logger.error("cant process system request, action "+action);
		continue;
	    }
	} // for each command
	return response;
    }

    /** 
     * do a configure on only part of the collection
     */
    protected boolean configureSubset(String subset) {
	
	File configFile = new File(GSFile.siteConfigFile(this.site_home));
	if (!configFile.exists() ) {
	    logger.error("site config file: "+configFile.getPath()+" not found!");
	    // wont be able to do any of the requests
	    return false;
	    
	}
	
	Document site_config_doc  = this.converter.getDOM(configFile);
	if (site_config_doc == null) {
	    logger.error("could not read in site config file: "+configFile.getPath());
	    return false;
	}
	
	Element site_config_elem = site_config_doc.getDocumentElement();
	Element cluster_config_elem = GSXML.getNamedElement((Element)GSXML.getChildByTagName(site_config_elem, GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER), GSXML.CLUSTER_ELEM, GSXML.NAME_ATT, this.cluster_name);
	if (cluster_config_elem == null) {
	    logger.error("site config file: "+configFile.getPath()+" has no element for cluster "+this.cluster_name);
	    // wont be able to do any of teh requests
	    return false;
	    
	}
	if (subset.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
	    Element service_rack_list = (Element)GSXML.getChildByTagName(cluster_config_elem, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
	    clearServices();
	    return configureServiceRackList(service_rack_list, null);
	} else if (subset.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER)) {
	    this.metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	    Element metadata_list = (Element)GSXML.getChildByTagName(cluster_config_elem, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	    return addMetadata(metadata_list);
	} else if (subset.equals(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER)) {
	    this.plugin_item_list = this.doc.createElement(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
	    Element import_list = (Element)GSXML.getChildByTagName(cluster_config_elem,GSXML.IMPORT_ELEM);
	    if (import_list != null)
	    {
	    	Element plugin_item_list = (Element)GSXML.getChildByTagName(cluster_config_elem,GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
	    	return addPlugins(plugin_item_list);
            }
	    else
	    	return false;
	} else {
	    logger.error("cannot process system request, configure "+subset);
	    return false;
	} 

    } 


    protected boolean addAllDisplayInfo(Element description, String lang) {
	
	NodeList items = this.display_item_list.getChildNodes();
	for (int i=0; i<items.getLength(); i++) { // for each key
	    Element m = (Element) items.item(i);
	    // find the child with the correct language
	    Element new_m = GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, lang);
	    if (new_m==null && lang != DEFAULT_LANG) {
		// use the default lang
		new_m = GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, DEFAULT_LANG);
	    }
	    if (new_m==null) {
		// just get the first one
		new_m = (Element)GSXML.getChildByTagName(m, GSXML.DISPLAY_TEXT_ELEM);
	    }
	    description.appendChild(new_m.cloneNode(true));
	}
	return true;
	
    }

    
    protected Element getDisplayTextElement(String key, String lang) {
	
	Element this_item = GSXML.getNamedElement(this.display_item_list, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, key);
	if (this_item == null) {
	    return null;
	}

	Element this_lang = GSXML.getNamedElement(this_item, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, lang);
	if (this_lang == null && lang != DEFAULT_LANG) {
	    // try the default
	    this_lang = GSXML.getNamedElement(this_item, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, DEFAULT_LANG);
	}
	if (this_lang == null) {
	    // just return the first one
	    return GSXML.getFirstElementChild(this_item);//(Element)this_item.getFirstChild().cloneNode(true);
	}
	return (Element)this_lang.cloneNode(true);
	
    }
    public HashMap getServiceMap() {
    	return service_map;
    }
}






