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

import java.time.format.DateTimeFormatter;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class MessageArchiver {

    private static final String CHANNEL_NAME = "pandabot-archive";
    
    public static void copyTo(Message message) {
        if (!message.isPinned())
            return;
        
        Guild guild = message.getGuild();
        List<TextChannel> channels = guild.getTextChannelsByName(CHANNEL_NAME, false);
        TextChannel archiveChannel;
        
        if (channels.isEmpty()) {
            archiveChannel = (TextChannel) guild.getController().createTextChannel(CHANNEL_NAME).complete();
        } else {
            archiveChannel = channels.get(0);
        }
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(guild.getMember(message.getAuthor()).getColor());
        eb.setAuthor(guild.getMember(message.getAuthor()).getEffectiveName());
        eb.setDescription(message.getContentDisplay());
        eb.setThumbnail(message.getAuthor().getEffectiveAvatarUrl());
        if (!message.getAttachments().isEmpty())
            eb.setImage(message.getAttachments().get(0).getUrl());
        eb.addField("Source", message.getTextChannel().getAsMention(), true);
        // If we get timezone problems later, stop hard coding the 4 hours here.
        // I don't feel like doing all the temporal crap just to print a single
        // date and time. So screw it, the one liner hack wins.
        eb.addField("Date (US Eastern)", message.getCreationTime().minusHours(4).format(DateTimeFormatter.ISO_LOCAL_DATE), true);
        eb.addField("Time (US Eastern)", message.getCreationTime().minusHours(4).format(DateTimeFormatter.ISO_LOCAL_TIME), true);
        archiveChannel.sendMessage(eb.build()).complete();
    }
}
