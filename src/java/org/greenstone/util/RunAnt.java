package org.greenstone.util;

import org.greenstone.util.RunTarget;

public class RunAnt extends RunTarget 
{
    public RunAnt()
    {
	super();

	targetSuccess  = "BUILD SUCCESSFUL";
	targetFailed   = "BUILD FAILED";
	targetFinished = "Total time";
    }
    
    public void setTargetCmd(String target) 
    {
	String osName = System.getProperty("os.name");
      	if (osName.startsWith("Windows")) {
	    targetCmd = "ant.bat " + target;
	} else {
	    targetCmd = "ant " + target;
	}
    }
}
