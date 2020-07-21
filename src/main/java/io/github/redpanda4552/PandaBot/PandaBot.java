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

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import io.github.redpanda4552.PandaBot.util.RunningState;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PandaBot {

    private static PandaBot self;
    
    public static PandaBot getSelf() {
        return self;
    }
    
    private final long startTime;
    private final String superuserId;
    
    private JDA jda;
    private CommandProcessor commandProcessor;
    
    public PandaBot(Logger log, String token, String superuserId) {
        startTime = System.currentTimeMillis();
        self = this;
        this.superuserId = superuserId;
        init(token);
    }
    
    /**
     * All the initialization goodness. Creates the {@link CommandProcessor} and
     * {@link JDA}
     * @param token - Discord bot token to use for JDA.
     */
    private void init(String token ) {
        commandProcessor = new CommandProcessor(this);
        
        if (token == null || token.isEmpty()) {
            LogBuffer.sysWarn("Null or empty Discord bot token!");
            return;
        }
        
        try {
            jda = JDABuilder.createDefault(token)
                    .setAutoReconnect(true)
                    .addEventListeners(new EventListener(this, commandProcessor))
                    .build().awaitReady();
        } catch (LoginException | IllegalArgumentException | InterruptedException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return;
        }
        
        updateRunningState(RunningState.READY);
        LogBuffer.sysInfo("PandaBot and JDA online and ready!");
    }
    
    /**
     * Stop all {@link ServerAudioController} instances, shut down {@link JDA}.
     * If reload is set to true, a new PandaBot will be instanced by 
     * {@link Main}.
     */
    public void shutdown(boolean reload) {
        LogBuffer.sysInfo("Stopping...");
        updateRunningState(RunningState.STOPPING);
        
        while (jda.getPresence().getActivity().getName() != RunningState.STOPPING.getStatusMessage()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            }
        } 
        
        jda.shutdown();
        
        if (reload) {
            Main.reinstance();
        } else {
            System.exit(0);
        }
    }
    
    /**
     * Updates the "Playing" field depending on the bot's running state.
     */
    private void updateRunningState(RunningState runningState) {
        if (jda != null)
            jda.getPresence().setActivity(Activity.watching(runningState.getStatusMessage()));
    }
    
    /**
     * Check if a member is the superuser.
     */
    public boolean memberIsSuperuser(Member member) {
        return member.getUser().getId().equalsIgnoreCase(superuserId);
    }
    
    public boolean memberHasPermission(Member member, String permission) {
        if (memberIsSuperuser(member)) {
            return true;
        }
        
        // Not implemented!
        return true;
    }
    
    public JDA getJDA() {
        return jda;
    }
    
    /**
     * Send a message to a channel using a built {@link Message}.
     * @return The final {@link Message} instance.
     */
    public Message sendMessage(MessageChannel msgChannel, Message msg) {
        return msgChannel.sendMessage(msg).complete();
    }
    
    /**
     * Send a message to a channel using an unformatted String.
     * @return The final {@link Message} instance.
     */
    public Message sendMessage(MessageChannel msgChannel, String str) {
        MessageBuilder mb = new MessageBuilder();
        mb.append(str);
        return sendMessage(msgChannel, mb.build());
    }
    
    /**
     * Send a direct message to a {@link Member}.
     */
    public void sendDirectMessage(Member member, Message msg) {
        member.getUser().openPrivateChannel().complete().sendMessage(msg).complete();
    }
    
    /**
     * Get PandaBot's own user ID
     */
    public String getBotId() {
        return jda.getSelfUser().getId();
    }   
    
    public long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public int getServerCount() {
        return jda.getGuilds().size();
    }
    
    public int getUserCount() {
        return jda.getUsers().size();
    }
    
    public int getTextChannelCount() {
        return jda.getTextChannels().size();
    }
    
    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }
}
