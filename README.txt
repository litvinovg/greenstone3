Greenstone 3 (GSDL3)  
Copyright (C) 2003 New Zealand Digital Libraries, University Of Waikato
Greenstone3 comes with ABSOLUTELY NO WARRANTY; for details see LICENSE.txt
This is free software, and you are welcome to redistribute it

Installing Greenstone
---------------------------------------------------------

Download the appropriate installer from sourceforge.net/projects/greenstone3 and run it. 

See 'Installing from a Source Distribution' section below for extra notes about installing from Source.

See the README-SVN.txt file for extra notes about installing directly from SVN.

Greenstone 3 requires Java and Ant to run. These will be installed during a
Greenstone binary installation.

Running Greenstone:
---------------------------------------------------------

To start up Greenstone, select Greenstone3 Digital Library from the Start menu 
(Windows), or run gs3-server.sh/bat. This launches a small server program 
which starts up Tomcat and launches a browser. A small window pops up which 
allows you to change some settings for your library and restart the Tomcat 
server. Closing this program will stop Tomcat running.

Alternatively, you can start Tomcat directly through using Ant. 'ant start', 
'ant restart' and 'ant stop' starts, restarts and shuts down Tomcat, 
respectively. This will only start/stop a local server (one installed by 
Greenstone). You will need to manually start/stop an external Tomcat 
(see below for notes about using a version of Tomcat external to Greenstone).

You will need to run 'gs3-setup' (Windows) or 'source gs3-setup.sh' (Linux/Mac)
before running ant targets.

Once the Tomcat server is running, Greenstone will be available in a browser 
at "http://localhost:8080/greenstone3" (or whatever port you specified 
during the installation process).
You can change the port number using File->Settings in the server program, or 
by changing the 'tomcat.port' property in build.properties, then running 
'ant configure'.

Building Collections:
----------------------------------------------------------

You need to have Perl installed and on your PATH. Perl is included as part of 
the Windows binary distribution.

You can build collections using the Greenstone Librarian Interface (GLI). 
To start GLI, select it from the Start Menu (Windows), run 'ant gli' from the 
greenstone3 directory, or cd to greenstone3/gli and run gli.sh/bat. 

Once you have created and built a collection, you can see it by clicking 'Preview collection' on the Build panel.

Java/Tomcat Notes
--------------------------------------------------------

Binary releases of Greenstone are compiled with Java 1.5 and come with JRE 1.5 and Tomcat 6. Tomcat 6 does not work with Java 1.4.

To recompile the source from a binary release: 
* Download the source code component and unpack into Greenstone3 directory.
* You will need to install a JDK, 1.5 or later.
* Rename the packages/jre folder to something else (eg jre.old)
* Set JAVA_HOME environment to be the root of your Java installation.
* run 'ant install'

If you wish to use Java 1.4:
* Do all the above steps but before running ant install,
* delete packages/tomcat/.flagfile and run 'ant prepare-tomcat' to get Tomcat 5.

Greenstone 3 from SVN will download Tomcat 5 if you are using Java 1.4, otherwise will download Tomcat 6.

Greenstone Admin
------------------------------------------------------

The Greenstone admin tool is currently under development.

Using SOAP:
-------------------------------------------------------

Greenstone comes with Apache Axis installed as part of the Greenstone web 
application. However, no SOAP services are deployed by default.

To deploy a SOAP server for localsite, run 'ant deploy-localsite'. You should 
now be able to see all localsite's collections through the gateway servlet. 
(http://localhost:8080/greenstone3/gateway)

To set up a SOAP server on a new site, run
ant soap-deploy-site
This will prompt you for the sitename (the directory name), and the site uri 
- this should be a unique identifier for the site's web service. 

For a non-interactive version, run
ant -Daxis.sitename=xxx -Daxis.siteuri=yyy soap-deploy-site

The service is accessible at 
http://localhost:8080/greenstone3/services/<siteuri>

(or http://<computer-web-address>:<port>/greenstone3/services/<siteuri>)

Note: Deploying a SOAP service for any site other than localsite requires 
the Greenstone source code to be installed. This is not installed by default 
for a binary distribution. To get the source code, re-run the installer, 
select custom install and deselect everything except the source code.

Using External Tomcat:
---------------------------------------------------

If you want to use an existing Tomcat, set the path to its base directory
in build.properties (tomcat.installed.path). Also set the tomcat.port 
property to be the port you are running Tomcat on, and change tomcat.server 
if the web address is not localhost. Then run 'ant configure'.

 You will need to modify the Tomcat setup slightly.

1. Tell Tomcat about the Greenstone web app. There are two ways to do this.

A. Copy the file greenstone3/resources/tomcat/greenstone3.xml into Tomcat's 
conf/Catalina/localhost directory. You'll need to edit the file and 
replace @gsdl3webhome@ with the full path to the web directory of your 
greenstone 3 installation. Any path separator is fine here ('/', '\\', '\').

B. Alternatively, you can move (and rename) the greenstone3/web directory to 
tomcat/webapps/greenstone3 (i.e. the resulting directories will be like
tomcat/webapps/greenstone3/WEB-INF, no web directory). This should be done 
after running the initial 'ant install'.

You will need to set the web.home property in the build.properties file
i.e.
web.home=${tomcat.installed.path}/webapps/greenstone3
And then run 'ant configure' to reset gsdl3home.

2. Set up the JNI libraries and Java wrappers.
JNI libraries and their Java wrappers cannot go into the web app. The 
libraries need to be loaded by the same class loader as their wrappers. The 
libraries need to be in java.library.path, and I think get loaded by the 
system class loader.
The wrappers need to be loaded by this too. 

These JNI bits are located by default in the lib/jni directory. There are 
two ways to get them into Tomcat:
A: Keep all the Greenstone stuff inside the greenstone3 directory, and just 
modify the environment that Tomcat runs in

Set LD_LIBRARY_PATH (GNU/Linux), DYLD_LIBRARY_PATH (Mac OS X) or PATH/Path 
(windows) to include the  greenstone3/lib/jni directory.
Add all the jar files in greenstone3/lib/jni directory to the CLASSPATH, 
then edit tomcats setclasspath.sh/bat to use the system CLASSPATH.
(in setclasspath.bat, change 
set CLASSPATH=%JAVA_HOME%\lib\tools.jar
to 
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%CLASSPATH%

in setclasspath.sh, change
# Set standard CLASSPATH
if [ "$1" = "debug" -o "$1" = "javac" ] ; then
  CLASSPATH="$JAVA_HOME"/lib/tools.jar
fi

to
# Set standard CLASSPATH
if [ "$1" = "debug" -o "$1" = "javac" ] ; then
  CLASSPATH="$JAVA_HOME"/lib/tools.jar:"$CLASSPATH"
fi


B: Copy the files into Tomcat installation:
Move the greenstone3/lib/jni jar files into tomcat's shared/lib directory.
Move the greenstone3/lib/jni library files (.so for GNU/Linux, .jnilib for 
Mac OS X .dll for Windows) into shared/classes, and set LD_LIBARARY_PATH 
(GNU/Linux), DYLD_LIBRARY_PATH (Mac OS X) or PATH/Path (Windows) to include 
this directory.
 This has the advantage that you can use this for other webapps without 
modifying the Tomcat environment.

Once all these changes have been made, you will need to restart the Tomcat 
server for them to take effect.


Notes for Mac OS
------------------------------------------------

Set JAVA_HOME to be /Library/Java/Home

Notes for Windows
-----------------------------------------------

You can set environment variables by going to 
Control Panel->System->Advanced->Environment Variables. 

Installing from a Source Distribution
----------------------------------------------

Download the greenstone-3.xx-src.tar.gz package from 
sourceforge.net/projects/greenstone3, and unpack it.

In the greenstone3 directory, edit the build.properties file and run 
ant install

Tomcat will be installed as part of the prepare process. To stop this set the 
tomcat.installed.path to be the root of an existing Tomcat installation. 

* Solaris notes:
** Make sure /usr/local/bin is in your PATH ahead of /usr/bin etc.
** Add /usr/local/lib to LD_LIBARY_PATH
** The gdbm database files (gs2mgdemo and gs2mgppdemo collections) were generated on a Mac, and don't seem to be compatible with Solaris. A text version of the file (database.txt) is included in the index/text directory. After installing Greenstone, and before running it, you'll need to:
in greenstone3/gs2build directory: run 'source setup.bash' (Or if you have greenstone 2 already installed, run 'source setup.bash' in your greenstone 2 installation)
in greenstone3/web/sites/localsite/collect/gs2mgdemo/index/text directories, run 'txt2db gs2mgdemo.bdb < database.txt'
in greenstone3/web/sites/localsite/collect/gs2mgppdemo/index/text directories, run 'txt2db gs2mgppdemo.bdb < database.txt'
** GLI shell scripts may not work like "./gli4gs3.sh". In this case, run "bash ./gli4gs3.sh" etc. You will need to compile GLI by hand (run "bash ./makegli.sh" in greenstone3/gli directory).
** Set CC=gcc environment variable if you don't have cc, before running ant install.

* Windows notes:
** You need to have Visual Studio installed to compile the C/C++ code. Set the path to the setup file in build.properties (compile.windows.c++.setup).

Configuring your installation:
--------------------------------------------------------

The file build.properties contains various parameters that can be set by the user. Please check these settings before running the install.
Note, either forward slash '/' or double backslash '\\' can be used as path separators in the build.properties file, but not single backslash '\'. 

Greenstone 3 comes with Tomcat bundled in.  

If you already have Tomcat running, you can set the 'tomcat.installed.path' property (in build.properties) to the base directory of your Tomcat installation, and Greenstone will not use its own Tomcat. (You can delete the packages/tomcat directory if you like.) Please read the section "Using External Tomcat" for details about how to configure Tomcat for Greenstone.

Mac OS X: You need to have GDBM installed (http://www.gnu.org/software/gdbm/gdbm.html). Please set the gdbm.installed.path property (in build.properties) to the root of your gdbm installation if it is not installed in a default place. If you run GLI or GS2 collection building from the command line, you will need to set the DYLD_LIBRARY_PATH environment variable to include <path-to-gdbm>/lib.

The install target will ask you if you accept the properties before starting.
To suppress this prompt, use the -Dproperties.accepted=yes flag. E.g.
ant -Dproperties.accepted=yes install 

To log the output in build.log, run
ant -Dproperties.accepted=yes -logfile build.log  install 

Recompiling
-------------------------------------------------------
To recompile your Greenstone3 installation, in the top level greenstone3 directory, run:

ant clean (use distclean instead if you want to regenerate the C++ Makefiles)
ant configure
ant configure-c++
ant compile

The compile target does Java and C/C++ compilation. On Windows, you need to set the compile.windows.c++.setup property to be your Visual Studio setup batch file. 

Any sub targets can be run by themselves. Run 'ant -projecthelp' for a list of public targets, otherwise you can look at the build.xml file to see which targets depend on other ones.


Notes on Versions of Third Party packages
-----------------------------------------------

Tomcat:

apache-tomcat-5.5.12.zip: latest production quality release as of October, 2005.
apache-tomcat-5.5.12-compat.zip: Tomcat 5 requires Java 1.5. If using Java 1.4, need to use this compatibility module.

website: http://tomcat.apache.org/
download: http://tomcat.apache.org/download-55.cgi

Axis:

Apache Web Services Project, SOAP implementation. Axis is a follow on project to Apache SOAP

axis-bin-1_2_1.zip: latest stable release as of October, 2005
website: http://ws.apache.org/axis/
download: http://www.apache.org/dyn/closer.cgi/ws/axis/1_2_1

All available from www.greenstone.org/gs3files if not available at their respective websites.

Other Notes:
-------------------------------------------------

See greenstone3/docs/manual/manual.pdf for more details about the software and installation etc.

Output is logged to web/logs. usage.log is a usage log, while greenstone.log is the error/message log. To change the level of logging, edit the web/WEB-INF/classes/log4j.properties file, and change the log4j.disable and log4j.rootCategory properties. Valid values are TRACE, DEBUG, INFO, WARN, ERROR and FATAL.

To prevent Tomcat showing directory listings, edit Tomcat's conf/web.xml file and set the value of the "listings" servlet parameter to false.

To enable symlinks to files outside the webapp root directory, edit Tomcat's conf/Catalina/localhost/greenstone3.xml file, and set the allowLinking attribute in the Context element to true.
(Note from Tomcat website: This flag MUST NOT be set to true on the Windows platform (or any other OS which does not have a case sensitive filesystem), as it will disable case sensitivity checks, allowing JSP source code disclosure, among other security problems.)

The file web/WEB-INF/classes/global.properties is generated on install and contains some properties for the run time system.

