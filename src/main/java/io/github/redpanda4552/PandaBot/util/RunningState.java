package io.github.redpanda4552.PandaBot.util;

import io.github.redpanda4552.PandaBot.CommandProcessor;

public enum RunningState {

    READY("Online // " + CommandProcessor.PREFIX + "help"),
    INIT("Starting..."),
    STOPPING("Shutting down");
    
    private String statusMessage;
    
    private RunningState(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
}
