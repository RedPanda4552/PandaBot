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
package io.github.redpanda4552.PandaBot.player;

import java.util.HashMap;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class SelectionTracker {

    private class SelectionRow {
        private Message message;
        private AudioPlaylist audioPlaylist;
        private HashMap<String, String> loadList;
        
        protected SelectionRow(Message message, AudioPlaylist audioPlaylist) {
            this.message = message;
            this.audioPlaylist = audioPlaylist;
        }
        
        protected SelectionRow(Message message, HashMap<String, String> loadList) {
            this.message = message;
            this.loadList = loadList;
        }
        
        protected Message getMessage() {
            return message;
        }
        
        protected AudioPlaylist getAudioPlaylist() {
            return audioPlaylist;
        }
        
        protected HashMap<String, String> getLoadList() {
            return loadList;
        }
    }
    
    private HashMap<User, SelectionRow> userMap;
    
    public SelectionTracker() {
        userMap = new HashMap<User, SelectionRow>();
    }
    
    public void upsertAP(User usr, Message msg, AudioPlaylist ap) {
        if (contains(usr))
            getMessageOf(usr).delete().complete();
        userMap.put(usr, new SelectionRow(msg, ap));
    }
    
    public void upsertLL(User usr, Message msg, HashMap<String, String> loadList) {
        if (contains(usr))
            getMessageOf(usr).delete().complete();
        userMap.put(usr, new SelectionRow(msg, loadList));
    }
    
    public void remove(User usr) {
        if (!contains(usr))
            return;
        getMessageOf(usr).delete().complete();
        userMap.remove(usr);
    }
    
    public boolean contains(User usr) {
        return userMap.containsKey(usr);
    }
    
    public Message getMessageOf(User usr) {
        if (!contains(usr))
            return null;
        return userMap.get(usr).getMessage();
    }
    
    /**
     * Audio playlist from this user's most recent query, or null if none stored
     */
    public AudioPlaylist getAudioPlaylistOf(User usr) {
        if (!contains(usr)) {
            return null;
        }
        
        return userMap.get(usr).getAudioPlaylist();
    }
    
    public HashMap<String, String> getLoadListOf(User usr) {
        if (!contains(usr))
            return null;
        return userMap.get(usr).getLoadList();
    }
    
    public void dropAll() {
        for (User usr : userMap.keySet())
            remove(usr);
    }
}
