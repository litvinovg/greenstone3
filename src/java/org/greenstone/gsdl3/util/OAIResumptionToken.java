/*
 *    OAIResumptionToken.java
 *    Copyright (C) 2014 New Zealand Digital Library, http://www.nzdl.org
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

import org.greenstone.util.GlobalProperties;

import org.w3c.dom.*;

import java.io.File;
import java.util.HashMap;

// import file Logger.java
import org.apache.log4j.*;

/** */
public class OAIResumptionToken {
  


  // Other fields we save
  public static final String CURRENT_SET = "current_set";
  public static final String CURRENT_CURSOR = "current_cursor";

  public static final String FILE_SEPARATOR = File.separator;
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSXML.class.getName());

  public static XMLConverter converter = new XMLConverter();
  public static Element resumption_token_elem = null;
  //used when saving the token file
  public static File resumption_token_file = null;

  private static HashMap<String, HashMap<String,String>> stored_tokens = new HashMap<String, HashMap<String,String>>();

  private static HashMap<String, Long> expiration_data = new HashMap<String, Long>();

  /** initialize stored resumption tokens */
  public static void init() {

  }
  /** generate the token, and store all the data for it */
  public static String createAndStoreResumptionToken(String set, String metadata_prefix, String from, String until, String cursor, String current_set, String current_cursor) {
    HashMap<String, String> data = new HashMap<String, String>();
    long expiration_date = System.currentTimeMillis();
    String base_token_name = ""+expiration_date;
    stored_tokens.put(base_token_name, data);   
    expiration_date += (OAIXML.token_expiration*1000);
    expiration_data.put(base_token_name, new Long(expiration_date));
    String token_name = base_token_name+":" + cursor + ":" + current_set +":"+ current_cursor;
    data.put(OAIXML.FROM, from);
    data.put(OAIXML.UNTIL, until);
    data.put(OAIXML.SET, set);
    data.put(OAIXML.METADATA_PREFIX, metadata_prefix);
    return token_name;

  }

  public static long getExpirationDate(String token) {
    if (token.indexOf(":") != -1) {
      token = token.substring(0, token.indexOf(":"));
    }
    return expiration_data.get(token).longValue();
  }

  public static String updateToken(String token, String cursor, String current_set, String current_cursor) {
    if (token.indexOf(":") != -1) {
      token = token.substring(0, token.indexOf(":"));
    }

    // when we are generating a new token, we update the expiration date to allow the new token to have the full length of time
    long exp_date = System.currentTimeMillis();
    exp_date += (OAIXML.token_expiration*1000);
    expiration_data.put(token, exp_date);
    
    token = token + ":" + cursor + ":" + current_set + ":" + current_cursor;
    return token;
  }

  /** check that this token is currently valid */
  public static boolean isValidToken(String token) {
    // take off the set/cursor parts to check for the main key
    if (token.indexOf(":") != -1) {
      token = token.substring(0, token.indexOf(":"));
      logger.error("looking up "+token);
    }
    if (stored_tokens.containsKey(token)) {
      return true;
    }
    return false;
  }

  public static HashMap<String, String> getTokenData(String token) {
    // find the base name
    String base_name = token;
    if (token.indexOf(":") != -1) {
      base_name = token.substring(0, token.indexOf(":"));
      logger.error("getting data for "+base_name);
    }
    HashMap<String, String> data = new HashMap<String, String>(stored_tokens.get(base_name));
    if (data == null) { 
      logger.error("data was null!!");
      return null;
    }
    if (base_name != token) {
      String[] parts = token.split(":");
      if (parts.length != 4) {
	// something wrong!!
      }
      data.put(OAIXML.CURSOR, parts[1]);
      data.put(CURRENT_SET, parts[2]);
      data.put(CURRENT_CURSOR, parts[3]);
    }
    // add in cursor, etc form the token name
    //return stored_tokens.get(token);
    return data;

  }

  public static void clearExpiredTokens() {

    
  }
  // ********************************************************************
  // *********************************************************************
  // following is old code
  /** Read in OAIResumptionToken.xml (residing web/WEB-INF/classes/) */ 
  public static Element getOAIResumptionTokenXML() {     
      
    // The system environment variable $GSDL3HOME(ends ../web) does not contain the file separator 
    resumption_token_file = new File(GlobalProperties.getGSDL3Home() + FILE_SEPARATOR +
				     "WEB-INF" + FILE_SEPARATOR + "classes" +FILE_SEPARATOR + "OAIResumptionToken.xml");
    if (resumption_token_file.exists()) {
      Document token_doc = converter.getDOM(resumption_token_file, "utf-8");
      if (token_doc != null) {
	resumption_token_elem = token_doc.getDocumentElement();
      } else {
	logger.error("Fail to parse resumption token file OAIReceptionToken.xml.");
	return null;
      }
      //remove all expired tokens
      clearExpiredTokens();
      return resumption_token_elem;  
    }
    //if resumption_token_file does not exist        
    logger.info("resumption token file: "+ resumption_token_file.getPath()+" not found! create an empty one.");
    Document doc = converter.newDOM();
    resumption_token_elem = doc.createElement(/*OAI_RESUMPTION_TOKENS*/"resumptionTokens");
    saveOAIResumptionTokenXML(resumption_token_elem);
    return resumption_token_elem;
  }
  public static void saveOAIResumptionTokenXML(Element token_elem) {     
    if(converter.writeDOM(token_elem, resumption_token_file) == false) {
      logger.error("Fail to save the resumption token file");
    }
  }
  public static void clearExpiredTokensOld() {
    boolean token_deleted = false;
    NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, OAIXML.RESUMPTION_TOKEN);
    for (int i=0; i<tokens.getLength(); i++) {
      Element token_elem = (Element)tokens.item(i);
      String expire_str = token_elem.getAttribute(OAIXML.EXPIRATION_DATE);
      long datestamp = OAIXML.getTime(expire_str); // expire_str is in milliseconds
      if(datestamp < System.currentTimeMillis()) {
	resumption_token_elem.removeChild(token_elem);
	token_elem = null;
	token_deleted = true;
      }
    } 
      
    if(token_deleted) {
      saveOAIResumptionTokenXML(resumption_token_elem);
    }
  }
  public static boolean containsToken(String token) {
    NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, OAIXML.RESUMPTION_TOKEN);
    for (int i=0; i<tokens.getLength(); i++) {
      if(token.equals(GSXML.getNodeText((Element)tokens.item(i)).trim() ))
	return true;
    }
    return false;
  }
  public static void addToken(Element token) {
    Document doc = resumption_token_elem.getOwnerDocument();
    resumption_token_elem.appendChild(doc.importNode(token, true));
    saveOAIResumptionTokenXML(resumption_token_elem);
  }
  public static void addToken(String token) {
    Element te = resumption_token_elem.getOwnerDocument().createElement(OAIXML.RESUMPTION_TOKEN);
    //add expiration att
    resumption_token_elem.appendChild(te);
    saveOAIResumptionTokenXML(resumption_token_elem);
  }
  public static boolean removeToken(String token) {
    NodeList tokens = GSXML.getChildrenByTagName(resumption_token_elem, OAIXML.RESUMPTION_TOKEN);
    int num_tokens = tokens.getLength();
    for (int i=0; i<num_tokens; i++) {
      Element e = (Element)(tokens.item(i));
      if(token.equals(GSXML.getNodeText(e))) {
	resumption_token_elem.removeChild(e);
	saveOAIResumptionTokenXML(resumption_token_elem);
	return true;
      }
    }
    return false;      
  }


}
