/*
 *    Library2.java
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
package org.greenstone.gsdl3;

import org.greenstone.gsdl3.core.*;
import org.greenstone.gsdl3.util.*;
import org.greenstone.util.GlobalProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * A program to take cgi args from the command line and return html
 * 
 * @author Katherine Don
 * @version $Revision$
 */

final public class Library2
{

	protected XMLConverter converter = null;
	protected Document doc = null;

	protected HashMap<String, String> saved_args = null;
	protected GSParams params = null;
	protected DefaultReceptionist recept = null;

	public Library2()
	{
		this.converter = new XMLConverter();
		this.doc = converter.newDOM();
		this.saved_args = new HashMap<String, String>();
		this.params = new GSParams();
		this.recept = new DefaultReceptionist();
	}

	public void configure(String site_name, String interface_name)
	{

		HashMap<String, Comparable> config_params = new HashMap<String, Comparable>();
		//config_params.put(GSConstants.GSDL3_HOME, gsdl_home);
		config_params.put(GSConstants.SITE_NAME, site_name);
		config_params.put(GSConstants.INTERFACE_NAME, interface_name);
		config_params.put(GSConstants.ALLOW_CLIENT_SIDE_XSLT, false);

		// new message router - create it and pass a handle to recept.
		// the servlet wont use this directly
		MessageRouter message_router = new MessageRouter();

		message_router.setSiteName(site_name);
		message_router.configure();
		// new receptionist
		recept.setConfigParams(config_params);
		recept.setMessageRouter(message_router);
		recept.setParams(params);
		recept.configure();
	}

    /*
     *  On Linux, run as:
     *  GS3> java -classpath "web/WEB-INF/lib/*":"lib/jni/*" org.greenstone.gsdl3.Library2 localsite default
     *  Press enter to accept the default cgi-args to pass in.
     * 
     *  For how to include all jars in a folder into the classpath to run a java program, see
     *  http://stackoverflow.com/questions/219585/setting-multiple-jars-in-java-classpath
     *  http://stackoverflow.com/questions/6780678/run-class-in-jar-file
     */
	public static void main(String args[])
	{

	    if (System.getenv("GSDL3SRCHOME") == null) {
		System.out.println("Before calling this script, run: source gs3-setup.sh");	
		System.exit(1);
	    }

		if (args.length != 2)
		{
			System.out.println("Usage: Library2 <site name> <interface name>");
			System.exit(1);
		}

		// force GlobalProperties to default GSDL3HOME to GSDL3SRCHOME/web if not already set
		GlobalProperties.loadGlobalProperties("");

		Library2 library = new Library2();
		library.configure(args[0], args[1]);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String query = null;
		String result = null;
		while (true)
		{
			System.out.println("Please enter the  cgi args (all on one line), or 'exit' to quit (default a=p&sa=home)");
			try
			{
				query = br.readLine();
			}
			catch (Exception e)
			{
				System.err.println("Library2 exception:" + e.getMessage());
			}
			if (query.startsWith("exit"))
			{
				System.exit(1);
			}

			result = library.process(query);

			System.out.println(result);

		}

	}

	protected String process(String query)
	{
		Element xml_message = generateRequest(query);
		System.out.println("*********************");
		System.out.println(converter.getPrettyString(xml_message));
		Node xml_result = recept.process(xml_message);
		return this.converter.getPrettyString(xml_result);
	}

	protected Element generateRequest(String cgiargs)
	{

		Element xml_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element xml_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PAGE, "", new UserContext());
		xml_message.appendChild(xml_request);
		Element xml_param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		xml_request.appendChild(xml_param_list);

		// the defaults
		String action = "p";
		String subaction = "home";
		String lang = saved_args.get(GSParams.LANGUAGE);
		if (lang == null)
		{
			lang = "en";
		}
		String output = "html";

		String args[] = cgiargs.split("&");
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			int pos = arg.indexOf('=');
			if (pos == -1)
				continue;
			String name = arg.substring(0, pos);
			String value = arg.substring(pos + 1);
			if (name.equals(GSParams.ACTION))
			{
				action = value;
			}
			else if (name.equals(GSParams.SUBACTION))
			{
				subaction = (value);
			}
			else if (name.equals(GSParams.LANGUAGE))
			{
				lang = value;
				saved_args.put(name, value);
			}
			else if (name.equals(GSParams.OUTPUT))
			{
				output = value;
			}
			else if (params.shouldSave(name))
			{
				saved_args.put(name, value);
			}
			else
			{
				// make a param now
				Element param = doc.createElement(GSXML.PARAM_ELEM);
				param.setAttribute(GSXML.NAME_ATT, name);
				param.setAttribute(GSXML.VALUE_ATT, GSXML.xmlSafe(value));
				xml_param_list.appendChild(param);
			}
		}

		xml_request.setAttribute(GSXML.OUTPUT_ATT, output);
		xml_request.setAttribute(GSXML.ACTION_ATT, action);
		xml_request.setAttribute(GSXML.SUBACTION_ATT, subaction);
		xml_request.setAttribute(GSXML.LANG_ATT, lang);

		// put in all the params from the session cache
		Set<String> params = saved_args.keySet();
		Iterator<String> i = params.iterator();
		while (i.hasNext())
		{
			String name = i.next();
			if (name.equals(GSParams.LANGUAGE))
				continue;
			Element param = this.doc.createElement(GSXML.PARAM_ELEM);
			param.setAttribute(GSXML.NAME_ATT, name);
			param.setAttribute(GSXML.VALUE_ATT, GSXML.xmlSafe(saved_args.get(name)));
			xml_param_list.appendChild(param);
		}

		return xml_message;
	}
}
