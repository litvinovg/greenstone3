package org.greenstone.gsdl3.sql.derby;

import  org.greenstone.gsdl3.sql.*;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import org.greenstone.gsdl3.util.DBInfo;

import org.apache.log4j.*;

public class DerbyDBWrapper implements MetadataDBWrapper{
    protected SQLServer sqlServer = null;
    protected SQLStatements sqlState = null;
    protected Connection connection = null; 
    protected static Logger logger = Logger.getLogger(org.greenstone.gsdl3.sql.derby.DerbyDBWrapper.class.getName());


    public DBInfo getInfo(String id){
	if(sqlState == null) return null;
        String dbInfoState = sqlState.getDBInfoStatement(id);
        ArrayList<HashMap<String, Object>> result = executeQuery(dbInfoState);
	if (result.size() == 0) return null;
	DBInfo info = new DBInfo();
	for(int i=0;  i<result.size();i++){
	    HashMap arow = result.get(i);
	    Iterator ite = arow.keySet().iterator();
	    while(ite.hasNext()){
		String key = (String)ite.next();
		Object value = arow.get(key);
		//logger.info(key + "=>" +value);
		if (value == null || value.equals("")) continue;
		info.addInfo(key.toLowerCase(),sqlState.undoDBSafe(value));
	    }
	}

	return info;
    }

    public void setSQLServer(SQLServer server){
	sqlServer = server;
    }

    public void setSQLStatements(SQLStatements state){
	sqlState = state;
    }

    public boolean openConnection(String databasepath){
	if(connection != null) return true;

	connection = sqlServer.connect(databasepath);
	if (connection == null) return false;
	try{
	    connection.setAutoCommit(true);	
	}
	catch(SQLException sqle){
	    logger.debug("Database Error occured when creating a statement object",sqle);
	    return false;
	}
	return true;
    }

    public boolean openAndCreateConnection(String databasepath){
	if(connection != null) return true;
    	connection = sqlServer.connectAndCreate(databasepath);
	
    	if (connection == null) {
    		logger.error("sql connection is null. database path="+databasepath);
    		return false;
    	}

    	try{
	    connection.setAutoCommit(true);	
	}
    	catch(SQLException sqle){
    		logger.debug("Database Error occured when creating a statement object",sqle);
    		return false;
    	}
    	return true;
    }

    public synchronized ArrayList<HashMap<String, Object>> executeQuery(String query_statement){
	//the database hasn't been correct yet
    	ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
	ResultSet rs = null;
	try{	
	    //by passing the two arguments, the ResultSet returned from executeQuery is updatable
	    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	   rs = statement.executeQuery(query_statement);
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int numOfColumns = rsmd.getColumnCount();
	    while(rs.next()){
		HashMap<String, Object> arow = new HashMap<String, Object>();
		for(int i = 1; i <= numOfColumns ; i++){
		    arow.put(rsmd.getColumnName(i).toLowerCase(), rs.getObject(i));
		}
		results.add(arow);  
	    }
	    statement.close();
	}
	catch(SQLException sqle){
	    logger.debug("Database Error occured when executeQuery " + query_statement,sqle);
	    return results;
	}
	catch(Exception e){    		
	    logger.debug(e);
	    return null;
	}
	return results;
    }
    /**
     * Same as executeQuery except the return type is ResultSet
     * @param stat
     * @return ResultSet of querying the statement 'stat'
     */
    public synchronized ResultSet queryResultSet(String stat){
    	ResultSet rs = null;
	//by passing the two arguments, the ResultSet returned from executeQuery is updatable

    	try{	       	
	    	Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    		if (statement == null){
    			logger.info("Null sql statement provided.");
    			return null;
    		}
    		logger.debug(stat);
    		rs = statement.executeQuery(stat);
   	      	logger.info("sql stat="+stat+ " result="+rs);
    	}
    	catch(SQLException sqle){
    		logger.info("Database Error occured when execute query " + stat, sqle);
    		return null;
    	}
    	catch(Exception e){    		
    		logger.info("Exception="+e);
    		return null;
    	}
    	logger.debug(" result="+rs);
    	return rs;
    }
    /**
     * Used by create (table)
     */
    public synchronized boolean execute(String stat) {
    	boolean rs;

    	try{	       	
	    Statement statement = connection.createStatement();
    		if (statement == null){
    			logger.info("statement is null.");
    			return false;
    		}

    		rs = statement.execute(stat);
		statement.close();
	}
    	catch(SQLException sqle){    		
    		logger.debug("Database Error occured when execute query " + stat, sqle);
    		return false;
    	}
    	catch(Exception e){    		
    		logger.debug(e);
    		return false;
    	}
    	return rs;
    }
    /**
     * Used by insert, update, and delete
     */
    public synchronized boolean executeUpdate(String stat) {
    	int rs;
	try{	       	
	    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    		if (statement == null){
    			logger.info("statement is null.");
    			return false;
    		}
    		
    		//either the row count of INSERT, UPDATE OR DELETE statement, or 0 if the statement returns nothing
    		rs = statement.executeUpdate(stat);
		statement.close();
		logger.debug("sql stat="+stat+ " result="+rs);
    	}
    	catch(SQLException sqle){    		
    		logger.debug("Database Error occured when execute query " + stat, sqle);
    		return false;
    	}
    	catch(Exception e){    		
    		logger.debug("Exception="+e);
    		return false;
    	}
    	logger.debug(" result="+rs);
    	return (rs==-1)? false : true;
    }
    /**
     * Used by checking the existence of table
     * Same as the method 'execute' except throws SQLException
     */
    public void check4Table(String stat) throws SQLException {
	Statement statement = connection.createStatement();
   	statement.executeQuery(stat);
	statement.close();
    }

    public void closeConnection(String databasepath){
	try{
	    if(connection != null){
		connection.close();
		connection = null;
		sqlServer.disconnect(databasepath);
	    }
	}
	catch(SQLException sqle){
	    logger.debug("Database Error occured when close connection " + databasepath, sqle);
	}
    }
  
 }