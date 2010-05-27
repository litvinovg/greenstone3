package org.greenstone.admin.guiext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.greenstone.admin.GAI;

import org.w3c.dom.Element;

public class Option
{
    protected String _type = null;
    protected String _name = null;
    protected String _id = null;
    protected String _value = null;    
    protected OptionList _parent = null;
    
    protected Object _classObject = null;
    protected Method _checkMethod = null;

    protected ImageIcon _image = null;

    private boolean _canCheck = false;
    private int _checkResult = -1;

    private final String GOOD_ICON = GAI.getGSDL3Home() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "icongreentick.png";
    private final String BAD_ICON = GAI.getGSDL3Home() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "iconredexclamation.png";
    private final String ERROR_ICON = GAI.getGSDL3Home() + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "iconorangetriange.png";

    public Option(Element optionElement, OptionList parent)
    {
	_parent = parent;

	if(optionElement != null){
	    _id = optionElement.getAttribute("id");
	    if(_id == null || _id.equals("")){
		System.err.println("This option element does not have an id");
	    }

	    _name = optionElement.getAttribute("label");
	    if(_name == null || _name.equals("")){
		_name = _id;
	    }
	    
	    _value = ExtXMLHelper.getValueFromSingleElement(optionElement, false);
	    
	    if(_value == null){
		_value = "";
	    }

	    String checkClass = optionElement.getAttribute("checkClass");
	    String checkMethod = optionElement.getAttribute("checkMethod");

	    if(!checkClass.equals("") && !checkMethod.equals("")){
		loadCheckMethod(checkClass, checkMethod);
	    }

	    performCheck();

	    _type = optionElement.getAttribute("type");
	    
	    if(!(_type.equals("") || _type.equals("password") || _type.equals("filebrowse") || _type.equals("folderbrowse"))){
		System.err.println("This option element specifies a type that is not \"password\", \"filebrowse\" or \"folderbrowse\"");	    
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.OPTION + "> element is null");
	}
    }

    private void loadCheckMethod(String checkClassString, String checkMethodString)
    {
	_parent.getParent().getParent().getParent().loadGuiExtFile();
	
	Class checkClass = null;
	try{
	    checkClass = Class.forName(checkClassString);
	}
	catch(Exception ex){
	    System.err.println("Could not get an instance of the check class, either the class name is incorrect or the class does not exist inside the guiext.jar file");
	    return;
	}
	
	try{
	    Constructor classConstructor = checkClass.getConstructor(new Class[0]);
	    _classObject = classConstructor.newInstance(new Object[0]);
	}
	catch(Exception ex){
	    System.err.println("Could not instantiate the check class, either the class name is incorrect or the class does not exist inside the guiext.jar file");
	    return;
	}

	try{
	    _checkMethod = checkClass.getDeclaredMethod(checkMethodString, new Class[]{Object.class});
	}
	catch(Exception ex){
	    System.err.println("Could not get the check method, either the method name is incorrect or the method does not take an Object as a parameter");
	    return;
	}
	   
	if(!(_checkMethod.getReturnType().equals(Boolean.TYPE))){
	    System.err.println("The check method does not return a boolean value as is required");
	    return;
	}
	_canCheck = true;
    }

    private void setImageFromCheck()
    {
	if(_canCheck){
	    if(_checkResult == 0){
		_image = new ImageIcon(GOOD_ICON);
	    }
	    else if(_checkResult == 1){
		_image = new ImageIcon(BAD_ICON);
	    }
	    else{
		_image = new ImageIcon(ERROR_ICON);
	    }
	}
	else{
	    _image = new ImageIcon(GOOD_ICON);
	}
    }
    
    private void performCheck()
    {
	if(_canCheck){
	    Boolean result = null;
	    try{
		result = ((Boolean)_checkMethod.invoke(_classObject, new Object[]{_value}));
	    }
	    catch(Exception ex){
		_checkResult = -1;
	    }
	    if(result){
		_checkResult = 0;
	    }
	    else{
		_checkResult = 1;
	    }
	}
	setImageFromCheck();
    }

    public boolean isCheckable()
    {
	return _canCheck;
    }

    public int getCheckResult()
    {
	return _checkResult;
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
	performCheck();

	JTable table = _parent.getParent().getTableFromOptionList(_parent);
	
	if(table != null){
	    table.repaint();
	    table.revalidate();
	}
    }

    public String getId()
    {
	return _id;
    }
    
    public OptionList getParent()
    {
	return _parent;
    }
    
    public String getType()
    {
	return _type;
    }

    public ImageIcon getImage()
    {
	return _image;
    }
}