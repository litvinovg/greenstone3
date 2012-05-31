/*
 *    XSLTServices.java
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

import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.selfContained.*;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.TextImpl;
import org.w3c.dom.Document; 
import org.w3c.dom.Node; 
import org.w3c.dom.Text; 
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList; 

import java.util.HashMap;
import java.util.Vector;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Locale;
import java.io.*;
import java.net.URLEncoder;
import java.net.URLDecoder;

import org.apache.log4j.*;

/**
 * A ServiceRack class for XSLT query. 
 *
 * Document ids are formed by encoding the document using standard URLEncoding
 *
 * @author Katherine Don
 * @author <a href="mailto:s.yeates@cs.waikato.ac.nz">Stuart Yeates</a>
 * @version $Revision$
 * @see ServiceRack
 * @see java.net.URLEncoder
 * @see java.net.URLDecoder
 */

public class XSLTServices 
  extends ServiceRack {
  
    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.XSLTServices.class.getName());

  // these strings must match what is found in the properties file
  // the services on offer
  private static final String XSLT_QUERY_SERVICE        = "XSLTQuery";
  private static final String RESOURCE_RETRIEVE_SERVICE = "ResourceRetrieve";
  // params used
  private static final String QUERY_PARAM        = "query";
  private static final String ALPHABET_PARAM     = "alphabet";
  
  private static final int RESULT_SET_SIZE        = 10;

  // alphabets
  private static final String MINIMUM_ALPHABET   = "minimum";
  private static final String ASCII_ALPHABET     = "ascii";
  private static final String UNICODE_ALPHABET   = "unicode";

  private static final String DEFAULT_QUERY_STRING   =
    "<xsl:stylesheet version=\"1.0\""+
    "                xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"+
    "    <xsl:template match=\"*|/\">"+
    "         <xsl:apply-templates/>"+
    "    </xsl:template>"+
    "    <xsl:template match=\"*|/\" mode=\"m\">"+
    "         <xsl:apply-templates mode=\"m\"/>"+
    "    </xsl:template>"+
    "    <xsl:template match=\"text()|@*\">"+
    "         <xsl:value-of select=\".\"/>"+
    "    </xsl:template>"+
    "    <xsl:template match=\"processing-instruction()|comment()\"/>"+
    "</xsl:stylesheet>";
  

  private String alphabet = MINIMUM_ALPHABET;
  private Element config_info_ = null;
  

  public XSLTServices() {
  }
  
    /** configure this service */
    public boolean configure(Element info, Element extra_info) {
      	if (!super.configure(info, extra_info)){
	    return false;
	}

      logger.info("configuring XSLTServices");
      this.config_info = info;
    try {
      logger.info("called with:");
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Element doc = GSXML.getFirstElementChild(info);//(Element)info.getFirstChild();
      Transformer transformer = transformerFactory.newTransformer();
      StreamResult result = new StreamResult(System.out);
      Source source = new DOMSource(doc);
      transformer.transform(source,result);
    } catch (Throwable t) {
      logger.error("Error printing XML in XSLTService::configure()");
    }
    return true;    
  }

    protected Element getServiceDescription(String service, String lang, String subset) {

	return null;
    }
  
  /** process method for specific services - must be implemented by all
   * subclasses
   * should implement all services supported by the servicesImpl except
   * for describe, which is implemented by this base class
   *
   */
  protected Element processService(String service, Element request){
    if (service.equals(XSLT_QUERY_SERVICE)) {
      return processXSLTQuery(request);
    } else if (service.equals(RESOURCE_RETRIEVE_SERVICE)) {
      return processResourceRetrieve(request);
    }
    // create the response so we can report the error
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    String from = GSPath.appendLink(this.cluster_name, RESOURCE_RETRIEVE_SERVICE);
    response.setAttribute(GSXML.FROM_ATT, from);
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
    
    logger.error("should never get here. service type wrong:"+service);
    GSXML.addError(this.doc, response,"XSLTServices:should never get here. service type wrong:"+service);
    return response;
  }

  /** process a document resquest query */
  protected Element processResourceRetrieve(Element request) {
    
    // create the result and set the path so we know where we are
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    String from = GSPath.appendLink(this.cluster_name, RESOURCE_RETRIEVE_SERVICE);
    response.setAttribute(GSXML.FROM_ATT, from);
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

    // get param list 
    Element param_elem=null;
    Node n = request.getFirstChild();
    while (n!=null) {
	String node_name = n.getNodeName();
	if (node_name.equals(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER)) {
	    param_elem = (Element)n;
	    break;
	}
	n = n.getNextSibling();
    }

    if (param_elem==null) { 
	logger.error("bad query request");
	GSXML.addError(this.doc, response,"bad query request in XSLTServices");
	return response; 
    }
    
    // Documents are just the ids decoding using standard URL decoding
    
    Element doc_list = (Element)GSXML.getChildByTagName(request, GSXML.DOCUMENT_ELEM+GSXML.LIST_MODIFIER);
    String []ids = GSXML.getAttributeValuesFromList(doc_list, GSXML.NAME_ATT);
    for (int j=0; j<ids.length; j++) {
      String document = null;
      try {
	document = URLDecoder.decode(ids[j],"UTF-8");
      } catch (UnsupportedEncodingException e) {
	throw new Error(e.toString());
      }
      // something funny with the doc - 
      Element new_doc = this.doc.createElement(GSXML.DOCUMENT_ELEM);
      new_doc.setAttribute(GSXML.NAME_ATT, ids[j]); //GSXML.createDocumentElement(this.doc, ids[j]);
      GSXML.addDocText(this.doc, new_doc, document);
      response.appendChild(new_doc);
    }
    
    return response;
  }
  
  /** process a XSLT query */
  protected Element processXSLTQuery(Element request) {
    
    // create the result and set the path so we know where we are
    Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
    String from = GSPath.appendLink(this.cluster_name, XSLT_QUERY_SERVICE);
    response.setAttribute(GSXML.FROM_ATT, from);
    response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

    // get the parameter list
    Element param_list = 
      (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);

    // extract the only parameter we care about
    HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
    String query = (String)params.get(QUERY_PARAM);
    
    if (query == null || query.equals(""))
      query = DEFAULT_QUERY_STRING;
    
    
    DocumentStream stream = null;
    if (alphabet.equals(MINIMUM_ALPHABET)){
      stream = new GeneratedDocumentStream('a','c');
    } else if(alphabet.equals(ASCII_ALPHABET)){
      stream = new GeneratedDocumentStream();
    } else if(alphabet.equals(UNICODE_ALPHABET)){
      stream = new GeneratedDocumentStream(Character.MIN_VALUE,
					   Character.MAX_VALUE);
    } else {
      logger.error("bad alphabet name : "+ alphabet);
      GSXML.addError(this.doc, response,"XSLTServices: bad alphabet name : "+ alphabet);
      stream = new GeneratedDocumentStream();
    }

    Element resource_list = this.doc.createElement(GSXML.RESOURCE_ELEM+GSXML.LIST_MODIFIER);
    response.appendChild(resource_list);

    // Framework to stringise the document
    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    // add each resource
    for (int d=0; d<RESULT_SET_SIZE; d++) {
      try {
	Document document = stream.nextDocument();
	Element doc = GSXML.getFirstElementChild(document);//(Element)document.getFirstChild();
	Transformer transformer = transformerFactory.newTransformer();
	StringWriter writer = new StringWriter();
	StreamResult result = new StreamResult(writer);
	Source source = new DOMSource(doc);
	transformer.transform(source,result);
	String id = writer.toString();
	Element e = this.doc.createElement(GSXML.DOCUMENT_ELEM);
	e.setAttribute(GSXML.NAME_ATT, id); 
	//Node no = GSXML.createDocumentElement(this.doc, id);
	resource_list.appendChild(e);
      } catch (Throwable t) {
	GSXML.addError(this.doc, response, "Error in XSLTServices finding results:" + t.toString());
      }
    }
    return response; 
  }
}  
 

