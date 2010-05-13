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
 * Date: 03.2005
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
/* This program is intended to confirm the initial user's setting about
 * the Tomcat server and MySQL server they would like to run GSDL3 with
 */
package org.greenstone.admin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Object;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.greenstone.util.Configuration;
import org.greenstone.core.ParsingProgress;
import org.greenstone.admin.gui.SetServerPane;
import org.greenstone.admin.GAI;

/** The initial setting pane is to request the user to input the preferred 
 *  Tomcat and Mysql server,they would like to run GSDL3 with
 *  @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *  @version
 */
public class GAIFirstSettingFrame
    extends JFrame
    implements ActionListener {

    public static Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
   
    /* This pane is to inform the user to set up the tomcat and mysql server 
     * if they are the first time run GAI, if the user doen't want to set up
     * the installation will be based on the gsdl3 default value*/
    protected JScrollPane setting_message_pane = null;
    protected JPanel first_setting_pane = null;
    protected JPanel main_pane = null;
    protected JTextArea first_setting_message = null;
    protected JButton fset_yes_button = null;
    protected JButton fset_no_button = null;
    protected JPanel button_pane = null;
    protected JPanel control_pane = null;
    
    public boolean setting_confirm = false;
    
    /** The various sizes for the screen layout*/
    static private Dimension MIN_SIZE = new Dimension( 90,  90);
    static private Dimension LIST_SIZE  = new Dimension(200, 450);
    static private Dimension DIALOG_SIZE = new Dimension (400,300);
    static private Dimension TABLE_SIZE = new Dimension(500,200);
    static final Dimension SIZE = new Dimension(400,75);						   
    
    //Constructor
    public GAIFirstSettingFrame() {
	super();

	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	this.setSize(DIALOG_SIZE);
	this.setTitle("First time setting Tomcat/MYSQL Server");

	// create all the control button panes
	button_pane  = new JPanel();

	//First time running button
	FirstSetYesButtonListener fsybl = new FirstSetYesButtonListener();
	FirstSetNoButtonListener fsnbl = new FirstSetNoButtonListener();
	
	// ImagesIcon for the buttons
	ImageIcon setYesButtonIcon = new ImageIcon(GAI.images_path + "refresh.gif");
	ImageIcon setNoButtonIcon = new ImageIcon(GAI.images_path + "exit.gif");
	
	fset_yes_button = new JButton("Yes", setYesButtonIcon);
	fset_yes_button.addActionListener(fsybl);
	fset_yes_button.setMnemonic(KeyEvent.VK_S);
	fset_yes_button.setToolTipText("Click this button to Set up the Tomcat/MYSQL server");
	fset_yes_button.setEnabled(false);
	fset_no_button = new JButton("No", setNoButtonIcon);
	fset_no_button.addActionListener(fsnbl);
	fset_no_button.setEnabled(false);
	fset_no_button.setMnemonic(KeyEvent.VK_N);
	fset_no_button.setToolTipText("Click this button to skip the First time Tomcat/MYSQL server setting");
	
	//First time running setting buttons
	fsybl = null;
	fsnbl = null;
	display();
    }

    /** Any implementation of ActionListener requires this method so that when an 
     **action is performed the appropriate effect can occur.*/
    public void actionPerformed(ActionEvent event) {
    }

    /** This method is callsed to actually layout the components.*/
    public void display() {
	//KeyListenerImpl key_listener = new KeyListenerImpl();
	//MouseListenerImpl mouse_listener = new MouseListenerImpl();
	//this.addKeyListener(key_listener);

	first_setting_message = new JTextArea();
	first_setting_message.setEditable(false);
	first_setting_message.setLineWrap(true);
	
	first_setting_message.setText("If you are about the first time to run GAI, you can set up " +
				      "your own TOMCAT/MYSQL server. Otherwise, your GSDL3 " +
				      "will be installed with the TOMCAT/MYSQL setting come " +
				      "with the package. You may change the setting later on.");

	first_setting_message.setFont(new Font("Arial", Font.BOLD, 14));
	
	setting_message_pane = new JScrollPane(first_setting_message);
	
	// The pane to store setting_message_pane
	first_setting_pane = new JPanel();
	first_setting_pane.setBorder(BorderFactory.createEmptyBorder(5,5,20,5));
	first_setting_pane.setLayout (new BorderLayout());
	first_setting_pane.add(setting_message_pane, BorderLayout.CENTER);

	// Button Control Layout 
	fset_yes_button.setEnabled(true);
	fset_no_button.setEnabled(true);

	button_pane.setLayout (new GridLayout(1,2));
	button_pane.add(fset_yes_button);
	button_pane.add(fset_no_button);

	control_pane = new JPanel();
	control_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	control_pane.setLayout(new BorderLayout());
	control_pane.add(new JLabel("Do you want to set up your own running Tomcat/Mysql Servers?"), BorderLayout.CENTER);
	control_pane.add(button_pane, BorderLayout.SOUTH);
			 
	main_pane = (JPanel) getContentPane();
	//main_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	main_pane.setLayout (new BorderLayout());
	main_pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	main_pane.add(first_setting_pane,BorderLayout.CENTER);
	main_pane.add(control_pane, BorderLayout.SOUTH);
	main_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	main_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));

	// Center and display
	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screen_size.width - SIZE.width) / 2,
		    (screen_size.height - SIZE.height) / 2);
	setVisible(true);
    }
    
    public boolean checkSettingConfirm(){
	return setting_confirm;
    }

    public void destroy() {
    }
        
    /** This class serves as the listener for actions on the build button.*/
    private class FirstSetYesButtonListener
	implements ActionListener {
	/** If you want to give up the change you have made to the build properties 
	 *  file before you save the change,  This button is to reload the log file 
	 *  whenever user want to */
	public void actionPerformed(ActionEvent event) {
	    SetServerPane set_server_pane = new SetServerPane ();
	    set_server_pane.display();
	    //disable the GAIFirstSettingFrame
	    setVisible (false);
	    setting_confirm = true;
	}
    }
    private class FirstSetNoButtonListener
	implements ActionListener {
	// Exit the Adminstration tool 
	public void actionPerformed(ActionEvent event) {
	    setting_confirm = false;
	    setVisible (false);
	    //return setting_confirm;
	    if (!setting_confirm) {
		GAIManager gai = new GAIManager(screen_size);
	    }
	}
    }
}
       

