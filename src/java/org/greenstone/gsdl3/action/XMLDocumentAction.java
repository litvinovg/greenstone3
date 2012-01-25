package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.service.TEIRetrieve;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.Map;
import java.util.Iterator;
import java.io.File;

/** action class for retrieving parts of XML documents */
public class XMLDocumentAction extends Action
{

	/**
	 * process - processes a request.
	 */
	public Node process(Node message_node)
	{

		Element message = this.converter.nodeToElement(message_node);

		// get the request - assume there is only one
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		// create the return message
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.appendChild(page_response);

		// extract the params from the cgi-request, and check that we have a coll specified
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(cgi_param_list, false);

		String collection = (String) params.get(GSParams.COLLECTION);
		if (collection == null || collection.equals(""))
		{
			return result;
		}
		String doc_name = (String) params.get(GSParams.DOCUMENT);
		if (doc_name == null || doc_name.equals(""))
		{
			return result;
		}
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		// subaction used to decide if we are returning content or structure
		String document_mode = request.getAttribute(GSXML.SUBACTION_ATT);
		String to = null;
		if (document_mode.equals("text"))
		{
			to = GSPath.appendLink(collection, "DocumentContentRetrieve");
		}
		else if (document_mode.equals("toc"))
		{
			to = GSPath.appendLink(collection, "DocumentStructureRetrieve");
		}
		else
		{ // the default is text
			to = GSPath.appendLink(collection, "DocumentContentRetrieve");
		}

		// make the request to the collection
		Element mr_message = this.doc.createElement(GSXML.MESSAGE_ELEM);

		Element ret_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		mr_message.appendChild(ret_request);

		Element doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		ret_request.appendChild(doc_list);
		Element doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		doc.setAttribute(GSXML.NODE_ID_ATT, doc_name);
		doc_list.appendChild(doc);

		// also add in a request for the Title metadata
		to = GSPath.appendLink(collection, "DocumentMetadataRetrieve");
		Element meta_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, lang, uid);
		// copy the doc list
		meta_request.appendChild(doc_list.cloneNode(true));
		// add in a metadata param
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		meta_request.appendChild(param_list);
		Element param = GSXML.createParameter(this.doc, "metadata", "root_Title");
		param_list.appendChild(param);

		// add the request to the message
		mr_message.appendChild(meta_request);

		Element ret_response = (Element) this.mr.process(mr_message);
		String[] links = { GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.DOC_NODE_ELEM };
		String path = GSPath.createPath(links);
		Element doc_node = (Element) this.doc.importNode(GSXML.getNodeByPath(ret_response, path), true);
		page_response.appendChild(doc_node);

		// get the metadata list
		Element meta_response = (Element) ret_response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(1);
		String[] mlinks = { GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER, GSXML.DOC_NODE_ELEM, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER };
		path = GSPath.createPath(mlinks);

		Element meta_list = (Element) GSXML.getNodeByPath(meta_response, path);
		if (meta_list != null)
		{
			doc_node.appendChild(this.doc.importNode(meta_list, true));
		}
		return result;
	}

}
