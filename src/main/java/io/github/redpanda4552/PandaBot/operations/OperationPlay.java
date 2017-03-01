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
package io.github.redpanda4552.PandaBot.operations;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import fredboat.audio.GuildPlayer;
import fredboat.audio.VideoSelection;
import fredboat.util.YoutubeAPI;
import fredboat.util.YoutubeVideo;
import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * This class is a modified version of a class from Frederikam's FredBoat bot.
 * <a href="https://github.com/Frederikam/FredBoat">GitHub Link</a>. 
 */
public class OperationPlay extends AbstractOperation {

    private User sender;
    private MessageChannel messageChannel;
    private Message message;
    private String url;
    
    public OperationPlay(PandaBot pandaBot, User sender, MessageChannel messageChannel, Message message, String url) {
        super(pandaBot);
        this.sender = sender;
        this.messageChannel = messageChannel;
        this.message = message;
        this.url = url;
    }
    
    @Override
    public void execute() {
        if (!message.getAttachments().isEmpty()) {
            GuildPlayer player = pandaBot.getGuildPlayer();
            player.setCurrentMessageChannel(messageChannel);
            
            for (Attachment atc : message.getAttachments()) {
                player.queue(atc.getUrl(), messageChannel, sender);
            }
            
            player.setPause(false);
            
            return;
        }

        if (url == null || url.isEmpty()) {
            handleNoArguments(PandaBot.getGuild(), messageChannel, message);
            return;
        }

        // What if we want to select a selection instead?
        // Original code had a length check on args. We aren't using args, but
        // since we null/empty check just above, it's unnecessary.
        if (StringUtils.isNumeric(url)) {
            select(PandaBot.getGuild(), messageChannel, sender, message, url);
            return;
        }

        //Search youtube for videos and let the user select a video
        if (!url.startsWith("http")) {
            try {
                searchForVideos(PandaBot.getGuild(), messageChannel, sender, message, url);
            } catch (RateLimitedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(messageChannel);

        player.queue(url, messageChannel, sender);
        player.setPause(false);

        try {
            message.deleteMessage().queue();
        } catch (Exception ignored) {

        }
        
        complete();
    }

    private void handleNoArguments(Guild guild, MessageChannel channel, Message message) {
        GuildPlayer player = pandaBot.getGuildPlayer();
        if (player.isQueueEmpty()) {
            channel.sendMessage("The player is not currently playing anything. Use the following syntax to add a song:\n/play <url-or-search-terms>").queue();
        } else if (player.isPlaying()) {
            channel.sendMessage("The player is already playing.").queue();
        } else if (player.getUsersInVC().isEmpty()) {
            channel.sendMessage("There are no users in the voice chat.").queue();
        } else {
            player.play();
            channel.sendMessage("The player will now play.").queue();
        }
    }
    
    private void select(Guild guild, MessageChannel channel, User invoker, Message message, String url) {
        GuildPlayer player = pandaBot.getGuildPlayer();
        player.setCurrentMessageChannel(channel);
        if (player.selections.containsKey(invoker.getId())) {
            VideoSelection selection = player.selections.get(invoker.getId());
            try {
                int i = Integer.valueOf(url);
                if (selection.getChoices().size() < i || i < 1) {
                    throw new NumberFormatException();
                } else {
                    YoutubeVideo selected = selection.choices.get(i - 1);
                    player.selections.remove(invoker.getId());
                    String msg = "Song **#" + i + "** has been selected: **" + selected.getName() + "** (" + selected.getDurationFormatted() + ")";
                    selection.getOutMsg().editMessage(msg).queue();
                    player.queue("https://www.youtube.com/watch?v=" + selected.getId(), channel, invoker);
                    player.setPause(false);
                    try {
                        message.deleteMessage().queue();
                    } catch (PermissionException ignored) {
                        
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                channel.sendMessage("Must be a number 1-" + selection.getChoices().size() + ".").queue();
            }
        } else {
            channel.sendMessage("You must first be given a selection to choose from.").queue();
        }
    }
    
    private void searchForVideos(Guild guild, MessageChannel channel, User invoker, Message message, String url) throws RateLimitedException {
        Matcher m = Pattern.compile("\\S+\\s+(.*)").matcher(message.getRawContent());
        m.find();
        String query = m.group(1);
        
        //Now remove all punctuation
        query = query.replaceAll("[.,/#!$%\\^&*;:{}=\\-_`~()]", "");
        Message outMsg = channel.sendMessage("Searching YouTube for `{q}`...".replace("{q}", query)).block();
        ArrayList<YoutubeVideo> vids = null;
        
        try {
            vids = YoutubeAPI.searchForVideos(query);
        } catch (JSONException e) {
            channel.sendMessage("An error occurred when searching YouTube. Consider linking directly to audio sources instead.\n```\n/play <url>```").queue();
            return;
        }

        if (vids.isEmpty()) {
            outMsg.editMessage("No results for `{q}`".replace("{q}", query)).queue();
        } else {
            //Clean up any last search by this user
            GuildPlayer player = pandaBot.getGuildPlayer();

            VideoSelection oldSelection = player.selections.get(invoker.getId());
            if(oldSelection != null) {
                oldSelection.getOutMsg().deleteMessage().queue();
            }

            MessageBuilder builder = new MessageBuilder();
            builder.append("**Please select a video with the `/play n` command:**");

            int i = 1;
            for (YoutubeVideo vid : vids) {
                builder.append("\n**")
                        .append(String.valueOf(i))
                        .append(":** ")
                        .append(vid.getName())
                        .append(" (")
                        .append(vid.getDurationFormatted())
                        .append(")");
                i++;
            }
            
            Message newMsg = builder.build();
            String raw = newMsg.getRawContent();
            RestAction<Message> ram = outMsg.editMessage(raw);
            
            try {
                ram.queue();
            } catch (PermissionException e) {
                pandaBot.addToHistory("Insufficient permission to edit message in OperationPlay", Level.WARNING);
            }
            
            player.setCurrentMessageChannel(channel);
            player.selections.put(invoker.getId(), new VideoSelection(vids, outMsg));
        }
    }
}
