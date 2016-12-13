/*
 *    OAIPMH.java
 *    Copyright (C) 2010 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OAIXML;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.XMLConverter;

import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.DBInfo;
// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// General Java classes
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/** Implements the oai metadata retrieval service for GS3 collections.
 *  Dig into each collection's database and retrieve the metadata
 *
 */

public class OAIPMH extends ServiceRack {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.OAIPMH.class.getName());
  
  protected SimpleCollectionDatabase coll_db = null;
  protected SimpleCollectionDatabase oaiinf_db = null;
  
  protected String site_name = "";
  protected String coll_name = "";
 
  // set this up during configure
  protected Element list_sets_response = null;
  
  protected Element meta_formats_definition = null;
  protected HashMap<String, HashSet<String>> format_elements_map = null;
  protected HashMap<String, Element> format_response_map = null;
  /** constructor */
  public OAIPMH() {

  }
  
  public void cleanUp() {
    super.cleanUp();//??
    this.coll_db.closeDatabase();
    this.oaiinf_db.closeDatabase();
  }
  /** configure this service 
  info is the OAIPMH service rack from collectionConfig.xml, and 
  extra_info is buildConfig.xml */
  public boolean configure(Element info, Element extra_info) {
    if (!super.configure(info, extra_info)){
      logger.info("Configuring ServiceRack.java returns false.");
      return false;
    }
    
    //get the names from ServiceRack.java
    this.site_name = this.router.getSiteName();
    this.coll_name = this.cluster_name;
    
    logger.info("Configuring OAIPMH...");

    this.config_info = info;
    
    // the index stem is either specified in the buildConfig.xml file (extra_info) or uses the collection name
    Element metadata_list = (Element) GSXML.getChildByTagName(extra_info, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
    String index_stem = "";
    String infodb_type = "";
    if (metadata_list != null) {
      
      Element index_stem_elem = (Element) GSXML.getNamedElement(metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "indexStem");
      
      if (index_stem_elem != null) {
	index_stem = GSXML.getNodeText(index_stem_elem);
      }

      Element infodb_type_elem = (Element) GSXML.getNamedElement(metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "infodbType");
      if (infodb_type_elem != null) {
	infodb_type = GSXML.getNodeText(infodb_type_elem);
      }

    }

    if (index_stem == null || index_stem.equals("")) {
	index_stem = this.cluster_name; // index_stem is the name of the db in indext/text, it is <colname>.<db>
    }
    if (infodb_type == null || infodb_type.equals("")) {
      infodb_type = "gdbm"; // the default
    }

    coll_db = new SimpleCollectionDatabase(infodb_type);
    if (!coll_db.databaseOK()) {
      logger.error("Couldn't create the collection database of type "+infodb_type);
      return false;
    }

    oaiinf_db = new SimpleCollectionDatabase(infodb_type);
    if (!oaiinf_db.databaseOK()) {
      logger.error("Couldn't create the oai-inf database of type "+infodb_type);
      oaiinf_db = null;
      return false;
    }

    
    // Open databases for querying
    String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, index_stem, infodb_type);
    if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ)) {
      logger.error("Could not open collection database!");
      return false;
    }
    // the oaiinf_db is called oai-inf.<infodb_type_extension>
    String oaiinf_db_file = GSFile.OAIInfoDatabaseFile(this.site_home, this.cluster_name, "oai-inf", infodb_type);
    if (oaiinf_db != null && !this.oaiinf_db.openDatabase(oaiinf_db_file, SimpleCollectionDatabase.READ)) {
      logger.warn("Could not open oai-inf database for collection + " + this.cluster_name + "!");
    }
    
    // work out what sets this collection has. Will usually contain the collection itself, optional super collection, and maybe subcolls if appropriate classifiers are present.
    configureSetInfo();
    // the short_service_info is used by the message router to find the method names, 

    Element list_records = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
    list_records.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_RECORDS);
    list_records.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_records);

    Element list_identifiers = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
    list_identifiers.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_IDENTIFIERS);
    list_identifiers.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_identifiers);
    
    Element list_sets = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
    list_sets.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_SETS);
    list_sets.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_sets);
    
    Element list_metadata_formats = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
    list_metadata_formats.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_METADATA_FORMATS);
    list_metadata_formats.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_metadata_formats);

    Element get_record = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
    get_record.setAttribute(GSXML.NAME_ATT, OAIXML.GET_RECORD);
    get_record.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(get_record);
    
    return true;
  }

  public boolean configureOAI(Element oai_config_elem) {
    this.meta_formats_definition = this.desc_doc.createElement(OAIXML.LIST_METADATA_FORMATS);
    this.format_response_map = new HashMap<String, Element>();
    this.format_elements_map = new HashMap<String, HashSet<String>>();

    // for now, all we want is the metadata prefix description and the mapping list
    Element main_lmf_elem = (Element) GSXML.getChildByTagName(oai_config_elem, OAIXML.LIST_METADATA_FORMATS);
    if (main_lmf_elem == null) {
      logger.error("No listMetadataFormats element found in OAIConfig.xml");
      return false;
    }
    NodeList meta_formats_list = this.config_info.getElementsByTagName(OAIXML.METADATA_FORMAT);
    if (meta_formats_list.getLength() == 0) {
      logger.error("no metadataFormat elements found in OAIPMH serviceRack element");
      return false;
    }
    boolean found_meta_format = false;
    for(int i=0; i<meta_formats_list.getLength(); i++) {
      Element mf = (Element) meta_formats_list.item(i);
      String prefix = mf.getAttribute(OAIXML.METADATA_PREFIX);
      if (prefix.equals("")) {
	logger.error("metadataFormat element had no metadataPrefix attribute");
	continue;
      }
      // get the right format from OAICOnfig
      Element meta_format = findNamedMetadataFormat(main_lmf_elem, prefix);
      if (meta_format == null) {
	logger.error("Couldn't find metadataFormat named "+prefix+" in OAIConfig.xml");
	continue;
      }
      // copy the format definition into our stored Element
      Element collection_version_format = (Element) this.desc_doc.importNode(meta_format, true);
      collection_version_format.setAttribute(GSXML.NAME_ATT, prefix); // for convenience
      this.meta_formats_definition.appendChild(collection_version_format);
      // set up the response element for this format
      format_response_map.put(prefix, OAIXML.getMetadataFormatShort(this.desc_doc, collection_version_format));
      // add in collection specific mappings
      addCollectionMappings(collection_version_format, mf);
      // now set up a list of all collection elements for reverse lookup of the mapping
      format_elements_map.put(prefix, getAllCollectionElements(collection_version_format));
      
    }
    return true;
  }

  protected Element findNamedMetadataFormat(Element list_meta_formats, String prefix) {
    NodeList formats = list_meta_formats.getElementsByTagName(OAIXML.METADATA_FORMAT);
    for (int i=0; i<formats.getLength(); i++) {
      Element format = (Element)formats.item(i);
      String meta_name = GSXML.getNodeText((Element)GSXML.getChildByTagName(format, OAIXML.METADATA_PREFIX));
      if (prefix.equals(meta_name)) {
	return format;
      }
    }
    return null;
  }
    
    /** goes through the mappings from the collection one, and replaces existing ones in the main one */
    protected void addCollectionMappings(Element main_meta_format, Element coll_meta_format) {

      Element element_list = (Element)GSXML.getChildByTagName(main_meta_format, OAIXML.ELEMENT+GSXML.LIST_MODIFIER);
      Document doc = element_list.getOwnerDocument();
      NodeList coll_elements = coll_meta_format.getElementsByTagName(OAIXML.ELEMENT);
      if (coll_elements.getLength()==0) {
	// no mappings to include
	return;
      }
      for (int i=0; i<coll_elements.getLength(); i++) {
	Element e = (Element)coll_elements.item(i);
	String elem_name = e.getAttribute(GSXML.NAME_ATT);
	Element main_elem = GSXML.getNamedElement(element_list, OAIXML.ELEMENT, GSXML.NAME_ATT, elem_name);
	if (main_elem == null) {
	  logger.error(elem_name+" not found in meta format, not using it");
	} else {
	  element_list.replaceChild(doc.importNode(e, true),main_elem );
      }
      }
    }

    /** goes through all the mappings and makes a set of all collection
	metadata names that could become an oai meta element - acts as
	a reverse lookup for the mappings */
    protected HashSet<String> getAllCollectionElements(Element meta_format) {
      HashSet<String> meta_name_set = new HashSet<String>();
      NodeList elements = meta_format.getElementsByTagName(OAIXML.ELEMENT);
      for (int i=0; i<elements.getLength(); i++) {
	Element e = (Element)elements.item(i);
	Element map = (Element)GSXML.getChildByTagName(e, OAIXML.MAPPING);
	if (map == null) {
	  // there is no mapping, just use the element name
	  meta_name_set.add(e.getAttribute(GSXML.NAME_ATT));
	} else {
	  String list_of_names = map.getAttribute(OAIXML.ELEMENTS);
	  String[] name_array = list_of_names.split(",");
	  for (int j=0; j<name_array.length; j++) {
	    meta_name_set.add(name_array[j]);
	  }
	}
      }
      return meta_name_set;
    }
   
  /** returns a specific service description */
  public Element getServiceDescription(Document doc, String service_id, String lang, String subset) {
    
    if (service_id.equals(OAIXML.LIST_RECORDS)) {
      Element list_records = doc.createElement(GSXML.SERVICE_ELEM);
      list_records.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_RECORDS);
      list_records.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_records;
    }
    
    if (service_id.equals(OAIXML.LIST_IDENTIFIERS)) {
      Element list_identifiers = doc.createElement(GSXML.SERVICE_ELEM);
      list_identifiers.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_IDENTIFIERS);
      list_identifiers.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_identifiers;
    }
    if (service_id.equals(OAIXML.LIST_SETS)) {
      Element list_sets = doc.createElement(GSXML.SERVICE_ELEM);
      list_sets.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_SETS);
      list_sets.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_sets;
    }
    if (service_id.equals(OAIXML.LIST_METADATA_FORMATS)) {
      Element list_metadata_formats = doc.createElement(GSXML.SERVICE_ELEM);
      list_metadata_formats.setAttribute(GSXML.NAME_ATT, OAIXML.LIST_METADATA_FORMATS);
      list_metadata_formats.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_metadata_formats;
    }
    
    if (service_id.equals(OAIXML.GET_RECORD)) {
      Element get_record = doc.createElement(GSXML.SERVICE_ELEM);
      get_record.setAttribute(GSXML.NAME_ATT, OAIXML.GET_RECORD);
      get_record.setAttribute(GSXML.TYPE_ATT, "oai");
      return get_record;
    }
    
    return null;
  }

  /** The list sets service returns all the sets that this collection is/is part of/contains. This is gathered by Receptionist from all collections to answer the OAI ListSets request.  */
  protected Element processListSets(Element req) {
    return list_sets_response;
  }
  /** returns the actual record element used in the OAI GetRecord response */
  protected Element processGetRecord(Element req) {
    /** arguments:
        identifier: required
        metadataPrefix: required
     *  Exceptions: badArgument; cannotDisseminateFormat; idDoesNotExist
     */ 
    NodeList params = GSXML.getChildrenByTagName(req, GSXML.PARAM_ELEM);
    HashMap<String, String> param_map = GSXML.getParamMap(params);    
    
    String prefix = param_map.get(OAIXML.METADATA_PREFIX);
    if (prefix == null || prefix.equals("")) {
      //Just a double-check
      logger.error("the value of metadataPrefix att is not present in the request.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    
    // check that we support this format
    if (!format_response_map.containsKey(prefix)) {
      logger.error("metadata prefix is not supported for collection "+this.coll_name);
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }

    Document doc = XMLConverter.newDOM();
    
    String oid = param_map.get(OAIXML.OID); // TODO should this be identifier???
    boolean OID_is_deleted = false;
    long millis = -1;

    DBInfo oai_info = null;
    if(oaiinf_db != null) {
	oai_info = this.oaiinf_db.getInfo(oid);
	if (oai_info == null) {
	    logger.warn("OID: " + oid + " is not present in the collection's oai-inf database.");
	} else  {
	    String oaiinf_status = oai_info.getInfo(OAIXML.OAI_INF_STATUS);
	    if(oaiinf_status != null && oaiinf_status.equals(OAIXML.OAI_INF_DELETED)) {
		OID_is_deleted = true;

		// get the right timestamp for deletion: from oaiinf db
		String timestamp = oai_info.getInfo(OAIXML.OAI_INF_TIMESTAMP); // in seconds presumably, like oailastmodified in the collection index db		
		
		millis = Long.parseLong(timestamp)*1000; // in milliseconds
	    }
	}
    }

    //get a DBInfo object of the identifier; if this identifier is not present in the database,
    // null is returned.
    DBInfo info = this.coll_db.getInfo(oid);
    if (info == null) {
      logger.error("OID: " + oid + " is not present in the collection database.");
      //return OAIXML.createErrorResponse(OAIXML.ID_DOES_NOT_EXIST, ""); // may exist as deleted in oai-inf db
    }
    else if (millis == -1) { // so !OID_is_deleted, get oailastmodified from collection's index db
	ArrayList<String> keys = new ArrayList<String>(info.getKeys());	
	millis = getDateStampMillis(info);	
    }
    String oailastmodified = (millis == -1) ? "" : OAIXML.getTime(millis);
    

    Element get_record_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element get_record = doc.createElement(OAIXML.GET_RECORD);
    get_record_response.appendChild(get_record);
    Element record = doc.createElement(OAIXML.RECORD);
    //compose the header element
    record.appendChild(createHeaderElement(doc, oid, oailastmodified, OID_is_deleted));      
    if(!OID_is_deleted) {
	//compose the metadata element
	record.appendChild(createMetadataElement(doc, prefix, info));
    }
    get_record.appendChild(record);
    return get_record_response;
  }

  /** return a list of records in specified set, containing metadata from specified prefix*/
  protected Element processListRecords(Element req) {
    return processListIdentifiersOrRecords(req, OAIXML.LIST_RECORDS, true);
  }

  /** return a list of identifiers in specified set that contain metadata belonging to specified prefix. */
  protected Element processListIdentifiers(Element req) {
    return processListIdentifiersOrRecords(req, OAIXML.LIST_IDENTIFIERS, false);
  }

  // Get a list of records/identifiers that match the parameters.
  protected Element processListIdentifiersOrRecords(Element req, String response_name, boolean include_metadata) {
    /** arguments:
        metadataPrefix: required
     *  from: optional
     *  until: optional
     *  set: optional
     *  resumptionToken: exclusive and optional (ignored as it has been handled by OAIReceptionist)
     *  Exceptions: badArgument; cannotDisseminateFormat; idDoesNotExist
     */ 
    NodeList params = GSXML.getChildrenByTagName(req, GSXML.PARAM_ELEM);
    
    if(params.getLength() == 0) {
      logger.error("must at least have the metadataPrefix parameter, can't be none");
      return OAIXML.createErrorResponse(OAIXML.BAD_ARGUMENT, "");
    }
    
    HashMap<String, String> param_map = GSXML.getParamMap(params);  
    
    String prefix = "";
    Date from_date = null;
    Date until_date = null;
    
    if(param_map.containsKey(OAIXML.METADATA_PREFIX) == false) {    
      //Just a double-check
      logger.error("A param element containing the metadataPrefix is not present.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    prefix = param_map.get(OAIXML.METADATA_PREFIX);
    if (prefix == null || prefix.equals("")) {
      //Just a double-check
      logger.error("the value of metadataPrefix att is not present in the request.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    
    if(param_map.containsKey(OAIXML.FROM)) {
      String from = param_map.get(OAIXML.FROM);
      from_date = OAIXML.getDate(from);
    }    
    if(param_map.containsKey(OAIXML.UNTIL)) {
      String until = param_map.get(OAIXML.UNTIL);
      until_date = OAIXML.getDate(until);
    }    

    if (!format_response_map.containsKey(prefix)) {
      logger.error(prefix + " metadata prefix is not supported for collection "+this.coll_name);
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }

    // get list of oids
    ArrayList<String> oid_list = null;
    if(oaiinf_db != null) { // try getting the OIDs from the oaiinf_db
	oid_list = new ArrayList<String>(oaiinf_db.getAllKeys());
    
	if(oid_list == null) { // try getting the OIDs from the oai entries in the index db
	    logger.warn("@@@@@@@@@@@@@ NO OIDs in oai-inf db for " + this.cluster_name);
	    oid_list = getChildrenIds(OAIXML.BROWSELIST);
	}
    }

    if (oid_list == null) {
      logger.error("No matched records found in collection: oai-inf and index db's browselist are empty");
      return OAIXML.createErrorResponse(OAIXML.NO_RECORDS_MATCH, "");
    }
    // all validation is done

    // get the list of elements that are in this metadata prefix
    HashSet<String> set_of_elems = format_elements_map.get(prefix);

    Document doc = XMLConverter.newDOM();
    Element list_items_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element list_items = doc.createElement(response_name);
    list_items_response.appendChild(list_items);

    for(int i=0; i<oid_list.size(); i++) {
      String oid = oid_list.get(i);
      boolean OID_is_deleted = false;
      long millis = -1;

      DBInfo oai_info = null;
      if(oaiinf_db != null) {
	  oai_info = this.oaiinf_db.getInfo(oid);
	  if (oai_info == null) {
	      logger.warn("OID: " + oid + " is not present in the collection's oai-inf database.");
	  } else  {
	      String oaiinf_status = oai_info.getInfo(OAIXML.OAI_INF_STATUS);
	      if(oaiinf_status != null && oaiinf_status.equals(OAIXML.OAI_INF_DELETED)) {
		  OID_is_deleted = true;
		  
		  // get the right timestamp for deletion: from oaiinf db
		  String timestamp = oai_info.getInfo(OAIXML.OAI_INF_TIMESTAMP); // in seconds presumably, like oailastmodified in the collection index db		
		  
		  millis = Long.parseLong(timestamp)*1000; // in milliseconds
	      }
	  }
      }
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) { // can happen if oid was deleted, in which case only oai_info keeps a record of it
        logger.error("Collection database does not contain information about oid: " +oid);
      }
      else if (millis == -1) { // so !OID_is_deleted, get oailastmodified from collection's index db
	  
	  millis = getDateStampMillis(info);
      }

      Date this_date = null;
      if (millis == -1) {
	  if (from_date != null || until_date !=null) {
	      continue; // if this doc doesn't have a date for some reason, and
	      // we are doing a date range, then don't include it.
	  }
      } else {
	  this_date = new Date(millis);
	  if (from_date != null) {
	      if(this_date.before(from_date)) {
		  continue;
	      }
	  }
	  if (until_date != null) {
	      if (this_date.after(until_date)) {
		  continue;
	      }
	  }    
      }
      
      
      // compose a record for adding header and metadata
      Element record = doc.createElement(OAIXML.RECORD);
      list_items.appendChild(record);
      //compose the header element
      record.appendChild(createHeaderElement(doc, oid, OAIXML.getTime(millis), OID_is_deleted));


      //Now check that this id has metadata for the required prefix.
      if (info != null && documentContainsMetadata(info, set_of_elems)) {
	  // YES, it does have some metadata for this prefix	    
	  
	    if (include_metadata) {		
		//compose the metadata element
		record.appendChild(createMetadataElement(doc, prefix, info));
	    } /*else {
	      //compose the header element and append it
	      list_items.appendChild(createHeaderElement(doc, oid, OAIXML.getTime(millis)));      
	      }*/
      } // otherwise we won't include this oid. with meta
      
      
      
    }//end of for(int i=0; i<oid_list.size(); i++) of doing thru each record
    
    return list_items_response;        
    
  }

  
  // have implemented setDescription as an element, instead of a container containing metadata
  private boolean configureSetInfo() {

    Document doc = XMLConverter.newDOM();
    this.list_sets_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element list_sets_elem = doc.createElement(OAIXML.LIST_SETS);
    this.list_sets_response.appendChild(list_sets_elem);
    String set_name = this.coll_name;
    String set_description = null;
    Element name_elem = (Element)GSXML.getChildByTagName(this.config_info, OAIXML.SET_NAME);
    if (name_elem!=null) {
      set_name = GSXML.getNodeText(name_elem);
      if (set_name.equals("")) {
	set_name = this.coll_name; // default to coll name if can't find one
      }
    }
    Element description_elem = (Element)GSXML.getChildByTagName(this.config_info, OAIXML.SET_DESCRIPTION);
    if (description_elem!=null) {
      set_description = GSXML.getNodeText(description_elem);
      if (set_description.equals("")) {
	set_description = null;
      }
    }
    Element coll_set = OAIXML.createSet(doc, this.coll_name, set_name, set_description);
    list_sets_elem.appendChild(coll_set);
    
    // are we part of any super sets?
    NodeList super_set_list = GSXML.getChildrenByTagName(this.config_info, OAIXML.OAI_SUPER_SET);
    for (int i=0; i<super_set_list.getLength(); i++) {
      String super_name = ((Element)super_set_list.item(i)).getAttribute(GSXML.NAME_ATT);
      if (super_name != null && !super_name.equals("")) {
	list_sets_elem.appendChild(OAIXML.createSet(doc, super_name, super_name, null));
      }
    }
    return true;
  }

 /** create the metadata element used when processing ListRecords/GetRecord requests
   */
  protected Element createMetadataElement(Document doc, String prefix, DBInfo info) {
    // the <metadata> element
    Element metadata = doc.createElement(OAIXML.METADATA);
    // the <oai:dc namespace...> element
    Element prfx_str_elem = OAIXML.getMetadataPrefixElement(doc, prefix, OAIXML.oai_version);
    metadata.appendChild(prfx_str_elem);

    Element meta_format_element = GSXML.getNamedElement(this.meta_formats_definition, OAIXML.METADATA_FORMAT, GSXML.NAME_ATT, prefix);
    NodeList elements = meta_format_element.getElementsByTagName(OAIXML.ELEMENT);
    // for each element in the definition
    for (int i=0; i<elements.getLength(); i++) {
      Element e = (Element)elements.item(i);
      Element map = (Element)GSXML.getChildByTagName(e, OAIXML.MAPPING);
      if (map == null) {
	// look up the element name
	addMetadata(prfx_str_elem, e.getAttribute(GSXML.NAME_ATT), info);
      } else {
	// we go though the list of names in the mapping
	addMetadata(prfx_str_elem, e.getAttribute(GSXML.NAME_ATT), map.getAttribute(OAIXML.SELECT), map.getAttribute(OAIXML.ELEMENTS), info);
      }
    }
    // output any metadata that is not just a simple mapping
    addCustomMetadata(prfx_str_elem, prefix, info);
    return metadata;
  }

  /** a simple addMetadata where we look for meta_name metadata, and add as that name*/
  protected void addMetadata(Element meta_list_elem, String meta_name, DBInfo info) {
    Vector<String> values = info.getMultiInfo(meta_name);
    if (values != null && values.size()!=0) {
      for (int i=0; i<values.size(); i++) {
	addMetadataElement(meta_list_elem, meta_name, values.get(i));
      }
    }
  }

  /** more complicated addMetadata - can add multiple items. */
  protected void addMetadata(Element meta_list_elem, String new_meta_name, String select_type, String name_list, DBInfo info) {
    String[] names = name_list.split(",");
    for (int i=0; i<names.length; i++) {
      Vector<String> values = info.getMultiInfo(names[i]);
      if (values == null || values.size()==0) {
	continue;
      }
      for (int j=0; j<values.size(); j++) {
	addMetadataElement(meta_list_elem, new_meta_name, values.get(j));
	if (select_type.equals(OAIXML.SELECT_SINGLE_VALUE)) {
	  return; // only want to add one value
	}
      }
      if (select_type.equals(OAIXML.SELECT_FIRST_VALID_META)) {
	return; // we have added all values of this meta elem
      }
      // otherwise, we will keep going through the list and add them all.
    }
  }
  
  // specific metadata formats might need to do some custom metadata that is not
  //just a standard mapping. eg oai_dc outputting an identifier that is a link
  protected void addCustomMetadata(Element meta_list_elem, String prefix, DBInfo info) {
    
    if (prefix.equals(OAIXML.META_FORMAT_DC)) {
      // we want to add in another dc:identifier element with a link to the resource if possible
      // try gs.OAIResourceURL first, then srclinkFile, then GS version of documnet
      String gsURL = info.getInfo(OAIXML.GS_OAI_RESOURCE_URL);
      if (gsURL.equals("")) {
	String base_url = OAIXML.getBaseURL(); // e.g. e.g. http://host:port/greenstone3/oaiserver
	// try srclinkFile
	gsURL = info.getInfo("srclinkFile");
	if (!gsURL.equals("")) {
	  // make up the link to the file
	  gsURL = base_url.replace("oaiserver", "") + "sites/" + this.site_name 
	    + "/collect/" + this.coll_name + "/index/assoc/" 
	    + info.getInfo("assocfilepath") + "/" + gsURL; 
	} else {
	  // no srclink file, lets provide a link to the greenstone doc
	  gsURL = base_url.replace("oaiserver", "library") + "/collection/" + this.coll_name + "/document/" + info.getInfo("Identifier");
	}
      }
      // now we have the url link, add as metadata
      addMetadataElement(meta_list_elem, "dc:identifier", gsURL);
    }
  }

  /** create the actual metadata element for the list */
  protected void addMetadataElement(Element meta_list_elem, String name, String value) {
    
    Element meta = GSXML.createTextElement(meta_list_elem.getOwnerDocument(), name, value);
    meta_list_elem.appendChild(meta);
  }


  /** create a header element used when processing requests like ListRecords/GetRecord/ListIdentifiers
   */  
  protected Element createHeaderElement(Document doc, String oid, String oailastmodified, boolean deleted) {

        Element header = doc.createElement(OAIXML.HEADER);
	
	// if deleted, get the date and change oailastmodified to timestamp in oaiinfo
	if(deleted) {
	    header.setAttribute(OAIXML.OAI_INF_STATUS, OAIXML.HEADER_STATUS_ATTR_DELETED); // set the header status to deleted
		// then the timestamp for deletion will be from oai-inf database 
	}
    
        Element identifier = doc.createElement(OAIXML.IDENTIFIER);
	GSXML.setNodeText(identifier, coll_name + ":" + oid);
        header.appendChild(identifier);
        Element set_spec = doc.createElement(OAIXML.SET_SPEC);
	GSXML.setNodeText(set_spec, coll_name);
        header.appendChild(set_spec);
        Element datestamp = doc.createElement(OAIXML.DATESTAMP);
        GSXML.setNodeText(datestamp, oailastmodified);
        header.appendChild(datestamp);
        return header;
  }

  /** return the metadata information  */
  protected Element processListMetadataFormats(Element req) {
    // the request sent here must contain an OID. see doListMetadataFormats() in OAIReceptionist
    Element param = GSXML.getNamedElement(req, GSXML.PARAM_ELEM, GSXML.NAME_ATT, OAIXML.OID);
    if (param == null) {
      logger.error("An element containing the OID attribute not is present.");
      return OAIXML.createErrorResponse(OAIXML.ID_DOES_NOT_EXIST, "");
    }
    String oid = param.getAttribute(GSXML.VALUE_ATT);
    if (oid == null || oid.equals("")) {
      logger.error("No OID is present in the request.");
      return OAIXML.createErrorResponse(OAIXML.ID_DOES_NOT_EXIST, "");
    }

    /*
    ArrayList<String> oid_list = null;
    if(oaiinf_db != null) { // try getting the OIDs from the oaiinf_db
	oid_list = new ArrayList<String>(oaiinf_db.getAllKeys());
	
	if(oid_list == null) { // try getting the OIDs from the oai entries in the index db
	    oid_list = getChildrenIds(OAIXML.BROWSELIST);
	}
    }
    */
    // assume meta formats are only for OIDs that have not been deleted
    // so don't need to check oai-inf db, and can just check collection's index db for list of OIDs
    ArrayList<String> oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null || oid_list.contains(oid) == false) {
      logger.error("OID: " + oid + " is not present in the database.");
      Element e= OAIXML.createErrorResponse(OAIXML.ID_DOES_NOT_EXIST, "");
//      logger.error((new XMLConverter()).getPrettyString (e));
      return e;
    }
    
    DBInfo info = null;    
    info = this.coll_db.getInfo(oid);
    if (info == null) { //just double check
      return OAIXML.createErrorResponse(OAIXML.OAI_SERVICE_UNAVAILABLE, "");
    }
    
    Document doc = XMLConverter.newDOM();
    Element list_metadata_formats_response = doc.createElement(GSXML.RESPONSE_ELEM);
    
    Element list_metadata_formats = doc.createElement(OAIXML.LIST_METADATA_FORMATS);
    list_metadata_formats_response.appendChild(list_metadata_formats);
    boolean has_meta_format = false;
    
    // for each format in format_elements_map
    Iterator<String> it = format_elements_map.keySet().iterator();
    while (it.hasNext()) {
      String format = it.next();
      HashSet<String> set_of_elems = format_elements_map.get(format);
      if (documentContainsMetadata(info, set_of_elems)) {
	// add this format into the response
	has_meta_format = true;
	list_metadata_formats.appendChild(doc.importNode(format_response_map.get(format), true));
      }
    }

    if (has_meta_format == false) {
      logger.error("Specified metadata names are not contained in the database.");
      return OAIXML.createErrorResponse(OAIXML.NO_METADATA_FORMATS, "");
    } else {
      return list_metadata_formats_response;
    }
  }

  protected boolean documentContainsMetadata(DBInfo info, HashSet<String> set_of_elems) {
    if (set_of_elems.size() == 0) {
      return false;
    }
    Iterator<String> i = set_of_elems.iterator();
    while (i.hasNext()) {
      if (!info.getInfo(i.next()).equals("")) {
	return true;
      }
    }
    return false;
  }

  /** returns a list of the child ids in order, null if no children */
  protected ArrayList<String> getChildrenIds(String node_id) {
    DBInfo info = this.coll_db.getInfo(node_id);
    if (info == null) {
      return null;
    }
    
    String contains = info.getInfo("contains");
    if (contains.equals("")) {
      return null;
    }
    ArrayList<String> children = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(contains, ";");
    while (st.hasMoreTokens()) {
      String child_id = st.nextToken().replaceAll("\"", node_id);
      children.add(child_id);
    }
    return children;    
  }
  /**method to check whether any of the 'metadata_names' is contained in the 'info'.
   * The name may be in the form: <name>,<mapped name>, in which the mapped name is
   * optional. The mapped name is looked up in the DBInfo; if not present, use the first
   * name which is mandatory.
   */
  protected boolean containsMetadata(DBInfo info, String[] metadata_names) {
    if (metadata_names == null) return false;
    logger.info("checking metadata names in db.");
    for(int i=0; i<metadata_names.length; i++) {
      int index = metadata_names[i].indexOf(",");
      String meta_name = (index == -1) ? metadata_names[i] :
                              metadata_names[i].substring(index + 1);
      
      if(info.getInfo(meta_name).equals("") == false) {
        return true;
      }
    }
    return false;
  }

  protected long getDateStampMillis(DBInfo info) {
    // gs.OAIDateStamp is in YYYY-MM-DD
    String time_stamp = info.getInfo(OAIXML.GS_OAI_DATE_STAMP);
    long millis = -1;
    if (!time_stamp.equals("")) {
      millis = OAIXML.getTime(time_stamp);
    }
    if (millis == -1) {
      // oailastmodified is in seconds
      time_stamp = info.getInfo(OAIXML.OAI_LASTMODIFIED);
      if (!time_stamp.equals("")) {
	millis = Long.parseLong(time_stamp)*1000;
      }
    }
    return millis;
    

  }
}


