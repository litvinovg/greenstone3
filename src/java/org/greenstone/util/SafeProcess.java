package org.greenstone.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.Arrays;

import org.apache.log4j.*;

// Use this class to run a Java Process. It follows the good and safe practices at
// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// to avoid blocking problems that can arise from a Process' input and output streams.

public class SafeProcess {

    static Logger logger = Logger.getLogger(org.greenstone.util.SafeProcess.class.getName());

    private String[] command_args;
    private String inputStr = null;

    private String outputStr = ""; 
    private String errorStr = "";

    private int process_exitValue = -1;
    private ProcessLineByLineHandler lineByLineHandler = null;

    // whether std/err output should be split at new lines
    private boolean splitStdOutputNewLines = false;
    private boolean splitStdErrorNewLines = false;

    // call one of these constructors
    public SafeProcess(String[] cmd_args)
    {
	this(cmd_args, null, null, false, false);
    }

    public SafeProcess(String[] cmd_args, ProcessLineByLineHandler lbl_handler)
    {
	this(cmd_args, lbl_handler, null, false, false);
    }

    public SafeProcess(String[] cmd_args, ProcessLineByLineHandler lbl_handler,
		       String sendStr, boolean splitStdOut, boolean splitStdErr)
    {	
	command_args = cmd_args;
	lineByLineHandler = lbl_handler;
	setInputString(sendStr);
	setSplitStdOutputNewLines(splitStdOut);
	setSplitStdErrorNewLines(splitStdErr);
	runProcess();
    }

    // The important methods:
    // to get the output from std err and std out streams after the process has been run
    public String getStdOutput() { return outputStr; }
    public String getStdError() { return errorStr; }
    public int getExitValue() { return process_exitValue; }

    public void setInputString(String sendStr) {
	inputStr = sendStr;
    }
    public void setSplitStdOutputNewLines(boolean split) {
	splitStdOutputNewLines = split;
    }
    public void setSplitStdErrorNewLines(boolean split) {
	splitStdErrorNewLines = split;
    }

//***************** Copied from gli's gui/FormatConversionDialog.java *************//
    private void runProcess() {

	try {	    
	    
	    Runtime rt = Runtime.getRuntime();
	    Process prcs = null;
	    
	    // http://stackoverflow.com/questions/5283444/convert-array-of-strings-into-a-string-in-java
	    logger.info("Running process: " + Arrays.toString(command_args));
	    prcs = rt.exec(this.command_args);

	    // send inputStr to process. The following constructor can handle inputStr being null
	    SafeProcess.OutputStreamGobbler inputGobbler = 
		new SafeProcess.OutputStreamGobbler(prcs.getOutputStream(), this.inputStr);
	    
	    // monitor for any error messages
            SafeProcess.InputStreamGobbler errorGobbler 
		= new SafeProcess.InputStreamGobbler(prcs.getErrorStream(), splitStdOutputNewLines);
            
            // monitor for the expected std output line(s)
            SafeProcess.InputStreamGobbler outputGobbler
		= new SafeProcess.InputStreamGobbler(prcs.getInputStream(), splitStdErrorNewLines);

            // kick them off
            inputGobbler.start();
            errorGobbler.start();
            outputGobbler.start();
                                    
            // any error???
            this.process_exitValue = prcs.waitFor();
            //System.out.println("ExitValue: " + exitVal); 

	    // From the comments of 
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // To avoid running into nondeterministic failures to get the process output
	    // if there's no waiting for the threads, call join() on each Thread (StreamGobbler) object:
	    outputGobbler.join();
	    errorGobbler.join();
	    inputGobbler.join();
	    
	    // set the variables the code that created a SafeProcess object may want to inspect
	    this.outputStr = outputGobbler.getOutput();
	    this.errorStr = errorGobbler.getOutput();

	    // the calling code should handle errorStr, not us, so can leave out the following code
	    if(!this.errorStr.equals("")) {
		logger.info("*** Process errorstream: \n" + this.errorStr + "\n****");
		System.err.println("*** Process errorstream: \n" + this.errorStr + "\n****");
	    }
       
	} catch(IOException ioe) {
	    logger.error("IOexception: " + ioe.getMessage());
	    System.err.println("IOexception " + ioe.getMessage());
	    ioe.printStackTrace();
	} catch(InterruptedException ie) {
	    logger.error("Process InterruptedException: " + ie.getMessage());
	    System.err.println("Process InterruptedException " + ie.getMessage());
	    ie.printStackTrace();
	}

    }

    

//**************** Inner class definitions copied from GLI **********//
// Static inner classes can be instantiated without having to instantiate an object of the outer class first

    
// When reading from a process' stdout or stderr stream, you can create a LineByLineHandler
// to do something on a line by line basis, such as sending the line to a log
// Can have public static interfaces too,
// see http://stackoverflow.com/questions/71625/why-would-a-static-nested-interface-be-used-in-java
public static interface ProcessLineByLineHandler {

    public void gotLine(String line);
}

// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// This class is used in FormatConversionDialog to properly read from the stdout and stderr
// streams of a Process, Process.getInputStream() and Process.getErrorSream()
public static class InputStreamGobbler extends Thread
{
    InputStream is = null;
    StringBuffer outputstr = new StringBuffer();
    boolean split_newlines = false;
    ProcessLineByLineHandler lineByLineHandler = null;
    
    public InputStreamGobbler(InputStream is)
    {
	this.is = is;
	split_newlines = false;
    }
    
    public InputStreamGobbler(InputStream is, boolean split_newlines)
    {
	this.is = is;
	this.split_newlines = split_newlines;
    }
    
    public void setLineByLineHandler(ProcessLineByLineHandler lblHandler) {
	lineByLineHandler = lblHandler;
    }

    public void run()
    {
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    String line=null;
	    while ( (line = br.readLine()) != null) {
		//System.out.println("@@@ GOT LINE: " + line);
		outputstr.append(line);
		if(split_newlines) {
		    outputstr.append("\n");
		}

		if(lineByLineHandler != null) { // let handler deal with newlines
		    lineByLineHandler.gotLine(line);
		}
		
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();  
	} finally {
	    SafeProcess.closeResource(br);
	}
    }
    
    public String getOutput() { 
	return outputstr.toString(); // implicit toString() call anyway. //return outputstr; 
    }
} // end static inner class InnerStreamGobbler


// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// This class is used in FormatConversionDialog to properly write to the inputstream of a Process
// Process.getOutputStream()
public class OutputStreamGobbler extends Thread
{
    OutputStream os = null;
    String inputstr = "";
    
    public OutputStreamGobbler(OutputStream os, String inputstr)
    {
	this.os = os;
	this.inputstr = inputstr;
    }
    
    public void run()
    {
	
	if (inputstr == null) {
		return;
	}
	
	BufferedWriter osw = null;
	try	{
	    osw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	    //System.out.println("@@@ SENDING LINE: " + inputstr);
	    osw.write(inputstr, 0, inputstr.length());
	    osw.newLine();//osw.write("\n");
	    osw.flush();
	    
	    // Don't explicitly send EOF when using StreamGobblers as below, 
	    // as the EOF char is echoed to output.
	    // Flushing the write handle and/or closing the resource seems 
	    // to already send EOF silently.
	    
	    /*if(Utility.isWindows()) {
	      osw.write("\032"); // octal for Ctrl-Z, EOF on Windows
	      } else { // EOF on Linux/Mac is Ctrl-D
	      osw.write("\004"); // octal for Ctrl-D, see http://www.unix-manuals.com/refs/misc/ascii-table.html
	      }
	      osw.flush();
	    */
	} catch (IOException ioe) {
	    ioe.printStackTrace();  
	} finally {
	    SafeProcess.closeResource(osw);
	}
    }	
} // end static inner class OutputStreamGobbler


//**************** Copied from GLI's Utility.java ******************
    // For safely closing streams/handles/resources. 
    // For examples of use look in the Input- and OutputStreamGobbler classes.
    // http://docs.oracle.com/javase/tutorial/essential/exceptions/finally.html
    // http://stackoverflow.com/questions/481446/throws-exception-in-finally-blocks
    public static void closeResource(Closeable resourceHandle) {
	try {
	    if(resourceHandle != null) {
		resourceHandle.close();
		resourceHandle = null;
	    }
	} catch(Exception e) {
	    System.err.println("Exception closing resource: " + e.getMessage());
	    e.printStackTrace();
	    resourceHandle = null;
	}
    }

    public static void closeProcess(Process prcs) {
	if( prcs != null ) { 
	    closeResource(prcs.getErrorStream()); 
	    closeResource(prcs.getOutputStream()); 
	    closeResource(prcs.getInputStream()); 
	    prcs.destroy(); 
	} 
    }

} // end class SafeProcess