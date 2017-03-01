package io.github.redpanda4552.PandaBot.listeners;

import java.util.logging.Level;

import io.github.redpanda4552.PandaBot.CommandExecutor;
import io.github.redpanda4552.PandaBot.Main;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
    
    private PandaBot pandaBot;
    private CommandExecutor commandExecutor;
    
    public Listener(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
        this.commandExecutor = new CommandExecutor(pandaBot);
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        pandaBot.checkChannelStatus(event);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            commandExecutor.processCommand(event.getMessage());
        } catch (Throwable t) {
            pandaBot.addToHistory(t.getMessage(), Level.WARNING);
            
            for (StackTraceElement elem : t.getStackTrace()) {
                pandaBot.addToHistory(elem.toString(), Level.WARNING);
            }
            
            pandaBot.queuePrivateMessage(PandaBot.getJDA().getUserById(Main.getOperatorId()), "An exception was caught by the catch-all in PandaBot's Listener. Logdump is below.");
            pandaBot.queueLogDump(PandaBot.getJDA().getUserById(Main.getOperatorId()), -1);
        }
    }
    
    @Override
    public void onShutdown(ShutdownEvent event) {
        PandaBot.waitingForShutdown = false;
        pandaBot.addToHistory("JDA shutdown completed.", Level.INFO);
    }
}
