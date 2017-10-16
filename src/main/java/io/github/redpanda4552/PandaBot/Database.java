/**
 * This file is part of PandaBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.PandaBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <a href="https://bukkit.org/threads/using-mysql-in-your-plugins.132309/">
 * Adapted from SQL Driver provided by Husky on the Bukkit Forums</a>.<br>
 * @author -_Husky_-
 * @author tips48
 * 
 * <b>This is excluded from build path!</b>
 */
public class Database {
    
    private final String dbLocation;
    
    private PandaBot pandaBot;
    private Connection connection;
    
    /**
     * Creates a new SQLite instance
     * 
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public Database(PandaBot pandaBot, String dbLocation) {
        this.pandaBot = pandaBot;
        this.dbLocation = dbLocation;
    }
    
    /**
     * Checks if a connection is open with the database
     * @return true if the connection is open
     * @throws SQLException if the connection cannot be checked
     */
    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }

        File file = new File(dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Unable to create database!");
            }
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
        return connection;
    }
    
    public boolean testConfiguration() {
        if (dbLocation != null && dbLocation.length() >= 4 && dbLocation.endsWith(".db")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Query for a single column (or wildcard *) where a column equals a value.
     * @param resultCol - The column to include in the ResultSet (SELECT)
     * @param table - The table to query (FROM)
     * @param whereCol - Single column to evaluate (WHERE)
     * @param whereVal - The value to compare to the column (WHERE)
     * @return ResultSet with rows matching the criteria, null if SQLException
     */
    public ResultSet selectWhereEquals(String resultCol, String table, String whereCol, String whereVal) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT ? FROM ? WHERE ? = ?;");
            ps.setString(1, resultCol);
            ps.setString(2, table);
            ps.setString(3, whereCol);
            ps.setString(4, whereVal);
            return ps.executeQuery();
        } catch (SQLException e) {
            pandaBot.logWarning(e.getMessage(), e.getStackTrace());
        }
        
        return null;
    }
    
    /**
     * Query for multiple columns where a column equals a value.
     * @param resultCol - Array of columns to include in the ResultSet (SELECT)
     * @param table - The table to query (FROM)
     * @param whereCol - The column to evaluate (WHERE)
     * @param whereVal - The value to compare to the column (WHERE)
     * @return ResultSet with rows matching the criteria, null if SQLException
     */
    public ResultSet selectManyWhereEquals(String[] resultCol, String table, String whereCol, String whereVal) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT ? FROM ? WHERE ? = ?;");
            String select = "";
            
            for (int i = 0; i < resultCol.length; i++) {
                select += resultCol[i];
                
                if (i < resultCol.length - 1) {
                    select += ", ";
                }
            }
            
            ps.setString(1, select);
            ps.setString(2, table);
            ps.setString(3, whereCol);
            ps.setString(4, whereVal);
            return ps.executeQuery();
        } catch (SQLException e) {
            pandaBot.logWarning(e.getMessage(), e.getStackTrace());
        }
        
        return null;
    }
}
