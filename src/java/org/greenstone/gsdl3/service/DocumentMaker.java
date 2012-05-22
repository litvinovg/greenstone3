/*
 *    DocumentMaker.java
 *    The Document Maker service that can be used to create/modify custom
 *    documents in Greenstone collection
 *
 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.*;

import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DocumentMaker extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ArchiveIO.class.getName());

	GSDocumentModel _GSDM = null;

	/****************************************************
	 * The list of services the Document Maker supports *
	 ***************************************************/
	//Document/section services
	protected static final String DOCUMENT_CREATE = "DocumentCreate";
	protected static final String DOCUMENT_DELETE = "DocumentDelete";
	protected static final String DOCUMENT_DUPLICATE = "DocumentDuplicate";
	protected static final String DOCUMENT_MOVE = "DocumentMove";
	protected static final String DOCUMENT_MERGE = "DocumentMerge";
	protected static final String DOCUMENT_SPLIT = "DocumentSplit";
	protected static final String DOCUMENT_GET_INFORMATION = "DocumentGetInformation";

	//Other services
	protected static final String DOCUMENT_EXECUTE_TRANSACTION = "DocumentExecuteTransaction";
	/***************************************************/

	String[] services = { DOCUMENT_CREATE, DOCUMENT_DELETE, DOCUMENT_DUPLICATE, DOCUMENT_GET_INFORMATION, DOCUMENT_MOVE, DOCUMENT_MERGE, DOCUMENT_SPLIT, DOCUMENT_EXECUTE_TRANSACTION };

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring DocumentMaker...");
		this.config_info = info;

		for (int i = 0; i < services.length; i++)
		{
			Element service = this.doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service.setAttribute(GSXML.NAME_ATT, services[i]);
			this.short_service_info.appendChild(service);
		}

		_GSDM = new GSDocumentModel(this.site_home, this.doc, this.router);

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		for (int i = 0; i < services.length; i++)
		{
			if (service_id.equals(services[i]))
			{
				Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
				service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
				service_elem.setAttribute(GSXML.NAME_ATT, services[i]);
				return service_elem;
			}
		}

		return null;
	}

	/************
	 * Services *
	 ***********/

	protected Element processDocumentCreate(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_CREATE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_CREATE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to create
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			//Get information about the current new document
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentCreate(oid, collection, userContext);
			if (_GSDM.checkError(result, DOCUMENT_CREATE))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentDelete(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_DELETE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_DELETE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to delete
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentDelete(oid, collection, userContext);
			if (_GSDM.checkError(result, DOCUMENT_DELETE))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentDuplicate(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_DUPLICATE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_DUPLICATE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String newOID = currentDoc.getAttribute("new" + GSXML.NODE_ID_ATT);
			String newCollection = currentDoc.getAttribute("new" + GSXML.COLLECTION_ATT);
			String operation = currentDoc.getAttribute("operation");

			_GSDM.documentMoveOrDuplicate(oid, collection, newOID, newCollection, _GSDM.operationStringToInt(operation), false, userContext);
			if (_GSDM.checkError(result, DOCUMENT_DUPLICATE))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentGetInformation(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_GET_INFORMATION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_GET_INFORMATION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			NodeList requestedInfoList = currentDoc.getElementsByTagName("info"); //TODO: Replace info with a constant
			String[] requestedInfo = new String[requestedInfoList.getLength()];

			for (int j = 0; j < requestedInfoList.getLength(); j++)
			{
				requestedInfo[j] = ((Element) requestedInfoList.item(j)).getAttribute(GSXML.NAME_ATT);
			}

			_GSDM.documentGetInformation(oid, collection, requestedInfo, userContext);
			if (_GSDM.checkError(result, DOCUMENT_GET_INFORMATION))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentMove(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_MOVE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_MOVE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String newOID = currentDoc.getAttribute("new" + GSXML.NODE_ID_ATT);
			String newCollection = currentDoc.getAttribute("new" + GSXML.COLLECTION_ATT);
			String operation = currentDoc.getAttribute("operation");

			_GSDM.documentMoveOrDuplicate(oid, collection, newOID, newCollection, _GSDM.operationStringToInt(operation), true, userContext);
			if (_GSDM.checkError(result, DOCUMENT_MOVE))
			{
				return result;
			}
		}
		return result;
	}

	protected Element processDocumentMerge(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_MERGE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_MERGE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String mergeOID = currentDoc.getAttribute("merge" + GSXML.NODE_ID_ATT);

			_GSDM.documentMerge(oid, collection, mergeOID, userContext);
			if (_GSDM.checkError(result, DOCUMENT_MERGE))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentSplit(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_SPLIT);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_SPLIT + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String splitPoint = currentDoc.getAttribute("splitpoint");

			int split;
			try
			{
				split = Integer.parseInt(splitPoint);
			}
			catch (Exception ex)
			{
				GSXML.addError(this.doc, result, DOCUMENT_SPLIT + ": The split point was not an integer", GSXML.ERROR_TYPE_SYNTAX);
				return result;
			}

			_GSDM.documentSplit(oid, collection, split, userContext);
			if (_GSDM.checkError(result, DOCUMENT_SPLIT))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentExecuteTransaction(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOCUMENT_EXECUTE_TRANSACTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_EXECUTE_TRANSACTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		UserContext userContext = new UserContext(request);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, DOCUMENT_EXECUTE_TRANSACTION + ": Request has no parameter list", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
		String transactionString = (String) params.get("transactions");
		transactionString = transactionString.replace("%26", "&");

		List<Map<String, String>> transactions = null;
		try
		{
			Gson gson = new Gson();
			Type type = new TypeToken<List<Map<String, String>>>()
			{
			}.getType();
			transactions = gson.fromJson(transactionString, type);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		ArrayList<String> collectionsToBuild = new ArrayList<String>();
		if (transactions != null && transactions.size() > 0)
		{
			for (int j = 0; j < transactions.size(); j++)
			{
				Map keyValueMap = transactions.get(j);
				String operation = (String) keyValueMap.get("operation");
				if (operation.equals("move") || operation.equals("duplicate"))
				{
					String origCollection = (String) keyValueMap.get("collection");
					String origOID = (String) keyValueMap.get("oid");
					String newCollection = (String) keyValueMap.get("newCollection");
					String newOID = (String) keyValueMap.get("newOID");
					String subOperation = (String) keyValueMap.get("subOperation");

					_GSDM.documentMoveOrDuplicate(origOID, origCollection, newOID, newCollection, _GSDM.operationStringToInt(subOperation), operation.equals("move"), userContext);
				}
				else if (operation.equals("createDocument"))
				{
					String oid = (String) keyValueMap.get("oid");
					String collection = (String) keyValueMap.get("collection");

					_GSDM.documentCreate(oid, collection, userContext);
				}
				else if (operation.equals("create"))
				{
					String oid = (String) keyValueMap.get("oid");
					String collection = (String) keyValueMap.get("collection");
					String subOperation = (String) keyValueMap.get("subOperation");

					//_GSDM.documentCreate(oid, collection, userContext); <--- Maybe go back to this
					_GSDM.documentXMLSetSection(oid, collection, this.doc.createElement(GSXML.DOCXML_SECTION_ELEM), _GSDM.operationStringToInt(subOperation), userContext);
				}
				else if (operation.equals("delete"))
				{
					String oid = (String) keyValueMap.get("oid");
					String collection = (String) keyValueMap.get("collection");

					_GSDM.documentDelete(oid, collection, userContext);
				}
				else if (operation.equals("setText"))
				{
					String oid = (String) keyValueMap.get("oid");
					String collection = (String) keyValueMap.get("collection");
					String newContent = (String) keyValueMap.get("text");

					_GSDM.documentXMLSetText(oid, collection, newContent, userContext);
				}

				if (_GSDM.checkError(result, DOCUMENT_EXECUTE_TRANSACTION))
				{
					return result;
				}
			}
		}
		return result;
	}
}