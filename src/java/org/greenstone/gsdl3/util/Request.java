package org.greenstone.gsdl3.util;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class Request {
	
	private Document document = null;
	private UserContext userContext = null;
	private ModuleInterface operator = null;
	private String type = null;
	private String to = "";
/**
 * 
 * @param userContext
 * @param router
 * @param requestType
 * @param doc Document for request
 */
	public Request(Document doc, UserContext userContext, ModuleInterface router, String requestType) {
		assert(router != null);
		assert(requestType != null);
		assert(doc != null);
		this.document = doc;
		this.userContext = userContext;
		this.operator = router;
		this.type = requestType;
	}
/**
 * 
 * @param docList requested document list
 * @param paramList request parameters
 * @return
 */
	public Element send(Element docList, Element paramList) {
		Element message = document.createElement(GSXML.MESSAGE_ELEM);
		Element request = GSXML.createBasicRequest(document, type, to, userContext);
		message.appendChild(request);
		request.appendChild(paramList);
		request.appendChild(docList);
		Element response = (Element) operator.process(message);
		return response;
	}
	public Element send() {
		Element message = document.createElement(GSXML.MESSAGE_ELEM);
		Element request = GSXML.createBasicRequest(document, type, to, userContext);
		message.appendChild(request);
		Element response = (Element) operator.process(message);
		return response;
	}
	/**
	 * 
	 * @param collection target collection
	 * @param service target service
	 */
	public void setToCollectionService(String collection, String service) {
		this.to = GSPath.appendLink(collection, service);
	}
}
