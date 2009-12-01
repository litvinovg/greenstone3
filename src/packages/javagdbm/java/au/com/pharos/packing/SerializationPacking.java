/*
 * class: SerializationPacking -- Store an object as serialized data
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/** A packing strategy which stores and retrieves objects using
 * <a href="http://www.javasoft.com/products/jdk/1.1/docs/guide/serialization/index.html">Java serialization</a>.
 *
 * @version $Revision$
 * @author Martin Pool
 * @see au.com.pharos.packing.Packing
 **/
public class SerializationPacking extends Packing
implements java.io.Serializable
{
    /** Convert an object to a Java serialization format.
     *
     * @returns a buffer containing a a serialized representation of <em>obj</em>;
     * or null if <em>obj</em> if null
     * @exception IllegalArgumentException if <em>obj</em> (or any object
     * referred to by <em>obj</em>) cannot be serialized
     **/
    public byte[] toBytes(Object obj) throws IllegalArgumentException
    {
	if (obj == null)
	    return null;

	// Serialize via a stream into a buffer
	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	try {
	    ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
	    objStream.writeObject(obj);
	    // Don't close the stream, or you will lose the byte array
	    objStream.flush();
	} catch (IOException e) {
	    throw new IllegalArgumentException("IOException during serialization: " +
					       e.toString());
	}

	return byteStream.toByteArray();
    }

    /** Interprets <em>raw</em> as a Java serialization stream, and
     * returns <B>the first</B> object read from that stream.
     *
     * <P><B>Note:</B>There is currently no means by which any later
     * objects in the can be retrieved: it is assumed that the object
     * written to the buffer contains references to all the
     * information required.  This is consistent with the fact that
     * toBytes() stores only a single object, so this packing strategy
     * is completely reversable.
     **/
    // FIXME: Is the correct word 'reversable', 'reflexive', or
    // something else?  -- mbp@pharos.com.au
    public Object fromBytes(byte[] raw)
    {
	if (raw == null)
	    return null;

	Object obj;
	try {
	    ByteArrayInputStream byteStream = new ByteArrayInputStream(raw);
	    ObjectInputStream objStream = new ObjectInputStream(byteStream);
	    obj = objStream.readObject();
	    objStream.close();
	} catch (IOException e) {
	    throw new IllegalArgumentException("Exception during deserialization: " +
					       e.toString());
	} catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException("Exception during deserialization: " +
					       e.toString());
	}	    
	return obj;
    }
}
    
