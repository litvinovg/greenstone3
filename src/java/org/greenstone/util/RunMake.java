package org.greenstone.util;

import org.greenstone.util.RunTarget;

public class RunMake extends RunTarget 
{

    public RunMake()
    {
	super();

	targetSuccess  = "MAKE SUCCESSFUL";
	targetFailed   = "MAKE FAILED";
	targetFinished = "MAKE DONE";
    }


    public void setTargetCmd(String target) 
    {
	String osName = System.getProperty("os.name");
      	if (osName.startsWith("Windows")) {
	    //targetCmd = "fakemake.bat " + target;
	    targetCmd = "gsicontrol.bat " + target;
	} else {
	    targetCmd = "./gsicontrol.sh " + target;
	}
    }
}
