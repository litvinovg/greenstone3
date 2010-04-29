package org.greenstone.admin.guiext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class ExtXMLHelper
{
    static public final String EXTENSION = "extension";
    static public final String GROUP = "group";
    static public final String BASE_EXT = "base_ext";
    static public final String FILE_STEM = "file_stem";
    static public final String NAME = "name";
    static public final String DOWNLOAD = "download";
    static public final String MAIN_SOURCE = "mainSource";
    static public final String AUX_SOURCE = "auxSource";
    static public final String SOURCE = "source";
    static public final String INSTALL = "install";
    static public final String UNINSTALL = "uninstall";
    static public final String ENABLE = "enable";
    static public final String DISABLE = "disable";
    static public final String DESTINATION = "destination";
    static public final String DESCRIPTION = "description";
    static public final String PROPERTY = "property";
    static public final String SEQUENCE_LIST = "sequence_list";
    static public final String STEP ="step";
    static public final String COMMAND ="command";
    static public final String CALLBACK ="callback";
    static public final String OPTION_LIST = "optionList";
    static public final String OPTION = "option";
    static public final String CLASS = "class";
    static public final String OS = "os";

    private ExtXMLHelper(){}
    
    public static String getValueFromSingleElement(Element parent, String valueName, boolean strict)
    {
	if(parent == null){
	    System.err.println("Parent element is null");
	    return null;
	}
	else if(valueName == null){
	    System.err.println("Value name element is null");
	    return null;
	}

	Element e = getSingleChildElement(parent, valueName, strict);
	if(e == null){
	    System.err.println("Cannot get a value from element \"" + valueName +"\" as it does not exist in this parent");
	    return null;
	}

	String s = getValueFromSingleElement(e, strict);
	if(s == null){
	    if(strict){
		System.err.println("Cannot get a value from the element \"" + valueName + "\" as it is empty");
	    }
	    
	    return null;
	}

	return s;
    }

    public static String getValueFromSingleElement(Element e, boolean strict)
    {
	if(e == null){
	    System.err.println("Element is null");
	    return null;
	}

	Node textChild = e.getFirstChild();
	if(textChild == null){
	    if(strict){
		System.err.println("This element has no value");
	    }
	    return null;
	}

	String value = textChild.getNodeValue();
	if(value.equals("")){
	    if(strict){
		System.err.println("The value for this element is empty");
	    }
	    return null;
	}
	return value;
    }

    public static Element getSingleChildElement(Element parent, String elementName, boolean strict)
    {
	if(parent == null){
	    System.err.println("Parent element is null");
	    return null;
	}
	else if(elementName == null){
	    System.err.println("Element name is null");
	    return null;
	}

	NodeList nodeList = parent.getElementsByTagName(elementName);
	if(nodeList == null || nodeList.getLength() == 0){
	    if(strict){
		System.err.println("The element " + elementName + " does not exist in this parent element");
	    }
	    return null;
	}
	else if(nodeList.getLength() > 1){
	    System.err.println("The element " + elementName + " has more than one element in this parent element");
	    return null;
	}

	return (Element)nodeList.item(0);
    }
    
    public static Element[] getMultipleChildElements(Element parent, String elementName, boolean strict)
    {
	if(parent == null){
	    System.err.println("Parent element is null");
	    return null;
	}
	else if(elementName == null){
	    System.err.println("Element name is null");
	    return null;
	}

	NodeList nodeList = parent.getElementsByTagName(elementName);
	if(nodeList == null || nodeList.getLength() == 0){
	    if(strict){
		System.err.println("The element " + elementName + " does not exist in this parent element");
	    }
	    return null;
	}
	
	Element[] elements = new Element[nodeList.getLength()];
	
	for(int i = 0; i < nodeList.getLength(); i++){
	    elements[i] = (Element)nodeList.item(i);
	}

	return elements;
    }
}