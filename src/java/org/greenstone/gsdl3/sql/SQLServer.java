package org.greenstone.gsdl3.sql;

import java.sql.Connection;

/*
 * this class provide an interface for connecting and querying a database system. In order to not tie Greenstone to a particalur database system 
 * every database action should go through this interface 
 * 
 */
public interface SQLServer{
 
    /** connect to a database without creating the database if it doesn't exist */
    public Connection connect(String databasePath);

    /** connect to a database and create the database if it doesn't exist */
    public Connection connectAndCreate(String databasePath); 

    public boolean disconnect(String databasePath);

   	
 }
