package org.greenstone.gsdl3.action;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;
// XML classes
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.*;

/** action for GS2 style classifier browsing */
public class GS2BrowseAction extends Action
{

	public static final String CLASSIFIER_ARG = "cl";

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.action.GS2BrowseAction.class.getName());

	/** process the request */
	public Node process(Node message_node)
	{

		Element message = this.converter.nodeToElement(message_node);

		// get the request - assume only one
		Element request = (Element) GSXML.getChildByTagName(message, GSXML.REQUEST_ELEM);

		// the result
		Element result = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element response = classifierBrowse(request);
		result.appendChild(response);
		return result;
	}

	protected Element classifierBrowse(Element request)
	{

		Element page_response = this.doc.createElement(GSXML.RESPONSE_ELEM);

		// extract the params from the cgi-request, and check that we have a coll specified
		Element cgi_paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		HashMap<String, Serializable> params = GSXML.extractParams(cgi_paramList, false);

		String service_name = (String) params.get(GSParams.SERVICE);
		String collection = (String) params.get(GSParams.COLLECTION);
		if (collection == null || collection.equals(""))
		{
			logger.error("classifierBrowse, need to specify a collection!");
			return page_response;

		}

		UserContext userContext = new UserContext(request);
		String to = GSPath.appendLink(collection, service_name);

		// the first part of the response is the service description 
		// for now get this again from the service. 
		// this should be cached somehow later on. 

		Element info_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element info_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_DESCRIBE, to, userContext);
		info_message.appendChild(info_request);

		// also get the format stuff now if there is some
		Element format_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_FORMAT, to, userContext);
		info_message.appendChild(format_request);
		// process the requests

		Element info_response = (Element) this.mr.process(info_message);

		// the two responses
		NodeList responses = info_response.getElementsByTagName(GSXML.RESPONSE_ELEM);
		Element service_response = (Element) responses.item(0);
		Element format_response = (Element) responses.item(1);

		Element service_description = (Element) GSXML.getChildByTagName(service_response, GSXML.SERVICE_ELEM);
		page_response.appendChild(this.doc.importNode(service_description, true));

		//append site metadata
		addSiteMetadata(page_response, userContext);
		addInterfaceOptions(page_response);

		// if rt=d, then we are just displaying the service
		String request_type = (String) params.get(GSParams.REQUEST_TYPE);
		if (request_type.equals("d"))
		{
			//return the page that we have so far
			return page_response;
		}

		// get the node that the user has clicked on
		String classifier_node = (String) params.get(CLASSIFIER_ARG);

		// if the node is not defined, return the page that we have so far
		if (classifier_node == null || classifier_node.equals(""))
		{
			return page_response;
		}

		// the id of the classifier is the top id of the selected node
		String top_id = OID.getTop(classifier_node);
		HashSet<String> doc_meta_names = new HashSet<String>();
		HashSet<String> class_meta_names = new HashSet<String>();
		// add in the defaults
		doc_meta_names.add("Title");
		class_meta_names.add("Title");

		// add the format info into the response
		Element format_elem = (Element) GSXML.getChildByTagName(format_response, GSXML.FORMAT_ELEM);
		if (format_elem != null)
		{

			// find the one for the classifier we are in
			Element this_format = GSXML.getNamedElement(format_elem, GSXML.CLASSIFIER_ELEM, GSXML.NAME_ATT, top_id);
			if (this_format == null)
			{
				this_format = (Element) GSXML.getChildByTagName(format_elem, GSXML.DEFAULT_ELEM);
			}

			if (this_format != null)
			{
				Element new_format = GSXML.duplicateWithNewName(this.doc, this_format, GSXML.FORMAT_ELEM, false);
				extractMetadataNames(new_format, doc_meta_names, class_meta_names);
				// set the format type
				new_format.setAttribute(GSXML.TYPE_ATT, "browse");

				page_response.appendChild(new_format);
			}
		}

		// find out if this classifier is horizontal at top
		Element class_list = (Element) GSXML.getChildByTagName(service_description, GSXML.CLASSIFIER_ELEM + GSXML.LIST_MODIFIER);
		Element this_classifier = GSXML.getNamedElement(class_list, GSXML.CLASSIFIER_ELEM, GSXML.NAME_ATT, top_id);
		boolean horizontal_at_top = false;
		if (!this_classifier.getAttribute("horizontalAtTop").equals(""))
		{
			horizontal_at_top = true;
		}
		if (top_id.equals(classifier_node) && horizontal_at_top)
		{
			// we have asked for a top node - if the first list is horizontal, we will select the first element of that list
			// this is a hack. also it craps out if the classifier really isn't horizontalAtTop. - 
			classifier_node = classifier_node + ".1";

		}

		// get the browse structure for the selected node
		Element classify_message = this.doc.createElement(GSXML.MESSAGE_ELEM);
		Element classify_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to, userContext);
		classify_message.appendChild(classify_request);

		//Create a parameter list to specify the required structure information
		// for now, always get ancestors and children
		Element param_list = this.doc.createElement(GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		classify_request.appendChild(param_list);
		Element param = this.doc.createElement(GSXML.PARAM_ELEM);
		param_list.appendChild(param);
		param.setAttribute(GSXML.NAME_ATT, "structure");
		param.setAttribute(GSXML.VALUE_ATT, "ancestors");
		param = this.doc.createElement(GSXML.PARAM_ELEM);
		param_list.appendChild(param);
		param.setAttribute(GSXML.NAME_ATT, "structure");
		param.setAttribute(GSXML.VALUE_ATT, "children");

		// put the classifier node into a classifier node list
		Element classifier_list = this.doc.createElement(GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		Element classifier = this.doc.createElement(GSXML.CLASS_NODE_ELEM);
		classifier.setAttribute(GSXML.NODE_ID_ATT, classifier_node);
		classifier_list.appendChild(classifier);
		classify_request.appendChild(classifier_list);

		if (horizontal_at_top && !classifier_node.equals(top_id))
		{
			// also put the top id in, to get the persistant horizontal info
			classifier = this.doc.createElement(GSXML.CLASS_NODE_ELEM);
			classifier.setAttribute(GSXML.NODE_ID_ATT, top_id);
			classifier_list.appendChild(classifier);
		}
		// process the request
		Element classify_response = (Element) this.mr.process(classify_message);
		String path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
		Element class_node_list = (Element) GSXML.getNodeByPath(classify_response, path);

		path = GSPath.appendLink(GSXML.CLASS_NODE_ELEM, GSXML.NODE_STRUCTURE_ELEM);
		// assume that we always get back the top level CL1 node - this becomes the page_classifier node
		path = GSPath.appendLink(path, GSXML.CLASS_NODE_ELEM);
		Element cl_structure = (Element) GSXML.getNodeByPath(class_node_list, path);
		if (cl_structure == null)
		{
			logger.error("classifier structure request returned no structure");
			return page_response;
		}

		Element page_classifier = null;
		if (horizontal_at_top && !classifier_node.equals(top_id))
		{
			// get the info for the top node
			Element top_node = GSXML.getNamedElement(class_node_list, GSXML.CLASS_NODE_ELEM, GSXML.NODE_ID_ATT, top_id);
			if (top_node != null)
			{
				path = GSPath.appendLink(GSXML.NODE_STRUCTURE_ELEM, GSXML.CLASS_NODE_ELEM);
				Element top_structure = (Element) GSXML.getNodeByPath(top_node, path);
				// add this as the classifier elem
				page_classifier = GSXML.duplicateWithNewName(this.doc, top_structure, GSXML.CLASSIFIER_ELEM, true);
				page_response.appendChild(page_classifier);
				// now replace the child with the structure from the other request
				Element new_classifier = (Element) GSXML.getChildByTagName(cl_structure, GSXML.CLASS_NODE_ELEM);
				String replace_name = new_classifier.getAttribute(GSXML.NODE_ID_ATT);

				// find the appropriate child
				Element old_classifier = GSXML.getNamedElement(page_classifier, GSXML.CLASS_NODE_ELEM, GSXML.NODE_ID_ATT, replace_name);
				page_classifier.replaceChild(this.doc.importNode(new_classifier, true), old_classifier);
				page_classifier.setAttribute(GSXML.NAME_ATT, top_id);
			}
			else
			{
				// add the single classifier node as the page classifier 
				page_classifier = GSXML.duplicateWithNewName(this.doc, cl_structure, GSXML.CLASSIFIER_ELEM, true);
				page_response.appendChild(page_classifier);
				page_classifier.setAttribute(GSXML.NAME_ATT, top_id);
			}

		}
		else
		{
			// add the single classifier node as the page classifier 
			page_classifier = GSXML.duplicateWithNewName(this.doc, cl_structure, GSXML.CLASSIFIER_ELEM, true);
			page_response.appendChild(page_classifier);
			page_classifier.setAttribute(GSXML.NAME_ATT, top_id);
		}
		// get the metadata for each classifier node, 
		// then for each document node

		Element metadata_message = this.doc.createElement(GSXML.MESSAGE_ELEM);

		boolean did_classifier = false;
		boolean did_documents = false;

		// if there are classifier nodes 
		// create a metadata request for the classifier, and add it to 
		// the the message
		NodeList cl_nodes = page_classifier.getElementsByTagName(GSXML.CLASS_NODE_ELEM);

		if (cl_nodes.getLength() > 0)
		{
			did_classifier = true;
			Element cl_meta_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, to + "MetadataRetrieve", userContext);
			metadata_message.appendChild(cl_meta_request);

			Element new_cl_nodes_list = this.doc.createElement(GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
			cl_meta_request.appendChild(new_cl_nodes_list);

			for (int c = 0; c < cl_nodes.getLength(); c++)
			{

				Element cl = this.doc.createElement(GSXML.CLASS_NODE_ELEM);
				cl.setAttribute(GSXML.NODE_ID_ATT, ((Element) cl_nodes.item(c)).getAttribute(GSXML.NODE_ID_ATT));
				new_cl_nodes_list.appendChild(cl);
			}

			// create and add in the param list - for now get all the metadata
			// should be based on info sent in from the recept, and the 
			// format stuff
			Element cl_param_list = createMetadataParamList(class_meta_names);
			cl_meta_request.appendChild(cl_param_list);

		}

		// if there are document nodes in the classification (happens 
		// sometimes), create a second request for document metadata and 
		// append to the message
		NodeList doc_nodes = page_classifier.getElementsByTagName(GSXML.DOC_NODE_ELEM);
		if (doc_nodes.getLength() > 0)
		{
			did_documents = true;
			Element doc_meta_request = GSXML.createBasicRequest(this.doc, GSXML.REQUEST_TYPE_PROCESS, GSPath.appendLink(collection, "DocumentMetadataRetrieve"), userContext);
			metadata_message.appendChild(doc_meta_request);

			Element doc_list = this.doc.createElement(GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
			doc_meta_request.appendChild(doc_list);

			for (int c = 0; c < doc_nodes.getLength(); c++)
			{

				Element d = this.doc.createElement(GSXML.DOC_NODE_ELEM);
				d.setAttribute(GSXML.NODE_ID_ATT, ((Element) doc_nodes.item(c)).getAttribute(GSXML.NODE_ID_ATT));
				doc_list.appendChild(d);
			}

			// create and add in the param list - add all for now
			Element doc_param_list = createMetadataParamList(doc_meta_names);
			doc_meta_request.appendChild(doc_param_list);

		}

		// process the metadata requests
		Element metadata_response = (Element) this.mr.process(metadata_message);
		if (did_classifier)
		{
			// the classifier one will be the first response
			// add the metadata lists for each node back into the 
			// page_classifier nodes
			path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.CLASS_NODE_ELEM + GSXML.LIST_MODIFIER);
			Node pathNode = GSXML.getNodeByPath(metadata_response, path);
			if (pathNode == null)
			{
				return page_response;
			}
			//NodeList meta_response_cls = (Element)pathNode.getChildNodes(); // can't handle empty elements from converting formatted strings (with empty newlines) into XML
			NodeList meta_response_cls = ((Element) pathNode).getElementsByTagName(GSXML.CLASS_NODE_ELEM);
			for (int i = 0; i < cl_nodes.getLength(); i++)
			{
				GSXML.mergeMetadataLists(cl_nodes.item(i), meta_response_cls.item(i));
			}
		}

		if (did_documents)
		{
			NodeList meta_response_docs = null;
			if (!did_classifier)
			{
				// its the first response
				path = GSPath.appendLink(GSXML.RESPONSE_ELEM, GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				Node pathNode = GSXML.getNodeByPath(metadata_response, path);
				if (pathNode == null)
				{
					return page_response;
				}

				meta_response_docs = pathNode.getChildNodes();

			}
			else
			{ // its the second response
				Node nodes = GSXML.getChildByTagName(metadata_response.getElementsByTagName(GSXML.RESPONSE_ELEM).item(1), GSXML.DOC_NODE_ELEM + GSXML.LIST_MODIFIER);
				if (nodes == null)
				{
					return page_response;
				}
				meta_response_docs = nodes.getChildNodes();
			}

			for (int i = 0; i < doc_nodes.getLength(); i++)
			{
				GSXML.mergeMetadataLists(doc_nodes.item(i), meta_response_docs.item(i));
			}
		}

		logger.debug("(GS2BrowseAction) Page:\n" + this.converter.getPrettyString(page_response));
		return page_response;
	}

	protected void extractMetadataNames(Element new_format, HashSet<String> doc_meta_names, HashSet<String> class_meta_names)
	{

		NodeList templates = new_format.getElementsByTagName("gsf:template");
		for (int i = 0; i < templates.getLength(); i++)
		{
			Element template = (Element) templates.item(i);
			String match = template.getAttribute("match");
			if (match.equals("documentNode"))
			{
				getRequiredMetadataNames(template, doc_meta_names);
			}
			else if (match.equals("classifierNode"))
			{
				getRequiredMetadataNames(template, class_meta_names);
			}
		}
	}

}
