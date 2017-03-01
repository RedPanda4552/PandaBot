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
package io.github.redpanda4552.PandaBot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fredboat.audio.GuildPlayer;
import fredboat.util.TextUtils;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandSeek extends AbstractCommand {

    public CommandSeek(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        
        if (args.length < 1) {
            pandaBot.queueSendMessage("Missing time parameter. /seek <time>", channel, sender);
            return;
        }
        
        GuildPlayer player = pandaBot.getGuildPlayer();
        
        if (player.isQueueEmpty()) {
            pandaBot.queueSendMessage("Nothing is playing, nothing to seek.", channel, sender);
            return;
        }
        
        long time;
        
        try {
            time = TextUtils.parseTimeString(args[0]);
        } catch (IllegalStateException e) {
            pandaBot.queueSendMessage("Argument is not a properly formatted time. Should look something like 0:21 or 1:30.", channel, sender);
            return;
        }
        
        AudioTrack at = player.getPlayingTrack().getTrack();
        long max = at.getDuration();
        
        if (time > max) {
            pandaBot.queueSendMessage("Time exceeds video length.", channel, sender);
            return;
        } else if (time < 0) {
            pandaBot.queueSendMessage("Cannot seek to a negative time.", channel, sender);
            return;
        }
        
        pandaBot.queueSeek(time);
        pandaBot.queueSendMessage("Seeking **" + at.getInfo().title + "** to " + args[0], channel);
    }

}
