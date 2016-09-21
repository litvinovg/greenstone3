/*
 *    DisplayItemUtil.java
 *    Copyright (C) 2016 New Zealand Digital Library, http://www.nzdl.org
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

import org.apache.log4j.Logger;

import org.greenstone.gsdl3.util.Dictionary;
import org.greenstone.gsdl3.util.GSXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** various methods for handling displayItems and choosing the right one for the speicifed language */

public class DisplayItemUtil
{

  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.DisplayItemUtil.class.getName());

  /** looks for displayItems from config element and stores them in displayItemList in the form
   
      <displayItem name="X">
        <displayItem name="X" lang="">value</displayItem>
        <displayItem name="X" lang="">value</displayItem>
      </displayItem>

   */
  public static boolean storeDisplayItems(Element display_item_list, Element config) {
    if (config == null) {
      logger.error("the config element is null, no displayItems to be found");
      return false;
    }
    Document doc = display_item_list.getOwnerDocument();
    NodeList displaynodes = config.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
    if (displaynodes.getLength() > 0) {
      
      for (int k = 0; k < displaynodes.getLength(); k++) {
	Element d = (Element) displaynodes.item(k);
	String name = d.getAttribute(GSXML.NAME_ATT);
	Element this_item = GSXML.getNamedElement(display_item_list, GSXML.DISPLAY_TEXT_ELEM, GSXML.NAME_ATT, name);
	if (this_item == null) {
	  this_item = doc.createElement(GSXML.DISPLAY_TEXT_ELEM);
	  this_item.setAttribute(GSXML.NAME_ATT, name);
	  display_item_list.appendChild(this_item);
	}
	
	this_item.appendChild(doc.importNode(d, true));
      }
    }
    
    return true;
     
  }
  
  /** Finds the best language specific match for each displayItem in display_item_list and adds it to description */
  public static boolean addLanguageSpecificDisplayItems(Element description, Element display_item_list, String lang, String default_lang, ClassLoader class_loader) {

    Document doc = description.getOwnerDocument();
    NodeList items = display_item_list.getChildNodes();
    for (int i = 0; i < items.getLength(); i++)
      { // for each key
	Element m = (Element) items.item(i);
	// is there one with the specified language?
	Element new_m = GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, lang);
	if (new_m == null) {
	  // if not, have we got one with a key?
	  new_m = GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.KEY_ATT, null);
	  if (new_m != null) {
	    // look up the dictionary
	    String value = getTextString(new_m.getAttribute(GSXML.KEY_ATT), lang, new_m.getAttribute(GSXML.DICTIONARY_ATT), class_loader);
	    if (value != null) {
	      GSXML.setNodeText(new_m, value);
	    }
	    else {
	      // haven't found the key in the dictionary, ignore this display item
	      new_m = null;
	    }
	  }
	}
	if (new_m == null && lang != default_lang) {
	  // still haven't got a value. can we use the default lang?
	  new_m = GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, default_lang);
	}
	if (new_m == null)
	  {
	    // STILL haven't found one, lets use the first one with a lang att (so we don't just get the key one back
	    new_m = (Element) GSXML.getNamedElement(m, GSXML.DISPLAY_TEXT_ELEM, GSXML.LANG_ATT, null);
	  }
	if (new_m != null) {
	  description.appendChild(doc.importNode(new_m, true));
	}
      } // for each key
    return true;
    
  }

  protected static String getTextString(String key, String lang, String dictionary, ClassLoader class_loader) {
    return getTextString(key, lang, dictionary, null, class_loader);
  }

  protected static String getTextString(String key, String lang, String dictionary, String[] args, ClassLoader class_loader)
  {
    Dictionary dict;
    if (class_loader != null) {
      dict = new Dictionary(dictionary, lang, class_loader);
    } else {
      dict = new Dictionary(dictionary, lang);
    }
    String result = dict.get(key, args);
    return result;
  }
  

}