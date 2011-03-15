/*
 *    GS2MacroResolver.java
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;

// Apache Commons
import org.apache.commons.lang3.*;

import java.util.Stack;

public class GS2MacroResolver 
    extends MacroResolver
{

    protected SimpleCollectionDatabase coll_db = null;

    private static Pattern p_back_slash = Pattern.compile("\\\"");// create a pattern "\\\"", but it matches both " and \"
    
    // need to make it not add macros if they are already present
    public GS2MacroResolver(SimpleCollectionDatabase db) {
	super();
	coll_db = db;
    }

    public GS2MacroResolver() {
	super();
    }

    public void setDB(SimpleCollectionDatabase db) {
	this.coll_db = db;
    }

    
    public String resolve(String text, String lang, String scope, 
			  String doc_oid) {
	if (text == null || text.equals("")) return text;
	if (scope.equals(SCOPE_TEXT) && text_macros.size()==0) return text;
	if (scope.equals(SCOPE_META) && metadata_macros.size() ==0) return text;
	DBInfo node_info = null;
	DBInfo root_info = null;
	boolean new_lang = false;
	if (this.lang == null || !this.lang.equals(lang) ) {
	    new_lang = true;
	    this.lang = lang;
	} 

	Stack macros;//ArrayList macros;
	if (scope.equals(SCOPE_TEXT)) {
	    macros = text_macros;
	} else {
	    macros = metadata_macros;
	}
	//for (int i=0; i<macros.size(); i++) {
	while(!macros.empty()) {
	    String new_text = null;
	    Macro m = (Macro)macros.pop();//.get(i);
	    switch (m.type) {
	    case TYPE_DICT:
		if (m.text==null || new_lang) {
		    Dictionary dict = new Dictionary(m.bundle, lang);
		    m.text = dict.get(m.key, null);
		}
		// we assume that dictionary entries will contain no macros
		// otherwise we can't cache the answer because it might be 
		// document specific
		text = StringUtils.replace(text, m.macro, m.text);
		break;
	    case TYPE_TEXT:
		// make sure we resolve any macros in the text
		// the (?s) treats the string as a single line, cos . 
		// doesn't necessarily match line breaks
		//if (text.matches("(?s).*"+m.macro+".*")) {
		
		/*Pattern p_text = Pattern.compile(".*" + m.macro + ".*",Pattern.DOTALL);
		Matcher match_text = p_text.matcher(text);*/

		// sm252
		// String.contains is far faster than regex!
		if (text.contains(m.macro)) { //match_text.matches()) { //text.matches("(?s).*"+m.macro+".*")) {
		    if (m.resolve) {
			new_text = this.resolve(m.text, lang, scope, doc_oid);
		    } else {
			new_text = m.text;
		    }
		    text = StringUtils.replace(text, m.macro, new_text);
		    if (m.macro.endsWith("\\\\")){ // to get rid of "\" from the string likes: "src="http://www.greenstone.org:80/.../mw.gif\">"
			
			Matcher m_slash = p_back_slash.matcher(text);
			String clean_str = "";
			int s=0;
			while (m_slash.find()){
			    if (!text.substring(m_slash.end()-2,m_slash.end()-1).equals("\\")){
				clean_str = clean_str + text.substring(s,m_slash.end()-1); // it matches ", so get a substring before "
			    }else{
				clean_str = clean_str + text.substring(s,m_slash.end()-2);// it matches \", so get a substring before \
			    }
			    s = m_slash.end();// get the index of the last match
			    clean_str = clean_str + "\"";
			}
			text = clean_str + text.substring(s,text.length());
		    }
		}
		break;
	    case TYPE_META:
		//Pattern p = Pattern.compile(".*" + m.macro + ".*",Pattern.DOTALL);
		//Matcher match = p.matcher(text);
		// sm252
		if (text.contains(m.macro)) { //(match.matches()) { //text.matches("(?s).*"+m.macro+".*")) {
		    if (node_info == null) {
			node_info = coll_db.getInfo(doc_oid);
			if (node_info == null) {
			    break;
			}
		    }
		    new_text = node_info.getInfo(m.text);
		    if (new_text == null || new_text.equals("")) {
			// try the root node
			if (root_info == null && !OID.isTop(doc_oid)) {
			    root_info = coll_db.getInfo(OID.getTop(doc_oid));
			}
			if (root_info == null) break;
			new_text = root_info.getInfo(m.text);
		    }
		    if (new_text != null) {
			if (m.resolve) {
			    new_text = this.resolve(new_text, lang, scope, doc_oid);
			}
			text =  StringUtils.replace(text, m.macro, new_text);
		    }
		    
		}
		
		break;
	    } // switch
	   
	}
	return text;
	
    }

}
