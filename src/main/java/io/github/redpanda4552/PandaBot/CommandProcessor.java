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
package io.github.redpanda4552.PandaBot;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.PandaBot.commands.*;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand.CommandType;
import io.github.redpanda4552.PandaBot.commands.general.*;
import io.github.redpanda4552.PandaBot.commands.music.*;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandProcessor {

    public static final String PREFIX = "!";
    
    private PandaBot pandaBot;
    private HashMap<String, AbstractCommand> commandMap;
    private long commandAttempts, commandExecutions;
    
    public CommandProcessor(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        commandMap = new HashMap<String, AbstractCommand>();
        // General
        commandMap.put("echo", new CommandEcho(pandaBot, this));
        commandMap.put("f", new CommandF(pandaBot, this));
        commandMap.put("nsfw", new CommandNSFW(pandaBot, this));
        commandMap.put("lmgtfy", new CommandLmgtfy(pandaBot, this));
        commandMap.put("ping", new CommandPing(pandaBot, this));
        commandMap.put("metrics", new CommandMetrics(pandaBot, this));
        commandMap.put("logdump", new CommandLogdump(pandaBot, this));
        commandMap.put("report", new CommandReport(pandaBot, this));
        // Music
        commandMap.put("play", new CommandPlay(pandaBot, this));
        commandMap.put("replay", new CommandReplay(pandaBot, this));
        commandMap.put("pause", new CommandPause(pandaBot, this));
        commandMap.put("skip", new CommandSkip(pandaBot, this));
        commandMap.put("stop", new CommandStop(pandaBot, this));
        commandMap.put("nowplaying", new CommandNowplaying(pandaBot, this));
        commandMap.put("queue", new CommandQueue(pandaBot, this));
        commandMap.put("join", new CommandJoin(pandaBot, this));
        commandMap.put("leave", new CommandLeave(pandaBot, this));
        commandMap.put("save", new CommandSave(pandaBot, this));
        commandMap.put("load", new CommandLoad(pandaBot, this));
        // Super
        commandMap.put("reload", new CommandReload(pandaBot, this));
        commandMap.put("shutdown", new CommandShutdown(pandaBot, this));
        // Help MUST be the last command registered! It looks at this map!
        commandMap.put("help", new CommandHelp(pandaBot, this));
    }
    
    /**
     * Test if a string starts with the command prefix.
     */
    private boolean hasPrefix(String str) {
        return str.startsWith(PREFIX);
    }
    
    /**
     * Check if a message is a command, and execute if so.
     */
    public void process(Guild guild, MessageChannel msgChannel, Member member, Message message) {
        String messageContent = message.getContentDisplay();
        
        if (!hasPrefix(messageContent))
            return;
        
        String[] args = messageContent.split(" ");
        // hasPrefix is true at this point, so there is at LEAST 1 char in here
        String commandStr = args[0].replaceFirst(PREFIX, "").toLowerCase(); // Remove the leading prefix
        args = (String[]) ArrayUtils.remove(args, 0);
        
        if (commandMap.containsKey(commandStr)) {
            AbstractCommand ac = commandMap.get(commandStr);
            boolean hasPermission = ac.getCommandType() == CommandType.SUPER ? pandaBot.memberIsSuperuser(member) : pandaBot.memberHasPermission(member, commandStr);
            LogBuffer.guildInfo(guild, String.format(
                    "Command // g:%s // mc:%s // u:%s // c:%s // a:%s // p:%b", 
                    guild.getName(), 
                    msgChannel.getName(), 
                    member.getUser().getName(),
                    commandStr, 
                    ArrayUtils.toString(args), 
                    hasPermission
            ));
            commandAttempts++;
            
            if (hasPermission) {
                commandExecutions++;
                ac.execute(guild, msgChannel, member, args);
            }
            
            return;
        }
    }
    
    public Set<String> getAllCommands() {
        return commandMap.keySet();
    }
    
    public AbstractCommand getCommand(String name) {
        if (commandMap.containsKey(name))
            return commandMap.get(name);
        return null;
    }
    
    public long getCommandAttempts() {
        return commandAttempts;
    }
    
    public long getCommandExecutions() {
        return commandExecutions;
    }
}
