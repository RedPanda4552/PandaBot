package io.github.redpanda4552.PandaBot.operations;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;

public class OperationDelete extends AbstractOperation {

    private Message message;
    
    public OperationDelete(PandaBot pandaBot, Message message) {
        super(pandaBot);
        this.message = message;
    }
    
    @Override
    public void execute() {
        if (message.getChannelType() == ChannelType.TEXT) {
            message.deleteMessage().queue();
            complete();
        } else {
            completeWithError("Cannot delete message; MessageChannel is of type " + message.getChannelType().toString().toLowerCase() + ", not a public channel.");
        }
    }
}
