/*
 *    OAIMessageRouter.java
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

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.greenstone.gsdl3.collection.OAICollection;
import org.greenstone.gsdl3.collection.ServiceCluster;
import org.greenstone.gsdl3.comms.Communicator;
import org.greenstone.gsdl3.comms.SOAPCommunicator;
import org.greenstone.gsdl3.service.ServiceRack;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OAIXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The hub of a Greenstone OAI server.
 * 
 * A simplified version of MessageRouter for OAIServer. Only loads up collections that have OAI services.
 */
public class OAIMessageRouter extends MessageRouter
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.MessageRouter.class.getName());

  public Element oai_config = null;
	//***************************************************************
	// public methods
	//***************************************************************

	/** constructor */
	public OAIMessageRouter()
	{
	}

	/**
	 * read thru own site config file - create services and connect to sites
	 */
	protected boolean configureLocalSite()
	{
	  
	  // this may be a reconfigure, so clean up the old moduleMap
		cleanUpModuleMapEntire();

		// for oai, we don't do anything with the site config file. But we'll read it in and keep it in case need it later, eg for replace elements when retrieving metadata - which I don't think has been implemented
		File configFile = new File(GSFile.siteConfigFile(this.site_home));

		if (!configFile.exists())
		{
			logger.error(" site config file: " + configFile.getPath() + " not found!");
			return false;
		}

		Document config_doc = XMLConverter.getDOM(configFile);
		if (config_doc == null)
		{
			logger.error(" couldn't parse site config file: " + configFile.getPath());
			return false;
		}

		this.config_info = config_doc.getDocumentElement();

		// this is the receptionists OAIConfig.xml. Need to rethink how the MR gets this this if we ever talk to remote site, and whether it should be using it anyway
		this.oai_config = OAIXML.getOAIConfigXML();
		if (this.oai_config == null)
		{
		  logger.error("Couldn't load in OAIConfig.xml");
		  return false;
		}
		Document doc = XMLConverter.newDOM();
		// load up the collections
		this.collection_list = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		configureCollections();

		return true;

	}


	/**
	 * creates and configures a new collection if this is done for a
	 * reconfigure, the collection should be deactivated first.
	 * 
	 * @param col_name
	 *            the name of the collection
	 * @return true if collection created ok
	 */
	protected boolean activateCollectionByName(String col_name)
	{

		logger.info("Activating collection: " + col_name + ".");
		Document doc = this.collection_list.getOwnerDocument();
		// use our special OAICollection - this will only load in oai services
		OAICollection c = new OAICollection();

		c.setCollectionName(col_name);
		c.setSiteHome(this.site_home);
		c.setSiteAddress(this.site_http_address);
		c.setMessageRouter(this);
		if (!c.configure()) {
		  logger.error("Couldn't configure collection: " + col_name + ".");
		  return false;
		}

		logger.info("have just configured collection " + col_name);
		if (!c.hasOAI()) {
		  logger.info ("collection "+col_name+" has no OAI services. Not keeping it loaded");
		  return false;
		}
		if (!c.configureOAI(this.oai_config)) {
		  logger.info("couldn't configure the collection : "+col_name +" with the oai config info");
		  return false;
		}
		// add to list of collections
		this.module_map.put(col_name, c);
		Element e = doc.createElement(GSXML.COLLECTION_ELEM);
		e.setAttribute(GSXML.NAME_ATT, col_name);
		e.setAttribute(OAIXML.LASTMODIFIED, "" + c.getLastmodified());
		e.setAttribute(OAIXML.EARLIEST_DATESTAMP, "" + c.getEarliestDatestamp());
		e.setAttribute(OAIXML.EARLIEST_OAI_DATESTAMP, "" + c.getEarliestOAIDatestamp());
		this.collection_list.appendChild(e);
		return true;
		
	}



	//*****************************************************************
	// auxiliary process methods
	//*****************************************************************

	/**
	 * handles requests made to the MessageRouter itself
	 * 
	 * @param req
	 *            - the request Element- <request>
	 * @return the result Element - should be <response>
	 */
  protected Element processMessage(Element req) {
    Document doc = XMLConverter.newDOM();
    String type = req.getAttribute(GSXML.TYPE_ATT);
    Element response = doc.createElement(GSXML.RESPONSE_ELEM);
    response.setAttribute(GSXML.FROM_ATT, "");
    
    if (type.equals(OAIXML.OAI_SET_LIST))
      {
	logger.info("oaiSetList request received");
	//this is the oai receptionist asking for a list of oai-support collections
	response.setAttribute(GSXML.TYPE_ATT, OAIXML.OAI_SET_LIST);
	response.appendChild(doc.importNode(this.collection_list, true));
	return response;
      }
    return super.processMessage(req);
  }


}
