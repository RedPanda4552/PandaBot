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

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import io.github.redpanda4552.PandaBot.util.LogFormatter;

public class Main {

    private static final Logger log = Logger.getLogger(PandaBot.class.getName());
    
    public static void main(String[] args) {
        log.setUseParentHandlers(false);
        LogFormatter logFormatter = new LogFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(logFormatter);
        log.addHandler(consoleHandler);
        
        if (args.length != 3) {
            log.info("Usage: java -jar PandaBot-x.y.z.jar <discord-bot-token> <youtube-api-key> <discord-user-id>");
            log.info("More information available at https://github.com/redpanda4552/PandaBot/");
            return;
        }
        
        while (true)
            new PandaBot(log, args[0], args[1], args[2]);
    }
}
