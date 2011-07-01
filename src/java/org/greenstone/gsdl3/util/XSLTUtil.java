/*
 *    XSLTUtil.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
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

import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.log4j.*;
import org.w3c.dom.Node; 

import org.apache.commons.lang3.StringUtils;

/** a class to contain various static methods that are used by the xslt
 * stylesheets
 */
public class XSLTUtil {

     static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.XSLTUtil.class.getName());

    /* some tests */
    public static boolean equals(String s1, String s2) {
	return s1.equals(s2);
    }
    public static boolean notEquals(String s1, String s2) {
	return !s1.equals(s2);
    }
    public static boolean exists(String s1, String s2) {
	return !s1.equals("");
    }
    public static boolean contains(String s1, String s2) {
	return (s1.indexOf(s2) != -1);
    }
    public static boolean startsWith(String s1, String s2) {
	return s1.startsWith(s2);
    }
    public static boolean endsWith(String s1, String s2) {
	return s1.endsWith(s2);
    }
    public static boolean lessThan(String s1, String s2) {
	return (s1.compareTo(s2) < 0);
    }
    public static boolean lessThanOrEquals(String s1, String s2) {
	return (s1.compareTo(s2) <= 0);
    }
    public static boolean greaterThan(String s1, String s2) {
	return (s1.compareTo(s2) > 0);
    }
    public static boolean greaterThanOrEquals(String s1, String s2) {
	return (s1.compareTo(s2) >= 0);
    }

    /* some preprocessing functions */
    public static String toLower(String orig) {
	return orig.toLowerCase();
    }
    public static String toUpper(String orig) {
	return orig.toUpperCase();
    }

    public static byte[] toUTF8(String orig) {
	
	try {
	    byte[] utf8 = orig.getBytes("UTF-8");
	    return utf8;
	}
	catch (Exception e){
	    logger.error("unsupported encoding");
	    return orig.getBytes();
	}
    }

    public static String getNumberedItem(String list, int number) {
	String [] items = StringUtils.split(list, ",", -1);	
	if (items.length > number) {
	    return items[number];
	}
	return ""; // index out of bounds
    }

    /** Generates links to equivalent documents for a document with a default document icon/type. 
     * Links are generated from the parameters: a list of document icons which are each in turn embedded 
     * in the matching starting link tag in the list of docStartLinks (these starting links link to the 
     * equivalent documents in another format). Each link's start tag is closed with the corresponding 
     * closing tag in the docEndLinks list. Parameter token is the list separator. Parameter divider is 
     * the string that should separate each final link generated from the next.
     * Returns a string that represents a sequence of links to equivalent documents, where the anchor is
     * a document icon. */
    public static String getEquivDocLinks(String token, String docIconsString, String docStartLinksString, 
					  String docEndLinksString, String divider)
    {
	String [] docIcons = StringUtils.split(docIconsString, token, -1);
	String [] startLinks = StringUtils.split(docStartLinksString, token, -1);
	String [] endLinks = StringUtils.split(docEndLinksString, token, -1);

	StringBuffer buffer = new StringBuffer();
	for(int i = 0; i < docIcons.length; i++) {
	    if(i > 0) {
		buffer.append(divider);
	    }
	    buffer.append(startLinks[i]+docIcons[i]+endLinks[i]);
	}
	
	return buffer.toString();
    }

    
    public static String tidyWhitespace(String original) {
	
	if (original==null || original.equals("")) {
	    return original;
	}
	String new_s = original.replaceAll("\\s+", " ");
	return new_s;
    }


    public static String getInterfaceText(String interface_name, String lang, String key) {
	return getInterfaceText(interface_name, lang, key, null);
    }
    public static String getInterfaceText(String interface_name, String lang, String key, String args_str) {
	String [] args = null;
	if (args_str!=null && !args_str.equals("")) {
	    args = StringUtils.split(args_str, ";");
	}
	Dictionary dict = new Dictionary("interface_"+interface_name, lang);
	String result = dict.get(key, args);
	if (result == null) { // not found
		//if not found, search a separate subdirectory named by the interface name
		String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface"; 
		dict = new Dictionary(sep_interface_dir, lang);
		result = dict.get(key, args);
		if(result != null) {
			return result;
		}   
	 }

	if (result == null && !interface_name.equals("default")) { // not found, try the default interface
	    dict = new Dictionary("interface_default", lang);
	    result = dict.get(key, args);
	}
	    
        if (result == null) { // not found
    	    	return "_"+key+"_";
        } 
        return result;
    }

    public static String getInterfaceTextWithDOM(String interface_name, String lang, String key, Node arg_node) {
	String [] args = new String [1];
 
	String node_str = XMLConverter.getString(arg_node);
	args[0] = node_str;
	Dictionary dict = new Dictionary("interface_"+interface_name, lang);
	String result = dict.get(key, args);
	if (result == null) { // not found
	    //if not found, search a separate subdirectory named by the interface name
	    String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface"; 
	    dict = new Dictionary(sep_interface_dir, lang);
	    result = dict.get(key, args);
	    if(result != null) {
		return result;
	    }   
	}

	if (result == null && !interface_name.equals("default")) { // not found, try the default interface
	    dict = new Dictionary("interface_default", lang);
	    result = dict.get(key, args);
	}
	    
        if (result == null) { // not found
    	    	return "_"+key+"_";
        }

        return result;
    }
    public static String getInterfaceTextWithDOM(String interface_name, String lang, String key, Node arg1_node, Node arg2_node) {
	String [] args = new String [2];
 
	String node_str = XMLConverter.getString(arg1_node);
	args[0] = node_str; 
	node_str = XMLConverter.getString(arg2_node);
	args[1] = node_str;
	Dictionary dict = new Dictionary("interface_"+interface_name, lang);
	String result = dict.get(key, args);
	if (result == null) { // not found
	    //if not found, search a separate subdirectory named by the interface name
	    String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface"; 
	    dict = new Dictionary(sep_interface_dir, lang);
	    result = dict.get(key, args);
	    if(result != null) {
		return result;
	    }   
	}

	if (result == null && !interface_name.equals("default")) { // not found, try the default interface
	    dict = new Dictionary("interface_default", lang);
	    result = dict.get(key, args);
	}
	    
        if (result == null) { // not found
    	    	return "_"+key+"_";
        }
	
        return result;
    }

    public static boolean isImage(String mimetype) {
	if (mimetype.startsWith("image/")) {
	    return true;
	}
	return false;
    }

    public static String formatDate(String date, String lang) {
	
	String in_pattern = "yyyyMMdd";
	String out_pattern = "dd MMMM yyyy";
	if (date.length()==6) {
	    in_pattern = "yyyyMM";
	}
	
	SimpleDateFormat formatter = new SimpleDateFormat(in_pattern, new Locale(lang));
	try {
	    Date d = formatter.parse(date);
	    formatter.applyPattern(out_pattern);
	    String new_date = formatter.format(d);
	    return new_date;
	} catch (Exception e) {
	    return date;
	}
	
    }

    public static String formatLanguage(String display_lang, String lang) {

	return new Locale(display_lang).getDisplayLanguage(new Locale(lang));
    }

    public static String cgiSafe(String original, String lang) {

	original = original.replace('&', ' ');
	original = original.replaceAll(" ", "%20");
	return original;
    }

    public static String formatBigNumber(String num){

	String num_str = num;
	char[] num_chars = num_str.toCharArray();
	String zero_str = "";
	String formatted_str = "";

	    for(int i = num_chars.length-4; i >=0; i--){
		zero_str += '0';
	    }

	    String sig_str = "";
	    for(int i = 0; i<3 && i < num_chars.length; i++){
		sig_str = sig_str + num_chars[i];
		if(i == 1 && i+1 < num_chars.length){
		    sig_str = sig_str + ".";
		}
	    }

	    int sig_int = Math.round(Float.parseFloat(sig_str));
	    String new_sig_str = sig_int +"";
	    if(sig_str.length() > 2){
		new_sig_str = sig_int + "0";
	    }

	    char[] final_chars = (new_sig_str+zero_str).toCharArray();
	    int count = 1;
	    for(int i=final_chars.length -1 ; i>=0; i-- ){
		formatted_str = final_chars[i] + formatted_str ;
		if(count == 3 && i !=0){
		    formatted_str = "," +formatted_str;
		    count = 1;
		}
		else{
		    count++;
		}
	    }
	    return formatted_str;
	}
    
	public static String hashToSectionId(String hashString)
	{
		if(hashString == null || hashString.length() == 0) {return "";}
		
		int firstDotIndex = hashString.indexOf(".");
		if(firstDotIndex == -1)
		{	
			return "";
		}
		
		String sectionString = hashString.substring(firstDotIndex + 1);
		
		return sectionString;
	}
	
	public static String hashToDepthClass(String hashString)
	{
		if(hashString == null || hashString.length() == 0) {return "";}
		
		String sectionString = hashToSectionId(hashString);
		
		int count = sectionString.split("\\.").length;
		
		if (sectionString.equals(""))
		{
			return "sectionHeaderDepthTitle";
		}
		else 
		{
			return "sectionHeaderDepth" + count;
		}
	}
}

