package io.github.redpanda4552.PandaBot;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import io.github.redpanda4552.PandaBot.util.LogFormatter;

public class Main {

    private static final Logger log = Logger.getLogger(PandaBot.class.getName());
    
    public static void main(String[] args) {
        log.setUseParentHandlers(false);
        LogFormatter logFormatter = new LogFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(logFormatter);
        log.addHandler(consoleHandler);
        
        if (args.length != 3) {
            log.info("Usage: java -jar PandaBot-x.y.z.jar <discord-bot-token> <youtube-api-key> <discord-user-id>");
            log.info("More information available at https://github.com/redpanda4552/PandaBot/");
            return;
        }
        
        while (true)
            new PandaBot(log, args[0], args[1], args[2]);
    }
}
