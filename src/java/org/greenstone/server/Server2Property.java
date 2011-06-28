package org.greenstone.server;

import org.greenstone.server.BaseProperty;

public class Server2Property extends BaseProperty 
{
    Server2Property()
    {
	// Initialising customised final variables 
	// Version number, WEB_PORT
	super("2", "portnumber", "autoenter", "start_browser", "keepport");
    }

}
