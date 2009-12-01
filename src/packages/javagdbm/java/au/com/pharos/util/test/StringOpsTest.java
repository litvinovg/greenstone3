/*
 * Copyright (C) 1997 by Pharos IP Pty Ltd
 * Confidential.  All rights reserved.
 * $Id$
 * $Source$
 */

package au.com.pharos.util.test;

import au.com.pharos.test.Test;

import au.com.pharos.util.StringOps;

/**
 * Test harness for <CODE>au.com.pharos.util.StringOps</CODE>
 *
 * @author Martin Pool
 * @version $Revision$ $Date$
 **/
public class StringOpsTest
{
    static public void main(String[] args)
    {
	new StringOpsTest().run();
    }

    public void run()
    {
	try {
	    testCountOccurrences();
	}
	finally {
	    Test.summary();
	}
    }

    public void testCountOccurrences()
    {
	// Simple tests of countOccurences(String, char)
	Test.ok(100, ( StringOps.countOccurrences("hello world", 'z') == 0) );
	Test.ok(110, ( StringOps.countOccurrences("zhello world", 'z') == 1) );
	Test.ok(120, ( StringOps.countOccurrences("hello worldz", 'z') == 1) );
	Test.ok(130, ( StringOps.countOccurrences("z", 'z') == 1) );
	Test.ok(140, ( StringOps.countOccurrences("zzz", 'z') == 3) );
	Test.ok(150, ( StringOps.countOccurrences("0z0z0z", 'z') == 3) );
	Test.ok(160, ( StringOps.countOccurrences("\0z\0z\0z", '\0') == 3) );
    }
}
