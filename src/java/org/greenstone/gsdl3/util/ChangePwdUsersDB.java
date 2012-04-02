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

public class ChangePwdUsersDB {
    
    public static void main(String[] args) throws SQLException {
	
	if (args.length!=3){
	    System.out.println("Usage: java org.greenstone.gsdl3.ChangePwdUsersDB <full_path_of_the_usersDB> <username> <password>");
	    System.exit(0);
	}

	DerbyWrapper dw=new DerbyWrapper();
	
	String usersDB = args[0];
	String username = args[1];
	String password = args[2];
	
	if(password.length() < 3 || password.length() > 8) {
	    System.err.println("Password should be between 3 and 8 characters (inclusive).");
	} else {
	    // find the user to modify
	    dw.connectDatabase(usersDB,false);
	    UserQueryResult findUserResult = dw.findUser(username);
	    
	    if(findUserResult == null) {
		System.err.println("Failed to change password. Cannot find user " + username + " in " + usersDB + " database.");
	    }
	    else { // modify existing user data, now we have a (valid) password
		
		// if any of the other fields are not specified, get them from the database
		UserTermInfo user = findUserResult.getUserTerms().get(0);
		String groups = user.groups;
		String accountstatus = user.accountstatus;
		String comment = user.comment;
		String email = user.email;
		
		// Use the same encryption technique used by the Admin Authentication page
		// This ensures that the password generated for a string remains consistent
		password = Authentication.hashPassword(password);
		//System.err.println("**** Password: " + password);
		
		//System.err.println("**** " + username + " " + password + " " + groups + " " + accountstatus + " " + comment + " " + email);
		dw.modifyUserInfo(username, password, groups, accountstatus, comment, email); // remaining fields are allowed to be ""
	    }
	    
	    dw.closeDatabase();
	}
    }
}
