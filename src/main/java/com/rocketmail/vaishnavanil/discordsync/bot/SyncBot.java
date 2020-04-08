package com.rocketmail.vaishnavanil.discordsync.bot;

import com.rocketmail.vaishnavanil.discordsync.bot.sync.MessageListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SyncBot {
    DiscordApi api;
    List<Channel> channels = new ArrayList<>();
    public SyncBot(DiscordApi api,long[] ch){
        this.api = api;
        assert ch.length >0;
        for(long l:ch){
            Optional<Channel> c =api.getChannelById(l);
            if(!c.isPresent())continue;
            channels.add(c.get());
        }

        List<Server> nkd = new ArrayList<>();
        for(Channel c:channels){
            Optional<ServerChannel> s = c.asServerChannel();
            if(!s.isPresent())continue;
            ServerChannel sc = s.get();
            if (!nkd.contains(c.asServerChannel().get().getServer())){
                nkd.add(sc.getServer());
                api.getYourself().updateNickname(sc.getServer()," ឵឵");
            }
        }
    }


    public void begin(){
        System.out.println("Sync ready!");
        api.addMessageCreateListener(new MessageListener(this));
    }

    public boolean listensTo(Channel channel){
        return channels.contains(channel);
    }
    public void syncMessage(Icon icon, String author, String message, Channel ch){
        for(Channel c:channels){
            if(c.getId()==ch.getId())continue;
            Optional<TextChannel> tc = c.asTextChannel();
            if(!tc.isPresent())continue;
            TextChannel tcc = tc.get();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor(author,"",icon);
            eb.setDescription(message);
            tcc.sendMessage(eb);
        }
    }
}
