/*
 * Copyright (C) 1997 by Pharos IP Pty Ltd
 * Confidential.  All rights reserved.
 * $Id$
 * $Source$
 *
 * ``No strings attached.''
 */

package au.com.pharos.util;

/**
 * Mixed bag of string manipulation functions.  It would be nice if
 * some of these were in <CODE>java.lang.String</CODE>, but since
 * they're not, they're implemented as static functions within this
 * class.
 *
 * Typical use:
 * <PRE>
 * String foo = "some string"
 * int cnt = StringOps.countOccurrences(foo, 's');
 * // --> cnt == 2
 * </PRE>
 *
 * @author Martin Pool
 * @version $Revision$, $Date$
 **/
public class StringOps
{
    /** This class contains only static public functions. **/
    private StringOps() { ; }

    /** Count the number of occurences of a character in a
     * string.
     *
     * @param needle the character to search
     * @param haystack string to be searched
     *
     * @return 0 if <EM>needle</EM> does not occur in <EM>haystack</EM>;
     * or a positive number indicating the number of occurrences
     **/
    static public int countOccurrences(String haystack, char needle)
    {
	byte[] hay = haystack.getBytes();
	int count = 0;
	for (int i = 0; i < hay.length; i++)
	    if (hay[i] == needle)
		count++;
	return count;
    }
}
