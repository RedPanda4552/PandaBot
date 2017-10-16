package io.github.redpanda4552.PandaBot.commands.general;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandLmgtfy extends AbstractCommand {

    public CommandLmgtfy(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append("https://lmgtfy.com/?q=");
        
        for (String arg : args) {
            messageBuilder.append(arg).append("+");
        }
        
        messageBuilder.replaceLast("+", "");
        pandaBot.sendMessage(msgChannel, messageBuilder.build());
    }
    
    @Override
    public String getHelpArgs() {
        return "<search terms>";
    }

    @Override
    public String getHelpMessage() {
        return "Generate a \"Let Me Google That For You\" link.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
