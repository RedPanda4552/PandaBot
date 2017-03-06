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
import java.util.logging.Level;

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.commands.CommandEcho;
import io.github.redpanda4552.PandaBot.commands.CommandHelp;
import io.github.redpanda4552.PandaBot.commands.CommandLeave;
import io.github.redpanda4552.PandaBot.commands.CommandLogDump;
import io.github.redpanda4552.PandaBot.commands.CommandNSFW;
import io.github.redpanda4552.PandaBot.commands.CommandNowPlaying;
import io.github.redpanda4552.PandaBot.commands.CommandPause;
import io.github.redpanda4552.PandaBot.commands.CommandPlay;
import io.github.redpanda4552.PandaBot.commands.CommandReload;
import io.github.redpanda4552.PandaBot.commands.CommandSeek;
import io.github.redpanda4552.PandaBot.commands.CommandSkip;
import io.github.redpanda4552.PandaBot.commands.CommandStop;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Responsible for receiving command attempts and calling execute() on valid
 * command attempts.
 */
public class CommandExecutor {

    private PandaBot pandaBot;
    private HashMap<String, AbstractCommand> cmds;
    
    public CommandExecutor(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        cmds = new HashMap<String, AbstractCommand>();
        cmds.put("/help", new CommandHelp(pandaBot));
        cmds.put("/echo", new CommandEcho(pandaBot));
        cmds.put("/play", new CommandPlay(pandaBot));
        cmds.put("/pause", new CommandPause(pandaBot));
        cmds.put("/seek", new CommandSeek(pandaBot));
        cmds.put("/skip", new CommandSkip(pandaBot));
        cmds.put("/stop", new CommandStop(pandaBot));
        cmds.put("/nowplaying", new CommandNowPlaying(pandaBot));
        cmds.put("/leave", new CommandLeave(pandaBot));
        cmds.put("/reload", new CommandReload(pandaBot));
        cmds.put("/logdump", new CommandLogDump(pandaBot));
        cmds.put("/nsfw", new CommandNSFW(pandaBot));
    }
    
    /**
     * Called for all messages sent to a text chat the bot can see.
     * Checks if the message starts with the command prefix and further parses 
     * the command and arguments. Fires command execution.
     */
    public void processCommand(Message message) {
        String text = message.getContent();
        User sender = message.getAuthor();
        MessageChannel channel = message.getChannel();
        VoiceChannel voiceChannel = pandaBot.getUserVoiceChannel(sender);
        
        pandaBot.getAudioManager();
        
        if (text.startsWith("/")) {
            String[] args = text.split(" ");
            args[0] = args[0].toLowerCase();
            AbstractCommand command = getCommand(args[0]);
            
            if (command == null) {
                pandaBot.queueDeleteMessage(message);
                StringBuilder sb = new StringBuilder(sender.getAsMention());
                sb.append(" " + args[0] + " is not a valid command. Use /help for a list of commands.");
                pandaBot.queueSendMessage(sb.toString(), channel);
                return;
            }
            
            pandaBot.addToHistory(sender.getName() + " executed command \"" + text + "\".", Level.INFO);
            command.execute(sender, message, channel, voiceChannel, ArrayUtils.remove(args, 0));
        }
    }
    
    /**
     * Gets the {@link AbstractCommand AbstractCommand} instance that matches
     * a given String name.
     * @param str - The String to fetch the command instance for.
     * @return The command instance that matches str, or null if no such command
     * exists.
     */
    private AbstractCommand getCommand(String str) {
        return cmds.containsKey(str) ? cmds.get(str) : null;
    }
}
