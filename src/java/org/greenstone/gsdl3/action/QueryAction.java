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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** action class for queries */
public class QueryAction extends Action
{

  public static final String HITS_PER_PAGE = "hitsPerPage";
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.QueryAction.class.getName());
  
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
		String service_name = (String) params.get(GSParams.SERVICE);
		String collection = (String) params.get(GSParams.COLLECTION);
		String lang = request.getAttribute(GSXML.LANG_ATT);
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
		page_response.appendChild(doc.importNode(format_elem, true));

		//if (request_type.indexOf("d") != -1)
		//{
		// get the service description
			// we have been asked for the service description
			Element mr_info_message = doc.createElement(GSXML.MESSAGE_ELEM);
			Element mr_info_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
			mr_info_message.appendChild(mr_info_request);

			// process the message
			Element mr_info_response = (Element) this.mr.process(mr_info_message);
			// the response

			Element service_response = (Element) GSXML.getChildByTagName(mr_info_response, GSXML.RESPONSE_ELEM);

			Element service_description = (Element) doc.importNode(GSXML.getChildByTagName(service_response, GSXML.SERVICE_ELEM), true);


			// have we been asked to return it as part of the response?
			if (request_type.indexOf("d") != -1) {
			  page_response.appendChild(service_description);
			}
			//}
		boolean does_paging = false;
		Element meta_list =(Element) GSXML.getChildByTagName(service_description, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
		if (meta_list != null) {
		  String value = GSXML.getMetadataValue(meta_list, "does_paging");
		  if (value.equals("true")) {
		    does_paging = true;
		  }
		}

		if (does_paging == false) {
		  // we will do the paging, so lets add in a hitsPerPage param to the service
		  addHitsParamToService(doc, service_description, lang);
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
		Element mr_query_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		mr_query_message.appendChild(mr_query_request);

		Element query_param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		GSXML.addParametersToList(query_param_list, service_params);
		mr_query_request.appendChild(query_param_list);

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
			page_response.appendChild(doc.importNode(query_result_metadata_list, true));
		}

		Element query_term_info_list = (Element) GSXML.getChildByTagName(query_response, GSXML.TERM_ELEM + GSXML.LIST_MODIFIER);
		if (query_term_info_list == null)
		{
			logger.error("No query term information.\n");
		}
		else
		{ // add it into the page response
			page_response.appendChild(doc.importNode(query_term_info_list, true));
		}

		Element facet_list = (Element) GSXML.getChildByTagName(query_response, GSXML.FACET_ELEM + GSXML.LIST_MODIFIER);
		if (facet_list == null)
		{
			logger.error("No query term information.\n");
		}
		else
		{ // add it into the page response
			page_response.appendChild(doc.importNode(facet_list, true));
		}

		// check that there are some documents - for now check the list, but later should use a numdocs metadata elem	
		Element document_list = (Element) GSXML.getChildByTagName(query_response, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		// documentList not present if no docs found
		if (document_list == null)
		{
			// add in a dummy doc node list - used by the display. need to think about this
			page_response.appendChild(doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER));
			//append site metadata
			addSiteMetadata(page_response, userContext);
			addInterfaceOptions(page_response);
			return page_response;
		}

		// now we check to see if there is metadata already - some search services return predefined metadata. if there is some, don't do a metadata request
		NodeList doc_metadata = document_list.getElementsByTagName(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		if (doc_metadata.getLength() > 0)
		{
		  // why are we not paging these results?????
		  // append the doc list to the result
			page_response.appendChild(doc.importNode(document_list, true));
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
		Element filtered_doc_list;
		if (does_paging) {
		  filtered_doc_list = (Element)doc.importNode(document_list, true);
		} else {
		  filtered_doc_list = filterDocList(doc, params, service_params, document_list);
		}
		// do the metadata request on the filtered list
		Element mr_metadata_message = doc.createElement(GSXML.MESSAGE_ELEM);
		to = "DocumentMetadataRetrieve";
		if (collection != null)
		{
			to = GSPath.prependLink(to, collection);
		}
		Element mr_metadata_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
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

		Element dm_param_list = createMetadataParamList(doc,metadata_names);

		mr_metadata_request.appendChild(dm_param_list);

		// add in the doc node list too
		mr_metadata_request.appendChild(filtered_doc_list);

		Element mr_metadata_response = (Element) this.mr.process(mr_metadata_message);

		Element query_result_snippet_list = (Element) GSXML.getChildByTagName(query_response, GSXML.HL_SNIPPET_ELEM + GSXML.LIST_MODIFIER);
		
		// check for errors
		processErrorElements(mr_metadata_response, page_response);

		Element metadata_response = (Element) GSXML.getChildByTagName(mr_metadata_response, GSXML.RESPONSE_ELEM);

		Element query_result_document_list = (Element) GSXML.getChildByTagName(metadata_response, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				
		if (query_result_document_list != null)
		{
			page_response.appendChild(doc.importNode(query_result_document_list, true));
			if (query_result_snippet_list != null) 
			{
				page_response.appendChild(doc.importNode(query_result_snippet_list,true));
			}
		}
		
		//logger.debug("Query page:\n" + this.converter.getPrettyString(page_response));
		//append site metadata
		addSiteMetadata(page_response, userContext);
		addInterfaceOptions(page_response);
		return page_response;
	}

	/** this filters out some of the doc results for result paging */
	protected Element filterDocList(Document doc, HashMap<String, Serializable> params, HashMap service_params, Element orig_doc_list)
	{

	  String hits_pp = (String) service_params.get(HITS_PER_PAGE);
	 
		int hits = 20;
		if (hits_pp != null && !hits_pp.equals(""))
		{
		  if (hits_pp.equals("all")) {
		    hits = -1;
		  } else {
			try
			{
				hits = Integer.parseInt(hits_pp);
			}
			catch (Exception e)
			{
				hits = 20;
			}
		  }
		}
		if (hits == -1)
		{ // all
			return (Element) doc.importNode(orig_doc_list, true);
		}
		NodeList result_docs = orig_doc_list.getElementsByTagName(GSXML.DOC_NODE_ELEM);

		int num_docs = result_docs.getLength();
		if (num_docs <= hits)
		{
			// too few docs to do paging
			return (Element) doc.importNode(orig_doc_list, true);
		}

		// now we need our own doc list
		Element result_list = doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);

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
			result_list.appendChild(doc.importNode(result_docs.item(i), true));
		}

		return result_list;
	}

  protected boolean addHitsParamToService(Document doc, Element service_description, String lang) {
    Element param_list = (Element)GSXML.getChildByTagName(service_description, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
    Element param = GSXML.createParameterDescription(doc, HITS_PER_PAGE, getTextString("param." + HITS_PER_PAGE, lang, "ServiceRack", null), GSXML.PARAM_TYPE_INTEGER, "20", null, null);
    Element query_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, "query");
    if (query_param != null) {
      param_list.insertBefore(param, query_param);
    } else {
      param_list.appendChild(param);
    }
    return true;
  }
}
