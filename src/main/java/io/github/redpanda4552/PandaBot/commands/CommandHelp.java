package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandHelp extends AbstractCommand {

    private final String help = 
            "```" + "\n" + 
            "== PandaBot Help ==" + "\n" +
            "> /help - Display this help message." + "\n" + 
            "> /echo <text> - Make the bot send the following message." + "\n" + 
            "> /play <url | search terms> - Stream from a YouTube URL or search YouTube." + "\n" +
            "> /pause - Toggle pause on PandaBot" + "\n" +
            "> /seek <time> - Seek to the specified time in the current video." + "\n" + 
            "> /skip - Skip the current track and move on to the next in the queue." + "\n" +
            "> /stop - Stop playback from PandaBot, and empty the queue." + "\n" +
            "> /leave - Remove PandaBot from it's current channel." + "\n" +
            "> /nowplaying - Show what is currently playing, if anything." + "\n" + 
            "> /reload - Stops JDA, sends the current PandaBot to the GC and starts a new instance. May help if the player is struggling." + "\n" +
            "> /logdump <n | all> <public> - Dump n or all log entries to your DMs or the public channel. You cannot dump more than 20 logs to the public channel." + "\n" + 
            "```"
    ;
    
    public CommandHelp(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queuePrivateMessage(sender, help);
        pandaBot.queueDeleteMessage(message);
        pandaBot.queueSendMessage(sender.getAsMention() + ", check your DM's for help info.", channel);
    }

}
