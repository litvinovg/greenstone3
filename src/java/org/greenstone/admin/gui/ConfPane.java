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
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.Object;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.event.MouseEvent;
import java.net.*;
import java.sql.*;

import org.greenstone.admin.GAI;
import org.greenstone.admin.GAIManager;
import org.greenstone.admin.gui.ThreadControl;
//import org.greenstone.admin.gui.ConfSettingTableModel;
import org.greenstone.admin.gui.SiteConfSetting;
import org.greenstone.core.ParsingProgress;

import org.greenstone.util.Configuration;
import org.greenstone.gsdl3.util.GSFile;

/** The Configuration pane is to view the status of relevant configure files in GSIII
 * @author Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 * @version
 */
public class ConfPane
    extends JPanel
    implements ActionListener {

    /* The pane to demonstrate Configuration information, including the build property file
     * being monitored and their content, and control functions */
    protected JSplitPane main_conf_pane = null;
    
    protected JPanel main_contentPane = null;
    public Hashtable conf_table;
    
    /** The panel that contains a Conf_list */
    private JPanel confList_pane =  null;
    
    /** The panel that contains the conf content. */
    private JPanel confContent_pane =  null;
    private JScrollPane conf_table_pane = null;
    
    /** The List showing all the Configuration files concerned. */
    private JList conf_list=null;
    private JTree conf_tree = null;

    /** The label at the top of the confList_pane. */
    private JLabel confList_label =  null;
    /** The label shown at the top of the confContent Pane. */
    private JLabel confContent_label  =  null;

    // The control buttons used to manipulate Configuration Pane
    protected JPanel inner_button_pane = null;
    protected JPanel inner_control_pane = null;
    protected JPanel tomcat_button_pane = null;
    protected JPanel mysql_button_pane = null;
    protected JPanel setting_shown_pane = null;
    
    /**Buttons in inner button pane */
    private JButton startup_tomcat_button = null;
    private JButton shutdown_tomcat_button = null;
    private JButton restart_tomcat_button = null;
    private JButton startup_mysql_button = null;
    private JButton shutdown_mysql_button = null;
    private JButton restart_mysql_button = null;

    private JButton save_button = null;

    private JLabel conf_label = null;
    private JLabel conf_setting_label = null;
    
    /* ConfPane empty AREA to show XML configuration file*/
    //private JTextArea conf_xml_area = null;

    //control tomcat and mysql server running status
    private String tomcat_server;
    private String tomcat_port;
    private String mysql_adminuser;
    private String mysql_server;
    private String mysql_port;
    private String conf_pane_name;
  
    private boolean tomcat_server_up = false;
    private boolean mysql_server_up = false;

    /** The scrollable area into which the configuration content is placed. */
    //private JScrollPane conf_xml_content = null;
    
    public SiteConfSetting site_conf = null;
    public SiteConfSetting interface_conf = null;

    /** The various sizes for the screen layout*/
    static private Dimension MIN_SIZE = new Dimension( 90,  90);
    static private Dimension LIST_SIZE  = new Dimension(200, 450);
    static private Dimension CONTENT_SIZE = new Dimension (600,450);
    static private Dimension TABLE_SIZE = new Dimension(500,200);
    static private int MINIMUM_TABLE_HEADER_SIZE = 15;
    
    private JTable conf_setting_table = null;

    private int num_of_setting;

    //The details display in the table
    int display_row_count = 5;
    int rowLength = 20;
    int colLength = 3;
    private Object[][] conf_display = new Object [rowLength][colLength];
    private Object[][] conf_setting;
    
    //An array to store all the details in the build.properties file
    private ArrayList conf_array;
    
    //Site configuration
    // TODO!! there are more than one site and interface - need to extend this
    public File site_conf_file = new File(GSFile.siteConfigFile(GSFile.siteHome(GAI.gsdl3_web_home, "localsite")));
    public File interface_conf_file = new File(GSFile.interfaceConfigFile(GSFile.interfaceHome(GAI.gsdl3_web_home, "default")));
    public boolean  project_conf_changed = false;

    ConfSettingTableModel conf_table_model = null;
    DefaultMutableTreeNode top = new DefaultMutableTreeNode("Configuration Files");
    //Constructor
    public ConfPane() {
	conf_table = new Hashtable();
	// create all the control button panes
	inner_control_pane = new JPanel();
	inner_button_pane = new JPanel();
	tomcat_button_pane = new JPanel();
	mysql_button_pane = new JPanel();
	
	/*the class for showing the site configurations including
	 * siteConfig.xml and interfaceConfig.xml
	 */
	site_conf = new SiteConfSetting("siteConfig", site_conf_file);
	interface_conf =  new SiteConfSetting("interfaceConfig", interface_conf_file);
	
	// Main Configuration Pane
	main_conf_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
       	// Main pane for Configuration Content Pane
	main_contentPane = new JPanel();
	
	//Create a tree for a list Configuration files we are interested
	//DefaultMutableTreeNode child1 = new DefaultMutableTreeNode();
	DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Project Configuration");

	DefaultMutableTreeNode compile = new DefaultMutableTreeNode("Compile");
	DefaultMutableTreeNode tomcat = new DefaultMutableTreeNode("Tomcat");
	DefaultMutableTreeNode proxy = new DefaultMutableTreeNode("Proxy");
	DefaultMutableTreeNode mysql = new DefaultMutableTreeNode("MySQL");	
	DefaultMutableTreeNode gsdl = new DefaultMutableTreeNode("GSDL");
	child1.add(compile);
	child1.add(tomcat);
	child1.add(proxy);
	child1.add(mysql);
	child1.add(gsdl);

	DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("Site Configuration");
	//DefaultMutableTreeNode child2 = new DefaultMutableTreeNode();	
	DefaultMutableTreeNode siteConf = new DefaultMutableTreeNode("SiteConfig");
	DefaultMutableTreeNode interfaceConf = new DefaultMutableTreeNode("InterfaceConfig");
	child2.add(siteConf);
	child2.add(interfaceConf);

	//DefaultMutableTreeNode child3 = new DefaultMutableTreeNode("Conf 3");
	top.add(child1);
	top.add(child2);
	//top.add(child3);
	conf_tree = new JTree(top);
	conf_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	conf_tree.addTreeSelectionListener(new ConfTreeListener());

	// inner button_pane Buttons
	StartupTomcatButtonListener stbl = new StartupTomcatButtonListener();
	ShutdownTomcatButtonListener stdbl = new ShutdownTomcatButtonListener();
	RestartTomcatButtonListener rtbl = new RestartTomcatButtonListener();

	StartupMysqlButtonListener smbl = new StartupMysqlButtonListener();
	ShutdownMysqlButtonListener smdbl = new ShutdownMysqlButtonListener();
	RestartMysqlButtonListener rmbl = new RestartMysqlButtonListener();
		
	ImageIcon startupTomcatButtonIcon = new ImageIcon(GAI.images_path + "redo.gif");
	ImageIcon shutdownTomcatButtonIcon = new ImageIcon(GAI.images_path + "stop.gif");
	ImageIcon restartTomcatButtonIcon = new ImageIcon(GAI.images_path + "stop.gif");
	
	ImageIcon startupMysqlButtonIcon = new ImageIcon(GAI.images_path + "redo.gif");
	ImageIcon shutdownMysqlButtonIcon = new ImageIcon(GAI.images_path + "stop.gif");
	ImageIcon restartMysqlButtonIcon = new ImageIcon(GAI.images_path + "stop.gif");
	ImageIcon saveButtonIcon = new ImageIcon(GAI.images_path  + "save.gif");

	startup_tomcat_button = new JButton();	
	startup_tomcat_button.addActionListener(stbl);
	startup_tomcat_button.setMnemonic(KeyEvent.VK_S);
	startup_tomcat_button.setEnabled(false);
	startup_tomcat_button.setText(GAI.dictionary.get("ConfPane.Tomcat_Startup"));
	startup_tomcat_button.setToolTipText(GAI.dictionary.get("ConfPane.Tomcat_Startup_Tooltip"));

	shutdown_tomcat_button = new JButton();
	shutdown_tomcat_button.addActionListener(stdbl);
	shutdown_tomcat_button.setEnabled(false);
	shutdown_tomcat_button.setText(GAI.dictionary.get("ConfPane.Tomcat_Shutdown"));
	shutdown_tomcat_button.setToolTipText(GAI.dictionary.get("ConfPane.Tomcat_Shutdown_Tooltip"));

	restart_tomcat_button = new JButton();
	restart_tomcat_button.addActionListener(rtbl);
	restart_tomcat_button.setEnabled(false);
	restart_tomcat_button.setText(GAI.dictionary.get("ConfPane.Tomcat_Restart"));
	restart_tomcat_button.setToolTipText(GAI.dictionary.get("ConfPane.Tomcat_Restart_Tooltip"));

	startup_mysql_button = new JButton();
	startup_mysql_button.addActionListener(smbl);
	startup_mysql_button.setMnemonic(KeyEvent.VK_S);
	startup_mysql_button.setEnabled(false);
	startup_mysql_button.setText(GAI.dictionary.get("ConfPane.MySQL_Startup"));
	startup_mysql_button.setToolTipText(GAI.dictionary.get("ConfPane.MySQL_Startup_Tooltip"));

	shutdown_mysql_button = new JButton();
	shutdown_mysql_button.addActionListener(smdbl);
	shutdown_mysql_button.setEnabled(false);
	shutdown_mysql_button.setText(GAI.dictionary.get("ConfPane.MySQL_Shutdown"));
	shutdown_mysql_button.setToolTipText(GAI.dictionary.get("ConfPane.MySQL_Shutdown_Tooltip"));
	
	restart_mysql_button = new JButton();
	restart_mysql_button.addActionListener(rmbl);
	restart_mysql_button.setEnabled(false);
	restart_mysql_button.setText(GAI.dictionary.get("ConfPane.MySQL_Restart"));
	restart_mysql_button.setToolTipText(GAI.dictionary.get("ConfPane.MySQL_Restart_Tooltip"));

	//tomcat control buttons
	stbl = null;
	stdbl = null;
	rtbl = null;
	
	//mysql control button
	smbl = null;
	smdbl = null;
	rmbl = null;
	
	//read in build properties
	getConfContent();
    }

    /** Any implementation of ActionListener requires this method so that when an 
     **action is performed the appropriate effect can occur.*/
    public void actionPerformed(ActionEvent event) {
    }
    
    
    /** This method is called to actually layout the components.*/
    public void display() {
	//Create Components.
	//KeyListenerImpl key_listener = new KeyListenerImpl();
	//MouseListenerImpl mouse_listener = new MouseListenerImpl();
	//this.addKeyListener(key_listener);
	
	//confList_Pane
	confList_pane = new JPanel();
	confList_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	confList_pane.setPreferredSize(LIST_SIZE);
	confList_pane.setSize(LIST_SIZE);
	confList_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	confList_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	
	confList_label = new JLabel();
	confList_label.setOpaque(true);
	confList_label.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	confList_label.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	confList_label.setText(GAI.dictionary.get("ConfPane.Conf_List"));

	// confContent_Pane
	confContent_pane = new JPanel();
	confContent_pane.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
	confContent_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	confContent_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));

	confContent_label = new JLabel();
	confContent_label.setOpaque(true);
	confContent_label.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	confContent_label.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	confContent_label.setText(GAI.dictionary.get("ConfPane.Conf_Content"));
		
	conf_table_model = new ConfSettingTableModel();
	conf_setting_table = new JTable(conf_table_model){
		public String getToolTipText(MouseEvent e){
		    String tip = null;
		    Point p = e.getPoint();
		    int rowIndex = rowAtPoint(p);
		    int colIndex = columnAtPoint(p);
		    int realColumnIndex = convertColumnIndexToModel(colIndex);
		    if (realColumnIndex == 0){
			tip = conf_display[rowIndex][realColumnIndex].toString();
		    } else {
			tip = super.getToolTipText(e);
		    }
		    return tip;
		}
		public Component prepareRenderer (TableCellRenderer renderer, 
						  int rowIndex, int colIndex){
		    Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
		    if (colIndex == 0) {
			c.setBackground(Configuration.getColor("coloring.table_noneditable_background"));
		    } else {
			c.setBackground(Configuration.getColor("coloring.table_editable_background"));
		    }
		    return c;
		}
	    };
	
	//When the MySQL and TOMCAT servers are still running and detect double-clicking occuring (editing mode)
	conf_setting_table.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e){
		    if (e.getClickCount() == 2){ 
			if (conf_pane_name.matches("MYSQL")){
			    mysql_server_up = false;
			    mysql_server_up = checkMysqlServer();
			    if (mysql_server_up) {
				JOptionPane.showMessageDialog((Component) null,"MySQL is running, shutdown the MySQL server before making any changes");
			    }
			}
			if (conf_pane_name.matches("TOMCAT")){
			    tomcat_server_up = false;
			    tomcat_server_up = checkTomcatServer();
			    if (tomcat_server_up) {
				JOptionPane.showMessageDialog((Component) null,"TOMCAT is running, shutdown the TOMCAT server before making any changes");
			    }
			}	
		    }
		}
	    });
	conf_setting_table.addKeyListener(new KeyAdapter(){
		public void keyPressed(KeyEvent ke){
		    if (ke.getKeyCode() == KeyEvent.VK_F2){ 
			if (conf_pane_name.matches("MYSQL")){
			    mysql_server_up = checkMysqlServer();
			    if (mysql_server_up) {
				JOptionPane.showMessageDialog((Component) null,"MySQL is running, shutdown the MySQL server before making any changes");
			    }
			}
			if (conf_pane_name.matches("TOMCAT")){
			    tomcat_server_up = checkTomcatServer();
			    if (tomcat_server_up) {
				JOptionPane.showMessageDialog((Component) null,"TOMCAT is running, shutdown the TOMCAT server before making any changes");
			    }
			}
		    }
		}
	    });
	
	//Set up tableHeader
	JTableHeader table_header = conf_setting_table.getTableHeader();
	Dimension table_header_preferred_size = table_header.getPreferredSize();
	if (table_header_preferred_size.height < MINIMUM_TABLE_HEADER_SIZE) {
	    table_header_preferred_size.setSize(table_header_preferred_size.width, MINIMUM_TABLE_HEADER_SIZE);
	    table_header.setPreferredSize(table_header_preferred_size);
	}

	table_header.setFont(new Font("Arial", Font.BOLD, 13));
	conf_setting_table.setRowHeight(30);
	conf_table_pane = new JScrollPane(conf_setting_table);
	conf_table_pane.setVisible(false);
	conf_table_pane.getViewport().setBackground(Configuration.getColor("coloring.collection_tree_background"));
	
	// Layout Components
	confList_pane.setLayout(new BorderLayout());
	JScrollPane conf_treeView = new JScrollPane(conf_tree);
	confList_pane.add(conf_treeView, BorderLayout.CENTER);
	confList_pane.add(confList_label, BorderLayout.NORTH);
	confContent_pane.setLayout(new BorderLayout());
	confContent_pane.add(conf_table_pane, BorderLayout.CENTER);
	
	inner_button_pane.setLayout(new GridLayout(1,2));
	inner_control_pane.setLayout (new BorderLayout());
	inner_control_pane.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	inner_control_pane.setPreferredSize(new Dimension(50,50));
	inner_control_pane.setSize(new Dimension(50,50));
	inner_control_pane.add (inner_button_pane, BorderLayout.CENTER);
	
	main_contentPane.setLayout (new BorderLayout());
	main_contentPane.add(confContent_label, BorderLayout.NORTH);
	main_contentPane.add(confContent_pane, BorderLayout.CENTER);
	main_contentPane.add(inner_control_pane, BorderLayout.SOUTH);
	main_conf_pane.add(confList_pane, JSplitPane.LEFT);
	main_conf_pane.add(main_contentPane, JSplitPane.RIGHT);
	main_conf_pane.setDividerLocation(LIST_SIZE.width - 10);
	
	this.setLayout(new BorderLayout());
	this.add(main_conf_pane, BorderLayout.CENTER);
    }

    public void save() {
	if (project_conf_changed) {
	    saveProjectConf(GAI.build_properties_file);
	}
	if (site_conf.confChanged()){
	    site_conf.saveFile(site_conf_file);
	}
	if (interface_conf.confChanged()) {
	    interface_conf.saveFile(interface_conf_file);
	}
	

    }
    
    public boolean configurationChanged() {
	return (project_conf_changed || site_conf.confChanged() ||
		interface_conf.confChanged());

    }

    private class StartupTomcatButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    /*before startup Tomcat server, we want to make sure project and Site configuration 
	     * files are saved*/
	    if (project_conf_changed) {
		saveProjectConf(GAI.build_properties_file);
	    }
	    /*boolean site_conf_changed = site_conf.confChanged();
	    boolean interface_conf_changed=interface_conf.confChanged();
	    if (site_conf_changed) {
		site_conf.saveFile(site_conf_file);
	    }
	    if (interface_conf_changed) {
		interface_conf.saveFile(interface_conf_file);
		}*/
	    tomcat_server_up = checkTomcatServer();
	    if (!tomcat_server_up) {
		ThreadControl threadControl = new ThreadControl();
		Runnable startupTomcatThread = threadControl.new startupTomcatServer("start-tomcat");
		new Thread(startupTomcatThread).start();
		threadControl.destroy();
		tomcat_server_up = true;
		JOptionPane.showMessageDialog((Component) null,"Tomcat server has been Startup successfully!");  
	    } else {
		JOptionPane.showMessageDialog((Component) null,"Tomcat server is running");
	    }
	    changeTomcatButtonPane1();
	}
    }
    
    private class ShutdownTomcatButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    tomcat_server_up = checkTomcatServer();
	    if (tomcat_server_up){
		ThreadControl threadControl = new ThreadControl();
		Runnable shutdownTomcatThread = threadControl.new shutdownTomcatServer("stop-tomcat");
		new Thread(shutdownTomcatThread).start();
		threadControl.destroy();
		tomcat_server_up = false;
		JOptionPane.showMessageDialog((Component) null, "Tomcat server has been shutted down !");
	    } else {
		JOptionPane.showMessageDialog((Component) null,"Tomcat server was not running!");
	    }
	    changeTomcatButtonPane2();
	}
    }
    
    private class RestartTomcatButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    //before we restart Tomcat server, we want to make sure the conf_pane settings are saved
	    if (project_conf_changed) {
		saveProjectConf(GAI.build_properties_file);
	    }
	    /*boolean site_conf_changed = site_conf.confChanged();
	    boolean interface_conf_changed=interface_conf.confChanged();
	    if (site_conf_changed) {
		site_conf.saveFile(site_conf_file);
	    }
	    if (interface_conf_changed) {
		interface_conf.saveFile(interface_conf_file);
		}*/
	    tomcat_server_up = checkTomcatServer();
	    if (tomcat_server_up) {
		ThreadControl threadControl = new ThreadControl();
		Runnable restartTomcatThread = threadControl.new restartTomcatServer("restart-tomcat");
 		new Thread(restartTomcatThread).start();
		threadControl.destroy();
		tomcat_server_up = true;
		JOptionPane.showMessageDialog((Component) null,"Tomcat server has been Restarted successfully!"); 
	    } else {
		JOptionPane.showMessageDialog((Component) null,"Tomcat server was not running!"); 
	    }
	}
    }
    
    private class StartupMysqlButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    //before we startup the MySQL server, we want to make sure the Conf_pane settings are saved
	    saveProjectConf(GAI.build_properties_file);
	    mysql_server_up = checkMysqlServer();
	    if (!mysql_server_up) {
		ThreadControl threadControl = new ThreadControl();
		Runnable startupMysqlThread = threadControl.new startupMysqlServer("start-mysql");
		new Thread(startupMysqlThread).start();
		threadControl.destroy();
		mysql_server_up = true;
		JOptionPane.showMessageDialog((Component) null,"MYSQL server has been Startup successfully!");  
	    } else {
		JOptionPane.showMessageDialog((Component) null,"MYSQL server has been running");  	
	    }
	    changeMysqlButtonPane1();
	}
    }  
    
    private class ShutdownMysqlButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    mysql_server_up = checkMysqlServer();
	    if (mysql_server_up){
		ThreadControl threadControl = new ThreadControl();
		Runnable shutdownMysqlThread = threadControl.new shutdownMysqlServer("stop-mysql");
		new Thread(shutdownMysqlThread).start();
		threadControl.destroy();
		JOptionPane.showMessageDialog((Component) null,"MYSQL server has been Shutdown successfully !");
		mysql_server_up = false;
	    } else {
		JOptionPane.showMessageDialog((Component) null,"MYSQL server was not running!");
	    }
	    changeMysqlButtonPane2();
	}
    }
    
    private class RestartMysqlButtonListener
	implements ActionListener {
	public void actionPerformed(ActionEvent event) {
	    saveProjectConf(GAI.build_properties_file);
	    mysql_server_up = checkMysqlServer();
	    if (mysql_server_up) {
		ThreadControl threadControl = new ThreadControl();
		Runnable restartMysqlThread = threadControl.new restartMysqlServer("restart-mysql");
		new Thread(restartMysqlThread).start();
		threadControl.destroy();
		JOptionPane.showMessageDialog((Component) null,"MYSQL server has been Restarted successfully!");  
		mysql_server_up = true;
	    } else { 
		JOptionPane.showMessageDialog((Component) null,"MYSQL server was not running!"); 
	    }
	}
    }

    // Save the configuration file globally,when click File->Save or restart (or startup) Tomcat and MySQl server
    public void saveProjectConf(File file){
    	String new_string;
	Enumeration keys = conf_table.keys();
	String key;
	String value;
	String filename = file.getPath();
	while (keys.hasMoreElements()) {
	    key = (String) keys.nextElement();
	    value = (String) conf_table.get(key);
	    for (int j=0 ; j < conf_array.size(); j++){
		if (((String)conf_array.get(j)).startsWith(key.toLowerCase())){
		    new_string = key+"="+value; 
		    conf_array.set(j, new_string.toLowerCase());
		}
	    }
	}

	try {
	    BufferedWriter conf_out = new BufferedWriter(new FileWriter(filename));
	    for (int j=0 ; j < conf_array.size(); j++){
		conf_out.write(conf_array.get(j).toString());
		conf_out.newLine();
	    }
	    conf_out.close();
	    getConfContent();
	    project_conf_changed = false;
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public boolean projectConfChanged() {
	return project_conf_changed;
    }

    public void getConfContent(){
	String filename = GAI.build_properties_file.getPath();
	if (!GAI.build_properties_file.exists()){ 
	    JOptionPane.showMessageDialog((Component) null,"Build property file does not exist");  
	} else {
	    readProjectConf(filename);
	}
    }
    
    /* Read build.properties file 
     * @param
     */
    public void readProjectConf (String filename) {
	String fileLine;
	/*inside the array will store, the conf_setting[i][0]:Comment, 
	 * conf_setting[i][1]:Para and conf_setting[i][2]:Value
	 * conf_array[] store all the details from the build.properties*/
	conf_array = new ArrayList();
	conf_setting = new Object [rowLength][colLength];
	try {
	    BufferedReader conf_in = new BufferedReader(new FileReader(filename));
	    int i = 0;
	    while ((fileLine = conf_in.readLine()) != null) {
		// This is an empty line
		if (fileLine.matches("^\\s*$")) {
		    // Do Nothing
		} else if (fileLine.matches("##.+")){
		    //This line shows the specific service for these conf setting
		    conf_array.add(fileLine);
		} else 	if (fileLine.matches("#.+")){
		    //This line is Configuration Comment line
		    conf_array.add(fileLine);
		    conf_setting[i][0] = fileLine.substring(1); // use 1 to get rid of the # symbol.
		} else if (!fileLine.matches("#.+") && !fileLine.matches("^\\s*$") ){
		    //This line is Setting line
		    int end_index = fileLine.indexOf("=");
		    conf_setting[i][1] = fileLine.substring(0,end_index).toUpperCase();
		    conf_setting[i][2] = fileLine.substring(end_index+1);
		    conf_table.put(conf_setting[i][1], conf_setting[i][2]);
		    i++; //calculat the number of settings
		    conf_array.add(fileLine);
		} else {
		    // Wrong character in the line
		}
	    }
	    num_of_setting = i;
	    for (int j=0; j<num_of_setting; j++){
		if (conf_setting[j][1].toString().matches("TOMCAT.SERVER")){
		    tomcat_server = conf_setting[j][2].toString();
		}
		if (conf_setting[j][1].toString().matches("TOMCAT.PORT")){
		    tomcat_port = conf_setting[j][2].toString();
		}
		if (conf_setting[j][1].toString().matches("MYSQL.ADMIN.USER")){
		    mysql_adminuser = conf_setting[j][2].toString();
		}
		if (conf_setting[j][1].toString().matches("MYSQL.SERVER")){
		    mysql_server = conf_setting[j][2].toString();
		}
		if (conf_setting[j][1].toString().matches("MYSQL.PORT")){
		    mysql_port = conf_setting[j][2].toString();
		}	
	    }
	    conf_in.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    

    public void clearTable (){
	for (int i=0; i< rowLength; i++){
	    conf_display[i][0] = "";
	    conf_display[i][1] = "";
	    conf_display[i][2] = "";
	}
    }

    private boolean checkTomcatServer (){
	try {
	    if (tomcat_server != "" && tomcat_port != ""){
		String http_string = "http://" + tomcat_server+":"+tomcat_port+"/";
		URL tomcatURL = new URL(http_string);
		HttpURLConnection tomcatConn = (HttpURLConnection) tomcatURL.openConnection();
		tomcatConn.connect();
		//necessary to get a Response, even if you don't care about contents
		InputStream connIn = tomcatConn.getInputStream(); 
		int pageStatus = tomcatConn.getResponseCode();
		if (pageStatus == HttpURLConnection.HTTP_NOT_FOUND) {//404 error
		    //Tomcat is not running
		    tomcat_server_up = false;
		} else {
		    tomcat_server_up = true;
		}  
	    } else {
		System.err.println("Either Tomcat server or Tomcat port was not set properly");
	    }
	} catch (Exception ex) {
	    tomcat_server_up = false;
	    System.err.println(ex.getMessage());
	}
	return tomcat_server_up;
    }
    
    private boolean checkMysqlServer(){
	Connection conn = null;
	try {
	    String mysql_userName ="gsdl3reader";
	    String mysql_password = ""; 
	    String mysql_url ="jdbc:mysql://"+mysql_server+":"+mysql_port+"/localsite_gs3mgdemo";
	    //System.err.println("what is Mysql_URL:" + mysql_url);
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(mysql_url, mysql_userName, mysql_password);
	    if (conn == null) {
		mysql_server_up =false;
	    } else {
		mysql_server_up = true;
	    }
	} catch (Exception e) {
	    mysql_server_up = false;
	    System.err.println("Cannot connect to database server");
	}
	return mysql_server_up;
    }
    
    public class ConfSettingTableModel 
	extends AbstractTableModel
	{
	String[] columnNames = {"Configuration Parameter",
	"Configuration Value"};
	
	public int getColumnCount(){
	return columnNames.length;
	}
	
	public int getRowCount() {
	return display_row_count;
	}
	
	public String getColumnName(int col){
	    
	    return columnNames[col];
	}
	
	public Object getValueAt(int row,int col){
	    return conf_display[row][col+1];
	}
	
	public boolean isCellEditable(int row, int col){
	    if (col==0){
		return false;
	    } else if (conf_display[row][col].toString().matches("MYSQL.+")){
		mysql_server_up = checkMysqlServer();
		if (mysql_server_up) {
		    return false;
		}
	    } else if (conf_display[row][col].toString().matches("TOMCAT.+")){
	   	tomcat_server_up = checkTomcatServer();
		if (tomcat_server_up) {
		   return false;
		}
	    }
	    return true;
	}
	
	public void setValueAt(Object value, int row, int col){
	    if (!isCellEditable(row, col))
		return;
	    conf_display[row][col+1] = value;
	    project_conf_changed = true;
	    //System.err.println("**ConfDisplay:" + conf_display[row][col].toString());
	    conf_table.put(conf_display[row][col].toString(), value);
	    fireTableCellUpdated(row,col+1);
	    updateUI();
	}
    }

    public int showProjectConf(String conf_param) {
	conf_table_pane.setVisible(true);
	clearPrevConfPane();
	confContent_pane.add(conf_table_pane, BorderLayout.CENTER);
	confContent_pane.revalidate();	
	clearTable();
	int j=0;
	int row_count = 0;
	for (int i=0; i< num_of_setting; i++){
	    if (conf_setting[i][1].toString().matches("^("+ conf_param +").+")) {
		row_count = row_count +1;
		conf_display[j][0] = conf_setting[i][0];
	    	conf_display[j][1] = conf_setting[i][1];
	   	conf_display[j][2] = conf_table.get(conf_setting[i][1]);
		j++;
	    }
	}
	return row_count;
    }
      
    public void displaySiteConf(SiteConfSetting site_conf_tmp){
	clearPrevConfPane();
	confContent_pane.add(site_conf_tmp, BorderLayout.CENTER);
	try {
	    site_conf_tmp.showText();
	    site_conf_tmp.goTop();
	    confContent_pane.revalidate();
	} catch (Exception e) {
	    e.printStackTrace();
	}    
     }
    
    public void clearPrevConfPane() {
	confContent_pane.remove(conf_table_pane);
	confContent_pane.remove(site_conf);
	confContent_pane.remove(interface_conf);
    }
	
   
    private class ConfTreeListener implements TreeSelectionListener{
	public void valueChanged (TreeSelectionEvent e){
	    String option = conf_tree.getLastSelectedPathComponent().toString();
	    conf_table_pane.setVisible(true);
	    if (option == "Compile"){
		conf_pane_name = "COMPILE";
		display_row_count = showProjectConf(conf_pane_name);
		inner_button_pane.removeAll();
		inner_button_pane.setLayout(new GridLayout(1,3));
	    } else if (option == "Tomcat"){
		conf_pane_name ="TOMCAT";
		display_row_count = showProjectConf(conf_pane_name);
		//Setup Tomcat button control pane
		inner_button_pane.removeAll();
		inner_button_pane.setLayout(new GridLayout(1,3));
		if (checkTomcatServer()) {
		    changeTomcatButtonPane1();
		} else {
		    changeTomcatButtonPane2();
		}
	    } else if (option =="Proxy"){
		conf_pane_name="PROXY";
		display_row_count = showProjectConf(conf_pane_name);
		//Setup Proxy button control pane
		inner_button_pane.removeAll();
		inner_button_pane.setLayout(new GridLayout(1,3));
	    } else if (option == "MySQL"){
		conf_pane_name="MYSQL";
		display_row_count = showProjectConf(conf_pane_name);
		// Setup MySQL button control pane
		inner_button_pane.removeAll();
		inner_button_pane.setLayout(new GridLayout(1,3));
		if (checkMysqlServer()) {
		    changeMysqlButtonPane1();
		} else {
		    changeMysqlButtonPane2();
		}
	    } else if (option == "GSDL"){
		conf_pane_name ="GSDL";
		display_row_count = showProjectConf(conf_pane_name);
		inner_button_pane.removeAll();
	    } else if (option == "SiteConfig"){
		conf_pane_name = "SiteConfig";
		inner_button_pane.removeAll();
		displaySiteConf(site_conf);
	    } else if (option == "InterfaceConfig") {
		conf_pane_name = "InterfaceConfig";
		inner_button_pane.removeAll();
		displaySiteConf(interface_conf);
	    }
	    updateUI();
	}
    }

    public void modeChanged (int mode){
	return;
    }

    public void gainFocus() {
	return;
    }

    public void changeTomcatButtonPane1 (){
	inner_button_pane.removeAll();
	inner_button_pane.setLayout(new GridLayout(1,2));
	inner_button_pane.add(restart_tomcat_button);
	inner_button_pane.add(shutdown_tomcat_button);
	restart_tomcat_button.setEnabled (true);
	shutdown_tomcat_button.setEnabled (true);
	updateUI();
    }
    public void changeTomcatButtonPane2 (){
	inner_button_pane.removeAll();
	inner_button_pane.setLayout(new GridLayout(1,2));
	inner_button_pane.add(startup_tomcat_button);
	inner_button_pane.add(shutdown_tomcat_button);
	startup_tomcat_button.setEnabled (true);
	shutdown_tomcat_button.setEnabled (false);
	updateUI();
    }	
    public void changeMysqlButtonPane1 (){
	inner_button_pane.removeAll();
	inner_button_pane.setLayout(new GridLayout(1,2));
	inner_button_pane.add(restart_mysql_button);
	inner_button_pane.add(shutdown_mysql_button);
	restart_mysql_button.setEnabled (true);
	shutdown_mysql_button.setEnabled (true);
	updateUI();
    }
    public void changeMysqlButtonPane2 (){
	inner_button_pane.removeAll();
	inner_button_pane.setLayout(new GridLayout(1,2));
	inner_button_pane.add(startup_mysql_button);
	inner_button_pane.add(shutdown_mysql_button);
	startup_mysql_button.setEnabled (true);
	shutdown_mysql_button.setEnabled (false);
	updateUI();
    }	
    public void afterDisplay(){
	return;
    }
} 

