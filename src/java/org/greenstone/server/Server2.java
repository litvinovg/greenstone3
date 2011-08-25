
package org.greenstone.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
//import java.net.URLConnection;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.log4j.*;

import org.greenstone.util.PortFinder;
import org.greenstone.util.ScriptReadWrite;
import org.greenstone.util.RunMake;
import org.greenstone.util.RunAnt;

import org.greenstone.server.BaseServer;
import org.greenstone.server.BaseProperty;


public class Server2 extends BaseServer 
{
    private static final int WAITING_TIME = 10; // time to wait and check for whether the server is running
    private static final String URL_PENDING="URL_pending";	
	
    protected String libraryURL;
	
    private class QuitListener extends Thread 
    {
	int quitPort = -1;
	ServerSocket serverSocket = null;

	public QuitListener(int quitport) throws Exception {
	    ///Server2.this.recordSuccess("In QuitListener constructor");
	    this.quitPort = quitport;
	    serverSocket = new ServerSocket(quitPort);
	}

	public void run() {
	    Socket connection = null;

	    try {
		// wait for a connection
		connection = serverSocket.accept();
		boolean stop = false;

		// read input
		try {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String line = null;
		    while((line = reader.readLine()) != null) {
			if(line.equals("QUIT")) {
			    stop = true;			    
			    // Server2.this.recordSuccess("In QuitListener - line is QUIT");
			    reader.close();
			    reader = null;
			    serverSocket.close();
			    serverSocket = null;
			    break;
			} else if(line.equals("RECONFIGURE")) {
			    server_control_.displayMessage(dictionary.get("ServerControl.Configuring"));
			    reconfigRequired();
			} else if(line.equals("RESTART")) {

			    // If the GSI is set to NOT autoenter/autostart the server, then write url=URL_PENDING out to the file.
			    // When the user finally presses the Enter Library button and so has started up the server, the correct
			    // url will be written out to the configfile.
			    if(config_properties.getProperty(BaseServer.Property.AUTOSTART, "").equals("0")) {
				if(config_properties.getProperty("url") == null) {
				    config_properties.setProperty("url", URL_PENDING);
				    ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
				    ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
				    scriptReadWrite.replaceOrAddLine(fileLines, "url", URL_PENDING, true);
				    scriptReadWrite.writeOutFile(config_properties_file, fileLines);
				}
			    }

			    autoStart();
			}
		    }
		} catch(Exception e) {
		    Server2.this.recordError("Exception in QuitListener thread.");
		} finally {
		    if(stop) {
			Server2.this.stop();
			System.exit(0);
		    }
		}
	    } catch(IOException ioe) {
		Server2.this.recordError("Server2.QuitListener: Unable to make the connection with the client socket." + ioe);
	    }
	}
    }


    public Server2(String gsdl2_home, String lang, String configfile, int quitPort, String property_prefix)
    {
	super(gsdl2_home, lang, configfile, "etc"+File.separator+"logs-gsi");	
	               // configfile can be either glisite.cfg or llssite.cfg	

	//logger_.error("gsdlhome: " + gsdl2_home + " | lang: " + lang + " | configfile: " 
		//+ configfile + " | mode: " + property_prefix + " | quitport: " + quitPort);
	
	// property_prefix is the mode we're running in (gli or empty) and contains the prefix
	// string to look for in config file for auto_enter and start_browser properties	
	if(!property_prefix.equals("") && !property_prefix.endsWith(".")) { // ensure a '.' is suffixed if non-empty
		property_prefix += ".";
	}
	Property = new Server2Property(property_prefix);
	
	
	String frame_title = dictionary.get("ServerControl.Frame_Title");
	server_control_ = new Server2Control(this,frame_title);

	/* Make command targets for managing Web server */
	START_CMD     = "web-start";
	RESTART_CMD   = "web-restart";
	CONFIGURE_CMD = "configure-web \"" + configfile + "\"";
	STOP_CMD      = "web-stop";

	// now we can monitor for the quit command if applicable
	if(quitPort != -1) { 
	    // First check if given port is within the range of allowed ports
	    if(PortFinder.isAssignablePortNumber(quitPort)) {
		try {
		    new QuitListener(quitPort).start();
		} catch(Exception e) {
		    Server2.this.recordError("Exception constructing the QuitListener thread.");
		}
	    }
	    else {
		recordError("QuitPort provided is not within acceptable range: ("
			    + PortFinder.PORTS_RESERVED + " - " + PortFinder.MAX_PORT + "]" );
		quitPort = -1;
	    }
	}
	
	// For some machines, localhost is not sufficient, 
	// need hostname defined as well (e.g. Ubuntu 10.10)
	InetAddress inetAddress = null;
	try {
	   	inetAddress = InetAddress.getLocalHost();
		String hosts = inetAddress.getHostName();
		ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
		ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
		scriptReadWrite.replaceOrAddLine(fileLines, "hosts", hosts, true);
		scriptReadWrite.writeOutFile(config_properties_file, fileLines);
	} catch(UnknownHostException e) {
	   	// Unable to get hostname, need to try for the default localhost without it
	}		

	// If the GSI is set to NOT autoenter/autostart the server, then write url=URL_PENDING out to the file.
	// When the user finally presses the Enter Library button and so has started up the server, the correct
	// url will be written out to the configfile.
	if(config_properties.getProperty(BaseServer.Property.AUTOSTART, "").equals("0")) {//if(configfile.endsWith("llssite.cfg")) {
	    if(config_properties.getProperty("url") == null) {
		config_properties.setProperty("url", URL_PENDING);
		ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
		ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
		scriptReadWrite.replaceOrAddLine(fileLines, "url", URL_PENDING, true);
		scriptReadWrite.writeOutFile(config_properties_file, fileLines);
	    }
	}
	
	autoStart();
    }

    // Prepare the log4j.properties for GS2
    protected void initLogger() {

	String libjavaFolder = gsdl_home+File.separator+"lib"+File.separator+"java"+File.separator;
	File propsFile = new File(libjavaFolder+"log4j.properties");

	// create it from the template file log4j.properties.in
	if(!propsFile.exists()) {
	    try {
		// need to set gsdl2.home property's value to be gsdl_home,
		// so that the location  of the log files gets resolved correctly
		
		// load the template log4j.properties.in file into logProps
		FileInputStream infile = new FileInputStream(new File(libjavaFolder+"log4j.properties.in"));
		if(infile != null) {
		    Properties logProps = new Properties();
		    logProps.load(infile);
		    infile.close();

		    // set gsdl3.home to gsdl_home
		    logProps.setProperty("gsdl2.home", gsdl_home);
		    
		    // write the customised properties out to a custom log4j.properties file
		    FileOutputStream outfile = new FileOutputStream(propsFile);
		    if(outfile != null) {
			logProps.store(outfile, "Customised log4j.properties file");
			outfile.close();
		    } else {
			System.err.println("Could not store properties file " + propsFile + " for Server2.");
		    }
		}
	    } catch(Exception e) {
		System.err.println("Exception occurred when custom-configuring the logger for Server2.\n" + e);
	    }
	}

	// now configure the logger with the custom log4j.properties file
	if(propsFile.exists()) {
	    PropertyConfigurator.configure(propsFile.getAbsolutePath());
	} else {
	    System.err.println("Could not create properties file " + propsFile + " for Server2.");
	}
    }
    

    protected int runTarget(String cmd)
    {
	RunMake runMake = new RunMake();
	runMake.setTargetCmd(cmd);
	runMake.run();
   	return runMake.getTargetState();
    }

    public String getBrowserURL() {
	return libraryURL;
    }

    // works out the library URL again
    public void reload() {
	// default values, to be replaced with what's in gsdlsite.cfg
	String host = "localhost";
	String port = "80";
	String gwcgi;
	String httpprefix = "/greenstone";
	String suffix = "/cgi-bin/library.cgi";

	// get the prefix from the gsdlsite.cfg and build.properties files (port number and servername)
	try{
	    File gsdlsite_cfg = new File(gsdl_home + File.separator + "cgi-bin" + File.separator + "gsdlsite.cfg");
	    FileInputStream fin = new FileInputStream(gsdlsite_cfg); 
	    Properties gsdlProperties = new Properties();
	    if(fin != null) {
		gsdlProperties.load(fin);
	    
		gwcgi = gsdlProperties.getProperty("gwcgi");
		if(gwcgi != null) {
		    suffix = gwcgi;
		} else {
		    httpprefix = gsdlProperties.getProperty("httpprefix", httpprefix);
		    suffix = httpprefix + suffix;
		}
		fin.close();
	    } else {
		recordError("Could not open gsdlsite_cfg for reading, using default library prefix.");
	    }
	    //reloadConfigProperties();
	    port = config_properties.getProperty("portnumber", port);
	    
	    // The "hosts" property in the config file contains more than one allowed host
	    // Need to work out the particular host chosen from the address_resolution_method
	    // Default is address_resolution_method 2: localhost
	    String addressResolutionMethod = config_properties.getProperty("address_resolution_method");
	    int address_resolution_method = (addressResolutionMethod == null) ? 2 : Integer.parseInt(addressResolutionMethod);
	    InetAddress inetAddress = null;
	    try {
		inetAddress = InetAddress.getLocalHost();
	    } catch(UnknownHostException e) {
		logger_.error(e);
		logger_.info("Defaulting host IP to "+ host); // use the default		
		address_resolution_method = 2;
		inetAddress = null;
	    }
	    switch(address_resolution_method) {
	    case 0:
		host = inetAddress.getHostName();
		break;
	    case 1:
		host = inetAddress.getHostAddress();
		break;
	    case 2:
		host = "localhost";
		break;
	    case 3:
		host = "127.0.0.1";
		break;
	    default:
		host = "localhost";		    
	    }
	} catch(Exception e) {
	    recordError("Exception trying to load properties from gsdlsite_cfg. Using default library prefix.", e);
	    suffix = httpprefix + suffix;
	}

	libraryURL = "http://" + host + ":" + port + suffix;
    }

    public boolean reloadConfigProperties(boolean port_has_changed) {
	super.reloadConfigProperties(port_has_changed);

	// make sure the port is okay, otherwise find another port
	// first choice is port 80, second choice starts at 8282
	String port = config_properties.getProperty("portnumber", "80");
	String keepport = config_properties.getProperty("keepport", "0"); // default is to not try to force the same port if in use by other servers

	int portDefault = 8282;
	try {
	  int portNum = Integer.parseInt(port);
	  boolean verbose = true;
	  if(port_has_changed) { // this is the test that prevents the server from arbitrarily shifting the port. 
							// only check at configured port if it's not the current port (the port we
							// are still running on), because that will always be in use and unavailable.
		if(!PortFinder.isPortAvailable(portNum, verbose)) { // first time, print any Port Unavailable messages
		if(keepport.equals("1")) {
			server_control_.errorMessage(dictionary.get("ServerSettings.SettingsUnchangedPortOccupied", new String[]{port}));
		    String errorMsg = "Unable to run the Greenstone server on port " + port + ". It appears to already be in use.";
		    System.err.println("\n******************");
			logger_.error(errorMsg);
		    System.err.println("If you wish to try another port, go to File > Settings of the Greenstone Server interface and either change the port number or untick the \"Do Not Modify Port\" option there. Then press the \"Enter Library\" button.");
		    System.err.println("******************\n");
			
			return false; // property change is unsuccessful
			
		} else { // can modify port, try to find a new port
		
		    PortFinder portFinder = new PortFinder(portDefault, 101);
		    // Search for a free port silently from now on--don't want more
		    // messages saying that a port could not be found...
		    portNum = portFinder.findPortInRange(!verbose);
		    
		    if (portNum == -1) {
			// If we've still not found a free port, do we try the default port again?
			System.err.println("No free port found. Going to try on " + portDefault + " anyway.");
			port = Integer.toString(portDefault);
		    } else {
			port = Integer.toString(portNum);
		    }
		    config_properties.setProperty("portnumber", port); // store the correct port
		    
		    // write this updated port to the config file, since the configure target uses the file to run
		    ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
		    ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
		    scriptReadWrite.replaceOrAddLine(fileLines, "portnumber", port, false); // write the correct port
		    scriptReadWrite.writeOutFile(config_properties_file, fileLines);
		    
		    configure_required_ = true;
		    System.err.println("Running server on port " + port + ".");
		}
	    }
	  }	
	} catch (Exception e) {
	    recordError("Exception in Server2.reload(): " + e.getMessage());
	    port = Integer.toString(portDefault);
	}
	
	return true;
    }

    
    // About to stop the webserver
    // Custom GS2 action: remove the url property from the config file
    protected void preStop() {
	ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
	ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);

	// Remove the url=... line, start searching from the end
	boolean done = false;
	for (int i = fileLines.size()-1; i >= 0 && !done; i--) {
	    String line = ((String) fileLines.get(i)).trim();
	    if(line.startsWith("url=")) {
		fileLines.remove(i);
		done = true;
	    }
	}
	scriptReadWrite.writeOutFile(config_properties_file, fileLines);
    }

    // Called when the URL has been changed (called after a reload() and starting the server).
    // By the time we get here, reload() would already have been called and have set
    // both the port and worked out libraryURL
    // This method needs to write the URL to the configfile since things should work
    // like GS2's Local Lib Server for Windows
    protected void postStart() {

	URL libURL = null;
	try {
	    libURL = new URL(libraryURL);
	} catch (Exception e) {
	    recordError("Unable to convert library URL string into a valid URL, Server2.java." + e);
	}
	
	// 1. Test that the server is running at the libraryURL before writing it out to glisite.cfg/configfile 
	// A quick test involves opening a connection to get the home page for this collection
	if(libURL != null && !libraryURL.equals(URL_PENDING)) {

	    boolean ready = false; 
	    for(int i = 0; i < WAITING_TIME && !ready; i++) {
		try {
		    libURL.openConnection();
		    //URLConnection connection = new URL(libraryURL).openConnection();
		    //connection.getContent();
		    ready = true;
		    recordSuccess("Try connecting to server on url: '" + libraryURL + "'");
		} catch (IOException bad_url_connection) {
		    // keep looping
		    recordSuccess("NOT YET CONNECTED. Waiting to try again...");
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException ie) {
			ready = true;
			recordError("Unexpected: got an InterruptedException in sleeping thread, Server2.java." + ie);
		    }
		} catch (Exception e) {
		    ready = true;
		    recordError("Got an Exception while waiting for the connection to become live, Server2.java." + e);
		}
	    }
	}

	// 2. Now write the URL to the config file
	String port = config_properties.getProperty("portnumber");

	ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
	ArrayList fileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
	scriptReadWrite.replaceOrAddLine(fileLines, "url", libraryURL, true);
	scriptReadWrite.replaceOrAddLine(fileLines, "portnumber", port, false); // write the correct port
	scriptReadWrite.writeOutFile(config_properties_file, fileLines);
    }


    public static void main (String[] args)
    {
    	if ((args.length < 1) || (args.length > 5)) {
	    System.err.println(
	       "Usage: java org.greenstone.server.Server2 <gsdl2-home-dir> [lang] [--mode=\"gli\"] [--config=configfile] [--quitport=portNum]");
	    System.exit(1);
		}
	
	String gsdl2_home = args[0];
	File gsdl2_dir = new File(gsdl2_home);
	if (!gsdl2_dir.isDirectory()) {
	    System.err.println("gsdl-home-dir directory does not exist!");
	    System.exit(1);
	}
	
	int index = 1; // move onto any subsequent arguments
	
	// defaults for optional arguments
	// if no config file is given, then it defaults to llssite.cfg (embedded in quotes to preserve spaces in the filepath)
	File defaultConfigFile = new File(gsdl2_dir, "llssite.cfg");	
	String configfile = defaultConfigFile.getAbsolutePath();
	int port = -1;
	String mode = "";
	String lang = "en";	
	
	// cycle through arguments, parsing and storing them
	while(args.length > index) {
			
		if(args[index].startsWith("--config=")) {
			configfile = args[index].substring(args[index].indexOf('=')+1); // get value after '=' sign			
			gsdl2_dir = null;
			defaultConfigFile = null;
		}
		
		else if(args[index].startsWith("--quitport=")) {
			String quitport = args[index].substring(args[index].indexOf('=')+1);			
			try {				
				port = Integer.parseInt(quitport);
			} catch(Exception e) { // parse fails
				System.err.println("Port must be numeric. Continuing without it.");
			}		
		}
		
		// mode can be: "gli" if launched by GLI or unspecified. If unspecified, then 
		// the gs2-server was launched independently.
		else if(args[index].startsWith("--mode=")) {
			mode = args[index].substring(args[index].indexOf('=')+1);
		}
		
		else if(args[index].length() == 2) { 
			// there is an optional argument, but without a --FLAGNAME= prefix, so it's a language code			
			lang = args[index];
		}
		
		index++;
	}
	
	new Server2(gsdl2_home, lang, configfile, port, mode);
    }
}
