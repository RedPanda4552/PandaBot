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

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandLogDump extends AbstractCommand {

    public CommandLogDump(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        
        int limiter = 10;
        boolean pub = false;
        
        if (args.length == 0) {
            pandaBot.queueSendMessage(args.toString(), channel);
            pandaBot.queueSendMessage(sender.getAsMention() + " No arguments specified, assuming 10 logs.", channel);
        }
        
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("all")) {
                limiter = -1;
            } else {
                try {
                    limiter = Integer.parseInt(args[0]);
                    
                    if (limiter < 1) {
                        limiter = 10;
                        pandaBot.queueSendMessage(sender.getAsMention() + " \"" + args[0] + "\" is not allowed, must be greater than 0. Assuming 10.", channel);
                    }
                } catch (NumberFormatException e) {
                    limiter = 10;
                    pandaBot.queueSendMessage(sender.getAsMention() + " \"" + args[0] + "\" is not an integer. Assuming 10.", channel);
                }
            }
        }
        
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("public")) {
                pub = true;
            }
        }
        
        
        if (pub) {
            pandaBot.queueLogDump(channel, limiter);
        } else {
            pandaBot.queueLogDump(sender, limiter);
        }
    }
}
