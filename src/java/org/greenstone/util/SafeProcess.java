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
    
    public static final int STDERR = 0;
    public static final int STDOUT = 1;
    public static final int STDIN = 2;    

    static Logger logger = Logger.getLogger(org.greenstone.util.SafeProcess.class.getName());

    // input to SafeProcess and initialising it
    private String command = null;
    private String[] command_args = null;
    private String[] envp = null;
    private File dir = null;
    private String inputStr = null;

    // output from running SafeProcess.runProcess()
    private String outputStr = ""; 
    private String errorStr = "";
    private int exitValue = -1;

    // allow callers to process exceptions of the main process thread if they want
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

    // cmd string version
    public SafeProcess(String cmdStr)
    {
	command = cmdStr;
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

    private Process doRuntimeExec() throws IOException {
	Process prcs = null;
	Runtime rt = Runtime.getRuntime();
	
	if(this.command != null) {
	    prcs = rt.exec(this.command);
	}
	else { // at least command_args must be set now
	    
	    // http://stackoverflow.com/questions/5283444/convert-array-of-strings-into-a-string-in-java
	    logger.info("SafeProcess running: " + Arrays.toString(command_args));
	    //System.err.println("SafeProcess running: " + Arrays.toString(command_args));
	    
	    if(this.envp == null) { 
		prcs = rt.exec(this.command_args);
	    } else { // launch process using cmd str with env params		
		
		if(this.dir == null) {
		    ///logger.info("\twith: " + Arrays.toString(this.envp));
		    ///System.err.println("\twith: " + Arrays.toString(this.envp));
		    prcs = rt.exec(this.command_args, this.envp);
		} else {
		    ///logger.info("\tfrom directory: " + this.dir);
		    ///logger.info("\twith: " + Arrays.toString(this.envp));
		    ///System.err.println("\tfrom directory: " + this.dir);
		    ///System.err.println("\twith: " + Arrays.toString(this.envp));
			prcs = rt.exec(this.command_args, this.envp, this.dir);
		}
	    }
	}

	return prcs;
    }

    // Copied from gli's gui/FormatConversionDialog.java
    private int waitForWithStreams(Process prcs,
				   SafeProcess.OutputStreamGobbler inputGobbler,
				   SafeProcess.InputStreamGobbler outputGobbler,
				   SafeProcess.InputStreamGobbler errorGobbler)
	throws IOException, InterruptedException
    {

            // kick off the stream gobblers
            inputGobbler.start();
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            this.exitValue = prcs.waitFor(); // can throw an InterruptedException if process did not terminate

            ///logger.info("Process exitValue: " + exitValue); 
	    ///System.err.println("Process exitValue: " + exitValue); 

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

	    return this.exitValue;
    }


    // Run a very basic process: with no reading from or writing to the Process' iostreams,
    // this just execs the process and waits for it to return
    public int runBasicProcess() {
	Process prcs = null;
	try {
	    // 1. create the process
	    prcs = doRuntimeExec();
	    // 2. basic waitFor the process to finish
	    this.exitValue = prcs.waitFor();

	    
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		logger.error("IOException: " + ioe.getMessage(), ioe);
		//System.err.println("IOException " + ioe.getMessage());
		//ioe.printStackTrace();
	    }
	} catch(InterruptedException ie) {

	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
	    } else {
		logger.error("Process InterruptedException: " + ie.getMessage(), ie);
		//System.err.println("Process InterruptedException " + ie.getMessage());
		///ie.printStackTrace(); // an interrupt here is not an error, it can be a cancel action
	    }
	    
	    Thread.currentThread().interrupt();
	} finally { 

	    if( prcs != null ) {
		prcs.destroy(); // see runProcess() below
	    }
	}
	return this.exitValue;
    }

    // Runs a process with default stream processing. Returns the exitValue
    public int runProcess() {
	return runProcess(null, null, null); // use default processing of all 3 of the process' iostreams
    }

    // Run a process with custom stream processing (any custom handlers passed in that are null
    // will use the default stream processing). 
    // Returns the exitValue from running the Process
    public int runProcess(CustomProcessHandler procInHandler,
			   CustomProcessHandler procOutHandler,
			   CustomProcessHandler procErrHandler)
    {
	Process prcs = null;
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {
	    // 1. get the Process object
	    prcs = doRuntimeExec();
	    

	    // 2. create the streamgobblers and set any specified handlers on them

	    // PROC INPUT STREAM
	    if(procInHandler == null) {
		// send inputStr to process. The following constructor can handle inputStr being null
		inputGobbler = // WriterToProcessInputStream
		    new SafeProcess.OutputStreamGobbler(prcs.getOutputStream(), this.inputStr);
	    } else { // user will do custom handling of process' InputStream 
		inputGobbler = new SafeProcess.OutputStreamGobbler(prcs.getOutputStream(), procInHandler);
	    }

	    // PROC ERR STREAM to monitor for any error messages or expected output in the process' stderr
	    if(procErrHandler == null) {
		errorGobbler // ReaderFromProcessOutputStream
		    = new SafeProcess.InputStreamGobbler(prcs.getErrorStream(), splitStdErrorNewLines);
	    } else {
		errorGobbler
		    = new SafeProcess.InputStreamGobbler(prcs.getErrorStream(), procErrHandler);
	    }

            // PROC OUT STREAM to monitor for the expected std output line(s)
	    if(procOutHandler == null) {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(prcs.getInputStream(), splitStdOutputNewLines);
	    } else {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(prcs.getInputStream(), procOutHandler);
	    }


            // 3. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(prcs, inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		logger.error("IOexception: " + ioe.getMessage(), ioe);
		//System.err.println("IOexception " + ioe.getMessage());
		//ioe.printStackTrace();
	    }
	} catch(InterruptedException ie) {

	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
	    } else {
		logger.error("Process InterruptedException: " + ie.getMessage(), ie);
		//System.err.println("Process InterruptedException " + ie.getMessage());
		///ie.printStackTrace(); // an interrupt here is not an error, it can be a cancel action
	    }

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
	
	return this.exitValue;
    }
    
    public int runProcess(LineByLineHandler outLineByLineHandler, LineByLineHandler errLineByLineHandler)
    {
	Process prcs = null;
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {
	    // 1. get the Process object
	    prcs = doRuntimeExec();
	    

	    // 2. create the streamgobblers and set any specified handlers on them

	    // PROC INPUT STREAM
	    // send inputStr to process. The following constructor can handle inputStr being null
	    inputGobbler = // WriterToProcessInputStream
		new SafeProcess.OutputStreamGobbler(prcs.getOutputStream(), this.inputStr);

	    // PROC ERR STREAM to monitor for any error messages or expected output in the process' stderr    
	    errorGobbler // ReaderFromProcessOutputStream
		    = new SafeProcess.InputStreamGobbler(prcs.getErrorStream(), splitStdErrorNewLines);		
            // PROC OUT STREAM to monitor for the expected std output line(s)
	    outputGobbler
		= new SafeProcess.InputStreamGobbler(prcs.getInputStream(), splitStdOutputNewLines);


	    // 3. register line by line handlers, if any were set, for the process stderr and stdout streams
	    if(outLineByLineHandler != null) {
		outputGobbler.setLineByLineHandler(outLineByLineHandler);
	    }
	    if(errLineByLineHandler != null) {
		errorGobbler.setLineByLineHandler(errLineByLineHandler);
	    }	    


            // 4. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(prcs, inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		logger.error("IOexception: " + ioe.getMessage(), ioe);
		//System.err.println("IOexception " + ioe.getMessage());
		//ioe.printStackTrace();
	    }
	} catch(InterruptedException ie) {

	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
	    } else {
		logger.error("Process InterruptedException: " + ie.getMessage(), ie);
		//System.err.println("Process InterruptedException " + ie.getMessage());
		///ie.printStackTrace(); // an interrupt here is not an error, it can be a cancel action
	    }

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
	
	return this.exitValue;
    }


//******************** Inner class and interface definitions ********************//
// Static inner classes can be instantiated without having to instantiate an object of the outer class first

// Can have public static interfaces too,
// see http://stackoverflow.com/questions/71625/why-would-a-static-nested-interface-be-used-in-java
// Implementors need to take care that the implementations are thread safe
// http://stackoverflow.com/questions/14520814/why-synchronized-method-is-not-included-in-interface
public static interface ExceptionHandler {

    // when implementing ExceptionHandler.gotException(), if it manipulates anything that's
    // not threadsafe, declare gotException() as a synchronized method to ensure thread safety
    public void gotException(Exception e); // can't declare as synchronized in interface method declaration
}

// Write your own run() body for any StreamGobbler. You need to create an instance of a class
// extending CustomProcessHandler for EACH IOSTREAM of the process that you want to handle.
// Do not create a single CustomProcessHandler instance and reuse it for all three streams,
// i.e. don't call SafeProcess' runProcess(x, x, x); It should be runProcess(x, y, z).
// Make sure your implementation is threadsafe if you're sharing immutable objects between the threaded streams
// example implementation is in the GS2PerlConstructor.SynchronizedProcessHandler class.
// CustomProcessHandler is made an abstract class instead of an interface to force classes that want
// to use a CustomProcessHandler to create a separate class that extends CustomProcessHandler, rather than
// that the classes that wish to use it "implementing" the CustomProcessHandler interface itself: the
// CustomProcessHandler.run() method may then be called in the major thread from which the Process is being
// executed, rather than from the individual threads that deal with each iostream of the Process.
public static abstract class CustomProcessHandler {

    protected final int source;

    protected CustomProcessHandler(int src) {
	this.source = src; // STDERR or STDOUT or STDIN
    }
    
    public abstract void run(Closeable stream); //InputStream or OutputStream
}

// When using the default stream processing to read from a process' stdout or stderr stream, you can
// create a class extending LineByLineHandler for the process' err stream and one for its output stream
// to do something on a line by line basis, such as sending the line to a log
public static abstract class LineByLineHandler {
    protected final int source;

    protected LineByLineHandler(int src) {
	this.source = src; // STDERR or STDOUT
    }

    public abstract void gotLine(String line); // first non-null line
    public abstract void gotException(Exception e); // for when an exception occurs instead of getting a line
}

//**************** StreamGobbler Inner class definitions (stream gobblers copied from GLI) **********//

// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// This class is used in FormatConversionDialog to properly read from the stdout and stderr
// streams of a Process, Process.getInputStream() and Process.getErrorSream()
public static class InputStreamGobbler extends Thread
{
    private InputStream is = null;
    private StringBuffer outputstr = new StringBuffer();
    private boolean split_newlines = false;
    private CustomProcessHandler customHandler = null;
    private LineByLineHandler lineByLineHandler = null;

    public InputStreamGobbler(InputStream is)
    {
	this.is = is;
	this.split_newlines = false;
    }
    
    public InputStreamGobbler(InputStream is, boolean split_newlines)
    {
	this.is = is;
	this.split_newlines = split_newlines;
    }
    
    public InputStreamGobbler(InputStream is, CustomProcessHandler customHandler)
    {
	this.is = is;
	this.customHandler = customHandler;
    }

    public void setLineByLineHandler(LineByLineHandler lblHandler) {
	this.lineByLineHandler = lblHandler;
    }

    // default run() behaviour
    public void runDefault()
    {
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    String line=null;
	    while ( (line = br.readLine()) != null ) {

		if(this.isInterrupted()) { // should we not instead check if SafeProcess thread was interrupted?
		    logger.info("Got interrupted when reading lines from process err/out stream.");
		    //System.err.println("InputStreamGobbler.runDefault() Got interrupted when reading lines from process err/out stream.");
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
	    if(lineByLineHandler != null) {
		lineByLineHandler.gotException(ioe);
	    } else {
		logger.error("Exception when reading from a process' stdout/stderr stream: ", ioe);
		//System.err.println("Exception when reading from a process' stdout/stderr stream: ");
		//ioe.printStackTrace();  
	    }
	} finally {
	    SafeProcess.closeResource(br);
	}
    }
    
    public void runCustom() {
	this.customHandler.run(is);
    }
    
    public void run() {
	if(this.customHandler == null) {
	    runDefault();
	} else {
	    runCustom();
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
    private OutputStream os = null;
    private String inputstr = "";
    private CustomProcessHandler customHandler = null;

    public OutputStreamGobbler(OutputStream os) {
	this.os = os;
    }

    public OutputStreamGobbler(OutputStream os, String inputstr)
    {
	this.os = os;
	this.inputstr = inputstr;
    }

    public OutputStreamGobbler(OutputStream os, CustomProcessHandler customHandler) {
	this.os = os;
	this.customHandler = customHandler;
    }

    // default run() behaviour
    public void runDefault() {
	
	if (inputstr == null) {
		return;
	}
	
	BufferedWriter osw = null;
	try {
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
	    //System.err.println("Exception writing to SafeProcess' inputstream: ");
	    //ioe.printStackTrace();	
	} finally {
	    SafeProcess.closeResource(osw);
	}
    }

    // call the user's custom handler for the run() method
    public void runCustom() {
	this.customHandler.run(os);	
    }

    public void run()
    {
	if(this.customHandler == null) {
	    runDefault();
	} else {
	    runCustom();
	}

    }	
} // end static inner class OutputStreamGobbler


//**************** Useful static methods. Copied from GLI's Utility.java ******************
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
