package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

// other java stuff
import java.io.File;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.*;

/** base class for Actions */
abstract public class Action
{

	/** the system set up variables */
	protected HashMap<String, Comparable> config_params = null;
	/** container Document to create XML Nodes */
	protected Document doc = null;
	/** a converter class to parse XML and create Docs */
	protected XMLConverter converter = null;
	/**
	 * a reference to the message router that it must talk to to get info. it
	 * may be a communicator acting as a proxy, but it doesn't care about that
	 */
	protected ModuleInterface mr = null;

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.Action.class.getName());

	public Action()
	{
		this.converter = new XMLConverter();
		this.doc = this.converter.newDOM();
	}

	/** the config variables must be set before configure is called */
	public void setConfigParams(HashMap<String, Comparable> params)
	{
		this.config_params = params;
	}

	/** sets the message router */
	public void setMessageRouter(ModuleInterface m)
	{
		this.mr = m;
	}

	public boolean configure()
	{
		// does nothing yet
		return true;
	}

	/**
	 * process takes an xml representation of cgi args and returns the page of
	 * results - may be in html/xml/other depending on the output att of the
	 * request
	 */
	public String process(String xml_in)
	{

		Document message_doc = this.converter.getDOM(xml_in);
		if (message_doc == null)
		{
			logger.error("Couldn't parse request");
			logger.error(xml_in);
			return null;
		}
		Node result = process(message_doc);
		return this.converter.getString(result);
	}

	/** the main process method - must be implemented in subclass */
	abstract public Node process(Node xml_in);

	/**
	 * tell the param class what its arguments are if an action has its own
	 * arguments, this should add them to the params object - particularly
	 * important for args that should not be saved
	 */
	public boolean addActionParameters(GSParams params)
	{
		return true;
	}

	protected void getRequiredMetadataNames(Element format, HashSet<String> meta_names)
	{
		extractMetadataNames(format, meta_names);
		addLinkMetadataNames(format, meta_names);
	}

	protected void extractMetadataNames(Element format, HashSet<String> meta_names)
	{
		//NodeList nodes = format.getElementsByTagNameNS("metadata", "http://www.greenstone.org/configformat");
		NodeList metadata_nodes = format.getElementsByTagName("gsf:metadata");
		for (int i = 0; i < metadata_nodes.getLength(); i++)
		{
			Element elem = (Element) metadata_nodes.item(i);
			StringBuffer metadata = new StringBuffer();
			String pos = elem.getAttribute("pos");
			String name = elem.getAttribute("name");
			String select = elem.getAttribute("select");
			String sep = elem.getAttribute("separator");
			if (!pos.equals(""))
			{
				metadata.append("pos" + pos); // first, last or indexing number
				metadata.append(GSConstants.META_RELATION_SEP);
			}
			if (!select.equals(""))
			{
				metadata.append(select);
				metadata.append(GSConstants.META_RELATION_SEP);
			}
			if (!sep.equals(""))
			{
				metadata.append(GSConstants.META_SEPARATOR_SEP);
				metadata.append(sep);
				metadata.append(GSConstants.META_SEPARATOR_SEP);
				metadata.append(GSConstants.META_RELATION_SEP);
			}

			metadata.append(name);
			meta_names.add(metadata.toString());
		}
	}

	protected void addLinkMetadataNames(Element format, HashSet<String> meta_names)
	{
		// The XSL tranform for
		//   gsf:link type="source" 
		// makes use of 'assocfilepath' so need to make sure it's asked for

		NodeList link_nodes = format.getElementsByTagName("gsf:link");
		for (int i = 0; i < link_nodes.getLength(); i++)
		{
			Element elem = (Element) link_nodes.item(i);
			String type = elem.getAttribute("type");
			if (type.equals("source"))
			{
				meta_names.add("assocfilepath");
				meta_names.add("srclinkFile");
			}
		}

		// get all the metadata necessary for when the user has used "gsf:equivlink"
		// so that we can build up the equivlink from the metadata components it needs
		link_nodes = format.getElementsByTagName("gsf:equivlinkgs3");
		if (link_nodes.getLength() > 0)
		{
			String[] equivlink_metanames = { "equivDocIcon", "equivDocLink", "/equivDocLink" };

			for (int i = 0; i < equivlink_metanames.length; i++)
			{
				StringBuffer metadata = new StringBuffer();
				metadata.append("all"); // this means the attr multiple = true;
				metadata.append(GSConstants.META_RELATION_SEP);

				metadata.append(GSConstants.META_SEPARATOR_SEP);
				metadata.append(','); // attr separator = ","
				metadata.append(GSConstants.META_SEPARATOR_SEP);
				metadata.append(GSConstants.META_RELATION_SEP);

				// the name of the metadata we're retrieving
				metadata.append(equivlink_metanames[i]);
				meta_names.add(metadata.toString());
			}
		}

		if (format.getElementsByTagName("gsf:image").getLength() > 0)
		{
			meta_names.add("Thumb");
			meta_names.add("Screen");
			meta_names.add("SourceFile");
		}
	}

	protected Element createMetadataParamList(HashSet<String> metadata_names)
	{
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		Element param = null;
		Iterator<String> i = metadata_names.iterator();
		while (i.hasNext())
		{
			String name = i.next();
			param = this.doc.createElement(GSXML.PARAM_ELEM);
			param_list.appendChild(param);
			param.setAttribute(GSXML.NAME_ATT, "metadata");
			param.setAttribute(GSXML.VALUE_ATT, name);

		}
		return param_list;
	}

	protected boolean processErrorElements(Element message, Element page)
	{
		NodeList error_nodes = message.getElementsByTagName(GSXML.ERROR_ELEM);
		if (error_nodes.getLength() == 0)
		{
			return false;
		}
		Document owner = page.getOwnerDocument();
		for (int i = 0; i < error_nodes.getLength(); i++)
		{
			page.appendChild(owner.importNode(error_nodes.item(i), true));
		}
		return true;
	}

	/**
	 * Takes an XML element and adds the metadata of the current site to it.
	 * Useful for adding the current site's metadata to a response before
	 * sending it
	 * 
	 * @param element
	 *            the element to add site metadata to
	 * @param lang
	 *            the current language
	 * @param uid
	 *            the current user id
	 */
	protected void addSiteMetadata(Element element, UserContext userContext)
	{
		//ADD SITE METADATA
		Element metadata_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		//create a hashmap of params
		HashMap subset_params = new HashMap(1);
		subset_params.put(GSXML.SUBSET_PARAM, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		//create the element to put the params in
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		//put them in
		GSXML.addParametersToList(this.doc, param_list, subset_params);
		metadata_request.appendChild(param_list);
		//create the message
		Element metadata_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		metadata_message.appendChild(metadata_request);
		//get response
		Element metadata_response_message = (Element) this.mr.process(metadata_message);
		//drill down to response
		Element metadata_response = (Element) GSXML.getChildByTagName(metadata_response_message, GSXML.RESPONSE_ELEM);
		//merge in metadata
		GSXML.mergeMetadataLists(element, metadata_response);
	}

	protected void addInterfaceOptions(Element elem)
	{
		Element documentOptionList = this.doc.createElement("interfaceOptions");
		for (Object key : this.config_params.keySet())
		{
			Element option = this.doc.createElement("option");
			option.setAttribute(GSXML.NAME_ATT, (String) key);
			option.setAttribute(GSXML.VALUE_ATT, this.config_params.get(key).toString());
			documentOptionList.appendChild(option);
		}
		elem.appendChild(elem.getOwnerDocument().importNode(documentOptionList, true));
	}
}
