package org.greenstone.gsdl3.service;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.util.HashSet;

public class CollectionGroups extends ServiceRack {

	private Element hierarchy = null;
	private Element groupDesc = null;

	private static final String GROUP_CONTENT = "GroupCurrentContent";

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.BerryBasket.class.getName());

	@Override
	protected Element getServiceDescription(Document doc, String service, String lang, String subset) {
		// TODO Auto-generated method stub
		return null;
	}

	String[] _services = { GROUP_CONTENT };

	public boolean configure(Element info, Element extra_info) {
		if (!super.configure(info, extra_info)) {
			return false;
		}

		logger.info("Configuring CollectionGroups...");
		this.config_info = info;

		for (int i = 0; i < _services.length; i++) {
			Element service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_GROUPINFO);
			service.setAttribute(GSXML.NAME_ATT, _services[i]);
			this.short_service_info.appendChild(service);
		}
		// Load group configuration from file
		readGroupConfiguration();

		return true;
	}

	protected Element processGroupCurrentContent(Element request) {

		Document doc = XMLConverter.newDOM();

		UserContext userContext = new UserContext(request);

		// Get param list from request
		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		// Set default group path (main page)
		String groupPath = "";
		// If param list not null and group param exists extract value and
		// override groupPath variable
		if (paramList != null) {
			Element paramGroup = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.GROUP_ELEM);
			if (paramGroup != null) {
				logger.error(GSXML.elementToString(paramGroup, true));
				groupPath = paramGroup.getAttribute(GSXML.VALUE_ATT);
			}
		}
		groupPath = groupPath.replaceAll("(/+)", "/");
		groupPath = groupPath.replaceAll("(^/+)|(/+$)", "");

		// first, get the message router info
		Element info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element coll_list_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		info_message.appendChild(coll_list_request);
		Element info_response_message = (Element) this.router.process(info_message);
		if (info_response_message == null) {
			logger.error(" couldn't query the message router!");
			return null;
		}
		Element info_response = (Element) GSXML.getChildByTagName(info_response_message, GSXML.RESPONSE_ELEM);
		if (info_response == null) {
			logger.error("couldn't query the message router!");
			return null;
		}

		Element collection_list = (Element) GSXML.getChildByTagName(info_response,
				GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		//collection_list = (Element) doc.importNode(collection_list, true);
		//Collections not defined in groups config file
		
		Element groupContent = getCurrentContent(groupPath);
		
		// Prepare basic response
		Element response = GSXML.createBasicResponse(doc, GSXML.SERVICE_TYPE_GROUPINFO);

		// Temporary
		// If groupContent is empty return empty collection list
		if (groupContent == null) {
			response.appendChild(doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER));
			return response;
		}
		NodeList currentContent = groupContent.getChildNodes();
		if (currentContent != null && currentContent.getLength() > 0) {
			// Create CollectionList element in response
			Element collection_list_element = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			response.appendChild(collection_list_element);
			Element group_list_element = doc.createElement(GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
			response.appendChild(group_list_element);
			logger.warn("GROUPCONTENT ----------- " + GSXML.elementToString(groupContent, true));
			// Iterate over collections from current view
			int i;
			for (i=0; i < currentContent.getLength(); i++) {
				//logger.warn("NODE CuR Content" + GSXML.xmlNodeToString(currentContent.item(i)));
				
				if (currentContent.item(i).getNodeName() == GSXML.COLLECTION_ELEM) {
					
					Element collection = (Element) currentContent.item(i);
					String collection_name = collection.getAttribute(GSXML.NAME_ATT);
					// Check wether collection from current view available also
					// by Message Router
					Element checkedCollection = GSXML.getNamedElement(collection_list, GSXML.COLLECTION_ELEM,GSXML.NAME_ATT, collection_name);
					//Set position value
					checkedCollection.setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
					if (checkedCollection != null) {
						// Add collection to response
						collection_list_element.appendChild(doc.importNode(checkedCollection, true));
					}
					
					//Iterate over groups in current view
				} else if (currentContent.item(i).getNodeName() == GSXML.GROUP_ELEM) {
					((Element)currentContent.item(i)).setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
					Element currentGroup = (Element) currentContent.item(i);
					String currentGroupName = currentGroup.getAttribute(GSXML.NAME_ATT);
					Element group = getGroupDescription(currentGroupName);
					//Set position value
					group.setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
					if (group != null) {
						group_list_element.appendChild(doc.importNode(group, true));
					}

				}

			}
			logger.warn("i " + i);
			//logger.warn("GROUP LIST" + GSXML.elementToString(group_list_element, true));
			//Add ungrouped collections if groupPath /+ or "" or null
			if (groupPath.isEmpty()) {
				//Get ungrouped collection list
				Element ungroupedCollections = getUngroupedCollections(collection_list);
				
				//Add each ungrouped collection to the collection list in loop
				NodeList ungroupedCollectionNodes = GSXML.getChildrenByTagName(ungroupedCollections, GSXML.COLLECTION_ATT);
				if (ungroupedCollectionNodes != null) {
					for (int j = 0; j < ungroupedCollectionNodes.getLength(); j++) {
						//logger.warn("UNGROUPED COLL ELEM" + GSXML.xmlNodeToString(ungroupedCollections).intern());
						Element ungroupedCollection = (Element) doc.importNode(ungroupedCollectionNodes.item(j), true);
						ungroupedCollection.setAttribute(GSXML.POSITION_ATT, String.valueOf(i++));
						collection_list_element.appendChild(ungroupedCollection);
					}
				}
				
			}
			
			

		}

		return response;
	}

	protected Element getCurrentContent(String path) {

		Document doc = XMLConverter.newDOM();
		
		if (path == null || path.isEmpty()) {
			// Return to the main page
		}
		String[] pathSteps = path.split("/");

		Element currentView = (Element) hierarchy.cloneNode(true);
		// Get the current view
		for (int i = 0; i < pathSteps.length; i++) {
			if (!pathSteps[i].isEmpty()) {
				currentView = GSXML.getNamedElement(currentView, GSXML.GROUP_ELEM, GSXML.NAME_ATT, pathSteps[i]);
			}
		}
		if (currentView == null || !currentView.hasChildNodes()) {
			// Return to the main page
			return null;
		}
		logger.error("CURRENT VIEW" + GSXML.elementToString(currentView, true));
		return currentView;
	}

	protected void readGroupConfiguration() {

		File configFile = new File(GSFile.groupConfigFile(site_home));
		//
		if (!configFile.exists()) {
			logger.error("Groups config file " + configFile.getPath() + " does not exists");
		}
		// Try to read and catch exception if it fails
		Document doc = XMLConverter.getDOM(configFile);
		Element content = doc.getDocumentElement();
		
		// XPath to find empty text nodes.
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
			NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			    Node emptyTextNode = emptyTextNodes.item(i);
			    emptyTextNode.getParentNode().removeChild(emptyTextNode);
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		
		hierarchy = (Element) GSXML.getChildByTagName(content, GSXML.HIERARCHY_ELEM);
		groupDesc = (Element) GSXML.getChildByTagName(content, GSXML.GROUP_DESC_ELEM);
	}

	protected Element getGroupDescription(String name) {
		if (groupDesc != null) {
			// logger.error(GSXML.elementToString(groupDesc, true));
			Element description = (Element) GSXML.getNamedElement(groupDesc, GSXML.GROUP_ELEM, GSXML.NAME_ATT, name);
			if (description != null) {
				return description;
			}

		} else {
			logger.error("GroupDescription is not defined.");
		}
		// In case
		return null;
	}

	protected Element getUngroupedCollections(Element mr_collection_list) {
		Document doc = XMLConverter.newDOM();
		// Create Set
		Set<String> hierarchy_unique_collections = new HashSet<String>();
		// Element hierarchyCollections = doc.createElement("coll_list");
		// Get collection nodes
		
		NodeList coll_list = hierarchy.getElementsByTagName(GSXML.COLLECTION_ELEM);

		for (int i = 0; i < coll_list.getLength(); i++) {
			Element collection_element = (Element) coll_list.item(i);
			hierarchy_unique_collections.add(collection_element.getAttribute(GSXML.NAME_ATT));
		}
		Set<String> mr_collections = new HashSet<String>();
		NodeList mr_coll_list = mr_collection_list.getElementsByTagName(GSXML.COLLECTION_ELEM);

		for (int i = 0; i < mr_coll_list.getLength(); i++) {
			Element collection_element = (Element) mr_coll_list.item(i);
			mr_collections.add(collection_element.getAttribute(GSXML.NAME_ATT));
		}
		Set<String> ungrouped_collections = new HashSet<String>();
		for (String string : mr_collections) {
			if (!hierarchy_unique_collections.contains(string)){
				ungrouped_collections.add(string);
			}
		}
		Element result = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		for (String name : ungrouped_collections) {
			logger.error("RESULT GETUNGROUPED - NAME " + name);
			Element collection = doc.createElement(GSXML.COLLECTION_ELEM);
			collection.setAttribute(GSXML.NAME_ATT, name);
			result.appendChild(collection);
			
		}
		
		
		
		
		return result;

	}

}
