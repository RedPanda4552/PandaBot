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

import java.util.HashMap;
import java.util.LinkedList;

import net.dv8tion.jda.api.entities.Guild;

public class LogBuffer {

    private static final int MAX_INFO = 80, MAX_WARN = 240;
    
    private static HashMap<Guild, LinkedList<String>> guildInfoBuf = new HashMap<Guild, LinkedList<String>>();
    private static LinkedList<String> systemInfoBuf = new LinkedList<String>();
    private static LinkedList<String> systemWarnBuf = new LinkedList<String>();
    
    public static void guildInfo(Guild guild, String... strArr) {
        if (!guildInfoBuf.containsKey(guild))
            guildInfoBuf.put(guild, new LinkedList<String>());
        
        LinkedList<String> list = guildInfoBuf.get(guild);
        
        for (String str : strArr)
            list.add(str);
        
        trim(list, MAX_INFO);
        guildInfoBuf.put(guild, list);
    }
    
    public static void sysInfo(String... strArr) {
        for (String str : strArr)
            systemInfoBuf.add(str);
        
        trim(systemInfoBuf, MAX_WARN);
    }
    
    public static void sysWarn(String... strArr) {
        for (String str : strArr)
            systemWarnBuf.add(str);
        
        trim(systemWarnBuf, MAX_WARN);
    }
    
    public static void sysWarn(String message, StackTraceElement[] steArr) {
        systemWarnBuf.add(message);
        
        for (StackTraceElement ste : steArr)
            systemWarnBuf.push(ste.toString());
        
        trim(systemWarnBuf, MAX_WARN);
    }
    
    public static void sysWarn(Exception e) {
        systemWarnBuf.add(e.getMessage());
        
        for (StackTraceElement ste : e.getStackTrace())
            systemWarnBuf.push(ste.toString());
        
        trim(systemWarnBuf, MAX_WARN);
    }
    
    public static LinkedList<String> getGuildInfo(Guild guild) {
        return new LinkedList<String>(guildInfoBuf.get(guild));
    }
    
    public static LinkedList<String> getSystemInfo() {
        return new LinkedList<String>(systemInfoBuf);
    }
    
    public static LinkedList<String> getWarnings() {
        return new LinkedList<String>(systemWarnBuf);
    }
    
    private static void trim(LinkedList<String> list, int max) {
        while (list.size() > max)
            list.pop();
    }
}
