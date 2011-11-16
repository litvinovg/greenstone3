package org.greenstone.gsdl3.build;

// greenstome classes
import org.greenstone.gsdl3.util.*;

// xml classes
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//general java classes
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Vector;

/**
 * CollectionConstructor class for greenstone 2 compatible building it uses the
 * perl scripts to do the building stuff
 */
public class GS2PerlConstructor extends CollectionConstructor
{

	public static final int NEW = 0;
	public static final int IMPORT = 1;
	public static final int BUILD = 2;
	public static final int ACTIVATE = 3;

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
		this.gsdl2home = System.getProperty("GSDLHOME");
		this.gsdl3home = System.getProperty("GSDL3HOME");
		this.gsdlos = System.getProperty("GSDLOS");
		this.path = System.getProperty("PATH");
		if (this.gsdl2home == null)
		{
			System.err.println("You must have greenstone2 installed, and GSDLHOME set for GS2Perl building to work!!");
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
		Vector command = new Vector();
		command.add("gs2_mkcol.pl");
		command.add("-site");
		command.add(this.site_home);
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = (String[]) command.toArray(command_str);
		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		} // else an error message has already been sent, do nothing

	}

	protected void importCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: import collection."));
		Vector command = new Vector();
		command.add("import.pl");
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = (String[]) command.toArray(command_str);

		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		} // else an error message has already been sent, do nothing
	}

	protected void buildCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: build collection."));
		Vector command = new Vector();
		command.add("buildcol.pl");
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = (String[]) command.toArray(command_str);

		if (runPerlCommand(command_str))
		{
			// success!! - need to send the final completed message
			sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
		}// else an error message has already been sent, do nothing
			//  	    

	}

	protected void activateCollection()
	{
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "Collection construction: activate collection."));

		// first check that we have a building directory
		File build_dir = new File(GSFile.collectionBuildDir(this.site_home, this.collection_name));
		if (!build_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.ERROR, "build dir doesn't exist!"));
			return;
		}

		// move building to index
		File index_dir = new File(GSFile.collectionIndexDir(this.site_home, this.collection_name));
		if (index_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.INFO, "deleting index directory"));
			index_dir.delete();
			if (index_dir.exists())
			{
				sendMessage(new ConstructionEvent(this, GSStatus.INFO, "index directory still exists!"));
			}
		}

		GSFile.moveDirectory(build_dir, index_dir);
		if (!index_dir.exists())
		{
			sendMessage(new ConstructionEvent(this, GSStatus.ERROR, "index dir wasn't created!"));
		}

		// run the convert coll script to convert the gs2 coll to gs3
		Vector command = new Vector();
		command.add("convert_coll_from_gs2.pl");
		command.add("-collectdir");
		command.add(GSFile.collectDir(this.site_home));
		command.addAll(extractParameters(this.process_params));
		command.add(this.collection_name);
		String[] command_str = {};
		command_str = (String[]) command.toArray(command_str);

		if (!runPerlCommand(command_str))
		{
			// an error has occurred, dont carry on
			return;
		}

		// success!!  - need to send the final completed message
		sendProcessComplete(new ConstructionEvent(this, GSStatus.COMPLETED, ""));
	}

	/** extracts all the args from the xml and returns them in a Vector */
	protected Vector extractParameters(Element param_list)
	{

		Vector args = new Vector();
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
	protected boolean runPerlCommand(String[] command)
	{
		String msg;
		ConstructionEvent evt;

		String args[] = { "GSDLHOME=" + this.gsdl2home, "GSDL3HOME=" + this.gsdl3home, "GSDLOS=" + this.gsdlos, "PATH=" + this.path };
		String command_str = "";
		for (int i = 0; i < command.length; i++)
		{
			command_str = command_str + command[i] + " ";
		}
		sendMessage(new ConstructionEvent(this, GSStatus.INFO, "command = " + command_str));
		try
		{
			Runtime rt = Runtime.getRuntime();
			sendProcessBegun(new ConstructionEvent(this, GSStatus.ACCEPTED, "starting"));
			Process prcs = rt.exec(command, args);

			InputStreamReader eisr = new InputStreamReader(prcs.getErrorStream());
			InputStreamReader stdinisr = new InputStreamReader(prcs.getInputStream());
			BufferedReader ebr = new BufferedReader(eisr);
			BufferedReader stdinbr = new BufferedReader(stdinisr);
			// Captures the std err of a program and pipes it into
			// std in of java
			String eline = null;
			String stdinline = null;
			while (((eline = ebr.readLine()) != null || (stdinline = stdinbr.readLine()) != null) && !this.cancel)
			{
				if (eline != null)
				{
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, eline));
				}
				if (stdinline != null)
				{
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, stdinline));
				}
			}
			if (!this.cancel)
			{
				// Now display final message based on exit value
				prcs.waitFor();

				if (prcs.exitValue() == 0)
				{
					//status = OK;
					sendProcessStatus(new ConstructionEvent(this, GSStatus.CONTINUING, "Success"));

				}
				else
				{
					//status = ERROR;
					sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Failure"));

					return false;

				}
			}
			else
			{
				// I need to somehow kill the child process. Unfortunately Thread.stop() and Process.destroy() both fail to do this. But now, thankx to the magic of Michaels 'close the stream suggestion', it works fine.
				sendProcessStatus(new ConstructionEvent(this, GSStatus.HALTED, "killing the process"));
				prcs.getOutputStream().close();
				prcs.destroy();
				//status = ERROR;
				return false;
			}

		}
		catch (Exception e)
		{
			sendProcessStatus(new ConstructionEvent(this, GSStatus.ERROR, "Exception occurred " + e.toString()));
		}

		// we're done, but we dont send a process complete message here cos there ight be stuff to do after this has finished.
		return true;

	}

}
