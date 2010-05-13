package org.greenstone.util;

/** Verbatim copy of what's in GLI's org.greenstone.gatherer.util */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.BindException;

// When needing to use sockets, this class can be used to find an available port
public class PortFinder {

    // Port numbers range from 0 to 65,535. RESERVED PORTS are below 1025
    // see http://www.gnu.org/software/hello/manual/libc/Ports.html
    public static final int MAX_PORT = 65535;
    public static final int PORTS_RESERVED = 1024;

    // Considering a limited range of ports that will be reused (circular buffer)
    public final int PORT_BASE;
    public final int PORT_BLOCK_SIZE;

    // Keep track of what port numbers we have checked for availability
    private int nextFreePort;

    // The socket port number that we will use
    private int port = -1;

    public PortFinder() {
	this( PORTS_RESERVED+1, (MAX_PORT - PORTS_RESERVED) );
    }

    public PortFinder(int base, int blocksize) {
	PORT_BASE = base;
	PORT_BLOCK_SIZE = blocksize;

	nextFreePort = PORT_BASE;
    }

    /** Circular buffer. Modifies the value of nextFreePort (the buffer index). */
    private void incrementNextFreePort() {
	int offset = nextFreePort - PORT_BASE;
	offset = (offset + 1) % PORT_BLOCK_SIZE;
	nextFreePort = PORT_BASE + offset;
    } 

    /** Finds the first available port in the range specified during Constructor call.
     *  @return the number of an available port.	
     */
    public int findPortInRange(boolean verbose) throws Exception {	
	try {
	    boolean foundFreePort = false;
	    for(int i = 0; i < PORT_BLOCK_SIZE; i++) {
		
		if(isPortAvailable(nextFreePort, verbose)) {
		    foundFreePort = true;
		    break;
		    
		} else {
		    incrementNextFreePort();
		}
	    }
	    
	    if(foundFreePort) {
		// Free port number currently found becomes the port number of the socket
		// that will be used
		this.port = nextFreePort;
		incrementNextFreePort();
		
	    } else {
		throw new Exception("Cannot find an available port in the range " 
				    + PORT_BASE + "-" + (PORT_BASE+PORT_BLOCK_SIZE));
	    }
	    
	} catch(IOException e) {
	    System.err.println("Error when trying to find an available port. In PortFinder.findPort() " + e);
	}
	return port;
    }

    /** @return true if the portnumber is in the valid range. */
    public static boolean isValidPortNumber(int portNum) {
	return (portNum >= 0 && portNum <= MAX_PORT);
    }

    /** @return true if the portnumber is in the useable range. */
    public static boolean isAssignablePortNumber(int portNum) {
	return (portNum > PORTS_RESERVED && portNum <= MAX_PORT);
    }

    /** Finds an available port.
     * @return the number of an available port.
     */
    public static int findAnyFreePort() throws Exception {	
	ServerSocket tmpSocket = null;
	int portNumber = -1;
	try {
	    // Creates a server socket, bound to the specified port.
	    // A port of 0 creates a socket on any free port. 
	    tmpSocket = new ServerSocket(0);
	    portNumber = tmpSocket.getLocalPort();
	    tmpSocket.close();
	} catch(Exception e) {
	    System.err.println("Unable to find a free port or close it. Got Exception: " + e);
	    tmpSocket = null;
	}
	return portNumber;
    }

    /** @return true if the portnum is available for use */
    public static boolean isPortAvailable(int portnum, boolean verbose) {
	ServerSocket tmpSocket = null;
	try {
	    tmpSocket = new ServerSocket(portnum);
	    tmpSocket.close();
	    //if(verbose) {
	    //System.err.println("Port " + portnum + " not yet in use.");
	    //}
	    return true;
	    
	} catch(BindException ex){
	    // "Signals that an error occurred while attempting to bind a 
	    // socket to a local address and port. Typically, the port is 
	    // in use, or the requested local address could not be assigned."
	    if(verbose) {
		System.err.println("Port " + portnum + " already in use or can't be assigned.");
	    }
	    tmpSocket = null;
	    return false;	    
	} catch(Exception ex) { 
	    // Some other problem creating or closing the server socket
	    System.err.println("Problem creating or closing server socket at port " + portnum);
	    tmpSocket = null;
	    return false;
	}
    }
}