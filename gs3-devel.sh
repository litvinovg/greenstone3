# if this file is executed, /bin/sh is used, as we don't start with #!
# this should work under ash, bash, zsh, ksh, sh style shells.

source ./gs3-setup.sh


if test -e gs2build/devel.bash ; then 
  echo ""
  echo "Sourcing gs2build/devel.bash"
  cd gs2build ; source ./devel.bash ; cd ..
fi


if test "x$gsopt_noexts" != "x1" ; then
    if test -e ext ; then
	for gsdl_ext in ext/* ; do
	    if [ -d $gsdl_ext ] ; then 
		cd $gsdl_ext > /dev/null
		if test -e gs3-devel.sh ; then 
		    source ./gs3-devel.sh 
		elif test -e devel.bash ; then 
		    source ./devel.bash 
		fi 
		cd ../..  
	    fi
	done
    fi
fi

if test -e local ; then
  if test -e local/gs3-devel.sh ; then 
    echo ""
    echo "Sourcing local/gs3-devel.sh"
    cd local ; source gs3-devel.sh ; cd ..
  fi
fi

echo ""
