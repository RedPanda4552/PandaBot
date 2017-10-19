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

import java.util.Set;
import java.util.TreeSet;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandHelp extends AbstractCommand {

    private final TreeSet<String> general;
    private final TreeSet<String> music;
    
    public CommandHelp(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
        TreeSet<String> g = new TreeSet<String>();
        TreeSet<String> m = new TreeSet<String>();
        Set<String> commandStringSet = commandProcessor.getAllCommands();
        
        for (String commandString : commandStringSet) {
            AbstractCommand abstractCommand = commandProcessor.getCommand(commandString);
            
            switch (abstractCommand.getCommandType()) {
            case GENERAL:
                g.add(commandString);
                break;
            case MUSIC:
                m.add(commandString);
                break;
            case SUPER:
                break;
            }
        }
        
        general = g;
        music = m;
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        MessageBuilder helpBuilder = new MessageBuilder();
        helpBuilder.append("```ini\n");
        helpBuilder.append("[ PandaBot Commands - General ]\n");
        helpBuilder.append("``````diff\n");
        
        for (String command : general) {
            if (pandaBot.memberHasPermission(member, command)) {
                helpBuilder.append("+ ");
            } else {
                helpBuilder.append("- ");
            }
            
            helpBuilder.append(String.format("%s%s %s // %s\n", CommandProcessor.PREFIX, command, commandProcessor.getCommand(command).getHelpArgs(), commandProcessor.getCommand(command).getHelpMessage()));
        }
        
        helpBuilder.append("``````ini\n");
        helpBuilder.append("[ PandaBot Commands - Music ]");
        helpBuilder.append("``````diff\n");
        
        for (String command : music) {
            if (pandaBot.memberHasPermission(member, command)) {
                helpBuilder.append("+ ");
            } else {
                helpBuilder.append("- ");
            }
            
            helpBuilder.append(String.format("%s%s %s // %s\n", CommandProcessor.PREFIX, command, commandProcessor.getCommand(command).getHelpArgs(), commandProcessor.getCommand(command).getHelpMessage()));
        }
        
        helpBuilder.append("```");
        MessageBuilder mentionBuilder = new MessageBuilder();
        mentionBuilder.append(member.getAsMention() + ", check your DMs for help info");
        pandaBot.sendMessage(msgChannel, mentionBuilder.build());
        pandaBot.sendDirectMessage(member, helpBuilder.build());
    }

    @Override
    public String getHelpArgs() {
        return "";
    }
    
    @Override
    public String getHelpMessage() {
        return "Display this help dialog.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
