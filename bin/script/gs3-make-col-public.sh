#!/bin/bash

#check that GSDL3HOME is set
if [ -z "$GSDL3HOME" ] ; then
  echo "You need to 'source gs3-setup.sh' in the greenstone3 directory before running this script" >&2
 exit;
fi

if [ $# != 2 ] ; then
  echo "Usage: gs3-make-col-private.sh <site-name> <collection-name>" >&2
  exit;
fi

sitename=$1
collectionname=$2

collectdir="$GSDL3HOME/sites/$sitename/collect/$collectionname"
collectconfigfile="$collectdir/etc/collectionConfig.xml"

if [ ! -d "$collectdir" ] ; then
  echo "Unable to find directory: \"$collectdir\"" >&2
  exit
fi

if [ ! -f "$collectconfigfile" ] ; then
  echo "Unable to find collection config file \"$collectconfigfile\"" >&2
  exit
fi

# Want to find (and change) lines like:
#   <metadata lang="en" name="public">false</metadata>

/bin/cp "$collectconfigfile" "$collectconfigfile.bak" \
&& \
cat "$collectconfigfile" \
  | sed 's%\(<metadata[^>]\+name="public"[^>]*>\).\+\(</metadata>\)%\1true\2%g' \
  > "$collectconfigfile.new" \
&& \
/bin/mv "$collectconfigfile.new" "$collectconfigfile" 

if [ $? = 0 ] ; then
  echo "====="
  echo "| Set collection metadata name 'public' to true"
  echo "| Restart (or reconfigure) the web server for this change to take effect"
  echo "====="
else
  echo "Failed to set collection metadata name 'public' to true" >&2
  exit $?
fi






