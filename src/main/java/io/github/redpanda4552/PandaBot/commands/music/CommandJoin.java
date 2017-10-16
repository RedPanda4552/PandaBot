package io.github.redpanda4552.PandaBot.commands.music;

import io.github.redpanda4552.PandaBot.CommandProcessor;
import io.github.redpanda4552.PandaBot.PandaBot;
import io.github.redpanda4552.PandaBot.commands.AbstractCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class CommandJoin extends AbstractCommand {

    public CommandJoin(PandaBot pandaBot, CommandProcessor commandProcessor) {
        super(pandaBot, commandProcessor);
    }

    @Override
    public void execute(Guild guild, MessageChannel msgChannel, Member member, String[] args) {
        VoiceChannel vc = member.getVoiceState().getChannel(); 
        
        if (vc == null) {
            MessageBuilder mb = new MessageBuilder();
            mb.append(member.getAsMention());
            mb.append(" You are not in a voice channel, so I have nowhere to join to!");
            pandaBot.sendMessage(msgChannel, mb.build());
            return;
        }
        
        pandaBot.joinVoiceChannel(guild, vc);
    }

    @Override
    public String getHelpArgs() {
        return "";
    }

    @Override
    public String getHelpMessage() {
        return "Make PandaBot join your current voice channel.";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.MUSIC;
    }

}
