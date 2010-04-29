package org.greenstone.admin.guiext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.JTextArea;

import org.w3c.dom.Element;

public class Callback implements Runnable
{
    String _class = null;
    String _param = null;
    CommandStep _parent = null;
    
    public Callback(Element callbackElement, CommandStep parent)
    {
	_parent = parent;

	if(callbackElement != null){
	    _class = ExtXMLHelper.getValueFromSingleElement(callbackElement, true);
	    _param = callbackElement.getAttribute("type");
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.CALLBACK + "> element is null");
	}
    }

    public void run()
    {
	_parent.getParent().getParent().loadGuiExtFile();
	JTextArea messageArea = _parent.getMessageArea();

	Class extClass = null;
	try{
	    extClass = Class.forName(_class);
	}
	catch(Exception ex){
	    System.err.println("Could not create the extension class used for callback methods, either the class name is incorrect or the class does not exist inside the guiext.jar file");
	}
	Constructor classConstructor = null;
	Object extObj = null;
	
	try{
	    classConstructor = extClass.getConstructor(new Class[0]);
	    extObj = classConstructor.newInstance(new Object[0]);
	}
	catch(Exception ex){
	    ex.printStackTrace();
	    System.err.println("Could not create the extension class used for callback methods, either the class name is incorrect or the class does not exist inside the guiext.jar file");
	    return;
	}

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