#!/bin/bash
gailang=en

echo
    echo "Greenstone Administrator Interface (GAI)"
    echo "Copyright (C) 2005, New Zealand Digital Library Project, University Of Waikato"
    echo "GAI comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt"
    echo "This is free software, and you are welcome to redistribute it"
echo

##  -------- Run the Greenstone Administrator Interface --------
# Need to run this script from its own directory instead of whichever directory it may be called from

thisdir="`dirname \"$0\"`"
thisdir="`cd \"$thisdir\" && pwd`"
cd "$thisdir"

##  ---- Determine GSDL3HOME ----
gsdl3path=

# Some users may set the above line manually
if [ "$gsdl3path" == "" ]; then
    # Check the environment variable first
    if [ "$GSDL3SRCHOME" != "" ]; then
	gsdl3path=$GSDL3SRCHOME

    # If it is not set, assume that the GAI is installed as a subdirectory of Greenstone
    else
	pushd .. > /dev/null
	gsdl3path=`pwd`
	popd > /dev/null
    fi
fi

# Setup Greenstone3, unless it has already been done
if [ "$GSDL3HOME" == "" ]; then
    pushd $gsdl3path > /dev/null
    source gs3-setup.sh
    popd > /dev/null
fi

# Other arguments you can provide to GLI to work around memory limitations, or debug
# -Xms<number>M    To set minimum memory (by default 32MB)
# -Xmx<number>M    To set maximum memory (by default the nearest 2^n to the total remaining physical memory)
# -verbose:gc      To set garbage collection messages
# -Xincgc          For incremental garbage collection (significantly slows performance)
# -Xprof           Function call profiling
# -Xloggc:<file>   Write garbage collection log

# $javapath -classpath classes/:GLI.jar:lib/apache.jar:lib/qfslib.jar:lib/mail.jar:lib/activation.jar org.greenstone.gatherer.GathererProg -gsdl $GSDLHOME -wget $wgetpath $*

java -cp $CLASSPATH org.greenstone.admin.GAI $GSDL3SRCHOME $GSDL3HOME
