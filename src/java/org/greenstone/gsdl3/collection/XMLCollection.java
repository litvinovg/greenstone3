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

	// add metadata to stored metadata list from collConfig and buildConfig
	Element meta_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	addMetadata(meta_list);
	meta_list = (Element)GSXML.getChildByTagName(build_config_xml, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
	addMetadata(meta_list);

	addMetadata("httpPath", this.site_http_address+"/collect/"+this.cluster_name);

	// display stuff
	Element display_list = (Element)GSXML.getChildByTagName(coll_config_xml, GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER);
	if (display_list != null) {
	  resolveMacros(display_list);
	    addDisplayItems(display_list);
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
	    document_list = (Element)desc_doc.importNode(config_doc_list, true);
	} else {
	    document_list = desc_doc.createElement(GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER);
	}
	return true;

    }

    /** handles requests made to the ServiceCluster itself 
     *
     * @param req - the request Element- <request>
     * @return the result Element - should be <response>
     */
  protected Element processMessage(Document response_doc, Element request) {

	Element response = response_doc.createElement(GSXML.RESPONSE_ELEM);
	response.setAttribute(GSXML.FROM_ATT, this.cluster_name);
	String type = request.getAttribute(GSXML.TYPE_ATT);
	String lang = request.getAttribute(GSXML.LANG_ATT);
	response.setAttribute(GSXML.TYPE_ATT, type);
	
	if (type.equals(GSXML.REQUEST_TYPE_DESCRIBE)) {
	    // create the collection element
	  Element description = (Element)response_doc.importNode(this.description, false);
	    response.appendChild(description);
	    // check the param list
	    Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	    if (param_list == null) {
		addAllDisplayInfo(description, lang);
		description.appendChild(response_doc.importNode(this.service_list, true));
		description.appendChild(response_doc.importNode(this.metadata_list, true));
		description.appendChild(response_doc.importNode(this.library_param_list, true));
		description.appendChild(response_doc.importNode(this.document_list, true));
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
			description.appendChild(response_doc.importNode(this.service_list, true));
		    } else if (info.equals(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(response_doc.importNode(metadata_list, true));
		    } else if (info.equals(GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER)) {
			addAllDisplayInfo(description, lang);
			
		    } else if (info.equals(GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER)) {
			description.appendChild(response_doc.importNode(this.document_list, true));
		    } else if (info.equals(GSXML.LIBRARY_PARAM_ELEM+GSXML.LIST_MODIFIER)) {
		      
			description.appendChild(response_doc.importNode(this.library_param_list, true));
		    }
		
		}
	    }
	    return response;
	}
	return super.processMessage(response_doc, request);
	    
    }
  
}
