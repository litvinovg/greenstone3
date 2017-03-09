# if this file is executed, /bin/sh is used, as we don't start with #!
# this should work under ash, bash, zsh, ksh, sh style shells.

#the purpose of this file is to check/set up the environment for greenstone3
#sorts out:
#  - gsdl3home
#  - java

# java_min_version gets passed to search4j as the minimum java version
java_min_version=1.5.0_00
DEBUG=true

function testSource(){

  if test "$0" != "`echo $0 | sed s/gs3-setup\.sh//`" ; then
  # if $0 contains "gs3-setup.sh" we've been run... $0 is shellname if sourced.
  # One exception is zsh has an option to set it temporarily to the script name
    if test -z "$ZSH_NAME" ; then
    # we aren't using zsh
     gsdl_not_sourced=true
   fi 
  fi

  if test -n "$gsdl_not_sourced" ; then
     echo "  Error: Make sure you source this script, not execute it. Eg:"
     echo "    $ source gs3-setup.sh"
     echo "  or"
     echo "    $ . gs3-setup.sh"
     echo "  not"
     echo "    $ ./gs3-setup.sh"
     unset gsdl_not_sourced
     exit 1
  fi

  if test ! -f gs3-setup.sh ; then
    echo "You must source the script from within the Greenstone home directory"
    return 1
  fi
  return 0
}

# if GSDL3SRCHOME is set, then we assume that we have already sourced the 
# script so don't do it again. UNLESS, GSDL3SRCHOME doesn't match the 
# current directory in which case it was a different gs3 installation, so lets
# do it now.x
function testAlreadySourced() {
  if [ ! -z "$GSDL3SRCHOME" ]; then 
    localgs3sourcehome="`pwd`"
    if [ "$GSDL3SRCHOME" == "$localgs3sourcehome" ]; then
      echo "Your environment is already set up for Greenstone3"
      return 1
    fi
    echo "Your environment was set up for Greenstone 3 in $GSDL3SRCHOME."
    echo "Overwriting that set up for the current Greenstone 3 in $localgs3sourcehome"
  fi
  return 0
}

function setGS3ENV() {

  echo "Setting up your environment for Greenstone3"
  ## main greenstone environment variables ##
  GSDL3SRCHOME="`pwd`"
  GSDL3HOME="$GSDL3SRCHOME/web"
  export GSDL3HOME
  export GSDL3SRCHOME

  if test "x$GSDLOS" = "x" ; then
    GSDLOS=`uname -s | tr 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' 'abcdefghijklmnopqrstuvwxyz'`
    # check for running bash under cygwin
    if test "`echo $GSDLOS | sed 's/cygwin//'`" != "$GSDLOS" ; then
      GSDLOS=windows
    fi 
  fi
  export GSDLOS
  echo "  - Exported GSDL3HOME, GSDL3SRCHOME and GSDLOS"

  #change this if external tomcat
  TOMCAT_HOME="$GSDL3SRCHOME/packages/tomcat"

  ## adjustments to users (existing) environment ##

  #PATH
  addtopath PATH "$GSDL3SRCHOME/bin/script"
  addtopath PATH "$GSDL3SRCHOME/bin"
  echo "  - Adjusted PATH"

  #MANPATH
  addtopath MANPATH "$GSDL3SRCHOME/doc/man"
  echo "  - Adjusted MANPATH"
 
  #CLASSPATH
  addtopath CLASSPATH "."
  addtopath CLASSPATH "$GSDL3HOME/WEB-INF/classes"
  addtopath CLASSPATH "$GSDL3SRCHOME/resources/java"
  addtopath CLASSPATH "$GSDL3SRCHOME/cp.jar"

  # Tomcat 5 jar files
  for JARFILE in "$TOMCAT_HOME"/common/endorsed/*.jar; do
    addtopath CLASSPATH "$JARFILE"
  done    
  # Tomcat 6 jar files
  for JARFILE in "$TOMCAT_HOME"/lib/*.jar; do
    addtopath CLASSPATH "$JARFILE"
  done    
  
  #shouldn't need these as they will have been copied to their correct locations elsewhere in the greenstone3 installation
  #for JARFILE in "$GSDL3SRCHOME"/build/*.jar; do
  #  addtopath CLASSPATH "$JARFILE"
  #done

  echo "  - Adjusted CLASSPATH"

  #LD_LIBRARY_PATH
  addtopath LD_LIBRARY_PATH "$GSDL3SRCHOME/lib/jni"
  addtopath DYLD_LIBRARY_PATH "$GSDL3SRCHOME/lib/jni"
  echo "  - Adjusted LD_LIBRARY_PATH and DYLD_LIBRARY_PATH"

  #ant
  ANT_VERSION=1.7.1
  if [ -x "$GSDL3SRCHOME/packages/ant/bin/ant" ]; then
    ANT_HOME="$GSDL3SRCHOME/packages/ant"
    export ANT_HOME
    addtopath PATH "$ANT_HOME/bin"
    echo "  - Setup Greenstone ant ($GSDL3SRCHOME/packages/ant)"
  else
    which ant &> /dev/null
    if [ "$?" == "0" ]; then
      echo "  - WARNING: Greenstone 'Ant' package missing - falling back to system Ant"
      echo "             Note that Greenstone requires Ant $ANT_VERSION or greater"
    elif [ "ANT_HOME" != "" ]; then
      addtopath PATH "$ANT_HOME/bin"
      echo "  - WARNING: Greenstone 'Ant' package missing - falling back to system Ant"
      echo "             Note that Greenstone requires Ant $ANT_VERSION or greater"
    else
      echo "  - ERROR: Greenstone 'Ant' package missing - please install Ant yourself"
      echo "           Note that Greenstone requires Ant $ANT_VERSION or greater"
    fi
  fi

  #ImageMagick
  #if test -d "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/imagemagick" ; then
  #  addtopath PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/imagemagick/bin"
  #  MAGICK_HOME="$GSDL3SRCHOME/gs2build/bin/$GSDLOS/imagemagick"
  #  export MAGICK_HOME
  #  if test "$GSDLOS" = "linux"; then
  #    addtopath LD_LIBRARY_PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/imagemagick/lib"
  #  elif test "$GSDLOS" = "darwin"; then
  #    addtopath DYLD_LIBRARY_PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/imagemagick/lib"
  #  fi
  #  echo "  - Setup ImageMagick"
  #fi

  #Ghostscript
  if test -d "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/ghostscript"; then
    addtopath PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/ghostscript/bin"
    GS_LIB="$GSDL3SRCHOME/gs2build/bin/$GSDLOS/ghostscript/share/ghostscript/8.63/lib"
    export GS_LIB
    GS_FONTPATH="$GSDL3SRCHOME/gs2build/bin/$GSDLOS/ghostscript/share/ghostscript/8.63/Resource/Font"
    export GS_FONTPATH
    echo "  - Setup GhostScript"
  fi

  #wvWare 
  # wvWare's environment is now set up by bin/script/wvware.pl
  # The wvware.pl script can be called from the cmdline to perform wvware tasks.
  # GLI calls gsConvert.pl which calls wvware.pl to similarly perform wvware tasks.
#  if test -d "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/wv"; then
#    if test "$GSDLOS" = "linux"; then
#      addtopath LD_LIBRARY_PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/wv/lib"
#    elif test "$GSDLOS" = "darwin"; then
#      addtopath DYLD_LIBRARY_PATH "$GSDL3SRCHOME/gs2build/bin/$GSDLOS/wv/lib"
#    fi
#    echo "  - Setup wvWare"
#  fi

}

function checkJava() {

  # we now include a JRE with Mac (Mountain) Lion too, because from Yosemite onwards there's no system Java on Macs
  BUNDLED_JRE="`pwd`/packages/jre"
  HINT=$BUNDLED_JRE
  
  #if [ "$GSDLOS" = "darwin" ] && [ ! -d "$HINT" ]; then
  if [ "$GSDLOS" = "darwin" ]; then
      if [ "$JAVA_HOME" != "" ] && [ -d "$JAVA_HOME" ]; then
	  HINT=$JAVA_HOME
      elif [ ! -d "$HINT" ]; then
	  HINT=`/usr/libexec/java_home`
          # old code used as fallback:
	  if [ ! -d "$HINT" ]; then
              HINT=/System/Library/Frameworks/JavaVM.framework/Home
	  fi
      fi
  fi

  if [ "$DEBUG" == "true" ]; then echo "**********************************************"; fi

  # If the file utility exists, use it to determine the bitness of this GS3,
  # particularly of this GS3's lib\jni\libgdbmjava.so (gdbmjava.dll), since we prefer a matching java
  # If any executable doesn't exist, the return value is 127.
  # If file utility exists, then 0 is returned on successful execution, 1 is an error exit code
  # Running file without arg returns 1 therefore.

  # Determine the bitness of this GS3 installation, by running:
  # `file lib/jni/libgdbmjava.so`
  # Output:
  #    lib/jni/libgdbmjava.so: ELF 64-bit LSB  shared object, x86-64, version 1 (SYSV),
  #    dynamically linked, BuildID[sha1]=5ae42cf69275408bdce97697d69e9e6fd481420d, not stripped
  # On 32-bit linux, the output will contain "lib/jni/libgdbmjava.so: ELF 32-bit ..."
  # Check output string contains bitness: http://stackoverflow.com/questions/229551/string-contains-in-bash

  fileexists=`file 2&> /dev/null`

  # Integer comparison, http://tldp.org/LDP/abs/html/comparison-ops.html
  #    can also use double brackets:  if [[ $? > 1 ]]; then ...
  if [ "$?" -gt "1" ]; then
      if [ "$DEBUG" == "true" ]; then 
	  echo "    'file' utility not found installed on this unix-based system."
	  echo "    Unable to use 'file' utility to determine bitness of this GS3 to see if it matches that of any Java found."
      fi
      bitness=-1
  elif [ ! -f "$GSDL3SRCHOME/lib/jni/libgdbmjava.so" ]; then
      # the file we want to test the bitness of, to determine GS3's bitness by, doesn't exist yet
      bitness=-1
  else
      bitness=`file $GSDL3SRCHOME/lib/jni/libgdbmjava.so`
      if [[ $bitness == *"64-bit"* ]]; then
	  bitness=64
	  echo "The installed Greenstone is $bitness bit"
      elif [[ $bitness == *"32-bit"* ]]; then
	  bitness=32
	  echo "The installed Greenstone is $bitness bit"
      else
	  bitness=-1
	  echo "WARNING: Greenstone installation is of unknown bitness. \"$bitness\" is neither 32 nor 64 bit"
      fi
  fi

  # If search4j is present, use it to locate a java.
  # If search4j finds a Java, then:
  # - If its bitness doesn't match and there's a bundled jre, use the bundled jre instead.
  # - If its bitness doesn't match and there's no bundled jre, use the java found by search4j anyway,
  # we'll print a warning about this bitness mismatch at the end

  javaset=false
  if [ -x bin/search4j ] ; then
      FOUNDJAVAHOME="`bin/search4j -d -p \"$HINT\" -m $java_min_version`"
      javahome_retval=$?
      FOUNDJREHOME="`bin/search4j -r -p \"$HINT\" -m $java_min_version`"
      jrehome_retval=$?
  fi

  # 1. check the bitness of any JDK java found by search4j, and use if appropriate
  if [ "$javahome_retval" == "0" ]; then
      checkJavaBitnessAgainstGSBitness "$FOUNDJAVAHOME" "$bitness"

      if [ "$?" == "0" ]; then
          #found a suitable java
	  if [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then
	      echo "    The detected JDK at $FOUNDJAVAHOME is a matching $bitness bit"
	  fi
	  setupJavaAt "$FOUNDJAVAHOME" "JDK"
	  javaset=true

      elif [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then	  
	  echo "    The detected JDK java at $FOUNDJAVAHOME is an incompatible bit architecture"
      fi
  fi

  # 2. check bitness of any JRE java found by search4j, and use if appropriate
  # http://tldp.org/LDP/abs/html/comparison-ops.html
  if [ "$javaset" != "true" ] && [ "$jrehome_retval" == "0" ]; then      
      checkJavaBitnessAgainstGSBitness "$FOUNDJREHOME" "$bitness"
      
      if [ "$?" == "0" ]; then
          #found a suitable jre
	  if [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then
	      echo "    The detected JRE at $FOUNDJREHOME is a matching $bitness bit"
	  fi
	  setupJavaAt "$FOUNDJREHOME" "JRE"
	  javaset=true
	  
      elif [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then
	  echo "    The detected JRE java at $FOUNDJREHOME is an incompatible bit architecture"
      fi      
  fi

  # 3. check the bitness of any bundled JRE, and use if appropriate
  if [ "$javaset" != "true" ] && [ -d "$BUNDLED_JRE" ]; then
      checkJavaBitnessAgainstGSBitness "$BUNDLED_JRE" "$bitness"
      
      # For linux, the bundled JRE ought to be of a bitness matching this OS.
      if [ "$?" == "0" ]; then
	  if [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then
	      # bundled JRE matches GS bitness
	      echo "*** Changing to use Greenstone's bundled $bitness-bit jre at $BUNDLED_JRE"
	  fi
	  setupJavaAt "$BUNDLED_JRE" "JRE"
	  javaset=true

      elif [ "$bitness" != "-1" ] && [ "$DEBUG" == "true" ]; then
	  echo "    The bundled JRE java is an incompatible bit architecture"
      fi
  fi

  # 4. None of the java found so far, if any (via search4j, bundled_jre), may have matched bitness wise
  # So, fall back to using whichever is available in sequence anyway.
  # We'll print a warning of bitness mismatch later

  if [ "$javaset" != "true" ]; then
      # go with any JAVA_HOME else JRE_HOME that search4j found, else with any bundled JRE if present
      if [ "$javahome_retval" == "0" ]; then
	  setupJavaAt "$FOUNDJAVAHOME" "JDK"
	  javaset=true
      elif [ "$jrehome_retval" == "0" ]; then
	  setupJavaAt "$FOUNDJREHOME" "JRE"
	  javaset=true
      elif [ -d "$BUNDLED_JRE" ]; then
	  # bundled JRE should be >= than minimum version of java required
	  setupJavaAt "$BUNDLED_JRE" "JRE"
	  javaset=true
      fi
  fi

  # 5. lastly, check if java already setup
  if [ "$javaset" != "true" ]; then

    if [ -x bin/search4j ]; then

      # no suitable java could be found by search4j
      echo "  - ERROR: Failed to locate java $java_min_version or greater"	  
      echo "           Please set JAVA_HOME or JRE_HOME to point to an appropriate java"
      echo "           And add JAVA_HOME/bin or JRE_HOME/bin to your PATH"

    else
      # search4j wasn't present, and no bundled JRE, so check JAVA_HOME or JRE_HOME manually
      echo "*** Could not find an appropriate JDK or JRE java"
      echo "*** Attempting to use JAVA_HOME else JRE_HOME in the environment"

      if [ "$JAVA_HOME" != "" ] && [ "`which java`" == "$JAVA_HOME/bin/java" ]; then
	  echo "  - Using java at $JAVA_HOME"
	  echo "  - WARNING: Greenstone has not checked the version number of this java installation"
	  echo "             The source distribution of Greenstone3 requires java 1.5 or greater"
	  echo "             (SVN users may still use java 1.4)"
      elif [ "$JRE_HOME" != "" ] && [ "`which java`" == "$JRE_HOME/bin/java" ]; then
	  echo "  - Using java at $JRE_HOME"
	  echo "  - WARNING: Greenstone has not checked the version number of this java installation"
	  echo "             The source distribution of Greenstone3 requires java 1.5 or greater"
	  echo "             (SVN users may still use java 1.4)"
	  
      #failing all that, print a warning
      else
          #no suitable java exists
	  echo "  - ERROR: Failed to locate java"
	  echo "           Please set JAVA_HOME or JRE_HOME to point to an appropriate java"
	  echo "           And add JAVA_HOME/bin or JRE_HOME/bin to your PATH"
	  return
      fi
    fi
  fi

  # If we know the bitness of this GS3 installation, then warn if there's a mismatch
  # with the bitness of the Java found

  if [ "$bitness" != "-1" ]; then
      if [ "$JAVA_HOME" != "" ]; then
	  JAVA_FOUND=$JAVA_HOME
      elif [ "$JRE_HOME" != "" ]; then
	  JAVA_FOUND=$JRE_HOME
      fi
      checkJavaBitnessAgainstGSBitness "$JAVA_FOUND" "$bitness"
      if [ "$?" == "1" ]; then
	  echo "*** WARNING: Detected mismatch between the bit-ness of your GS installation ($bitness bit)"
	  echo "*** and the Java found at $JAVA_FOUND/bin/java"
	  echo "*** Continuing with this Java anyway:"
	  echo "*** This will only affect MG/MGPP collections for searching, and GDBM database collections"
	  echo "*** Else set JAVA_HOME or JRE_HOME to point to an appropriate $bitness bit Java"
	  echo "*** Or recompile GS with your system Java:"
	  if [ "$JAVA_HOME" != "" ]; then
	      echo "*** JAVA_HOME at $JAVA_HOME"
	  else
	      echo "*** JRE_HOME at $JRE_HOME"
	  fi
      fi
  fi

  if [ "$DEBUG" == "true" ]; then echo "**********************************************"; fi

}

# if bitness (parameter #2) is -1, then this function returns 0 (generally meaning success).
function checkJavaBitnessAgainstGSBitness() {
#    if [ "$DEBUG" == "true" ]; then echo "Testing bitness of java found at $java_installation"; fi
    java_installation="$1"
    bitness="$2"

    # bitness can be -1 if the 'file' utility could not be found to determine bitness
    # or if its output no longer prints "32-bit" or "64-bit". Should continue gracefully
    if [ "$bitness" == "-1" ]; then
	return 0
    fi

    # now we can actually work out if the java install's bitness matches that of GS ($bitness)
    # java -d32 -version should return 0 if the Java is 32 bit, and 1 (failure) if the Java is 64 bit.
    # Likewise, java -d64 -version will return 0 if the Java is 64 bit, and 1 (failure) if the Java is 32 bit.
    `$java_installation/bin/java -d$bitness -version 2> /dev/null`

    if [ "$?" == "0" ]; then
	return 0
    elif [ "$?" == "1" ]; then
	return 1
    else
	echo "*** Problem determining bitness of java using java at $java_installation"
	return $?
    fi
}


function setupJavaAt() {

  # check the second parameter if non-null
  if [ -n "$2" ] && [ "$2" == "JRE" ]; then
    export JRE_HOME="$1"
    addtopath PATH "$JAVA_HOME/bin"

    BUNDLED_JRE="`pwd`/packages/jre"    
    if [[ "$JRE_HOME" == *"$BUNDLED_JRE"* ]]; then
	msg="the bundled"
    fi

    echo "  - Exported JRE_HOME to $msg $JRE_HOME"
  else
    export JAVA_HOME="$1"
    addtopath PATH "$JAVA_HOME/bin"
    echo "  - Exported JAVA_HOME to $JAVA_HOME"
  fi

  
}

function pauseAndExit(){
 echo -n "Please press any key to continue... "
 read 
}

function isinpath() {
  for file in `echo $1 | sed 's/:/ /g'`; do
    if [ "$file" == "$2" ]; then
      echo true
      return
    fi
  done
  echo false
}

function addtopath() {
  eval "PV=\$$1"
  #echo "$1 += $2"
  if [ "$PV" == "" ]; then
    cmd="$1=\"$2\""
  else
    cmd="$1=\"$2:\$$1\""
  fi
  eval $cmd
  eval "export $1"
}

# Note: use return not exit from a sourced script otherwise it kills the shell
echo
testSource
if [ "$?" == "1" ]; then
return
fi
testAlreadySourced
if [ "$?" == "1" ]; then
return
fi
setGS3ENV

if test -e gs2build/setup.bash ; then 
  echo ""
  echo "Sourcing gs2build/setup.bash"
  cd gs2build ; source setup.bash ; cd ..
fi


if test "x$gsopt_noexts" != "x1" ; then
    if test -e ext ; then
	for gsdl_ext in ext/* ; do
	    if [ -d $gsdl_ext ] ; then 
		cd $gsdl_ext > /dev/null
		if test -e gs3-setup.sh ; then 
		    source ./gs3-setup.sh 
		elif test -e setup.bash ; then 
		    source ./setup.bash 
		fi 
		cd ../..  
	    fi
	done
    fi
fi

if test -e local ; then
  if test -e local/gs3-setup.sh ; then 
    echo ""
    echo "Sourcing local/gs3-setup.sh"
    cd local ; source gs3-setup.sh ; cd ..
  fi
fi


checkJava
echo ""
