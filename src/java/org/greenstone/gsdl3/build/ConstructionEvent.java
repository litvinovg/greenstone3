package org.greenstone.gsdl3.build;

import java.awt.AWTEvent;     

public class ConstructionEvent 
    extends AWTEvent {
    
    /** the event id for these events - use a random number thats
	not reserved*/
    private static final int EVENT_ID = RESERVED_ID_MAX+77;
    /** The status associated with the event. */
    private final int status;
    /** Any message associated with this event. */
    private final String message;
    /* Constructor.
     * @param source The <strong>CollectionConstructor</strong> that fired this message.
     * @param status The status code for this event          
     * @param message A <strong>String</strong> representing any message attatched with this event.
     */
    public ConstructionEvent(Object source, int status, String message) {
	super(source, EVENT_ID);
	this.message = message;
	this.status = status;
    }
    /** Gets the message associated with this event.
     * @return The message as a <strong>String</strong> or <i>null</i>. 
     */
    public String getMessage() {
	return this.message;
    }
    /** Gets the status associated with this event. This status can then be matched back to the constants in <strong>GShell</strong>.
     * @return An <strong>int</strong> signifying the process status.
     */
    public int getStatus() {
	return this.status;
    }
    /** returns a String representation of the event */
    public String toString() {
	return "org.greenstone.gsdl3.build.ConstructionEvent[" + this.message
+ "," + this.status + "]";
    }
}
