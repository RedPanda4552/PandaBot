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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import fredboat.audio.GuildPlayer;
import io.github.redpanda4552.PandaBot.listeners.Listener;
import io.github.redpanda4552.PandaBot.operations.OperationDelete;
import io.github.redpanda4552.PandaBot.operations.OperationLeave;
import io.github.redpanda4552.PandaBot.operations.OperationLogDump;
import io.github.redpanda4552.PandaBot.operations.OperationNowPlaying;
import io.github.redpanda4552.PandaBot.operations.OperationPause;
import io.github.redpanda4552.PandaBot.operations.OperationPlay;
import io.github.redpanda4552.PandaBot.operations.OperationPrivate;
import io.github.redpanda4552.PandaBot.operations.OperationReload;
import io.github.redpanda4552.PandaBot.operations.OperationSeek;
import io.github.redpanda4552.PandaBot.operations.OperationSendMessage;
import io.github.redpanda4552.PandaBot.operations.OperationSkip;
import io.github.redpanda4552.PandaBot.operations.OperationStop;
import io.github.redpanda4552.PandaBot.operations.OperationVoiceChannelConnect;
import io.github.redpanda4552.PandaBot.util.BotState;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.managers.AudioManager;

public class PandaBot {

    private final Logger log = Main.log;
    
    private static JDA jda;
    private static String guildId;
    
    private GuildPlayer guildPlayer;
    private ArrayList<String> logHistory;
    
    public static boolean enabled = true, waitingForShutdown = true;
    
    public OperationsQueue operationsQueue;
    
    /**
     * Send a message to the logger and also store in in the log history for
     * future logdump commands.
     * @param str - The message to log
     * @param logLevel - The level the message should be logged as
     */
    public void addToHistory(String str, Level logLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append("PandaBot").append("//").append(logLevel.toString()).append("] ");
        builder.append(str);
        logHistory.add(builder.toString());
        log.log(logLevel, str);
    }
    
    public ArrayList<String> getHistory() {
        return logHistory;
    }
    
    private JDA createClient(String token) {
        JDA jda = null;
        
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(token).addListener(new Listener(this)).buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        }
        
        return jda;
    }
    
    /**
     * Check if anyone else is in the channel. If not, leave.
     * @param event - The GuildVoiceLeaveEvent that called this method.
     */
    public void checkChannelStatus(GuildVoiceLeaveEvent event) {
        List<VoiceChannel> channels = getGuild().getVoiceChannels();
        
        if (channels.isEmpty()) {
            return;
        }
        
        AudioManager am = getAudioManager();
        
        if (getGuildPlayer().getUsersInVC().isEmpty()) {
            if (getGuildPlayer().getUserCurrentVoiceChannel(event.getGuild().getSelfMember()) == event.getChannelLeft()) {
                if (!getGuildPlayer().isPaused()) {
                    getGuildPlayer().pause();
                    queueSendMessage("All users have disconnected, stopping playback.", getGuildPlayer().getActiveTextChannel());
                    am.closeAudioConnection();
                }
            }
        }
    }
    
    public VoiceChannel getUserVoiceChannel(User user) {
        for (VoiceChannel voiceChannel : getGuild().getVoiceChannels()) {
            for (Member member : voiceChannel.getMembers()) {
                if (user.getId().equals(member.getUser().getId())) {
                    return voiceChannel;
                }
            }
        }
        
        return null;
    }
    
    public AudioManager getAudioManager() {
        return getGuild().getAudioManager();
    }
    
    public GuildPlayer getGuildPlayer() {
        return guildPlayer;
    }
    
    public static JDA getJDA() {
        return jda;
    }
    
    public static Guild getGuild() {
        return jda.getGuildById(guildId);
    }
    
    public static VoiceChannel getVoiceChannelById(String str) {
        for (VoiceChannel voiceChannel : getGuild().getVoiceChannels()) {
            if (voiceChannel.getId().equals(str)) {
                return voiceChannel;
            }
        }
        
        return null;
    }
    
    public static TextChannel getTextChannelById(String str) {
        for (TextChannel channel : getGuild().getTextChannels()) {
            if (channel.getId().equals(str)) {
                return channel;
            }
        }
        
        return null;
    }
    
    private void updateState(BotState state) {
        jda.getPresence().setGame(Game.of(state.toString()));
    }
    
    public PandaBot(String token, String guildId) {
        if (!enabled) {
            enabled = true;
            waitingForShutdown = true;
        }
        
        logHistory = new ArrayList<String>();
        
        if ((jda = createClient(token)) == null) {
            addToHistory("Failed to create Discord client!", Level.SEVERE);
            return;
        }
        
        updateState(BotState.STARTING);
        
        PandaBot.guildId = guildId;
        guildPlayer = new GuildPlayer(jda, getGuild());
        operationsQueue = new OperationsQueue();
        updateState(BotState.RUNNING);
        addToHistory("Startup complete.", Level.INFO);
        
        while (enabled) {
            try {
                Thread.sleep(10);
                operationsQueue.executeNext();
            } catch (InterruptedException e) {
                addToHistory("Sleep interrupted while waiting for next operation!", Level.WARNING);
            } catch (Throwable t) {
                addToHistory(t.getMessage(), Level
                        .WARNING);
                
                for (StackTraceElement elem : t.getStackTrace()) {
                    addToHistory(elem.toString(), Level.WARNING);
                }
                
                queuePrivateMessage(getJDA().getUserById(Main.getOperatorId()), "An exception was caught by the catch-all in PandaBot's main thread. Logdump is below.");
                queueLogDump(getJDA().getUserById(Main.getOperatorId()), null, -1);
            }
        }
        
        updateState(BotState.STOPPING);
        getJDA().shutdown(false);
        
        while (waitingForShutdown) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                addToHistory("Sleep interrupted while waiting for shutdown!", Level.SEVERE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Tells PandaBot to send a message to a MessageChannel. Will make no effort
     * to mention anyone.
     * @param text - The text content of the message.
     * @param channel - The MessageChannel to send to.
     */
    public void queueSendMessage(String text, MessageChannel channel) {
        // Since this is expecting User... as last parameter, two nulls
        //suppresses the warning saying "meh it should be an array bleh bleh"
        queueSendMessage(text, channel, null, null);
    }
    
    /**
     * Tells PandaBot to send a message to a MessageChannel. Will mention the
     * specified users.
     *@param text - The text content of the message.
     * @param channel - The MessageChannel to send to.
     * @param mention - Users to mention
     */
    public void queueSendMessage(String text, MessageChannel channel, User... mention) {
        operationsQueue.add(new OperationSendMessage(this, text, channel, mention));
    }
    
    public void queuePrivateMessage(User user, String text) {
        operationsQueue.add(new OperationPrivate(this, user, text));
    }
    
    public void queueDeleteMessage(Message message) {
        operationsQueue.add(new OperationDelete(this, message));
    }
    
    public void queueVoiceChannelConnect(VoiceChannel voiceChannel) {
        operationsQueue.add(new OperationVoiceChannelConnect(this, voiceChannel));
    }
    
    public void queuePlay(User sender, MessageChannel channel, Message message, String link) {
        operationsQueue.add(new OperationPlay(this, sender, channel, message, link));
    }
    
    public void queueStop(MessageChannel channel) {
        operationsQueue.add(new OperationStop(this, channel));
    }
    
    public void queuePause(User sender, MessageChannel channel) {
        operationsQueue.add(new OperationPause(this, sender, channel));
    }
    
    public void queueSkip(MessageChannel channel) {
        operationsQueue.add(new OperationSkip(this, channel));
    }
    
    public void queueSeek(long time) {
        operationsQueue.add(new OperationSeek(this, time));
    }
    
    public void queueLeave(MessageChannel channel) {
        operationsQueue.add(new OperationLeave(this, channel));
    }
    
    public void queueNowPlaying(MessageChannel channel) {
        operationsQueue.add(new OperationNowPlaying(this, channel));
    }
    
    public void queueReload() {
        operationsQueue.add(new OperationReload(this));
    }
    
    public void queueLogDump(User user, int limiter) {
        queueLogDump(user, null, limiter);
    }
    
    public void queueLogDump(MessageChannel channel, int limiter) {
        queueLogDump(null, channel, limiter > 20 ? 20 : limiter);
    }
    
    private void queueLogDump(User user, MessageChannel channel, int limiter) {
        operationsQueue.add(new OperationLogDump(this, user, channel, limiter));
    }
}
