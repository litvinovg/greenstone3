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


class MatSVNDownloadThread extends Thread {
    
    JTextArea _messageTextArea;
    String _extensionName;
    String _URL;
    Mat _mat;
    MatWorkingCopy _MatWorkingCopy = new MatWorkingCopy();
    String _destination;
    public MatSVNDownloadThread (Mat mat,JTextArea messageArea, String svnURL, String destination, String extension_name) {
	    _messageTextArea = messageArea;
    	_URL = svnURL;
    	_extensionName = extension_name;
    	_mat = mat;
    	_destination = destination;
    
    }

    public void run(){
	try{
		MatWorkingCopy wc = new MatWorkingCopy();
		wc.Download(_messageTextArea, _URL,_destination, _extensionName);
	if(wc.getStatus()){
		_mat.setCommandStatus("download");
	}
	}catch(Exception ex){
		ex.printStackTrace();
		return;
	}
    }     
}
 
