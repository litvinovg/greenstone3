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

/** StringPacking converts Java Strings to and from byte arrays
 * using the default UTF-8 encoding.
 *
 * <P>For strings containing only ASCII-7 characters, this is
 * equivalent to <code>NativeStringPacking</code> but probably
 * somewhat slower.  However, it will safely handle non-ASCII
 * characters, or unusual locales.
 *
 * @see au.com.pharos.packing.Packing
 * @see au.com.pharos.packing.NativeStringPacking
 **/
public class StringPacking extends Packing
implements java.io.Serializable
{
    /** Convert a String to an array of bytes using Java's default
     * encoding.
     *
     * <P>If <em>obj</em> is not a String, array of bytes, or null
     * then it's <code>toString()</code> method is called first to
     * convert it to an array of bytes.  This will lose information in
     * many cases.
     *
     * @param obj The object to convert.
     *
     * @return <em>obj</em> converted to an array of bytes; or null if
     * <em>obj</em> is null.
     **/
    public byte[] toBytes(Object obj)
    {
	if (obj == null)
	    return null;
	else if (obj instanceof byte[])
	    return (byte[]) obj;
	else if (obj instanceof String)
	    return ((String) obj).getBytes();
	else
	    return obj.toString().getBytes();
    }

    /** Decode an array of bytes using the default String encoding.
     *
     * @param raw An array of bytes to decode.
     *
     * @return A String representation of <em>raw</em>; or null if
     * <em>raw</em> is null.
     **/     
    public Object fromBytes(byte[] raw)
    {
	if (raw == null)
	    return null;
	else {
	    try{
	    return new String(raw, "UTF-8");
	    } catch (Exception e) {
		System.err.println("String Packing:encoding not supported");
		return new String(raw);
	    }
	}
    }
}
    
