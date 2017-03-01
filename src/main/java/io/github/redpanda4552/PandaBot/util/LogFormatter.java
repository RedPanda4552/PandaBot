package io.github.redpanda4552.PandaBot.util;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append("PandaBot").append("//").append(record.getLevel()).append("] ");
        builder.append(record.getMessage());
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
