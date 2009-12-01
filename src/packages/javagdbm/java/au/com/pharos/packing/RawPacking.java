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

/** A packing strategy which stores only raw arrays of bytes.  Other
 * objects cannot be stored using this strategy.
 *
 * @author Martin Pool
 * @version $Revision$
 **/
public class RawPacking extends Packing implements java.io.Serializable
{
    /** Perform the trivial packing case, casting an array of bytes
     * into the same.
     *
     * @throw IllegalArgumentException if <em>obj</em> is neith null nor
     * an array of bytes.
     */
    public byte[] toBytes(Object obj) throws IllegalArgumentException
    {
	if (obj == null)
	    return null;
	else if (obj instanceof byte[])
	    return (byte[]) obj;
	else
	    throw cantConvert(obj);
    }

    /** Performs the trivial unpacking case of returning <em>raw</em>
     * untouched.
     *
     * @return either null or <em>raw</em>; always assignable to
     * <code>byte[]</code>
     **/
    public Object fromBytes(byte[] raw)
    {
	return raw;
    }
}
    
