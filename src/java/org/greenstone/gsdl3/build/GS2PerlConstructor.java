package org.greenstone.gsdl3.build;

// greenstome classes
import org.greenstone.gsdl3.util.*;
import org.greenstone.util.Misc;
import org.greenstone.util.GlobalProperties;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.*;

/**
 * CollectionConstructor class for greenstone 2 compatible building it uses the
 * perl scripts to do the building stuff
 */
public class GS2PerlConstructor extends CollectionConstructor
{
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.build.GS2PerlConstructor.class.getName());

	public static final int NEW = 0;
	public static final int IMPORT = 1;
	public static final int BUILD = 2;
	public static final int ACTIVATE = 3;
    public static final int SET_METADATA_SERVER = 4;

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
		case SET_METADATA_SERVER:
			setMetadataForCollection();
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
		command.add(GlobalProperties.getGS2Build() + File.separator + "bin" + File.separator + "script" + File.separator + "buildcol.pl");
		command.add("-site");
		command.add(this.site_name);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.add("-removeold"); // saves some seconds processing time when this flag's added in explicitly
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
		File build_dir = new File(GSFile.collectionBuildDir(this.site_home, this.collection_name));
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
		command.add("-site");
		command.add(this.site_name);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.add("-removeold"); // saves some seconds processing time when this flag's added in explicitly
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


    protected void setMetadataForCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection metadata: setMetadata for collection."));

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

	/** returns true if completed correctly, false otherwise */
    protected boolean runPerlCommand(String[] command) {
	return runPerlCommand(command, null, null);
    }
	
	protected boolean runPerlCommand(String[] command, String[] envvars, File dir)
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
			closeResource(bw); 
			
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
				closeResource(prcs.getErrorStream());
				closeResource(prcs.getOutputStream());
				closeResource(prcs.getInputStream());
				prcs.destroy();
			}
		
			closeResource(ebr);
			closeResource(stdinbr);
		}

		// we're done, but we don't send a process complete message here cos there might be stuff to do after this has finished.
		//return true;		
		return success;
	}
	
	public static void closeResource(Closeable resourceHandle) {
		try {
			if(resourceHandle != null) {
				resourceHandle.close();
				resourceHandle = null;
			}
		} catch(Exception e) {
			System.err.println("Exception closing resource: " + e.getMessage());
			e.printStackTrace();
		}
    }
}
