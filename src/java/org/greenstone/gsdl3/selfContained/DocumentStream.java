/*
 *    DocumentStream.java
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
package org.greenstone.gsdl3.selfContained;

// XML classes
import org.w3c.dom.Document; 

// other java classes
import java.io.Serializable;


/**
 * Document Stream represents a stream of XML documents
 *
 * Based on algorithms in ...
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s
.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * 
 */
public interface DocumentStream extends Cloneable, Serializable {
  
  /**
   * Get the next document
   * 
   * @exception java.io.Exception when something goes wrong
   * @return the next document
   */
  public Document nextDocument() throws Exception ;
  /**
   * Is there a next document?
   * 
   * @exception java.io.Exception when something goes wrong
   * @return true if there is a next document
   */
  public boolean hasNextDocument() throws Exception ;
  
}
