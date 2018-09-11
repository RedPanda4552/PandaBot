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
import java.sql.SQLException;

import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;

/**
 * <a href="https://bukkit.org/threads/using-mysql-in-your-plugins.132309/">
 * Adapted from SQL Driver provided by Husky on the Bukkit Forums</a>.<br>
 * @author -_Husky_-
 * @author tips48
 */
public class AdapterSQLite extends AbstractAdapter {
    
    private final String dbFilePath;
    
    /**
     * Creates a new SQLite instance
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public AdapterSQLite(PandaBot pandaBot, String dbLocation) {
        super(pandaBot);
        this.dbFilePath = dbLocation;
    }
    
    public Connection openConnection() {
        if (isConnectionOpen()) {
            return connection;
        }

        File file = new File(dbFilePath);
        
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
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        } catch (SQLException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return null;
        }
        
        return connection;
    }
}
