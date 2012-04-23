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

import java.sql.SQLException;
import org.greenstone.gsdl3.service.Authentication;
//import org.greenstone.admin.guiext.PropertiesStep;

// To run this from the command-line, first make sure that the tomcat server is stopped, then run:
// java -cp /full/path/to/GS3/web/WEB-INF/lib/gsdl3.jar:/full/path/to/GS3/web/WEB-INF/lib/derby.jar org.greenstone.gsdl3.util.usersDB2txt web/sites/localsite/etc/usersDB/
public class ModifyUsersDB {
    
    public static void main(String[] args) throws SQLException {
	
	if (args.length < 3) { // at minimum one field belonging to a username has to be changed
	    System.out.println("Usage: java org.greenstone.gsdl3.ModifyUsersDB <full_path_of_the_usersDB> <username> [-noAdd] [password=pwd] [groups=grp] [accounstatus=status] [comment=cmt] [email=address]");
	    System.exit(0);
	}

	DerbyWrapper dw=new DerbyWrapper();
	
	String usersDB = args[0];
	String username = args[1];

	String password = "";
	String groups = "";
	String accountstatus = "true";
	String comment = "";
	String email = "";

	boolean noAdd = false;
	
	// If the user specifically sets any of the fields on the cmdline, they'll be overwritten in the db, 
	// even if the user had set them to empty. Except the password which must be between 3 and 8 characters.
	for(int i = 2; i < args.length; i++) {
	    if(args[i].startsWith("password=")) {
		password = args[i].substring("password=".length());
		
		if(password.length() < 3 || password.length() > 8) {
		    if(!password.equals("")) {
			System.out.println("Password not updated. It should be between 3 and 8 characters (inclusive).");
		    }
		} else {
		    // Use the same encryption technique used by the Admin Authentication page
		    // This ensures that the password generated for a string remains consistent
		    //System.err.println("**** Password entered was: " + password);
		    password = Authentication.hashPassword(password);
		}
		
	    } else if(args[i].startsWith("groups=")) {
		groups = args[i].substring("groups=".length());
	    } else if(args[i].startsWith("accountstatus=")) {
		accountstatus = args[i].substring("accountstatus=".length());
	    } else if(args[i].startsWith("comment=")) {
		comment = args[i].substring("comment=".length());
	    } else if(args[i].startsWith("email=")) {
		email = args[i].substring("email=".length());
	    } else if(args[i].equals("-noAdd")) {
		noAdd = true;
	    }
	}
	    

	// find the user to modify
	dw.connectDatabase(usersDB,false);
	UserQueryResult findUserResult = dw.findUser(username);
	
	if(findUserResult == null) {
	    if(noAdd) {
		System.out.println("Failed to update user. Cannot find user " + username + " in " + usersDB + " database.");
	    } else { // add new user
		//System.err.println("**** Trying to add user: ");
		//System.err.println("**** " + username + " " + password + " " + groups + " " + accountstatus + " " + comment + " " + email);
		dw.addUser(username, password, groups, accountstatus, comment, email);
	    }
	}
	else { // modify existing user data
	    
	    // in case any of the other fields are not specified, get fallbacks from the database
	    UserTermInfo user = findUserResult.getUserTerms().get(0);

	    if(password.equals("")) { 
		password = user.password; // already stored hashed-and-hexed in DB
	    }
	    if(groups.equals("")) {
		groups = user.groups;
	    }
	    if(accountstatus.equals("")) { 
		accountstatus = user.accountstatus.equals("") ? "true" : user.accountstatus;
	    }
	    if(comment.equals("")) { 
		comment = user.comment; 
	    }
	    if(email.equals("")) {
		email = user.email;
	    }    
	    
	    //System.err.println("**** " + username + " " + password + " " + groups + " " + accountstatus + " " + comment + " " + email);
	    dw.modifyUserInfo(username, password, groups, accountstatus, comment, email); // other than username and pwd, remaining fields are allowed to be ""
	}
	    
	dw.closeDatabase();
	
    }
}
