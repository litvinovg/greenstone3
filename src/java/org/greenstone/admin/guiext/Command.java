package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;

public class Command implements Runnable
{
    HashMap _osCommands = new HashMap();
    CommandStep _parent = null;

    public Command(Element commandElement, CommandStep parent) 
    {
	_parent = parent;						

	if(commandElement != null){
	    Element[] commands = ExtXMLHelper.getMultipleChildElements(commandElement, "os", true);
	    for(int i = 0; i < commands.length; i++){
		Element currentCommand = commands[i];
		String os = currentCommand.getAttribute("name");

		if(os.equals("")){
		    System.err.println("A <" + ExtXMLHelper.COMMAND + "> element does not have an \"os\" attribute");
		}
		else{
		    _osCommands.put(os, ExtXMLHelper.getValueFromSingleElement(currentCommand, true));
		}
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.COMMAND + "> element is null");
	}
    }

    public void run()
    {
	JTextArea messageArea = _parent.getMessageArea();
	String command = getCommandForCurrentOS();
	
	if(command == null){
	    System.err.println("No commands exist for your current operating system, to avoid seeing this warning please place <os name=\"default\"/> into the set of commands for this step in the extension_project_list.xml file located in your extension directory");
	    _parent.threadError();
	    return;
	}
	else if(command == ""){
	    _parent.threadError();
	    return;
	}
		
	File workingDirectory = new File(_parent.getParent().getParent().getExtensionDirectory());
	Process commandLineProc = null;
	try{
	    commandLineProc = null;
	    
	    if(System.getProperty("os.name").contains("Windows")){
		String[] args = new String[3];
		args[0] = "cmd.exe";
		args[1] = "/C";
		args[2] = command;

		String allArgs = new String();
		for(int i = 0; i < args.length; i++){
		    if(i != 0){allArgs += " ";}
		    allArgs += args[i];
		}

		messageArea.append("\nExecuting \"" + allArgs + "\" on the command line\n");

		commandLineProc = Runtime.getRuntime().exec(args, null, workingDirectory);
	    }
	    else{
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = command;

		String allArgs = new String();
		for(int i = 0; i < args.length; i++){
		    if(i != 0){allArgs += " ";}
		    allArgs += args[i];
		}

		messageArea.append("\nExecuting \"" + allArgs + "\" on the command line\n");
		commandLineProc = Runtime.getRuntime().exec(args, null, workingDirectory);
	    }
	    
	    BufferedReader stdInput = new BufferedReader(new InputStreamReader(commandLineProc.getInputStream()));
	    BufferedReader errInput = new BufferedReader(new InputStreamReader(commandLineProc.getErrorStream()));
	    String s= "";
	    
	    while ((s = stdInput.readLine()) != null) {
		messageArea.append(s + "\n");
		messageArea.setSelectionEnd(messageArea.getDocument().getLength());
	    }  
	    
	    boolean error = false;
	    while ((s = errInput.readLine()) != null){
		messageArea.append(s + "\n");
		error = true;
	    }  
	    
	    if(error){
		JOptionPane.showMessageDialog(new JFrame(), "There was an error while running the command line process");	
		_parent.threadError();
		return;
	    }
	    
	    int success = commandLineProc.waitFor();
	    
	    if(success != 0){
		System.err.println("Command line process \"" + command  + "\" returned unsuccessfully with the value \"" + success + "\"");
		_parent.threadError();
		return;
	    }
	}
	catch(Exception ex){
	    ex.printStackTrace();
	    _parent.threadError();
	    return;
	} 
	_parent.startNextThread();
    }

    public boolean executeCommand()
    {
	return false;
    }

    public CommandStep getParent()
    {
	return _parent;
    }

    public String getCommandForCurrentOS()
    {
	String currentos = System.getProperty("os.name");

	String command = (String)_osCommands.get(currentos);
	if(command != null){
	    return command;
	}
	
	if(currentos.contains("Windows")){
	    command = (String)_osCommands.get("Windows");
	    
	    if(command != null){
		return command;
	    }   
	}
	return (String)_osCommands.get("default");
    }
}