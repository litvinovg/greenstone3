/*
 *    QueryDocumentStream.java
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

// other java classes
import java.io.StringReader;

/**
 * QueryDocumentStream takes another document stream and removes 
 * from the stream all documents which fail to match a XSLT query.
 * The default query returns only those documents containing the 
 * string "the".
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s
.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * @see DocumentStream
 * 
 */
public class QueryDocumentStream implements DocumentStream {
  
  /** The default XSLT query string. Is true (returns a non-empty document) when the input document contains the string "the". */
  public static final String DEFAULT_QUERY_STRING =   
    "<xsl:stylesheet version=\"1.0\" " +
    "                    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
    "   <xsl:output omit-xml-declaration=\"yes\"/>" +
    
    "   <xsl:template match=\"/*\">" +
    "      <xsl:if test=\"contains(.,'the')\">" +
    "         <xsl:copy-of select=\".\"/>" +
    "      </xsl:if>" + 
    "   </xsl:template>" +
    "</xsl:stylesheet>";
  /** The first part of the default quesy for a string other than "the" */
  public static final String DEFAULT_QUERY_STRING_1_1 =   
    "<xsl:stylesheet version=\"1.0\" " +
    "                    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
    "   <xsl:output omit-xml-declaration=\"yes\"/>" +
    
    "   <xsl:template match=\"/*\">" +
    "      <xsl:if test=\"contains(.,'";

  /** The second part of the default quesy for a string other than "the" */
  public static final String DEFAULT_QUERY_STRING_1_2 =   
    "')\">" +
    "         <xsl:copy-of select=\".\"/>" +
    "      </xsl:if>" + 
    "   </xsl:template>" +
    "</xsl:stylesheet>";


  /** The query */
  protected StreamSource query = new StreamSource(new StringReader(DEFAULT_QUERY_STRING));

  /** The underlying DocumentStream object */
  protected DocumentStream stream = null;

  /** A cached Document */
  protected Document cached = null;

  /** The transformer factory */
  TransformerFactory transformerFactory  = null;

  /** The transformer */
  Transformer transformer  = null;

  /** Uses the default query (looking for the) over the default DocumentStream (all ascii strings) */
  public QueryDocumentStream() {
    this.query = new StreamSource(new StringReader(DEFAULT_QUERY_STRING));
    this.stream = new GeneratedDocumentStream();

    try {
      this.transformerFactory 
	= org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
      
      this.transformer
	= transformerFactory.newTransformer(query);
    } catch (TransformerConfigurationException e) {
      System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
    }
  }
  
  /** Looks for the given string in the stream of documents  */
  public QueryDocumentStream(DocumentStream stream, String query) {
    String fullQuery = DEFAULT_QUERY_STRING_1_1 + query + DEFAULT_QUERY_STRING_1_2;
    this.query = new StreamSource(new StringReader(fullQuery));
    this.stream = stream;

    try {
      this.transformerFactory 
	= org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
      
      this.transformer
	= transformerFactory.newTransformer(this.query);
    } catch (TransformerConfigurationException e) {
      System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
    }
  }
  

  /** Applies the stylesheet to the stream.  */
  public QueryDocumentStream(DocumentStream stream, StreamSource source) {
    this.query = source;
    this.stream = stream;

    try {
      this.transformerFactory 
	= org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
      
      this.transformer
	= transformerFactory.newTransformer(query);
    } catch (TransformerConfigurationException e) {
      System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
    }
  }
  

  /** The default constructor. Searches for documents containing "the" */
  public QueryDocumentStream(DocumentStream stream) {
    this.query = new StreamSource(new StringReader(DEFAULT_QUERY_STRING));
    this.stream = stream;

    try {
      this.transformerFactory 
	= org.apache.xalan.processor.TransformerFactoryImpl.newInstance();
      
      this.transformer
	= transformerFactory.newTransformer(query);
    } catch (TransformerConfigurationException e) {
      System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
    }
  }
  /**
   * Generates the nest document in the sequence
   * 
   * @exception java.io.Exception when something goes wrong in the XML stuff
   * @return the next Document
   * @see org.w3c.dom.Document
   */
  public Document nextDocument() throws Exception {

    if (hasNextDocument() == false)
      throw new Error("no more docs");
    Document result = cached;
    cached = null;
    return result;
  }
  /**
   * Are there more documents ?
   * 
   * @return true if there are more docs
   */
  public boolean hasNextDocument() throws Exception {
    lookInStream();
    return (cached != null);
  }
  /**
   *
   * 
   * @exception java.io.IOException when ...
   * @param arg2 ... 
   * @param arg1 ...
   * @return ...
   */
  protected boolean lookInStream() throws Exception {

    while (stream.hasNextDocument() && cached == null) {
      
      Document candidate = stream.nextDocument();
      
      DOMSource input = new DOMSource(candidate);
      DOMResult output = new DOMResult();
      
      try {
	
	transformer.transform(input, output);
      
	// if the transformed document is not empty don't cache it
	if (output.getNode().getFirstChild() != null)
	  cached = (Document) output.getNode();
      
      } catch (TransformerConfigurationException e) {
	System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
      } catch (TransformerException e) {
	System.err.println("XMLTransformer: couldn't transform the source: " + e.getMessage());
	cached = null;
	return false;
      } catch (Exception e) {
	System.err.println("Exception: " + e.getMessage());
	cached = null;
	return false;
      }
      
      if (false) { // debugging info
	
	try {
	  StreamResult result = new StreamResult(System.out);
	  Transformer transformer = transformerFactory.newTransformer();
	  
	  Source source = new DOMSource(candidate);
	  transformer.transform(source,result);
	  
	  System.out.print(" ==> ");
	  
	  if (cached != null) {
	    Source source2 = new DOMSource(cached);
	    transformer.transform(source2,result);
	  } 
	  System.out.println();
	  System.out.println("======================================================");
	  
	} catch (TransformerConfigurationException e) {
	  System.err.println("XMLTransformer: couldn't create transformer object: "+e.getMessage());
	} catch (TransformerException e) {
	  System.err.println("XMLTransformer: couldn't transform the source: " + e.getMessage());
	  cached = null;
	  return false;
	} catch (Exception e) {
	  System.err.println("Exception: " + e.getMessage());
	  cached = null;
	  return false;
	}
	
      }
    }
    return cached == null;    
  }

  /**
   * Generates an unbounded stream of documents
   */
  public static void main(String args[]) throws Exception
  {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    
    GeneratedDocumentStream gStream = new GeneratedDocumentStream();
    QueryDocumentStream qStream = new QueryDocumentStream(gStream);
    while (qStream.hasNextDocument()) {
      Document document = qStream.nextDocument();
      
      StreamResult result = new StreamResult(System.out);
      
      Source source = new DOMSource(document);
      transformer.transform(source,result);
      
      System.out.println();
      System.out.println("======================================================");
    }
  }
  
}

