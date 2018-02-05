package org.greenstone.gsdl3.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.Request;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
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
import java.util.Iterator;

public class CollectionGroups extends ServiceRack {

	private Element hierarchy = null;
	private Element groupDesc = null;

	private static final String GROUP_CONTENT = "GroupCurrentContent";
	private static final String UNIQUE_COLLECTIONS = "UniqueCollections";
	private static final String COLLECTIONS_HIERARCHY = "CollectionsHierarchy";

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.BerryBasket.class.getName());

	@Override
	protected Element getServiceDescription(Document doc, String service, String lang, String subset) {
		// TODO Auto-generated method stub
		return null;
	}

	String[] _services = { GROUP_CONTENT, UNIQUE_COLLECTIONS, COLLECTIONS_HIERARCHY };

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
		return readGroupConfiguration();
	}

	protected Element processGroupCurrentContent(Element request) {

		Document doc = XMLConverter.newDOM();
		UserContext userContext = new UserContext(request);

	  // Prepare basic response
		Element result = GSXML.createBasicResponse(doc, GSXML.SERVICE_TYPE_GROUPINFO);
		
		
		// Get param list from request
		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		// Set default group path (main page)
		String groupPath = "";
		// If param list not null and group param exists extract value and
		// override groupPath variable
		if (paramList != null) {
			Element paramGroup = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.GROUP_ELEM);
			if (paramGroup != null) {
				groupPath = paramGroup.getAttribute(GSXML.VALUE_ATT);
			}
		}
		//Remove leading, ending / and dupliclated /
		groupPath = groupPath.replaceAll("(/+)", "/");
		groupPath = groupPath.replaceAll("(^/+)|(/+$)", "");

		Element mrCollectionList = getAvailableCollectionList(userContext);
		if (mrCollectionList == null){
			result.appendChild(doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER));
			return result;
		}
		//Get current groups and collections
		Element groupContent = getRawCurrentContent(groupPath);
	
		//Get ungrouped collection list
		Element ungroupedCollections = getUngroupedCollections(mrCollectionList);
		
		// If groupContent is empty return empty collection list
		if (groupContent == null) {
			result.appendChild(doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER));
			return result;
		}
		NodeList currentContent = groupContent.getChildNodes();
		if (currentContent != null && currentContent.getLength() > 0) {
			// Create CollectionList element in response
			Element result_collection_list = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			result.appendChild(result_collection_list);
			Element result_group_list = doc.createElement(GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
			result.appendChild(result_group_list);
			// Iterate over collections from current view
			// i represents current position in groupConfig.xml
			int i;
			for (i=0; i < currentContent.getLength(); i++) {
				//logger.warn("NODE CuR Content" + GSXML.xmlNodeToString(currentContent.item(i)));
				
				if (currentContent.item(i).getNodeName() == GSXML.COLLECTION_ELEM) {
					
					Element collection = (Element) currentContent.item(i);
					String collection_name = collection.getAttribute(GSXML.NAME_ATT);
					// Check whether collection from current view exists in message router response
					Element checkedCollection = GSXML.getNamedElement(mrCollectionList, GSXML.COLLECTION_ELEM,GSXML.NAME_ATT, collection_name);
					if (checkedCollection != null) {
						//Set position value
						checkedCollection.setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
						// Add collection to response
						result_collection_list.appendChild(doc.importNode(checkedCollection, true));
					}
					
					//Iterate over groups in current view
				} else if (currentContent.item(i).getNodeName() == GSXML.GROUP_ELEM) {
					Element currentGroup = (Element) currentContent.item(i);
					String currentGroupName = currentGroup.getAttribute(GSXML.NAME_ATT);
					Element groupDescription = getGroupDescription(currentGroupName);
					
					groupDescription.setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
					result_group_list.appendChild(doc.importNode(groupDescription, true));
				}

			}
			//Add ungrouped collections if groupPath /+ or "" or null
			if (groupPath.isEmpty()) {
				
				//Add each ungrouped collection to the collection list in loop
				NodeList ungroupedCollectionNodes = GSXML.getChildrenByTagName(ungroupedCollections, GSXML.COLLECTION_ATT);
				if (ungroupedCollectionNodes != null) {
					for (int j = 0; j < ungroupedCollectionNodes.getLength(); j++) {
						//logger.warn("UNGROUPED COLL ELEM" + GSXML.xmlNodeToString(ungroupedCollections).intern());
						Element ungroupedCollection = (Element) doc.importNode(ungroupedCollectionNodes.item(j), true);
						ungroupedCollection.setAttribute(GSXML.POSITION_ATT, String.valueOf(i++));
						result_collection_list.appendChild(ungroupedCollection);
					}
				}
				
			} else {
				Element groupDescription = getPathInfo(groupPath);
				if (groupContent != null){
					result.appendChild(doc.importNode(groupDescription, true));
				}
			}
		}

		return result;
	}

	protected Element processUniqueCollections(Element request) {
		Document doc = XMLConverter.newDOM();
		UserContext userContext = new UserContext(request);
		// Prepare basic response
		Element result = GSXML.createBasicResponse(doc, GSXML.SERVICE_TYPE_GROUPINFO);
		Element resultCollections = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		result.appendChild(resultCollections);
		Element mrCollectionList = getAvailableCollectionList(userContext);
		//Collections from message router check
		if (mrCollectionList == null){
			logger.error("mrCollectionList is null!");
			return result;
		}
		//paramList check
		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList == null) {
			logger.error("UniqueCollections request had no paramList.");
			return result;
		}
		//Add collections from request
		Set<String> uniq_colls = new HashSet<String>();
		Element collParam = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.COLLECTION_ELEM);
		if (collParam != null){
			String colls = GSXML.getValue(collParam);
			if (!colls.isEmpty())
			{
				String[] _colls = colls.split(",");
				for (int i=0;i < _colls.length;i++){
					uniq_colls.add(_colls[i]);
				}
			}
		}
		//groupParam check
		Element groupParam = GSXML.getNamedElement(paramList, GSXML.PARAM_ELEM, GSXML.NAME_ATT, GSXML.GROUP_ELEM);
		if (groupParam == null) {
			logger.error("UniqueCollections request had no groupParam.");
			return result;
		}
		//Add collections from groups
		String[] groups = null;
		groups = GSXML.getValue(groupParam).split(",");
		for (int i = 0; i < groups.length; i++) {
			Element groupContent = getRawCurrentContent(groups[i]);
			//If group exists
			if (groupContent != null) {
				NodeList collectionNodes = GSXML.getChildrenByTagName(groupContent, GSXML.COLLECTION_ELEM);
				for (int j = 0; j < collectionNodes.getLength(); j++) {
					String collName = ((Element) collectionNodes.item(j)).getAttribute(GSXML.NAME_ATT);
					uniq_colls.add(collName);
				}
			}
	
		}
		//Fill result collectionList 
		for (Iterator<String> iterator = uniq_colls.iterator(); iterator.hasNext();) {
			String collectionName = (String) iterator.next();
			Element checkedCollection = GSXML.getNamedElement(mrCollectionList, GSXML.COLLECTION_ELEM,GSXML.NAME_ATT, collectionName);
			if (checkedCollection != null){
				resultCollections.appendChild(doc.importNode(checkedCollection, true));
			}
		}
		return result;

	}
	
	protected Element processCollectionsHierarchy(Element request){
		
		Document doc = XMLConverter.newDOM();
		UserContext userContext = new UserContext(request);
		// Prepare basic response
		Element result = GSXML.createBasicResponse(doc, GSXML.SERVICE_TYPE_GROUPINFO);
		String currentPath = "";
		Element currentContent = getRawCurrentContent(currentPath);
		if (currentContent == null){
			return result;
		}
		Element searchableCollectionList = getSearchableCollectionList(userContext);
		if (searchableCollectionList == null){
			return result;
		}
		
		//Get ungrouped collection list
		sanitizeCurrentContent(currentContent, searchableCollectionList);
		addGroupInfo(currentContent, currentPath);
		addUngroupedCollections(currentContent, searchableCollectionList);
		addAllOption(currentContent);
		result.appendChild(doc.importNode(currentContent, true));
		return result;
	}
	
	private void addAllOption(Element currentContent) {
		if (currentContent == null){
			return;
		}
		Document doc = currentContent.getOwnerDocument();
		Element allOption = doc.createElement(GSXML.COLLECTION_ELEM);
		allOption.setAttribute(GSXML.NAME_ATT, "all");
		if (currentContent.hasChildNodes()){
			currentContent.insertBefore(allOption,currentContent.getFirstChild());	
		} else 
			currentContent.appendChild(allOption);
		
		
	}

	private void addGroupInfo(Element currentContent, String groupPath) {
		NodeList groups = GSXML.getChildrenByTagName(currentContent,GSXML.GROUP_ELEM);
		for (int i=0;i<groups.getLength();i++){
			Element group = (Element) groups.item(i);
			String name = group.getAttribute(GSXML.NAME_ATT);
			String newPath = groupPath + "/" + name; 
			group.setAttribute(GSXML.PATH_ATT, newPath);
			Element groupDescription = getGroupDescription(name);
			Element titleEl = (Element) GSXML.getChildByTagName(groupDescription, GSXML.TITLE_ELEM);
			String title;
			if (titleEl != null) {
				 title = titleEl.getTextContent();	
			} else {
				title = name;
			}
			group.setAttribute(GSXML.TITLE_ELEM, title	);
			addGroupInfo(group, newPath);
		}
		
	}

	private Element getRawCurrentContent(String path) {

		if (path == null) {
			 path = "";
		}

		Document doc = XMLConverter.newDOM();
		path = path.replaceAll("(/+)", "/");
		path = path.replaceAll("(^/+)|(/+$)", "");
		
		String[] pathSteps = path.split("/");

		
		Element currentContent = (Element) hierarchy.cloneNode(true);
		// Get the current view
		for (int i = 0; i < pathSteps.length; i++) {
			if (!pathSteps[i].isEmpty()) {
				currentContent = GSXML.getNamedElement(currentContent, GSXML.GROUP_ELEM, GSXML.NAME_ATT, pathSteps[i]);
				if (currentContent == null){
					break;
				}
			}
		}
		if (currentContent == null || !currentContent.hasChildNodes()) {
			// Return to the main page
			return null;
		}
		return currentContent;
	}
	
	private void sanitizeCurrentContent(Element currentContent, Element checkedCollectionList){
		if (currentContent == null){
			return;
		}
		NodeList nodes = currentContent.getElementsByTagName(GSXML.COLLECTION_ELEM);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			String name = element.getAttribute(GSXML.NAME_ATT);
			Element checkedCollection = GSXML.getNamedElement(checkedCollectionList, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, name);
			if (checkedCollection == null) {
				element.getParentNode().removeChild(element);
				i--;
			}
		}
	}
	
	private void addUngroupedCollections(Element currentContent, Element availableCollections){
		if (currentContent == null){
			return;
		}
		Document doc = currentContent.getOwnerDocument();
		NodeList collectionList = availableCollections.getElementsByTagName(GSXML.COLLECTION_ELEM);
		for (int i = 0; i < collectionList.getLength();i++){
			Element collection = (Element) collectionList.item(i);
			String name = collection.getAttribute(GSXML.NAME_ATT);
			NodeList foundCollection = GSXML.getNamedElements(currentContent, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, name);
			if (foundCollection.getLength() == 0){
				Element ungroupedCollection = doc.createElement(GSXML.COLLECTION_ELEM);
				ungroupedCollection.setAttribute(GSXML.NAME_ATT, name);
				currentContent.appendChild(ungroupedCollection);
			}
		}
		
	}

	private boolean readGroupConfiguration() {

		File configFile = new File(GSFile.groupConfigFile(site_home));
		//
		if (!configFile.exists()) {
			logger.info("Groups config file " + configFile.getPath() + " does not exist.");
			return false;
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
			logger.error("Error occurred while trying to remove emtpy nodes from groupConfig.xml");
			e.printStackTrace();
			return false;
		}  
		
		hierarchy = (Element) GSXML.getChildByTagName(content, GSXML.HIERARCHY_ELEM);
		groupDesc = (Element) GSXML.getChildByTagName(content, GSXML.GROUP_DESC_ELEM);
		if (hierarchy == null || groupDesc == null){
			logger.error("Error processing groups configuration. <groupConfig> should have both <hierarchy> and <groupDescriptions> elements. Check groupConfig.xml");
			return false;
		}
		verifyGroupDescription();
		return true;
	}

	private void verifyGroupDescription() {
		Document doc = groupDesc.getOwnerDocument();
		NodeList groups = hierarchy.getElementsByTagName(GSXML.GROUP_ELEM);
		for (int i = 0; i < groups.getLength(); i++) {
			Element group = (Element) groups.item(i); 
			String name = group.getAttribute(GSXML.NAME_ATT);
			Element foundDescription = GSXML.getNamedElement(groupDesc, GSXML.GROUP_ELEM, GSXML.NAME_ATT, name);
			if (foundDescription == null){
				Element defaultDescription = doc.createElement(GSXML.GROUP_ELEM); 
				defaultDescription.setAttribute(GSXML.NAME_ATT, name);
				Element groupTitle = doc.createElement(GSXML.TITLE_ELEM);
				groupTitle.setTextContent(name);
				defaultDescription.appendChild(groupTitle);
				groupDesc.appendChild(defaultDescription);
			}
		} 
		
	}

	private Element getGroupDescription(String name) {
		Element description = (Element) GSXML.getNamedElement(groupDesc, GSXML.GROUP_ELEM, GSXML.NAME_ATT, name);
		if (description == null) {
			logger.error("GroupDescription is not defined. Check your groupConfig.xml");
		}
		return description;
	}

	private Element getUngroupedCollections(Element mr_collection_list) {
		
		Document doc = XMLConverter.newDOM();
		// Create Set
		Set<String> hierarchy_unique_collections = new HashSet<String>();
		// Element hierarchyCollections = doc.createElement("coll_list");
		// Get collection nodes
		
		NodeList hierarchy_all_collection_list = hierarchy.getElementsByTagName(GSXML.COLLECTION_ELEM);
		// Save hierarchy collection names to Hashset
		for (int i = 0; i < hierarchy_all_collection_list.getLength(); i++) {
			Element collection_element = (Element) hierarchy_all_collection_list.item(i);
			hierarchy_unique_collections.add(collection_element.getAttribute(GSXML.NAME_ATT));
		}
		// Save available by message router collection names to Hashset
		Set<String> mr_collections = new HashSet<String>();
		NodeList mr_coll_list = mr_collection_list.getElementsByTagName(GSXML.COLLECTION_ELEM);

		for (int i = 0; i < mr_coll_list.getLength(); i++) {
			Element collection_element = (Element) mr_coll_list.item(i);
			mr_collections.add(collection_element.getAttribute(GSXML.NAME_ATT));
		}
		//Save collections available by message router and not existed in hierarchy to ungrouped collections set
		Set<String> ungrouped_collections = new HashSet<String>();
		for (String string : mr_collections) {
			if (!hierarchy_unique_collections.contains(string)){
				ungrouped_collections.add(string);
			}
		}
		//Output
		Element result = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		for (String name : ungrouped_collections) {
			Element collection = doc.createElement(GSXML.COLLECTION_ELEM);
			collection.setAttribute(GSXML.NAME_ATT, name);
			result.appendChild(collection);
		}
		return result;

	}
	private Element getPathInfo(String path) {

		Document doc = XMLConverter.newDOM();
		
		if (path == null) {
			 path = "";
		}
		String[] pathSteps = path.split("/");

		Element pathInfo = doc.createElement(GSXML.PATH_ELEM + GSXML.LIST_MODIFIER);
		
		String currentPath = "";
		for (int i = 0; i < pathSteps.length; i++) {
			if (!pathSteps[i].isEmpty()) {
				currentPath += "/" + pathSteps[i];
				Element pathStepDescription = getGroupDescription(pathSteps[i]);
				if (pathStepDescription != null){
					pathStepDescription.setAttribute(GSXML.POSITION_ATT, String.valueOf(i));
					pathStepDescription.setAttribute(GSXML.PATH_ATT, currentPath);
				}
				pathInfo.appendChild(doc.importNode(pathStepDescription, true));
			}
		}
		
		return pathInfo;
	}
	private Element getAvailableCollectionList(UserContext userContext){
		Document doc = XMLConverter.newDOM();
		// Get the message router info
		Element inforesponseMessage = new Request(doc, userContext, router, GSXML.REQUEST_TYPE_DESCRIBE).send();
		if (inforesponseMessage == null) {
			logger.error(" couldn't query the message router!");
			return null;
		}
		Element mr_info_response = (Element) GSXML.getChildByTagName(inforesponseMessage, GSXML.RESPONSE_ELEM);
		if (mr_info_response == null) {
			logger.error("Message router response is null!");
			return null;
		}
		
		Element mr_collection_list = (Element) GSXML.getChildByTagName(mr_info_response, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		
		return mr_collection_list;
	}
	private Element getSearchableCollectionList(UserContext userContext){
		Document doc = XMLConverter.newDOM();
		Element collectionList = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		// Get the message router info
		Element mr_info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element mr_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "TextQuery" , userContext);
		mr_info_message.appendChild(mr_request);
		Element mr_info_response_message = (Element) this.router.process(mr_info_message);
		if (mr_info_response_message == null) {
			logger.error(" couldn't query the message router!");
			return null;
		}
		NodeList options = mr_info_response_message.getElementsByTagName(GSXML.PARAM_OPTION_ELEM);
		for (int i = 0; i < options.getLength(); i++) {
			Element option = (Element) options.item(i);
			String name = option.getAttribute(GSXML.NAME_ATT);
			if (name.equals("all")){
				continue;
			}
			Element collection = doc.createElement(GSXML.COLLECTION_ELEM);
			collection.setAttribute(GSXML.NAME_ATT, name);
			collectionList.appendChild(collection);
		}
		return collectionList;
	}
}
