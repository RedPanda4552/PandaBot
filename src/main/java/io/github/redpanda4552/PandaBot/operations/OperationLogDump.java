package io.github.redpanda4552.PandaBot.operations;

import java.util.ArrayList;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class OperationLogDump extends AbstractOperation {

    private User sender;
    private MessageChannel channel;
    private int limiter;
    
    public OperationLogDump(PandaBot pandaBot, User sender, MessageChannel channel, int limiter) {
        super(pandaBot);
        this.sender = sender;
        this.channel = channel;
        this.limiter = limiter;
    }

    @Override
    public void execute() {
        if (channel == null) {
            if (sender == null) {
                return;
            }
        }
        
        // Making sure we are using a local copy and not a reference.
        ArrayList<String> logHistory = new ArrayList<String>();
        logHistory.addAll(pandaBot.getHistory());
        int logHistorySize = logHistory.size();
        
        // If we have a limiter, we need to start by purging
        if (logHistory.size() > limiter && limiter != -1) {
            for (int i = 1; i < logHistorySize - limiter; i++) {
                logHistory.remove(0);
            }
        }
        
        int pageCount = 1, logCount = logHistory.size();
        StringBuilder sb = new StringBuilder("```PandaBot Log Dump Page " + pageCount + " (top is old, bottom new)\n");
        
        for (String str : logHistory) {
            logCount--;
            // We are purposely undershooting Discord's 2000 char limit to
            // accommodate for newlines. And also ridiculously long walls of text
            if (sb.length() + str.length() < 1500 && logCount > 0) {
                sb.append(str);
                sb.append("\n");
            } else {
                sb.append("```");
                
                if (sender != null) {
                    pandaBot.queuePrivateMessage(sender, sb.toString());
                } else {
                    pandaBot.queueSendMessage(sb.toString(), channel);
                }
                
                pageCount++;
                sb = new StringBuilder("```PandaBot Log Dump Page " + pageCount + " (top is old, bottom new)\n");
            }
        }
        
        complete();
    }
}
