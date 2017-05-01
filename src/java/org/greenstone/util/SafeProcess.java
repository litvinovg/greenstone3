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

//import org.greenstone.gatherer.DebugStream;

// Use this class to run a Java Process. It follows the good and safe practices at
// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// to avoid blocking problems that can arise from a Process' input and output streams.

public class SafeProcess {
    //public static int DEBUG = 0;

    public static final int STDERR = 0;
    public static final int STDOUT = 1;
    public static final int STDIN = 2;

    // charset for reading process stderr and stdout streams
    //public static final String UTF8 = "UTF-8";    

    static Logger logger = Logger.getLogger(org.greenstone.util.SafeProcess.class.getName());

    // input to SafeProcess and initialising it
    private String command = null;
    private String[] command_args = null;
    private String[] envp = null;
    private File dir = null;
    private String inputStr = null;
    private Process process = null;

    // output from running SafeProcess.runProcess()
    private String outputStr = ""; 
    private String errorStr = "";
    private int exitValue = -1;
    //private String charset = null;

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

    //public void setStreamCharSet(String charset) { this.charset = charset; } 

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
	    log("SafeProcess running: " + command);
	    prcs = rt.exec(this.command);
	}
	else { // at least command_args must be set now
	    
	    // http://stackoverflow.com/questions/5283444/convert-array-of-strings-into-a-string-in-java
	    log("SafeProcess running: " + Arrays.toString(command_args));
	    
	    if(this.envp == null) { 
		prcs = rt.exec(this.command_args);
	    } else { // launch process using cmd str with env params		
		
		if(this.dir == null) {
		    //log("\twith: " + Arrays.toString(this.envp));
		    prcs = rt.exec(this.command_args, this.envp);
		} else {
		    //log("\tfrom directory: " + this.dir);
		    //log("\twith: " + Arrays.toString(this.envp));		    
		    prcs = rt.exec(this.command_args, this.envp, this.dir);
		}
	    }
	}

	return prcs;
    }

    // Copied from gli's gui/FormatConversionDialog.java
    private int waitForWithStreams(SafeProcess.OutputStreamGobbler inputGobbler,
				   SafeProcess.InputStreamGobbler outputGobbler,
				   SafeProcess.InputStreamGobbler errorGobbler)
	throws IOException, InterruptedException
    {

	
	// kick off the stream gobblers
	inputGobbler.start();
	errorGobbler.start();
	outputGobbler.start();
	
	// any error???
	try {
	    this.exitValue = process.waitFor(); // can throw an InterruptedException if process did not terminate
	} catch(InterruptedException ie) {
	    log("*** Process interrupted (InterruptedException). Expected to be a Cancel operation.");
	        // don't print stacktrace: an interrupt here is not an error, it's expected to be a cancel action
	    if(exceptionHandler != null) {		
		exceptionHandler.gotException(ie);
	    }	        
	    
	    // propagate interrupts to worker threads here
	    // unless the interrupt emanated from any of them in any join(),
	    // which will be caught by caller's catch on InterruptedException.
	    // Only if the thread that SafeProcess runs in was interrupted
	    // should we propagate the interrupt to the worker threads.
	    // http://stackoverflow.com/questions/2126997/who-is-calling-the-java-thread-interrupt-method-if-im-not
	    // "I know that in JCiP it is mentioned that you should never interrupt threads you do not own"
	    // But SafeProcess owns the worker threads, so it has every right to interrupt them
	    // Also read http://stackoverflow.com/questions/13623445/future-cancel-method-is-not-working?noredirect=1&lq=1
	    
	    // http://stackoverflow.com/questions/3976344/handling-interruptedexception-in-java
	    // http://stackoverflow.com/questions/4906799/why-invoke-thread-currentthread-interrupt-when-catch-any-interruptexception
	    // "Only code that implements a thread's interruption policy may swallow an interruption request. General-purpose task and library code should never swallow interruption requests."
	    // Does that mean that since this code implements this thread's interruption policy, it's ok
	    // to swallow the interrupt this time and not let it propagate by commenting out the next line?
	    //Thread.currentThread().interrupt(); // re-interrupt the thread

	    inputGobbler.interrupt();
	    errorGobbler.interrupt();
	    outputGobbler.interrupt();

	    // even after the interrupts, we want to proceed to calling join() on all the worker threads
	    // in order to wait for each of them to die before attempting to destroy the process if it
	    // still hasn't terminated after all that.
	} finally {		
	    
	    //log("Process exitValue: " + exitValue);
	    
	    // From the comments of 
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // To avoid running into nondeterministic failures to get the process output
	    // if there's no waiting for the threads, call join() on each Thread (StreamGobbler) object.
	    // From Thread API: join() "Waits for this thread (the thread join() is invoked on) to die."
	    
	    // Wait for each of the threads to die, before attempting to destroy the process
	    // Any of these can throw InterruptedExceptions too
	    // and will be processed by the calling function's catch on InterruptedException.
	    // However, no one besides us will interrupting these threads I think...
	    // and we won't be throwing the InterruptedException from within the threads...
	    // So if any streamgobbler.join() call throws an InterruptedException, that would be unexpected

	    outputGobbler.join(); 
	    errorGobbler.join();
	    inputGobbler.join();
	    
	    
	    // set the variables that the code which created a SafeProcess object may want to inspect
	    this.outputStr = outputGobbler.getOutput();
	    this.errorStr = errorGobbler.getOutput();
	    
	    // Since we didn't have an exception, process should have terminated now (waitFor blocks until then)
	    // Set process to null so we don't forcibly terminate it below with process.destroy()
	    this.process = null;	    
	}

	// Don't return from finally, it's considered an abrupt completion and exceptions are lost, see
	// http://stackoverflow.com/questions/18205493/can-we-use-return-in-finally-block
	return this.exitValue;
    }


    public synchronized boolean processRunning() {
	if(process == null) return false;
	return SafeProcess.processRunning(this.process);
    }

    // Run a very basic process: with no reading from or writing to the Process' iostreams,
    // this just execs the process and waits for it to return
    public int runBasicProcess() {
	try {
	    // 1. create the process
	    process = doRuntimeExec();
	    // 2. basic waitFor the process to finish
	    this.exitValue = process.waitFor();

	    
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		log("IOException: " + ioe.getMessage(), ioe);
	    }
	} catch(InterruptedException ie) {

	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
	    } else { // Unexpected InterruptedException, so printstacktrace
		log("Process InterruptedException: " + ie.getMessage(), ie);
	    }
	    
	    Thread.currentThread().interrupt();
	} finally { 

	    if( process != null ) {
		process.destroy(); // see runProcess() below
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
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {
	    // 1. get the Process object
	    process = doRuntimeExec();
	    

	    // 2. create the streamgobblers and set any specified handlers on them

	    // PROC INPUT STREAM
	    if(procInHandler == null) {
		// send inputStr to process. The following constructor can handle inputStr being null
		inputGobbler = // WriterToProcessInputStream
		    new SafeProcess.OutputStreamGobbler(process.getOutputStream(), this.inputStr);
	    } else { // user will do custom handling of process' InputStream 
		inputGobbler = new SafeProcess.OutputStreamGobbler(process.getOutputStream(), procInHandler);
	    }

	    // PROC ERR STREAM to monitor for any error messages or expected output in the process' stderr
	    if(procErrHandler == null) {
		errorGobbler // ReaderFromProcessOutputStream
		    = new SafeProcess.InputStreamGobbler(process.getErrorStream(), splitStdErrorNewLines);
	    } else {
		errorGobbler
		    = new SafeProcess.InputStreamGobbler(process.getErrorStream(), procErrHandler);
	    }

            // PROC OUT STREAM to monitor for the expected std output line(s)
	    if(procOutHandler == null) {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(process.getInputStream(), splitStdOutputNewLines);
	    } else {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(process.getInputStream(), procOutHandler);
	    }


            // 3. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		log("IOexception: " + ioe.getMessage(), ioe);
	    }
	} catch(InterruptedException ie) { // caused during any of the gobblers.join() calls, this is unexpected so print stack trace
	    
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
		log("@@@@ Unexpected InterruptedException when waiting for process stream gobblers to die");
	    } else {
		log("*** Unexpected InterruptException when waiting for process stream gobblers to die:" + ie.getMessage(), ie);
	    }

	    // see comments in other runProcess()
	    Thread.currentThread().interrupt();
	
	} finally { 
	    //String cmd = (this.command == null) ? Arrays.toString(this.command_args) : this.command;
	    //log("*** In finally of SafeProcess.runProcess(3 params): " + cmd);

	    if( process != null ) {
		log("*** Going to call process.destroy 2");
		process.destroy();
		process = null;
		log("*** Have called process.destroy 2");
	    }
	    
	}
	
	return this.exitValue;
    }
    
    public int runProcess(LineByLineHandler outLineByLineHandler, LineByLineHandler errLineByLineHandler)
    {
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {
	    // 1. get the Process object
	    process = doRuntimeExec();
	    

	    // 2. create the streamgobblers and set any specified handlers on them

	    // PROC INPUT STREAM
	    // send inputStr to process. The following constructor can handle inputStr being null
	    inputGobbler = // WriterToProcessInputStream
		new SafeProcess.OutputStreamGobbler(process.getOutputStream(), this.inputStr);

	    // PROC ERR STREAM to monitor for any error messages or expected output in the process' stderr    
	    errorGobbler // ReaderFromProcessOutputStream
		    = new SafeProcess.InputStreamGobbler(process.getErrorStream(), splitStdErrorNewLines);		
            // PROC OUT STREAM to monitor for the expected std output line(s)
	    outputGobbler
		= new SafeProcess.InputStreamGobbler(process.getInputStream(), splitStdOutputNewLines);


	    // 3. register line by line handlers, if any were set, for the process stderr and stdout streams
	    if(outLineByLineHandler != null) {
		outputGobbler.setLineByLineHandler(outLineByLineHandler);
	    }
	    if(errLineByLineHandler != null) {
		errorGobbler.setLineByLineHandler(errLineByLineHandler);
	    }	    


            // 4. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		log("IOexception: " + ioe.getMessage(), ioe);		
	    }
	} catch(InterruptedException ie) { // caused during any of the gobblers.join() calls, this is unexpected so log it
	    
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
		log("@@@@ Unexpected InterruptedException when waiting for process stream gobblers to die");
	    } else {
		log("*** Unexpected InterruptException when waiting for process stream gobblers to die: " + ie.getMessage(), ie);
	    }
	    // We're not causing any interruptions that may occur when trying to stop the worker threads
	    // So resort to default behaviour in this catch?
	    // "On catching InterruptedException, re-interrupt the thread."
	    // This is just how InterruptedExceptions tend to be handled
	    // See also http://stackoverflow.com/questions/4906799/why-invoke-thread-currentthread-interrupt-when-catch-any-interruptexception
	    // and https://praveer09.github.io/technology/2015/12/06/understanding-thread-interruption-in-java/
	    Thread.currentThread().interrupt(); // re-interrupt the thread - which thread? Infinite loop?
	
	} finally { 

	    // Moved into here from GS2PerlConstructor and GShell.runLocal() which said
	    // "I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine (no it doesn't!)"
	    // http://steveliles.github.io/invoking_processes_from_java.html
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // http://mark.koli.ch/leaky-pipes-remember-to-close-your-streams-when-using-javas-runtimegetruntimeexec	    
	    
	    //String cmd = (this.command == null) ? Arrays.toString(this.command_args) : this.command;
	    //log("*** In finally of SafeProcess.runProcess(2 params): " + cmd);

	    if( process != null ) {
		log("*** Going to call process.destroy 1");
		process.destroy();
		process = null;
		log("*** Have called process.destroy 1");
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

    public String getThreadNamePrefix() {
	return SafeProcess.streamToString(this.source); 
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

    public String getThreadNamePrefix() {
	return SafeProcess.streamToString(this.source); 
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

    protected InputStreamGobbler() {
	super("InputStreamGobbler");
    }

    public InputStreamGobbler(InputStream is)
    {
	this(); // sets thread name
	this.is = is;
	this.split_newlines = false;
    }
    
    public InputStreamGobbler(InputStream is, boolean split_newlines)
    {
	this(); // sets thread name
	this.is = is;
	this.split_newlines = split_newlines;
    }
    
    public InputStreamGobbler(InputStream is, CustomProcessHandler customHandler)
    {
	this(); // thread name
	this.is = is;
	this.customHandler = customHandler;
	this.adjustThreadName(customHandler.getThreadNamePrefix());
    }

    
    private void adjustThreadName(String prefix) {	
	this.setName(prefix + this.getName());
    }

    public void setLineByLineHandler(LineByLineHandler lblHandler) {
	this.lineByLineHandler = lblHandler;
	this.adjustThreadName(lblHandler.getThreadNamePrefix());
    }

    // default run() behaviour
    public void runDefault()
    {
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    String line=null;
	    while ( !this.isInterrupted() && (line = br.readLine()) != null ) {

		//log("@@@ GOT LINE: " + line);
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
		log("Exception when reading process stream with " + this.getName() + ": ", ioe);
	    }
	} finally {
	    if(this.isInterrupted()) {
		log("@@@ Successfully interrupted " + this.getName() + ".");
	    }
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

    protected OutputStreamGobbler() {
	super("stdinOutputStreamGobbler"); // thread name
    }

    public OutputStreamGobbler(OutputStream os) {
	this(); // set thread name
	this.os = os;
    }

    public OutputStreamGobbler(OutputStream os, String inputstr)
    {
	this(); // set thread name
	this.os = os;
	this.inputstr = inputstr;
    }

    public OutputStreamGobbler(OutputStream os, CustomProcessHandler customHandler) {
	this(); // set thread name
	this.os = os;
	this.customHandler = customHandler;
    }

    // default run() behaviour
    public void runDefault() {
	
	if (inputstr == null) {
		return;
	}

	// also quit if the process was interrupted before we could send anything to its stdin
	if(this.isInterrupted()) {
	    log(this.getName() + " thread was interrupted.");
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
	    log("Exception writing to SafeProcess' inputstream: ", ioe);
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

//**************** Static methods **************//


    // logger and DebugStream print commands are synchronized, therefore thread safe.
    public static void log(String msg) {
	logger.info(msg);

	//System.err.println(msg);

	//DebugStream.println(msg);
    }

    public static void log(String msg, Exception e) { // Print stack trace on the exception
	logger.error(msg, e);

	//System.err.println(msg);
	//e.printStackTrace();

	//DebugStream.println(msg);
	//DebugStream.printStackTrace(e);
    }

    public static void log(Exception e) {
	logger.error(e);

	//e.printStackTrace();

	//DebugStream.printStackTrace(e);
    }

    public static void log(String msg, Exception e, boolean printStackTrace) {
	if(printStackTrace) { 
	    log(msg, e);
	} else {
	    log(msg);
	}
    }

    public static String streamToString(int src) {
	String stream;
	switch(src) {
	case STDERR:
	    stream = "stderr"; 
	    break;
	case STDOUT:
	    stream = "stdout";
	    break;
	default:
	    stream = "stdin";
	}
	return stream;
    }

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
	    log("Exception closing resource: " + e.getMessage(), e);
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

// Moved from GShell.java
    /** Determine if the given process is still executing. It does this by attempting to throw an exception - not the most efficient way, but the only one as far as I know
     * @param process the Process to test
     * @return true if it is still executing, false otherwise
     */
    static public boolean processRunning(Process process) {
	boolean process_running = false;

	try {
	    process.exitValue(); // This will throw an exception if the process hasn't ended yet.
	}
	catch(IllegalThreadStateException itse) {
	    process_running = true;
	}
	catch(Exception exception) {
	    log(exception); // DebugStream.printStackTrace(exception);
	}
	return process_running;
    }

} // end class SafeProcess
