package io.github.redpanda4552.PandaBot;

import java.util.LinkedList;

import io.github.redpanda4552.PandaBot.operations.AbstractOperation;

/**
 * Handles all {@link AbstractOperation AbstractOperation}s that are waiting to
 * be processed.
 */
public class OperationsQueue {

    private LinkedList<AbstractOperation> queue;
    
    public OperationsQueue() {
        queue = new LinkedList<AbstractOperation>();
    }
    
    /**
     * Add an {@link AbstractOperation AbstracctOperation} to the queue.
     * @param operation - The {@link AbstractOperation AbstractOperation} to add
     */
    public void add(AbstractOperation operation) {
        queue.add(operation);
    }
    
    /**
     * Execute the frontmost {@link AbstractOperation AbstractOperation}.
     */
    public void executeNext() {
        if (queue.isEmpty()) {
            return;
        }
        
        queue.removeFirst().execute();
    }
}
