/*
 *    XMLUtil.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.selfContained;

// XML classes
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.parsers.DOMParser;

// other java classes
import java.io.IOException;


/**
 * Class XMLUtil holds static global XML configuration objects. 
 * 
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s
.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * 
 */
class XMLUtil {

  /** the singleton instance */
  protected static XMLUtil instance = null;


  /** JAXP parser factory */ 
  protected DocumentBuilderFactory doc_build_fact=null;

  /** JAXP parser factory */ 
  static public DocumentBuilderFactory getDocumentBuilderFactory() {
    if (instance == null) 
      init();
    return instance.doc_build_fact;
  }

  /** JAXP parser */ 
  protected DocumentBuilder doc_builder=null;

  /** JAXP parser */ 
  static public DocumentBuilder getDocumentBuilder() {
    if (instance == null) 
      init();
    return instance.doc_builder;
   }

  /** xerces parser */
  protected DOMParser parser = null;

  /** xerces parser */
  static public DOMParser getDOMParser() {
    if (instance == null) 
      init();
    return instance.parser;
  }

  /** initalise the statics */
  static protected void init() {
    instance = new XMLUtil();
    try {
      instance.doc_build_fact = DocumentBuilderFactory.newInstance();
      
      instance.doc_builder = instance.doc_build_fact.newDocumentBuilder(); 
      instance.parser = new DOMParser();
    } catch (Exception e) {
      System.out.println("XMLConverter:exception "+e.getMessage());
    }
  }

  /**
   * Checks that the objects can be instantiated
   *
   */
  public static void main(String args[]) throws IOException
  {
    XMLUtil.getDOMParser();
  }
}

