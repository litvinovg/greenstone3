/**********************************************************************
 *
 * ResultItemLink.java -- a thesaurus link in the Phind applet
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

It contains information describing a thesaurus link.

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

public class ResultItemLink extends ResultItem {

    // The phrase number, stored as a string and as a number
    String id;
    int symbol;

    // The text of the link is stored in the inherited text field.

    // The type of thesaurus link
    String linkType;

    // The total frequency is stored in the inherited frequency field.

    // The document frequency of the thesaurus item
    int documentFrequency;

    // Create a ResultItem for a thesaurus link
    ResultItemLink(String newId, String newText, String newLinkType, 
		   String tf, String df) { 

	kind = linkItem;
	id = newId;
	symbol = Integer.valueOf(newId).intValue();
	text = newText.trim();
	sort = sortLinkItem;
	linkType = newLinkType;
	frequency = Integer.valueOf(tf).intValue();
	documentFrequency = Integer.valueOf(df).intValue();
    }

    // Is this item a phrase?
    public boolean isLink() { return true; }

    // Return the text of the item components
    public String hiddenText() { return id; }
    public String docsText() { 
	if (documentFrequency <= 0) { return ""; }
	else { return Integer.toString(documentFrequency); }
    }
    public String prefixText() { 
	if (linkType.equals("RT")) { return "Related term"; }
	else if (linkType.equals("BT")) { return "Broader term"; }
	else if (linkType.equals("NT")) { return "Narrower term"; }
	else if (linkType.equals("USE")) { return "Use"; }
	else if (linkType.startsWith("UF")) { return "Used for"; }
	else { return linkType; }
    }
}



