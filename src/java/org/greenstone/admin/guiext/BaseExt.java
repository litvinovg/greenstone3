package org.greenstone.admin.guiext;

import java.awt.Insets;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.greenstone.gsdl3.util.GSPath;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.*;

import org.greenstone.admin.GAI;

public abstract class BaseExt {
    
    static protected JScrollPane description_pane;
    static protected JPanel main_pane;
    static protected JPanel control_pane;
    static protected JTextArea descriptionTextArea;
    static protected String description;
	
    public JPanel button_pane;
    public String extension_name;
    public String destination_folder;
    public String url;
    public String group;
    public String download_type;
    public Boolean configurable;
    
    GlobalProperties globalProperty = null;
    static String fileSeparator = File.separator;
    GSPath gspath = null;
    
    public BaseExt(){
    }
    
    protected abstract JPanel getControlPane();
    
    protected abstract JPanel getDescriptionPane();    
    
    protected abstract boolean getCommandStatus(String Command);
    
    public boolean doCallback(String methodName)
    {
	return false;
    }

    public boolean doCommand(String cmd)
    {
	return false;
    }
    
    public static Element getRootElement(String url){
    	
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
	
    protected abstract JPanel getButtonPane();
}
