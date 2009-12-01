/*
 * module: pip/java/packing -- Packing strategy objects for converting
 *           Java objects to and from stored data.
 * class: Packing -- Abstract strategy class for packing/unpacking data
 *
 * $Id$
 * Copyright (C) 1997 Pharos IP Pty Ltd
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

/** A packing Packing defines a method of converting an object to
 * and from an array of bytes for storage in a database.
 *
 * <P>Generally, each packing Packing should be reflexive: that is,
 * converting any object to and from an array of bytes should yield
 * the same object, or an object appropriately equivalent.
 *
 * <P>Some Packing subclasses may not be able to pack all
 * objects, or may not be able to unpack an object from some byte
 * arrays.  For example, only Strings can safely be stored as string
 * representations, and not all arrays are valid Java serialization
 * forms.  In this case, the Packing object should throw an
 * <code>IllegalArgumentException</code> from <code>toBytes()</code>
 * or <code>toBytes()</code>.
 *
 * <P>In both cases, null references should be passed through
 * unchanged.
 *
 * <P>Example:
 * <PRE>
 *    Packing strategy = new StringPacking();
 *    byte[] raw = strategy.toBytes("Hello world");
 *    ... store and retrieve raw[]
 *    String result = (String) strategy.fromBytes(raw)
 * </PRE>
 *
 *
 * @see au.com.pharos.packing.StringPacking
 * @see au.com.pharos.packing.NativeStringPacking
 * @see au.com.pharos.packing.RawPacking
 * @see au.com.pharos.packing.SerializationPacking
 *
 * @version $Revision$
 * @author Martin Pool
 **/
public abstract class Packing {
    /** Convert an object to an array of bytes. **/
    public abstract byte[] toBytes(Object obj) throws IllegalArgumentException;

    /** Reconstitute an array of bytes into an object. **/
    public abstract Object fromBytes(byte[] raw) throws IllegalArgumentException;

    /** Helper function that generates an IllegalArgumentException */
    protected IllegalArgumentException cantConvert(Object obj) {
	return new IllegalArgumentException
	    ("Can't pack object \"" + obj.toString() +
	     "\" (" + obj.getClass().toString() + ")" + 
	     "\n\tusing strategy " + this.toString());
    }
}

    
