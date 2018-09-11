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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class ServerAudioController extends AudioEventAdapter implements AudioSendHandler {
    
    private final String YOUTUBE_SHORT_URL = "https://youtu.be", YOUTUBE_URL_TIME_PARAM = "t=";
    
    private PandaBot pandaBot;
    private AudioPlayerManager apm;
    private AudioPlayer ap;
    private AudioFrame lastFrame;
    private MessageChannel lastMessageChannel;
    private LinkedList<AudioTrack> queue;
    private String lastIdentifier;
    private HashMap<String, Long> startAheadPositions = new HashMap<String, Long>();
    
    public ServerAudioController(PandaBot pandaBot, AudioPlayerManager apm, AudioPlayer ap) {
        this.pandaBot = pandaBot;
        this.apm = apm;
        this.ap = ap;
        queue = new LinkedList<AudioTrack>();
    }
    
    public AudioPlayer getAudioPlayer() {
        return ap;
    }
    
    /**
     * Clear the queue and return the number of items removed.
     */
    public int emptyQueue() {
        int ret = queue.size();
        queue.clear();
        return ret;
    }
    
    public LinkedList<AudioTrack> getQueue() {
        return queue;
    }
    
    public String getLastIdentifier() {
        return lastIdentifier;
    }
    
    public void loadResource(MessageChannel msgChannel, Member member, String identifier) {
        lastMessageChannel = msgChannel;
        
        if (identifier.startsWith(YOUTUBE_SHORT_URL) && identifier.contains("?" + YOUTUBE_URL_TIME_PARAM)) {
            String timeStr = identifier.split(YOUTUBE_URL_TIME_PARAM)[1];
            String[] spl = timeStr.split("h|m|s");
            int hour = 0, minute = 0, second = 0;
            
            for (int i = 0; i < spl.length; i++) {
                if (timeStr.contains("h") && hour == 0)
                    hour = Integer.parseInt(spl[i]);
                else if (timeStr.contains("m") && minute == 0)
                    minute = Integer.parseInt(spl[i]);
                else if (timeStr.contains("s") && second == 0)
                    second = Integer.parseInt(spl[i]);
            }
            
            long positionMS = ((((hour * 60) + minute) * 60) + second) * 1000;
            startAheadPositions.put(identifier, positionMS);
        }
        
        apm.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (ap.getPlayingTrack() == null && queue.isEmpty()) {
                    // Your math teacher was right, you WILL use factoring!
                    for (String identifier : startAheadPositions.keySet()) {
                        if (identifier.contains(track.getIdentifier())) {
                            track.setPosition(startAheadPositions.get(identifier));
                            startAheadPositions.remove(identifier);
                            break;
                        }
                    }
                    ap.playTrack(track);
                } else {
                    MessageBuilder mb = new MessageBuilder();
                    
                    if (queue.add(track)) {
                        mb.append("Adding to queue: **")
                          .append(track.getInfo().title)
                          .append("**\n\t**Length:** ")
                          .append(DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss"))
                          .append("\t**Channel:** ")
                          .append(track.getInfo().author);
                    } else {
                        mb.append("Failed to queue: **")
                          .append(track.getInfo().title)
                          .append("**\n\t**Length:** ")
                          .append(DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss"))
                          .append("\t**Channel:** ")
                          .append(track.getInfo().author);
                    }
                    
                    pandaBot.sendMessage(msgChannel, mb.build());
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks())
                    trackLoaded(track);
            }

            @Override
            public void noMatches() {
                MessageBuilder mb = new MessageBuilder();
                mb.append(member.getAsMention())
                  .append(" **")
                  .append(identifier)
                  .append("** returned no matches.");
                pandaBot.sendMessage(msgChannel, mb.build());
            }

            @Override
            public void loadFailed(FriendlyException e) {
                MessageBuilder mb = new MessageBuilder();
                mb.append(e.getMessage());
                pandaBot.sendMessage(msgChannel, mb.build());
            }
        });
    }
    
    @Override
    public void onPlayerPause(AudioPlayer player) {
        MessageBuilder mb = new MessageBuilder();
        mb.append("Paused **")
          .append(player.getPlayingTrack().getInfo().title)
          .append("**")
          .append("\n**\tTime:** ")
          .append(DurationFormatUtils.formatDuration(player.getPlayingTrack().getPosition(), "mm:ss"))
          .append("\t**Channel:** ")
          .append(player.getPlayingTrack().getInfo().author);
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        MessageBuilder mb = new MessageBuilder();
        mb.append("Resumed **")
          .append(player.getPlayingTrack().getInfo().title)
          .append("**")
          .append("\n**\tTime:** ")
          .append(DurationFormatUtils.formatDuration(player.getPlayingTrack().getPosition(), "mm:ss"))
          .append("\t**Channel:** ")
          .append(player.getPlayingTrack().getInfo().author);
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(track.getInfo().title)
          .setDescription(track.getInfo().uri)
          .setAuthor("Now Playing")
          .addField("Channel", track.getInfo().author, true)
          .addField("Length", DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss"), true);
        if (track.getSourceManager() instanceof YoutubeAudioSourceManager)
            eb.setColor(0xff0000); // Sampled from the Youtube logo
        else if (track.getSourceManager() instanceof SoundCloudAudioSourceManager)
            eb.setColor(0xff5500); // Sampled from the SoundCloud play button
        else if (track.getSourceManager() instanceof TwitchStreamAudioSourceManager)
            eb.setColor(0x4b367c);
        pandaBot.sendMessage(lastMessageChannel, new MessageBuilder().setEmbed(eb.build()).build());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        lastIdentifier = track.getIdentifier();
        
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.STOPPED) {
            ap.playTrack(queue.poll());
        } else {
            if (!queue.isEmpty()) {
                emptyQueue();
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (exception.getCause() instanceof InterruptedException) {
            pandaBot.sendMessage(lastMessageChannel, "**Player stopping!** Something is shutting me down!");
        } else {
            pandaBot.sendMessage(lastMessageChannel, "The player just encountered an internal error. If it messed up, try again in a moment.");
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        pandaBot.sendMessage(lastMessageChannel, "The player may have hit a snag; if it's stuck, consider skipping the track and trying again?");
    }
    
    @Override
    public boolean canProvide() {
        lastFrame = ap.provide();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.getData();
    }
    
    @Override
    public boolean isOpus() {
        return true;
    }

}
