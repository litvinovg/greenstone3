/*
 * module: pip/java/packing -- Strategy objects for converting
 * Java objects to and from stored data.
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

/** Convert an array of bytes in a Java string without using String
 * encodings, just using expanding 8-bit bytes to 16-bit chars.
 *
 * <P>This will only work for ASCII characters, of course, but for
 * moving bulk data around it is considerably faster than using
 * UTF-8 encoding strategies.  Naively compressing Unicode into
 * 8-bit characters in this manner is deprecated in JDK1.1 to
 * help ensure that programs are Unicode-safe.
 *
 * @author Martin Pool
 * @version $Revision$
 *
 * @see au.com.pharos.packing.Packing
 * @see au.com.pharos.packing.StringPacking
 */
public class NativeStringPacking extends Packing
{
    /** Convert an object to an ASCII String representation.
     *
     * @return null if obj is null; obj is obj is already a
     * byte[]; otherwise an native 8-bite encoded version of
     * obj.toString().
     **/
    public byte[] toBytes(final Object obj)
    {
	String str;
	if (obj == null)
	    return null;
	else if (obj instanceof byte[])
	    return (byte[]) obj;
	else if (obj instanceof String)
	    str = (String) obj;
	else
	    str = obj.toString();

	int len = str.length();
	byte[] rawArray = new byte[len];
	str.getBytes(0, len, rawArray, 0);
	return rawArray;		     
    }

    /** Convert an array of bytes into a String
     *
     * @param raw the raw data to be decoded.
     *
     * @return null if <em>raw</em> is null; otherwise a string with the low
     * 8 bits of each character taken from the corresponding byte in
     * <em>raw</em>.
     **/
    public Object fromBytes(final byte[] raw)
    {
	if (raw == null)
	    return null;
	else
	    return new String(raw, 0);
    }
}
    
