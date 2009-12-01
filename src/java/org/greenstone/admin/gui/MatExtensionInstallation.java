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

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.io.DataInputStream;
import java.io.*;
import java.awt.Insets;

class MatExtensionInstallation extends Thread {
    
    Mat adaptee;
	
    public MatExtensionInstallation (Mat m){
	adaptee = m;
    }
    
    public void run() {
    	try{
	    
	    String gsdl3Home = adaptee.get_GSDL3HOME();
	    String fileSeparator = File.separator;
            
	    File wd = new File(adaptee.extension_path);
	    Process proc = null;
	    
	    try {
		
		if(adaptee.os_type.toLowerCase().indexOf("windows")!=-1){
		    String[] arrays = {"ant.bat", "compile"};
		    proc = Runtime.getRuntime().exec(arrays, null, wd);
		}
		
		else{
		    proc = Runtime.getRuntime().exec("ant compile", null, wd);
		}
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader errInput = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		String s= "";
		
		ExtPane.message_textarea.setText("");
		
		while ((s = stdInput.readLine()) != null) {
		    ExtPane.message_textarea.append(s+"\n");
		    ExtPane.message_textarea.setSelectionEnd(ExtPane.message_textarea.getDocument().getLength());
		}  
		
		while ((s = errInput.readLine()) != null) {
   		   ExtPane.message_textarea.append(s+"\n");
		   JOptionPane.showMessageDialog(new JFrame(),adaptee.InstallErrorMsg);	
		  return;
		}  
		
		ExtPane.message_textarea.append(adaptee.InstallCompleteMsg+"\n");
		
		Calendar cl;
		SimpleDateFormat sdf;    
		cl=Calendar.getInstance(); 
		sdf = new SimpleDateFormat("dd/MMM/yyyy 'at' HH:mm:ss z 'GMT'Z");
		String timestamp = sdf.format(cl.getTime());
		
		ExtPane.message_textarea.append(timestamp);
    	proc.waitFor();
		JTextPaneStyle msgpane  = new JTextPaneStyle(adaptee.extension_path,adaptee.web_xml_path);
		msgpane.display();

		ExtPane.updateExtensionContentPane();
		
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(new JFrame(),adaptee.InstallErrorMsg+" \"Ant\" could not be run.\n Please run gs3-setup");	
	    }
	    
    	}catch(Exception ex){
	    ex.printStackTrace();
    	}
    }
} 


class JTextPaneStyle implements ActionListener{

    private static String message ="";

	JFrame frame = new JFrame("The extension (Mat) has been installed.");
	private String filePath;
	private String webPath;
	public JTextPaneStyle (String path, String web_xml_path){
		filePath = path + "README.txt";
		webPath = web_xml_path;
		loadReadMe();
	}

    public void loadReadMe(){
		message = new String();

       	try{

			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null)   {
				if(strLine.indexOf("web@xml@path")!=-1){
				webPath = webPath.replaceAll("\\\\","@");
				strLine = strLine.replaceAll("web@xml@path",webPath);
				strLine = strLine.replaceAll("@","\\\\");
				}
				message = message+ "\n "+ strLine;
			}

			in.close();
    	}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
    	}
	}

	public void display() {

	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(new Dimension(550, 450));
	    JPanel mainPane = new JPanel(new BorderLayout());
	    JTextArea textPane = new JTextArea(message);
	    textPane.setEditable(false);
		textPane.setLineWrap(true);
		textPane.setWrapStyleWord(true);
		textPane.setMargin(new Insets(0,ExtPane.left_padding,0,0));
	    JScrollPane scrollPane = new JScrollPane(textPane);
	    mainPane.add(scrollPane,BorderLayout.CENTER);
		JPanel button_pane = new JPanel(new GridLayout(1,3));

	
	    JButton jbutton = new JButton("OK");
		
		button_pane.add(Box.createRigidArea(new Dimension(18,5)));
		button_pane.add(jbutton);
		button_pane.add(Box.createRigidArea(new Dimension(18,5)));
		
	    jbutton.addActionListener(this);
	    mainPane.add(button_pane,BorderLayout.SOUTH);
	    frame.getContentPane().add(mainPane, BorderLayout.CENTER);
	    frame.setVisible(true);
	  }

	public void actionPerformed(ActionEvent e) {
		writeReadMe();
		frame.dispose();
	}
	
	
	
	public void writeReadMe(){
       	try{
			FileWriter fstream = new FileWriter(filePath);
			BufferedWriter br = new BufferedWriter(fstream);
			br.write(message);
			br.close();
    	}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
    	}
	}
}


