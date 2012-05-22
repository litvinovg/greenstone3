/*
*    SharedSoleneGS2FieldSearch.java -- shared base code for Solr and Lucene
*    Copyright (C) 2006 New Zealand Digital Library, http://www.nzdl.org
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
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Document; 
// java classes
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Vector;

// Logging
import org.apache.log4j.Logger;

import org.greenstone.LuceneWrapper3.SharedSoleneQueryResult;
import org.greenstone.LuceneWrapper3.SharedSoleneQuery;

// Shared code for Solr and Lucene GS2FieldSearch

public abstract class SharedSoleneGS2FieldSearch extends AbstractGS2FieldSearch
{
    protected static final String RANK_PARAM_RANK_VALUE = "rank";

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.SharedSoleneGS2FieldSearch.class.getName());

    protected SharedSoleneQuery solene_src=null;    

    public SharedSoleneGS2FieldSearch()
    {
	// Lucene/Solr uses double operators, not single
	AND_OPERATOR = "&&";
	OR_OPERATOR = "||";

	does_paging = true;
	does_chunking = true;
    }
	
	
    /** configure this service */
    public boolean configure(Element info, Element extra_info)
    {
	if (!super.configure(info, extra_info)){
	    return false;
	}
		
	// Lucene/Solr doesn't do case folding or stemming or accent folding at the 
	// moment
	does_case = false;
	does_stem = false;
	does_accent = false;

	return true;
    }
	
    /** add in the Lucene/Solr specific params to TextQuery */
    protected void addCustomQueryParams(Element param_list, String lang) 
    {
	super.addCustomQueryParams(param_list, lang);
	/** Lucene's/Solr's rank param is based on index fields, not ranked/not */
	createParameter(RANK_PARAM, param_list, lang);
    }
	
    /** create a param and add to the list */
    /** we override this to do a special rank param */
    protected void createParameter(String name, Element param_list, String lang) 
    {
		Element param = null;
		if (name.equals(RANK_PARAM)) {
			// get the fields
			ArrayList<String> fields = new ArrayList<String>();
			fields.add(RANK_PARAM_RANK_VALUE);
			ArrayList<String> field_names = new ArrayList<String>();
			field_names.add(getTextString("param.sortBy.rank", lang)); 
			getSortByIndexData(fields, field_names, lang);
			
			param = GSXML.createParameterDescription2(this.doc, name, getTextString("param."+name, lang), GSXML.PARAM_TYPE_ENUM_SINGLE, fields.get(0), fields, field_names );
		}
		if (param != null) {
			param_list.appendChild(param);
		} else {
			super.createParameter(name, param_list, lang);
		}
	}
	
	protected void getSortByIndexData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang) {
		// the index info -
		Element index_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_ELEM+GSXML.LIST_MODIFIER);
		NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
		int len = indexes.getLength();
		// now add even if there is only one
		for (int i=0; i<len; i++) {
			Element index = (Element)indexes.item(i);
			String shortname = index.getAttribute(GSXML.SHORTNAME_ATT);
			if (shortname.equals("") || shortname.equals("ZZ") || shortname.equals("TX")) {
				continue;
			}
			index_ids.add("by"+shortname);
			String display_name = GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en");
			if (display_name.equals("")) {
				display_name = index.getAttribute(GSXML.NAME_ATT);
				if (display_name.equals("")) {
					display_name = shortname;
				}
			}
			index_names.add(display_name);
			
		}
		
	}

    protected String addFieldInfo(String query, String field) {
	if (field.equals("") || field.equals("ZZ")) {
	    return query;
	}
	return field+":("+query+")";
    }
	
    protected void addQueryElem(StringBuffer s, String q, String f, String c) {
	
	String combine="";
	if (s.length()>0) {
	    combine = " "+c+" ";
	}
	s.append(combine + addFieldInfo(q,f));
    }
	
    /** Lucene/Solr doesn't use these options at the moment */
    protected String addStemOptions(String query, String stem,
				    String casef, String accent) 
    {
	return query;
    }

    /** Lucene/Solr does not use internal ids. It just uses hash ids. So we need 
	to override these methods so no conversion is done. */
    /** convert indexer internal id to Greenstone oid */
    protected String internalNum2OID(long docnum)
    {
	return Long.toString(docnum);	
    }

    protected String internalNum2OID(String docnum)
    {
	return docnum;
	
    }
}
