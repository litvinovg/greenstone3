package org.greenstone.server;

import org.greenstone.server.BaseProperty;

public class Server2Property extends BaseProperty 
{
    Server2Property(String property_prefix)
    {
	// Initialising customised final variables 
	// Version number, WEB_PORT
	super("2", "portnumber", property_prefix+"autoenter", property_prefix+"start_browser", "keepport");
    }

}
