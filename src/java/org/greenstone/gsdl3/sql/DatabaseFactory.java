package org.greenstone.gsdl3.sql;

/**
 * Returns a sql server according to the user's selection
 *
 */

import org.apache.log4j.*;

import java.util.HashMap;

public class DatabaseFactory{
    private static HashMap<String, SQLServer> serverMap = new HashMap<String, SQLServer>();
    private static Logger logger = Logger.getLogger(org.greenstone.gsdl3.sql.DatabaseFactory.class.getName());


    //upcase the first letter
    public static String properDBName(String name_in){
	String name_out = "";
	name_in = name_in.toLowerCase();
	if (name_in.length() >= 1){
	    String firstLetter = name_in.substring(0,1).toUpperCase();
	    name_out = firstLetter +  name_in.substring(1);
	}
	return name_out;
    }


    public static final SQLServer getDatabaseServer(String dbname){
	dbname = properDBName(dbname);
	if (serverMap.containsKey(dbname)) return serverMap.get(dbname);
	String fullName = "org.greenstone.gsdl3.sql." + dbname.toLowerCase()+ "." + dbname +"SQLServer";
	try {
	    SQLServer server = (SQLServer)Class.forName(fullName).newInstance();
	    serverMap.put(dbname, server);
	    return server;
	} catch (Exception e) {
	    logger.debug("Couldn't load the database server "+ fullName);
	}
	
	return null; 
    }

   public static final SQLStatements getSQLStatements(String sqlstate){
       String fullsqlstate = "org.greenstone.gsdl3.sql." + sqlstate;
	//load SQLStatements class
       try{
	   SQLStatements state = (SQLStatements)Class.forName(fullsqlstate).newInstance(); 
	   return state;
       }
       catch (Exception e) {
	   // if falied use sqlstate as a full path
	   try{
	       SQLStatements state = (SQLStatements)Class.forName(sqlstate).newInstance(); 
	       return state;
	   }
	   catch (Exception e2) {
	       // failed again, give up
	       logger.debug("Couldn't load the sql statement  "+ sqlstate);
	   }
       }
		
       return null; 
   }

    public static final MetadataDBWrapper getMetadataDBWrapper(String dbname, String sqlstate){
	dbname = properDBName(dbname);

	String fullName = "org.greenstone.gsdl3.sql." + dbname.toLowerCase()+ "." + dbname +"DBWrapper";
	try {
	    MetadataDBWrapper wrapper = (MetadataDBWrapper)Class.forName(fullName).newInstance();
	    if (wrapper != null){
		//load SQLServer class
		SQLServer server = getDatabaseServer(dbname);
		if (server != null){
		    wrapper.setSQLServer(server);
		}

		String fullsqlstate = "org.greenstone.gsdl3.sql." + sqlstate;
		//load SQLStatements class
		try{
		   SQLStatements state = (SQLStatements)Class.forName(fullsqlstate).newInstance(); 
		   if (state != null){
		       wrapper.setSQLStatements(state); 
		       return wrapper;
		   }
		}
		catch (Exception e) {
		    // if falied use sqlstate as a full path
		    try{
			SQLStatements state = (SQLStatements)Class.forName(sqlstate).newInstance(); 
			if (state != null){
			    wrapper.setSQLStatements(state);
			    return wrapper;
			}
		    }
		    catch (Exception e2) {
			// failed again, give up
			logger.debug("Couldn't load the sql statement  "+ sqlstate);
		    }
		}
	    }
	} catch (Exception e) {
	    logger.debug("Couldn't load the database wrapper "+ fullName);
	}
	
	return null; 

    }

}
