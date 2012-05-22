/*
 *    MacroResolver.java
 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


abstract public class MacroResolver {

    public static final String SCOPE_TEXT = "text";
    public static final String SCOPE_META = "metadata";
    public static final String SCOPE_ALL = "all";

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_META = 1;
    public static final int TYPE_DICT = 2;
    
    protected ArrayList<Macro> text_macros = null;
    protected ArrayList<Macro> metadata_macros = null;

    String lang = null;

    public MacroResolver() {
    	this.text_macros = new ArrayList<Macro>();
	this.metadata_macros = new ArrayList<Macro>();
    }

    
    public void setSiteDetails(String site_address, String cluster_name, String library_name) {
	if (site_address != null) {
	    addMacro(TYPE_TEXT, "_httpsite_", site_address, SCOPE_ALL, false);
	}
	if (cluster_name != null) {
	    addMacro(TYPE_TEXT, "_clustername_", cluster_name, SCOPE_ALL, false);
	}
	if (library_name!=null){
	    addMacro(TYPE_TEXT, "_libraryname_", library_name, SCOPE_ALL, false);
	}
    }

    public void addMacro(int type, String macro, String text_or_metadata, String scope, boolean resolve) {
	Macro m = new Macro();
	m.type = type;
	m.macro = macro;
	m.text = text_or_metadata;
	m.resolve = resolve;
	addMacro(m, scope);
    }

    public void addMacro(int type, String macro, String bundle, String key, String scope, boolean resolve) {
	Macro m = new Macro();
	m.type = type;
	m.macro = macro;
	m.bundle = bundle;
	m.key = key;
	m.resolve = resolve;
	addMacro(m, scope);
    }
    
    public void addMacros(Element replace_list_elem) {
	NodeList replaces = replace_list_elem.getElementsByTagName(GSXML.REPLACE_ELEM);
	for (int i=0; i<replaces.getLength(); i++) {
	    Element e = (Element)replaces.item(i);
	    String scope = e.getAttribute("scope");
	    if (scope.equals("")) {
		scope = SCOPE_ALL;
	    }
	    boolean resolve = true;
	    String resolve_str = e.getAttribute("resolve");
	    if (resolve_str.equals("false")) {
		resolve = false;
	    }
	    String from = e.getAttribute("macro");
	    String to = e.getAttribute("text");
	    if (!to.equals("")) {
		addMacro(TYPE_TEXT, from, to, scope, resolve);
	    } else {
		String meta = e.getAttribute("metadata");
		if (!meta.equals("")) {
		    addMacro(TYPE_META, from, meta, scope, resolve);
		} else {
		    String key = e.getAttribute("key");
		    String bundle = e.getAttribute("bundle");
		    addMacro(TYPE_DICT, from, bundle, key, scope, resolve);
		}
                                                                                                             	    } 
	}
	
    }

    protected void addMacro(Macro m, String scope) {

	if (scope.equals(SCOPE_TEXT)) {
	    this.text_macros.add(m);
	}
	else if (scope.equals(SCOPE_META)) {
	    this.metadata_macros.add(m);
	}
	else if (scope.equals(SCOPE_ALL)) {
	    this.text_macros.add(m);
	    this.metadata_macros.add(m);
	    
	}

    }
    
    abstract public String resolve(String text, String lang, String scope, 
				   String doc_oid);
    
    protected class Macro {
	
	/** type of replacement: TEXT, METADATA, DICTIONARY */
	public int type;
	/** the macro to replace */
	public String macro = null;
	/** If text type, holds the text to replace with. If metadata type, holds the metadata name */
	public String text = null; 
	/** If dictionary type, holds the key to look up in the dictionary */
	public String key = null;
	/** If dictionary type, holds the resource bundle name */
	public String bundle = null;
	/** If true, will try to resolve macros in the replacement text */
	public boolean resolve = true;
	
    }
	
}
