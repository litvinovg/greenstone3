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

import org.apache.log4j.*;

/* for a collection that hasn't been built with greenstone build stuff. expects a documentList in the collectionConfig file, and it stores this. this is where doc level metadata comes from */
public class XMLCollection 
    extends Collection {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.collection.XMLCollection.class.getName());

    protected Element document_list = null;
    /** overwrite this to keep the document list
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

	//plugin stuff
	Element import_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.IMPORT_ELEM);
	if (import_list != null)
	{
		Element plugin_list = (Element)GSXML.getChildByTagName(import_list, GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER);
		addPlugins(plugin_list);
	}
	
	// are we a private collection??
	if (this.metadata_list != null) {
	  
	  Element meta_elem = (Element) GSXML.getNamedElement(this.metadata_list, GSXML.METADATA_ELEM, GSXML.NAME_ATT, "public");
	  if (meta_elem != null) {
	    
	    String value = GSXML.getValue(meta_elem).toLowerCase().trim();
	    if (value.equals("false")) {
	      is_public = false;
	    }
	  }
	}
	Element config_doc_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER);
	if (config_doc_list != null) {
	    document_list = (Element)this.doc.importNode(config_doc_list, true);
	} else {
	    document_list = this.doc.createElement(GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER);
	}
	return true;

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
	    response.appendChild(description);
	    // check the param list
	    Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	    if (param_list == null) {
		addAllDisplayInfo(description, lang);
		description.appendChild(this.service_list);
		description.appendChild(this.metadata_list);
		description.appendChild(this.plugin_item_list);
		description.appendChild(this.document_list);
		return response;
	    }
	    
	    // go through the param list and see what components are wanted
	    NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
	    for (int i=0; i<params.getLength(); i++) {
		
		Element param = (Element)params.item(i);
		// Identify the structure information desired
		if (param.getAttribute(GSXML.NAME_ATT) == GSXML.SUBSET_PARAM ) {
		    String info = param.getAttribute(GSXML.VALUE_ATT);
		    if (info.equals(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(this.service_list);
		    } else if (info.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(metadata_list);
		    } else if (info.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
			addAllDisplayInfo(description, lang);
			
		    } else if (info.equals(GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(this.document_list);
		    } else if (info.equals(GSXML.PLUGIN_ELEM+GSXML.LIST_MODIFIER)) {
		    	description.appendChild(this.plugin_item_list);
		    }
		    
		}
	    }
	    return response;
	}
	return super.processMessage(request);
	    
    }
  
}
