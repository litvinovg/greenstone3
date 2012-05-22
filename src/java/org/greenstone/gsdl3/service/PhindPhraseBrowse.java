/*
 *    PhindServices.java
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

import org.greenstone.gsdl3.util.*;

import org.greenstone.mgpp.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.Vector;
import java.util.HashMap;
import java.io.File;
import java.io.Serializable;

import org.apache.log4j.*;

/**
 * PhindServices - the phind phrase browsing service
 *
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */
public class PhindPhraseBrowse
  extends ServiceRack {
  
  static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.PhindPhraseBrowse.class.getName());
  
  // the services on offer
  private static final String PHIND_SERVICE = "PhindApplet";
  
  private static MGPPRetrieveWrapper mgpp_retrieve_src=null;
  private static MGPPSearchWrapper mgpp_search_src=null;
  private String basepath = null;
  
  private Element applet_description = null;
  
  public PhindPhraseBrowse() {
    if(this.mgpp_retrieve_src == null) {
      this.mgpp_retrieve_src = new MGPPRetrieveWrapper();
    }
    if(this.mgpp_search_src == null) {
      this.mgpp_search_src = new MGPPSearchWrapper();
    }
    // set up the default params
    this.mgpp_search_src.setQueryLevel("Document");
    this.mgpp_search_src.setReturnLevel("Document");
    this.mgpp_search_src.setMaxDocs(5);
    this.mgpp_search_src.setStem(false);
    this.mgpp_search_src.setCase(true);
  }
  
  public void cleanUp() {
    super.cleanUp();
    this.mgpp_search_src.unloadIndexData();
  }
  
  /** configure the service module
   *
   * @param info a DOM Element containing any config info for the service
   * @return true if configured
   */
  public boolean configure(Element info, Element extra_info) {
    
    if (!super.configure(info, extra_info)){
      return false;
    }
    
    logger.info("configuring PhindPhraseBrowse");
    
    // set up short_service_info_ - for now just has name and type
    Element e = this.doc.createElement(GSXML.SERVICE_ELEM);
    e.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_APPLET);
    e.setAttribute(GSXML.NAME_ATT, PHIND_SERVICE);
    this.short_service_info.appendChild(e);
    
    // set up the static applet description
    
    applet_description = this.doc.createElement(GSXML.SERVICE_ELEM);
    applet_description.setAttribute(GSXML.TYPE_ATT, GSXML.SERVICE_TYPE_APPLET);
    applet_description.setAttribute(GSXML.NAME_ATT, PHIND_SERVICE);
    
    // add in the applet info for the phind applet
    // need to make this dynamic - library names etc
    // change the applet params - have a single param with the library name
    // this is left blank at this end, and must be filled in by applet action - if the library name is not needed, this param is left out
    // phindcgi param now is not complete - library must be prepended to it.
    String app_info = "<"+GSXML.APPLET_ELEM+" CODEBASE='applet' CODE='org.greenstone.applet.phind.Phind.class' ARCHIVE='phind.jar, xercesImpl.jar, xml-apis.jar' WIDTH='500' HEIGHT='400'><PARAM NAME='library' VALUE=''/> <PARAM NAME='phindcgi' VALUE='?";
    app_info += GSParams.ACTION +"=a&amp;"+GSParams.REQUEST_TYPE +"=r&amp;"+GSParams.SERVICE+"="+PHIND_SERVICE+"&amp;"+GSParams.OUTPUT+"=xml&amp;"+GSParams.RESPONSE_ONLY+"=1'/>";
    app_info +="<PARAM NAME='collection'   VALUE='";
    app_info += this.cluster_name;
    app_info += "'/> <PARAM NAME='classifier' VALUE='1'/>  <PARAM NAME='orientation'  VALUE='vertical'/> <PARAM NAME='depth' VALUE='2'/> <PARAM NAME='resultorder' VALUE='L,l,E,e,D,d'/> <PARAM NAME='backdrop' VALUE='interfaces/default/images/phindbg1.jpg'/><PARAM NAME='fontsize' VALUE='10'/> <PARAM NAME='blocksize'    VALUE='10'/>The Phind java applet.</"+GSXML.APPLET_ELEM+">";
    
    Document dom = this.converter.getDOM(app_info);
    if (dom==null) {
      logger.error("Couldn't parse applet info");
      return false;
    }
    Element app_elem = dom.getDocumentElement();
    applet_description.appendChild(this.doc.importNode(app_elem, true));
    
    return true;
  }
  
  protected Element getServiceDescription(String service, String lang, String subset) {
    if (!service.equals(PHIND_SERVICE)) {
      return null;
    }
    Element describe = (Element) applet_description.cloneNode(true);
    describe.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_NAME,  getTextString(PHIND_SERVICE+".name", lang)));
    describe.appendChild(GSXML.createDisplayTextElement(this.doc, GSXML.DISPLAY_TEXT_DESCRIPTION,  getTextString(PHIND_SERVICE+".description", lang)));
    return describe;
  }
  
  protected Element processPhindApplet(Element request) {
    
    Element param_elem = (Element)GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
    HashMap<String, Serializable> params = GSXML.extractParams(param_elem, false);
    
    long first_e = Long.parseLong((String)params.get("pfe"));
    long last_e = Long.parseLong((String)params.get("ple"));
    long first_l = Long.parseLong((String)params.get("pfl"));
    long last_l = Long.parseLong((String)params.get("pll"));
    long first_d = Long.parseLong((String)params.get("pfd"));
    long last_d = Long.parseLong((String)params.get("pld"));
    
    long phrase;
    String phrase_str = (String)params.get("ppnum");
    if (phrase_str == null || phrase_str.equals("")) {
      phrase=0;
    } else {
      phrase = Long.parseLong(phrase_str);
    }
    String word = (String)params.get("pptext");
    String phind_index = (String)params.get("pc");
    // the location of the mgpp database files
    this.basepath = GSFile.phindBaseDir(this.site_home, this.cluster_name, phind_index);
    
    // the result element
    Element result = this.doc.createElement(GSXML.RESPONSE_ELEM);
    result.setAttribute(GSXML.FROM_ATT, PHIND_SERVICE);
    result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);
    
    // applet result info must be in appletInfo element
    Element applet_data = this.doc.createElement(GSXML.APPLET_DATA_ELEM);
    result.appendChild(applet_data);
    Element phind_data = this.doc.createElement("phindData");
    applet_data.appendChild(phind_data);
    
    
    // if we dont know the phrase number, look it up
    if (phrase == 0) {
      if (word==null || word.equals("")) {
        Element error = phindError("no word or phrase");
        phind_data.appendChild(error);
        return result;
      }
      phrase = findPhraseNumberFromWord( word);
    }
    if (phrase==0) {
      // the word is not in the collection
      // return a phind error string
      Element error = phindError("the term "+word+" is not in the collection");
      phind_data.appendChild(error);
      return result;
    }
    
    // get the phrase data into the phind_data node
    getPhraseData(phind_data, phrase, first_l, last_l,
      first_e, last_e,  first_d, last_d);
    return result;
    
    
  }// processPhindApplet
  
  protected long findPhraseNumberFromWord(String word) {
    synchronized (mgpp_search_src) {
    	// set the mgpp index data - we are looking up pword
    	mgpp_search_src.loadIndexData(this.basepath+File.separatorChar+"pword");
    	
    	mgpp_search_src.runQuery(word);
    	
    	MGPPQueryResult res = mgpp_search_src.getQueryResult();
    	Vector docs = res.getDocs();
    	if (docs.size()==0) {
    		// phrase not found
    		return 0;
    	}
    	MGPPDocInfo doc = (MGPPDocInfo)docs.firstElement();
    	return doc.num_;
    }
  }
  
  protected boolean getPhraseData(Element phind_data,
    long phrase, long first_l, long last_l,
    long first_e, long last_e, long first_d,
    long last_d) {
    
	  synchronized (mgpp_retrieve_src) {
    String record = this.mgpp_retrieve_src.getDocument(this.basepath+File.separatorChar+"pdata", "Document",
      phrase);
    if (record.equals("")) {
      Element error = phindError("somethings gone wrong - we haven't got a record for phrase number "+phrase);
      phind_data.appendChild(error);
      return false;
    }
    
    // parse the record - its in gordons cryptic form
    // ":word:tf:ef:df:el:dl:lf:ll"
    // el: e,e,e
    // dl: d;f,d;f,
    // lf and ll may be null
    // l: type,dest, dest; type,dest,dest
    
    // ignore everything up to and including first colon (has
    // <Document>3505: at the start)
    record = record.substring(record.indexOf(':')+1);
    
    // split on ':'
    String [] fields = record.split(":");
    String word = fields[0];
    String tf = fields[1];
    String ef = fields[2];
    String df = fields[3];
    
    
    String expansions = fields[4];
    String documents = fields[5];
    String lf = "0";
    String linklist = "";
    if (fields.length > 7) {// have thesaurus stuff
      lf =fields[6];
      linklist = fields[7];
    }
    
    // the phindData attributes and phrase
    phind_data.setAttribute("id", Long.toString(phrase));
    phind_data.setAttribute("df", df);
    phind_data.setAttribute("ef", ef);
    phind_data.setAttribute("lf", lf);
    phind_data.setAttribute("tf", tf);
    GSXML.createTextElement(this.doc, "phrase", word);
    
    addExpansionList(phind_data, expansions, word, ef, first_e, last_e);
    addDocumentList(phind_data, documents, word, df, first_d, last_d);
    if (!lf.equals("0")) {
      addThesaurusList(phind_data, linklist, word, lf, first_l, last_l);
    }
    return true;
	  }
  }
  
  protected boolean addExpansionList( Element phind_data, String record,
    String word,
    String freq,
    long first, long last) {
    
    Element expansion_list = this.doc.createElement("expansionList");
    phind_data.appendChild(expansion_list);
    expansion_list.setAttribute("length", freq);
    expansion_list.setAttribute("start", Long.toString(first));
    expansion_list.setAttribute("end", Long.toString(last));
    
    // get the list of strings
    String [] expansions = record.split(",");
    int length = expansions.length;
    if (length < last) last = length;
    for (long i = first; i < last; i++) {
      long num  = Long.parseLong(expansions[(int)i]);
      Element expansion = getExpansion( num, word);
      expansion.setAttribute("num", Long.toString(i));
      expansion_list.appendChild(expansion);
    }
    return true;
  }
  
  protected Element getExpansion(long phrase_num,
    String orig_phrase) {
    
    // look up the phrase in the pdata thingy
    String record = this.mgpp_retrieve_src.getDocument(this.basepath+File.separatorChar+"pdata", "Document",
      phrase_num);
    
    if (record ==null || record.equals("")) return null;
    
    // ignore everything up to and including first colon
    record = record.substring(record.indexOf(':')+1);
    
    String [] fields = record.split(":");
    String phrase = fields[0];
    String tf = fields[1];
    //String ef = fields[2]; dont use this
    String df = fields[3];
    
    Element expansion = this.doc.createElement("expansion");
    expansion.setAttribute("tf", tf);
    expansion.setAttribute("df", df);
    expansion.setAttribute("id", Long.toString(phrase_num));
    
    // get teh suffix and prefix
    String [] ends = splitPhraseOnWord(phrase, orig_phrase);
    if (!ends[0].equals("")) {
      expansion.appendChild(GSXML.createTextElement(this.doc, "prefix", ends[0]));
    }
    if (!ends[1].equals("")) {
      expansion.appendChild(GSXML.createTextElement(this.doc, "suffix", ends[1]));
    }
    
    return expansion;
    
  }
  
  protected boolean addDocumentList(Element phind_data, String record,
    String word,
    String freq,
    long first, long last) {
    
    Element document_list = this.doc.createElement("documentList");
    phind_data.appendChild(document_list);
    document_list.setAttribute("length", freq);
    document_list.setAttribute("start", Long.toString(first));
    document_list.setAttribute("end", Long.toString(last));
    
    // get the list of doc,freq
    String [] doc_freqs = record.split(";");
    int length = doc_freqs.length;
    if (length<last) last=length;
    
    for (long i = first; i < last; i++) {
      String doc_elem = doc_freqs[(int)i];
      int p = doc_elem.indexOf(',');
      long doc_num;
      String doc_freq;
      if (p == -1) { // there is no freq in the record
        doc_num =Long.parseLong(doc_elem);
        doc_freq = "1";
      } else {
        doc_num = Long.parseLong(doc_elem.substring(0,p));
        doc_freq = doc_elem.substring(p+1);
      }
      Element document = getDocument( doc_num);
      document.setAttribute("freq", doc_freq);
      document.setAttribute("num", Long.toString(i));
      document_list.appendChild(document);
    }
    
    
    return true;
  }
  
  
  protected Element getDocument(long doc_num) {
    
    // look up the phrase in the docs thingy
    String record = this.mgpp_retrieve_src.getDocument(this.basepath+File.separatorChar+"docs", "Document",
      doc_num);
    
    if (record ==null || record.equals("")) return null;
    
    // ignore everything up to and including first \t
    record = record.substring(record.indexOf('\t')+1);
    
    String [] fields = record.split("\t");
    String hash = fields[0];
    String title = fields[1];
    
    Element d = this.doc.createElement("document");
    d.setAttribute("hash", hash);
    d.appendChild(GSXML.createTextElement(this.doc, "title", title));
    
    return d;
    
  }
  protected boolean addThesaurusList(Element phind_data, String record,
    String word,
    String freq,
    long first, long last) {
    
    
    Element thesaurus_list = this.doc.createElement("thesaurusList");
    phind_data.appendChild(thesaurus_list);
    thesaurus_list.setAttribute("length", freq);
    thesaurus_list.setAttribute("start", Long.toString(first));
    thesaurus_list.setAttribute("end", Long.toString(last));
    
    // get the list of type,dest,dest
    String [] links = record.split(";");
    int length = links.length;
    long index = 0;
    for (int i = 0; i < length; i++) { // go through the entries
      String link_info = links[(int)i];
      String [] items = link_info.split(",");
      // the first entry is teh type
      String type = items[0];
      for (int j = 1; j<items.length; j++, index++) {
        if (index >= first && index < last) { // only output the ones we want
          long phrase = Long.parseLong(items[j]);
          Element t = getThesaurus(phrase);
          t.setAttribute("type", type);
          thesaurus_list.appendChild(t);
        }
      }
    }
    
    return true;
  }
  
  protected Element getThesaurus(long phrase_num) {
    
    // look up the phrase in the pdata thingy
    String record = this.mgpp_retrieve_src.getDocument(this.basepath+File.separatorChar+"pdata", "Document",
      phrase_num);
    
    if (record ==null || record.equals("")) return null;
    
    // ignore everything up to and including first colon
    record = record.substring(record.indexOf(':')+1);
    
    String [] fields = record.split(":");
    String phrase = fields[0];
    String tf = fields[1];
    //String ef = fields[2]; dont use this
    String df = fields[3];
    
    Element thesaurus = this.doc.createElement("thesaurus");
    thesaurus.setAttribute("tf", tf);
    thesaurus.setAttribute("df", df);
    thesaurus.setAttribute("id", Long.toString(phrase_num));
    thesaurus.appendChild(GSXML.createTextElement(this.doc, "phrase", phrase));
    return thesaurus;
    
  }
  
  /** returns an array of two elements - the prefix and the suffix*/
  protected String [] splitPhraseOnWord(String phrase, String word) {
    
    if (word.equals("")) {
      
      String [] res =  {phrase, ""};
      return res;
    }
    // use 2 so that we only split on the first occurrance. trailing empty strings should be included
    String [] result = phrase.split(word, 2);
    return result;
    
  }
  
  protected Element phindError(String message) {
    Element e = this.doc.createElement("phindError");
    Text t = this.doc.createTextNode(message);
    e.appendChild(t);
    return e;
  }
  
}

