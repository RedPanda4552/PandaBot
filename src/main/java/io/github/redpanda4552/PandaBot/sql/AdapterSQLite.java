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
package io.github.redpanda4552.PandaBot.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;

/**
 * <a href="https://bukkit.org/threads/using-mysql-in-your-plugins.132309/">
 * Adapted from SQL Driver provided by Husky on the Bukkit Forums</a>.<br>
 * @author -_Husky_-
 * @author tips48
 */
public class AdapterSQLite extends AbstractAdapter {
    
    private final String dbLocation;
    protected Connection connection;
    
    /**
     * Creates a new SQLite instance
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public AdapterSQLite(PandaBot pandaBot, String dbLocation) {
        super(pandaBot);
        this.dbLocation = dbLocation;
    }
    
    public boolean isConnectionOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    public Connection openConnection() {
        if (isConnectionOpen()) {
            return connection;
        }

        File file = new File(dbLocation);
        
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
                return null;
            }
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return null;
        }
        
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return null;
        }
        
        return connection;
    }
    
    public void closeConnection() {
        if (!isConnectionOpen())
            return;
        
        try {
            connection.close();
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
        }
    }
    
    public void processTable(Table table) {
        try {
            StringBuilder createBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            createBuilder.append(table.getName())
                         .append(" (");
            
            for (int i = 0; i < table.getColumns().length; i++) {
                // Ninja trick to avoid comma at start or end
                if (i != 0)
                    createBuilder.append(", ");
                createBuilder.append(table.getColumns()[i])
                             .append(" varchar(255)");
            }
            
            createBuilder.append(");");
            PreparedStatement ps = connection.prepareStatement(createBuilder.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
        }
    }
    
    @Override
    public ResultSet processQuery(Query query) {
        if (query.getSelect().size() < 1)
            return null;
        if (query.getFrom() == null)
            return null;
        
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT ");
            Iterator<String> iter = query.getSelect().iterator();
            
            while (iter.hasNext()) {
                queryBuilder.append(iter.next());
                if (iter.hasNext())
                    queryBuilder.append(", ");
            }
            
            
            queryBuilder.append(" FROM ")
                        .append(query.getFrom().getName())
                        .append(" WHERE ")
                        .append(query.getWhere())
                        .append(";");
            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            return ps.executeQuery();
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    @Override
    public void processInsert(Insert insert) {
        if (insert.getTable() == null)
            return;
        if (insert.getValues().length == 0)
            return;
        
        try {
            StringBuilder insertBuilder = new StringBuilder("INSERT INTO ");
            insertBuilder.append(insert.getTable().getName())
                         .append(" (");
            
            for (int i = 0; i < insert.getTable().getColumns().length; i++) {
                // Ninja trick to avoid comma at start or end
                if (i != 0)
                    insertBuilder.append(", ");
                insertBuilder.append(insert.getTable().getColumns()[i]);
            }
            
            insertBuilder.append(") VALUES (");
            
            for (int i = 0; i < insert.getValues().length; i++) {
                // Ninja trick to avoid comma at start or end
                if (i != 0)
                    insertBuilder.append(", ");
                insertBuilder.append("?"); 
            }
            
            insertBuilder.append(");");
            
            PreparedStatement ps = connection.prepareStatement(insertBuilder.toString());

            for (int i = 0; i < insert.getValues().length; i++) {
                // They handle indexing the wrong way, so we have to + 1...
                ps.setString(i + 1, insert.getValues()[i]);
            }
            
            ps.executeUpdate();
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
        }
    }
}
