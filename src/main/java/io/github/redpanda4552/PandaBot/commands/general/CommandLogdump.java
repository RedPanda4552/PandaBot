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
package io.github.redpanda4552.PandaBot.commands.general;

import java.util.Iterator;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandLogdump extends AbstractCommand {

    private final int CHAR_LIMIT = 2000;
    
    public CommandLogdump(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        int pageLimit = 1;
        MessageBuilder mb = new MessageBuilder();
        
        if (args.length >= 1) {
            try {
                pageLimit = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                mb.append("Argument \"")
                  .append(args[0])
                  .append("\" is not an integer, assuming 1.\n");
            }
        }
        
        Iterator<String> iterator = LogBuffer.getGuildInfo(guild).iterator();
        int page = 1;
        mb.append("```ini\n")
          .append("[PandaBot Logdump Page ")
          .append(page)
          .append("]\n");
        page++;
        String next = "";
        
        while (iterator.hasNext()) {
            next = iterator.next();
            
            if (mb.length() + next.length() < CHAR_LIMIT && iterator.hasNext()) {
                mb.append(next).append("\n");
            } else {
                mb.append("```");
                pandaBot.sendMessage(msgChannel, mb.build());
                mb.clear();
                if (page > pageLimit)
                    break;
                mb.append("```ini\n")
                  .append("[PandaBot Logdump Page ")
                  .append(page)
                  .append("]\n");
                page++;
            }
        }
    }

    @Override
    public String getHelpArgs() {
        return "<page count>";
    }

    @Override
    public String getHelpMessage() {
        return "Dump command and log history for this server.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }
    
}
