/**
 *#########################################################################
 *
 * A component of the GAI application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * <BR><BR>
 *
 * Author: John Thompson, Greenstone Digital Library, University of Waikato
 * Modified by: Chi-Yu Huang
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

/*
import org.greenstone.gatherer.Gatherer;
import org.greenstone.gatherer.LocalLibraryServer;
import org.greenstone.gatherer.collection.BasicCollectionConfiguration;
import org.greenstone.gatherer.file.WorkspaceTree;  // !!! Don't like this here
import org.greenstone.gatherer.util.ArrayTools;
import org.greenstone.gatherer.util.StaticStrings;*/


/** This class provides the functionality to delete current collections from the 
 ** GSDLHOME/collect/ directory. The user chooses the collection from a list, where 
 ** each entry also displays details about itself, confirms the delete of a collection 
 ** by checking a checkbox then presses the ok button to actually delete the collection.
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version 1.0
 */
public class DeleteLogFilePrompt 
	extends JDialog {
    
    /** A reference to ourself so any inner-classes can dispose of us. */
    private DeleteLogFilePrompt prompt = null;
    private File log_file = null;

    //A flag showing the deleting status
    private boolean current_logfile_deleted = false;

    /** The size of the delete prompt screen. */
    static final Dimension SIZE = new Dimension(500, 500);

    // Constructor.
    public DeleteLogFilePrompt() {
	super();
	prompt = this;
    }
    
    /** Destructor. */
    public void destroy() {
	prompt = null;
    }

    /** This method causes the Delete Confirmation Dialog to be displayed. 
     * returns true if it has deleted the log file that is currently open */
    public boolean display(File log_file) {
	int result = JOptionPane.showConfirmDialog((Component) null, "Do you really want to delete this log file", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
	if ( result == JOptionPane.YES_OPTION) {
	    log_file.delete();
	    current_logfile_deleted = true;
	    JOptionPane.showMessageDialog(prompt,log_file.getPath() + " log file has been deleted. You have to restart the action to create the log file");
	} else if (result == JOptionPane.NO_OPTION) {
	    current_logfile_deleted = false;
	    JOptionPane.showMessageDialog(prompt, "Cancel deleting the " + log_file.getPath() +" log file");
	    prompt.dispose();
	}
	return current_logfile_deleted;
    }
    
    
    /** Shows a delete complete prompt. 
     * @param success A <strong>boolean</strong> indicating if the collection was successfully deleted.
     * @see org.greenstone.gatherer.collection.Collection
     */
    /*public void resultPrompt(boolean success) {
	if (success) {
	    JOptionPane.showMessageDialog(prompt,"Selected log file has been deleted. You have to restart the action to create the log file");
	} else {
	    JOptionPane.showMessageDialog(prompt, "Cancel deleting the log file");
	}
	}*/
    
    /** Any implementation of ActionListener requires this method so that when an 
     **action is performed the appropriate effect can occur.*/
    public void actionPerformed(ActionEvent event) {
    }
}
