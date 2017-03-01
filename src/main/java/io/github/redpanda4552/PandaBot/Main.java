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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import io.github.redpanda4552.PandaBot.util.LogFormatter;

public class Main {

    public static final Logger log = Logger.getLogger(PandaBot.class.getName());
    private static String operatorId;
    public static String youtubeAPIKey;
    
    private static String discordToken;
    private static String guildId;
    
    public static void main(String[] args) {
        log.setUseParentHandlers(false);
        LogFormatter logFormatter = new LogFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(logFormatter);
        log.addHandler(consoleHandler);
        log.info("Logger formatting set up");
        
        // Making this a class because otherwise getClass() causes problems
        class ConfigLoader {
            public ConfigLoader(File file) {
                File configFile = new File("pandabot.cfg");
                
                if (file != null) {
                    configFile = file;
                }
                
                if (!configFile.exists()) {
                    try {
                        FileUtils.copyURLToFile(getClass().getResource("/pandabot.cfg"), configFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(configFile));
                    String str = "";
                    String[] strArr = null;
                    
                    // If a config line isn't filled out but has a space at the
                    // end, it will be split around the : and have white space
                    // as the 2nd array element. The trim here will stop this.
                    while ((str = reader.readLine()) != null) {
                        if (str.startsWith("//")) {
                            continue;
                        }
                        
                        str = str.trim();
                        strArr = str.split(":");
                        
                        if (strArr.length == 2) {
                            if (strArr[0].equals("operatorId")) {
                                operatorId = strArr[1].trim();
                            } else if (strArr[0].equals("youtubeAPIKey")) {
                                youtubeAPIKey = strArr[1].trim();
                            } else if (strArr[0].equals("discordToken")) {
                                discordToken = strArr[1].trim();
                            } else if (strArr[0].equals("guildId")) {
                                guildId = strArr[1].trim();
                            }
                        }
                    }
                    
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        
        File file = null;
        
        if (args.length >= 1) {
            file = new File(args[0]);
            
            if (!file.exists()) {
                file = null;
            }
        }
        
        new ConfigLoader(file);
        
        if (discordToken == null || youtubeAPIKey == null || guildId == null) {
            log.warning("The file \"pandabot.cfg\" has been dropped in the same folder as the PandaBot jar.");
            log.warning("To run PandaBot, this file needs to be filled out.");
            return;
        }
        
        PandaBot.enabled = true;
        PandaBot.waitingForShutdown = true;
        
        while (true) {
            new PandaBot(discordToken, guildId);
        }
    }
    
    public static String getOperatorId() {
        return operatorId;
    }
    
    public static String getYoutubeAPIKey() {
        return youtubeAPIKey;
    }
}


