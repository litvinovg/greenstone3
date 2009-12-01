package org.greenstone.gsdl3.build;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

import org.greenstone.gsdl3.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import java.net.URL;
import java.io.DataInputStream;


/** StatusDisplay - a java applet thats sends a message at regular intervals 
 * to a pre specified address (the library param) and then displays the 
 * status messages received.
 * to use, embed in a web page using something like: (note the cgi args may differ - see process.xsl for details )
 * <applet code="org.greenstone.gsdl3.build.StatusDisplay.class" codebase='lib/java' archive='gsdl3.jar, xercesImpl.jar, jaxp.jar, xml-apis.jar' width='537' height='100'>The status display applet.<param name='library' value='mylibrary?a=pr&rt=s&c=build&o=xml&ro=1&s=ImportCollection&l=en&coll=demo'/></applet>
 * the cgi params for the library param are dependent on what action/service 
 *  is using the applet
 */
public class StatusDisplay extends Applet 
    implements Runnable {

    /** the display area for the status messages */
    protected TextArea display;
    /** how long to delay between requests */
    protected int delay=3000; // 3 secs
    /** the thread used */
    protected Thread updator_thread;
    /** the url for the requests */ 
    protected URL status_cgi = null;
    /** if true, stops sending requests, and cant be unset */
    protected boolean completed = false;
    // init is called if you leave the page and go back - need to somehow retrieve all the previous messages that have been obtained cos we may have left the page and gone back to it during an import or something. dont want to lose all the info.
    public void init() {
	//Create the text field and make it uneditable.
	this.display = new TextArea();
	this.display.setEditable(false);
	this.display.setBackground(Color.white);
	//Set the layout manager so that the text field will be
	//as wide as possible.
	setLayout(new java.awt.GridLayout(1,0));

	//Add the text field to the applet.
	add(this.display);
	validate();

	// add the initial status text
	String initial_text = parameterValue("initial_text");
	addItem(initial_text);

	// check the initial error state - there may be no more messages to come
	this.completed = GSStatus.isCompleted(Integer.parseInt(parameterValue("initial_code")));
		
	// we always do the same query
	String library = parameterValue("library");
	library = tidy_URL(library);
	try {
	    this.status_cgi = new URL(library);
	} catch (Exception e) {
	    System.out.println("Error creating URL for "+library+": "+e.getMessage());
	}
    }
    
    public void start() {
	if (this.completed) {
	    // do nothing
	} else {
	    if (this.updator_thread == null) {
		this.updator_thread = new Thread(this);
	    }
	    this.updator_thread.start();
	}
    }
    public void stop() {
	this.updator_thread = null;
    }
    public void run() {
	
	if (this.completed) { // do nothing. dont know if we need this here
	    return;
	}
        //Just to be nice, lower this thread's priority
        //so it can't interfere with other processing going on.
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        //Remember which thread we are.
        Thread current_thread = Thread.currentThread();

        //This is the  loop.
        while (current_thread == this.updator_thread && !this.completed) {
	    queryServer();
            this.display.repaint();
	    
            //Delay for the specified time
            try {
		Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /** add some more text to the display    */
    void addItem(String new_text) {
	String t = this.display.getText()+"\n"+new_text;
	this.display.setText(t);
	this.display.setCaretPosition(t.length()); // so that you always see the 
	// most recent entries - ie so that it scrolls down automatically
	repaint();
    }
    
    void queryServer() {

	// Send the query 
	System.out.println("sending query: " + this.status_cgi); // this appears in the java console
	try {
	    DataInputStream in = new DataInputStream(this.status_cgi.openStream());
	    DOMParser parser = new DOMParser();
	    parser.parse(new InputSource(in));
	    Document data_doc = parser.getDocument();
	    Element data_elem = data_doc.getDocumentElement();
	    Element status_elem = (Element)GSXML.getChildByTagName(data_elem, GSXML.STATUS_ELEM);
	    if (status_elem != null) {
		String status = status_elem.getAttribute(GSXML.STATUS_ERROR_CODE_ATT);
		System.out.println("received status "+status);
		this.completed = GSStatus.isCompleted(Integer.parseInt(status));
		addItem(GSXML.getNodeText(status_elem));
	    } else {
		System.out.println("Error - no status receieved");
		this.completed=true;
	    }
	    in.close();
	} catch (Exception e) {
	    System.err.println( "Error sending query ("+this.status_cgi+"): " + e);
	}

    }
  
    // Get a REQUIRED string.  Stop the applet if we cannot.
    String parameterValue(String name) {
	try { 
	    return getParameter(name);
	} catch (Exception e) {
	    System.err.println("StatusDisplay: you must give a parameter for \""+ name + "\".  Stopping.");
	    stop();
	}
	return "";
    }

    /**Tidy up URLs
     *
     * Ensure a URL address (as string) has a protocol, host, and file.
     */
    String tidy_URL(String address) {

	// make sure the URL has protocol, host, and file
	if (address.startsWith("http")) {
	    // the address has all the necessary components
	} else if (address.startsWith("/")) {
	    // there is not protocol and host
	    URL document = getDocumentBase();
	    String port = "";
	    if (document.getPort()!=-1) {
		port = ":" + document.getPort();
	    }
	    address = "http://" + document.getHost() + port  + address;
	} else {
	    // this URL is relative to the directory the document is in
	    URL document = getDocumentBase();
	    String directory = document.getFile();
	    int end = directory.lastIndexOf('/');
	    String port = "";
	    if (document.getPort()!=-1) {
		port = ":" + document.getPort();
	    }
	    directory = directory.substring(0,end + 1);
	    address = "http://" + document.getHost() + port + directory + address;

	}

	return address;
    }

}
