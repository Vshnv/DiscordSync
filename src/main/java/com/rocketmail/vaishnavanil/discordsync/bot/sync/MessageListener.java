package com.rocketmail.vaishnavanil.discordsync.bot.sync;

import com.rocketmail.vaishnavanil.discordsync.bot.SyncBot;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class MessageListener implements MessageCreateListener {
    SyncBot sync;
    List<Channel> channels;
    public MessageListener(SyncBot sb){
        sync = sb;
    }
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(!sync.listensTo(event.getChannel())){
            return;
        }
        if(event.getMessageAuthor().isBotUser())return;
        sync.syncMessage(event.getMessage().getAuthor().getAvatar(),event.getMessageAuthor().getDisplayName(),event.getMessageContent(),event.getChannel());
    }
}
