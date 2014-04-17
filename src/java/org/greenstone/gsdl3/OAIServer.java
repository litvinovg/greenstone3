/*
 *    OAIServer.java
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.comms.Communicator;
import org.greenstone.gsdl3.comms.SOAPCommunicator;
import org.greenstone.gsdl3.core.OAIMessageRouter;
import org.greenstone.gsdl3.core.OAIReceptionist;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.OAIResumptionToken;
import org.greenstone.gsdl3.util.OAIXML;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** a servlet to serve the OAI metadata harvesting - we are using servlets instead
 * of cgi
 * the init method is called only once - the first time the servlet classes
 * are loaded. Each time a request comes in to the servlet, the session()
 * method is called in a new thread (calls doGet/doPut etc)
 * takes the verb= type args and builds a simple request to send to
 * the oai receptionist, which returns a result in xml, conforming to the OAI-PMH
 * protocol.
 * @see Receptionist
 */
/**
 * OAI server configuration instructions *
 * 
 */
public class OAIServer extends BaseGreenstoneServlet
{

	/** the receptionist to send messages to */
	protected OAIReceptionist recept = null;
	/**
	 * the default language - is specified by setting a servlet param, otherwise
	 * DEFAULT_LANG is used
	 */
	protected String default_lang = null;
	/**
	 * The default default - used if a default lang is not specified in the
	 * servlet params
	 */
	protected final String DEFAULT_LANG = "en";

	/** A HashSet which contains all the legal verbs. */
	protected HashSet<String> verb_set = null;
	/**
	 * A HashSet which contains all the legal oai keys in the key/value argument
	 * pair.
	 */
	protected HashSet<String> param_set = null;
	/**
	 * The name of the site with which we will finally be dealing, whether it is
	 * a local site or a remote site through a communicator.
	 */
	protected String site = "";

  // can be overriddden in OAIConfig.xml
	// do we output the stylesheet processing instruction?
	protected boolean use_oai_stylesheet = true;
	protected String oai_stylesheet = "interfaces/oai/oai2.xsl";

	// there is no getQueryString() method in the HttpServletRequest returned from doPost, 
	// since that is actually of type apache RequestFacade, and doesn't define such a method
	protected String queryString = null;

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.OAIServer.class.getName());

	/**
	 * initialise the servlet
	 */
	public void init(ServletConfig config) throws ServletException
	{
		// always call super.init, i.e., HttpServlet.;
		super.init(config);
		this.default_lang = config.getInitParameter(GSConstants.DEFAULT_LANG);

		initVerbs();
		initParams();

		String site_name = config.getInitParameter(GSConstants.SITE_NAME);
		String remote_site_name = null;
		String remote_site_type = null;
		String remote_site_address = null;

		if (site_name == null)
		{
			// no local site, try for communicator (remote site)
			remote_site_name = config.getInitParameter("remote_site_name");
			remote_site_type = config.getInitParameter("remote_site_type");
			remote_site_address = config.getInitParameter("remote_site_address");
			if (remote_site_name == null || remote_site_type == null || remote_site_address == null)
			{
				logger.error("initialisation paramters not all set!");
				logger.error("if site_name is not set, then you must have remote_site_name, remote_site_type and remote_site_address set");
				throw new UnavailableException("OAIServer: incorrect servlet parameters");
			}
		}

		if (this.default_lang == null)
		{
			// choose english
			this.default_lang = DEFAULT_LANG;
		}

		// the receptionist -the servlet will talk to this
		this.recept = new OAIReceptionist();

		// the receptionist uses a OAIMessageRouter or Communicator to send its requests to. We either create a OAIMessageRouter here for the designated site (if site_name set), or we create a Communicator for a remote site. The is given to teh Receptionist, and the servlet never talks to it again.directly.
		if (site_name != null)
		{
			//this site_name could consist of comma separated more than one site name.
			String mr_name = (String) config.getInitParameter("messagerouter_class");
			OAIMessageRouter message_router = null;
			if (mr_name == null)
			{ // just use the normal MR *********
				message_router = new OAIMessageRouter();
			}
			else
			{ // try the specified one
				try
				{
					message_router = (OAIMessageRouter) Class.forName("org.greenstone.gsdl3.core." + mr_name).newInstance();
				}
				catch (Exception e)
				{ // cant use this new one, so use normal one
				  logger.error("OAIServlet configure exception when trying to use a new OAIMessageRouter " + mr_name, e);
				  message_router = new OAIMessageRouter();
				}
			}

			message_router.setSiteName(site_name);
			// lots of work is done in this step; see OAIMessageRouter.java
			if (!message_router.configure()) {
			  throw new UnavailableException("OAIServer: Couldn't configure OAIMessageRouter");
			}
			this.recept.setSiteName(site_name);
			this.recept.setMessageRouter(message_router);

		}
		else
		{
			// talking to a remote site, create a communicator
			Communicator communicator = null;
			// we need to create the XML to configure the communicator
			Document site_doc = XMLConverter.newDOM();
			Element site_elem = site_doc.createElement(GSXML.SITE_ELEM);
			site_elem.setAttribute(GSXML.TYPE_ATT, remote_site_type);
			site_elem.setAttribute(GSXML.NAME_ATT, remote_site_name);
			site_elem.setAttribute(GSXML.ADDRESS_ATT, remote_site_address);

			if (remote_site_type.equals(GSXML.COMM_TYPE_SOAP_JAVA))
			{
				communicator = new SOAPCommunicator();
			}
			else
			{
				logger.error("OAIServlet.init Error: invalid Communicator type: " + remote_site_type);
				throw new UnavailableException("OAIServer: invalid communicator type");
			}

			if (!communicator.configure(site_elem))
			{
				logger.error("OAIServlet.init Error: Couldn't configure communicator");
				throw new UnavailableException("OAIServer: Couldn't configure communicator");
			}
			this.recept.setSiteName(remote_site_name);
			this.recept.setMessageRouter(communicator);
		}

		// Read in OAIConfig.xml (residing web/WEB-INF/classes/) and 
		//use it to configure the receptionist. 
		Element oai_config = OAIXML.getOAIConfigXML();
		if (oai_config == null)
		{
			logger.error("Fail to parse oai config file OAIConfig.xml.");
			throw new UnavailableException("OAIServer: Couldn't parse OAIConfig.xml");
		}
		// pass it to the receptionist
		if (!this.recept.configure(oai_config)) {
		  logger.error("Couldn't configure receptionist");
		  throw new UnavailableException("OAIServer: Couldn't configure receptionist"); 
		}
		// also, we have something we want to get from here - useOAIStylesheet
		this.configure(oai_config);
		// Initialise the resumption tokens
		OAIResumptionToken.init();

	}//end of init()

	private void configure(Element oai_config)
	{
		Element use_stylesheet_elem = (Element) GSXML.getChildByTagName(oai_config, OAIXML.USE_STYLESHEET);
		if (use_stylesheet_elem != null)
		{
			String value = GSXML.getNodeText(use_stylesheet_elem);
			if (value.equals("no"))
			{
				this.use_oai_stylesheet = false;
			}
		}
		if (this.use_oai_stylesheet)
		{
			// now see if there is a custom stylesheet specified
			Element stylesheet_elem = (Element) GSXML.getChildByTagName(oai_config, OAIXML.STYLESHEET);
			if (stylesheet_elem != null)
			{
				String value = GSXML.getNodeText(stylesheet_elem);
				if (!value.equals(""))
				{
					oai_stylesheet = value;
				}
			}

		}
	}

	private void initVerbs()
	{
		verb_set = new HashSet<String>();
		verb_set.add(OAIXML.GET_RECORD);
		verb_set.add(OAIXML.LIST_RECORDS);
		verb_set.add(OAIXML.LIST_IDENTIFIERS);
		verb_set.add(OAIXML.LIST_SETS);
		verb_set.add(OAIXML.LIST_METADATA_FORMATS);
		verb_set.add(OAIXML.IDENTIFY);
	}

	private void initParams()
	{
		param_set = new HashSet<String>();
		param_set.add(OAIXML.METADATA_PREFIX);
		param_set.add(OAIXML.FROM);
		param_set.add(OAIXML.UNTIL);
		param_set.add(OAIXML.SET);
		param_set.add(OAIXML.RESUMPTION_TOKEN);
		param_set.add(OAIXML.IDENTIFIER);
	}

	private void logUsageInfo(HttpServletRequest request)
	{
		String usageInfo = "";

		String query = (queryString == null) ? request.getQueryString() : queryString;

		//logged info = general-info + session-info
		usageInfo = request.getContextPath() + " " + //session id
		request.getServletPath() + " " + //serlvet
		"[" + query + "]" + " " + //the query string
		"[" + usageInfo.trim() + "]" + " " + // params stored in a session
		request.getRemoteAddr() + " " + //remote address
		request.getHeader("user-agent") + " "; //the remote brower info

		logger.info(usageInfo);
	}

	/**
	 * return true if the url is in the form of baseURL?verb=...,
	 */
	private boolean validate(String query, String verb)
	{
		//Here in OAIServer, only the verbs are validated. All the validation for individual verb
		// is taken in their doXXX() methods.
		if (query == null || !query.startsWith(OAIXML.VERB + "="))
		{
			return false;
		}
		if (!verb_set.contains(verb))
		{
			return false;
		}
		return true;
	}

	private String getVerb(String query)
	{
		if (query == null)
			return "";
		int verb_start_index = query.indexOf("=") + 1;// first occurence of '='
		int verb_end_index = query.indexOf("&");
		if (verb_end_index == -1)
		{
			return query.substring(verb_start_index);
		}
		return query.substring(verb_start_index, verb_end_index);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		logUsageInfo(request);

		// oai always requires the content type be text/xml
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();

		//
		String lang = request.getParameter(GSParams.LANGUAGE);
		if (lang == null || lang.equals(""))
		{
			// use the default
			lang = this.default_lang;
		}
		//we don't get the baseURL from the http request because what we get might be different from the one known publicly due to local network redirection.
		//For example, puka.cs.waikato.ac.nz vs www.greenstone.org
		//String base_url = request.getRequestURL().toString();
		
		// if called by doPost (if this was originally a POST request), var queryString would have been set
		String query = (queryString == null) ? request.getQueryString() : queryString;
		queryString = null; // reset member variable, else no doGet will work as long as the server remains running

		if (query!=null && query.equals("reset")) {
		  logger.error("reset was called*******************");
		  out.println("<?xml version='1.0' encoding='UTF-8' ?>");
		  out.println(this.recept.process("<message><request reset='true'/></message>"));
		  return;
		}
		String[] pairs = (query == null) ? null : query.split("&");//split into key/value pairs
		
		String verb = getVerb(query);
		Document response_doc = XMLConverter.newDOM();
		Element xml_response = OAIXML.createBasicResponse(response_doc, verb, pairs);
		Element verb_elem = null;

		if (validate(query, verb) == false)
		{
			if (verb_set.contains(verb) == false)
			{
				logger.error(OAIXML.BAD_VERB + ": " + query);
				verb_elem = OAIXML.createErrorElement(response_doc, OAIXML.BAD_VERB, OAIXML.ILLEGAL_OAI_VERB);
			}
			else
			{
				//must be something else other than bad verbs caused an error, so bad argument
				logger.error(OAIXML.BAD_ARGUMENT + ": " + query);
				verb_elem = OAIXML.createErrorElement(response_doc, OAIXML.BAD_ARGUMENT, "");
			}
			xml_response.appendChild(verb_elem);

			out.println("<?xml version='1.0' encoding='UTF-8' ?>");
			if (this.use_oai_stylesheet)
			{
				out.println("<?xml-stylesheet type='text/xsl' href='" + this.oai_stylesheet + "' ?>\n");
			}
			out.println(XMLConverter.getPrettyString(xml_response));
			return;
		}//end of if(validate

		// The query is valid, we can now
		// compose the request message to the receptionist
		Document request_doc = XMLConverter.newDOM();
		Element xml_message = request_doc.createElement(GSXML.MESSAGE_ELEM);
		Element xml_request = request_doc.createElement(GSXML.REQUEST_ELEM);
		// The type attribute is set to be 'oaiService' from OAIServer to OAIReceptionist.
		//xml_request.setAttribute(GSXML.TYPE_ATT, OAIXML.OAI_SERVICE);
		xml_request.setAttribute(GSXML.LANG_ATT, lang);
		xml_request.setAttribute(GSXML.TO_ATT, verb);
		addParams(xml_request, pairs);

		//xml_request.setAttribute(GSXML.OUTPUT_ATT, output);????
		xml_message.appendChild(xml_request);

		Node xml_result = this.recept.process(xml_message);
		if (xml_result == null)
		{
			logger.info("xml_result is null");
			verb_elem = OAIXML.createErrorElement(response_doc, "Internal error", "");
			xml_response.appendChild(verb_elem);
		}
		else
		{

			/**
			 * All response elements are in the form (with a corresponding verb
			 * name): <message> <response> <verb> ... <resumptionToken> .. this
			 * is optional! </resumptionToken> </verb> </response> </message>
			 */
			Node res = GSXML.getChildByTagName(xml_result, GSXML.RESPONSE_ELEM);
			if (res == null)
			{
				logger.info("response element in xml_result is null");
				verb_elem = OAIXML.createErrorElement(response_doc, "Internal error", "");
			}
			else
			{
				verb_elem = GSXML.getFirstElementChild(res);
			}

			if ( verb_elem.getTagName().equals(OAIXML.ERROR))
			{
			  xml_response.appendChild(response_doc.importNode(verb_elem, true));
			}
			else if (OAIXML.oai_version.equals(OAIXML.OAI_VERSION2)) {
			  xml_response.appendChild(response_doc.importNode(verb_elem, true));
			}
			else
			{
				GSXML.copyAllChildren(xml_response, verb_elem);
			}
		}
		out.println("<?xml version='1.0' encoding='UTF-8' ?>");
		if (this.use_oai_stylesheet)
		{
			out.println("<?xml-stylesheet type='text/xsl' href='" + this.oai_stylesheet + "' ?>\n");
		}
		out.println(XMLConverter.getPrettyString(xml_response));
		return;
	}

	/** append parameter elements to the request sent to the receptionist */
	public void addParams(Element request, String[] pairs)
	{
	  Document doc = request.getOwnerDocument();
		// no params apart from the verb
		if (pairs == null || pairs.length < 2)
			return;

		/**
		 * the request xml is composed in the form: <request> <param name=.../>
		 * <param name=.../> </request> (No paramList element in between).
		 */
		for (int i = 1; i < pairs.length; i++)
		{
			//the first pair in pairs is the verb=xxx
			int index = pairs[i].indexOf("=");
			if (index != -1)
			{ //just a double check
			  Element param = GSXML.createParameter(doc, pairs[i].substring(0, index), OAIXML.oaiDecode(pairs[i].substring(index + 1)));
			  request.appendChild(param);
			}
		}
	}

	// For OAI version 2.0, validation tests indicated that POST needs to be supported. Some
	// modification was required in order to ensure that the request is passed intact to doGet()
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		// the post method returns a wrapper of type RequestFacade by apache and there
		// is no getQueryString() method defined for it. Therefore, need to work this out
		// manually before calling doGet(request, response) so that doGet can work as before.

		queryString = "";
		Iterator parameter_entries = request.getParameterMap().entrySet().iterator();
		while (parameter_entries.hasNext())
		{
			Map.Entry param_entry = (Map.Entry) parameter_entries.next();
			String[] paramVals = (String[]) param_entry.getValue();
			if (paramVals != null)
			{
				if (paramVals.length > 0)
				{
					logger.error("POST request received: " + param_entry.getKey() + " - " + paramVals[0]);
					queryString = queryString + "&" + param_entry.getKey() + "=" + paramVals[0];
				}
			}
		}
		if (queryString.length() > 0)
		{
			queryString = queryString.substring(1);
			//queryString = OAIXML.oaiEncode(queryString);
		}
		if (queryString.equals(""))
		{
			queryString = null;
		}
		doGet(request, response);
	}


	public void destroy()
	{
		recept.cleanUp();
	}

}
