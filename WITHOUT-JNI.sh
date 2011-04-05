#!/bin/bash

echo "Renaming MG and GDBM related source code to 'java..tmp' file names"

pushd src/java/org/greenstone/gsdl3

cd service
echo "  Processing 'service' directory"

for f in *MG*java PhindPhraseBrowse.java ; do
  if [ -e $f ] ; then
    echo "    $f -> $f.tmp"
    /bin/mv $f $f.tmp
  fi
done

cd ..


cd util
echo "  Processing 'util' directory"

for f in *GDBM*java ; do
  if [ -e $f ] ; then
    echo "    $f -> $f.tmp"
    /bin/mv $f  $f.tmp
  fi
done
cd ..

popd



