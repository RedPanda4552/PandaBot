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
package io.github.redpanda4552.PandaBot.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;

import io.github.redpanda4552.PandaBot.LogBuffer;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.util.ReportTask;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class ServerReportManager {

    private final String OUTPUT_CHANNEL_NAME = "pandabot-reporting";
    private final String REPORT_FILE_PATH = "./reports/";
    
    private PandaBot pandaBot;
    private Timer timer;
    private File[] reportFiles;
    private MessageChannel outputChannel;
    
    public ServerReportManager(Guild server) {
        pandaBot = PandaBot.getSelf();
        this.timer = new Timer();
        List<TextChannel> channels = pandaBot.getJDA().getTextChannelsByName(OUTPUT_CHANNEL_NAME, false);

        if (channels.size() > 0) {
            outputChannel = channels.get(0);
        } else {
            outputChannel = (MessageChannel) server.getController().createTextChannel(OUTPUT_CHANNEL_NAME).complete();
        }
        
        File dir = new File(REPORT_FILE_PATH);
        
        if (!dir.exists() && !dir.mkdir()) {
            LogBuffer.sysWarn("Could not create reports directory!");
            return;
        }
        
        File template = new File(REPORT_FILE_PATH + "template.report");
        
        if (!template.exists()) {
            try {
                template.createNewFile();
                InputStream is = getClass().getResourceAsStream("/template.report");
                Files.copy(is, template.getAbsoluteFile().toPath());
            } catch (IOException e) {
                LogBuffer.sysWarn(e);
            }
        }
        
        reportFiles = dir.listFiles();
        
        for (File reportFile : reportFiles) {
            if (!reportFile.getName().equals("template.report")) {
                // Daily reports are hard coded to run every 24 hours, and fetch 24 hours.
                timer.schedule(new ReportTask(reportFile, outputChannel, 24), OffsetDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000 - OffsetDateTime.now().toEpochSecond() * 1000, 24 * 60 * 60 * 1000);
            }
        }
    }
    
    public void runNow(File reportFile, MessageChannel outputChannel, int hours) {
        pandaBot.sendMessage(outputChannel, "Running a report... Depending on how many messages there are and how long you specified, this may take a while...");
        timer.schedule(new ReportTask(reportFile, outputChannel, hours), 10);
    }
    
    public File[] getReportFiles() {
        return reportFiles;
    }
}
