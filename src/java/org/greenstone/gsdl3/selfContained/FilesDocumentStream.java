/*
 *    DocumentStreamFromFiles.java
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
import org.xml.sax.InputSource;
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
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;


  
/**
 * DocumentStreamFromFiles creates a stream of documents from a directory or directories.
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s
.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * @see java.io.File
 * @see DocumentStream
 */
public class FilesDocumentStream 
  implements DocumentStream {
  /** the files we've found so far */
  Vector files = new Vector();
  /** the directories we've found so far */
  Vector directories = new Vector();

  /**
   * Filename constructor
   * 
   * @exception java.io.IOException file access do
   * @param filename the filename of the file or directory
   */
  public FilesDocumentStream(String filename)  {
    File file = new File(filename);
    if (file.exists() && file.isDirectory())
      directories.add(file.getAbsolutePath());
    else
      files.add(file.getAbsolutePath());
  }
  /**
   * File constructor
   * 
   * @exception java.io.IOException file access do
   * @param file a handle to the file
   */
  public FilesDocumentStream(File file) {
    if (file.exists() && file.isDirectory())
      directories.add(file.getAbsolutePath());
    else
      files.add(file.getAbsolutePath());
  }

  /** 
   * A small inner class to filter out those files that don't 
   * end in ".xml" or ".XML"
   */
  class XMLFilenameFilter implements FilenameFilter {
    /** Tests if a specified file should be included in a file list. */
    public boolean accept(File dir, String name) {
      String target = ".xml";
      if (name.length() < target.length())
	return false;
      String extension = name.substring(name.length() - 4, name.length());
      //System.out.println(name + " ==>> " +  extension);
      if (extension.equalsIgnoreCase(target)) {
	return true;
      } else {
	return false;
      }
    }    
  }

  /**
   * Opens a new directory if there are no files are queued
   * 
   * @exception java.io.IOException when the underlying file access does
   */
  protected void expandIfNecessary() {
    while (directories.size() > 0) {
      String dirname = (String) directories.elementAt(0);
      directories.removeElement(dirname);
      File dir = new File(dirname);
      if (!dir.exists()) throw new Error ("error in expand: expecting a directory " + dirname);
      String[] fileArray = dir.list();
      for (int i=0;i<fileArray.length;i++){
	File file = new File(dir, fileArray[i]);
	if (file.exists() && file.isDirectory()) 
	  directories.add(file.getPath());
      }
      
      fileArray = dir.list(new XMLFilenameFilter());
      for (int i=0;i<fileArray.length;i++){
	File file = new File(dir, fileArray[i]);
	if (file.exists() && !file.isDirectory())
	  files.add(file.getPath());
      }
      if (files.size() > 0)
	return;
    }
  }
  
  /**
   * Returns the next document
   * 
   * @exception java.io.IOException when the underlying file access does
   * @return the next document
   */
  public Document nextDocument()
    throws Exception {
    
    if (!hasNextDocument()) throw new Error("Doesn't have another Document");

    String filename = (String) files.elementAt(files.size() - 1);
    files.removeElementAt(files.size() - 1);
    File file = new File(filename);
    
    Reader reader = new FileReader(file);
    InputSource xml_source = new InputSource(reader);

    XMLUtil.getDOMParser().parse(xml_source);
    Document doc = XMLUtil.getDOMParser().getDocument();
    
    return doc;
  }
    
  /**
   * Is there another document ?
   * 
   * @exception java.io.IOException when the underlying file access does
   * @return the next document
   */

  public boolean hasNextDocument()  throws Exception {
    expandIfNecessary();
    return (files.size() > 0);
  }
  /**
   * Tests...
   *
   * 
   * @exception java.io.IOException when ...
   * @param args the arguments ...
   */

  public static void main(String args[]) throws Exception
  {
   
    StreamResult result = new StreamResult(System.out);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    FilesDocumentStream stream = new FilesDocumentStream(".");
    while (stream.hasNextDocument()) {
      Document document = stream.nextDocument();
      Transformer transformer = transformerFactory.newTransformer();
      Source source = new DOMSource(document);
      transformer.transform(source,result);
      System.out.println();
      System.out.println();
    }

  } 
}

