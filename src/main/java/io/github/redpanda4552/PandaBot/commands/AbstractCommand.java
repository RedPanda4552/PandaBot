package io.github.redpanda4552.PandaBot.commands;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public abstract class AbstractCommand {

    protected PandaBot pandaBot;
    
    public AbstractCommand(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
    }
    
    public abstract void execute(User sender, Message message, MessageChannel channel, VoiceChannel voiceChannel, String[] args);
    
    protected String concatArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        
        for (String str : args) {
            sb.append(str).append(" ");
        }
        
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        
        return sb.toString();
    }
}
