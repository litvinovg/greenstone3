package org.greenstone.gsdl3.core;

import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.action.*;
// XML classes
import org.w3c.dom.Node; 
import org.w3c.dom.NodeList; 
import org.w3c.dom.Document; 
import org.w3c.dom.Element; 

// other java classes
import java.io.File;
import java.util.HashMap;
import java.util.Enumeration;

import org.apache.log4j.*;

/** The default greenstone receptionist - needs some extra info for each page
*/
public class DefaultReceptionist extends TransformingReceptionist {

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.core.DefaultReceptionist.class.getName());
	
	/** add in the collection description to the page, then for each service, add in the service description */
	protected void addExtraInfo(Element page) {
		super.addExtraInfo(page);
		
		Element page_request = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_REQUEST_ELEM);
		// if it is a system request, then we don't bother with this.
		String action = page_request.getAttribute(GSXML.ACTION_ATT);
		if (action.equals("s")) {
			logger.error("HACK: don't ask for coll info if system action");
			return;
		}
		logger.debug("add extra info, page request="+this.converter.getString(page_request));
		// is a collection defined?
		Element param_list = (Element)GSXML.getChildByTagName(page_request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (param_list==null) { // must be the original home page
			logger.debug(" no param list, assuming home page");
			return;
		}
		Element coll_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSParams.COLLECTION);
		if (coll_param == null) {
			logger.debug(" coll param is null, returning");
			return;
		}

		// see if the collection/cluster element is already there
		String coll_name = coll_param.getAttribute(GSXML.VALUE_ATT);
		String lang = page_request.getAttribute(GSXML.LANG_ATT);
		String uid = page_request.getAttribute(GSXML.USER_ID_ATT);
		
		if (coll_name.equals("")) {
			coll_name = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, "p.c").getAttribute(GSXML.VALUE_ATT);
		}
		
		boolean get_service_description = false;
		Element page_response = (Element)GSXML.getChildByTagName(page, GSXML.PAGE_RESPONSE_ELEM);
		if (this.language_list != null) {
			page_response.appendChild(this.language_list);
		}
		Element coll_description = (Element)GSXML.getChildByTagName(page_response, GSXML.COLLECTION_ELEM);
		if (coll_description == null) {
			// try cluster
			coll_description = (Element)GSXML.getChildByTagName(page_response, GSXML.CLUSTER_ELEM);
		}
		if (coll_description == null) { 
		
			// we dont have one yet - get it
			Element coll_about_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
			Element coll_about_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE,  coll_name, lang, uid);
			coll_about_message.appendChild(coll_about_request);
			
			Node coll_about_response_message = this.mr.process(coll_about_message);
			Element coll_about_response = (Element)GSXML.getChildByTagName(coll_about_response_message, GSXML.RESPONSE_ELEM);
			if (coll_about_response == null) {
				return;
			}
			coll_description = (Element)GSXML.getChildByTagName(coll_about_response, GSXML.COLLECTION_ELEM);
			if (coll_description==null) { // may be a cluster
				coll_description = (Element)GSXML.getChildByTagName(coll_about_response, GSXML.CLUSTER_ELEM);
			}
			
			if (coll_description == null) {
				logger.error(" no collection description, returning");
				return;
			}
			// have found one, append it to the page response
			coll_description = (Element)this.doc.importNode(coll_description, true);
			page_response.appendChild(coll_description);
			get_service_description = true;
		}

		// have got a coll description
		// now get the dispay info for the services
		Element service_list = (Element)GSXML.getChildByTagName(coll_description, GSXML.SERVICE_ELEM+GSXML.LIST_MODIFIER);
		if (service_list == null) {
			logger.error(" no service list, returning");
			// something weird has gone wrong
			return;
		}

		NodeList services = service_list.getElementsByTagName(GSXML.SERVICE_ELEM);
		if (services.getLength()==0) {
			logger.error("DefaultReceoptionist: no services found for colllection/cluster "+ coll_name);
			return;
		}
		// check one service for display items
		if (!get_service_description) {
			// we dont know yet if we need to get these
			int i=1;
			Element test_s = (Element)services.item(0);
			while (i<services.getLength() && (test_s.getAttribute(GSXML.TYPE_ATT).equals(GSXML.SERVICE_TYPE_RETRIEVE) || test_s.getAttribute(GSXML.TYPE_ATT).equals(GSXML.SERVICE_TYPE_OAI))) {
				test_s = (Element)services.item(i); i++;
			}
			if (i==services.getLength()) {
				// we have only found retrieve or oai services, so dont need descripitons anyway
				return;
			}
			if (GSXML.getChildByTagName(test_s, GSXML.DISPLAY_TEXT_ELEM) !=null) {
				// have got descriptions already, 
				return;
			}
		}

		// if get here, we need to get the service descriptions
		
		// we will send all the requests in a single message
		Element info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		for (int i=0; i<services.getLength(); i++) {
			Element c = (Element)services.item(i);
			String name = c.getAttribute(GSXML.NAME_ATT);
			String address = GSPath.appendLink(coll_name, name);
			Element info_request = GSXML.createBasicRequest(this.doc,  GSXML.REQUEST_TYPE_DESCRIBE, address, lang, uid);
			//Element req_param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
			//req_param_list.appendChild(GSXML.createParameter(this.doc, GSXML.SUBSET_PARAM, GSXML.DISPLAY_TEXT_ELEM+GSXML.LIST_MODIFIER));
			//info_request.appendChild(req_param_list);
			info_message.appendChild(info_request);
			
		}

		Element info_response = (Element)this.mr.process(info_message);

		NodeList service_responses = info_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		// check that have same number of responses as collections
		if (services.getLength() != service_responses.getLength()) {
			logger.error(" didn't get a response for each service - somethings gone wrong!");
			// for now, dont use the metadata
		} else {
			for (int i=0; i<services.getLength(); i++) {
				Element c1 = (Element)services.item(i);
				Element c2 = (Element)GSXML.getChildByTagName((Element)service_responses.item(i), GSXML.SERVICE_ELEM);
				if (c1 !=null && c2 !=null && c1.getAttribute(GSXML.NAME_ATT).equals(c2.getAttribute(GSXML.NAME_ATT))) {
					//add the service data into the original response
					GSXML.mergeElements(c1, c2);
				} else {
					logger.debug(" response does not correspond to request!");
				} 
				
			}
		}
	}
}

