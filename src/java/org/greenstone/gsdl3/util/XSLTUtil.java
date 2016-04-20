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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.greenstone.util.GlobalProperties;
import org.w3c.dom.Node;

/**
 * a class to contain various static methods that are used by the xslt
 * stylesheets
 */
public class XSLTUtil
{
	protected static HashMap<String, ArrayList<String>> _foundTableValues = new HashMap<String, ArrayList<String>>();
	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.XSLTUtil.class.getName());
	protected static HashMap<String, String> _stringVariables = new HashMap<String, String>();

	public static void storeString(String name, String value)
	{
		_stringVariables.put(name, value);
	}

	public static String getString(String name)
	{
		return _stringVariables.get(name);
	}

	/* some tests */
	public static boolean equals(String s1, String s2)
	{
		return s1.equals(s2);
	}

	public static boolean notEquals(String s1, String s2)
	{
		return !s1.equals(s2);
	}

	public static boolean exists(String s1, String s2)
	{
		return !s1.equals("");
	}

	public static boolean contains(String s1, String s2)
	{
		return (s1.indexOf(s2) != -1);
	}

	public static boolean startsWith(String s1, String s2)
	{
		return s1.startsWith(s2);
	}

	public static boolean endsWith(String s1, String s2)
	{
		return s1.endsWith(s2);
	}

	public static boolean lessThan(String s1, String s2)
	{
		return (s1.compareTo(s2) < 0);
	}

	public static boolean lessThanOrEquals(String s1, String s2)
	{
		return (s1.compareTo(s2) <= 0);
	}

	public static boolean greaterThan(String s1, String s2)
	{
		return (s1.compareTo(s2) > 0);
	}

	public static boolean greaterThanOrEquals(String s1, String s2)
	{
		return (s1.compareTo(s2) >= 0);
	}

	public static boolean oidIsMatchOrParent(String first, String second)
	{
		if (first.equals(second))
		{
			return true;
		}

		String[] firstParts = first.split(".");
		String[] secondParts = second.split(".");

		if (firstParts.length >= secondParts.length)
		{
			return false;
		}

		for (int i = 0; i < firstParts.length; i++)
		{
			if (!firstParts[i].equals(secondParts[i]))
			{
				return false;
			}
		}

		return true;
	}

	public static String oidDocumentRoot(String oid)
	{
		String[] oidParts = oid.split("\\.");

		return oidParts[0];
	}

	public static String replace(String orig, String match, String replacement)
	{
		return orig.replace(match, replacement);
	}

	public static String getNumberedItem(String list, int number)
	{
	    String[] items = list.split(",", -1); //String[] items = StringUtils.split(list, ",", -1); 
	    // Using StringUtils.split() causes an off-by-one error for the boolean operators (fqk)
	    // where boolean operators combining rows in multiforms are shifted up by 1 row.

		if (items.length > number)
		{
			return items[number];
		}
		return ""; // index out of bounds
	}

	/**
	 * Generates links to equivalent documents for a document with a default
	 * document icon/type. Links are generated from the parameters: a list of
	 * document icons which are each in turn embedded in the matching starting
	 * link tag in the list of docStartLinks (these starting links link to the
	 * equivalent documents in another format). Each link's start tag is closed
	 * with the corresponding closing tag in the docEndLinks list. Parameter
	 * token is the list separator. Parameter divider is the string that should
	 * separate each final link generated from the next. Returns a string that
	 * represents a sequence of links to equivalent documents, where the anchor
	 * is a document icon.
	 */
	public static String getEquivDocLinks(String token, String docIconsString, String docStartLinksString, String docEndLinksString, String divider)
	{
		String[] docIcons = StringUtils.split(docIconsString, token, -1);
		String[] startLinks = StringUtils.split(docStartLinksString, token, -1);
		String[] endLinks = StringUtils.split(docEndLinksString, token, -1);

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < docIcons.length; i++)
		{
			if (i > 0)
			{
				buffer.append(divider);
			}
			buffer.append(startLinks[i] + docIcons[i] + endLinks[i]);
		}

		return buffer.toString();
	}

	public static String getInterfaceText(String interface_name, String lang, String key)
	{
		return getInterfaceText(interface_name, lang, key, null);
	}

	public static String getInterfaceText(String interface_name, String lang, String key, String args_str)
	{
		key = key.replaceAll("__INTERFACE_NAME__", interface_name);

		String[] args = null;
		if (args_str != null && !args_str.equals(""))
		{
			args = StringUtils.split(args_str, ";");
		}
		Dictionary dict = new Dictionary("interface_" + interface_name, lang);
		String result = dict.get(key, args);
		if (result == null)
		{ // not found
			//if not found, search a separate subdirectory named by the interface name
			String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface";
			dict = new Dictionary(sep_interface_dir, lang);
			result = dict.get(key, args);
			if (result != null)
			{
				result = result.replaceAll("__INTERFACE_NAME__", interface_name);
				return result;
			}
		}

		if (result == null && !interface_name.equals("default"))
		{ // not found, try the default interface
			dict = new Dictionary("interface_default", lang);
			result = dict.get(key, args);
		}

		if (result == null)
		{ // not found
			return "_" + key + "_";
		}
		result = result.replaceAll("__INTERFACE_NAME__", interface_name);
		return result;
	}

	public static String getInterfaceText(String interfaceName, String dictionaryName, String lang, String key, String args_str)
	{
		key = key.replaceAll("__INTERFACE_NAME__", interfaceName);

		String[] args = null;
		if (args_str != null && !args_str.equals(""))
		{
			args = StringUtils.split(args_str, ";");
		}
		Dictionary dict = new Dictionary(dictionaryName, lang);
		String result = dict.get(key, args);
		if (result == null)
		{ // not found
			//if not found, search a separate subdirectory named by the interface name
			String sep_interface_dir = interfaceName + File.separatorChar + lang + File.separatorChar + "interface";
			dict = new Dictionary(sep_interface_dir, lang);
			result = dict.get(key, args);
			if (result != null)
			{
				result = result.replaceAll("__INTERFACE_NAME__", interfaceName);
				return result;
			}
		}

		if (result == null && !interfaceName.equals("default"))
		{ // not found, try the default interface
			dict = new Dictionary("interface_default", lang);
			result = dict.get(key, args);
		}

		if (result == null)
		{ // not found
			return "_" + key + "_";
		}
		result = result.replaceAll("__INTERFACE_NAME__", interfaceName);
		return result;
	}

	public static String getInterfaceTextWithDOM(String interface_name, String lang, String key, Node arg_node)
	{
		String[] args = new String[1];

		String node_str = XMLConverter.getString(arg_node);
		args[0] = node_str;
		Dictionary dict = new Dictionary("interface_" + interface_name, lang);
		String result = dict.get(key, args);
		if (result == null)
		{ // not found
			//if not found, search a separate subdirectory named by the interface name
			String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface";
			dict = new Dictionary(sep_interface_dir, lang);
			result = dict.get(key, args);
			if (result != null)
			{
				return result;
			}
		}

		if (result == null && !interface_name.equals("default"))
		{ // not found, try the default interface
			dict = new Dictionary("interface_default", lang);
			result = dict.get(key, args);
		}

		if (result == null)
		{ // not found
			return "_" + key + "_";
		}

		return result;
	}

	public static String getInterfaceTextWithDOM(String interface_name, String lang, String key, Node arg1_node, Node arg2_node)
	{
		String[] args = new String[2];

		String node_str = XMLConverter.getString(arg1_node);
		args[0] = node_str;
		node_str = XMLConverter.getString(arg2_node);
		args[1] = node_str;
		Dictionary dict = new Dictionary("interface_" + interface_name, lang);
		String result = dict.get(key, args);
		if (result == null)
		{ // not found
			//if not found, search a separate subdirectory named by the interface name
			String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface";
			dict = new Dictionary(sep_interface_dir, lang);
			result = dict.get(key, args);
			if (result != null)
			{
				return result;
			}
		}

		if (result == null && !interface_name.equals("default"))
		{ // not found, try the default interface
			dict = new Dictionary("interface_default", lang);
			result = dict.get(key, args);
		}

		if (result == null)
		{ // not found
			return "_" + key + "_";
		}

		return result;
	}


    	public static String getInterfaceTextSubstituteArgs(String value, String args_str)
	{
	    String[] args = null;
	    if (args_str != null && !args_str.equals("")) {
		args = StringUtils.split(args_str, ";");
	    }

	    return Dictionary.processArgs(value,args);
	}
	    
	public static Node getCollectionText(String collection, String site_name, String lang, String key)
	{
		return getCollectionTextWithArgs(collection, site_name, lang, key, null);
	}

	public static Node getCollectionText(String collection, String site_name, String lang, String key, String args_str)
	{

		String[] args = null;
		if (args_str != null && !args_str.equals(""))
		{
			args = StringUtils.split(args_str, ";");
		}

		return getCollectionTextWithArgs(collection, site_name, lang, key, args);
	}

	// xslt didn't like calling the function with Node varargs, so have this hack for now
	public static Node getCollectionTextWithDOM(String collection, String site_name, String lang, String key, Node n1)
	{
		return getCollectionTextWithDOMMulti(collection, site_name, lang, key, n1);
	}

	public static Node getCollectionTextWithDOM(String collection, String site_name, String lang, String key, Node n1, Node n2)
	{
		return getCollectionTextWithDOMMulti(collection, site_name, lang, key, n1, n2);
	}

	public static Node getCollectionTextWithDOM(String collection, String site_name, String lang, String key, Node n1, Node n2, Node n3)
	{
		return getCollectionTextWithDOMMulti(collection, site_name, lang, key, n1, n2, n3);
	}

	public static Node getCollectionTextWithDOM(String collection, String site_name, String lang, String key, Node n1, Node n2, Node n3, Node n4)
	{
		return getCollectionTextWithDOMMulti(collection, site_name, lang, key, n1, n2, n3, n4);
	}

	public static Node getCollectionTextWithDOMMulti(String collection, String site_name, String lang, String key, Node... nodes)
	{
		int num_nodes = nodes.length;
		String[] args = null;
		if (num_nodes != 0)
		{
			args = new String[num_nodes];

			for (int i = 0; i < num_nodes; i++)
			{
				String node_str = XMLConverter.getString(nodes[i]);
				args[i] = node_str;
			}
		}
		return getCollectionTextWithArgs(collection, site_name, lang, key, args);
	}

	public static Node getCollectionTextWithArgs(String collection, String site_name, String lang, String key, String[] args)
	{
		try
		{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			CollectionClassLoader class_loader = new CollectionClassLoader(XSLTUtil.class.getClassLoader(), GSFile.siteHome(GlobalProperties.getGSDL3Home(), site_name), collection);
			Dictionary dict = new Dictionary(collection, lang, class_loader);
			String result = dict.get(key, args);
			if (result != null)
			{
				return docBuilder.parse(new ByteArrayInputStream(("<fragment>" + result + "</fragment>").getBytes("UTF-8"))).getDocumentElement();
			}
			return docBuilder.parse(new ByteArrayInputStream(("<fragment>" + "text:" + collection + ":" + key + "</fragment>").getBytes())).getDocumentElement();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public static boolean isImage(String mimetype)
	{
		if (mimetype.startsWith("image/"))
		{
			return true;
		}
		return false;
	}

	// formatting /preprocessing functions
	// some require a language, so we'll have a language param for all
	public static String toLower(String orig, String lang)
	{
		return orig.toLowerCase();
	}

	public static String toUpper(String orig, String lang)
	{
		return orig.toUpperCase();
	}

	public static String tidyWhitespace(String original, String lang)
	{

		if (original == null || original.equals(""))
		{
			return original;
		}
		String new_s = original.replaceAll("\\s+", " ");
		return new_s;
	}

	public static String stripWhitespace(String original, String lang)
	{

		if (original == null || original.equals(""))
		{
			return original;
		}
		String new_s = original.replaceAll("\\s+", "");
		return new_s;
	}

	public static byte[] toUTF8(String orig, String lang)
	{
		try
		{
			byte[] utf8 = orig.getBytes("UTF-8");
			return utf8;
		}
		catch (Exception e)
		{
			logger.error("unsupported encoding");
			return orig.getBytes();
		}
	}

	public static String formatDate(String date, String lang)
	{
		String in_pattern = "yyyyMMdd";
		String out_pattern = "dd MMMM yyyy";
		if (date.length() == 6)
		{
			in_pattern = "yyyyMM";
		}

		SimpleDateFormat formatter = new SimpleDateFormat(in_pattern, new Locale(lang));
		try
		{
			Date d = formatter.parse(date);
			formatter.applyPattern(out_pattern);
			String new_date = formatter.format(d);
			return new_date;
		}
		catch (Exception e)
		{
			return date;
		}

	}

	public static String getDetailFromDate(String date, String detail, String lang)
	{
		String in_pattern = "yyyyMMdd";
		if (date.length() == 6)
		{
			in_pattern = "yyyyMM";
		}

		SimpleDateFormat formatter = new SimpleDateFormat(in_pattern, new Locale(lang));
		try
		{
			Date d = formatter.parse(date);
			if (detail.toLowerCase().equals("day"))
			{
				formatter.applyPattern("dd");
			}
			else if (detail.toLowerCase().equals("month"))
			{
				formatter.applyPattern("MMMM");
			}
			else if (detail.toLowerCase().equals("year"))
			{
				formatter.applyPattern("yyyy");
			}
			else
			{
				return "";
			}
			return formatter.format(d);
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	public static String formatLanguage(String display_lang, String lang)
	{

		return new Locale(display_lang).getDisplayLanguage(new Locale(lang));
	}
	public static String uriEncode(String input)
	{
		String result = "";
		try {
			result = URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}

	public static String cgiSafe(String original, String lang)
	{

		original = original.replace('&', ' ');
		original = original.replaceAll(" ", "%20");
		return original;
	}

	public static String formatBigNumber(String num, String lang)
	{

		String num_str = num;
		char[] num_chars = num_str.toCharArray();
		String zero_str = "";
		String formatted_str = "";

		for (int i = num_chars.length - 4; i >= 0; i--)
		{
			zero_str += '0';
		}

		String sig_str = "";
		for (int i = 0; i < 3 && i < num_chars.length; i++)
		{
			sig_str = sig_str + num_chars[i];
			if (i == 1 && i + 1 < num_chars.length)
			{
				sig_str = sig_str + ".";
			}
		}

		int sig_int = Math.round(Float.parseFloat(sig_str));
		String new_sig_str = sig_int + "";
		if (sig_str.length() > 2)
		{
			new_sig_str = sig_int + "0";
		}

		char[] final_chars = (new_sig_str + zero_str).toCharArray();
		int count = 1;
		for (int i = final_chars.length - 1; i >= 0; i--)
		{
			formatted_str = final_chars[i] + formatted_str;
			if (count == 3 && i != 0)
			{
				formatted_str = "," + formatted_str;
				count = 1;
			}
			else
			{
				count++;
			}
		}
		return formatted_str;
	}

	public static String hashToSectionId(String hashString)
	{
		if (hashString == null || hashString.length() == 0)
		{
			return "";
		}

		int firstDotIndex = hashString.indexOf(".");
		if (firstDotIndex == -1)
		{
			return "";
		}

		String sectionString = hashString.substring(firstDotIndex + 1);

		return sectionString;
	}

	public static String hashToDepthClass(String hashString)
	{
		if (hashString == null || hashString.length() == 0)
		{
			return "";
		}

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

	public static String escapeNewLines(String str)
	{
		if (str == null || str.length() < 1)
		{
			return null;
		}
		return str.replace("\n", "\\\n");
	}

	public static String escapeQuotes(String str)
	{
		if (str == null || str.length() < 1)
		{
			return null;
		}
		return str.replace("\"", "\\\"");
	}

	public static String escapeNewLinesAndQuotes(String str)
	{
		if (str == null || str.length() < 1)
		{
			return null;
		}
		return escapeNewLines(escapeQuotes(str));
	}

	public static String getGlobalProperty(String name)
	{
		return GlobalProperties.getProperty(name);
	}

	public static void clearMetadataStorage()
	{
		_foundTableValues.clear();
	}

	public static boolean checkMetadataNotDuplicate(String name, String value)
	{
		if (_foundTableValues.containsKey(name))
		{
			for (String mapValue : _foundTableValues.get(name))
			{
				if (mapValue.equals(value))
				{
					return false;
				}
			}
			_foundTableValues.get(name).add(value);
			return true;
		}

		ArrayList<String> newList = new ArrayList<String>();
		newList.add(value);

		_foundTableValues.put(name, newList);

		return true;
	}

	public static String reCAPTCHAimage(String publicKey, String privateKey)
	{
		ReCaptcha c = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
		return c.createRecaptchaHtml(null, null);
	}

	public static String getInterfaceStringsAsJavascript(String interface_name, String lang, String prefix)
	{
		String prependToPrefix = "gs.text";
		return XSLTUtil.getInterfaceStringsAsJavascript(interface_name, lang, prefix, prependToPrefix);
	}

	// generates javascript: 2 arrays are declared and populated with strings that declare variables and assign their values
	// to be strings loaded from the interface_name.properties file for the language.    
	public static String getInterfaceStringsAsJavascript(String interface_name, String lang, String prefix, String prependToPrefix)
	{
		// 1. Generating Javascript of the form:
		// if(!gs.text) { gs.text = new Array(); }
		// if(!gs.text.dse) { gs.text.dse = new Array(); }
		StringBuffer outputStr = new StringBuffer();
		outputStr.append("if(!gs.text) { ");
		outputStr.append(prependToPrefix + " = new Array(); ");
		outputStr.append("}\n");
		outputStr.append("if(!gs.text." + prefix + ") { ");
		outputStr.append(prependToPrefix + "." + prefix + " = new Array(); ");
		outputStr.append("}\n");

		int foundCount = 0;

		for (String dictName : new String[] { "interface_" + interface_name, "interface_default", "interface_default2" })
		{
			Dictionary dict = new Dictionary(dictName, lang);
			Enumeration keys = dict.getKeys();
			if (keys == null)
			{
				continue;
			}

			// Get all properties in the language-specific dictionary with the given key prefix
			// Create Javascript strings of the form:
			// prependToPrefix.key= "value";\n
			while (keys.hasMoreElements())
			{
				String key = (String) keys.nextElement();
				if (key.startsWith(prefix))
				{
					String value = getInterfaceText(interface_name, dictName, lang, key, null);

					outputStr.append(prependToPrefix);
					outputStr.append(".");
					outputStr.append(key);
					outputStr.append("=\"");
					outputStr.append(value);
					outputStr.append("\";\n");
				}
			}

			if (foundCount > 0)
			{
				break;
			}
		}

		return outputStr.toString();
	}

	public static String xmlNodeToString(Node node)
	{
		return GSXML.xmlNodeToString(node);
	}

	// Test from cmdline with:
	// java -classpath /research/ak19/gs3-svn/web/WEB-INF/lib/gsdl3.jar:/research/ak19/gs3-svn/web/WEB-INF/lib/log4j-1.2.8.jar:/research/ak19/gs3-svn/web/WEB-INF/classes/ org.greenstone.gsdl3.util.XSLTUtil
	public static void main(String args[])
	{
		System.out.println("\n@@@@@\n" + XSLTUtil.getInterfaceStringsAsJavascript("default", "en", "dse", "gs.text") + "@@@@@\n");
	}
}
