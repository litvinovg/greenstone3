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


class MatLaunchApplication extends Thread{
 
    Mat adaptee;
	
    public MatLaunchApplication (Mat m){
	adaptee = m;
    }
    
    public void run(){
    	
	try{
	    String gsdl3Home = adaptee.get_GSDL3HOME();
	    String fileSeparator = File.separator;
	    
	    File wd = new File(adaptee.extension_path);
	    Process proc = null;
	    ArrayList alist = new ArrayList();
	    
	    if(adaptee.os_type.toLowerCase().indexOf("windows")!=-1){
	    	alist.add("cmd.exe");
	    	alist.add("/C");
	    	alist.add("Mat.bat");
	    }
	    else{
	    	alist.add("bash");
	    	alist.add("-c");
	    	alist.add("bash Mat.sh");
	    }
	    String[] arrays = new String[alist.size()];
	    for(int i = 0; i<arrays.length; i++){
	    	arrays[i] = (String)alist.get(i);
	    }
	    
	    /*
	      String[] arrays = new String[3];
	      arrays[0] = "bash";
	      arrays[1] = "-c";
	      arrays[2] = "bash Mat.sh";
	    */
	    try {
		proc = Runtime.getRuntime().exec(arrays, null, wd);
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String s= "";
		    
		while ((s = stdInput.readLine()) != null) {
		    System.out.println(s);
		}   
		
		proc.waitFor();
		    
		
	    } catch (Exception e) {
		e.printStackTrace();
	    }
    	}catch(Exception ex){
	    ex.printStackTrace();
    	}
    }
} 
