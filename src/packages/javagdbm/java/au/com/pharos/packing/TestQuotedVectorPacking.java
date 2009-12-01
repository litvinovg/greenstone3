/*
 * module: pip/java/packing -- Strategy objects for converting
 *         Java objects to and from stored data.
 * class:  TestQuotedVectorPacking -- Test harness for QuotedVectorPacking
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

// TODO: Perhaps make this into a more general-purpose test harness
// for testing any Packing strategy? -- mbp 19971021

/** Test harness for the QuotedVectorPacking strategy class.
 *
 * <P><B>Usage:</B>
 * <UL><P><CODE>java TestQuotedVectorPacking -[de] ARGS</CODE><UL>
 *
 * <P><B>Options</B>
 * <DL>
 *   <P><DT><CODE>-d</CODE><DD>Print the results of decoding
 *      the each of the command-
 *      line arguments as QuotedVectorPacking encoding strings
 *
 *   <P><DT><CODE>-e</CODE><DD>Print the results of encoding
 *      a vector containing
 *      all of the command-line arguments 
 * </DL>
 *
 * @author Martin Pool
 * @version $Revision$ $Date$
 **/
public class TestQuotedVectorPacking {
    private TestQuotedVectorPacking() {}
    
    public static void main(String[] argv) {
	Packing strat = new QuotedVectorPacking();
	final String usage = "usage: TestQuotedVectorPacking -[ed] ARGS";

	if (argv.length == 0) {
	    System.err.println(usage);
	    return;
	}
	
	if (argv[0].equals("-e")) {
	    Vector encode = new Vector(argv.length - 1);
	    for (int i = 1; i < argv.length; i++) {
		System.out.println(argv[i]);
		encode.addElement(argv[i]);
	    }
	    byte[] packed = strat.toBytes(encode);
	    System.out.print("\t");
	    System.out.println(new String(packed));
	} else if (argv[0].equals("-d")) {
	    for (int i = 1; i < argv.length; i++) {
		Vector decode = (Vector) strat.fromBytes(argv[i].getBytes());
		System.out.println(argv[i]);
		for (int j = 0; j < decode.size(); j++) {
		    System.out.print("\t");
		    System.out.println(decode.elementAt(j));
		}
	    }
	} else {
	    System.err.println(usage);
	}
    }
}
