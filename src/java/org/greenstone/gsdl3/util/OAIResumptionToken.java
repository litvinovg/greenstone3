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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


// import file Logger.java
import org.apache.log4j.*;

/** */
public class OAIResumptionToken {
  


  // Other fields we save
  public static final String CURRENT_SET = "current_set";
  public static final String CURRENT_CURSOR = "current_cursor";

  // for token XML
  public static final String TOKEN = "token";
  public static final String TOKEN_LIST = "tokenList";
  public static final String NAME = "name";
  public static final String EXPIRATION = "expiration";
  public static final String INITIAL_TIME = "initial";
  //public static final String FROM
  public static final String FILE_SEPARATOR = File.separator;
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.GSXML.class.getName());

  public static XMLConverter converter = new XMLConverter();
  public static Element resumption_token_elem = null;
  //used when saving the token file
  public static File resumption_token_file = null;

  private static HashMap<String, HashMap<String,String>> stored_tokens = new HashMap<String, HashMap<String,String>>();

  private static HashMap<String, Long> expiration_data = new HashMap<String, Long>();

  /** initialize stored resumption tokens and clear any expired ones*/
  public static void init() {
    if (!findOrCreateTokenFile()) {
      // can't read in old tokens
      logger.error("Can't find token file, so we can't initialise tokens");
      return;
    }

    Document token_doc = converter.getDOM(resumption_token_file, "utf-8");
    Element token_elem = null;
    if (token_doc != null) {
      token_elem = token_doc.getDocumentElement();
    } else {
      logger.error("Failed to parse oai resumption token file OAIResumptionToken.xml.");
      resetTokenFile();
      return;
    }
      
    // read in stored tokens
    NodeList tokens = token_elem.getElementsByTagName(TOKEN);
    for(int i=0; i<tokens.getLength(); i++) {
      Element token = (Element)tokens.item(i);
      String set = token.getAttribute(OAIXML.SET);
      if (set.isEmpty()) {
	set = null;
      }
      String meta_prefix = token.getAttribute(OAIXML.METADATA_PREFIX);
      if (meta_prefix.isEmpty()) {
	meta_prefix = null;
      }
      String from = token.getAttribute(OAIXML.FROM);
      if (from.isEmpty()) {
	from = null;
      }
      String until = token.getAttribute(OAIXML.UNTIL);
      if (until.isEmpty()) {
	until = null;
      }
      storeToken(token.getAttribute(NAME), set, meta_prefix, from, until, token.getAttribute(EXPIRATION));

    }
  }
  // store the token read in from saved tokens file
  protected static void storeToken(String name, String set, String metadata_prefix, String from, String until, String expiration) {
    HashMap<String, String> data = new HashMap<String, String>();
    data.put(OAIXML.FROM, from);
    data.put(OAIXML.UNTIL, until);
    data.put(OAIXML.SET, set);
    data.put(OAIXML.METADATA_PREFIX, metadata_prefix);
    stored_tokens.put(name, data);
    expiration_data.put(name, new Long(expiration));
  }

  /** generate the token, and store all the data for it */
  public static String createAndStoreResumptionToken(String set, String metadata_prefix, String from, String until, String cursor, String current_set, String current_cursor) {
    HashMap<String, String> data = new HashMap<String, String>();
    long expiration_date = System.currentTimeMillis();
    String base_token_name = ""+expiration_date;
    stored_tokens.put(base_token_name, data);   
    expiration_date += (OAIXML.getTokenExpiration());
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
    exp_date += (OAIXML.getTokenExpiration());
    expiration_data.put(token, exp_date);
    
    token = token + ":" + cursor + ":" + current_set + ":" + current_cursor;
    return token;
  }

  /** check that this token is currently valid */
  public static boolean isValidToken(String token) {
    // we clear expired tokens each time we check one.
    clearExpiredTokens();
    // take off the set/cursor parts to check for the main key
    if (token.indexOf(":") != -1) {
      token = token.substring(0, token.indexOf(":"));
      logger.info("looking up "+token);
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
      logger.info("getting data for "+base_name);
    }
    HashMap<String, String> data = new HashMap<String, String>(stored_tokens.get(base_name));
    if (data == null) { 
      logger.warn("data was null!!");
      return null;
    }
    // add in cursor, etc from the token name    
    if (base_name != token) {
      String[] parts = token.split(":");
      if (parts.length != 4) {
	// something wrong!!
      }
      data.put(OAIXML.CURSOR, parts[1]);
      data.put(CURRENT_SET, parts[2]);
      data.put(CURRENT_CURSOR, parts[3]);
      data.put(INITIAL_TIME, base_name);
    }
    return data;

  }

  // used to manually expire a particular token - eg when one of its collections has changed since token was issued.
  public static void expireToken(String token) {
    if (token.indexOf(":") != -1) {
      token = token.substring(0, token.indexOf(":"));
    }
    expiration_data.remove(token);
    stored_tokens.remove(token);
  }
  // read through all stored expiry dates, and delete any tokens that are too
  // old
  public static void clearExpiredTokens() {

    Set<Map.Entry<String,Long>> token_set = expiration_data.entrySet();
    Iterator<Map.Entry<String,Long>> i = token_set.iterator();
    int size = expiration_data.size();
    logger.info("start tokens "+size);
    Long time_now = System.currentTimeMillis();
    while (i.hasNext()==true) {
      Map.Entry<String,Long> entry = i.next();
      String key = entry.getKey();
      Long exp = entry.getValue();
      if (exp < time_now) {
	logger.info("token "+key+" is expired, "+ OAIXML.getTime(exp));
	i.remove();
	// also remove the token from the stored tokens
	stored_tokens.remove(key);
      }
    }
    size = expiration_data.size();
    logger.info("end tokens "+size);
  }

  protected static boolean findOrCreateTokenFile() {
    
    try {
      URL token_file_url = Class.forName("org.greenstone.gsdl3.OAIServer").getClassLoader().getResource("OAIResumptionToken.xml");
      if (token_file_url != null) {
	resumption_token_file = new File(token_file_url.toURI());
	if (resumption_token_file != null && resumption_token_file.exists()) {
	  return true;
	}
	else {
	  logger.error("Resumption token file found, "+ token_file_url.toURI()+" but couldn't create a file from it.");
	}
      }
    } catch (Exception e) {
      logger.error("couldn't find or work with ResumptionToken.xml "+e.getMessage());
    }
    // if we have got here, have't managed to load file via class loader -
    // it may not exist yet.
    // try and create a new empty one
    if (resetTokenFile()) {
      return true;
    }
    return false;
  }

  // If there was no file, or something went wrong with the file, use this to
  // create a new one.
  public static boolean resetTokenFile() {
    resumption_token_file = new File(GlobalProperties.getGSDL3Home() + 
				     FILE_SEPARATOR + "WEB-INF" + 
				     FILE_SEPARATOR + "classes" +
				     FILE_SEPARATOR + "OAIResumptionToken.xml");
    try {
      if (!resumption_token_file.exists()) {

	if (!resumption_token_file.createNewFile()) {
	  logger.error("Couldn't create a new resumption token file "+ resumption_token_file.getPath());
	  resumption_token_file = null;
	  return false;
	}
      }
    } catch (Exception e) {
      logger.error("Couldn't create a new resumption token file "+ resumption_token_file.getPath()+", "+e.getMessage());
      resumption_token_file = null;
      return false;
    }
    Document doc = converter.newDOM();
    Element elem = doc.createElement(TOKEN_LIST);
    if (converter.writeDOM(elem, resumption_token_file)) {
      return true;
    }
    resumption_token_file = null;
    return false;
  }
  

  public static boolean saveTokensToFile() {
    clearExpiredTokens();
    if (resumption_token_file == null) {
      logger.warn("no available resumption token file, not storing tokens");
      return false;
    }
    
    // Create an XML representation of the stored tokens
    Document doc = converter.newDOM();
    Element token_list = doc.createElement(TOKEN_LIST);

    if (stored_tokens.size() == 0) {
      // nothing to store, store an empty file
      converter.writeDOM(token_list, resumption_token_file);
      return true;
    }
    Set<String> keys = stored_tokens.keySet();
    Iterator<String> i = keys.iterator();
    while(i.hasNext()) {
      String token = i.next();
      HashMap<String, String> data = stored_tokens.get(token);
      Element token_elem = doc.createElement(TOKEN);
      token_elem.setAttribute(NAME, token);
      token_elem.setAttribute(OAIXML.FROM, data.get(OAIXML.FROM));
      token_elem.setAttribute(OAIXML.UNTIL, data.get(OAIXML.UNTIL));
      token_elem.setAttribute(OAIXML.SET, data.get(OAIXML.SET));
      token_elem.setAttribute(OAIXML.METADATA_PREFIX, data.get(OAIXML.METADATA_PREFIX));
      token_elem.setAttribute(EXPIRATION, ""+expiration_data.get(token));
      token_list.appendChild(token_elem);
    }
    converter.writeDOM(token_list, resumption_token_file);
    return true;
  }

}
