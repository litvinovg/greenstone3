/*
 *    GS2MGRetrieve.java
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
import org.greenstone.mg.MGRetrieveWrapper;
import org.greenstone.gsdl3.core.GSException;
import org.greenstone.gsdl3.util.GSFile;
import org.greenstone.gsdl3.util.GSXML;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

// General Java classes
import java.io.File;

import org.apache.log4j.*;

// Apache Commons
import org.apache.commons.lang3.*;

public class GS2MGRetrieve
extends AbstractGS2DocumentRetrieve {
    static Logger logger = Logger.getLogger (org.greenstone.gsdl3.service.GS2MGRetrieve.class.getName ());
    
    // Elements used in the config file that are specific to this class
    private static final String DEFAULT_INDEX_ELEM = "defaultIndex";
    private static final String INDEX_LIST_ELEM = "indexList";
    private static final String INDEX_ELEM = "index";
    private static final String DEFAULT_INDEX_SUBCOLLECTION_ELEM = "defaultIndexSubcollection";
    private static final String DEFAULT_INDEX_LANGUAGE_ELEM = "defaultIndexLanguage";
    
    private static MGRetrieveWrapper mg_src = null;
    private String mg_basedir = null;
    private String mg_textdir = null;
    private String default_index = null;
    private boolean has_default_index = false;
    
    public GS2MGRetrieve () {
	if (this.mg_src == null){
	    this.mg_src = new MGRetrieveWrapper ();
	}
    }
    
    public void cleanUp () {
        super.cleanUp ();
        this.mg_src.unloadIndexData ();
    }
    
    /** configure this service */
    public boolean configure (Element info, Element extra_info) {
        if (!super.configure (info, extra_info)){
            return false;
        }
        
        // Do specific configuration
        logger.info ("Configuring GS2MGRetrieve...");
        
        // Get the default index out of <defaultIndex> (buildConfig.xml)
        Element def = (Element) GSXML.getChildByTagName (info, DEFAULT_INDEX_ELEM);
        if (def != null) {
            this.default_index = def.getAttribute (GSXML.SHORTNAME_ATT);
        }
        Element defSub = (Element) GSXML.getChildByTagName (info, DEFAULT_INDEX_SUBCOLLECTION_ELEM);
        if (defSub != null) {
            this.default_index += defSub.getAttribute (GSXML.SHORTNAME_ATT);
            logger.info ("default indexSubcollection is "+defSub.getAttribute (GSXML.SHORTNAME_ATT));
        } //concate defaultIndex + defaultIndexSubcollection
        
        //get the default indexLanguage out of <defaultIndexLanguage> (buildConfig.xml)
        Element defLang = (Element) GSXML.getChildByTagName (info, DEFAULT_INDEX_LANGUAGE_ELEM);
        if (defLang != null) {
            this.default_index += defLang.getAttribute (GSXML.SHORTNAME_ATT);
            logger.info ("default indexLanguage is "+defLang.getAttribute (GSXML.SHORTNAME_ATT));
        } //concate defaultIndex + defaultIndexSubcollection + defaultIndexLanguage
        
        
        if (this.default_index == null || this.default_index.equals ("")) {
            logger.error ("default index is not specified, the content of a document will not be retrieved");
            has_default_index = false;
            return true;
            //}
        }
        logger.debug ("Default index: " + this.default_index);
        
        // The location of the MG index and text files
        mg_basedir = GSFile.collectionBaseDir (this.site_home, this.cluster_name) + File.separatorChar;  // Needed by MG
        mg_textdir = GSFile.collectionTextPath (this.index_stem);
        // index is only needed to start up MG, not used so just use the default index
        String indexpath = GSFile.collectionIndexPath (this.index_stem, this.default_index);
        this.mg_src.setIndex (indexpath);
        has_default_index = true;
        return true;
    }
    
    /** returns the content of a node
     * should return a nodeContent element:
     * <nodeContent>text content or other elements</nodeContent>
     */
  protected Element getNodeContent (Document doc, String doc_id, String lang) throws GSException {
        long doc_num = this.coll_db.OID2DocnumLong (doc_id);
        if (doc_num == -1) {
            logger.error ("OID "+doc_id +" couldn't be converted to mg num");
            return null;
        }
        Element content_node = doc.createElement (GSXML.NODE_CONTENT_ELEM);
                
        String doc_content = null;
        
        //means that this.mg_src is up and running
        if (has_default_index ){
	    synchronized(this.mg_src){
                String indexpath = GSFile.collectionIndexPath(this.index_stem, this.default_index);        
		this.mg_src.setIndex(indexpath);
		doc_content =  this.mg_src.getDocument (this.mg_basedir,
							this.mg_textdir, doc_num);
	    }
        }
        
        if (doc_content!=null) {
            // remove any ctrl-c or ctrl-b
            doc_content = StringUtils.replace(doc_content, "\u0002|\u0003", "");
            // replace _httpimg_ with the correct address
            doc_content = resolveTextMacros (doc_content, doc_id, lang);
            //GSXML.addDocText(doc, doc_content);
        } else {
            logger.error ("the doc content was null, not getting that section\n");
            doc_content = "couldn't retrieve content for this section, please check the log file for more detail\n";
        }
        Text t = doc.createTextNode (doc_content);
        content_node.appendChild (t);
        return content_node;
        
    }       
}
