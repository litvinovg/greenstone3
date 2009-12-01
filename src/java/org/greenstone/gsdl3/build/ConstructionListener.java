package org.greenstone.gsdl3.build;

import java.util.EventListener;

public interface ConstructionListener 
    extends EventListener {

    /** This event handler used to signify that a task has been started */
    public void processBegun(ConstructionEvent evt);
    /** This event handler used to signify that a task has been completed */
    public void processComplete(ConstructionEvent evt);
    /** This event handler used to send status updates as the task is progressing */
    public void processStatus(ConstructionEvent evt);
    /**  This event handler used to send any other messages to the listeners */
    public void message(ConstructionEvent evt);
}
