#!/bin/bash

#http://www.wikihow.com/Find-Your-IP-Address-on-a-Mac#Finding_Your_Internal_IP_Using_the_Terminal_sub
#http://stackoverflow.com/questions/1469849/how-to-split-one-string-into-multiple-strings-in-bash-shell
#http://www.linuxforums.org/forum/red-hat-fedora-linux/193076-ifconfig-doesnt-display-ipv4-address.html


# The following echoes the IPv4, e.g. 100.200.300.45
# But need to still replace the . with \.

#echo `ifconfig | grep "inet " | grep -v 127.0.0.1`|cut -d' ' -f 2|cut -d':' -f 2


tmp=`ifconfig | grep "inet " | grep -v 127.0.0.1`

tmp=`echo $tmp|cut -d' ' -f 2|cut -d':' -f 2`

#http://stackoverflow.com/questions/13210880/replace-one-substring-for-another-string-in-shell-script
#${original_string//searchterm/$string_to_replace_searchterm_with}
replace="."
replacement="\."

tmp=${tmp//$replace/$replacement}

# next, if the tmp variable is not the empty string, prefix the | operator
# http://unix.stackexchange.com/questions/146942/how-can-i-test-if-a-variable-is-empty-or-contains-only-spaces
# Can just return | if $tmp is empty, as that doesn't really matter. Will be consistent with windows
if [[ ! -z "${tmp// }" ]]; then
	tmp="|$tmp"
else
	tmp="|"
fi


echo $tmp
