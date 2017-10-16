package io.github.redpanda4552.PandaBot.commands.music;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandStop extends AbstractCommand {

    public CommandStop(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        pandaBot.getGlobalAudioController().stop(guild, msgChannel);
    }

    @Override
    public String getHelpArgs() {
        return "";
    }

    @Override
    public String getHelpMessage() {
        return "Empty the queue and stop the current track.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
