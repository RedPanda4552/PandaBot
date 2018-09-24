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

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.redpanda4552.PandaBot.player.PlayerAction;
import io.github.redpanda4552.PandaBot.player.QueueElement;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class MessageEmbedBuilder {

    /**
     * Create a MessageEmbed representation of a standard Message. Returns an
     * embed with the sender's:
     * <ul><li>color</li><li>message author</li><li>message content</li>
     * <li>sender avatar</li><li>attached image if present</li>
     * <li>source channel</li><li>time stamps</li></ul>
     * @param message - The Message to generate an embed for.
     * @return A MessageEmbed representation of the Message parameter, or null
     * if null passed in.
     */
    public static MessageEmbed messageAsEmbed(Message message) {
        if (message == null)
            return null;
        
        Guild guild = message.getGuild();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(guild.getMember(message.getAuthor()).getColor());
        eb.setAuthor(guild.getMember(message.getAuthor()).getEffectiveName());
        eb.setDescription(message.getContentDisplay());
        eb.setThumbnail(message.getAuthor().getEffectiveAvatarUrl());
        if (!message.getAttachments().isEmpty())
            eb.setImage(message.getAttachments().get(0).getUrl());
        eb.addField("Source", message.getTextChannel().getAsMention(), true);
        // If we get timezone problems later, stop hard coding the 4 hours here.
        // I don't feel like doing all the temporal crap just to print a single
        // date and time. So screw it, the one liner hack wins.
        eb.addField("Date (US Eastern)", message.getCreationTime().minusHours(4).format(DateTimeFormatter.ISO_LOCAL_DATE), true);
        eb.addField("Time (US Eastern)", message.getCreationTime().minusHours(4).format(DateTimeFormatter.ISO_LOCAL_TIME), true);
        return eb.build();
    }
    
    public static MessageEmbed playerEmbed(PlayerAction action, AudioTrack track, Member member) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(action.getDisplayText())
          .setDescription(MarkdownFilter.clean(track.getInfo().title))
          .appendDescription("\n")
          .appendDescription(MarkdownFilter.clean(track.getInfo().uri))
          .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
          .addField("Channel", track.getInfo().author, true)
          .addField("Position", DurationFormatUtils.formatDuration(track.getPosition(), "mm:ss"), true)
          .addField("Length", DurationFormatUtils.formatDuration(track.getDuration(), "mm:ss"), true);
        if (track.getSourceManager() instanceof YoutubeAudioSourceManager)
            eb.setColor(0xff0000); // Sampled from the Youtube logo
        else if (track.getSourceManager() instanceof SoundCloudAudioSourceManager)
            eb.setColor(0xff5500); // Sampled from the SoundCloud play button
        else if (track.getSourceManager() instanceof TwitchStreamAudioSourceManager)
            eb.setColor(0x4b367c);
        return eb.build();
    }
    
    public static MessageEmbed playerQueueContentsEmbed(LinkedList<QueueElement> queue, Member member) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Queue")
          .setDescription(queue.size() + " tracks queued.")
          .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl());
        
        for (QueueElement element : queue) {
            AudioTrack track = element.getAudioTrack();
            eb.addField(MarkdownFilter.clean(track.getInfo().title), MarkdownFilter.clean(track.getInfo().author), false);
        }
        
        return eb.build();
    }
    
    public static MessageEmbed playerQueueEmptiedEmbed(LinkedList<QueueElement> queue, Member member) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Queue Emptied")
          .setDescription(queue.size() + " tracks removed.")
          .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl());
        
        for (QueueElement element : queue)
            eb.addField(element.getAudioTrack().getInfo().title, element.getAudioTrack().getInfo().uri, true);
        
        return eb.build();
    }
    
    public static MessageEmbed playerNoMatchesEmbed(String identifier, Member member) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("No Matches")
          .setDescription("No matches found for \"" + MarkdownFilter.clean(identifier) + "\"")
          .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl());
        return eb.build();
    }
    
    public static MessageEmbed playerLoadFailed(FriendlyException e, Member member) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Load Failed")
          .setDescription(MarkdownFilter.clean(e.getMessage()))
          .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl());
        return eb.build();
    }
}
