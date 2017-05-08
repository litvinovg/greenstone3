package org.greenstone.util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.greenstone.util.LogStreamGobbler;

import org.apache.log4j.*;

public abstract class RunTarget extends Thread 
{
    protected String targetCmd = "";
    protected static Logger logger = Logger.getLogger(RunTarget.class.getName());

    protected int state = -1; //success: 0 error: 1    
    public static int SUCCESS = 0;
    public static int FAILED  = 1; 

    protected String targetSuccess;
    protected String targetFailed;
    protected String targetFinished;

    public void run() 
    {
	 
	 try {
	     state = -1;
	     String targetCmd = getTargetCmd();
	     logger.info("Target: " + targetCmd);

	     /*
	     Runtime run = Runtime.getRuntime();
             Process process = run.exec(targetCmd);
	     BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	     String line = null;
             
	     while ((line = br.readLine()) != null) {

		 logger.info(line);

		 if (line.equals(targetSuccess)){
		     state = 0;
		 }
		 
		 if (line.equals(targetFailed)){
		     state = 1; 
		 }

		 if(line.startsWith(targetFinished)){
		     break;		 
		 }
	     }
	     
	     br.close();
	     */
	     
	     
	     SafeProcess process = new SafeProcess(targetCmd);
	     process.setSplitStdOutputNewLines(true);
	     process.runProcess();	     
	     String output = process.getStdOutput();
	     String[] lines = output.split("[\\r\\n]+"); // http://stackoverflow.com/questions/454908/split-java-string-by-new-line
	     for(int i = 0; i < lines.length; i++) {
		 //System.err.println("*** Got line:|" + lines[i] + "|***");
		 String line = lines[i].trim();
		 if (line.equals(targetSuccess)){
		     state = 0;
		 }
		 
		 if (line.equals(targetFailed)){
		     state = 1; 
		 }

		 if(line.startsWith(targetFinished)){
		     break;		 
		 }
	     }
	     //System.err.println("\n\n");

	     
	     if(state < 0) {
		 logger.info("Unexpected end of input when running target: " + targetCmd);
	     }
	 } catch (Exception e) {
	     e.printStackTrace();
	     logger.error(e);
	     state = 1;
	 } 
    }
  
    public int getTargetState()
    {
	return state;     
    }
    
    public abstract void setTargetCmd(String cmd);

    public String getTargetCmd() 
    {
	return this.targetCmd;
    }
}
