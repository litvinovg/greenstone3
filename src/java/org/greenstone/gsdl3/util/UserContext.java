package org.greenstone.gsdl3.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UserContext
{
	protected String _userID = null;
	protected String _username = null;
	protected String _lang = null;
	protected String[] _groups = null;

	public UserContext()
	{
	}

	public UserContext(Element xmlRequest)
	{
		NodeList elems = xmlRequest.getElementsByTagName("userContext");

		if (elems.getLength() > 0)
		{
			Element userContext = (Element) elems.item(0);
			_userID = userContext.getAttribute(GSXML.USER_ID_ATT);
			_username = userContext.getAttribute(GSXML.USERNAME_ATT);
			_lang = userContext.getAttribute(GSXML.LANG_ATT);
			_groups = userContext.getAttribute(GSXML.GROUPS_ATT).split(",");
		}
	}

	public UserContext(String lang, String username, String userID, String[] groups)
	{
		_userID = userID;
		_username = username;
		_lang = lang;
		_groups = groups;
	}

	public void setUsername(String username)
	{
		_username = username;
	}

	public void setLanguage(String lang)
	{
		_lang = lang;
	}

	public void setUserID(String userID)
	{
		_userID = userID;
	}

	public void setGroups(String[] groups)
	{
		_groups = groups;
	}

	public String getLanguage()
	{
		if (_lang != null)
		{
			return _lang;
		}
		return "";
	}

	public String getUserID()
	{
		if (_userID != null)
		{
			return _userID;
		}
		return "";
	}

	public String getUsername()
	{
		if (_username != null)
		{
			return _username;
		}
		return "";
	}

	public String[] getGroups()
	{
		if (_groups != null)
		{
			return _groups;
		}
		return new String[0];
	}
}