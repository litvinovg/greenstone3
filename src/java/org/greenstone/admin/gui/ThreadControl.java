/**
 *#########################################################################
 *
 * A component of the GAI application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * <BR><BR>
 *
 * Author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *
 * <BR><BR>
 *
 * Copyright (C) 1999 New Zealand Digital Library Project
 *
 * <BR><BR>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * <BR><BR>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * <BR><BR>
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *########################################################################
 */
package org.greenstone.admin.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

import org.greenstone.admin.GAI;
import org.greenstone.admin.GAIManager;
import org.greenstone.admin.gui.LogPane;
import org.greenstone.core.ParsingProgress;
import org.apache.tools.ant.*;
import org.apache.tools.ant.helper.ProjectHelperImpl;



public class ThreadControl 
    extends Thread {
    
    private GAIManager gai_man;

    public ThreadControl(){
    }

    public void run(){
    }   

    
    public void executeAntTarget(String command_string) throws IOException  {
	Project ant = new Project();
	File buildFile = new File (GAI.gsdl3_src_home + File.separator + "build.xml");
	
	//Set up a PrintStream pointing at a file
	//FileOutputStream ant_out =
	//   new FileOutputStream(GAI.gsdl3_src_home + File.separator + "ant_out.log");
	//FileOutputStream ant_err = 
	//   new FileOutputStream(GAI.gsdl3_src_home + File.separator + "ant_err.log");
	
	//	BufferedOutputStream ant_out_buffer = 
	//   new BufferedOutputStream(ant_out, 1024);
	//BufferedOutputStream ant_err_buffer = 
	///   new BufferedOutputStream(ant_err, 1024);
	
	//PrintStream ant_out_ps =
	//   new PrintStream(ant_out_buffer, false);
	//PrintStream ant_err_ps =
	//   new PrintStream(ant_err_buffer, false);
	
	DefaultLogger log = new DefaultLogger();
        //log.setErrorPrintStream(ant_err_ps);
	//log.setOutputPrintStream(ant_out_ps);

	log.setErrorPrintStream(System.err);
	log.setOutputPrintStream(System.out);
        log.setMessageOutputLevel(Project.MSG_INFO);
	ant.addBuildListener(log);
	try {
	    ant.init();
	    ant.fireBuildStarted();
	    ProjectHelper helper = ProjectHelper.getProjectHelper();
	    helper.parse(ant, buildFile);
	    ant.executeTarget(command_string);
	    ant.fireBuildFinished(null);
	}
	catch (BuildException e) {
	    e.printStackTrace(System.err); 
	    ant.fireBuildFinished(e);
	}
	/*finally {
	    System.setOut(ant_out_ps);
	    System.setErr(ant_err_ps);
	    }*/

	//boolean ant_success = checkAntResult();
	//ant_out_ps.close();
	//ant_err_ps.close();
	/*if (!ant_success){
	    int result = JOptionPane.showConfirmDialog((Component) null, "Ant building call was failed, do you want to see the log file?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
	    if ( result == JOptionPane.YES_OPTION) {
		//System.exit(1);
		// Showing ant_out.log content
	    } else if (result == JOptionPane.NO_OPTION) {
		JOptionPane.showMessageDialog((Component) null,"You can see the log file from Log Pane!");
	    } 
	} else {
	    //System.err.println("****Ant build successed");
	    //JOptionPane.showMessageDialog((Component) null,"You can see the log file from Log Pane!");
	    
	    }*/
    }

    /*public boolean checkAntResult(){
	try {
	    String filename = GAI.gsdl3_src_home + File.separator +"ant_out.log";
	    String fileLine;
	    
	    BufferedReader ant_result = new BufferedReader(new FileReader(filename));
	    
	    while ((fileLine = ant_result.readLine()) != null) {
		if (fileLine.matches("BUILD SUCCESSFUL")){
		    //return true;
		    return false;
		} else if (fileLine.matches("BUILD FAILED")){
		    //System.err.println("***Ant result"+fileLine);
		    return false;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	   
	}	
	return false;
	}*/
    

    public class restartTomcatServer 
	implements Runnable {
	private String command_string = null;
	/* Construct a restartTomcatServer Object*/
	public restartTomcatServer(String command_string) {
	    this.command_string = command_string;
	}
	
	public void run() {
	    try {
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class startupTomcatServer 
	implements Runnable {
	private String command_string = null;
	/* Construct a startupTomcatServer Object*/
	public startupTomcatServer(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try { 
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class shutdownTomcatServer 
	implements Runnable {
	private String command_string = null;
	/* Construct a shutdownTomcatServer Object*/
	public shutdownTomcatServer(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try { 
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class startupMysqlServer 
	implements Runnable {
	private String command_string = null;

	/* Construct a startupMysqlServer Object*/
	public startupMysqlServer(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try { 
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class shutdownMysqlServer 
	implements Runnable {
	private String command_string = null;
	/* Construct a shutdownMysqlServer Object*/
	public shutdownMysqlServer(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try { 
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class restartMysqlServer 
	implements Runnable {
	private String command_string = null;
	/* Construct a restartMysqlServer Object*/
	public restartMysqlServer(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try { 
		executeAntTarget(this.command_string);
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    public class installGS3 
	implements Runnable {
	private String command_string = null;

	/* Install GSDL3 through ant program*/
	public installGS3(String command_string) {
	    this.command_string = command_string;
	}
	public void run() {
	    try {
     
		executeAntTarget(this.command_string);
		// ant_success;
	    } catch(Exception e) { 
		System.out.println(e.getMessage()); 
	    }
	}
    }

    
    public void destroy(){
    }
}


/***
 * This program shows how you can time a set of threads.  The main()
 * program demonstrates the timing classes by spawning and timing a
 * set of threads that just execute a dummy loop.
 ***/
/*public class ThreadTiming {

    /** Times a group of threads.  Works with TimingWrapper. */
//    public static class ThreadTimer {
//	private boolean __resultReady;
//	private int __monitor[], __numThreads;
//	private long __begin, __end;
	
	/** Creates a ThreadTimer that can time a given number of threads. */
//	public ThreadTimer(int numThreads) {
//	    __monitor     = new int[1];
//	    __monitor[0]  = 0;
//	    __numThreads  = numThreads;
//	    __resultReady = false;
//	}
	
	/** Starts the timer when all the threads are ready to start. */
//	public void start() {
//    synchronized(__monitor) {
//		if(++__monitor[0] >= __numThreads) {
//		    __begin = System.currentTimeMillis();
//		    __monitor.notifyAll();
//		} else {
//		    while(true) {
//			try {
//			    __monitor.wait();
//			} catch(InterruptedException e) {
//			    continue;
//			}
//			break;
//		    }
//		}
//	    }
//	}
	
	/** Stops the timer when the last thread is done. */
//	public void stop() {
//	    synchronized(__monitor) {
//	if(--__monitor[0] <= 0) {
//		    __end   = System.currentTimeMillis();
//		    synchronized(this) {
//		__resultReady = true;
//		notify();
//	    }
//	}
//   }
//}/
	
	/** Calling thread waits until timing result is ready. */
//	public synchronized void waitForResult() {
//	    while(!__resultReady) {
//		try {
///	    wait();
//	} catch(InterruptedException e) {
//	}
//    }
//    __resultReady = false;
//}
//
///** Returns elapsed time in milliseconds. */
//public long getElapsedTime() {
//	    return (__end - __begin);
//}
//  }
//  
    /** Wraps a thread so it can be timed with a group. */
//    public static class TimingWrapper implements Runnable {
//	private Runnable __runMe;
//private ThreadTimer __timer;

//	public TimingWrapper(Runnable runMe, ThreadTimer timer) {
//    __runMe   = runMe;
//    __timer   = timer;
//}
	
//public void run() {
//    // Note, this timing approach does not measure thread creation
//    // and thread joining overhead.
//    __timer.start();
//    __runMe.run();
//    __timer.stop();
//}
//  }


//public static final int NUM_THREADS = 10;
//public static final int NUM_LOOPS   = 10000000;
    
//   public static void main(String[] args) {
//int thread;
//ThreadTimer timer;
//
//	timer = new ThreadTimer(NUM_THREADS);

	// Start a number of threads that iterate a dummy loop
//	for(thread = 0; thread < NUM_THREADS; thread++)
//    new Thread(new TimingWrapper(new Runnable() {
//	    public void run() {
//		for(int i=0; i < NUM_LOOPS; i++);
//	    }
//	}, timer)).start();
//
//timer.waitForResult();
	
//	System.out.println("Elapsed Seconds: " +
//			   ((double)timer.getElapsedTime())/1000.0);
//   }
    
//}*/
