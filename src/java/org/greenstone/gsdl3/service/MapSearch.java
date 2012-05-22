/*
 *    MapSearch.java
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
import org.greenstone.gsdl3.util.*;

// XML classes
import org.w3c.dom.Element; 
import org.w3c.dom.Document;
import org.w3c.dom.NodeList; 

// General java classes
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.ClassNotFoundException;

import org.apache.log4j.*;

/**
 *
 * 
 */
public class MapSearch 
    extends AbstractTextSearch {


    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.service.MapSearch.class.getName());

    protected String files_home_dir = null;

    protected static final String FREQ_ATT = "freq";

    // use the following if we add back in index searching
    //protected String default_index = null;
    //protected static final String DEFAULT_INDEX_ELEM = "defaultIndex";
    //protected static final String INDEX_ELEM = "index";  
    
    /** Hashtable containing the place names database, indexed on the place name in lower case. */
    public static Hashtable hashNames;

    /** Constructor */
    public MapSearch() {}

    /** */
    public boolean configure(Element info, Element extra_info)
    {
	if (!super.configure(info, extra_info)) {
	    return false;
	}
	logger.info("Configuring MapSearch...");

	// set the files_home_dir variable for this collection
	this.files_home_dir = GSFile.collectionIndexDir(site_home, cluster_name)+File.separator+"assoc"+File.separator;

	//load the hashtable
	hashNames = new Hashtable();
	try{
	    ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.files_home_dir+"hashnames.dat"));
	    hashNames = (Hashtable)in.readObject();
	    in.close();
	}catch(IOException ioe){
	    ioe.printStackTrace();
	    return false;
	}catch(ClassNotFoundException cnf){
	    cnf.printStackTrace();
	    return false;
	}
	 
	// just in case we get the index stuff going, here is the code
	
	// get the display stuff for the indexes
	
// 	NodeList indexes = info.getElementsByTagName(GSXML.INDEX_ELEM);
// 	if (extra_info != null) {
// 	    Document owner = info.getOwnerDocument();
// 	    Element config_search = (Element)GSXML.getChildByTagName(extra_info, GSXML.SEARCH_ELEM);
// 	    if (config_search != null) {
// 		for (int i=0; i<indexes.getLength();i++) {
// 		    Element ind = (Element)indexes.item(i);
// 		    String name = ind.getAttribute(GSXML.NAME_ATT);
// 		    Element node_extra = GSXML.getNamedElement(config_search,
// 							       GSXML.INDEX_ELEM,
// 							       GSXML.NAME_ATT,
// 							       name);
// 		    if (node_extra == null) {
// 			// no index specification for this index
// 			continue;
// 		    }
		    
// 		    // get the display elements if any - displayName
// 		    NodeList display_names = node_extra.getElementsByTagName(GSXML.DISPLAY_TEXT_ELEM);
// 		    if (display_names !=null) {
// 			for (int j=0; j<display_names.getLength(); j++) {
// 			    Element e = (Element)display_names.item(j);
// 			    ind.appendChild(owner.importNode(e, true));
// 			}
// 		    }
// 		} // for each index
// 	    }
// 	}
// 	// Get the default index out of <defaultIndex> (buildConfig.xml)
// 	Element def = (Element) GSXML.getChildByTagName(info, DEFAULT_INDEX_ELEM);
// 	if (def != null) {
// 	    default_index = def.getAttribute(GSXML.NAME_ATT);
// 	}
// 	if (default_index == null || default_index.equals("")) {
// 	    // use the first one from the index list
// 	    default_index = ((Element)indexes.item(0)).getAttribute(GSXML.NAME_ATT);
// 	}

	return true;
    }

    protected void getIndexData(ArrayList<String> index_ids, ArrayList<String> index_names, String lang) 
    {
	// for now, we just have one dummy index
	index_ids.add("idx");
	index_names.add("maps"); // get from properties

	// use the following if we ever get the index search working
// 	Element index_list = (Element)GSXML.getChildByTagName(this.config_info, INDEX_ELEM+GSXML.LIST_MODIFIER);
// 	NodeList indexes = index_list.getElementsByTagName(INDEX_ELEM);
// 	for (int i=0; i<indexes.getLength(); i++) {
// 	    Element index = (Element)indexes.item(i);
// 	    index_ids.add( index.getAttribute(GSXML.NAME_ATT));
// 	    index_names.add(GSXML.getDisplayText(index, GSXML.DISPLAY_TEXT_NAME, lang, "en"));
		
// 	}
    }
    

    /** Process a map query */
    protected Element processTextQuery(Element request) 
    {
	// Create a new (empty) result message
	Element result = doc.createElement(GSXML.RESPONSE_ELEM);
	result.setAttribute(GSXML.FROM_ATT, QUERY_SERVICE);
	result.setAttribute(GSXML.TYPE_ATT, GSXML.REQUEST_TYPE_PROCESS);

	// Get the parameters of the request
	Element param_list = (Element) GSXML.getChildByTagName(request, GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	if (param_list == null) {
	    logger.error("TextQuery request had no paramList.");
	    return result;  // Return the empty result
	}

	// Process the request parameters
	HashMap<String, Serializable> params = GSXML.extractParams(param_list, false);

	// Make sure a query has been specified
	String searchTerm = (String) params.get(QUERY_PARAM);
	if (searchTerm == null || searchTerm.equals("")) {
	    return result;  // Return the empty result
	}

	// If an index hasn't been specified, use the default
// 	String index = (String) params.get(INDEX_PARAM); 
// 	if (index == null) {
// 	    index = default_index;
// 	}

	//convert the query string from the form '&quotTe Awamutu&quot Hamilton' to 'Te Awamutu+Hamilton'

	//replace ' ' with '+'
	searchTerm = searchTerm.replace(' ','+');

	//replace '&quot' with '"'
	int place = -1;
	while(searchTerm.indexOf("&quot;",place+1) != -1){
	    place = searchTerm.indexOf("&quot;",place+1);
	    searchTerm = (searchTerm.substring(0,place) + "\"" + searchTerm.substring(place+6,searchTerm.length()));
	}

	//replace spaces in string enclosed in quotes
	place = -1;
	while(searchTerm.indexOf('"',place+1) != -1){
	    place = searchTerm.indexOf('"',place+1);
	    if(searchTerm.indexOf('"',place+1) != -1)
		searchTerm = (searchTerm.substring(0,place) + searchTerm.substring(place, searchTerm.indexOf('"',place+1)).replace('+',' ') + searchTerm.substring(searchTerm.indexOf('"',place+1), searchTerm.length()));
	    place = searchTerm.indexOf('"',place);
	}

	//remove speech marks
	place = 0;
	while(place != -1){
	    place = searchTerm.indexOf('"', place);
	    if(place != -1){
		searchTerm = searchTerm.substring(0, place) + searchTerm.substring(place+1, searchTerm.length());
		place=0;
	    }
	}

	//find the number of search terms (number of '+' plus 1)
	int words = 1;
	place = 0;
	while (place != -1){
	    place = searchTerm.indexOf('+', place+1);
	    if(place != -1) words++;
	}
	place = 0;

	//store each search term in a string array
	String terms[] = new String[words];
	String terms_freq[] = new String[words];
	for (int i=0; i<(words-1); i++){
	    terms[i]=searchTerm.substring(place, searchTerm.indexOf('+', place));
	    place = searchTerm.indexOf('+', place)+1;
	}
	terms[words-1] = searchTerm.substring(place, searchTerm.length());

	Object nameArray[] = new Object[1];
	int nameArraySize;
	String nameData;
	double xco, yco;
	LinkedList<Object> mapList = new LinkedList<Object>();
	LinkedList placeList = new LinkedList();
	String readString = "";

	//capitalise the search terms
	for(int i=0;i<words;i++)
	    terms[i] = terms[i].toUpperCase();

	for(int k=0; k<words; k++){
	    nameArraySize=0;


	    if((nameArray = (Object[])hashNames.get(terms[k].toLowerCase())) != null){
		logger.debug(hashNames.get(terms[k].toLowerCase()));
		
		nameArraySize = Arrays.asList(nameArray).size();
	    }
	    
	    
	    int h;
	    for(h=0;h<nameArraySize;h++){

		nameData = (String)nameArray[h];

		// get the co-ordinates of the current place name
		xco = Double.parseDouble(nameData.substring(nameData.lastIndexOf('`')+1,nameData.length()));
		yco = Double.parseDouble(nameData.substring(nameData.lastIndexOf('`',nameData.lastIndexOf('`')-1)+2,nameData.lastIndexOf('`')));

		//open the text file containing map metadata (first line is a description of the file)
		try{
		    BufferedReader inmap = new BufferedReader(new FileReader(this.files_home_dir + "files"+File.separator+"maps"+File.separator+"mapdata.txt"));
		    inmap.readLine();

		    // for each map
		    while(inmap.ready()){
			readString = inmap.readLine();
			int indexer = 0;
			indexer = readString.indexOf('`')+2;
			// get the co-ordinates of the top left and bottom right corners of the map.
			double ytop = Double.parseDouble(readString.substring(indexer,indexer+5));
			indexer = readString.indexOf('`',indexer)+1;
			double xtop = Double.parseDouble(readString.substring(indexer,indexer+6));
			indexer = readString.indexOf('`',indexer)+2;
			double ybot = Double.parseDouble(readString.substring(indexer,indexer+5));
			indexer = readString.indexOf('`',indexer)+1;
			double xbot = Double.parseDouble(readString.substring(indexer,indexer+6));

			//if the place is within the map, put the map metadata and the placename metadata in linked list
			if(xco >= xtop && xco < xbot && yco >= ytop && yco < ybot)
			    mapList.add(readString+"```"+nameData);
		    }
		}catch(Exception e){e.printStackTrace();}
	    }
	}

	// use an array object to sort the linked list by map number
	Object[] mapListArray = mapList.toArray();
	Arrays.sort(mapListArray);
	mapList.clear();
	for(int mla=0; mla<Array.getLength(mapListArray); mla++)
	    mapList.addFirst(mapListArray[mla]);

	//for each map, create a list of the query matches on that map
	LinkedList<Object> tempList = new LinkedList<Object>();
	String mapNumber = "";
	String currentMap[] = {"",""};
	int mapFreq = 0;
	while(mapList.size()>0){
	    readString = (String)mapList.removeFirst();
	    if(mapNumber.equals(readString.substring(0,readString.indexOf('.')))){
		currentMap[1] = currentMap[1]+readString.substring(readString.indexOf("```"),readString.length());
		mapFreq++;
	    }
	    else{
		if(!currentMap[0].equals(""))
		    tempList.add(currentMap[0]+"`"+mapFreq+currentMap[1]);
		currentMap[0] = readString.substring(0,readString.indexOf("```"));
		currentMap[1] = readString.substring(readString.indexOf("```"),readString.length());
		mapNumber = readString.substring(0,readString.indexOf('.'));
		mapFreq=1;
	    }
	}
	if(!currentMap[0].equals(""))
	    tempList.add(currentMap[0]+"`"+mapFreq+currentMap[1]);

	int totalDocs = tempList.size();

	// use an array object to sort the linked list by number of matches on each map
	Object[] tempListArray = tempList.toArray();
	Arrays.sort(tempListArray, new MapResultSorter());
	tempList.clear();
	for(int tla=0; tla<Array.getLength(tempListArray); tla++)
	    tempList.add(tempListArray[tla]);

	// Create a metadata list to store information about the query results
	Element metadata_list = doc.createElement(GSXML.METADATA_ELEM+GSXML.LIST_MODIFIER); 
	result.appendChild(metadata_list);
			
	// Add a metadata element specifying the number of matching documents
	GSXML.addMetadata(this.doc, metadata_list, "numDocsMatched", "" + totalDocs);
	GSXML.addMetadata(this.doc, metadata_list, "numDocsReturned", ""+totalDocs);
       	// Create a document list to store the matching documents, and add them
	Element document_list = doc.createElement(GSXML.DOC_NODE_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(document_list);
	for (int d = 0; d < totalDocs; d++) {
	    String doc_id = (String)tempList.get(d);

	    Element doc_node = doc.createElement(GSXML.DOC_NODE_ELEM);
	    doc_node.setAttribute(GSXML.NODE_ID_ATT, doc_id);
	    doc_node.setAttribute(GSXML.NODE_TYPE_ATT, "thumbnail");
	    doc_node.setAttribute(GSXML.DOC_TYPE_ATT, "map");
	    document_list.appendChild(doc_node);
	}
			
	// Create a term list to store the term information, and add it
	Element term_list = doc.createElement(GSXML.TERM_ELEM+GSXML.LIST_MODIFIER);
	result.appendChild(term_list);	
	

	for (int t=0; t<words; t++){
	    String term = terms[t];
	
	    Element term_elem = doc.createElement(GSXML.TERM_ELEM);
  	    term_elem.setAttribute(GSXML.NAME_ATT, term);
  	    term_elem.setAttribute(FREQ_ATT, "" + terms_freq[t]);

	    term_list.appendChild(term_elem);
	}
	return result;
    }
 

    public class MapResultSorter implements Comparator {
	
	public int compare(Object o1, Object o2) {
	    String first = (String)o1;
	    String second = (String) o2;
	    int firstInt;
	    int secondInt;
	    first = first.substring(first.lastIndexOf('`',first.indexOf("```")-1)+1,first.indexOf("```"));
	    second = second.substring(second.lastIndexOf('`',second.indexOf("```")-1)+1,second.indexOf("```"));
	    firstInt = Integer.parseInt(first);
	    secondInt = Integer.parseInt(second);
	    if(firstInt > secondInt)
		return -1;
	    else if(firstInt == secondInt)
		return 0;
	    else
		return 1;
	}
    }
}
