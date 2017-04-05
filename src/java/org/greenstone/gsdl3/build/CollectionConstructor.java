package org.greenstone.gsdl3.build;

import java.io.File;

import org.greenstone.gsdl3.util.*;
//import java.util.Thread
import javax.swing.event.EventListenerList;
import org.w3c.dom.Element;

/** base class for collection construction */
public abstract class CollectionConstructor extends Thread
{

	/** the site in which building is to take place */
	protected String site_home = null;
	/** the name of the site */
	protected String site_name = null;
	/** the name of the collection */
	protected String collection_name = null;
	/** the stage of construction */
	protected int process_type = -1;
	/** other arguments/parameters for the construction process - in a paramList */
	protected Element process_params = null;
	/** the list of listeners for the process */
	protected EventListenerList listeners = null;
	/** A flag used to determine if this process has been asked to cancel. */
	protected boolean cancel = false;
	/** Stores the name of the manifest file (if one is needed) */
	protected String manifest_file = null;
    /** The URL params constructed as a query string, representing the CGI QUERY_STRING to the process */
    protected String query_string = null;

	public CollectionConstructor(String name)
	{
		super(name);
		this.listeners = new EventListenerList();
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
		this.listeners.add(ConstructionListener.class, listener);
		return true;
	}

	public boolean removeListener(ConstructionListener listener)
	{
		this.listeners.remove(ConstructionListener.class, listener);
		return true;
	}

	protected void sendProcessBegun(ConstructionEvent evt)
	{
		Object[] concerned = this.listeners.getListenerList();
		for (int i = 0; i < concerned.length; i += 2)
		{
			if (concerned[i] == ConstructionListener.class)
			{
				((ConstructionListener) concerned[i + 1]).processBegun(evt);
			}
		}
	}

	protected void sendProcessComplete(ConstructionEvent evt)
	{
		Object[] concerned = this.listeners.getListenerList();
		for (int i = 0; i < concerned.length; i += 2)
		{
			if (concerned[i] == ConstructionListener.class)
			{
				((ConstructionListener) concerned[i + 1]).processComplete(evt);
			}
		}
	}

	protected void sendProcessStatus(ConstructionEvent evt)
	{

		Object[] concerned = this.listeners.getListenerList();
		for (int i = 0; i < concerned.length; i += 2)
		{
			if (concerned[i] == ConstructionListener.class)
			{
				((ConstructionListener) concerned[i + 1]).processStatus(evt);
			}
		}
	}

	protected void sendMessage(ConstructionEvent evt)
	{

		Object[] concerned = this.listeners.getListenerList();
		for (int i = 0; i < concerned.length; i += 2)
		{
			if (concerned[i] == ConstructionListener.class)
			{
				((ConstructionListener) concerned[i + 1]).message(evt);
			}
		}
	}

	abstract public void run();
}
