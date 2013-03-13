package org.greenstone.gsdl3.util;

import org.w3c.dom.Element;

public class UserContext
{
	protected String _userID = null;
	protected String _lang = null;
	protected String[] _groups = null;

	public UserContext()
	{
	}

	public UserContext(Element xmlRequest)
	{
		_lang = xmlRequest.getAttribute(GSXML.LANG_ATT);
		_userID = xmlRequest.getAttribute(GSXML.USER_ID_ATT);
		_groups = xmlRequest.getAttribute(GSXML.GROUPS_ATT).split(",");
	}

	public UserContext(String lang, String userID, String[] groups)
	{
		_lang = lang;
		_userID = userID;
		_groups = groups;
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

	public String[] getGroups()
	{
		if (_groups != null)
		{
			return _groups;
		}
		return new String[0];
	}
}