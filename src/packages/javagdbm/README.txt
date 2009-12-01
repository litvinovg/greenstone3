A Java Interface to GDBM

*****
This is most of the Java gdbm download. I have left out some unnecessary bits for the greenstone version. The original home page for this is no longer available, but http://aurora.regenstrief.org/~schadow/dbm-java/ has links to the original tar file.
I have also updated the Makefiles so that it works on Windows and MacOSX.
[kjdon, 2005]
*****

Martin Pool <m.pool@pharos.com.au>  1998-03-12

 This is a Java interface to the GDBM database library written by Philip A Nelson. Like GDBM itself, this interface is free software, and comes with no warranty of any kind.

GDBM is a library of database functions for implementing a hash on disk. JavaGDBM is an API for the Java programmer, not an applet or database package for end users.

In Java terms, a GdbmFile is very similar to a java.util.Dictionary object, except that objects are reliably written to and retrieved from disk. An adaptor class, GdbmDictionary, presents that interface to maintain compatibility with existing code.

GDBM enforces file and object locking to allow databases to be safely shared between multiple threads or processes. A database may be open read-write by one process, or read-only by any number of processes. Within each process, any number of threads may share the database. Within the limits of the operating system and hardware, GDBM will ensure that changes are atomically committed to disk, making the database safe even if the program terminates abnormally.

JavaGDBM stores key/data pairs in a GdbmFile object, which represents a single disk file. Each key must be unique, and is paired with a single data item. The keys are not ordered: if you want to retrieve the objects in sorted order you can build an external index. The basic operations on

As for a Dictionary, the basic operations that may be performed on a GdbmFile are to fetch, test for existence, store and delete records.

Since Java objects are dynamically typed and GDBM data are typeless, JavaGDBM uses Strategy objects to allow objects to be encoded and decoded in various ways. Classes derived from the abstract Packing class may pack ojbects for storage as byte arrays, Unicode or ASCII strings, as serialized Java objects, or in a user-defined fashion. The packing strategy is defined at runtime for both the key and value of the records.

Some example code is available in the test harness GdbmTest.java.

The current implementation of JavaGDBM uses native glue code to link to the GDBM library via JNI, the Java Native Interface. This means that you must have a C compiler, the GDBM library, and related tools to compile the native portions JavaGDBM also requires a JDK1.1-compatible Java compiler, libraries, and runtime.

If the GDBM libraries didn't come with your system, then you can download the GDBM source and install it.

GDBM is known to work on Linux, NetBSD, Solaris, and AIX. 