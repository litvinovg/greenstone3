/**
 *#########################################################################
 *
 * A component of the Greenstone Administrator Interface, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * Author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *
 * Copyright (C) 1999 New Zealand Digital Library Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *########################################################################
 */

package org.greenstone.admin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

import org.greenstone.gsdl3.util.Dictionary;
import org.greenstone.admin.gui.SetServerPane;

/** Containing the top-level "core" for the GAI(Greenstone Administrator
 * Interface) this class is the common core for the GAI application and 
 * applet. It first parses the command line arguments, preparing to update 
 * the configuration as required. Next it loads several important support 
 * classes such as the Configuration and Dictionary. Finally it creates the 
 * other important managers and sends them on their way.
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version ###
 */
public class GAI
{
    public static Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    public static String gsdl3_src_home;
    public static String gsdl3_web_home;
    public static String tomcat_home;
    
    public static File build_properties_file;

    public static String images_path;
    /** A public reference to the GAIManager. */
    static public GAIManager ga_man;

    /** a public reference to the Dictionary */
    static public Dictionary dictionary = null;
    
    public GAI(String gsdl3_src_home, String gsdl3_web_home)
    {
	this.gsdl3_src_home = gsdl3_src_home;
	this.gsdl3_web_home = gsdl3_web_home;
	
	// set up the Configuration
	new Configuration();
	
	// Read Dictionary
	this.dictionary = new Dictionary("gai", Configuration.getLocale("general.locale"));
	this.build_properties_file = new File(this.gsdl3_src_home, "build.properties");
	// this may change if using preinstalled tomcat
	this.tomcat_home = this.gsdl3_src_home+File.separatorChar+"packages"+File.separatorChar+"tomcat";
	
	// should come from classpath ??
	this.images_path = this.gsdl3_src_home+File.separatorChar+"resources"+File.separatorChar+"images"+File.separatorChar;
	// start the GAIManager
	ga_man  = new GAIManager(screen_size);
	ga_man.display();

    }
    
    public static void main (String[] args){
	// A serious hack, but its good enough to stop crappy 'Could not
        // lock user prefs' error messages.  Thanks to Walter Schatz from
        // the java forums.
        System.setProperty("java.util.prefs.syncInterval","2000000");

	if (args.length != 2) {
	    System.err.println("Usage: java org.greenstone.admin.GAI <gsdl3 src home> <gsdl3 web home>");
	    System.exit(1);
	}
	
	File gsdl3_src_dir = new File(args[0]);
	File gsdl3_web_dir = new File(args[1]);
	if (!gsdl3_src_dir.isDirectory() || !gsdl3_web_dir.isDirectory()) {
	    System.err.println("Usage: java org.greenstone.admin.GAI <gsdl3 src home> <gsdl3 web home>");
	    System.err.println("src or web directory does not exist!");
	    System.exit(1);
	}
	
	GAI gai = new GAI(args[0], args[1]);

    }

}


