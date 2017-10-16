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
            if (pandaBot.userHasPermission(member, command)) {
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
            if (pandaBot.userHasPermission(member, command)) {
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
