/*
 *    Increment.java
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
import java.io.Writer;


/**
 * Class Increment is an abstract class for incrementing streams and strings
 *
 *
 * @author <a href="http://www.cs.waikato.ac.nz/~say1/">stuart yeates</a> (<a href="mailto:s.yeates@cs.waikato.ac.nz">s.yeates@cs.waikato.ac.nz</a>) at the <a href="http://www.nzdl.org">New Zealand Digital Library</a>
 * @version $Revision$
 */

abstract public class Increment implements Cloneable, Serializable
{
  /**
   * Increment a string of characters
   * 
   * @exception java.io.Exception if any of the characters in the string are outside the range of first-last
   * @param str the string to be incremented
   * @return the incremented string
   * @see Increment
   */
  abstract String incrementString(String str);

  /**
   * Increment a stream of characters, reading from the reader and writing the incremented characters to the writer
   * 
   * @exception java.io.Exception if any of the characters in the string are outside the range of first-last
   * @param str the string to be incremented
   * @return the incremented string
   * @see Increment
   */
  abstract long incrementStream(Reader rdr, Writer wtr);

  /**
   * Print to standard out a verbose message about the 
   * results of incrementing this string
   * 
   * @param s the string to increment
   * @see #incrementString(String)
   */
  final void checkStr(String s) {
    try {
      System.out.println("incrementing: \"" + s +
			 "\" to \"" + incrementString(s) +
			 "\"  " + stringToOctal(s) +
			 " ===> " + stringToHex(incrementString(s)));
    } catch (Exception e) {
      System.out.println("Caught exception: " + e);
    }
  }

  /**
   * Converts a unicode string to Hex
   * 
   * @exception java.io.IOException when ...
   * @param s the string to convert
   * @return a string in hex
   * @see Integer#toHexString(int)
   */
  static final String stringToHex(String s) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0;i<s.length();i++) {
      buffer.append(" 0x");
      buffer.append(Integer.toHexString(s.charAt(i)));
    } 
    return buffer.toString();
  }

  /**
   * Converts a unicode string to Octal
   * 
   * @param s the string
   * @return the string in octal
   * @see Integer#toOctalString(int)
   */
  static final String stringToOctal(String s) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0;i<s.length();i++) {
      buffer.append(" 0x");
      buffer.append(Integer.toOctalString(s.charAt(i)));
    } 
    return buffer.toString();
  }
}

