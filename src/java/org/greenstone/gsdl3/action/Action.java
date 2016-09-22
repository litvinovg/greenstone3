package org.greenstone.gsdl3.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.Dictionary;
import org.greenstone.gsdl3.util.GSConstants;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.GSXSLT;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** base class for Actions */
abstract public class Action
{

	/** the system set up variables */
	protected HashMap<String, Object> config_params = null;
	
	
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
	}

	/** the config variables must be set before configure is called */
	public void setConfigParams(HashMap<String, Object> params)
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

  protected void getRequiredMetadataNames(Element format, HashSet<String> meta_names) {
    GSXSLT.findExtraMetadataNames(format, meta_names);
  }


	protected Element createMetadataParamList(Document doc, HashSet<String> metadata_names)
	{
		Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);

		Element param = null;
		Iterator<String> i = metadata_names.iterator();
		while (i.hasNext())
		{
			String name = i.next();
			param = doc.createElement(GSXML.PARAM_ELEM);
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
		Document doc = element.getOwnerDocument();
		
		//ADD SITE METADATA
		Element metadata_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		//create a hashmap of params
		HashMap subset_params = new HashMap(2);
		subset_params.put(GSXML.SUBSET_PARAM, GSXML.METADATA_ELEM + GSXML.LIST_MODIFIER);
		subset_params.put(GSXML.SUBSET_PARAM, GSXML.DISPLAY_TEXT_ELEM + GSXML.LIST_MODIFIER);
		//create the element to put the params in
		Element param_list = doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		//put them in
		GSXML.addParametersToList(param_list, subset_params);
		metadata_request.appendChild(param_list);
		//create the message
		Element metadata_message = doc.createElement(GSXML.MESSAGE_ELEM);
		metadata_message.appendChild(metadata_request);
		//get response
		Element metadata_response_message = (Element) this.mr.process(metadata_message);
		//drill down to response
		Element metadata_response = (Element) GSXML.getChildByTagName(metadata_response_message, GSXML.RESPONSE_ELEM);
		//merge in metadata
		// *************** need to merge the displayITem lists too
		GSXML.mergeMetadataLists(element, metadata_response);
		GSXML.mergeSpecifiedLists(element, metadata_response, GSXML.DISPLAY_TEXT_ELEM);
	}

	protected void addInterfaceOptions(Element elem)
	{
		Document doc = elem.getOwnerDocument();
		
		Element documentOptionList = doc.createElement("interfaceOptions");
		for (Object key : this.config_params.keySet())
		{
			Element option = doc.createElement("option");
			option.setAttribute(GSXML.NAME_ATT, (String) key);
			option.setAttribute(GSXML.VALUE_ATT, this.config_params.get(key).toString());
			documentOptionList.appendChild(option);
		}
		elem.appendChild(elem.getOwnerDocument().importNode(documentOptionList, true));
	}

	protected Element getFormatInfo(String to, UserContext userContext)
	{
		// Eclipse call hierarchy shows the element returned from this method is 
		// subsequently used in a 'importNode'.  For this reason it is safe here
		// for call up our own document DOM.  
		//
		// If this pattern changes for any reason, then the DOM will need to be
		// passed in as a parameter
		
		Document doc = XMLConverter.newDOM();
		
		Element mr_format_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_format_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_FORMAT, to, userContext);
		mr_format_message.appendChild(mr_format_request);

		// process the message
		Element mr_response_message = (Element) this.mr.process(mr_format_message);
		// the response

		Element format_response = (Element) GSXML.getChildByTagName(mr_response_message, GSXML.RESPONSE_ELEM);

		Element format_elem = (Element) GSXML.getChildByTagName(format_response, GSXML.FORMAT_ELEM);
		if (format_elem != null)
		{
			Element global_format_elem = (Element) GSXML.getChildByTagName(format_response, GSXML.GLOBAL_FORMAT_ELEM);
			if (global_format_elem != null)
			{
				GSXSLT.mergeFormatElements(format_elem, global_format_elem, false);
			}
		}
		return format_elem;
	}

	protected String getTextString(String key, String lang, String dictionary, String[] args)
	{
	  logger.error("lang = "+lang);
		if (dictionary != null)
		{
			// just try the one specified dictionary
		  Dictionary dict = new Dictionary(dictionary, lang);
			String result = dict.get(key, args);
			if (result == null)
			{ // not found
				return "_" + key + "_";
			}
			return result;
		}

		// otherwise we try class names for dictionary names
		String class_name = this.getClass().getName();
		class_name = class_name.substring(class_name.lastIndexOf('.') + 1);
		Dictionary dict = new Dictionary(class_name, lang);
		String result = dict.get(key, args);
		if (result != null)
		{
			return result;
		}

		// we have to try super classes
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
			dict = new Dictionary(class_name, lang);
			result = dict.get(key, args);
			c = c.getSuperclass();
		}
		if (result == null)
		{
			return "_" + key + "_";
		}
		return result;

	}

}
