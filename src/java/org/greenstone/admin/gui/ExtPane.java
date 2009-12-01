package org.greenstone.admin.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenstone.admin.GAIManager;
import org.greenstone.admin.GAI;
import org.greenstone.admin.Configuration;
import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GlobalProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class ExtPane extends JPanel  {

    /* The pane to demonstrate extension information*/

    protected JSplitPane main_ext_pane = null;

    /** The panel that contains a log_list */
    private JPanel extensionList_pane =  null;

    /** The panel that contains the log content. */
    protected static JPanel extensionContent_pane =  null;

    /** The List showing all the log files concerned. */
    //protected JList extension_list=null;

    /** The label at the top of the logList_pane. */
    protected JLabel extensionList_label =  null;
      
    /** The label shown at the top of the logContent Pane. */
    protected JLabel extensionContent_label  =  null;
    
    /** The scrollable area into which the log content is placed. */
    protected JScrollPane description_content = null;
    
    /* Log TEXT AREA*/
    protected JTextArea description_textarea = null;
   
    protected JScrollPane message_content = null;
    
    /* Log TEXT AREA*/
    protected static JTextArea message_textarea = null;

    protected static JPanel current_description_pane = null;

    protected JPanel button_pane = null;
    
    /** Buttons */
    //protected JButton download_button = null;
    //protected static JButton pre_configure_button = null;
    //protected JButton install_button = null;
    //protected JButton post_configure_button = null;
    //protected JButton uninstall_button = null;
    //protected JButton switch_button = null;

    protected static JPanel control_pane = null;

    protected JPanel main_content_pane = null;
    
    protected JTree extensionTreeList = null;
      
    protected DefaultMutableTreeNode top = new DefaultMutableTreeNode("Extension List");
    
    protected HashMap extensionInformation = null;
      
    protected JPanel extensionContentHeaderPane = null;
    
    protected String file_location;
    
    protected final String extension_status_root = "Configuration";
    protected File extension_status_file = null;
    protected String extension_file = null;
    
    /** The various sizes for the screen layout*/
    static protected Dimension MIN_SIZE = new Dimension( 90,  90);
    static protected Dimension LIST_SIZE  = new Dimension(200, 450);
    static protected Dimension LOGPANE_SIZE = new Dimension (800,450);
    static protected int left_padding = 5;
    static String fileSeparator = File.separator;
    
    GlobalProperties globalProperty = null;

    GSPath gspath = null;
       
    public ExtPane() {
	

    String gsdl3Home = get_GSDL3HOME();

    file_location = gsdl3Home+fileSeparator+"ext"+fileSeparator+"extension_project_list.xml";


	// create all the necessary panes
	control_pane = new JPanel();
	button_pane = new JPanel();
	current_description_pane = new JPanel();
	extensionContentHeaderPane = new JPanel();
	extensionInformation = new HashMap();

	// Extension_Pane
	main_ext_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	main_ext_pane.setPreferredSize(LOGPANE_SIZE);

	// Extension_TextArea
	description_textarea = new JTextArea();
	description_textarea.setEditable(false);
	description_textarea.setLineWrap(true);
	description_textarea.setWrapStyleWord(true);
	description_textarea.setMargin(new Insets(0,left_padding,0,0));
	//description_textarea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	
	message_textarea = new JTextArea();
	message_textarea.setEditable(false);
	message_textarea.setAutoscrolls(true);
	message_textarea.setLineWrap(true);
	message_textarea.setWrapStyleWord(true);
	message_textarea.setMargin(new Insets(0,left_padding,0,0));
	
	/*
	download_button = new JButton();
	download_button.setEnabled(false);
	download_button.setText("Download");
	
	ActionListener donwloadButton = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    download_jButton_Adapter(ae);
		}
	    };
	
	ActionListener cursorDoIt1 = CursorController.createListener(this, donwloadButton);
	download_button.addActionListener(cursorDoIt1);
	
	pre_configure_button = new JButton();
	pre_configure_button.setEnabled(false);
	pre_configure_button.setText("Pre-Configuration");

	
	ActionListener preconfigureButton = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			preConfigure_jButton_Adapter(ae);
		}
	    };
	
	ActionListener cursorDoIt3 = CursorController.createListener(this,preconfigureButton);
	pre_configure_button.addActionListener(cursorDoIt3);

	
	install_button = new JButton();
	install_button.setEnabled(false);
	install_button.setText("Compile/Install");
	
	post_configure_button = new JButton();
	post_configure_button.setText("Post-Configuration");
	post_configure_button.setEnabled(false);
	
	uninstall_button = new JButton();
	uninstall_button.setText("Uninstall");
	uninstall_button.setEnabled(false);
    
	switch_button = new JButton();
	switch_button.setText("Switch");
	switch_button.setEnabled(false);
    
	
	ActionListener switchButton = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    switch_jButton_Adapter(ae);
		}
	    };
	
	ActionListener cursorDoIt2 = CursorController.createListener(this, switchButton);
	switch_button.addActionListener(cursorDoIt2);
	*/
    }


    public void actionPerformed(ActionEvent event) {
    }


    /** This method is callsed to actually layout the components.*/
    public void display() {
         	
	load_Admin_UI();
	HashMap projectNameMap = getProjectList();
    	Set s = projectNameMap.keySet();
    	Iterator it = s.iterator();
    	int i = 0;
    	
	while(it.hasNext()){
	    String projectNameGroup = (String)it.next(); 
	    ArrayList alist = (ArrayList)projectNameMap.get(projectNameGroup);
	    DefaultMutableTreeNode projecti = new DefaultMutableTreeNode (projectNameGroup);
	    
	    for (int j = 0; j< alist.size(); j++){
		String projectName = (String)alist.get(j);
		DefaultMutableTreeNode projectNameGroupj = new DefaultMutableTreeNode (projectName);
		projecti.add(projectNameGroupj);
	    }
	    top.add(projecti);
	    i++;
    	}

    	extensionTreeList  = new JTree(top); 
    	extensionTreeList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    	extensionTreeList.addTreeSelectionListener(new ConfTreeListener());
    	
	// extensionList_Pane
	extensionList_pane = new JPanel();
	extensionList_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	extensionList_label = new JLabel();
	extensionList_label.setOpaque(true);
	extensionList_label.setBackground(Configuration.getColor("coloring.workspace_heading_background"));
	extensionList_label.setForeground(Configuration.getColor("coloring.workspace_heading_foreground"));
	extensionList_label.setText("Available Extensions");
	extensionList_label.setBorder(BorderFactory.createEmptyBorder(0, left_padding, 0, 0));
	// extensionContent_Pane
	extensionContent_pane = new JPanel();
	extensionContent_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	extensionContent_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	extensionContent_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	
	extensionContent_label = new JLabel();
	extensionContent_label.setBorder(BorderFactory.createEmptyBorder(0, left_padding, 0, 0));
	extensionContent_label.setOpaque(false);
	extensionContent_label.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	extensionContent_label.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	extensionContent_label.setText("Extension Content");
	main_content_pane = new JPanel();

	// TEXTAREA Layout
	description_content = new JScrollPane(description_textarea);
	description_content .setName("description_content");
	//description_content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	message_content = new JScrollPane(message_textarea);

	control_pane.setLayout (new BorderLayout());
	control_pane.setBorder(BorderFactory.createEmptyBorder(05,10,5,10));
	control_pane.setPreferredSize(new Dimension(200,50));
	control_pane.setSize(new Dimension(200,50));
	control_pane.add (button_pane, BorderLayout.CENTER);

	// Layout Components
	extensionList_pane.setLayout(new BorderLayout());
	extensionList_pane.add(extensionTreeList, BorderLayout.CENTER);
	extensionList_pane.add(extensionList_label, BorderLayout.NORTH);
			
	main_content_pane.setLayout(new BorderLayout());
	main_content_pane.setPreferredSize(new Dimension(600, 200));
	main_content_pane.setSize(new Dimension(600, 200));
	main_content_pane.add(control_pane, BorderLayout.WEST);
	main_content_pane.add(message_content, BorderLayout.CENTER);
	
	//extensionContentHeaderPane.setLayout(new FlowLayout());
	//extensionContentHeaderPane.add(extensionContent_label);
	//extensionContentHeaderPane.add(switch_button);
	
	extensionContent_pane.setLayout(new BorderLayout());
	extensionContent_pane.add(main_content_pane,BorderLayout.SOUTH);
	extensionContent_pane.add(extensionContent_label, BorderLayout.NORTH);
	extensionContent_pane.add(description_content, BorderLayout.CENTER);
	
	main_ext_pane.add(extensionList_pane, JSplitPane.LEFT);
	main_ext_pane.add(extensionContent_pane, JSplitPane.RIGHT);
	main_ext_pane.setDividerLocation(LIST_SIZE.width - 10);
	
	this.setLayout(new BorderLayout());
	this.add(main_ext_pane, BorderLayout.CENTER);
	
    }
    
    
    protected Element getRootElement(){
    	
	Element rootNode = null;
    	
	try{
    	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	    Document doc = docBuilder.newDocument();
    	    doc = docBuilder.parse (new File(file_location));
    	    rootNode = doc.getDocumentElement();  
    	    return rootNode;
    	
	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
    	}
    }
    
    protected HashMap getProjectList(){
    	
	HashMap projectGroupMap = new HashMap();
	Element rootNode = getRootElement();
	NodeList projectList =  rootNode.getElementsByTagName(adminUI_Pane.EXTENSION);

    	for(int i = 0; i<projectList.getLength(); i++){
	    
	    Element projectNode = (Element)projectList.item(i);
	    NodeList nameList = projectNode.getElementsByTagName(adminUI_Pane.NAME);
	    String name = nameList.item(0).getChildNodes().item(0).getNodeValue();
    		
	    NodeList groupList = projectNode.getElementsByTagName(adminUI_Pane.GROUP);
	    String group = groupList.item(0).getChildNodes().item(0).getNodeValue();

	    if(projectGroupMap.containsKey(group)){
		ArrayList alist = (ArrayList)projectGroupMap.get(group);
		alist.add(name);
		projectGroupMap.put(group, alist);
	    }
	    else{
		ArrayList alist = new ArrayList();
		alist.add(name);
		projectGroupMap.put(group, alist);
	    }
    	}
	return projectGroupMap;
    }
     
    public String get_GSDL3HOME(){
    	
    	String gsdl3Home = globalProperty.getGSDL3Home();
    	String os = "linux";
    	
    	if(System.getProperty("os.name").toLowerCase().indexOf("windows")!=-1){
    	    gsdl3Home = gsdl3Home.replaceAll("\\\\", "/");
    	    os = "windows";
    	}
    	

    	gsdl3Home = gspath.removeLastLink(gsdl3Home);
    	
    	if(os.equals("windows")){
    	    gsdl3Home = gsdl3Home.replaceAll("/", "\\\\");
    	}
    	return gsdl3Home;
        }
    
    protected class ConfTreeListener implements TreeSelectionListener{
	
	public void valueChanged (TreeSelectionEvent e){
	    
	    String option = new String();
	    
	    try{
		option = extensionTreeList.getLastSelectedPathComponent().toString();
	    }catch(Exception ex){
	    	
	    }
	    
	    if(extensionInformation.containsKey(option)){
		
		try{
		    extensionContent_pane.remove(2);
		    extensionContent_pane.validate();
		    extensionContent_pane.repaint();
		    
		    ArrayList alist = (ArrayList) extensionInformation.get(option);
		    Class c = (Class)alist.get(0);
		    Object o = (Object)alist.get(1);
		    
		   
		    
		    
		    JPanel cardLayoutPane = new JPanel(new CardLayout());
		    Method get_Description_Pane = c.getMethod("getDescriptionPane", new Class[0]);
		    JPanel descriptionPane = (JPanel)get_Description_Pane.invoke(o,new Object[0]);
		    Method get_Control_Pane = c.getMethod("getControlPane", new Class[0]);
		    JPanel controlPane = (JPanel)get_Control_Pane.invoke(o,new Object[0]);

		    cardLayoutPane.add(descriptionPane, "Description");
		    cardLayoutPane.add(controlPane, "Control");
		    current_description_pane = cardLayoutPane;


		    control_pane.remove(0);
		    control_pane.revalidate();
		    control_pane.repaint();
		    
		    button_pane = getProjectButtonPane();
		    control_pane.add(button_pane);
    		    control_pane.revalidate();
    		    control_pane.repaint();
    		
                    /*
		    Method isConfigurable = c.getMethod("isConfigurable", new Class[0]);
		    Boolean configurable = (Boolean) isConfigurable.invoke(o,new Object[0]);
		    */
		    extensionContent_label.setText(option);
		    		
		    extensionContent_pane.add(current_description_pane, BorderLayout.CENTER);
		    extensionContent_pane.validate();
		    extensionContent_pane.repaint();
		    
		    Class[] types = new Class[] { String.class};
		    Method getStatus = c.getMethod("getCommandStatus", types);

		    Object[] args = new Object[] { "install"};
		    Boolean bv = (Boolean)(getStatus.invoke(o, args));
		    boolean status = bv.booleanValue();

		    if(status){
		    	updateExtensionContentPane();
		    }
		    /*
		     * 
		     * 
		     * updateExtensionContentPane
		     */
		    
		}catch(Exception ex){
		    ex.printStackTrace();
		    description_textarea.setText("");
		    description_textarea.append("Sorry, this extension is not available!");
		    extensionContent_pane.add(description_content, BorderLayout.CENTER);
		    extensionContent_pane.validate();
		    extensionContent_pane.repaint();
		}
	    }
	    else{
	    	if(control_pane.getComponentCount()>0){
	    		//System.out.println("remove");
	    		control_pane.remove(0);
	    		control_pane.revalidate();
	    		control_pane.repaint();
	    		control_pane.add(new JPanel());
	    		control_pane.revalidate();
	    		control_pane.repaint();
	    	}

	    	if(extensionContent_pane.getComponentCount()>0){

	    		extensionContent_label.setText("Extension Content");
	    		extensionContent_pane.remove(2);
	    		extensionContent_pane.revalidate();
	    		extensionContent_pane.repaint();
	    		

	    		JTextArea ja= new JTextArea();
	    		JScrollPane jp = new JScrollPane(ja);
	    		extensionContent_pane.add(jp);
	    		extensionContent_pane.revalidate();
	    		extensionContent_pane.repaint();
	    		
	    		

	    	}
	    }
	}
    }
   

  
    
    public JPanel getProjectButtonPane(){
    	String option = extensionTreeList.getLastSelectedPathComponent().toString();
    	JPanel buttonPane = new JPanel();
    	try{

        	ArrayList alist = (ArrayList) extensionInformation.get(option);
        	Class c = (Class)alist.get(0);
        	Object o = (Object)alist.get(1);

        	Method a = c.getMethod("getButtonPane", new Class[0]);
        	buttonPane = (JPanel)a.invoke(o, new Object[0]);
        		
        		
        	}catch (Exception ex){
        	    ex.printStackTrace();
        	}
        	return buttonPane;
    }
    
    public void load_Admin_UI(){
    	
	Element rootNode = getRootElement();
    
	NodeList projectList =  rootNode.getElementsByTagName(adminUI_Pane.EXTENSION);

	for(int i = 0; i<projectList.getLength(); i++){
        	
	    Element projectNode = (Element)projectList.item(i);
	    ArrayList alist = new ArrayList();    		
	    NodeList nameList = projectNode.getElementsByTagName(adminUI_Pane.NAME);
	    String name = nameList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    NodeList uiList = projectNode.getElementsByTagName(adminUI_Pane.ADMIN_UI);
	    String admin_ui = uiList.item(0).getChildNodes().item(0).getNodeValue();

	    try{
		Class c = Class.forName(admin_ui);
		Class[] paramTypes = {String.class};
		Constructor ct = c.getConstructor(paramTypes);
		Object[] paramList = {name};
		
		Object theNewObject = (Object)ct.newInstance(paramList);
		alist.add(c);
		alist.add(theNewObject);
		extensionInformation.put(name, alist);
	    }catch (Exception ex){
		ex.printStackTrace();
	    }
	    
	}
    }
    
    public static void updateButtonPane(JPanel pane){
	    control_pane.remove(0);
	    control_pane.revalidate();
	    control_pane.repaint();
	    control_pane.add(pane);
    }
    
    public static void updateExtensionContentPane(){
	     CardLayout cl = (CardLayout)( current_description_pane.getLayout());
	     cl.next(current_description_pane);

    }
    
  /*  
    private void updateStatus(String project_name, String action){
	
	try{
	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc = docBuilder.newDocument();
	    Element root = doc.createElement(extension_status_root);
			
	    boolean fileExist = extension_status_file.exists();
	    
	    if(!fileExist){
		extension_status_file = new File (extension_file);
		root = doc.createElement(extension_status_root);
	    }
	    else{
		doc = docBuilder.parse (extension_status_file);
		root = doc.getDocumentElement();
	    }
	    
	    
	    TransformerFactory tf = TransformerFactory.newInstance();     
	    Transformer transformer= tf.newTransformer();     
	    DOMSource source= new DOMSource(root);     
	    transformer.setOutputProperty(OutputKeys.INDENT,"yes");     
			
	    Writer pwx= new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(extension_status_file),"UTF-8"));     
	    StreamResult result= new StreamResult(pwx);     
	    transformer.transform(source,result);  
	    pwx.close();
	    
	    root = null;
	    docBuilderFactory = null;
	    docBuilder = null;
	    doc = null;
	    
	}catch (Exception e) {
	    System.out.println(e);
	}	
    }*/

}

class ProjectGroup{
    
    protected String groupName = null;
    protected ArrayList nameList = null;
    
    public ProjectGroup(){
	nameList = new ArrayList();
    }
	 
    public void setGroupName(String name){
	groupName = name;
    }
	 
    public String getGroupName(){
	return groupName;
    }
	 
    public void addGroupName(String name){
	nameList.add(name);
    }
    
    public ArrayList getGroupNameList(){
	return nameList;
    }
}
