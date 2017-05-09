package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;

import org.greenstone.util.SafeProcess;

public class Command implements Runnable
{
    HashMap<String, String> _osCommands = new HashMap<String, String>();
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
		    _osCommands.put(os, ExtXMLHelper.getValueFromSingleElement(currentCommand, false));
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
	SafeProcess commandLineProc = null;
	try{
	    commandLineProc = null;
	    String[] args = new String[3];
	    
	    if(System.getProperty("os.name").contains("Windows")){
		args[0] = "cmd.exe";
		args[1] = "/C";
		args[2] = command;

		String allArgs = new String();
		for(int i = 0; i < args.length; i++){
		    if(i != 0){allArgs += " ";}
		    allArgs += args[i];
		}

		messageArea.append("\nExecuting \"" + allArgs + "\" on the command line\n");

		//commandLineProc = Runtime.getRuntime().exec(args, null, workingDirectory);
	    }
	    else{
		args[0] = "sh";
		args[1] = "-c";
		args[2] = command;

		String allArgs = new String();
		for(int i = 0; i < args.length; i++){
		    if(i != 0){allArgs += " ";}
		    allArgs += args[i];
		}

		messageArea.append("\nExecuting \"" + allArgs + "\" on the command line\n");
		//commandLineProc = Runtime.getRuntime().exec(args, null, workingDirectory);
	    }

	    commandLineProc = new SafeProcess(args, null, workingDirectory); 

	    /*
	    BufferedReader stdInput = new BufferedReader(new InputStreamReader(commandLineProc.getInputStream()));

	    Thread stdPrinter = new PrinterThread(messageArea, stdInput);
	    stdPrinter.start();

	    BufferedReader stdError = new BufferedReader(new InputStreamReader(commandLineProc.getErrorStream()));

	    Thread errPrinter = new PrinterThread(messageArea, stdError);
	    errPrinter.start();
	    
	    int success = commandLineProc.waitFor();
	    */

	    // Replacing the above and its use of the PrinterThread inner class with SafeProcess.java:
	    SafeProcess.LineByLineHandler outLineHandler = new ProcessLineHandler(messageArea, SafeProcess.STDOUT);
	    SafeProcess.LineByLineHandler errLineHandler = new ProcessLineHandler(messageArea, SafeProcess.STDERR);		    

	    int success = commandLineProc.runProcess(outLineHandler, errLineHandler);

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

	String command = _osCommands.get(currentos);
	if(command != null){
	    return command;
	}
	
	if(currentos.contains("Windows")){
	    command = _osCommands.get("Windows");
	    
	    if(command != null){
		return command;
	    }   
	}
	return _osCommands.get("default");
    }

    /*
    public class PrinterThread extends Thread
    {
	JTextArea _messageArea = null;
	BufferedReader _output = null;

	public PrinterThread(JTextArea messageArea, BufferedReader output)
	{
	    _messageArea = messageArea;
	    _output = output;
	}
	
	public void run()
	{
	    String s = "";

	    try{
		while ((s = _output.readLine()) != null) {
		    _messageArea.append(s + "\n");
		    _messageArea.setSelectionEnd(_messageArea.getDocument().getLength());
		}  
	    }
	    catch(Exception ex){
		ex.printStackTrace();
	    }
	}
    }*/

    public class ProcessLineHandler extends SafeProcess.LineByLineHandler
    {
	// These members need to be final in order to synchronize on them
	private final JTextArea _messageArea;

	public ProcessLineHandler(JTextArea messageArea, int src)
	{
	    super(src); // will set this.source to STDERR or STDOUT
	    _messageArea = messageArea;
	}

	public void gotLine(String line) { // first non-null line

	    // messageArea needs to be synchronized, since both the process'
	    // stderr and stdout will be attempting to append to it from their own threads

	    synchronized(_messageArea) {
		_messageArea.append(line + "\n");
		_messageArea.setSelectionEnd(_messageArea.getDocument().getLength());
	    }
	}
	public void gotException(Exception e) {
	    e.printStackTrace();
	}

    }
}
