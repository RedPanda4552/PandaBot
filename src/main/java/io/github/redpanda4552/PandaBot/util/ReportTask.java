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
package io.github.redpanda4552.PandaBot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageHistory;

public class ReportTask extends TimerTask {

    private final int MAX_HISTORY = 100;
    
    private File reportFile;
    private MessageChannel outputChannel;
    private int hours;
    private HashMap<String, Integer> stats = new HashMap<String, Integer>();
    
    public ReportTask(File reportFile, MessageChannel outputChannel, int hours) {
        this.reportFile = reportFile;
        this.outputChannel = outputChannel;
        this.hours = hours;
    }
    
    @Override
    public void run() {
        try {
            HashMap<String, String> settings = new HashMap<String, String>();
            BufferedReader reader = new BufferedReader(new FileReader(reportFile));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] split = line.split("=");
                
                if (split.length > 0 && !split[0].isEmpty())
                    settings.put(split[0], split.length == 2 ? split[1] : null);
            }
            
            reader.close();
            JDA jda = PandaBot.getSelf().getJDA();
            MessageChannel targetChannel;
            if (!settings.containsKey("channel-id")) return;
            targetChannel = jda.getTextChannelById(settings.get("channel-id"));
            
            MessageHistory history = targetChannel.getHistory();
            history.retrievePast(MAX_HISTORY).complete();
            OffsetDateTime start = OffsetDateTime.now().minusHours(hours);
            
            while (start.isBefore(history.getRetrievedHistory().get(history.getRetrievedHistory().size() - 1).getCreationTime()))
                history.retrievePast(MAX_HISTORY).complete();
            
            stats.put("all", 0);
            stats.put("embeds", 0);
            stats.put("attachments", 0);
            ArrayList<String> keywords = new ArrayList<String>();
            
            for (String key : settings.keySet()) {
                if (key.startsWith("keyword")) {
                    stats.put(settings.get(key), 0);
                    keywords.add(settings.get(key));
                }
            }
            
            for (Message msg : history.getRetrievedHistory()) {
                if (msg.getCreationTime().isBefore(start)) {
                    continue;
                } else if (settings.get("user-id") == null || settings.get("user-id").equals(msg.getAuthor().getId())) {
                    increment("all");
                    add("embeds", msg.getEmbeds().size());
                    add("attachments", msg.getAttachments().size());
                    
                    for (String keyword : keywords) {
                        String content = msg.getContentDisplay().toLowerCase();
                        int counter = 0;
                        
                        while (content.contains(keyword.toLowerCase())) {
                            content = content.replaceFirst(keyword, "");
                            counter++;
                        }
                        
                        add(keyword, counter);
                    }
                }
            }
            
            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor("Message Report for " + targetChannel.getName());
            String userId = settings.get("user-id");
            
            if (userId != null && !userId.isEmpty()) {
                String username = PandaBot.getSelf().getJDA().getUserById(userId).getName();  
                eb.setDescription("Messages by user \"" + username + "\"");
            } else {
                eb.setDescription("Messages by all users");
            }
            
            eb.appendDescription(" in the last " + hours + " hours:");
            
            for (String key : stats.keySet())
                eb.addField(key, String.valueOf(stats.get(key)), true);
            
            PandaBot.getSelf().sendMessage(outputChannel, new MessageBuilder().setEmbed(eb.build()).build());
        } catch (IOException e) {
            LogBuffer.sysWarn(e);
        }
    }
    
    private void increment(String key) {
        stats.put(key, stats.get(key) + 1);
    }
    
    private void add(String key, int i) {
        if (i == 0) return;
        stats.put(key, stats.get(key) + i);
    }
}
