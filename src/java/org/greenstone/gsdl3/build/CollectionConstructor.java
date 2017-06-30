package org.greenstone.gsdl3.build;

import java.io.File;

import org.greenstone.gsdl3.util.*;
//import java.util.Thread
import java.util.concurrent.CopyOnWriteArrayList;
import org.w3c.dom.Element;

/** base class for collection construction */
public abstract class CollectionConstructor extends Thread
{

	/** the site in which building is to take place */
	protected String site_home = null;
	/** the name of the site */
	protected String site_name = null;
  /** the library (servlet) name of the site */
  protected String library_name = null;
	/** the name of the collection */
	protected String collection_name = null;
	/** the stage of construction */
	protected int process_type = -1;
	/** other arguments/parameters for the construction process - in a paramList */
	protected Element process_params = null;
	/** the list of listeners for the process. We need it to be threadsafe. 
	 * see http://stackoverflow.com/questions/8259479/should-i-synchronize-listener-notifications-or-not
	 * https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/CopyOnWriteArrayList.html
	 * "A thread-safe variant of ArrayList in which all mutative operations (add, set, and so on) are
	 * implemented by making a fresh copy of the underlying array.
	 * This is ordinarily too costly, but may be more efficient than alternatives when traversal operations
	 * vastly outnumber mutations, and is useful when you cannot or don't want to synchronize traversals,
	 * yet need to preclude interference among concurrent threads."
	 */
	protected final CopyOnWriteArrayList<ConstructionListener> listeners;
	/** A flag used to determine if this process has been asked to cancel. */
	protected boolean cancel = false; // Not really used (in any way that works)
	/** Stores the name of the manifest file (if one is needed) */
	protected String manifest_file = null;
	/** The URL params constructed as a query string, representing the CGI QUERY_STRING to the process */
	protected String query_string = null;

	public CollectionConstructor(String name)
	{
		super(name);
		this.listeners = new CopyOnWriteArrayList<ConstructionListener>();
	}

	/**
	 * carry out any set up stuff - returns false if couldn't set up properly
	 */
	public boolean configure()
	{
		return true;
	}

    // this method never gets called. And, the way subclass GS2PerlConstructor.runPerlCommand() was originally
    // coded, setting cancel to true never had any effect anyway in stopping any perl command that was run.
	public void stopAction()
	{
		this.cancel = true;
	}

	public void setActionType(int type)
	{
		this.process_type = type;
	}

	public void setSiteHome(String site_home)
	{
		this.site_home = site_home;

		File siteHomeFile = new File(site_home);
		this.site_name = siteHomeFile.getName();
	}

  public void setLibraryName(String library_name) {
    this.library_name = library_name;
  }
	public void setCollectionName(String coll_name)
	{
		this.collection_name = coll_name;
	}

    	public void setQueryString(String querystring)
	{
		this.query_string = querystring;
	}

	public void setProcessParams(Element params)
	{
		this.process_params = params;
	}
	
	public void setManifestFile(String manifestFile)
	{
		this.manifest_file = manifestFile;
	}

	public boolean addListener(ConstructionListener listener)
	{
	    this.listeners.add(listener);
	    return true;
	}

    // We never call removeListener. If we do start calling removeListener() change the listeners list
    // over to type ConcurrentLinkedQueue, for reasons explained at
    // http://stackoverflow.com/questions/8259479/should-i-synchronize-listener-notifications-or-not
    // The current listeners list type is CopyOnWriteArrayList, which provides thread safety. But it
    // can still send off events to listeners just as they're being unregistered, and that could be a
    // problem if we were specifically removing the listener because we wanted to cease it from
    // listening and responding to subsequent events.
	public boolean removeListener(ConstructionListener listener)
	{	    
	    this.listeners.remove(listener);
	    return true;
	}

	protected void sendProcessBegun(ConstructionEvent evt)
	{
	    // See http://stackoverflow.com/questions/8259479/should-i-synchronize-listener-notifications-or-not
	    for (ConstructionListener l: this.listeners) {
		l.processBegun(evt);
	    }
	}

	protected void sendProcessComplete(ConstructionEvent evt)
	{
	    for (ConstructionListener l: this.listeners) {
		l.processComplete(evt);
	    }
	}

    // Method doesn't need to be synchronized any more, since it uses the ThreadSafe CopyOnWriteArrayList
    // for listeners list.
    // See http://stackoverflow.com/questions/8259479/should-i-synchronize-listener-notifications-or-not
    // See http://stackoverflow.com/questions/574240/is-there-an-advantage-to-use-a-synchronized-method-instead-of-a-synchronized-blo
	protected void sendProcessStatus(ConstructionEvent evt)
	{
	    for (ConstructionListener l: this.listeners) {
		l.processStatus(evt);
	    }
	}

	protected void sendMessage(ConstructionEvent evt)
	{
	    for (ConstructionListener l: this.listeners) {
		l.message(evt);
	    }
	}

	abstract public void run();
}
