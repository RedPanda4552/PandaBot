package io.github.redpanda4552.PandaBot.player;

import java.util.LinkedList;

import org.apache.commons.lang3.time.DurationFormatUtils;

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
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class ServerAudioController extends AudioEventAdapter implements AudioSendHandler {
    
    private PandaBot pandaBot;
    private AudioPlayerManager apm;
    private AudioPlayer ap;
    private AudioFrame lastFrame;
    private MessageChannel lastMessageChannel;
    private LinkedList<AudioTrack> queue;
    
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
    
    public void loadResource(MessageChannel msgChannel, Member member, String identifier) {
        lastMessageChannel = msgChannel;
        
        apm.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (ap.getPlayingTrack() == null && queue.isEmpty()) {
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
        MessageBuilder mb = new MessageBuilder();
        mb.append("Now playing **")
          .append(track.getInfo().title)
          .append("**\n\t**Length:** ")
          .append(DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss"))
          .append("\t**Channel:** ")
          .append(track.getInfo().author);
        pandaBot.sendMessage(lastMessageChannel, mb.build());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
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
        return lastFrame.data;
    }
    
    @Override
    public boolean isOpus() {
        return true;
    }

}
