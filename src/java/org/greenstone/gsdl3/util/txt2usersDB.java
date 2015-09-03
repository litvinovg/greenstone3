/*
 *    txt2usersDB.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;

import org.greenstone.gsdl3.service.Authentication;

/** 
    To run this from the command-line, first make sure that the derby networked server is running (ant start-derby),
    then run:

    java -Dgsdl3.writablehome=/full/path/to/GS3/web -cp web/WEB-INF/lib/gsdl3.jar:web/WEB-INF/lib/gutil.jar:web/WEB-INF/lib/derby.jar:./web/WEB-INF/lib/derbyclient.jar:./web/WEB-INF/lib/log4j-1.2.8.jar:./web/WEB-INF/classes org.greenstone.gsdl3.util.txt2usersDB <filename>.txt web/etc/usersDB/ [-append]

    Don't forget to stop the networked derby server again at the end, if you had started it: ant stop-derby

    Or if using embedded derby, ensure that tomcat is stopped, then run:
    java -cp /full/path/to/GS3/web/WEB-INF/lib/gsdl3.jar:/full/path/to/GS3/web/WEB-INF/lib/derby.jar org.greenstone.gsdl3.util.txt2usersDB <filename>.txt web/etc/usersDB/ [-append]
*/
public class txt2usersDB
{

	public static void main(String[] args) throws SQLException
	{
		boolean appending = false;

		String usage = "Usage: java org.greenstone.gsdl3.txt2usersDB full_path_of_the_text_file full_path_of_the_usersDB [-append]";
		if (args.length < 2)
		{
			System.out.println(usage);
			System.exit(0);
		}
		File txtfile = new File(args[0]);
		if (!txtfile.exists())
		{
			System.out.println("File " + args[0] + " does not exist.");
			System.out.println(usage);
			System.exit(0);
		}

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			String str;
			DerbyWrapper dw = new DerbyWrapper(args[1]);

			if (args.length > 2 && args[2].equals("-append"))
			{
				appending = true;
			}
			else
			{
				// no appending, replace existing database: the text file 
				// represents the new database, so delete the existing DB first
				boolean delete_rows = dw.deleteAllUser();
				dw.closeDatabase();
				if (!delete_rows)
				{
					System.out.println("Couldn't delete rows of the users table");
					System.exit(0);
				}
			}

			String username = null;
			String password = null;
			String groups = null;
			String accountstatus = null;
			String comment = null;
			String email = null;

			while ((str = in.readLine()) != null)
			{
				//ystem.out.println(str);

				if (str.indexOf(" = ") != -1)
				{ // works with DerbyWrapper.db2txt() and usersDB2txt.java. Fields listed as: USERNAME = admin
					String field = str.substring(0, str.indexOf(" = "));
					if (field.equalsIgnoreCase("email"))
					{
						email = str.substring(str.indexOf(" = ") + 3, str.length());
					}
					if (field.equalsIgnoreCase("comment"))
					{
						comment = str.substring(str.indexOf(" = ") + 3, str.length());
					}
					if (field.equalsIgnoreCase("status"))
					{
						accountstatus = str.substring(str.indexOf(" = ") + 3, str.length());
					}
					if (field.equalsIgnoreCase("groups"))
					{
						groups = str.substring(str.indexOf(" = ") + 3, str.length());
					}
					if (field.equalsIgnoreCase("password"))
					{
						//password=dw.rot13(str.substring(str.indexOf(">")+1,str.length()));
						password = str.substring(str.indexOf(" = ") + 3, str.length());
					}
					if (field.equalsIgnoreCase("username"))
					{
						username = str.substring(str.indexOf(" = ") + 3, str.length());
					}
				}
				else if (str.startsWith("<"))
				{ // fields listed as: <username>admin
					String field = str.substring(1, str.indexOf(">"));
					if (field.equals("email"))
					{
						email = str.substring(str.indexOf(">") + 1, str.length());
					}
					if (field.equals("comment"))
					{
						comment = str.substring(str.indexOf(">") + 1, str.length());
					}
					if (field.equals("enabled") || field.equals("status"))
					{
						accountstatus = str.substring(str.indexOf(">") + 1, str.length());
					}
					if (field.equals("groups"))
					{
						groups = str.substring(str.indexOf(">") + 1, str.length());
					}
					if (field.equals("password"))
					{
						password = str.substring(str.indexOf(">") + 1, str.length());
					}
					if (field.equals("username"))
					{
						username = str.substring(str.indexOf(">") + 1, str.length());
					}
				}
				else if (str.equals("----------------------------------------------------------------------") || str.equals("-------------------------------------"))
				{

					if ((username != null) && (password != null) && (groups != null) && (accountstatus != null) && (comment != null))
					{
						dw.connectDatabase(args[1], false);

						// check if it's a new user or already exists in the database
						UserQueryResult findUserResult = dw.findUser(username);

						if (findUserResult == null)
						{ // add new user
							if (password.length() >= 3 && password.length() <= 8)
							{ // if not yet encrypted, encrypt first
								password = Authentication.hashPassword(password);
							} // if > 8 chars, password for user being added was already encrypted (hashed-and-hexed)
							dw.addUser(username, password, groups, accountstatus, comment, email);
						}

						else
						{ // modify existing user
							// if any of the other fields are not specified, get them from the database
							UserTermInfo user = findUserResult.getUserTerms().get(0);

							if (password.length() < 3 || password.length() > 8)
							{ // includes empty string case
								password = user.password;
							}
							else
							{ // need to first encrypt (hash-and-hex) the user-entered password
								// Use the same encryption technique used by the Admin Authentication page
								// This ensures that the password generated for a string remains consistent
								password = Authentication.hashPassword(password);
							}
							groups = groups.equals("") ? user.groups : groups;
							accountstatus = accountstatus.equals("") ? user.accountstatus : accountstatus;
							comment = comment.equals("") ? user.comment : comment;

							if (email == null)
							{ // special checking for backwards compatibility since old DB did not have email field
								email = "";
							}
							if (user.email == null)
							{
								user.email = "";
							}
							if (email.equals(""))
							{
								email = user.email;
							}

							//System.err.println("**** Password: " + password);				
							//System.err.println("**** " + username + " " + password + " " + groups + " " + accountstatus + " " + comment + " " + email);
							dw.modifyUserInfo(username, password, groups, accountstatus, comment, email);
						}

						username = null;
						password = null;
						groups = null;
						accountstatus = null;
						comment = null;
						email = null;
						//dw.connectDatabase(args[1],false); // should this be closeDatabase()????
						dw.closeDatabase();
					}
				}

				// only true back when when hashed passwords weren't being converted to hex
				//else { // encrypted passwords can span multiple lines for some reason
				// assume that is the case here
				//if(password != null) { 
				//	password = password + "\n" + str;
				//  }
				//}

			}
			//dw.closeDatabase();
			in.close();
		}
		catch (IOException e)
		{
		}
	}
}
