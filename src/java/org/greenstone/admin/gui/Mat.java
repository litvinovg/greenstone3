package org.greenstone.admin.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.ProjectHelper;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GlobalProperties;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


public class Mat extends adminUI_Pane{

    JButton start_button;
    JButton enable_disable_button;
    JButton option_button;
    JButton description_control_button;
    JButton control_description_button;
    JPanel description_control_button_pane;
    
    JPanel control_content_pane;
    JPanel control_button_pane;
    JPanel control_description_pane;
    
    JLabel install_path;
    JLabel oai_temp_directory;
    JTextField install_path_text;
    JTextField oai_temp_directory_text;
    JCheckBox web_tool_checkbox;
    JCheckBox java_tool_checkbox;
    SimpleDateFormat sdf;
		
    private GlobalProperties globalProperty = null;
    private final String fileSeparator = File.separator;
    private GSPath gspath = null;
    
    static final int left_margin = 7;
    static final int right_margin = 7;
    static final int HORIZONTAL_GAP = 5;
    static final int VERTICAL_GAP = 5;
    
    String install_options_filePath = null;
    static String extension_list_path;
    static String extension_buildProperty_path;
    static String extension_runtimeProperty_path;
    static String extension_log_path;
    static String extension_log_directory;
    static String extension_path;
    static String os_type;
    static String oai_folder;
    static String web_xml_path;
    static final String InstallCompleteMsg = "The extension (Mat) has been installed successfully.";
    static final String InstallErrorMsg =  "Sorry, the extension (Mat) could not be installed. Please try again!";
    
    static final String DownloadCompleteMsg = "The extension (Mat) has been downloaded successfully.";
    static final String DownloadErrorMsg =  "Sorry, the extension (Mat) could not be downloaded. Please try again!";
    
    public Mat(){}
    
    public Mat (String extensionName){

	group = new String();		
	description = new String();
	download_type = new String();
	
	extension_name = extensionName;
	url = new String();
	configurable = new Boolean(false);
	
	os_type = System.getProperty("os.name");
	
	String gsdl3Home = get_GSDL3HOME();
	
	extension_list_path = gsdl3Home+fileSeparator+"ext"+fileSeparator+"extension_project_list.xml";
	extension_buildProperty_path = gsdl3Home+fileSeparator+"ext"+fileSeparator+"mat"+fileSeparator+"build.properties";
	extension_runtimeProperty_path = gsdl3Home+fileSeparator+"ext"+fileSeparator+"mat"+fileSeparator+"properties.xml";
	extension_log_path = gsdl3Home+fileSeparator+"ext"+fileSeparator+"logs"+fileSeparator+"mat"+fileSeparator+"extension_log.xml";
	extension_log_directory = gsdl3Home+fileSeparator+"ext"+fileSeparator+"logs"+fileSeparator+"mat";
	extension_path = gsdl3Home+fileSeparator+"ext"+fileSeparator+"mat"+fileSeparator;
	oai_folder = gsdl3Home+fileSeparator+"ext"+fileSeparator+"mat"+fileSeparator+"tmp"+fileSeparator;
	web_xml_path = gsdl3Home+fileSeparator+"web"+fileSeparator+"WEB-INF"+fileSeparator+"web.xml";
	
	start_button = new JButton("Analyse");
	start_button.setPreferredSize(new Dimension(50,25));
	start_button.setMaximumSize(new Dimension(50,25));
	start_button.setName("Analyse");
	start_button.setEnabled(false);
	start_button.addActionListener(new aListener(this));
		
	enable_disable_button = new JButton("Enable");
	enable_disable_button.setName("enable_disable");
	enable_disable_button.setVisible(false);
	enable_disable_button.addActionListener(new aListener(this));
	enable_disable_button.setPreferredSize(new Dimension(50,25));
	enable_disable_button.setMaximumSize(new Dimension(50,25));
	
	option_button = new JButton("Options");
	option_button.setName("options");
	option_button.addActionListener(new aListener(this));
	
	control_description_button = new JButton("Description");
	control_description_button.setName("control_description");
	control_description_button.addActionListener(new aListener(this));
	
	description_control_button = new JButton("Control Panel");
	description_control_button.setName("description_control");
	description_control_button.addActionListener(new aListener(this));
	description_control_button.setEnabled(getCommandStatus("install"));
	description_control_button_pane = new JPanel(new GridLayout(1,3));
	
	description_pane = new JScrollPane ();
	control_pane = new JPanel ();
	descriptionTextArea = new JTextArea();
	button_pane = new JPanel();

	oai_temp_directory = new JLabel();
	oai_temp_directory.setText("OAI temporary download directory:");
	oai_temp_directory_text = new JTextField();
	oai_temp_directory_text.setText(oai_folder);
	oai_temp_directory_text.setEditable(false);
	oai_temp_directory_text.setBackground(Color.WHITE);
	
	install_path = new JLabel();
	install_path.setText("Metadata Quality Tool is installed in:");
	install_path_text = new JTextField();
	install_path_text.setText(extension_path);
	install_path_text.setEditable(false);
	install_path_text.setBackground(Color.WHITE);
		
	web_tool_checkbox = new JCheckBox("Web OAI Collection Building");
	web_tool_checkbox.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	web_tool_checkbox.setVisible(false);
	java_tool_checkbox = new JCheckBox("Local Collection Analysis");
	java_tool_checkbox.addItemListener(new checkBoxStatus(this));
	java_tool_checkbox.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	
	GridLayout gridLayout3 = new GridLayout(3,3);
	gridLayout3.setVgap(VERTICAL_GAP);
	control_content_pane = new JPanel(gridLayout3);
	control_content_pane.setBorder(BorderFactory.createEmptyBorder(5, left_margin, 5, right_margin));
	
	GridLayout gridLayout2 = new GridLayout(1,2);
	gridLayout2.setHgap(HORIZONTAL_GAP);
	control_button_pane = new JPanel(gridLayout2);
	control_button_pane.setBorder(BorderFactory.createEmptyBorder(5, left_margin, 5, right_margin));
	
	GridLayout gridLayout1 = new GridLayout(2,2);
	gridLayout1.setVgap(VERTICAL_GAP);
	control_description_pane = new JPanel(gridLayout1);
	control_description_pane.setBorder(BorderFactory.createEmptyBorder(5, left_margin, 5, right_margin));
	
	control_pane = new JPanel();
	control_pane.setLayout(new BoxLayout(control_pane, BoxLayout.Y_AXIS));
	
	setup();
	setupControlPane();
	loadProjectInformation();


    };
    
    public JPanel getControlPane(){

	return control_pane;
    }

    public JPanel getButtonPane(){
	return button_pane;
	}
   
    public JPanel getDescriptionPane(){
	
	
	description_control_button_pane.add(Box.createRigidArea(new Dimension(18,5)));
	description_control_button_pane.add(description_control_button);
	description_control_button_pane.add(Box.createRigidArea(new Dimension(18,5)));
	
	main_pane = new JPanel(new BorderLayout());
	main_pane.add(description_pane, BorderLayout.CENTER);
	main_pane.add(description_control_button_pane, BorderLayout.SOUTH);
	return main_pane;
	
    }
	
    private void setupControlPane(){
	
	control_content_pane.add(java_tool_checkbox);
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	
	JPanel tempPane = new JPanel(new BorderLayout());
	tempPane.add(Box.createRigidArea(new Dimension(18,5)), BorderLayout.WEST);
	tempPane.add(start_button, BorderLayout.CENTER);
	control_content_pane.add(tempPane);
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));

	
	//control_content_pane.add(web_tool_checkbox);
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
/*
	JPanel tempPane2 = new JPanel(new BorderLayout());
	tempPane2.add(Box.createRigidArea(new Dimension(18,5)), BorderLayout.WEST);
	tempPane2.add(enable_disable_button, BorderLayout.CENTER);
	control_content_pane.add(tempPane2);
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
	control_content_pane.add(Box.createRigidArea(new Dimension(10,5)));
*/
	control_description_pane.add(install_path);
	control_description_pane.add(install_path_text);
	
	control_description_pane.add(oai_temp_directory);
	control_description_pane.add(oai_temp_directory_text);
	
	control_button_pane.add(control_description_button);
	control_button_pane.add(option_button);
	
	control_content_pane.setAlignmentX(Component.LEFT_ALIGNMENT);
	control_pane.add(control_content_pane);
	
	control_description_pane.setAlignmentX(Component.LEFT_ALIGNMENT);
	control_pane.add(control_description_pane);
	
	control_button_pane.setAlignmentX(Component.LEFT_ALIGNMENT);
	control_pane.add(control_button_pane);
	
    }
	
    protected Object[][] getConfigureContent(){
	
	return null;
    
    }
    	
    public Boolean isConfigurable(){
	
	return configurable ;
    
    }

    public String getDestinationLocation(){
	
	return destination_folder;
    }
    
    public String getDownloadLocation(){
	
	return url;
    
    }
	
    public String getDownloadType(){
	
	return download_type;
    
    }

    protected void loadProjectInformation(){
        
    	Element rootNode = getRootElement(extension_list_path);
	
    	NodeList projectNodeList = rootNode.getElementsByTagName(EXTENSION);
        
	for(int i = 0; i < projectNodeList.getLength(); i++){
	    
    	    Element projectNode = (Element)projectNodeList.item(i);
    	    NodeList nameNodeList = projectNode.getElementsByTagName(NAME);
    	    String name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    if(name.equalsIgnoreCase(extension_name)){	
		NodeList groupNodeList = projectNode.getElementsByTagName(GROUP);
		group = groupNodeList.item(0).getChildNodes().item(0).getNodeValue();
           	
           	
		NodeList stepList = projectNode.getElementsByTagName(STEP);
		GridLayout gl = new GridLayout(stepList.getLength(),1);
		gl.setVgap(5);
		button_pane.setLayout(gl);
		
		for (int y = 0; y< stepList.getLength(); y++){
		    
		    Node e = stepList.item(y);
		    Element stepElement = (Element) e;
		    JButton button = new JButton();
		    String command = stepElement.getAttribute("action");
		    button.setText(stepElement.getAttribute("label"));
		    button.setName(command);
				
		    button.addActionListener(new aListener(this));
		    boolean status = getCommandStatus(command);
		    button.setEnabled(status);

		    if(stepElement.getAttribute("action").equalsIgnoreCase(DOWNLOAD)){
		    	download_type = stepElement.getAttribute("method");
		    	url = stepElement.getElementsByTagName("source").item(0).getChildNodes().item(0).getNodeValue();
		    	//destination_folder = stepElement.getElementsByTagName("destination").item(0).getChildNodes().item(0).getNodeValue();
		    	destination_folder = extension_path;
		    }
		    
		    if(stepElement.getAttribute("action").equalsIgnoreCase("Install_option")){
		    	 //install_options_filePath = stepElement.getAttribute("filePath");
		    	 install_options_filePath = extension_path+"build.properties";
		    }
		    button_pane.add(button);
		}
	    }
	}
    }
    
    public boolean getCommandStatus (String command){

     	Element root = getRootElement(extension_list_path);

    	NodeList extensionList = root.getElementsByTagName("extension");

    	boolean s = false;

    	for(int i = 0; i < extensionList.getLength(); i++){
    		 		
    	    Element projectNode = (Element)extensionList.item(i);
    	    NodeList nameNodeList = projectNode.getElementsByTagName(NAME);
    	    String name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    if(name.equalsIgnoreCase(extension_name)){	
    		
		Element actionElement = (Element)extensionList.item(0);
		NodeList individual_extension_detail_list = actionElement.getElementsByTagName("sequence_list");
		Node sequenceListNode = individual_extension_detail_list.item(0);
		NodeList sequenceDetailList = sequenceListNode.getChildNodes();
		
		for(int j = 0; j < sequenceDetailList.getLength(); j++){
		    Node n = sequenceDetailList.item(j);
		    
		    if(n instanceof Element){
    			
			Element e = (Element)n;
			if(e.getAttribute("action").equalsIgnoreCase(command)){
    						String status = e.getAttribute("status");
    						
					Boolean bv = Boolean.valueOf(status);
					s = bv.booleanValue();
			}
		    }
		}
	    }
	}//
	return s;
    }

    public void updateButtonPane(){
    	
	String gsdl3Home = get_GSDL3HOME();
    	Element root = getRootElement(extension_list_path);
	NodeList extensionList = root.getElementsByTagName("extension");
    	    	
    	for(int a = 0; a < extensionList.getLength(); a++){
	    
    	    Element projectNode = (Element)extensionList.item(a);
    	    NodeList nameNodeList = projectNode.getElementsByTagName(NAME);
    	    String name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
    	    if(name.equalsIgnoreCase(extension_name)){	
    	    	//System.out.println(extension_name);
		NodeList actionList = projectNode.getElementsByTagName("sequence_list");
		//System.out.println(actionList.getLength());
		Node x  = actionList.item(0);
		NodeList stepList = x.getChildNodes();
		
		for(int i = 0; i<stepList.getLength(); i++){
		    Node n = stepList.item(i);
		    if(n instanceof Element){
			Element actionElement = (Element) n;
			
			String action = actionElement.getAttribute("action");
			boolean status = getCommandStatus(action);
				
			Component[] c = button_pane.getComponents();
			
		    	for(int j = 0; j<c.length; j++){
			    JButton button = (JButton)c[j];
			    if(button.getName().equalsIgnoreCase(action)){
				button.setEnabled(status);
				button_pane.updateUI();
			    }
		    	}
			}
		    
		}///
	    }
    	}
	
    }
    
    public void setCommandStatus(String command){
    	
    	String gsdl3Home = get_GSDL3HOME();
    	Element root = getRootElement(extension_list_path);
    	NodeList actionList = root.getElementsByTagName("sequence_list");
	
    	if(command.equalsIgnoreCase("download")){
	    
	    Node x  = actionList.item(0);
	    NodeList stepList = x.getChildNodes();
	    
	    for(int i = 0; i<stepList.getLength(); i++){
		Node n = stepList.item(i);
		if(n instanceof Element){
		    Element actionElement = (Element) n;
		    
		    String action = actionElement.getAttribute("action");
		    
		    if(!action.equalsIgnoreCase(command)){
    			
			String status = actionElement.getAttribute("status");
			 actionElement.setAttribute("status", "true");
		    }
    		}
	    }
	}
    	
    	else if(command.equalsIgnoreCase("uninstall")){
    	    Node x  = actionList.item(0);
    	    NodeList stepList = x.getChildNodes();
    	    
    	    for(int i = 0; i<stepList.getLength(); i++){
    		Node n = stepList.item(i);
    		if(n instanceof Element){
    		    Element actionElement = (Element) n;
    		    
    		    String action = actionElement.getAttribute("action");
    		    
    		    if(!action.equalsIgnoreCase("download")){
        			
    			String status = actionElement.getAttribute("status");
    			    actionElement.setAttribute("status", "false");
    		    }
        	}
    	    }
    	}
    	
	try{
	    TransformerFactory tf= TransformerFactory.newInstance();     
	    Transformer transformer= tf.newTransformer();     
	    DOMSource source= new DOMSource(root);     
	    transformer.setOutputProperty(OutputKeys.INDENT,"yes");     
	    
	    Writer pwx= new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(extension_list_path),"UTF-8"));     
	    StreamResult   result= new   StreamResult(pwx);     
	    transformer.transform(source,result);  
	    pwx.close();
	    
	    updateButtonPane();
    	}catch(Exception ex){
	    ex.printStackTrace();
    	}
    }
    
    public Object[][] getConfiguration(String filePath){
    	
    	try{
	    Properties prop = new Properties();
	    FileInputStream fis = new FileInputStream(filePath);
	    prop.load(fis);
	    Set s = prop.keySet();
	    Object[][] properties = new Object[prop.size()][2];
	    Iterator i = s.iterator();
	    int counter = 0;
	    
	    while(i.hasNext()){
        	String setting = (String)i.next();
        	String value = prop.getProperty(setting);
		
    		properties[counter][0] = setting;
    		properties[counter][1] = value;
    		counter++;
	    }
	    
	    return properties;
    	}catch(IOException ex){
	    ex.printStackTrace();
	    return new Object[0][0];
    	}
    }
}

class checkBoxStatus implements ItemListener{

    private Mat adaptee;
    
    public checkBoxStatus(Mat adaptee){
	
	this.adaptee = adaptee;
    }
    
    public void itemStateChanged(ItemEvent e) {
	
	JCheckBox source = (JCheckBox)e.getItemSelectable();
	
	if (source == adaptee.java_tool_checkbox) {
	    if(source.isSelected()){
		adaptee.start_button.setEnabled(true);
	    }
	    else{
		adaptee.start_button.setEnabled(false);
	    }
	} 
	else{
	}
    }
}

class aListener implements ActionListener {
    
    private Mat adaptee;
    Calendar cl;
    SimpleDateFormat sdf;
    private final String fileSeparator = File.separator;
    
    aListener(Mat adaptee) {
        
	this.adaptee = adaptee;
    	cl=Calendar.getInstance(); 
    	sdf = new SimpleDateFormat("dd/MMM/yyyy 'at' HH:mm:ss z 'GMT'Z");
    }
    
    public void actionPerformed(ActionEvent e) {
	
	JButton button = (JButton)e.getSource();
	
	if(button.getName().equalsIgnoreCase("Download")){
	    downloadExtension();
	    
	    String msg = createInfoMsg(button.getName());
	    String timestamp = getTimestamp();
	
	    //updateLOG("Download",msg , timestamp);
	    //adaptee.setCommandStatus("Download");
	}
	else if(button.getName().equalsIgnoreCase("control_description") || button.getName().equalsIgnoreCase("description_control")){
		ExtPane.updateExtensionContentPane();
	}
	else if(button.getName().equalsIgnoreCase("options")){
	    configure_option();
	}
	else if(button.getName().equalsIgnoreCase("Install")){
	    installExtension();
	    String msg = createInfoMsg(button.getName());
	    String timestamp = getTimestamp();
	    //updateLOG("Install", msg , timestamp);
	}
	else if(button.getName().equalsIgnoreCase("Install_option")){
	    //System.out.println("--------install options-------------");
	    install_configuration();
	}
	else if(button.getName().equalsIgnoreCase("Analyse")){
	    execute_application();
	}
	else if(button.getName().equalsIgnoreCase("Uninstall")){
	    String msg = createInfoMsg(button.getName());
	    String timestamp = getTimestamp();
 	   uninstallExtension();
	   adaptee.setCommandStatus("Uninstall");
	   updateLOG("Unistall", msg , timestamp);
	}
	else if(button.getName().equalsIgnoreCase("disable")){
	    String msg = createInfoMsg(button.getName());
	    String timestamp = getTimestamp();
	    updateLOG("Disable", msg , timestamp);
	}
	else if(button.getName().equalsIgnoreCase("enable")){
	    String msg = createInfoMsg(button.getName());
	    String timestamp = getTimestamp();
	    updateLOG("Enable", msg , timestamp);
	}
    }
    
    public void uninstallExtension(){
    	File temp = new File(adaptee.extension_path);
    	deleteDir(temp);
    }
    
    private static boolean deleteDir(File dir) {
        
    	if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
    	return dir.delete();
        } 


    public void downloadExtension(){
	MatSVNDownloadThread download = new MatSVNDownloadThread(adaptee, ExtPane.message_textarea ,adaptee.url, adaptee.destination_folder, adaptee.extension_name);
	download.start();
    }

    public void installExtension(){
	MatExtensionInstallation ei = new MatExtensionInstallation(adaptee);
	ei.start();
	adaptee.description_control_button.setEnabled(adaptee.getCommandStatus("install"));
    }
    
    public void install_configuration(){
	File buildProperty = new File (adaptee.install_options_filePath);
	if(!buildProperty.exists()) { return; }
	Object[][] temp = adaptee.getConfiguration(adaptee.install_options_filePath);
	MatExtensionConfigurePrompt ecp = new MatExtensionConfigurePrompt(temp,adaptee.install_options_filePath,adaptee.extension_name,"Configure Install Settings");
	ecp.display();
    }
    
    public void configure_option(){
	String gsdl3Home = adaptee.get_GSDL3HOME();
	File buildProperty = new File (adaptee.extension_runtimeProperty_path);
	if(!buildProperty.exists()) { return; }
	Object[][] temp = adaptee.getConfiguration(adaptee.extension_runtimeProperty_path);
	MatExtensionConfigurePrompt ecp = new MatExtensionConfigurePrompt(temp,adaptee.extension_runtimeProperty_path,adaptee.extension_name,"Configure Runtime Settings");
	ecp.display();
    }
    
    public void execute_application(){
	MatLaunchApplication app = new MatLaunchApplication(adaptee);
	app.start();
    }
	
    public String createInfoMsg(String step){
	
	String info = new String();
	
	if(step.equalsIgnoreCase(adminUI_Pane.DOWNLOAD)){
	    info = "The extension Mat has been downloaded to local folder";
	}
	else if (step.equalsIgnoreCase(adminUI_Pane.INSTALL)){
	    info = "The extension Mat has been installed";
	}
	else if (step.equalsIgnoreCase(adminUI_Pane.UNINSTALL)){
	    info = "The extension Mat has been uninstalled";
	}
	else if (step.equalsIgnoreCase(adminUI_Pane.ENABLE)){
	    info = "The OAI Web Building has been enabled";
	}
	else if (step.equalsIgnoreCase(adminUI_Pane.DISABLE)){
	    info = "The OAI Web Building has been disabled";
	}
	
	return info;
    }
    
    public String getTimestamp(){
	
	String timestamp = sdf.format(cl.getTime());
	return timestamp;
    }
    
    public void updateLOG(String type , String info, String timestamp){

    	Element root = null;
	
	try{
	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc = docBuilder.newDocument();
	    String gsdl3Home = adaptee.get_GSDL3HOME();
	    
	    File logFile = new File(adaptee.extension_log_path);
	    boolean fileExist = logFile.exists();
	    
	    File logDir = new File(adaptee.extension_log_directory);
	    if(logDir.mkdirs()){}

	    if(!fileExist){
	    	logFile.createNewFile();
	    	root = doc.createElement("log");
	    }
	    else{
		
		doc = docBuilder.parse (new File(adaptee.extension_log_path));
		root = doc.getDocumentElement();
	    }
	    
	    Element actElement = doc.createElement("action");
	    actElement.setAttribute("type", type);
	    Element timeElement = doc.createElement("time");
	    Element infoElement = doc.createElement("info");
	    Text text_info = doc.createTextNode(info);
	    infoElement.appendChild(text_info);
		    
	    Text text_time = doc.createTextNode(timestamp);
	    timeElement.appendChild(text_time);
	    actElement.appendChild(infoElement);
	    actElement.appendChild(timeElement);
	    root.appendChild(actElement);
		    
	    TransformerFactory tf= TransformerFactory.newInstance();     
	    Transformer transformer= tf.newTransformer();     
	    DOMSource source= new DOMSource(root);     
	    transformer.setOutputProperty(OutputKeys.INDENT,"yes");     
			
	    Writer pwx= new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(adaptee.extension_log_path),"UTF-8"));     
	    StreamResult   result= new   StreamResult(pwx);     
	    transformer.transform(source,result);  
	    pwx.close();
	    
	}catch(Exception ex){
	    ex.printStackTrace();
	}
    }
}
