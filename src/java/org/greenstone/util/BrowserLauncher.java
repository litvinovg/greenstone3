package org.greenstone.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.greenstone.server.BaseProperty;
import org.greenstone.util.Misc;
import org.greenstone.util.SafeProcess;
import org.apache.log4j.*;

public class BrowserLauncher 
    extends Thread {
    private String url = "about:blank" ;
    static Logger logger = Logger.getLogger(org.greenstone.util.BrowserLauncher.class.getName());
    private String[] default_browsers = new String[]{"firefox","mozilla"};
    private String command = "";
    private int state = -1; //0: launch success, 1: launch failed 
    public final  static int LAUNCHSUCCESS = 0;
    public final static int LAUNCHFAILED = 1;
    private String browserPath = ""; 

    public BrowserLauncher(String browserPath, String url) {
	this.url = url;
        this.browserPath = browserPath;
        //use the default browser
	if (this.browserPath.equals("")){
	    setBrowserCommand();
	}
	else{
	    this.command = this.browserPath + " " + this.url;
	}
    }

    public BrowserLauncher(){

    }

    // we should try and use settings from the settings panel??
    protected void setBrowserCommand() {
	if(Misc.isWindows()) {
	    // we use cmd and start
	    if (Misc.isWindows9x()) {
		this.command = BaseProperty.WIN_9X_OPEN_COMMAND;//"command.com /c start \""+url+"\"";
	    } else {
		this.command = BaseProperty.WIN_OPEN_COMMAND;//"cmd.exe /c start \"\" \""+url+"\"";
	    }
	} else if (Misc.isMac()) {
	    this.command = BaseProperty.MAC_OPEN_COMMAND; // "open %1"
	} else {
	     // we try to look for a browser
 	    for (int i=0; i<default_browsers.length; i++) {
 		if (SafeProcess.isAvailable(default_browsers[i])) {
 		    this.command = default_browsers[i] + " %1";
 		    break;
 		}
 	    }
	}

	this.command = this.command.replaceAll("%1",this.url);
    }

    // Replaced by SafeProcess.isAvailable(program)
    /*
    protected boolean isAvailable(String program) {
	try {
             Runtime run = Runtime.getRuntime();
             Process process = run.exec("which "+ program);
             BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	     String line = null;
	     while ((line = br.readLine()) != null){
		 if (line.indexOf("no "+program) !=-1){
		     return false;
		 }
	     }    	
	     
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }*/

    public  int getBrowserState(){
	return state;
    }
    
    public void run() {
	// Call an external process using the args.
	if(command.equals("")) {
	    state = LAUNCHFAILED;
            logger.error("launching command is empty: no browser found.");
	    return;
	}
	
        try {
	    int exitCode = 0;
	    String prog_name = this.command.substring(0,this.command.indexOf(" "));
	    String lower_name = prog_name.toLowerCase();
	    if (lower_name.indexOf("mozilla") != -1 || lower_name.indexOf("firefox") != -1) { 
		logger.info("found mozilla or firefox, trying to remotely launch it");
                // mozilla and netscape, try using a remote command to get things in the same window
		String new_command = prog_name +" -raise -remote openURL("+url+",new-tab)";
		logger.info(new_command);
		Runtime rt = Runtime.getRuntime();
		//Process process = rt.exec(new_command);
               	state = LAUNCHSUCCESS;
		//exitCode = process.waitFor();
		SafeProcess process = new SafeProcess(new_command);
		exitCode = process.runProcess();
		process = null;
		logger.info("ExitCode:" + exitCode);              
		if (exitCode != 0) { // if Netscape or mozilla was not open
		    logger.info("couldn't do remote, trying original command");
                    logger.info(this.command);
		    //process = rt.exec(this.command); // try the original command
                    state = LAUNCHSUCCESS;
		    //for some reason the following part is not executed sometimes.
                    //exitCode = process.waitFor();
		    process = new SafeProcess(this.command);
		    exitCode = process.runProcess();
		    process = null;
		}
	    } else {
		logger.info(this.command);
                Runtime rt = Runtime.getRuntime();
		//Process process = rt.exec(this.command);
                state = LAUNCHSUCCESS;
              	//for some reason the following part is not executed sometimes.
              	//exitCode = process.waitFor();
		SafeProcess process = new SafeProcess(this.command);
		exitCode = process.runProcess();
	    }

	    logger.info("ExitCode:" + exitCode);
	    if (exitCode != 0) { // if trying to launch the browser above failed
	       state = LAUNCHFAILED;
	       logger.error("Failed to launch web browser when running command:");
	       logger.error("\t" + this.command);		
	    }
	}
	catch (Exception e) {
	    logger.error(e);
            state = LAUNCHFAILED;
	}
	
    }
}
