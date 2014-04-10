package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
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
import java.io.Serializable;

import org.apache.log4j.*;

/**
 * action class for queries this is used when querying isn't collection
 * specific, but it occurs across all collections in the site. The service
 * description is assumed to be known by the xslt so we dont ask for it. we just
 * pass all the service params to the TextQuery service of all the collections
 */
public class NoCollQueryAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.NoCollQueryAction.class.getName());

	/**
	 * process - processes a request.
	 */
	public Node process(Node message_node)
	{

		Element message = GSXML.nodeToElement(message_node);
	    Document doc = message.getOwnerDocument();
	    
		// get the request - assume there is only one
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		// create the return message
		Element result = doc.createElement(GSXML.MESSAGE_ELEM);
		// for now we only have one type of query - subaction not used
		Element response = basicQuery(request);
		result.appendChild(doc.importNode(response, true));
		return result;
	}

	/**
	 * a generic query handler this gets the service description, does the query
	 * (just passes all the params to the service, then gets the titles for any
	 * results
	 */
	protected Element basicQuery(Element request)
	{
		// the result
		Document doc = request.getOwnerDocument();
		
		Element page_response = doc.createElement(GSXML.RESPONSE_ELEM);

		// extract the params from the cgi-request, and check that we have a coll specified
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
		UserContext userContext = new UserContext(request);
		if (request_type.equals("d"))
		{
			// display the query page
			// the only info we need to return is the collection list cos the xslt does teh rest

			Element coll_list = getCollectionList(doc, userContext);
			page_response.appendChild(doc.importNode(coll_list, true));
			return page_response;

		}

		// else we have a query
		String service_name = (String) params.get(GSParams.SERVICE);
		if (service_name == null || service_name.equals(""))
		{
			service_name = "TextQuery";
		}
		String query_coll_list = (String) params.get(GSParams.COLLECTION);

		if (query_coll_list == null || query_coll_list.equals(""))
		{
			logger.error("no collections were specified!");
			Element coll_list = getCollectionList(doc,userContext);
			page_response.appendChild(doc.importNode(coll_list, true));
			return page_response;
		}

		// service paramList
		HashMap service_params = (HashMap) params.get("s1");
		if (service_params == null)
		{ // no query
			return page_response;
		}
		Element query_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(query_param_list, service_params);

		// we will do a query for each coll
		String[] colls = query_coll_list.split(",");

		Element mr_query_message = doc.createElement(GSXML.MESSAGE_ELEM);

		for (int i = 0; i < colls.length; i++)
		{
			String to = GSPath.appendLink(colls[i], service_name);
			Element mr_query_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
			mr_query_message.appendChild(mr_query_request);
			mr_query_request.appendChild(query_param_list.cloneNode(true));

		}

		// do the query
		Element mr_query_response = (Element) this.mr.process(mr_query_message);

		// get the Title metadata for each node - need Node title and root title
		Element mr_meta_message = doc.createElement(GSXML.MESSAGE_ELEM);
		NodeList responses = mr_query_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		for (int j = 0; j < responses.getLength(); j++)
		{
			Element document_list = (Element) GSXML.getChildByTagName((Element) responses.item(j), GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			if (document_list != null)
			{
				String coll_name = extractCollName(((Element) responses.item(j)).getAttribute(GSXML.FROM_ATT));
				String path = GSPath.appendLink(coll_name, "DocumentMetadataRetrieve");
				Element mr_meta_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, path, userContext);
				mr_meta_message.appendChild(mr_meta_request);
				mr_meta_request.appendChild(doc.importNode(document_list, true));
				// metadata params 
				Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				Element param = GSXML.createParameter(doc, "metadata", "Title");
				param_list.appendChild(param);
				param = GSXML.createParameter(doc, "metadata", "root_Title");
				param_list.appendChild(param);
				mr_meta_request.appendChild(param_list);

			}
		}

		// do the request
		Element mr_meta_response = (Element) this.mr.process(mr_meta_message);

		// the result 
		Element result_doc_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		page_response.appendChild(result_doc_list);

		responses = mr_meta_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		for (int j = 0; j < responses.getLength(); j++)
		{
			Element document_list = (Element) GSXML.getChildByTagName((Element) responses.item(j), GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			String coll_name = extractCollName(((Element) responses.item(j)).getAttribute(GSXML.FROM_ATT));

			mergeDocLists(result_doc_list, document_list, coll_name);
		}

		return page_response;
	}

	protected String extractCollName(String path)
	{
		return GSPath.removeLastLink(path);
	}

	protected void mergeDocLists(Element result_list, Element from_list, String collection)
	{

		Document owner = result_list.getOwnerDocument();
		Node child = from_list.getFirstChild();
		while (child != null && child.getNodeType() == Node.ELEMENT_NODE)
		{
			((Element) child).setAttribute("collection", collection);
			result_list.appendChild(owner.importNode(child, true));
			child = child.getNextSibling();
		}

	}

	protected Element getCollectionList(Document doc, UserContext userContext)
	{

		// first, get the message router info
		Element coll_list_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element coll_list_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		coll_list_message.appendChild(coll_list_request);
		Element coll_list_response = (Element) this.mr.process(coll_list_message);
		if (coll_list_response == null)
		{
			logger.error("couldn't query the message router!");
			return null;
		}

		// second, get the display info for each collection 
		NodeList colls = coll_list_response.getElementsByTagName(GSXML.COLLECTION_ELEM);

		Element coll_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element param = GSXML.createParameter(doc, GSXML.SUBSET_PARAM, GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER);
		coll_param_list.appendChild(param);
		// we will send all the requests in a single message
		Element metadata_message = doc.createElement(GSXML.MESSAGE_ELEM);
		for (int i = 0; i < colls.getLength(); i++)
		{
			Element c = (Element) colls.item(i);
			String name = c.getAttribute(GSXML.NAME_ATT);

			Element metadata_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, name, userContext);
			metadata_request.appendChild(coll_param_list.cloneNode(true));
			metadata_message.appendChild(metadata_request);
		}

		Element metadata_response = (Element) this.mr.process(metadata_message);

		NodeList coll_responses = metadata_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		// check that have same number of responses as collections
		if (colls.getLength() != coll_responses.getLength())
		{
			logger.error("didn't get a response for each collection - somethings gone wrong!");
			// for now, dont use the metadata
		}
		else
		{
			for (int i = 0; i < colls.getLength(); i++)
			{
				Element c1 = (Element) colls.item(i);
				Element c2 = (Element) coll_responses.item(i);
				if (c1.getAttribute(GSXML.NAME_ATT).equals(c2.getAttribute(GSXML.FROM_ATT)))
				{
					//add the collection data into the original response
					GSXML.mergeElements(c1, (Element) GSXML.getChildByTagName(c2, GSXML.COLLECTION_ELEM));
				}
				else
				{
					logger.error("response does not correspond to request!");
				}

			}
		}

		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		Element response = (Element) GSXML.getNodeByPath(coll_list_response, path);
		return response;

	}
}
