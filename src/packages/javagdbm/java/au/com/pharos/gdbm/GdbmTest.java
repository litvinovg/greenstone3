/*
 * module: pip/java/gdbm -- A Java interface to the GDBM library
 * class:  GdbmTest -- Test suite for this package
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

package au.com.pharos.gdbm;

import java.io.PrintStream;

import java.util.Enumeration;

import au.com.pharos.test.Test;
import au.com.pharos.packing.Packing;
import au.com.pharos.packing.StringPacking;
import au.com.pharos.packing.RawPacking;

/** Test harness for the Gdbm library.
 *
 * <P>The source of this class forms a useful demonstration of the
 * JavaGDBM library and is commended to the intending programmer.
 *
 * <P>More test cases would be very welcome.
 *
 * @see au.com.pharos.gdbm.GdbmFile
 * @see au.com.pharos.packing.Packing
 * @author Martin Pool
 * @version $Revision$
 */
public class GdbmTest {
    // Don't instantiate
    private GdbmTest() { ; }
    
    private static void testLongData(GdbmFile db)
	 throws Exception
    {
	StringBuffer lgBuffer = new StringBuffer();
	for (int i = 0; i < 100; i++)
	    lgBuffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890" +
			    "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890" +
			    "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890");

	String lgString = lgBuffer.toString();
	lgBuffer = null;
	String smallKey = "largeDatum";

	db.store(smallKey, lgString);
	String retr = (String) db.fetch(smallKey);

	Test.ok(9, retr.hashCode() == lgString.hashCode());
	Test.ok(10, retr.equals(lgString));

	db.delete(smallKey);
	Test.ok(11, true);

	db.reorganize();
	Test.ok(12, true);
    }


    static final int MANY_RECORDS = 200;
    
    private static void testBigFile(GdbmFile db)
	 throws Exception
    {
	String key;

	// Test a lot of data
	byte[] bigData = new byte[10000];
	for (int i = 0; i < bigData.length; i++)
	    bigData[i] = (byte)((i & 63) + 32);
	db.setValuePacking(new RawPacking());
	Test.ok(150, true);

	// Store a series of records in the file
	for (int i = 0; i < MANY_RECORDS; i++) {
	    if ((i % 100) == 0)
		System.out.print(Integer.toString(i)+ " ");
	    db.store(Integer.toString(i, 16), bigData);
	}
	System.out.println();
	Test.ok(151, true);

	// Enumerate keys and values out of the file
	int count = 0;
	Enumeration keys = db.keys();
	while ( keys.hasMoreElements() ) {
	    key = (String) keys.nextElement();
	    count++;
	    if ((count % 100) == 0)
		System.out.print(Integer.toString(count)+ " ");
	}
	System.out.println();
	Test.ok(152, count==MANY_RECORDS);

	// Delete all the records from the file
	count = 0;
	while ( (key = (String) db.getFirstKey()) != null ) {
	    db.delete(key);
	    count++;
	    if ((count % 100) == 0)
		System.out.print(Integer.toString(count)+ " ");
	}
	System.out.println();
	Test.ok(153, count==MANY_RECORDS);

	db.reorganize();
	Test.ok(154, true);
    }

    public static void main(String[] argv) {
	try {
	    final String dbFile = "/tmp/java.gdbm";
	    String key, value;
	    boolean found;

	    // Test cases, inspired by db-hash.t in the Perl5.003_93
	    // distribution
	    System.out.print("GDBM Database tests:\n\t" +
			     GdbmFile.getLibraryVersion() + "\n\t" +
			     GdbmFile.getWrapperVersion() + "\n");

	    // Create a simple database
	    GdbmFile db = new GdbmFile(dbFile, GdbmFile.NEWDB);
	    Test.ok(1, db != null);

	    // Set the packing strategy
	    db.setKeyPacking(new StringPacking());
	    db.setValuePacking(new StringPacking());

	    // Store some data in the file
	    db.store("mountain", "Kosciusko");
	    Test.ok(2, true);

	    // Store and read back data
	    db.storeNoReplace("abc", "ABC");
	    Test.ok(3, ((String) db.fetch("abc")).equals("ABC"));

	    // Check whether keys exist or not
	    Test.ok(4, db.exists("abc"));
	    Test.ok(5, !db.exists("jimmy"));
	    Test.ok(6, !db.exists("abc\0"));

	    // Delete those records
	    db.delete("abc");
	    db.delete("mountain");

	    Test.ok(10, !db.exists("abc"));
	    Test.ok(11, !db.exists("mountain"));

	    // Store a series of records in the file
	    for (int i = 100; i < 200; i++)
		db.storeNoReplace(Integer.toString(i, Character.MAX_RADIX),
				  Integer.toString(i, Character.MIN_RADIX));

	    // Enumerate keys and values out of the file
	    int count = 0;
	    Enumeration keys = db.keys();
	    while ( keys.hasMoreElements() ) {
		key = (String) keys.nextElement();
		// System.out.print(key + "=");
		// value = (String) db.fetch(key);
		// System.out.println(value);
		count++;
	    }
	    Test.ok(20, count==100);

	    // Delete all the records from the file
	    count = 0;
	    while ( (key = (String) db.getFirstKey()) != null ) {
		db.delete(key);
		count++;
	    }
	    Test.ok(30, count==100);

	    // Try to fetch a non-existent key
	    boolean caught = false;
	    try {
		db.fetch("larry!");
	    } catch ( GdbmException e ) {
		// TODO: Check the exception was the one we expected?
		caught = true;
	    }
	    Test.ok(40, caught);

	    // Store some very large data
	    testLongData(db);

	    // Store strings containing non-ASCII characters
	    final String euroString = "Some funny characters: «¡ æ ø â ß !»";
	    db.store("high ASCII", euroString);
	    Test.ok(50, db.fetch("high ASCII").equals(euroString));

	    // Close the database
	    db.close();
	    Test.ok(60, true);

	    // Try to read from a database that is closed
	    caught = false;
	    try {
		db.fetch("larry!");
	    } catch ( Exception e ) {
		System.out.println("caught: " + e.toString());
		caught = true;
	    }
	    Test.ok(70, caught);

	    // Try to open a non-existent database
	    caught = false;
	    try {
		db = new GdbmFile("/tmp/somenonexistentdatabase.gdbm",
				  GdbmFile.READER);
	    } catch ( GdbmException e ) {
		System.out.println("caught: " + e.toString());
		caught = true;
	    }
	    Test.ok(80, caught);

	    // Open a database read-only
	    db = new GdbmFile(dbFile, GdbmFile.READER);
	    db.setKeyPacking(new StringPacking());
	    db.setValuePacking(new StringPacking());
	    Test.ok(90);

	    // Try to insert
	    caught = false;
	    try {
		db.store("apple", "crumble");
	    } catch ( GdbmException e ) {
		System.out.println("caught: " + e.toString());
		caught = true;
	    }
	    Test.ok(91, caught);

	    // Replace an existing database
	    db.close();
	    db = new GdbmFile(dbFile, GdbmFile.NEWDB);
	    db.setKeyPacking(new StringPacking());
	    db.setValuePacking(new StringPacking());
	    Test.ok(100, !db.exists("camel"));

	    // Sync to disk
	    db.sync();
	    Test.ok(105, true);

	    // Juggle three databases at once
	    final String dbFile2 = "/tmp/java2.gdbm";
	    GdbmFile db2 = new GdbmFile(dbFile2, GdbmFile.NEWDB);
	    final String dbFile3 = "/tmp/java3.gdbm";
	    GdbmFile db3 = new GdbmFile(dbFile3, GdbmFile.NEWDB);
	    Test.ok(110, true);

	    keys = db.keys();
	    while (keys.hasMoreElements()) {
		key = (String) keys.nextElement();
		value = (String) db.fetch(key);
		db2.store(key.getBytes(), value.getBytes());
		db3.store(key.getBytes(), value.getBytes());
	    }
	    Test.ok(120, true);

	    keys = db2.keys();
	    found = true;
	    while (keys.hasMoreElements())
		found &= db3.exists(keys.nextElement());
	    Test.ok(130, found);

	    db2.close();
	    db3.close();

	    Test.ok(140, true);

	    testBigFile(db);

	    db.close();
	    Test.ok(160, true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
