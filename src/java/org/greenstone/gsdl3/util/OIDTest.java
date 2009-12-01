/*
 *    OIDTest.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import junit.framework.*; 

public class OIDTest extends TestCase {

    /** what type of test - used by the testing framework */
    public static final String TEST_ALL_TEST_TYPE="UNIT";

    private static final String empty = "";
    private static final String oid1="HASH123";
    private static final String oid2="HASH123.1";
    private static final String oid3="HASH123.1.2";
    private static final String oidfc = oid1+".fc";
    private static final String oidlc = oid1+".lc";
    private static final String oidpr = oid1+".pr";
    private static final String oidns = oid1+".ns";
    private static final String oidps = oid1+".ps";

    public OIDTest(String name) {
      super(name);
    }

    /** test suite that dynamically runs all the tests */
    public static Test suite() {
      return new TestSuite(OIDTest.class);
    }

    public void testGetTop() {
      assertEquals(oid1, OID.getTop(oid1));
      assertEquals(oid1, OID.getTop(oid2));
      assertEquals(empty, OID.getTop(empty));
    }
  
    public void testIsTop() {
      assertTrue(!OID.isTop(empty));
      assertTrue(OID.isTop(oid1));
      assertTrue(!OID.isTop(oid2));
    }

    public void testGetParent() {
      assertEquals(oid1, OID.getParent(oid1));
      assertEquals(oid1, OID.getParent(oid2));
      assertEquals(empty, OID.getParent(empty));
    }

    public void testTranslateParent() {
      String short_id = "\".1";
      assertEquals(oid2, OID.translateParent(short_id, oid1));
      // should return the original oid if its not a shortened form
      assertEquals(oid2, OID.translateParent(oid2, oid1));
    }

    public void testShrinkParent() {
      String short_oid2 = "\".1";
      String short_oid3 = "\".2";

      assertEquals(oid1, OID.shrinkParent(oid1));
      assertEquals(short_oid2, OID.shrinkParent(oid2));
      assertEquals(short_oid3, OID.shrinkParent(oid3));
    }

    public void testNeedsTranslating() {
      assertTrue(!OID.needsTranslating(oid1));
      assertTrue(!OID.needsTranslating(oid2));
      assertTrue(OID.needsTranslating(oidfc));
      assertTrue(OID.needsTranslating(oidlc));
      assertTrue(OID.needsTranslating(oidpr));
      assertTrue(OID.needsTranslating(oidns));
      assertTrue(OID.needsTranslating(oidps));

    }

    public void testStripSuffix() {
      assertEquals(oid1, OID.stripSuffix(oidfc));
      assertEquals(oid1, OID.stripSuffix(oidlc));
      assertEquals(oid1, OID.stripSuffix(oidpr));
      assertEquals(oid1, OID.stripSuffix(oidns));
      assertEquals(oid1, OID.stripSuffix(oidps));
    }

    public void testIsChildOf() {
      assertTrue(OID.isChildOf(oid1, oid2));
      assertTrue(OID.isChildOf(oid1, oid3));
      assertTrue(OID.isChildOf(oid2, oid3));
      assertTrue(!OID.isChildOf(oid3, oid1));
      assertTrue(!OID.isChildOf(oid1, oid1));
    }

}
