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
package io.github.redpanda4552.PandaBot.player;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.util.TrackEmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class GlobalAudioController {

    private PandaBot pandaBot;
    private AudioPlayerManager apm;
    private HashMap<Guild, ServerAudioController> sacMap; // Guild, SAC Inst
    
    public GlobalAudioController(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        apm = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(apm);
        sacMap = new HashMap<Guild, ServerAudioController>();
    }
    
    public void createServerAudioController(Guild guild) {
        AudioPlayer ap = apm.createPlayer();
        ServerAudioController sac = new ServerAudioController(pandaBot, apm, ap);
        ap.addListener(sac);
        guild.getAudioManager().setSendingHandler(sac);
        sacMap.put(guild, sac);
    }
    
    /**
     * Immediately destroy the AudioPlayerManager. This will disable all audio
     * capabilities and should only be used before reloads and shutdowns.
     */
    public void killAll() {
        apm.shutdown();
    }
    
    private ServerAudioController getServerAudioController(Guild guild) {
        if (!sacMap.containsKey(guild))
            createServerAudioController(guild);
        return sacMap.get(guild);
    }
    
    public void play(Guild guild, MessageChannel msgChannel, Member member, String identifier) {
        getServerAudioController(guild).loadResource(msgChannel, member, identifier);
    }
    
    public void replay(Guild guild, MessageChannel msgChannel, Member member) {
        String identifier = getServerAudioController(guild).getLastIdentifier();
        
        if (identifier == null || identifier.isEmpty()) {
            pandaBot.sendMessage(msgChannel, "Nothing to replay!");
            return;
        }
        
        play(guild, msgChannel, member, identifier);
    }
    
    public void pause(Guild guild, MessageChannel msgChannel) {
        AudioPlayer ap = getServerAudioController(guild).getAudioPlayer();
        ap.setPaused(!ap.isPaused());
    }
    
    public void skip(Guild guild, MessageChannel msgChannel) {
        AudioPlayer ap = getServerAudioController(guild).getAudioPlayer();
        AudioTrack at = ap.getPlayingTrack();
        
        if (at == null) {
            pandaBot.sendMessage(msgChannel, "Nothing to skip!");
            return;
        }
        
        ap.stopTrack();
        MessageBuilder mb = new MessageBuilder();
        mb.append("Skipping current track **");
        mb.append(at.getInfo().title);
        mb.append("**");
        pandaBot.sendMessage(msgChannel, mb.build());
    }
    
    public void stop(Guild guild, MessageChannel msgChannel) {
        ServerAudioController sac = getServerAudioController(guild);
        MessageBuilder mb = new MessageBuilder();
        mb.append("Cleared **");
        mb.append(sac.emptyQueue());
        mb.append("** tracks from the queue.");
        pandaBot.sendMessage(msgChannel, mb.build());
        skip(guild, msgChannel);
    }
    
    public void nowPlaying(Guild guild, MessageChannel msgChannel) {
        AudioTrack at = getServerAudioController(guild).getAudioPlayer().getPlayingTrack();
        
        if (at == null) {
            pandaBot.sendMessage(msgChannel, "Nothing is playing!");
            return;
        }
        
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(TrackEmbedBuilder.buildFor(at));
        pandaBot.sendMessage(msgChannel, mb.build());
    }
    
    public void queue(Guild guild, MessageChannel msgChannel) {
        LinkedList<AudioTrack> queue = getServerAudioController(guild).getQueue();
        
        if (queue.isEmpty()) {
            pandaBot.sendMessage(msgChannel, "The queue is empty!");
            return;
        }
        
        MessageBuilder mb = new MessageBuilder();
        mb.append("Queued tracks:\n");
        int i = 1;
        
        for (AudioTrack at : queue) {
            mb.append("\t**")
              .append(i)
              .append(":** ")
              .append(at.getInfo().title)
              .append("\n\t\t**Length:** ")
              .append(DurationFormatUtils.formatDuration(at.getDuration(), "mm:ss"))
              .append("\t**Channel:** ")
              .append(at.getInfo().author)
              .append("\n");
            i++;
        }
        
        pandaBot.sendMessage(msgChannel, mb.build());
    }
}
