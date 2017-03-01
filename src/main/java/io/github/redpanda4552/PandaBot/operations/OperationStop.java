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
import fredboat.audio.PlayerRegistry;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * This class is a modified version of a class from Frederikam's FredBoat bot.
 * <a href="https://github.com/Frederikam/FredBoat">GitHub Link</a>. 
 */
public class OperationStop extends AbstractOperation {

    private MessageChannel messageChannel;
    
    public OperationStop(PandaBot pandaBot, MessageChannel messageChannel) {
        super(pandaBot);
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = PlayerRegistry.get(PandaBot.getGuild());
        player.setCurrentMessageChannel(messageChannel);
        int count = player.getRemainingTracks().size();

        player.clear();
        player.skip();

        switch (count) {
            case 0:
                messageChannel.sendMessage("The queue was already empty.").queue();
                break;
            case 1:
                messageChannel.sendMessage("The queue has been emptied, `1` song has been removed.").queue();
                break;
            default:
                messageChannel.sendMessage("The queue has been emptied, `" + count + "` songs have been removed.").queue();
                break;
        }
        
        player.leaveVoiceChannelRequest(messageChannel, true);
        complete();
    }

}
