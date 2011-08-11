/*
 *    DocXMLUtil.java
 *    Used to manipulate archive doc.xml files
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

import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.log4j.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DocXMLUtil extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ArchiveIO.class.getName());
	GSDocumentModel _GSDM = null;

	/******************************************************************
	 * The list of services the doc.xml utility service rack supports *
	 *****************************************************************/
	protected static final String DOC_XML_CREATE_EMPTY_FILE = "DocXMLCreateEmptyFile";
	protected static final String DOC_XML_GET_METADATA = "DocXMLGetMetadata";
	protected static final String DOC_XML_SET_METADATA = "DocXMLSetMetadata";
	protected static final String DOC_XML_CREATE_SECTION = "DocXMLCreateSection";
	protected static final String DOC_XML_DELETE_SECTION = "DocXMLDeleteSection";
	protected static final String DOC_XML_GET_SECTION = "DocXMLGetSection";
	protected static final String DOC_XML_SET_SECTION = "DocXMLSetSection";
	protected static final String DOC_XML_GET_TEXT = "DocXMLGetText";
	protected static final String DOC_XML_SET_TEXT = "DocXMLSetText";
	protected static final String DOC_XML_DELETE_TEXT = "DocXMLDeleteText";
	/*****************************************************************/

	String[] services = { DOC_XML_CREATE_EMPTY_FILE, DOC_XML_GET_METADATA, DOC_XML_SET_METADATA, DOC_XML_GET_SECTION, DOC_XML_SET_SECTION, DOC_XML_DELETE_SECTION, DOC_XML_GET_TEXT, DOC_XML_SET_TEXT };

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring DocXMLUtil...");
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

	protected Element processDocXMLCreateEmptyFile(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_CREATE_EMPTY_FILE);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_CREATE_EMPTY_FILE + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the document items that are requested
		NodeList docList = request.getElementsByTagName(GSXML.DOCUMENT_ELEM);
		for (int i = 0; i < docList.getLength(); i++)
		{
			Element currentDoc = (Element) docList.item(i);
			String oid = currentDoc.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentDoc.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentXMLCreateDocXML(oid, collection, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_CREATE_EMPTY_FILE))
			{
				return result;
			}
		}
		return result;
	}

	protected Element processDocXMLGetMetadata(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_GET_METADATA);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_GET_METADATA + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the metadata items that are requested
		NodeList metadataList = request.getElementsByTagName(GSXML.METADATA_ELEM);
		for (int i = 0; i < metadataList.getLength(); i++)
		{
			Element currentMetadata = (Element) metadataList.item(i);
			String oid = currentMetadata.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentMetadata.getAttribute(GSXML.COLLECTION_ATT);
			String metadataName = currentMetadata.getAttribute(GSXML.NAME_ATT);

			ArrayList<Element> metadataValues = _GSDM.documentXMLGetMetadata(oid, collection, metadataName, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_GET_METADATA))
			{
				return result;
			}
			
			for(Element metadataValue : metadataValues)
			{
				Element metadataElem = this.doc.createElement(GSXML.METADATA_ELEM);
				metadataElem.setAttribute(GSXML.NAME_ATT, metadataName);
				metadataElem.setAttribute(GSXML.VALUE_ATT, metadataValue.getFirstChild().getNodeValue());
				result.appendChild(metadataElem);
			}
		}

		return result;
	}

	protected Element processDocXMLSetMetadata(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_SET_METADATA);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_SET_METADATA + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the metadata items that are requested
		NodeList metadataList = request.getElementsByTagName(GSXML.METADATA_ELEM);
		for (int i = 0; i < metadataList.getLength(); i++)
		{
			Element currentMetadata = (Element) metadataList.item(i);
			String oid = currentMetadata.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentMetadata.getAttribute(GSXML.COLLECTION_ATT);
			String metadataName = currentMetadata.getAttribute(GSXML.NAME_ATT);
			String newMetadataValue = currentMetadata.getAttribute(GSXML.VALUE_ATT);
			
			//Optional values
			String oldMetadataValue = currentMetadata.getAttribute("old" + GSXML.VALUE_ATT);
			String position = currentMetadata.getAttribute("position"); //TODO: Replace "position" with a constant
			String operation = currentMetadata.getAttribute("operation");
			
			int op = GSDocumentModel.OPERATION_APPEND;
			if(operation.toLowerCase().equals("insertbefore"))
			{
				op = GSDocumentModel.OPERATION_INSERT_BEFORE;
			}
			else if (operation.toLowerCase().equals("insertafter"))
			{
				op = GSDocumentModel.OPERATION_INSERT_AFTER;
			}
			else if (operation.toLowerCase().equals("replace"))
			{
				op = GSDocumentModel.OPERATION_REPLACE;
			}
			
			//If we are given a position then set the value at that position
			if(position != null && !position.equals(""))
			{
				int pos = -1;
				try
				{
					pos = Integer.parseInt(position);
				}
				catch(Exception ex)
				{
					GSXML.addError(this.doc, result, DOC_XML_SET_METADATA + ": Error converting the position attribute to an integer", GSXML.ERROR_TYPE_SYNTAX);
					return result;
				}
				_GSDM.documentXMLSetMetadata(oid, collection, metadataName, newMetadataValue, pos, op, lang, uid);
				if(_GSDM.checkError(result, DOC_XML_SET_METADATA))
				{
					return result;
				}
			}
			//If we are given a value to replace with then call the replacement method
			else if (oldMetadataValue != null && !oldMetadataValue.equals(""))
			{
				_GSDM.documentXMLReplaceMetadata(oid, collection, metadataName, oldMetadataValue, newMetadataValue, lang, uid);
				if(_GSDM.checkError(result, DOC_XML_SET_METADATA))
				{
					return result;
				}
			}
			else
			{
				GSXML.addError(this.doc, result, DOC_XML_SET_METADATA + ": A position or previous value was not given", GSXML.ERROR_TYPE_SYNTAX);
				return result;
			}
		}

		return result;
	}
	
	protected Element processDocXMLCreateSection(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_CREATE_SECTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_CREATE_SECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList sectionList = request.getElementsByTagName(GSXML.DOCXML_SECTION_ELEM); //TODO: Replace "Section" with a constant
		for (int i = 0; i < sectionList.getLength(); i++)
		{
			Element currentSection = (Element) sectionList.item(i);
			String oid = currentSection.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentSection.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentXMLCreateSection(oid, collection, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_CREATE_SECTION))
			{
				return result;
			}
		}
		return result;
	}
	
	protected Element processDocXMLDeleteSection(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_DELETE_SECTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_DELETE_SECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList sectionList = request.getElementsByTagName(GSXML.DOCXML_SECTION_ELEM); 
		for (int i = 0; i < sectionList.getLength(); i++)
		{
			Element currentSection = (Element) sectionList.item(i);
			String oid = currentSection.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentSection.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentXMLDeleteSection(oid, collection, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_DELETE_SECTION))
			{
				return result;
			}
		}
		return result;
	}
	
	protected Element processDocXMLGetSection(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_GET_SECTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_GET_SECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList sectionList = request.getElementsByTagName(GSXML.DOCXML_SECTION_ELEM);
		for (int i = 0; i < sectionList.getLength(); i++)
		{
			Element currentSection = (Element) sectionList.item(i);
			String oid = currentSection.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentSection.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentXMLGetSection(oid, collection, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_GET_SECTION))
			{
				return result;
			}
		}
		return result;
	}

	protected Element processDocXMLSetSection(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_SET_SECTION);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_SET_SECTION + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList sectionList = request.getElementsByTagName(GSXML.DOCXML_SECTION_ELEM);
		for (int i = 0; i < sectionList.getLength(); i++)
		{
			Element currentSection = (Element) sectionList.item(i);
			String oid = currentSection.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentSection.getAttribute(GSXML.COLLECTION_ATT);
			String operation = currentSection.getAttribute("operation");
			
			int op = GSDocumentModel.OPERATION_REPLACE;
			if(operation.equals("insertbefore"))
			{
				op = GSDocumentModel.OPERATION_INSERT_BEFORE;
			}
			else if (operation.equals("insertafter"))
			{
				op = GSDocumentModel.OPERATION_INSERT_AFTER;
			}
			else if (operation.equals("append"))
			{
				op = GSDocumentModel.OPERATION_APPEND;
			}
			
			_GSDM.documentXMLSetSection(oid, collection, currentSection, op, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_SET_SECTION))
			{
				return result;
			}
		}

		return result;
	}

	protected Element processDocXMLGetText(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_GET_TEXT);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_GET_TEXT + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList contentList = request.getElementsByTagName(GSXML.DOCXML_CONTENT_ELEM);
		for (int i = 0; i < contentList.getLength(); i++)
		{
			Element currentContent = (Element) contentList.item(i);
			String oid = currentContent.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentContent.getAttribute(GSXML.COLLECTION_ATT);

			String content = _GSDM.documentXMLGetText(oid, collection, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_GET_TEXT))
			{
				return result;
			}
			
			if (content == null)
			{
				result.appendChild(this.doc.createElement(GSXML.DOCXML_CONTENT_ELEM));
			}
			else
			{
				Element contentElem = this.doc.createElement(GSXML.DOCXML_CONTENT_ELEM);
				Node textNode = this.doc.createTextNode(content);
				contentElem.appendChild(textNode);
				result.appendChild(contentElem); 
			}
		}

		return result;
	}

	protected Element processDocXMLSetText(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, DOC_XML_SET_TEXT);

		if (request == null)
		{
			GSXML.addError(this.doc, result, DOC_XML_SET_TEXT + ": Request is null", GSXML.ERROR_TYPE_SYNTAX);
			return result;
		}

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);

		//Go through each of the requests 
		NodeList contentList = request.getElementsByTagName(GSXML.DOCXML_CONTENT_ELEM);
		for (int i = 0; i < contentList.getLength(); i++)
		{
			Element currentContent = (Element) contentList.item(i);
			String oid = currentContent.getAttribute(GSXML.NODE_ID_ATT);
			String collection = currentContent.getAttribute(GSXML.COLLECTION_ATT);

			_GSDM.documentXMLSetText(oid, collection, currentContent, lang, uid);
			if(_GSDM.checkError(result, DOC_XML_SET_TEXT))
			{
				return result;
			}
		}

		return result;
	}
}