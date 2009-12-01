/**********************************************************************
 *
 * Phind.java -- the Phind java applet - modified to work with gsdl3 kjdon
 *
 * Copyright 1997-2000 Gordon W. Paynter
 * Copyright 2000 The New Zealand Digital Library Project
 *
 * A component of the Greenstone digital library software
 * from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *********************************************************************/


/*********************************************************************

To use the applet, you'll need to embed it in a web page like this:

<APPLET CODE="Phind.class" WIDTH=500 HEIGHT=500>

  <PARAM NAME=collection  VALUE="fao.org">
  <PARAM NAME=classifier  VALUE="1">
  <PARAM NAME=phindcgi    VALUE="http://kowhai/cgi-bin/phindcgi">
  <PARAM NAME=library     VALUE="http://kowhai/cgi-bin/library">
  <PARAM NAME=backdrop    VALUE="http://kowhai/~paynter/transfer/phindtest/green1.jpg">
  The Phind java applet.
</APPLET>

There are a bunch of other parameters; these are described in the 
getParameters method below.  It is all done for you in Greenstone
in the document.dm macro file (the _phindapplet_ macro).

You may have problems with Java applet security.  Java applet's can only
open socket connections (including the HTTP connections the applet uses 
to get data) to the same server the applet was loaded from.  This means
that your phindcgi, library, and (optional) backdrop URLs must be on the
same machine that your web page was loaded from.

**********************************************************************

The applet comprises several classes:

1. Phind (this file) is the applet class, loaded by the browser.
   It also handles network connections.
2. ResultDisplay is a Panel that sits in the applet and displays 
   things; displays are connected in a doubly-linked list.
3. ResultBox holds the results of a query.  Result boxes are shown
   to the user through ResultDisplays.  Another doubly-linked list.
4. ResultTitle acts as a caption to a ResultBox describing its contents.
5. ResultCanvas is what the ResultBox data is drawn on.  It draws the
   words on the screen, and handles user input from the mouse.
6. ResultItem represents a single result object (a phrase or document).
7. PhindTitle is for drawing backdrops in ResultDisplays.

**********************************************************************/

package org.greenstone.applet.phind;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.net.URL;
import java.io.DataInputStream;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import java.util.Vector;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
//import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;


public class Phind extends java.applet.Applet 
    implements ActionListener {
    
    // What is the collection called?
    public String collection;

    // Which phind classifier are we using? (There may be more than one.)
    public String classifier;

    // Internet address of phind resources
    public String library_address, phindcgi_address;

    // Initial search term
    public String initialSearch;

    // Number of phrases to retrieve at any one time
    public int phraseBlockSize;

    // Appearance parameters
    public boolean vertical;
    public int depth;

    // Font
    public int fontSize;
    public String fontName;
    public Font plainFont, boldFont;

    // Do we want a background image in the applet?
    public boolean showImage;
    public String backdrop_address;
    public Image backgroundImage;
    public boolean showBorder;

    // Colours
    public Color panel_fg, panel_bg,
	column_1_fg, column_1_bg, 
	column_2_fg, column_2_bg,
	highlight_fg, highlight_bg,
	thesaurus_fg, thesaurus_bg, thesaurus_bar_fg, thesaurus_bar_bg,
	expansion_fg, expansion_bg, expansion_bar_fg, expansion_bar_bg,
	document_fg, document_bg, document_bar_fg, document_bar_bg,
	message_fg, message_bg;
    
    // Column dimensions
    int column_1_width, column_2_width;

    // Where do we open new windows
    String searchWindowName, documentWindowName;

    // the mode of operation
    int mode;
    final int initMode = 0;
    final int idleMode = 1;
    final int searchMode = 2;

    // Elements of the control panel
    boolean showControlPanel;
    Label titleLabel;
    TextField wordField;
    Button searchButton, prevButton, nextButton;

    // Holders for the downloaded data
    Panel resultPanel;
    ResultDisplay firstDisplay, lastDisplay;

    // The time at which the last query finished
    Date lastQueryEndTime;

    // lastQueryEndTime is stored to ensure a 1 second gap between a query
    // returning and a new one beginning.  It is needed because the FAO
    // folks in Rome have a huge lag, and frquently click several times
    // while they wait; these clicks are turned into new queries, which
    // they await again.  It is no elegant solution, but it seems like the
    // easiest, given that I don't know threads.
    // 1. The search button is easy to disable, and is disabled when a
    //    socket connection is in progress.
    // 2. ResutCanvas widgets can'r be similarly disabled because the
    //    browser hides or wipes them, which looks very bad.
    // 3. I cannot just ignore clicks on canvasses because the browser
    //    caches the clicks while the socket connection is going on, and then
    //    sends them through afterwards, when the canvas is accepting clicks
    //    again.
    // 4. Current sequence of events is to record the time the last query
    //    ends, then whenever a click happens make sure a second has past.  if
    //    you double-click the the first query is sent, returns, end-tie is
    //    set, and the second (and any others made during query time) is
    //    *immediately* processed, but since 1 second hasn't past it is
    //    ignored.

  
    public String getAppletInfo() {
	return "Phind by Gordon Paynter (paynter@cs.waikato.ac.nz). Copyright 1997-2000.";
    }


    public void init() {

	mode = initMode;
	
	// Read applet parameters
	getParameters();

	// Initialise the user interface
	setBackground(panel_bg);
	lastQueryEndTime = new Date();

	// fonts used to output text
	plainFont = new Font(fontName, Font.PLAIN, fontSize);
	boldFont  = new Font(fontName, Font.BOLD, fontSize);

	// The phind applet layout manager
	setLayout(new BorderLayout());

	// Panel containing the displays is in the center of the display
	resultPanel = new Panel();
	if (vertical) {
	    resultPanel.setLayout(new GridLayout(depth,1,0,2));
	} else {
	    System.out.println("horizontal");
	    resultPanel.setLayout(new GridLayout(1,depth,2,0));
	}
	add("Center", resultPanel);

	// Create ResultDisplays and place into the interface
	ResultDisplay d1, d2 = null;
	firstDisplay = new ResultDisplay(this, null);
	resultPanel.add(firstDisplay);

	if (depth == 1) {
	    lastDisplay = firstDisplay;
	} else {
	    d1 = firstDisplay;
	    for (int i = 2; i <= depth; i++) {
		d2 = new ResultDisplay(this, d1);
		resultPanel.add(d2);
		d1 = d2;
	    }
	    lastDisplay = d2;
	}

	// The control panel
	initialiseControlPanel();

	// lets get started then
	setStatus("Welcome to Phind.");
	mode = idleMode;

	// Perform initial search, if requested
	if (initialSearch.length() > 0) {
	    searchForWord(initialSearch);
	}
    }


    // Display a message in the status bar
    void setStatus(String status) {
	showStatus(status);
    }

    // The user performs an action in the interface
    /*   public boolean action(Event evt, Object arg) {

	if (evt.target == searchButton) {
	    System.out.println("evt.target==searchButton");
	    searchForWord(getSearchTerm());
	} else if (evt.target == wordField) {
	    System.out.println("evt.target==wordField");
	    searchForWord(getSearchTerm());
	} else if (evt.target == prevButton) {
	    shiftPrevious();
	} else if (evt.target == nextButton) {
	    shiftNext();
	} else {
	    System.out.println("unknown action: " + evt.toString() 
			       + ", object: " + arg.toString());
	}
	return true;
    }

    */

    public void actionPerformed(ActionEvent evt) {
	
	Component target = (Component)evt.getSource();
	if (target==searchButton) {
	    System.out.println("search button pressed");
	    searchForWord(getSearchTerm());
	} else if (target == wordField)  {
	    System.out.println("word field entered");
	    searchForWord(getSearchTerm());
	} else if (target == prevButton) {
	    System.out.println("prev button pressed");
	    shiftPrevious();
	}else if (target == nextButton) {
	    System.out.println("prev button pressed");
	    shiftNext();
	} else {
	    System.out.println("unknown action: " + evt.toString() );
	    
	}
    }

    // Search for a word 
    //
    // Called on two occasions:
    //   when the "Search" Button is pressed, or
    //   to perform an "initial search"
    void searchForWord(String searchWord) {

	System.err.println("in searchforword!!");
	if (mode == idleMode) {
	    
	    setSearchTerm(searchWord);

	    // Convert the String from UTF8 charaters into 
	    // an encoding that is okay for a URL.
	    searchWord = URLUTF8Encoder.encode(searchWord);

	    // Look up the word
	    if (searchWord.length() > 1) {
		setStatus("searching for \"" + searchWord + "\""); 
		firstDisplay.emptyContents();
		ResultBox result = lookupPhraseOnServer(null, false, searchWord, searchWord, 2);

		// if there is an error, return
		if (result == null) {
		    setStatus("No results for \"" + searchWord + "\""); 
		    return;
		}

		// display the result
		result.display = firstDisplay.display(result);
		result.setSize(result.display.getSize());
		result.paintAll(result.getGraphics());
	    }

	    enablePreviousAndNext();
	}
    }
  

    // Search for a phrase
    //
    // If querymode is 2, the user has clicked on a phrase.
    // If querymode is 3, the user has requested more phrases.
    // If querymode is 4, the user has requested more documents.
    void searchForPhrase(ResultBox source, String key, String phrase, int queryMode) {

	// System.out.println("searchForPhrase: " + key + " " + phrase + " " + queryMode);

	if (mode == idleMode) {

	    // If we are going to replace the first ResultDisplay, then empty it
	    if (queryMode <= 2) {
		if (source.display.next != null) source.display.next.emptyContents();
	    }

	    // look up the word
	    setStatus("Searching for \"" + phrase + "\""); 
	    ResultBox result = lookupPhraseOnServer(source, true, key, phrase, queryMode);
	    if (result == null) {
		setStatus("No result for \"" + phrase + "\""); 
		return;
	    }

	    // If this is not already displayed, display it in the last free spot
	    if (queryMode <= 2) {
		result.display = lastDisplay.display(result);
		result.setSize(result.display.getSize());
		result.paintAll(result.getGraphics());
	    }

	    enablePreviousAndNext();
	}
    }


    // Look up a phrase (or symbol) on the server
    //
    // Arguments are the source of the query (a ResultBox, or null if the
    // query comes from hitting the search button), the key to search for
    // (the text of a phrase or a symbol number), the phrase as a string, 
    // and the query mode.
    // Query modes are: 
    //   0 = obsolete
    //   1 = obsolete
    //   2 = get first N phrases and URLs,
    //   3 = get another N phrases into same window
    //   4 = get another N documents into same window
    //   5 = get another N thesaurus links into same window

    ResultBox lookupPhraseOnServer(ResultBox source, 
				   boolean keyKnown, String key, String phrase, 
				   int queryMode) { 
    	disableSearchButton();
	mode = searchMode;
	ResultBox r = null;

	if (queryMode <= 2) {
	    r = new ResultBox(this, collection, key, phrase, source);
	} else if ((queryMode == 3) || (queryMode == 4) || (queryMode == 5)) {
	    r = source;
	}

	try { 
	    queryServer(keyKnown, key, queryMode, r);
	} catch (Exception e) { 
	    System.out.println("Phind query error: " + e.toString());
	    setStatus("Query error: " + e.toString());
	    mode = idleMode;
	    enableSearchButton();
	    return null;
	}
      
	// The query is finished
	setStatus(r.c.numberOfItems + " results for \"" + phrase + "\""); 
	mode = idleMode;
	enableSearchButton();
	lastQueryEndTime = new Date();

	return r;
    }


    // Query the phindcgi program
    //
    // Send a query (a word or symbol number) to the server
    // and pass the response to a ResultBox.

    void queryServer(boolean keyKnown, String word, int queryMode, ResultBox area) 
	throws IOException {

	// Build the query
	String query = phindcgi_address + "c=" + collection + "&pc=" + classifier;

	if (keyKnown) {
	  query = query + "&ppnum=" + word;
	} else {
	  query = query + "&ppnum=0" + "&pptext=" + word;
	}

	
	// Specify the set of results to return 
	int first_e = 0;
	int last_e = 0;
	int first_d = 0;
	int last_d = 0;
	int first_l = 0;
	int last_l = 0;

	// the initial query
	if (queryMode <= 2) {
	  last_e = phraseBlockSize;
	  last_d = phraseBlockSize;
	  last_l = phraseBlockSize;
	}

	// add phrases to an existing result set
	else if (queryMode == 3) {
	  first_e = area.nextPhraseBlock * phraseBlockSize;
	  area.nextPhraseBlock++;
	  last_e = area.nextPhraseBlock * phraseBlockSize;
	} 
	
	// add documents to existing result set
	else if (queryMode == 4) {
	  first_d = area.nextDocumentBlock * phraseBlockSize;
	  area.nextDocumentBlock++;
	  last_d = area.nextDocumentBlock * phraseBlockSize;
	}

	// add thesaurus links to existing result set
	else if (queryMode == 5) {
	  first_l = area.nextThesaurusLinkBlock * phraseBlockSize;
	  area.nextThesaurusLinkBlock++;
	  last_l = area.nextThesaurusLinkBlock * phraseBlockSize;
	}

	query = query + "&pfe=" + first_e + "&ple=" + last_e
                      + "&pfd=" + first_d + "&pld=" + last_d
                      + "&pfl=" + first_l + "&pll=" + last_l;

	// Send the query to the phindcgi program 
	System.out.println("1:sending query: " + query);
	try {
	    URL phindcgi = new URL(query);
	    DataInputStream in = new DataInputStream(phindcgi.openStream());
	    DOMParser parser = new DOMParser();
	    parser.parse(new InputSource(in));
	    Document data_doc = parser.getDocument();
	    Element data_elem = data_doc.getDocumentElement();
	    area.parseXML(data_elem);
	    in.close();
	} catch (Exception e) {
	    System.err.println( "Error sending query to phindcgi: " + e);
	    e.printStackTrace();
	}
	area.repaint();
    }
    

    // Tidy up URLs
    //
    // Ensure a URL address (as string) has a protocol, host, and file.
    //
    // If the URL is a CGI script URL, it should be tidied up so that it is
    // appropriate to tage attrib=value pairs on the end.  This means it
    // must either end with a "?" or (if it contains a question-mark
    // internally) end with a "&".
    String tidy_URL(String address, boolean isCGI) {

	// System.err.println("tidy URL: " + address);
	
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

	// if the URL is a cgi script, make sure it has a "?" in ti,
	// and that it ends with a "?" or "&"
	if (isCGI) {
	    if (address.indexOf((int) '?') == -1) { 
		address = address + "?";
	    } else if (!address.endsWith("?")) {
		address = address + "&";
	    }
	}

	return address;
    }



    // Open an arbitrary web page
    void displayWebPage(String address, String window) {
	try { 
	    URL url= new URL(address);
	    if (window.length() > 0) {
		getAppletContext().showDocument(url, window);
	    } else {
		getAppletContext().showDocument(url);
	    }
	} catch (Exception e) { 
	    System.out.println("Cannot open web page: " + e.toString());
	}
    }


    // Get the applet parameters
    void getParameters() {

	// What is this collection called?
	collection = parameterValue("collection");
	System.out.println("Phind collection: " + collection);

	// Which of the collection's classifiers are we using?
	classifier = parameterValue("classifier", "1");
	System.out.println("Phind classifier: " + classifier);

	// Where is the Greenstone library
	library_address = parameterValue("library");
	library_address = tidy_URL(library_address, true);
	System.out.println("Phind library: " + library_address);

	// Where is the phind CGI script
	// we assume this is relative to the greenstone library
	phindcgi_address = parameterValue("library")+parameterValue("phindcgi");
	phindcgi_address = tidy_URL(phindcgi_address, true);
	System.out.println("Phind phindcgi: " + phindcgi_address);

	
	// Is there a default search term?
	initialSearch = parameterValue("initial_search", "");

	// Should we display the control panel
	showControlPanel = true;
	if (parameterValue("control_panel", "show").toLowerCase().equals("hide")) {
	    showControlPanel = false;
	}
	
	// Should we show a background image?
	backdrop_address = parameterValue("backdrop", "");
	if (backdrop_address.length() > 0) {
	    backdrop_address = tidy_URL(backdrop_address, false);
	    System.out.println("Phind backdrop URL: " + backdrop_address);

	    try {
		URL backdrop_url = new URL(backdrop_address);
		backgroundImage = getImage(backdrop_url);
		showImage = true;
	    } catch (Exception e) { 
		System.out.println("Phind could not load " + backdrop_address);
		showImage = false;
	    }
	}

	// Should we draw a border?
	showBorder = parameterValue("border", "on").equals("off");

	// Are the windows arranged vertically or horizontally
	if (parameterValue("orientation", "vertical").toLowerCase().startsWith("hori")) {
	    vertical = false;
	} else {
	    vertical = true;
	}
	
	// How many phind windows are there?
	depth = parameterValue("depth", 3);

	// Result sort order
	// Standard is "LlEeDd", expansion-first is "EeLlDd"
	String order = parameterValue("resultorder", "standard");
	if (!order.equals("standard")) {
	    int next = 20;
	    ResultItem.sortMessage = next;
	    for (int x = 0; x < order.length(); x++) {
		if (order.charAt(x) == ',') {
		    next--;
		} else if (order.charAt(x) == 'L') {
		    ResultItem.sortLinkItem = next;
		} else if (order.charAt(x) == 'l') {
		    ResultItem.sortMoreLinks = next;
		} else if (order.charAt(x) == 'E') {
		    ResultItem.sortPhraseItem = next;
		} else if (order.charAt(x) == 'e') {
		    ResultItem.sortMorePhrases = next;
		} else if (order.charAt(x) == 'D') {
		    ResultItem.sortDocumentItem = next;
		} else if (order.charAt(x) == 'd') {
		    ResultItem.sortMoreDocuments = next;
		}
	    }
	    System.out.println("link: " + ResultItem.sortLinkItem);
	    System.out.println("exps: " + ResultItem.sortPhraseItem);
	    System.out.println("docs: " + ResultItem.sortDocumentItem);

	}

	// How many phrases should we fetch at any given time?
	phraseBlockSize = parameterValue("blocksize", 10);

	// What font should we use?
	fontSize = parameterValue("fontsize", 10);
	fontName = parameterValue("fontname", "Helvetica");

	// Column dimensions
	column_1_width = parameterValue("first_column_width", 6);
	column_2_width = parameterValue("second_column_width", column_1_width);
	
	// Where do we open new windows
	searchWindowName = parameterValue("search_window", "phindsearch");
	documentWindowName = parameterValue("document_window", "phinddoc");

	// Colours
	panel_fg = parameterValue("panel_fg", Color.black);
	panel_bg = parameterValue("panel_bg", Color.white);

	highlight_bg = parameterValue("highlight_bg", Color.yellow);

	expansion_fg = parameterValue("expansion_fg", Color.black);
	thesaurus_fg = parameterValue("thesaurus_fg", new Color(0, 100, 0));
	document_fg = parameterValue("document_fg", Color.blue);

	thesaurus_bar_fg = parameterValue("thesaurus_bar_fg", Color.black);
	expansion_bar_fg = parameterValue("expansion_bar_fg", Color.black);
	document_bar_fg = parameterValue("document_bar_fg", Color.black);

	thesaurus_bar_bg = parameterValue("thesaurus_bar_bg", new Color(160, 160, 190));
	expansion_bar_bg = parameterValue("expansion_bar_bg", new Color(255, 200, 200));
	document_bar_bg = parameterValue("document_bar_bg", new Color(150, 193, 156));

	column_1_fg = parameterValue("first_column_fg", Color.black);
	column_1_bg = parameterValue("first_column_bg", new Color(235, 245, 235));
	column_2_fg = parameterValue("second_column_fg", Color.black);
	column_2_bg = parameterValue("second_column_bg", new Color(200, 220, 200));

	message_fg = parameterValue("message_fg", Color.black);
	message_bg = parameterValue("message_bg", Color.white);

	// Colours I don't use, yet
	// thesaurus_bg = parameterValue("thesaurus_bg", Color.white);
	// expansion_bg = parameterValue("expansion_bg", Color.white);
	// document_bg = parameterValue("document_bg", Color.white);
    }

    // Get the value of a parameter given its name.
    // There are many types of parameters, hence the variety of functions.

    // Get a REQUIRED string.  Stop the applet if we cannot.
    String parameterValue(String name) {
	try { 
	    return getParameter(name);
	} catch (Exception e) {
	    System.err.println("Phind: you must give a parameter for \"" 
			       + name + "\".  Stopping.");
	    stop();
	}
	return "";
    }

    // Get an optional parameter.  Return a default if we cannot.
    String parameterValue(String name, String defaultValue) {
	String text = getParameter(name);
	if (text == null) {
	    return defaultValue;
	}
	System.out.println("Phind " + name + ": " + text);
	return text;
    }

    int parameterValue(String name, int defaultValue) {
	int value;
	try { 
	    value = Integer.parseInt(getParameter(name));
	} catch (Exception e) {
	    return defaultValue;
	}
	System.out.println("Phind " + name + ": " + value);
	return value;
    }

    Color parameterValue(String name, Color defaultValue) {

	String text = getParameter(name);
	if (text == null) {
	    return defaultValue;
	}
	text = text.toLowerCase();

	// a number of the form "#ddffee" 
	if (text.startsWith("#") && (text.length() == 7)) {
	    text = text.substring(1);
	    int r, g, b;
	    try {
		r = Integer.parseInt(text.substring(0,2), 16);
		g = Integer.parseInt(text.substring(2,4), 16);
		b = Integer.parseInt(text.substring(4), 16);
		return new Color(r, g, b);
	    }  catch (Exception e) {
		return defaultValue;
	    }
	}

	// a known Java colour string
	else if (text.equals("black")) { return Color.black; }
	else if (text.equals("blue")) { return Color.blue; }
	else if (text.equals("cyan")) { return Color.cyan; }
	else if (text.equals("darkgray")) { return Color.darkGray; }
	else if (text.equals("gray")) { return Color.gray; }
	else if (text.equals("green")) { return Color.green; }
	else if (text.equals("lightgray")) { return Color.lightGray; }
	else if (text.equals("magenta")) { return Color.magenta; }
	else if (text.equals("orange")) { return Color.orange; }
	else if (text.equals("pink")) { return Color.pink; }
	else if (text.equals("red")) { return Color.red; }
	else if (text.equals("white")) { return Color.white; }
	else if (text.equals("yellow")) { return Color.yellow; }

	return defaultValue;
    }


    // Control panel operations

    // Initialise the control panel
    void initialiseControlPanel() {
	
	if (showControlPanel) {
	    Panel p1 = new Panel();
	    add("North", p1);
	    p1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
	    
	    searchButton = new Button("Search");
	    searchButton.setFont(boldFont);
	    //searchButton.setEnabled(true);
	    searchButton.addActionListener(this);
	    p1.add(searchButton);
	    
	    Label tempLabel = new Label("  for");
	    tempLabel.setFont(boldFont);
	    p1.add(tempLabel);
	    
	    wordField = new TextField(12);
	    wordField.setFont(boldFont);
	    wordField.addActionListener(this);
	    p1.add(wordField);
	    
	    Label temp2 = new Label("          ");
	    p1.add(temp2);
	    
	    prevButton = new Button("Previous");
	    prevButton.setFont(boldFont);
	    prevButton.addActionListener(this);
	    prevButton.setEnabled(false);
	    
	    p1.add(prevButton);
	    
	    nextButton = new Button("  Next  ");
	    nextButton.setFont(boldFont);
	    nextButton.addActionListener(this);
	    nextButton.setEnabled(false);
	    p1.add(nextButton);
	    
	}
    }
    
    // Button and field functionality
    
    // Enable and disable the word field
    void enableSearchButton() {
	if (showControlPanel) {
	    searchButton.setEnabled(true);
	}
    }
    void disableSearchButton() {
	if (showControlPanel) {
	    searchButton.setEnabled(false);
	}
    }

    // Get and set the search text in the wordField
    String getSearchTerm() {
	if (showControlPanel) {
	    return wordField.getText();
	} else {
	    return initialSearch;
	}
    }
    void setSearchTerm(String word) {
	if (showControlPanel) {
	    wordField.setText(word);
	} 
    }

    // Enable or disable the "Previous" and "Next" buttons
    void enablePreviousAndNext() {
	if (showControlPanel) {
	    Component c = firstDisplay.current;
	    if (c.getClass().getName().endsWith("ResultBox")) {
		if (((ResultBox) c).prevBoxExists()) {
		    prevButton.setEnabled(true);
		} else {
		    prevButton.setEnabled(false);
		}
	    }
	    
	    c = lastDisplay.current;
	    if (c.getClass().getName().endsWith("ResultBox")) {
		if (((ResultBox) c).nextBoxExists()) {
		    nextButton.setEnabled(true);
		} else {
		    nextButton.setEnabled(false);
		}
	    }
	}
    }

    // Shift to previous box
    //
    // If the user clicks "Previous" then scroll up.
    void shiftPrevious() {

	Component c = firstDisplay.current;
	if (c.getClass().getName().endsWith("ResultBox")) {

	    ResultBox b = (ResultBox) c;
	    if (b.prevBoxExists()) {
		b = b.prev;

		// empty all the displays
		firstDisplay.emptyContents();

		// add as many result boxes as there are displays
		for (int i = 1 ; ((i <= depth) && (b != null)); i++) {
		    lastDisplay.display(b);
		    b.setSize(b.display.getSize());
		    b.paintAll(b.getGraphics());
		    b = b.next;
		}
	    }
	}
	enablePreviousAndNext();
    }

    // Shift to next box
    //
    // If the user clicks "Next" then scroll down if possible
    void shiftNext() {

	Component c = lastDisplay.current;
	if (c.getClass().getName().endsWith("ResultBox")) {

	    ResultBox b = (ResultBox) c;
	    if (b.nextBoxExists()) {

		// find the new "first" displayed box
		c = firstDisplay.current;
		b = (ResultBox) c;
		b = b.next;

		// empty all the displays
		firstDisplay.emptyContents();

		// add as many result boxes as there are displays
		for (int i = 1 ; ((i <= depth) && (b != null)); i++) {
		    lastDisplay.display(b);
		    b.setSize(b.display.getSize());
		    b.paintAll(b.getGraphics());
		    b = b.next;
		}
	    }
	}
	enablePreviousAndNext();
    }





}
