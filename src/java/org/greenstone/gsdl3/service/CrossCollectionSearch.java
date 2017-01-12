/*
 *    CrossCollectionSearch.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This ServiceRack gets specified in siteConfig.xml. So it is loaded by the MessaegRouter, and two services get activated: TextQuery, DocumentMetadataRetrieve.
These are located at MR level, not inside a collection. QueryAction will send messages to "TextQuery", rather than eg "mgppdemo/TextQuery".
These two services will requery the MR for search results/document metadata based on collections or documents listed.
 */

public class CrossCollectionSearch extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.CrossCollectionSearch.class.getName());
	protected static final String QUERY_PARAM = "query";
	protected static final String COLLECTION_PARAM = "collection";
	protected static final String GROUP_PARAM = "group";
  protected static final String MAXDOCS_PARAM = "maxDocs"; // matches standard maxDocs, but in this case, means max docs per collection
  protected static final String HITS_PER_PAGE_PARAM = "hitsPerPage";
  protected static final String MAXDOCS_DEFAULT = "20";
	// the services on offer - these proxy the actual collection ones
	protected static final String TEXT_QUERY_SERVICE = "TextQuery";
	protected static final String DOCUMENT_METADATA_RETRIEVE_SERVICE = "DocumentMetadataRetrieve";

	protected String[] coll_ids_list = null;
	protected String[] coll_ids_list_no_all = null;
	// maps lang to coll names list
	protected HashMap<String, String[]> coll_names_map = null;

	//protected String[] coll_names_list = null;

	/** constructor */
	public CrossCollectionSearch()
	{
	}

	public boolean configure(Element info, Element extra_info)
	{
		// any parameters? colls to include??
		logger.info("Configuring CrossCollectionSearch...");
		// query service
		Element ccs_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		ccs_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
		ccs_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);
		this.short_service_info.appendChild(ccs_service);

		// metadata service
		Element dmr_service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		dmr_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		dmr_service.setAttribute(GSXML.NAME_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
		this.short_service_info.appendChild(dmr_service);

		// get any format info
		Element format_info = (Element) GSXML.getChildByTagName(info, GSXML.FORMAT_ELEM);
		if (format_info != null)
		{
			this.format_info_map.put(TEXT_QUERY_SERVICE, this.desc_doc.importNode(format_info, true));
		}
		else
		{
			// add in a default format statement
		  //"xmlns:gsf='" + GSXML.GSF_NAMESPACE + "' xmlns:xsl='" + GSXML.XSL_NAMESPACE + "
		  String format_string = "<format "+GSXML.STD_NAMESPACES_ATTS + "><gsf:template match='documentNode'><td><a><xsl:attribute name='href'>?a=d&amp;c=<xsl:value-of select='@collection'/>&amp;d=<xsl:value-of select='@nodeID'/><xsl:if test=\"@nodeType='leaf'\">&amp;sib=1</xsl:if>&amp;dt=<xsl:value-of select='@docType'/>&amp;p.a=q&amp;p.s=" + TEXT_QUERY_SERVICE + "&amp;p.c=";
			if (this.cluster_name != null)
			{
				format_string += this.cluster_name;
			}
			format_string += "</xsl:attribute><gsf:icon/></a></td><td><gsf:metadata name='Title'/> (<xsl:value-of select='@collection'/>) </td></gsf:template></format>";
			this.format_info_map.put(TEXT_QUERY_SERVICE, this.desc_doc.importNode(this.converter.getDOM(format_string).getDocumentElement(), true));
		}
		return true;
	}

  protected Element getServiceDescription(Document doc, String service, String lang, String subset)
	{
		if (service.equals(TEXT_QUERY_SERVICE))
		{

			Element ccs_service = doc.createElement(GSXML.SERVICE_ELEM);
			ccs_service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_QUERY);
			ccs_service.setAttribute(GSXML.NAME_ATT, TEXT_QUERY_SERVICE);

			// display info
			if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
			{
				ccs_service.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_NAME, getTextString(TEXT_QUERY_SERVICE + ".name", lang)));
				ccs_service.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(TEXT_QUERY_SERVICE + ".submit", lang)));
				ccs_service.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(TEXT_QUERY_SERVICE + ".description", lang)));
			}
			// param info
			if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
			{
				Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
				// collection list
				if (coll_ids_list == null)
				{
					initCollectionList(lang);
				}
				if (!coll_names_map.containsKey(lang))
				{
					addCollectionNames(lang);
				}
				Element param = GSXML.createParameterDescription(doc, COLLECTION_PARAM, getTextString("param." + COLLECTION_PARAM, lang), GSXML.PARAM_TYPE_ENUM_MULTI, "all", coll_ids_list, coll_names_map.get(lang));
				param_list.appendChild(param);
				// max docs param
				param = GSXML.createParameterDescription(doc, MAXDOCS_PARAM, getTextString("param." + MAXDOCS_PARAM, lang), GSXML.PARAM_TYPE_INTEGER, MAXDOCS_DEFAULT, null, null);
				param_list.appendChild(param);
				// query param
				param = GSXML.createParameterDescription(doc, QUERY_PARAM, getTextString("param." + QUERY_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
				param_list.appendChild(param);
				ccs_service.appendChild(param_list);
			}

			logger.debug("service description=" + this.converter.getPrettyString(ccs_service));
			return ccs_service;
		}
		// these ones are probably never called, but put them here just in case
		Element service_elem = doc.createElement(GSXML.SERVICE_ELEM);
		service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		service_elem.setAttribute(GSXML.NAME_ATT, service);
		return service_elem;

	}

	protected Element processTextQuery(Element request)
	{
		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, TEXT_QUERY_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("TextQuery request had no paramList.");
			return result; // Return the empty result
		}
		// get the collection list
		String[] colls_list = coll_ids_list_no_all;
		Element coll_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, COLLECTION_PARAM);
		if (coll_param != null)
		{
			String coll_list = GSXML.getValue(coll_param);
			if (!coll_list.equals("all") && !coll_list.equals(""))
			{
				colls_list = coll_list.split(",");
			}
		}
			
		colls_list = mergeGroups(userContext, param_list, colls_list);
		
		String maxdocs = MAXDOCS_DEFAULT;
		Element maxdocs_param = GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, MAXDOCS_PARAM);
		if (maxdocs_param != null) {
		  maxdocs = GSXML.getValue(maxdocs_param);
		}
		
		Document msg_doc = XMLConverter.newDOM();
		Element query_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);
		// we are sending the same request to each collection - build up the to
		// attribute for the request
		StringBuffer to_att = new StringBuffer();
		for (int i = 0; i < colls_list.length; i++)
		{
			if (i > 0)
			{
				to_att.append(",");
			}
			to_att.append(GSPath.appendLink(colls_list[i], "TextQuery"));

		}
		// send the query to all colls
		Element query_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_PROCESS, to_att.toString(), userContext);
		query_message.appendChild(query_request);
		// should we add params individually?
		Element new_param_list = msg_doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		query_request.appendChild(new_param_list);
		new_param_list.appendChild(msg_doc.importNode(GSXML.getNamedElement(param_list, GSXML.PARAM_ELEM, GSXML.NAME_ATT, QUERY_PARAM), true));

		// for cross coll search, we only want maxdocs from each collection
		// some colls use maxdocs, some use hits per page so lets send both
		new_param_list.appendChild(GSXML.createParameter(msg_doc, MAXDOCS_PARAM, maxdocs));
		new_param_list.appendChild(GSXML.createParameter(msg_doc, HITS_PER_PAGE_PARAM, maxdocs));
		Element query_result = (Element) this.router.process(query_message);
		// create the doc list for the response
		Element doc_node_list = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(doc_node_list);
		Element result_snippet_list = result_doc.createElement(GSXML.HL_SNIPPET_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(result_snippet_list);
		NodeList hl_snippet_list = query_result.getElementsByTagName(GSXML.HL_SNIPPET_ELEM);
		if (hl_snippet_list != null){
			for (int hls = 0; hls < hl_snippet_list.getLength(); hls++){
				result_snippet_list.appendChild(result_doc.importNode(hl_snippet_list.item(hls), true));
			}
		}

		NodeList responses = query_result.getElementsByTagName(GSXML.RESPONSE_ELEM);
		int num_docs = 0;
		for (int k = 0; k < responses.getLength(); k++)
		{
			String coll_name = GSPath.removeLastLink(((Element) responses.item(k)).getAttribute(GSXML.FROM_ATT));
			NodeList nodes = ((Element) responses.item(k)).getElementsByTagName(GSXML.DOC_NODE_ELEM);
			if (nodes == null || nodes.getLength() == 0)
				continue;
			num_docs += nodes.getLength();
			Element last_node = null;
			Element this_node = null;
			for (int n = 0; n < nodes.getLength(); n++)
			{
				this_node = (Element) nodes.item(n);
				this_node.setAttribute("collection", coll_name);
	
				if (k == 0)
				{

					doc_node_list.appendChild(result_doc.importNode(this_node, true));
				}
				else
				{
					if (last_node == null)
					{
						last_node = (Element) GSXML.getChildByTagName(doc_node_list, GSXML.DOC_NODE_ELEM);
					}
					last_node = GSXML.insertIntoOrderedList(doc_node_list, GSXML.DOC_NODE_ELEM, last_node, this_node, "rank", true);
				}

			}
		}
		// just send back num docs returned. Too hard to work out number of matches etc as each index type
		// does it differently
		Element metadata_list = result_doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER); 
		result.appendChild(metadata_list);
		GSXML.addMetadata(metadata_list, "numDocsReturned", "" + num_docs);
		return result;
	}

	//     protected Element processAdvTextQuery(Element request) 
	//     {

	//     }
	
	
	
	protected boolean initCollectionList(String lang)
	{
		UserContext userContext = new UserContext();
		userContext.setLanguage(lang);
		userContext.setUserID("");

		// first, get the message router info
		Document msg_doc = XMLConverter.newDOM();
		Element coll_list_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);
		Element coll_list_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext); // uid
		coll_list_message.appendChild(coll_list_request);
		logger.debug("coll list request = " + this.converter.getPrettyString(coll_list_request));
		Element coll_list_response = (Element) this.router.process(coll_list_message);
		if (coll_list_response == null)
		{
			logger.error("couldn't query the message router!");
			return false;
		}
		logger.debug("coll list response = " + this.converter.getPrettyString(coll_list_response));
		// second, get some info from each collection. we want the coll name 
		// and whether its got a text query service 

		NodeList colls = coll_list_response.getElementsByTagName(GSXML.COLLECTION_ELEM);
		// we can send the same request to multiple collections at once by using a comma separated list
		Element metadata_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);
		StringBuffer colls_sb = new StringBuffer();
		for (int i = 0; i < colls.getLength(); i++)
		{
			Element c = (Element) colls.item(i);
			String name = c.getAttribute(GSXML.NAME_ATT);
			if (i != 0)
			{
				colls_sb.append(",");
			}
			colls_sb.append(name);
			//Element metadata_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, name, userContext);
			//metadata_message.appendChild(metadata_request);
		}

		Element metadata_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_DESCRIBE, colls_sb.toString(), userContext);
		metadata_message.appendChild(metadata_request);
		logger.debug("metadata request = " + this.converter.getPrettyString(metadata_message));
		Element metadata_response = (Element) this.router.process(metadata_message);
		logger.debug("metadata response = " + this.converter.getPrettyString(metadata_response));
		NodeList coll_responses = metadata_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		ArrayList<String> valid_colls = new ArrayList<String>();
		ArrayList<String> valid_coll_names = new ArrayList<String>();
		for (int i = 0; i < coll_responses.getLength(); i++)
		{
			Element response = (Element) coll_responses.item(i);
			Element coll = (Element) GSXML.getChildByTagName(response, GSXML.COLLECTION_ELEM);
			Element service_list = (Element) GSXML.getChildByTagName(coll, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
			if (service_list == null)
				continue;
			Element query_service = GSXML.getNamedElement(service_list, GSXML.SERVICE_ELEM, GSXML.NAME_ATT, TEXT_QUERY_SERVICE); // should be AbstractTextSearch.TEXT_QUERY_SERVICE
			if (query_service == null)
				continue;
			// use the name of the response in case we are talking to a remote collection, not the name of the collection.
			String coll_id = response.getAttribute(GSXML.FROM_ATT);
			String coll_name = getDisplayText(coll, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			valid_colls.add(coll_id);
			valid_coll_names.add(coll_name);
		}

		this.coll_names_map = new HashMap<String, String[]>();

		// ids no all has the list without 'all' option.
		this.coll_ids_list_no_all = new String[1];
		this.coll_ids_list_no_all = valid_colls.toArray(coll_ids_list_no_all);

		valid_colls.add(0, "all");
		valid_coll_names.add(0, getTextString("param." + COLLECTION_PARAM + ".all", lang));

		this.coll_ids_list = new String[1];
		this.coll_ids_list = valid_colls.toArray(coll_ids_list);

		String[] coll_names_list = new String[1];
		coll_names_list = valid_coll_names.toArray(coll_names_list);
		this.coll_names_map.put(lang, coll_names_list);
		return true;
	}

	protected void addCollectionNames(String lang)
	{

		UserContext userContext = new UserContext();
		userContext.setLanguage(lang);
		userContext.setUserID("");

		ArrayList<String> coll_names = new ArrayList<String>();
		coll_names.add(getTextString("param." + COLLECTION_PARAM + ".all", lang));

		// need to request MR for collection descriptions
		Document msg_doc = XMLConverter.newDOM();
		Element metadata_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);

		// get a comma separated list of coll ids to send to MR
		// the first item is the place holder for 'all'
		StringBuffer colls_sb = new StringBuffer();
		for (int i = 1; i < coll_ids_list.length; i++)
		{
			if (i != 1)
			{
				colls_sb.append(",");
			}
			colls_sb.append(coll_ids_list[i]);
		}
		Element metadata_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_DESCRIBE, colls_sb.toString(), userContext);
		// param_list to request just displayTextList
		Element param_list = msg_doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element param = GSXML.createParameter(msg_doc, GSXML.SUBSET_PARAM, GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER);
		param_list.appendChild(param);
		metadata_request.appendChild(param_list);
		metadata_message.appendChild(metadata_request);
		logger.debug("coll names metadata request = " + this.converter.getPrettyString(metadata_message));
		Element metadata_response = (Element) this.router.process(metadata_message);
		logger.debug("coll names metadata response = " + this.converter.getPrettyString(metadata_response));
		NodeList coll_responses = metadata_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		for (int i = 0; i < coll_responses.getLength(); i++)
		{
			Element response = (Element) coll_responses.item(i);
			Element coll = (Element) GSXML.getChildByTagName(response, GSXML.COLLECTION_ELEM);
			String coll_name = getDisplayText(coll, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			coll_names.add(coll_name);
		}

		String[] coll_names_list = new String[1];
		coll_names_list = coll_names.toArray(coll_names_list);
		this.coll_names_map.put(lang, coll_names_list);

	}

	protected Element processDocumentMetadataRetrieve(Element request)
	{
		// Create a new (empty) result message
	  Document result_doc = XMLConverter.newDOM();
		Element result = result_doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_METADATA_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			logger.error("DocumentMetadataRetrieve request had no paramList.");
			return result; // Return the empty result
		}

		NodeList query_doc_list = request.getElementsByTagName(GSXML.DOC_NODE_ELEM);
		if (query_doc_list.getLength() == 0)
		{
			logger.error("DocumentMetadataRetrieve request had no documentNodes.");
			return result; // Return the empty result
		}

		// the resulting doc node list
		Element result_node_list = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(result_node_list);


		// organise the nodes into collection lists
		HashMap<String, Node> coll_map = new HashMap<String, Node>();

		for (int i = 0; i < query_doc_list.getLength(); i++)
		{
			Element doc_node = (Element) query_doc_list.item(i);
			String coll_name = doc_node.getAttribute("collection");
			Element coll_items = (Element) coll_map.get(coll_name);
			if (coll_items == null)
			{
				coll_items = result_doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				coll_map.put(coll_name, coll_items);
			}
			coll_items.appendChild(result_doc.importNode(doc_node, true));
		}

		// create teh individual requests
		Document msg_doc = XMLConverter.newDOM();
		Element meta_request_message = msg_doc.createElement(GSXML.MESSAGE_ELEM);

		Set mapping_set = coll_map.entrySet();
		Iterator iter = mapping_set.iterator();

		while (iter.hasNext())
		{
			Map.Entry e = (Map.Entry) iter.next();
			String cname = (String) e.getKey();
			Element doc_nodes = (Element) e.getValue();
			Element meta_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_PROCESS, GSPath.appendLink(cname, DOCUMENT_METADATA_RETRIEVE_SERVICE), userContext);
			meta_request.appendChild(msg_doc.importNode(doc_nodes, true));
			meta_request.appendChild(msg_doc.importNode(param_list, true));
			meta_request_message.appendChild(meta_request);

		}

		Node meta_result_node = this.router.process(meta_request_message);
		Element meta_result = GSXML.nodeToElement(meta_result_node);

		// now need to put the doc nodes back in the right order
		// go through the original list again. keep an element pointer to
		// the next element in each collections list
		NodeList meta_responses = meta_result.getElementsByTagName(GSXML.RESPONSE_ELEM);
		for (int i = 0; i < meta_responses.getLength(); i++)
		{
			String collname = GSPath.removeLastLink(((Element) meta_responses.item(i)).getAttribute(GSXML.FROM_ATT));
			Element first_elem = (Element) GSXML.getNodeByPath(meta_responses.item(i), "documentNodeList/documentNode");
			coll_map.put(collname, first_elem);
		}

		for (int i = 0; i < query_doc_list.getLength(); i++)
		{
			Element doc_node = (Element) query_doc_list.item(i);
			Element new_node = (Element) result_doc.importNode(doc_node, false);
			result_node_list.appendChild(new_node);
			String coll_name = doc_node.getAttribute("collection");

			Element meta_elem = (Element) coll_map.get(coll_name);
			GSXML.mergeMetadataLists(new_node, meta_elem);
			coll_map.put(coll_name, meta_elem.getNextSibling());
		}
		return result;
	}
	
	private String[] mergeGroups(UserContext userContext, Element paramList, String[] collArray){
		Document doc = XMLConverter.newDOM();
		Element groupParam = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GROUP_PARAM);
		Element collParam = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, COLLECTION_PARAM);
		//Group param not null and coll param null or not 'all'
		if (groupParam != null && (collParam == null || !GSXML.getValue(collParam).equals("all")))
		{	
			//GroupInfo service to get uniq collections
			String uniqCollServiceName = "UniqueCollections";
			Element infoResponse = getMRInfo(userContext);
			Element serviceList = (Element) GSXML.getChildByTagName(infoResponse, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
			if (serviceList == null) { 
				logger.error("Service list is null!");
				return collArray;
			}
			Element groupInfoService = GSXML.getNamedElement(serviceList, GSXML.SERVICE_ELEM, GSXML.NAME_ATT,	uniqCollServiceName);
			if (groupInfoService == null){
				logger.error("UniqueCollections service unavailable; Check your groupConfig.xml");
				return collArray;
			}
			Element groupQueryMessage = doc.createElement(GSXML.MESSAGE_ELEM);
			Element groupQueryRequest = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_PROCESS, uniqCollServiceName, userContext);
			groupQueryMessage.appendChild(groupQueryRequest);
			Element groupParamList = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			groupQueryRequest.appendChild(groupParamList);
			if (collParam != null){
				groupParamList.appendChild(doc.importNode(GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.COLLECTION_ELEM), true));	
			}
			groupParamList.appendChild(doc.importNode(GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.GROUP_ELEM), true));
			Element groupQueryResult = (Element) this.router.process(groupQueryMessage);
			Element groupQueryResponse = (Element) GSXML.getChildByTagName(groupQueryResult, GSXML.RESPONSE_ELEM);
			Element collList = (Element) GSXML.getChildByTagName(groupQueryResponse, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			NodeList collections = GSXML.getChildrenByTagName(collList, GSXML.COLLECTION_ELEM);
			collArray = new String[collections.getLength()];
			for (int i = 0; i < collections.getLength(); i++){
				String collName = ((Element) collections.item(i)).getAttribute(GSXML.NAME_ATT);
				collArray[i] = collName;
			}
			return collArray;
		}
		return collArray;
		
	}
	private Element getMRInfo(UserContext userContext){
		Document doc = XMLConverter.newDOM();
		Element infoMessage = doc.createElement(GSXML.MESSAGE_ELEM);
		Element infoRequest = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		Element paramList = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		infoRequest.appendChild(paramList);
		GSXML.addParameterToList(paramList, GSXML.SUBSET_PARAM, GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		infoMessage.appendChild(infoRequest);
		Element responseMessage = (Element) this.router.process(infoMessage);
		if (responseMessage == null)
		{
			logger.error("couldn't query the message router!");
			return null;
		}
		Element infoResponse = (Element) GSXML.getChildByTagName(responseMessage, GSXML.RESPONSE_ELEM);
		if (infoResponse == null)
		{
			logger.error("response from message router is null!");
			return null;
		}
		
		return infoResponse;
		
	}
	
}
