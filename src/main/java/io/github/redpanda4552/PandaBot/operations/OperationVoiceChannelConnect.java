package io.github.redpanda4552.PandaBot.operations;

import io.github.redpanda4552.PandaBot.PandaBot;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class OperationVoiceChannelConnect extends AbstractOperation {

    private VoiceChannel voiceChannel;
    
    public OperationVoiceChannelConnect(PandaBot pandaBot, VoiceChannel voiceChannel) {
        super(pandaBot);
        this.voiceChannel = voiceChannel;
    }

    @Override
    public void execute() {
        AudioManager am = pandaBot.getAudioManager();
        am.openAudioConnection(voiceChannel);
        complete();
    }
}
