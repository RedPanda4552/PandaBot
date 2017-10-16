package io.github.redpanda4552.PandaBot;

import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import io.github.redpanda4552.PandaBot.player.GlobalAudioController;
import io.github.redpanda4552.PandaBot.player.SelectionTracker;
import io.github.redpanda4552.PandaBot.player.ServerAudioController;
import io.github.redpanda4552.PandaBot.player.YoutubeAPI;
import io.github.redpanda4552.PandaBot.util.RunningState;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class PandaBot {

    private final Logger log;
    private final String superuserId;
    
    private boolean running = true;
    private JDA jda;
    private CommandProcessor commandProcessor;
    private GlobalAudioController gac;
    private SelectionTracker st;
    
    public PandaBot(Logger log, String token, String youtubeApiKey, String superuserId) {
        this.log = log;
        this.superuserId = superuserId;
        init(token, youtubeApiKey);
        
        // This loop keeps the core PandaBot thread alive. JDA and LavaPlayer
        // threads do all the work; killing this loop causes Main to generate a
        // new PandaBot instance.
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logWarning(e.getMessage(), e.getStackTrace());
            }
        }
    }
    
    /**
     * All the initialization goodness. Creates the {@link CommandProcessor},
     * {@link JDA} and {@link GlobalAudioController}.
     * @param token - Discord bot token to use for JDA.
     * @param youtubeApiKey - Youtube API Key to use for video searches and info
     */
    private void init(String token, String youtubeApiKey) {
        st = new SelectionTracker(); // Required by the play command
        commandProcessor = new CommandProcessor(this);
        
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setAutoReconnect(true)
                    .addEventListener(new EventListener(this, commandProcessor))
                    .buildBlocking();
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            logWarning(e.getMessage(), e.getStackTrace());
            return;
        }
        
        updateRunningState(RunningState.INIT);
        YoutubeAPI.setAPIKey(youtubeApiKey);
        gac = new GlobalAudioController(this);

        for (Guild guild : jda.getGuilds()) {
            gac.createServerAudioController(guild);
        }
        
        updateRunningState(RunningState.READY);
    }
    
    /**
     * Stop all {@link ServerAudioController} instances, shut down {@link JDA},
     * and stop the PandaBot constructor loop so that a new PandaBot instance
     * can be spawned by {@link Main}.
     */
    public void reload() {
        logInfo("Stopping...");
        updateRunningState(RunningState.STOPPING);
        st.dropAll();
        getGlobalAudioController().killAll();
        jda.shutdown();
        running = false;
    }
    
    /**
     * Updates the "Playing" field depending on the bot's running state.
     */
    private void updateRunningState(RunningState runningState) {
        if (jda != null)
            jda.getPresence().setGame(Game.of(runningState.getStatusMessage()));
    }
    
    /**
     * Log messages to the console at INFO level
     */
    public void logInfo(String... strArr) {
        for (String str : strArr)
            log.info(str);
    }
    
    /**
     * Log messages to the console at WARNING level
     */
    public void logWarning(String... strArr) {
        for (String str : strArr)
            log.warning(str);
    }
    
    /**
     * Log a stack trace to the console at WARNING level
     */
    public void logWarning(String message, StackTraceElement[] steArr) {
        log.warning(message);
        
        for (StackTraceElement ste : steArr)
            log.warning(ste.toString());
    }
    
    /**
     * Check if a user has permission for a command. Currently only for
     * superuser commands.
     */
    public boolean userHasPermission(Member member, String perm) {
        if (perm.equalsIgnoreCase("super")) {
            return member.getUser().getId().equalsIgnoreCase(superuserId);
        }
        
        return true;
    }
    
    /**
     * Send a message to a channel using a built {@link Message}.
     * @return The final {@link Message} instance.
     */
    public Message sendMessage(MessageChannel msgChannel, Message msg) {
        return msgChannel.sendMessage(msg).complete();
    }
    
    /**
     * Send a message to a channel using an unformatted String.
     * @return The final {@link Message} instance.
     */
    public Message sendMessage(MessageChannel msgChannel, String str) {
        MessageBuilder mb = new MessageBuilder();
        mb.append(str);
        return sendMessage(msgChannel, mb.build());
    }
    
    /**
     * Send a direct message to a {@link Member}.
     */
    public void sendDirectMessage(Member member, Message msg) {
        member.getUser().openPrivateChannel().complete().sendMessage(msg).complete();
    }
    
    /**
     * Attempt to join a {@link VoiceChannel}.
     */
    public void joinVoiceChannel(Guild guild, VoiceChannel vc) {
        if (guild.getAudioManager().getConnectedChannel() == vc)
            return;
        if (guild.getAudioManager().isAttemptingToConnect())
            return;
        guild.getAudioManager().openAudioConnection(vc);
    }
    
    /**
     * Attempt to leave a {@link VoiceChannel}.
     */
    public void leaveVoiceChannel(Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
        }
    }
    
    /**
     * Get PandaBot's own user ID
     */
    public String getBotId() {
        return jda.getSelfUser().getId();
    }
    
    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }
    
    public GlobalAudioController getGlobalAudioController() {
        return gac;
    }
    
    public SelectionTracker getSelectionTracker() {
        return st;
    }
}
