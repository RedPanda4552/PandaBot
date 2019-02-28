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
package io.github.redpanda4552.PandaBot.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.github.redpanda4552.PandaBot.LogBuffer;

public class Config {

    private final String FILE_PATH = "./pandabot.cfg";
    private final String[][] DEFAULT_CONFIG = {
        {"discord-bot-token", ""},
        {"superuser-id", ""},
        {"youtube-api-key", ""},
        {"idle-channel", ""}
    };
    
    private File configFile = new File(FILE_PATH);
    private String[][] currentConfig = DEFAULT_CONFIG;
    
    public Config() {
        initFile();
    }
    
    private void initFile() {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                StringBuilder sb = new StringBuilder();
                BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                
                for (String[] kvPair : DEFAULT_CONFIG) {
                    sb.append(kvPair[0]).append("=").append(kvPair[1]).append("\n");
                    writer.write(sb.toString());
                }
                
                writer.close();
            }
            
            String[] inPair = new String[2];
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            
            // Not the world's most efficient method, a lot of wasted iterations
            // really. But without breaking out hashmaps that have to be populated
            // at runtime, this is about as child-proof as you can get. In case
            // the config file gets reordered or something, we don't want to trust
            // array indicies are 1:1.
            while ((inPair = reader.readLine().split("=")) != null) {
                for (String[] configPair : currentConfig) {
                    if (configPair[0].equals(inPair[0])) {
                        configPair[1] = inPair[1];
                        break;
                    }
                }
            }
            
            reader.close();
        } catch (IOException e) {
            LogBuffer.sysWarn(e);
        }
    }
    
    public String getString(String key) {
        for (String[] kvPair : currentConfig) {
            if (kvPair[0].equals(key)) {
                return kvPair[1];
            }
        }
        
        return null;
    }
}
