/*
 *    DerbyWrapper.java
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

public class DerbyWrapper
{
	static final String PROTOCOL = "jdbc:derby:";
	static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	static final String USERSDB = "usersDB";
	static final String USERS = "users";
	private Connection conn = null;
	private Statement state = null;
	private String protocol_str;

	public DerbyWrapper()
	{
	}

	public DerbyWrapper(String dbpath)
	{
		connectDatabase(dbpath, false);
	}

	public void connectDatabase(String dbpath, boolean create_database)
	{
		try
		{
			Class.forName(DRIVER).newInstance();
			//System.out.println("Loaded the embedded driver.");
			protocol_str = PROTOCOL + dbpath;
			if (create_database)
			{
				conn = DriverManager.getConnection(protocol_str + ";create=true");
			}
			else
			{
				conn = DriverManager.getConnection(protocol_str);
			}
			state = conn.createStatement();
		}
		catch (Throwable e)
		{
			System.out.println("exception thrown:");
			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				e.printStackTrace();
			}
		}
	}

	public void closeDatabase()
	{
		state = null;
		conn = null;
		boolean gotSQLExc = false;
		try
		{
			DriverManager.getConnection(protocol_str + ";shutdown=true");
		}
		catch (SQLException se)
		{
			gotSQLExc = true;
		}
		if (!gotSQLExc)
		{
			System.out.println("Database did not shut down normally");
		}
	}

	public void createDatabase() throws SQLException
	{
		conn.setAutoCommit(false);
		state.execute("create table users (username varchar(40) not null, password varchar(40) not null, groups varchar(500), accountstatus varchar(10), comment varchar(100), primary key(username))");
		//ystem.out.println("table users created successfully!");
		state.execute("insert into " + USERS + " values ('admin', 'admin', 'administrator,all-collections-editor', 'true', 'change the password for this account as soon as possible')");
		conn.commit();
	}

	public UserQueryResult listAllUser() throws SQLException
	{
		UserQueryResult userQueryResult = new UserQueryResult();
		String sql_list_all_user = "select username, password, groups, accountstatus, comment from " + USERS;

		ResultSet rs = state.executeQuery(sql_list_all_user);
		while (rs.next())
		{
			String returned_username = rs.getString("username");
			String returned_password = rs.getString("password");
			String returned_groups = rs.getString("groups");
			String returned_accountstatus = rs.getString("accountstatus");
			String returned_comment = rs.getString("comment");
			userQueryResult.addUserTerm(returned_username, returned_password, returned_groups, returned_accountstatus, returned_comment);
		}
		if (userQueryResult.getSize() == 0)
		{
			System.out.println("couldn't find any users");
			return null;
		}
		else
		{
			return userQueryResult;
		}
	}

	public String addUser(String username, String password, String groups, String accountstatus, String comment) throws SQLException
	{
		conn.setAutoCommit(false);
		String sql_insert_user = "insert into " + USERS + " values ('" + username + "', '" + password + "', '" + groups + "', '" + accountstatus + "', '" + comment + "')";
		try
		{
			state.execute(sql_insert_user);
			conn.commit();
		}
		catch (Throwable e)
		{
			System.out.println("exception thrown:");
			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				e.printStackTrace();
			}
			closeDatabase();
			System.out.println("Error:" + e.getMessage());
			return "Error:" + e.getMessage();
		}
		return "succeed";
	}

	public String deleteUser(String del_username) throws SQLException
	{
		conn.setAutoCommit(false);
		String sql_delete_user = "delete from " + USERS + " where username='" + del_username + "'";
		try
		{
			state.execute(sql_delete_user);
			conn.commit();
		}
		catch (Throwable e)
		{
			System.out.println("exception thrown:");
			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				e.printStackTrace();
			}
			closeDatabase();
			return "Error:" + e.getMessage();
		}
		return "succeed";
	}

	public boolean deleteAllUser() throws SQLException
	{
		conn.setAutoCommit(false);
		try
		{
			state.execute("delete from " + USERS);
			conn.commit();
		}
		catch (Throwable e)
		{
			System.out.println("exception thrown:");
			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				e.printStackTrace();
			}
			closeDatabase();
			return false;
		}
		return true;
	}

	public UserQueryResult findUser(String username, String password) throws SQLException
	{
		UserQueryResult userQueryResult = new UserQueryResult();

		conn.setAutoCommit(false);
		String sql_find_user = "SELECT  username, password, groups, accountstatus, comment FROM " + USERS;
		String append_sql = "";

		if (username != null)
		{
			append_sql = " WHERE username = '" + username + "'";
		}
		if (password != null)
		{
			if (append_sql.equals(""))
			{
				append_sql = " WHERE password = '" + password + "'";
			}
			else
			{
				append_sql += " and password = '" + password + "'";
			}
		}
		if (!append_sql.equals(""))
		{
			sql_find_user += append_sql;
		}
		ResultSet rs = state.executeQuery(sql_find_user);
		while (rs.next())
		{
			String returned_username = rs.getString("username");
			//System.out.println("returned_username :" + returned_username);
			String returned_password = rs.getString("password");
			//System.out.println("returned_password :" + returned_password);
			String returned_groups = rs.getString("groups");
			//System.out.println("returned_groups :" + returned_groups);
			String returned_accountstatus = rs.getString("accountstatus");
			//System.out.println("returned_accountstatus :" + returned_accountstatus);
			String returned_comment = rs.getString("comment");
			//System.out.println("returned_comment :" + returned_comment);
			userQueryResult.addUserTerm(returned_username, returned_password, returned_groups, returned_accountstatus, returned_comment);
			//System.out.println(userQueryResult.toString());
		}
		conn.commit();
		if (userQueryResult.getSize() > 0)
		{
			return userQueryResult;
		}
		else
		{
			System.out.println("couldn't find the user");
			return null;
		}
	}

	public String modifyUserInfo(String username, String new_password, String groups, String accountstatus, String comment) throws SQLException
	{
		conn.setAutoCommit(false);
		String sql_modify_user_info = "update " + USERS + " set ";
		if (new_password != null && !new_password.equals(""))
		{
			sql_modify_user_info += "password='" + new_password + "'";
		}

		if (groups != null && accountstatus != null && comment != null)
		{
			sql_modify_user_info += ", groups='" + groups + "'" + ", accountstatus='" + accountstatus + "'" + ", comment='" + comment + "'";
		}
		sql_modify_user_info += " where username='" + username + "'";

		try
		{
			System.out.println(sql_modify_user_info);
			state.execute(sql_modify_user_info);
			conn.commit();
		}
		catch (Throwable e)
		{
			System.out.println("exception thrown:");
			if (e instanceof SQLException)
			{
				printSQLError((SQLException) e);
			}
			else
			{
				e.printStackTrace();
			}
			closeDatabase();
			return "Error:" + e.getMessage();
		}
		return "succeed";
	}

	public void db2txt() throws SQLException
	{
		UserQueryResult userQueryResult = new UserQueryResult();
		String sql_list_all_user = "select username, password, groups, accountstatus, comment from " + USERS;
		ResultSet rs = state.executeQuery(sql_list_all_user);

		while (rs.next())
		{
			String returned_username = rs.getString("username");
			System.out.println("[" + returned_username + "]");
			String returned_comment = rs.getString("comment");
			System.out.println("<comment>" + returned_comment);
			String returned_accountstatus = rs.getString("accountstatus");
			System.out.println("<enabled>" + returned_accountstatus);
			String returned_groups = rs.getString("groups");
			System.out.println("<groups>" + returned_groups);
			String returned_password = rot13(rs.getString("password"));
			System.out.println("<password>" + returned_password);
			System.out.println("<username>" + returned_username);
			System.out.println("");
			System.out.println("----------------------------------------------------------------------");
		}
		conn.commit();
		closeDatabase();
	}

	static void printSQLError(SQLException e)
	{
		while (e != null)
		{
			System.out.println(e.toString());
			e = e.getNextException();
		}
	}

	//Simply use rot-13 to encrypt and decrypt the password
	public String rot13(String password)
	{
		String out_password = "";
		for (int i = 0; i < password.length(); i++)
		{
			char c = password.charAt(i);
			if (c >= 'a' && c <= 'm')
				c += 13;
			else if (c >= 'n' && c <= 'z')
				c -= 13;
			else if (c >= 'A' && c <= 'M')
				c += 13;
			else if (c >= 'A' && c <= 'Z')
				c -= 13;
			out_password += c;
		}
		return out_password;
	}
}
