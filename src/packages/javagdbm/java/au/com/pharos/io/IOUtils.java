/*
 * Copyright (C) 1997 by Pharos IP Pty Ltd
 * All rights reserved.
 * $Id$
 */

package au.com.pharos.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Plumbing utilities for manipulating Java IO classes.
 *
 * <P>These utilities really ought to be part of the
 * <CODE>java.io</CODE> package, but they're not.  Since they're
 * pretty useful, here they are.
 *
 * @version $Revision$
 * @author Martin Pool
 **/
public class IOUtils {
    /** Size of blocks to read during IO. **/
    static public final int BLOCK_SIZE = 4<<10;

    static public void pumpStream(InputStream inStream,
				  OutputStream outStream,
				  int contentLength)
	 throws IOException
    {
	int readBytes = 0;
	byte[] block = new byte[BLOCK_SIZE];
	int blockBytes;
	while (readBytes < contentLength
	       && (blockBytes = inStream.read(block)) > 0) {
	    readBytes += blockBytes;
	    outStream.write(block, 0, blockBytes);
	}
    }

    static public int pumpStream(InputStream inStream,
				 OutputStream outStream)
	 throws IOException
    {
	byte[] block = new byte[BLOCK_SIZE];
	int blockBytes, readBytes = 0;
	while ( (blockBytes = inStream.read(block)) > 0 ) {
	    outStream.write(block, 0, blockBytes);
	    readBytes += blockBytes;
	}
	return readBytes;
    }

    /** Reads the entire contents of the stream into a
     * newly-constructed byte array.
     **/
    static public byte[] readAll(InputStream inStream)
	 throws IOException
    {
	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	pumpStream(inStream, byteStream);
	byte[] buf = byteStream.toByteArray();
	byteStream.close();
	return buf;
    }
}
