/*
 *    OID.java
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

/** utility class to handle greenstone OIDs
 *
 * based around OIDtools.h from gsdl
 */
public class OID {
    
    /** returns everything up to the first dot 
      if no dot, returns oid */
    public static String getTop(String oid) {
      int pos = oid.indexOf('.');
      if (pos == -1) {
          return oid;
      }
      return oid.substring(0, pos);
      
    }
    /** returns true is oid is top level (ie has no dots) 
     returns false for an empty oid */
    public static boolean isTop(String oid) {
      if (oid.equals("")) {
          return false;
      }
      return (oid.indexOf('.')==-1);
    }
      
    /** returns the parent of oid (everything up to last dot) 
     returns oid if oid has no parent */
    public static String getParent(String oid) {
      int pos = oid.lastIndexOf('.');
      if (pos == -1) {
          return oid;
      }
      return oid.substring(0, pos);
    }

    /** returns the full name - replaces all " with parent */
    public static String translateParent(String oid, String parent) {
      return oid.replaceAll("\"", parent); 
    }
    /** does the opposite to translate_parent */
    public static String shrinkParent(String oid) {
      int pos = oid.lastIndexOf('.');
      if (pos==-1) return oid;
      return "\""+oid.substring(pos);
    }
    /** returns true if oid uses .fc, .lc, .pr, .ns, .ps .rt (root) .ss (specified sibling)*/
    public static boolean needsTranslating(String oid) {
      if (oid.length()<4) return false;
      String tail = oid.substring(oid.length()-3);
      return (tail.equals(".fc") || tail.equals(".lc") || 
            tail.equals(".pr") || tail.equals(".ns") || 
            tail.equals(".ps") || tail.equals(".rt") || 
            tail.equals(".ss") || tail.equals(".np") ||
            tail.equals(".pp"));
    }
    /** strips suffix from end */
    public static String stripSuffix(String oid) {
      String tail = oid.substring(oid.length()-3);
      while (tail.equals(".fc") || tail.equals(".lc") || 
             tail.equals(".pr") || tail.equals(".ns") || 
             tail.equals(".ps") || tail.equals(".ss") || 
             tail.equals(".np") || tail.equals(".pp") ||
             tail.equals(".rt") ) {
          if (tail.equals(".ss")) { // have doc.sibnum.ss
            oid = oid.substring(0, oid.length()-3);
            int pos = oid.lastIndexOf('.');
            //strip that too
            oid = oid.substring(0, pos);
          } 
          oid = oid.substring(0, oid.length()-3);
          tail = oid.substring(oid.length()-3);
      }
      
      return oid;
    }
    /** returns true if child is a child of parent 
     an oid is not a child of itself */
    public static boolean isChildOf(String parent, String child) {
      if (parent.equals(child)) {
          return false;
      }
      return child.startsWith(parent);
    }
}
