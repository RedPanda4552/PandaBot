package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandLogDump extends AbstractCommand {

    public CommandLogDump(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        
        int limiter = 10;
        boolean pub = false;
        
        if (args.length == 0) {
            pandaBot.queueSendMessage(args.toString(), channel);
            pandaBot.queueSendMessage(sender.getAsMention() + " No arguments specified, assuming 10 logs.", channel);
        }
        
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("all")) {
                limiter = -1;
            } else {
                try {
                    limiter = Integer.parseInt(args[0]);
                    
                    if (limiter < 1) {
                        limiter = 10;
                        pandaBot.queueSendMessage(sender.getAsMention() + " \"" + args[0] + "\" is not allowed, must be greater than 0. Assuming 10.", channel);
                    }
                } catch (NumberFormatException e) {
                    limiter = 10;
                    pandaBot.queueSendMessage(sender.getAsMention() + " \"" + args[0] + "\" is not an integer. Assuming 10.", channel);
                }
            }
        }
        
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("public")) {
                pub = true;
            }
        }
        
        
        if (pub) {
            pandaBot.queueLogDump(channel, limiter);
        } else {
            pandaBot.queueLogDump(sender, limiter);
        }
    }
}
