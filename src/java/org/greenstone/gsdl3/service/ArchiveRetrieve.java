/*
*    ArchiveRetrieve.java
*    a base class for retrieval services

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

import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.UserContext;

import org.w3c.dom.Element; 

import org.apache.log4j.*;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

public class ArchiveRetrieve extends ServiceRack 
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ArchiveRetrieve.class.getName());

	protected static final String DOCUMENT_FILE_PATH_RETRIEVE_SERVICE = "DocumentFilePathRetrieve";
	protected static final String ASSOCIATED_IMPORT_FILES_RETRIEVE_SERVICE = "AssociatedImportFilesRetrieve";
	protected static final String SOURCE_FILE_OID_RETRIEVE = "SourceFileOIDRetrieve";
	
	protected SimpleCollectionDatabase coll_db = null;

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring ArchiveRetrieve...");
		this.config_info = info;
		
		Element documentFilePathRetrieveService = this.doc.createElement(GSXML.SERVICE_ELEM);
		documentFilePathRetrieveService.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		documentFilePathRetrieveService.setAttribute(GSXML.NAME_ATT, DOCUMENT_FILE_PATH_RETRIEVE_SERVICE);
		this.short_service_info.appendChild(documentFilePathRetrieveService);
		
		Element associatedImportFilesRetrieveService = this.doc.createElement(GSXML.SERVICE_ELEM);
		associatedImportFilesRetrieveService.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		associatedImportFilesRetrieveService.setAttribute(GSXML.NAME_ATT, ASSOCIATED_IMPORT_FILES_RETRIEVE_SERVICE);
		this.short_service_info.appendChild(associatedImportFilesRetrieveService);
		
		Element sourceFileDocIDRetrieveService = this.doc.createElement(GSXML.SERVICE_ELEM);
		sourceFileDocIDRetrieveService.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		sourceFileDocIDRetrieveService.setAttribute(GSXML.NAME_ATT, SOURCE_FILE_OID_RETRIEVE);
		this.short_service_info.appendChild(sourceFileDocIDRetrieveService);
		
		return true;
	}
	
	protected Element getServiceDescription(String service_id, String lang, String subset) 
	{
		if (service_id.equals(DOCUMENT_FILE_PATH_RETRIEVE_SERVICE)) 
		{
			Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
			service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service_elem.setAttribute(GSXML.NAME_ATT, DOCUMENT_FILE_PATH_RETRIEVE_SERVICE);
			return service_elem;
		}
		else if (service_id.equals(ASSOCIATED_IMPORT_FILES_RETRIEVE_SERVICE)) 
		{
			Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
			service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service_elem.setAttribute(GSXML.NAME_ATT, ASSOCIATED_IMPORT_FILES_RETRIEVE_SERVICE);
			return service_elem;
		}
		else if (service_id.equals(SOURCE_FILE_OID_RETRIEVE))
		{
			Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
			service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service_elem.setAttribute(GSXML.NAME_ATT, SOURCE_FILE_OID_RETRIEVE);
			return service_elem;
		}
		return null;
	}
	
	protected Element processDocumentFilePathRetrieve(Element request)
	{
		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, DOCUMENT_FILE_PATH_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
		
		UserContext userContext = new UserContext(request);
		
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (param_list == null) {
			GSXML.addError(this.doc, result, "DocumentFilePathRetrieve: missing "+ GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;  
		}
		HashMap params = GSXML.extractParams(param_list, false);
		
		String oid = (String) params.get("oid");
		String collection = (String) params.get("c");
		
		String assocFilePath = getAssocFilePathFromDocID(oid, collection, userContext);
		
		String docFilePath = this.site_home + File.separatorChar + 
			"collect" + File.separatorChar +
			collection + File.separatorChar + 
			"archives" + File.separatorChar + 
			assocFilePath + File.separatorChar + 
			"doc.xml";

		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(createMetadataElement("docfilepath", docFilePath));
		result.appendChild(metadataList);
		
		return result;
	}
	
	protected Element processSourceFileOIDRetrieveService(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, SOURCE_FILE_OID_RETRIEVE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
		
		UserContext userContext = new UserContext(request);
		
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (param_list == null) 
		{
			GSXML.addError(this.doc, result, "DocumentFilePathRetrieve: missing "+ GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;  
		}
		HashMap params = GSXML.extractParams(param_list, false);
		
		String srcFile = (String) params.get("srcfile");
		String collection = (String) params.get("c");
		
		//Find out what kind of database we have
		String databaseType = getDatabaseTypeFromCollection(collection, userContext);
		if (databaseType == null || databaseType.equals("")) 
		{
			databaseType = "gdbm"; // the default
		}
		
		String dbExt = null;
		if (databaseType.equalsIgnoreCase("jdbm")) 
		{
			dbExt = ".jdb";
		} 
		else 
		{
			dbExt = ".gdb"; // assume gdbm
		}
		
		coll_db = new SimpleCollectionDatabase(databaseType);
		if (!coll_db.databaseOK()) 
		{
			logger.error("Couldn't create the collection database of type "+databaseType);
			return null;
		}

		coll_db.openDatabase
		(
			this.site_home + File.separatorChar + 
			"collect" + File.separatorChar +
			collection + File.separatorChar + 
			"archives" + File.separatorChar + 
			"archiveinf-src" + dbExt, 
			SimpleCollectionDatabase.READ
		);
		
		DBInfo info = coll_db.getInfo(srcFile);
		
		if (info == null)
		{
			return result;
		}
		
		String oid = info.getInfo("oid");
		
		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(createMetadataElement("oid", oid));
		result.appendChild(metadataList);
		
		return result;
	}
	
	protected Element processAssociatedImportFilesRetrieve(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ASSOCIATED_IMPORT_FILES_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
		
		UserContext userContext = new UserContext(request);
		
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (param_list == null) 
		{
			GSXML.addError(this.doc, result, "AssociatedImportFilesRetrieve: missing "+ GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;  
		}
		HashMap params = GSXML.extractParams(param_list, false);
		
		String oid = (String) params.get("oid");
		String collection = (String) params.get("c");
		
		String databaseType = getDatabaseTypeFromCollection(collection, userContext);
		if (databaseType == null || databaseType.equals("")) 
		{
			databaseType = "gdbm"; // the default
		}
		
		String dbExt = null;
		if (databaseType.equalsIgnoreCase("jdbm")) 
		{
			dbExt = ".jdb";
		} 
		else 
		{
			dbExt = ".gdb"; // assume gdbm
		}
		
		coll_db = new SimpleCollectionDatabase(databaseType);
		if (!coll_db.databaseOK()) 
		{
			logger.error("Couldn't create the collection database of type "+databaseType);
			return null;
		}

		coll_db.openDatabase
		(
			this.site_home + File.separatorChar + 
			"collect" + File.separatorChar +
			collection + File.separatorChar + 
			"archives" + File.separatorChar + 
			"archiveinf-doc" + dbExt, 
			SimpleCollectionDatabase.READ
		);
		
		DBInfo info = coll_db.getInfo(oid);
		
		if (info == null)
		{
			return result;
		}
		
		String srcFile = info.getInfo("src-file");
		Vector data = info.getMultiInfo("assoc-file");
		
		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(createMetadataElement("srcfile", srcFile));
		
		for (int i = 0; i < data.size(); i++)
		{
			metadataList.appendChild(createMetadataElement("assocfile", (String)data.get(i)));
		}
		
		result.appendChild(metadataList);
		
		return result;
	}
	
	public Element createMetadataElement(String name, String value)
	{
		Element metaElem = this.doc.createElement(GSXML.METADATA_ELEM);
		metaElem.setAttribute("name", name);
		metaElem.setAttribute("value", value);
		return metaElem;
	}
	
	public String getAssocFilePathFromDocID(String oid, String collection, UserContext userContext)
	{
		Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest (this.doc, GSXML.REQUEST_TYPE_PAGE, collection + "/DocumentMetadataRetrieve", userContext);
		mr_query_message.appendChild(mr_query_request);
		
		Element paramList = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		paramList.appendChild(createMetadataElement("metadata", "assocfilepath"));
		
		mr_query_request.appendChild(paramList);

		Element docListElem = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
		Element docElem = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		docElem.setAttribute(GSXML.NODE_ID_ATT, oid);
		docListElem.appendChild(docElem);
		mr_query_request.appendChild(docListElem);

		Element response = (Element) this.router.process(mr_query_message);
		
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
		path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
		path = GSPath.appendLink(path, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
		Element metadataListElem = (Element) GSXML.getNodeByPath(response, path);
		Element metadataElem = (Element) metadataListElem.getFirstChild();
		
		return metadataElem.getFirstChild().getNodeValue();
	}
	
	public String getDatabaseTypeFromCollection(String collection, UserContext userContext)
	{
		//Find out what kind of database we have
		Element dbTypeMessage = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element dbTypeRequest = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, collection, userContext);
		dbTypeMessage.appendChild(dbTypeRequest);
		Element dbTypeResponse = (Element)this.router.process(dbTypeMessage);
		
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.COLLECTION_ELEM);
		Element collectionElem = (Element) GSXML.getNodeByPath(dbTypeResponse, path);
		
		if (collectionElem != null)
		{
			return collectionElem.getAttribute(GSXML.DB_TYPE_ATT);
		}
		return null;
	}
}   