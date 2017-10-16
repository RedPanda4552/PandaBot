package io.github.redpanda4552.PandaBot.commands.general;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandNSFW extends AbstractCommand {

    public CommandNSFW(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        if (args.length == 0)
            return;
        MessageBuilder messageBuilder;
        
        for (String arg : args) {
            messageBuilder = new MessageBuilder();
            messageBuilder.append("NSFW from ").append(member.getAsMention()).append(": <");
            messageBuilder.append(arg).append(">");
            pandaBot.sendMessage(msgChannel, messageBuilder.build());
        }
    }

    @Override
    public String getHelpArgs() {
        return "<url>";
    }
    
    @Override
    public String getHelpMessage() {
        return "Suppress the image preview from a URL. Useful for NSFW or spoiler content.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
