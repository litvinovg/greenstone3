package org.greenstone.gsdl3.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** action class for queries */
public class QueryAction extends Action
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.QueryAction.class.getName());

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
		Element response = basicQuery(request);
		result.appendChild(this.doc.importNode(response, true));
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
		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

		// extract the params from the cgi-request, and check that we have a coll specified
		Element cgi_param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_param_list, false);

		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
		String service_name = (String) params.get(GSParams.SERVICE);
		String collection = (String) params.get(GSParams.COLLECTION);

		// collection may be null or empty when we are doing cross coll services
		if (collection == null || collection.equals(""))
		{
			collection = null;
		}

		UserContext userContext = new UserContext(request);
		String to = service_name;
		if (collection != null)
		{
			to = GSPath.prependLink(to, collection);
		}

		// get the format info - there may be global format info in the collection that searching needs
		Element format_elem = getFormatInfo(to, userContext);
		// set the format type
		format_elem.setAttribute(GSXML.TYPE_ATT, "search");
		// for now just add to the response
		page_response.appendChild(this.doc.importNode(format_elem, true));

		if (request_type.indexOf("d") != -1)
		{
			// we have been asked for the service description
			Element mr_info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
			Element mr_info_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
			mr_info_message.appendChild(mr_info_request);

			// process the message
			Element mr_info_response = (Element) this.mr.process(mr_info_message);
			// the response

			Element service_response = (Element) GSXML.getChildByTagName(mr_info_response, GSXML.RESPONSE_ELEM);

			Element service_description = (Element) this.doc.importNode(GSXML.getChildByTagName(service_response, GSXML.SERVICE_ELEM), true);
			page_response.appendChild(service_description);
		}

		if (request_type.indexOf("r") == -1)
		{
			// just a display request, no actual processing to do
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		// check that we have some service params
		HashMap service_params = (HashMap) params.get("s1");
		if (service_params == null)
		{ // no query
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		// create the query request
		Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		mr_query_message.appendChild(mr_query_request);

		Element query_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(this.doc, query_param_list, service_params);
		mr_query_request.appendChild(query_param_list);

		logger.debug(GSXML.xmlNodeToString(mr_query_message));

		// do the query
		Element mr_query_response = (Element) this.mr.process(mr_query_message);

		// check for errors
		if (processErrorElements(mr_query_response, page_response))
		{
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		Element query_response = (Element) GSXML.getChildByTagName(mr_query_response, GSXML.RESPONSE_ELEM);
		Element query_result_metadata_list = (Element) GSXML.getChildByTagName(query_response, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		if (query_result_metadata_list == null)
		{
			logger.error("No query result metadata.\n");
		}
		else
		{ // add it into the page response
			page_response.appendChild(this.doc.importNode(query_result_metadata_list, true));
		}

		Element query_term_info_list = (Element) GSXML.getChildByTagName(query_response, GSXML.TERM_ELEM + GSXML.LIST_MODIFIER);
		if (query_term_info_list == null)
		{
			logger.error("No query term information.\n");
		}
		else
		{ // add it into the page response
			page_response.appendChild(this.doc.importNode(query_term_info_list, true));
		}

		Element facet_list = (Element) GSXML.getChildByTagName(query_response, GSXML.FACET_ELEM + GSXML.LIST_MODIFIER);
		if (facet_list == null)
		{
			logger.error("No query term information.\n");
		}
		else
		{ // add it into the page response
			page_response.appendChild(this.doc.importNode(facet_list, true));
		}

		// check that there are some documents - for now check the list, but later should use a numdocs metadata elem	
		Element document_list = (Element) GSXML.getChildByTagName(query_response, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		// documentList not present if no docs found
		if (document_list == null)
		{
			// add in a dummy doc node list - used by the display. need to think about this
			page_response.appendChild(this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER));
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		// now we check to see if there is metadata already - some search services return predefined metadata. if there is some, don't do a metadata request
		NodeList doc_metadata = document_list.getElementsByTagName(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		if (doc_metadata.getLength() > 0)
		{
			logger.error("have already found metadata!");
			// append the doc list to the result
			page_response.appendChild(this.doc.importNode(document_list, true));
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		// get the metadata elements needed from the format statement if any
		HashSet<String> metadata_names = new HashSet<String>();
		metadata_names.add("Title");
		// we already got the format element earlier
		if (format_elem != null)
		{
		  getRequiredMetadataNames(format_elem, metadata_names);
		}

		// paging of the results is done here - we filter the list to remove unwanted entries before retrieving metadata
		Element filtered_doc_list = filterDocList(params, service_params, document_list);

		// do the metadata request on the filtered list
		Element mr_metadata_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		to = "DocumentMetadataRetrieve";
		if (collection != null)
		{
			to = GSPath.prependLink(to, collection);
		}
		Element mr_metadata_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		mr_metadata_message.appendChild(mr_metadata_request);

		// just get all for now - the receptionist should perhaps pass in some
		// metadata that it wants, and QueryAction should look through the format stuff to see if there is any other?

		Element extraMetaListElem = (Element) GSXML.getChildByTagName(request, GSXML.EXTRA_METADATA + GSXML.LIST_MODIFIER);
		if(extraMetaListElem != null)
		{
			NodeList extraMetaList = extraMetaListElem.getElementsByTagName(GSXML.EXTRA_METADATA);
			for(int i = 0; i < extraMetaList.getLength(); i++)
			{
				metadata_names.add(((Element)extraMetaList.item(i)).getAttribute(GSXML.NAME_ATT));
			}
		}

		Element dm_param_list = createMetadataParamList(metadata_names);

		mr_metadata_request.appendChild(dm_param_list);

		// add in the doc node list too
		mr_metadata_request.appendChild(filtered_doc_list);

		Element mr_metadata_response = (Element) this.mr.process(mr_metadata_message);

		// check for errors
		processErrorElements(mr_metadata_response, page_response);

		Element metadata_response = (Element) GSXML.getChildByTagName(mr_metadata_response, GSXML.RESPONSE_ELEM);

		Element query_result_document_list = (Element) GSXML.getChildByTagName(metadata_response, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);

		if (query_result_document_list != null)
		{
			page_response.appendChild(this.doc.importNode(query_result_document_list, true));
		}

		logger.debug("Query page:\n" + this.converter.getPrettyString(page_response));
		//append site metadata
		addSiteMetadata(page_response, userContext);
		addInterfaceOptions(page_response);
		return page_response;
	}

	/** this filters out some of the doc results for result paging */
	protected Element filterDocList(HashMap<String, Serializable> params, HashMap service_params, Element orig_doc_list)
	{

		// check the hits_per_page param - is it a service param??
		String hits_pp = (String) service_params.get("hitsPerPage");
		if (hits_pp == null)
		{
			// the service is doing the paging, so we want to display all of the returned docs(???)
			//    return (Element)this.doc.importNode(orig_doc_list, true);
			// try hitsPerPage in the globle param
			hits_pp = (String) params.get("hitsPerPage");
		}

		int hits = 20;
		if (hits_pp != null && !hits_pp.equals(""))
		{
			try
			{
				hits = Integer.parseInt(hits_pp);
			}
			catch (Exception e)
			{
				hits = 20;
			}
		}

		if (hits == -1)
		{ // all
			return (Element) this.doc.importNode(orig_doc_list, true);
		}
		NodeList result_docs = orig_doc_list.getElementsByTagName(GSXML.DOC_NODE_ELEM);

		int num_docs = result_docs.getLength();
		if (num_docs <= hits)
		{
			// too few docs to do paging
			return (Element) this.doc.importNode(orig_doc_list, true);
		}

		// now we need our own doc list
		Element result_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);

		String start_p = (String) service_params.get("startPage");
		if (start_p == null)
		{
			start_p = (String) params.get("startPage");
		}

		int start = 1;
		if (start_p != null && !start_p.equals(""))
		{
			try
			{
				start = Integer.parseInt(start_p);
			}
			catch (Exception e)
			{
				start = 1;
			}
		}

		int start_from = (start - 1) * hits;
		int end_at = (start * hits) - 1;

		if (start_from > num_docs)
		{
			// something has gone wrong
			return result_list;
		}

		if (end_at > num_docs)
		{
			end_at = num_docs - 1;
		}

		// now we finally have the docs numbers to use
		for (int i = start_from; i <= end_at; i++)
		{
			result_list.appendChild(this.doc.importNode(result_docs.item(i), true));
		}

		return result_list;
	}

}
