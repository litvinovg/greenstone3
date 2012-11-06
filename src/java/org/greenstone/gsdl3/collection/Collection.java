/*
 *    Collection.java
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
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OAIXML;
import org.greenstone.gsdl3.util.SimpleMacroResolver;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a collection in Greenstone. A collection is an extension of a
 * ServiceCluster - it has local data that the services use.
 * 
 * @author Katherine Don
 * @see ModuleInterface
 */
public class Collection extends ServiceCluster
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.collection.Collection.class.getName());

	/** is this collection being tidied and therefore can support realistic book view?*/
	protected boolean useBook = false;
	/** is this collection public or private  - public collections will 
	    appear on the home page, whereas private collections won't*/
	protected boolean is_public = true;
	/** collection type : mg, mgpp or lucene */
	protected String col_type = "";
	/** database type : gdbm, jdbm or sqlite */
	protected String db_type = "";

	/** does this collection provide the OAI service */
	protected boolean has_oai = false;
	/** time when this collection was built */
	protected long lastmodified = 0;
	/** earliestDatestamp of this collection. Necessary for OAI */
	protected long earliestDatestamp = 0;

	/** Stores the default accessibility of guest users */
	protected boolean _publicAccess = true;
	/** Stores the scope of any security rules (either collection or document) */
	protected boolean _securityScopeCollection = true;

	protected HashMap<String, ArrayList<Element>> _documentSets = new HashMap<String, ArrayList<Element>>();
	protected ArrayList<HashMap<String, ArrayList<String>>> _securityExceptions = new ArrayList<HashMap<String, ArrayList<String>>>();

  
	protected XMLTransformer transformer = null;

	/** same as setClusterName */
	public void setCollectionName(String name)
	{
		setClusterName(name);
	}

	public Collection()
	{
		super();
		this.description = this.doc.createElement(GSXML.COLLECTION_ELEM);
	}

	/**
	 * Configures the collection.
	 * 
	 * gsdlHome and collectionName must be set before configure is called.
	 * 
	 * the file buildcfg.xml is located in gsdlHome/collect/collectionName
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
		Element build_config_xml = loadBuildConfigFile();

		if (coll_config_xml == null || build_config_xml == null)
		{
			return false;
		}

		// get the collection type attribute
		Element search = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.SEARCH_ELEM);
		if (search != null)
		{
			col_type = search.getAttribute(GSXML.TYPE_ATT);
		}

		Element browse = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.INFODB_ELEM);
		if (browse != null)
		{
			db_type = browse.getAttribute(GSXML.TYPE_ATT);
		}
		else
		{
			db_type = "gdbm"; //Default database type
		}

		this.description.setAttribute(GSXML.TYPE_ATT, col_type);
		this.description.setAttribute(GSXML.DB_TYPE_ATT, db_type);
		
		_globalFormat = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.FORMAT_ELEM);
		// process the metadata and display items and default library params
		super.configureLocalData(coll_config_xml);
		super.configureLocalData(build_config_xml);
		// get extra collection specific stuff
		findAndLoadInfo(coll_config_xml, build_config_xml);

		loadSecurityInformation(coll_config_xml);

		// now do the services
		configureServiceRacks(coll_config_xml, build_config_xml);

		return true;

	}

	public boolean useBook()
	{
		return useBook;
	}

	public boolean isPublic()
	{
		return is_public;
	}

	// Not used anymore by the OAIReceptionist to find out the earliest datestamp 
	// amongst all oai collections in the repository. May be useful generally.
	public long getLastmodified()
	{
		return lastmodified;
	}

	//used by the OAIReceptionist to find out the earliest datestamp amongst all oai collections in the repository
	public long getEarliestDatestamp()
	{
		return earliestDatestamp;
	}

	/**
	 * whether the service_map in ServiceCluster.java contains the service
	 * 'OAIPMH' 11/06/2007 xiao
	 */
	public boolean hasOAI()
	{
		return has_oai;
	}

	/**
	 * load in the collection config file into a DOM Element
	 */
	protected Element loadCollConfigFile()
	{

		File coll_config_file = new File(GSFile.collectionConfigFile(this.site_home, this.cluster_name));

		if (!coll_config_file.exists())
		{
			logger.error("Collection: couldn't configure collection: " + this.cluster_name + ", " + coll_config_file + " does not exist");
			return null;
		}
		// get the xml
		Document coll_config_doc = this.converter.getDOM(coll_config_file, CONFIG_ENCODING);
		Element coll_config_elem = null;
		if (coll_config_doc != null)
		{
			coll_config_elem = coll_config_doc.getDocumentElement();
		}
		return coll_config_elem;

	}

	/**
	 * load in the collection build config file into a DOM Element
	 */
	protected Element loadBuildConfigFile()
	{
		File build_config_file = new File(GSFile.collectionBuildConfigFile(this.site_home, this.cluster_name));
		if (!build_config_file.exists())
		{
			logger.error("Collection: couldn't configure collection: " + this.cluster_name + ", " + build_config_file + " does not exist");
			return null;
		}
		Document build_config_doc = this.converter.getDOM(build_config_file, CONFIG_ENCODING);
		Element build_config_elem = null;
		if (build_config_doc != null)
		{
			build_config_elem = build_config_doc.getDocumentElement();
		}

		lastmodified = build_config_file.lastModified();

		return build_config_elem;
	}

	/**
	 * find the metadata and display elems from the two config files and add it
	 * to the appropriate lists
	 */
	protected boolean findAndLoadInfo(Element coll_config_xml, Element build_config_xml)
	{
		// Element meta_list = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		// addMetadata(meta_list);
		// meta_list = (Element) GSXML.getChildByTagName(build_config_xml, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		// addMetadata(meta_list);
		addMetadata("httpPath", this.site_http_address + "/collect/" + this.cluster_name);

		// display stuff
		// Element display_list = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER);
		// if (display_list != null)
		// {
		// 	resolveMacros(display_list);
		// 	addDisplayItems(display_list);
		// }

		//check whether the html are tidy or not
		Element import_list = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.IMPORT_ELEM);
		if (import_list != null)
		{
			Element plugin_list = (Element) GSXML.getChildByTagName(import_list, GSXML.PLUGIN_ELEM + GSXML.LIST_MODIFIER);
			//addPlugins(plugin_list);
			if (plugin_list != null)
			{
				Element plugin_elem = (Element) GSXML.getNamedElement(plugin_list, GSXML.PLUGIN_ELEM, GSXML.NAME_ATT, "HTMLPlugin");
				if (plugin_elem != null)
				{
					//get the option
					Element option_elem = (Element) GSXML.getNamedElement(plugin_elem, GSXML.PARAM_OPTION_ELEM, GSXML.NAME_ATT, "-use_realistic_book");
					if (option_elem != null)
					{
						useBook = true;
					}
				}
			}
		}
		String tidy = (useBook == true ? "tidy" : "untidy");
		addMetadata("tidyoption", tidy);

		// check whether we are public or not
		if (this.metadata_list != null)
		{
			Element meta_elem = (Element) GSXML.getNamedElement(this.metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "public");
			if (meta_elem != null)
			{
				String value = GSXML.getValue(meta_elem).toLowerCase().trim();
				if (value.equals("false"))
				{
					is_public = false;
				}
			}
		}
		return true;
	}

	protected void loadSecurityInformation(Element coll_config_xml)
	{
		Element securityBlock = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.SECURITY_ELEM);

		if (securityBlock == null)
		{
			return;
		}

		String scope = securityBlock.getAttribute(GSXML.SCOPE_ATT);
		String defaultAccess = securityBlock.getAttribute(GSXML.DEFAULT_ACCESS_ATT);

		if (defaultAccess.toLowerCase().equals("public"))
		{
			_publicAccess = true;
		}
		else if (defaultAccess.toLowerCase().equals("private"))
		{
			_publicAccess = false;
		}
		else
		{
			logger.warn("Default access for collection " + this.cluster_name + " is neither public or private, assuming public");
		}

		if (scope.toLowerCase().equals("collection"))
		{
			_securityScopeCollection = true;
		}
		else if (scope.toLowerCase().equals("documents") || scope.toLowerCase().equals("document"))
		{
			_securityScopeCollection = false;
		}
		else
		{
			logger.warn("Security scope is neither collection or document, assuming collection");
		}

		NodeList exceptions = GSXML.getChildrenByTagName(securityBlock, GSXML.EXCEPTION_ELEM);

		if (exceptions.getLength() > 0)
		{
			if (!_securityScopeCollection)
			{
				NodeList documentSetElems = GSXML.getChildrenByTagName(securityBlock, GSXML.DOCUMENT_SET_ELEM);
				for (int i = 0; i < documentSetElems.getLength(); i++)
				{
					Element documentSet = (Element) documentSetElems.item(i);
					String setName = documentSet.getAttribute(GSXML.NAME_ATT);
					NodeList matchStatements = GSXML.getChildrenByTagName(documentSet, GSXML.MATCH_ELEM);
					ArrayList<Element> matchStatementList = new ArrayList<Element>();
					for (int j = 0; j < matchStatements.getLength(); j++)
					{
						matchStatementList.add((Element) matchStatements.item(j));
					}
					_documentSets.put(setName, matchStatementList);
				}
			}

			for (int i = 0; i < exceptions.getLength(); i++)
			{
				HashMap<String, ArrayList<String>> securityException = new HashMap<String, ArrayList<String>>();
				ArrayList<String> exceptionGroups = new ArrayList<String>();
				ArrayList<String> exceptionSets = new ArrayList<String>();

				Element exception = (Element) exceptions.item(i);
				NodeList groups = GSXML.getChildrenByTagName(exception, GSXML.GROUP_ELEM);
				for (int j = 0; j < groups.getLength(); j++)
				{
					Element group = (Element) groups.item(j);
					String groupName = group.getAttribute(GSXML.NAME_ATT);
					exceptionGroups.add(groupName);
				}
				NodeList docSets = GSXML.getChildrenByTagName(exception, GSXML.DOCUMENT_SET_ELEM);
				for (int j = 0; j < docSets.getLength(); j++)
				{
					Element docSet = (Element) docSets.item(j);
					String docSetName = docSet.getAttribute(GSXML.NAME_ATT);
					exceptionSets.add(docSetName);
				}

				securityException.put("groups", exceptionGroups);
				securityException.put("sets", exceptionSets);
				_securityExceptions.add(securityException);
			}
		}
	}

	protected boolean configureServiceRacks(Element coll_config_xml, Element build_config_xml)
	{
		clearServices();
		Element service_list = (Element) GSXML.getChildByTagName(build_config_xml, GSXML.SERVICE_CLASS_ELEM + GSXML.LIST_MODIFIER);
		Element oai_service_rack = null;
		if (service_list != null) {
		  configureServiceRackList(service_list, coll_config_xml);
		  oai_service_rack = GSXML.getNamedElement(service_list, GSXML.SERVICE_CLASS_ELEM, OAIXML.NAME, OAIXML.OAIPMH);
		}
		// collection Config may also contain manually added service racks
		service_list = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.SERVICE_CLASS_ELEM + GSXML.LIST_MODIFIER);
		if (service_list != null)
		{
		  configureServiceRackList(service_list, build_config_xml);
		  // this oai used in preference to one in buildConfig.xml
		  oai_service_rack = GSXML.getNamedElement(service_list, GSXML.SERVICE_CLASS_ELEM, OAIXML.NAME, OAIXML.OAIPMH);
		}
		// Check for oai

		if (oai_service_rack != null)
		  {
		    has_oai = true;
		    logger.info(this.cluster_name +" has OAI services");
		    // extract earliestDatestamp from the buildconfig.xml for OAI
		    Element metadata_list = (Element) GSXML.getChildByTagName(build_config_xml, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		    
		    if (metadata_list != null)
		      {
			NodeList children = metadata_list.getElementsByTagName(GSXML.METADATA_ELEM);
			// can't do getChildNodes(), because whitespace, such as newlines, creates Text nodes
			for (int i = 0; i < children.getLength(); i++)
			  {
			    Element metadata = (Element) children.item(i);
			    if (metadata.getAttribute(GSXML.NAME_ATT).equals(OAIXML.EARLIEST_DATESTAMP))
			      {
				String earliestDatestampStr = GSXML.getValue(metadata);
				if (!earliestDatestampStr.equals(""))
				  {
				    earliestDatestamp = Long.parseLong(earliestDatestampStr);
				  }
				break; // found a metadata element with name=earliestDatestamp in buildconfig
			      }
			  }
		      }
		    
		    // If at the end of this, there is no value for earliestDatestamp, print out a warning
		    logger.warn("No earliestDatestamp in buildConfig.xml for collection: " + this.cluster_name + ". Defaulting to 0.");
		    
		  } // if oai_service_rack != null
	
		return true;
	}


	/**
	 * do a configure on only part of the collection
	 */
	protected boolean configureSubset(String subset)
	{

		// need the coll config files
		Element coll_config_elem = loadCollConfigFile();
		Element build_config_elem = loadBuildConfigFile();
		if (coll_config_elem == null || build_config_elem == null)
		{
			// wont be able to do any of the requests
			return false;
		}

		if (subset.equals(GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER))
		{
			return configureServiceRacks(coll_config_elem, build_config_elem);
		}

		if (subset.equals(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER) || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER) || subset.equals("libraryParamList"))
		{
		  configureLocalData(coll_config_elem);
		  configureLocalData(build_config_elem);
			return findAndLoadInfo(coll_config_elem, build_config_elem);

		}

		logger.error("Collection: cant process system request, configure " + subset);
		return false;
	}

	/**
	 * handles requests made to the ServiceCluster itself
	 * 
	 * @param req
	 *            - the request Element- <request>
	 * @return the result Element - should be <response>
	 */
	protected Element processMessage(Element request)
	{
	  String type = request.getAttribute(GSXML.TYPE_ATT);
		if (type.equals(GSXML.REQUEST_TYPE_FORMAT_STRING))
		{
		  return processFormatStringRequest(request);
		}
		else if (type.equals(GSXML.REQUEST_TYPE_SECURITY))
		{
		  return processSecurityRequest(request);
		}
		else if (type.equals(GSXML.REQUEST_TYPE_FORMAT))
		{
		Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, this.cluster_name);
		response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_FORMAT);
			if(_globalFormat != null)
			{
				response.appendChild(this.doc.importNode(_globalFormat, true));
			}
			return response;
		}
		// unknown type
		return super.processMessage(request);

	}

  protected Element processSecurityRequest(Element request) {
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    response.setAttribute(GSXML.FROM_ATT, this.cluster_name);
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_SECURITY);
    
    String oid = request.getAttribute("oid");
    if (oid.contains("."))
      {
	oid = oid.substring(0, oid.indexOf("."));
      }
    
    ArrayList<String> groups = getPermittedGroups(oid);
    
    Element groupList = this.doc.createElement(GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
    response.appendChild(groupList);
    
    for (String groupName : groups)
      {
	Element group = this.doc.createElement(GSXML.GROUP_ELEM);
	groupList.appendChild(group);
	group.setAttribute(GSXML.NAME_ATT, groupName);
      }
    return response;
  }

	protected ArrayList<String> getPermittedGroups(String oid)
	{
		ArrayList<String> groups = new ArrayList<String>();

		if (_securityScopeCollection)
		{
			if (_publicAccess)
			{
				groups.add("");
			}
			else
			{
				for (HashMap<String, ArrayList<String>> exception : _securityExceptions)
				{
					for (String group : exception.get("groups"))
					{
						groups.add(group);
					}
				}
			}
		}
		else
		{
			if (oid != null && !oid.equals(""))
			{
				boolean inSet = false;
				for (HashMap<String, ArrayList<String>> exception : _securityExceptions)
				{
					for (String setName : exception.get("sets"))
					{
						if (documentIsInSet(oid, setName))
						{
							inSet = true;
							for (String group : exception.get("groups"))
							{
								groups.add(group);
							}
						}
					}
				}

				if (!inSet && _publicAccess)
				{
					groups.add("");
				}
			}
			else
			{
				groups.add("");
			}
		}

		return groups;
	}

	protected boolean documentIsInSet(String oid, String setName)
	{
		ArrayList<Element> matchStatements = _documentSets.get(setName);
		if (matchStatements == null || matchStatements.size() == 0)
		{
			return false;
		}

		for (Element currentMatchStatement : matchStatements)
		{
			String fieldName = currentMatchStatement.getAttribute(GSXML.FIELD_ATT);
			if (fieldName == null || fieldName.equals(""))
			{
				fieldName = "oid";
			}

			String type = currentMatchStatement.getAttribute(GSXML.TYPE_ATT);
			if (type == null || type.equals(""))
			{
				type = "match";
			}

			String fieldValue = "";
			if (!fieldName.equals("oid"))
			{
				fieldValue = getFieldValue(oid, fieldName);
				if (fieldValue == null)
				{
					return false;
				}
			}
			else
			{
				fieldValue = oid;
			}

			String matchValue = GSXML.getNodeText(currentMatchStatement);
			if (type.equals("match"))
			{
				if (matchValue.equals(fieldValue))
				{
					return true;
				}
			}
			else if (type.equals("regex"))
			{
				if (fieldValue.matches(matchValue))
				{
					return true;
				}
			}
			else
			{
				logger.warn("Unknown type of match specified in security block of collection " + this.cluster_name + ".");
			}
		}

		return false;
	}

	protected String getFieldValue(String oid, String fieldName)
	{
		Element metadataMessage = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element metadataRequest = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, this.cluster_name + "/DocumentMetadataRetrieve", new UserContext());
		metadataMessage.appendChild(metadataRequest);

		Element paramList = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		metadataRequest.appendChild(paramList);

		Element param = this.doc.createElement(GSXML.PARAM_ELEM);
		paramList.appendChild(param);

		param.setAttribute(GSXML.NAME_ATT, "metadata");
		param.setAttribute(GSXML.VALUE_ATT, fieldName);

		Element docList = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		metadataRequest.appendChild(docList);

		Element doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		docList.appendChild(doc);

		doc.setAttribute(GSXML.NODE_ID_ATT, oid);

		Element response = (Element) this.router.process(metadataMessage);
		NodeList metadataElems = response.getElementsByTagName(GSXML.METADATA_ELEM);

		if (metadataElems.getLength() > 0)
		{
			Element metadata = (Element) metadataElems.item(0);
			return GSXML.getNodeText(metadata);
		}

		return null;
	}

  protected Element processFormatStringRequest(Element request) {
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_FORMAT_STRING);
    response.setAttribute(GSXML.FROM_ATT, this.cluster_name);

    String subaction = request.getAttribute("subaction");
    String service = request.getAttribute("service");
    
    String classifier = null;
    if (service.equals("ClassifierBrowse"))
      {
	classifier = request.getAttribute("classifier");
      }

    // check for version file
    String directory = new File(GSFile.collectionConfigFile(this.site_home, this.cluster_name)).getParent() + File.separator;

    String version_filename = "";
    if (service.equals("ClassifierBrowse"))
      version_filename = directory + "browse_" + classifier + "_format_statement_version.txt";
    else
      version_filename = directory + "query_format_statement_version.txt";

    File version_file = new File(version_filename);

    if (subaction.equals("update"))
      {
	Element format_element = (Element) GSXML.getChildByTagName(request, GSXML.FORMAT_STRING_ELEM);
	//String format_string = GSXML.getNodeText(format_element);
	Element format_statement = (Element) format_element.getFirstChild();

	String version_number = "1";
	BufferedWriter writer;

	try
	  {

	    if (version_file.exists())
	      {
		// Read version
		BufferedReader reader = new BufferedReader(new FileReader(version_filename));
		version_number = reader.readLine();
		int aInt = Integer.parseInt(version_number) + 1;
		version_number = Integer.toString(aInt);
		reader.close();
	      }
	    else
	      {
		// Create
		version_file.createNewFile();
		writer = new BufferedWriter(new FileWriter(version_filename));
		writer.write(version_number);
		writer.close();
	      }

	    // Write version file
	    String format_statement_filename = "";

	    if (service.equals("ClassifierBrowse"))
	      format_statement_filename = directory + "browse_" + classifier + "_format_statement_v" + version_number + ".txt";
	    else
	      format_statement_filename = directory + "query_format_statement_v" + version_number + ".txt";

	    // Write format statement
	    String format_string = this.converter.getString(format_statement); //GSXML.xmlNodeToString(format_statement);
	    writer = new BufferedWriter(new FileWriter(format_statement_filename));
	    writer.write(format_string);
	    writer.close();

	    // Update version number
	    writer = new BufferedWriter(new FileWriter(version_filename));
	    writer.write(version_number);
	    writer.close();

	  }
	catch (IOException e)
	  {
	    logger.error("IO Exception " + e);
	  }
      }

    if (subaction.equals("saveDocument"))
      {
	Element format_element = (Element) GSXML.getChildByTagName(request, GSXML.FORMAT_STRING_ELEM);
	//String format_string = GSXML.getNodeText(format_element);
	// Get display tag
	Element display_format = (Element) format_element.getFirstChild();

	String collection_config = directory + "collectionConfig.xml";
	Document config = this.converter.getDOM(new File(collection_config), "UTF-8");

	Node current_node = GSXML.getChildByTagName(config, "CollectionConfig");

	// Get display child
	if (GSXML.getChildByTagName(current_node, "display") == null)
	  {
	    // well then create a format tag
	    Element display_tag = config.createElement("display");
	    current_node = (Node) current_node.appendChild(display_tag);
	  }
	else
	  {
	    current_node = GSXML.getChildByTagName(current_node, "display");
	  }

	if (GSXML.getChildByTagName(current_node, "format") == null)
	  {
	    // well then create a format tag
	    Element format_tag = config.createElement("format");
	    current_node.appendChild(format_tag);
	  }

	current_node.replaceChild(config.importNode(display_format, true), GSXML.getChildByTagName(current_node, "format"));

	String new_config = this.converter.getString(config);

	new_config = StringUtils.replace(new_config, "&lt;", "<");
	new_config = StringUtils.replace(new_config, "&gt;", ">");
	new_config = StringUtils.replace(new_config, "&quot;", "\"");

	try
	  {
	    // Write to file (not original! for now)
	    BufferedWriter writer = new BufferedWriter(new FileWriter(collection_config + ".new"));
	    writer.write(new_config);
	    writer.close();
	  }
	catch (IOException e)
	  {
	    logger.error("IO Exception " + e);
	  }
      }

    if (subaction.equals("save"))
      {
	Element format_element = (Element) GSXML.getChildByTagName(request, GSXML.FORMAT_STRING_ELEM);
	Element format_statement = (Element) format_element.getFirstChild();

	try
	  {
	    // open collectionConfig.xml and read in to w3 Document
	    String collection_config = directory + "collectionConfig.xml";
	    Document config = this.converter.getDOM(new File(collection_config), "UTF-8");

	    //String tag_name = "";
	    int k;
	    int index;
	    Element elem;
	    // Try importing entire tree to this.doc so we can add and remove children at ease
	    //Node current_node = this.doc.importNode(GSXML.getChildByTagName(config, "CollectionConfig"),true);
	    Node current_node = GSXML.getChildByTagName(config, "CollectionConfig");
	    NodeList current_node_list;

	    if (service.equals("ClassifierBrowse"))
	      {
		//tag_name = "browse";
		// if CLX then need to look in <classifier> X then <format>
		// default is <browse><format>

		current_node = GSXML.getChildByTagName(current_node, "browse");

		// find CLX
		if (classifier != null)
		  {
		    current_node_list = GSXML.getChildrenByTagName(current_node, "classifier");
		    index = Integer.parseInt(classifier.substring(2)) - 1;

		    // index should be given by X-1
		    current_node = current_node_list.item(index);
		    // what if classifier does not have a format tag?
		    if (GSXML.getChildByTagName(current_node, "format") == null)
		      {
			// well then create a format tag
			Element format_tag = config.createElement("format");
			current_node.appendChild(format_tag);
		      }
		  }
		else
		  {
		    // To support all classifiers, set classifier to null?  There is the chance here that the format tag does not exist
		    if (GSXML.getChildByTagName(current_node, "format") == null)
		      {
			// well then create a format tag
			Element format_tag = config.createElement("format");
			current_node.appendChild(format_tag);
		      }
		  }
	      }
	    else if (service.equals("AllClassifierBrowse"))
	      {
		current_node = GSXML.getChildByTagName(current_node, "browse");
		if (GSXML.getChildByTagName(current_node, "format") == null)
		  {
		    // well then create a format tag
		    Element format_tag = config.createElement("format");
		    current_node.appendChild(format_tag);
		  }
	      }
	    else
	      {
		// look in <format> with no attributes
		current_node_list = GSXML.getChildrenByTagName(current_node, "search");
		for (k = 0; k < current_node_list.getLength(); k++)
		  {
		    current_node = current_node_list.item(k);
		    // if current_node has no attributes then break
		    elem = (Element) current_node;
		    if (elem.hasAttribute("name") == false)
		      break;
		  }
	      }

	    current_node.replaceChild(config.importNode(format_statement, true), GSXML.getChildByTagName(current_node, "format"));

	    // Now convert config document to string for writing to file
	    String new_config = this.converter.getString(config);

	    new_config = StringUtils.replace(new_config, "&lt;", "<");
	    new_config = StringUtils.replace(new_config, "&gt;", ">");
	    new_config = StringUtils.replace(new_config, "&quot;", "\"");

	    // Write to file (not original! for now)
	    BufferedWriter writer = new BufferedWriter(new FileWriter(collection_config + ".new"));
	    writer.write(new_config);
	    writer.close();

	  }
	catch (Exception ex)
	  {
	    logger.error("There was an exception " + ex);

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw, true);
	    ex.printStackTrace(pw);
	    pw.flush();
	    sw.flush();
	    logger.error(sw.toString());
	  }

      }

    return response;
  }
}
