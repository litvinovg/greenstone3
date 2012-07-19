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

import java.util.Iterator;
import java.util.Vector;
import java.util.Set;

// change the class name (and the filename) to something more appropriate
public class CoverageMetadataRetrieve extends ServiceRack
{

	// add in a logger for error messages
	static Logger logger = Logger.getLogger("CoverageMetadataRetrieve");

	protected SimpleCollectionDatabase coll_db = null;
	protected String index_stem = null;

	// the new service names
	protected static final String COVERAGE_SERVICE = "CoverageMetadataRetrieve";

	// initialize any custom variables
	public CoverageMetadataRetrieve()
	{

	}

	// clean up anything that we need to 
	public void cleanUp()
	{
		super.cleanUp();
	}

	// Configure the class based in info in buildConfig.xml and collectionConfig.xml
	// info is the <serviceRack name="MyNewServicesTemplate"/> element from
	// buildConfig.xml, and extra_info is the whole collectionConfig.xml file
	// in case its needed
	public boolean configure(Element info, Element extra_info)
	{

		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring CoverageMetadataRetrieve...");

		// set up short_service_info - this currently is a list of services, 
		// with their names and service types
		// we have two services, a new textquery, and a new one of a new type
		//Element tq_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		//tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		//tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE);
		//this.short_service_info.appendChild(tq_service);

		Element diff_service = this.doc.createElement(GSXML.SERVICE_ELEM);
		diff_service.setAttribute(GSXML.TYPE_ATT, "retrieve");
		diff_service.setAttribute(GSXML.NAME_ATT, COVERAGE_SERVICE);
		this.short_service_info.appendChild(diff_service);

		// the index stem is either specified in the config file or is  the collection name
		Element index_stem_elem = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_STEM_ELEM);
		if (index_stem_elem != null)
		{
			this.index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (this.index_stem == null || this.index_stem.equals(""))
		{
			logger.error("CoverageMetadataRetrieve.configure(): indexStem element not found, stem will default to collection name");
			this.index_stem = this.cluster_name;
		}

		// find out what kind of database we have
		Element database_type_elem = (Element) GSXML.getChildByTagName(info, GSXML.DATABASE_TYPE_ELEM);
		String database_type = null;
		if (database_type_elem != null)
		{
			database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (database_type == null || database_type.equals(""))
		{
			database_type = "gdbm"; // the default
		}
		coll_db = new SimpleCollectionDatabase(database_type);
		if (!coll_db.databaseOK())
		{
			logger.error("Couldn't create the collection database of type " + database_type);
			return false;
		}

		// Open database for querying
		String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, this.index_stem, database_type);
		if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ))
		{
			logger.error("Could not open collection database!");
			return false;
		}

		// Extract any relevant information from info and extra_info
		// This can be used to set up variables.

		// If there is any formatting information, add it in to format_info_map

		// Do this for all services as appropriate
		Element format = null; // find it from info/extra_info
		if (format != null)
		{
			this.format_info_map.put(COVERAGE_SERVICE, this.doc.importNode(format, true));
		}

		return true;

	}

	// get the desription of a service. Could include parameter lists, displayText
	protected Element getServiceDescription(String service, String lang, String subset)
	{

		// check that we have been asked for the right service
		if (!service.equals(COVERAGE_SERVICE))
		{
			return null;
		}

		/*
		 * if (service.equals(QUERY_SERVICE)) { Element tq_service =
		 * this.doc.createElement(GSXML.SERVICE_ELEM);
		 * tq_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		 * tq_service.setAttribute(GSXML.NAME_ATT, QUERY_SERVICE); if
		 * (subset==null ||
		 * subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) { // add
		 * in any <displayText> elements // name, for example - get from
		 * properties file
		 * tq_service.appendChild(GSXML.createDisplayTextElement(this.doc,
		 * GSXML.DISPLAY_TEXT_NAME, getTextString(QUERY_SERVICE+".name", lang)
		 * )); }
		 * 
		 * if (subset==null ||
		 * subset.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) { // add in a
		 * param list if this service has parameters Element param_list =
		 * this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		 * tq_service.appendChild(param_list); // create any params and append
		 * to param_list } return tq_service; }
		 */

		if (service.equals(COVERAGE_SERVICE))
		{
			Element diff_service = this.doc.createElement(GSXML.SERVICE_ELEM);
			diff_service.setAttribute(GSXML.TYPE_ATT, "retrieve");
			diff_service.setAttribute(GSXML.NAME_ATT, COVERAGE_SERVICE);
			if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
			{
				// add in any <displayText> elements
				// name, for example - get from properties file
				diff_service.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(COVERAGE_SERVICE + ".name", lang)));
			}

			if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
			{
				// add in a param list if this service has parameters
				Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				diff_service.appendChild(param_list);
				// create any params and append to param_list
			}

			return diff_service;
		}

		// not a valid service for this class
		return null;

	}

	/** This is the method that actually handles the TextQuery Service */
	//protected Element processTextQuery(Element request) {

	//Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
	//result.setAttribute(GSXML.FROM_ATT, QUERY_SERVICE);
	//result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	// fill in the rest
	//return result;
	//}

	/** This is the method that actually handles the MyDifferentService service */
	protected Element processCoverageMetadataRetrieve(Element request)
	{

		if (!this.coll_db.databaseOK())
		{
			logger.error("No valid database found\n");
			return null;
		}

		DBInfo collection_info = this.coll_db.getInfo("collection");

		Set<String> keys = collection_info.getKeys();

		Vector<String> valid_keys = new Vector<String>();

		// Iterate over keys and add valid ones to the valid_keys vector
		String current_key = null;
		Iterator<String> iter = keys.iterator();

		while (iter.hasNext())
		{
			current_key = iter.next();
			if (current_key.matches("^metadatalist-([a-zA-Z][^-])*$"))
			{
				logger.error("********** ADDING " + current_key + " TO VALID KEYS LIST **********\n");
				valid_keys.add(current_key);
			}
		}

		// Create response
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, COVERAGE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		Element metadataSetList = this.doc.createElement("metadataSetList");
		result.appendChild(metadataSetList);

		// Iterate over valid keys and build up response
		Element metadataSet = null;
		Element metadata = null;
		String value = null;
		String name = null;
		iter = valid_keys.iterator();

		while (iter.hasNext())
		{
			current_key = iter.next();

			// Create metadataSet using the current key and add to metadataSetList
			metadataSet = this.doc.createElement("metadataSet");
			if (current_key.indexOf("-") != -1)
			{
				name = current_key.split("-")[1];
			}
			metadataSet.setAttribute(GSXML.NAME_ATT, name);
			metadataSetList.appendChild(metadataSet);

			// Create a metadata element for each value and add to metadataSet
			Vector<String> sub_info = collection_info.getMultiInfo(current_key);
			Iterator<String> iter2 = sub_info.iterator();
			while (iter2.hasNext())
			{
				value = iter2.next();
				metadata = this.doc.createElement("metadata");
				metadata.setAttribute(GSXML.NAME_ATT, value);
				metadataSet.appendChild(metadata);
			}

		}

		return result;

	}
}
