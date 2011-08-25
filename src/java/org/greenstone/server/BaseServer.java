
package org.greenstone.server;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.swing.JOptionPane;

import org.apache.log4j.*;

import org.greenstone.util.RunTarget;
import org.greenstone.util.BrowserLauncher;

import org.greenstone.gsdl3.util.Dictionary;

public abstract class BaseServer 
{
    static protected String START_CMD;
    static protected String RESTART_CMD;
    static protected String CONFIGURE_CMD;
    static protected String STOP_CMD;

    static protected final int SERVER_STARTED = 0;
    static protected final int SERVER_START_FAILED = 1;
    static protected final int BROWSER_LAUNCHED = 2;
    static protected final int BROWSER_LAUNCH_FAILED = 3;
    static protected final int START_SERVER = 4;
 
    static protected Properties config_properties;
    static protected Logger logger_;

    static public File config_properties_file;
    static public Dictionary dictionary;
    static public BaseProperty Property;

    protected int server_state_ = -1;    
    protected boolean configure_required_ = true;
    protected String gsdl_home;
    protected String logs_folder;
    protected boolean start_browser;

    protected BaseServerControl server_control_;    
    
    protected BaseServer(String gsdl_home, String lang, String config_properties_path, String logs)
    {
	this.gsdl_home = gsdl_home;
	// expand the relative location of the logs folder
	this.logs_folder = this.gsdl_home+File.separator+logs;

	// make sure we write to the correct logs
	initLogger();
	logger_ = Logger.getLogger(BaseServer.class.getName());

	config_properties_file = new File(config_properties_path);
	
	if (!config_properties_file.exists()) {
	    logger_.fatal("Can't find configuration file "+config_properties_path);
	    System.exit(1);
	}
	
	config_properties = new Properties();
	reloadConfigProperties(true); // first time starting the server, work out port_number

	dictionary = new Dictionary("server", lang, this.getClass().getClassLoader());	
    }

    public void autoStart()
    {
	String auto_start = config_properties.getProperty(BaseServer.Property.AUTOSTART, "true");
	if (auto_start.equals("true") || auto_start.equals("1")) {
	    String start_browser = config_properties.getProperty(BaseServer.Property.START_BROWSER, "true");

	    if (start_browser.equals("true") || start_browser.equals("1")) {
		restart();
	    }
	    else{
		start();
	    }
	    
	    server_control_.setState(java.awt.Frame.ICONIFIED); // minimise the server interface window
	} else {
	    if (configure_required_){
		server_control_.displayMessage(dictionary.get("ServerControl.Configuring"));
		int state = runTarget(CONFIGURE_CMD);
		
		if (state != RunTarget.SUCCESS){
		    recordError(CONFIGURE_CMD);
		}
	    }
	    reload(); // browser URL or other important properties may not yet be initialised
	    configure_required_ = false;

	    server_state_ = START_SERVER;
	    server_control_.updateControl();
	}
    }

    // package access methods
    BaseServerControl getServerControl() { 
	return server_control_; 
    }

    protected int getServerState()
    {
	return server_state_;
    }

    // override to write to the correct logs
    protected void initLogger() {}

    protected abstract int runTarget(String cmd);
    public abstract String getBrowserURL();
    public abstract void reload(); // reload properties, since they may have changed
    protected void preStop() {}
    protected void postStart() {}
    
    public void reconfigRequired()
    {
	configure_required_ = true;
    }

    public void start() 
    {
        int state = -1;
        server_state_ = -1;
	server_control_.updateControl();

	server_control_.displayMessage(dictionary.get("ServerControl.Starting"));
	stop(true); // silent, no messages displayed
	
  	// reconfigure if necessary
        if (configure_required_){
	    server_control_.displayMessage(dictionary.get("ServerControl.Configuring"));
	    state = runTarget(CONFIGURE_CMD);
	    
	   if (state != RunTarget.SUCCESS){
	       recordError(CONFIGURE_CMD);
	   }
	   reload(); // work out the browserURL again
	   configure_required_ = false;
	}
	else{
	    recordSuccess(CONFIGURE_CMD);
	}
	
	try{
	    Thread.sleep(5000);
	} catch(Exception e) {
	    logger_.error("Exception trying to sleep: " + e);
	}
        state = runTarget(START_CMD);
	
	if (state != RunTarget.SUCCESS){
	    recordError(START_CMD);
            server_state_ = SERVER_START_FAILED;            
	}	            
	else{
	    recordSuccess(START_CMD); 
            server_state_ = SERVER_STARTED; 
	    postStart();
       	} 

       	server_control_.updateControl();
    }
    
    protected void recordError(String message){
        message = dictionary.get("ServerControl.Error",new String[]{message,logs_folder});
	server_control_.displayMessage(message);
	logger_.error(dictionary.get("ServerControl.Failed",new String[]{message}));
    }


    protected void recordError(String message, Exception e){
        message = dictionary.get("ServerControl.Error",new String[]{message,logs_folder});
	server_control_.displayMessage(message);
	logger_.error(dictionary.get("ServerControl.Failed",new String[]{message}),e);
    }

    protected void recordSuccess(String message){
        message = dictionary.get("ServerControl.Success",new String[]{message});
	server_control_.displayMessage(message);
	logger_.info(message);
    }

    public void launchBrowser() {
	server_state_ = -1;
        server_control_.updateControl();   
	String url = getBrowserURL();
        String message = dictionary.get("ServerControl.LaunchingBrowser");
    	server_control_.displayMessage(message); 
	//recordError("**** browserURL: " + url);
	BrowserLauncher launcher = new BrowserLauncher(config_properties.getProperty(BaseServer.Property.BROWSER_PATH, ""), url);
        logger_.info(message);

	launcher.start();
     
        //wait for a while
        while(launcher.getBrowserState() == -1){
            try{
		Thread.sleep(3000);
	    }
	    catch(Exception e){
		logger_.error(e);
	    }
	}
	
	if (launcher.getBrowserState() != BrowserLauncher.LAUNCHSUCCESS ){
	    recordError(dictionary.get("ServerControl.LaunchBrowser")); 
	    server_state_ = BROWSER_LAUNCH_FAILED;
	}
	else{
	    recordSuccess(dictionary.get("ServerControl.LaunchBrowser")); 
	    server_state_ = BROWSER_LAUNCHED;
	}    
        
	server_control_.updateControl();        
    }
    
    public void restart(){
	start();       
        if (server_state_ == SERVER_STARTED){
	    launchBrowser();
	}  
    }

    // Preserving the current behaviour of stop() which is to
    // display the message on stopping
    public void stop() {
	stop(false);
    }

    public void stop(boolean silent) {
	preStop();
	if(!silent) {
	    server_control_.displayMessage(dictionary.get("ServerControl.Stopping"));
	}
	int state = runTarget(STOP_CMD);
	
        if (state != RunTarget.SUCCESS){
	    recordError(STOP_CMD);
	}
	else{
	    recordSuccess(STOP_CMD);
	}

    }
    
	// returns true on success
    public boolean reloadConfigProperties(boolean port_has_changed) {
	try {
	    FileInputStream in = new FileInputStream(config_properties_file);

	    if (in != null) {
		logger_.info("loading configuration properties: " + config_properties_file);
		config_properties.load(in);
		in.close();
	    } else {
		logger_.error("Couldn't load configuration properties from " + config_properties_file + "!");
	    }
	} catch (Exception e) {
	    logger_.error("Exception trying to reload configuration properties " +config_properties_file + ": " +e);
	}
	
	return true;
    }

}
