/*
 *    ModuleWrapper.java
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
package org.greenstone.gsdl3.service;
import org.greenstone.gsdl3.core.ModuleInterface;

// XML classes
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document; 
import org.w3c.dom.Element;
import org.w3c.dom.Node; 
import org.w3c.dom.Node;                                                
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// java classes
import java.io.StringReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

import org.apache.log4j.*;

/**
 * A classes for logging and checking all traffic to and from a ModuleInterface
 * 
 * if logCounter hasn't been initialised, initialise it to a random 
 * value. This has to use secure random rather than a normal random
 * or every time the application is run the same numbers will be
 * generated and the search for a free range will become a linear
 * search rather than a hash.
 *
 *
 * @author <a href="mailto:s.yeates@cs.waikato.ac.nz">Stuart Yeates</a>
 * @version $Revision$
 */
public class ModuleWrapper 
  implements ModuleInterface 
{

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service. ModuleWrapper.class.getName());

  /** the module we're wrapping */
  protected ModuleInterface inner = null;
  /** the module we're wrapping */
  public ModuleInterface getInner(){return inner;};
  /** the module we're wrapping */
  public void setInner(ModuleInterface i){inner=i;};

  /** The schema of the IN XML */
  protected String fromSchema = null;
  /** The schema of the IN XML */
  public String getFromSchema(){return fromSchema;};
  /** The schema of the IN XML */
  public void setFromSchema(String s){fromSchema=s;};

  /** The schema of the OUT XML */
  protected String toSchema = null;
  /** The schema of the OUT XML */
  public String getToSchema(){return toSchema;};
  /** The schema of the OUT XML */
  public void setToSchema(String s){toSchema=s;};

  /** Are we logging the XML ? */
  protected boolean logging = true;
  /** Are we logging the XML ? */
  public boolean getLogging(){return logging;};
  /** Are we logging the XML ? */
  public void setLogging(boolean b){logging=b;};

  /** Where we are logging the XML */
  protected String logDirectory = "/tmp/";
  /** Where we are logging the XML */
  public String getLogDirectory(){return logDirectory;};
  /** Where we are logging the XML */
  public void setLogDirectory(String s){logDirectory=s;};

  /** The number of the previous log file */
  protected static long logCounter = 0;

  /** The no-args constructor */
  public ModuleWrapper(){

  }
  
  public void cleanUp() {}
  
  /** The all-args constructor */
  public ModuleWrapper(String in,String out, ModuleInterface inner){
    this.setFromSchema(in);
    this.setToSchema(out);
    this.setInner(inner);
  }

  /**
   * Process an XML request - as a String
   *
   * @param xmlIn the request to process
   * @return the response - contains any error messages
   * @see java.lang.String
   */
  public String process(String xmlIn) {
    
    long logNumber = 0;
    String xmlOut = "";
    if (getLogging()) {
      if (logCounter == 0)
	logCounter = new SecureRandom().nextLong();
      try {
	logNumber = logCounter++;
	String filename = getLogDirectory() + File.separator + logNumber + ".in";
	File file = new File(filename);
	
	while (file.exists()){
	  logCounter = new SecureRandom().nextLong();
	  logNumber = logCounter++;
	  filename = getLogDirectory() + File.separator + logNumber + ".in";
	  file = new File(filename);
	}
	
	FileWriter writer = new FileWriter(file);
	writer.write(xmlIn);
      } catch (IOException e) {
	logger.error("caught exception: " +e );
      }
    }
    xmlOut = processInner(xmlIn);

    try {
      String filename = getLogDirectory() + File.separator + logNumber + ".out";
      File file = new File(filename);
      FileWriter writer = new FileWriter(file);
      writer.write(xmlOut);
    } catch (IOException e) {
      logger.error("caught exception: " +e );
    }
    return xmlOut;
  }

  /**
   * Process an XML request - as a String
   *
   * @param xmlIn the request to process
   * @return the response - contains any error messages
   * @see java.lang.String
   */
  protected String processInner(String xmlIn) 
  {
    try {
      DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
      DocumentBuilder builder= factory.newDocumentBuilder();
      DOMParser parser = new DOMParser();
      try {
	
	parser.setFeature("http://xml.org/sax/features/validation",true);
	parser.setFeature("http://apache.org/xml/features/validation/schema",true);
	parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
	parser.setFeature("http://apache.org/xml/features/validation/schema", true);
	parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
	parser.setFeature("http://apache.org/xml/features/validation/schema/normalized-value", true);
	parser.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
	parser.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
	parser.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
      } catch (Exception a) {
	logger.error("unable to set feature:" +a);
	a.printStackTrace();
      }

      // do the pre-call checking
      InputSource xml_source = new InputSource(new StringReader(xmlIn));  
      try {
	parser.parse(xml_source);
      } catch (Exception e){  
	logger.error("parsing error:" +e);
	e.printStackTrace();
	return "<response> <error class=\"ModuleWrapper\" code=1> Error: supplied string contained the parse error: " + e + " </error> </response>";
      } 

      String xmlOut = inner.process(xmlIn);

      // do the post-call checking
      InputSource xmlResult = new InputSource(new StringReader(xmlIn));  
      try {
	parser.parse(xmlResult);
      } catch (Exception e){  
	logger.error("parsing error:" +e);
	e.printStackTrace();
	return "<response> <error class=\"ModuleWrapper\" code=2> Error: returned string contained the parse error: " + e + "</error></response>";
      } 

      return xmlOut;

    } catch (Exception e){  
      logger.error("other error:" +e);
      e.printStackTrace();
      return "<response> <error class=\"ModuleWrapper\" code=3>Error: Unknown error or warning: " + e + " </error></response>";
    } 
  }

  /**
   * Process an XML request - as a DOM Element
   *
   * @param in the request to process
   * @return the response - contains any error messages
   * @see org.w3c.dom.Element
   */
  public Node process(Node xmlIn) {
    throw new Error("Not implmented yet. Should be faked by stringizing the node.");
  }
}    
                                                                           








