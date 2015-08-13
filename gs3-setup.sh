# if this file is executed, /bin/sh is used, as we don't start with #!
# this should work under ash, bash, zsh, ksh, sh style shells.

#the purpose of this file is to check/set up the environment for greenstone3
#sorts out:
#  - gsdl3home
#  - java

# java_min_version gets passed to search4j as the minimum java version
java_min_version=1.5.0_00


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
  HINT="`pwd`/packages/jre"
  if [ "$GSDLOS" = "darwin" ] && [ ! -d "$HINT" ]; then
      HINT=`/usr/libexec/java_home`
      # old code used as fallback:
      if [ ! -d "$HINT" ]; then
          HINT=/System/Library/Frameworks/JavaVM.framework/Home
      fi
  fi

  #if search4j is present, use it
  if [ -x bin/search4j ] ; then
    FOUNDJAVAHOME="`bin/search4j -p \"$HINT\" -m $java_min_version`"
    if [ "$?" == "0" ]; then
      #found a suitable java
      setupJavaAt "$FOUNDJAVAHOME"
    else
      #no suitable java exists
      echo "  - ERROR: Failed to locate java $java_min_version or greater"
      echo "           Please set JAVA_HOME or JRE_HOME to point to an appropriate java"
      echo "           And add JAVA_HOME/bin or JRE_HOME/bin to your PATH"
    fi

  #otherwise manually try the hint
  elif [ -d "$HINT" ]; then
      #found a suitible java
      setupJavaAt "$HINT"

  #lastly, check if java already setup
  elif [ "$JAVA_HOME" != "" ] && [ "`which java`" == "$JAVA_HOME/bin/java" ]; then
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
  fi
}

function setupJavaAt() {
  export JAVA_HOME="$1"
  addtopath PATH "$JAVA_HOME/bin"
  echo "  - Exported JAVA_HOME to $JAVA_HOME"
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
