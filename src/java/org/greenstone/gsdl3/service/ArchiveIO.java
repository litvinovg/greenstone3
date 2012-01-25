/*
 *    ArchiveIO.java
 *    Used to retrieve information about collection archives
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

import org.greenstone.gsdl3.util.DBHelper;
import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.UserContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.log4j.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class ArchiveIO extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ArchiveIO.class.getName());
	GSDocumentModel _GSDM = null;
	
	/********************************************************************
	 * The list of services the Archive retrieval service rack supports *
	 *******************************************************************/
	protected static final String ARCHIVE_GET_DOCUMENT_FILE_PATH = "ArchiveGetDocumentFilePath";
	protected static final String ARCHIVE_GET_ASSOCIATED_IMPORT_FILES = "ArchiveGetAssociatedImportFiles";
	protected static final String ARCHIVE_GET_SOURCE_FILE_OID = "ArchiveGetSourceFileOID";
	protected static final String ARCHIVE_CHECK_DOCUMENT_OR_SECTION_EXISTS = "ArchiveCheckDocumentOrSectionExists";
	protected static final String ARCHIVE_WRITE_ENTRY_TO_DATABASE = "ArchiveWriteEntryToDatabase";
	protected static final String ARCHIVE_REMOVE_ENTRY_FROM_DATABASE = "ArchiveRemoveEntryFromDatabase";
	/*******************************************************************/
	
	String[] _services = { ARCHIVE_GET_DOCUMENT_FILE_PATH, ARCHIVE_GET_ASSOCIATED_IMPORT_FILES, ARCHIVE_GET_SOURCE_FILE_OID, ARCHIVE_CHECK_DOCUMENT_OR_SECTION_EXISTS, ARCHIVE_WRITE_ENTRY_TO_DATABASE, ARCHIVE_REMOVE_ENTRY_FROM_DATABASE };

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring DocXMLUtil...");
		this.config_info = info;

		for (int i = 0; i < _services.length; i++)
		{
			Element service = this.doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service.setAttribute(GSXML.NAME_ATT, _services[i]);
			this.short_service_info.appendChild(service);
		}
		
		_GSDM = new GSDocumentModel(this.site_home, this.doc, this.router);

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		for (int i = 0; i < _services.length; i++)
		{
			if (service_id.equals(_services[i]))
			{
				Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
				service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
				service_elem.setAttribute(GSXML.NAME_ATT, _services[i]);
				return service_elem;
			}
		}

		return null;
	}
	
	/************
	 * Services *
	 ***********/

	protected Element processArchiveGetDocumentFilePath(Element request)
	{
		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_GET_DOCUMENT_FILE_PATH);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_GET_DOCUMENT_FILE_PATH + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		HashMap params = GSXML.extractParams(param_list, false);

		String oid = (String) params.get(GSXML.NODE_ID_ATT);
		String collection = (String) params.get(GSXML.COLLECTION_ATT);

		String filePath = _GSDM.archiveGetDocumentFilePath(oid, collection, userContext);
		
		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(GSXML.createMetadataElement(this.doc, "docfilepath", filePath)); //TODO: Replace "docfilepath" with a constant 
		result.appendChild(metadataList);

		return result;
	}

	protected Element processArchiveGetSourceFileOID(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_GET_SOURCE_FILE_OID);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_GET_SOURCE_FILE_OID + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		HashMap params = GSXML.extractParams(param_list, false);

		String srcFile = (String) params.get("sourcefile"); //TODO: Replace with a constant
		String collection = (String) params.get(GSXML.COLLECTION_ATT);

		String oid = _GSDM.archiveGetSourceFileOID(srcFile, collection, userContext);
		if(_GSDM.checkError(result, ARCHIVE_GET_SOURCE_FILE_OID))
		{
			return result;
		}

		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(GSXML.createMetadataElement(this.doc, GSXML.NODE_ID_ATT, oid));
		result.appendChild(metadataList);

		return result;
	}

	protected Element processArchiveCheckDocumentOrSectionExists(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_CHECK_DOCUMENT_OR_SECTION_EXISTS);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_CHECK_DOCUMENT_OR_SECTION_EXISTS + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		HashMap params = GSXML.extractParams(param_list, false);

		String oid = (String) params.get(GSXML.NODE_ID_ATT);
		String collection = (String) params.get(GSXML.COLLECTION_ATT);
		
		boolean exists = _GSDM.archiveCheckDocumentOrSectionExists(oid, collection, userContext);
		if(_GSDM.checkError(result, ARCHIVE_CHECK_DOCUMENT_OR_SECTION_EXISTS))
		{
			return result;
		}
		
		result.setAttribute("check", exists ? "true" : "false");

		return result;
	}

	protected Element processArchiveWriteEntryToDatabase(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_WRITE_ENTRY_TO_DATABASE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_WRITE_ENTRY_TO_DATABASE + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		HashMap<String, ArrayList<String>> info = new HashMap<String, ArrayList<String>>();
		String collection = null;
		String oid = null;

		NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
		for (int i = 0; i < params.getLength(); i++)
		{
			Element currentParam = (Element) params.item(i);
			String type = currentParam.getAttribute(GSXML.TYPE_ATT);
			String name = currentParam.getAttribute(GSXML.NAME_ATT);
			String value = currentParam.getAttribute(GSXML.VALUE_ATT);
			if (type != null && type.equals("entry"))
			{
				if(info.get(name) != null)
				{
					info.get(name).add(value);
				}
				else
				{
					ArrayList<String> values = new ArrayList<String>();
					values.add(value);
					info.put(name, values);
				}
			}
			else
			{
				if (name.equals(GSXML.COLLECTION_ATT))
				{
					collection = value;
				}
				else if (name.equals(GSXML.NODE_ID_ATT))
				{
					oid = value;
				}
			}
		}
		
		_GSDM.archiveWriteEntryToDatabase(oid, collection, info, userContext);
		_GSDM.checkError(result, ARCHIVE_WRITE_ENTRY_TO_DATABASE);

		return result;
	}
	
	protected Element processArchiveRemoveEntryFromDatabase(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_REMOVE_ENTRY_FROM_DATABASE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_REMOVE_ENTRY_FROM_DATABASE + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String collection = null;
		String oid = null;

		NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
		for (int i = 0; i < params.getLength(); i++)
		{
			Element currentParam = (Element) params.item(i);
			String name = currentParam.getAttribute(GSXML.NAME_ATT);
			String value = currentParam.getAttribute(GSXML.VALUE_ATT);
	
			if (name.equals(GSXML.COLLECTION_ATT))
			{
				collection = value;
			}
			else if (name.equals(GSXML.NODE_ID_ATT))
			{
				oid = value;
			}
		}
		
		_GSDM.archiveRemoveEntryFromDatabase(oid, collection, userContext);
		_GSDM.checkError(result, ARCHIVE_REMOVE_ENTRY_FROM_DATABASE);

		return result;
	}

	protected Element processArchiveGetAssociatedImportFiles(Element request)
	{
		//Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_GET_ASSOCIATED_IMPORT_FILES);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

		UserContext userContext = new UserContext(request);

		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (param_list == null)
		{
			GSXML.addError(this.doc, result, ARCHIVE_GET_ASSOCIATED_IMPORT_FILES + ": Missing " + GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}
		HashMap params = GSXML.extractParams(param_list, false);

		String oid = (String) params.get(GSXML.NODE_ID_ATT);
		String collection = (String) params.get(GSXML.COLLECTION_ATT);

		ArrayList<String> assocFiles = _GSDM.archiveGetAssociatedImportFiles(oid, collection, userContext);
		if(_GSDM.checkError(result, ARCHIVE_GET_ASSOCIATED_IMPORT_FILES))
		{
			return result;
		}
		
		Element metadataList = this.doc.createElement(GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		metadataList.appendChild(GSXML.createMetadataElement(this.doc, "srcfile", assocFiles.get(0)));

		for (int i = 1; i < assocFiles.size(); i++)
		{
			metadataList.appendChild(GSXML.createMetadataElement(this.doc, "assocfile", (String) assocFiles.get(i)));
		}

		result.appendChild(metadataList);

		return result;
	}
}