package org.greenstone.admin.guiext;

import java.lang.reflect.Method;
import javax.swing.JTextArea;

import org.w3c.dom.Element;

public class Callback implements Runnable
{
    String _param = null;
    CommandStep _parent = null;
    
    public Callback(Element callbackElement, CommandStep parent)
    {
	_parent = parent;

	if(callbackElement != null){
	    _param = ExtXMLHelper.getValueFromSingleElement(callbackElement, true);
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.CALLBACK + "> element is null");
	}
    }

    public void run()
    {
	JTextArea messageArea = _parent.getMessageArea();

	Object extObj = _parent.getParent().getParent().getExtObject();
	Class[] params = new Class[]{String.class};
	
	Method callbackMethod = null;
	try{
	    callbackMethod = extObj.getClass().getDeclaredMethod("doCallback", params);
	}
	catch(Exception ex){
	    System.err.println("Specified class does not have a doCallback(String) method");
	    _parent.threadError();
	    return;
	}

	messageArea.append("\nCalling the callback method with \"" + _param +"\" as an argument");
	Boolean success = null;
	try{
	    success = (Boolean)callbackMethod.invoke(extObj, (Object)_param);
	}
	catch(Exception ex){
	    System.err.println("Error while attempting to invoke specified method -> ");
	    ex.printStackTrace();
	    _parent.threadError();
	    return;
	}
	
	if(!success){
	    System.err.println("Call to doCallback method with the value \"" + _param + "\" returned unsuccessfully");
	    _parent.threadError();
	    return;
	}

	_parent.startNextThread();
    }
    
    public String getParam()
    {
	return _param;
    }
    
    public CommandStep getParent()
    {
	return _parent;
    }
}