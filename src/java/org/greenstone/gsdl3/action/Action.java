package org.greenstone.gsdl3.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.core.ModuleInterface;
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

	protected void getRequiredMetadataNames(Element format, HashSet<String> meta_names)
	{
		extractMetadataNames(format, meta_names);
		addLinkMetadataNames(format, meta_names);
	}

  // should change to metadataList?? and use attributes for select rather than
  // prepending parent_ etc
  protected void extractMetadataNames(Element format, HashSet<String> meta_names)
	{
	    
	  NodeList metadata_nodes = format.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "metadata"); // gsf:metadata
	  for (int i = 0; i < metadata_nodes.getLength(); i++)
	    {
	      Element elem = (Element) metadata_nodes.item(i);
	      String name = elem.getAttribute("name");
	      String select = elem.getAttribute("select");

	      if (!select.equals("")) {
		name = select+GSConstants.META_RELATION_SEP+name;
	      }
	      meta_names.add(name);
	    }
	      
	  NodeList foreach_metadata_nodes = format.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "foreach-metadata"); // gsf:foreach-metadata
	  for (int i = 0; i < foreach_metadata_nodes.getLength(); i++)
	    {
	      Element elem = (Element) foreach_metadata_nodes.item(i);
	      String name = elem.getAttribute("name");
	      String select = elem.getAttribute("select");

	      if (!select.equals("")) {
		name = select+GSConstants.META_RELATION_SEP+name;
	      }
	      meta_names.add(name);
	    }	      
	}

	protected void addLinkMetadataNames(Element format, HashSet<String> meta_names)
	{
		// The XSL tranform for
		//   gsf:link type="source" 
		// makes use of 'assocfilepath' so need to make sure it's asked for

		boolean getEquivLinkMeta = false;

		NodeList link_nodes = format.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "link");
		for (int i = 0; i < link_nodes.getLength(); i++)
		{
			Element elem = (Element) link_nodes.item(i);
			String type = elem.getAttribute("type");
			if (type.equals("source"))
			{
				meta_names.add("assocfilepath");
				meta_names.add("srclinkFile");
			}
			else if (type.equals("web"))
			{
				meta_names.add("weblink");
				meta_names.add("webicon");
				meta_names.add("/weblink");
			}
			else if (type.equals("equivdoc"))
			{
				getEquivLinkMeta = true;
			}
		}

		// get all the metadata necessary for when the user has used "gsf:equivlink"
		// so that we can build up the equivlink from the metadata components it needs
		link_nodes = format.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "equivlinkgs3");
		if (getEquivLinkMeta || link_nodes.getLength() > 0)
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

		if (format.getElementsByTagNameNS(GSXML.GSF_NAMESPACE, "image").getLength() > 0)
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

  protected Element getFormatInfo(String to, UserContext userContext) {
    Element mr_format_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
    Element mr_format_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_FORMAT, to, userContext);
    mr_format_message.appendChild(mr_format_request);

    // process the message
    Element mr_response_message = (Element) this.mr.process(mr_format_message);
    // the response
    
    Element format_response = (Element) GSXML.getChildByTagName(mr_response_message, GSXML.RESPONSE_ELEM);
    
    Element format_elem = (Element) GSXML.getChildByTagName(format_response, GSXML.FORMAT_ELEM);
    if (format_elem!= null) {
      Element global_format_elem = (Element) GSXML.getChildByTagName(format_response, GSXML.GLOBAL_FORMAT_ELEM);
      if (global_format_elem != null)
	{
	  GSXSLT.mergeFormatElements(format_elem, global_format_elem, false);
	}
    }
    return format_elem;
  }
}

  

