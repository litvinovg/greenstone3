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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.greenstone.gsdl3.service.Authentication;
import org.greenstone.util.GlobalProperties;

public class DerbyWrapper
{
	static final String PROTOCOL = "jdbc:derby:";
	static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	static final String USERSDB = "usersDB";
	static final String USERS = "users";
	static final String ROLES = "roles";
	static final String DATA = "data";
	private Connection conn = null;
	private static String protocol_str;

	public DerbyWrapper(String dbpath)
	{
		connectDatabase(dbpath, false);
	}

	public static void createDatabaseIfNeeded()
	{
		String usersDB_dir = GlobalProperties.getGSDL3Home() + File.separatorChar + "etc" + File.separatorChar + "usersDB";
		protocol_str = PROTOCOL + usersDB_dir;
		File usersDB_file = new File(usersDB_dir);
		if (!usersDB_file.exists())
		{
			String etc_dir = GlobalProperties.getGSDL3Home() + File.separatorChar + "etc";
			File etc_file = new File(etc_dir);
			if (!etc_file.exists())
			{
				boolean success = etc_file.mkdir();
				if (!success)
				{
					System.err.println("Couldn't create the etc dir under " + GlobalProperties.getGSDL3Home() + ".");
				}
			}
			try
			{
				DerbyWrapper.createDatabase(DriverManager.getConnection(protocol_str + ";create=true"));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void connectDatabase(String dbpath, boolean create_database)
	{
		try
		{
			if (conn != null)
			{
				System.err.println("Connection already established, close the database first");
				return;
			}

			Class.forName(DRIVER).newInstance();
			protocol_str = PROTOCOL + dbpath;
			if (create_database)
			{
				conn = DriverManager.getConnection(protocol_str + ";create=true");
			}
			else
			{
				conn = DriverManager.getConnection(protocol_str);
			}
			conn.setAutoCommit(false);
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
		try
		{
			conn.commit();
			conn.close();
			conn = null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void shutdownDatabaseServer()
	{
		boolean gotSQLExc = false;

		try
		{
			//  shutdown the whole server
			DriverManager.getConnection(PROTOCOL + ";shutdown=true");
		}
		catch (SQLException se)
		{
			// this is good (i.e. what Derby is designed to do on a successful shutdown)
			gotSQLExc = true;
			//System.out.println("Shutdown returned: " + se);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!gotSQLExc)
		{
			System.err.println("Warning: Derby did not shut down normally");
		}
	}

	public void clearUserData()
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("drop table data");
			state.execute("create table data (username varchar(40) not null, name varchar(128) not null, value clob, primary key (username, name))");
			conn.commit();
			state.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void clearTrackerData()
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("drop table usertracker");
			state.execute("create table usertracker (username varchar(40) not null, collection varchar(128) not null, site varchar(128) not null, oid varchar(128) not null, time varchar(128) not null, action varchar(128) not null, primary key (username, time))");
			conn.commit();
			state.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void createDatabase(Connection conn)
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("create table users (username varchar(40) not null, password varchar(40) not null, accountstatus varchar(10), comment varchar(100), email varchar(40), primary key(username))");
			state.execute("create table roles (username varchar(40) not null, role varchar(40) not null, primary key (username, role))");
			state.execute("create table data (username varchar(40) not null, name varchar(128) not null, value clob, primary key (username, name))");
			state.execute("create table usertracker (username varchar(40) not null, collection varchar(128) not null, site varchar(128) not null, oid varchar(128) not null, time varchar(128) not null, action varchar(128) not null, primary key (username, time))");

			state.execute("insert into " + USERS + " values ('admin', '" + Authentication.hashPassword("admin") + "', 'true', 'change the password for this account as soon as possible', '')");
			state.execute("insert into " + ROLES + " values ('admin', 'administrator')");
			state.execute("insert into " + ROLES + " values ('admin', 'all-collections-editor')");
			conn.commit();
			state.close();
			conn.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void addUserAction(String username, String site, String collection, String oid, String action)
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("INSERT INTO usertracker VALUES ('" + username + "', '" + collection + "', '" + site + "', '" + oid + "', '" + System.currentTimeMillis() + "', '" + action + "')");
			conn.commit();
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public ArrayList<HashMap<String, String>> getMostRecentUserActions(String site, String collection, String oid)
	{
		ArrayList<HashMap<String, String>> actions = new ArrayList<HashMap<String, String>>();

		try
		{
			String query = "SELECT username, action, time FROM usertracker WHERE site = '" + site + "' and collection = '" + collection + "' and oid = '" + oid + "' ORDER BY time";
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery(query);
			conn.commit();

			HashSet<String> usernamesSeen = new HashSet<String>();
			while (rs.next())
			{
				String timeStr = rs.getString("time");
				long time = Long.parseLong(timeStr);

				if (System.currentTimeMillis() - time > 6000)
				{
					continue;
				}

				HashMap<String, String> action = new HashMap<String, String>();
				if (!usernamesSeen.contains(rs.getString("username")))
				{
					action.put("username", rs.getString("username"));
					action.put("action", rs.getString("action"));
					actions.add(action);

					usernamesSeen.add(rs.getString("username"));
				}
			}
			state.close();

			clearOldUserActions();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return actions;
	}

	public void clearOldUserActions()
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("DELETE FROM usertracker WHERE (CAST (time AS BIGINT)) < " + (System.currentTimeMillis() - 20000));
			conn.commit();
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public UserQueryResult listAllUsers() throws SQLException
	{
		UserQueryResult userQueryResult = new UserQueryResult();
		String sql_list_all_user = "SELECT username, password, accountstatus, email, comment FROM " + USERS;

		ArrayList<HashMap<String, String>> users = new ArrayList<HashMap<String, String>>();
		Statement state = conn.createStatement();
		ResultSet rs = state.executeQuery(sql_list_all_user);
		conn.commit();
		state.close();

		while (rs.next())
		{
			HashMap<String, String> user = new HashMap<String, String>();
			user.put("username", rs.getString("username"));
			user.put("password", rs.getString("password"));
			user.put("as", rs.getString("accountstatus"));
			user.put("comment", rs.getString("comment"));
			user.put("email", rs.getString("email"));

			users.add(user);
		}

		for (HashMap<String, String> user : users)
		{
			ResultSet gs = state.executeQuery("SELECT role FROM " + ROLES + " WHERE username = '" + user.get("username") + "'");
			String group = "";
			while (gs.next())
			{
				if (!group.equals(""))
				{
					group += ",";
				}
				group += gs.getString("role");
			}
			userQueryResult.addUserTerm(user.get("username"), user.get("password"), group, user.get("as"), user.get("comment"), user.get("email"));
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

	public boolean addUserData(String username, String name, String value)
	{
		//Check if we already have a value under this name
		boolean found = false;
		try
		{
			System.err.println("xx" + conn.isClosed());
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery("SELECT * FROM " + DATA + " WHERE username='" + username + "' AND name='" + name + "'");
			conn.commit();
			if (rs.next())
			{
				found = true;
			}
			else
			{
				found = false;
			}
			state.close();
		}
		catch (Exception ex)
		{
			System.out.println("exception thrown:");
			if (ex instanceof SQLException)
			{
				printSQLError((SQLException) ex);
				ex.printStackTrace();
			}
			else
			{
				ex.printStackTrace();
			}

			System.out.println("Error:" + ex.getMessage());
			return false;
		}

		try
		{
			PreparedStatement stmt = null;
			if (!found)
			{
				stmt = conn.prepareStatement("INSERT INTO " + DATA + " VALUES (?, ?, ?)");
				stmt.setString(1, username);
				stmt.setString(2, name);
				stmt.setString(3, value);
				stmt.executeUpdate();
			}
			else
			{
				stmt = conn.prepareStatement("UPDATE " + DATA + " SET value=? WHERE username=? AND name=?");
				stmt.setString(1, value);
				stmt.setString(2, username);
				stmt.setString(3, name);
				stmt.executeUpdate();
			}
			conn.commit();
			stmt.close();
		}
		catch (Exception ex)
		{
			System.out.println("exception thrown:");
			if (ex instanceof SQLException)
			{
				printSQLError((SQLException) ex);
			}
			else
			{
				ex.printStackTrace();
			}

			System.out.println("Error:" + ex.getMessage());
			return false;
		}
		return true;
	}

	public String getUserData(String username, String name)
	{
		try
		{
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery("SELECT * FROM " + DATA + " WHERE username='" + username + "' AND name='" + name + "'");
			conn.commit();
			if (rs.next())
			{
				return rs.getString("value");
			}
			state.close();
		}
		catch (Exception ex)
		{
			System.out.println("exception thrown:");
			if (ex instanceof SQLException)
			{
				printSQLError((SQLException) ex);
				ex.printStackTrace();
			}
			else
			{
				ex.printStackTrace();
			}

			System.out.println("Error:" + ex.getMessage());
		}
		return null;
	}

	public boolean addUser(String username, String password, String groups, String accountstatus, String comment, String email)
	{
		try
		{
			Statement state = conn.createStatement();
			String sql_insert_user = "insert into " + USERS + " values ('" + username + "', '" + password + "', '" + accountstatus + "', '" + comment + "', '" + email + "')";
			state.execute(sql_insert_user);

			String[] groupArray = groups.split(",");
			for (String g : groupArray)
			{
				String sql_insert_group = "insert into " + ROLES + " values ('" + username + "', '" + g + "')";
				state.execute(sql_insert_group);
			}

			conn.commit();
			state.close();
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

			System.out.println("Error:" + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean deleteUser(String del_username)
	{
		try
		{
			String sql_delete_user = "delete from " + USERS + " where username='" + del_username + "'";
			String sql_delete_groups = "delete from " + ROLES + " where username='" + del_username + "'";
			Statement state = conn.createStatement();
			state.execute(sql_delete_user);
			state.execute(sql_delete_groups);
			conn.commit();
			state.close();
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
			return false;
		}
		return true;
	}

	public boolean deleteAllUser() throws SQLException
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("delete from " + USERS);
			state.execute("delete from " + ROLES);
			conn.commit();
			state.close();
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

			return false;
		}
		return true;
	}

	public UserQueryResult findUser(String username, String password)
	{
		UserQueryResult userQueryResult = new UserQueryResult();

		String sql_find_user = "SELECT  username, password, accountstatus, comment, email FROM " + USERS;
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

		try
		{
			ArrayList<HashMap<String, String>> users = new ArrayList<HashMap<String, String>>();
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery(sql_find_user);
			while (rs.next())
			{
				HashMap<String, String> user = new HashMap<String, String>();
				user.put("username", rs.getString("username"));
				user.put("password", rs.getString("password"));
				user.put("as", rs.getString("accountstatus"));
				user.put("comment", rs.getString("comment"));
				user.put("email", rs.getString("email"));

				users.add(user);
			}
			conn.commit();

			for (HashMap<String, String> user : users)
			{
				ResultSet gs = state.executeQuery("SELECT role FROM " + ROLES + " WHERE username = '" + user.get("username") + "'");

				String group = "";
				while (gs.next())
				{
					if (!group.equals(""))
					{
						group += ",";
					}
					group += gs.getString("role");
				}

				userQueryResult.addUserTerm(user.get("username"), user.get("password"), group, user.get("as"), user.get("comment"), user.get("email"));
			}
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}

		if (userQueryResult.getSize() > 0)
		{
			return userQueryResult;
		}
		else
		{
			return null;
		}
	}

	// findUser(null) will return all users, which is why a UserQueryResult 
	// (a vector of UserTermInfo) is returned
	public UserQueryResult findUser(String username)
	{
		UserQueryResult userQueryResult = new UserQueryResult();

		String sql_find_user = "SELECT  username, password, accountstatus, comment, email FROM " + USERS;
		String append_sql = "";

		if (username != null)
		{
			append_sql = " WHERE username = '" + username + "'";
		}
		if (!append_sql.equals(""))
		{
			sql_find_user += append_sql;
		}

		ArrayList<HashMap<String, String>> users = new ArrayList<HashMap<String, String>>();

		try
		{
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery(sql_find_user);
			conn.commit();
			while (rs.next())
			{
				HashMap<String, String> user = new HashMap<String, String>();
				user.put("username", rs.getString("username"));
				user.put("password", rs.getString("password"));
				user.put("as", rs.getString("accountstatus"));
				user.put("comment", rs.getString("comment"));
				user.put("email", rs.getString("email"));

				users.add(user);
			}
			state.close();

			state = conn.createStatement();
			for (HashMap<String, String> user : users)
			{
				ResultSet gs = state.executeQuery("SELECT role FROM " + ROLES + " WHERE username = '" + user.get("username") + "'");
				conn.commit();

				String group = "";
				while (gs.next())
				{
					if (!group.equals(""))
					{
						group += ",";
					}
					group += gs.getString("role");
				}

				userQueryResult.addUserTerm(user.get("username"), user.get("password"), group, user.get("as"), user.get("comment"), user.get("email"));
			}
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

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

	public String modifyUserInfo(String username, String new_password, String groups, String accountstatus, String comment, String email)
	{
		try
		{
			String sql_modify_user_info = "update " + USERS + " set ";

			boolean needComma = false;
			if (new_password != null && !new_password.equals(""))
			{
				sql_modify_user_info += "password='" + new_password + "'";
				needComma = true;
			}

			if (accountstatus != null && comment != null)
			{
				sql_modify_user_info += (needComma ? "," : "") + " accountstatus='" + accountstatus + "'" + ", comment='" + comment + "'";
				needComma = true;
			}

			if (email != null)
			{
				sql_modify_user_info += (needComma ? "," : "") + " email='" + email + "'";
			}

			sql_modify_user_info += " where username='" + username + "'";
			Statement state = conn.createStatement();
			state.execute(sql_modify_user_info);

			String sql_delete_groups = "delete from " + ROLES + " where username='" + username + "'";
			state.execute(sql_delete_groups);

			String[] groupsArray = groups.split(",");
			for (String g : groupsArray)
			{
				String sql_insert_group = "insert into " + ROLES + " values ('" + username + "', '" + g + "')";
				state.execute(sql_insert_group);
			}

			conn.commit();
			state.close();
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

			return "Error:" + e.getMessage();
		}
		return "succeed";
	}

	public void db2txt()
	{
		System.err.println(db2txtString());
	}

	public String db2txtString()
	{
		//String db2txt = "Error in converting db2txt string.";
		String db2txt = "";
		try
		{
			String sql_list_all_user = "select username, password, accountstatus, comment, email from " + USERS;

			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery(sql_list_all_user);

			ArrayList<HashMap<String, String>> infoMap = new ArrayList<HashMap<String, String>>();

			while (rs.next())
			{
				HashMap<String, String> userMap = new HashMap<String, String>();
				userMap.put("username", rs.getString("username"));
				userMap.put("password", rs.getString("password"));
				userMap.put("status", rs.getString("accountstatus"));
				userMap.put("comment", rs.getString("comment"));
				userMap.put("email", rs.getString("email"));
				infoMap.add(userMap);
			}
			conn.commit();

			StringBuffer buffer = new StringBuffer();//("-------------------------------------");
			for (HashMap<String, String> user : infoMap)
			{
				ResultSet groupsSet = state.executeQuery("SELECT role FROM " + ROLES + " WHERE username = '" + user.get("username") + "'");
				String returnedGroups = "";
				while (groupsSet.next())
				{
					if (!returnedGroups.equals(""))
					{
						returnedGroups += ",";
					}
					returnedGroups += groupsSet.getString("role");
				}
				conn.commit();

				buffer.append("USERNAME = " + user.get("username"));
				buffer.append("\nPASSWORD = " + user.get("password"));
				buffer.append("\nGROUPS = " + returnedGroups);
				buffer.append("\nSTATUS = " + user.get("status"));
				buffer.append("\nCOMMENT = " + user.get("comment"));
				buffer.append("\nEMAIL = " + user.get("email"));
				buffer.append("\n-------------------------------------\n");
			}
			db2txt = buffer.toString();

			conn.commit();
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			return db2txt;
		}
	}

	static void printSQLError(SQLException e)
	{
		while (e != null)
		{
			System.out.println(e.toString());
			e = e.getNextException();
		}
	}

	public void clearUserDataWithPrefix(String username, String prefix)
	{
		try
		{
			Statement state = conn.createStatement();
			state.execute("DELETE FROM data WHERE username = '" + username + "' AND SUBSTR(name, 1, " + prefix.length() + ") = '" + prefix + "'");
			conn.commit();
			state.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
