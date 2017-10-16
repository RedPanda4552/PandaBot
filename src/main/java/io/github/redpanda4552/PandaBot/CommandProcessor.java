package io.github.redpanda4552.PandaBot;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand.CommandType;
import io.github.redpanda4552.PandaBot.commands.CommandReload;
import io.github.redpanda4552.PandaBot.commands.general.CommandEcho;
import io.github.redpanda4552.PandaBot.commands.general.CommandHelp;
import io.github.redpanda4552.PandaBot.commands.general.CommandLmgtfy;
import io.github.redpanda4552.PandaBot.commands.general.CommandNSFW;
import io.github.redpanda4552.PandaBot.commands.general.CommandPing;
import io.github.redpanda4552.PandaBot.commands.music.CommandJoin;
import io.github.redpanda4552.PandaBot.commands.music.CommandLeave;
import io.github.redpanda4552.PandaBot.commands.music.CommandNowplaying;
import io.github.redpanda4552.PandaBot.commands.music.CommandPause;
import io.github.redpanda4552.PandaBot.commands.music.CommandPlay;
import io.github.redpanda4552.PandaBot.commands.music.CommandQueue;
import io.github.redpanda4552.PandaBot.commands.music.CommandSkip;
import io.github.redpanda4552.PandaBot.commands.music.CommandStop;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandProcessor {

    public static final String PREFIX = "!";
    
    private PandaBot pandaBot;
    private HashMap<String, AbstractCommand> commandMap;
    
    public CommandProcessor(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        commandMap = new HashMap<String, AbstractCommand>();
        commandMap.put("echo", new CommandEcho(pandaBot, this));
        commandMap.put("nsfw", new CommandNSFW(pandaBot, this));
        commandMap.put("lmgtfy", new CommandLmgtfy(pandaBot, this));
        commandMap.put("play", new CommandPlay(pandaBot, this));
        commandMap.put("pause", new CommandPause(pandaBot, this));
        commandMap.put("skip", new CommandSkip(pandaBot, this));
        commandMap.put("stop", new CommandStop(pandaBot, this));
        commandMap.put("nowplaying", new CommandNowplaying(pandaBot, this));
        commandMap.put("queue", new CommandQueue(pandaBot, this));
        commandMap.put("join", new CommandJoin(pandaBot, this));
        commandMap.put("leave", new CommandLeave(pandaBot, this));
        commandMap.put("reload", new CommandReload(pandaBot, this));
        commandMap.put("ping", new CommandPing(pandaBot, this));
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
        String messageContent = message.getStrippedContent();
        
        if (!hasPrefix(messageContent))
            return;
        
        String[] args = messageContent.split(" ");
        // hasPrefix is true at this point, so there is at LEAST 1 char in here
        String commandStr = args[0].replaceFirst(PREFIX, ""); // Remove the leading prefix
        args = (String[]) ArrayUtils.remove(args, 0);
        
        if (commandMap.containsKey(commandStr)) {
            AbstractCommand ac = commandMap.get(commandStr);
            boolean hasPermission = pandaBot.userHasPermission(member, ac.getCommandType() == CommandType.SUPER ? "super" : commandStr);
            
            if (hasPermission) {
                message.delete().complete();
                ac.execute(guild, msgChannel, member, args);
            }
            
            pandaBot.logInfo(String.format(
                    "Command // g:%s[%s] // mc:%s[%s] // u:%s[%s] // c:%s // a:%s // p:%b", 
                    guild.getName(), guild.getId(), 
                    msgChannel.getName(), msgChannel.getId(), 
                    member.getUser().getName(), member.getUser().getId(), 
                    commandStr, 
                    ArrayUtils.toString(args), 
                    hasPermission
            ));
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
}
