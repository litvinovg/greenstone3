#!/bin/bash
serverlang=en

java_min_version=1.4.0_00

autoset_gsdl_home() {

  # remove leading ./ if present
  prog="${0#./}"

  isrelative="${prog%%/*}"

  if [ ! -z $isrelative ] ; then
    # some text is left after stripping
    # => is relative
    pwd="$PWD"
    prog="$pwd/$prog"
  fi

  fulldir="${prog%/*}"

  # remove trailing /. if present
  eval $1=\""${fulldir%/.}"\"
}

check_gsdl3home_writable() {

    echo "Checking if the Greenstone3 web directory is writable ..."
    (echo "This is a temporary file. It is safe to delete it." > "$GSDL3HOME/testing.tmp" ) 2>/dev/null
    if [ -e "$GSDL3HOME/testing.tmp" ] ; then
	/bin/rm "$GSDL3HOME/testing.tmp"
	gsdl3home_isreadonly=0
	echo " ... yes."
    else
	gsdl3home_isreadonly=1
	gsdl3_writablehome="/tmp/greenstone/web"

	echo " ... no."
	echo "Setting Greenstone3 web home writable area to be: $gsdl3_writablehome"


#	if [ ! -d "$gsdl3_writablehome" ] ; then
#	    echo "Creating the directory: $gsdl3_writablehome"

#	    mkdir -p "$gsdl3_writablehome" 
#	    mkdir -p "$gsdl3_writablehome/packages" 
#	    mkdir -p "$gsdl3_writablehome/logs" 
#	    mkdir -p "$gsdl3_writablehome/ext/solr" 

#	    chmod a+rwx "$gsdl3_writablehome" 
#	    chmod a+rwx "$gsdl3_writablehome/packages" 
#	    chmod a+rwx "$gsdl3_writablehome/logs" 
#	    chmod a+rwx "$gsdl3_writablehome/ext/solr" 

##	    echo "Copying to $gsdl3_writablehome/packages/tomcat"
##    	    /bin/cp -r "$GSDL3SRCHOME/packages/tomcat" "$gsdl3_writablehome/packages/."

##            echo "=> Copying Greenstone's web/WEB-INF to writable area"
	    
##	    gsdl3_home=$GSDL3HOME
##	    /bin/cp -r "$gsdl3_home/WEB-INF" "$gsdl3_writablehome/."
##	    /bin/cp -r "$gsdl3_home/index.html" "$gsdl3_writablehome/."
##	fi
    fi
}

echo "Greenstone 3 Server"
echo "Copyright (C) 2009, New Zealand Digital Library Project, University Of Waikato"
echo "This software comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt"
echo "This is free software, and you are welcome to redistribute it"

##  -------- Run the Greenstone 3 Server --------


##  ---- Determine GSDL3SRCHOME ----
gsdl3path=

# Some users may set the above line manually
if [ -z "$gsdl3path" ]; then
   autoset_gsdl_home "gsdl3path"
fi

# Setup Greenstone3
pushd $gsdl3path > /dev/null
source ./gs3-setup.sh
popd > /dev/null


check_gsdl3home_writable

opt_properties=
if [ $gsdl3home_isreadonly = 1 ] ; then
    opt_properties="-Dgsdl3home.isreadonly=true -Dgsdl3.writablehome=$gsdl3_writablehome"
else
    opt_properties="-Dgsdl3.writablehome=$GSDL3HOME"
fi

# Calling ant target to initialize the gsdl3-writablehome area (if it doesn't already exist)
# ... including the all important global.properties.

ant $opt_properties configure-web


# JRE_HOME or JAVA_HOME must be set correctly to run this program
HINT="`pwd`/packages/jre"
if [ "$GSDLOS" = "darwin" ] && [ ! -d "$HINT" ]; then
    HINT=`/usr/libexec/java_home`
fi
javapath=`search4j -p "$HINT" -m $java_min_version -e` 
if [ "$?" == "0" ]; then
    # In Java code, '...getResourceAsStream("build.properties")'
    # needs up to be in the right directory when run
    pushd "$gsdl3path" > /dev/null

    "$javapath" $opt_properties org.greenstone.server.Server3 "$GSDL3SRCHOME" $serverlang

    popd > /dev/null
fi

