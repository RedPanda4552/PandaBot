package io.github.redpanda4552.PandaBot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fredboat.audio.GuildPlayer;
import fredboat.util.TextUtils;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandSeek extends AbstractCommand {

    public CommandSeek(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args) {
        pandaBot.queueDeleteMessage(message);
        
        if (args.length < 1) {
            pandaBot.queueSendMessage("Missing time parameter. /seek <time>", channel, sender);
            return;
        }
        
        GuildPlayer player = pandaBot.getGuildPlayer();
        
        if (player.isQueueEmpty()) {
            pandaBot.queueSendMessage("Nothing is playing, nothing to seek.", channel, sender);
            return;
        }
        
        long time;
        
        try {
            time = TextUtils.parseTimeString(args[0]);
        } catch (IllegalStateException e) {
            pandaBot.queueSendMessage("Argument is not a properly formatted time. Should look something like 0:21 or 1:30.", channel, sender);
            return;
        }
        
        AudioTrack at = player.getPlayingTrack().getTrack();
        long max = at.getDuration();
        
        if (time > max) {
            pandaBot.queueSendMessage("Time exceeds video length.", channel, sender);
            return;
        } else if (time < 0) {
            pandaBot.queueSendMessage("Cannot seek to a negative time.", channel, sender);
            return;
        }
        
        pandaBot.queueSeek(time);
        pandaBot.queueSendMessage("Seeking **" + at.getInfo().title + "** to " + args[0], channel);
    }

}
