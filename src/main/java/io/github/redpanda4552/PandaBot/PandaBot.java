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

import java.io.IOException;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import com.mashape.unirest.http.Unirest;

import io.github.redpanda4552.PandaBot.player.GlobalAudioController;
import io.github.redpanda4552.PandaBot.player.SelectionTracker;
import io.github.redpanda4552.PandaBot.player.ServerAudioController;
import io.github.redpanda4552.PandaBot.player.YoutubeAPI;
import io.github.redpanda4552.PandaBot.reporting.GlobalReportManager;
import io.github.redpanda4552.PandaBot.sql.AbstractAdapter;
import io.github.redpanda4552.PandaBot.sql.AdapterSQLite;
import io.github.redpanda4552.PandaBot.sql.TableIndex;
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

public class PandaBot {

    private static PandaBot self;
    
    public static PandaBot getSelf() {
        return self;
    }
    
    private final long startTime;
    private final String superuserId;
    
    private JDA jda;
    private CommandProcessor commandProcessor;
    private GlobalAudioController gac;
    private SelectionTracker st;
    private GlobalReportManager grm;
    private AbstractAdapter sql;
    
    public PandaBot(Logger log, String token, String superuserId, String youtubeApiKey) {
        startTime = System.currentTimeMillis();
        self = this;
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
        st = new SelectionTracker(); // Required by the play command
        commandProcessor = new CommandProcessor(this);
        
        if (token == null || token.isEmpty()) {
            LogBuffer.sysWarn("Null or empty Discord bot token!");
            return;
        }
        
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setAutoReconnect(true)
                    .addEventListener(new EventListener(this, commandProcessor))
                    .build().awaitReady();
        } catch (LoginException | IllegalArgumentException | InterruptedException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            return;
        }
        
        updateRunningState(RunningState.INIT);
        sql = new AdapterSQLite(this, "./pandabot.db");
        sql.openConnection();
        sql.processTable(TableIndex.MUSIC);
        YoutubeAPI.setAPIKey(youtubeApiKey);
        gac = new GlobalAudioController(this);

        for (Guild guild : jda.getGuilds()) {
            gac.createServerAudioController(guild);
        }
        
        grm = new GlobalReportManager(jda.getGuilds());
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
        
        while (jda.getPresence().getGame().getName() != RunningState.STOPPING.getStatusMessage()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
            }
        } 
        
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            LogBuffer.sysWarn(e.getMessage(), e.getStackTrace());
        }
        
        st.dropAll();
        getGlobalAudioController().killAll();
        sql.closeConnection();
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
            jda.getPresence().setGame(Game.listening(runningState.getStatusMessage()));
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
    
    public int getTextChannelCount() {
        return jda.getTextChannels().size();
    }
    
    public int getVoiceChannelPlayingCount() {
        int ret = 0;
        
        for (ServerAudioController sac : getGlobalAudioController().getAllServerAudioControllers())
            if (sac.getAudioPlayer().getPlayingTrack() != null)
                ret++;
        
        return ret;
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
    
    public GlobalReportManager getGlobalReportManager() {
        return grm;
    }
    
    public AbstractAdapter getSQL() {
        return sql;
    }
}
