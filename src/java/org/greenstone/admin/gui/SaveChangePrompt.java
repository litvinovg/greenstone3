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

import org.greenstone.admin.gui.ConfPane;

/** This class provides the functionality to show a prompt to save changed file  
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version 1.0
 */
public class SaveChangePrompt
    extends JDialog {
    
    /** A reference to ourself so any inner-classes can dispose of us. */
    private SaveChangePrompt prompt = null;
    private File file = null;
        
    //A flag showing the saving change status
    private boolean current_change_saved = false;
    
    /** The size of the save prompt screen. */
    static final Dimension SIZE = new Dimension(500, 500);
    
    // Constructor.
    public SaveChangePrompt() {
	super();
	prompt = this;
    }
    
    /** Destructor. */
    public void destroy() {
	prompt = null;
    }
    
    /** This method causes the Save Change Confirmation Dialog to be displayed.
     * returns true if it has save the change file that is currently open */
    // public boolean display(File file) {
	/*int result = JOptionPane.showConfirmDialog((Component) null, "Do you really want to save the change", "Save Confirmation", JOptionPane.YES_NO_OPTION);
	  if ( result == JOptionPane.YES_OPTION) {
	  ConfPane confPane = new ConfPane();
	  confPane.saveNewConfSetting();
	  confPane.writeConfFile(file);
	  current_change_saved = true;
	  } else if (result == JOptionPane.NO_OPTION) {
	    current_change_saved = false;
	    prompt.dispose();
	    }
	    resultPrompt(current_change_saved);
	    return current_change_saved;*/
    //}
    
    /** Shows a save complete prompt. 
     * @param success A <strong>boolean</strong> indicating if the collection was successfully saved
     */
    public void resultPrompt(boolean success) {
	if (success) {
	    JOptionPane.showMessageDialog(prompt,"The build property file has been saved.");
	} else {
	    JOptionPane.showMessageDialog(prompt, "Cancel saving the change to build property file");
	}
    }
    
    /** Any implementation of ActionListener requires this method so that when an 
     **action is performed the appropriate effect can occur.*/
    public void actionPerformed(ActionEvent event) {
    }
}
