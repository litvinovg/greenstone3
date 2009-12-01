/**********************************************************************
 *
 * ResultBox.java -- a list of phrases in the Phind java interface
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

A ResultBox holds the results of a query to phindcgi.  They deal mostly
with the information content of the query, and have methods for parsing the
input into phrase and document items.  They have little do with display:
ResultBoxes are shown to the user through "ResultDisplay" panels, and are
drawn using "ResultCanvas" objects.

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

import java.awt.Panel;
import java.awt.BorderLayout;
import java.awt.Scrollbar;

import java.awt.Label;
import java.awt.AWTEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.net.*;
import java.applet.*;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResultBox extends Panel 
    implements AdjustmentListener {
    
    // Objects at a "higher" level than this one
    Phind phind;
    ResultDisplay display;

    // Surroundig objects
    ResultBox prev, next;

    // The components contained by this one.
    ResultCanvas c;
    ResultTitle t;
    Scrollbar s;
    Panel label;

    // The key identifying the phrase displayed, with its text and the
    // collection from which it is drawn
    String searchKey, searchPhrase, searchCollection;
    
    // The total frequency, expansion frequency, and document frequency
    // of the phrase
    int numberOfOccurances;
    int numberOfExpansions;
    int numberOfDocuments;
    int numberOfThesaurusLinks;

    // The number of phrases and documents retrieved, and the number of
    // times the user has requested more phrases or documents.
    int expansionsRetrieved;
    int documentsRetrieved;
    int thesaurusLinksRetrieved;
    int nextPhraseBlock;
    int nextDocumentBlock;
    int nextThesaurusLinkBlock;
    
    int mode;
    final int initMode = 0;
    public final int emptyMode = 1;
    final int loadingMode = 2;
    final int finishedMode = 3;

    String buffer;
    boolean finished;


    // Create a ResultBox
    // given details of the search that generated it.
    ResultBox(Phind p, String collect, String key, String phrase, ResultBox rb) {

	super();
	mode = initMode;

	phind = p;
	display = null;
	next = null;
	prev = rb;
	if (prev != null) prev.next = this;

	searchKey = key;
	searchPhrase = phrase;
	searchCollection = collect;

	numberOfOccurances = -1;
	numberOfExpansions = -1;
	numberOfDocuments = -1;
	numberOfThesaurusLinks = -1;

	expansionsRetrieved = 0;
	documentsRetrieved = 0;
	thesaurusLinksRetrieved = 0;

	nextPhraseBlock = 1;
	nextDocumentBlock = 1;
	nextThesaurusLinkBlock = 1;


	setLayout(new BorderLayout());

	s = new Scrollbar(Scrollbar.VERTICAL);
	disableScrollbar();
	s.addAdjustmentListener(this);
	add("East", s);

	c = new ResultCanvas(this);
	add("Center", c);

	t = new ResultTitle(this);
	add("North", t);

	buffer = "";
	finished = false;
	mode = emptyMode;
    }

    static String describeContents(String phrase, String c) {
	return( "\"" + phrase + "\" in " + c + ".");
    }

    String describeContents() {
	return( describeContents(searchPhrase, searchCollection) );
    }


    // Reset the contents of the box
    void resetBox( ) {
	buffer = "";
	finished = false;
	c.resetCanvas();
	disableScrollbar();
	mode = emptyMode;
    
	numberOfExpansions = -1;
	numberOfDocuments = -1;
	numberOfThesaurusLinks = -1;

	expansionsRetrieved = 0;
	documentsRetrieved = 0;
	thesaurusLinksRetrieved = 0;
    }

    void setStatus( String status ) {
	phind.setStatus(status);
    }

    void disableScrollbar() {
	if (s.isEnabled()) {
	    s.setValues(0, 1, 0, 1);
	    s.setUnitIncrement(1);
	    s.setBlockIncrement(1);
	    s.setEnabled(false);
	}
    }

    // Are there displays previous to and after this?
    public boolean prevBoxExists () {
	return (prev != null);
    }
    public boolean nextBoxExists () {
	return (next != null);
    }

    
    // Look up a phrase
    // Phrase lookups are passed on to the Phind applet itself.
    void lookupPhrase(String key, String phrase, int queryMode) {
	buffer = "";
	finished = false;
	phind.searchForPhrase(this, key, phrase, queryMode);
	t.repaint();
    }

    /*   public void processEvent(AWTEvent event) {
	//    System.out.println("event: " + event.toString());
	if ( (event.target == s) && 
	     ( (event.id == Event.SCROLL_ABSOLUTE) ||
	       (event.id == Event.SCROLL_LINE_DOWN) ||
	       (event.id == Event.SCROLL_LINE_UP) ||
	       (event.id == Event.SCROLL_PAGE_DOWN) ||
	       (event.id == Event.SCROLL_PAGE_UP) ) ) {
	    c.repaint();
	    
	} else {
	    super.processEvent(event);
	}
	} */  

    public void adjustmentValueChanged(AdjustmentEvent evt) {
	c.repaint();
    }

    void parseXML(Element data) {

	//System.out.println("phinddata:"+data.toString());

	//String id="", phrase="";
	//int df=0, ef=0, lf=0, tf=0;
	
	searchKey = data.getAttribute("id");
	
	numberOfDocuments = Integer.valueOf(data.getAttribute("df")).intValue();
	numberOfExpansions = Integer.valueOf(data.getAttribute("ef")).intValue();
	numberOfThesaurusLinks = Integer.valueOf(data.getAttribute("lf")).intValue();
	numberOfOccurances = Integer.valueOf(data.getAttribute("tf")).intValue();

	//searchPhrase = "cat"; // for now

	// go through children
	Node e = data.getFirstChild();
	while (e!=null) {
	    String node_name = e.getNodeName();
	    if (node_name.equals("phrase")) {
		// the value of the phrase
		searchPhrase = getNodeText(e);
	    } else if (node_name.equals("expansionList")) {
		
		NodeList expansions = ((Element)e).getElementsByTagName("expansion");
		for (int i=0; i<expansions.getLength(); i++) {
		    processExpansionElement((Element)expansions.item(i));
		}
	    } else if (node_name.equals("documentList")) {
			
		NodeList documents = ((Element)e).getElementsByTagName("document");
		for (int i=0; i<documents.getLength(); i++) {
		    processDocumentElement((Element)documents.item(i));
		}
	
	
	    } else if (node_name.equals("thesaurusList")) {
				
		NodeList thesaurai = ((Element)e).getElementsByTagName("thesaurus");
		for (int i=0; i<thesaurai.getLength(); i++) {
		    processThesaurusElement((Element)thesaurai.item(i));
		}
		
	    } else {
		System.out.println("error in phinddata format, have unwanted node, "+node_name);
	    }
	    e = e.getNextSibling();
	}
	// finished parsing, update the getMoreDocs markers
	c.updateMarkers();
    }


    /** Add an expansion tag
     *
     * Given a string containing an XML expansion element of the form:
     *   <expansion num="3" id="8421" prefix="PEOPLE and" suffix="" tf="3" df="3"><suffix></suffix><prefix>PEOPLE</prefix></expansion>
     *
     * Create a new ResultItemPhrase for display
     *
     *  Return true if successful, otherwise false. */
    boolean processExpansionElement(Element e) {

	String num = "", id = "", tf = "", df = "", 
	    prefix = "", body = "", suffix = "";

	body = searchPhrase;

	id = e.getAttribute("id");
	tf = e.getAttribute("tf");
	df = e.getAttribute("df");
	num = e.getAttribute("num");

	// get prefix child
	Node prefix_node = getChildByTagName(e, "prefix");
	if (prefix_node!=null) {
	    prefix = getNodeText(prefix_node);
	}
	Node suffix_node = getChildByTagName(e, "suffix");
	if (suffix_node !=null) {
	    suffix = getNodeText(suffix_node);
	}

	ResultItemPhrase ri = new ResultItemPhrase(id, tf, df, prefix, body, suffix);
	
	if (c.addResultItem(ri)) {
	    expansionsRetrieved++;
	    return true;
	}
	
	return false;
    }

    /** Add a document tag
     *
     * Given an XML Element of the form:
     *   <document num="2" hash="HASH424e64b811fdad933be69c" freq="1"><title>CONTENTS</title></document>
     *
     * Create a new ResultItemDocument for display
     *
     * Return true if successful, otherwise false. */

    boolean processDocumentElement(Element d ) {
	// why do we have num??
	String num = "", hash = "", freq = "", title = "";

	num = d.getAttribute("num");
	freq = d.getAttribute("freq");
	hash = d.getAttribute("hash");

	Node title_node = getChildByTagName(d, "title");
	if (title_node != null) {
	    title = getNodeText(title_node);
	}
	// Create a new ResultItem and add it to the display
	ResultItemDocument ri = new ResultItemDocument(hash, title, freq);
	
	if (c.addResultItem(ri)) {
	    documentsRetrieved++;
	    return true;
	}
	
	return false;
    }

    /** Add a thesaurus tag
     *
     * Given an  XML element  of the form:
     *
     * <thesaurus num="3" id="36506" tf="0" df="0" type="RT"><phrase>ANGLOPHONE AFRICA</phrase></thesaurus>
     *
     * Create a new ResultItemLink for display
     *
     * Return true if successful, otherwise false. */

    boolean processThesaurusElement(Element t ) {
	
	// why do we have num??? - not used anywhere
	String num = "", id = "", tf = "", df = "", type = "", phrase = "";

	id = t.getAttribute("id");
	tf = t.getAttribute("tf");
	df = t.getAttribute("df");
	type = t.getAttribute("type");
	num = t.getAttribute("num");

	Node phrase_node = getChildByTagName(t, "phrase");
	if (phrase_node !=null) {
	    phrase = getNodeText(phrase_node);
	}
	// Create a new ResultItem and add it to the display
	ResultItemLink ri = new ResultItemLink(id, phrase, type, tf, df);

	if (c.addResultItem(ri)) {
	    thesaurusLinksRetrieved++;
	    return true;
	}
	
	return false;
    }

    /** extracts the text out of a node */
    protected String getNodeText(Node elem) {
	elem.normalize();
	Node n = elem.getFirstChild();
	while (n!=null && n.getNodeType() !=Node.TEXT_NODE) {
	    n=n.getNextSibling();
	}
	if (n==null) { // no text node
	    return "";
	}
	return n.getNodeValue();
    }

    /** returns the (first) child element with the given name */
    protected Node getChildByTagName(Node n, String name) {

	Node child = n.getFirstChild();
	while (child!=null) {
	    if (child.getNodeName().equals(name)) {
		return child;
	    }
	    child = child.getNextSibling();
	}
	return null; //not found
    }


}




