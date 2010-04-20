package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

public class OptionList
{
    Option[] _options = null;
    String _label = null;
    String _id = null;
    String _file = null;
    PropertiesStep _parent = null;
    
    public OptionList(Element optionListElement, PropertiesStep parent, boolean fromFile)
    {
	_parent = parent;

	if(optionListElement != null){
	    _label = optionListElement.getAttribute("label");
	    _id = optionListElement.getAttribute("id");
	    _file = optionListElement.getAttribute("file");
	    
	    Element[] optionElements = ExtXMLHelper.getMultipleChildElements(optionListElement, ExtXMLHelper.OPTION, true);
	    if(optionElements == null){
		System.err.println("This optionList element does not contain any option elements");
	    }

	    Properties properties = null;
	    if(fromFile){
		File propertiesFile = null;
		if(_file.equals("")){
		    propertiesFile = new File(_parent.getParent().getParent().getExtensionDirectory() + System.getProperty("file.separator") + "build.properties");
		}
		else{
		    propertiesFile = new File(_parent.getParent().getParent().getExtensionDirectory() + System.getProperty("file.separator") + _file);
		}

		if(propertiesFile.exists()){
		    properties = new Properties();
		    
		    try{
			properties.load(new FileInputStream(propertiesFile));
		    }
		    catch(Exception ex){
			System.err.println("Error loading previous properties values from the properties file, using default values");
			//ex.printStackTrace();
		    }
		}
	    }
	    
	    _options = new Option[optionElements.length];
	    for(int i = 0; i < optionElements.length; i++){
		_options[i] = new Option(optionElements[i], this);

		if(fromFile && properties != null){
		    String value = null;
		    if(_id.equals("")){
			value = properties.getProperty(_options[i].getId());
		    }
		    else{
			value = properties.getProperty(_id + "." + _options[i].getId());
		    }

		    if(value != null){
			_options[i].setValue(value);
		    }
		}
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.OPTION_LIST  + "> element is null");
	}
    }

    public Option[] getOptions()
    {
	return _options;
    }

    public String getLabel()
    {
	return _label;
    }

    public String getId()
    {
	return _id;
    }

    public String getFilename()
    {
	return _file;
    }
    
    public PropertiesStep getParent()
    {
	return _parent;
    }
}