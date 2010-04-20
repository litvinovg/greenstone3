/**
 *#########################################################################
 *
 * A component of the Administration Tool (GAI) application, part of the 
 * Greenstone digital library suite from the New Zealand Digital Library 
 * Project at the University of Waikato, New Zealand.
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
 *
 * Author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *
 *########################################################################
 */

package org.greenstone.admin;

import org.greenstone.admin.GAI;
import org.greenstone.admin.gui.TabbedPane;
import org.greenstone.admin.gui.MenuBar;
import org.greenstone.admin.gui.LogPane;
import org.greenstone.admin.gui.ConfPane;
import org.greenstone.admin.guiext.ExtPane;

//import java AWT classes
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

public class GAIManager 
    extends JFrame 
    implements ActionListener, ChangeListener{
    
    private Dimension size = null;
    // The ConfPane contains the configuration details of Greenstone III build and installation
    public ConfPane conf_pane = null;
    // The LogPane contains the log details for Tomcat Server used by gsdl3
    public LogPane log_pane = null;
    // Not designed yet
    public ExtPane ext_pane = null;
    // Not designed yet
    public JPanel monitor_pane = null;
       
    static public ImageIcon CONF_ICON = null; 
    static public ImageIcon EXT_ICON = null; 
    static public ImageIcon MONITOR_ICON = null; 
    static public ImageIcon LOG_ICON = null; 
    
    // Create a menu bar in the main Pane
    public MenuBar menu_bar = null;

    // HelpFrame
    
    private JPanel previous_pane = null;
    private JPanel content_pane = null;
    private JTabbedPane tab_pane = null;

    /** A threaded tab changer to try and avoid NPE on exit. */
    private TabUpdater tab_updater = null;
    
    //Contructor
    public GAIManager(Dimension size){
	super();
	this.size = size;
	this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	this.setTitle("GreenStone 3 Administration Tool");
	/* Add a focus listener to ourselves. Thus if we gain focus when a Modal Dialog should 
	 * instead have it, we can try to bring the modal dialog to the front*/
	//this.addFocusListener(new GAIGUIFocusListener());	
	//display();
    }

    private class GAIGUIFocusListener
	extends FocusAdapter {
	public void focusGained(FocusEvent e) {
	    /*  if (ModalDialog.current_modal != null) {
		ModalDialog.current_modal.makeVisible();
		ModalDialog.current_modal.toFront();
		}*/
	}
    }

   public void display(){
	CONF_ICON = new ImageIcon (GAI.images_path + "create.gif");
	EXT_ICON = new ImageIcon (GAI.images_path + "information.gif");	
	MONITOR_ICON = new ImageIcon (GAI.images_path + "zoom.gif");
	LOG_ICON = new ImageIcon (GAI.images_path + "history.gif");
	content_pane = (JPanel) this.getContentPane();

	try {
	    this.setSize(size);
	    //create a menuBar
	    menu_bar = new MenuBar(new MenuListenerImpl());
	    this.setJMenuBar(menu_bar);
	    
	    //Create the tabbed pane and put it in the center
	    tab_pane = new JTabbedPane();
	    tab_pane.addChangeListener(this);
	    
	    //set up the Configuration Pane
	    if (Configuration.get("admin.conf")){
		conf_pane = new ConfPane();
		conf_pane.display();
		tab_pane.addTab("Configuration", CONF_ICON, conf_pane);
		tab_pane.setEnabledAt(tab_pane.indexOfComponent(conf_pane),Configuration.get("admin.conf"));
	    }
	    	    
	    //set up the ext Pane
	    if (Configuration.get("admin.ext")){
		ext_pane = new ExtPane();

		ext_pane.display();
		tab_pane.addTab("Extensions", EXT_ICON, ext_pane);
		tab_pane.setEnabledAt(tab_pane.indexOfComponent(ext_pane),Configuration.get("admin.ext"));
	    }
	    	    
	    //set up the Monitor Pane
	    if (Configuration.get("admin.monitor")){
		monitor_pane = new JPanel();
		//monitor_pane.display();
		tab_pane.addTab("Monitor", MONITOR_ICON, monitor_pane);
		tab_pane.setEnabledAt(tab_pane.indexOfComponent(monitor_pane),Configuration.get("admin.monitor"));
	    }

	    //set up the Log Pane
	    if (Configuration.get("admin.log")){
		log_pane = new LogPane();
		log_pane.display();
		tab_pane.addTab("Log", LOG_ICON, log_pane);
		tab_pane.setEnabledAt(tab_pane.indexOfComponent(log_pane),Configuration.get("admin.log"));
	    }   
	    content_pane.setLayout(new BorderLayout());
	    content_pane.add(tab_pane, BorderLayout.CENTER);
	    pack();
	    setVisible(true);
	} catch (Exception e){
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    /** Any implementation of <i>ActionListener</i> must include this method so that we can be informed 
     *  when an action has occured. In this case we are listening to actions from the menu-bar, and should 
     *  react appropriately.
     * @param event An <strong>ActionEvent</strong> containing information about the action that has occured.
     */
    
    public void actionPerformed (ActionEvent event){
	Object esrc = event.getSource();
	if (esrc == menu_bar.file_exit){
	    if (conf_pane.configurationChanged()) {
		// prompt users for save
		int result = JOptionPane.showConfirmDialog((Component) null, "The Configuration files have been changed, do you want to save the change", "Save Confirmation", JOptionPane.YES_NO_OPTION);
		if ( result == JOptionPane.YES_OPTION) {
		    conf_pane.save();	
		} 
	    }
	    exit();
	}
	else if (esrc == menu_bar.file_save){
	    /* sliently and Globally save Project and Site configuration*/
	    conf_pane.save();
	}
	else if (esrc == menu_bar.help_conf){
	}
	else if (esrc == menu_bar.help_ext){
	} 
	else if (esrc == menu_bar.help_monitor){
	}
	else if (esrc == menu_bar.help_log){
	}
	else if (esrc == menu_bar.help_about) {
	}
    }
    
    
    public void destory (){
	// Destroying create pane ensures the latest log has been closed
	//if (status_pane != null) {
	//}
    }
    /**Overridden from JFrame so we can exit safely when window is closed 
     *(or destroyed). @param event A <strong>WindowEvent</strong> containing 
     *information about the event that fired this call.
     */
    protected void processWindowEvent(WindowEvent event) {
	if(event.getID() == WindowEvent.WINDOW_CLOSING) {
	    exit();
	}
    }


    /** Listens to actions upon the menu bar, and if it detects a click over 
     *the help menu brings the help window to the front if it has become hidden.
     */
    private class MenuListenerImpl
	implements MenuListener {
	public void menuCanceled (MenuEvent e) {
	}
	
	public void menuDeselected (MenuEvent e){
	}
	
	public void menuSelected (MenuEvent e){
	    if (e.getSource() == menu_bar.file_exit) {
		exit();
	    }
	  
	    if (e.getSource() == menu_bar.help){
		if (menu_bar.help.isSelected()){
		    menu_bar.help.doClick(10);
		}
	    }
	}
    }


    /** This method ensures that all the things needing saving 
     *  are saved before GAIManager.exit() is called.
     */
    public void exit() {
	System.exit(0);
    }


    /** Any implementation of ChangeListener must include this method 
     **so we can be informed when the state of one of the registered 
     **objects changes. In this case we are listening to view changes 
     **within the tabbed pane.
     * @param event A ChangeEvent containing information about the event that fired this call.
     */
    public void stateChanged(ChangeEvent event) {
	if(previous_pane != null) {
	}
	
	//menu_bar.tabSelected(tab_pane.getSelectedIndex());
	int selected_index = tab_pane.getSelectedIndex();
	if (selected_index == tab_pane.indexOfComponent(log_pane)) {
	    log_pane.gainFocus();
	}
	else if (selected_index == tab_pane.indexOfComponent(conf_pane)) {
	    conf_pane.gainFocus();
	}
	previous_pane = (JPanel) tab_pane.getSelectedComponent();
    }
    
    public void modeChanged (int mode){
	if (log_pane != null){
	    log_pane.modeChanged(mode);
	}
	if (conf_pane !=null) {
	    conf_pane.modeChanged(mode);
	}
    }

    //   public void refresh (int refresh_reason, boolean collection_loaded)
    public void refresh () {
	if (log_pane != null ){
	}
    }

    public void returnToInitialPane(){
	if (log_pane != null) {
	    tab_pane.setSelectedComponent(log_pane);
	}
    }
    public void wait(boolean waiting) {
	Component glass_pane = getGlassPane();
	if(waiting) {
	    // Show wait cursor.
	    //glass_pane.addMouseListener(mouse_blocker_listener);
	    glass_pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    glass_pane.setVisible(true);
	}
	else {
	    // Hide wait cursor.
	    glass_pane.setVisible(false);
	    glass_pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    //glass_pane.removeMouseListener(mouse_blocker_listener);
	}
	glass_pane = null;
    }

    private class TabUpdater
	implements Runnable {
	private boolean ready = false;
	private int conf_pos = -1;
	private int ext_pos = -1;
	private int monitor_pos = -1;
	private int log_pos = -1;
	private JTabbedPane tab_pane = null;
	
	public TabUpdater (JTabbedPane tab_pane, boolean ready){
	    this.ready = ready;
	    this.tab_pane = tab_pane;
	    conf_pos = tab_pane.indexOfComponent(conf_pane);
	    ext_pos = tab_pane.indexOfComponent(ext_pane);
	    monitor_pos = tab_pane.indexOfComponent(monitor_pane);  
	    log_pos = tab_pane.indexOfComponent(log_pane);
	}
	
	public void run() {
	    if (conf_pos != -1){
		if (ready) {
		    tab_pane.setEnabledAt (conf_pos,Configuration.get("admin.conf"));
		} else {
		    tab_pane.setEnabledAt (conf_pos,false);
		}
	    }
	    if (ext_pos != -1){
		if (ready) {
		    tab_pane.setEnabledAt (ext_pos,Configuration.get("admin.ext"));
		} else {
		    tab_pane.setEnabledAt (ext_pos,false);
		}
	    }
	    if (monitor_pos != -1){
		if (ready) {
		    tab_pane.setEnabledAt (monitor_pos,Configuration.get("admin.monitor"));
		} else {
		    tab_pane.setEnabledAt (monitor_pos,false);
		}
	    }
	    if (log_pos != -1){
		if (ready) {
		    tab_pane.setEnabledAt (log_pos,Configuration.get("admin.log"));
		} else {
		    tab_pane.setEnabledAt (log_pos,false);
		}
	    }
	}
	public void setReady (boolean ready){

	    this.ready = ready;
	}
    }
}
