/*
 *    Processing.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import java.io.InputStreamReader;

import org.apache.log4j.*;

public class Processing {

      static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.Processing.class.getName());
 
      /** Determine if the given process is still executing. It does this by attempting to throw an exception - not the most efficient way, but the only one as far as I know
     * @param process the Process to test
     * @return true if it is still executing, false otherwise
     */
    static public boolean processRunning(Process process) {
	boolean process_running = false;

	try {
	    process.exitValue(); // This will throw an exception if the process hasn't ended yet.
	}
	catch(IllegalThreadStateException itse) {
	    process_running = true;
	}
	catch(Exception exception) {
	    exception.printStackTrace();	    
	}
	return process_running;
    }
    
    static public int runProcess(String  command) {
	
	logger.error("executing command "+command);
	int exit_value = -1;
	try {
	    Process prcs = Runtime.getRuntime().exec(command);
	    
	    InputStreamReader error_stream = new InputStreamReader( prcs.getErrorStream(), "UTF-8" );
	    InputStreamReader output_stream = new InputStreamReader( prcs.getInputStream(), "UTF-8"  );
	    
	    StringBuffer error_buffer = new StringBuffer();
	    StringBuffer output_buffer = new StringBuffer();
	    
	    while(processRunning(prcs)) {
		// Hopefully this doesn't block if the process is trying to write to STDOUT.
		if((error_stream!=null)) {// && error_stream.ready()) { 
		    while (error_stream.ready()) {
			error_buffer.append((char)error_stream.read());
		    }
		    //error_buffer = get_stream_char(error_stream,error_buffer,bos);
		}
		//Hopefully this won't block if the process is trying to write to STDERR
		/*else if*/ while (output_stream.ready()) {
		    output_buffer.append((char)output_stream.read());
		    //output_buffer = get_stream_char(output_stream,output_buffer,bos);
		}
		//else {
		try {
		    Thread.sleep(100);
		}
		catch(Exception exception) {
		}
		// }
	    }
	    
	    // Of course, just because the process is finished doesn't
	    // mean the incoming streams are empty. Unfortunately I've
	    // got no chance of preserving order, so I'll process the
	    // error stream first, then the out stream
	    while(error_stream.ready()) {
		error_buffer.append((char)error_stream.read());
		//error_buffer = get_stream_char(error_stream,error_buffer,bos);
	    }
	    
	    while(output_stream.ready()) {
		output_buffer.append((char)output_stream.read());
		//output_buffer = get_stream_char(output_stream,output_buffer,bos);
	    }
	    
	    // do something with the messages
	    logger.error("err>"+error_buffer.toString());
	    logger.error("out>"+output_buffer.toString());
	    // Ensure that any messages still remaining in the string buffers are fired off.
	    // Now display final message based on exit value
	    
	    prcs.waitFor(); 
	    error_stream.close();
	    output_stream.close();
	    error_buffer = null;
	    output_buffer = null;
	    
	    exit_value =  prcs.exitValue();
	} catch (Exception e) {
	    logger.error(e);
	    return 1;
	}
	return exit_value;
	
	
    }
   

}
