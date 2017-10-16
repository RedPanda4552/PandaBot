package io.github.redpanda4552.PandaBot.commands.general;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandEcho extends AbstractCommand {

    public CommandEcho(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        if (args.length == 0)
            return;
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(argsToStr(args));
        pandaBot.sendMessage(msgChannel, messageBuilder.build());
    }
    
    @Override
    public String getHelpArgs() {
        return "<text>";
    }

    @Override
    public String getHelpMessage() {
        return "Make PandaBot say something.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
