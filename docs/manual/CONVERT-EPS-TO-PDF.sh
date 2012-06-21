#!/bin/bash

for f in *.eps ; do 
  echo "Converting: $f"
  epstopdf $f
done
