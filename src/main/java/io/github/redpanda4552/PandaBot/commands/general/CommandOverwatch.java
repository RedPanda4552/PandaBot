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

import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.util.OverwatchEmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandOverwatch extends AbstractCommand {
    
    private final String SWITCH_PREFIX = "-";

    public CommandOverwatch(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        // https://ow-api.com/v1/stats/xbl/us/pandubz/heroes/moira
        // https://ow-api.com/v1/stats/xbl/us/pandubz/complete
        
        if (args.length < 2) {
            MessageBuilder mb = new MessageBuilder();
            mb.append(member.getAsMention())
              .append(" You must at least specify the platform and username you are trying to look up!");
            pandaBot.sendMessage(msgChannel, mb.build());
            return;
        }
        
        String platform = args[0];
        String username = args[1];
        String hero = null;
        String mode = "comp";
        String region = "us";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(SWITCH_PREFIX) && args.length > (i + 1)) {
                switch (args[i].substring(1)) {
                case "h":
                    hero = args[i + 1];
                    break;
                case "m":
                    mode = args[i + 1];
                    break;
                case "r":
                    region = args[i + 1];
                    break;
                }
                
                i++;
            }
        }
       
        StringBuilder calloutURLBuilder = new StringBuilder("https://ow-api.com/v1/stats/");
        calloutURLBuilder.append(platform).append("/")
                         .append(region).append("/")
                         .append(username).append("/");
        
        if (hero == null || hero.isEmpty()) {
            calloutURLBuilder.append("complete");
        } else {
            calloutURLBuilder.append("heroes/")
                             .append(hero);
        }
        
        JSONObject data = null;
        
        try {
            data = Unirest.get(calloutURLBuilder.toString())
                          .asJson()
                          .getBody()
                          .getObject();
        } catch (UnirestException e) {
            pandaBot.sendMessage(msgChannel, "Something went wrong trying to fetch Overwatch info. Try again in a moment.");
            pandaBot.logWarning(e.getMessage(), e.getStackTrace());
            return;
        }
        
        if (data.length() == 1 && data.has("error")) {
            pandaBot.sendMessage(msgChannel, data.getString("error"));
        } else {
            MessageEmbed embed = OverwatchEmbedBuilder.build(data, platform, hero, mode);
            MessageBuilder mb = new MessageBuilder();
            mb.setEmbed(embed);
            pandaBot.sendMessage(msgChannel, mb.build());
        }
    }

    @Override
    public String getHelpArgs() {
        return "<pc|xbl|psn> <username> [-h <heroname>] [-m <comp|qp>] [-r <us|eu|asia>]";
    }

    @Override
    public String getHelpMessage() {
        return "Look up Overwatch player statistics. Defaults to all heroes, competitive.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.GENERAL;
    }

}
