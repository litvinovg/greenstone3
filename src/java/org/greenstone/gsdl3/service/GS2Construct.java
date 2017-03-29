/*
 *    GS2Construct.java 
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.build.GS2PerlConstructor;
import org.greenstone.gsdl3.build.GS2PerlListener;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSStatus;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OID;
import org.greenstone.gsdl3.util.SimpleCollectionDatabase;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;

// https://developer.android.com/reference/org/json/JSONObject.html
// https://developer.android.com/reference/org/json/JSONArray.html
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * A Services class for building collections provides a wrapper around the old
 * perl scripts
 * 
 * @author Katherine Don
 */
public class GS2Construct extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2Construct.class.getName());

    // default error message
    private static final String NO_PERMISSIONS_ERROR = "This user does not have the required permissions to perform this action.";

	// services offered
	private static final String NEW_SERVICE = "NewCollection";
	private static final String ADD_DOC_SERVICE = "AddDocument";
	private static final String IMPORT_SERVICE = "ImportCollection";
	private static final String BUILD_SERVICE = "BuildCollection";
	private static final String ACTIVATE_SERVICE = "ActivateCollection";
	private static final String BUILD_AND_ACTIVATE_SERVICE = "BuildAndActivateCollection";
	private static final String DELETE_SERVICE = "DeleteCollection";
	private static final String RELOAD_SERVICE = "ReloadCollection";
	private static final String MODIFY_METADATA_SERVICE = "ModifyMetadata"; // set or remove metadata


	// params used
	private static final String COL_PARAM = "collection";
	private static final String NEW_COL_TITLE_PARAM = "collTitle";
	private static final String NEW_COL_ABOUT_PARAM = "collAbout";
	private static final String CREATOR_PARAM = "creator";
	private static final String NEW_FILE_PARAM = "newfile";
	private static final String PROCESS_ID_PARAM = GSParams.PROCESS_ID;
	private static final String BUILDTYPE_PARAM = "buildType";
	private static final String BUILDTYPE_MG = "mg";
	private static final String BUILDTYPE_MGPP = "mgpp";

	protected static String DATABASE_TYPE = null;
	protected SimpleCollectionDatabase coll_db = null;
	
	// the list of the collections - store between some method calls
	private String[] collection_list = null;

	// set of listeners for any construction commands
	protected Map<String, GS2PerlListener> listeners = null;
	protected HashMap<String, Boolean> collectionOperationMap = new HashMap<String, Boolean>();

	public GS2Construct()
	{
		this.listeners = Collections.synchronizedMap(new HashMap<String, GS2PerlListener>());
	}

	/** returns a specific service description */
	protected Element getServiceDescription(Document doc, String service, String lang, String subset)
	{

		Element description = doc.createElement(GSXML.SERVICE_ELEM);
		description.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		description.setAttribute(GSXML.NAME_ATT, service);
		if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
		{
			description.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_NAME, getTextString(service + ".name", lang)));
			description.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(service + ".description", lang)));
			description.appendChild(GSXML.createDisplayTextElement(doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(service + ".submit", lang)));
		}
		if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
		{
			Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			description.appendChild(param_list);

			if (service.equals(NEW_SERVICE))
			{

				Element param = GSXML.createParameterDescription(doc, NEW_COL_TITLE_PARAM, getTextString("param." + NEW_COL_TITLE_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
				param_list.appendChild(param);
				param = GSXML.createParameterDescription(doc, CREATOR_PARAM, getTextString("param." + CREATOR_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
				param_list.appendChild(param);
				param = GSXML.createParameterDescription(doc, NEW_COL_ABOUT_PARAM, getTextString("param." + NEW_COL_ABOUT_PARAM, lang), GSXML.PARAM_TYPE_TEXT, null, null, null);
				param_list.appendChild(param);
				String[] types = { BUILDTYPE_MGPP, BUILDTYPE_MG };
				String[] type_texts = { getTextString("param." + BUILDTYPE_PARAM + "." + BUILDTYPE_MGPP, lang), getTextString("param." + BUILDTYPE_PARAM + "." + BUILDTYPE_MG, lang) };

				param = GSXML.createParameterDescription(doc, BUILDTYPE_PARAM, getTextString("param." + BUILDTYPE_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, BUILDTYPE_MGPP, types, type_texts);
				param_list.appendChild(param);
			}
			else if (service.equals(ACTIVATE_SERVICE) || service.equals(IMPORT_SERVICE) || service.equals(BUILD_SERVICE) || service.equals(RELOAD_SERVICE) || service.equals(DELETE_SERVICE) || service.equals(MODIFY_METADATA_SERVICE))
			{

				this.collection_list = getCollectionList();
				Element param = GSXML.createParameterDescription(doc, COL_PARAM, getTextString("param." + COL_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, null, this.collection_list, this.collection_list);
				param_list.appendChild(param);
			}
			else
			{
				// invalid service name
				return null;
			}
		}
		return description;
	}

	// each service must have a method "process<New service name>"

	protected Element processNewCollection(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processNewCollection", NO_PERMISSIONS_ERROR);
	    }
	    return runCommand(request, GS2PerlConstructor.NEW);
	}

	/** TODO:implement this */
	protected Element processAddDocument(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processAddDocument", NO_PERMISSIONS_ERROR);
	    }

	  Document result_doc = XMLConverter.newDOM();
		// decode the file name, add it to the import directory
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = result_doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		//String lang = request.getAttribute(GSXML.LANG_ATT);
		//String request_type = request.getAttribute(GSXML.TYPE_ATT);
		Text t = result_doc.createTextNode("AddDocument: not implemented yet");
		status.appendChild(t);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
		return response;
	}

	protected Element processBuildAndActivateCollection(Element request)
	{
	    // check permissions
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processBuildAndActivateCollection", NO_PERMISSIONS_ERROR);
	    }

		    
		waitUntilReady(request);
		Element buildResponse = processBuildCollection(request);
		if (buildResponse.getElementsByTagName(GSXML.ERROR_ELEM).getLength() > 0)
		{
			signalReady(request);
			return buildResponse;
		}

		Element statusElem = (Element) buildResponse.getElementsByTagName(GSXML.STATUS_ELEM).item(0);
		String id = statusElem.getAttribute("pid");

		GS2PerlListener currentListener = this.listeners.get(id);
		int statusCode = currentListener.getStatus();
		while (!GSStatus.isCompleted(statusCode))
		{
			// wait for the process, and keep checking the status code
			// there is probably a better way to do this.
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (Exception e)
			{ // ignore
			}
			statusCode = currentListener.getStatus();
		}

		Element activateResponse = processActivateCollection(request);
		signalReady(request);
		return activateResponse;
	}

	protected Element processImportCollection(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processImportCollection", NO_PERMISSIONS_ERROR);
	    }

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		if (params == null)
		{
			return null;
		}

		//If we have been requested to only build certain documents then we need to create a manifest file
		String documentsParam = (String) params.get("documents");
		if (documentsParam != null && !documentsParam.equals(""))
		{
			String s = File.separator;
			String manifestFolderPath = this.site_home + s + "collect" + s + params.get(COL_PARAM) + s + "manifests";
			String manifestFilePath = manifestFolderPath + File.separator + "tempManifest.xml";

			File manifestFolderFile = new File(manifestFolderPath);
			if (!manifestFolderFile.exists())
			{
				manifestFolderFile.mkdirs();
			}

			File manifestFile = new File(manifestFilePath);
			if (!manifestFile.exists())
			{
				try
				{
					manifestFile.createNewFile();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return null; //Probably should return an actual error
				}
			}
			String[] docList = documentsParam.split(",");

			try
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(manifestFile));
				bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				bw.write("<Manifest>\n");
				bw.write("  <Index>\n");
				for (int j = 0; j < docList.length; j++)
				{
					bw.write("    <Filename>" + docList[j] + "</Filename>\n");
				}
				bw.write("  </Index>\n");
				bw.write("</Manifest>\n");
				bw.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null; //Probably should return an actual error
			}
		}

		return runCommand(request, GS2PerlConstructor.IMPORT);
	}

	protected Element processBuildCollection(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processBuildCollection", NO_PERMISSIONS_ERROR);
	    }

		return runCommand(request, GS2PerlConstructor.BUILD);
	}

	protected Element processModifyMetadata(Element request)
	{

	    // There are two types of operations whereby metadata gets modified:
	    // - document including document-meta editing: user needs document editing powers
	    // - adding user comments: user just needs an account and needs to be logged in
	    // We handle both cases in this service.

	    Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
	    HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
	    
	    String metaserver_command = (String) params.get("a"); // e.g. set-archives-metadata or set-metadata-array
	    boolean supportsSettingMultipleMeta = metaserver_command.equals("set-metadata-array") ? true : false;
	    String json_str = (String) params.get("json"); // String or null if no param named "json"

	    String[] docids = null;
	    

	    if (userHasCollectionEditPermissions(request, params)) { // means user can modify ANY metadata

		// if dealing with an array of meta, then parse out the docids from the json
		if(supportsSettingMultipleMeta) {
		    docids = getDocIdsWithOptFilter(json_str, null);
		} // else set-meta operation on single metadata field of single doc, 
		  // and docid will be obtained in runCommand() where it's needed

	    } else {
		// check if user logged in
		// shouldn't be able to do any meta modification if not logged in

		UserContext context = new UserContext(request);
		if (context.getUsername().equals("")) {
		    
		    return errorResponse("processModifyMetadata", "Cannot modify any metadata when not logged in.");
		} else { // User is logged in at least, see whether they can do any restricted set-meta ops
		    // that are open to regular users (those without permissions to edit this collection).
		    // For now, there's only one restricted set-meta operation open to any logged in users
		    // who don't otherwise have editing permissions for the collection: adding user comments.
		    
		    boolean isAddingUserComments = false;
		    Pattern allowedMetaFieldsPattern = Pattern.compile("^(username|usertimestamp|usercomment)$");
		    if(supportsSettingMultipleMeta) {
			
			docids = getDocIdsWithOptFilter(json_str, allowedMetaFieldsPattern);
			if(docids != null) {
			    isAddingUserComments = true;
			}
		    } else {
			String metaname = (String) params.get("metaname");
			if(isAllowedToSetMeta(metaname, allowedMetaFieldsPattern)) {
			    isAddingUserComments = true;
			}
		    }
		
		    if(!isAddingUserComments) { // logged in user is attempting to set meta outside restricted set,
			     // In this case, they're attempting to set meta not related to user comments
			return errorResponse("processModifyMetadata", NO_PERMISSIONS_ERROR);
		    }
		}
	    }
		
		// wait until we can reserve the collection for processing
		waitUntilReady(request);
	
		
		// process
		Element response = runCommand(request, GS2PerlConstructor.MODIFY_METADATA_SERVER, docids);
		
		if (response.getElementsByTagName(GSXML.ERROR_ELEM).getLength() <= 0) // if no errors, wait for process to finish
		{
			Element statusElem = (Element) response.getElementsByTagName(GSXML.STATUS_ELEM).item(0);
			String id = statusElem.getAttribute("pid");
			
			GS2PerlListener currentListener = this.listeners.get(id);
			int statusCode = currentListener.getStatus();
			while (!GSStatus.isCompleted(statusCode))
			{
				// wait for the process, and keep checking the status code
				// there is probably a better way to do this.
				try
				{		
					Thread.currentThread().sleep(100);
				}
				catch (Exception e)
				{ // ignore
				}
				statusCode = currentListener.getStatus();
			}	    
		}		
		
		Element statusElem = (Element) response.getElementsByTagName(GSXML.STATUS_ELEM).item(0);
		String statusString = GSXML.getNodeText(statusElem);
		statusString += " and monitored until done."; 
		GSXML.setNodeText(statusElem, statusString);
		
		// release hold on collection
		signalReady(request);
		return response;
		
	}

	protected Element processActivateCollection(Element request)
	{

	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processActivateCollection", NO_PERMISSIONS_ERROR);
	    }

		// this activates the collection on disk. but now we need to tell
		// the MR about it. but we have to wait until the process is finished.
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);
		String coll_name = (String) params.get(COL_PARAM);
		String lang = request.getAttribute(GSXML.LANG_ATT);

		UserContext userContext = new UserContext(request);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);
		
		// now we de-activate the collection before running activate.pl, and then re-activate at end 
		// So activate.pl only does the moving, no activation. This way will prevent java from launching 
		// perl, exiting and then leaving dangling file handles (on index/text/col.gdb) in perl.
		if (!request_type.equals(GSXML.REQUEST_TYPE_STATUS)) {			
			systemRequest("delete", coll_name, null, userContext); // deactivate collection
		}	

		Element response = runCommand(request, GS2PerlConstructor.ACTIVATE); // if request is for STATUS, then this won't run activate.pl

		Element status = (Element) GSXML.getChildByTagName(response, GSXML.STATUS_ELEM);

		// check for finished
		int status_code = Integer.parseInt(status.getAttribute(GSXML.STATUS_ERROR_CODE_ATT));
		if (GSStatus.isCompleted(status_code) && GSStatus.isError(status_code))
		{
			// we shouldn't carry out the next bit, just return the response
			return response;
		}
		String id = status.getAttribute(GSXML.STATUS_PROCESS_ID_ATT);
		GS2PerlListener listener = this.listeners.get(id);
		if (listener == null)
		{
			logger.error("somethings gone wrong, couldn't find the listener");
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		while (!GSStatus.isCompleted(status_code))
		{
			// wait for the process, and keep checking the status code
			// there is probably a better way to do this.
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (Exception e)
			{ // ignore
			}
			status_code = listener.getStatus();
		}

		// add the rest of the messages to the status node
		Text t = status.getOwnerDocument().createTextNode("\n" + listener.getUpdate());
		status.appendChild(t);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(listener.getStatus()));
		if (GSStatus.isError(status_code))
		{
			return response; // without doing the next bit
		}

		t = status.getOwnerDocument().createTextNode("\n");
		status.appendChild(t);
		// once have got here, we assume
		// the first bit proceeded successfully, now reload the collection (sends a collection reactivation request)
		systemRequest("reload", coll_name, status, userContext); // this will append more messages to the status, and overwrite the error code att
		return response;

	}

	protected Element processDeleteCollection(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processDeleteCollection", NO_PERMISSIONS_ERROR);
	    }

	  Document result_doc = XMLConverter.newDOM();
		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = result_doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		Text t = null; // the text node for the error/success message
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		boolean get_status_only = false;
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			get_status_only = true;
		}
		if (get_status_only)
		{
			// at the moment, delete is synchronous. but it may take ages so should do the command in another thread maybe? in which case we will want to ask for status
			logger.error("had a status request for delete - this shouldn't happen!!");
			//t = result_doc.createTextNode("");
			//status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}
		String coll_name = (String) params.get(COL_PARAM);
		String[] args = { coll_name };
		File coll_dir = new File(GSFile.collectionBaseDir(this.site_home, coll_name));
		// check that the coll is there in the first place
		if (!coll_dir.exists())
		{
			t = result_doc.createTextNode(getTextString("delete.exists_error", lang, args));
			status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		// try to delete the directory
		if (!GSFile.deleteFile(coll_dir))
		{
			t = result_doc.createTextNode(getTextString("delete.delete_error", lang, args));
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			status.appendChild(t);
			return response;
		}

		UserContext userContext = new UserContext(request);

		systemRequest("delete", coll_name, status, userContext);
		return response;
	}

	protected Element processReloadCollection(Element request)
	{
	    if (!userHasCollectionEditPermissions(request)) {
		return errorResponse("processReloadCollection", NO_PERMISSIONS_ERROR);
	    }

	  Document result_doc = XMLConverter.newDOM();
		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = result_doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		Text t = null; // the text node for the error/success message

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		boolean get_status_only = false;
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			get_status_only = true;
		}
		if (get_status_only)
		{
			// reload is synchronous - this makes no sense
			logger.error("had a status request for reload - this shouldn't happen!!");
			//t = result_doc.createTextNode("");
			//status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		String coll_name = (String) params.get(COL_PARAM);

		UserContext userContext = new UserContext(request);

		systemRequest("reload", coll_name, status, userContext);
		return response;

	}

	/**
	 * send a configure request to the message router action name should be
	 * "delete" or "reload" response will be put into the status element
	 */
	protected void systemRequest(String operation, String coll_name, Element status, UserContext userContext)
	{
		// send the request to the MR
	  Document doc = XMLConverter.newDOM();
		Element message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_SYSTEM, "", userContext);
		message.appendChild(request);
		Element command = doc.createElement(GSXML.SYSTEM_ELEM);
		request.appendChild(command);
		command.setAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT, GSXML.COLLECTION_ELEM);
		command.setAttribute(GSXML.SYSTEM_MODULE_NAME_ATT, coll_name);

		if (operation.equals("delete"))
		{
			command.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_DEACTIVATE);
		}
		else if (operation.equals("reload"))
		{
			command.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_ACTIVATE);
		}
		else
		{
			logger.error("invalid action name passed to systemRequest:" + operation);
			return;
		}
		request.appendChild(command);
		Node response = this.router.process(message); // at the moment, get no info in response so ignore it
		Text t;
		String[] args = { coll_name };

		if (status != null)
		{
			if (response == null)
			{
			  t = status.getOwnerDocument().createTextNode(getTextString(operation + ".configure_error", userContext.getLanguage(), args));
				status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
				status.appendChild(t);
				return;
			}

			// if we got here, we have succeeded!
			t = status.getOwnerDocument().createTextNode(getTextString(operation + ".success", userContext.getLanguage(), args));
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.SUCCESS));
			status.appendChild(t);
		}
	}

	/**
	 * configure the service module for now, all services have type=build - need
	 * to think about this
	 */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("configuring GS2Construct");

		Element e = null;
		// hard code in the services for now

		// set up short_service_info_ - for now just has name and type

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, NEW_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, IMPORT_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, BUILD_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, ACTIVATE_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, BUILD_AND_ACTIVATE_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, DELETE_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, RELOAD_SERVICE);
		this.short_service_info.appendChild(e);

		//e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		//e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		//e.setAttribute(GSXML.NAME_ATT, ADD_DOC_SERVICE);
		//this.short_service_info.appendChild(e);

		e = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, MODIFY_METADATA_SERVICE);
		this.short_service_info.appendChild(e);

		return true;
	}

	protected Element runCommand(Element request, int type) {
		return runCommand(request, type, null);
	}

	/** returns a response element */
	protected Element runCommand(Element request, int type, String[] docids)
	{
		Document result_doc = XMLConverter.newDOM();
		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = result_doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		boolean get_status_only = false;
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			get_status_only = true;
		}

		// just check for status messages if that's all that's required
		if (get_status_only)
		{
			String id = (String) params.get(PROCESS_ID_PARAM);
			status.setAttribute(GSXML.STATUS_PROCESS_ID_ATT, id);
			GS2PerlListener listener = this.listeners.get(id);
			if (listener == null)
			{
				Text t = result_doc.createTextNode(getTextString("general.process_id_error", lang));
				status.appendChild(t);
				status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			}
			else
			{
				Text t = result_doc.createTextNode(listener.getUpdate());
				status.appendChild(t);
				status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(listener.getStatus()));
				// check that we actually should be removing the listener here
				if (listener.isFinished())
				{ // remove this listener - its job is done
					this.listeners.remove(id); // not working
				}
			}
			return response;

		}

		// do the actual command
		String coll_name = null;
		if (type == GS2PerlConstructor.NEW)
		{
			String coll_title = (String) params.get(NEW_COL_TITLE_PARAM);
			coll_name = createNewCollName(coll_title);
		}
		else
		{
			coll_name = (String) params.get(COL_PARAM);
		}

		// makes a paramList of the relevant params
		Element other_params = extractOtherParams(params, type);

		//create the constructor to do the work
		GS2PerlConstructor constructor = new GS2PerlConstructor("perl_build");
		if (!constructor.configure())
		{
			Text t = result_doc.createTextNode(getTextString("general.configure_constructor_error", lang));
			status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		constructor.setSiteHome(this.site_home);
		constructor.setCollectionName(coll_name);
		constructor.setActionType(type);
		constructor.setProcessParams(other_params);
		if (type == GS2PerlConstructor.IMPORT)
		{
			constructor.setManifestFile(this.site_home + File.separator + "collect" + File.separator + params.get(COL_PARAM) + File.separator + "manifests" + File.separator + "tempManifest.xml");
		}
		else if (type == GS2PerlConstructor.MODIFY_METADATA_SERVER) {
		    StringBuffer querystring = new StringBuffer();
		    
		    // convert params into a single string again?
		    Set<Map.Entry<String, Serializable>> entries = params.entrySet();
		    Iterator<Map.Entry<String, Serializable>> i = entries.iterator();
			
		    String oid = null;

		    while (i.hasNext()) {

				Map.Entry<String, Serializable> entry = i.next();
				String paramname = entry.getKey();
				paramname = paramname.replace("s1.", ""); // replaces all occurrences
				if (paramname.equals("collection")) {
					paramname = "c";
				}
				if (paramname.equals("d")){
					oid = (String) entry.getValue();
				}
				
				String paramvalue = (String) entry.getValue();

				querystring.append(paramname + "=" + paramvalue);
				if (i.hasNext()) {
					querystring.append("&");
				}
		    }
		    
		    if(oid != null) { // if we have only one oid
			markDocumentInFlatDatabase("R", coll_name, OID.getTop(oid));
		    } else if (docids != null) { // check if we are dealing with many doc ids, as cold in theory happen when set-metadata-array is called
			
			for(int index = 0; index < docids.length; index++) {
			    String docid = docids[index];
			    markDocumentInFlatDatabase("R", coll_name, OID.getTop(docid));
			}
		    } else {
			String msg = getTextString("general.no_valid_docid_error", lang);
			logger.error("*** " + msg);
			Text t = result_doc.createTextNode(msg);
			status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		    }
		    
		    constructor.setQueryString(querystring.toString());
		}

		GS2PerlListener listener = new GS2PerlListener();
		constructor.addListener(listener);
		constructor.start();

		String id = newID();
		this.listeners.put(id, listener);

		status.setAttribute(GSXML.STATUS_PROCESS_ID_ATT, id);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ACCEPTED));
		Text t = result_doc.createTextNode(getTextString("general.process_start", lang));
		status.appendChild(t);
		return response;
	}

	//************************
	// some helper functions
	//************************

	/** parse the collect directory and return a list of collection names */
	protected String[] getCollectionList()
	{

		File collectDir = new File(GSFile.collectDir(this.site_home));
		if (!collectDir.exists())
		{
			logger.error("couldn't find collect dir: " + collectDir.toString());
			return null;
		}
		logger.info("GS2Construct: reading thru directory " + collectDir.getPath() + " to find collections.");
		File[] contents = collectDir.listFiles();
		int num_colls = 0;
		for (int i = 0; i < contents.length; i++)
		{
			if (contents[i].isDirectory() && !contents[i].getName().startsWith("CVS"))
			{
				num_colls++;
			}
		}

		String[] names = new String[num_colls];

		for (int i = 0, j = 0; i < contents.length; i++)
		{
			if (contents[i].isDirectory())
			{
				String colName = contents[i].getName();
				if (!colName.startsWith("CVS"))
				{
					names[j] = colName;
					j++;
				}

			}
		}

		return names;

	}

	/** ids used for process id */
	private int current_id = 0;

	private String newID()
	{
		current_id++;
		return Integer.toString(current_id);
	}

	/** creates a new short name from the collection title */
	protected String createNewCollName(String coll_title)
	{

		String base_name = null;
		// take the first 6 letters
		if (coll_title.length() < 6)
		{
			base_name = coll_title;
		}
		else
		{
			base_name = coll_title.substring(0, 6);
		}
		File coll_dir = new File(GSFile.collectionBaseDir(this.site_home, base_name));
		if (!coll_dir.exists())
		{ // this name is ok - not used yet
			return base_name;
		}

		// now we have to make a new name until we get a good one
		// try name1, name2 name3 etc
		int i = 0;
		while (coll_dir.exists())
		{
			i++;
			coll_dir = new File(GSFile.collectionBaseDir(this.site_home, base_name + Integer.toString(i)));
		}
		return base_name + Integer.toString(i);

	}

	/**
	 * takes the params from the request (in the HashMap) and extracts any that
	 * need to be passed to the constructor and puts them into a paramList
	 * element
	 */
	protected Element extractOtherParams(HashMap<String, Serializable> params, int type)
	{
	  Document doc = XMLConverter.newDOM();
		Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (type == GS2PerlConstructor.NEW)
		{
			Element param = doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "creator");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(CREATOR_PARAM));

			param_list.appendChild(param);
			param = doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "about");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(NEW_COL_ABOUT_PARAM));
			param_list.appendChild(param);
			param = doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "title");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(NEW_COL_TITLE_PARAM));
			param_list.appendChild(param);
			param = doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "buildtype");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(BUILDTYPE_PARAM));
			param_list.appendChild(param);
			return param_list;
		}

		// other ones dont have params yet
		return null;
	}

	protected void waitUntilReady(Element request)
	{
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String collection = (String) params.get(COL_PARAM);

		if (checkCollectionIsNotBusy(collection))
		{
			return;
		}

		while (!checkCollectionIsNotBusy(collection)) // When the collection ceases to be busy, we place a hold on it
		{
			try
			{
				Thread.currentThread().sleep(1000);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	protected void signalReady(Element request)
	{
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

		String collection = (String) params.get(COL_PARAM);

		collectionOperationMap.remove(collection);
	}

	// If collection is NOT busy, then reserve it
	protected synchronized boolean checkCollectionIsNotBusy(String collection)
	{
		if (collectionOperationMap.get(collection) == null)
		{
			collectionOperationMap.put(collection, true);
			return true;
		}
		return false;
	}

    protected boolean isAllowedToSetMeta(String metaname, Pattern allowedMetaFieldsPattern) 
    {
	if(allowedMetaFieldsPattern == null) { // null when user has edit permissions, so they can set any meta
	    logger.info("### User has permissions to set any meta.");
	    return true;
	}
	if(metaname == null) {
	    logger.info("### Can't check null metaname against pattern");
	    return false;
	}
	
	Matcher m = allowedMetaFieldsPattern.matcher(metaname);
	if(!m.matches()) {
	    logger.info("### metaname: " + metaname + " doesn't match allowed allowed fields: " + allowedMetaFieldsPattern.toString());
	    return false;
	} else {
	    return true;
	}
    }
    
    protected String[] getDocIdsWithOptFilter(String json_str, Pattern filterFields) // boolean strictOrPermissible
    {
	if(json_str == null) {
	    logger.error("### Shouldn't be happening: null json string");
	    return null;
	}

	String[] docids = null;

	// check that the name of each metadata being set matches the pattern filterFields.
	// The presence of any other meta means something other than adding user comments is being attempted,
	// which is invalid
	try {
	    
	    JSONArray docInfoList = new JSONArray(json_str);
	    docids = new String[docInfoList.length()];
	    for(int index = 0; index < docInfoList.length(); index++) {
		JSONObject docInfo = docInfoList.getJSONObject(index);
		if(docInfo.has("metatable")) { // should exist for metadata arrays
		    
		    docids[index] = docInfo.getString("docid"); // should exist if metatable existed

		    logger.info("@@@ Found docid: " + docids[index]);

		    JSONArray metatable = docInfo.getJSONArray("metatable");
		    for(int i = 0; i < metatable.length(); i++) {
			JSONObject meta = metatable.getJSONObject(i);

			String metaname = meta.getString("metaname");
			logger.info("### metaname: " + metaname);
			
			if(!isAllowedToSetMeta(metaname, filterFields)) {
			    return null;
			}
		    }
		}
	    }
	} catch(JSONException jsonex) {
	    logger.error("Exception when parsing json string: " + json_str);
	    logger.error(jsonex);
	    
	}
	
	// if we're here, then it means that the JSON only asked for username|usercomment|usertimestamp meta
	// meaning that the setmeta operation was a valid user comment operation.
	// In that case, we have a docid for which we need to add a user comment 
	// set-metadata-array can take more docids, but doesn't happen for a user comment. And one comment
	// is added at a time, but 3 meta fields are set for each comment: username, usercomment and timestamp
	// hence the use of set-meta-array.
	return docids;

    }

    protected Element errorResponse(String serviceName, String errorMsg) {
	Document result_doc = XMLConverter.newDOM();
	Element result = GSXML.createBasicResponse(result_doc, serviceName);
	GSXML.addError(result, errorMsg);
	return result;
    }

    /** Copy from DebugService.userHasEditPermissions
     This function checks that the user is logged in and that the user 
     is in the right group to edit the collection */
    protected boolean userHasCollectionEditPermissions(Element request) {
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
	HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

	return userHasCollectionEditPermissions(request, params);

    }

    protected boolean userHasCollectionEditPermissions(Element request, HashMap<String, Serializable> params) {
	String collection = (String) params.get(COL_PARAM); // could be null on newcoll operation


    UserContext context = new UserContext(request);
    if(collection == null) {
	return !context.getUsername().equals("");
    }

    // START DEBUG
    Set<Map.Entry<String, Serializable>> entries = params.entrySet();
    Iterator<Map.Entry<String, Serializable>> i = entries.iterator();
    
    StringBuffer parametersLine = new StringBuffer();
    while (i.hasNext()) {
	
	Map.Entry<String, Serializable> entry = i.next();
	String paramname = entry.getKey();
	String paramvalue = (String) entry.getValue();
	
	parametersLine.append("\t" + paramname + ": " + paramvalue + "\n");
    }
    
    logger.info("XXXXXXXXXXXXX PARAMETERS:\n" + parametersLine);
    // END DEBUG

    for (String group : context.getGroups()) {
      // administrator always has permission
      if (group.equals("administrator")) {
	return true;
      }
      // all-collections-editor can edit any collection
      if (!collection.equals("")) {
	if (group.equals("all-collections-editor")) {
	  return true;
	}
	if (group.equals(collection+"-collection-editor")) {
	  return true;
	}
      }
    }
    // haven't found a group with edit permissions
    return false;
    
  }
    protected void markDocumentInFlatDatabase(String mark, String collection, String oid) {
		
		Document msg_doc = XMLConverter.newDOM();
		Element message = msg_doc.createElement(GSXML.MESSAGE_ELEM);
		UserContext userContext = new UserContext();
		Element query_request = GSXML.createBasicRequest(msg_doc, GSXML.REQUEST_TYPE_DESCRIBE , collection, userContext);		
		message.appendChild(query_request);
		Element result = (Element) this.router.process(message);
		Element resp_elem = (Element) GSXML.getChildByTagName(result, GSXML.RESPONSE_ELEM);
		Element coll_elem = (Element) GSXML.getChildByTagName(resp_elem, GSXML.COLLECTION_ELEM);
		String dbtype = coll_elem.getAttribute(GSXML.DB_TYPE_ATT);
		
		SimpleCollectionDatabase coll_db = new SimpleCollectionDatabase(dbtype);
		if (!coll_db.databaseOK())
		{
			logger.error("Couldn't create the collection database of type " + dbtype);
			return;
		}
		
		// Open database for reading. It may not exist if collection is pre-built without archives (such as demo collections)
		String coll_db_file = GSFile.archivesDatabaseFile(this.site_home, collection, dbtype);
		if (!coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.READ))
		{
			logger.error("Could not open collection archives database. Database doesn't exist or else somebody's already using it?");
			return;
		}
		// now we know we have an archives folder
		String old_value = coll_db.getValue(oid);
		String new_value = old_value.replace("<index-status>B", "<index-status>" + mark);
		// Close database for reading
		coll_db.closeDatabase();
		if (!coll_db.openDatabase(coll_db_file, SimpleCollectionDatabase.WRITE))
		{
			logger.error("Could not open collection archives database. Somebody already using this database!");
			return;
		}
		coll_db.setValue(oid, new_value);
		coll_db.closeDatabase();
		
	}
}
