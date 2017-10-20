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

import io.github.redpanda4552.PandaBot.player.GlobalAudioController;
import io.github.redpanda4552.PandaBot.player.SelectionTracker;
import io.github.redpanda4552.PandaBot.player.ServerAudioController;
import io.github.redpanda4552.PandaBot.player.YoutubeAPI;
import io.github.redpanda4552.PandaBot.util.RunningState;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class PandaBot {

    private final long startTime;
    private final Logger log;
    private final String superuserId;
    
    private LogBuffer logBuffer;
    private JDA jda;
    private CommandProcessor commandProcessor;
    private GlobalAudioController gac;
    private SelectionTracker st;
    
    public PandaBot(Logger log, String token, String youtubeApiKey, String superuserId) {
        startTime = System.currentTimeMillis();
        this.log = log;
        this.superuserId = superuserId;
        init(token, youtubeApiKey);
    }
    
    /**
     * All the initialization goodness. Creates the {@link CommandProcessor},
     * {@link JDA} and {@link GlobalAudioController}.
     * @param token - Discord bot token to use for JDA.
     * @param youtubeApiKey - Youtube API Key to use for video searches and info
     */
    private void init(String token, String youtubeApiKey) {
        logBuffer = new LogBuffer();
        st = new SelectionTracker(); // Required by the play command
        commandProcessor = new CommandProcessor(this);
        
        if (token == null || token.isEmpty()) {
            logWarning("Null or empty Discord bot token!");
            return;
        }
        
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setAutoReconnect(true)
                    .addEventListener(new EventListener(this, commandProcessor))
                    .buildBlocking();
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            logWarning(e.getMessage(), e.getStackTrace());
            return;
        }
        
        updateRunningState(RunningState.INIT);
        YoutubeAPI.setAPIKey(youtubeApiKey);
        gac = new GlobalAudioController(this);

        for (Guild guild : jda.getGuilds()) {
            gac.createServerAudioController(guild);
        }
        
        updateRunningState(RunningState.READY);
        logSystemInfo("PandaBot and JDA online and ready!");
    }
    
    /**
     * Stop all {@link ServerAudioController} instances, shut down {@link JDA}.
     * If reload is set to true, a new PandaBot will be instanced by 
     * {@link Main}.
     */
    public void shutdown(boolean reload) {
        logSystemInfo("Stopping...");
        updateRunningState(RunningState.STOPPING);
        
        while (jda.getPresence().getGame().getName() != RunningState.STOPPING.getStatusMessage()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logWarning(e.getMessage(), e.getStackTrace());
            }
        } 
        
        st.dropAll();
        getGlobalAudioController().killAll();
        jda.shutdown();
        
        if (reload)
            Main.reinstance();
    }
    
    /**
     * Updates the "Playing" field depending on the bot's running state.
     */
    private void updateRunningState(RunningState runningState) {
        if (jda != null)
            jda.getPresence().setGame(Game.of(runningState.getStatusMessage()));
    }
    
    /**
     * Log messages to the console at INFO level and add to Guild log buffer
     */
    public void logGuildInfo(Guild guild, String... strArr) {
        for (String str : strArr) {
            log.info(str);
            logBuffer.addGuildInfo(guild, str);
        }
    }
    
    public void logSystemInfo(String... strArr) {
        for (String str : strArr) {
            log.info(str);
            logBuffer.addSystemInfo(str);
        }
    }
    
    /**
     * Log messages to the console at WARNING level
     */
    public void logWarning(String... strArr) {
        for (String str : strArr) {
            log.warning(str);
            logBuffer.addWarning(str);
        }
    }
    
    /**
     * Log a stack trace to the console at WARNING level
     */
    public void logWarning(String message, StackTraceElement[] steArr) {
        log.warning(message);
        logBuffer.addWarning(message);
        
        for (StackTraceElement ste : steArr) {
            log.warning(ste.toString());
            logBuffer.addWarning(ste.toString());
        }
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
     * Attempt to join a {@link VoiceChannel}.
     */
    public void joinVoiceChannel(Guild guild, VoiceChannel vc) {
        if (guild.getAudioManager().getConnectedChannel() == vc)
            return;
        if (guild.getAudioManager().isAttemptingToConnect())
            return;
        guild.getAudioManager().openAudioConnection(vc);
    }
    
    /**
     * Attempt to leave a {@link VoiceChannel}.
     */
    public void leaveVoiceChannel(Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
        }
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
    
    public LogBuffer getLogBuffer() {
        return logBuffer;
    }
    
    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }
    
    public GlobalAudioController getGlobalAudioController() {
        return gac;
    }
    
    public SelectionTracker getSelectionTracker() {
        return st;
    }
}
