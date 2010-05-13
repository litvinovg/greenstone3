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
 * Date: 05.2005
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
package org.greenstone.admin.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.Object;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.event.MouseEvent;
import java.net.*;
import java.sql.*;

import org.greenstone.util.Configuration;
import org.greenstone.core.ParsingProgress;
import org.greenstone.admin.GAI;


/** The initial setting pane is to request the user to input the preferred 
 * Tomcat and Mysql server,they would like to run GSDL3 with
 *  @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *  @version
 */
public class SetServerPane
    extends JFrame
    implements ActionListener {

    /* This pane is to allow the user to set up their own running Tomcat/Mysql server 
     * before the installation of the GSDL3*/
    // protected JScrollPane setting_message_pane = null;
    protected JPanel main_pane = null;
    protected JPanel button_pane = null;
    protected JPanel outter_control_pane = null;
    protected JScrollPane setting_table_pane = null;
    protected JTable server_setting_table = null;
    
    private JButton save_button = null;
    private JButton install_button = null;
    private JButton exit_button = null;

    ServerSettingTableModel server_setting_table_model = null;
    private boolean setting_confirm = false;
    private ArrayList conf_array;
    private boolean success = false;
    private boolean file_saved = true;
    
    /** The various sizes for the screen layout*/
    static private Dimension MIN_SIZE = new Dimension( 90,  90);
    static private Dimension LIST_SIZE  = new Dimension(200, 450);
    static private Dimension DIALOG_SIZE = new Dimension (400,300);
    static private Dimension FRAME_SIZE = new Dimension (500, 450);
    static private Dimension TABLE_SIZE = new Dimension(500,200);
    static final Dimension SIZE = new Dimension(400,400);
    
    //Constructor
    public SetServerPane() {
	super();
	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	this.setSize(FRAME_SIZE);
	this.setTitle("Setting Tomcat/MYSQL Servers");
	
	// create all the control button panes
	button_pane  = new JPanel();

	//First time running button
	SaveButtonListener ybl = new SaveButtonListener();
	InstallButtonListener nbl = new InstallButtonListener();
	ExitButtonListener ebl = new ExitButtonListener();
	
	// ImagesIcon for the buttons
	ImageIcon SaveButtonIcon = new ImageIcon(GAI.images_path + "refresh.gif");
	ImageIcon InstallButtonIcon = new ImageIcon(GAI.images_path + "exit.gif");
	ImageIcon ExitButtonIcon = new ImageIcon(GAI.images_path + "exit.gif");

	save_button = new JButton("Save Setting", SaveButtonIcon);
	save_button.addActionListener(ybl);
	save_button.setMnemonic(KeyEvent.VK_S);
	save_button.setToolTipText("Click this button to save up the setting");
	save_button.setEnabled(false);

	install_button = new JButton("Install GSDL3", InstallButtonIcon);
	install_button.addActionListener(nbl);
	install_button.setEnabled(false);
	install_button.setMnemonic(KeyEvent.VK_I);
	install_button.setToolTipText("Click this button to Install Greenstone III");

	exit_button = new JButton("Exit Setting and Installation", ExitButtonIcon);
	exit_button.addActionListener(ebl);
	exit_button.setEnabled(false);
	install_button.setMnemonic(KeyEvent.VK_I);
	install_button.setToolTipText("Click this button to Exit the Setting and Installation of Greenstone III");

	//Setting&Installation buttons
	ybl = null;
	nbl = null;
	ebl = null;
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
	
	server_setting_table_model = new ServerSettingTableModel();
	server_setting_table = new JTable(server_setting_table_model) {
		protected String[] rowToolTips = {
		    "The name of the machine that Tomcat is/will be run on",
		    "The port number that Tomcat is/will be run on",
		    "The base directory for the existing Tomcat installation",
		    "The name of the machine that Mysql is/will be run on",
		    "The port number that Mysql is/will be run on",
		    "The base directory for the existing MYSQL installations",
		    "The user name for the administrator of using MySQL",
		    "The socket that Mysql is/will be run on"};
		
		public String getToolTipText(MouseEvent e){
		    String tip = null;
		    Point p = e.getPoint();
		    int rowIndex = rowAtPoint(p);
		    int colIndex = columnAtPoint(p);
		    int realColumnIndex = convertColumnIndexToModel(colIndex);
		    if (realColumnIndex == 0){
			tip = rowToolTips[rowIndex];
		    } else {
			tip = super.getToolTipText(e);
		    }
		    return tip;
		}
	    };
	
	//Set up tableHeader
	JTableHeader header = server_setting_table.getTableHeader();
	header.setFont(new Font("Arial", Font.BOLD, 14));
	
	server_setting_table.setRowHeight(30);
	setting_table_pane = new JScrollPane(server_setting_table);
	
	// Button Control Layout 
	save_button.setEnabled(true);
	install_button.setEnabled(true);
	exit_button.setEnabled(true);
	
	button_pane.setLayout (new GridLayout(1,3));
	button_pane.add(save_button);
	button_pane.add(install_button);
	button_pane.add(exit_button);

	outter_control_pane = new JPanel();
	outter_control_pane.setLayout (new BorderLayout());
	outter_control_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	outter_control_pane.setPreferredSize(new Dimension(50,50));
	outter_control_pane.setSize(new Dimension(50,50));
	outter_control_pane.add (button_pane, BorderLayout.CENTER);
	
	main_pane = (JPanel) getContentPane();
	main_pane.setLayout (new BorderLayout());
	main_pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	main_pane.add(setting_table_pane, BorderLayout.CENTER);
	main_pane.add(outter_control_pane, BorderLayout.SOUTH);
	main_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	main_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	
	// Center and display
	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screen_size.width - SIZE.width) / 2,
		    (screen_size.height - SIZE.height) / 2);
	setVisible(true);
    }
    
    public void destroy() {
    }
        
    /** This class serves as the listener for actions on the build button.*/
    private class SaveButtonListener
	implements ActionListener {
	/** This button is to save any change you have added
	 ** to the Tomcat/Mysql servers setting*/
	public void actionPerformed(ActionEvent event) {
	    setting_confirm = true;
	    int result = JOptionPane.showConfirmDialog((Component) null, "Do you really want to save the setting?", "Save Confirmation", JOptionPane.YES_NO_OPTION);
	    if ( result == JOptionPane.YES_OPTION) {
		updateSetting (GAI.build_properties_file);
		file_saved = writeFile (GAI.build_properties_file);
		if (file_saved) {
		    JOptionPane.showMessageDialog((Component) null,"Change has been saved succefully!");
		} else {
		    JOptionPane.showMessageDialog((Component) null,"Change has not been saved succefully!");
		    file_saved = false;
		} 
	    } else if (result == JOptionPane.NO_OPTION) {
		JOptionPane.showMessageDialog((Component) null,"Change has not been saved!");
		file_saved = false;
	    }
	}
    }

    public void updateSetting(File build_properties_file) {
	readFile (GAI.build_properties_file);
	String new_string;
	for (int i=0; i < server_setting_table_model.getRowCount(); i++){
	    //System.err.println("What is the value here:" + server_setting_table_model.getValueAt(i,0).toString());
	    if (!server_setting_table_model.getValueAt(i,1).toString().matches("^\\s*$")){
		for (int j=0; j < conf_array.size(); j++){
		    if (((String)conf_array.get(j)).startsWith(server_setting_table_model.getValueAt(i,0).toString().toLowerCase())){
			//System.err.println("What is the value in conf_array:" + conf_array.get(j));
			new_string = server_setting_table_model.getValueAt(i,0).toString()+"="+server_setting_table_model.getValueAt(i,1).toString();
			conf_array.set(j, new_string.toLowerCase());
		    }
		}
	    }
	}
	
    }
    public void readFile (File build_properties_file) {
	String filename = build_properties_file.getPath();
	String fileLine;
	/*conf_array[] store all the details from the build.properties*/
	conf_array = new ArrayList();
	try {
	    BufferedReader conf_in = new BufferedReader(new FileReader(filename));
	    while ((fileLine = conf_in.readLine()) != null) {
		// Besides an empty line, all the other lines will be stored in the conf_array
		if (!fileLine.matches("^\\s*$")) {
		    conf_array.add(fileLine);
		} 
	    }
	    conf_in.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public boolean writeFile(File build_properties_file){
	String filename = build_properties_file.getPath();
	try {
	    BufferedWriter conf_out = new BufferedWriter(new FileWriter(filename));
	    for (int j=0 ; j < conf_array.size(); j++){
		conf_out.write(conf_array.get(j).toString());
		conf_out.newLine();
	    }
	    success = true;
	    conf_out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    success = false;
	}
	return success;
    }

    private class InstallButtonListener
	implements ActionListener {
	// Exit the Adminstration tool 
	public void actionPerformed(ActionEvent event) {
	    setting_confirm = false;
	    /*if (!setting_confirm) {
	      GAIManager gai = new GAIManager("/research/chi/gsdl3-test/gsdl3", screen_size);
	      }*/
	}
    }
    
    private class ExitButtonListener
	implements ActionListener {
	// Exit the Adminstration tool 
	public void actionPerformed(ActionEvent event) {
	    if (!file_saved) {
		int result = JOptionPane.showConfirmDialog((Component) null, "The value of configuration has been changed,do you really want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
		if ( result == JOptionPane.YES_OPTION) {
		    System.exit(1);
		} else if (result == JOptionPane.NO_OPTION) {
		    JOptionPane.showMessageDialog((Component) null,"Press Save button to save the change!");
		}
	    } else {
		System.exit(1);
	    }	    
	}
    }
    
    class ServerSettingTableModel extends AbstractTableModel {
	String[] columnNames = {"Configuration Parameter",
				"Configuration Value"};
	Object [][] conf_display = {{"TOMCAT.SERVER", ""},
				    {"TOMCAT.PORT", ""},
				    {"TOMCAT.INSTALLED.PATH",""},
				    {"MYSQL.SERVER",""},
				    {"MYSQL.PORT",""},
				    {"MYSQL.INSTALLED.PATH",""},
				    {"MYSQL.ADMIN.USER",""},
				    {"MYSQL.SOCKET",""}};
	
	public int getColumnCount(){
	    return columnNames.length;
	}
	
	public int getRowCount() {
	    return conf_display.length;
	}
	
	public String getColumnName(int col){
	    return columnNames[col];
	}
	
	public Object getValueAt(int row,int col){
	    return conf_display[row][col];
	}
	
	public boolean isCellEditable(int row, int col){
	    if (col == 0){
		return false;
	    } else {
		return true;
	    }
	}

	public void setValueAt(Object value, int row, int col){
	    conf_display[row][col] = value;
	    fireTableCellUpdated(row,col);
	}
    }

}
       

