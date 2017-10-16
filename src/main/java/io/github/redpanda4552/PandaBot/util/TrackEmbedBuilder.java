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
