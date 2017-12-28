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
package io.github.redpanda4552.PandaBot.commands.music;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.player.SelectionTracker;
import io.github.redpanda4552.PandaBot.sql.Query;
import io.github.redpanda4552.PandaBot.sql.TableIndex;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandLoad extends AbstractCommand {

    private SelectionTracker st;
    
    public CommandLoad(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
        st = pandaBot.getSelectionTracker();
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        Query query = new Query();
        query.from(TableIndex.MUSIC);
        query.select("name", "url");
        StringBuilder whereBuilder = new StringBuilder("userId = '");
        whereBuilder.append(member.getUser().getId())
                    .append("'");
        
        if (args.length > 0) {
            whereBuilder.append(" AND name = '")
                        .append(args[0])
                        .append("'");
        }
        
        query.where(whereBuilder.toString());
        ResultSet resultSet = pandaBot.getSQL().processQuery(query);
        HashMap<String, String> resultMap = new HashMap<String, String>();
        
        try {
            while (resultSet.next()) {
                resultMap.put(resultSet.getString("name"), resultSet.getString("url"));
            }
        } catch (SQLException e) {
            pandaBot.logWarning(e.getMessage(), e.getStackTrace());
        }
        
        if (resultMap.isEmpty()) {
            MessageBuilder mb = new MessageBuilder();
            
            if (args.length == 0) {
                mb.append("No saved tracks.");
            } else {
                mb.append("Could not find saved track with name **")
                  .append(args[0])
                  .append("**.");
            }
            
            pandaBot.sendMessage(msgChannel, mb.build());
            return;
        }
        
        if (args.length == 0) {
            StringBuilder listBuilder = new StringBuilder("Saved tracks - Use **!load <name>** to load a track.\n");
            
            for (String track : resultMap.keySet()) {
                listBuilder.append("\t")
                           .append(track)
                           .append(" - <")
                           .append(resultMap.get(track))
                           .append(">\n");
            }
            
            Message msg = pandaBot.sendMessage(msgChannel, listBuilder.toString());
            st.upsertLL(member.getUser(), msg, resultMap);
        } else {
            st.remove(member.getUser());
            
            VoiceChannel vc = member.getVoiceState().getChannel();
            
            // If member is not in a voice channel
            if (vc == null) {
                MessageBuilder mb = new MessageBuilder();
                mb.append(member.getAsMention());
                mb.append(" You are not in a voice channel, so I have nowhere to join to!");
                pandaBot.sendMessage(msgChannel, mb.build());
                return;
            }
            
            pandaBot.joinVoiceChannel(guild, vc);
            pandaBot.getGlobalAudioController().play(guild, msgChannel, member, resultMap.get(args[0]));
        }
    }

    @Override
    public String getHelpArgs() {
        return "[<name>]";
    }

    @Override
    public String getHelpMessage() {
        return "Load a saved track. No arguments will list your saved tracks.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
