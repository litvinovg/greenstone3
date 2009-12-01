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
import java.io.FileReader;
import java.sql.SQLException;

public class txt2usersDB {
    
    public static void main(String[] args) throws SQLException{
	
	if (args.length!=2){
	    System.out.println("Usage: java org.greenstone.gsdl3.txt2usersDB full_path_of_the_text_file full_path_of_the_usersDB");
	    System.exit(0);
	}
	try {
	    BufferedReader in = new BufferedReader(new FileReader(args[0]));
	    String str;
	    DerbyWrapper dw=new DerbyWrapper();
	    dw.connectDatabase(args[1],false);
	    boolean delete_rows = dw.deleteAllUser();
	    if (!delete_rows){
		System.out.println("Couldn't delete rows of the users table");
		System.exit(0);
	    }
	    String username=null;
	    String password=null;
	    String groups=null;
	    String accountstatus=null;
	    String comment=null;
	    while ((str = in.readLine()) != null) {
		//ystem.out.println(str);
		if (str.startsWith("<")){
		    String field=str.substring(1,str.indexOf(">"));
		    if (field.equals("comment")){
			comment=str.substring(str.indexOf(">")+1,str.length());
		    }
		    if (field.equals("enabled")){
			accountstatus=str.substring(str.indexOf(">")+1,str.length());
		    }
		    if (field.equals("groups")){
			groups=str.substring(str.indexOf(">")+1,str.length());
		    }
		    if (field.equals("password")){
			password=dw.rot13(str.substring(str.indexOf(">")+1,str.length()));
		    }
		    if (field.equals("username")){
			username=str.substring(str.indexOf(">")+1,str.length());
		    }
		}
		if (str.equals("----------------------------------------------------------------------")){
		    if ((username!=null) && (password!=null) && (groups!=null) && (accountstatus!=null) && (comment!=null)){
			dw.connectDatabase(args[1],false);
			dw.addUser(username, password, groups, accountstatus, comment);
			username=null;
			password=null;
			groups=null;
			accountstatus=null;
			comment=null;
			dw.connectDatabase(args[1],false);
		    }
		}
	    }	
	    in.close();
	} catch (IOException e) {
	}
    }
}
