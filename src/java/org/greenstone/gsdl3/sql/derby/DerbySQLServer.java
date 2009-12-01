package org.greenstone.gsdl3.sql.derby;


import org.apache.log4j.*;
import org.greenstone.gsdl3.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbySQLServer implements SQLServer{
    static final String PROTOCOL = "jdbc:derby:";
    static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static Logger logger = Logger.getLogger(org.greenstone.gsdl3.sql.derby.DerbySQLServer.class.getName());     

    public DerbySQLServer(){
	try{
	    Class.forName(DRIVER).newInstance();
	}
        catch(Exception e){
	    logger.error("Couldn't find derby driver: "+DRIVER,e);
	}
    }

    public Connection connect(String databasePath){
	try{
	    String  protocol_str = PROTOCOL + databasePath; 
	    Connection connection = DriverManager.getConnection(protocol_str);
	    return connection;
	}
        catch(Exception e){
	    logger.info("Connect to database "+databasePath + " failed!");
	}

	return null;
	
    }

    public Connection connectAndCreate(String databasePath){
	try{
	    String  protocol_str = PROTOCOL + databasePath + ";create=true"; 
	    Connection connection = DriverManager.getConnection(protocol_str);
	    return connection;
	}
	catch(Exception e){
	    logger.error("Connect to database "+databasePath + " failed!",e);
	
	}
	return null;
    } 

    public boolean disconnect(String databasePath){
	try{
	    String protocol_str = PROTOCOL + databasePath + ";shutdown=true"; 
	    DriverManager.getConnection(protocol_str);
	}catch (SQLException se){
	    String theError = (se).getSQLState();
	    if (!theError.equals("08006")){
//		logger.error("Database "+databasePath + " couldn't be shut down properly!",se);
	    }
	    else{
		return true;
	    }
	}
	
	return false;
    }


   
}
