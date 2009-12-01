
package org.greenstone.server;

import java.io.File;

import org.greenstone.server.BaseServer;
import org.greenstone.server.BaseProperty;
import org.greenstone.gsdl3.util.GlobalProperties;

public class Server3 extends BaseServer
{
    
    public Server3(String gsdl3_src_home, String lang)
    {
	super(gsdl3_src_home,lang, gsdl3_src_home+File.separatorChar+"build.properties", "logs");

	Property = new Server3Property();

	String frame_title = dictionary.get("ServerControl.Frame_Title");
	server_control_ = new Server3Control(this,frame_title);

	/** Ant command tagets for managing Web server */
	START_CMD     = "start";
	RESTART_CMD   = "restart";
	CONFIGURE_CMD = "configure";
	STOP_CMD      = "stop";

	autoStart();
    }
        
    protected int runTarget(String cmd)
    {
	RunAnt runAnt = new RunAnt();
	runAnt.setTargetCmd(cmd);
	runAnt.run();
   	return runAnt.getTargetState();
    }
    
    public String getBrowserURL() {
	return GlobalProperties.getGSDL3WebAddress()+ config_properties.getProperty(BaseServer.Property.DEFAULT_SERVLET);
    }

    public void reload() {
	GlobalProperties.reload(); // properties file may have changed, so reload it
    }

    public static void main (String[] args) 
    {
    	if ((args.length < 1) || (args.length>2)) {
	    System.err.println("Usage: java org.greenstone.server.Server3 <gsdl3-src-home> [lang]");
	    System.exit(1);
	}

	String gsdl3_src_home = args[0];	
	File gsdl3_src_dir = new File(gsdl3_src_home);
	if (!gsdl3_src_dir.isDirectory()) {
	    System.err.println("src directory does not exist!");
	    System.exit(1);
	}

	String lang = (args.length==2) ? args[1] : "en";
	new Server3(gsdl3_src_home,lang);
    }
}
