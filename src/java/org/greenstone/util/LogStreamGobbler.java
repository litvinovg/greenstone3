package org.greenstone.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.*;

class LogStreamGobbler{
    static Logger logger = Logger.getLogger(org.greenstone.util.LogStreamGobbler.class.getName());
    
    public static void logError(InputStream in)
      {
	  try {
	      BufferedReader br = new BufferedReader(new InputStreamReader(in));
	      String line = null;
              //using null as a guard doesn't work on windows 
  	      while ((line = br.readLine()) != null){
		  logger.error(line);
	      }
	  } catch (IOException ioe) {
	      logger.error(ioe);
	  }
      }
    
    
    public void logInfo(InputStream in){
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line = null;
	    while ((line = br.readLine()) != null){
		  logger.info(line);
	      }
	  } catch (IOException ioe) {
	      logger.error(ioe);
	  }
    }
}
