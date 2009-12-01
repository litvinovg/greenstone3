/**********************************************************************
 *
 * ResultItemDocument.java -- a result in the Phind applet
 *
 * Copyright 2000 Gordon W. Paynter
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

A result item holds the data describing a single line returned from a phind
query.  The complex functionality of phrase and URL items is implemented in
subclasses; the base class handles simpler cases like the "get more phrases" 
marker.

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

public class ResultItem {

    // A text string describing the item
    String text = "Generic ResultItem";

    // There are several kinds of ResultItem, identified by their
    // "kind" field, which has an integer value from the list below.
    final static int message = 8;
    final static int linkItem = 7;
    final static int moreLinks = 6;
    final static int phraseItem = 5;
    final static int morePhrases = 4;
    final static int documentItem = 3;
    final static int moreDocuments = 2;
    final static int unknownResultItem = 1;

    // The kind of the item
    int kind = unknownResultItem;

    // When results are displayed, they are loosely sorted by their
    // kind.  The user can change the sort order numbers with the 
    // ResultOrder parameter in Phind.java.
    static int sortMessage = 8;
    static int sortLinkItem = 7;
    static int sortMoreLinks = 6;
    static int sortPhraseItem = 5;
    static int sortMorePhrases = 4;
    static int sortDocumentItem = 3;
    static int sortMoreDocuments = 2;
    static int sortUnknownResultItem = 1;

    // the primary sort key
    int sort = 0;
    
    // The frequency of the item (secondary sort key)
    int frequency = 0;


    // Create a blank ResultItem
    ResultItem() {
	text = "Unknown result type";
	kind = unknownResultItem;
	frequency = 0;
    }

    // Create a new ResultItem of some given kind
    ResultItem(int newKind) {
	kind = newKind;
	frequency = 0;

	if (kind == moreLinks) {
	    text = "get more thesaurus links";
	    sort = sortMoreLinks;
	} else if (kind == morePhrases) {
	    text = "get more phrases";
	    sort = sortMorePhrases;
	} else if (kind == moreDocuments) {
	    text = "get more documents";
	    sort = sortMoreDocuments;
	} else {
	    text = "Unknown result type";
	    sort = sortUnknownResultItem;
	}
    }

    // Test the type of a ResultItem
    public boolean isLink() { return false; }
    public boolean isMoreLinks() { return (kind == moreLinks); }
    public boolean isPhrase() { return false; }
    public boolean isMorePhrases() { return (kind == morePhrases); }
    public boolean isDocument() { return false; }
    public boolean isMoreDocuments() { return (kind == moreDocuments); }

    // Return the bare text of the item
    public String toString() { return(text.trim()); }

    // Return the text of the item components
    public String mainText() { return text; }
    public String freqText() { 
	if (frequency > 0) { return Integer.toString(frequency); } 
	else { return ""; }
    }
    public String docsText() { return ""; }
    public String prefixText() { return ""; }
    public String suffixText() { return ""; }
    public String hiddenText() { return ""; }
}

