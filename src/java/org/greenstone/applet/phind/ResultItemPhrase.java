/**********************************************************************
 *
 * ResultItemPhrase.java -- a phrase result in the Phind applet
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

It contains information describing a phrase.

**********************************************************************/

//package org.nzdl.gsdl.Phind;
package org.greenstone.applet.phind;
public class ResultItemPhrase extends ResultItem {

    // The phrase number, stored as a string and as a number
    String id;
    int symbol;

    // The text of the phrase is split into a prefix, body, and suffix.
    // The body is the same as the parent phrase (or search term) and 
    // the prefix and suffix are the surrounding text.
    String prefix;
    String body;
    String suffix;

    // The document frequency of the phrase
    int documentFrequency;

    // Create a ResultItem for a phrase
    ResultItemPhrase(String newId, String tf, String df, 
		     String p, String b, String s) {

	kind = phraseItem;

	id = newId;
	symbol = Integer.valueOf(newId).intValue();

	sort = sortPhraseItem;
	frequency = Integer.valueOf(tf).intValue();
	documentFrequency = Integer.valueOf(df).intValue();

	prefix = p;
	body = b;
	suffix = s;
	text = (prefix + " " + body + " " + suffix);

    }

    // Is this item a phrase?
    public boolean isPhrase() { return true; }

    // Return the text of the item components
    public String mainText() { return body; }
    public String docsText() { return Integer.toString(documentFrequency); }
    public String prefixText() { return prefix; }
    public String suffixText() { return suffix; }
    public String hiddenText() { return id; }

}

