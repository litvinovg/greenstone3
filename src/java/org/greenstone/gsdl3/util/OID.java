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

import org.apache.log4j.*;

/** utility class to handle greenstone OIDs
 *
 * based around OIDtools.h from gsdl
 */
public class OID {

    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.OID.class.getName());

    public interface OIDTranslatable {
	public String processOID(String doc_id, String top, String suffix, int sibling_num);
    }
    
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

    
  /** translates relative oids into proper oids:
   * .pr (parent), .rt (root) .fc (first child), .lc (last child),
   * .ns (next sibling), .ps (previous sibling) 
   * .np (next page), .pp (previous page) : links sections in the order that you'd read the document
   * a suffix is expected to be present so test before using
   * Other classes can implement the method processOID of interface OIDTranslatable
   * to process the oid further to handle siblings.
   */
    public static String translateOID(OIDTranslatable translator, String oid) {
    int p = oid.lastIndexOf('.');
    if (p != oid.length()-3) {
      logger.info("translateoid error: '.' is not the third to last char!!");
      return oid;
    }
	
    String top = oid.substring(0, p);
    String suff = oid.substring(p+1);

    // just in case we have multiple extensions, we must translate
    // we process inner ones first
    if (OID.needsTranslating(top)) {
	top = OID.translateOID(translator, top);
    }
    if (suff.equals("pr")) {
      return OID.getParent(top);
    } 
    if (suff.equals("rt")) {
      return OID.getTop(top);
    } 
    if (suff.equals("np")) {
      // try first child

      String node_id = OID.translateOID(translator, top+".fc");
      if (!node_id.equals(top)) {
	  return node_id;
      }

      // try next sibling
      node_id = OID.translateOID(translator, top+".ns");
      if (!node_id.equals(top)) {
	  return node_id;
      }
      // otherwise we keep trying parents sibling
      String child_id = top;
      String parent_id = OID.getParent(child_id);
      while(!parent_id.equals(child_id)) {
	node_id = OID.translateOID(translator, parent_id+".ns");
	if (!node_id.equals(parent_id)) {
	  return node_id;
	}
	child_id = parent_id;
	parent_id = OID.getParent(child_id);
      }
      return top; // we couldn't get a next page, so just return the original
    } 
    if (suff.equals("pp")) {
      String prev_sib = OID.translateOID(translator, top+".ps");
      if (prev_sib.equals(top)) {
	// no previous sibling, so return the parent
	return OID.getParent(top);
      }
      // there is a previous sibling, so it's either this section, or the last child of the last child
      String last_child = OID.translateOID(translator, prev_sib+".lc");
      while (!last_child.equals(prev_sib)) {
	prev_sib = last_child;
	last_child = OID.translateOID(translator, prev_sib+".lc");
      }
      return last_child;
    } 
	
    int sibling_num = 0;
    if (suff.equals("ss")) {
      // we have to remove the sib num before we get top
      p = top.lastIndexOf('.');
      sibling_num = Integer.parseInt(top.substring(p+1));
      top = top.substring(0, p);
    }
	
    // need to get info out of Fedora
    String doc_id = top;
    if (suff.endsWith("s")) {
      doc_id = OID.getParent(top);
      if (doc_id.equals(top)) {
	// i.e. we are already at the top
	return top;
      }
    }
    
    return translator.processOID(doc_id, top, suff, sibling_num);

  }
}
