package io.github.redpanda4552.PandaBot.commands.music;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandPause extends AbstractCommand {

    public CommandPause(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        pandaBot.getGlobalAudioController().pause(guild, msgChannel);
    }

    @Override
    public String getHelpArgs() {
        return "";
    }

    @Override
    public String getHelpMessage() {
        return "Pause or unpause the current track.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
