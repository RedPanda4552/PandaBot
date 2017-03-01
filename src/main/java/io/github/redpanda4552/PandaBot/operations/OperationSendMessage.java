package io.github.redpanda4552.PandaBot.operations;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;

public class OperationSendMessage extends AbstractOperation {

    private String text;
    private MessageChannel channel;
    private User[] mention;
    
    public OperationSendMessage(PandaBot pandaBot, String text, MessageChannel channel, User... mention) {
        super(pandaBot);
        this.text = text;
        this.channel = channel;
        this.mention = mention;
    }
    
    @Override
    public void execute() {
        try {
            StringBuilder mentionBuilder = new StringBuilder("");
            
            if (mention.length != 0) {
                for (User user : mention) {
                    if (user != null) {
                        mentionBuilder.append(user.getAsMention()).append(" ");
                    }
                }
            }
            
            channel.sendMessage(mentionBuilder.toString() + text).queue();
            complete();
        } catch (PermissionException e) {
            completeWithError("Insufficient permissions!");
        }
    }
}
