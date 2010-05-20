package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.util.*;
// XML classes
import org.w3c.dom.Node; 
import org.w3c.dom.Element; 

import java.util.HashMap;

public class GeneralAction extends Action {
    
    /** process a request */
    public Node process (Node message_node) {

	Element message = this.converter.nodeToElement(message_node);

	// the result
	Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
	result.appendChild(page_response);

	// assume only one request
	Element request = (Element)GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);
	logger.debug(" request="+this.converter.getString(request));

	String lang = request.getAttribute(GSXML.LANG_ATT);
	String uid = request.getAttribute(GSXML.USER_ID_ATT);

	// get the param list
	Element cgi_param_list = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	HashMap params = GSXML.extractParams(cgi_param_list, false);
	String service_name = (String) params.get(GSParams.SERVICE);
	String cluster_name = (String) params.get(GSParams.CLUSTER);
	String response_only_p = (String)params.get(GSParams.RESPONSE_ONLY);
	boolean response_only = false;
	if (response_only_p!=null) {
	    response_only = (response_only_p.equals("1")?true:false);
	}
	String request_type = (String) params.get(GSParams.REQUEST_TYPE);
	// what is carried out depends on the request_type
	// if rt=d, then a describe request is done,
	// is rt=r, a request and then a describe request is done
	// if rt=s, a status request is done.

	// if ro=1, then this calls for a process only page - we do the request
	// (rt should be r or s) and just give the response straight back
	// without any page processing


	// where to send requests
	String to;
	if (cluster_name != null) {
	    to = GSPath.appendLink(cluster_name, service_name);
	} else {
	    to = service_name;
	}

	if (request_type.equals("r")) {
	    //do the request
	    
	    Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	    Element mr_query_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
	    
	    mr_query_message.appendChild(mr_query_request);
	    
	    Element param_list = null;
	    // add in the service params - except the ones only used by the action
	    HashMap service_params = (HashMap)params.get("s1");
	    if (service_params != null) { 
		param_list = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(this.doc, param_list, service_params);
		mr_query_request.appendChild(param_list);
	    }
	    
	    Element mr_query_response = (Element)this.mr.process(mr_query_message);
	    Element result_response = (Element)GSXML.getChildByTagName(mr_query_response, GSXML.RESPONSE_ELEM);
	    
	    if (response_only) {
		// just send the reponse as is
		addSiteMetadata(result_response, lang, uid);
		return result_response;
	    }
	    if (result_response != null){
		// else append the contents of the response to the page 
		GSXML.copyAllChildren(page_response, result_response);
	    }
	}
	

	// another part of the page is the service description

	// request the service info for the selected service - should be cached
	Element mr_info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
	Element mr_info_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, to, lang, uid);
	mr_info_message.appendChild(mr_info_request);
	Element mr_info_response = (Element) this.mr.process(mr_info_message);

        
	String path = GSXML.RESPONSE_ELEM;
	path = GSPath.appendLink(path, GSXML.SERVICE_ELEM);
	

	Node desNode = GSXML.getNodeByPath(mr_info_response, path);
	if(desNode != null){
	    page_response.appendChild((Element)this.doc.importNode(desNode, true));
	}

	addSiteMetadata(page_response, lang, uid);
	return result;
    }

}
