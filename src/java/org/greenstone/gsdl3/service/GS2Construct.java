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

import org.greenstone.gsdl3.util.*;
import org.greenstone.gsdl3.build.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Locale;

import org.apache.log4j.*;

/**
 * A Services class for building collections provides a wrapper around the old
 * perl scripts
 * 
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */
public class GS2Construct extends ServiceRack
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2Construct.class.getName());

	// services offered
	private static final String NEW_SERVICE = "NewCollection";
	private static final String ADD_DOC_SERVICE = "AddDocument";
	private static final String IMPORT_SERVICE = "ImportCollection";
	private static final String BUILD_SERVICE = "BuildCollection";
	private static final String ACTIVATE_SERVICE = "ActivateCollection";
	private static final String DELETE_SERVICE = "DeleteCollection";
	private static final String RELOAD_SERVICE = "ReloadCollection";

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

	// the list of the collections - store between some method calls
	private String[] collection_list = null;

	// set of listeners for any construction commands
	protected Map listeners = null;

	public GS2Construct()
	{
		this.listeners = Collections.synchronizedMap(new HashMap());

	}

	/** returns a specific service description */
	protected Element getServiceDescription(String service, String lang, String subset)
	{

		Element description = this.doc.createElement(GSXML.SERVICE_ELEM);
		description.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		description.setAttribute(GSXML.NAME_ATT, service);
		if (subset == null || subset.equals(GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER))
		{
			description.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME, getTextString(service + ".name", lang)));
			description.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION, getTextString(service + ".description", lang)));
			description.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_SUBMIT, getTextString(service + ".submit", lang)));
		}
		if (subset == null || subset.equals(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER))
		{
			Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			description.appendChild(param_list);

			if (service.equals(NEW_SERVICE))
			{

				Element param = GSXML.createParameterDescription(this.doc, NEW_COL_TITLE_PARAM, getTextString("param." + NEW_COL_TITLE_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
				param_list.appendChild(param);
				param = GSXML.createParameterDescription(this.doc, CREATOR_PARAM, getTextString("param." + CREATOR_PARAM, lang), GSXML.PARAM_TYPE_STRING, null, null, null);
				param_list.appendChild(param);
				param = GSXML.createParameterDescription(this.doc, NEW_COL_ABOUT_PARAM, getTextString("param." + NEW_COL_ABOUT_PARAM, lang), GSXML.PARAM_TYPE_TEXT, null, null, null);
				param_list.appendChild(param);
				String[] types = { BUILDTYPE_MGPP, BUILDTYPE_MG };
				String[] type_texts = { getTextString("param." + BUILDTYPE_PARAM + "." + BUILDTYPE_MGPP, lang), getTextString("param." + BUILDTYPE_PARAM + "." + BUILDTYPE_MG, lang) };

				param = GSXML.createParameterDescription(this.doc, BUILDTYPE_PARAM, getTextString("param." + BUILDTYPE_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, BUILDTYPE_MGPP, types, type_texts);
				param_list.appendChild(param);
			}
			else if (service.equals(ACTIVATE_SERVICE) || service.equals(IMPORT_SERVICE) || service.equals(BUILD_SERVICE) || service.equals(RELOAD_SERVICE) || service.equals(DELETE_SERVICE))
			{

				this.collection_list = getCollectionList();
				Element param = GSXML.createParameterDescription(this.doc, COL_PARAM, getTextString("param." + COL_PARAM, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, null, this.collection_list, this.collection_list);
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
		return runCommand(request, GS2PerlConstructor.NEW);
	}

	/** TODO:implement this */
	protected Element processAddDocument(Element request)
	{
		// decode the file name, add it to the import directory
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = this.doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		//String lang = request.getAttribute(GSXML.LANG_ATT);
		//String request_type = request.getAttribute(GSXML.TYPE_ATT);
		Text t = this.doc.createTextNode("AddDocument: not implemented yet");
		status.appendChild(t);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
		return response;
	}

	protected Element processImportCollection(Element request)
	{
		return runCommand(request, GS2PerlConstructor.IMPORT);
	}

	protected Element processBuildCollection(Element request)
	{
		return runCommand(request, GS2PerlConstructor.BUILD);
	}

	protected Element processActivateCollection(Element request)
	{
		// this activates the collection on disk. but now we need to tell
		// the MR about it. but we have to wait until the process is finished.
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(param_list, false);
		String coll_name = (String) params.get(COL_PARAM);
		String lang = request.getAttribute(GSXML.LANG_ATT);

		systemRequest("delete", coll_name, null, lang);

		Element response = runCommand(request, GS2PerlConstructor.ACTIVATE);
		Element status = (Element) GSXML.getChildByTagName(response, GSXML.STATUS_ELEM);

		String request_type = request.getAttribute(GSXML.TYPE_ATT);
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			return response;
		}

		// check for finished
		int status_code = Integer.parseInt(status.getAttribute(GSXML.STATUS_ERROR_CODE_ATT));
		if (GSStatus.isCompleted(status_code) && GSStatus.isError(status_code))
		{
			// we shouldn't carry out the next bit, just return the response
			return response;
		}
		String id = status.getAttribute(GSXML.STATUS_PROCESS_ID_ATT);
		GS2PerlListener listener = (GS2PerlListener) this.listeners.get(id);
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
		Text t = this.doc.createTextNode("\n" + listener.getUpdate());
		status.appendChild(t);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(listener.getStatus()));
		if (GSStatus.isError(status_code))
		{
			return response; // without doing the next bit
		}

		t = this.doc.createTextNode("\n");
		status.appendChild(t);
		// once have got here, we assume
		// the first bit proceeded successfully, now reload the collection
		systemRequest("reload", coll_name, status, lang); // this will append more messages to the status, and overwrite the error code att
		return response;

	}

	protected Element processDeleteCollection(Element request)
	{

		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = this.doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		Text t = null; // the text node for the error/success message
		String lang = request.getAttribute(GSXML.LANG_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(param_list, false);

		boolean get_status_only = false;
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			get_status_only = true;
		}
		if (get_status_only)
		{
			// at the moment, delete is synchronous. but it may take ages so should do the command in another thread maybe? in which case we will want to ask for status
			logger.error("had a status request for delete - this shouldn't happen!!");
			//t = this.doc.createTextNode("");
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
			t = this.doc.createTextNode(getTextString("delete.exists_error", lang, args));
			status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		// try to delete the directory
		if (!GSFile.deleteFile(coll_dir))
		{
			t = this.doc.createTextNode(getTextString("delete.delete_error", lang, args));
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			status.appendChild(t);
			return response;
		}
		systemRequest("delete", coll_name, status, lang);
		return response;
	}

	protected Element processReloadCollection(Element request)
	{

		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = this.doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);
		Text t = null; // the text node for the error/success message

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(param_list, false);

		boolean get_status_only = false;
		if (request_type.equals(GSXML.REQUEST_TYPE_STATUS))
		{
			get_status_only = true;
		}
		if (get_status_only)
		{
			// reload is synchronous - this makes no sense
			logger.error("had a status request for reload - this shouldn't happen!!");
			//t = this.doc.createTextNode("");
			//status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		String coll_name = (String) params.get(COL_PARAM);
		systemRequest("reload", coll_name, status, lang);
		return response;

	}

	/**
	 * send a configure request to the message router action name should be
	 * "delete" or "reload" response will be put into the status element
	 */
	protected void systemRequest(String action_name, String coll_name, Element status, String lang)
	{
		// send the request to the MR
		Element message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_SYSTEM, "", lang, "");
		message.appendChild(request);
		Element command = this.doc.createElement(GSXML.SYSTEM_ELEM);
		request.appendChild(command);
		command.setAttribute(GSXML.SYSTEM_MODULE_TYPE_ATT, GSXML.COLLECTION_ELEM);
		command.setAttribute(GSXML.SYSTEM_MODULE_NAME_ATT, coll_name);

		if (action_name.equals("delete"))
		{
			command.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_DEACTIVATE);
		}
		else if (action_name.equals("reload"))
		{
			command.setAttribute(GSXML.TYPE_ATT, GSXML.SYSTEM_TYPE_ACTIVATE);
		}
		else
		{
			logger.error("invalid action name passed to systemRequest:" + action_name);
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
				t = this.doc.createTextNode(getTextString(action_name + ".configure_error", lang, args));
				status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
				status.appendChild(t);
				return;
			}

			// if we got here, we have succeeded!
			t = this.doc.createTextNode(getTextString(action_name + ".success", lang, args));
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

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, NEW_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, IMPORT_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, BUILD_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, ACTIVATE_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, DELETE_SERVICE);
		this.short_service_info.appendChild(e);

		e = this.doc.createElement(GSXML.SERVICE_ELEM);
		e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		e.setAttribute(GSXML.NAME_ATT, RELOAD_SERVICE);
		this.short_service_info.appendChild(e);

		//e = this.doc.createElement(GSXML.SERVICE_ELEM);
		//e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_PROCESS);
		//e.setAttribute(GSXML.NAME_ATT, ADD_DOC_SERVICE);
		//this.short_service_info.appendChild(e);

		return true;
	}

	/** returns a response element */
	protected Element runCommand(Element request, int type)
	{
		// the response to send back
		String name = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		Element response = this.doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, name);
		Element status = this.doc.createElement(GSXML.STATUS_ELEM);
		response.appendChild(status);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String uid = request.getAttribute(GSXML.USER_ID_ATT);
		String request_type = request.getAttribute(GSXML.TYPE_ATT);

		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap params = GSXML.extractParams(param_list, false);

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
			GS2PerlListener listener = (GS2PerlListener) this.listeners.get(id);
			if (listener == null)
			{
				Text t = this.doc.createTextNode(getTextString("general.process_id_error", lang));
				status.appendChild(t);
				status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			}
			else
			{
				Text t = this.doc.createTextNode(listener.getUpdate());
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

		// do teh actual command
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
		logger.info("Coll name = " + coll_name);
		// makes a paramList of the relevant params
		Element other_params = extractOtherParams(params, type);

		//create the constructor to do the work
		GS2PerlConstructor constructor = new GS2PerlConstructor("perl_build");
		if (!constructor.configure())
		{
			Text t = this.doc.createTextNode(getTextString("general.configure_constructor_error", lang));
			status.appendChild(t);
			status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ERROR));
			return response;
		}

		GS2PerlListener listener = new GS2PerlListener();
		constructor.setSiteHome(this.site_home);
		constructor.setCollectionName(coll_name);
		constructor.setActionType(type);
		constructor.setProcessParams(other_params);

		constructor.addListener(listener);
		constructor.start();

		String id = newID();
		this.listeners.put(id, listener);

		status.setAttribute(GSXML.STATUS_PROCESS_ID_ATT, id);
		status.setAttribute(GSXML.STATUS_ERROR_CODE_ATT, Integer.toString(GSStatus.ACCEPTED));
		Text t = this.doc.createTextNode(getTextString("general.process_start", lang));
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
	protected Element extractOtherParams(HashMap params, int type)
	{

		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (type == GS2PerlConstructor.NEW)
		{
			Element param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "creator");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(CREATOR_PARAM));

			param_list.appendChild(param);
			param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "about");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(NEW_COL_ABOUT_PARAM));
			param_list.appendChild(param);
			param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "title");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(NEW_COL_TITLE_PARAM));
			param_list.appendChild(param);
			param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, "buildtype");
			param.setAttribute(GSXML.VALUE_ATT, (String) params.get(BUILDTYPE_PARAM));
			param_list.appendChild(param);
			return param_list;
		}

		// other ones dont have params yet
		return null;
	}

}
