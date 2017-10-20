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

import net.dv8tion.jda.core.entities.Guild;

public class LogBuffer {

    private final int MAX_INFO = 80, MAX_WARN = 240;
    
    private HashMap<Guild, LinkedList<String>> guildInfoBuf;
    private LinkedList<String> systemInfoBuf;
    private LinkedList<String> warnBuf;
    
    public LogBuffer() {
        guildInfoBuf = new HashMap<Guild, LinkedList<String>>();
        systemInfoBuf = new LinkedList<String>();
        warnBuf = new LinkedList<String>();
    }
    
    public void addGuildInfo(Guild guild, String str) {
        if (!guildInfoBuf.containsKey(guild))
            guildInfoBuf.put(guild, new LinkedList<String>());
        
        LinkedList<String> list = guildInfoBuf.get(guild);
        list.add(str);
        trim(list, MAX_INFO);
        guildInfoBuf.put(guild, list);
    }
    
    public void addSystemInfo(String str) {
        systemInfoBuf.add(str);
        trim(systemInfoBuf, MAX_WARN);
    }
    
    public void addWarning(String str) {
        warnBuf.add(str);
        trim(warnBuf, MAX_WARN);
    }
    
    public void addWarning(String message, StackTraceElement[] steArr) {
        warnBuf.add(message);
        
        for (StackTraceElement ste : steArr) {
            warnBuf.push(ste.toString());
        }
        
        trim(warnBuf, MAX_WARN);
    }
    
    public LinkedList<String> getGuildInfo(Guild guild) {
        return new LinkedList<String>(guildInfoBuf.get(guild));
    }
    
    public LinkedList<String> getSystemInfo() {
        return new LinkedList<String>(systemInfoBuf);
    }
    
    public LinkedList<String> getWarnings() {
        return new LinkedList<String>(warnBuf);
    }
    
    private void trim(LinkedList<String> list, int max) {
        while (list.size() > max)
            list.pop();
    }
}
