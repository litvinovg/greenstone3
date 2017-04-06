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

    private String[] command_args = null;
    private String[] envp = null;
    private File dir = null;
    private String inputStr = null;

    private String outputStr = ""; 
    private String errorStr = "";

    private int exitValue = -1;

    // user can write custom LineByLineHandler to deal with stdout lines as they come out one line at a time
    // and stderr lines as they come out one at a time
    private LineByLineHandler errLineByLineHandler = null;
    private LineByLineHandler outLineByLineHandler = null;
    private ExceptionHandler exceptionHandler = null;

    // whether std/err output should be split at new lines
    private boolean splitStdOutputNewLines = false;
    private boolean splitStdErrorNewLines = false;

    // call one of these constructors

    // cmd args version
    public SafeProcess(String[] cmd_args)
    {
	command_args = cmd_args;
    }

    // cmd args with env version, launchDir can be null.
    public SafeProcess(String[] cmd_args, String[] envparams, File launchDir)
    {
	command_args = cmd_args;
	envp = envparams;
	dir = launchDir;
    }

    // The important methods:
    // to get the output from std err and std out streams after the process has been run
    public String getStdOutput() { return outputStr; }
    public String getStdError() { return errorStr; }
    public int getExitValue() { return exitValue; }

    
    // set any string to send as input to the process spawned by SafeProcess
    public void setInputString(String sendStr) {
	inputStr = sendStr;
    }

    // register a handler whose gotLine() method will get called as each line is read from the process' stdout
    public void setStdOutLineByLineHandler(LineByLineHandler out_lbl_handler) {
	outLineByLineHandler = out_lbl_handler;
    }

    // register a handler whose gotLine() method will get called as each line is read from the process' stderr
    public void setStdErrLineByLineHandler(LineByLineHandler err_lbl_handler) {
	errLineByLineHandler = err_lbl_handler;
    }

    // register a SafeProcess ExceptionHandler whose gotException() method will
    // get called for each exception encountered
    public void setExceptionHandler(ExceptionHandler exception_handler) {
	exceptionHandler = exception_handler;	
    }

    // set if you want the std output or err output to have \n at each newline read from the stream
    public void setSplitStdOutputNewLines(boolean split) {
	splitStdOutputNewLines = split;
    }
    public void setSplitStdErrorNewLines(boolean split) {
	splitStdErrorNewLines = split;
    }

//***************** Copied from gli's gui/FormatConversionDialog.java *************//
    public void runProcess() {

	Process prcs = null;
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {	    
	    Runtime rt = Runtime.getRuntime();	    
	    
	    // http://stackoverflow.com/questions/5283444/convert-array-of-strings-into-a-string-in-java
	    //logger.info("Running process: " + Arrays.toString(command_args));

	    if(this.envp == null) {
		prcs = rt.exec(this.command_args);
	    } else { // launch process using cmd str with env params		

		if(this.dir == null) {
		    //logger.info("\twith: " + Arrays.toString(this.envp));
		    prcs = rt.exec(this.command_args, this.envp);
		} else {
		    //logger.info("\tfrom directory: " + this.dir);
		    //logger.info("\twith: " + Arrays.toString(this.envp));
		    prcs = rt.exec(this.command_args, this.envp, this.dir);
		}
	    }

	    // send inputStr to process. The following constructor can handle inputStr being null
	    inputGobbler = // WriterToProcessInputStream
		new SafeProcess.OutputStreamGobbler(prcs.getOutputStream(), this.inputStr);
	    
	    // monitor for any error messages
            errorGobbler // ReaderFromProcessOutputStream
		= new SafeProcess.InputStreamGobbler(prcs.getErrorStream(), splitStdErrorNewLines);

            // monitor for the expected std output line(s)
            outputGobbler
		= new SafeProcess.InputStreamGobbler(prcs.getInputStream(), splitStdOutputNewLines);
	                
	    // register line by line handlers, if any were set, for the process stderr and stdout streams
	    if(this.outLineByLineHandler != null) {
		outputGobbler.setLineByLineHandler(this.outLineByLineHandler);
	    }
	    if(this.errLineByLineHandler != null) {
		errorGobbler.setLineByLineHandler(this.errLineByLineHandler);
	    }
	    if(this.exceptionHandler != null) {
		inputGobbler.setExceptionHandler(this.exceptionHandler);
	    }	    

            // kick off the stream gobblers
            inputGobbler.start();
            errorGobbler.start();
            outputGobbler.start();
                                    
            // any error???
            this.exitValue = prcs.waitFor(); // can throw an InterruptedException if process did not terminate	    	    
	    // From the comments of 
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // To avoid running into nondeterministic failures to get the process output
	    // if there's no waiting for the threads, call join() on each Thread (StreamGobbler) object.
	    // From Thread API: join() "Waits for this thread (the thread join() is invoked on) to die."
	    outputGobbler.join(); 
	    errorGobbler.join();
	    inputGobbler.join(); 
	    
	    // set the variables the code that created a SafeProcess object may want to inspect
	    this.outputStr = outputGobbler.getOutput();
	    this.errorStr = errorGobbler.getOutput();

	    // Since we didn't have an exception, process should have terminated now (waitFor blocks until then)
	    // Set process to null so we don't forcibly terminate it below with process.destroy()
	    prcs = null;
       
	} catch(IOException ioe) {
	    logger.error("IOexception: " + ioe.getMessage(), ioe);
	    //System.err.println("IOexception " + ioe.getMessage());
	    //ioe.printStackTrace();
	    if(exceptionHandler != null) exceptionHandler.gotException(ioe);
	} catch(InterruptedException ie) {
	    logger.error("Process InterruptedException: " + ie.getMessage(), ie);
	    //System.err.println("Process InterruptedException " + ie.getMessage());
	    //ie.printStackTrace();
	    if(exceptionHandler != null) exceptionHandler.gotException(ie);

	    // propagate interrupts to worker threads here?
	    // unless the interrupt emanated from any of them in any join()...
	    // Only if the thread SafeProcess runs in was interrupted
	    // do we propagate the interrupt to the worker threads.
	    // http://stackoverflow.com/questions/2126997/who-is-calling-the-java-thread-interrupt-method-if-im-not
	    // "I know that in JCiP it is mentioned that you should never interrupt threads you do not own"
	    // But SafeProcess owns the worker threads, so it have every right to interrupt them
	    // Also read http://stackoverflow.com/questions/13623445/future-cancel-method-is-not-working?noredirect=1&lq=1
	    if(Thread.currentThread().isInterrupted()) {
		inputGobbler.interrupt();
		errorGobbler.interrupt();
		outputGobbler.interrupt();	    
	    }

	    // On catchingInterruptedException, re-interrupt the thread.
	    // This is just how InterruptedExceptions tend to be handled
	    // See also http://stackoverflow.com/questions/4906799/why-invoke-thread-currentthread-interrupt-when-catch-any-interruptexception
	    // and https://praveer09.github.io/technology/2015/12/06/understanding-thread-interruption-in-java/

	    // http://stackoverflow.com/questions/3976344/handling-interruptedexception-in-java
	    // http://stackoverflow.com/questions/4906799/why-invoke-thread-currentthread-interrupt-when-catch-any-interruptexception
	    // "Only code that implements a thread's interruption policy may swallow an interruption request. General-purpose task and library code should never swallow interruption requests."
	    // Does that mean that since this code implements this thread's interruption policy, it's ok
	    // to swallow the interrupt this time and not let it propagate by commenting out the next line?
	    Thread.currentThread().interrupt(); // re-interrupt the thread - which thread? Infinite loop?
	} finally { 

	    // Moved into here from GS2PerlConstructor which said
	    // "I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine."
	    // http://steveliles.github.io/invoking_processes_from_java.html
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // http://mark.koli.ch/leaky-pipes-remember-to-close-your-streams-when-using-javas-runtimegetruntimeexec	    
	    if( prcs != null ) {		
		prcs.destroy();
	    }
	}

    }
    

//**************** Inner class definitions (stream gobblers copied from GLI) **********//
// Static inner classes can be instantiated without having to instantiate an object of the outer class first

// Can have public static interfaces too,
// see http://stackoverflow.com/questions/71625/why-would-a-static-nested-interface-be-used-in-java
// Implementors need to take care that the implementations are thread safe
// http://stackoverflow.com/questions/14520814/why-synchronized-method-is-not-included-in-interface
public static interface ExceptionHandler {

    // SHOULD I DECLARE THIS SYNCHRONIZED?
    // It ends up being thread safe for the particular instance I'm using it for, but that doesn't
    // make it future proof...
    public void gotException(Exception e);
}

// When reading from a process' stdout or stderr stream, you can create a LineByLineHandler
// to do something on a line by line basis, such as sending the line to a log
public static interface LineByLineHandler {
    public void gotLine(String line);
    public void gotException(Exception e); // for when an exception occurs instead of getting a line
}


// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// This class is used in FormatConversionDialog to properly read from the stdout and stderr
// streams of a Process, Process.getInputStream() and Process.getErrorSream()
public static class InputStreamGobbler extends Thread
{
    InputStream is = null;
    StringBuffer outputstr = new StringBuffer();
    boolean split_newlines = false;
    LineByLineHandler lineByLineHandler = null;
    
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
    
    public void setLineByLineHandler(LineByLineHandler lblHandler) {
	lineByLineHandler = lblHandler;
    }


    public void run()
    {
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    String line=null;
	    while ( (line = br.readLine()) != null ) {

		if(this.isInterrupted()) { // should we not instead check if SafeProcess thread was interrupted?
		    logger.info("Got interrupted when reading lines from process err/out stream.");
		    break; // will go to finally block
		}

		//System.out.println("@@@ GOT LINE: " + line);
		outputstr.append(line);
		if(split_newlines) {
		    outputstr.append(Misc.NEWLINE); // "\n" is system dependent (Win must be "\r\n")
		}

		if(lineByLineHandler != null) { // let handler deal with newlines
		    lineByLineHandler.gotLine(line);
		}		
	    }
	} catch (IOException ioe) {
	    logger.error("Exception when reading from a process' stdout/stderr stream: ", ioe);
	    if(lineByLineHandler != null) lineByLineHandler.gotException(ioe);
	    //ioe.printStackTrace();  
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
public static class OutputStreamGobbler extends Thread
{
    OutputStream os = null;
    String inputstr = "";
    ExceptionHandler exceptionHandler = null;

    public OutputStreamGobbler(OutputStream os, String inputstr)
    {
	this.os = os;
	this.inputstr = inputstr;
    }
    
    public void setExceptionHandler(ExceptionHandler eHandler) {
	exceptionHandler = eHandler;
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
	    logger.error("Exception writing to SafeProcess' inputstream: ", ioe);	    
	    //ioe.printStackTrace();

	    if (this.exceptionHandler != null) this.exceptionHandler.gotException(ioe);
	    
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
    public static boolean closeResource(Closeable resourceHandle) {
	boolean success = false;
	try {
	    if(resourceHandle != null) {
		resourceHandle.close();
		resourceHandle = null;
		success = true;
	    }
	} catch(Exception e) {
	    logger.error("Exception closing resource: ", e);
	    //System.err.println("Exception closing resource: " + e.getMessage());
	    //e.printStackTrace();
	    resourceHandle = null;
	    success = false;
	} finally {
	    return success;
	}
    }

    public static boolean closeProcess(Process prcs) {
	boolean success = true;
	if( prcs != null ) { 
	    success = success && closeResource(prcs.getErrorStream()); 
	    success = success && closeResource(prcs.getOutputStream()); 
	    success = success && closeResource(prcs.getInputStream()); 
	    prcs.destroy(); 
	}
	return success;
    }

} // end class SafeProcess
