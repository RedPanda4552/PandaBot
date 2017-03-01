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
package io.github.redpanda4552.PandaBot.operations;

import java.util.ArrayList;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class OperationLogDump extends AbstractOperation {

    private User sender;
    private MessageChannel channel;
    private int limiter;
    
    public OperationLogDump(PandaBot pandaBot, User sender, MessageChannel channel, int limiter) {
        super(pandaBot);
        this.sender = sender;
        this.channel = channel;
        this.limiter = limiter;
    }

    @Override
    public void execute() {
        if (channel == null) {
            if (sender == null) {
                return;
            }
        }
        
        // Making sure we are using a local copy and not a reference.
        ArrayList<String> logHistory = new ArrayList<String>();
        logHistory.addAll(pandaBot.getHistory());
        int logHistorySize = logHistory.size();
        
        // If we have a limiter, we need to start by purging
        if (logHistory.size() > limiter && limiter != -1) {
            for (int i = 1; i < logHistorySize - limiter; i++) {
                logHistory.remove(0);
            }
        }
        
        int pageCount = 1, logCount = logHistory.size();
        StringBuilder sb = new StringBuilder("```PandaBot Log Dump Page " + pageCount + " (top is old, bottom new)\n");
        
        for (String str : logHistory) {
            logCount--;
            // We are purposely undershooting Discord's 2000 char limit to
            // accommodate for newlines. And also ridiculously long walls of text
            if (sb.length() + str.length() < 1500 && logCount > 0) {
                sb.append(str);
                sb.append("\n");
            } else {
                sb.append("```");
                
                if (sender != null) {
                    pandaBot.queuePrivateMessage(sender, sb.toString());
                } else {
                    pandaBot.queueSendMessage(sb.toString(), channel);
                }
                
                pageCount++;
                sb = new StringBuilder("```PandaBot Log Dump Page " + pageCount + " (top is old, bottom new)\n");
            }
        }
        
        complete();
    }
}
