/*
 *    GenerateDocumentStream.java
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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.TextImpl;
import org.w3c.dom.Document; 


// other java classes

/**
 * GeneratedDocumentStream generates an unbounded stream of XML documents
 * Each containing a single string.
 *
 * Based as a wrapper around an Increment object
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s
.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * @see DocumentStream
 * @see Increment
 * 
 */
public class GeneratedDocumentStream implements DocumentStream {
  
  /** The next string */
  protected String next = "";
  /** The underlying Increment object */
  protected Increment incrementer = new IncrementRange();

  /** The default constructor. Generates ASCII-only documents */
  public GeneratedDocumentStream() {

  }
  
  /** 
   * Custom constructor 
   *
   * @param incrementer the incrementer to use (controls the character set to use)
   */
  public GeneratedDocumentStream(Increment incrementer) {
    this.incrementer = incrementer;
  }
  
  /** 
   * Custom constructor 
   *
   * @param next the string for the first document (must suit the incrementor)
   */
  public GeneratedDocumentStream(String next) {
    this.next = next;
  }
  
  /** 
   * Custom constructor 
   *
   * @param incrementer the incrementer to use (controls the character set to use)
   * @param next the string for the first document (must suit the incrementor)
   */
  public GeneratedDocumentStream(Increment incrementer, String next) {
    this.incrementer = incrementer;
    this.next = next;
  }
  /** 
   * Custom constructor 
   *
   * @param first the first letter in the alphabet
   * @param last the last letter in the alphabet
   */
  public GeneratedDocumentStream(char first, char last) {
    this.incrementer = new IncrementRange(first,last);
  }


  /**
   * Generates the nest document in the sequence
   * 
   * @exception java.io.Exception when something goes wrong in the XML stuff
   * @return the next Document
   * @see org.w3c.dom.Document
   */
  public Document nextDocument() {


   DocumentImpl doc = new DocumentImpl(true);
    
   ElementImpl root = new ElementImpl(doc,"document");
   TextImpl text = new TextImpl(doc,next);
    
   doc.appendChild(root);
   root.appendChild(text);
 
   next = incrementer.incrementString(next);
   return doc;
   
  }
  /**
   * Are there more documents ?
   * 
   * @return yes
   */
  public boolean hasNextDocument(){
    return true;
  }
  /**
   * Generates an unbounded stream of documents
   */
  public static void main(String args[]) throws Exception
  {
    
    StreamResult result = new StreamResult(System.out);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    GeneratedDocumentStream stream = new GeneratedDocumentStream();
    while (stream.hasNextDocument()) {
      Document document = stream.nextDocument();
      Transformer transformer = transformerFactory.newTransformer();
      Source source = new DOMSource(document);
      transformer.transform(source,result);
    }
  }
  
}

