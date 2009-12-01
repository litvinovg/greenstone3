package org.greenstone.gsdl3.build;
import org.greenstone.gsdl3.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.io.File;

/** the class containing the main program for collection building - is essentially a wrapper around a CollectionConstructor
 * @see CollectionConstructor
 */
public class ConstructCollection 
    implements ConstructionListener {
    
    static public void main(String [] args) {

	String site_home = null;
	String coll_name = null;
	int process_mode = -1;
	if (args.length < 4) {
	    printUsage();
	    return;
	}

	// parse the args

	XMLConverter converter = new XMLConverter();
	Document doc = converter.newDOM();
	Element option_list = doc.createElement(GSXML.PARAM_ELEM+GSXML.LIST_MODIFIER);
	Element option;

	for (int i=0; i<args.length-1; i++) {
	    String key = args[i];
	    if (!key.startsWith("-")) {
		continue; // ignore options that dont start with '-'
	    }
	    key = key.substring(1);
	    String val = args[i+1];
	    if (val.startsWith("-")) {
		val=null;
	    } else {
		i++;
	    }
	    
	    if (key.equals("site")) {
		site_home = val;
	    } else if (key.equals("mode")) {
		if (val==null) {
		    continue;
		}
		if (val.equals("new")) {
		    process_mode = GS2PerlConstructor.NEW;
		} else if (val.equals("import")) {
		    process_mode = GS2PerlConstructor.IMPORT;
		} else if (val.equals("build")) {
		    process_mode = GS2PerlConstructor.BUILD;
		} else if (val.equals("activate")) {
		    process_mode = GS2PerlConstructor.ACTIVATE;
		}

	    } else { // an option to pass to the builder
 		option = doc.createElement(GSXML.PARAM_ELEM);
		option.setAttribute(GSXML.NAME_ATT, key);
		if (val!=null) {
		    option.setAttribute(GSXML.VALUE_ATT, val);
		}
		option_list.appendChild(option);
	    }
	}
	String last_arg = args[args.length-1];
	if (last_arg.startsWith("-")) { // should be coll name
	    System.out.println("ERROR: the last arg should be the collection name!");
	    printUsage();
	    return;
	}
	
	coll_name = last_arg;
	
	// check that we have the required fields
	if (site_home==null || process_mode == -1 || coll_name==null) {
	    System.out.println("ERROR: you have not specified all the necessary args!");
	    printUsage();
	    return;
	}

	// check that the collection name is valid - ie the directory exists if this is not a new one, or doesn't exist if we are creating a new one
	File coll_dir = new File(GSFile.collectionBaseDir(site_home, coll_name));
	if (process_mode != GS2PerlConstructor.NEW && !coll_dir.exists()) {
	    System.out.println("ERROR: Invalid collection ("+coll_name+").");
	    printUsage();
	    return;
	} else if (process_mode == GS2PerlConstructor.NEW && 
		   coll_dir.exists()) {
	    System.out.println("ERROR: there is already a collection named "+coll_name);
	    printUsage();
	    return;
	}
	
   
	ConstructCollection processor = new ConstructCollection();
	GS2PerlConstructor constructor = new GS2PerlConstructor("perl_build");
	if (!constructor.configure()) {
	    System.out.println("couldn't configure the constructor!!");
	    return;
	}
	constructor.setCollectionName(coll_name);
	constructor.setSiteHome(site_home);
	constructor.setProcessParams(option_list);
	constructor.setActionType(process_mode);
	constructor.addListener(processor);
	constructor.start();
    }
    
    static protected void printUsage() {
	System.out.println("Usage: the following arguments need to be supplied:\n -site <site-home> -mode new|import|build|activate [options] <coll-name>");
	System.out.println("Options available are:");
	System.out.println("-maxdocs N\t process at most N documents\n"+
			   "-gs2\t use greenstone 2 style building");
    }

    /** This event handler used to signify that a task has been started */
    public void processBegun(ConstructionEvent evt) {
	System.out.println("begun: "+evt.getMessage());
    }
    /** This event handler used to signify that a task has been completed */
    public void processComplete(ConstructionEvent evt){
	System.out.println("complete: "+evt.getMessage());
    }
    /** This event handler used to send status updates as the task is progressing */
    public void processStatus(ConstructionEvent evt){
	System.out.println(evt.getMessage());
    }
    /**  This event handler used to send any other messages to the listeners */
    public void message(ConstructionEvent evt){
	System.out.println(evt.getMessage());
    }




}
