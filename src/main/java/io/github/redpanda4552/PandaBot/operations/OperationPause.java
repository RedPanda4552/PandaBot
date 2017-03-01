package io.github.redpanda4552.PandaBot.operations;

import fredboat.audio.GuildPlayer;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class OperationPause extends AbstractOperation {
    
    private User sender;
    private MessageChannel messageChannel;

    public OperationPause(PandaBot pandaBot, User sender, MessageChannel messageChannel) {
        super(pandaBot);
        this.sender = sender;
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(messageChannel);
        
        if (player.isQueueEmpty()) {
            messageChannel.sendMessage("The queue is empty.").queue();
        } else if (player.isPaused()) {
            player.play();
            messageChannel.sendMessage("The player is already paused.").queue();
        } else if (player.getUsersInVC().isEmpty()) {
            player.leaveVoiceChannelRequest(messageChannel, true);
            messageChannel.sendMessage("There is no one in the voice channel. Exiting.");
        } else {
            player.pause();
            messageChannel.sendMessage("Playback paused by **" + sender.getName() + "**. You can unpause it with `/pause` or `/play`.").queue();
        }
        
        complete();
    }
}
