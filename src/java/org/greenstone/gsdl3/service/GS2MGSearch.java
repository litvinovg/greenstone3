/*
 *    GS2MGSearch.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *   the Free Software Foundation; either version 2 of the License, or
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
import org.greenstone.mg.*;
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// java
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.io.File;

import org.apache.log4j.*;

/**
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @author <a href="mailto:mdewsnip@cs.waikato.ac.nz">Michael Dewsnip</a>
 */

public class GS2MGSearch
extends AbstractGS2Search {
    
    protected static MGSearchWrapper mg_src = null;
    
    static Logger logger = Logger.getLogger (org.greenstone.gsdl3.service.GS2MGSearch.class.getName ());
    
    
    /** constructor */
    public GS2MGSearch () {
	if(this.mg_src == null){
	    this.mg_src = new MGSearchWrapper ();
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
        
        this.mg_src.setMaxNumeric (this.maxnumeric);
        return true;
    }
    
    
    
    /** do the actual query */
    protected Element processTextQuery (Element request) {
        synchronized(this.mg_src){
        // Create a new (empty) result message ('doc' is in ServiceRack.java)
        Element result = this.doc.createElement (GSXML.RESPONSE_ELEM);
        result.setAttribute (GSXML.FROM_ATT, TEXT_QUERY_SERVICE);
        result.setAttribute (GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
        
        // Get the parameters of the request
        Element param_list = (Element) GSXML.getChildByTagName (request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
        if (param_list == null) {
            logger.error ("TextQuery request had no paramList.");
            return result;  // Return the empty result
        }
        
        // Process the request parameters
        HashMap params = GSXML.extractParams (param_list, false);
        
        // Make sure a query has been specified
        String query = (String) params.get (QUERY_PARAM);
        if (query == null || query.equals ("")) {
            return result;  // Return the empty result
        }
        
        // If an index hasn't been specified, use the default
        String index = (String) params.get (INDEX_PARAM);
        if (index == null) {
            index = this.default_index;
        }
        
        // If a subcollection index has been specified, use it
        String indexSub = (String) params.get (INDEX_SUBCOLLECTION_PARAM);
        if (indexSub != null) {
            index += indexSub;
        }
        else{
            if (!this.default_index_subcollection.equals ("")){
                index += this.default_index_subcollection;
            }
        }
        
        // If a subcollection index has been specified, use it
        String indexLang = (String) params.get (INDEX_LANGUAGE_PARAM);
        if (indexLang != null) {
            index += indexLang;
        }
        else{
            if (!this.default_index_language.equals ("")){
                index += this.default_index_language;
            }
        }
        
        // The location of the MG index and text files
        String basedir = GSFile.collectionBaseDir (this.site_home, this.cluster_name) +  File.separatorChar;  // Needed for MG
        String textdir = GSFile.collectionTextPath (this.index_stem);
        String indexpath = GSFile.collectionIndexPath (this.index_stem, index);
        this.mg_src.setIndex (indexpath);
        
        // set the mg query parameters to the values the user has specified
        setStandardQueryParams (params);
        this.mg_src.runQuery (basedir, textdir, query);
        MGQueryResult mqr = this.mg_src.getQueryResult ();
        if (mqr.isClear ()) {
            // something has gone wrong
            GSXML.addError (this.doc, result, "Couldn't query the mg database", GSXML.ERROR_TYPE_SYSTEM);
            return result;
        }
        long totalDocs = mqr.getTotalDocs ();
        
        // Get the docnums out, and convert to HASH ids
        Vector docs = mqr.getDocs ();
        if (docs.size () == 0) {
            logger.error ("No results found...\n");
        }
        
        // Create a metadata list to store information about the query results
        Element metadata_list = this.doc.createElement (GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER);
        result.appendChild (metadata_list);
        
        // Add a metadata element specifying the number of matching documents
        // because teh total number is just the number returned, use numDocsReturned, not numDocsMatched
        GSXML.addMetadata (this.doc, metadata_list, "numDocsReturned", ""+totalDocs);
        // add a metadata item to specify what actual query was done - eg if stuff was stripped out etc. and then we can use the query later, cos we don't know which parameter was the query
        GSXML.addMetadata (this.doc, metadata_list, "query", query);
        
        if (docs.size () > 0) {
            // Create a document list to store the matching documents, and add them
            Element document_list = this.doc.createElement (GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
            result.appendChild (document_list);
            for (int d = 0; d < docs.size (); d++) {
                long docnum = ((MGDocInfo) docs.elementAt (d)).num_;
                float rank = ((MGDocInfo) docs.elementAt (d)).rank_;
                String doc_id = internalNum2OID (docnum);
                Element doc_node = createDocNode (doc_id, Float.toString (rank));
                document_list.appendChild (doc_node);
            }
        }
        
        // Create a term list to store the term information, and add it
        Element term_list = this.doc.createElement (GSXML.TERM_ELEM+GSXML.LIST_MODIFIER);
        result.appendChild (term_list);
        Vector terms = mqr.getTerms ();
        for (int t = 0; t < terms.size (); t++) {
            MGTermInfo term_info = (MGTermInfo) terms.get (t);
            
            String term = term_info.term_;
            int stem_method = term_info.stem_method_;
            Vector equiv_terms = term_info.equiv_terms_;
            
            Element term_elem = this.doc.createElement (GSXML.TERM_ELEM);
            term_elem.setAttribute (GSXML.NAME_ATT, term);
            term_elem.setAttribute (STEM_ATT, "" + stem_method);
            
            Element equiv_term_list = this.doc.createElement (EQUIV_TERM_ELEM+GSXML.LIST_MODIFIER);
            term_elem.appendChild (equiv_term_list);
            
            long total_term_freq = 0;
            for (int et = 0; et < equiv_terms.size (); et++) {
                MGEquivTermInfo equiv_term_info = (MGEquivTermInfo) equiv_terms.get (et);
                
                Element equiv_term_elem = this.doc.createElement (GSXML.TERM_ELEM);
                equiv_term_elem.setAttribute (GSXML.NAME_ATT, equiv_term_info.term_);
                equiv_term_elem.setAttribute (NUM_DOCS_MATCH_ATT, "" + equiv_term_info.match_docs_);
                equiv_term_elem.setAttribute (FREQ_ATT, "" + equiv_term_info.term_freq_);
                equiv_term_list.appendChild (equiv_term_elem);
                
                total_term_freq += equiv_term_info.term_freq_;
            }
            
            term_elem.setAttribute (FREQ_ATT, "" + total_term_freq);
            term_list.appendChild (term_elem);
        }
        return result;
	}//end of synchronized
    }
    
    // should probably use a list rather than map
    protected boolean setStandardQueryParams(HashMap params)
    {
	// set the default settings that gs uses
	this.mg_src.setReturnTerms(true);
	this.mg_src.setCase(true);
	this.mg_src.setStem(false);	
	Set entries = params.entrySet();
	Iterator i = entries.iterator();
	while (i.hasNext()) {
	    Map.Entry m = (Map.Entry)i.next();
	    String name = (String)m.getKey();
	    String value = (String)m.getValue();

	    if (name.equals(CASE_PARAM) && this.does_case) {
		boolean val = (value.equals(BOOLEAN_PARAM_ON) ? true : false);
		this.mg_src.setCase(val);
	    }
	    else if (name.equals(STEM_PARAM) && this.does_stem) {
		boolean val = (value.equals(BOOLEAN_PARAM_ON) ? true : false);
		this.mg_src.setStem(val);
	    }
	    else if (name.equals(MATCH_PARAM)) {
		int mode = (value.equals(MATCH_PARAM_ALL) ? 1 : 0);
		this.mg_src.setMatchMode(mode);
	    }
	    else if (name.equals(MAXDOCS_PARAM)) {
		int docs = Integer.parseInt(value);
		this.mg_src.setMaxDocs(docs);
	    } // ignore any others
	}
	return true;
    }
    
    
}


