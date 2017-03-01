package io.github.redpanda4552.PandaBot.operations;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.User;

public class OperationPrivate extends AbstractOperation {

    private User user;
    private String text;
    
    public OperationPrivate(PandaBot pandaBot, User user, String text) {
        super(pandaBot);
        this.user = user;
        this.text = text;
    }
    
    @Override
    public void execute() {
        if (!user.hasPrivateChannel()) {
            user.openPrivateChannel().queue();
        }
        
        user.getPrivateChannel().sendMessage(text).queue();
        complete();
    }
}
