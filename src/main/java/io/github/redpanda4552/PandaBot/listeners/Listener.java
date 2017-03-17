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
package io.github.redpanda4552.PandaBot.listeners;

import java.util.logging.Level;

import io.github.redpanda4552.PandaBot.CommandExecutor;
import io.github.redpanda4552.PandaBot.Main;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
    
    private PandaBot pandaBot;
    private CommandExecutor commandExecutor;
    
    public Listener(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        this.commandExecutor = new CommandExecutor(pandaBot);
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        pandaBot.checkChannelStatus(event);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            commandExecutor.processCommand(event.getMessage());
        } catch (Throwable t) {
            pandaBot.addToHistory(t.getMessage(), Level.WARNING);
            
            for (StackTraceElement elem : t.getStackTrace()) {
                pandaBot.addToHistory(elem.toString(), Level.WARNING);
            }
            
            pandaBot.queuePrivateMessage(PandaBot.getJDA().getUserById(Main.config.get("operator-id")), "An exception was caught by the catch-all in PandaBot's Listener. Logdump is below.");
            pandaBot.queueLogDump(PandaBot.getJDA().getUserById(Main.config.get("operator-id")), -1);
        }
    }
    
    @Override
    public void onShutdown(ShutdownEvent event) {
        pandaBot.addToHistory("JDA shutdown completed.", Level.INFO);
        PandaBot.waitingForShutdown = false;
    }
}
