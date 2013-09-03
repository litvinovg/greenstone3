package org.greenstone.gsdl3.service;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.greenstone.gsdl3.util.DerbyWrapper;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Element;

public class UserTracker extends ServiceRack
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.DocXMLUtil.class.getName());

	/**************************************************
	 * The list of services the User Tracker supports *
	 *************************************************/
	protected static final String RECORD_USER_ACTION = "RecordUserAction";
	protected static final String GET_ACTIVITY_ON_PAGE = "GetActivityOnPage";
	/*************************************************/

	String[] services = { RECORD_USER_ACTION, GET_ACTIVITY_ON_PAGE };

	/** configure this service */
	public boolean configure(Element info, Element extra_info)
	{
		if (!super.configure(info, extra_info))
		{
			return false;
		}

		logger.info("Configuring DocXMLUtil...");
		this.config_info = info;

		for (int i = 0; i < services.length; i++)
		{
			Element service = this.doc.createElement(GSXML.SERVICE_ELEM);
			service.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
			service.setAttribute(GSXML.NAME_ATT, services[i]);
			this.short_service_info.appendChild(service);
		}

		return true;
	}

	protected Element getServiceDescription(String service_id, String lang, String subset)
	{
		for (int i = 0; i < services.length; i++)
		{
			if (service_id.equals(services[i]))
			{
				Element service_elem = this.doc.createElement(GSXML.SERVICE_ELEM);
				service_elem.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_RETRIEVE);
				service_elem.setAttribute(GSXML.NAME_ATT, services[i]);
				return service_elem;
			}
		}

		return null;
	}

	protected synchronized Element processRecordUserAction(Element request)
	{
		Element result = GSXML.createBasicResponse(this.doc, RECORD_USER_ACTION);

		Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
		if (paramList == null)
		{
			GSXML.addError(this.doc, result, "Request has no parameter list");
			return result;
		}

		HashMap<String, Serializable> params = GSXML.extractParams(paramList, true);
		String username = (String) params.get("username");
		String collection = (String) params.get("collection");
		String site = (String) params.get("site");
		String oid = (String) params.get("oid");
		String action = (String) params.get("action");

		DerbyWrapper database = new DerbyWrapper(GlobalProperties.getGSDL3Home() + File.separatorChar + "etc" + File.separatorChar + "usersDB");
		database.addUserAction(username, site, collection, oid, action);
		database.closeDatabase();

		return result;
	}

	protected synchronized Element processGetActivityOnPage(Element request)
	{
		System.err.println("CALLED");
		Element result = GSXML.createBasicResponse(this.doc, GET_ACTIVITY_ON_PAGE);
		try
		{

			Element paramList = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM + GSXML.LIST_MODIFIER);
			if (paramList == null)
			{
				System.err.println("WHA?");
				GSXML.addError(this.doc, result, "Request has no parameter list");
				return result;
			}

			HashMap<String, Serializable> params = GSXML.extractParams(paramList, true);
			String collection = (String) params.get("collection");
			String site = (String) params.get("site");
			String oid = (String) params.get("oid");

			DerbyWrapper database = new DerbyWrapper(GlobalProperties.getGSDL3Home() + File.separatorChar + "etc" + File.separatorChar + "usersDB");
			ArrayList<HashMap<String, String>> userActions = database.getMostRecentUserActions(site, collection, oid);

			System.err.println(userActions.size());

			Element userList = this.doc.createElement("userList");
			for (HashMap<String, String> userAction : userActions)
			{
				Element user = this.doc.createElement("user");
				user.setAttribute("username", userAction.get("username"));
				user.setAttribute("action", userAction.get("action"));
				userList.appendChild(user);
			}
			result.appendChild(userList);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
}