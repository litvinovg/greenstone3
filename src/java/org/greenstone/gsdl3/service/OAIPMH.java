/*
 *    OAIPMH.java
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
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/** Implements the oai metadata retrieval service for GS3 collections.
 *  Dig into each collection's database and retrieve the metadata
 *
 */

public class OAIPMH extends ServiceRack {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.OAIPMH.class.getName());
  
  protected SimpleCollectionDatabase coll_db = null;
  
  protected String site_name = "";
  protected String coll_name = "";
 
  // set this up during configure
  protected Element list_sets_response = null;
  
  /** constructor */
  public OAIPMH() {

  }
  
  public void cleanUp() {
    super.cleanUp();//??
    this.coll_db.closeDatabase();
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
      index_stem = this.cluster_name;
    }
    if (infodb_type == null || infodb_type.equals("")) {
      infodb_type = "gdbm"; // the default
    }

    coll_db = new SimpleCollectionDatabase(infodb_type);
    if (!coll_db.databaseOK()) {
      logger.error("Couldn't create the collection database of type "+infodb_type);
      return false;
    }
    
    // Open database for querying
    String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, index_stem, infodb_type);
    if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ)) {
      logger.error("Could not open collection database!");
      return false;
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
  // /** return the metadata information about this set of the repository */
  // protected Element processIdentify(Element req) {
  //   return null;
  // }
  /** return the metadata information  */
  protected Element processListSets(Element req) {
    return list_sets_response;
  }
  /** return the metadata information  */
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
    
    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    
    String oid = param_map.get(OAIXML.OID); // TODO should this be identifier???

    //get a DBInfo object of the identifier; if this identifier is not present in the database,
    // null is returned.
    DBInfo info = this.coll_db.getInfo(oid);
    if (info == null) {
      logger.error("OID: " + oid + " is not present in the database.");
      return OAIXML.createErrorResponse(OAIXML.ID_DOES_NOT_EXIST, "");
    }

    Document doc = XMLConverter.newDOM();
    ArrayList<String> keys = new ArrayList<String>(info.getKeys());
    String oailastmodified = "";
    if(keys.contains(OAIXML.OAI_LASTMODIFIED)) {
      oailastmodified = info.getInfo(OAIXML.OAI_LASTMODIFIED);
      oailastmodified = OAIXML.getTime(Long.parseLong(oailastmodified)*1000); // java wants dates in milliseconds
    }

    Element get_record_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element get_record = doc.createElement(OAIXML.GET_RECORD);
    get_record_response.appendChild(get_record);
    Element record = doc.createElement(OAIXML.RECORD);
    //compose the header element
    record.appendChild(createHeaderElement(doc, oid, oailastmodified));      
    //compose the metadata element
    record.appendChild(createMetadataElement(doc, prefix, info, metadata_format));
    get_record.appendChild(record);
    return get_record_response;
  }
  /** return a list of identifiers  */
  protected Element processListIdentifiers(Element req) {
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

    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    ArrayList<String> oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null) {
      logger.error("No matched records found in collection: browselist is empty");
      return OAIXML.createErrorResponse(OAIXML.NO_RECORDS_MATCH, "");
    }
    // all validation is done

    Document doc = XMLConverter.newDOM();
    Element list_identifiers_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element list_identifiers = doc.createElement(OAIXML.LIST_IDENTIFIERS);
    list_identifiers_response.appendChild(list_identifiers);

    for(int i=0; i<oid_list.size(); i++) {
      String oid = oid_list.get(i);
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) {
        logger.error("Database does not contains information about oid: " +oid);
        continue;
      }
      ArrayList<String> keys = new ArrayList<String>(info.getKeys());
      String oailastmodified = "";
      if(keys.contains(OAIXML.OAI_LASTMODIFIED)) {
        oailastmodified = info.getInfo(OAIXML.OAI_LASTMODIFIED);
	oailastmodified = OAIXML.getTime(Long.parseLong(oailastmodified)*1000); // java wants dates in milliseconds
      }
      
      Date this_date = OAIXML.getDate(oailastmodified);        
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
      //compose the header element and append it
      list_identifiers.appendChild(createHeaderElement(doc, oid, oailastmodified));      
    }//end of for(int i=0; i<oid_list.size(); i++) of doing thru each record
    
    return list_identifiers_response;        
  }
  /** return a list of records  */
  protected Element processListRecords(Element req) {
    /** the request sent here may contain optional 'from', 'untill', 'metadataPrefix', 
     * and 'resumptionToken' params. see doListSets() in OAIReceptionist.
     * if the request contains 'resumptionToken' then it should have been handled by the
     * OAIReceptionist. Therefore, the request sent here must not contain 'resumptionToken'
     * argument but a 'metadataPrefix' param. The OAIReceptionist makes sure of this.
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
    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.createErrorResponse(OAIXML.CANNOT_DISSEMINATE_FORMAT, "");
    }
    
    //get a list of identifiers (it contains a list of strings)
    ArrayList<String> oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null) {
      logger.error("No matched records found in collection: browselist is empty");
      return OAIXML.createErrorResponse(OAIXML.NO_RECORDS_MATCH, "");
    }
    // all validation is done

    Document doc = XMLConverter.newDOM();
    Element list_records_response = doc.createElement(GSXML.RESPONSE_ELEM);
    Element list_records = doc.createElement(OAIXML.LIST_RECORDS);
    list_records_response.appendChild(list_records);
    for(int i=0; i<oid_list.size(); i++) {
      String oid = oid_list.get(i);
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) {
        logger.error("Database does not contains information about oid: " +oid);
        continue;
      }
      ArrayList<String> keys = new ArrayList<String>(info.getKeys());
      String oailastmodified = "";
      if(keys.contains(OAIXML.OAI_LASTMODIFIED)) {
        oailastmodified = info.getInfo(OAIXML.OAI_LASTMODIFIED);
	oailastmodified = OAIXML.getTime(Long.parseLong(oailastmodified)*1000); // java wants dates in milliseconds
      }
      
      Date this_date = OAIXML.getDate(oailastmodified);        
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
      
      Element record = doc.createElement(OAIXML.RECORD);
      list_records.appendChild(record);
      //compose the header element
      record.appendChild(createHeaderElement(doc, oid, oailastmodified));      
      //compose the metadata element
      record.appendChild(createMetadataElement(doc, prefix, info, metadata_format));
      
    }//end of for(int i=0; i<oid_list.size(); i++) of doing thru each record
    
    return list_records_response;    
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
  /** get the metadataFormat element from the collectionConfig.xml containing the specified metadata prefix.
   *  return null if not found.
   */
  private Element getMetadataFormatElement(String prefix) {
    Element list_meta_format = (Element)GSXML.getChildByTagName(this.config_info, OAIXML.LIST_METADATA_FORMATS);
    Element metadata_format = GSXML.getNamedElement(list_meta_format, OAIXML.METADATA_FORMAT, OAIXML.METADATA_PREFIX, prefix);
    return metadata_format;
  }
  /** create the metadata element used when processing ListRecords/GetRecord requests
   */
  private Element createMetadataElement(Document doc, String prefix, DBInfo info, Element metadata_format) {
        //the prefix string is in the form: oai_dc, for example.
        String prfx_str = "";
        //the metadata namespace used to retrieve metadata in the repository
        //For example, if the prefix is like 'oai_ex' then we used 'ex' to get the metadata
        //Normally we would use 'dc' to find metadata.
        String meta_ns = "";
        if(prefix.equals(OAIXML.OAI_DC)) {
          if(OAIXML.oai_version.equals(OAIXML.OAI_VERSION2)) {
            prfx_str = prefix + ":" + OAIXML.DC;
          } else {
            prfx_str = OAIXML.DC;//oai version 1
          }
          meta_ns = OAIXML.DC;
        } else {
          prfx_str = prefix.substring(prefix.indexOf("_") + 1);
          meta_ns = prfx_str;
        }
        //create the <metadata> element
        //OAIXML.oai_version is read from OAIConfig.xml and its default value is "2.0"
	Element metadata = doc.createElement(OAIXML.METADATA);
        Element prfx_str_elem = OAIXML.getMetadataPrefixElement(doc, prfx_str, OAIXML.oai_version);
        metadata.appendChild(prfx_str_elem);
        String[] metadata_names = getMetadataNameMapping(metadata_format);
        HashMap meta_map = getInfoByNames(info, metadata_names);
		
		// if there's no dc:identifier already after the mapping, we'll add it in
		if(!meta_map.containsKey(OAIXML.DC+":identifier")) { // dc:identifier OAIXML.IDENTIFIER
			outputCustomMetadata(meta_map, info, OAIXML.DC+":identifier");
		}
	
		
	if (meta_map == null) {
	  return metadata;
	}
	ArrayList meta_list = new ArrayList(meta_map.entrySet());
	for (int j=0; j<meta_list.size(); j++) {
	  Entry men = (Entry)meta_list.get(j);
	  String meta_name = (String)men.getKey();
	  //meta_name = meta_name.replace('.', ':'); // namespace separator should be : for oai
	  String meta_value = (String)men.getValue();
	  Element e = doc.createElement(meta_name);
	  GSXML.setNodeText(e, meta_value);
	  prfx_str_elem.appendChild(e);
	}
	
        return metadata;
  }
  /** create a header element used when processing requests like ListRecords/GetRecord/ListIdentifiers
   */
  private Element createHeaderElement(Document doc, String oid, String oailastmodified) {    
        Element header = doc.createElement(OAIXML.HEADER);
        Element identifier = doc.createElement(OAIXML.IDENTIFIER);
        //GSXML.setNodeText(identifier, site_name + ":" + coll_name + ":" + oid);
	GSXML.setNodeText(identifier, coll_name + ":" + oid);
        header.appendChild(identifier);
        Element set_spec = doc.createElement(OAIXML.SET_SPEC);
        //GSXML.setNodeText(set_spec, site_name + ":" + coll_name);
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
    
    NodeList meta_list = getMetadataFormatList();
    if (meta_list == null || meta_list.getLength() == 0) {
      logger.error("No metadata format is present in collectionConfig.xml");
      return OAIXML.createErrorResponse(OAIXML.NO_METADATA_FORMATS, "");
    }

    Document doc = XMLConverter.newDOM();
    Element list_metadata_formats_response = doc.createElement(GSXML.RESPONSE_ELEM);
    
    Element list_metadata_formats = doc.createElement(OAIXML.LIST_METADATA_FORMATS);
    list_metadata_formats_response.appendChild(list_metadata_formats);
    boolean has_meta_format = false;
    
    for (int i=0; i<meta_list.getLength(); i++) {
      Element metadata_format = (Element)meta_list.item(i);
      String[] metadata_names = getMetadataNameMapping(metadata_format);
      if (containsMetadata(info, metadata_names) == true) {
        has_meta_format = true;
	// TODO, can we do this in an easier way??
        Element meta_fmt = doc.createElement(OAIXML.METADATA_FORMAT);
        GSXML.copyNamedElement(meta_fmt, metadata_format, OAIXML.METADATA_PREFIX);
        GSXML.copyNamedElement(meta_fmt, metadata_format, OAIXML.METADATA_NAMESPACE);
        GSXML.copyNamedElement(meta_fmt, metadata_format, OAIXML.SCHEMA);
        list_metadata_formats.appendChild(meta_fmt);
      }
    }//end of for loop
    if (has_meta_format == false) {
      logger.error("Specified metadata names are not contained in the database.");
      return OAIXML.createErrorResponse(OAIXML.NO_METADATA_FORMATS, "");
    } else {
      return list_metadata_formats_response;
    }
  }

  /** return the ListMetadataFormats element in collectionConfig.xml
   *  Currently, it will only contain one metadata format: oai_dc
   */
  protected NodeList getMetadataFormatList() {
    Element list_meta_formats = (Element)GSXML.getChildByTagName(this.config_info, OAIXML.LIST_METADATA_FORMATS);
    return GSXML.getChildrenByTagName(list_meta_formats, OAIXML.METADATA_FORMAT);
  }
  /** @param metadata_format - the metadataFormat element in collectionConfig.xml
   */
  protected String[] getMetadataNameMapping(Element metadata_format) {
  
    String[] names = OAIXML.getMetadataMapping(metadata_format);
    if (names != null) {
      return names;
    }
    logger.info("No metadata mappings are provided in collectionConfig.xml. Try for global mapping");
    names = OAIXML.getGlobalMetadataMapping(metadata_format.getAttribute(OAIXML.METADATA_PREFIX));
    return names;
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
   * name which is mendatory.
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
    /** @param keys - contains a list of keys in string format.
     * Here is a typical record in the collection database, 'keys' contains the values in <...>:
     *----------------------------------------------------------------------
[HASH01a84acb0f1aad2380493b3a]
<doctype>doc
<hastxt>1
<Language>en
<Encoding>windows_1252
<Plugin>HTMLPlug
<FileSize>205093
<Source>wb34te.htm
<hascover>1
<dls.Organization>World Bank
<dls.Title>Development in practice: Toward Gender Equality (wb34te)
<dls.Language>English
<dls.AZList>A-B-C-D-E-F-G-H-I-J-K-L-M-N-O-P-Q-R-S-T-U-V-W-X-Y-Z
<dls.Subject>Women, gender and development, women's organizations
<dls.Keyword>achieve gender equality
<URL>http://wb34te/wb34te.htm
<Title>Development in practice: Toward Gender Equality
<lastmodified>1178245194
<assocfilepath>HASH01a8.dir
<memberof>CL3
<archivedir>HASH01a8.dir
<thistype>VList
<childtype>VList
<contains>".1;".2;".3;".4;".5;".6;".7;".8;".9
<docnum>349
<oailastmodified>1303283795
<lastmodifieddate>20110412
<oailastmodifieddate>20110420
----------------------------------------------------------------------
     */
  public String[] getMetadata(DBInfo info, String names) {
    String[] name_value = new String[2];
    ArrayList<String> keys = new ArrayList<String>(info.getKeys());
    for (int i=0; i<keys.size(); i++) {
      String key = keys.get(i);
      String first_name = "";
      String second_name = "";
      int index = names.indexOf(",");
      if(index != -1) {
        first_name = names.substring(0, index);
        second_name = names.substring(index + 1);
      } else {
        first_name = second_name = names;
      }
      if(key.equals(second_name)) {
	String meta_value = info.getInfo(key);
        name_value[0] = first_name;
	name_value[1] = meta_value;
	return name_value;
      }
    }
    return null;
  }
  protected HashMap getInfoByNames(DBInfo info, String[] metadata_names) {

    if (metadata_names == null) {
      return null;
    }
    HashMap<String, String> map = new HashMap<String, String>();
    boolean empty_map = true;
    
    for(int i=0; i<metadata_names.length; i++) {
      String[] name_value = getMetadata(info, metadata_names[i]);
      if(name_value != null) { 
        map.put(name_value[0], name_value[1]);
        empty_map = false;
      }
    }
    return (empty_map == true) ? null : map;
  }
  
  // GS3 version of GS2's runtime-src/src/oaiservr/dublincore.cpp function output_custom_metadata
  protected void outputCustomMetadata(HashMap meta_map, DBInfo info, String dc_identifier) {
		
	// try gs.OAIResourceURL, else srclinkFile, else the GS version of the document
	String identifier_value = info.getInfo(OAIXML.GS_OAI_RESOURCE_URL);
	
	if(identifier_value.equals("")) {
		String url = OAIXML.getBaseURL(); // e.g. e.g. http://host:port/greenstone3/library/oaiserver
	
		identifier_value = info.getInfo("srclinkFile");
		if(identifier_value.equals("")) 
		{
			// no source file to link to, so link to the GS version of the document (need to use URL-style slashes)
			// e.g. http://host:port/greenstone3/library/collection/lucene-jdbm-demo/document/HASH014602ec89e16b7d431c7627
			
			identifier_value = url.replace("oaiserver", "library") + "collection/" + this.coll_name + "/document/" + info.getInfo("identifier"); // OID
		} 
		else // use srclinkFile
		{		
			// e.g. http://host:port/greenstone3/sites/localsite/collect/backdrop/index/assoc/D0.dir/Cat.jpg
			identifier_value = url.replace("oaiserver", "") + "sites/" + this.site_name 
				+ "/collect/" + this.coll_name + "/index/assoc/" 
				+ info.getInfo("assocfilepath") + "/" + identifier_value; // srclinkFile
		}
	} // else use gs.OAIResourceURL as-is
	
	//logger.info("**** dc:identifier: " + identifier_value);
	
	meta_map.put(dc_identifier, identifier_value);
  }
}


