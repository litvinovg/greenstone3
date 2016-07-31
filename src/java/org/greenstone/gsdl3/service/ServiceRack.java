/*
 *    ServiceRack.java
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

// greenstone classes
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.collection.ServiceCluster;
import org.greenstone.gsdl3.core.MessageRouter;
import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.CollectionClassLoader;
import org.greenstone.gsdl3.util.Dictionary;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * ServiceRack - abstract base class for services
 * 
 * A ServiceRack provides one or more Services. This base class implements the
 * process method. Each service is invoked by a method called process<service
 * name> which takes one parameter - the xml request Element, and returns an XML
 * response Element. for example, the TextQuery service would be invoked by
 * processTextQuery(Element request)
 * 
 * @author Katherine Don
 */
public abstract class ServiceRack implements ModuleInterface
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.ServiceRack.class.getName());

	/** the absolute address of the site home */
	protected String site_home = null;
	/** the http address of the site home */
	protected String site_http_address = null;

	protected String library_name = null;
	/**
	 * the name of the cluster (or collection) that this service belongs to - if
	 * any
	 */
	protected String cluster_name = null;

	/** some services can talk back to the message router */
	protected MessageRouter router = null;

        /** Access to the collection/serviceCluster object for service subclasses */
        protected ServiceCluster serviceCluster = null;

	/** a converter class to create Documents etc */
	protected XMLConverter converter = null;

	/** the original config info - if need to store it */
	protected Element config_info = null;

  /** XML element for stored description XML */
  protected Document desc_doc = null;

	/**
	 * XML element for describe requests - list of supported services - this is
	 * static
	 */
	protected Element short_service_info = null;

	/**
	 * XML element for stylesheet requests - map of service name to format elem
	 */
	protected HashMap<String, Node> format_info_map = null;

	protected Element _globalFormat = null;

	/**
	 * A class loader that knows about the collection resources directory can
	 * put properties files, dtds etc in here
	 */
	CollectionClassLoader class_loader = null;

	/** sets the cluster name */
	public void setClusterName(String cluster_name)
	{
		this.cluster_name = cluster_name;
	}

    	/** sets the serviceCluster/Collection object */
	public void setServiceCluster(ServiceCluster serviceCluster)
	{
		this.serviceCluster = serviceCluster;
	}

	/** sets the collect name */
	public void setCollectionName(String coll_name)
	{
		setClusterName(coll_name);
	}

	public void cleanUp()
	{
	}

	public void setGlobalFormat(Element globalFormat)
	{
	  _globalFormat = GSXML.duplicateWithNewName(this.desc_doc, globalFormat, GSXML.GLOBAL_FORMAT_ELEM, false);
	}

	/** sets the site home */
	public void setSiteHome(String site_home)
	{
		this.site_home = site_home;
	}

	/** sets the site http address */
	public void setSiteAddress(String site_address)
	{
		this.site_http_address = site_address;
	}

	public void setLibraryName(String library_name)
	{
		this.library_name = library_name;
	}

	public String getLibraryName()
	{
		return this.library_name;
	}

	/** sets the message router */
	public void setMessageRouter(MessageRouter m)
	{
		this.router = m;
		setLibraryName(m.getLibraryName());
	}

	/** the no-args constructor */
	public ServiceRack()
	{
		this.converter = new XMLConverter();
		this.desc_doc = XMLConverter.newDOM();
		this.short_service_info = this.desc_doc.createElement(GSXML.SERVICE_ELEM + GSXML.LIST_MODIFIER);
		this.format_info_map = new HashMap<String, Node>();
	}

	/**
	 * configure the service module
	 * 
	 * @param info
	 *            the XML node <serviceRack name="XXX"/> with name equal to the
	 *            class name (of the subclass)
	 * 
	 *            must configure short_service_info_ and service_info_map_
	 * @return true if configured ok must be implemented in subclasses
	 */
	public boolean configure(Element info)
	{
		return configure(info, null);
	}

	public boolean configure(Element info, Element extra_info)
	{
		// set up the class loader
		this.class_loader = new CollectionClassLoader(this.getClass().getClassLoader(), this.site_home, this.cluster_name);
		return true;
	}

	/**
	 * Process an XML document - convenience method that uses Strings rather
	 * than Elements. just calls process(Element).
	 * 
	 * @param xml_in
	 *            the Document to process - a string
	 * @return the resultant document as a string - contains any error messages
	 * @see String
	 */
	public String process(String xml_in)
	{

		Document doc = this.converter.getDOM(xml_in);
		if (doc == null)
		{
			logger.error("Couldn't parse request");
			logger.error(xml_in);
			return null;
		}
		Node res = process(doc);
		return this.converter.getString(res);

	}

	/**
	 * process an XML request in DOM form
	 * 
	 * @param message
	 *            the Node node containing the request should be <message>
	 * @return an Node with the result XML
	 * @see Node/Element
	 */
	public Node process(Node message_node)
	{
		Element message = GSXML.nodeToElement(message_node);

		NodeList requests = message.getElementsByTagName(GSXML.REQUEST_ELEM);
		Document result_doc = XMLConverter.newDOM();
		Element mainResult = result_doc.createElement(GSXML.MESSAGE_ELEM);
		if (requests.getLength() == 0)
		{
			// no requests
			return mainResult; // empty message for now
		}

		for (int i = 0; i < requests.getLength(); i++)
		{
			Element request = (Element) requests.item(i);

			String type = request.getAttribute(GSXML.TYPE_ATT);
			if (type.equals(GSXML.REQUEST_TYPE_DESCRIBE))
			{
			  Element response = processDescribe(request);
				if (response != null)
				{
				  mainResult.appendChild(result_doc.importNode(response, true));
				}

			}
			else if (type.equals(GSXML.REQUEST_TYPE_FORMAT))
			{
			  Element response = processFormat(request);
			  mainResult.appendChild(result_doc.importNode(response, true));
			}
			else
			{
				// other type of request, must be processed by the subclass - 
				// send to the service method
				StringBuffer error_string = new StringBuffer();
				String to = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
				Element response = null;
				try
				{
					Class c = this.getClass();
					Class[] params = { Class.forName("org.w3c.dom.Element") };

					String method_name = "process" + to;
					Method m = null;
					while (c != null)
					{

						try
						{
							m = c.getDeclaredMethod(method_name, params);
							// if this has worked, break
							break;
						}
						catch (NoSuchMethodException e)
						{
							c = c.getSuperclass();
						}
						catch (SecurityException e)
						{
							logger.error("security exception for finding method " + method_name);
							error_string.append("ServiceRack.process: security exception for finding method " + method_name);
						}
					} // while
					if (m != null)
					{
						Object[] args = { request };
						try
						{
							response = (Element) m.invoke(this, args);

						}
						catch (Exception e)
						{
							logger.error("Trying to call a processService type method (process" + to + ") on a subclass(" + this.getClass().getName() + "), but an exception happened:" + e.toString(), e);
							// for debugging, it's useful to see what's really causing this supposed "invocationTargetException"
							// When the error message is displayed in the "Net" tab in the browser's web inspector, scroll down to "Caused by"
							StringWriter errors = new StringWriter();
							e.printStackTrace(new PrintWriter(errors));
							String errStr = errors.toString();
							errStr += XMLConverter.getString(request);


							error_string.append("Trying to call a processService type method (process" + to + ") on a subclass(" + this.getClass().getName() + "), but an exception happened:" + e.toString() + "\n" + errStr );
						}
					}
					else
					{
						logger.error("method " + method_name + " not found for class " + this.getClass().getName());
						error_string.append("ServiceRack.process: method " + method_name + " not found for class " + this.getClass().getName());
					}

				}
				catch (ClassNotFoundException e)
				{
					logger.error("Element class not found");
					error_string.append("Element class not found");
				}
				if (response != null)
				{
					mainResult.appendChild(result_doc.importNode(response, true));
				}
				else
				{
					// add in a dummy response
					logger.error("adding in an error element\n");
					response = result_doc.createElement(GSXML.RESPONSE_ELEM);
					GSXML.addError(response, error_string.toString());
					mainResult.appendChild(response);

				}

			} // else process request
		} // for each request

		return mainResult;

	}

	/**
	 * process method for describe requests
	 */
	protected Element processDescribe(Element request)
	{
	  Document result_doc = XMLConverter.newDOM();
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_DESCRIBE);

		String lang = request.getAttribute(GSXML.LANG_ATT);
		String to = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));
		if (to.equals(""))
		{ // return the service list
		  response.appendChild(result_doc.importNode(getServiceList(result_doc, lang), true));
			return response;
		}
		response.setAttribute(GSXML.FROM_ATT, to);
		Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		Element description = null;
		if (param_list == null)
		{
		  description = getServiceDescription(result_doc, to, lang, null);
		}
		else
		{
			NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);
			for (int i = 0; i < params.getLength(); i++)
			{

				Element param = (Element) params.item(i);
				// Identify the structure information desired
				if (param.getAttribute(GSXML.NAME_ATT).equals(GSXML.SUBSET_PARAM))
				{
					String info = param.getAttribute(GSXML.VALUE_ATT);
					if (description == null)
					{
					  description = getServiceDescription(result_doc, to, lang, info);
					}
					else
					{
					  Element temp = getServiceDescription(result_doc, to, lang, info);
						GSXML.mergeElements(description, temp);
					}
				}
			}
		}
		if (description != null)
		{ // may be null if non-existant service
		  response.appendChild(description);
		}
		return response;

	}

	/**
	 * process method for stylesheet requests
	 */
	protected Element processFormat(Element request)
	{
	  Document result_doc = XMLConverter.newDOM();
		Element response = result_doc.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_FORMAT);

		String to = GSPath.getFirstLink(request.getAttribute(GSXML.TO_ATT));

		if (to.equals(""))
		{ // serviceRack query - is this appropriate??
			return response;
		}

		// describe a particular service	
		if (this.format_info_map.containsKey(to))
		{
		  response.appendChild(getServiceFormat(result_doc, to));
		  if (_globalFormat != null)
			{
			  response.appendChild(result_doc.importNode(this._globalFormat, true));
			}
			response.setAttribute(GSXML.FROM_ATT, to);
			return response;
		}

		// else no format info
		logger.error("ServiceRack describe request: no format info for " + to + ".");
		return response;
	}

	/** returns the service list for the subclass */
  protected Element getServiceList(Document doc, String lang)
	{
		// for now, it is static and has no language stuff
	  return (Element)doc.importNode(this.short_service_info, true);
	}

	/** returns a specific service description */
  abstract protected Element getServiceDescription(Document doc, String service, String lang, String subset);

  protected Element getServiceFormat(Document doc, String service)
	{
	  Element format = (Element) this.format_info_map.get(service);
	  if (format != null) {
	    return (Element)doc.importNode(format, true);
	  }
	  return null;
	}


	/**
	 * Returns the appropriate language element from a display elem, display is
	 * the containing element, name is the name of the element to look for, lang
	 * is the preferred language, lang_default is the fall back lang, if neither
	 * lang is found will return the first one it finds
	 */
  public String getDisplayText(Element display, String name, String lang, String lang_default) {
    return getDisplayText(display, name, lang, lang_default, null);
  }

  public String getDisplayText(Element display, String name, String lang, String lang_default, String dictionary_name) {
	

		String def = null;
		String first = null;
		String key = null;
		NodeList elems = display.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
		if (elems.getLength() == 0)
			return "";
		for (int i = 0; i < elems.getLength(); i++)
		{
			Element e = (Element) elems.item(i);
			String n = e.getAttribute(GSXML.NAME_ATT);
			if (name.equals(n))
			{
				String l = e.getAttribute(GSXML.LANG_ATT);
				String k = e.getAttribute(GSXML.KEY_ATT);
				// if we have a specific lang value, return that
				if (!l.equals("") && lang.equals(l))
				{
					return GSXML.getNodeText(e);
				}
				else if (!l.equals("") && lang_default.equals(l))
				{
					def = GSXML.getNodeText(e);
				}
				else if (!k.equals("")) {
				  if ( key == null) {
				  // a key specified. only allowed one spec with key
				    key = k;
				  }
				}
				// we have no key and we don't match the languages
				else if (first == null)
				{
				  // but we are the first one, so we remember the value
					first = GSXML.getNodeText(e);
				}
			}
			else
			{
				continue;
			}
		}

		
		if (key != null) {
		  String s = getTextString(key, lang, dictionary_name);
		  // only use this one if a value was actually found
		  if (s!= null) {
		    return s;
		  }
		  //if (!s.equals( "_"+key +"_")) {
		  //  return s;
		  //}
		}

		if (def != null)
		{
			return def;
		}
		if (first != null)
		{
			return first;
		}
		return "";
	}

	/** overloaded version for no args case */
	protected String getTextString(String key, String lang)
	{
		return getTextString(key, lang, null, null);
	}

	protected String getTextString(String key, String lang, String dictionary)
	{
		return getTextString(key, lang, dictionary, null);
	}

	protected String getTextString(String key, String lang, String[] args)
	{
		return getTextString(key, lang, null, args);
	}

	/**
	 * getTextString - retrieves a language specific text string for the given
	 * key and locale, from the specified resource_bundle (dictionary)
	 */
	protected String getTextString(String key, String lang, String dictionary, String[] args)
	{

		// we want to use the collection class loader in case there are coll specific files
		if (dictionary != null)
		{
			// just try the one specified dictionary
			Dictionary dict = new Dictionary(dictionary, lang, this.class_loader);
			String result = dict.get(key, args);
			if (result == null)
			{ // not found
			  //return "_" + key + "_";
			  return null;
			}
			return result;
		}

		// now we try class names for dictionary names
		String original_class_name = this.getClass().getName();
		original_class_name = original_class_name.substring(original_class_name.lastIndexOf('.') + 1);
		Dictionary dict = new Dictionary(original_class_name, lang, this.class_loader);
		String result = dict.get(key, args);
		if (result != null)
		{
			return result;
		}

		// we have to try super classes
		String class_name;
		Class c = this.getClass().getSuperclass();
		while (result == null && c != null)
		{
			class_name = c.getName();
			class_name = class_name.substring(class_name.lastIndexOf('.') + 1);
			if (class_name.equals("ServiceRack"))
			{
				// this is as far as we go
				break;
			}
			dict = new Dictionary(class_name, lang, this.class_loader);
			result = dict.get(key, args);
			c = c.getSuperclass();
		}
		if (result == null)
		{
		  // try the ServiceRack properties
		  // at the moment we look for original_class_name.key, then just key
		  // if there is lots of inheritance, may want to try down the list of superclasses too...?
		  class_name = "ServiceRack";
		  dict = new Dictionary(class_name, lang, this.class_loader);
		  String full_key = original_class_name+"."+key;
		  result = dict.get(full_key, args);
		  if(result == null) {
		    result = dict.get(key, args);
		  }
		  if(result == null) {
		    //return "_" + key + "_";
		    return null;
		  }
		}
		return result;
	
	}

	protected String getMetadataNameText(String key, String lang)
	{

		String properties_name = "metadata_names";
		Dictionary dict = new Dictionary(properties_name, lang);

		String result = dict.get(key);
		if (result == null)
		{ // not found
			return null;
		}
		return result;
	}
}
