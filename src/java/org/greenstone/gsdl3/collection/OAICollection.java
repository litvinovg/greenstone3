/*
 *    OAICollection.java
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
package org.greenstone.gsdl3.collection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.service.ServiceRack;
import org.greenstone.gsdl3.service.OAIPMH;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.OAIXML;
import org.greenstone.gsdl3.util.SimpleMacroResolver;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.gsdl3.util.XMLTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a collection for the OAI server. This is a cut down version of Collection, as we
 * only want to load the OAIPMH service rack, not any of the others
 * 
 */
public class OAICollection extends Collection
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.collection.OAICollection.class.getName());

	/** does this collection provide the OAI service */
	protected boolean has_oai = false;

  /** a reference to the OAIPMH service rack */
  protected OAIPMH oai_service_rack = null;

	/**
	 * Configures the collection.
	 * 
	 * site_home and cluster_name  must be set before configure is called.
	 * 
	 * collection metadata is obtained, and services loaded.
	 * 
	 * @return true/false on success/fail
	 */
	public boolean configure()
	{
		if (this.site_home == null || this.cluster_name == null)
		{
			logger.error("Collection: site_home and collection_name must be set before configure called!");
			return false;
		}

		macro_resolver.addMacro("_httpcollection_", this.site_http_address + "/collect/" + this.cluster_name);

		Element coll_config_xml = loadCollConfigFile();
		GSXSLT.modifyCollectionConfigForDebug(coll_config_xml);
		Element build_config_xml = loadBuildConfigFile();

		if (coll_config_xml == null || build_config_xml == null)
		{
			return false;
		}

		// process the metadata and display items and default library params
		super.configureLocalData(coll_config_xml);
		super.configureLocalData(build_config_xml);
		// get extra collection specific stuff
		findAndLoadInfo(coll_config_xml, build_config_xml);

		// load up the OAIPMH serviceRack
		configureServiceRacks(coll_config_xml, build_config_xml);

		return true;

	}


	/**
	 * whether this collection has OAIPMH services
	 */
	public boolean hasOAI()
	{
		return has_oai;
	}

  /** add any extra info for collection from OAIConfig.xml */
  public boolean configureOAI(Element oai_config) {
    // just pass the element to each service - should only be one
    return this.oai_service_rack.configureOAI(oai_config);
  }

  /** override this to only load up OAIPMH serviceRack  - don't need all the rest of them for oai*/
	protected boolean configureServiceRackList(Element service_rack_list, Element extra_info)
	{

	  // find the OAIPMH service
	  Element oai_service_xml = GSXML.getNamedElement(service_rack_list, GSXML.SERVICE_CLASS_ELEM, GSXML.NAME_ATT, "OAIPMH");
	  if (oai_service_xml == null) {
	    return false;
	  }
	  
	  // the xml request to send to the serviceRack to query what services it provides
	  Document doc = XMLConverter.newDOM();
	  Element message = doc.createElement(GSXML.MESSAGE_ELEM);
	  Element request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", new UserContext());
	  message.appendChild(request);

	  this.oai_service_rack = new OAIPMH();
	  this.oai_service_rack.setSiteHome(this.site_home);
	  this.oai_service_rack.setSiteAddress(this.site_http_address);
	  this.oai_service_rack.setClusterName(this.cluster_name);
	  this.oai_service_rack.setServiceCluster(this);
	  this.oai_service_rack.setMessageRouter(this.router);
	  // pass the xml node to the service for configuration
	  if (this.oai_service_rack.configure(oai_service_xml, extra_info)) {
	    
	    // find out the supported service types for this service module
	    Node types = this.oai_service_rack.process(message);
	    NodeList typenodes = ((Element) types).getElementsByTagName(GSXML.SERVICE_ELEM);
	    
	    for (int j = 0; j < typenodes.getLength(); j++)
	      {
		String service = ((Element) typenodes.item(j)).getAttribute(GSXML.NAME_ATT);
		
		if (service_map.get(service) != null)
		  {
		    char extra = '0';
		    String new_service = service + extra;
		    
		    while (service_map.get(new_service) != null)
		      {
			extra++;
			new_service = service + extra;
		      }
		    this.service_name_map.put(new_service, service);
		    service = new_service;
		    ((Element) typenodes.item(j)).setAttribute(GSXML.NAME_ATT, service);
		  }
		this.service_map.put(service, this.oai_service_rack);
		// also add info to the ServiceInfo XML element
		this.service_list.appendChild(this.desc_doc.importNode(typenodes.item(j), true));
	      }
	    has_oai = true;
	    return true;
	  }
	  

	  return false;
	}

}
