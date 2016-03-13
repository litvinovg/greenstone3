/*
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

import au.com.pharos.packing.Packing;
import au.com.pharos.packing.RawPacking;

/** Java interface to a GDBM database table.
 *
 * <P>This database is a simple on-disk hash table.
 *
 * <P>Both the hash keys and values
 * are binary strings of any length.  They are converted to and
 * from Java objects using customizable packing strategies.
 *
 * <P>The implementation of this class consists of two levels: a
 * Java-level interface, and a set of private native functions
 * implemented in C.  As much functionality as possible is implemented
 * in Java: the C functions generally just marshal the information for
 * presentation to the native GDBM library.
 *
 * <P>The native library <CODE>gdbmjava</CODE> must be available
 * for dynamic loading in a system-dependant manner.
 *
 * <p>See the GDBM documentation file, and the
 * <A HREF="http://www.pharos.com.au/gdbm/">JavaGDBM home page</A>
 * for more information.
 *
 * @see au.com.pharos.packing.Packing
 * @see au.com.pharos.gdbm.GdbmTest
 * @see au.com.pharos.gdbm.GdbmDictionary
 *
 * @author Martin Pool
 * @version $Revision$
 **/

public class GdbmFile implements Closeable {
    /** The GDBM handle for the database file, or 0 if the database has
     * been closed.  Java doesn't understand it, but it stores it and
     * passes it to the C routines. */
    private transient volatile long dbf;

    // Remember the parameters used to open the database
    private int openFlags;
    private String dbFilename;

    // -----------------------------------------------------------------
    // Constructors

    // Values must match those from gdbm.h !
    /** Indicates that the caller will just read the database.  Many
     * readers may share a database. */
    public final static int READER = 0;

    /** The caller wants read/write access to an existing database and
     * requires exclusive access. */
    public final static int WRITER = 1;

    /** The caller wants exclusive read/write access, and the database
     * should be created if it does not already exist. */
    public final static int WRCREAT = 2;

    /** The caller wants exclusive read/write access, and the database
     * should be replaced if it already exists. */
    public final static int NEWDB = 3;

    /** Flag indicating GDBM should write to the database without disc
     * synchronization.  This allows faster writes, but may produce an
     * inconsistent database in the event of abnormal termination of
     * the writer. */
    public final static int FAST = 16;

    // TODO: Allow FAST mode to be toggled

    /** Creates a new GdbmFile object representing an disk database.  The
     * database is opened or created on disk.
     *
     * <P>Both key and value strategies default to
     * RawPacking, meaning that the database will use raw byte arrays
     * for both key and value.
     *
     * <P>TODO: Allow the caller to specify the block size and/or
     * cache size.
     *
     * @param fileName the disk filename in which the data will be stored.
     *
     * @param flags any of READER, WRITER, WRCREAT, and NEWDB, optionally
     * ORed with FAST
     *
     * @exception GdbmException if the database cannot be opened or
     * created.
     *
     * @see GdbmFile#READER
     * @see GdbmFile#WRITER
     * @see GdbmFile#WRCREAT
     * @see GdbmFile#NEWDB
     * @see GdbmFile#FAST
     */
    public GdbmFile(String fileName, int flags)
	 throws GdbmException
    {
	openFlags = flags;
	dbFilename = fileName;
	keyPacking = new RawPacking();
	valuePacking = new RawPacking();
	dbf = gdbm_open(fileName, flags);
    }

    /** Close the database file if it is still open.
     *
     * <P><B>Note</B> that the disk file is locked against access from
     * other processes while it is open.  To prevent contention, it
     * may be useful to explicitly close the file rather than waiting
     * for the garbage-collector.
     *
     * @see GdbmFile#isOpen()
     **/
    public synchronized void close() throws GdbmException
    {
	if (dbf != 0)
	    gdbm_close(dbf);
	dbf = 0;		// No longer connected
    }

    /** Close the database when the GdbmFile is garbage-collected.
     *
     * @see GdbmFile#close()
     */
    public void finalize() throws GdbmException
    {
	close();
    }

    /* The Gdbm file is unlocked when the process terminates, but
     * nevertheless it would be good to close it in finalization, in
     * case the OS process continues after the JVM terminates.
     *
     * XXX: Unfortunately, anybody else can turn this off again: it
     * would be nice if there was a way to avoid this. */
    static {
	System.runFinalizersOnExit(true);
    }



    // -----------------------------------------------------------------
    // Packing strategies
    private Packing keyPacking, valuePacking;

    /** Set the object to be used as the packing strategy for
     * converting key objects to and from the byte arrays stored in
     * the database.
     *
     * <P>Depending on the class of Java object used as the key of your
     * database, it may be appropriate to use a packing strategy from
     * the
     * <A HREF="Package-au.com.pharos.packing.html"><CODE>au.com.pharos.packing</CODE></A>
     * package, or to define a new subclass of one of those strategies.
     *
     * @see au.com.pharos.packing.Packing
     * @see GdbmFile#setValuePacking(au.com.pharos.packing.Packing)
     **/
    public void setKeyPacking(Packing newPacking) {
	keyPacking = newPacking;
    }

    /** Set the object to be used as the packing strategy for
     * converting value objects to and from the byte arrays stored
     *  in the database.
     *
     * <P>Depending on the class of Java object used as the key of your
     * database, it may be appropriate to use a packing strategy from
     * the
     * <A HREF="Package-au.com.pharos.packing.html"><CODE>au.com.pharos.packing</CODE></A>
     * package, or to define a new subclass of one of those strategies.
     *
     * @see au.com.pharos.packing.Packing
     * @see GdbmFile#setKeyPacking(au.com.pharos.packing.Packing )
     **/
    public void setValuePacking(Packing newPacking) {
	valuePacking = newPacking;
    }

    // -----------------------------------------------------------------

    /** Returns a string indicating the version of the underlying GDBM
     * library.
     *
     * @return the version of the native GDBM library.
     **/
    public static String getLibraryVersion()
    {
	return gdbm_getversion();
    }

    /** Return a string indicating the version of the JavaGDBM library
     * wrapper.
     *
     * <P>The most current release is available from the
     * <A HREF="http://www.pharos.com.au/javagdbm/">JavaGDBM home page</A>.
     *
     * @return the release number of the JavaGDBM library
     **/
    public static String getWrapperVersion()
    {
	return gdbm_wrapperVersion();
    }

    /** Indicate whether the database is writable.
     *
     * <P>Databases are opened in either read-write or read-only mode,
     * and remain in that mode until they are closed.
     *
     * @return true if the database may be written; otherwise false.
     *
     * @see GdbmFile#GdbmFile(java.lang.String, int)
     **/
    public boolean isWritable()
    {
	return (openFlags & 0x03) != READER;
    }

    /** Indicate whether the database is open or not.
     *
     * <P>A database is open from the point of creation until close()
     * is called, if ever, after which it is closed.
     *
     * @return false if the database has been closed; otherwise true.
     *
     * @see GdbmFile#close()
     **/
    public boolean isOpen()
    {
	return dbf != 0;
    }


    /** Compact the database file.
     *
     * <P>If you have had a lot of deletions and would like to shrink the
     * space used by the GDBM file, this function will reorganize the
     * database.  GDBM will not shorten the length of a GDBM file
     * (deleted file space will be reused) except by using this
     * reorganization, though it will reuse the vacant space.
     *
     * <P>The database must be writable.
     */
    public void reorganize()
	 throws GdbmException
    {
	gdbm_reorganize(dbf);
    }
    

    /** Flush changes to disk.
     *
     * <P>This function is only required when the database is
     * opened with the FAST flag set.  By default, changes are
     * flushed to disk after every update.
     *
     * <P>The database must be writable.
     *
     * @see GdbmFile#FAST
     **/
    public void sync()
	 throws GdbmException
    {
	gdbm_sync(dbf);
    }



    /** Retrieve the value corresponding to a particular key.
     *
     * @param key the key of the record to be retrieved
     *
     * @return the value of the record with the specified key; or
     * null if the key is not present in the database.
     */
    public Object fetch(Object key) throws GdbmException
    {
	byte[] keyBytes = keyPacking.toBytes(key);
	return valuePacking.fromBytes(gdbm_fetch(dbf, keyBytes));
    }


    /** Indicate whether the specified key is in the database,
     * without returning the value.
     *
     * @param key the key of the record to be retrieved
     *
     * @return true if a record with the specified key is present;
     * otherwise false.
     */
    public boolean exists(Object key) throws GdbmException
    {
	byte[] keyBytes = keyPacking.toBytes(key);
	return gdbm_exists(dbf, keyBytes);
    }



    /** Store a value in the database, replacing any existing value
     * with the same key.
     *
     * @param key key under which to store the value
     *
     * @param value value to be stored
     *
     * @exception GdbmException if the object is a reader; or
     * if an IO error occurs
     **/
    public void store(Object key, Object value)
	 throws GdbmException
    {
	byte[] keyBytes = keyPacking.toBytes(key);
	byte[] valueBytes = valuePacking.toBytes(value);
	gdbm_store(dbf, keyBytes, valueBytes, true);
    }


    
    /** Store a value in the database, unless a record with the same
     * key already exists.
     *
     * @param key key under which to store the value.
     *
     * @param value value to be stored.
     *
     * @exception GdbmException if a record with the specified key
     * already exists; or if the object is a reader; or
     * if an IO error occurs
     */
    public void storeNoReplace(Object key, Object value)
	 throws GdbmException
    {
	byte[] keyBytes = keyPacking.toBytes(key);
	byte[] valueBytes = valuePacking.toBytes(value);
 	gdbm_store(dbf, keyBytes, valueBytes, false);
    }


    
    /** Remove a record from the database.
     *
     * @param key key of the record to be removed
     *
     * @exception GdbmException if there is no record with the
     * specified key; or if the object is a reader; or if an IO error
     * occurs.
     **/
    public void delete(Object key)
	 throws GdbmException
    {
	byte[] keyBytes = keyPacking.toBytes(key);
	gdbm_delete(dbf, keyBytes);
    }





    // -----------------------------------------------------------------
    // Iterator support

    // TODO: Use a flag/timestamp approach to throw an exception if
    // somebody modifies the database while they're trying to iterate
    // over it?  Approach suggested by Doug Lea's Collections API. --
    // mbp

    /** Return an enumeration which will return all of the keys for the
     * database of the file in (apparently) random order.
     *
     * <P><B>Caution:</B> If the database is modified while
     * an enumeration is in progress,
     * then changes in the hash table may cause the enumeration to
     * miss some records.
     *
     * @see java.util.Enumeration
     **/
    public Enumeration keys() throws GdbmException
    {
	// Return an inner class which will do the iteration in the
	// context of this GdbmFile object.
	return new KeyEnumeration();
    }

    // Synchronization is performed in the GdbmFile's methods
    class KeyEnumeration implements Enumeration {
	private byte[] currKey, nextKey;

	KeyEnumeration() throws GdbmException {
	    currKey = null;
	    nextKey = getFirstKeyRaw();
	}

	public boolean hasMoreElements() {
	    return nextKey != null;
	}

	public Object nextElement() throws NoSuchElementException {
	    try {
		currKey = nextKey;
		if (currKey == null)
		    throw new NoSuchElementException();
		else
		    nextKey = getNextKeyRaw(currKey);
		return keyPacking.fromBytes(currKey);
	    } catch ( GdbmException e ) {
		// Can't propagate this through java.util.Enumeration, dammit
		throw new NoSuchElementException();
	    }
	}
    }

    /** Start a visit to all keys in the hashtable, using raw data values.
     *
     * @return the first key in the hash table as a byte array; or null if
     * the database is empty. */
    byte[] getFirstKeyRaw() throws GdbmException
    {
	return gdbm_firstkey(dbf);
    }

    /** Return the first record in the hashtable.
     *
     * <P>Note that the database
     * is not ordered, so the key returned is simply the first in the
     * hashtable and effectively randomly selected.
     *
     * @return the first key in the hash table; or null if
     * the database is empty.
     *
     * @see GdbmFile#getNextKey(java.lang.Object)
     * @see GdbmFile#keys()
     **/
    Object getFirstKey() throws GdbmException
    {
	return keyPacking.fromBytes(getFirstKeyRaw());
    }


    /** Find and read the next key in the hashtable.
     *
     * @return the next key; or null if <EM>keyBytes</EM> is the last key
     * in the hashtable.
     **/
    byte[] getNextKeyRaw(byte[] keyBytes) throws GdbmException
    {
	return gdbm_nextkey(dbf, keyBytes);
    }


    /** Check whether the database is empty.
     *
     * @return true if the database contains no records; otherwise
     * false.
     **/
    public boolean isEmpty() throws GdbmException
    {
	return getFirstKeyRaw() == null;
    }


    /** Count the records in the database.
     *
     * <P>This is implemented by iterating over the database, and so is
     * a fairly expensive operation.
     *
     * <P>This method locks the database to make sure the count is
     * accurate.
     *
     * @return the number of records in the database
     * @see GdbmFile#isEmpty()
     **/
    public synchronized int size() throws GdbmException
    {
	int s = 0;
	byte[] key = getFirstKeyRaw();
	while (key != null) {
	    s++;
	    key = getNextKeyRaw(key);
	}
	return s;
    }


    static {
	String libraryFile = System.getProperty
	    ("au.com.pharos.gdbm.libraryFile");

	if (libraryFile != null) {
	    System.load(libraryFile);
	} else {
	    
	    String gsdlos = System.getenv("GSDLOS");
	    if (gsdlos!=null && gsdlos.equals("darwin")) {
		// As of MacOX 10.11 (El Capitan), effectivly supresses DYLD_LIBRARY_PATH (does
		// not propagate it to child processes).  This is a result of changes to their
		// security model, and seems to come into effect for 'untrusted' executables.
		// Greenstone run as a regular user, is 'unstrusted'.  It is possible, with
		// admin rights, to override this, however that is not really a viable solution
		// for our project.  Hence the change here to use Systen.load() with an
		// absolute pathname, rather than rely of System.loadLibrary().

		String gsdl3srchome = System.getenv("GSDL3SRCHOME");
		String full_jni_library = gsdl3srchome + "/lib/jni/libgdbmjava.jnilib";
		System.load(full_jni_library);
	    }
	    else {

		System.loadLibrary("gdbmjava");
	    }
	}
    }

    private synchronized native long
    gdbm_open(String fileName, int flags)
	 throws GdbmException;

    private synchronized native void
    gdbm_close(long dbf);

    private synchronized native void
    gdbm_store(long dbf,
	       byte[] key,
	       byte[] content,
	       boolean replace);

    private synchronized native byte[]
    gdbm_fetch(long dbf,
	       byte[] key);

    private synchronized native boolean
    gdbm_exists(long dbf,
		byte[] key);

    private synchronized native void
    gdbm_delete(long dbf,
		byte[] key);

    private synchronized native byte[]
    gdbm_firstkey(long dbf)
	 throws GdbmException;

    private synchronized native byte[]
    gdbm_nextkey(long dbf, byte[] key)
	 throws GdbmException;

    private synchronized static native String
    gdbm_getversion();

    private synchronized static native String
    gdbm_wrapperVersion();

    private synchronized native void
    gdbm_reorganize(long dbf);

    private synchronized native void
    gdbm_sync(long dbf);
}
