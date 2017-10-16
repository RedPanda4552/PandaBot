package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public abstract class AbstractCommand {

    public enum CommandType {SUPER, GENERAL, MUSIC};
    
    protected PandaBot pandaBot;
    protected CommandProcessor commandProcessor;
    protected CommandType commandType;
    
    public AbstractCommand(PandaBot pandaBot, CommandProcessor commandProcessor) {
        this.pandaBot = pandaBot;
        this.commandProcessor = commandProcessor;
    }
    
    public abstract void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args);
    public abstract String getHelpArgs();
    public abstract String getHelpMessage();
    public abstract CommandType getCommandType();
    
    /**
     * Add spaces in between the args to restore them back to a single string.
     */
    protected String argsToStr(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        
        return sb.toString().trim();
    }
}
