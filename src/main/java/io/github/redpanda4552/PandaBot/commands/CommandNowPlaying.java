package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandNowPlaying extends AbstractCommand {

    public CommandNowPlaying(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        pandaBot.queueNowPlaying(channel);
    }

}
