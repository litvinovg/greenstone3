/*
 *    GS2MGPPSearch.java
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
import org.greenstone.mgpp.*;
import org.greenstone.gsdl3.util.*;
import org.w3c.dom.Element; 

import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.log4j.*;

/**
 *
 * @author Shaoqun Wu
 */

public class GoogleNgramMGPPSearch
  extends GS2MGPPSearch {
   static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.GoogleNgramMGPPSearch.class.getName());
						
  protected String default_max_docs = "-1";
  protected String default_hits_per_page = "30";
    /** constructor */
    public GoogleNgramMGPPSearch(){
	this.does_paging = true;
    }
    
  /** configure this service */
    public boolean configure(Element info, Element extra_info) {
	if (!super.configure(info, extra_info)){
	    return false;
	}

	this.default_max_docs = "-1";
	this.default_hits_per_page = "30";
        this.does_stem = false;
	return true;
    }
  
    // sort the doc_nums by their frequency
    protected String [] getDocIDs(Object query_result) {
	try{
	  Vector docs = ((MGPPQueryResult)query_result).getDocs();
	  //ArrayList docList_past = new ArrayList(); 
          //ArrayList docList_future = new ArrayList();	 
	  //ArrayList docList_present = new ArrayList();
	  
	 ArrayList<DocWrapper> docList = new ArrayList<DocWrapper>();

	  for (int d = 0; d < docs.size(); d++) {
	      String num = Long.toString((((MGPPDocInfo) docs.elementAt(d)).num_));
	      String doc_id = internalNum2OID(num);
	      DBInfo dbInfo = this.gs_doc_db.getInfo(doc_id);
	      String fre = (String)dbInfo.getInfo("Frequency");
	      String tense = (String)dbInfo.getInfo("Tense");
	     
	      if(!fre.equals("")){
		  //  if (tense.equals("past")){
		  //    docList_past.add(new DocWrapper(num,Integer.parseInt(fre),tense));
		  // }
		  // else{
		  //    if (tense.equals("future")){
		  //	  docList_future.add(new DocWrapper(num,Integer.parseInt(fre),tense));
		  //     }
		  //    else{
		  //	  if(tense.equals("present")){
		  //	      docList_present.add(new DocWrapper(num,Integer.parseInt(fre),tense));   
		  //	  }
		  //    }
		  //}
		  docList.add(new DocWrapper(num,Integer.parseInt(fre),tense));
	      }
	      
	  }
	  
	  
	  //Collections.sort(docList_past);
	  //Collections.sort(docList_future);
	  //Collections.sort(docList_present);
	  
	  Collections.sort(docList);
	  int i_pa =  0;
	  int i_f =  0;
	  int i_pre = 0;
	  
	  //String [] doc_nums = new String [docList_past.size()+docList_future.size()+docList_present.size()];
	  String [] doc_nums = new String [docList.size()];
	  int interval = 10;

	  for(int d = 0; d < doc_nums.length; d++){

	      // for(;i_pre < docList_present.size() && interval > 0;i_pre++){
// 		  doc_nums[d] = ((DocWrapper)docList_present.get(i_pre)).num;
// 		  d++;
// 		  interval--;
// 	      }
	      
// 	      interval = 10+interval;
	 
// 	      for(;i_pa < docList_past.size() && interval > 0;i_pa++){
// 		  doc_nums[d] = ((DocWrapper)docList_past.get(i_pa)).num;
// 		  d++;
// 		  interval--;
// 	      }

	     
// 	      interval = 10+interval;

// 	      for(;i_f < docList_future.size() && interval > 0;i_f++){
// 		  doc_nums[d] = ((DocWrapper)docList_future.get(i_f)).num;
// 		  d++;
// 		  interval--;
// 	      }

// 	    interval = 10;

	      doc_nums[d] = docList.get(d).num;
	    
	  }

	  return doc_nums;
    }
    catch(Exception e){
	e.printStackTrace();
    }

    return null;
  }

  static class DocWrapper implements Comparable{
	public int fre = 0;
	public String num = "";
        public String tense = "";


      public DocWrapper(String num, int fre, String tense){
	    this.fre = fre;
	    this.num = num;
	    this.tense = tense;
	}

	public int compareTo(Object o){
	    
	    if (!(o instanceof DocWrapper)) return -1;
	    DocWrapper docIn = (DocWrapper)o;
	    if (num.equals(docIn.num)){
		return 0;
	    }

	    if (fre > docIn.fre) return -1; 
	    return 1;	
	}

	public boolean equals(Object o){
	    if (!(o instanceof DocWrapper)) return false;
	    DocWrapper docIn = (DocWrapper)o;
	    if (num.equals(docIn.num)){
		return true;
	    }
	    return false;
	}


    }
    
      
}


