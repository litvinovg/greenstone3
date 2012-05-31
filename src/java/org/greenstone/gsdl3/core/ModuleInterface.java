/*
 *    ModuleInterface.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.core;
   
import org.w3c.dom.Node;

/**
 * interface for all modules in greenstone.
 *
 * all components talk via a process method - in its simplest form,
 * it takes a String of XML, and returns a String of XML
 *
 * the more efficient process method uses DOM Nodes instead 
 * of Strings - this avoids parsing the XML at each module
 *
 * @author Katherine Don
 * @version $Revision$
 */
public interface ModuleInterface {
 
  /**
   * Process an XML request - as a String
   *
   * @param xml_in the request to process
   * @return the response - contains any error messages
   * @see java.lang.String
   */
    abstract public String process(String xml_in);

  /**
   * Process an XML request - as a DOM model Node
   *
   * @param xml_in the request to process
   * @return the response - contains any error messages
   * @see org.w3c.dom.Node
   */
    abstract public Node process(Node xml_in);

    /** 
     * Do any clean up necessary for deactivating the module, eg
     * close any open file handles (gdbm in particular) or windows
     * holds locks on them.
     */
    abstract public void cleanUp();
}    
                                                                           
