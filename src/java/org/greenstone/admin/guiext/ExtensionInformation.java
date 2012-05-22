package org.greenstone.admin.guiext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URLClassLoader;
import java.net.URL;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.io.File;

import java.awt.BorderLayout;

import javax.swing.JTextArea;
import javax.swing.JPanel;

import org.greenstone.admin.GAI;

public class ExtensionInformation
{
    protected String _name = null;
    protected String _group = null;
    protected String _fileStem = null;
    protected String _description = null;
    protected String _baseURL = null;
    protected SequenceList _sequenceList= null;
    protected boolean _guiExtLoaded = false;

    protected static JPanel _extPane = new JPanel();

    public ExtensionInformation(Element extensionElement, String baseURL){
	if(extensionElement != null){
	    _name = ExtXMLHelper.getValueFromSingleElement(extensionElement, ExtXMLHelper.NAME, true);
	    _group = ExtXMLHelper.getValueFromSingleElement(extensionElement, ExtXMLHelper.GROUP, true);
	    _fileStem = ExtXMLHelper.getValueFromSingleElement(extensionElement, ExtXMLHelper.FILE_STEM, true);
	    _description = ExtXMLHelper.getValueFromSingleElement(extensionElement, ExtXMLHelper.DESCRIPTION, true);
	    _baseURL = baseURL;
	    _sequenceList = new SequenceList(ExtXMLHelper.getSingleChildElement(extensionElement, ExtXMLHelper.SEQUENCE_LIST, true), this);
	}
	else{
	    System.err.println("<" + ExtXMLHelper.EXTENSION + "> element is null");
	}
    }

    public String getBaseURL()
    {
	return _baseURL;
    }

    public String getName()
    {
	return _name;
    }

    public String getGroup()
    {
	return _group;
    }
    
    public String getFileStem()
    {
	return _fileStem;
    }

    public String getDescription()
    {
	return _description;
    }

    public SequenceList getSequenceList()
    {
	return _sequenceList;
    }

    public void loadExternalJar(String filename){
	//Load the dynamic jar loader
	URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	Class<URLClassLoader> sysclass = URLClassLoader.class;
	final Class[] parameters = new Class[]{URL.class};
	Method method = null;
	try{
	    method = sysclass.getDeclaredMethod("addURL", parameters);
	}
	catch(Exception ex){
	    ex.printStackTrace();
	}
	method.setAccessible(true);

	File externalFile = new File(filename);
	if(!externalFile.exists()){
	    System.err.println("Cannot load the external jar file at " + filename + " as it does not exist");
	    return;
	}

	try {
	    URL u = new URL(externalFile.toURI().toURL().toString());
	    method.invoke(sysloader, new Object[]{u});
	} catch (Throwable t) {
	    t.printStackTrace();
	}

	_guiExtLoaded = true;
    }

    public void loadGuiExtFile()
    {
	if(!_guiExtLoaded){
	    loadExternalJar(getExtensionDirectory() + System.getProperty("file.separator") + "guiext.jar");		
	}
    }    

    public boolean isGuiExtLoaded()
    {
	return _guiExtLoaded;
    }

    public void changeExtPane(JPanel extPane)
    {
	_extPane.removeAll();
	_extPane.add(extPane, BorderLayout.CENTER);
	_extPane.revalidate();
	_extPane.repaint();
    }

    public static void setExtPane(JPanel extPane)
    {	
	_extPane = extPane;
    }

    public JPanel getExtPane()
    {
	return _extPane;
    }

    public String getExtensionDirectory()
    {
	return GAI.getGSDL3ExtensionHome() + System.getProperty("file.separator") + _fileStem;
    }
}