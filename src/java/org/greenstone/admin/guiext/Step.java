package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;

public class Step
{
    protected SequenceList _parent = null;
    protected String _id = null;
    protected String _action = null;
    protected String _label = null;
    protected String _dependsOn = null;
    protected String _rollbacks = null;
    protected JButton _button = null;
    protected static JTextArea _commonMessageArea = new JTextArea();

    public Step(String id, String label, String dependsOn, String rollbacks, SequenceList parent)
    {
	_parent = parent;
	_id = id;
	_label = label;
	_dependsOn = dependsOn;
	_rollbacks = rollbacks;

	_button = new JButton(_label);
	_button.setEnabled(false);
    }

    public Step(Element stepElement, SequenceList parent)
    {
	_parent = parent;
	if(stepElement != null){
	    _id = stepElement.getAttribute("id");
	    if(_id.equals("")){
		System.err.println("This <" + ExtXMLHelper.STEP + "> element has no id attribute or the id attribute is empty");
	    }
	    
	    _label = stepElement.getAttribute("label");
	    if(_label.equals("")){
		System.err.println("This <" + ExtXMLHelper.STEP + "> element has no label attribute or the label attribute is empty");
	    }
	    
	    _action = stepElement.getAttribute("action");
	    if(_action.equals("")){
		System.err.println("This <" + ExtXMLHelper.STEP + "> element has no action attribute or the action attribute is empty");
	    }
	    
	    _dependsOn = stepElement.getAttribute("dependsOn");
	    String[] dependencies = _dependsOn.split(",");
	    for(int i = 0; i < dependencies.length; i++){
		if(dependencies[i].equals(_id)){
		    System.err.println("The " + _id + " step depends on itself which is not allowed");
		    _dependsOn = "";
		}
	    }

	    _rollbacks = stepElement.getAttribute("rollbackTo");

	    _button = new JButton(_label);
	    _button.setEnabled(false);
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.STEP + "> element is null");
	}
    }

    public JButton getButton()
    {
	return _button;
    }

    public String getId()
    {
	return _id;
    }

    public String getAction()
    {
	return _action;
    }

    public String getLabel()
    {
	return _label;
    }
    
    public String getDependsOn()
    {
	return _dependsOn;
    }

    public String getRollbacks()
    {
	return _rollbacks;
    }

    public SequenceList getParent()
    {
	return _parent;
    }

    public JTextArea getCommonMessageArea()
    {
	return _commonMessageArea;
    }
}