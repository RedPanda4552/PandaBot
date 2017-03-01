package io.github.redpanda4552.PandaBot.operations;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * This class is a modified version of a class from Frederikam's FredBoat bot.
 * <a href="https://github.com/Frederikam/FredBoat">GitHub Link</a>. 
 */
public class OperationStop extends AbstractOperation {

    private MessageChannel messageChannel;
    
    public OperationStop(PandaBot pandaBot, MessageChannel messageChannel) {
        super(pandaBot);
        this.messageChannel = messageChannel;
    }

    @Override
    public void execute() {
        GuildPlayer player = PlayerRegistry.get(PandaBot.getGuild());
        player.setCurrentMessageChannel(messageChannel);
        int count = player.getRemainingTracks().size();

        player.clear();
        player.skip();

        switch (count) {
            case 0:
                messageChannel.sendMessage("The queue was already empty.").queue();
                break;
            case 1:
                messageChannel.sendMessage("The queue has been emptied, `1` song has been removed.").queue();
                break;
            default:
                messageChannel.sendMessage("The queue has been emptied, `" + count + "` songs have been removed.").queue();
                break;
        }
        
        player.leaveVoiceChannelRequest(messageChannel, true);
        complete();
    }

}
