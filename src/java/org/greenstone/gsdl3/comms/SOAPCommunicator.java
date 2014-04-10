/*
 *    SOAPCommunicator.java
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
package org.greenstone.gsdl3.comms;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.core.*;

// classes needed for SOAP stuff
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 

import org.apache.log4j.*;

/*
 * The Client side of a SOAP Connection
 *
 * @author Katherine Don
 * @version $Revision$
 * @see <a href="http://www.w3.org/TR/SOAP/">Simple Object Access Protocol (SOAP) 1.1 </a>
 */
public class SOAPCommunicator
    extends Communicator {

      static Logger logger = Logger.getLogger(org.greenstone.gsdl3.comms.SOAPCommunicator.class.getName());

    /** address of site to connect to */
    protected String remote_site_address_=null;

    /** the call object that does the SOAP talking */
    private Call call_ = null;

    /** The no-args constructor */
    public SOAPCommunicator() {	
    }
        
    public boolean configure(Element site_elem) {
	
	String type = site_elem.getAttribute(GSXML.TYPE_ATT);
	if (!type.equals(GSXML.COMM_TYPE_SOAP_JAVA)) {
	    logger.error("wrong type of site");
	    return false;
	}
	remote_site_name_ = site_elem.getAttribute(GSXML.NAME_ATT);
	if (remote_site_name_.equals("")) {
	    logger.error("must have name attribute in site element");
	    return false;
	}
	remote_site_address_ = site_elem.getAttribute(GSXML.ADDRESS_ATT);
	String local_site_name = site_elem.getAttribute(GSXML.LOCAL_SITE_ATT);
	if (remote_site_address_.equals("") && local_site_name.equals("")) {
	    logger.error("must have address or localSite attributes in site element");
	    return false;
	}
	if (remote_site_address_.equals("")) {
	    remote_site_address_ = GlobalProperties.getGSDL3WebAddress()+"/services/"+local_site_name;
	}

	try {
	    Service service = new Service();
	    call_ = (Call) service.createCall();
	    call_.setTargetEndpointAddress( new java.net.URL(remote_site_address_) );
	} catch (Exception e) {
	    logger.error("SOAPCommunicator.configure() Error: Exception occurred "+e);
	    return false;
	}
	return true;
    }

    public Node process(Node message_node) {
	
	Element message = GSXML.nodeToElement(message_node);

	NodeList requests = message.getElementsByTagName(GSXML.REQUEST_ELEM);
	if (requests.getLength()==0) {
	    // no requests
	    return null;
	}

	// if the communicator is part of a site, it needs to edit the to and from fields, otherwise it just passes the message on, as is
	// for now, use local_site_name_ as the flag.
	if (local_site_name_!=null) { // no local site
	    
	    for (int i=0;i<requests.getLength(); i++) {
		Element e = (Element)requests.item(i);
		String to = e.getAttribute(GSXML.TO_ATT);
		to = GSPath.removeFirstLink(to); // remove the server name, should check that it is present
		e.setAttribute(GSXML.TO_ATT, to);
		String from = e.getAttribute(GSXML.FROM_ATT);
		from = GSPath.appendLink(from, local_site_name_);
		e.setAttribute(GSXML.FROM_ATT, from);
	    }
	}
	// else do nothing to the requests, just pass it on

	// the soap impl needs a namespace for the top level element
	Element message_to_send = message;
	if (message.getNamespaceURI() == null) {
	    message_to_send = GSXML.duplicateWithNewNameNS(message.getOwnerDocument(), message, "gs3:"+message.getTagName(), "urn:foo", true);
	}
	
	// do the soap query
	Element result = null;
	try {
	    SOAPBodyElement[] input = new SOAPBodyElement[1];
	    input[0] = new SOAPBodyElement(message_to_send);
	    Vector output = (Vector) call_.invoke( input );
	    SOAPBodyElement elem = (SOAPBodyElement) output.get(0);
	    result = elem.getAsDOM();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	if (local_site_name_ != null) {// have to modify the from field
	    
	    NodeList responses = result.getElementsByTagName(GSXML.RESPONSE_ELEM);
	    for (int i=0;i<responses.getLength(); i++) {
		Element e = (Element)responses.item(i);
		String from = e.getAttribute(GSXML.FROM_ATT);
		from = GSPath.prependLink(from, remote_site_name_);
		e.setAttribute(GSXML.FROM_ATT, from);
	    }
	} // else return as is
	
	return result;
    }

    public static void main (String [] args) {
	SOAPCommunicator comm = new SOAPCommunicator();
	XMLConverter converter = new XMLConverter();
	String message = "<site name=\"localsite\" address=\"http://kanuka.cs.waikato.ac.nz:7070/axis/services/localsite\" type=\"soap\"/>";
	Element site_elem = converter.getDOM(message).getDocumentElement();
	comm.configure(site_elem);
	message = "<message><request type=\"describe\" to=\"\" lang=\"en\"/></message>";
	Element request_elem = converter.getDOM(message).getDocumentElement();
	Node response = comm.process(request_elem);
	
	logger.error("response was "+converter.getPrettyString(response));
    }
}








