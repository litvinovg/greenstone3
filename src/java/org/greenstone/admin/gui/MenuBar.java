/**
 *#########################################################################
 *
 * A component of the GAI application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * <BR><BR>
 *
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
 * Author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *########################################################################
 */
package org.greenstone.admin.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.greenstone.admin.GAI;
import org.greenstone.admin.Configuration;

/** The menu bar for the Administration tools main GUI. 
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version
 */
public class MenuBar
    extends JMenuBar {
    /** The icon to be displayed alongside the context choosen help file. */
    private int current_tab = -1;
    
    private JMenu file = null;
    private JMenu edit = null;
    public JMenu help  = null;
    // public JMenuItem file_cdimage    = null;
    // public JMenuItem file_close      = null;
    //public JMenuItem file_delete     = null;
    public JMenuItem file_exit = null;
    public JMenuItem file_save = null;
    public JMenuItem help_general;
    public JMenuItem help_conf;
    public JMenuItem help_ext;
    public JMenuItem help_monitor;
    public JMenuItem help_log;
    public JMenuItem help_about;

    static public ImageIcon HELP_ICON = null; 
    static public ImageIcon BLANK_ICON = null;
    
    //    public MenuBar(MenuListener Menu_listener)
    public MenuBar(MenuListener menu_listener)
    {
	HELP_ICON = new ImageIcon(GAI.images_path +"help.gif");
	BLANK_ICON = new ImageIcon(GAI.images_path+"blank.gif");

	//file = new JMenu("File");
	file = new JMenu();
	file.setMnemonic(KeyEvent.VK_F);
	file.setText(GAI.dictionary.get("Menu.File"));
	
	//file_save = new JMenuItem("Save");
	file_save = new JMenuItem();
	file_save.addActionListener(GAI.ga_man);
	file_save.setMnemonic(KeyEvent.VK_S);
	file_save.setText(GAI.dictionary.get("Menu.File_Save"));
	
	//file_exit = new JMenuItem("Exit");
	file_exit = new JMenuItem();
	file_exit.addActionListener(GAI.ga_man);
	file_exit.setMnemonic(KeyEvent.VK_X);
	file_exit.setText(GAI.dictionary.get("Menu.File_Exit"));

	//Layout (File menu)
	file.add(file_save);
	file.add(new JSeparator());
	file.add(file_exit);

	// Edit menu
	//edit = new JMenu("Edit");
	edit = new JMenu();
	edit.setMnemonic(KeyEvent.VK_E);
	edit.setText(GAI.dictionary.get("Menu.Edit"));
			  
	// Help menu
	//help = new JMenu("Help");
	help = new JMenu();
	help.setMnemonic(KeyEvent.VK_H);
	help.setIcon(HELP_ICON);
	help.setText(GAI.dictionary.get("Menu.Help"));
	
	help_general = new JMenuItem();
	help_general.addActionListener(GAI.ga_man);
	help_general.setText(GAI.dictionary.get("Source.General"));
		
	help_conf = new JMenuItem();
	help_conf.addActionListener(GAI.ga_man);
	help_conf.setText(GAI.dictionary.get("GAI.Configuration"));
		
	help_ext = new JMenuItem();
	help_ext.addActionListener(GAI.ga_man);
	help_ext.setText(GAI.dictionary.get("GAI.Ext"));
	
	help_monitor = new JMenuItem();
	help_monitor.addActionListener(GAI.ga_man);
	help_monitor.setText(GAI.dictionary.get("GAI.Monitor"));
	
	help_log = new JMenuItem();
	help_log.addActionListener(GAI.ga_man);
	help_log.setText(GAI.dictionary.get("GAI.Log"));
	
	help_about = new JMenuItem();
	help_about.addActionListener(GAI.ga_man);
	help_about.setText(GAI.dictionary.get("Menu.Help_About"));
			
	
	// Layout (help menu)
	help.add(help_general);
	help.add(new JSeparator());
	if (Configuration.get("admin.conf")) {
	    help.add(help_conf);
	}
	if (Configuration.get("admin.ext")) {
	    help.add(help_ext);
	}
	if (Configuration.get("admin.monitor")) {
	    help.add(help_monitor);
	}
	if (Configuration.get("admin.log")) {
	    help.add(help_log);
	}
	
	help.add(new JSeparator());
	help.add(help_about);

	// Layout (menu bar)
	add(file);
	add(Box.createHorizontalStrut(15));
	add(edit);
	add(Box.createHorizontalGlue());
	add(help);
    }

    /** Once a quit has been requested by the user, prevent any further menu selections. */
    public void exit() {
	file.setEnabled(false);
	edit.setEnabled(false);
	help.setEnabled(false);
    }

    /*public void refresh(int refresh_reason, boolean ready)
    {
	file_close.setEnabled(ready);
	file_save.setEnabled(ready);
	}*/


    /** In order to provide context aware help advice we keep track of which
     *  tab the user has open, and then highlight that help menu item with
     *  separators.
     *  @param tab_index The index of the selected tab (0-7).
     */
    public void tabSelected(int tab_index) {
	JMenuItem selected;
	if(current_tab != -1) {
	    // Remove the image
	    selected = help.getItem(current_tab);
	    if(selected != null) {
		selected.setIcon(BLANK_ICON);
	    }
	}
	current_tab = tab_index + 2;
	selected = help.getItem(current_tab);
	if(selected != null) {
	    selected.setIcon(HELP_ICON);
	    
	}
	selected = null;
    }
}
