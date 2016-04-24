package org.greenstone.gsdl3.core;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GSHttpServletRequestWrapper extends HttpServletRequestWrapper
{
    private HashMap<String, String[]> _newParams = new HashMap<String, String[]>();
    
    public GSHttpServletRequestWrapper(ServletRequest request)
    {
	super((HttpServletRequest) request);
    }
    
    public void setParameter(String paramName, String[] paramValues)
    {
	_newParams.put(paramName, paramValues);
    }
    
    public void setParameter(String paramName, String paramValue)
    {
	_newParams.put(paramName, new String[] { paramValue });
    }
    
    public String getParameter(String paramName)
    {
	if (super.getParameter(paramName) != null)
	    {
		return super.getParameter(paramName);
	    }
	else
	    {
		if (_newParams.get(paramName) != null && _newParams.get(paramName)[0] != null)
		    {
			return _newParams.get(paramName)[0];
		    }
		return null;
	    }
    }
    
    public String[] getParameterValues(String paramName)
    {
	if (super.getParameterValues(paramName) != null)
	    {
		return super.getParameterValues(paramName);
	    }
	else
	    {
		return _newParams.get(paramName);
	    }
    }
    
    public Map<String, String[]> getParameterMap()
    {
	HashMap<String, String[]> returnMap = new HashMap<String, String[]>();
	returnMap.putAll(super.getParameterMap());
	returnMap.putAll(_newParams);
	return returnMap;
    }
}
