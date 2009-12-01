/*
 * Copyright (C) 1997 Pharos IP Pty Ltd
 * $Id$
 * Confidential. All rights reserved.
 */

package au.com.pharos.meta;

import java.lang.reflect.Field;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.PrintWriter;

/*
 * TODO: Allow people to specify an input file?
 * TODO: Allow people to customize the display format?
 *
 * FIXME: We should use a custom classloader to load the untrusted
 * classes under examination and to allow for flexible handling or
 * tracing as classes are loaded.
 */


/** Java utility to display in a debugging format the contents of a
 * serialized object stream.
 *
 * <P>Usage:
 * <PRE>
 *   java au.com.pharos.meta.SerializationDumper <SERFILE
 * </PRE>
 *
 * where SERFILE is the name of the serialization file.
 *
 * <P><B>Limitations:</B>
 *
 * <OL>
 *   <P><LI>Values are displayed only for public fields, due to Java
 *    security restrictions.
 *   <P><LI>Only top-level objects are dumped.
 * </OL>
 *
 * <P>These are fairly serious restrictions, but SerializationDumper
 * at least checks that the stream is well-formed, and gives some
 * indication of what it contains.
 *
 * @author Martin Pool
 * @version $Revision$
 **/
public class SerializationDumper {
    public static void main(String argv[])
    {
	try {
	    ObjectInput in = new ObjectInputStream(System.in);
	    PrintWriter writer = new PrintWriter(System.out);
	    SerializationDumper dumper = new SerializationDumper();
	    dumper.dump(in, writer);
	    writer.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /** Dump all objects read from <EM>in</EM> to <EM>out</EM>
     * in a human-readable debugging form.
     **/
    public void dump(ObjectInput in, PrintWriter out)
	 throws IOException
    {
	while (true) {		// exception on eof
	    Object obj = null;
	    try {
		obj = in.readObject();
		displayObject(obj, out);
	    }
	    catch (ClassNotFoundException e) {
		warning(e, out);
	    }
	    catch (OptionalDataException e) {
		error(e, out);
		return;
	    }
	    catch (ObjectStreamException e) {
		warning(e, out);
	    }
	    catch (EOFException e) {
		return;
	    }
	}
    }


    /** Display an object in human-readable form. **/
    private void displayObject(Object obj, PrintWriter out)
	 throws IOException
    {
	if (obj == null) {
	    out.println("(null)");
	    return;
	}

	Class cls = obj.getClass();

	// Print the header: the name, hashcode, and string
	// representation
	out.println(cls.getName() + "@0x" +
		    Integer.toHexString(obj.hashCode()) + "{");
	out.println("\t\"" + obj.toString() + "\"");

	// TODO: Recursive descent of the graph, allowing for circular
	// structures.

	// Print each field of the object
	Field[] fields = cls.getDeclaredFields();
	for (int i = 0; i < fields.length; i++)
	    displayField(obj, fields[i], out);

	out.println("}");
    }


    /** Display a particular field from an object in human-readable
      * form.  Fields which are not accessible for security reasons
      * are noted as such, and the name and type of the field is
      * still printed. **/
    private void displayField(Object obj, Field fld, PrintWriter out)
	 throws IOException
    {
	int flags = fld.getModifiers();
	out.print("\t");
	out.print(fld.toString() + ": ");
	try {
	    Object value = fld.get(obj);
	    if (value == null) {
		out.println("(null)");
	    }
	    else {
		out.println(value.getClass().getName() + "@0x" +
			    Integer.toHexString(value.hashCode()) + ":");
		out.println("\t\t\"" + value.toString() + "\"");
	    }
	} catch (IllegalAccessException e) {
	    out.println("(not accessible)");
	}
    }


    /** Display a warning that an exception occurred while dumping
     * information. **/
    private void warning(Exception e, PrintWriter out)
	 throws IOException
    {
	out.println("warning: " + e);
    }

    /** Display an exception that occurred while dumping
     * information. **/
    private void error(Exception e, PrintWriter out)
	 throws IOException
    {
	out.println("error: " + e.toString());
    }
}

