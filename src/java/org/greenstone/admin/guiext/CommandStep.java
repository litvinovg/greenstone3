package org.greenstone.admin.guiext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.greenstone.admin.GAI;
import org.greenstone.admin.LoggedMessageArea;

public class CommandStep extends Step
{
    protected ArrayList _commandsAndCallbacks = new ArrayList();
    protected LoggedMessageArea _messageArea = new LoggedMessageArea(this.getClass());
    protected int _threadCount = -1;

    /*
    Command[] _commands = null;
    Callback[] _callbacks = null;
    */

    public CommandStep(Element commandStepElement, SequenceList parent)
    {
	super(commandStepElement, parent);
	
	if(commandStepElement != null){
	    NodeList commandAndCallbackElements = commandStepElement.getElementsByTagName("*");

	    for(int i = 0; i < commandAndCallbackElements.getLength(); i++){
		Element currentChild = (Element)commandAndCallbackElements.item(i);
		if(currentChild.getTagName().equalsIgnoreCase(ExtXMLHelper.COMMAND)){
		    _commandsAndCallbacks.add(new Command(currentChild, this));
		}
		else if (currentChild.getTagName().equalsIgnoreCase(ExtXMLHelper.CALLBACK)){
		    _commandsAndCallbacks.add(new Callback(currentChild, this));
		}
		else if (currentChild.getTagName().equalsIgnoreCase(ExtXMLHelper.OS)){
		}
		else{
		    System.err.println(currentChild.getTagName() + "A child of this command <" + ExtXMLHelper.STEP + "> is not either a <" + ExtXMLHelper.CALLBACK + "> element or a <" + ExtXMLHelper.COMMAND  + "> element");
		}
	    }

	    if(_commandsAndCallbacks.size() == 0){
		System.err.println("This command step does not have any <" + ExtXMLHelper.COMMAND  + "> or <" + ExtXMLHelper.CALLBACK  +"> elements");
	    }

	    _button.addActionListener(new CommandButtonListener());

	}
	else{
	    System.err.println("This command <" + ExtXMLHelper.STEP + "> element is null");
	}
    }

    public class CommandButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    ExtensionInformation info = _parent.getParent();

	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());
	    panel.add(_messageArea, BorderLayout.CENTER);
	    info.changeExtPane(panel);

	    _button.setEnabled(false);
	    _button.setText("Running...");

	    _threadCount = 0;
	    Thread newThread = new Thread((Runnable)_commandsAndCallbacks.get(0));
	    newThread.start();
	}
    }

    public JTextArea getMessageArea()
    {
	return _messageArea;
    }

    public void threadError()
    {
	_button.setText(_label);
	_button.setEnabled(true);
    }

    public void startNextThread()
    {
	_threadCount++;

	if(_threadCount < _commandsAndCallbacks.size()){
	    Thread newThread = new Thread((Runnable)_commandsAndCallbacks.get(_threadCount));
	    newThread.start();
	}
	else{
	    _button.setText(_label);
	    _button.setEnabled(true);
	    _parent.registerStepCompletion(CommandStep.this);
	}
    }
}