/*
 * module: pip/java/packing -- Strategy objects for converting
 *         Java objects to and from stored data.
 * class:  QuotedVectorPacking -- Vectors of Strings packed in a Perl-
 *         style format
 *
 * Copyright (C) 1997 Pharos IP Pty Ltd
 * $Id$
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package au.com.pharos.packing;

import java.util.Vector;

// FIXME: Escape bad characters! -- mbp
// TODO: Write a more formal definition of the encoding system
// TODO: More documentation

// XXX: Perhaps the strategy for encoding characters should be in a
// separate class used by this class?  They would have to
// interact to make sure that separator chatacters were decoded
// properly.  Probably not worth worrying about, as this class
// is only a compatibility measure in any case.

/** Stores Vectors of strings, packed into a single string, as used by
 * some Perl code.  The encoding format is similar to that used in
 * Unix databases like
 * <A HREF="file:/etc/passwd/"><CODE>/etc/passwd</CODE></A>, except that
 * it uses escape sequences to represent characters that would
 * normally not be allowed.
 *
 * <P>The strings within the vector are separated by separator characters,
 * which default to colons.
 *
 * <P>The resulting packed string is UTF encoded after escaping
 * control-characters, so non-ASCII characters should be passed through
 * unchanged.
 *
 * <P>Certain characters are escaped as quoted-printable strings.
 * These characters include colon, equals, and escape characters.  The
 * encoding accepts and interprets any hex escape when reading data.
 *
 * <P>If any elements of the vector are null, they are stored as empty
 * strings.  On unpacking, empty strings are returned as such.
 *
 * <P>An example of a vector encoded in this manner is:
 * <BLOCKQUOTE>
 * <PRE>oC7okz059wGnQ:vf:Peter Barnes%2c Pharos Business Solutions</PRE>
 * </BLOCKQUOTE>
 *
 * @author Martin Pool
 * @version $Revision$ $Date$
 *
 * @see java.util.Vector
 **/
public class QuotedVectorPacking extends Packing
implements java.io.Serializable
{
    private char hexEscape;
    private char separator;
    
    /** Constructs a QuotedVectorPacking using the default separator
     * character ':' and escape character '%'.
     **/
    public QuotedVectorPacking() {
	this('%', ':');
    }


    /** Constructs a QuotedVectorPacking with specified separator and
     * hex-escape characters.
     *
     * @param hexEscape the character used to begin a three-character
     * hex escape
     *
     * @param separator the character used to separate elements in the
     * vector.
     **/
    public QuotedVectorPacking(char hexEscape, char separator) {
	if (hexEscape == separator)
	    throw new IllegalArgumentException
		("hexEscape and separator characters are the same");
	this.hexEscape = hexEscape;
	this.separator = separator;
    }
    
    
    /** Convert a Vector of Strings to a QuotedVector represented as an
     * array of bytes.
     *
     * @param obj the Vector to encode; or null.
     * 
     * @returns an array of bytes containing a packed representation of
     * <EM>obj</EM>; or null if <EM>obj</EM> is null.
     *
     * @exception java.lang.ClassCastException if obj is not a Vector
     * or null; or if any element of <EM>obj</EM> is not a String or
     * null.
     **/
    public byte[] toBytes(Object obj)
    {
	if (obj == null)
	    return null;
	Vector vec = (Vector) obj;
	StringBuffer sb = new StringBuffer();
	Object elem;
	for (int i = 0; i < vec.size(); i++) {
	    if (i > 0)
		sb.append(separator);
	    elem = vec.elementAt(i);
	    sb.append((elem == null) ? "" : encodeString((String) elem));
	}
	return sb.toString().getBytes();
    }



    /** Perform hex-escapes on a string.
     * @returns <EM>from</EM> encoded as a quoted-printable string
     **/
    private String encodeString(String from) {
	StringBuffer buf = new StringBuffer(from.length());
	char[] chars = from.toCharArray();
	for (int i = 0; i < chars.length; i++) {
	    char ch = chars[i];
	    if (ch == separator
		|| ch == hexEscape
		|| Character.isISOControl(ch)
		|| (ch != ' ' && ch != '\t' && Character.isWhitespace(ch)))
		{
		    buf.append(hexEscape);
		    // XXX: This will truncate high Unicode characters, but I
		    // don't know what else we can do with them. -- mbp
		    buf.append(Character.forDigit((ch>>4) & 0xf, 16));
		    buf.append(Character.forDigit(ch      & 0xf, 16));
		} else {
		    buf.append(ch);
		}
	}
	return buf.toString();
    }
    
    

    /** Interprets <em>raw</em> as a packed quoted Vector, and
     * returns the unpacked Vector of strings. */
    public Object fromBytes(byte[] raw)
    {
	if (raw == null)
	    return null;
	
	Vector result = new Vector();
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < raw.length; i++) {
	    char ch = (char) raw[i];
	    if (ch == separator) {
		result.addElement(sb.toString());
		sb.setLength(0);
	    } else if (ch == hexEscape && i < (raw.length - 2)) {
		byte[] hexBytes = new byte[2];
		hexBytes[0] = raw[++i];
		hexBytes[1] = raw[++i];
		String hex = new String(hexBytes);
		ch = (char)Integer.parseInt(hex, 16);
		sb.append(ch);
	    } else {
		sb.append(ch);
	    }
	}
	result.addElement(sb.toString());
	return result;
    }
}
