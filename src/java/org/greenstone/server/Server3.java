package org.greenstone.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import org.greenstone.server.BaseServer;
import org.greenstone.server.BaseProperty;
import org.greenstone.util.GlobalProperties;
import org.greenstone.util.RunAnt;

public class Server3 extends BaseServer
{
        String opt_ant_properties = null;

	public Server3(String gsdl3_src_home, String lang)
	{
		super(gsdl3_src_home, lang, gsdl3_src_home + File.separatorChar + "build.properties", "web"+File.separator+"logs");

		Property = new Server3Property();

		String frame_title = dictionary.get("ServerControl.Frame_Title");
		server_control_ = new Server3Control(this, frame_title);

		/** Ant command tagets for managing Web server */
		START_CMD = "start";
		RESTART_CMD = "restart";
		CONFIGURE_CMD = "configure";
		STOP_CMD = "force-stop-tomcat";

		String is_read_only = System.getProperty("gsdl3home.isreadonly","false");
		if (is_read_only.equals("true")) {
		    String default_gsdl3_home = gsdl3_src_home + File.separatorChar + "web";
		    String gsdl3_writablehome = System.getProperty("gsdl3.writablehome",default_gsdl3_home);
		    opt_ant_properties = "-Dgsdl3home.isreadonly=true -Dgsdl3.writablehome="+gsdl3_writablehome;
		}
		
		autoStart();
	}

	protected int runTarget(String cmd)
	{
		RunAnt runAnt = new RunAnt(opt_ant_properties);
		runAnt.setTargetCmd(cmd);
		runAnt.run();
		return runAnt.getTargetState();
	}

	public String getBrowserURL()
	{
		return GlobalProperties.getFullGSDL3WebAddress() + config_properties.getProperty(BaseServer.Property.DEFAULT_SERVLET);
	}

	public String fallbackGSDL3Home() 
	{ 
		return gsdl_home + File.separator + "web"; //System.getenv("GSDL3SRCHOME") + File.separator + "web"; 
	} 

	public void reload()
	{
	        String gsdl3_writablehome = System.getProperty("gsdl3.writablehome",fallbackGSDL3Home());

		GlobalProperties.loadGlobalProperties(gsdl3_writablehome); // properties file may have changed, so reload it
	}

	public static void main(String[] args)
	{
		if ((args.length < 1) || (args.length > 2))
		{
			System.err.println("Usage: java org.greenstone.server.Server3 <gsdl3-src-home> [lang]");
			System.exit(1);
		}

		String gsdl3_src_home = args[0];
		File gsdl3_src_dir = new File(gsdl3_src_home);
		if (!gsdl3_src_dir.isDirectory())
		{
			System.err.println("src directory does not exist!");
			System.exit(1);
		}

		String lang = (args.length == 2) ? args[1] : "en";
		new Server3(gsdl3_src_home, lang);
	}

    // Prepare the log4j.properties for GS3
    protected void initLogger() {

	String gsdl3_home = GlobalProperties.getGSDL3Home(); // may not be initialised at this stage	
	if(gsdl3_home == null) { // use gsdl_home/web
	    gsdl3_home = fallbackGSDL3Home();
	}

	String gsdl3_writablehome = System.getProperty("gsdl3.writablehome",gsdl3_home);

	String propsFolder = gsdl3_writablehome + File.separator + "WEB-INF" + File.separator + "classes" + File.separator;
	File propsFile = new File(propsFolder+"log4j.properties");

	// create it from the template file GS3/resources/web/log4j.properties
	// Always do this => helps make Greenstone3 portable


	    try {
		// need to set gsdl3.home property's value to be gsdl_home/web,
		// so that the location  of the log files gets resolved correctly
		
		// load the template log4j.properties.in file into logProps
		FileInputStream infile = new FileInputStream(new File(gsdl_home+File.separator+"resources"+File.separator+"web"+File.separator+"log4j.properties"));
		if(infile != null) {
		    Properties logProps = new Properties();
		    logProps.load(infile);
		    infile.close();

		    // set gsdl3.home to web home
		    logProps.setProperty("gsdl3.home", gsdl3_home);
		    logProps.setProperty("gsdl3.writablehome", gsdl3_writablehome);
		    
		    // write the customised properties out to a custom log4j.properties file
		    FileOutputStream outfile = new FileOutputStream(propsFile);
		    if(outfile != null) {
			logProps.store(outfile, "Customised log4j.properties file");
			outfile.close();
		    } else {
			System.err.println("Could not store properties file " + propsFile + " for Server3.");
		    }
		}
	    } catch(Exception e) {
		System.err.println("Exception occurred when custom-configuring the logger for Server3.\n" + e);
		e.printStackTrace();
	    }

	// now configure the logger with the custom log4j.properties file
	if(propsFile.exists()) {
	    PropertyConfigurator.configure(propsFile.getAbsolutePath());
	} else {
	    System.err.println("Could not create properties file " + propsFile + " for Server3.");
	}
    }
}
