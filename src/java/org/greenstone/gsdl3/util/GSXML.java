/*
 *    GSXML.java
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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;

//import java.util.Locale;

import org.apache.log4j.*;

/** various functions for extracting info out of GS XML */
public class GSXML
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSXML.class.getName());

	// greenstone xml elements
	public static final String MESSAGE_ELEM = "message";
	public static final String REQUEST_ELEM = "request";
	public static final String RESPONSE_ELEM = "response";
	public static final String COLLECTION_ELEM = "collection";
	public static final String SERVICE_ELEM = "service";
	public static final String CLUSTER_ELEM = "serviceCluster";
	public static final String SITE_ELEM = "site";
	public static final String PARAM_ELEM = "param";
	public static final String PARAM_OPTION_ELEM = "option";
	public static final String CONTENT_ELEM = "content";
	public static final String RESOURCE_ELEM = "resource";
	public static final String DOCUMENT_ELEM = "document";
	public static final String METADATA_ELEM = "metadata";
	public static final String SERVICE_CLASS_ELEM = "serviceRack";
	public static final String CLASSIFIER_ELEM = "classifier";
	public static final String APPLET_ELEM = "applet";
	public static final String APPLET_DATA_ELEM = "appletData";
	public static final String CONFIGURE_ELEM = "configure";
	public static final String STATUS_ELEM = "status";
	public static final String ERROR_ELEM = "error";
	public static final String DEFAULT_ELEM = "default";
	public static final String STYLESHEET_ELEM = "format";//"stylesheet"; // any additional stylesheet stuff is carried in the message inside this elem
	public static final String FORMAT_ELEM = "format"; // config files use format - should we use this instead of stylesheet??
	public static final String TERM_ELEM = "term";
	public static final String STOPWORD_ELEM = "stopword";
	public static final String SYSTEM_ELEM = "system";
	public static final String FORMAT_STRING_ELEM = "formatString";

	//config file elems
	public static final String COLLECTION_CONFIG_ELEM = "collectionConfig";
	public static final String COLLECTION_BUILD_ELEM = "buildConfig";
	public static final String COLLECTION_INIT_ELEM = "collectionInit";
	public static final String RECOGNISE_ELEM = "recognise";
	public static final String DOC_TYPE_ELEM = "docType";
	public static final String SEARCH_ELEM = "search";
	public static final String INFODB_ELEM = "infodb";
	public static final String INDEX_ELEM = "index";
	public static final String INDEX_STEM_ELEM = "indexStem";
	public static final String INDEX_OPTION_ELEM = "indexOption";
	public static final String BROWSE_ELEM = "browse";
	public static final String DISPLAY_ELEM = "display";
	public static final String LEVEL_ELEM = "level";

	public static final String DBINFO_ELEM = "dbInfo";
	public static final String DBNAME_ATT = "dbname";
	public static final String DBPATH_ATT = "dbpath";
	public static final String SQLSTATE_ATT = "sqlstate";
	public static final String DATABASE_TYPE_ELEM = "databaseType";
	public static final String SHORTNAME_ATT = "shortname";
	public static final String NOTIFY_ELEM = "notify";
	public static final String NOTIFY_HOST_ATT = "host";

	//doc.xml file elems
	public static final String DOCXML_SECTION_ELEM = "Section";
	public static final String DOCXML_DESCRIPTION_ELEM = "Description";
	public static final String DOCXML_METADATA_ELEM = "Metadata";
	public static final String DOCXML_CONTENT_ELEM = "Content";

	// elems for the pages to be processed by xslt
	public final static String PAGE_ELEM = "page";
	public final static String CONFIGURATION_ELEM = "config";
	public final static String PAGE_REQUEST_ELEM = "pageRequest";
	public final static String PAGE_RESPONSE_ELEM = "pageResponse";
	public final static String PAGE_EXTRA_ELEM = "pageExtra";

	//public final static String DESCRIPTION_ELEM = "description";

	public static final String ACTION_ELEM = "action";
	public static final String SUBACTION_ELEM = "subaction";

	// add on to another elem type to get a list of that type
	public static final String LIST_MODIFIER = "List";

	// greenstone xml attributes
	public static final String COLLECTION_ATT = "collection";
	public static final String NAME_ATT = "name";
	public static final String TO_ATT = "to";
	public static final String USER_ID_ATT = "uid";
	public static final String FROM_ATT = "from";
	public static final String LANG_ATT = "lang";
	public static final String TYPE_ATT = "type";
	public static final String DB_TYPE_ATT = "dbType";
	public static final String VALUE_ATT = "value";
	public static final String DEFAULT_ATT = "default";
	public static final String INFO_ATT = "info";
	public static final String ACTION_ATT = "action";
	public static final String SUBACTION_ATT = "subaction";
	public static final String OUTPUT_ATT = "output";
	public static final String ADDRESS_ATT = "address";
	public static final String LOCAL_SITE_ATT = "localSite";
	public static final String LOCAL_SITE_NAME_ATT = "localSiteName";
	public static final String STATUS_ERROR_CODE_ATT = "code";
	public static final String STATUS_PROCESS_ID_ATT = "pid";
	public static final String PARAM_SHORTNAME_ATT = "shortname";
	public static final String PARAM_IGNORE_POS_ATT = "ignore";
	public static final String CLASSIFIER_CONTENT_ATT = "content";
	public static final String ERROR_TYPE_ATT = "type";
	public static final String COLLECT_TYPE_ATT = "ct";
	public static final String HIDDEN_ATT = "hidden";

	// document stuff
	public static final String DOC_TYPE_ATT = "docType";
	public static final String DOC_NODE_ELEM = "documentNode";
	public static final String NODE_CONTENT_ELEM = "nodeContent";
	public static final String NODE_STRUCTURE_ELEM = "nodeStructure";
	public static final String NODE_ID_ATT = "nodeID";
	public static final String NODE_OID = "oid";
	public static final String NODE_NAME_ATT = "nodeName";
	public static final String NODE_TYPE_ATT = "nodeType";
	public static final String NODE_RANK_ATT = "rank";
	public static final String NODE_TYPE_ROOT = "root";
	public static final String NODE_TYPE_INTERNAL = "internal";
	public static final String NODE_TYPE_LEAF = "leaf";

	public static final String DOC_TYPE_SIMPLE = "simple";
	public static final String DOC_TYPE_PAGED = "paged";
	public static final String DOC_TYPE_HIERARCHY = "hierarchy";

	public static final String SESSION_EXPIRATION = "session_expiration";
	public static final String USER_SESSION_CACHE_ATT = "user_session_cache";

	// classifier stuff
	public static final String CLASS_NODE_ELEM = "classifierNode";
	public static final String CLASS_NODE_ORIENTATION_ATT = "orientation";

	// parameter types
	public static final String PARAM_TYPE_INTEGER = "integer";
	public static final String PARAM_TYPE_BOOLEAN = "boolean";
	public static final String PARAM_TYPE_ENUM_START = "enum";
	public static final String PARAM_TYPE_ENUM_SINGLE = "enum_single";
	public static final String PARAM_TYPE_ENUM_MULTI = "enum_multi";
	public static final String PARAM_TYPE_STRING = "string";
	public static final String PARAM_TYPE_TEXT = "text";
	public static final String PARAM_TYPE_MULTI = "multi";
	public static final String PARAM_TYPE_FILE = "file";
	public static final String PARAM_TYPE_INVISIBLE = "invisible";
	// stuff for text strings
	public static final String DISPLAY_TEXT_ELEM = "displayItem";
	// the following are used for the name attributes
	public static final String DISPLAY_TEXT_NAME = "name";
	public static final String DISPLAY_TEXT_SUBMIT = "submit";
	public static final String DISPLAY_TEXT_DESCRIPTION = "description";

	// request types
	// get the module description
	public static final String REQUEST_TYPE_DESCRIBE = "describe";
	// startup a process
	public static final String REQUEST_TYPE_PROCESS = "process";
	// get the status of an ongoing process
	public static final String REQUEST_TYPE_STATUS = "status";
	// system type request - eg reload a collection
	public static final String REQUEST_TYPE_SYSTEM = "system";
	// page requests to the Receptionist/Actions
	public static final String REQUEST_TYPE_PAGE = "page"; // used to be cgi
	// get any format info for a service
	public static final String REQUEST_TYPE_FORMAT = "format";
	// modify the requests
	public static final String REQUEST_TYPE_MESSAGING = "messaging";
	// save the format string
	public static final String REQUEST_TYPE_FORMAT_STRING = "formatString";
	// check credentials
	public static final String REQUEST_TYPE_SECURITY = "security";

	// service types
	public static final String SERVICE_TYPE_QUERY = "query";
	public static final String SERVICE_TYPE_RETRIEVE = "retrieve";
	public static final String SERVICE_TYPE_BROWSE = "browse";
	public static final String SERVICE_TYPE_APPLET = "applet";
	public static final String SERVICE_TYPE_PROCESS = "process";
	public static final String SERVICE_TYPE_ENRICH = "enrich";
	public static final String SERVICE_TYPE_OAI = "oai";
	public static final String FLAX_PAGE = "flaxPage";
	public static final String FLAX_PAGE_GENERATION = "FlaxPageGeneration";

	// system command types and attributes
	public static final String SYSTEM_TYPE_CONFIGURE = "configure";
	public static final String SYSTEM_TYPE_ACTIVATE = "activate";
	public static final String SYSTEM_TYPE_DEACTIVATE = "deactivate";

	public static final String SYSTEM_SUBSET_ATT = "subset";
	public static final String SYSTEM_MODULE_TYPE_ATT = "moduleType";
	public static final String SYSTEM_MODULE_NAME_ATT = "moduleName";

	// communicator types
	public static final String COMM_TYPE_SOAP_JAVA = "soap";

	// error types
	public static final String ERROR_TYPE_SYNTAX = "syntax";
	public static final String ERROR_TYPE_SYSTEM = "system";
	public static final String ERROR_TYPE_INVALID_ID = "invalid_id";
	public static final String ERROR_TYPE_OTHER = "other";

	// some system wide param names
	public static final String SUBSET_PARAM = "subset";

	//for plugin
	public static final String PLUGIN_ELEM = "plugin";
	public static final String IMPORT_ELEM = "import";

	//for authentication
	public static final String AUTHEN_NODE_ELEM = "authenticationNode";
	public static final String USER_NODE_ELEM = "userNode";

	//for configure action results
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";

	//security tags and attributes
	public static final String SECURITY_ELEM = "security";
	public static final String SCOPE_ATT = "scope";
	public static final String DEFAULT_ACCESS_ATT = "default_access";
	public static final String EXCEPTION_ELEM = "exception";
	public static final String DOCUMENT_SET_ELEM = "documentSet";
	public static final String GROUP_ELEM = "group";
	public static final String MATCH_ELEM = "match";
	public static final String FIELD_ATT = "field";
	public static final String USER_INFORMATION_ELEM = "userInformation";
	public static final String USERNAME_ATT = "username";
	public static final String GROUPS_ATT = "groups";
	public static final String BASE_URL = "baseURL";

	/**
	 * takes a list of elements, and returns an array of strings of the values
	 * of attribute att_name
	 */
	public static String[] getAttributeValuesFromList(Element list, String att_name)
	{

		NodeList children = list.getChildNodes();

		int num_nodes = children.getLength();
		String[] ids = new String[num_nodes];
		for (int i = 0; i < num_nodes; i++)
		{
			Element e = (Element) children.item(i);
			String id = e.getAttribute(att_name);
			ids[i] = id;
		}

		return ids;
	}

	public static HashMap extractParams(Element xml, boolean deep)
	{
		return extractParams(xml, deep, null);
	}

	/**
	 * takes a paramList element, and gets a HashMap of name-value pairs if
	 * deep=true, extracts embedded params, otherwise just top level params
	 */
	public static HashMap extractParams(Element xml, boolean deep, String toFind)
	{

		if (!xml.getNodeName().equals(PARAM_ELEM + LIST_MODIFIER))
		{
			logger.error("paramList element should have been passed to extractParams, instead it was " + xml.getNodeName());
			return null;
		}

		NodeList params = null;
		if (deep)
		{ // get all the nested ones
			params = xml.getElementsByTagName(PARAM_ELEM);
		}
		else
		{ // just get the top  level ones
			params = xml.getChildNodes();
		}
		HashMap param_map = new HashMap();
		for (int i = 0; i < params.getLength(); i++)
		{
			if (params.item(i).getNodeName().equals(PARAM_ELEM))
			{
				Element param = (Element) params.item(i);
				String name = param.getAttribute(NAME_ATT);
				String value = getValue(param); //att or content

				// For only one parameter
				if (toFind != null && name.equals(toFind))
				{
					param_map.put(name, value);
					return param_map;
				}
				else if (toFind != null)
					continue;

				int pos = name.indexOf('.');
				if (pos == -1)
				{ // a base param
					param_map.put(name, value);
				}
				else
				{ // a namespaced param

					String namespace = name.substring(0, pos);
					name = name.substring(pos + 1);
					HashMap map = (HashMap) param_map.get(namespace);
					if (map == null)
					{
						map = new HashMap();
						param_map.put(namespace, map);
					}
					map.put(name, value);
				}
			}
		}
		return param_map;
	}

	/** gets the value att or the text content */
	public static String getValue(Element e)
	{
		String val = e.getAttribute(VALUE_ATT);
		if (val == null || val.equals(""))
		{
			// have to get it out of the text
			val = getNodeText(e);

		}
		else
		{
			// unescape the xml stuff
			val = unXmlSafe(val);
		}
		return val;
	}

	/** extracts the text out of a node */
	public static Node getNodeTextNode(Element param)
	{
		param.normalize();
		Node n = param.getFirstChild();
		while (n != null && n.getNodeType() != Node.TEXT_NODE)
		{
			n = n.getNextSibling();
		}
		return n;
	}

	/** extracts the text out of a node */
	public static String getNodeText(Element param)
	{
		Node text_node = getNodeTextNode(param);
		if (text_node == null)
		{
			return "";
		}
		return text_node.getNodeValue();
	}

	public static void setNodeText(Element elem, String text)
	{
		Node old_text_node = getNodeTextNode(elem);
		if (old_text_node != null)
		{
			elem.removeChild(old_text_node);
		}
		Text t = elem.getOwnerDocument().createTextNode(text);
		elem.appendChild(t);
	}

	/** add text to a document/subsection element */
	public static boolean addDocText(Document owner, Element doc, String text)
	{

		Element content = owner.createElement(NODE_CONTENT_ELEM);
		Text t = owner.createTextNode(text);
		content.appendChild(t);
		doc.appendChild(content);
		return true;
	}

	/** add an error message, unknown error type */
	public static boolean addError(Document owner, Element doc, String text)
	{
		return addError(owner, doc, text, ERROR_TYPE_OTHER);
	}

	/** add an error message */
	public static boolean addError(Document owner, Element doc, String text, String error_type)
	{

		Element content = owner.createElement(ERROR_ELEM);
		content.setAttribute(ERROR_TYPE_ATT, error_type);
		Text t = owner.createTextNode(text);
		content.appendChild(t);
		doc.appendChild(content);
		return true;
	}

	/** add an error message */
	public static boolean addError(Document owner, Element doc, Throwable error)
	{
		return addError(owner, doc, error, ERROR_TYPE_OTHER);
	}

	/** add an error message */
	public static boolean addError(Document owner, Element doc, Throwable error, String error_type)
	{
		error.printStackTrace();
		return addError(owner, doc, error.toString(), error_type);
	}

	public static Element createMetadataParamList(Document owner, Vector meta_values)
	{

		Element meta_param_list = owner.createElement(PARAM_ELEM + LIST_MODIFIER);
		Iterator i = meta_values.iterator();
		while (i.hasNext())
		{
			String next = (String) i.next();
			Element meta_param = owner.createElement(PARAM_ELEM);
			meta_param_list.appendChild(meta_param);
			meta_param.setAttribute(NAME_ATT, "metadata");
			meta_param.setAttribute(VALUE_ATT, next);
		}
		return meta_param_list;
	}

	/** adds a metadata elem to a list */
	public static boolean addMetadata(Document owner, Element list, String meta_name, String meta_value)
	{
		if (meta_value == null || meta_value.equals(""))
		{
			return false;
		}
		Element data = owner.createElement(METADATA_ELEM);
		data.setAttribute(NAME_ATT, meta_name);
		Text t = owner.createTextNode(meta_value);
		data.appendChild(t);
		list.appendChild(data);
		return true;

	}

	/**
	 * copies the metadata out of the metadataList of 'from' into the
	 * metadataList of 'to'
	 */
	public static boolean mergeMetadataLists(Node to, Node from)
	{
		Node from_meta = getChildByTagName(from, METADATA_ELEM + LIST_MODIFIER);
		if (from_meta == null)
		{ // nothing to copy
			return true;
		}
		return mergeMetadataFromList(to, from_meta);
	}

	/**
	 * copies the metadata out of the meta_list metadataList into the
	 * metadataList of 'to'
	 */
	public static boolean mergeMetadataFromList(Node to, Node meta_list)
	{
		if (meta_list == null)
			return false;
		Node to_meta = getChildByTagName(to, METADATA_ELEM + LIST_MODIFIER);
		Document to_owner = to.getOwnerDocument();
		if (to_meta == null)
		{
			to.appendChild(to_owner.importNode(meta_list, true));
			return true;
		}
		// copy individual metadata elements
		NodeList meta_items = ((Element) meta_list).getElementsByTagName(METADATA_ELEM);
		for (int i = 0; i < meta_items.getLength(); i++)
		{
			to_meta.appendChild(to_owner.importNode(meta_items.item(i), true));
		}
		return true;
	}

	/** copies all the children from from to to */
	public static boolean mergeElements(Element to, Element from)
	{

		Document owner = to.getOwnerDocument();
		Node child = from.getFirstChild();
		while (child != null)
		{
			to.appendChild(owner.importNode(child, true));
			child = child.getNextSibling();
		}
		return true;
	}

	/** returns the (first) element child of the node n */
	public static Element getFirstElementChild(Node n)
	{

		Node child = n.getFirstChild();
		while (child != null)
		{
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				return (Element) child;
			}
			child = child.getNextSibling();
		}
		return null; //no element child found
	}

	/** returns the (first) child element with the given name */
	public static Node getChildByTagName(Node n, String name)
	{
		if (n != null)
		{ // this line is an attempted solution to the NullPointerException mentioned 
			// in trac bug ticket #225. If n is null can't do n.getFirstChild() below. As per bug #225: 
			// GSXML.getNodeByPath() is called by GS2BrowseAction, which then calls this method.
			// If n is null, null will be returned which GS2BrowseAction already checks for. It's here
			// that the NullPointerException was thrown.

			Node child = n.getFirstChild();
			while (child != null)
			{
				if (child.getNodeName().equals(name))
				{
					return child;
				}
				child = child.getNextSibling();
			}
		}
		return null; //not found
	}

	/**
	 * returns the (nth) child element with the given name index numbers start
	 * at 0
	 */
	public static Node getChildByTagNameIndexed(Node n, String name, int index)
	{
		if (index == -1)
		{
			return getChildByTagName(n, name);
		}
		int count = 0;
		Node child = n.getFirstChild();
		while (child != null)
		{
			if (child.getNodeName().equals(name))
			{
				if (count == index)
				{
					return child;
				}
				else
				{
					count++;
				}
			}
			child = child.getNextSibling();
		}
		return null; //not found
	}

	/**
	 * takes an xpath type expression of the form name/name/... and returns the
	 * first node that matches, or null if not found
	 */
	public static Node getNodeByPath(Node n, String path)
	{

		String link = GSPath.getFirstLink(path);
		path = GSPath.removeFirstLink(path);
		while (!link.equals(""))
		{
			n = getChildByTagName(n, link);
			if (n == null)
			{
				return null;
			}
			link = GSPath.getFirstLink(path);
			path = GSPath.removeFirstLink(path);
		}
		return n;
	}

	/**
	 * takes an xpath type expression of the form name/name/... and returns the
	 * first node that matches, or null if not found can include [i] indices.
	 * index numbers start at 0
	 */
	public static Node getNodeByPathIndexed(Node n, String path)
	{

		String link = GSPath.getFirstLink(path);
		int index = GSPath.getIndex(link);
		if (index != -1)
		{
			link = GSPath.removeIndex(link);
		}
		path = GSPath.removeFirstLink(path);
		while (!link.equals(""))
		{
			n = getChildByTagNameIndexed(n, link, index);
			if (n == null)
			{
				return null;
			}
			link = GSPath.getFirstLink(path);
			index = GSPath.getIndex(link);
			if (index != -1)
			{
				link = GSPath.removeIndex(link);
			}
			path = GSPath.removeFirstLink(path);
		}
		return n;
	}

	public static HashMap getChildrenMap(Node n)
	{

		HashMap map = new HashMap();
		Node child = n.getFirstChild();
		while (child != null)
		{
			String name = child.getNodeName();
			map.put(name, child);
			child = child.getNextSibling();
		}
		return map;
	}

	public static NodeList getChildrenByTagName(Node n, String name)
	{
		MyNodeList node_list = new MyNodeList();
		Node child = n.getFirstChild();
		while (child != null)
		{
			if (child.getNodeName().equals(name))
			{
				node_list.addNode(child);
			}
			child = child.getNextSibling();
		}
		return node_list;
	}

	/** Duplicates an element, but gives it a new name */
	public static Element duplicateWithNewName(Document owner, Element element, String element_name, boolean with_attributes)
	{
		return duplicateWithNewNameNS(owner, element, element_name, null, with_attributes);
	}

	/** Duplicates an element, but gives it a new name */
	public static Element duplicateWithNewNameNS(Document owner, Element element, String element_name, String namespace_uri, boolean with_attributes)
	{
		Element duplicate;
		if (namespace_uri == null)
		{
			duplicate = owner.createElement(element_name);
		}
		else
		{
			duplicate = owner.createElementNS(namespace_uri, element_name);
		}
		// Copy element attributes
		if (with_attributes)
		{
			NamedNodeMap attributes = element.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				duplicate.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
			}
		}

		// Copy element children
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			duplicate.appendChild(owner.importNode(child, true));
		}

		return duplicate;
	}

	public static void copyAllChildren(Element to, Element from)
	{

		Document to_doc = to.getOwnerDocument();
		Node child = from.getFirstChild();
		while (child != null)
		{
			to.appendChild(to_doc.importNode(child, true));
			child = child.getNextSibling();
		}
	}

	/** returns a basic request message */
	public static Element createBasicRequest(Document owner, String request_type, String to, UserContext userContext)
	{
		Element request = owner.createElement(REQUEST_ELEM);
		request.setAttribute(TYPE_ATT, request_type);
		request.setAttribute(LANG_ATT, userContext._lang);
		request.setAttribute(TO_ATT, to);
		request.setAttribute(USER_ID_ATT, userContext._userID);
		return request;
	}

	public static Element createBasicResponse(Document owner, String from)
	{
		Element response = owner.createElement(GSXML.RESPONSE_ELEM);
		response.setAttribute(GSXML.FROM_ATT, from);
		response.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
		return response;
	}

	public static Element createMetadataElement(Document owner, String name, String value)
	{
		Element metaElem = owner.createElement(GSXML.METADATA_ELEM);
		metaElem.setAttribute(GSXML.NAME_ATT, name);
		metaElem.setAttribute(GSXML.VALUE_ATT, value);
		return metaElem;
	}

	public static Element createTextElement(Document owner, String elem_name, String text)
	{
		Element e = owner.createElement(elem_name);
		Text t = owner.createTextNode(text);
		e.appendChild(t);
		return e;

	}

	public static Element createTextElement(Document owner, String elem_name, String text, String att_name, String att_value)
	{
		Element e = owner.createElement(elem_name);
		e.setAttribute(att_name, att_value);
		Text t = owner.createTextNode(text);
		e.appendChild(t);
		return e;

	}

	public static Element createDisplayTextElement(Document owner, String text_name, String text)
	{
		Element e = owner.createElement(DISPLAY_TEXT_ELEM);
		e.setAttribute(NAME_ATT, text_name);
		Text t = owner.createTextNode(text);
		e.appendChild(t);
		return e;

	}

	public static Element createParameter(Document owner, String name, String value)
	{
		Element param = owner.createElement(PARAM_ELEM);
		param.setAttribute(NAME_ATT, name);
		param.setAttribute(VALUE_ATT, value);
		return param;
	}

	public static void addParametersToList(Document owner, Element param_list, HashMap params)
	{
		if (params == null)
		{
			return;
		}

		Set items = params.entrySet();
		Iterator i = items.iterator();
		while (i.hasNext())
		{
			Map.Entry m = (Map.Entry) i.next();
			param_list.appendChild(createParameter(owner, (String) m.getKey(), (String) m.getValue()));
		}

	}

	public static Element createParameterDescription(Document owner, String id, String display_name, String type, String default_value, String[] option_ids, String[] option_names)
	{

		Element p = owner.createElement(PARAM_ELEM);
		p.setAttribute(NAME_ATT, id);
		p.setAttribute(TYPE_ATT, type);
		p.appendChild(createDisplayTextElement(owner, GSXML.DISPLAY_TEXT_NAME, display_name));

		if (default_value != null)
		{
			p.setAttribute(DEFAULT_ATT, default_value);
		}
		if (option_ids != null && option_names != null)
		{
			for (int i = 0; i < option_ids.length; i++)
			{
				Element e = owner.createElement(PARAM_OPTION_ELEM);
				e.setAttribute(NAME_ATT, option_ids[i]);
				e.appendChild(createDisplayTextElement(owner, GSXML.DISPLAY_TEXT_NAME, option_names[i]));
				p.appendChild(e);
			}
		}
		return p;
	}

	public static Element createParameterDescription2(Document owner, String id, String display_name, String type, String default_value, ArrayList option_ids, ArrayList option_names)
	{

		Element p = owner.createElement(PARAM_ELEM);
		p.setAttribute(NAME_ATT, id);
		p.setAttribute(TYPE_ATT, type);
		p.appendChild(createDisplayTextElement(owner, GSXML.DISPLAY_TEXT_NAME, display_name));
		if (default_value != null)
		{
			p.setAttribute(DEFAULT_ATT, default_value);
		}
		if (option_ids != null && option_names != null)
		{
			for (int i = 0; i < option_ids.size(); i++)
			{
				Element e = owner.createElement(PARAM_OPTION_ELEM);
				e.setAttribute(NAME_ATT, (String) option_ids.get(i));
				e.appendChild(createDisplayTextElement(owner, GSXML.DISPLAY_TEXT_NAME, (String) option_names.get(i)));
				p.appendChild(e);
			}
		}
		return p;
	}

	/** returns the element parent/node_name[@attribute_name='attribute_value'] */
	public static Element getNamedElement(Element parent, String node_name, String attribute_name, String attribute_value)
	{

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeName().equals(node_name))
			{
				if (((Element) child).getAttribute(attribute_name).equals(attribute_value))
					return (Element) child;
			}
		}
		// not found
		return null;
	}

	/**
	 * returns a NodeList of elements:
	 * ancestor/node_name[@attribute_name='attribute_value']
	 */
	public static NodeList getNamedElements(Element ancestor, String node_name, String attribute_name, String attribute_value)
	{
		MyNodeList node_list = new MyNodeList();
		NodeList children = ancestor.getElementsByTagName(node_name);

		if (children != null && children.getLength() > 0)
		{

			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				if (child.getNodeName().equals(node_name))
				{
					if (((Element) child).getAttribute(attribute_name).equals(attribute_value))
						node_list.addNode(child);
				}
			}
		}
		return node_list;
	}

	public static int SORT_TYPE_STRING = 0;
	public static int SORT_TYPE_INT = 1;
	public static int SORT_TYPE_FLOAT = 2;

	// sort type:
	public static Element insertIntoOrderedList(Element parent_node, String node_name, Element start_from_elem, Element new_elem, String sort_att, boolean descending)
	{
		if (new_elem == null)
			return null;
		Element cloned_elem = (Element) parent_node.getOwnerDocument().importNode(new_elem, true);
		if (start_from_elem == null)
		{
			parent_node.appendChild(cloned_elem);
			return cloned_elem;
		}

		Node current_node = start_from_elem;
		String insert_att = cloned_elem.getAttribute(sort_att);
		String list_att = start_from_elem.getAttribute(sort_att);
		while ((!descending && list_att.compareTo(insert_att) < 0) || (descending && list_att.compareTo(insert_att) > 0))
		{
			current_node = current_node.getNextSibling();
			if (current_node == null)
				break; // end of the list
			if (!current_node.getNodeName().equals(node_name))
			{
				continue; // not a valid node
			}
			list_att = ((Element) current_node).getAttribute(sort_att);
		}

		parent_node.insertBefore(cloned_elem, current_node);
		return cloned_elem;
	}

	/**
	 * Returns the appropriate language element from a display elem, display is
	 * the containing element, name is the name of the element to look for, lang
	 * is the preferred language, lang_default is the fall back lang if neither
	 * lang is found, will return the first one it finds
	 */
	public static String getDisplayText(Element display, String name, String lang, String lang_default)
	{

		String def = null;
		String first = null;
		NodeList elems = display.getElementsByTagName(DISPLAY_TEXT_ELEM);
		if (elems.getLength() == 0)
			return "";
		for (int i = 0; i < elems.getLength(); i++)
		{
			Element e = (Element) elems.item(i);
			String n = e.getAttribute(NAME_ATT);
			if (name.equals(n))
			{
				String l = e.getAttribute(LANG_ATT);
				if (lang.equals(l))
				{
					return getNodeText(e);
				}
				else if (lang_default.equals(l))
				{
					def = getNodeText(e);
				}
				else if (first == null)
				{
					first = getNodeText(e);
				}
			}
			else
			{
				continue;
			}
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

	// replaces < > " ' & in the original with their entities
	public static String xmlSafe(String original)
	{

		StringBuffer filtered = new StringBuffer(original.length());
		char c;
		for (int i = 0; i < original.length(); i++)
		{
			c = original.charAt(i);
			if (c == '>')
			{
				filtered.append("&gt;");
			}
			else if (c == '<')
			{
				filtered.append("&lt;");
			}
			else if (c == '"')
			{
				filtered.append("&quot;");
			}
			else if (c == '&')
			{
				filtered.append("&amp;");
			}
			else if (c == '\'')
			{
				filtered.append("&apos;");
			}
			else
			{
				filtered.append(c);
			}
		}
		return filtered.toString();
	}

	// replaces < > " ' & entities with their originals
	public static String unXmlSafe(String original)
	{

		StringBuffer filtered = new StringBuffer(original.length());
		char c;
		for (int i = 0; i < original.length(); i++)
		{
			c = original.charAt(i);
			if (c == '&')
			{
				int pos = original.indexOf(";", i);
				String entity = original.substring(i + 1, pos);
				if (entity.equals("gt"))
				{
					filtered.append(">");
				}
				else if (entity.equals("lt"))
				{
					filtered.append("<");
				}
				else if (entity.equals("apos"))
				{
					filtered.append("'");
				}
				else if (entity.equals("amp"))
				{
					filtered.append("&");
				}
				else if (entity.equals("quot"))
				{
					filtered.append("\"");
				}
				else
				{
					filtered.append("&" + entity + ";");
				}
				i = pos;
			}
			else
			{
				filtered.append(c);
			}
		}
		return filtered.toString();
	}

	public static void printXMLNode(Node e, boolean printText)
	{
		printXMLNode(e, 0, printText);
	}

	public static String xmlNodeToString(Node e)
	{
		StringBuffer sb = new StringBuffer("");
		xmlNodeToString(sb, e, 0, true);
		return sb.toString();
	}

	public static String xmlNodeToString(Node e, boolean printText)
	{
		StringBuffer sb = new StringBuffer("");
		xmlNodeToString(sb, e, 0, printText);
		return sb.toString();
	}

	private static void xmlNodeToString(StringBuffer sb, Node e, int depth, boolean printText)
	{
		if(e == null)
		{
			return;
		}
		
		for (int i = 0; i < depth; i++)
			sb.append(' ');

		if (e.getNodeType() == Node.TEXT_NODE)
		{
			if (printText)
			{
				sb.append(e.getNodeValue());
			}
			else
			{
				sb.append("text");
			}
			return;
		}

		sb.append('<');
		sb.append(e.getNodeName());
		NamedNodeMap attrs = e.getAttributes();
		if (attrs != null)
		{
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				sb.append(' ');
				sb.append(attr.getNodeName());
				sb.append("=\"");
				sb.append(attr.getNodeValue());
				sb.append('"');
			}
		}

		NodeList children = e.getChildNodes();

		if (children == null || children.getLength() == 0)
			sb.append("/>\n");
		else
		{

			sb.append(">\n");

			int len = children.getLength();
			for (int i = 0; i < len; i++)
			{
				xmlNodeToString(sb, children.item(i), depth + 1, printText);
			}

			for (int i = 0; i < depth; i++)
				sb.append(' ');

			sb.append("</" + e.getNodeName() + ">\n");
		}

	}

	public static void printXMLNode(Node e, int depth, boolean printText)
	{ //recursive method call using DOM API...

		if (e == null)
		{
			return;
		}

		for (int i = 0; i < depth; i++)
			System.out.print(' ');

		if (e.getNodeType() == Node.TEXT_NODE)
		{
			if (printText)
			{
				System.out.println(e.getNodeValue());
			}
			else
			{
				System.out.println("text");
			}
			return;
		}

		System.out.print('<');
		System.out.print(e.getNodeName());
		NamedNodeMap attrs = e.getAttributes();

		if (attrs != null)
		{
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				System.out.print(' ');
				System.out.print(attr.getNodeName());
				System.out.print("=\"");
				System.out.print(attr.getNodeValue());
				System.out.print('"');
			}
		}

		NodeList children = e.getChildNodes();

		if (children == null || children.getLength() == 0)
			System.out.println("/>");
		else
		{

			System.out.println('>');

			int len = children.getLength();
			for (int i = 0; i < len; i++)
			{
				printXMLNode(children.item(i), depth + 1, printText);
			}

			for (int i = 0; i < depth; i++)
				System.out.print(' ');

			System.out.println("</" + e.getNodeName() + ">");
		}
	}

	public static void elementToLogAsString(Element e)
	{
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			StringWriter sw = new StringWriter();
			trans.transform(new DOMSource(e), new StreamResult(sw));
			System.err.println(sw.toString());
		}
		catch (Exception ex)
		{
			System.err.println("couldn't write " + e + " to log");
		}
	}

	public static ArrayList<String> getGroupsFromSecurityResponse(Element securityResponse)
	{
		ArrayList<String> groups = new ArrayList<String>();
		
		Element groupList = (Element) GSXML.getChildByTagName(securityResponse, GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
		if(groupList == null)
		{
			return groups;
		}
		
		NodeList groupElems = GSXML.getChildrenByTagName(groupList, GSXML.GROUP_ELEM);
		
		for(int i = 0; i < groupElems.getLength(); i++)
		{
			Element groupElem = (Element) groupElems.item(i);
			groups.add(groupElem.getAttribute(GSXML.NAME_ATT));
		}
		
		return groups;
	}
}
