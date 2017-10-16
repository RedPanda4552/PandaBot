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
    private HashMap<String, ServerAudioController> sacMap; // GuildId, SAC Inst
    
    public GlobalAudioController(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        apm = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(apm);
        sacMap = new HashMap<String, ServerAudioController>();
    }
    
    public void createServerAudioController(Guild guild) {
        AudioPlayer ap = apm.createPlayer();
        ServerAudioController sac = new ServerAudioController(pandaBot, apm, ap);
        ap.addListener(sac);
        guild.getAudioManager().setSendingHandler(sac);
        sacMap.put(guild.getId(), sac);
    }
    
    /**
     * Immediately destroy the AudioPlayerManager. This will disable all audio
     * capabilities and should only be used before reloads and shutdowns.
     */
    public void killAll() {
        apm.shutdown();
    }
    
    private ServerAudioController getServerAudioController(String guildId) {
        if (sacMap.containsKey(guildId))
            return sacMap.get(guildId);
        return null;
    }
    
    public void play(Guild guild, MessageChannel msgChannel, Member member, String identifier) {
        getServerAudioController(guild.getId()).loadResource(msgChannel, member, identifier);
    }
    
    public void pause(Guild guild, MessageChannel msgChannel) {
        AudioPlayer ap = getServerAudioController(guild.getId()).getAudioPlayer();
        ap.setPaused(!ap.isPaused());
    }
    
    public void skip(Guild guild, MessageChannel msgChannel) {
        AudioPlayer ap = getServerAudioController(guild.getId()).getAudioPlayer();
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
        ServerAudioController sac = getServerAudioController(guild.getId());
        MessageBuilder mb = new MessageBuilder();
        mb.append("Cleared **");
        mb.append(sac.emptyQueue());
        mb.append("** tracks from the queue.");
        pandaBot.sendMessage(msgChannel, mb.build());
        skip(guild, msgChannel);
    }
    
    public void nowPlaying(Guild guild, MessageChannel msgChannel) {
        AudioTrack at = getServerAudioController(guild.getId()).getAudioPlayer().getPlayingTrack();
        
        if (at == null) {
            pandaBot.sendMessage(msgChannel, "Nothing is playing!");
            return;
        }
        
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbed(TrackEmbedBuilder.buildFor(at));
        pandaBot.sendMessage(msgChannel, mb.build());
    }
    
    public void queue(Guild guild, MessageChannel msgChannel) {
        LinkedList<AudioTrack> queue = getServerAudioController(guild.getId()).getQueue();
        
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
