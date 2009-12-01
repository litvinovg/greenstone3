package org.greenstone.gsdl3.sql;

public interface SQLStatements{

    public String getDBInfoStatement(String node_id);
    public String undoDBSafe(Object text);
}