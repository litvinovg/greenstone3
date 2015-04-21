package org.greenstone.server;

public class BaseProperty {

    public final String GSDL_HOME;
    public final String GSDL_VERSION;
    public final String AUTOSTART;
    public final String KEEPPORT;
    public final String START_BROWSER;
    public final String ALLOW_EXTERNAL_ACCESS;

    public final String DEFAULT_SERVLET = "server.default.servlet";

    public final String WEB_PORT;

    public static final String WIN_9X_OPEN_COMMAND = "command.com /c start %1";
    public static final String WIN_OPEN_COMMAND = "cmd.exe /c start \"\" %1";
    public static final String MAC_OPEN_COMMAND = "open %1";

    public final String BROWSER_PATH = "browser.path";

    public final String SERVER_CONTROL;
    public final String SERVER_SETTINGS;
    
    protected BaseProperty(String version, 
			   String web_port,
			   String autostart, 
			   String startbrowser, 
			   String keepport,
			   String allowExternalAccess) 
    {
	// property names
	WEB_PORT = web_port;
	GSDL_HOME = "gsdl"+version +".home";
	GSDL_VERSION = "gsdl"+version+".version";
	SERVER_CONTROL = "Server"+version+"Control";
	SERVER_SETTINGS = "Server"+version+"Settings";
	AUTOSTART = autostart;
	START_BROWSER = startbrowser;
	KEEPPORT = keepport;
	ALLOW_EXTERNAL_ACCESS = allowExternalAccess;
    }

}
