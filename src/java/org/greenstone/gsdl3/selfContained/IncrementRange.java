/*
 *    IncrementRange.java
 *    Copyright (C) 2000-2001 Stuart Yeates 
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

// the package we're in
package org.greenstone.gsdl3.selfContained;

// java standard library classes used
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Class IncrementRange increments strings and streams over a limited range of Characters.
 *
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 * @see Increment
 * 
 */
final public class IncrementRange
  extends Increment 
  implements Cloneable, Serializable
{
  /**
   * Create an IncrementRange which counts between first and last inclusive
   * 
   * @param first the first character in the range
   * @param last the last character in the range
   * @see Increment
   */
  IncrementRange(char first, char last){
    this.first = first;
    this.last = last;
  }
  /**
   * Create a default IncrementRange
   * 
   * @see Increment
   */
  IncrementRange(){
  }

  /**
   * Create a IncrementRange over all Unicode characters
   * 
   * @see Increment
   */
  IncrementRange(boolean b){
    if (true) {
      this.first = Character.MIN_VALUE;
      this.last = Character.MAX_VALUE;
    }
  }  
  /** the last character in out range */
  private char first = ' ';
  /** the last character in out range */
  private char last = 'z';

  /**
   * Increment a stream of characters, reading from the reader and writing the incremented characters to the writer
   * 
   * @exception java.io.Error if any of the characters in the string are outside the range of first-last
   * @param str the string to be incremented
   * @return the incremented string
   * @see Increment
   */
  long incrementStream(Reader rdr, Writer wtr) {
    try {
      long count = 0; // the return value
    
      int i = rdr.read();
      boolean carry = true;
    
      while (i != -1) {
	char c = (char) i;
      
	// check the validity of the character we're about to increment
	if (c < first || c > last) 
	  throw new Error("Character '" + c + "' is ouside range");
      
	if (c == last) {
	  if (carry == true) {
	    wtr.write(first);
	    carry = true;
	  } else {
	    wtr.write(c);
	    carry = false;
	  }
	} else {
	  if (carry == true) {
	    wtr.write(++c);
	    carry = false;
	  } else {
	    wtr.write(c);
	    carry = false;
	  } 
	}
	count++;

	// read the next character
	i = rdr.read();
      }
      if (carry == true) {
	wtr.write(first);
	count++;
      }
      return count;
    } catch (IOException e) {
      System.err.println("IOException in incrementStream()");
      throw new Error(e.toString());
    }
  }

  /**
   * Increment a string of characters
   * 
   * @exception java.lang.Error if any of the characters in the string are outside the range of first-last
   * @param str the string to be incremented
   * @return the incremented string
   * @see Increment
   */
  public final String incrementString(String str)  {
    StringBuffer buffer = new StringBuffer();
    if (str.equals("")) {
      buffer.append(first);
      return buffer.toString();
    } else {
      boolean carry = true;
      for (int i=0;i<str.length();i++) {
	char c = str.charAt(i);
	if (c < first || c > last) 
	  throw new Error("Character '" + c + "' is ouside range");

	if (c == last) {
	  if (carry == true) {
	    buffer.append(first);
	    carry = true;
	  } else {
	    buffer.append(c);
	    carry = false;
	  }
	} else {
	  if (carry == true) {
	    buffer.append(++c);
	    carry = false;
	  } else {
	    buffer.append(c);
	    carry = false;
	  } 
	}
      }
      if (carry == true)
	buffer.append(first);
    }
    return buffer.toString();
  }


  /**
   * Test the incrementing of strings (never returns)
   */
  public static void testStringIncrement() throws IOException
  {
    IncrementRange n = new IncrementRange ();
    try {
      String tmp = "";
      System.out.println(stringToHex(tmp));
      while (true) {
	//checkStr(tmp);
	tmp = n.incrementString(tmp);
	n.checkStr(tmp);
      }
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
    }

  }

  /**
   * Test the incrementing of streams (never returns)
   *
   */
  public static void testStreamIncrement() 
  {
    IncrementRange n = new IncrementRange ();
    try {
      long counter = 0;
      String tmp = "";
      System.out.println(stringToHex(tmp));
      while (true) {
	//checkStr(tmp);
	String tmp2 = n.incrementString(tmp);
	StringWriter wtr = new StringWriter();
	n.incrementStream(new StringReader(tmp),wtr);
	if (!tmp2.equals(wtr.toString()))
	  throw new Exception("Error: \"" + tmp2 + 
			      "\" and \"" + tmp2 + 
			      "\"");
	counter++;
	if (counter % 1000000 == 0)
	  System.out.println(counter + " " + tmp);

	tmp = tmp2;
      }
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
    }
  }

  /**
   * Tests stream incrementing 
   *
   * 
   */
  public static void main(String args[]) 
  {
    testStreamIncrement();
  } 
}

