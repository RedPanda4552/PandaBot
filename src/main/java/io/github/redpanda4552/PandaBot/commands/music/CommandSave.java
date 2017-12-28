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

import java.net.MalformedURLException;
import java.net.URL;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import io.github.redpanda4552.PandaBot.sql.Insert;
import io.github.redpanda4552.PandaBot.sql.TableIndex;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandSave extends AbstractCommand {

    public CommandSave(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        if (args.length == 2) {
            try {
                new URL(args[1]);
            } catch (MalformedURLException e) {
                pandaBot.sendMessage(msgChannel, "Save failed; bad URL.");
                return;
            }
            
            Insert insert = new Insert();
            insert.into(TableIndex.MUSIC);
            insert.values(member.getUser().getId(), args[0], args[1]);
            pandaBot.getSQL().processInsert(insert);
        } else {
            MessageBuilder mb = new MessageBuilder();
            mb.append("Save failed; too ")
              .append(args.length > 2 ? "many" : "few")
              .append(" arguments.");
            pandaBot.sendMessage(msgChannel, mb.build());
        }
    }

    @Override
    public String getHelpArgs() {
        return "<name> <url>";
    }

    @Override
    public String getHelpMessage() {
        return "Save a track for quick access later. Warning! Names must NOT contain spaces.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
