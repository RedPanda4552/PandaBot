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
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import io.github.redpanda4552.PandaBot.util.Configuration;
import io.github.redpanda4552.PandaBot.util.LogFormatter;

public class Main {

    public static final Logger log = Logger.getLogger(PandaBot.class.getName());
    public static Configuration config;
    
    public static void main(String[] args) {
        log.setUseParentHandlers(false);
        LogFormatter logFormatter = new LogFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(logFormatter);
        log.addHandler(consoleHandler);
        log.info("Logger formatting set up");
        String altConfigPath = null;
        
        if (args.length >= 1) {
            File file = new File(args[0]);
            
            if (file.exists() && args[0].toLowerCase().endsWith(".xml")) {
                altConfigPath = args[0];
            }
        }
        
        config = new Configuration(altConfigPath != null ? altConfigPath : null);
        
        if (!config.isReady()) {
            log.warning("Something went wrong trying to set up the configuration! See the above stack trace for details.");
            return;
        }
        
        if (!config.isComplete()) {
            log.warning("The file \"pandabot.xml\" has been dropped in the same folder as the PandaBot jar.");
            log.warning("To run PandaBot, this file needs to be filled out. Documentation is available at: ");
            log.warning("https://github.com/RedPanda4552/PandaBot#configuration");
            return;
        }
        
        PandaBot.enabled = true;
        PandaBot.waitingForShutdown = true;
        
        while (true) {
            new PandaBot(config.get("discord-token"), config.get("guild-id"));
        }
    }
}


