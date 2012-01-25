package org.greenstone.gsdl3.util;

import org.w3c.dom.Element;

public class UserContext
{
	protected String _userID = null;
	protected String _lang = null;
	
	public UserContext(){}
	
	public UserContext(Element xmlRequest)
	{
		_lang = xmlRequest.getAttribute(GSXML.LANG_ATT);
		_userID = xmlRequest.getAttribute(GSXML.USER_ID_ATT);
	}
	
	public void setLanguage(String lang)
	{
		_lang = lang;
	}
	
	public void setUserID(String userID)
	{
		_userID = userID;
	}
	
	public String getLanguage()
	{
		if(_lang != null)
		{
			return _lang;
		}
		return "";
	}
	
	public String getUserID()
	{
		if(_userID != null)
		{
			return _userID;
		}
		return "";
	}
}