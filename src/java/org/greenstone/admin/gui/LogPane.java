/**
 *#########################################################################
 *
 * A component of the Gatherer application, part of the Greenstone digital
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.greenstone.admin.GAIManager;
import org.greenstone.admin.GAI;
import org.greenstone.admin.Configuration;

/** The Log pane is to view the status of relevant log files in GSIII
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version
 */
public class LogPane
    extends JPanel
    implements ActionListener {

    /* The pane to demonstrate log information, including  the log files being 
     *monitored and their content*/
    protected JSplitPane main_log_pane = null;

    /** The panel that contains a log_list */
    private JPanel logList_pane =  null;

    /** The panel that contains the log content. */
    private JPanel logContent_pane =  null;

    /** The List showing all the log files concerned. */
    private JList log_list=null;

    /** The scrollable area into which the log content is placed. */
    private JScrollPane log_content = null;

    /** The label at the top of the logList_pane. */
    private JLabel logList_label =  null;

    /** The label shown at the top of the logContent Pane. */
    private JLabel logContent_label  =  null;

    /* Log TEXT AREA*/
    private JTextArea log_textarea = null;

    // The control buttons used to manipulate log Pane
    protected JPanel button_pane = null;
    
    /** Buttons */
    private JButton reload_button = null;
    private JButton clear_button = null;

    /*The pane which contains the controls for log files */
    private JPanel control_pane = null;

    private File log_file = null;
      
    /** The various sizes for the screen layout*/
    static private Dimension MIN_SIZE = new Dimension( 90,  90);
    static private Dimension LIST_SIZE  = new Dimension(200, 450);
    static private Dimension LOGPANE_SIZE = new Dimension (800,450);

    //Constructor
    public LogPane() {
	
	// create all the necessary panes
	control_pane = new JPanel();
	button_pane = new JPanel();

	// Log_Pane
	main_log_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	main_log_pane.setPreferredSize(LOGPANE_SIZE);
	//log_pane.setSize(LOGPANE_SIZE);
	
	// Log_list
	String[] log_files = { "Tomcat log file", "Extension log file", "Log file 3"};
	log_list = new JList(log_files);
	log_list.setBorder(BorderFactory.createLoweredBevelBorder());
	log_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	log_list.setVisibleRowCount(4);
	log_list.setBackground (Configuration.getColor("coloring.workspace_tree_background"));
	log_list.setForeground (Configuration.getColor("coloring.workspace_tree_foreground"));
	log_list.addListSelectionListener(new LogListListener());
	
	// Log_TextArea
	log_textarea = new JTextArea();
	log_textarea.setEditable(false);
		
	// Buttons
	ReloadButtonListener rbl = new ReloadButtonListener();
	ClearButtonListener dbl = new ClearButtonListener();
	ImageIcon reloadButtonIcon = new ImageIcon(GAI.images_path + "toolbarButtonGraphics/general/Refresh16.gif");
	ImageIcon clearButtonIcon = new ImageIcon(GAI.images_path + "toolbarButtonGraphics/general/Delete16.gif");

	//reload_button = new JButton("Reload", reloadButtonIcon);
	reload_button = new JButton();
	reload_button.addActionListener(rbl);
	reload_button.setEnabled(false);
	reload_button.setMnemonic(KeyEvent.VK_R);
	reload_button.setText(GAI.dictionary.get("LogPane.Reload_Log"));
	reload_button.setToolTipText(GAI.dictionary.get("LogPane.Reload_Log_Tooltip"));

	clear_button = new JButton();
	clear_button.addActionListener(dbl);
	clear_button.setEnabled(false);
	clear_button.setMnemonic(KeyEvent.VK_C);
	clear_button.setText(GAI.dictionary.get("LogPane.Clear_Log"));
	clear_button.setToolTipText(GAI.dictionary.get("LogPane.Clear_Log_Tooltip"));

	rbl = null;
	dbl = null;
    }

    /** Any implementation of ActionListener requires this method so that when an 
     **action is performed the appropriate effect can occur.*/

    public void actionPerformed(ActionEvent event) {
    }


    /** This method is callsed to actually layout the components.*/
    public void display() {
	// Create Components.
	//KeyListenerImpl key_listener = new KeyListenerImpl();
	//MouseListenerImpl mouse_listener = new MouseListenerImpl();
	//this.addKeyListener(key_listener);

	// logList_Pane
	logList_pane = new JPanel();
	logList_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	logList_label = new JLabel();
	logList_label.setOpaque(true);
	logList_label.setBackground(Configuration.getColor("coloring.workspace_heading_background"));
	logList_label.setForeground(Configuration.getColor("coloring.workspace_heading_foreground"));
	logList_label.setText(GAI.dictionary.get("LogPane.Log_List"));

	// logContent_Pane
	logContent_pane = new JPanel();
	logContent_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	logContent_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	logContent_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	logContent_label = new JLabel();
	logContent_label.setOpaque(true);
	logContent_label.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	logContent_label.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	logContent_label.setText(GAI.dictionary.get("LogPane.Log_Content"));
	
	// TEXTAREA Layout
	log_content = new JScrollPane(log_textarea);

	// Button Layout 
	button_pane.setLayout (new GridLayout(1,3));
	button_pane.add(reload_button);
	button_pane.add(clear_button);
	
	control_pane.setLayout (new BorderLayout());
	//control_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	control_pane.setBorder(BorderFactory.createEmptyBorder(05,10,5,10));
	control_pane.setPreferredSize(new Dimension(50,50));
	control_pane.setSize(new Dimension(50,50));
	control_pane.add (button_pane, BorderLayout.CENTER);

	// Layout Components
	logList_pane.setLayout(new BorderLayout());
	logList_pane.add(log_list, BorderLayout.CENTER);
	logList_pane.add(logList_label, BorderLayout.NORTH);
		
	logContent_pane.setLayout(new BorderLayout());
	logContent_pane.add(log_content,BorderLayout.CENTER);
	logContent_pane.add(logContent_label, BorderLayout.NORTH);
	logContent_pane.add(control_pane, BorderLayout.SOUTH);
	
	main_log_pane.add(logList_pane, JSplitPane.LEFT);
	main_log_pane.add(logContent_pane, JSplitPane.RIGHT);
	main_log_pane.setDividerLocation(LIST_SIZE.width - 10);
	
	this.setLayout(new BorderLayout());
	this.add(main_log_pane, BorderLayout.CENTER);
	//this.add(control_pane, BorderLayout.SOUTH);
    }

    /** Called whenever this pane gains focus, this method ensures that the various 
     **tree renderers are correctly colouring the tree (as these settings sometimes get lost).
     * @param event A <strong>FocusEvent</strong> containing details about the focus action performed.
     */
    /*public void focusGained(FocusEvent event) {
	DefaultTreeCellRenderer def = new DefaultTreeCellRenderer();
	DefaultTreeCellRenderer w = (DefaultTreeCellRenderer)workspace_tree.getCellRenderer();
	DefaultTreeCellRenderer c = (DefaultTreeCellRenderer)collection_tree.getCellRenderer();
	if(event.getSource() == workspace_tree) {
	    w.setBackgroundSelectionColor(def.getBackgroundSelectionColor());
	    c.setBackgroundSelectionColor(Color.lightGray);
	}
	else if(event.getSource() == collection_tree) {
	    c.setBackgroundSelectionColor(def.getBackgroundSelectionColor());
	    w.setBackgroundSelectionColor(Color.lightGray);
	}
	repaint();
	}*/
    
    /** Implementation side-effect, not used in any way.
     * @param event A <strong>FocusEvent</strong> containing details about the focus action performed.
     */
    /*public void focusLost(FocusEvent event) {
      }*/
    
    /** Called to inform this control panel that it has just gained focus as an effect of the user clicking on its tab.
     */
    public void gainFocus() {
	// Update the meta-audit view to show the current selection, if any.
	//Gatherer.g_man.meta_audit.setRecords(getCollectionTreeSelection());
    }
    
    /** Called whenever the detail mode changes to ensure the filters are at an appropriate 
     **level (ie only editable by those that understand regular expression matching)
     * @param mode the mode level as an int
     */
    /*  public void modeChanged(int mode) {
	collection_filter.setEditable(mode > Configuration.LIBRARIAN_MODE);
	workspace_filter.setEditable(mode > Configuration.LIBRARIAN_MODE);
	}*/
    
    private class KeyListenerImpl
	extends KeyAdapter {
	private boolean vk_left_pressed = false;
	public void keyReleased(KeyEvent event) {
	}
	// we need to watch for left clicks on an unopened folder - should shift the focus to the
	//parent folder. But because there is some other mysterious key listener that does opening and 
	//closing folders on right and left clicks, we must detect the situation before the other handler 
	//has done its job, and process it after.*/
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_LEFT) {
		
	    }
	}     
    }

    /** This class listens for mouse clicks and responds right mouse button clicks (popup menu)., left
     **  mouse button:log file selected */
    private class MouseListenerImpl
	extends MouseAdapter {
	/* Any subclass of MouseAdapter can override this method to respond to mouse click events. 
	 *In this case we want to open a pop-up menu if we detect a right mouse click over one of our 
	 *registered components, and start an external application if someone double clicks on a certain 
	 *file record. */
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	
    }
    
    /** This class serves as the listener for actions on the build button. */
    private class ReloadButtonListener
	implements ActionListener {
	/** As the log files could be modified in the runtime environment
	 *  This button is to reload the log file whenever user want to */
	public void actionPerformed(ActionEvent event) {
	    // Remember that for lower thresholds the above doesn't work, so try this instead
	    reload_button.setEnabled(true);
	    clear_button.setEnabled(true);
	    updateLogsContent(log_file.getPath());
	}
    }
    
    private class ClearButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    if (!log_file.exists()){
		JOptionPane.showMessageDialog((Component) null,log_file.getPath() + " log file does not exist");
		return;
	    } else {
		ClearLogFilePrompt cfp  = new ClearLogFilePrompt();
		boolean file_cleared = cfp.display(log_file);
		if (file_cleared) {
		    log_textarea.setText("");
		}
	    }
	    clear_button.setEnabled(false);
	}
    }
    
    public void updateLogsContent(String filename){
	if (!log_file.exists()){ 
	    log_textarea.setText("");
	    JOptionPane.showMessageDialog((Component) null, filename+" log file does not exist");  
	    clear_button.setEnabled(false);
	} else {
	    readFile(filename);
	}
    }

    public void readFile (String filename) {
	log_textarea.setText("");
	String fileLine;
	try {
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	    while ((fileLine = in.readLine()) != null) {
		log_textarea.append(fileLine);
		log_textarea.append("\n");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}	    
    }
   
    private class LogListListener implements ListSelectionListener {
	public void valueChanged (ListSelectionEvent e){
	    if (e.getValueIsAdjusting() == false){
		if (log_list.getSelectedIndex() == -1){
		    //no selection
		} else if (log_list.getSelectedIndex () == 0 ) {
		    log_file = new File (GAI.tomcat_home+File.separatorChar+"logs"+File.separatorChar+"catalina.out");
		    String filename = log_file.getPath();
		    updateLogsContent(filename);
		    reload_button.setEnabled(true);
		    clear_button.setEnabled(true);
		} else if (log_list.getSelectedIndex () == 1) {
		    log_file = new File (GAI.getGSDL3ExtensionHome() + File.separatorChar + "logs" + File.separatorChar + "ext.log");
		    String filename = log_file.getPath();
		    updateLogsContent(filename);
		    clear_button.setEnabled(true);
		    reload_button.setEnabled(true);
		} else if (log_list.getSelectedIndex () == 2) {
		    log_textarea.setText("");
		    JOptionPane.showMessageDialog((Component) null,"This file has not been defined yet");	
		    clear_button.setEnabled(false);
		    reload_button.setEnabled(false);
		}
	    }
	}
    }
    public void modeChanged (int mode){
	return;
    }
    public void afterDisplay() {
	return;
    }
}
