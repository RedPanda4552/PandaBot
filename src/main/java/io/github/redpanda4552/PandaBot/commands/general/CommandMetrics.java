/**
 * This file is part of PandaBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.PandaBot.commands.general;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

public class CommandMetrics extends AbstractCommand {

    public CommandMetrics(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        String version = "Error reading pom.xml";
        
        try {
            if (new File("pom.xml").exists()) {
                version = new MavenXpp3Reader().read(new FileReader("pom.xml")).getVersion();
            } else {
                version = new MavenXpp3Reader().read(new InputStreamReader(CommandMetrics.class.getResourceAsStream("/META-INF/maven/io.github.redpanda4552/PandaBot/pom.xml"))).getVersion();
            }
        } catch (IOException | XmlPullParserException e) {
            LogBuffer.sysWarn(e);
        }
        
        MessageBuilder mb = new MessageBuilder();
        EmbedBuilder eb = new EmbedBuilder()
          .setTitle("PandaBot Metrics")
          .addField("Version", version, true)
          .addField("Uptime", DurationFormatUtils.formatDuration(pandaBot.getRunningTime(), "d:HH:mm:ss"), true)
          .addField("Servers", String.valueOf(pandaBot.getServerCount()), true)
          .addField("Users", String.valueOf(pandaBot.getUserCount()), true)
          .addField("Message Channels", String.valueOf(pandaBot.getTextChannelCount()), true)
          .addField("Command Executions/Attempts", String.format("%d/%d", pandaBot.getCommandProcessor().getCommandExecutions(), pandaBot.getCommandProcessor().getCommandAttempts()), true)
          .setColor(new Color(20, 210, 45));
        mb.setEmbed(eb.build());
        pandaBot.sendMessage(msgChannel, mb.build());
    }

    @Override
    public String getHelpArgs() {
        return "";
    }

    @Override
    public String getHelpMessage() {
        return "View various metrics for the bot.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
