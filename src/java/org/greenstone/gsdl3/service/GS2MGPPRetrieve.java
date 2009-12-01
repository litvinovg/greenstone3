/*
 *    GS2MGPPRetrieve.java
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
package org.greenstone.gsdl3.service;

// Greenstone classes
import org.greenstone.mgpp.*;
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;

// XML classes
import org.w3c.dom.Element;
import org.w3c.dom.Text;

// General Java classes
import java.io.File;

import org.apache.log4j.*;

public class GS2MGPPRetrieve
  extends AbstractGS2DocumentRetrieve {
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GS2MGPPRetrieve.class.getName());
  
  // Parameters used
  private static final String LEVEL_PARAM = "level";
  
  // Elements used in the config file that are specific to this class
  private static final String DEFAULT_LEVEL_ELEM = "defaultLevel";
  
  private static MGPPRetrieveWrapper mgpp_src = null;
  
  private String default_level = null;
  private String mgpp_textdir = null;
  
  public GS2MGPPRetrieve() {
    if(mgpp_src == null) {
      mgpp_src = new MGPPRetrieveWrapper();
    }
  }
  
  public void cleanUp() {
    super.cleanUp();
  }
  
  /** configure this service */
  public boolean configure(Element info, Element extra_info) {
    if (!super.configure(info, extra_info)){
      return false;
    }
    
    // Do specific configuration
    logger.info("Configuring GS2MGPPRetrieve...");
    
    // Get the default level out of <defaultLevel> (buildConfig.xml)
    Element def = (Element) GSXML.getChildByTagName(info, DEFAULT_LEVEL_ELEM);
    if (def != null) {
      this.default_level = def.getAttribute(GSXML.SHORTNAME_ATT);
    }
    if (this.default_level == null || this.default_level.equals("")) {
      logger.error("default level not specified!");
      return false;
    }
    
    // The location of the MGPP text files
    mgpp_textdir = GSFile.collectionBaseDir(this.site_home, this.cluster_name) +
      File.separatorChar + GSFile.collectionTextPath(this.index_stem);
    
    // Do generic configuration
    return true;
    
  }
  
  /** returns the content of a node
   * should return a nodeContent element:
   * <nodeContent>text content or other elements</nodeContent>
   */
  protected Element getNodeContent(String doc_id, String lang) throws GSException {
    long doc_num = this.coll_db.OID2DocnumLong(doc_id);
    if (doc_num == -1) {
      logger.error("OID "+doc_id +" couldn't be converted to mgpp num");
      return null;
    }
    Element content_node = this.doc.createElement(GSXML.NODE_CONTENT_ELEM);
    synchronized (mgpp_src) {
    	String doc_content = "";
    	try {
    		
    		doc_content = mgpp_src.getDocument(this.mgpp_textdir,
    				this.default_level,
    				doc_num);
    		
    		if (doc_content != null) {
    			doc_content = resolveTextMacros(doc_content, doc_id, lang);
    		}
    		
    	} catch (Exception e) {
    		logger.info("exception happended with mgpp_src.getDocument()" + e);
    		doc_content = "this is the content for section hash id "+ doc_id+", mgpp doc num "+doc_num+"\n";
    		
    	}
    	Text t = this.doc.createTextNode(doc_content);
    	content_node.appendChild(t);
    	return content_node;
    }//end of synchronized
  }
  
  
}
