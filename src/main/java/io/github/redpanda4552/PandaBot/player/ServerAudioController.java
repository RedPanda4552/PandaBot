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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.util.MessageEmbedBuilder;
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
    private LinkedList<QueueElement> queue = new LinkedList<QueueElement>();
    private LastTrackContainer lastTrack;
    private HashMap<String, Long> startAheadPositions = new HashMap<String, Long>();
    
    /**
     * Used to track Members who execute these commands, so we can put their
     * names on the message embeds
     */
    public Member lastCommand;
    
    public ServerAudioController(PandaBot pandaBot, AudioPlayerManager apm, AudioPlayer ap) {
        this.pandaBot = pandaBot;
        this.apm = apm;
        this.ap = ap;
    }
    
    public AudioPlayer getAudioPlayer() {
        return ap;
    }
    
    /**
     * Clear the queue
     */
    public void emptyQueue(Member member) {
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(MessageEmbedBuilder.playerQueueEmptiedEmbed(queue, member));
        queue.clear();
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }
    
    public LinkedList<QueueElement> getQueue() {
        return queue;
    }
    
    public LastTrackContainer getLastTrack() {
        return lastTrack;
    }
    
    public void loadResource(MessageChannel msgChannel, Member member, String identifier, boolean isReplay) {
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
                // Your math teacher was right, you WILL use factoring!
                for (String identifier : startAheadPositions.keySet()) {
                    if (identifier.contains(track.getIdentifier())) {
                        track.setPosition(startAheadPositions.get(identifier));
                        startAheadPositions.remove(identifier);
                        break;
                    }
                }
                
                if (isReplay && lastTrack != null)
                    track.setPosition(lastTrack.getStartTime());
                
                if (ap.getPlayingTrack() == null && queue.isEmpty()) {
                    ap.playTrack(track);
                } else {
                    MessageBuilder mb = new MessageBuilder();
                    
                    if (queue.add(new QueueElement(track, member))) {
                        mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.QUEUE, track, member));
                    } else {
                        mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.QUEUE_FAIL, track, member));
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
                mb.setEmbed(MessageEmbedBuilder.playerNoMatchesEmbed(identifier, member));
                pandaBot.sendMessage(msgChannel, mb.build());
            }

            @Override
            public void loadFailed(FriendlyException e) {
                MessageBuilder mb = new MessageBuilder();
                mb.setEmbed(MessageEmbedBuilder.playerLoadFailed(e, member));
                pandaBot.sendMessage(msgChannel, mb.build());
            }
        });
    }
    
    @Override
    public void onPlayerPause(AudioPlayer player) {
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.PAUSE, player.getPlayingTrack(), lastCommand));
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.RESUME, player.getPlayingTrack(), lastCommand));
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        lastTrack = new LastTrackContainer(track.getIdentifier(), track.getPosition());
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.PLAY, track, lastCommand));
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.STOPPED) {
            MessageBuilder mb = new MessageBuilder();
            mb.setEmbed(MessageEmbedBuilder.playerEmbed(PlayerAction.SKIP, track, lastCommand));
            pandaBot.sendMessage(lastMessageChannel, mb.build());
        }
        
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.STOPPED) {
            if (!queue.isEmpty()) {
                QueueElement element = queue.poll();
                lastCommand = element.getMember();
                ap.playTrack(element.getAudioTrack());
            }
        }
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
