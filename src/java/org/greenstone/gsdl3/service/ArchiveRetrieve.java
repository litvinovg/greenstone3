/*
*    AbstractDocumentRetrieve.java
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

import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;

import org.w3c.dom.Element; 

import org.apache.log4j.*;

import java.io.File;
import java.util.HashMap;

/** Abstract class for Document Retrieval Services
*
* @author <a href="mailto:greenstone@cs.waikato.ac.nz">Katherine Don</a>
*/

public class ArchiveRetrieve extends ServiceRack {

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ArchiveRetrieve.class.getName());

	protected static final String ARCHIVE_FILE_PATH_RETRIEVE_SERVICE = "ArchiveFilePathRetrieve";
	
	protected SimpleCollectionDatabase coll_db = null;
	
	/** constructor */
	public ArchiveRetrieve()
	{
	}

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info)){
			return false;
		}

		logger.info("Configuring ArchiveRetrieve...");
		this.config_info = info;
		
		Element archiveFilePathRetrieveService = this.doc.createElement(GSXML.SERVICE_ELEM);
		archiveFilePathRetrieveService.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		archiveFilePathRetrieveService.setAttribute(GSXML.NAME_ATT, ARCHIVE_FILE_PATH_RETRIEVE_SERVICE);
		this.short_service_info.appendChild(archiveFilePathRetrieveService);
		
		return true;
	}
	
	protected Element getServiceDescription(String service_id, String lang, String subset) 
	{
		Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
		service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
		service_elem.setAttribute(GSXML.NAME_ATT, ARCHIVE_FILE_PATH_RETRIEVE_SERVICE);
		return service_elem;
	}
	
	protected Element processArchiveFilePathRetrieve(Element request)
	{
		// Create a new (empty) result message
		Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
		result.setAttribute(GSXML.FROM_ATT, ARCHIVE_FILE_PATH_RETRIEVE_SERVICE);
		result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
		
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		
		// Get the parameters of the request
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
		if (param_list == null) {
			GSXML.addError(this.doc, result, "DocumentMetadataRetrieve: missing "+ GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER, GSXML.ERROR_TYPE_SYNTAX);
			return result;  
		}
		HashMap params = GSXML.extractParams(param_list, false);
		
		String docID = (String) params.get("docID");
		String collection = (String) params.get("c");
		
		String assocFilePath = getAssocFilePathFromDocID(docID, collection, lang, uid);
		
		String docFilePath = this.site_home + File.separatorChar + 
			"collect" + File.separatorChar +
			collection + File.separatorChar + 
			"archives" + File.separatorChar + 
			assocFilePath + File.separatorChar + 
			"doc.xml";
		
		Element metaElem = this.doc.createElement(GSXML.METADATA_ELEM);
		metaElem.setAttribute("name", "docFilePath");
		metaElem.setAttribute("value", docFilePath);
		
		logger.error("DOCFILEPATH = " + docFilePath);
		
		result.appendChild(metaElem);
		
		return result;
	}
	
	protected Element processArchiveAssociatedImportFilesRetrieve(Element request)
	{
		// find out what kind of database we have
		//Element database_type_elem = (Element) GSXML.getChildByTagName(this.config_info, GSXML.DATABASE_TYPE_ELEM);
		//String database_type = "jdbm";
		/*if (database_type_elem != null) {
			database_type = database_type_elem.getAttribute(GSXML.NAME_ATT);
		}
		if (database_type == null || database_type.equals("")) {
			database_type = "gdbm"; // the default
		}
		
		String db_ext = null;
		if (database_type.equalsIgnoreCase("jdbm")) {
			db_ext = ".jdb";
		} else {
			db_ext = ".gdb"; // assume gdbm
		}
		
		
				coll_db = new SimpleCollectionDatabase(database_type);
		if (!coll_db.databaseOK()) {
			logger.error("Couldn't create the collection database of type "+database_type);
			return null;
		}
		*/
	
			/*coll_db.openDatabase(
			this.site_home + File.separatorChar + 
			"collect" + File.separatorChar +
			collection + File.separatorChar + 
			"archives" + File.separatorChar + 
			"archiveinf-doc" + db_ext, 
			SimpleCollectionDatabase.READ);
		
		logger.error("STUFF " + coll_db.getInfo(docID));
		
		coll_db.getInfo(docID);*/
		return null;
	}
	
	public String getAssocFilePathFromDocID(String docID, String collection, String lang, String uid)
	{
		Element mr_query_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_query_request = GSXML.createBasicRequest (this.doc, GSXML.REQUEST_TYPE_PAGE, collection + "/DocumentMetadataRetrieve", lang, uid);
		mr_query_message.appendChild(mr_query_request);
		
		Element paramList = this.doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);

		Element assocParam = this.doc.createElement(GSXML.PARAM_ELEM);
		assocParam.setAttribute("name", "metadata");
		assocParam.setAttribute("value", "assocfilepath");
		paramList.appendChild(assocParam);
		
		mr_query_request.appendChild(paramList);

		Element docList = this.doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
		Element doc = this.doc.createElement(GSXML.DOC_NODE_ELEM);
		doc.setAttribute(GSXML.NODE_ID_ATT, docID);
		docList.appendChild(doc);
		mr_query_request.appendChild(docList);

		Element response = (Element) this.router.process(mr_query_message);
		
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
		path = GSPath.appendLink(path, GSXML.DOC_NODE_ELEM);
		path = GSPath.appendLink(path, GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
		Element metadataListElem = (Element) GSXML.getNodeByPath(response, path);
		Element metadataElem = (Element) metadataListElem.getFirstChild();
		
		return metadataElem.getFirstChild().getNodeValue();
	}
}   