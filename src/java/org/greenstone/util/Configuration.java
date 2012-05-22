/**
 *#########################################################################
 * Configuration
 * A component of the GAI application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * <BR><BR>
 *
 * Copyright (C) 1999 New Zealand Digital Library Project
 *
 * <BR><BR>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * <BR><BR>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * <BR><BR>
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *########################################################################
 */
package org.greenstone.util;

import java.awt.Color;
import java.util.Locale;
import java.util.Hashtable;
import java.util.StringTokenizer;

// eventually this class will hold configuration settings from a config file. For now, lets use static info
// loosely copied from Gatherer Configuration
public class Configuration {
    
    static private Hashtable<String, String> hash = null;

    /** The first of three patterns used during tokenization, this pattern handles a comma separated list. */
    static final private String TOKENIZER_PATTERN1 = " ,\n\t";
    /** The second of three patterns used during tokenization, this pattern handles an underscore separated list. */
    static final private String TOKENIZER_PATTERN2 = "_\n\t";
    /** The last of three patterns used during tokenization, this pattern handles an comma separated list containing spaces. */
    static final private String TOKENIZER_PATTERN3 = ",\n\t";

    public Configuration() {
	
	hash = new Hashtable<String, String>();
	
	
	// load up all the initial stuff
	hash.put("admin.log", "true");
	hash.put("admin.conf", "true");
	hash.put("admin.ext", "true");
	hash.put("admin.monitor", "true");
	hash.put("coloring.workspace_selection_foreground", "0, 0, 0");
	hash.put("coloring.workspace_selection_background", "176, 208, 176");
	hash.put("coloring.table_noneditable_background", "255, 255, 255");
	hash.put("coloring.table_editable_background", "255, 255, 255");
	hash.put("coloring.collection_tree_background", "224, 240, 224");
	hash.put("coloring.collection_tree_foreground", "0, 0, 0");
	hash.put("coloring.workspace_tree_foreground", "0, 0, 0");
	hash.put("coloring.workspace_tree_background", "218, 237, 252");
	hash.put("coloring.workspace_heading_background", "128, 180, 216");
	hash.put("coloring.workspace_heading_foreground", "0, 0, 0");
	
    }

    /** Retrieve whether the named property is set or not */
    static public boolean get(String property) {
	String value = hash.get(property);
	return (value != null && value.equalsIgnoreCase("true"));
    }
    
    /** Retrieve the value of the named property as a String */
    static public String getString(String property) {
	return hash.get(property);
    }

    /** Retrieve the value of the named property as a Locale. */
    static public Locale getLocale(String property) {
	Locale result = Locale.getDefault();
	try {
	    String raw = hash.get(property);
	    if (raw==null) {
		return result;
	    }
	    // Locale is a underscore separated code.
	    StringTokenizer tokenizer = new StringTokenizer(raw, TOKENIZER_PATTERN2);
	    String language = tokenizer.nextToken();
	    if(tokenizer.hasMoreTokens()) {
		String country = tokenizer.nextToken();
		result = new Locale(language, country);
	    }
	    else {
		result = new Locale(language);
	    }
	}
	catch(Exception error) {
	    error.printStackTrace();
	}
	return result;
    }
    
    /** Retrieve the value of the named property as a Color. */
    static public Color getColor(String property) {
	Color result = Color.white; // Default
	String raw = hash.get(property);
	if (raw == null || raw.equals("")) {
	    return result;
	}
	try {
	    // Color is a RGB triplet list, comma separated (also remove whitespace)
	    StringTokenizer tokenizer = new StringTokenizer(raw, TOKENIZER_PATTERN1);
	    int red = Integer.parseInt(tokenizer.nextToken());
	    int green = Integer.parseInt(tokenizer.nextToken());
	    int blue = Integer.parseInt(tokenizer.nextToken());
	    result = new Color(red, green, blue);
	}
	catch(Exception error) {
	    error.printStackTrace();
	}
	return result;
    }
}
