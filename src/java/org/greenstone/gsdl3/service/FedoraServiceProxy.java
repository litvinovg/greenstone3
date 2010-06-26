/*
 *    ServiceRack.java
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

// greenstone classes
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.core.*;

// for fedora
import org.greenstone.gs3client.dlservices.*;
import org.greenstone.fedora.services.FedoraGS3Exception.CancelledException;

// xml classes
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Element; 
import org.w3c.dom.Document; 
import org.xml.sax.InputSource;
import javax.xml.parsers.*;
import org.apache.xpath.XPathAPI;

// general java classes
import java.io.Reader;
import java.io.StringReader;
import java.io.File;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Locale;
import java.lang.reflect.Method;

import org.apache.log4j.*;

/**
 * FedoraServiceProxy - communicates with the FedoraGS3 interface.
 *
 * @author Anupama Krishnan
 */
public class FedoraServiceProxy
    extends ServiceRack
{

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.FedoraServiceProxy.class.getName());

    /** The handle to the fedora connection */
    private DigitalLibraryServicesAPIA fedoraServicesAPIA;

    private String prevLanguage = "";

    public void cleanUp() { 
	super.cleanUp();
    }
    
    /** sets the message router */
    public void setMessageRouter(MessageRouter m) {
       this.router = m;
       setLibraryName(m.getLibraryName());
    }

    /** the no-args constructor */
    public FedoraServiceProxy() {
	super();

	this.converter = new XMLConverter();
	this.doc = this.converter.newDOM();
	this.short_service_info = this.doc.createElement(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER);
	this.format_info_map = new HashMap();

    }
    

    /* configure the service module
     *
     * @param info the XML node <serviceRack name="XXX"/> with name equal
     * to the class name (of the subclass)
     *
     * must configure short_service_info_ and service_info_map_
     * @return true if configured ok
     * must be implemented in subclasses
     */
    /*public boolean configure(Element info) {
	return configure(info, null);
	}*/
    
    public boolean configure(Element info, Element extra_info) {
	// set up the class loader
		
	if (!super.configure(info, extra_info)){
	    return false;
	}

	// Try to instantiate a Fedora dl handle
	try {
	    // The properties file containing the initial digital library connection
	    // settings which get displayed in the connection dialog fields
	    //final File propertiesFile = new File("gs3fedora.properties");
	    //fedoraServicesAPIA = new FedoraServicesAPIA(propertiesFile);

	    fedoraServicesAPIA = new FedoraServicesAPIA("http", "localhost", 8383, "fedoraIntCallUser", "changeme");//"fedoraAdmin", "pounamu"
	} catch(org.greenstone.fedora.services.FedoraGS3Exception.CancelledException e) {
	    // The user pressed cancel in the fedora services instantiation dlg
	    return false;
	} catch(Exception e) {
	    logger.error("Error instantiating the interface to the Fedora Repository:\n", e); // second parameter prints e's stacktrace
	    return false;
	} 

	
	// Need to put the available services into short_service_info
	// This is used by DefaultReceptionist.process() has an exception. But DefaultReceptionist.addExtraInfo() 
	// isn't helpful, and the problem actually already occurs in 
	// Receptionist.process() -> PageAction.process() -> MessageRouter.process() 
	// -> Collection/ServiceCluster.process() -> ServiceCluster.configureServiceRackList() 
	// -> ServiceRack.process() -> ServiceRack.processDescribe() -> ServiceRack.getServiceList().
	// ServiceRack.getServiceList() requires this ServiceRack's services to be filled into the 
	// short_service_info Element which needs to be done in this FedoraServiceProxy.configure().
	
	// get the display and format elements from the coll config file for
	// the classifiers
	AbstractBrowse.extractExtraClassifierInfo(info, extra_info);

	// Copied from IViaProxy.java:
	String collection = fedoraServicesAPIA.describeCollection(this.cluster_name);

	Element collNode = getResponseAsDOM(collection);
	Element serviceList = (Element)collNode.getElementsByTagName(GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER).item(0);

//this.short_service_info.appendChild(short_service_info.getOwnerDocument().importNode(serviceList, true));
	// we want the individual service Elements, not the serviceList Element which will wrap it later
	NodeList services = collNode.getElementsByTagName(GSXML.SERVICE_ELEM);
	for(int i = 0; i < services.getLength(); i++) {
	    Node service = services.item(i);
	    this.short_service_info.appendChild(short_service_info.getOwnerDocument().importNode(service, true));
	}

	// add some format info to service map if there is any
	String path = GSPath.appendLink(GSXML.SEARCH_ELEM, GSXML.FORMAT_ELEM);
	Element search_format = (Element) GSXML.getNodeByPath(extra_info, path);
	if (search_format != null) {
	    this.format_info_map.put("TextQuery", this.doc.importNode(search_format, true));
	}
	
	// look for document display format
	path = GSPath.appendLink(GSXML.DISPLAY_ELEM, GSXML.FORMAT_ELEM);
	Element display_format = (Element)GSXML.getNodeByPath(extra_info, path);
	if (display_format != null) {
	    this.format_info_map.put("DocumentContentRetrieve", this.doc.importNode(display_format, true));
	    // should we make a copy?
	}

	// the format info
	Element cb_format_info = this.doc.createElement(GSXML.FORMAT_ELEM);
	boolean format_found = false;

	// look for classifier <browse><format>
	path = GSPath.appendLink(GSXML.BROWSE_ELEM, GSXML.FORMAT_ELEM);
	Element browse_format = (Element)GSXML.getNodeByPath(extra_info, path);
	if (browse_format != null) {
	    cb_format_info.appendChild(GSXML.duplicateWithNewName(this.doc, browse_format, GSXML.DEFAULT_ELEM, true));
	    format_found = true;
	} 
	
	// add in to the description a simplified list of classifiers
	Element browse = (Element)GSXML.getChildByTagName(extra_info, "browse"); // the <browse>
	NodeList classifiers = browse.getElementsByTagName(GSXML.CLASSIFIER_ELEM);
	for(int i=0; i<classifiers.getLength(); i++) {
	    Element cl = (Element)classifiers.item(i);
	    Element new_cl = (Element)this.doc.importNode(cl, false); // just import this node, not the children
	    
	    // get the format info out, and put inside a classifier element
	    Element format_cl = (Element)new_cl.cloneNode(false);
	    Element format = (Element)GSXML.getChildByTagName(cl, GSXML.FORMAT_ELEM);
	    if (format != null) {
		
		//copy all the children
		NodeList elems = format.getChildNodes();
		for (int j=0; j<elems.getLength();j++) {
		    format_cl.appendChild(this.doc.importNode(elems.item(j), true));
		}
		cb_format_info.appendChild(format_cl);
		format_found = true;
	    }
	    	    
	}
	    
	if (format_found) {
	    this.format_info_map.put("ClassifierBrowse", cb_format_info);
	}
	
	return true;
    }

  
    /* "DocumentContentRetrieve", "DocumentMetadataRetrieve", "DocumentStructureRetrieve", 
      "TextQuery", "FieldQuery", "ClassifierBrowse", "ClassifierBrowseMetadataRetrieve" */

    protected Element processDocumentContentRetrieve(Element request) {
	String[] docIDs = parseDocIDs(request, GSXML.DOC_NODE_ELEM);
	if(docIDs == null) {
	    logger.error("DocumentContentRetrieve request specified no doc nodes.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	} else {
	    for(int i = 0; i < docIDs.length; i++) {
		docIDs[i] = translateId(docIDs[i]);
	    }
	}
	
	// first param (the collection) is not used by Fedora
	Element response = getResponseAsDOM(fedoraServicesAPIA.retrieveDocumentContent(this.cluster_name, docIDs));
	return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0); 
    }

    protected Element processDocumentStructureRetrieve(Element request) {
	String[] docIDs = parseDocIDs(request, GSXML.DOC_NODE_ELEM);
	
	if(docIDs == null) {
	    logger.error("DocumentStructureRetrieve request specified no doc nodes.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	} else {
	    for(int i = 0; i < docIDs.length; i++) {
		docIDs[i] = translateId(docIDs[i]);
	    }
	}

	NodeList params = request.getElementsByTagName(GSXML.PARAM_ELEM);
	String structure="";
	String info="";
	for(int i = 0; i < params.getLength(); i++) {
	    Element param = (Element)params.item(i);
	    if(param.getAttribute("name").equals("structure")) {
		structure = structure + param.getAttribute("value") + "|";
	    } else if(param.getAttribute("name").equals("info")) {
		info = info + param.getAttribute("value") + "|";
	    }
	}

	Element response = getResponseAsDOM(fedoraServicesAPIA.retrieveDocumentStructure(this.cluster_name, docIDs, new String[]{structure}, new String[]{info}));
	return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0); 
    }

    protected Element processDocumentMetadataRetrieve(Element request) {
	String[] docIDs = parseDocIDs(request, GSXML.DOC_NODE_ELEM);
	if(docIDs == null) {
	    logger.error("DocumentMetadataRetrieve request specified no doc nodes.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	} else {
	    for(int i = 0; i < docIDs.length; i++) {
		docIDs[i] = translateId(docIDs[i]);
	    }
	}
	
	NodeList params = request.getElementsByTagName(GSXML.PARAM_ELEM);
	String[] metafields = {};
	if(params.getLength() > 0) {
	    metafields = new String[params.getLength()];
	    for(int i = 0; i < metafields.length; i++) {
		Element param = (Element)params.item(i);
		//if(param.hasAttribute(GSXML.NAME_ATT) && param.getAttribute(GSXML.NAME_ATT).equals("metadata") && param.hasAttribute(GSXML.VALUE_ATT)) {
		if(param.hasAttribute(GSXML.VALUE_ATT)){ 
		    metafields[i] = param.getAttribute(GSXML.VALUE_ATT);
		} else {
		    metafields[i] = "";
		}
	    }
	}

	Element response = getResponseAsDOM(fedoraServicesAPIA.retrieveDocumentMetadata(this.cluster_name, docIDs, metafields));
	return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0); 
    }

    protected Element processClassifierBrowseMetadataRetrieve(Element request) {
	String[] classIDs = parseDocIDs(request, GSXML.CLASS_NODE_ELEM);
	if(classIDs == null) {
	    logger.error("ClassifierBrowseMetadataRetrieve request specified no classifier nodes.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	} else {
	    for(int i = 0; i < classIDs.length; i++) {
		classIDs[i] = translateId(classIDs[i]);
	    }
	}
	
	NodeList params = request.getElementsByTagName(GSXML.PARAM_ELEM);
	String[] metafields = {};
	if(params.getLength() > 0) {
	    metafields = new String[params.getLength()];
	    for(int i = 0; i < metafields.length; i++) {
		Element param = (Element)params.item(i);
		if(param.hasAttribute(GSXML.VALUE_ATT)){ 
		    metafields[i] = param.getAttribute(GSXML.VALUE_ATT);
		} else {
		    metafields[i] = "";
		}
	    }
	}

	Element response 
	    = getResponseAsDOM(fedoraServicesAPIA.retrieveBrowseMetadata(this.cluster_name, 
									 "ClassifierBrowseMetadataRetrieve", 
									 classIDs, metafields));
	return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0);
    }

    protected Element processClassifierBrowse(Element request) {
	String collection = this.cluster_name;
	String lang = request.getAttribute(GSXML.LANG_ATT);
	if(!lang.equals(prevLanguage)) {
	    prevLanguage = lang;
	    fedoraServicesAPIA.setLanguage(lang);
	}

	NodeList classNodes = request.getElementsByTagName(GSXML.CLASS_NODE_ELEM);
	if(classNodes == null || classNodes.getLength() <= 0) {
	    logger.error("ClassifierBrowse request specified no classifier IDs.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	}
	String classifierIDs[] = new String[classNodes.getLength()];
	for(int i = 0; i < classifierIDs.length; i++) {
	    Element e = (Element)classNodes.item(i);
	    classifierIDs[i] = e.getAttribute(GSXML.NODE_ID_ATT);
	    classifierIDs[i] = translateId(classifierIDs[i]);	
	}
	
	NodeList params = request.getElementsByTagName(GSXML.PARAM_ELEM);
	String structure="";
	String info="";
	for(int i = 0; i < params.getLength(); i++) {
	    Element param = (Element)params.item(i);
	    if(param.getAttribute("name").equals("structure")) {
		structure = structure + param.getAttribute("value") + "|";
	    } else if(param.getAttribute("name").equals("info")) {
		info = info + param.getAttribute("value") + "|";
	    }
	}
	///structure = structure + "siblings"; //test for getting with classifier browse structure: siblings
	
	Element response 
	    = getResponseAsDOM(fedoraServicesAPIA.retrieveBrowseStructure(collection, "ClassifierBrowse", classifierIDs,
									  new String[] {structure}, new String[] {info}));
	///logger.error("**** FedoraServiceProxy - Response from retrieveBrowseStructure: " + GSXML.nodeToFormattedString(response));	
	
	return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0);
    }

    protected Element processTextQuery(Element request) {
	return processQuery(request, "TextQuery");
    }

    protected Element processFieldQuery(Element request) {
	return processQuery(request, "FieldQuery");
    }

    protected Element processQuery(Element request, String querytype) {
	String collection = this.cluster_name;

	String lang = request.getAttribute(GSXML.LANG_ATT);
	if(!lang.equals(prevLanguage)) {
	    prevLanguage = lang;
	    fedoraServicesAPIA.setLanguage(lang);
	}

	NodeList paramNodes = request.getElementsByTagName(GSXML.PARAM_ELEM);
	if(paramNodes.getLength() > 0) {
	    HashMap params = new HashMap(paramNodes.getLength());
	    for(int i = 0; i < paramNodes.getLength(); i++) {
		Element param = (Element)paramNodes.item(i);
		params.put(param.getAttribute(GSXML.NAME_ATT), param.getAttribute(GSXML.VALUE_ATT));
	    }

	    Element response = getResponseAsDOM(fedoraServicesAPIA.query(collection, querytype, params));
	    return (Element)response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(0);
	} else {
	    logger.error("TextQuery request specified no parameters.\n");
	    return this.doc.createElement(GSXML.RESPONSE_ELEM); // empty response
	}
    }

    protected String[] parseDocIDs(Element request, String nodeType) {
	String lang = request.getAttribute(GSXML.LANG_ATT);
	if(!lang.equals(prevLanguage)) {
	    prevLanguage = lang;
	    fedoraServicesAPIA.setLanguage(lang);
	}
	
	String[] docIDs = null;

	Element docList = (Element) GSXML.getChildByTagName(request, nodeType+GSXML.LIST_MODIFIER);
	if (docList != null) {	    
	    NodeList docNodes = docList.getElementsByTagName(nodeType);
	    if(docNodes.getLength() > 0) {
		docIDs = new String[docNodes.getLength()];
		for(int i = 0; i < docIDs.length; i++) {
		    Element e = (Element)docNodes.item(i);
		    docIDs[i] = e.getAttribute(GSXML.NODE_ID_ATT);
		}
	    }
	}
	return docIDs;
    }

    /** if id ends in .fc, .pc etc, then translate it to the correct id 
     * For now (for testing things work) the default implementation is to just remove the suffix */
    protected String translateId(String id) {
	if (OID.needsTranslating(id)) {
	    return translateOID(id);
	}
	return id;}
    
    /** if an id is not a greenstone id (an external id) then translate 
     * it to a greenstone one 
     * default implementation: return the id */
    protected String translateExternalId(String id) {
	return id;
    }

  /** translates relative oids into proper oids:
   * .pr (parent), .rt (root) .fc (first child), .lc (last child),
   * .ns (next sibling), .ps (previous sibling) 
   * .np (next page), .pp (previous page) : links sections in the order that you'd read the document
   * a suffix is expected to be present so test before using 
   */
  public String translateOID(String oid) {      
    int p = oid.lastIndexOf('.');
    if (p != oid.length()-3) {
      logger.info("translateoid error: '.' is not the third to last char!!");
      return oid;
    }
	
    String top = oid.substring(0, p);
    String suff = oid.substring(p+1);

    // just in case we have multiple extensions, we must translate
    // we process inner ones first
    if (OID.needsTranslating(top)) {
      top = translateOID(top);
    }
    if (suff.equals("pr")) {
      return OID.getParent(top);
    } 
    if (suff.equals("rt")) {
      return OID.getTop(top);
    } 
    if (suff.equals("np")) {
      // try first child

      String node_id = translateOID(top+".fc");
      if (!node_id.equals(top)) {
	  return node_id;
      }

      // try next sibling
      node_id = translateOID(top+".ns");
      if (!node_id.equals(top)) {
	  return node_id;
      }
      // otherwise we keep trying parents sibling
      String child_id = top;
      String parent_id = OID.getParent(child_id);
      while(!parent_id.equals(child_id)) {
	node_id = translateOID(parent_id+".ns");
	if (!node_id.equals(parent_id)) {
	  return node_id;
	}
	child_id = parent_id;
	parent_id = OID.getParent(child_id);
      }
      return top; // we couldn't get a next page, so just return the original
    } 
    if (suff.equals("pp")) {
      String prev_sib = translateOID(top+".ps");
      if (prev_sib.equals(top)) {
	// no previous sibling, so return the parent
	return OID.getParent(top);
      }
      // there is a previous sibling, so its either this section, or the last child of the last child
      String last_child = translateOID(prev_sib+".lc");
      while (!last_child.equals(prev_sib)) {
	prev_sib = last_child;
	last_child = translateOID(prev_sib+".lc");
      }
      return last_child;
    } 
	
    int sibling_num = 0;
    if (suff.equals("ss")) {
      // we have to remove the sib num before we get top
      p = top.lastIndexOf('.');
      sibling_num = Integer.parseInt(top.substring(p+1));
      top = top.substring(0, p);
    }
	
    // need to get info out of Fedora
    String doc_id = top;
    if (suff.endsWith("s")) {
      doc_id = OID.getParent(top);
      if (doc_id.equals(top)) {
	// i.e. we are already at the top
	return top;
      }
    }
    
    // send off request to get sibling etc. information from Fedora
    Element response = null;
    String[] children = null;
    if(doc_id.startsWith("CL")) { // classifiernode
	response = getResponseAsDOM(fedoraServicesAPIA.retrieveBrowseStructure(this.cluster_name, "ClassifierBrowse", new String[]{doc_id},
									       new String[]{"children"}, new String[]{"siblingPosition"}));
	NodeList nl = response.getElementsByTagName(GSXML.NODE_STRUCTURE_ELEM);
	if(nl.getLength() > 0) {
	    Element nodeStructure = (Element)nl.item(0);

	    if(nodeStructure != null) {
		Element root = (Element) GSXML.getChildByTagName(nodeStructure, GSXML.CLASS_NODE_ELEM);
		if(root != null) { // get children
		    NodeList classNodes = root.getElementsByTagName(GSXML.CLASS_NODE_ELEM);
		    if(classNodes != null) {
			children = new String[classNodes.getLength()];
			for(int i = 0; i < children.length; i++) {
			    Element child = (Element)classNodes.item(i);
			    children[i] = child.getAttribute(GSXML.NODE_ID_ATT);
			}
		    }
		}
	    }
	}
    } else { // documentnode
	response = getResponseAsDOM(fedoraServicesAPIA.retrieveDocumentStructure(this.cluster_name, new String[]{doc_id},
										 new String[]{"children"}, new String[]{"siblingPosition"}));
	String path = GSPath.createPath(new String[]{GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER, 
					      GSXML.DOC_NODE_ELEM, GSXML.NODE_STRUCTURE_ELEM, GSXML.DOC_NODE_ELEM});	
	Element parentDocNode = (Element) GSXML.getNodeByPath(response, path);

	if (parentDocNode == null) {
	    return top;
	} // else 
	NodeList docNodes = parentDocNode.getElementsByTagName(GSXML.DOC_NODE_ELEM); // only children should remain, since that's what we requested
	if(docNodes.getLength() > 0) {
	    children = new String[docNodes.getLength()];
	    
	    for(int i = 0; i < children.length; i++) {
		Element e = (Element)docNodes.item(i);
		children[i] = e.getAttribute(GSXML.NODE_ID_ATT);
	    }
	} else { // return root node
	    children = new String[]{doc_id};
	}
    }
    
    if (suff.equals("fc")) {
      return children[0];
    } else if (suff.equals("lc")) {
      return children[children.length-1];
    } else {
      if (suff.equals("ss")) {
	return children[sibling_num-1];
      }
      // find the position that we are at.
      int i=0;
      while(i<children.length) {
	if (children[i].equals(top)) {
	  break;
	}
	i++;
      }
	    
      if (suff.equals("ns")) {
	if (i==children.length-1) {
	  return children[i];
	}
	return children[i+1];
      } else if (suff.equals("ps")) {
	if (i==0) {
	  return children[i];
	}
	return children[i-1];
      }
    }

    return top;
  }


    protected Element getResponseAsDOM(String response) {
	if(response == null) { // will not be the case, because an empty  
	    return null;    // response message will be sent instead
	}

	Element message = null;		
	try{
	    // turn the String xml response into a DOM tree:	
	    DocumentBuilder builder 
		= DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc 
		= builder.parse(new InputSource(new StringReader(response)));
	    message = doc.getDocumentElement();
	} catch(Exception e){
	    if(response == null) {
		response = "";
	    }
	    logger.error("An error occurred while trying to parse the response: ");
	    logger.error(response);
	    logger.error(e.getMessage());
	}
	
	// Error elements in message will be processed outside of here, just return the message
	return message;
    }

    /* //process method for stylesheet requests    
    protected Element processFormat(Element request) {}	*/
    
    /* returns the service list for the subclass */
    /* protected Element getServiceList(String lang) {
	// for now, it is static and has no language stuff
	return (Element) this.short_service_info.cloneNode(true);
	}*/

    /** returns a specific service description */
    protected Element getServiceDescription(String service, String lang, String subset) {
	if(!lang.equals(prevLanguage)) {
	    prevLanguage = lang;
	    fedoraServicesAPIA.setLanguage(lang);
	}
	String serviceResponse = fedoraServicesAPIA.describeService(service);
	Element response = getResponseAsDOM(serviceResponse);

	// should be no chance of an npe, since FedoraGS3 lists the services, so will have descriptions for each
	Element e = (Element)response.getElementsByTagName(GSXML.SERVICE_ELEM).item(0);
	e = (Element)this.doc.importNode(e, true);
	return e; 
    }

    protected Element getServiceFormat(String service) {
	Element format = (Element)((Element)this.format_info_map.get(service)).cloneNode(true);
	return format;
    }

    /** overloaded version for no args case */
    protected String getTextString(String key, String lang) {
	return getTextString(key, lang, null, null);
    }

    protected String getTextString(String key, String lang, String dictionary) {
	return getTextString(key, lang, dictionary, null);
    }
    protected String getTextString(String key, String lang, String [] args) {
	return getTextString(key, lang, null, args);
    }
	
    /** getTextString - retrieves a language specific text string for the given
key and locale, from the specified resource_bundle (dictionary)
    */
    protected String getTextString(String key, String lang, String dictionary, String[] args) {

	// we want to use the collection class loader in case there are coll specific files
	if (dictionary != null) {
	    // just try the one specified dictionary
	    Dictionary dict = new Dictionary(dictionary, lang, this.class_loader);
	    String result = dict.get(key, args);
	    if (result == null) { // not found
		return "_"+key+"_";
	    }
	    return result;
	}

	// now we try class names for dictionary names
	String class_name = this.getClass().getName();
	class_name = class_name.substring(class_name.lastIndexOf('.')+1);
	Dictionary dict = new Dictionary(class_name, lang, this.class_loader);
	String result = dict.get(key, args);
	if (result != null) {
	    return result;
	}

	// we have to try super classes
	Class c = this.getClass().getSuperclass();
	while (result == null && c != null) {
	    class_name = c.getName();
	    class_name = class_name.substring(class_name.lastIndexOf('.')+1);
	    if (class_name.equals("ServiceRack")) {
		// this is as far as we go
		break;
	    }
	    dict = new Dictionary(class_name, lang, this.class_loader);
	    result = dict.get(key, args);
	    c = c.getSuperclass();
	}
	if (result == null) {
	    return "_"+key+"_";
	}
	return result;
	
    }

    protected String getMetadataNameText(String key, String lang) {

	String properties_name = "metadata_names";
	Dictionary dict = new Dictionary(properties_name, lang);
	
	String result = dict.get(key);
	if (result == null) { // not found
	    return null;
	} 
	return result;
    }
}

