/*
 * module: pip/java/gdbm -- A Java interface to the GDBM library
 * class:  GdbmException -- An exception originating in the GDBM library
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

import java.io.IOException;

/** An exception originating in the GDBM library.
 *
 * @author Martin Pool
 * @version $Revision$ 
 * @see au.com.pharos.gdbm.GdbmFile
 */

public class GdbmException extends IOException
{
    // TODO: Perhaps provide more information about which particular
    // exception caused the problem?
    /** Constructs a GdbmException with the specified reason.
    **/
    public GdbmException(String reason)
    {
	super(reason);
    }
}


