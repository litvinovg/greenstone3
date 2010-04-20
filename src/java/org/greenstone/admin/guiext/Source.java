package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

public class Source
{
    String _method = null;
    String _url = null;
    String _folder = null;
    DownloadStep _parent = null;

    public Source(String method, String url, String folder, DownloadStep parent)
    {
	_parent = parent;
	_method = method;
	_url = url;
	_folder = folder;
    }

    public Source(Element sourceElement, DownloadStep parent)
    {
	_parent = parent;

	if(sourceElement != null){
	    _method = sourceElement.getAttribute("method");
	    if(_method.equals("")){
		System.err.println("This <" + ExtXMLHelper.SOURCE + "> element does not contain a method attribute");
	    }

	    _folder = sourceElement.getAttribute("folder");
	    
	    _url = ExtXMLHelper.getValueFromSingleElement(sourceElement, true);
	    if(_url == null || _url.equals("")){
		System.err.println("This <" + ExtXMLHelper.SOURCE + "> element does not contain a url");
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.SOURCE + "> element is null");
	}
    }

    public DownloadStep getParent()
    {
	return _parent;
    }

    public String getMethod()
    {
	return _method;
    }

    public String getURL()
    {
	return _url;
    }

    public String getFolder()
    {
	return _folder;
    }
}