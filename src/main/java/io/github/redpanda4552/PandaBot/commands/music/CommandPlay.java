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
package io.github.redpanda4552.PandaBot.commands.music;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.player.SelectionTracker;
import io.github.redpanda4552.PandaBot.player.YoutubeAPI;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandPlay extends AbstractCommand {

    private final int SEARCH_LIMIT = 5;
    
    private SelectionTracker st;
    private YoutubeAudioSourceManager ytasm;
    
    public CommandPlay(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
        st = pandaBot.getSelectionTracker();
        ytasm = new YoutubeAudioSourceManager();
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        VoiceChannel vc = member.getVoiceState().getChannel();
        
        // If member is not in a voice channel
        if (vc == null) {
            MessageBuilder mb = new MessageBuilder();
            mb.append(member.getAsMention());
            mb.append(" You are not in a voice channel, so I have nowhere to join to!");
            pandaBot.sendMessage(msgChannel, mb.build());
            return;
        }
        
        pandaBot.joinVoiceChannel(guild, vc);
        
        // If no arguments specified
        if (args.length == 0) {
            pandaBot.sendMessage(msgChannel, "No video or search specified!");
            return;
        }
        
        AudioPlaylist ap = st.getAudioPlaylistOf(member.getUser());
        
        // If a number is specified and we are expecting a selection from a search to be made
        if (StringUtils.isNumeric(args[0]) && st.contains(member.getUser())) {
            // -1 because arrays (rightfully) start at 0
            AudioTrack at = ap.getTracks().get(Integer.valueOf(args[0]) - 1);
            pandaBot.getGlobalAudioController().play(guild, msgChannel, member, at.getIdentifier());
            st.remove(member.getUser());
            return;
        }
        
        // Video search; a slightly modified version of FredBoat's.
        if (!args[0].toLowerCase().startsWith("http")) {
            String search = argsToStr(args)
                    .toLowerCase()
                    .replaceAll("[.,/#!$%\\^&*;:{}=\\-_`~()]", "");
            
            try {
                ap = YoutubeAPI.search(search, SEARCH_LIMIT, ytasm);
            } catch (UnirestException e) {
                pandaBot.sendMessage(msgChannel, "Something went wrong when searching. Try again or use a direct URL.");
                return;
            }
            
            if (ap.getTracks().isEmpty()) {
                pandaBot.sendMessage(msgChannel, String.format("No results for search **%s**.", search));
                return;
            }
            
            MessageBuilder mb = new MessageBuilder();
            mb.append("Showing top ")
              .append(SEARCH_LIMIT)
              .append(" results for **")
              .append(search)
              .append("**. Select the video to play with **")
              .append(CommandProcessor.PREFIX)
              .append("play <number>**:\n");
            int i = 1;
            
            for (AudioTrack at : ap.getTracks()) {
                mb.append("**")
                  .append(i)
                  .append(":** ")
                  .append(at.getInfo().title)
                  .append("\n\t**Length:** ")
                  .append(DurationFormatUtils.formatDuration(at.getDuration(), "mm:ss"))
                  .append("\t**Channel:** ")
                  .append(at.getInfo().author)
                  .append("\n");
                i++;
            }
            
            Message msg = pandaBot.sendMessage(msgChannel, mb.build());
            st.upsertAP(member.getUser(), msg, ap);
            return;
        }
        
        if (st.contains(member.getUser()))
            st.remove(member.getUser());
        
        pandaBot.getGlobalAudioController().play(guild, msgChannel, member, args[0]);
    }

    @Override
    public String getHelpArgs() {
        return "[<url>] [<search terms>]";
    }

    @Override
    public String getHelpMessage() {
        return "Play audio from Youtube, Soundcloud or other major media sources.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
