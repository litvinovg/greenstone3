package org.greenstone.gsdl3.build;

import org.greenstone.gsdl3.util.*;
//import java.util.Thread
import javax.swing.event.EventListenerList;
import org.w3c.dom.Element;

/** base class for collection construction */
public abstract class CollectionConstructor 
    extends Thread {

    /** the site in which building is to take place */
    protected String site_home = null;
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
    
    public CollectionConstructor(String name) {
	super(name);
	this.listeners = new EventListenerList();
    }
	
    /** carry out any set up stuff - returns false if couldn't set up properly
     */
    public boolean configure() {
	return true;
    }
    public void stopAction() {
	this.cancel = true;
    }
    
    public void setActionType(int type) {
	this.process_type = type;
    }
    public void setSiteHome(String site_home) {
	this.site_home = site_home;
    }
    public void setCollectionName(String coll_name) {
	this.collection_name = coll_name;
    }
    public void setProcessParams(Element params) {
	this.process_params = params;
    }
    public boolean addListener(ConstructionListener listener) {
	this.listeners.add(ConstructionListener.class, listener);
	return true;
    }
    public boolean removeListener(ConstructionListener listener) {
	this.listeners.remove(ConstructionListener.class, listener);
	return true;
    }

    protected void sendProcessBegun(ConstructionEvent evt) {
	Object[] concerned = this.listeners.getListenerList();
	for(int i = 0; i < concerned.length ; i+=2) {
	    if(concerned[i] == ConstructionListener.class) {
		((ConstructionListener)concerned[i+1]).processBegun(evt);
	    }
	}
    }
    protected void sendProcessComplete(ConstructionEvent evt) {
	Object[] concerned = this.listeners.getListenerList();
	for(int i = 0; i < concerned.length ; i+=2) {
	    if(concerned[i] == ConstructionListener.class) {
		((ConstructionListener)concerned[i+1]).processComplete(evt);
	    }
	}
    }
    
    protected void sendProcessStatus(ConstructionEvent evt) {

	Object[] concerned = this.listeners.getListenerList();
	for(int i = 0; i < concerned.length ; i+=2) {
	    if(concerned[i] == ConstructionListener.class) {
		((ConstructionListener)concerned[i+1]).processStatus(evt);
	    }
	}
    }
    protected void sendMessage(ConstructionEvent evt) {

	Object[] concerned = this.listeners.getListenerList();
	for(int i = 0; i < concerned.length ; i+=2) {
	    if(concerned[i] == ConstructionListener.class) {
		((ConstructionListener)concerned[i+1]).message(evt);
	    }
	}
    }
    
    abstract public void run();
}
