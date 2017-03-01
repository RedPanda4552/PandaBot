package io.github.redpanda4552.PandaBot.operations;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.redpanda4552.PandaBot.Main;
import io.github.redpanda4552.PandaBot.PandaBot;

public abstract class AbstractOperation {

    protected final Logger log = Main.log;
    
    protected PandaBot pandaBot;
    
    public AbstractOperation(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
    }
    
    /**
     * Execute this AbstractOperation's payload.<br>
     * Implementations of this method should call {@link #complete()} at the end
     * of a successful execution.
     * <b>This method is not defined by the AbstractOperation supertype and must
     *  be defined in each subtype.</b>
     */
    public abstract void execute();
    
    public void complete() {
        pandaBot.addToHistory("Executed " + this.getClass().getSimpleName(), Level.INFO);
    }
    
    public void completeWithError(String str) {
        pandaBot.addToHistory("Executed " + this.getClass().getSimpleName() + " but ran into problem: " + str, Level.WARNING);
    }
}
