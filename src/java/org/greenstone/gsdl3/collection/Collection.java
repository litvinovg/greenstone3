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

import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.core.*;
import org.greenstone.gsdl3.service.*;


// java XML classes we're using
import org.w3c.dom.Document; 
import org.w3c.dom.Node; 
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList; 

import java.io.*;
import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.log4j.*;

// Apache Commons
import org.apache.commons.lang3.*;

/**
 * Represents a collection in Greenstone. A collection is an extension of
 * a ServiceCluster - it has local data that the services use.
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @see ModuleInterface
 */
public class Collection 
    extends ServiceCluster {

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.collection.Collection.class.getName());

    /** is this collection being tidied */
    protected boolean useBook = false;
    /** is this collection public or private */
    protected boolean is_public = true;

    /** does this collection provide the OAI service */
    protected boolean has_oai = true;
    /** time when this collection was built */
    protected long lastmodified = 0;
    
    /** An element containing the serviceRackList element of buildConfig.xml, used to determine whether it contains
     *  the OAIPMH serviceRack 
     */
    //protected Element service_rack_list = null;
    
    protected XMLTransformer transformer = null;
    /** same as setClusterName */
    public void setCollectionName(String name) {
	setClusterName(name);
    }

    public Collection() {
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
    public boolean configure() {
	
	if (this.site_home == null || this.cluster_name== null) {
	    logger.error("Collection: site_home and collection_name must be set before configure called!");
	    return false;
	}
	
	Element coll_config_xml = loadCollConfigFile();
	Element build_config_xml = loadBuildConfigFile();
	
	if (coll_config_xml==null||build_config_xml==null) {
	    return false;
	}
	
	// get the collection type attribute
	Element search = (Element) GSXML.getChildByTagName(coll_config_xml, GSXML.SEARCH_ELEM);	
	if(search!=null) {
	  col_type = search.getAttribute(GSXML.TYPE_ATT);
	}
	
	// process the metadata and display items
	findAndLoadInfo(coll_config_xml, build_config_xml);
	
	// now do the services
	configureServiceRacks(coll_config_xml, build_config_xml);

	return true;
	
    }

    public boolean useBook() {
    	return useBook;
    }
    
    public boolean isPublic() {
	return is_public;
    }
    //used by the OAIReceptionist to find out the earliest datestamp amongst all oai collections in the repository
    public long getLastmodified() {
      return lastmodified;
    }
    /** whether the service_map in ServiceCluster.java contains the service 'OAIPMH'
     *  11/06/2007 xiao
     */
    public boolean hasOAI() {
      return has_oai;
    }
    /** 
     * load in the collection config file into a DOM Element 
     */    
    protected Element loadCollConfigFile() {

	File coll_config_file = new File(GSFile.collectionConfigFile(this.site_home, this.cluster_name));
	
	if (!coll_config_file.exists()) {
	    logger.error("Collection: couldn't configure collection: "+this.cluster_name+", "+coll_config_file+" does not exist");
	    return null;
	}
	// get the xml for both files
	Document coll_config_doc = this.converter.getDOM(coll_config_file, CONFIG_ENCODING);
	Element coll_config_elem = null;
	if (coll_config_doc != null) {
	    coll_config_elem = coll_config_doc.getDocumentElement();
	}
	return coll_config_elem;

    }
    
    /** 
     * load in the collection build config file into a DOM Element 
     */        
    protected Element loadBuildConfigFile() {
	
	File build_config_file = new File(GSFile.collectionBuildConfigFile(this.site_home, this.cluster_name));
	if (!build_config_file.exists()) {
	    logger.error("Collection: couldn't configure collection: "+this.cluster_name+", "+build_config_file+" does not exist");
	    return null;
	}
	Document build_config_doc = this.converter.getDOM(build_config_file, CONFIG_ENCODING);
	Element build_config_elem = null;
	if (build_config_doc != null) {
	    build_config_elem = build_config_doc.getDocumentElement();
	}
  
	lastmodified = build_config_file.lastModified();
  
	return build_config_elem;
    }

    /**
     * find the metadata and display elems from the two config files and add it to the appropriate lists
     */
    protected boolean findAndLoadInfo(Element coll_config_xml, 
				      Element build_config_xml){
		      
      // metadata
      Element meta_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
      addMetadata(meta_list);
      meta_list = (Element)GSXML.getChildByTagName(build_config_xml, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
      addMetadata(meta_list);
      
      meta_list = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
      GSXML.addMetadata(this.doc, meta_list, "httpPath", this.site_http_address+"/collect/"+this.cluster_name);
      addMetadata(meta_list);
      
      // display stuff
      Element display_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER);
      if (display_list != null) {
        resolveMacros(display_list);
        addDisplayItems(display_list);
      }
      
      //check whether the html are tidy or not
      Element import_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.IMPORT_ELEM);
      if (import_list != null) {
        Element plugin_list = (Element)GSXML.getChildByTagName(import_list, GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
        addPlugins(plugin_list);
        if (plugin_list != null){
          Element plugin_elem = (Element)GSXML.getNamedElement(plugin_list, GSXML.PLUGIN_ELEM, GSXML.NAME_ATT, "HTMLPlug");
          if (plugin_elem != null) {
            //get the option
            Element option_elem = (Element)GSXML.getNamedElement(plugin_elem, GSXML.PARAM_OPTION_ELEM, GSXML.NAME_ATT, "-tidy_html");
            if (option_elem != null) {
              useBook = true;
            }
          }
        }
      }
      meta_list = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
      if (useBook == true)
        GSXML.addMetadata(this.doc, meta_list, "tidyoption", "tidy");
      else
        GSXML.addMetadata(this.doc, meta_list, "tidyoption", "untidy");
      addMetadata(meta_list);
      
      // check whether we are public or not
      if (meta_list != null) {
        Element meta_elem = (Element) GSXML.getNamedElement(metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "public");
        if (meta_elem != null) {
          String value = GSXML.getValue(meta_elem).toLowerCase().trim();
          if (value.equals("false")) {
            is_public = false;
          }
        }
      }
      return true;

    }

  protected boolean configureServiceRacks(Element coll_config_xml, 
					  Element build_config_xml){
    clearServices();
    Element service_list = (Element)GSXML.getChildByTagName(build_config_xml, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
    configureServiceRackList(service_list, coll_config_xml);
        
    // collection Config may also contain manually added service racks
    service_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.SERVICE_CLASS_ELEM+GSXML.LIST_MODIFIER);
    if (service_list != null) {
      configureServiceRackList(service_list, build_config_xml);
      
      // Check for oai
      Element oai_service_rack = GSXML.getNamedElement(service_list, GSXML.SERVICE_CLASS_ELEM, OAIXML.NAME, OAIXML.OAIPMH);
      if (oai_service_rack == null) {
	has_oai = false;
	logger.info("No oai for collection: " + this.cluster_name);
	
      } else {
	has_oai = true;
      }
    } else { // no list of services (no ServiceRackList), so no oai_service_rack either
	// explicitly set has_oai to false here, since it's initialised to true by default
	has_oai = false;
    }
    return true;
  }
  
  protected boolean resolveMacros(Element display_list) {
	if (display_list==null) return false;
	NodeList displaynodes = display_list.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
	if (displaynodes.getLength()>0) { 
	    String http_site = this.site_http_address;
	    String http_collection = this.site_http_address +"/collect/"+this.cluster_name;
	    for(int k=0; k<displaynodes.getLength(); k++) {
		Element d = (Element) displaynodes.item(k);
		String text = GSXML.getNodeText(d);
		text = StringUtils.replace(text, "_httpsite_", http_site);
		text = StringUtils.replace(text, "_httpcollection_", http_collection);
		GSXML.setNodeText(d, text);
	    }
	}
	return true;
    }
    /** 
     * do a configure on only part of the collection
     */
    protected boolean configureSubset(String subset) {

	// need the coll config files
	Element coll_config_elem = loadCollConfigFile();
	Element build_config_elem = loadBuildConfigFile();
	if (coll_config_elem == null||build_config_elem == null) {
	    // wont be able to do any of the requests
	    return false;
	}    
	
	if (subset.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
	  return configureServiceRacks(coll_config_elem, build_config_elem);
	} 

	if (subset.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER) || subset.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER) || subset.equals(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER)) {
	    return findAndLoadInfo(coll_config_elem, build_config_elem);
	    
	}
	
	logger.error("Collection: cant process system request, configure "+subset);
	return false;
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

    if (type.equals(GSXML.REQUEST_TYPE_FORMAT_STRING)) {
        logger.error("Received format string request");

        String subaction = request.getAttribute("subaction");
        logger.error("Subaction is " + subaction);

        String service = request.getAttribute("service");
        logger.error("Service is " + service);

        String classifier = null;
        if(service.equals("ClassifierBrowse"))
        {
            classifier = request.getAttribute("classifier");
            logger.error("Classifier is " + classifier);
        }

        Element format_element = (Element) GSXML.getChildByTagName(request, GSXML.FORMAT_STRING_ELEM);
        //String format_string = GSXML.getNodeText(format_element);
        Element format_statement = (Element) format_element.getFirstChild();

        //logger.error("Format string: " + format_string);
        logger.error("Config file location = " + GSFile.collectionConfigFile(this.site_home, this.cluster_name));

        // check for version file

        String directory = new File(GSFile.collectionConfigFile(this.site_home, this.cluster_name)).getParent() + File.separator;
        logger.error("Directory is " + directory);

        String version_filename = "";
        if(service.equals("ClassifierBrowse"))
            version_filename = directory + "browse_"+classifier+"_format_statement_version.txt";
        else
            version_filename = directory + "query_format_statement_version.txt";

        File version_file = new File(version_filename);
        logger.error("Version filename is " + version_filename);


        if(subaction.equals("update"))
        {
            String version_number = "1";
            BufferedWriter writer;

            try{

                if(version_file.exists())
                {
                    // Read version
                    BufferedReader reader = new BufferedReader(new FileReader(version_filename));
                    version_number = reader.readLine();
                    int aInt = Integer.parseInt(version_number) + 1;
                    version_number = Integer.toString(aInt);
                    reader.close();
                }
                else{
                    // Create
                    version_file.createNewFile();
                    writer = new BufferedWriter(new FileWriter(version_filename));
                    writer.write(version_number);
                    writer.close();
                }

                // Write version file
                String format_statement_filename = "";

                if(service.equals("ClassifierBrowse"))
                    format_statement_filename = directory + "browse_"+classifier+"_format_statement_v" + version_number + ".txt";
                else
                    format_statement_filename = directory + "query_format_statement_v" + version_number + ".txt";

                logger.error("Format statement filename is " + format_statement_filename);

                // Write format statement
                String format_string = this.converter.getString(format_statement); //GSXML.xmlNodeToString(format_statement);
                writer = new BufferedWriter(new FileWriter(format_statement_filename));
                writer.write(format_string);
                writer.close();

                // Update version number
                writer = new BufferedWriter(new FileWriter(version_filename));
                writer.write(version_number);
                writer.close();

            } catch (IOException e) {
                logger.error("IO Exception "+e);
            }
        }

        if(subaction.equals("save"))
        {
            logger.error("SAVE format statement");

            try{

                // Convert format string to a document
                //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                //DocumentBuilder builder = factory.newDocumentBuilder();
                //String input = "<html><head><title></title></head><body>" + format_string + "</body></html>";
                //String input = format_string.substring(0,format_string.length()-1)+"</xml>";
                //logger.error(input);
                //InputSource is = new InputSource( new StringReader( input ) );
                //logger.error("About to parse format string");
                //Document format_statement = (Document) builder.parse( is );
                //logger.error("Done parsing format string");

                // open collectionConfig.xml and read in to w3 Document
                String collection_config = directory + "collectionConfig.xml";
                Document config = this.converter.getDOM(new File(collection_config), "UTF-8");
           
                //String tag_name = "";
                int k;
                int index;
                Element elem;
                Node current_node = GSXML.getChildByTagName(config, "CollectionConfig");
                NodeList current_node_list;
    
                if(service.equals("ClassifierBrowse"))
                {
                    //tag_name = "browse";
                    // if CLX then need to look in <classifier> X then <format>
                    // default is <browse><format>

                    current_node = GSXML.getChildByTagName(current_node, "browse");

                    // find CLX
                    if(classifier != null)
                    {
                        current_node_list = GSXML.getChildrenByTagName(current_node, "classifier");
                        index = Integer.parseInt(classifier.substring(2)) - 1;
                        // index should be given by X-1
                        current_node = current_node_list.item(index);
                        current_node = GSXML.getChildByTagName(current_node, "format");
                    }
                    else{
                        current_node = GSXML.getChildByTagName(current_node, "format");
                    }
                }
                else
                {
                    // look in <format> with no attributes
            
                    current_node_list = GSXML.getChildrenByTagName(current_node, "search");
                    for(k=0; k<current_node_list.getLength(); k++) 
                    {
                        current_node = current_node_list.item(k);
                        // if current_node has no attributes then break
                        elem = (Element) current_node;
                        if(elem.hasAttribute("name")==false)
                            break;
                    }
                }

                // Current_node should be a format tag
                elem = (Element) current_node;
    
                logger.error("Current_node = " + elem.getNodeName());

                // seems we want to remove current child/ren and replace with format_statement's child/ren?

                // remove existing
                current_node_list = elem.getChildNodes();
                for(k=0; k<current_node_list.getLength(); k++)
                {
                    current_node = elem.removeChild(current_node_list.item(k));
                }

                // append new but we have a string!
                //GSXML.setNodeText(elem, "THIS IS A TEST");
                //GSXML.setNodeText(elem, format_string);

                current_node_list = format_statement.getChildNodes();
                for(k=0; k<current_node_list.getLength(); k++)
                {
                    //if(transformed.getNodeType() == Node.DOCUMENT_NODE)
                //transformed = ((Document)transformed).getDocumentElement();
                    logger.error("Node type: "+current_node_list.item(k).getNodeType());
                    if(current_node_list.item(k).getNodeType() != Node.PROCESSING_INSTRUCTION_NODE)
                        current_node = elem.appendChild(this.doc.importNode(current_node_list.item(k),true));
                }

                //String text = GSXML.getNodeText(elem);
                //logger.error(text);
                //text = text.replaceAll("_httpsite_", http_site);
                //text = text.replaceAll("_httpcollection_", http_collection);
                //GSXML.setNodeText(d, text);

                // Now convert config document to string for writing to file
                String new_config = this.converter.getString(config);
    
                // Write to file (not original! for now)
                BufferedWriter writer = new BufferedWriter(new FileWriter(collection_config+".new"));
                writer.write(new_config);
                writer.close();

             } catch( Exception ex ) {
                logger.error("There was an exception "+ex);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                ex.printStackTrace(pw);
                pw.flush();
                sw.flush();
                logger.error(sw.toString());
            }

        }
    }
    else { // unknown type
        return super.processMessage(request);

    }
    return response;
    }

}




