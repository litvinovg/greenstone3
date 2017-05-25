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
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import javax.swing.SwingUtilities;


import com.sun.jna.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.lang.reflect.Field;

import org.apache.log4j.*;

//import org.greenstone.gatherer.DebugStream;

// Use this class to run a Java Process. It follows the good and safe practices at
// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
// to avoid blocking problems that can arise from a Process' input and output streams.

// On Windows, Perl could launch processes as proper ProcessTrees: http://search.cpan.org/~gsar/libwin32-0.191/
// Then killing the root process will kill child processes naturally.

public class SafeProcess {
    public static int DEBUG = 0;

    public static final int STDERR = 0;
    public static final int STDOUT = 1;
    public static final int STDIN = 2;
    // can't make this variable final and init in a static block, because it needs to use other SafeProcess static methods which rely on this in turn:
    public static String WIN_KILL_CMD;

	/**
	 * Boolean interruptible is used to mark any sections of blocking code that should not be interrupted
	 * with an InterruptedExceptions. At present only the cancelRunningProcess() attempts to do such a thing
	 * and avoids doing so when interruptible is false.
	 * Note that interruptible is also used as a lock, so remember to synchronize on it when using it!
	*/
    public Boolean interruptible = Boolean.TRUE; 
	
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
    private boolean forciblyTerminateProcess = false;

    /** a ref to the thread in which the Process is being executed (the thread wherein Runtime.exec() is called) */
    private Thread theProcessThread = null;
	
    // output from running SafeProcess.runProcess()
    private String outputStr = ""; 
    private String errorStr = "";
    private int exitValue = -1;
    //private String charset = null;

    // allow callers to process exceptions of the main process thread if they want
    private ExceptionHandler exceptionHandler = null;
	/** allow callers to implement hooks that get called during the main phases of the internal
	 * process' life cycle, such as before and after process.destroy() gets called
	*/
    private MainProcessHandler mainHandler = null;

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

    /** to set a handler that will handle the main (SafeProcess) thread,
	 * implementing the hooks that will get called during the internal process' life cycle,
     * such as before and after process.destroy() is called */
    public void setMainHandler(MainProcessHandler handler) {
	this.mainHandler = handler;
    }

    // set if you want the std output or err output to have \n at each newline read from the stream
    public void setSplitStdOutputNewLines(boolean split) {
	splitStdOutputNewLines = split;
    }
    public void setSplitStdErrorNewLines(boolean split) {
	splitStdErrorNewLines = split;
    }

	
    /*
    public boolean canInterrupt() {
	boolean canInterrupt;
	synchronized(interruptible) {
	    canInterrupt = interruptible.booleanValue();
	}
	return canInterrupt;
    }
    */

    /**
	 * Call this method when you want to prematurely and safely terminate any process
	 * that SafeProcess may be running.
	 * You may want to implement the SafeProcess.MainHandler interface to write code
	 * for any hooks that will get called during the process' life cycle.
     * @return false if process has already terminated or if it was already terminating
     * when cancel was called. In such cases no interrupt is sent. Returns boolean sentInterrupt.
     */
    public synchronized boolean cancelRunningProcess() {
	// on interrupt:
	// - forciblyTerminate will be changed to true if the interrupt came in when the process was
	// still running (and so before the process' streams were being joined)
	// - and forciblyTerminate will still remain false if the interrupt happens when the process'
	// streams are being/about to be joined (hence after the process naturally terminated).
	// So we don't touch the value of this.forciblyTerminate here.
	// The value of forciblyTerminate determines whether Process.destroy() and its associated before
	// and after handlers are called or not: we don't bother destroying the process if it came to
	// a natural end.

	// no process to interrupt, so we're done
	if(this.process == null) {
	    log("@@@ No Java process to interrupt.");
	    return false;
	}
	
	boolean sentInterrupt = false;

	// can't interrupt when SafeProcess is joining (cleanly terminating) worker threads
	// have to wait until afterward	    
	if (interruptible) {
	    // either way, we can now interrupt the thread - if we have one (we should)
	    if(this.theProcessThread != null) { // we're told which thread should be interrupted
		this.theProcessThread.interrupt();
		log("@@@ Successfully sent interrupt to process.");
		sentInterrupt = true;
	    }
	}
	else { // wait for join()s to finish.
	    // During and after joining(), there's no need to interrupt any more anyway: no calls
	    // subsequent to joins() block, so everything thereafter is insensitive to InterruptedExceptions.

	    if(SwingUtilities.isEventDispatchThread()) {
		log("#### Event Dispatch thread, returning");
		return false;
	    }

	    while(!interruptible) {

		log("######### Waiting for process to become interruptible...");
		
		// https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html
		// wait will release lock on this object, and regain it when loop condition interruptible is true
		try {
		    this.wait(); // can't interrupt when SafeProcess is joining (cleanly terminating) worker threads, so wait
		} catch(Exception e) {
		    log("@@@ Interrupted exception while waiting for SafeProcess' worker threads to finish joining on cancelling process");
		}
	    }

	    // now the process is sure to have ended as the worker threads would have been joined
	}

	return sentInterrupt;
    }

	
    // In future, think of changing the method doRuntimeExec() over to using ProcessBuilder
    // instead of Runtime.exec(). ProcessBuilder seems to have been introduced from Java 5.
    // https://docs.oracle.com/javase/7/docs/api/java/lang/ProcessBuilder.html
    // See also https://zeroturnaround.com/rebellabs/how-to-deal-with-subprocesses-in-java/
    // which suggests using Apache Common Exec to launch processes and says what will be forthcoming in Java 9
    
    private Process doRuntimeExec() throws IOException {
	Process prcs = null;
	Runtime rt = Runtime.getRuntime();
	
	if(this.command != null) {
	    log("SafeProcess running: " + command);
	    prcs = rt.exec(this.command);
	}
	else { // at least command_args must be set now
	    
	    // http://stackoverflow.com/questions/5283444/convert-array-of-strings-into-a-string-in-java
	    //log("SafeProcess running:" + Arrays.toString(command_args));
	    StringBuffer cmdDisplay = new StringBuffer();
	    for(int i = 0; i < command_args.length; i++) {
		cmdDisplay.append(" ").append(command_args[i]);
	    }
	    log("SafeProcess running: [" + cmdDisplay + "]");
	    cmdDisplay = null; // let the GC have it    
	    
	    
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

	this.theProcessThread = Thread.currentThread(); // store a ref to the thread wherein the Process is being run
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
	
	try {
	    this.exitValue = process.waitFor(); // can throw an InterruptedException if process was cancelled/prematurely terminated
	} catch(InterruptedException ie) {
	    log("*** Process interrupted (InterruptedException). Expected to be a Cancel operation.");
	        // don't print stacktrace: an interrupt here is not an error, it's expected to be a cancel action
	    if(exceptionHandler != null) {		
		exceptionHandler.gotException(ie);
	    }	        
	    
	    // propagate interrupts to worker threads here
	    // unless the interrupt emanated from any of them in any join(),
	    // which will be caught by the calling method's own catch on InterruptedException.
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
	    
	    // Since we have been cancelled (InterruptedException), or on any Exception, we need
	    // to forcibly terminate process eventually after the finally code first waits for each worker thread
	    // to die off. Don't set process=null until after we've forcibly terminated it if needs be.
	    this.forciblyTerminateProcess = true; 
		
	    // even after the interrupts, we want to proceed to calling join() on all the worker threads
	    // in order to wait for each of them to die before attempting to destroy the process if it
	    // still hasn't terminated after all that.
	} finally {		
	    
	    //log("Process exitValue: " + exitValue);
	    ///log("@@@@ Before join phase. Forcibly terminating: " + this.forciblyTerminateProcess);
	    
	    // From the comments of 
	    // http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	    // To avoid running into nondeterministic failures to get the process output
	    // if there's no waiting for the threads, call join() on each Thread (StreamGobbler) object.
	    // From Thread API: join() "Waits for this thread (the thread join() is invoked on) to die."
	    
	    // Wait for each of the threads to die, before attempting to destroy the process
	    // Any of these can throw InterruptedExceptions too
	    // and will be processed by the calling function's catch on InterruptedException.


	    // Thread.joins() below are blocking calls, same as Process.waitFor(), and a cancel action could
	    // send an interrupt during any Join: the InterruptedException ensuing will then break out of the
	    // joins() section. We don't want that to happen: by the time the joins() start happening, the
	    // actual process has finished in some way (naturally terminated or interrupted), and nothing
	    // should interrupt the joins() (nor ideally any potential p.destroy after that).
	    // So we mark the join() section as an un-interruptible section, and make anyone who's seeking
	    // to interrupt just then first wait for this Thread (in which SafeProcess runs) to become
	    // interruptible again. Thos actually assumes anything interruptible can still happen thereafter
	    // when in reality, none of the subsequent actions after the joins() block. So they nothing
	    // thereafter, which is the cleanup phase, will actually respond to an InterruptedException.

	    
	    if(this.mainHandler != null) {
		// this method can unset forcible termination flag
		// if the process had already naturally terminated by this stage:
		this.forciblyTerminateProcess = mainHandler.beforeWaitingForStreamsToEnd(this.forciblyTerminateProcess);
	    }

	    ///log("@@@@ After beforeJoin Handler. Forcibly terminating: " + this.forciblyTerminateProcess);

	    // Anyone could interrupt/cancel during waitFor() above,
	    // but no one should interrupt while the worker threads come to a clean close,
	    // so make anyone wanting to cancel the process at this stage wait()
	    // until we're done with the join()s:
	    synchronized(interruptible) {
		interruptible = Boolean.FALSE;
	    }
	    //Thread.sleep(5000); // Uncomment to test this uninterruptible section, also comment out block checking for
			// EventDispatchThread in cancelRunningProcess() and 2 calls to progress.enableCancelJob() in DownloadJob.java
	    outputGobbler.join();
	    errorGobbler.join();
	    inputGobbler.join(); 

	    synchronized(interruptible) {
		interruptible = Boolean.TRUE;
	    }

	    ///log("@@@@ Join phase done...");

	    // notify any of those waiting to interrupt this thread, that they may feel free to do so again
	    // https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html
	    synchronized(this) {
		this.notify();
	    }

	    // set the variables that the code which created a SafeProcess object may want to inspect
	    this.outputStr = outputGobbler.getOutput();
	    this.errorStr = errorGobbler.getOutput();
	    
	    // call the after join()s hook
	    if(this.mainHandler != null) {
		this.forciblyTerminateProcess = mainHandler.afterStreamsEnded(this.forciblyTerminateProcess);
	    }
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
    // this just execs the process and waits for it to return.
    // Don't call this method but the zero-argument runProcess() instead if your process will
    // output stuff to its stderr and stdout streams but you don't need to monitory these.
    // Because, as per a comment in GLI's GS3ServerThread.java,
    // in Java 6, it wil block if you don't handle a process' streams when the process is
    // outputting something. (Java 7+ won't block if you don't bother to handle the output streams)
    public int runBasicProcess() {
	try {
	    this.forciblyTerminateProcess = true;
		
	    // 1. create the process
	    process = doRuntimeExec();
	    // 2. basic waitFor the process to finish
	    this.exitValue = process.waitFor();

	    this.forciblyTerminateProcess = false;
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
	    
	    cleanUp("SafeProcess.runBasicProcess");
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
	    this.forciblyTerminateProcess = false;
		
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
		    = new SafeProcess.InputStreamGobbler(process.getErrorStream(), this.splitStdErrorNewLines);
	    } else {
		errorGobbler
		    = new SafeProcess.InputStreamGobbler(process.getErrorStream(), procErrHandler);
	    }

            // PROC OUT STREAM to monitor for the expected std output line(s)
	    if(procOutHandler == null) {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(process.getInputStream(), this.splitStdOutputNewLines);
	    } else {
		outputGobbler
		    = new SafeProcess.InputStreamGobbler(process.getInputStream(), procOutHandler);
	    }


	    // 3. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    this.forciblyTerminateProcess = true;

	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		log("IOexception: " + ioe.getMessage(), ioe);
	    }
	} catch(InterruptedException ie) { // caused during any of the gobblers.join() calls, this is unexpected so print stack trace
	    this.forciblyTerminateProcess = true;
		
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ie);
		log("@@@@ Unexpected InterruptedException when waiting for process stream gobblers to die");
	    } else {
		log("*** Unexpected InterruptException when waiting for process stream gobblers to die: " + ie.getMessage(), ie);
	    }

	    // see comments in other runProcess()
	    Thread.currentThread().interrupt();
	
	} finally {
	    
	    cleanUp("SafeProcess.runProcess(3 params)");
	}
	
	return this.exitValue;
    }
    
    public int runProcess(LineByLineHandler outLineByLineHandler, LineByLineHandler errLineByLineHandler)
    {
	SafeProcess.OutputStreamGobbler inputGobbler = null;
	SafeProcess.InputStreamGobbler errorGobbler = null;
	SafeProcess.InputStreamGobbler outputGobbler = null;

	try {
	    this.forciblyTerminateProcess = false;
		
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


	    // 3. kick off the stream gobblers
	    this.exitValue = waitForWithStreams(inputGobbler, outputGobbler, errorGobbler);
       
	} catch(IOException ioe) {
	    this.forciblyTerminateProcess = true;
		
	    if(exceptionHandler != null) {
		exceptionHandler.gotException(ioe);
	    } else {
		log("IOexception: " + ioe.getMessage(), ioe);		
	    }
	} catch(InterruptedException ie) { // caused during any of the gobblers.join() calls, this is unexpected so log it
	    this.forciblyTerminateProcess = true;
		
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
	    
	    cleanUp("SafeProcess.runProcess(2 params)");	
	}
	
	return this.exitValue;
    }

    private void cleanUp(String callingMethod) {
	
	// Moved into here from GS2PerlConstructor and GShell.runLocal() which said
	// "I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine (no it doesn't!)"
	// http://steveliles.github.io/invoking_processes_from_java.html
	// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
	// http://mark.koli.ch/leaky-pipes-remember-to-close-your-streams-when-using-javas-runtimegetruntimeexec	    

	//String cmd = (this.command == null) ? Arrays.toString(this.command_args) : this.command;
	//log("*** In finally of " + callingMethod + ": " + cmd);

	// if we're forcibly terminating the process, call the before- and afterDestroy hooks
	// besides actually destroying the process
	if( this.forciblyTerminateProcess ) {
	    log("*** Going to call process.destroy from " + callingMethod);

	    if(mainHandler != null) mainHandler.beforeProcessDestroy();
	    boolean noNeedToDestroyIfOnLinux = true; // Interrupt handling suffices to cleanup process and subprocesses on Linux
	    SafeProcess.destroyProcess(process, noNeedToDestroyIfOnLinux); // see runProcess(2 args/3 args)
	    if(mainHandler != null) mainHandler.afterProcessDestroy();

	    log("*** Have called process.destroy from " + callingMethod);
	}	

	process = null;
	this.theProcessThread = null; // let the process thread ref go too
	boolean wasForciblyTerminated = this.forciblyTerminateProcess;
	this.forciblyTerminateProcess = false; // reset
	
	if(mainHandler != null) mainHandler.doneCleanup(wasForciblyTerminated);
    }

/*

 On Windows, p.destroy() terminates process p that Java launched,
 but does not terminate any processes that p may have launched. Presumably since they didn't form a proper process tree.
    https://social.msdn.microsoft.com/Forums/windowsdesktop/en-US/e3cb7532-87f6-4ae3-9d80-a3afc8b9d437/how-to-kill-a-process-tree-in-cc-on-windows-platform?forum=vclanguage
    https://msdn.microsoft.com/en-us/library/windows/desktop/ms684161(v=vs.85).aspx

 Searching for: "forcibly terminate external process launched by Java on Windows"	
 Not possible: stackoverflow.com/questions/1835885/send-ctrl-c-to-process-open-by-java 
 But can use taskkill or tskill or wmic commands to terminate a process by processID
 stackoverflow.com/questions/912889/how-to-send-interrupt-key-sequence-to-a-java-process
 Taskkill command can kill by Image Name, such as all running perl, e.g. taskkill /f /im perl.exe
 But what if we kill perl instances not launched by GS?
    /f Specifies to forcefully terminate the process(es). We need this flag switched on to kill childprocesses.
    /t Terminates the specified process and any child processes which were started by it. 
			/t didn't work to terminate subprocesses. Maybe since the process wasn't launched as 
			a properly constructed processtree.
    /im is the image name (the name of the program), see Image Name column in Win Task Manager.
	
 We don't want to kill all perl running processes. 
 Another option is to use wmic, available since Windows XP, to kill a process based on its command
 which we sort of know (SafeProcess.command) and which can be seen in TaskManager under the 
 "Command Line" column of the Processes tab.
    https://superuser.com/questions/52159/kill-a-process-with-a-specific-command-line-from-command-line	
 The following works kill any Command Line that matches -site localsite lucene-jdbm-demo
    C:>wmic PATH win32_process Where "CommandLine like '%-site%localsite%%lucene-jdbm-demo%'" Call Terminate
 "WMIC Wildcard Search using 'like' and %"
    https://codeslammer.wordpress.com/2009/02/21/wmic-wildcard-search-using-like-and/
 However, we're not even guaranteed that every perl command GS launches will contain the collection name
 Nor do we want to kill all perl processes that GS launches with bin\windows\perl\bin\perl, though this works:
    wmic PATH win32_process Where "CommandLine like '%bin%windows%perl%bin%perl%'" Call Terminate
 The above could kill GS perl processes we don't intend to terminate, as they're not spawned by the particular
 Process we're trying to terminate from the root down.
	
 Solution: We can use taskkill or the longstanding tskill or wmic to kill a process by ID. Since we can
 kill an external process that SafeProcess launched OK, and only have trouble killing any child processes
 it launched, we need to know the pids of the child processes. 
 
 We can use Windows' wmic to discover the childpids of a process whose id we know.
 And we can use JNA to get the process ID of the external process that SafeProcess launched.
 
 To find the processID of the process launched by SafeProcess,
 need to use Java Native Access (JNA) jars, available jna.jar and jna-platform.jar.
    http://stackoverflow.com/questions/4750470/how-to-get-pid-of-process-ive-just-started-within-java-program
    http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
    http://www.golesny.de/p/code/javagetpid
    https://github.com/java-native-access/jna/blob/master/www/GettingStarted.md
 We're using JNA v 4.1.0, https://mvnrepository.com/artifact/net.java.dev.jna/jna
  
 WMIC can show us a list of parent process id and process id of running processes, and then we can
 kill those child processes with a specific process id.
    https://superuser.com/questions/851692/track-which-program-launches-a-certain-process
    http://stackoverflow.com/questions/7486717/finding-parent-process-id-on-windows
 WMIC can get us the pids of all childprocesses launched by parent process denoted by parent pid.
 And vice versa:
    if you know the parent pid and want to know all the pids of the child processes spawned:
        wmic process where (parentprocessid=596) get processid
    if you know a child process id and want to know the parent's id:
        wmic process where (processid=180) get parentprocessid
 
 The above is the current solution.
 
 Eventually, instead of running a windows command to kill the process ourselves, consider changing over to use
    https://github.com/flapdoodle-oss/de.flapdoodle.embed.process/blob/master/src/main/java/de/flapdoodle/embed/process/runtime/Processes.java 
 (works with Apache license, http://www.apache.org/licenses/LICENSE-2.0)
 This is a Java class that uses JNA to terminate processes. It also has the getProcessID() method. 
 
 Linux ps equivalent on Windows is "tasklist", see
    http://stackoverflow.com/questions/4750470/how-to-get-pid-of-process-ive-just-started-within-java-program

*/

// http://stackoverflow.com/questions/4750470/how-to-get-pid-of-process-ive-just-started-within-java-program
// Uses Java Native Access, JNA
public static long getProcessID(Process p)
{
    long pid = -1;
    try	{
	//for windows
	if (p.getClass().getName().equals("java.lang.Win32Process") ||
	    p.getClass().getName().equals("java.lang.ProcessImpl")) 
	    {
		Field f = p.getClass().getDeclaredField("handle");
		f.setAccessible(true);              
		long handl = f.getLong(p);
		Kernel32 kernel = Kernel32.INSTANCE;
		WinNT.HANDLE hand = new WinNT.HANDLE();
		hand.setPointer(Pointer.createConstant(handl));
		pid = kernel.GetProcessId(hand);
		f.setAccessible(false);
	    }
	//for unix based operating systems
	else if (p.getClass().getName().equals("java.lang.UNIXProcess")) 
	    {
		Field f = p.getClass().getDeclaredField("pid");
		f.setAccessible(true);
		pid = f.getLong(p);
		f.setAccessible(false);
	    }

    } catch(Exception ex) {
	log("SafeProcess.getProcessID(): Exception when attempting to get process ID for process " + ex.getMessage(), ex);	
	pid = -1;
    }
    return pid;
}
    

// Can't artificially send Ctrl-C: stackoverflow.com/questions/1835885/send-ctrl-c-to-process-open-by-java
// (Taskkill command can kill all running perl. But what if we kill perl instances not launched by GS?)
// stackoverflow.com/questions/912889/how-to-send-interrupt-key-sequence-to-a-java-process
// Searching for: "forcibly terminate external process launched by Java on Windows"	
static void killWinProcessWithID(long processID) {
    
    String cmd = SafeProcess.getWinProcessKillCmd(processID);
    if (cmd == null) return;
    
    try {		
	log("\tAttempting to terminate Win subprocess with pid: " + processID);
	SafeProcess proc = new SafeProcess(cmd);			
	int exitValue = proc.runProcess(); // no IOstreams for Taskkill, but for "wmic process pid delete"
	// there is output that needs flushing, so don't use runBasicProcess()
			
    } catch(Exception e) {
	log("@@@ Exception attempting to stop perl " + e.getMessage(), e);		
    }
}

// Kill signals, their names and numerical equivalents: http://www.faqs.org/qa/qa-831.html
// https://stackoverflow.com/questions/8533377/why-child-process-still-alive-after-parent-process-was-killed-in-linux
// Didn't work for when build scripts run from GLI: kill -TERM -pid
// but the other suggestion did work: pkill -TERM -P pid did work
// More reading:
// https://linux.die.net/man/1/kill (manual)
// https://unix.stackexchange.com/questions/117227/why-pidof-and-pgrep-are-behaving-differently
// https://unix.stackexchange.com/questions/67635/elegantly-get-list-of-children-processes
// https://stackoverflow.com/questions/994033/mac-os-x-quickest-way-to-kill-quit-an-entire-process-tree-from-within-a-cocoa-a
// https://unix.stackexchange.com/questions/132224/is-it-possible-to-get-process-group-id-from-proc
// https://unix.stackexchange.com/questions/99112/default-exit-code-when-process-is-terminated

/**
 * On Unix, will kill the process denoted by processID and any subprocessed this launched. Tested on a Mac, where this is used.
 * @param force if true will send the -KILL (-9) signal, which may result in abrupt termination without cleanup
 * if false, will send the -TERM (-15) signal, which will allow cleanup before termination. Sending a SIGTERM is preferred.
 * @param killEntireTree if false, will terminate only the process denoted by processID, otherwise all descendants/subprocesses too.
 * @return true if running the kill process returned an exit value of 0 or if it had already been terminated
*/
static boolean killUnixProcessWithID(long processID, boolean force, boolean killEntireTree) {
    
    String signal = force ? "KILL" : "TERM"; // kill -KILL (kill -9) vs preferred kill -TERM (kill -15)
    String cmd;
    if(killEntireTree) { // kill the process denoted by processID and any subprocesses this launched

	if(Misc.isMac()) { 
	    // this cmd works on Mac (tested Snow Leopard), but on Linux this cmd only terminates toplevel process
	    // when doing full-import, and doesn't always terminate subprocesses when doing full-buildcol.pl
	    cmd = "pkill -"+signal + " -P " + processID; // e.g. "pkill -TERM -P pid"
	} 
	else { // other unix
	    // this cmd works on linux, not recognised on Mac (tested Snow Leopard):
	    cmd = "kill -"+signal + " -"+processID; // e.g. "kill -TERM -pid"
	                                            // note the hyphen before pid to terminate subprocesses too
	}

    } else { // kill only the process represented by the processID.
	cmd = "kill -"+signal + " " + processID; // e.g. "kill -TERM pid"
    } 

    SafeProcess proc = new SafeProcess(cmd);
    int exitValue = proc.runProcess();
    
    
    if(exitValue == 0) {
	if(force) {
	    log("@@@ Successfully sent SIGKILL to unix process tree rooted at " + processID);
	} else { 
	    log("@@@ Successfully sent SIGTERM to unix process tree rooted at " + processID);
	}
	return true;
    } else if(!Misc.isMac() && exitValue == 1) {
	// https://stackoverflow.com/questions/28332888/return-value-of-kill
	// "kill returns an exit code of 0 (true) if the process still existed it and was killed.
	// kill returns an exit code of 1 (false) if the kill failed, probably because the process was no longer running."
	// On Linux, interrupting the process and its worker threads and closing resources already successfully terminates
	// the process and its subprocesses (don't need to call this method at all to terminate the processes: the processes
	// aren't running when we get to this method)
	log("@@@ Sending termination signal returned exit value 1. On linux this happens when the process has already been terminated");
	return true;
    } else {
	log("@@@ Not able to successfully terminate process, got exitvalue " + exitValue);
	log("@@@ Got output: |" + proc.getStdOutput() + "|"); 
	log("@@@ Got err output: |" + proc.getStdError() + "|");
	// caller can try again with kill -KILL, by setting force parameter to true
	return false;
    }
}

public static void destroyProcess(Process p) {
    // A cancel action results in an interruption to the process thread, which in turn interrupts
    // the SafeProcess' worker threads, all which clean up after themselves.
    // On linux, this suffices to cleanly terminate a Process and any subprocesses that may have launched
    // so we don't need to do extra work in such a case. But the interrupts happen only when SafeProcess calls
    // destroyProcess() on the Process it was running internally, and not if anyone else tries to end a 
    // Process by calling SafeProcess.destroyProcess(p). In such cases, the Process needs to be actively terminated:
    boolean canSkipExtraWorkIfLinux = true;
    SafeProcess.destroyProcess(p, !canSkipExtraWorkIfLinux);
}

// On linux, the SafeProcess code handling an Interruption suffices to successfully and cleanly terminate
// the process and any subprocesses launched by p as well (and not even an extra p.destroy() is needed).
// On Windows, and Mac too, we need to do more work, since otherwise processes launched by p remain
// around executing until they naturally terminate.
// e.g. full-import.pl may be terminated with p.destroy(), but it launches import.pl which is left running until it naturally terminates.
private static void destroyProcess(Process p, boolean canSkipExtraWorkIfLinux) {
    log("### in SafeProcess.destroyProcess(Process p)");

    // If it isn't windows, process.destroy() terminates any child processes too
    if(Misc.isWindows()) {
	
	if(!SafeProcess.isAvailable("wmic")) {
	    log("wmic, used to kill subprocesses, is not available. Unable to terminate subprocesses...");
	    log("Kill them manually from the TaskManager or they will proceed to run to termination");
	    
	    // At least we can get rid of the top level process we launched
	    p.destroy();
	    return;
	}	
	
	// get the process id of the process we launched,
	// so we can use it to find the pids of any subprocesses it launched in order to terminate those too.
	
	long processID = SafeProcess.getProcessID(p);
	if(processID == -1) { // the process doesn't exist or no longer exists (terminated naturally?)
	    p.destroy(); // minimum step, do this anyway, at worst there's no process and this won't have any effect
	} else {
	    log("Attempting to terminate sub processes of Windows process with pid " + processID);
	    terminateSubProcessesRecursively(processID, p);
	}
	return;
	
    }
    else { // linux or mac

	// if we're on linux and would have already terminated by now (in which case canSkipExtraWorkForLinux would be true),
	// then there's nothing much left to do. This would only be the case if SafeProcess is calling this method on its
	// internal process, since it would have successfully cleaned up on Interruption and there would be no process left running
	if(!Misc.isMac() && canSkipExtraWorkIfLinux) {
	    log("@@@ Linux: Cancelling a SafeProcess instance does not require any complicated system destroy operation");
	    p.destroy(); // vestigial: this will have no effect if the process had already terminated, which is the case in this block
	    return;
	}
	// else we're on a Mac or an external caller (not SafeProcess) has requested explicit termination on Linux
	
	long pid = SafeProcess.getProcessID(p);
	/*
	// On Macs (all Unix?) can't get the child processes of a process once it's been destroyed
	macTerminateSubProcessesRecursively(pid, p); // pid, true)	
	*/
	
	if(pid == -1) {
	    p.destroy(); // at minimum, will have no effect if the process had already terminated 
	} else {
	    boolean forceKill = true;
	    boolean killEntireProcessTree = true;
	    
	    if(!killUnixProcessWithID(pid, !forceKill, killEntireProcessTree)) { // send sig TERM (kill -15 or kill -TERM)
		killUnixProcessWithID(pid, forceKill, killEntireProcessTree); // send sig KILL (kill -9 or kill -KILL)
	    }	    
	}
	
	return;
    }    
}


// UNUSED and INCOMPLETE
// But if this method is needed, then need to parse childpids printed by "pgrep -P pid" and write recursive step
// The childpids are probably listed one per line, see https://unix.stackexchange.com/questions/117227/why-pidof-and-pgrep-are-behaving-differently
private static void macTerminateSubProcessesRecursively(long parent_pid, Process p) { //boolean isTopLevelProcess) {
    log("@@@ Attempting to terminate mac process recursively");

    // https://unix.stackexchange.com/questions/67635/elegantly-get-list-of-children-processes
    SafeProcess proc = new SafeProcess("pgrep -P "+parent_pid);
    int exitValue = proc.runProcess();
    String stdOutput = proc.getStdOutput();
    String stdErrOutput = proc.getStdError();

    // now we have the child processes, can terminate the parent process
    if(p != null) { // top level process, can just be terminated the java way with p.destroy()
	p.destroy();
    } else {
	boolean forceKill = true;
	boolean killSubprocesses = true;
	// get rid of process denoted by the current pid (but not killing subprocesses it may have launched,
	// since we'll deal with them recursively)
	if(!SafeProcess.killUnixProcessWithID(parent_pid, !forceKill, !killSubprocesses)) { // send kill -TERM, kill -15
	    SafeProcess.killUnixProcessWithID(parent_pid, forceKill, !killSubprocesses); // send kill -9, kill -KILL
	}
    }
    
    /*
    // get rid of any process with current pid
    if(!isTopLevelProcess && !SafeProcess.killUnixProcessWithID(parent_pid, false)) { // send kill -TERM, kill -15
	SafeProcess.killUnixProcessWithID(parent_pid, true); // send kill -9, kill -KILL
    }
    */

    if(stdOutput.trim().equals("") && stdErrOutput.trim().equals("") && exitValue == 1) {
	log("No child processes");
	// we're done
	return;
    } else {
	log("Got childpids on STDOUT: " + stdOutput);
	log("Got childpids on STDERR: " + stdErrOutput);
    }
}

// Helper function. Only for Windows.
// Counterintuitively, we're be killing all parent processess and then all child procs and all their descendants
// as soon as we discover any further process each (sub)process has launched. The parent processes are killed
// first in each case for 2 reasons: 
// 1. on Windows, killing the parent process leaves the child running as an orphan anyway, so killing the
// parent is an independent action, the child process is not dependent on the parent;
// 2. Killing a parent process prevents it from launching further processes while we're killing off each child process
private static void terminateSubProcessesRecursively(long parent_pid, Process p) {
	
    // Use Windows wmic to find the pids of any sub processes launched by the process denoted by parent_pid	
    SafeProcess proc = new SafeProcess("wmic process where (parentprocessid="+parent_pid+") get processid");
    proc.setSplitStdOutputNewLines(true); // since this is windows, splits lines by \r\n
    int exitValue = proc.runProcess(); // exitValue (%ERRORLEVEL%) is 0 either way.	
    //log("@@@@ Return value from proc: " + exitValue);
    
    // need output from both stdout and stderr: stderr will say there are no pids, stdout will contain pids
    String stdOutput = proc.getStdOutput();
    String stdErrOutput = proc.getStdError();
    
    
    // Now we know the pids of the immediate subprocesses, we can get rid of the parent process
    // We know the children remain running: since the whole problem on Windows is that these
    // child processes remain running as orphans after the parent is forcibly terminated.
    if(p != null) { // we're the top level process, terminate the java way
	p.destroy();
    } else { // terminate windows way		
	SafeProcess.killWinProcessWithID(parent_pid); // get rid of process with current pid
    }
    
    // parse the output to get the sub processes' pids	
    // Output looks like:
    // ProcessId
    // 6040
    // 180
    // 4948
    // 1084
    // 6384
    // If no children, then STDERR output starts with the following, possibly succeeded by empty lines:
    // No Instance(s) Available.
    
    // base step of the recursion
    if(stdErrOutput.indexOf("No Instance(s) Available.") != -1) { 
	//log("@@@@ Got output on stderr: " + stdErrOutput);
	// No further child processes. And we already terminated the parent process, so we're done
	return;
    } else {
	//log("@@@@ Got output on stdout:\n" + stdOutput);
	
	// http://stackoverflow.com/questions/691184/scanner-vs-stringtokenizer-vs-string-split	
	
	// find all childprocesses for that pid and terminate them too:
	Stack<Long> subprocs = new Stack<Long>();
	Scanner sc = new Scanner(stdOutput);
	while (sc.hasNext()) {
	    if(!sc.hasNextLong()) {
		sc.next(); // discard the current token since it's not a Long
	    } else {
		long child_pid = sc.nextLong();
		subprocs.push(new Long(child_pid));			
	    }
	}
	sc.close();		
	
	// recursion step if subprocs is not empty (but if it is empty, then it's another base step)
	if(!subprocs.empty()) {
	    long child_pid = subprocs.pop().longValue();
	    terminateSubProcessesRecursively(child_pid, null);
	}	
    }
}

// This method should only be called on a Windows OS
private static String getWinProcessKillCmd(Long processID) {
    // check if we first need to init WIN_KILL_CMD. We do this only once, but can't do it in a static codeblock
    // because of a cyclical dependency regarding this during static initialization

    if(WIN_KILL_CMD == null) {		
	if(SafeProcess.isAvailable("wmic")) {
	    // https://isc.sans.edu/diary/Windows+Command-Line+Kung+Fu+with+WMIC/1229
	    WIN_KILL_CMD = "wmic process _PROCID_ delete"; // like "kill -9" on Windows
	}
	else if(SafeProcess.isAvailable("taskkill")) { // check if we have taskkill or else use the longstanding tskill
	    
	    WIN_KILL_CMD = "taskkill /f /t /PID _PROCID_"; // need to forcefully /f terminate the process				
	        //  /t "Terminates the specified process and any child processes which were started by it."
	        // But despite the /T flag, the above doesn't kill subprocesses.
	}
	else { //if(SafeProcess.isAvailable("tskill")) { can't check availability since "which tskill" doesn't ever succeed
	    WIN_KILL_CMD = "tskill _PROCID_"; // https://ss64.com/nt/tskill.html
	}		
    }
    
    if(WIN_KILL_CMD == null) { // can happen if none of the above cmds were available
	return null;
    }
    return WIN_KILL_CMD.replace( "_PROCID_", Long.toString(processID) );
}


// Run `which` on a program to find out if it is available. which.exe is included in winbin.
// On Windows, can use where or which. GLI's file/FileAssociationManager.java used which, so we stick to the same.
// where is not part of winbin. where is a system command on windows, but only since 2003, https://ss64.com/nt/where.html
// There is no `where` on Linux/Mac, must use which for them.
// On windows, "which tskill" fails (and "where tskill" works), but "which" succeeds on taskkill|wmic|browser names.
public static boolean isAvailable(String program) {		
    try {
	// On linux `which bla` does nothing, prompt is returned; on Windows, it prints "which: no bla in"
	// `which grep` returns a line of output with the path to grep. On windows too, the location of the program is printed
	SafeProcess prcs = new SafeProcess("which " + program);		
	prcs.runProcess();
	String output = prcs.getStdOutput().trim();
	///System.err.println("*** 'which " + program + "' returned: |" + output + "|");
	if(output.equals("")) {
	    return false;
	} else if(output.indexOf("no "+program) !=-1) { // from GS3's org.greenstone.util.BrowserLauncher.java's isAvailable(program)
	    log("@@@ SafeProcess.isAvailable(): " + program + "is not available");
	    return false;
	}
	//System.err.println("*** 'which " + program + "' returned: " + output);
	return true;
    } catch (Exception exc) {
	return false;
    }
}	
	
// Google Java external process destroy kill subprocesses
// https://zeroturnaround.com/rebellabs/how-to-deal-with-subprocesses-in-java/	
	
//******************** Inner class and interface definitions ********************//
// Static inner classes can be instantiated without having to instantiate an object of the outer class first

// Can have public static interfaces too,
// see http://stackoverflow.com/questions/71625/why-would-a-static-nested-interface-be-used-in-java
// Implementors need to take care that the implementations are thread safe
// http://stackoverflow.com/questions/14520814/why-synchronized-method-is-not-included-in-interface
public static interface ExceptionHandler {

    /**
     * Called whenever an exception occurs during the execution of the main thread of SafeProcess
     * (the thread in which the Process is run).
     * Since this method can't be declared as synchronized in this interface method declaration,
     * when implementing ExceptionHandler.gotException(), if it manipulates anything that's
     * not threadsafe, declare gotException() as a synchronized method to ensure thread safety
     */
    public void gotException(Exception e);
}

/** On interrupting (cancelling) a process, 
 * if the class that uses SafeProcess wants to do special handling 
 * either before and after join() is called on all the worker threads,
 * or, only on forcible termination, before and after process.destroy() is to be called,
 * then that class can implement this MainProcessHandler interface
 */
public static interface MainProcessHandler {
    /** 
     * Called before the streamgobbler join()s.
     * If not overriding, the default implementation should be:
     * public boolean beforeWaitingForStreamsToEnd(boolean forciblyTerminating) { return forciblyTerminating; }
     * When overriding:
     * @param forciblyTerminating is true if currently it's been decided that the process needs to be
     * forcibly terminated. Return false if you don't want it to be. For a basic implementation,
     * return the parameter.
     * @return true if the process is still running and therefore still needs to be destroyed, or if
     * you can't determine whether it's still running or not. Process.destroy() will then be called.
     * @return false if the process has already naturally terminated by this stage. Process.destroy()
     * won't be called, and neither will the before- and after- processDestroy methods of this class.
    */
    public boolean beforeWaitingForStreamsToEnd(boolean forciblyTerminating);
    /** 
     * Called after the streamgobbler join()s have finished.
     * If not overriding, the default implementation should be:
     * public boolean afterStreamsEnded(boolean forciblyTerminating) { return forciblyTerminating; }
     * When overriding:
     * @param forciblyTerminating is true if currently it's been decided that the process needs to be
     * forcibly terminated. Return false if you don't want it to be. For a basic implementation,
     * return the parameter (usual case).
     * @return true if the process is still running and therefore still needs to be destroyed, or if
     * can't determine whether it's still running or not. Process.destroy() will then be called.
     * @return false if the process has already naturally terminated by this stage. Process.destroy()
     * won't be called, and neither will the before- and after- processDestroy methods of this class.
    */
    public boolean afterStreamsEnded(boolean forciblyTerminating);
    /**
     * called after join()s and before process.destroy()/destroyProcess(Process), iff forciblyTerminating
     */
    public void beforeProcessDestroy(); 
    /**
     * Called after process.destroy()/destroyProcess(Process), iff forciblyTerminating
     */
    public void afterProcessDestroy();

    /** 
     * Always called after process ended: whether it got destroyed or not
     */
    public void doneCleanup(boolean wasForciblyTerminated);
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
	    
	    /*if(Misc.isWindows()) {
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
	if(DEBUG == 0) return;
	logger.info(msg);

	//System.err.println(msg);

	//DebugStream.println(msg);
    }

    public static void log(String msg, Exception e) { // Print stack trace on the exception
	if(DEBUG == 0) return;
	logger.error(msg, e);

	//System.err.println(msg);
	//e.printStackTrace();

	//DebugStream.println(msg);
	//DebugStream.printStackTrace(e);
    }

    public static void log(Exception e) {
	if(DEBUG == 0) return;		
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

    // in Java 6, Sockets don't yet implement Closeable
    public static boolean closeSocket(Socket resourceHandle) {
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
