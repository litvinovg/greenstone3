/**********************************************************************
 *
 * ResultCanvas.java -- a Canvas onto which a phrase list is drawn
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

This class is used in the Phind java applet (Phind.java).

The results of a query are displayed on a ResultCanvas object.  Each 
entry in the result list is stored in a ResultItem, and the ResultCanvas 
contains a Vector of ResultItems.

Each ResultCanvas is embedded in a ResultBox alongside a Scrollbar.  
When the ResultCanvas is drawn, it looks at the scrollbar, calculates 
which ResultItems are visible, then draws them on the screen.

**********************************************************************/

package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

import java.awt.Canvas;
import java.awt.Scrollbar;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Color;
import java.util.Vector;
import java.awt.Image;
//import java.awt.Event;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.net.URL;
import java.applet.Applet;
import java.util.Date;

public class ResultCanvas extends Canvas {
    

    // Other objects associated with this 
    Phind phind;
    ResultBox parent;
    Scrollbar scrollbar;
    
    // fonts and font spacings to use
    Font areaPlain, areaBold;
    int lineSpacing;
    
    // The items to display on this canvas
    int numberOfItems;
    int firstItemDisplayed;
    int itemSelected;
    Vector<ResultItem> items;
    
    // the background image
    public static Image backgroundImage;
    
    
    // Create a ResultCanvas from the ResultBox which is its parent.
    ResultCanvas(ResultBox p) {
	
	parent = p;
	phind = p.phind;
	scrollbar = p.s;
	parent.disableScrollbar();

	areaPlain = phind.plainFont;
	areaBold  = phind.boldFont;

	lineSpacing = phind.fontSize + 2;

	items = new Vector<ResultItem>();
	numberOfItems = 0;
	firstItemDisplayed = 0;
	itemSelected = -1;
    
	if (backgroundImage == null) {
	    backgroundImage = p.phind.backgroundImage;
	}

	// add a mouse listener for the mouse clicks. we only want to override
	// one method, so we use the adaptor class
	addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent evt) {
		    ResultCanvas.this.mouseClicked(evt);
		}
	    });
	

    }

    void resetCanvas( ) {
	items.removeAllElements();
	numberOfItems = 0;
	repaint();
    }


    // Add a new search result.
    // Return true if successful, otherwise false.
    boolean addResultItem( ResultItem item ) {
 
	// Add a new result, in sorted order.
	// First sort key is item kind (lowest first),
	// second is frequency (highest first).
	// This is not efficient, but I don't care right now.
	int index = 0;
	while ((index < numberOfItems) &&
	       ((item.sort < items.elementAt(index).sort) ||
		((item.sort == items.elementAt(index).sort) &&
		 (item.frequency <= items.elementAt(index).frequency)))) {
	    index++;
	}
	items.insertElementAt(item,index);
	numberOfItems++;
	return true;
    }


    // Update more phrases/documents/links markers
    void updateMarkers() {
	updateMorePhrasesMarker();
	updateMoreDocumentsMarker();
	updateMoreLinksMarker();
    }

    // Make sure the more Phrases item only appears in the list
    // if it is meant to.
    void updateMorePhrasesMarker() {
    
	 System.out.println("updateMorePhrasesMarker() ");
	 System.out.println("expansions retrieved = "+parent.expansionsRetrieved +" num expansions = "+parent.numberOfExpansions);
	// look for a marker
	boolean found = false;
	int index = 0;
	while (!found && (index < numberOfItems)) {
	    if (items.elementAt(index).isMorePhrases()) {
		found = true;
	    } else {
		index++;
	    }
	}

	if (parent.expansionsRetrieved == parent.numberOfExpansions) {
	    // there should be no marker
	    if (found) {
		items.removeElementAt(index);
		numberOfItems--;
	    }

	} else if (parent.expansionsRetrieved < parent.numberOfExpansions) {
	    if (!found) {
		ResultItem ri = new ResultItem(ResultItem.morePhrases);
		addResultItem(ri);
	    }
	}
    }


    // Make sure the more documents marker appears when required
    void updateMoreDocumentsMarker() {
    
	// System.out.println("updateMoreDocumentsMarker() ");

	// look for a marker
	boolean found = false;
	int index = 0;
	while (!found && (index < numberOfItems)) {
	    if (items.elementAt(index).isMoreDocuments()) {
		found = true;
	    } else {
		index++;
	    }
	}

	if (parent.documentsRetrieved == parent.numberOfDocuments) {
	    // there should be no marker
	    if (found) {
		items.removeElementAt(index);
		numberOfItems--;
	    }

	} else if (parent.documentsRetrieved < parent.numberOfDocuments) {
	    // there should be a marker
	    if (!found) {
		ResultItem ri = new ResultItem(ResultItem.moreDocuments);
		addResultItem(ri);
	    }
	}
    }

      
    // Make sure the more links marker appears when required
    void updateMoreLinksMarker() {
    
	// System.out.println("updateMoreLinksMarker() ");

	// look for a marker
	boolean found = false;
	int index = 0;
	while (!found && (index < numberOfItems)) {
	    if (items.elementAt(index).isMoreLinks()) {
		found = true;
	    } else {
		index++;
	    }
	}

	if (parent.thesaurusLinksRetrieved == parent.numberOfThesaurusLinks) {
	    // there should be no marker
	    if (found) {
		items.removeElementAt(index);
		numberOfItems--;
	    }

	} else if (parent.thesaurusLinksRetrieved < parent.numberOfThesaurusLinks) {
	    // there should be a marker
	    if (!found) {
		ResultItem ri = new ResultItem(ResultItem.moreLinks);
		addResultItem(ri);
	    }
	}
    }

      

 


    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics g) {

	// calculate the canvas size, margins, and spacing
	
	// Note: the size() method is deprecated, but getSize fails in NetScape 4
	Dimension canvasSize = getSize();
	
	g.setFont(areaPlain);
	int space = g.getFontMetrics().stringWidth(" ");
	int nought = g.getFontMetrics().stringWidth("0");

	int firstColumnWidth = phind.column_1_width * nought;
	int secondColumnWidth = phind.column_2_width * nought;
	int columnWidth = firstColumnWidth + secondColumnWidth;

	int firstColumnLeft = canvasSize.width - columnWidth;
	int secondColumnLeft = firstColumnLeft + firstColumnWidth;

	int mainLeftMargin = 0;
	int mainRightMargin = firstColumnLeft;

	// the number of items that will fit on the canvas
	int visible = canvasSize.height / lineSpacing;

	// calculate the first item to output
	int scrollValue = scrollbar.getValue();
	if (numberOfItems <= visible) {
	    scrollValue = 0;
	} else if (scrollValue > (numberOfItems - visible)) {
	    scrollValue = numberOfItems - visible;
	} 
	firstItemDisplayed = scrollValue;

	// Draw the phrase area
	Color fore = phind.panel_fg;
	Color back = phind.panel_bg;


	// draw the background
	if (phind.showImage) {
	    try {
		g.drawImage(backgroundImage, 
			    mainLeftMargin, 0, mainRightMargin, canvasSize.height, 
			    back, null);
	    } catch (Exception e) {
		phind.showImage = false;
		System.err.println("ResultCanvas paint error: " + e); 
		g.setColor(back);
		g.fillRect(0, 0, canvasSize.width, canvasSize.height);
	    }
	} else {
	    g.setColor(back);
	    g.fillRect(0, 0, canvasSize.width, canvasSize.height);
	}
    
	// If there are no phrases, output a brief explanation.
	if (numberOfItems == 0) {
	    g.drawString("No phrases match this query.", mainLeftMargin + 10, lineSpacing);
	}

	// Output each of the visible ResultItems
	ResultItem result;
	int tab, i, y = 0;
	int center = mainLeftMargin 
	    + ((mainRightMargin - mainLeftMargin 
		- g.getFontMetrics().stringWidth(parent.searchPhrase)) / 2);
	int thesColumn = mainLeftMargin + firstColumnWidth 
	    + g.getFontMetrics().stringWidth("Narrower term");

	String body, prefix, suffix;
	
	for (i = scrollValue; 
	     (i < numberOfItems) && (y + lineSpacing < canvasSize.height); i++) {

	    // Get the resultItem to output
	    result = items.elementAt(i);
	    
	    // Graphics settings for drawing this line
	    y += lineSpacing;
	    g.setFont(areaPlain);

	    // Highlight the selected phrase.
	    if (i == itemSelected) {
		g.setColor(phind.highlight_bg);
		g.fillRect(mainLeftMargin, y-lineSpacing+2, mainRightMargin, lineSpacing);
	    }

	    // Draw the item

	    // Draw a phrase item
	    if (result.isPhrase()) {
		prefix = result.prefixText();
		body = result.mainText();
		suffix = result.suffixText();
		
		g.setColor(phind.expansion_fg);
		g.setFont(areaPlain);
		tab = center - g.getFontMetrics().stringWidth(prefix) - space;
		g.drawString(prefix, tab, y);
		
		g.setFont(areaBold);
		g.drawString(body, center, y);
		
		tab = center + space + g.getFontMetrics().stringWidth(body);
		g.setFont(areaPlain);
		g.drawString(suffix, tab, y);
	
	    } 

	    // Draw a document item
	    else if (result.isDocument()){
		body = result.mainText();

		g.setColor(phind.document_fg);
		g.setFont(areaPlain);
		tab = (mainRightMargin - g.getFontMetrics().stringWidth(body)) / 2;
		g.drawString(body, tab, y);
      
	    } 
	
	    // Draw a thesaurus link item
	    else if (result.isLink()){
		prefix = result.prefixText() + ":";
		body = result.mainText();
		tab = thesColumn - g.getFontMetrics().stringWidth(prefix) - space;

		g.setColor(phind.thesaurus_fg);
		g.setFont(areaPlain);
		g.drawString(prefix, tab, y);
		g.setFont(areaBold);
		g.drawString(body, thesColumn, y);
	    } 
	    
	    // Draw a "more phrases/documents/links" marker
	    else if (result.isMorePhrases() 
		     || result.isMoreDocuments() 
		     || result.isMoreLinks()){
		body = result.mainText();
		
		if (result.isMorePhrases()) {
		    g.setColor(phind.expansion_bar_bg);
		} else if (result.isMoreDocuments()) {
		    g.setColor(phind.document_bar_bg);
		} else {
		    g.setColor(phind.thesaurus_bar_bg);		    
		}
		g.fillRect(mainLeftMargin, y-lineSpacing+2, mainRightMargin, lineSpacing);

		if (result.isMorePhrases()) {
		    g.setColor(phind.expansion_bar_fg);
		} else if (result.isMoreDocuments()) {
		    g.setColor(phind.document_bar_fg);
		} else {
		    g.setColor(phind.thesaurus_bar_fg);		    
		}
		g.setFont(areaPlain);
		tab = (mainRightMargin - g.getFontMetrics().stringWidth(body)) / 2;
		g.drawString(body, tab, y);
	    } 
	}

	// Draw the frequecy columns

	// column backgrounds
	g.setColor(phind.column_1_bg);
	g.fillRect(firstColumnLeft, 0, firstColumnWidth, canvasSize.height);
	g.setColor(phind.column_2_bg);
	g.fillRect(secondColumnLeft, 0, secondColumnWidth, canvasSize.height);

	// fill in the numbers
	g.setColor(phind.column_1_fg);
	g.setFont(areaPlain);
	y = 0;
	String docsText, freqText;

	for (i = scrollValue; 
	     (i < numberOfItems) && (y + lineSpacing < canvasSize.height); i++) {
	    
	    // Get the resultItem to output
	    result = items.elementAt(i);
	    docsText = result.docsText();
	    freqText = result.freqText();
	    
	    // Graphics settings for drawing this line
	    y += lineSpacing;

	    // Write the document frequency
	    if (docsText.length() > 0) {
		tab = secondColumnLeft - space - g.getFontMetrics().stringWidth(docsText);
		g.drawString(docsText, tab, y);
	    }

	    // Write the term frequency
	    if (freqText.length() > 0) {
		tab = secondColumnLeft + secondColumnWidth 
		    - space - g.getFontMetrics().stringWidth(freqText);
		g.drawString(freqText, tab, y);
	    }
	}

	// Adjust the scrollbar
	if (visible >= numberOfItems) {
	    parent.disableScrollbar();
	} else {
	    scrollbar.setValues(scrollValue, visible, 0, numberOfItems);
	    // Deprecated - setPageIncrement() has been replaced by
	    // setBlockIncrement(), but the latter doesn't work on some
	    // browsers.  Damn.
	    scrollbar.setBlockIncrement(visible - 1);
	    //scrollbar.setPageIncrement(visible - 1);
	    // Deprecated - more nonsense: enable() has been replaced by
	    // setEnabled(true), but this doesn't work on some browsers.
	    scrollbar.setEnabled(true);
	    //scrollbar.enable();
	}

	
	// draw the border
	if (phind.showBorder) {
	    g.setColor(phind.panel_fg);
	    g.drawRect(0,0, canvasSize.width - 1, canvasSize.height - 1);
	}
    }

    // handle mouse clicks in the new way. just copy gordons thing for now
    public void mouseClicked(MouseEvent event) {

	// ignore actions that occur within 1 second of the last
	Date now = new Date();
	// System.out.println("Click time: " + now.toString());
	if (now.getTime() < (phind.lastQueryEndTime.getTime() + 1000)) {
	    System.out.println("Ignoring click - too close to last query.");
	    return;
	}

	// which Item is selected?
	int rowSelected = event.getY() / lineSpacing;
	itemSelected = rowSelected + firstItemDisplayed;
	ResultItem item = items.elementAt(itemSelected);

	if (itemSelected <= numberOfItems) {
	    
	    //User clicks on a phrase
	    if (item.isPhrase()) {
		itemSelected = itemSelected;
		update(getGraphics());
		parent.lookupPhrase(item.hiddenText(), item.toString(), 2);
		
		// If meta key is held down, send query to search engine
		/*if (event.metaDown() && !phind.library_address.equals("")) {
		    String address = phind.library_address  
			+ "a=q&sa=text&c=" + phind.collection
			+ "&q=%22" + item.toString().replace(' ', '+') + "%22";
		    phind.displayWebPage(address, phind.searchWindowName);
		    }*/
	    } 
	    
	    // Click on a thesaurus link
	    else if (item.isLink()) {
		itemSelected = itemSelected;
		update(getGraphics());
		parent.lookupPhrase(item.hiddenText(), item.toString(), 2);
		
		// If meta key is held down, send query to search engine
		/*if (event.metaDown() && !phind.library_address.equals("")) {
		    String address = phind.library_address  
			+ "a=q&sa=text&c=" + phind.collection
			+ "&q=%22" + item.toString().replace(' ', '+') + "%22";
		    phind.displayWebPage(address, phind.searchWindowName);
		    }*/
	    } 
	    
	    // The user clicks on a URL; display it.
	    else if (item.isDocument()) {
		itemSelected = itemSelected;
		update(getGraphics());
		
		String address = phind.library_address 
		    + "a=d&c=" + phind.collection 
		    + "&d=" + item.hiddenText()
		    + "&q=" + parent.searchPhrase.replace(' ', '+');
		
		phind.displayWebPage(address, phind.documentWindowName);
	    }
	    
	    // When the user clicks on "get more phrases" or other marker,
	    // we have to send a new query to the host
	    else if (item.isMorePhrases()){
		parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 3);
	    } else if (item.isMoreDocuments()){
		parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 4);
	    } else if (item.isMoreLinks()){
		parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 5);
		
		
	    }
	    repaint();
	}
    }
    

    // User interaction
    //
    // All interaction with the ResultCanvas is therough mouse clicks, and
    // is handles in this method.  Note we ignore clicks that follow
    // another too closely to avoid problems with slow connections.
    /*  public boolean processEvent(Event event) {
    
	if (event.id == Event.MOUSE_UP) {

	    // ignore actions that occur within 1 second of the last
	    Date now = new Date();
	    // System.out.println("Click time: " + now.toString());
	    if (now.getTime() < (phind.lastQueryEndTime.getTime() + 1000)) {
		System.out.println("Ignoring click - too close to last query.");
		return true;
	    }

	    // which Item is selected?
	    int rowSelected = event.y / lineSpacing;
	    itemSelected = rowSelected + firstItemDisplayed;
	    ResultItem item = (ResultItem) items.elementAt(itemSelected);

	    if (itemSelected <= numberOfItems) {
	
		//User clicks on a phrase
		if (item.isPhrase()) {
		    itemSelected = itemSelected;
		    update(getGraphics());
		    parent.lookupPhrase(item.hiddenText(), item.toString(), 2);

		    // If meta key is held down, send query to search engine
		    if (event.metaDown() && !phind.library_address.equals("")) {
			String address = phind.library_address  
			    + "a=q&sa=text&c=" + phind.collection
			    + "&q=%22" + item.toString().replace(' ', '+') + "%22";
			phind.displayWebPage(address, phind.searchWindowName);
		    }
		} 

		// Click on a thesaurus link
		else if (item.isLink()) {
		    itemSelected = itemSelected;
		    update(getGraphics());
		    parent.lookupPhrase(item.hiddenText(), item.toString(), 2);

		    // If meta key is held down, send query to search engine
		    if (event.metaDown() && !phind.library_address.equals("")) {
			String address = phind.library_address  
			    + "a=q&sa=text&c=" + phind.collection
			    + "&q=%22" + item.toString().replace(' ', '+') + "%22";
			phind.displayWebPage(address, phind.searchWindowName);
		    }
		} 

		// The user clicks on a URL; display it.
		else if (item.isDocument()) {
		    itemSelected = itemSelected;
		    update(getGraphics());

		    String address = phind.library_address 
			+ "a=r&c=" + phind.collection 
			+ "&r=" + item.hiddenText()
			+ "&q=" + parent.searchPhrase.replace(' ', '+');

		    phind.displayWebPage(address, phind.documentWindowName);
		}
		 
		// When the user clicks on "get more phrases" or other marker,
		// we have to send a new query to the host
		else if (item.isMorePhrases()){
		    parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 3);
		} else if (item.isMoreDocuments()){
		    parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 4);
		} else if (item.isMoreLinks()){
		    parent.lookupPhrase(parent.searchKey, parent.searchPhrase, 5);


		}
		repaint();
	    }
	}
	return true;
	} */   
  
}



