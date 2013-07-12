package org.greenstone.util;

import org.greenstone.util.RunTarget;

public class RunAnt extends RunTarget 
{
    String opt_cmd_args = "";

    public RunAnt(String opt_args)
    {
	super();

	if (opt_args!=null) {
	    opt_cmd_args = opt_args + " ";
	}

	targetSuccess  = "BUILD SUCCESSFUL";
	targetFailed   = "BUILD FAILED";
	targetFinished = "Total time";
    }

    public RunAnt()
    {
	this(null);
    }

    
    public void setTargetCmd(String target) 
    {
	String osName = System.getProperty("os.name");
      	if (osName.startsWith("Windows")) {
	    targetCmd = "ant.bat " + opt_cmd_args + target;
	} else {
	    targetCmd = "ant " + opt_cmd_args + target;
	}
    }
}
