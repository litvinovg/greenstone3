/*
 *    usersDB2txt.java
 *    Copyright (C) 2008 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;

/** 
    To run this from the command-line, first make sure that the networked derby server is running (ant start-derby)
    then run: 
 
    java -Dgsdl3.writablehome=/full/path/to/GS3/web -cp ./web/WEB-INF/lib/gsdl3.jar:./web/WEB-INF/lib/gutil.jar:./web/WEB-INF/lib/derby.jar:./web/WEB-INF/lib/derbyclient.jar:./web/WEB-INF/lib/log4j-1.2.8.jar:./web/WEB-INF/classes org.greenstone.gsdl3.util.usersDB2txt web/etc/usersDB/

    if redirecting to a file append ">& filename.txt" to the above command 
    since the usersDB2txt program output goes to System.err and needs to be redirected to the file too

    Don't forget to stop the networked derby server again at the end, if you had started it: ant stop-derby

    Or if using embedded derby, ensure that tomcat is stopped, then run:
    java -cp /full/path/to/GS3/web/WEB-INF/lib/gsdl3.jar:/full/path/to/GS3/web/WEB-INF/lib/derby.jar org.greenstone.gsdl3.util.usersDB2txt web/etc/usersDB/ [>& <output file>]
*/
public class usersDB2txt
{
    public static void main(String[] args) throws SQLException{
	if (args.length!=1){
	    System.out.println("The path of usersDB has to be given!");
	    System.exit(0);
	}
	DerbyWrapper derbyWrapper=new DerbyWrapper(args[0]);
	derbyWrapper.db2txt();
    }
}
