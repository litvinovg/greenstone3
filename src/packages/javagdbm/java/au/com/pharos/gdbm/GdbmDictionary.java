/*
 * module: pip/java/gdbm -- A Java interface to the GDBM library
 * class:  GdbmDictionary -- Wrap up a GDBM File for use as a Dictionary
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

package au.com.pharos.gdbm;

import java.util.Dictionary;
import java.util.Enumeration;

/** A GDBM database, presented as a Java dictionary.
 *
 * <P>This is a separate class which defers most operations to
 * GdbmFile because GdbmFile can throw IO exceptions, and because the
 * interface assumptions for Dictionary are not the same as those of
 * the IO library.  Perhaps they should be the same.
 *
 * <P>The Dictionary class will be partially replaced by the much
 * more elegant 
 * <A HREF="http://www.javasoft.com/products/jdk/preview/docs/guide/collections/overview.html">Collections API</A>
 * in JDK1.2, at which time a GDBM Collections interface will probably
 * be available.
 *
 * @see java.util.Dictionary
 * @see GdbmFile
 *
 * @author Martin Pool
 * @version $Revision$
 **/
// TODO: I wonder if this should be an inner class of GdbmFile?  It
// would bloat that source file a bit, but would (arguably) more
// accurately capture the intended design. -- mbp
public class GdbmDictionary extends Dictionary implements Closeable
{
    // XXX: This should probably become a private field, but on the other
    // hand it's probably useful to be able to apply GDBM-specific methods to
    // the underlying DB.  -- mbp
    GdbmFile db;

    public GdbmDictionary(GdbmFile db) {
	this.db = db;
    }

    private Exception caughtException;
    
    public Exception checkError() {
	return caughtException;
    }	

    public int size() {
	try {
	    return db.size();
	} catch (Exception e) {
	    caughtException = e;
	    return 0;
	}
    }

    public boolean isEmpty() {
	try {
	    return db.isEmpty();
	} catch (Exception e) {
	    caughtException = e;
	    return false;
	}
    }

    public Enumeration keys() {
	try {
	    return db.keys();
	} catch (Exception e) {
	    caughtException = e;
	    return null;
	}
    }

    public Enumeration elements() {
	// TODO: write an iterator class to do this
	throw new NoSuchMethodError();
    }

    public Object get(Object key) {
	try {
	    return db.fetch(key);
	} catch (Exception e) {
	    caughtException = e;
	    return null;
	}
    }

    public Object put(Object key, Object value)  {
	try {
	    Object oldValue = db.fetch(key);
	    db.store(key, value);
	    return oldValue;
	} catch (Exception e) {
	    caughtException = e;
	    return null;
	}
    }

    public Object remove(Object key) {
	try {
	    Object oldValue = db.fetch(key);
	    db.delete(key);
	    return oldValue;
	} catch (Exception e) {
	    caughtException = e;
	    return null;
	}
    }

    /** Write outstanding changes to the underlying database, and
     * close the database.
     *
     * @see au.com.pharos.gdbm.Closeable
     **/
    public void close() {
	try {
	    if (db != null && db.isOpen())
		db.close();
	    db = null;
	} catch (GdbmException e) {
	    ;
	}
    }

    /** Write outstanding changes to the underlying database, but
     * leave the database open.
     *
     * @see au.com.pharos.gdbm.Closeable
     **/
    public void sync() {
	try {
	    db.sync();
	} catch (GdbmException e) {
	    ;
	}
    }
}
