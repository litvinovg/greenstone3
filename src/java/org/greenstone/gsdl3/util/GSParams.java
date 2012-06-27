/*
 *    GSParams.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.util;

import java.util.HashMap;

/** keeps track of the interface parameters, and their defaults */
public class GSParams
{

	// cgi parameter names
	public static final String ACTION = "a"; // the major type of action- eg query or browse or process
	public static final String SUBACTION = "sa"; // subtype of action if we want different processing than the default
	public static final String REQUEST_TYPE = "rt"; // whether the request is just to display the service form, or to actually do a request to the service
	public static final String RESPONSE_ONLY = "ro"; // if == 1 do the request and pass back the response xml - no page formatting
	public static final String OUTPUT = "o"; // if processing is to be done, what type of output - html/xml/other??
	public static final String SERVICE = "s"; // the name of the service

	public static final String CLUSTER = "c"; // these two are the same
	public static final String COLLECTION = "c";
	public static final String COLLECTION_TYPE = "ct"; // collection type - mg, mgpp, lucene etc

	public static final String LANGUAGE = "l";
	public static final String DOCUMENT = "d";
	public static final String DOCUMENT_TYPE = "dt";
	public static final String START_PAGE = "startPage";
	public static final String S_START_PAGE = "s1.startPage";
	public static final String HREF = "href"; // url. might be an external url, or a relative one that needs translating
	public static final String RELATIVE_LINK = "rl"; // whether the href url is relative to the collection or not.
	public static final String EXTERNAL_LINK_TYPE = "el"; // for an external link, go direct to the page or frame it in the collection
	public static final String PROCESS_ID = "pid"; // if a request wasn't completed, this identifies the request - used when asking for a status update

	public static final String HTTPHEADERFIELDS = "hhf";

	// internal configure args
	public static final String SYSTEM_SUBSET = "ss";
	public static final String SYSTEM_CLUSTER = "sc";
	public static final String SYSTEM_MODULE_NAME = "sn";
	public static final String SYSTEM_MODULE_TYPE = "st";

	// used for filtering out a piece of the final page
	public static final String EXCERPT_ID = "excerptid";
	public static final String EXCERPT_TAG = "excerpttag";

	public static final String INLINE_TEMPLATE = "ilt";
	public static final String DISPLAY_METADATA = "dmd";
	public static final String FILE_LOCATION = "fl";
	public static final String DOC_EDIT = "docEdit";
	public static final String AJAX_LOAD_BYPASS = "alb";

	//Administration
	public static final String PASSWORD = "password";
	public static final String S_PASSWORD = "s1.password";
	public static final String S_NEW_PASSWORD = "s1.newPassword";
	public static final String S_OLD_PASSWORD = "s1.oldPassword";
	
	//Facets
	public static final String S_FACETS = "s1.facets";
	public static final String S_FACETS_QUERIES = "s1.facetQueries";

	// some standard arg values
	public static final String SYSTEM_ACTION = "s";

	public static final String EXTERNAL_LINK_TYPE_DIRECT = "direct";
	public static final String EXTERNAL_LINK_TYPE_FRAMED = "frame";

	protected HashMap<String, Param> param_map = null;

	public GSParams()
	{
		this.param_map = new HashMap<String, Param>(30);

		// add in all the standard params
		addParameter(ACTION, false);
		addParameter(SUBACTION, false);
		addParameter(REQUEST_TYPE, false);
		addParameter(RESPONSE_ONLY, false);
		addParameter(CLUSTER, false); // we don't want to save cluster/collection 
		addParameter(LANGUAGE, true);
		addParameter(DOCUMENT, true);
		addParameter(DOCUMENT_TYPE, true);
		addParameter(START_PAGE, false);
		addParameter(S_START_PAGE, false);
		// should the following two just be in doc action??
		addParameter(HREF, false);
		addParameter(RELATIVE_LINK, false);
		addParameter(OUTPUT, false);
		addParameter(SERVICE, false);
		addParameter(PROCESS_ID, true);
		addParameter(SYSTEM_SUBSET, false);
		addParameter(SYSTEM_CLUSTER, false);
		addParameter(SYSTEM_MODULE_NAME, false);
		addParameter(SYSTEM_MODULE_TYPE, false);
		addParameter(INLINE_TEMPLATE, false);
		addParameter(DISPLAY_METADATA, false);
		addParameter(AJAX_LOAD_BYPASS, false);
		addParameter(DOC_EDIT, false);
		addParameter(PASSWORD, false);
		addParameter(S_PASSWORD, false);
		addParameter(S_NEW_PASSWORD, false);
		addParameter(S_OLD_PASSWORD, false);
		
		addParameter(S_FACETS, false);
		addParameter(S_FACETS_QUERIES, false);

		addParameter(COLLECTION_TYPE, true);
		addParameter(EXTERNAL_LINK_TYPE, false);
		// filtering args must be specified each time
		addParameter(EXCERPT_ID, false);
		addParameter(EXCERPT_TAG, false);
	}

	public boolean addParameter(String name, boolean save)
	{
		return addParameter(name, "", save);
	}

	public boolean addParameter(String name, String default_value, boolean save)
	{
		if (this.param_map.containsKey(name))
		{
			// already there so could not add
			return false;
		}

		this.param_map.put(name, new Param(default_value, save));
		return true;
	}

	public boolean setParamDefault(String name, String default_value)
	{
		Param p = this.param_map.get(name);
		if (p == null)
			return false;
		p.default_value = default_value;
		return true;
	}

	public boolean shouldSave(String name)
	{
		// p. is used to store previous settings
		if (name.startsWith("p."))
			return false;
		Param p = this.param_map.get(name);
		if (p == null)
			return true; // if things are not in here, always save.
		return p.save;
	}

	private class Param
	{

		public String default_value = null;
		public boolean save = true;

		public Param(String default_value, boolean save)
		{
			this.default_value = default_value;
			this.save = save;
		}
	}
}
