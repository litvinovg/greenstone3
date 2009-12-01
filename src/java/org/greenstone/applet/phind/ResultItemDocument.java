/**********************************************************************
 *
 * ResultItemDocument.java -- a document result in the Phind applet
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

It contains information describing a document in which a search term 
occurs.

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

public class ResultItemDocument extends ResultItem {

    // The document's title is stored in the superclasses 
    // text variable, and the number of times the phrase occurs
    // in the frequency variable. 

    // The document's unique object identifier (hash value).
    String hash;

    // Create a ResultItem for a document
    ResultItemDocument(String newHash, String newTitle, String newFrequency) {
	
	kind = documentItem;
	text = newTitle;

	sort = sortDocumentItem;
	frequency = Integer.valueOf(newFrequency).intValue();
	hash = newHash;
    }

    // Is this item a document?
    public boolean isDocument() { return true; }

    // Return the bare text of the item
    public String toString() { return(text); }

    // Return the text of the item components
    public String hiddenText() { return hash; }
 

}

