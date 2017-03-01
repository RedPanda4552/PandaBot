package io.github.redpanda4552.PandaBot.operations;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fredboat.audio.GuildPlayer;
import fredboat.audio.queue.AudioTrackContext;
import io.github.redpanda4552.PandaBot.PandaBot;

public class OperationSeek extends AbstractOperation {

    private long time;
    
    public OperationSeek(PandaBot pandaBot, long time) {
        super(pandaBot);
        this.time = time;
    }

    @Override
    public void execute() {
        GuildPlayer player = pandaBot.getGuildPlayer();
        AudioTrackContext atc = player.getPlayingTrack();
        AudioTrack at = atc.getTrack();
        at.setPosition(time);
        complete();
    }

}
