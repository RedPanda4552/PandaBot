package io.github.redpanda4552.PandaBot.util;

public enum BotState {

    STARTING("Starting..."),
    RUNNING("Online - /help"),
    STOPPING("Stopping...");
    
    private BotState(String str) {
        this.str = str;
    }
    
    private String str;
    
    @Override
    public String toString() {
        return str;
    }
}
