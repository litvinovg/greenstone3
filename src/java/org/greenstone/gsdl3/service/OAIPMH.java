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
 * @author <a href="mailto:xiao@cs.waikato.ac.nz">Xiao</a>
 */

public class OAIPMH extends ServiceRack {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.OAIPMH.class.getName());
  
  protected SimpleCollectionDatabase coll_db = null;
  
  protected String site_name = "";
  protected String coll_name = "";
  protected Element coll_config_xml = null;
  
  /** constructor */
  public OAIPMH() {

  }
  
  public void cleanUp() {
    super.cleanUp();//??
    this.coll_db.closeDatabase();
  }
  /** configure this service */
  public boolean configure(Element info, Element extra_info) {
    if (!super.configure(info, extra_info)){
      logger.info("Configuring ServiceRack.java returns false.");
      return false;
    }
    
    //get the names from ServiceRack.java
    site_name = this.router.getSiteName();
    coll_name = this.cluster_name;
    //get the collection-specific configurations from collectionConfig.xml
    coll_config_xml = OAIXML.getCollectionConfigXML(site_name, coll_name);
    
    logger.info("Configuring OAIPMH...");
    // this call passes the indexStem in of ServiceRack element in buildConfig.xml to the super class.
    this.config_info = info;
    
    // the index stem is either specified in the buildConfig.xml file or uses the collection name
    Element index_stem_elem = (Element) GSXML.getChildByTagName(info, GSXML.INDEX_STEM_ELEM);
    String index_stem = null;
    if (index_stem_elem != null) {
      index_stem = index_stem_elem.getAttribute(GSXML.NAME_ATT);
    }
    if (index_stem == null || index_stem.equals("")) {
      index_stem = this.cluster_name;
    }
  
    // find out what kind of database we have
    Element database_type_elem = (Element) GSXML.getChildByTagName(info, GSXML.DATABASE_TYPE_ELEM);
    String database_type = null;
    if (database_type_elem != null) {
      database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
    }
    if (database_type == null || database_type.equals("")) {
      database_type = "gdbm"; // the default
    }
    coll_db = new SimpleCollectionDatabase(database_type);
    if (coll_db == null) {
      logger.error("Couldn't create the collection database of type "+database_type);
      return false;
    }
    
    // Open database for querying
    String coll_db_file = GSFile.collectionDatabaseFile(this.site_home, this.cluster_name, index_stem, database_type);
    if (!this.coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ)) {
      logger.error("Could not open collection database!");
      return false;
    }
    
    // the short_service_info is used by the message router to find the method names, 
    //so we just use the doc variable in class ServiceRack to create the xml; but
    // in each method we will use OAIXML to create the response xml
    // set up short_service_info_ - just the name
 
    Element identify = this.doc.createElement(OAIXML.SERVICE);

    identify.setAttribute(OAIXML.NAME, OAIXML.IDENTIFY);
    identify.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(identify);

    Element list_records = this.doc.createElement(OAIXML.SERVICE);
    list_records.setAttribute(OAIXML.NAME, OAIXML.LIST_RECORDS);
    list_records.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_records);

    Element list_identifiers = this.doc.createElement(OAIXML.SERVICE);
    list_identifiers.setAttribute(OAIXML.NAME, OAIXML.LIST_IDENTIFIERS);
    list_identifiers.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_identifiers);
    
    Element list_sets = this.doc.createElement(OAIXML.SERVICE);
    list_sets.setAttribute(OAIXML.NAME, OAIXML.LIST_SETS);
    list_sets.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_sets);
    
    Element list_metadata_formats = this.doc.createElement(OAIXML.SERVICE);
    list_metadata_formats.setAttribute(OAIXML.NAME, OAIXML.LIST_METADATA_FORMATS);
    list_metadata_formats.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(list_metadata_formats);

    Element get_record = this.doc.createElement(OAIXML.SERVICE);
    get_record.setAttribute(OAIXML.NAME, OAIXML.GET_RECORD);
    get_record.setAttribute(GSXML.TYPE_ATT, "oai");
    this.short_service_info.appendChild(get_record);
    
    return true;
  }
  /** returns a specific service description */
  public Element getServiceDescription(String service_id, String lang, String subset) {
    
    if (service_id.equals(OAIXML.IDENTIFY)) {
      Element identify = this.doc.createElement(OAIXML.SERVICE);
      identify.setAttribute(OAIXML.NAME, OAIXML.IDENTIFY);
      identify.setAttribute(GSXML.TYPE_ATT, "oai");
      return identify;
    }
    if (service_id.equals(OAIXML.LIST_RECORDS)) {
      Element list_records = this.doc.createElement(OAIXML.SERVICE);
      list_records.setAttribute(OAIXML.NAME, OAIXML.LIST_RECORDS);
      list_records.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_records;
    }
    
    if (service_id.equals(OAIXML.LIST_IDENTIFIERS)) {
      Element list_identifiers = this.doc.createElement(OAIXML.SERVICE);
      list_identifiers.setAttribute(OAIXML.NAME, OAIXML.LIST_IDENTIFIERS);
      list_identifiers.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_identifiers;
    }
    if (service_id.equals(OAIXML.LIST_SETS)) {
      Element list_sets = this.doc.createElement(OAIXML.SERVICE);
      list_sets.setAttribute(OAIXML.NAME, OAIXML.LIST_SETS);
      list_sets.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_sets;
    }
    if (service_id.equals(OAIXML.LIST_METADATA_FORMATS)) {
      Element list_metadata_formats = this.doc.createElement(OAIXML.SERVICE);
      list_metadata_formats.setAttribute(OAIXML.NAME, OAIXML.LIST_METADATA_FORMATS);
      list_metadata_formats.setAttribute(GSXML.TYPE_ATT, "oai");
      return list_metadata_formats;
    }
    
    if (service_id.equals(OAIXML.GET_RECORD)) {
      Element get_record = this.doc.createElement(OAIXML.SERVICE);
      get_record.setAttribute(OAIXML.NAME, OAIXML.GET_RECORD);
      get_record.setAttribute(GSXML.TYPE_ATT, "oai");
      return get_record;
    }
    
    return null;
  }
  /** return the metadata information about this set of the repository */
  protected Element processIdentify(Element req) {
    return null;
  }
  /** return the metadata information  */
  protected Element processListSets(Element req) {
    //This method is never called unless each set in the returned message contain a
    //'description' element so that we need to ask each collection for their info
    return null;    
  }
  /** return the metadata information  */
  protected Element processGetRecord(Element req) {
    /** arguments:
        identifier: required
        metadataPrefix: required
     *  Exceptions: badArgument; cannotDisseminateFormat; idDoesNotExist
     */ 
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    HashMap param_map = OAIXML.getParamMap(params);    
    
    String prefix = (String)param_map.get(OAIXML.METADATA_PREFIX);
    if (prefix == null || prefix.equals("")) {
      //Just a double-check
      logger.error("the value of metadataPrefix att is not present in the request.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    
    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    
    String oid = (String)param_map.get(OAIXML.OID);

    //get a DBInfo object of the identifier; if this identifier is not present in the database,
    // null is returned.
    DBInfo info = this.coll_db.getInfo(oid);
    if (info == null) {
      logger.error("OID: " + oid + " is not present in the database.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
    }

    ArrayList keys = new ArrayList(info.getKeys());
    String lastmodified = "";
    if(keys.contains(OAIXML.LASTMODIFIED)) {
      lastmodified = info.getInfo(OAIXML.LASTMODIFIED);
    }
    lastmodified = OAIXML.getTime(Long.parseLong(lastmodified));

    Element get_record = OAIXML.createElement(OAIXML.GET_RECORD);
    Element record = OAIXML.createElement(OAIXML.RECORD);
    //compose the header element
    record.appendChild(createHeaderElement(oid, lastmodified));      
    //compose the metadata element
    record.appendChild(createMetadataElement(prefix, info, metadata_format));
    get_record.appendChild(record);
    return OAIXML.getResponse(get_record);
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
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    
    if(params.getLength() == 0) {
      logger.error("must at least have the metadataPrefix parameter, can't be none");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    
    HashMap param_map = OAIXML.getParamMap(params);  
    
    String prefix = "";
    Date from_date = null;
    Date until_date = null;
    
    if(param_map.containsKey(OAIXML.METADATA_PREFIX) == false) {    
      //Just a double-check
      logger.error("A param element containing the metadataPrefix is not present.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    prefix = (String)param_map.get(OAIXML.METADATA_PREFIX);
    if (prefix == null || prefix.equals("")) {
      //Just a double-check
      logger.error("the value of metadataPrefix att is not present in the request.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    
    if(param_map.containsKey(OAIXML.FROM)) {
      String from = (String)param_map.get(OAIXML.FROM);
      from_date = OAIXML.getDate(from);
    }    
    if(param_map.containsKey(OAIXML.UNTIL)) {
      String until = (String)param_map.get(OAIXML.UNTIL);
      until_date = OAIXML.getDate(until);
    }    

    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    ArrayList oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null) {
      logger.error("No matched records found in collection: browselist is empty");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }
    // all validation is done
    Element list_identifiers = OAIXML.createElement(OAIXML.LIST_IDENTIFIERS);
    for(int i=0; i<oid_list.size(); i++) {
      String oid = (String)oid_list.get(i);
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) {
        logger.error("Database does not contains information about oid: " +oid);
        continue;
      }
      ArrayList keys = new ArrayList(info.getKeys());
      String lastmodified = "";
      if(keys.contains(OAIXML.LASTMODIFIED)) {
        lastmodified = info.getInfo(OAIXML.LASTMODIFIED);
      }
      lastmodified = OAIXML.getTime(Long.parseLong(lastmodified));
      
      Date this_date = OAIXML.getDate(lastmodified);        
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
      list_identifiers.appendChild(createHeaderElement(oid, lastmodified));      
    }//end of for(int i=0; i<oid_list.size(); i++) of doing thru each record
    
    return OAIXML.getResponse(list_identifiers);        
  }
  /** return a list of records  */
  protected Element processListRecords(Element req) {
    /** the request sent here may contain optional 'from', 'untill', 'metadataPrefix', 
     * and 'resumptionToken' params. see doListSets() in OAIReceptionist.
     * if the request contains 'resumptionToken' then it should have been handled by the
     * OAIReceptionist. Therefore, the request sent here must not contain 'resumptionToken'
     * argument but a 'metadataPrefix' param. The OAIReceptionist makes sure of this.
     */
    NodeList params = GSXML.getChildrenByTagName(req, OAIXML.PARAM);
    
    if(params.getLength() == 0) {
      logger.error("must at least have the metadataPrefix parameter, can't be none");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.BAD_ARGUMENT, ""));
    }
    
    HashMap param_map = OAIXML.getParamMap(params);    

    String prefix = "";
    Date from_date = null;
    Date until_date = null;
    
    if(param_map.containsKey(OAIXML.METADATA_PREFIX) == false) {    
      //Just a double-check
      logger.error("A param element containing the metadataPrefix is not present.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    prefix = (String)param_map.get(OAIXML.METADATA_PREFIX);
    if (prefix == null || prefix.equals("")) {
      //Just a double-check
      logger.error("the value of metadataPrefix att is not present in the request.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
    
    if(param_map.containsKey(OAIXML.FROM)) {
      String from = (String)param_map.get(OAIXML.FROM);
      from_date = OAIXML.getDate(from);
    }    
    if(param_map.containsKey(OAIXML.UNTIL)) {
      String until = (String)param_map.get(OAIXML.UNTIL);
      until_date = OAIXML.getDate(until);
    }    
    Element metadata_format = getMetadataFormatElement(prefix);
    if(metadata_format == null) {
      logger.error("metadata prefix is not supported.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
    }
// Another way of doing the same job!    
//    HashMap prefix_map = OAIXML.getChildrenMapByTagName(coll_config_xml, OAIXML.METADATA_PREFIX);
//    if(!prefix_map.contains(prefix)) {
//      logger.error("metadata prefix is not supported.");
//      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.CANNOT_DISSEMINATE_FORMAT, ""));
//    }
    
    //get a list of identifiers (it contains a list of strings)
    ArrayList oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null) {
      logger.error("No matched records found in collection: browselist is empty");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.NO_RECORDS_MATCH, ""));
    }
    // all validation is done
    Element list_records = OAIXML.createElement(OAIXML.LIST_RECORDS);
    for(int i=0; i<oid_list.size(); i++) {
      String oid = (String)oid_list.get(i);
      DBInfo info = this.coll_db.getInfo(oid);
      if (info == null) {
        logger.error("Database does not contains information about oid: " +oid);
        continue;
      }
      ArrayList keys = new ArrayList(info.getKeys());
      String lastmodified = "";
      if(keys.contains(OAIXML.LASTMODIFIED)) {
        lastmodified = info.getInfo(OAIXML.LASTMODIFIED);
      }
      lastmodified = OAIXML.getTime(Long.parseLong(lastmodified));
      
      Date this_date = OAIXML.getDate(lastmodified);        
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
      
      Element record = OAIXML.createElement(OAIXML.RECORD);
      list_records.appendChild(record);
      //compose the header element
      record.appendChild(createHeaderElement(oid, lastmodified));      
      //compose the metadata element
      record.appendChild(createMetadataElement(prefix, info, metadata_format));
      
    }//end of for(int i=0; i<oid_list.size(); i++) of doing thru each record
    
    return OAIXML.getResponse(list_records);    
  }
  
  /** get the metadataFormat element from the collectionConfig.xml containing the specified metadata prefix.
   *  return null if not found.
   */
  private Element getMetadataFormatElement(String prefix) {
    Element oai = (Element)GSXML.getChildByTagName(this.coll_config_xml, OAIXML.OAI);
    Element list_meta_format = (Element)GSXML.getChildByTagName(oai, OAIXML.LIST_METADATA_FORMATS);
    Element metadata_format = GSXML.getNamedElement(list_meta_format, OAIXML.METADATA_FORMAT, OAIXML.METADATA_PREFIX, prefix);
    return metadata_format;
  }
  /** create the metadata element used when processing ListRecords/GetRecord requests
   */
  private Element createMetadataElement(String prefix, DBInfo info, Element metadata_format) {
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
        Element prfx_str_elem = OAIXML.getMetadataPrefixElement(prfx_str, OAIXML.oai_version);
        String[] metadata_names = getMetadataNames(metadata_format);
        HashMap meta_map = getInfoByNames(info, metadata_names);
        ArrayList meta_list = new ArrayList(meta_map.entrySet());
        for (int j=0; j<meta_list.size(); j++) {
          Entry men = (Entry)meta_list.get(j);
          String meta_name = (String)men.getKey();
          String meta_value = (String)men.getValue();
          Element e = OAIXML.createElement(meta_name);
          GSXML.setNodeText(e, meta_value);
          prfx_str_elem.appendChild(e);
        }
        Element metadata = OAIXML.createElement(OAIXML.METADATA);
        metadata.appendChild(prfx_str_elem);
        return metadata;
  }
  /** create a header element used when processing requests like ListRecords/GetRecord/ListIdentifiers
   */
  private Element createHeaderElement(String oid, String lastmodified) {    
        Element header = OAIXML.createElement(OAIXML.HEADER);
        Element identifier = OAIXML.createElement(OAIXML.IDENTIFIER);
        GSXML.setNodeText(identifier, site_name + ":" + coll_name + ":" + oid);
        header.appendChild(identifier);
        Element set_spec = OAIXML.createElement(OAIXML.SET_SPEC);
        GSXML.setNodeText(set_spec, site_name + ":" + coll_name);
        header.appendChild(set_spec);
        Element datestamp = OAIXML.createElement(OAIXML.DATESTAMP);
        GSXML.setNodeText(datestamp, lastmodified);
        header.appendChild(datestamp);
        return header;
  }
  /** return the metadata information  */
  protected Element processListMetadataFormats(Element req) {
    // the request sent here must contain an OID. see doListMetadataFormats() in OAIReceptionist
    Element param = GSXML.getNamedElement(req, OAIXML.PARAM, OAIXML.NAME, OAIXML.OID);
    if (param == null) {
      logger.error("An element containing the OID attribute not is present.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
    }
    String oid = param.getAttribute(OAIXML.VALUE);
    if (oid == null || oid.equals("")) {
      logger.error("No OID is present in the request.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
    }
    ArrayList oid_list = getChildrenIds(OAIXML.BROWSELIST);
    if (oid_list == null || oid_list.contains(oid) == false) {
      logger.error("OID: " + oid + " is not present in the database.");
      Element e= OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.ID_DOES_NOT_EXIST, ""));
//      logger.error((new XMLConverter()).getPrettyString (e));
      return e;
    }
    
    DBInfo info = null;    
    info = this.coll_db.getInfo(oid);
    if (info == null) { //just double check
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.OAI_SERVICE_UNAVAILABLE, ""));
    }
    
    NodeList meta_list = getMetadataFormatList(this.coll_config_xml);
    if (meta_list == null || meta_list.getLength() == 0) {
      logger.error("No metadata format is present in collectionConfig.xml");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.NO_METADATA_FORMATS, ""));
    }

    Element list_metadata_formats = OAIXML.createElement(OAIXML.LIST_METADATA_FORMATS);
    boolean has_meta_format = false;
    
    for (int i=0; i<meta_list.getLength(); i++) {
      Element metadata_format = (Element)meta_list.item(i);
      String[] metadata_names = getMetadataNames(metadata_format);
      if (containsMetadata(info, metadata_names) == true) {
        has_meta_format = true;
        Element meta_fmt = OAIXML.createElement(OAIXML.METADATA_FORMAT);
        OAIXML.copyElement(meta_fmt, metadata_format, OAIXML.METADATA_PREFIX);
        OAIXML.copyElement(meta_fmt, metadata_format, OAIXML.METADATA_NAMESPACE);
        OAIXML.copyElement(meta_fmt, metadata_format, OAIXML.SCHEMA);
        list_metadata_formats.appendChild(meta_fmt);
      }
    }//end of for loop
    if (has_meta_format == false) {
      logger.error("Specified metadata names are not contained in the database.");
      return OAIXML.getResponse(OAIXML.createErrorElement(OAIXML.NO_METADATA_FORMATS, ""));
    } else {
      return OAIXML.getResponse(list_metadata_formats);
    }
  }

  /** return the ListMetadataFormats element in collectionConfig.xml
   *  Currently, it will only contain one metadata format: oai_dc
   */
  protected NodeList getMetadataFormatList(Element coll_config_xml) {
    Element oai_elem = (Element)GSXML.getChildByTagName(coll_config_xml, OAIXML.OAI);
    Element list_meta_formats = (Element)GSXML.getChildByTagName(oai_elem, OAIXML.LIST_METADATA_FORMATS);
    return GSXML.getChildrenByTagName(list_meta_formats, OAIXML.METADATA_FORMAT);
  }
  /** @param metadata_format - the metadataFormat element in collectionConfig.xml
   */
  protected String[] getMetadataNames(Element metadata_format) {
    String[] names = null;
    
    //read the mappingList element
    Element mapping_list = (Element)GSXML.getChildByTagName(metadata_format, OAIXML.MAPPING_LIST);
    if (mapping_list == null) {
      logger.info("No metadata mappings are provided in collectionConfig.xml. Use the standard Dublin Core names.");
      names = OAIXML.getGlobalMetadataMapping(metadata_format.getAttribute(OAIXML.METADATA_PREFIX));
      
      return (names != null)? names : OAIXML.getDublinCoreNames();
    }
    NodeList mappings = GSXML.getChildrenByTagName(mapping_list, OAIXML.MAPPING);
    int size = mappings.getLength();
    if (size == 0) {
        logger.info("No metadata mappings are provided in collectionConfig.xml. \n Return standard DC names.");
        // read the standard Dublin Core metadata names
        return OAIXML.getDublinCoreNames();
    }
    names = new String[size];
    for (int i=0; i<size; i++) {
      names[i] = GSXML.getNodeText((Element)mappings.item(i)).trim();
    }
    return names;
  }

  /** returns a list of the child ids in order, null if no children */
  protected ArrayList getChildrenIds(String node_id) {
    DBInfo info = this.coll_db.getInfo(node_id);
    if (info == null) {
      return null;
    }
    
    String contains = info.getInfo("contains");
    if (contains.equals("")) {
      return null;
    }
    ArrayList children = new ArrayList();
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
----------------------------------------------------------------------
     */
  public String[] getMetadata(DBInfo info, String names) {
    String[] name_value = new String[2];
    ArrayList keys = new ArrayList(info.getKeys());
    for (int i=0; i<keys.size(); i++) {
      String key = (String)keys.get(i);
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
    HashMap map = new HashMap();
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
}


