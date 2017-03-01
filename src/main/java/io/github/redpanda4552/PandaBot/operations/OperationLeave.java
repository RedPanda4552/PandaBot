package io.github.redpanda4552.PandaBot.operations;

import fredboat.audio.GuildPlayer;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;

public class OperationLeave extends AbstractOperation {

    private MessageChannel messageChannel;
    
    public OperationLeave(PandaBot pandaBot, MessageChannel messageChannel) {
        super(pandaBot);
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(messageChannel);
        player.leaveVoiceChannelRequest(messageChannel, false);
        complete();
    }

}
