package org.greenstone.gsdl3.sql;

import java.util.ArrayList;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.greenstone.gsdl3.util.DBInfo;
import org.greenstone.gsdl3.sql.SQLServer;

public interface MetadataDBWrapper{

    public DBInfo getInfo(String id);

    public void setSQLServer(SQLServer server);

    public void setSQLStatements(SQLStatements sqlstate);

    public boolean openConnection(String databasepath);

    public boolean openAndCreateConnection(String databasepath);

     //return a list of rows
    public ArrayList executeQuery(String query_statement);

    public ResultSet queryResultSet(String query_statement);

    public boolean execute(String stat);

    public boolean executeUpdate(String stat);

    public void check4Table(String stat) throws SQLException;

    public void closeConnection(String databasepath);
   
}