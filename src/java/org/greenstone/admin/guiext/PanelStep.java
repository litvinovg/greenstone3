package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.lang.reflect.Constructor;

public class PanelStep extends Step
{
    String _controlPanelClass = null;

    public PanelStep(Element panelStepElement, SequenceList parent)
    {
	super(panelStepElement, parent);

	if(panelStepElement != null){

	    _controlPanelClass = ExtXMLHelper.getValueFromSingleElement(panelStepElement, ExtXMLHelper.CLASS, true);

	    if(_controlPanelClass == null || _controlPanelClass.equals("")){
		System.err.println("The <" + ExtXMLHelper.CLASS + "> element inside this panel <" + ExtXMLHelper.STEP + "> either does not exist or is empty");
	    }
	    
	    _button.addActionListener(new PanelButtonListener());   
	}
	else{
	     System.err.println("This panel <" + ExtXMLHelper.STEP + "> element is null");
	}
    }

    public class PanelButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    _parent.getParent().loadGuiExtFile();

	    Class controlPanelClass = null;
	    try{
		controlPanelClass = Class.forName(_controlPanelClass);
	    }
	    catch(Exception ex){
		ex.printStackTrace();
		return;
	    }

	    Object classObj = null;
	    try{
		Constructor classConstructor = controlPanelClass.getConstructor(new Class[0]);
		classObj = classConstructor.newInstance(new Object[0]);
	    }
	    catch(Exception ex){
		System.err.println("Could not create the control panel class, either the class name is incorrect or the class does not exist inside the guiext.jar file");
		return;
	    }
		
	    _parent.getParent().changeExtPane(((BasePanel)(classObj)).getPanel());

	    _parent.registerStepCompletion(PanelStep.this);
	}
    }
}