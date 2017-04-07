package org.greenstone.gsdl3.build;

// greenstome classes
import org.greenstone.gsdl3.util.*;
import org.greenstone.util.Misc;
import org.greenstone.util.GlobalProperties;
import org.greenstone.util.SafeProcess;

// xml classes
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//general java classes
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.*;

/**
 * CollectionConstructor class for greenstone 2 compatible building it uses the
 * perl scripts to do the building stuff
 */
public class GS2PerlConstructor extends CollectionConstructor implements SafeProcess.ExceptionHandler
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.build.GS2PerlConstructor.class.getName());

	public static final int NEW = 0;
	public static final int IMPORT = 1;
	public static final int BUILD = 2;
	public static final int ACTIVATE = 3;
	public static final int MODIFY_METADATA_SERVER = 4;

	/**
	 * gsdlhome for greenstone 2 - we use the perl modules and building scripts
	 * from there
	 */
	protected String gsdl2home = null;
	/** gsdlhome for gsdl3 - shouldn't need this eventually ?? */
	protected String gsdl3home = null;
	/** gsdlos for greenstone 2 */
	protected String gsdlos = null;
	/** the path environment variable */
	protected String path = null;


	public GS2PerlConstructor(String name)
	{
		super(name);
	}

	/** retrieves the necessary environment variables */
	public boolean configure()
	{
		// try to get the environment variables
		this.gsdl3home = GlobalProperties.getGSDL3Home();
		this.gsdl2home = this.gsdl3home + File.separator + ".." + File.separator + "gs2build";
		this.gsdlos = Misc.getGsdlOS();
            
		this.path = System.getenv("PATH");

		if (this.gsdl2home == null)
		{
			System.err.println("You must have gs2build installed, and GSDLHOME set for GS2Perl building to work!!");
			return false;
		}
		if (this.gsdl3home == null || this.gsdlos == null)
		{
			System.err.println("You must have GSDL3HOME and GSDLOS set for GS2Perl building to work!!");
			return false;
		}
		if (this.path == null)
		{
			System.err.println("You must have the PATH set for GS2Perl building to work!!");
			return false;
		}
		return true;
	}

	public void run()
	{
		String msg;
		ConstructionEvent evt;
		if (this.process_type == -1)
		{
			msg = "Error: you must set the action type";
			evt = new ConstructionEvent(this, GSStatus.ERROR, msg);
			sendMessage(evt);
			return;
		}
		if (this.site_home == null)
		{
			msg = "Error: you must set site_home";
			evt = new ConstructionEvent(this, GSStatus.ERROR, msg);
			sendMessage(evt);
			return;
		}
		if (this.process_type != NEW && this.collection_name == null)
		{
			msg = "Error: you must set collection_name";
			evt = new ConstructionEvent(this, GSStatus.ERROR, msg);
			sendMessage(evt);
			return;
		}

		switch (this.process_type)
		{
		case NEW:
			newCollection();
			break;
		case IMPORT:
			importCollection();
			break;
		case BUILD:
			buildCollection();
			break;
		case ACTIVATE:
			activateCollection();
			break;
		case MODIFY_METADATA_SERVER:
			modifyMetadataForCollection();
			break;
		default:
			msg = "wrong type of action specified!";
			evt = new ConstructionEvent(this, GSStatus.ERROR, msg);
			sendMessage(evt);
			break;
		}
	}

	protected void newCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: new collection."));
		Vector<String> command = new Vector<String>();
		command.add("gs2_mkcol.pl");
		command.add("-site");
		command.add(this.site_home);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = command.toArray(command_str);
		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		} // else an error message has already been sent, do nothing

	}

	protected void importCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: import collection."));
		Vector<String> command = new Vector<String>();

		String perlPath = GlobalProperties.getProperty("perl.path", "perl");
		if (perlPath.charAt(perlPath.length() - 1) != File.separatorChar)
		{
			perlPath = perlPath + File.separator;
		}

		command.add(perlPath + "perl");
		command.add("-S");
		command.add(GlobalProperties.getGS2Build() + File.separator + "bin" + File.separator + "script" + File.separator + "import.pl");
		if (this.manifest_file != null)
		{
			command.add("-keepold");
			command.add("-manifest");
			command.add(this.manifest_file);
		}
		command.add("-site");
		command.add(this.site_name);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = command.toArray(command_str);

		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		} // else an error message has already been sent, do nothing
	}

	protected void buildCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: build collection."));
		Vector<String> command = new Vector<String>();

		String perlPath = GlobalProperties.getProperty("perl.path", "perl");
		if (perlPath.charAt(perlPath.length() - 1) != File.separatorChar)
		{
			perlPath = perlPath + File.separator;
		}

		command.add(perlPath + "perl");
		command.add("-S");
		command.add(GlobalProperties.getGS2Build() + File.separator + "bin" + File.separator + "script" + File.separator + "incremental-buildcol.pl");
		command.add("-incremental");
		command.add("-builddir");
		command.add(GSFile.collectDir(this.site_home) + File.separator + this.collection_name + File.separator +"index");
		command.add("-site");
		command.add(this.site_name);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
//		command.add("-removeold"); // saves some seconds processing time when this flag's added in explicitly
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);

		String[] command_str = {};
		command_str = command.toArray(command_str);

		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		}// else an error message has already been sent, do nothing
	}

	protected void activateCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: activate collection."));

		// first check that we have a building directory
		// (don't want to bother running activate.pl otherwise)
		File build_dir = new File(GSFile.collectionIndexDir(this.site_home, this.collection_name));
		if (!build_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.ERROR, "build dir doesn't exist!"));
			return;
		}

		/*

		// move building to index
		File index_dir = new File(GSFile.collectionIndexDir(this.site_home, this.collection_name));
		if (index_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.INFO, "deleting index directory"));
			GSFile.deleteFile(index_dir);
			if (index_dir.exists())
			{
				sendMessage(new ConstructionEvent(this, GSStatus.ERROR, "index directory still exists!"));
				return;
			}
		}

		GSFile.moveDirectory(build_dir, index_dir);
		if (!index_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.ERROR, "index dir wasn't created!"));
		}

		// success!!  - need to send the final completed message
		sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		*/

		// Running activate.pl instead of making java move building to index as above
		// circumvents the issue of the jdbm .lg log file (managed by TransactionManager) 
		// in index dir not getting deleted at times. The perl code is able to delete this
		// sucessfully consistently during testing, whereas java at times is unable to delete it.
		Vector<String> command = new Vector<String>();

		String perlPath = GlobalProperties.getProperty("perl.path", "perl");
		if (perlPath.charAt(perlPath.length() - 1) != File.separatorChar)
		{
			perlPath = perlPath + File.separator;
		}

		command.add(perlPath + "perl");
		command.add("-S");
		command.add(GlobalProperties.getGS2Build() + File.separator + "bin" + File.separator + "script" + File.separator + "activate.pl");
		command.add("-incremental");
		command.add("-builddir");
		command.add(GSFile.collectDir(this.site_home) + File.separator + this.collection_name + File.separator +"index");
		command.add("-site");
		command.add(this.site_name);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
//		command.add("-removeold"); // saves some seconds processing time when this flag's added in explicitly. Shouldn't be added for incremental building
		command.add("-skipactivation"); // gsdl3/util/GS2Construct does the activation and reactivation
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);

		String[] command_str = {};
		command_str = command.toArray(command_str);

		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		}// else an error message has already been sent, do nothing	    

	}


    protected void modifyMetadataForCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection metadata: modifyMetadata (set or remove meta) for collection."));

		Vector<String> command = new Vector<String>();

		String perlPath = GlobalProperties.getProperty("perl.path", "perl");
		if (perlPath.charAt(perlPath.length() - 1) != File.separatorChar)
		{
			perlPath = perlPath + File.separator;
		}

		String cgi_directory = GlobalProperties.getGSDL3Home() + File.separator + "WEB-INF" + File.separator + "cgi";
		command.add(perlPath + "perl");
		command.add("-S");
		//command.add(GlobalProperties.getGSDL3Home() + File.separator + "WEB-INF" + File.separator + "cgi" + File.separator + "metadata-server.pl");
		command.add(cgi_directory + File.separator + "metadata-server.pl");
		
		// Need to set QUERY_STRING and REQUEST_METHOD=GET in environment
		// http://www.cgi101.com/class/ch3/text.html
		String[] envvars = {
		    "QUERY_STRING=" + this.query_string,
		    "REQUEST_METHOD=GET"
		};

		String[] command_str = {};
		command_str = command.toArray(command_str);

		// http://www.cgi101.com/class/ch3/text.html
		// setenv QUERY_STRING and REQUEST_METHOD = GET.
		if (runPerlCommand(command_str, envvars, new File(cgi_directory)))
				   //new File(GlobalProperties.getGSDL3Home() + File.separator + "WEB-INF" + File.separator + "cgi")))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		}// else an error message has already been sent, do nothing	    

	}

	/** extracts all the args from the xml and returns them in a Vector */
	protected Vector<String> extractParameters(Element param_list)
	{

		Vector<String> args = new Vector<String>();
		if (param_list == null)
		{
			return args; // return an empty vector
		}
		NodeList params = param_list.getElementsByTagName(GSXML.PARAM_ELEM);

		for (int i = 0; i < params.getLength(); i++)
		{
			Element p = (Element) params.item(i);
			String name = p.getAttribute(GSXML.NAME_ATT);
			String value = p.getAttribute(GSXML.VALUE_ATT);
			if (!name.equals(""))
			{
				args.add("-" + name);
				if (!value.equals(""))
				{
					args.add(value);
				}
			}
		}

		return args;
	}

    protected SafeProcess createPerlProcess(String[] command, String[] envvars, File dir) {
	int sepIndex = this.gsdl3home.lastIndexOf(File.separator);
	String srcHome = this.gsdl3home.substring(0, sepIndex);
	
	ArrayList<String> args = new ArrayList<String>();
	args.add("GSDLHOME=" + this.gsdl2home);
	args.add("GSDL3HOME=" + this.gsdl3home);
	args.add("GSDL3SRCHOME=" + srcHome);
	args.add("GSDLOS=" + this.gsdlos);
	args.add("GSDL-RUN-SETUP=true");
	args.add("PERL_PERTURB_KEYS=0");
	
	if(envvars != null) {
	    for(int i = 0; i < envvars.length; i++) {
		args.add(envvars[i]);
	    }
	}
	
	for (String a : System.getenv().keySet()) {
	    args.add(a + "=" + System.getenv(a));
	}

	SafeProcess perlProcess 
	    = new SafeProcess(command, args.toArray(new String[args.size()]), dir); //  dir can be null
	
	return perlProcess;
    }

    // If you want to run a Perl command without doing GS2PerlConstructor's custom logging in the build log
    // The use the runSimplePerlCommand() versions, which use the default behaviour of running a SafeProcess
    protected boolean runSimplePerlCommand(String[] command) {	
	return runSimplePerlCommand(command, null, null);
    }
    
    protected boolean runSimplePerlCommand(String[] command, String[] envvars, File dir) {
	boolean success = false;	
	
	String command_str = "";
	for (int i = 0; i < command.length; i++) {
	    command_str = command_str + command[i] + " ";
	}
	
	sendMessage(new ConstructionEvent(this, GSStatus.INFO, "command = " + command_str));
	
	///logger.info("### Running simple command = " + command_str);

	// This is where we create and run our perl process safely
	SafeProcess perlProcess = createPerlProcess(command, envvars, dir); //  dir can be null
	
	perlProcess.setExceptionHandler(this);

	sendProcessBegun(new ConstructionEvent(this, GSStatus.ACCEPTED, "starting"));

	int exitVal = perlProcess.runProcess(); // uses default processing of the perl process' iostreams provided by SafeProcess

	if (exitVal == 0) {
	    success = true;
	    sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, "Success"));
	} else {
	    sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Failure"));	    
	    success = false; // explicit
	}
	
	return success;
    }


    /** returns true if completed correctly, false otherwise 
     * Building operations call runPerlCommand which sends build output to collect/log/build_log.#*.txt
     */
    protected boolean runPerlCommand(String[] command) {
	return runPerlCommand(command, null, null);
    }

    	
    protected boolean runPerlCommand(String[] command, String[] envvars, File dir)
    {
	boolean success = true;
	
	String command_str = "";
	for (int i = 0; i < command.length; i++) {
	    command_str = command_str + command[i] + " ";
	}
	
	sendMessage(new ConstructionEvent(this, GSStatus.INFO, "command = " + command_str));
	
	///logger.info("### Running logged command = " + command_str);

	// This is where we create and run our perl process safely
	SafeProcess perlProcess = createPerlProcess(command, envvars, dir); //  dir can be null
	
	sendProcessBegun(new ConstructionEvent(this, GSStatus.ACCEPTED, "starting"));
	
	File logDir = new File(GSFile.collectDir(this.site_home) + File.separator + this.collection_name + File.separator + "log");
	if (!logDir.exists()) {
	    logDir.mkdir();
	}
	
	// Only from Java 7+: Try-with-Resources block will safely close the BufferedWriter
	// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
	BufferedWriter bw = null;
	try {

	    bw = new BufferedWriter(new FileWriter(new File(logDir, "build_log." + (System.currentTimeMillis()) + ".txt")));
	
	    bw.write("Document Editor Build \n");		
	    bw.write("Command = " + command_str + "\n");
	    
	    // handle each incoming line from stdout and stderr streams, and any exceptions that occur then
	    SafeProcess.CustomProcessHandler processOutHandler
		= new SynchronizedProcessHandler(bw, SynchronizedProcessHandler.STDOUT);
	    SafeProcess.CustomProcessHandler processErrHandler
		= new SynchronizedProcessHandler(bw, SynchronizedProcessHandler.STDERR);
	    
	    // GS2PerlConstructor will do further handling of exceptions that may occur during the perl
	    // process (including if writing something to the process' inputstream, not that we're doing that for this perlProcess)
	    perlProcess.setExceptionHandler(this); 
	    
	    // finally, execute the process
	    
	    // Captures the std err of a program and pipes it into
	    // std in of java, as before.

	    perlProcess.runProcess(null, processOutHandler, processErrHandler); // use default procIn handling
	    
	// The original runPerlCommand() code had an ineffective check for whether the cmd had been cancelled
	// midway through executing the perl, as condition of a while loop reading from stderr and stdout.
	// We don't include the cancel check here, as superclass CollectionConstructor.stopAction(), which set
	// this.cancel to true, never got called anywhere.
	// But I think a proper cancel of our perl process launched by this GS2PerlConstructor Thread object
	// and of the worker threads it launches, could be implemented with interrupts. See:
	// http://stackoverflow.com/questions/6859681/better-way-to-signal-other-thread-to-stop
	// https://docs.oracle.com/javase/tutorial/essential/concurrency/interrupt.html
	// https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#interrupted()
	// https://praveer09.github.io/technology/2015/12/06/understanding-thread-interruption-in-java/
	// The code that calls GS2PerlConstructor.stopAction() should also call GSPerlConstructor.interrupt()
	// Then in SafeProcess.runProcess(), I think the waitFor() will throw an InterruptedException()
	// This can be caught and interrupt() called on SafeProcess' workerthreads, 
	// Any workerthreads' run() methods that block (IO, loops) can test this.isInterrupted() 
	// and can break out of any loops and release resources in finally.
	// Back in SafeProcess.runProcess, the InterruptedException catch block will be followed by finally
	// that will clear up any further resources and destroy the process forcibly if it hadn't been ended.

	} catch(IOException e) {
	    e.printStackTrace();
	    sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Exception occurred " + e.toString()));
	} finally {
	    SafeProcess.closeResource(bw);
	}

	if (!this.cancel) {
	    // Now display final message based on exit value		    
	    
	    if (perlProcess.getExitValue() == 0) { //status = OK;
		
		sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, "Success"));
		success = true;

	    } else { //status = ERROR;
		sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Failure"));
		success = false;
		
	    }
	} else { // cancelled. The code would never come here, including in the old version of runPerlCommand
	    // but leaving this here for an exact port of the old runPerlCommand to the new one which
	    // uses SafeProcess. Also to allow the cancel functionality in future
	    
	    // I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine.
	    sendProcessStatus(new ConstructionEvent(this, GSStatus.HALTED, "killing the process"));
	    success = false;
	}
	// we're done, but we don't send a process complete message here cos there might be stuff to do after this has finished.
	//return true;		
	return success;
    }
	

    // this method is blocking again, on Windows when adding user comments
    protected boolean old_runPerlCommand(String[] command, String[] envvars, File dir)
	{
		boolean success = true;
	
		int sepIndex = this.gsdl3home.lastIndexOf(File.separator);
		String srcHome = this.gsdl3home.substring(0, sepIndex);

		ArrayList<String> args = new ArrayList<String>();
		args.add("GSDLHOME=" + this.gsdl2home);
		args.add("GSDL3HOME=" + this.gsdl3home);
		args.add("GSDL3SRCHOME=" + srcHome);
		args.add("GSDLOS=" + this.gsdlos);
		args.add("GSDL-RUN-SETUP=true");
		args.add("PERL_PERTURB_KEYS=0");

		if(envvars != null) {
		    for(int i = 0; i < envvars.length; i++) {
			args.add(envvars[i]);
		    }
		}

		for (String a : System.getenv().keySet())
		{
			args.add(a + "=" + System.getenv(a));
		}

		String command_str = "";
		for (int i = 0; i < command.length; i++)
		{
			command_str = command_str + command[i] + " ";
		}

		// logger.info("### old runPerlCmd, command = " + command_str);

		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "command = " + command_str));
		Process prcs = null;
		BufferedReader ebr = null;
		BufferedReader stdinbr = null;
		try
		{
			Runtime rt = Runtime.getRuntime();
			sendProcessBegun(new ConstructionEvent(this, GSStatus.ACCEPTED, "starting"));
			prcs = (dir == null) 
			    ? rt.exec(command, args.toArray(new String[args.size()]))
			    : rt.exec(command, args.toArray(new String[args.size()]), dir);

			InputStreamReader eisr = new InputStreamReader(prcs.getErrorStream());
			InputStreamReader stdinisr = new InputStreamReader(prcs.getInputStream());
			ebr = new BufferedReader(eisr);
			stdinbr = new BufferedReader(stdinisr);
			// Captures the std err of a program and pipes it into
			// std in of java

			File logDir = new File(GSFile.collectDir(this.site_home) + File.separator + this.collection_name + File.separator + "log");
			if (!logDir.exists())
			{
				logDir.mkdir();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(GSFile.collectDir(this.site_home) + File.separator + this.collection_name + File.separator + "log" + File.separator + "build_log." + (System.currentTimeMillis()) + ".txt"));
			bw.write("Document Editor Build \n");

			bw.write("Command = " + command_str + "\n");

			String eline = null;
			String stdinline = null;
			while (((eline = ebr.readLine()) != null || (stdinline = stdinbr.readLine()) != null) && !this.cancel)
			{
				if (eline != null)
				{
					//System.err.println("ERROR: " + eline);
					bw.write(eline + "\n");
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, eline));
				}
				if (stdinline != null)
				{
					//System.err.println("OUT: " + stdinline);
					bw.write(stdinline + "\n");
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, stdinline));
				}
			}
			SafeProcess.closeResource(bw); 
			
			if (!this.cancel)
			{
				// Now display final message based on exit value
				prcs.waitFor();

				if (prcs.exitValue() == 0)
				{	
					//status = OK;
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, "Success"));
					
					success = true;
				}
				else
				{
					//status = ERROR;
					sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Failure"));

					//return false;
					success = false;

				}
			}
			else
			{
				// I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine.
				sendProcessStatus(new ConstructionEvent(this, GSStatus.HALTED, "killing the process"));
				//prcs.getOutputStream().close();
				//prcs.destroy();
				////status = ERROR;
				
				//return false;
				success = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Exception occurred " + e.toString()));
		} finally { 
			// http://steveliles.github.io/invoking_processes_from_java.html
			// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
			// http://mark.koli.ch/leaky-pipes-remember-to-close-your-streams-when-using-javas-runtimegetruntimeexec
		
			 if( prcs != null ) {
				SafeProcess.closeResource(prcs.getErrorStream());
				SafeProcess.closeResource(prcs.getOutputStream());
				SafeProcess.closeResource(prcs.getInputStream());
				prcs.destroy();
			}
		
			SafeProcess.closeResource(ebr);
			SafeProcess.closeResource(stdinbr);
		}

		// we're done, but we don't send a process complete message here cos there might be stuff to do after this has finished.
		//return true;		
		return success;
	}


    // From interface SafeProcess.ExceptionHandler
    // Called when an exception happens during the running of our perl process. However,
    // exceptions when reading from our perl process' stderr and stdout streams are handled by
    // SynchronizedProcessHandler.gotException() below, since they happen in separate threads
    // from this one (the ine from which the perl process is run).
    public synchronized void gotException(Exception e) {

	// do what original runPerlCommand() code always did when an exception occurred
	// when running the perl process:
	e.printStackTrace();
	sendProcessStatus(new ConstructionEvent(this,GSStatus.ERROR, 
						"Exception occurred " + e.toString()));
    }
    
    // Each instance of this class is run in its own thread by class SafeProcess.InputGobbler.
    // This class deals with each incoming line from the perl process' stderr or stdout streams. One
    // instance of this class for each stream. However, since multiple instances of this CustomProcessHandler
    // could be (and in fact, are) writing to the same file in their own threads, several objects, not just
    // the bufferedwriter object, needed to be made threadsafe.
    // This class also handles exceptions during the running of the perl process.
    // The runPerlCommand code originally would do a sendProcessStatus on each exception, so we ensure
    // we do that here too, to continue original behaviour. These calls are also synchronized to make their
    // use of the EventListeners threadsafe.
    protected class SynchronizedProcessHandler implements SafeProcess.CustomProcessHandler
    {
	public static final int STDERR = 0;
	public static final int STDOUT = 1;

	private final int source;
	private final BufferedWriter bwHandle; // needs to be final to synchronize on the object
		

	public SynchronizedProcessHandler(BufferedWriter bw, int src) {
	    this.bwHandle = bw; // caller will close bw, since many more than one
	                        // SynchronizedProcessHandlers are using it
	    this.source = src; // STDERR or STDOUT
	}

	public void run(Closeable inputStream) {
	    InputStream is = (InputStream) inputStream;

	    BufferedReader br = null;
	    try {
		br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line=null;
		while ( (line = br.readLine()) != null ) {
		    
		    if(Thread.currentThread().isInterrupted()) { // should we not instead check if SafeProcess thread was interrupted?
			System.err.println("Got interrupted when reading lines from process err/out stream.");
			break; // will go to finally block
		    }
		    
		    ///System.out.println("@@@ GOT LINE: " + line);


		    //if(this.source == STDERR) {
		    ///System.err.println("ERROR: " + line);
		    //} else {
		    ///System.err.println("OUT: " + line);
		    //}	
		    

		    this.gotLine(line); // synchronized
		    
		    /*
		    try {
			synchronized(bwHandle) { // get a lock on the writer handle, then write
			    
			    bwHandle.write(line + "\n");
			} 
		    } catch(IOException ioe) {
			String msg = (source == STDERR) ? "stderr" : "stdout";
			msg = "Exception when writing out a line read from perl process' " + msg + " stream.";
			GS2PerlConstructor.logger.error(msg, ioe);
		    }
		    
		    // this next method is thread safe since only synchronized methods are invoked.
		    // and only immutable (final) vars are used. 
		    // NO, What about the listeners???
		    sendProcessStatus(new ConstructionEvent(GS2PerlConstructor.this, GSStatus.CONTINUING, line));		    
		    */
		}
	    } catch (IOException ioe) { // problem with reading in from process with BufferedReader br

		String msg = (source == STDERR) ? "stderr" : "stdout";
		msg = "Got exception when processing the perl process' " + msg + " stream.";
		GS2PerlConstructor.logger.error(msg, ioe);
		// now do what the original runPerlCommand() code always did:
		ioe.printStackTrace();
		logException(ioe); // synchronized

	    } catch (Exception e) { // problem with BufferedWriter bwHandle on processing each line
		e.printStackTrace();
		logException(e); // synchronized
	    } finally {
		SafeProcess.closeResource(br);
	    }
	}

	// trying to keep synchronized methods as short as possible
	private synchronized void logException(Exception e) {
	    sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Exception occurred " + e.toString()));
	}

	// trying to keep synchronized methods as short as possible
	private synchronized void gotLine(String line) throws Exception {

	    // BufferedWriter writes may not be atomic
	    // http://stackoverflow.com/questions/9512433/is-writer-an-atomic-method
	    // Choosing to put try-catch outside of sync block, since it's okay to give up lock on exception
	    // http://stackoverflow.com/questions/14944551/it-is-better-to-have-a-synchronized-block-inside-a-try-block-or-a-try-block-insi
	    try {					    
		bwHandle.write(line + "\n");	
	    
///		GS2PerlConstructor.logger.info("@@@ WROTE LINE: " + line);

		// this next method is thread safe since only synchronized methods are invoked.
		// and only immutable (final) vars are used.
		sendProcessStatus(new ConstructionEvent(GS2PerlConstructor.this, GSStatus.CONTINUING, line));
	    
	    } catch(IOException ioe) { // can't throw Exceptions, but are forced to handle Exceptions here
		// since our method definition doesn't specify a throws list.
		// "All methods on Logger are multi-thread safe", see
		// http://stackoverflow.com/questions/14211629/java-util-logger-write-synchronization
		
		String msg = (source == STDERR) ? "stderr" : "stdout";
		msg = "IOException when writing out a line read from perl process' " + msg + " stream.";
		msg += "\nGot line: " + line + "\n";
		throw new Exception(msg, ioe);
	    }		    
	}

    } // end inner class SynchronizedProcessHandler

}
