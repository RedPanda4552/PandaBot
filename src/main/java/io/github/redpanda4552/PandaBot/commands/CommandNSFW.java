package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandNSFW extends AbstractCommand {

    public CommandNSFW(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        
        if (args.length < 1) {
            pandaBot.queueSendMessage("Nothing for me to make a NSFW for.", channel, sender);
            return;
        }

        StringBuilder mb = new StringBuilder();
        mb.append("NSFW from ");
        mb.append(sender.getAsMention() + ": **");
        mb.append(message.getContent().replaceFirst("/nsfw", ""));
        mb.append("**");
        pandaBot.queueSendMessage(mb.toString(), channel);
    }

}
