package io.github.redpanda4552.PandaBot;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

    private PandaBot pandaBot;
    private CommandProcessor commandProcessor;
    
    public EventListener(PandaBot pandaBot, CommandProcessor commandProcessor) {
        this.pandaBot = pandaBot;
        this.commandProcessor = commandProcessor;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            commandProcessor.process(event.getGuild(), event.getChannel(), event.getMember(), event.getMessage());
        } catch (Throwable t) {
            pandaBot.logWarning(t.getMessage(), t.getStackTrace());
        }
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        
        if (vc.getMembers().size() == 1 && vc.getMembers().get(0).getUser().getId().equals(pandaBot.getBotId())) {
            pandaBot.leaveVoiceChannel(event.getGuild());
        }
    }
    
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        
        if (vc.getMembers().size() == 1 && vc.getMembers().get(0).getUser().getId().equals(pandaBot.getBotId())) {
            pandaBot.leaveVoiceChannel(event.getGuild());
        }
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        MessageBuilder mb = new MessageBuilder();
        mb.append("Welcome ")
          .append(event.getMember().getAsMention())
          .append(" to **")
          .append(event.getGuild().getName())
          .append("**!");
        pandaBot.sendMessage(event.getMember().getDefaultChannel(), mb.build());
    }
}
