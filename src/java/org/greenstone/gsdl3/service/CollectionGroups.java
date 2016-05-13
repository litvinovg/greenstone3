package org.greenstone.gsdl3.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.GSDocumentModel;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.UserContext;
import org.greenstone.gsdl3.util.XMLConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CollectionGroups extends ServiceRack {
	
	private Element hierarchy; 
	private Element groupDesc;

	private static final String GROUP_CONTENT = "GroupCurrentContent";
	
	
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.BerryBasket.class.getName());

	@Override
	protected Element getServiceDescription(Document doc, String service, String lang, String subset) {
		// TODO Auto-generated method stub
		return null;
	}
	
	String[] _services = { GROUP_CONTENT};


	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring CollectionGroups...");
		this.config_info = info;

		for (int i = 0; i < _services.length; i++)
		{
			Element service = this.desc_doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_GROUPINFO);
			service.setAttribute(GSXML.NAME_ATT, _services[i]);
			this.short_service_info.appendChild(service);
		}
		//Load group configuration from file
		readGroupConfiguration();

		return true;
	}
	protected Element processGroupCurrentContent(Element request){
		
		Document doc = XMLConverter.newDOM();
		
		UserContext userContext = new UserContext(request);
		
		
		Element response = GSXML.createBasicResponse(doc, GSXML.SERVICE_TYPE_GROUPINFO);
		
		// first, get the message router info
		Element info_message = doc.createElement(GSXML.MESSAGE_ELEM);
		Element coll_list_request = GSXML.createBasicRequest(doc, GSXML.REQUEST_TYPE_DESCRIBE, "", userContext);
		info_message.appendChild(coll_list_request);
		Element info_response_message = (Element) this.router.process(info_message);
		if (info_response_message == null)
		{
			logger.error(" couldn't query the message router!");
			return null;
		}
		Element info_response = (Element) GSXML.getChildByTagName(info_response_message, GSXML.RESPONSE_ELEM);
		if (info_response == null)
		{
			logger.error("couldn't query the message router!");
			return null;
		}
		
		Element collection_list = (Element) GSXML.getChildByTagName(info_response, GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
		Element groupContent = getCurrentContent("////group2////group3///");
		
		// Temporary
		//If groupContent is empty return all collections
		if (groupContent == null) {
			response.appendChild(collection_list);
			return response;	
		}
		NodeList currentContent = groupContent.getChildNodes();
		if (currentContent != null && currentContent.getLength() > 0) {
			//Create CollectionList element in response
			Element collection_list_element = doc.createElement(GSXML.COLLECTION_ELEM + GSXML.LIST_MODIFIER);
			response.appendChild(collection_list_element);
			Element group_list_element = doc.createElement(GSXML.GROUP_ELEM + GSXML.LIST_MODIFIER);
			response.appendChild(group_list_element);

			//Iterate over collections from current view
			for (int i = 0; i < currentContent.getLength(); i++) {
				if (currentContent.item(i).getNodeName() == GSXML.COLLECTION_ELEM) {
					Element collection = (Element) currentContent.item(i);
					String collection_name = collection.getAttribute(GSXML.NAME_ATT);
					// Check wether collection from current view available also by Message Router
					Element checkedCollection = GSXML.getNamedElement(collection_list, GSXML.COLLECTION_ELEM, GSXML.NAME_ATT, collection_name);
					if ( checkedCollection != null) {
						//Add collection to response
						collection_list_element.appendChild(doc.importNode(checkedCollection, true));
					}	
				} else if (currentContent.item(i).getNodeName() == GSXML.GROUP_ELEM) {
					
					Element currentGroup = (Element) currentContent.item(i);
					String currentGroupName = currentGroup.getAttribute(GSXML.NAME_ATT);
					Element group = getGroupDescription(currentGroupName);
					if (group != null) {
						group_list_element.appendChild(doc.importNode(group, true));
					}
					
				}
				
					
			}
			
		}
		
		return response;
	}
	
	protected Element getCurrentContent(String path){
		
		Document doc = XMLConverter.newDOM();
		path = path.replaceAll("(/+)", "/");
		path = path.replaceAll("(^/+)|(/+$)", "");
		
	
		if (path == null || path.isEmpty()) {
			//Return to the main page
			
		}
		
		String[] pathSteps = path.split("/");
		
		Element currentView = hierarchy;
		//Get the current view
		for (int i = 0; i < pathSteps.length; i++) {
			currentView = GSXML.getNamedElement(currentView, GSXML.GROUP_ELEM, GSXML.NAME_ATT, pathSteps[i]);
		//	logger.warn("STEP" + i + "CURRENTVIEW--------------------------------------------------------------------------");
		//	logger.warn(GSXML.elementToString(currentView, true));
		}
		if (!currentView.hasChildNodes()){
			//Return to the main page
			return null;
		}
		//logger.warn("CURRENT CONTENT---------------------------------------------------------------------------------------");
		//logger.warn(GSXML.elementToString(currentView, true));
		
		
		
		return currentView;
		
	}
	
	protected boolean readGroupConfiguration(){
		
		File configFile = new File(GSFile.groupConfigFile(site_home));
		if (!configFile.exists()){
			logger.error("Groups config file " + configFile.getPath() + " does not exists");
			return false;
		}
		Document doc = XMLConverter.getDOM(configFile);
		
		Element content = doc.getDocumentElement();
		hierarchy = (Element) GSXML.getChildByTagName(content, GSXML.HIERARCHY_ELEM);
		groupDesc = (Element) GSXML.getChildByTagName(content, GSXML.GROUP_DESC_ELEM);
		
		//TEST
		
		//doc.getElementsByTagName(/arg0)
		//logger.error("HIERARCHY ---------------------------------------------------------------------------------------");
		//logger.error(GSXML.elementToString(hierarchy, true));
		//logger.error("GROUPDESC ---------------------------------------------------------------------------------------");
		//logger.error(GSXML.elementToString(groupDesc, true));
		
		return true;
	}
	protected Element getGroupDescription(String name){
		if (groupDesc != null) {
			//logger.error(GSXML.elementToString(groupDesc, true));
			Element description = (Element) GSXML.getNamedElement(groupDesc, GSXML.GROUP_ELEM, GSXML.NAME_ATT, name);
			if (description != null) {
				return description;
			}
			
		} else {
			logger.error("GroupDescription is not defined.");
		}
		//In case 
		return null;
	}
	
}
