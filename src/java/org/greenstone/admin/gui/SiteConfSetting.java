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
 * Modified: 03.2005
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
import java.util.*;
import java.lang.Object;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import org.greenstone.admin.GAI;

/*Site configuration demonstrated in XML format
 *@author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *@version:
 */
public class SiteConfSetting
    extends JPanel {
    // implements TextListener {

    //The Text area to demonstarte configuration content
    private JTextArea site_conf_content = null;

    //The scrollable area into which the configuration content is placed
    private JScrollPane site_conf_area = null;
    
    private String site_conf_name;
    private boolean conf_changed = false;
    private StringBuffer site_conf_in;
    
    public SiteConfSetting(String conf_name, File conf_file) {
	super();
	this.site_conf_name = conf_name;
	/*A StringBuffer to store the content of Site Configuration files such as 
	 *SiteConfig.xml and InterfaceConfig.xml files*/
	this.site_conf_in = new StringBuffer();
	this.site_conf_in = readConfFile(conf_file);
	this.site_conf_content = new JTextArea();
	this.site_conf_content.setEditable(true);
	this.site_conf_content.append(this.site_conf_in.toString());
	this.site_conf_area = new JScrollPane(site_conf_content);  
	this.site_conf_content.getDocument().addDocumentListener(new ConfDocumentListener());
	setLayout(new BorderLayout());
	add(site_conf_area,BorderLayout.CENTER);
    }
    
    private class ConfDocumentListener implements DocumentListener {
        final String newline = "\n";
	
        public void insertUpdate(DocumentEvent e) {
	    conf_changed = true;
        }
        
	public void removeUpdate(DocumentEvent e) {
	    conf_changed = true;
	}
	
        public void changedUpdate(DocumentEvent e) {
	    conf_changed = true;
	}
	
        public void updateLog(DocumentEvent e, String action) {
	}
    }
    
    public boolean confChanged ()
    {
	return this.conf_changed;
    }
    
    public void showText()
    {
	site_conf_content.getText();
    }
    
    public void goTop()
    {
	//Go to the top of ScrollPane
	site_conf_content.setCaretPosition(0);
    }
    
    public void saveFile(File filename) 
    {
	try {
	    BufferedWriter site_out = new BufferedWriter(new FileWriter(filename.getPath()));
	    String text = site_conf_content.getText();
	    site_out.write(text);
	    site_out.close();
	    conf_changed = false;
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private StringBuffer readConfFile(File filename)
    {
	StringBuffer file_buffer = new StringBuffer();
	String fileLine;
	try {
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	    while ((fileLine = in.readLine())!= null) {
		file_buffer.append(fileLine);
		file_buffer.append("\n");
	    }
   	} catch (Exception e) {
	    e.printStackTrace();
	}	    
	return file_buffer;
    }


}
