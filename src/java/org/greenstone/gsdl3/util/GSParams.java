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

	public static final String ACTION = "a"; // the major type of action- eg query or browse or process
	public static final String SUBACTION = "sa"; // subtype of action if we want different processing than the default
	public static final String REQUEST_TYPE = "rt"; // whether the request is just to display the service form, or to actually do a request to the service
	public static final String RESPONSE_ONLY = "ro"; // if == 1 do the request and pass back the response xml - no page formatting
	public static final String OUTPUT = "o"; // if processing is to be done, what type of output - html/xml/other??
	public static final String HTTPHEADERFIELDS = "hhf";
	public static final String SERVICE = "s"; // the name of the service
	public static final String CLUSTER = "c"; // these two are the same
	public static final String SYSTEM = "s";
	public static final String CONFIGURE = "c";
	public static final String COLLECTION = "c";
	public static final String LANGUAGE = "l";
	public static final String DOCUMENT = "d";
	public static final String DOCUMENT_TYPE = "dt";
	public static final String RESOURCE = "r";
	public static final String PROCESS_ID = "pid"; // if a request wasn't completed, this identifies the request - used when asking for a status update
	public static final String COLLECTION_TYPE = "ct";

	public static final String SIBLING = "sib"; // this should not be in here
	// internal configure args
	public static final String SYSTEM_SUBSET = "ss";
	public static final String SYSTEM_CLUSTER = "sc";
	public static final String SYSTEM_MODULE_NAME = "sn";
	public static final String SYSTEM_MODULE_TYPE = "st";

	public static final String EXPAND_DOCUMENT = "ed";
	public static final String EXPAND_CONTENTS = "ec";
	public static final String REALISTIC_BOOK = "book";

	// used for filtering out a piece of the final page
	public static final String EXCERPT_ID = "excerptid";
	public static final String EXCERPT_TAG = "excerpttag";

	public static final String INLINE_TEMPLATE = "ilt";
	public static final String DISPLAY_METADATA = "dmd";

	protected HashMap param_map = null;

	public GSParams()
	{
		this.param_map = new HashMap(30);

		// add in all the standard params
		addParameter(ACTION, false);
		addParameter(SUBACTION, false);
		addParameter(REQUEST_TYPE, false);
		addParameter(RESPONSE_ONLY, false);
		addParameter(CLUSTER, false); // we don't want to save collection 
		//addParameter(COLLECTION); 
		addParameter(LANGUAGE, true);
		addParameter(DOCUMENT, true);
		addParameter(RESOURCE, true);
		addParameter(OUTPUT, false);
		addParameter(SERVICE, false);
		addParameter(PROCESS_ID, true);
		addParameter(SYSTEM_SUBSET, false);
		addParameter(SYSTEM_CLUSTER, false);
		addParameter(SYSTEM_MODULE_NAME, false);
		addParameter(SYSTEM_MODULE_TYPE, false);
		addParameter(SIBLING, false);
		addParameter(DOCUMENT_TYPE, true);
		addParameter(EXPAND_DOCUMENT, false);
		addParameter(EXPAND_CONTENTS, false);
		addParameter(REALISTIC_BOOK, false);
		addParameter(INLINE_TEMPLATE, false);
		addParameter(DISPLAY_METADATA, false);

		//addParameter();
		// ugly hack so we don't save the extlink param
		addParameter("s0.ext", false);
		addParameter(COLLECTION_TYPE, true); // collection type - mg or mgpp

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
		Param p = (Param) this.param_map.get(name);
		if (p == null)
			return false;
		p.default_value = default_value;
		return true;
	}

	public boolean shouldSave(String name)
	{
		if (name.startsWith("p."))
			return false;
		Param p = (Param) this.param_map.get(name);
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
