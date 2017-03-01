package io.github.redpanda4552.PandaBot.operations;

import io.github.redpanda4552.PandaBot.PandaBot;

public class OperationReload extends AbstractOperation {

    public OperationReload(PandaBot pandaBot) {
        super(pandaBot);
    }

    @Override
    public void execute() {
        PandaBot.enabled = false;
        complete();
    }

}
