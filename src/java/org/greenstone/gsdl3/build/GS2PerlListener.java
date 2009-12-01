package org.greenstone.gsdl3.build;

public class GS2PerlListener 
    implements ConstructionListener {

    /** a buffer holding all the messages */
    protected StringBuffer log=null;
    /** a buffer holding only the latest update */
    protected StringBuffer update = null;

    /** a status code */
    protected int status = -1;
    /** whether the process this is listeneing to is finshed */
    protected boolean finished = false;

    public GS2PerlListener() {
	this.log = new StringBuffer();
	this.update = new StringBuffer();
    }

    public boolean isFinished() {
	return this.finished;
    }
    public String getLog() {
	return this.log.toString();
    }
    public int getStatus() {
	return this.status;
    }
    synchronized public String getUpdate() {
	this.log.append(this.update);
	String tmp = this.update.toString();
	this.update.delete(0, this.update.length());
	return tmp;
    }

    // do we need to synchronize the methods below?

    /** This event handler used to signify that a task has been started */
    synchronized public void processBegun(ConstructionEvent evt) {
	this.status = evt.getStatus();
	this.update.append(evt.getMessage()+"\n");
	
    }
    /** This event handler used to signify that a task has been completed */
    synchronized public void processComplete(ConstructionEvent evt){
	this.status = evt.getStatus();
	this.update.append(evt.getMessage()+"\n");
    }
    /** This event handler used to send status updates as the task is progressing */
    synchronized public void processStatus(ConstructionEvent evt){
	this.status = evt.getStatus();
	this.update.append(evt.getMessage()+"\n");
    }
    /**  This event handler used to send any other messages to the listeners */
    synchronized public void message(ConstructionEvent evt){
	this.status = evt.getStatus();
	this.update.append(evt.getMessage()+"\n");
    }


}
