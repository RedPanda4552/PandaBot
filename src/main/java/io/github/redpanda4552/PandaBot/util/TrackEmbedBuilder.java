package io.github.redpanda4552.PandaBot.util;

import java.awt.Color;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.redpanda4552.PandaBot.player.YoutubeAPI;
import io.github.redpanda4552.PandaBot.player.YoutubeVideo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class TrackEmbedBuilder {
    
    public static MessageEmbed buildFor(AudioTrack at) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("Time", DurationFormatUtils.formatDuration(at.getPosition(), "[mm:ss]"), true);
        eb.addField("Length", DurationFormatUtils.formatDuration(at.getDuration(), "[mm:ss]"), true);
        eb.addField("Remaining", DurationFormatUtils.formatDuration(at.getDuration() - at.getPosition(), "[mm:ss]"), true);
        
        if (at instanceof YoutubeAudioTrack) {
            return youtube(at, eb);
        } else {
            return generic(at, eb);
        }
    }

    private static MessageEmbed youtube(AudioTrack at, EmbedBuilder eb) {
        YoutubeVideo yv = YoutubeAPI.getVideoFromID(at.getIdentifier(), true);
        eb.setTitle(at.getInfo().title + " https://www.youtube.com/watch?v=" + at.getIdentifier());
        eb.addField("Description", yv.getDescription(), false);
        eb.setColor(new Color(20, 210, 45));
        eb.setAuthor(yv.getChannelTitle(), yv.getChannelUrl(), yv.getChannelThumbUrl());
        return eb.build();
    }
    
    private static MessageEmbed generic(AudioTrack at, EmbedBuilder eb) {
        
        return eb.build();
    }
}
