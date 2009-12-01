/*
 * Copyright (C) 1997 Pharos IP Pty Ltd
 * Confidential.  All rights reserved.
 * $Id$
 */

package au.com.pharos.gdbm;

import java.io.IOException;

/** A Closeable object is one having a connection to an underlying
 * data store that may be flushed or closed.
 *
 * <P>This interface is intended to complement the Dictionary and
 * Collection interfaces by reflecting the fact that many such objects
 * may be closed.
 *
 * @author Martin Pool
 * @version $Revision$
 **/
public interface Closeable {
    /** Write outstanding changes to the underlying database, and
     * break the connection, freeing any associated resources or
     * locks.  This generally does not imply that the data will be
     * lost, merely that it will be released.  If this is not
     * meaningful for a particular Closeable object, then it should be
     * a no-op.
     *
     * @exception java.io.IOException passed up from the underlying
     * database.
     **/
    public void close() throws IOException;

    /** Write outstanding changes to the underlying database, but
     * leave the database open.  If this is not meaningful for a
     * particular Closeable object, then it should be a no-op.
     *
     * @exception java.io.IOException passed up from the underlying
     * database.
     **/
    public void sync() throws IOException;
}
