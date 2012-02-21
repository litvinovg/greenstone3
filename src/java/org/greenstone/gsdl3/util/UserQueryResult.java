/*
 *    UserQueryResult.java
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

import java.util.Vector;
import org.greenstone.gsdl3.util.UserTermInfo;

public class UserQueryResult{
    /** the list of UserInfo */
    public Vector<UserTermInfo> users_=null;

    UserQueryResult(){
	users_=new Vector<UserTermInfo>();
    }

    public void clear(){
	users_.clear();
    }

    public void addUserTerm(String username, String password, String groups, String accountstatus, String comment){
	UserTermInfo ui=new UserTermInfo();
	ui.username_=username;
	ui.password_=password;
	ui.groups_=groups;
	ui.accountstatus_=accountstatus;
	ui.comment_=comment;
	users_.add(ui);
    }

    public Vector<UserTermInfo> getUserTerms(){
	return users_;
    }

    public String toString(){
	String result="";
	for (int i=0;i<users_.size();i++){
	    result +=((UserTermInfo)users_.elementAt(i)).toString()+", ";
	}
	
	return result;
    }

    public int getSize(){
	return users_.size();
    }
}
