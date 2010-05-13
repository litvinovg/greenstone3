package org.greenstone.admin.guiext;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import java.net.URLClassLoader;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.TableCellRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenstone.admin.GAIManager;
import org.greenstone.admin.GAI;

import org.greenstone.util.Configuration;
import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.GSPath;

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
    public static JTextArea message_textarea = null;

    protected static JPanel current_description_pane = null;

    protected JPanel button_pane = null;
    
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
    static public int left_padding = 5;
    public String fileSeparator = File.separator;
    
    protected Element extFileRootElement = null;
    protected HashMap extensions = null;
    protected HashMap extClasses = null;
    protected Properties dependencies = new Properties();
    protected JPanel extPanel = new JPanel();
    protected JSplitPane extensionListContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
       
    public ExtPane() {
  
	String gsdl3Home = GAI.getGSDL3Home();

	file_location = gsdl3Home+fileSeparator+"ext"+fileSeparator+"extension_project_list.xml";
	
	extFileRootElement = getRootElement();

	//Load the steps that have already been performed (used for dependencies)
	loadExtensionInformation();
	    
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
    }

    public void loadExtensionInformation()
    {
	Element[] extensionElements = ExtXMLHelper.getMultipleChildElements(extFileRootElement, ExtXMLHelper.EXTENSION, true);
	if(extensionElements != null){
	    extensions = new HashMap();

	    for(int i = 0; i < extensionElements.length; i++){
		ExtensionInformation ei = new ExtensionInformation(extensionElements[i], extFileRootElement.getAttribute("baseURL"));
		extensions.put(ei.getName(), ei);
	    }
	}
	else{
	    System.err.println("There are no <" + ExtXMLHelper.EXTENSION  + "> elements in the extension_project_list.xml file in the extension directory");
	}
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
    	extensionTreeList.addMouseListener(new ConfTreeMouseListener());
    	
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

	// Layout Components
	extensionList_pane.setLayout(new BorderLayout());
	extensionList_pane.add(extensionTreeList, BorderLayout.CENTER);
	extensionList_pane.add(extensionList_label, BorderLayout.NORTH);
	extensionListContainer.add(extensionList_pane, JSplitPane.TOP);
	extensionListContainer.setDividerLocation(600);
	
	extPanel.setLayout(new BorderLayout());
	ExtensionInformation.setExtPane(extPanel);

	JScrollPane extScrollPane = new JScrollPane(extPanel);

	extensionContent_pane.setLayout(new BorderLayout());
	//extensionContent_pane.add(main_content_pane,BorderLayout.SOUTH);
	extensionContent_pane.add(extensionContent_label, BorderLayout.NORTH);
	extensionContent_pane.add(extScrollPane, BorderLayout.CENTER);
	
	main_ext_pane.add(extensionListContainer, JSplitPane.LEFT);
	main_ext_pane.add(extensionContent_pane, JSplitPane.RIGHT);
	main_ext_pane.setDividerLocation(LIST_SIZE.width);

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
	Element rootNode = extFileRootElement;
	NodeList projectList =  rootNode.getElementsByTagName(ExtXMLHelper.EXTENSION);

    	for(int i = 0; i<projectList.getLength(); i++){
	    
	    Element projectNode = (Element)projectList.item(i);
	    NodeList nameList = projectNode.getElementsByTagName(ExtXMLHelper.NAME);
	    String name = nameList.item(0).getChildNodes().item(0).getNodeValue();
    		
	    NodeList groupList = projectNode.getElementsByTagName(ExtXMLHelper.GROUP);
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

    protected class ConfTreeMouseListener implements MouseListener
    {
	public void mouseClicked(MouseEvent e)
	{
	    Object comp = extensionTreeList.getLastSelectedPathComponent();
	    if(comp == null){
		return;
	    }

	    String option = comp.toString();
	    if(option == null){
		return;
	    }

	    ExtensionInformation info = (ExtensionInformation)extensions.get(option);

	    if(info != null){
		extPanel.removeAll();
		
		JTextArea descArea = new JTextArea();
		descArea.setText(info.getDescription());
		descArea.setEditable(false);
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		
		//JEditorPane descArea = new JEditorPane("text/plain" info.getDescription());
		//descArea.setEditable(false);

		extPanel.add(descArea, BorderLayout.CENTER);
		extPanel.revalidate();
		extPanel.repaint();
	    }
	}
         
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){} 
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
		    extensionListContainer.setBottomComponent(getProjectButtonPane(option));
		    extensionListContainer.setDividerLocation(250);

		    extensionContent_label.setText(option);    
		}
		catch(Exception ex){
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
		    //extensionContent_pane.remove(2);
		    //extensionContent_pane.revalidate();
		    //extensionContent_pane.repaint();
		    
		    
		    //JTextArea ja= new JTextArea();
		    //JScrollPane jp = new JScrollPane(ja);
		    //extensionContent_pane.add(jp);
		    //extensionContent_pane.revalidate();
		    //extensionContent_pane.repaint();
		    //xxxxx
	    	}
	    }
	}
    }

    public String loadDescription(String className){
	
	String description = "";
    	Element rootNode = extFileRootElement;
    	NodeList projectNodeList = rootNode.getElementsByTagName(ExtXMLHelper.EXTENSION);
    	
    	for(int i = 0; i < projectNodeList.getLength(); i++){
	    
	    Element projectNode = (Element)projectNodeList.item(i);
	    NodeList nameNodeList = projectNode.getElementsByTagName(ExtXMLHelper.NAME);
	    String name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    if(name.equals(className)){
		NodeList descriptionNodeList = projectNode.getElementsByTagName(ExtXMLHelper.DESCRIPTION);
		description = descriptionNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    }
    	}    	
	return description;
    }

    public void load_Admin_UI(){
    	
	Element rootNode = extFileRootElement;
    
	NodeList projectList =  rootNode.getElementsByTagName(ExtXMLHelper.EXTENSION);

	for(int i = 0; i < projectList.getLength(); i++){
        	
	    Element projectNode = (Element)projectList.item(i);
	    ArrayList alist = new ArrayList();    		
	    NodeList nameList = projectNode.getElementsByTagName(ExtXMLHelper.NAME);
	    String name = nameList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    //Maybe recode this? //xxxxx
	    try{
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

    public static boolean deleteDir(File dir) {
        
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

    public JPanel getProjectButtonPane(String projectName){

    	String option = extensionTreeList.getLastSelectedPathComponent().toString();
    	JPanel buttonPane = new JPanel();

	ExtensionInformation extension = (ExtensionInformation)extensions.get(projectName);

	String gsdl3ExtHome = GAI.getGSDL3ExtensionHome();

	JButton[] buttons = extension.getSequenceList().getButtons();

	GridLayout gl = new GridLayout(buttons.length, 1);
	gl.setVgap(5);
	buttonPane.setLayout(gl);
	
	for(int j = 0; j < buttons.length; j++){
	    buttonPane.add(buttons[j]);
	}
	
	extension.getSequenceList().updateButtons();
	
	return buttonPane;
    }
}