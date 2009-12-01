package org.greenstone.admin.gui;

import java.awt.Insets;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.gsdl3.util.GlobalProperties;
import org.w3c.dom.*;

public abstract class adminUI_Pane {
    
    static JScrollPane description_pane;
    static JPanel main_pane;
    static JPanel control_pane;
    static JTextArea descriptionTextArea;
    static String description;
	
    JPanel button_pane;
    String extension_name;
    String destination_folder;
    String url;
    String group;
    String download_type;
    Boolean configurable;
	
    static final String EXTENSION = "extension";
    static final String GROUP = "group";
    static final String ADMIN_UI = "admin_ui";
    static final String NAME = "name";
    static final String DOWNLOAD = "download";
    static final String INSTALL = "install";
    static final String UNINSTALL = "uninstall";
    static final String ENABLE = "enable";
    static final String DISABLE = "disable";
    static final String DESTINATION = "destination";
    static final String DESCRIPTION = "description";
    static final String PROPERTY = "property";
    static final String STEP ="step";
    
    GlobalProperties globalProperty = null;
    String fileSeparator = File.separator;
    GSPath gspath = null;
    
    public adminUI_Pane(){
	
    }
    
    public void setup(){
	
	descriptionTextArea.setText("");
	description = loadDescription(extension_name);
	descriptionTextArea = new JTextArea();
	descriptionTextArea.setText("");
	descriptionTextArea.append(description);
	descriptionTextArea.setEditable(false);
	descriptionTextArea.setLineWrap(true);
	descriptionTextArea.setWrapStyleWord(true);
	descriptionTextArea.setMargin(new Insets(0,ExtPane.left_padding,0,0));
	
	description_pane = new JScrollPane(descriptionTextArea);
	description_pane.setAutoscrolls(true);
    }
    
    abstract JPanel getControlPane();
    
    abstract JPanel getDescriptionPane();    
    
    abstract boolean getCommandStatus(String Command);

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
    
    public String loadDescription(String className){
	
     	String gsdl3Home = get_GSDL3HOME();
    	Element rootNode = getRootElement(gsdl3Home+fileSeparator+"ext"+fileSeparator+"extension_project_list.xml");
    	NodeList projectNodeList = rootNode.getElementsByTagName(EXTENSION);
    	
    	for(int i = 0; i < projectNodeList.getLength(); i++){
	    
	    Element projectNode = (Element)projectNodeList.item(i);
	    NodeList nameNodeList = projectNode.getElementsByTagName(NAME);
	    String name = nameNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    
	    if(name.equals(className)){
		NodeList descriptionNodeList = projectNode.getElementsByTagName(DESCRIPTION);
		description = descriptionNodeList.item(0).getChildNodes().item(0).getNodeValue();
	    }
    	}    	
	return description;
    }
    
    protected Element getRootElement(String url){
    	
	Element rootNode = null;
    	try{
    	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	    Document doc = docBuilder.newDocument();
    	    doc = docBuilder.parse (new File(url));
    	    rootNode = doc.getDocumentElement();  
    	    return rootNode;
    	}catch (Exception e) {
	    e.printStackTrace();
	    return null;
    	}
    }
    
    protected abstract Object[][] getConfigureContent();
    
    //protected abstract Boolean isConfigurable();
	
    abstract JPanel getButtonPane();
}
