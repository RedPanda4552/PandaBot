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

import io.github.redpanda4552.PandaBot.util.MessageArchiver;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

    private PandaBot pandaBot;
    private CommandProcessor commandProcessor;
    
    public EventListener(PandaBot pandaBot, CommandProcessor commandProcessor) {
        this.pandaBot = pandaBot;
        this.commandProcessor = commandProcessor;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            commandProcessor.process(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage());
        } catch (Throwable t) {
            LogBuffer.sysWarn(t.getMessage(), t.getStackTrace());
        }
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        
        if (vc.getMembers().size() == 1 && vc.getMembers().get(0).getUser().getId().equals(pandaBot.getBotId())) {
            pandaBot.leaveVoiceChannel(event.getGuild());
        }
    }
    
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        
        if (vc.getMembers().size() == 1 && vc.getMembers().get(0).getUser().getId().equals(pandaBot.getBotId())) {
            pandaBot.leaveVoiceChannel(event.getGuild());
        }
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        MessageBuilder mb = new MessageBuilder();
        mb.append("Welcome ")
          .append(event.getMember().getAsMention())
          .append(" to **")
          .append(event.getGuild().getName())
          .append("**!");
        pandaBot.sendMessage(event.getMember().getDefaultChannel(), mb.build());
    }
    
    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        MessageArchiver.copyTo(event.getMessage());
    }
}
