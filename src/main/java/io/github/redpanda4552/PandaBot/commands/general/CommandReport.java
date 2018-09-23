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

import java.io.File;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.reporting.ServerReportManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandReport extends AbstractCommand {

    public CommandReport(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        ServerReportManager srm = pandaBot.getGlobalReportManager().getServerReportManager(guild);
        File[] reportFiles = srm.getReportFiles();
        
        if (args.length == 2) {
            for (File reportFile : reportFiles) {
                if (reportFile.getName().equalsIgnoreCase(args[0])) {
                    try {
                        int i = Integer.parseInt(args[1]);
                        srm.runNow(reportFile, msgChannel, i);
                    } catch (NumberFormatException e) {
                        pandaBot.sendMessage(msgChannel, "Hour argument was not an integer, aborting");
                    }
                }
            }
        } else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor("Report Usage:");
            eb.setDescription("!report <report name> <hours>\n\n");
            eb.appendDescription("<report name> - The full name of the report as listed below.\n");
            eb.appendDescription("<hours> - Number of hours back to look at messages.\n");
            
            for (File reportFile : reportFiles)
                if (!reportFile.getName().equals("template.report")) eb.addField(reportFile.getName(), "", true);
            
            pandaBot.sendMessage(msgChannel, new MessageBuilder().setEmbed(eb.build()).build());
        } 
    }

    @Override
    public String getHelpArgs() {
        return "[report-name] [hours]";
    }

    @Override
    public String getHelpMessage() {
        return "Immediately execute a report. Hours specifies how far in time to go. Use no arguments to list available reports.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
