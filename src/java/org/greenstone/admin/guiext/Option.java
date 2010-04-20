package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

public class Option
{
    String _name = null;
    String _id = null;
    String _value = null;    
    OptionList _parent = null;

    public Option(Element optionElement, OptionList parent)
    {
	_parent = parent;

	if(optionElement != null){
	    _name = optionElement.getAttribute("label");
	    if(_name == null || _name.equals("")){
		System.err.println("This option element does not have a label");
	    }

	    _id = optionElement.getAttribute("id");
	    if(_id == null || _id.equals("")){
		System.err.println("This option element does not have an id");
	    }
	    
	    _value = ExtXMLHelper.getValueFromSingleElement(optionElement, false);
	    
	    if(_value == null){
		_value = "";
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.OPTION + "> element is null");
	}
    }
    
    public String getName()
    {
	return _name;
    }

    public String getValue()
    {
	return _value;
    }

    public void setValue(String value)
    {
	_value = value;
    }

    public String getId()
    {
	return _id;
    }
    
    public OptionList getParent()
    {
	return _parent;
    }
}