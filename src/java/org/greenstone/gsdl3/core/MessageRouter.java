/*
 *    MessageRouter.java
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
package org.greenstone.gsdl3.core;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.service.*;
import org.greenstone.gsdl3.comms.*;
import org.greenstone.gsdl3.collection.*;

// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;

// other java classes
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.*;

import org.apache.commons.lang3.StringUtils;

/**
 * The hub of a Greenstone system.
 *
 * Accepts XML requests (via process method of ModuleInterface) and routes them
 * to the appropriate collection or service or external entity.
 *
 * contains a map of module objects - may be services, collections, comms
 * objects talking to other MessageRouters etc.
 *
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 * @see ModuleInterface
 * @see Collection
 * @see ServiceRack
 * @see Communicator
 *
 * Since some service classes are moved into a separate directory in order for
 * them to be checked out from a different repository, we modify the
 * configureServices method to search some of the classes in other place if they
 * are not found in the service directory.
 */
public class MessageRouter implements  ModuleInterface {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.MessageRouter.class.getName());
  
  /** the (directory) name of the site */
  protected String site_name = null;
  /** site home - the home directory for the site */
  protected String site_home=null;
  /** the http address for this site */
  protected String site_http_address=null;
  
  
  protected String library_name = null;

  /** map of names to Module objects */
  protected HashMap module_map=null;
  
  /** container Document to create XML Nodes */
  protected Document doc=null;
  /** the full description of this site */
  
  // should these things be separated into local and remote??
  
  /** the original xml config element */
  public Element config_info = null;
  
  /** list of collections that can be reached */
  protected Element collection_list = null;
  /** list of collections that are loaded but are private */
  protected Element private_collection_list = null;
    
       
  /** list of collections that are public and OAI-supportive */
  protected Element oai_collection_list = null;

  /** list of service clusters that can be reached */
  protected Element cluster_list = null;
  /** list of single services that can be reached */
  protected Element service_list = null;
  /** list of sites that can be reached */
  protected Element site_list = null;
  /** list of metadata for the site */
  protected Element metadata_list = null;
  
  
  /** a converter class to parse XML and create Docs */
  protected XMLConverter converter=null;
  
  //***************************************************************
  // public methods
  //***************************************************************
  
  /** constructor */
  public MessageRouter() {
    this.converter = new XMLConverter();
    this.doc = this.converter.newDOM();
  }
  
  public void cleanUp() {
    cleanUpModuleMapEntire();
  }
  
  /** site_name must be set before configure is called */
  public void setSiteName(String site_name) {
    this.site_name = site_name;
  }
  public String getSiteName() {
    return this.site_name;
  }
  
  /** library_name must be set before configure is called */
  public void setLibraryName(String library_name) {
    this.library_name = library_name;
  }
  public String getLibraryName() {
    return this.library_name;
  }

  /**
   * configures the system
   *
   * looks in site_home/collect for collections, reads config file
   * site_home/siteConfig.xml
   *
   */
  public boolean configure() {
    
    logger.info("configuring the Message Router");
    
    if (this.site_name==null) {
      logger.error(" You must set site_name before calling configure");
      return false;
    }
    this.site_home = GSFile.siteHome(GlobalProperties.getGSDL3Home(), this.site_name);
    this.site_http_address = GlobalProperties.getGSDL3WebAddress()+"/sites/"+this.site_name;
    
    // are we behind a firewall?? - is there a better place to set up the proxy?
    String host = GlobalProperties.getProperty("proxy.host");
    String port = GlobalProperties.getProperty("proxy.port");
    final String user = GlobalProperties.getProperty("proxy.user");
    final String passwd = GlobalProperties.getProperty("proxy.password");
    
    if (host != null && !host.equals("") && port !=null && !port.equals("")) {
      System.setProperty("http.proxyType", "4");
      System.setProperty("http.proxyHost", host);
      System.setProperty("http.proxyPort", port);
      System.setProperty("http.proxySet", "true");
      // have we got a user/password?
      if (user != null && !user.equals("") && passwd != null && !passwd.equals("")) {
        try {
          // set up the authenticator
          Authenticator.setDefault(new Authenticator(){
          protected PasswordAuthentication getPasswordAuthentication(){
            return new PasswordAuthentication(user, new String(passwd).toCharArray());
          }
        });
          
        } catch (Exception e) {
          logger.error("MessageRouter Error: couldn't set up an authenticator the proxy");
          
        }
      }
    }
    
    this.module_map = new HashMap();
    
    // This stuff may be done at a reconfigure also
    return configureLocalSite();
  
  }
  
  
  /**
   * Process an XML request - as a String
   *
   * @param xml_in the request to process
   * @return the response - contains any error messages
   * @see String
   */
  public String process(String xml_in) {
    
    Document doc = this.converter.getDOM(xml_in);
    
    Node result = process(doc);
    return this.converter.getString(result);
  }
  
  /**
   * Process an XML request - as a DOM Element
   *
   * @param xml_in the message to process - should be <message>
   * @return the response - contains any error messages
   * @see Element
   */
  public Node process(Node message_node) {
    
      Element message = this.converter.nodeToElement(message_node);

    // check that its a correct message tag
    if (!message.getTagName().equals(GSXML.MESSAGE_ELEM)) {
      logger.error(" Invalid message. GSDL message should start with <"+GSXML.MESSAGE_ELEM+">, instead it starts with:"+message.getTagName()+".");
      return null;
    }
    
    NodeList requests = message.getElementsByTagName(GSXML.REQUEST_ELEM);
    
    Element mainResult = this.doc.createElement(GSXML.MESSAGE_ELEM);
    
    // empty request
    if (requests.getLength()==0) {
      logger.error("empty request");
      return mainResult;
    }
    
    Document message_doc = message.getOwnerDocument();
    
    // for now, just process each request one by one, and append the results to mainResult
    // Note: if you add an element to another node in the same document, it
    // gets removed from where it was. This changes the node list - you cant iterate over the node list in a normal manner if you are moving elements out of it
    int num_requests = requests.getLength();
    for (int i=0; i< num_requests; i++) {
      Node result=null;
      Element req = (Element)requests.item(i);
      if (req == null) {
        logger.error("request "+i+" is null");
        continue;
      }
      String path = req.getAttribute(GSXML.TO_ATT); // returns "" if no att of this name
      if (path.equals("")) {
        // its a message for the message router
        String type_att = req.getAttribute(GSXML.TYPE_ATT);
        if (type_att.equals(GSXML.REQUEST_TYPE_MESSAGING)) {
          // its a messaging request - modifies the requests/responses
          result = modifyMessages(req, message, mainResult);
        } else {
          // standard request
          result = processMessage(req);
        }

        mainResult.appendChild(this.doc.importNode(result, true));
      } else {
        // The message needs to go to another module. The same message can 
        // be passed to multiple modules  - they will be in a comma 
        // separated list in the 'to' attribute
        String [] modules = StringUtils.split(path, ",");
        
        for (int j=0; j<modules.length; j++) {
          // why can't we do this outside the loop??
          Element mess = this.doc.createElement(GSXML.MESSAGE_ELEM);
          Element copied_request = (Element)this.doc.importNode(req, true);
          mess.appendChild(copied_request);
          
          String this_mod = modules[j];
          // find the module to pass it on to
          // need to put the request into a message element
          String obj = GSPath.getFirstLink(this_mod);
          
          if (this.module_map.containsKey(obj)) {
            copied_request.setAttribute(GSXML.TO_ATT, this_mod);
            result = ((ModuleInterface)this.module_map.get(obj)).process(mess);
            if (result !=null ) {
              // append the contents of the message to the mainResult - there will only be one response at this stage
              Node res = GSXML.getChildByTagName(result, GSXML.RESPONSE_ELEM);
              if (res != null){
                mainResult.appendChild(this.doc.importNode(res, true));
 
              }
            } else {
              // add in a place holder response
              Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
              response.setAttribute(GSXML.FROM_ATT, this_mod);
              mainResult.appendChild(response);
              logger.error("MessageRouter Error: request had null result!");
            }
            
          } else {
            logger.error("MessageRouter Error: request has illegal module name in:\n"+this.converter.getString(req));
          }
        }
      }
      
    } // for each request
    
    logger.debug("MR returned response");
    logger.debug(this.converter.getString(mainResult));
    
    return mainResult;    
  }
  public Element getCollectionList() {
    return collection_list;
  }
  public Element getPrivateCollectionList() {
	return private_collection_list;
  }
  public HashMap getModuleMap() {
    return module_map;
  }
  // ********************************************************************
  // auxiliary configure and cleanup methods
  // *******************************************************************
  
  /** Calls clean up on all modules referenced in the module_map and 
      removes them . */
  protected void cleanUpModuleMapEntire() {
    if (this.module_map != null) {
      Iterator i = this.module_map.values().iterator();
      while (i.hasNext()) {
        ((ModuleInterface)i.next()).cleanUp();
        i.remove();
      }
    }
  }

  /**
     Goes through the children of list, and for each local/site-specific
     name attribute, calls cleanUp on the module and removes it from the 
     module_map and removes it from the list
  */
  protected void cleanUpModuleMapSubset(Element list, String remote_site) {
    logger.error(this.converter.getString(list));
    NodeList elements = list.getChildNodes(); // we are assuming no extraneous nodes
    for(int i=elements.getLength()-1; i>=0; i--) {
      Element item = (Element)elements.item(i);
      String name = item.getAttribute(GSXML.NAME_ATT);
      String potential_site_name = GSPath.getFirstLink(name);
      if (remote_site != null) {
        if (remote_site.equals(potential_site_name)) {
          list.removeChild(item);
        }
      } else {
        if (name.equals(potential_site_name)) {// there was no site
          list.removeChild(item);
          ModuleInterface m = (ModuleInterface)this.module_map.remove(name);
          m.cleanUp(); // clean up any open files/connections etc 
          m=null;
        }
      }
    }
    logger.error(this.converter.getString(list));
  }

  /** removes all site modules from module_map, and any stored info about this sites collections and services */
  protected void cleanUpAllExternalSiteInfo() {

    NodeList site_nodes = this.site_list.getChildNodes();
    for(int i=site_nodes.getLength()-1; i>=0; i--) {
      Element item = (Element)site_nodes.item(i);
      String name = item.getAttribute(GSXML.NAME_ATT);
      // will remove the node from site_list
      deactivateModule(GSXML.SITE_ELEM, name);
    }

  }

  /** read thru own site config file - create services and connect to sites
   */
  protected boolean configureLocalSite() {

    // this may be a reconfigure, so clean up the old moduleMap
    cleanUpModuleMapEntire();

    File configFile = new File(GSFile.siteConfigFile(this.site_home));
    
    if (!configFile.exists() ) {
      logger.error(" site config file: "+configFile.getPath()+" not found!");
      return false;
    }
    
    Document config_doc = this.converter.getDOM(configFile);
    if (config_doc == null) {
      logger.error(" couldn't parse site config file: "+configFile.getPath());
      return false;
    }
    
    this.config_info = config_doc.getDocumentElement();
    
    // load up the services: serviceRackList
    this.service_list = this.doc.createElement(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER);
    Element service_rack_list_elem = (Element)GSXML.getChildByTagName(config_info, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
    configureServices(service_rack_list_elem);
    
    // load up the service clusters
    this.cluster_list = this.doc.createElement(GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER);
    Element cluster_list_elem = (Element)GSXML.getChildByTagName(config_info, GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER);
    configureClusters(cluster_list_elem);
    
    // load up the collections
    this.collection_list = this.doc.createElement(GSXML.COLLECTION_ELEM+GSXML.LIST_MODIFIER);
    this.private_collection_list = this.doc.createElement(GSXML.COLLECTION_ELEM+GSXML.LIST_MODIFIER);
    this.oai_collection_list = this.doc.createElement(GSXML.COLLECTION_ELEM+GSXML.LIST_MODIFIER);
    configureCollections();
    
    // load up the external sites - this also adds their services/clusters/collections to the other lists - so must be done last
    this.site_list = this.doc.createElement(GSXML.SITE_ELEM+GSXML.LIST_MODIFIER);
    Element site_list_elem = (Element)GSXML.getChildByTagName(config_info, GSXML.SITE_ELEM+GSXML.LIST_MODIFIER);
    configureExternalSites(site_list_elem);

    // load up the site metadata
    this.metadata_list = this.doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
    Element metadata_list_elem = (Element)GSXML.getChildByTagName(config_info, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
    loadMetadata(metadata_list_elem);

        
    return true;
    
  }

  protected boolean configureServices(Element service_rack_list) {
    
    
    // load up the individual services
    logger.info("loading service modules...");
    
    if (service_rack_list == null) {
      logger.info("... none to be loaded");
      return true;
    }
    
    NodeList service_racks = service_rack_list.getElementsByTagName(GSXML.SERVICE_CLASS_ELEM);
    if (service_racks.getLength()==0) {
      logger.info("... none to be loaded");
      return true;
    }
    
    Element service_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
    Element service_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", "", "");
    service_message.appendChild(service_request);
    
    for(int i=0; i<service_racks.getLength(); i++) {
      Element n = (Element)service_racks.item(i);
      String service_name = n.getAttribute(GSXML.NAME_ATT);
      logger.info("..."+service_name);
      
      Class service_class = null;
      try {
        service_class = Class.forName("org.greenstone.gsdl3.service."+service_name);
      } catch(ClassNotFoundException e) {
        try {
          //try the service_name alone in case the package name is already specified
          service_class = Class.forName(service_name);
        }catch(ClassNotFoundException ae) {
          logger.info(ae.getMessage());
        }
      }
      try {
        ServiceRack s = (ServiceRack)service_class.newInstance();
        s.setSiteHome(this.site_home);
        s.setSiteAddress(this.site_http_address);
        s.setLibraryName(this.library_name);
        s.setMessageRouter(this);
        // pass the XML node to the service for service configuration
        if (!s.configure(n, null)) {
          logger.error ("couldn't configure ServiceRack "+service_name);
          continue;
        }
        
        // find out the supported services for this service module
        Element service_response = (Element) s.process(service_message);
        NodeList services = service_response.getElementsByTagName(GSXML.SERVICE_ELEM);
        if (services.getLength()==0) {
          logger.error("MessageRouter configure error: serviceRack "+service_name+" has no services!");
        } else {
          for (int j=0; j<services.getLength();j++) {
            String service = ((Element)services.item(j)).getAttribute(GSXML.NAME_ATT);
            
            this.module_map.put(service, s);
            
            // add short info to service_list_ XML
            this.service_list.appendChild(this.doc.importNode(services.item(j), true));
          }
        }
      } catch (Exception e ) {
        logger.error("MessageRouter configure exception:  in ServiceRack class specification:  "+ e.getMessage());
        e.printStackTrace();
      }
    } // for each service module
    return true;
  }
  
  protected boolean configureClusters(Element config_cluster_list) {
    
    // load up the service clusters
    logger.info("loading service clusters ...");
    if (config_cluster_list == null) {
      logger.info("... none to be loaded");
      return true;
    }
    NodeList service_clusters = config_cluster_list.getElementsByTagName(GSXML.CLUSTER_ELEM);
    if (service_clusters.getLength()==0) {
      logger.info("... none to be loaded");
      return true;
    }
    
    for (int i=0; i<service_clusters.getLength(); i++) {
      Element cluster = (Element)service_clusters.item(i);
      String name = cluster.getAttribute(GSXML.NAME_ATT);
      logger.info("..."+name);
      ServiceCluster sc = new ServiceCluster();
      sc.setSiteHome(this.site_home);
      sc.setSiteAddress(this.site_http_address);
      sc.setClusterName(name);
      sc.setMessageRouter(this);
      if (!sc.configure(cluster)) {
        logger.error ("couldn't configure ServiceCluster "+name);
        continue;
      }

      this.module_map.put(name, sc); // this replaces the old one if there was one already present
      //add short info to cluster list
      Element e = this.doc.createElement(GSXML.CLUSTER_ELEM);
      e.setAttribute(GSXML.NAME_ATT, name);
      this.cluster_list.appendChild(e);
      
    }
    return true;
  }
  
  /** looks through the collect directory and activates any collections it finds. If this is a reconfigure, clean up must be done first before calling this */
  protected boolean configureCollections() {
    
    // read thru the collect directory and activate all the valid collections
    File collectDir = new File(GSFile.collectDir(this.site_home));
    if (collectDir.exists()) {
      logger.info("Reading thru directory "+collectDir.getPath()+" to find collections.");
      File[] contents = collectDir.listFiles();
      for (int i=0; i<contents.length;i++) {
        if(contents[i].isDirectory()) {
          
          String colName = contents[i].getName();
          if (!colName.startsWith("CVS") && !colName.startsWith(".svn")) {
            activateCollectionByName(colName);
          }
        }
      }
    } // collectDir
    return true;
  }
  
  /** creates and configures a new collection
      if this is done for a reconfigure, the collection should be deactivated first.
      *
      *@param col_name the name of the collection
      *@return true if collection created ok
      */
  protected boolean activateCollectionByName(String col_name) {
    
    logger.info("Activating collection: "+col_name+".");
    
    // Look for the etc/collectionInit.xml file, and see what sort of Collection to load
    Collection c = null;
    File init_file = new File(GSFile.collectionInitFile(this.site_home, col_name));
    
    if (init_file.exists()) {
      Document init_doc = this.converter.getDOM(init_file);
      if (init_doc != null) {
        Element init_elem = init_doc.getDocumentElement();
        if (init_elem != null) {
          String coll_class_name = init_elem.getAttribute("class");
          if (!coll_class_name.equals("")) {
            try {
              c = (Collection)Class.forName("org.greenstone.gsdl3.collection."+coll_class_name).newInstance();
            } catch (Exception e) {
              logger.info(" couldn't create a new collection, type "+coll_class_name+", defaulting to class Collection");
            }
          }
        }
      }
    }
    if (c==null) { // we haven't found another classname to use
      c = new Collection();
    }
    
    c.setCollectionName(col_name);
    c.setSiteHome(this.site_home);
    c.setSiteAddress(this.site_http_address);
    c.setMessageRouter(this);
    if (c.configure()) {
      logger.info("have just configured collection " + col_name);
      // add to list of collections
      this.module_map.put(col_name, c);
      Element e = this.doc.createElement(GSXML.COLLECTION_ELEM);
      e.setAttribute(GSXML.NAME_ATT, col_name);
      
      if(c.isPublic()) {
        // only public collections will appear on the home page
        // add short description_ to collection_list_
        this.collection_list.appendChild(e);

        if (c.hasOAI()) {
          Element ane = this.doc.createElement(GSXML.COLLECTION_ELEM);
          //The collection name is returned as site_name:coll_name, which is in fact the set specification
          ane.setAttribute(GSXML.NAME_ATT, site_name + ":" + col_name);
          ane.setAttribute(OAIXML.LASTMODIFIED, "" + c.getLastmodified()); 
	       // lastmodified not of use anymore for OAI, perhaps useful as general information
	  ane.setAttribute(OAIXML.EARLIEST_DATESTAMP, "" + c.getEarliestDatestamp()); // for OAI

          this.oai_collection_list.appendChild(ane);
          //logger.info(GSXML.xmlNodeToString(oai_collection_list));
        }      
        
      } else {
        this.private_collection_list.appendChild(e);
      }
      return true;
    } else {
      logger.error("Couldn't configure collection: "+
        col_name+".");
      return false;
    }
  }
  

  /** Goes through the siteList and activates each site found. If this is done for a reconfigure, a clean up must be done first ****HOW??? */
  protected boolean configureExternalSites(Element config_site_list) {
    
    // load up the sites
    logger.info("loading external sites...");
    if (config_site_list ==null ) {
      logger.info("...none found");
      return true;
    }
    
    NodeList sites = config_site_list.getElementsByTagName(GSXML.SITE_ELEM);
    if (sites.getLength()==0) {
      logger.info("...none found");
      return true;
    }
    
    // this is a name to identify the current site in the Communicator
    String local_site_name = config_site_list.getAttribute(GSXML.LOCAL_SITE_NAME_ATT);
    if (local_site_name.equals("")) {
      local_site_name = site_name;
    }
    
    for (int i=0; i<sites.getLength(); i++) {
      Element s = (Element)sites.item(i);
      activateSite(s, local_site_name);
    }
    return true;
  }
  
  protected boolean activateSiteByName(String site_name) {
    logger.info("Activating site: "+site_name+".");
    
    File configFile = new File(GSFile.siteConfigFile(this.site_home));
    
    if (!configFile.exists() ) {
      logger.error(" site config file: "+configFile.getPath()+" not found!");
      return false;
    }
    Document config_doc = this.converter.getDOM(configFile);
    if (config_doc == null) {
      logger.error(" couldn't parse site config file: "+configFile.getPath());
      return false;
    }
    Element config_elem = config_doc.getDocumentElement();
    
    Element config_site_list = (Element)GSXML.getChildByTagName(config_elem, GSXML.SITE_ELEM+GSXML.LIST_MODIFIER);
    if (config_site_list ==null ) {
      logger.error("activateSite, no sites found");
      return false;
    }
    // this is a name to identify the current site in the Communicator
    String local_site_name = config_site_list.getAttribute("localSiteName");
    if (local_site_name.equals("")) {
      local_site_name = site_name;
    }
    
    Element this_site_elem = GSXML.getNamedElement(config_site_list, GSXML.SITE_ELEM, GSXML.NAME_ATT, site_name);
    if (this_site_elem == null) {
      logger.error("activateSite, site "+site_name+" not found");
      return false;
    }
    
    return activateSite(this_site_elem, local_site_name);
  }

  protected boolean activateSite(Element site_elem, String local_site_name) {
    
    Communicator comm=null;
    String type = site_elem.getAttribute(GSXML.TYPE_ATT);
    String name = site_elem.getAttribute(GSXML.NAME_ATT);
    if (type.equals(GSXML.COMM_TYPE_SOAP_JAVA)) {
      logger.info("activating SOAP site "+name);
      comm = new SOAPCommunicator();
      if (comm.configure(site_elem)) {
        comm.setLocalSiteName(local_site_name);
        
        // add to map of modules
        this.module_map.put(name, comm);
        this.site_list.appendChild(this.doc.importNode(site_elem, true));
        // need to get collection list and service
        // list from here- if the site isn't up yet, the site will
        // have to be added later
        if (!getRemoteSiteInfo(comm, name)) {
          logger.error(" couldn't get info from site");
        }
      } else {
        logger.error(" couldn't configure site");
        return false;
      }
      
    } else {
      logger.error(" cant talk to server of type:"+type + ", so not making a connection to "+name);
      return false;
    }
    return true;
  }

  /** Goes through the metadataList and loads each metadatum found */
  protected boolean loadMetadata(Element config_metadata_list) {
    
    // load up the sites
    logger.info("loading site metadata...");
    if (config_metadata_list ==null ) {
      logger.info("...none found");
      return true;
    }
    
    NodeList metadata = config_metadata_list.getElementsByTagName(GSXML.METADATA_ELEM);
    if (metadata.getLength()==0) {
      logger.info("...none found");
      return true;
    }
    

    for (int i=0; i<metadata.getLength(); i++) {
      Element s = (Element)metadata.item(i);
      this.metadata_list.appendChild(this.doc.importNode(s, true));
    }
    return true;
  }

  
  /** get site info from external site
   *
   * @param comm - the communicator object for the external site
   * @param site_name - the name of the external site
   * @return true if successful
   */
  protected boolean getRemoteSiteInfo(Communicator comm, String site_name) {
    
    logger.info(" getting info from site:"+site_name);
    
    Element info_request = this.doc.createElement(GSXML.MESSAGE_ELEM);
    Element req = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", "", "");
    info_request.appendChild(req);
    
    // process the message
    Node info_response_node = comm.process(info_request);
    Element info_response = converter.nodeToElement(info_response_node);

    if (info_response == null) {
      return false;
    }
    // collection info
    NodeList colls = info_response.getElementsByTagName(GSXML.COLLECTION_ELEM);
    if (colls.getLength()>0) {
      for (int i=0; i<colls.getLength(); i++) {
        Element e = (Element)colls.item(i);
        String col_name = e.getAttribute(GSXML.NAME_ATT);
        // add the info to own coll list - may want to keep
        // this separate in future - so can distinguish own and
        // other collections ??
        e.setAttribute(GSXML.NAME_ATT, GSPath.prependLink(col_name, site_name));
        this.collection_list.appendChild(this.doc.importNode(e, true));
      }
    }
    
    // service info
    NodeList services = info_response.getElementsByTagName(GSXML.SERVICE_ELEM);
    if (services.getLength()>0) {
      for (int i=0; i<services.getLength(); i++) {
        Element e = (Element)services.item(i);
        String serv_name = e.getAttribute(GSXML.NAME_ATT);
        e.setAttribute(GSXML.NAME_ATT, GSPath.prependLink(serv_name, site_name));
        this.service_list.appendChild(this.doc.importNode(e, true));
      }
    }
    
    // serviceCluster info
    NodeList clusters = info_response.getElementsByTagName(GSXML.CLUSTER_ELEM);
    if (clusters.getLength()>0) {
      for (int i=0; i<clusters.getLength(); i++) {
        Element e = (Element)clusters.item(i);
        String clus_name = e.getAttribute(GSXML.NAME_ATT);
        e.setAttribute(GSXML.NAME_ATT, GSPath.prependLink(clus_name, site_name));
        this.cluster_list.appendChild(this.doc.importNode(e, true));
      }
    }
    return true;
  }
  
  
  
  protected boolean activateServiceClusterByName(String cluster_name) {
    return false;
    
  }
  
  protected boolean activateServiceRackByName(String module_name) {
    return false;
  }
  
  protected boolean deactivateModule(String type, String name) {
    
    logger.info("deactivating "+ type+"  module: "+name);
    if (this.module_map.containsKey(name)) {
      
      logger.info("found the module");
      ModuleInterface m = (ModuleInterface)this.module_map.remove(name);
      // also remove the xml bit from description list
      if (type.equals(GSXML.COLLECTION_ELEM)) {
        if (((Collection)m).isPublic()) { 
          Element this_col = GSXML.getNamedElement(this.collection_list, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, name);
          if (this_col != null) {
            this.collection_list.removeChild(this_col);
          }
          if (((Collection)m).hasOAI()) {
            this_col = GSXML.getNamedElement(this.oai_collection_list, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, name);
            if (this_col != null) {
              this.oai_collection_list.removeChild(this_col);
            }
          }
        } else {
          // a private collection
          Element this_col = GSXML.getNamedElement(this.private_collection_list, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, name);
          if (this_col != null) {
            this.private_collection_list.removeChild(this_col);
          } 
        }
      } else if (type.equals(GSXML.SERVICE_ELEM)) {
        Element this_service = GSXML.getNamedElement(this.service_list, GSXML.SERVICE_ELEM, GSXML.NAME_ATT, name);
        if (this_service != null) {
          this.service_list.removeChild(this_service);
        }
      } else if (type.equals(GSXML.CLUSTER_ELEM)) {
        Element this_cluster = GSXML.getNamedElement(this.cluster_list, GSXML.CLUSTER_ELEM, GSXML.NAME_ATT, name);
        if (this_cluster != null) {
          this.cluster_list.removeChild(this_cluster);
        }
      } else if (type.equals(GSXML.SITE_ELEM)) {
        Element this_site = GSXML.getNamedElement(this.site_list, GSXML.SITE_ELEM, GSXML.NAME_ATT, name);
        if (this_site != null) {
          this.site_list.removeChild(this_site);
         
          // also remove this sites colls, services, clusters etc
          cleanUpModuleMapSubset(this.collection_list, name);
          cleanUpModuleMapSubset(this.cluster_list, name);
          cleanUpModuleMapSubset(this.service_list, name);
          
          // can remote collections be in the oai_coll list, or private coll list ??
        }
      } else {
        logger.error("invalid module type: "+type+", can't remove info about this module");
      }
      
      m.cleanUp(); // clean up any open files/connections etc - can cause trouble on windows
      m=null;
      return true;
    }
    // else not deactivated
    logger.error(name+" module not found");
    return false;
    
  }
   
  //*****************************************************************
  // auxiliary process methods
  //*****************************************************************
  
  /** handles requests made to the MessageRouter itself
   *
   * @param req - the request Element- <request>
   * @return the result Element - should be <response>
   */
  protected Element processMessage(Element req) {
    
    // message for self, should be type=describe/configure at this stage
    String type = req.getAttribute(GSXML.TYPE_ATT);
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    response.setAttribute(GSXML.FROM_ATT, "");
    if (type.equals(GSXML.REQUEST_TYPE_DESCRIBE)) {
      response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_DESCRIBE);
      // check the param list
      Element param_list = (Element) GSXML.getChildByTagName(req, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
      if (param_list == null) {
        response.appendChild(this.collection_list);
        response.appendChild(this.cluster_list);
        response.appendChild(this.site_list);
        response.appendChild(this.service_list);
        response.appendChild(this.metadata_list);
        return response;
      }

      NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
      
      // go through the param list and see what components are wanted
      for (int i=0; i<params.getLength(); i++) {
        
        Element param = (Element)params.item(i);
        // Identify the structure information desired
        if (param.getAttribute(GSXML.NAME_ATT).equals(GSXML.SUBSET_PARAM)) {
          String info = param.getAttribute(GSXML.VALUE_ATT);
          if (info.equals(GSXML.COLLECTION_ELEM+GSXML.LIST_MODIFIER)) {
            response.appendChild(this.collection_list);
          } else if (info.equals(GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER)) {
            response.appendChild(this.cluster_list);
          } else if (info.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
            response.appendChild(this.service_list);
          } else if (info.equals(GSXML.SITE_ELEM+GSXML.LIST_MODIFIER)) {
            response.appendChild(this.site_list);
          } else if (info.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER)) {
            response.appendChild(this.metadata_list);
          }
        }
      }
      return response;
      
    }
        
    if (type.equals(OAIXML.OAI_SET_LIST)) {
      logger.info("oaiSetList request received");
      //this is the oai receptionist asking for a list of oai-support collections
      response.setAttribute(GSXML.TYPE_ATT, OAIXML.OAI_SET_LIST );
      response.appendChild(this.oai_collection_list);
      return response;
    } 


    if (type.equals(GSXML.REQUEST_TYPE_SYSTEM)) {
      
      // a list of system requests - should put any error messages
      // or success messages into response
      NodeList commands = req.getElementsByTagName(GSXML.SYSTEM_ELEM);
      Element site_config_elem = null;
      boolean success = false;
      
      for (int i=0; i<commands.getLength(); i++) {
        // all the commands should be Elements
        Element elem = (Element)commands.item(i);
        String action = elem.getAttribute(GSXML.TYPE_ATT);
        if (action.equals(GSXML.SYSTEM_TYPE_CONFIGURE)) {
          String subset = elem.getAttribute(GSXML.SYSTEM_SUBSET_ATT);
          if (subset.equals("")) {
            // need to reconfigure the MR
            this.configureLocalSite();
            Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM,  "MessageRouter reconfigured successfully");
            response.appendChild(s);
            
          } else {
            // else it a specific request
            if (subset.equals(GSXML.COLLECTION_ELEM+GSXML.LIST_MODIFIER)) {
              // get rid of all the old collection stuff (not counting remote ones) before activating all the new ones
              cleanUpModuleMapSubset(this.collection_list, null);
              cleanUpModuleMapSubset(this.private_collection_list, null);
              success = configureCollections();
            } else {
              
              // need the site config file
              if (site_config_elem==null) {
                
                File configFile = new File(GSFile.siteConfigFile(this.site_home));
                if (!configFile.exists() ) {
                  logger.error(" site config file: "+configFile.getPath()+" not found!");
                  continue;
                }
                Document site_config_doc = this.converter.getDOM(configFile);
                if (site_config_doc == null) {
                  logger.error(" couldn't parse site config file: "+configFile.getPath());
                  continue;
                }
                site_config_elem  = site_config_doc.getDocumentElement();
              }
              if (subset.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
                Element service_rack_list = (Element)GSXML.getChildByTagName(site_config_elem, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
                cleanUpModuleMapSubset(this.service_list, null);
                success = configureServices(service_rack_list);
              } else if (subset.equals(GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER)) {
                Element cluster_list = (Element)GSXML.getChildByTagName(site_config_elem, GSXML.CLUSTER_ELEM+GSXML.LIST_MODIFIER);
                cleanUpModuleMapSubset(this.cluster_list, null);
                success = configureClusters(cluster_list);
              } else if (subset.equals(GSXML.SITE_ELEM+GSXML.LIST_MODIFIER)) {
                Element site_list = (Element)GSXML.getChildByTagName(site_config_elem, GSXML.SITE_ELEM+GSXML.LIST_MODIFIER);
                cleanUpAllExternalSiteInfo();
                success = configureExternalSites(site_list);
              }
            }
            String message=null;
            if (success) {
              message = subset + "reconfigured successfully";
            } else {
              message = "Error in reconfiguring "+subset;
            }
            Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, message);
            response.appendChild(s);
          }
          
          
        } else {
          String module_name = elem.getAttribute(GSXML.SYSTEM_MODULE_NAME_ATT);
          String module_type = elem.getAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT);
          
          if (action.equals(GSXML.SYSTEM_TYPE_DEACTIVATE)) {
            success = deactivateModule(module_type, module_name);
            if (success) {
              Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, module_type+": "+module_name+" deactivated", 
            		  GSXML.SYSTEM_TYPE_DEACTIVATE, GSXML.SUCCESS);
              response.appendChild(s);
            } else {
              Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, module_type+": "+module_name+" could not be deactivated",
            		  GSXML.SYSTEM_TYPE_DEACTIVATE, GSXML.ERROR);
              response.appendChild(s);
            }
            
          } else if (action.equals(GSXML.SYSTEM_TYPE_ACTIVATE)) {
            // we need to deactivate the module first, in case this is a 
            // reconfigure
            deactivateModule(module_type, module_name);
            if (module_type.equals(GSXML.COLLECTION_ELEM)) {
              success = activateCollectionByName(module_name);
            } else if (module_type.equals(GSXML.SITE_ELEM)) {
              success = activateSiteByName(module_name);
            } else if (module_type.equals(GSXML.CLUSTER_ELEM)) {
              success = activateServiceClusterByName(module_name);
            }
            if (success) {
              Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, module_type+": "+module_name+" activated",
            		  GSXML.SYSTEM_TYPE_ACTIVATE, GSXML.SUCCESS);
              response.appendChild(s);
            } else {
              Element s = GSXML.createTextElement(this.doc, GSXML.STATUS_ELEM, module_type+": "+module_name+" could not be activated",
            		  GSXML.SYSTEM_TYPE_ACTIVATE, GSXML.ERROR);
              response.appendChild(s);
            }
          }
        } // else not a configure action
      } // for all commands
      return response;
      
      
    } // system type request
    
    // if get here something has gone wrong
    logger.error(" cant process request:");
    logger.error(this.converter.getString(req));
    return null;
    
  }

  //* Used to copy nodes from one message to another. E.g. copy a response node to the next request. Not sure if this is actually used anywhere yet... */

  protected Element modifyMessages(Element request, Element message, Element result) {
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    response.setAttribute(GSXML.FROM_ATT, "");
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_MESSAGING);
    
    NodeList commands = request.getElementsByTagName("command");
    if (commands == null) {
      logger.error("no commands, "+converter.getPrettyString(request));
      return response;
    }
    for (int i=0; i<commands.getLength(); i++) {
      Element action = (Element)commands.item(i);
      String type = action.getAttribute(GSXML.TYPE_ATT);
      if (type.equals("copyNode")) {
        // copies the from node as a child of to node
        String from_path = action.getAttribute("from");
        String to_path = action.getAttribute("to");
        Element from_node = null;
        String from_node_root = GSPath.getFirstLink(from_path);
        if (from_node_root.startsWith(GSXML.REQUEST_ELEM)) {
          from_node = message;
        } else if (from_node_root.startsWith(GSXML.RESPONSE_ELEM)) {
          from_node = result;
        }
        if (from_node == null) {
          continue;
        }
        Element to_node = null;
        String to_node_root = GSPath.getFirstLink(to_path);
        if (to_node_root.startsWith(GSXML.REQUEST_ELEM)) {
          to_node = message;
        } else if (to_node_root.startsWith(GSXML.RESPONSE_ELEM)) {
          to_node = result;
        }
        if (to_node == null) {
          continue;
        }
        // now we know what node to copy where
        Node orig_node = GSXML.getNodeByPathIndexed(from_node, from_path);
        if (orig_node == null) {
          continue;
        }
        Node new_parent = GSXML.getNodeByPathIndexed(to_node, to_path);
        if (new_parent == null) {
          continue;
          
        }
        new_parent.appendChild(to_node.getOwnerDocument().importNode(orig_node, true));
      }
      
      else if (type.equals("copyChildren")) {
        
      }
    } // for each command
    return response;
  }
  
  // ****************************************************
  // other methods
  // ****************************************************
  
 
  
}


