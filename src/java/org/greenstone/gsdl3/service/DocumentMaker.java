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

import org.apache.log4j.*;

import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSXML;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to create
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			//Get information about the current new document
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentCreate(oid, collection, lang, uid);
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to delete
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentDelete(oid, collection, lang, uid);
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String newOID = currentDoc.getAttribute("new" + GSXML.NODE_ID_ATT);
			String newCollection = currentDoc.getAttribute("new" + GSXML.COLLECTION_ATT);

			_GSDM.documentDuplicate(oid, collection, newOID, newCollection, lang, uid);
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			NodeList requestedInfoList = currentDoc.getElementsByTagName("info"); //TODO: Replace info with a constant
			String[] requestedInfo = new String[requestedInfoList.getLength()];

			for(int j = 0; j < requestedInfoList.getLength(); j++)
			{
				requestedInfo[j] = ((Element) requestedInfoList.item(j)).getAttribute(GSXML.NAME_ATT);
			}
			
			_GSDM.documentGetInformation(oid, collection, requestedInfo, lang, uid);
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String newOID = currentDoc.getAttribute("new" + GSXML.NODE_ID_ATT);
			String newCollection = currentDoc.getAttribute("new" + GSXML.COLLECTION_ATT);

			_GSDM.documentMove(oid, collection, newOID, newCollection, lang, uid);
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Get the list of documents to duplicate
		NodeList documents = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < documents.getLength(); i++)
		{
			Element currentDoc = (Element) documents.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);
			String mergeOID = currentDoc.getAttribute("merge" + GSXML.NODE_ID_ATT);

			_GSDM.documentMerge(oid, collection, mergeOID, lang, uid);
			if(_GSDM.checkError(result, DOCUMENT_MERGE))
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

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

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
			catch(Exception ex)
			{
				GSXML.addError(this.doc, result, DOCUMENT_SPLIT + ": The split point was not an integer", GSXML.ERROR_TYPE_SYNTAX);
				return result;
			}
			
			_GSDM.documentSplit(oid, collection, split, lang, uid);
			if(_GSDM.checkError(result, DOCUMENT_SPLIT))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocumentExecuteTransaction(Element request)
	{
		return null;
	}
}