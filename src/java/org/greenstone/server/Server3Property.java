package org.greenstone.server;

import org.greenstone.server.BaseProperty;

public class Server3Property extends BaseProperty 
{
    Server3Property()
    {
	// Initialising customised final variables 
	// Version number, WEB_PORT, autoenter, startbrowser
	// For GS3, the last two are controlled by the same property
	super("3", "tomcat.port", "server.auto.start", "server.auto.start", 
	      "server.keep.port", "server.external.access");
    }

}
