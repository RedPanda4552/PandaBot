package io.github.redpanda4552.PandaBot.operations;

import fredboat.audio.GuildPlayer;
import fredboat.audio.queue.AudioTrackContext;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * This class is a modified version of a class from Frederikam's FredBoat bot.
 * <a href="https://github.com/Frederikam/FredBoat">GitHub Link</a>. 
 */
public class OperationSkip extends AbstractOperation {

    private MessageChannel messageChannel;
    
    public OperationSkip(PandaBot pandaBot, MessageChannel messageChannel) {
        super(pandaBot);
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(messageChannel);
        
        if (player.isQueueEmpty()) {
            messageChannel.sendMessage("The queue is empty!").queue();
            player.leaveVoiceChannelRequest(messageChannel, true);
        }
        
        AudioTrackContext atc = player.getPlayingTrack();
        player.skip();
        
        if (atc == null) {
            messageChannel.sendMessage("Couldn't find track to skip.").queue();
        } else {
            messageChannel.sendMessage("Skipped track #1: **" + atc.getTrack().getInfo().title + "**").queue();
        }
        
        complete();
    }
}
