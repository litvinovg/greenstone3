#!/bin/sh 

#check that GSDL3HOME is set
if test -z "$GSDL3HOME" ; then
  echo "You need to 'source gs3-setup.sh' in the greenstone3 directory before running this script"
 exit;
fi

if test $# != 2; then
  echo "Usage: gs3-mkcol.sh <site name> <collection name>"
  exit;
fi

sitename=$1
collectionname=$2

java -DGSDL3HOME=$GSDL3HOME -DGSDL3SRCHOME=$GSDL3SRCHOME -Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser org.greenstone.gsdl3.gs3build.Create -site $sitename -collect $collectionname

#/opt/sun-jdk-1.4.2.03/bin/java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y -DGSDL3HOME=$GSDL3HOME -Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser org.greenstone.gsdl3.gs3build.Build -site $sitename -collect $collectionname




