/*
 * module: pip/jni/gdbm -- A Java interface to the GDBM library
 * file:   gdbmjava.c -- Native parts of the au.com.pharos.gdbm.GdbmFile
 *             Java class
 *
 * Copyright (C) 1997 Pharos IP Pty Ltd
 *
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

/* NOTE ON MEMORY MANAGEMENT: GDBM always mallocs memory and expects
 * the caller to free it.  Java requires native functions to interact
 * with it to lock and unlock buffers. */

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if defined (__WIN32__)
#include <gdbmdefs.h>
#include <gdbmerrno.h>
#define GDBM_FILE gdbm_file_info *
extern char* gdbm_version;
#else
#include <gdbm.h>
#endif

#include <jni.h>

#include <GdbmFile.h>

#ifdef DEBUG
#define ASSERT(x) if (!(x)) { \
  fprintf(stderr, "%s:%d: assertion failed\n", __FILE__, __LINE__); \
  abort(); }
#else /* !DEBUG */
#define ASSERT(x) 
#endif /* !DEBUG */

/* The Java class within which the native methods are declared */
#define JAVA_CLASS "au.com.pharos.gdbm.GdbmFile"

#define NULL_PTR_EXCEPTION(env) nullPtrException(env, __FILE__, __LINE__)
#define GDBM_EXCEPTION(env) gdbmException(env, __FILE__, __LINE__)

#define CHECK_NOT_NULL(ptr, env) if (!ptr) { NULL_PTR_EXCEPTION(env); return 0; }
#define CHECK_NOT_NULL_VOID(ptr, env) if (!ptr) { NULL_PTR_EXCEPTION(env); return; }

void gdbmException(JNIEnv *env, const char *file, int line);
void nullPtrException(JNIEnv *env, const char *file, int line);

void releaseArray(JNIEnv *env, jbyteArray array, datum *fromDatum);
void releaseArrayAbort(JNIEnv *env, jbyteArray array, datum *fromDatum);
int arrayToDatum(JNIEnv *env, jbyteArray fromArray, datum *toDatum);
jbyteArray datumToArray(JNIEnv *env, datum *fromDatum);

/* Convert between a jlong and a void ptr using a well-defined cast.
 * (Casting between a pointer and an integer of different sizes spooks
 * both gcc and mbp. */
#if (SIZEOF_VOID_P == SIZEOF_LONG)
#  define DBF_TO_JLONG(x) ((jlong)((long) x))
#  define JLONG_TO_DBF(x) ((GDBM_FILE)((long) x))
#elif (SIZEOF_VOID_P == SIZEOF_INT)
#  define DBF_TO_JLONG(x) ((jlong)((int) (x)))
#  define JLONG_TO_DBF(x) ((GDBM_FILE)((int) (x)))
#else
#  define DBF_TO_JLONG(x) ((jlong)(x))
#  define JLONG_TO_DBF(x) ((GDBM_FILE)(x))
#endif


JNIEXPORT jlong JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1open
    (JNIEnv *env, jobject obj, jstring fileName, jint flags)
{
    GDBM_FILE dbf;
    const char *utfFileName;
   
    utfFileName = (*env)->GetStringUTFChars(env, fileName, 0);
    if (!utfFileName) 
	return 0;
    
    setbuf(stderr, 0);
   
    /* XXX: Should we let the caller specify the file mode?  I think
     * not -- Java is above the level of octal file modes. [mbp] */
    /* Couldn't get it to work properly on Windows without the 0 here - 
       wouldn't allow mulitple READs on a single file [kjdon] */
#if defined (__WIN32__) 
    dbf = gdbm_open((char *) utfFileName, 0, flags, 0660, NULL, 0);
#else
    dbf = gdbm_open((char *) utfFileName, 0, flags, 0660, NULL);
#endif
    if (utfFileName)
	(*env)->ReleaseStringUTFChars(env, fileName, utfFileName);

    if (!dbf) {
	GDBM_EXCEPTION(env);
	return 0;
    }	
   
    return DBF_TO_JLONG(dbf);
}





JNIEXPORT jbyteArray JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1firstkey
(JNIEnv *env, jobject obj, jlong dbf)
{
    datum	keyDatum;
    jbyteArray 	keyArray;

    CHECK_NOT_NULL(dbf, env);

    keyDatum = gdbm_firstkey(JLONG_TO_DBF(dbf));
    if ( gdbm_errno != GDBM_NO_ERROR ) {
	GDBM_EXCEPTION(env);
	return 0;
    }
    if ( !keyDatum.dptr )
	return 0;

    keyArray = datumToArray(env, &keyDatum);
    free(keyDatum.dptr); 

    return keyArray;
}





JNIEXPORT jbyteArray JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1nextkey
(JNIEnv *env, jobject this, jlong dbf, jbyteArray keyArray)
{
    datum	keyDatum;
    datum	nextDatum;
    jbyteArray	nextArray;

    CHECK_NOT_NULL(dbf, env);

    if (!arrayToDatum(env, keyArray, &keyDatum)) {
	NULL_PTR_EXCEPTION(env);
	return 0;
    }
    
    nextDatum = gdbm_nextkey(JLONG_TO_DBF(dbf), keyDatum);
    releaseArrayAbort(env, keyArray, &keyDatum);

    if ( gdbm_errno != GDBM_NO_ERROR ) {
	GDBM_EXCEPTION(env);
	return 0;
    }
    if ( !nextDatum.dptr )
	return 0;

    nextArray = datumToArray(env, &nextDatum);
    free(nextDatum.dptr);
    
    return nextArray;
}






JNIEXPORT jbyteArray JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1fetch
  (JNIEnv *env, jobject this, jlong dbf, jbyteArray keyArray)
{
    datum 	keyDatum;
    datum 	valueDatum;
    jbyteArray	valueArray;

    CHECK_NOT_NULL(dbf, env);

    if (!arrayToDatum(env, keyArray, &keyDatum)) {
	NULL_PTR_EXCEPTION(env);
	return 0;
    }

    valueDatum = gdbm_fetch(JLONG_TO_DBF(dbf), keyDatum);
    releaseArrayAbort(env, keyArray, &keyDatum);

    if ( !valueDatum.dptr )
	return 0;
    if ( gdbm_errno != GDBM_NO_ERROR ) {
	GDBM_EXCEPTION(env);
	return 0;
    }

    valueArray = datumToArray(env, &valueDatum);
    free(valueDatum.dptr);

    return valueArray;
}



JNIEXPORT jboolean JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1exists
  (JNIEnv *env, jobject obj, jlong dbf, jbyteArray keyArray)
{
    datum 	keyDatum;
    int		result;

    CHECK_NOT_NULL(dbf, env);
    if (!arrayToDatum(env, keyArray, &keyDatum)) {
	NULL_PTR_EXCEPTION(env);
	return JNI_FALSE;
    }
    result = gdbm_exists(JLONG_TO_DBF(dbf), keyDatum);
    if ( gdbm_errno != GDBM_NO_ERROR ) {
	GDBM_EXCEPTION(env);
	return 0;
    }
    releaseArrayAbort(env, keyArray, &keyDatum);
    return result ? JNI_TRUE : JNI_FALSE;
}





JNIEXPORT void JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1store
  (JNIEnv *env, jobject obj, jlong dbf, 
   jbyteArray keyArray, jbyteArray valueArray, jboolean replace)
{
    datum	keyDatum;
    datum	valueDatum;
    
    CHECK_NOT_NULL_VOID(dbf, env);
    
    if ( !arrayToDatum(env, keyArray, &keyDatum)
	 || !arrayToDatum(env, valueArray, &valueDatum) ) {
	NULL_PTR_EXCEPTION(env);
	return;
    }

    gdbm_store(JLONG_TO_DBF(dbf), keyDatum, valueDatum, 
	       replace ? GDBM_REPLACE : 0);

    releaseArrayAbort(env, keyArray, &keyDatum);
    releaseArrayAbort(env, valueArray, &valueDatum);

    if ( gdbm_errno != GDBM_NO_ERROR )
	GDBM_EXCEPTION(env);
}



JNIEXPORT void JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1delete
   (JNIEnv *env, jobject obj, jlong dbf, jbyteArray keyArray)
{
    datum 	keyDatum;

    CHECK_NOT_NULL_VOID(dbf, env);

    if (!arrayToDatum(env, keyArray, &keyDatum)) {
	NULL_PTR_EXCEPTION(env);
	return;
    }

    gdbm_delete(JLONG_TO_DBF(dbf), keyDatum);

    releaseArrayAbort(env, keyArray, &keyDatum);
    
    if ( gdbm_errno != GDBM_NO_ERROR )
	GDBM_EXCEPTION(env);
}



JNIEXPORT jstring JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1getversion
  (JNIEnv *env, jclass cls)
{
    return (*env)->NewStringUTF(env, gdbm_version);
}


JNIEXPORT jstring JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1wrapperVersion
  (JNIEnv *env, jclass cls)
{
    return (*env)->NewStringUTF(env, "JavaGDBM release 0005 built " __DATE__);
}
				



JNIEXPORT void JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1reorganize
  (JNIEnv *env, jobject obj, jlong dbf)
{
    CHECK_NOT_NULL_VOID(dbf, env);

    gdbm_reorganize(JLONG_TO_DBF(dbf));

    if ( gdbm_errno != GDBM_NO_ERROR )
	GDBM_EXCEPTION(env);
}



JNIEXPORT void JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1close
  (JNIEnv *env, jobject obj, jlong dbf)
{
    CHECK_NOT_NULL_VOID(dbf, env);

    gdbm_close(JLONG_TO_DBF(dbf));

    if ( gdbm_errno != GDBM_NO_ERROR )
	GDBM_EXCEPTION(env);
}



JNIEXPORT void JNICALL 
Java_au_com_pharos_gdbm_GdbmFile_gdbm_1sync
  (JNIEnv *env, jobject obj, jlong dbf)
{
    CHECK_NOT_NULL_VOID(dbf, env);

    gdbm_sync(JLONG_TO_DBF(dbf));

    if ( gdbm_errno != GDBM_NO_ERROR )
	GDBM_EXCEPTION(env);
}




/**********************************************************************
 *
 * Following are support functions which aid in interfacing C to 
 * Java. */

/** Create a new Java byte array from a GDBM datum, and return a
 * pointer thereto.  */

jbyteArray datumToArray(JNIEnv *env, datum *fromDatum) 
{
    jbyteArray toArray;

    if (!fromDatum || !fromDatum->dptr)
	return 0;

    toArray = (*env)->NewByteArray(env, fromDatum->dsize);
    ASSERT(toArray);
    (*env)->SetByteArrayRegion(env, toArray, 
			       0, fromDatum->dsize, fromDatum->dptr);
    
    return toArray;
}

/** Convert a Java byte array to a GDBM datum.
 *
 * The Java array is pinned or copied for use in the datum, and must
 * be released after use by releaseBytes. 
 *
 * Returns true if the array is non-null and could be pinned.  Otherwise,
 * returns false. 
 */
int arrayToDatum(JNIEnv *env, jbyteArray fromArray, datum *toDatum)
{
    if (fromArray) {
	toDatum->dptr = (*env)->GetByteArrayElements(env, fromArray, 0);
	toDatum->dsize = (*env)->GetArrayLength(env, fromArray);
	return (int) toDatum->dptr;
    }
    else 
	return 0;
}

/** Release a byte array pinned or copied for use in a datum. */
void releaseArray(JNIEnv *env, jbyteArray array, datum *fromDatum) 
{
    ASSERT(fromDatum->dptr);
    (*env)->ReleaseByteArrayElements(env, array, fromDatum->dptr, 0);
    fromDatum->dptr = 0;	/* no longer valid */
}

/** Release a byte array pinned or copied for use in a datum, aborting 
 * any changes.  This potentially saves the runtime from having to 
 * copy back an unchanged array. */
void releaseArrayAbort(JNIEnv *env, jbyteArray array, datum *fromDatum) 
{
    ASSERT(fromDatum->dptr);
    (*env)->ReleaseByteArrayElements(env, array, fromDatum->dptr, 
				     JNI_ABORT);
    fromDatum->dptr = 0;	/* no longer valid */
}


/** Throw a null pointer exception. */
void nullPtrException(JNIEnv *env, const char *file, int line) 
{
    jclass exClass;
    char reason[1024];
    sprintf(reason, "Null pointer exception in GDBM wrapper (%s:%d)",
	    file, line);
    
    exClass = (*env)->FindClass(env, "java/lang/NullPointerException");
    ASSERT(exClass);

    (*env)->ThrowNew(env, exClass, reason);
}

/** Translate the GDBM error into throw a Java exception, and throw
 * same. 
 *
 * TODO: Throw different classes of exceptions depending on what the 
 * underlying error is. 
 */
void gdbmException(JNIEnv *env, const char *file, int line) {
    jclass exClass;
    static char reason[1500];
    static char srcLocation[500];

    exClass = (*env)->FindClass(env, "au/com/pharos/gdbm/GdbmException");
    ASSERT(exClass);

    strncpy(reason, gdbm_strerror(gdbm_errno), 500);
    sprintf(srcLocation, " (%s:%d)", file, line);

    /* If the error code suggests that an OS or stdio error may have occurred,
     * include supplementary information from errno. */
    switch (gdbm_errno) {
    case GDBM_FILE_OPEN_ERROR:
    case GDBM_FILE_WRITE_ERROR:
    case GDBM_FILE_SEEK_ERROR:
    case GDBM_FILE_READ_ERROR:
    case GDBM_MALLOC_ERROR:
    case GDBM_REORGANIZE_FAILED:
	strcat(reason, ": ");
	strncat(reason, strerror(errno), 490);
	strcat(reason, "?");
    default:
	/* errno is probably not useful */
	;
    }
    
    strncat(reason, srcLocation, 495);
    gdbm_errno = GDBM_NO_ERROR;	/* this one has been handled */
    (*env)->ThrowNew(env, exClass, reason);    
}

/* 
 * Local variables:
 * c-basic-offset: 4
 * End:
 */
