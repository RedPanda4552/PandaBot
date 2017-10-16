package io.github.redpanda4552.PandaBot.player;

import java.util.HashMap;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class SelectionTracker {

    private class SelectionRow {
        private Message message;
        private AudioPlaylist audioPlaylist;
        
        protected SelectionRow(Message message, AudioPlaylist audioPlaylist) {
            this.message = message;
            this.audioPlaylist = audioPlaylist;
        }
        
        protected Message getMessage() {
            return message;
        }
        
        protected AudioPlaylist getAudioPlaylist() {
            return audioPlaylist;
        }
    }
    
    private HashMap<User, SelectionRow> userMap;
    
    public SelectionTracker() {
        userMap = new HashMap<User, SelectionRow>();
    }
    
    public void upsert(User usr, Message msg, AudioPlaylist ap) {
        if (contains(usr)) {
            getMessageOf(usr).delete().complete();
        }
        
        userMap.put(usr, new SelectionRow(msg, ap));
    }
    
    public void remove(User usr) {
        if (!contains(usr)) {
            return;
        }
        
        getMessageOf(usr).delete().complete();
        userMap.remove(usr);
    }
    
    public boolean contains(User usr) {
        return userMap.containsKey(usr);
    }
    
    public Message getMessageOf(User usr) {
        if (!contains(usr)) {
            return null;
        }
        
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
    
    public void dropAll() {
        for (User usr : userMap.keySet()) {
            remove(usr);
        }
    }
}
