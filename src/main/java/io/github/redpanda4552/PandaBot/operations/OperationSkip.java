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
package io.github.redpanda4552.PandaBot.operations;

import fredboat.audio.GuildPlayer;
import fredboat.audio.queue.AudioTrackContext;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * This class is a modified version of a class from Frederikam's FredBoat bot.
 * <a href="https://github.com/Frederikam/FredBoat">GitHub Link</a>. 
 */
public class OperationSkip extends AbstractOperation {

    private MessageChannel messageChannel;
    
    public OperationSkip(PandaBot pandaBot, MessageChannel messageChannel) {
        super(pandaBot);
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(messageChannel);
        
        if (player.isQueueEmpty()) {
            messageChannel.sendMessage("The queue is empty!").queue();
            player.leaveVoiceChannelRequest(messageChannel, true);
        }
        
        AudioTrackContext atc = player.getPlayingTrack();
        player.skip();
        
        if (atc == null) {
            messageChannel.sendMessage("Couldn't find track to skip.").queue();
        } else {
            messageChannel.sendMessage("Skipped track #1: **" + atc.getTrack().getInfo().title + "**").queue();
        }
        
        complete();
    }
}
