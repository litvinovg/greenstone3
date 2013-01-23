Greenstone 3 (GSDL3)
Copyright (C) 2003 New Zealand Digital Libraries, University Of Waikato
Greenstone3 comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt
This is free software, and you are welcome to redistribute it

These are some extra notes for installing Greenstone from SVN. Please also 
read the README.txt file for general information as almost all of that applies 
here too.

You will need Java and Ant to run Greenstone 3. 

Your Java version should be 1.4 or higher. We recommend Sun Java. You need the 
SDK (development environment). Set the environment variable JAVA_HOME to be 
the root of your Java installation.

Ant (Apache's Java based build tool) can be downloaded from 
http://ant.apache.org/bindownload.cgi. Set the environment variable 
ANT_HOME to be the root of your Ant installation, and make sure the Ant 
executables are on your PATH. 
Greenstone 3 requires Ant 1.7.1 or higher.

Installing Greenstone from an SVN checkout:
---------------------------------------------------------------------

Make sure an SVN executable is on your PATH.

Checkout the code:

  svn co http://svn.greenstone.org/greenstone3/trunk greenstone3


Local Settings
--------------

You will then need to make sure 'ant' (version 1.7 or greater) and 'javac'
(along with an appropriate JAVA_HOME environment variable) can be 
found from your command line.

To help with getting such local command-line settings right, we have
started to build up a set of 'local' folder examples available
on our svn server.  You can browse these at:

  http://trac.greenstone.org/browser/local/

For example, in the top-level folder of your Greenstone3 installation,
for a build on a 64-bit linux computer you might like to check out:

  svn co http://svn.greenstone.org/local/greenstone3/linux-64bit local

You should then check files like 'local/gs3-setup.bash' and 
'local/gs3-devel.bash' and edit the values there, such as JAVA_HOME,
to reflect where such programs exist on your local filesystem.
to reflect where 

Prior to compiling, 'cd' into this folder and source the appropriate setup
file (the developer 'gs3-devel' file if it exists, as in the case of cross
compiling for Anroid), or else the 'gs3-setup' one (for instance gs-setup.sh,
gs-setup.bash, or setup.bash)

For example:

  cd local
  source ./gs3-setup.bash
  cd ..


Build and install:
-----------------

In the greenstone3 directory, check and/or edit the build.properties file. In
 particular, set the Tomcat port number. See 'Configuring your installation' in
 README.txt for more information.

Note, initial  checkouts from SVN have a build.properties.in file. Running 
'ant' will result in the build.properties file being generated from the .in 
file (a straight copy).

Then, run 'ant prepare install'. The two targets can be run separately if you 
like.

The 'prepare' target will download additional code (using SVN and http), so 
you need to be online to run it. The 'install' target can be run offline.

The prepare/install targets will ask you if you accept the properties before 
starting.
To suppress this prompt, use the -Dproperties.accepted=yes flag. E.g.
ant -Dproperties.accepted=yes prepare install

To log the output, run
ant -Dproperties.accepted=yes -logfile build.log prepare install 

On Windows, Visual Studio is used for compiling. The VCVARS32.bat script needs 
to be run in the command prompt before compiling with "ant install".

In the greenstone3 directory, you can run 'ant' which will give you a help 
message.
Running 'ant -projecthelp' gives a list of the targets that you can run - these
do various things like compile the source code, start up the server etc.

Extra Configuration notes:
--------------------------------------------------------------

Greenstone 3 uses some parts of Greenstone 2 for collection building, 
including external packages and the Librarian Interface. These will be 
installed during the Greenstone 3 installation process. If you do not want 
collection building capability, please set the disable.collection.building 
property to true in build.properties. 


Common install/update targets:
---------------------------------------------------------------------
1. Install for the first time from SVN:
svn co http://svn.greenstone.org/greenstone3/trunk greenstone3
cd greenstone3
ant prepare install

2. Install for the first time from SVN, mostly offline:
[online]
svn co http://svn.greenstone.org/greenstone3/trunk greenstone3
cd greenstone3
ant prepare
[offline]
ant install

3. Updating your Greenstone installation from SVN (and reconfigure/recompile):
cd greenstone3
ant update

4. Updating your Greenstone installation from SVN, mostly offline:
cd greenstone3
[online]
ant svnupdate
[offline]
ant -Dnosvn.mode=yes update

