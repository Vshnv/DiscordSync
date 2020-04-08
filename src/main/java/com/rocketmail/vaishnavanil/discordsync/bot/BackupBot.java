package com.rocketmail.vaishnavanil.discordsync.bot;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerBuilder;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.RichInvite;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class BackupBot {
    DiscordApi api;
    Server s;
    public BackupBot(DiscordApi api,long serverToBackup){
        this.api = api;
        s = api.getServerById(serverToBackup).isPresent() ? api.getServerById(serverToBackup).get() : null;
        if(s == null)throw new IllegalArgumentException("Invalid Server ID or not Invitied, Invite using " + api.createBotInvite(Permissions.fromBitmask(8)));
    }


    public void begin() throws ExecutionException, InterruptedException {
        HashMap<Long,Long> TchannelMap = new HashMap<>();
        System.out.println("Creating server...");
        ServerBuilder sb = api.createServerBuilder();
        System.out.println("Setting permissions...");
        sb.setAfkTimeoutInSeconds(s.getAfkTimeoutInSeconds());
        sb.setDefaultMessageNotificationLevel(s.getDefaultMessageNotificationLevel());
        sb.setExplicitContentFilterLevel(s.getExplicitContentFilterLevel());
        if(s.getIcon().isPresent())sb.setIcon(s.getIcon().get());
        sb.setName(s.getName() + " BACKUP");
        sb.setRegion(s.getRegion());
        sb.setVerificationLevel(s.getVerificationLevel());
        Server sbs;
        AtomicLong l = new AtomicLong();
        CompletableFuture<Long> longCompletableFuture = sb.create().whenCompleteAsync((aLong, throwable) ->
        {
            l.set(aLong);
        });
        Thread.sleep(10000);
        sbs = api.getServerById(l.get()).get();
        if(sbs == null){
            System.out.println("Could not create new server! try again later!");
            System.exit(0);
            return;
        }
        System.out.println("Server permissions ready...");
        System.out.println("Creating roles...");
        List<Role> roles = s.getRoles();
        for(int i = 0; i<roles.size();i++){
            Role r = roles.get(i);
            System.out.println("    Role: " + r.getName());
            RoleBuilder rb = sbs.createRoleBuilder();
            rb.setAuditLogReason("Backup");
            if(r.getColor().isPresent())rb.setColor(r.getColor().get());
            rb.setDisplaySeparately(r.isDisplayedSeparately());
            rb.setMentionable(r.isMentionable());
            rb.setName(r.getName());
            rb.setPermissions(r.getPermissions());
            rb.create().get();
        }
        System.out.println("Server roles ready...");
        System.out.println("Creating categories...");
        List<ChannelCategory> cc = s.getChannelCategories();
        for(int i = 0; i<cc.size();i++){
            ChannelCategory c = cc.get(i);
            System.out.println("    Category: " + c.getName());
            ChannelCategoryBuilder cb = sbs.createChannelCategoryBuilder();
            cb.setAuditLogReason("Backup");
            Map<Permissionable,Permissions> perm = c.getOverwrittenPermissions();
            if(!perm.isEmpty()) {
                for (Permissionable p : perm.keySet()) {
                    cb.addPermissionOverwrite((Permissionable & DiscordEntity) p, perm.get(p));
                }
            }
            cb.setName(c.getName());
            cb.create().get();
        }

        System.out.println("Server categories ready...");
        System.out.println("Creating channels...");
        List<ServerChannel> cs = s.getChannels();
        for(int i = 0; i<cs.size();i++){
            ServerChannel c = cs.get(i);
            if(c.asChannelCategory().isPresent())continue;
            System.out.println("    Channel: " + c.getName());

            if(c.asServerTextChannel().isPresent()){
                ServerTextChannelBuilder stb = sbs.createTextChannelBuilder();
                stb.setAuditLogReason("Backup");
                Map<Permissionable,Permissions> perm = c.getOverwrittenPermissions();
                if(!perm.isEmpty()) {
                    for (Permissionable p : perm.keySet()) {
                        stb.addPermissionOverwrite((Permissionable & DiscordEntity) p, perm.get(p));
                    }
                }
                if(c.asCategorizable().isPresent()){
                    Optional<ChannelCategory> cat = c.asCategorizable().get().getCategory();
                    if(cat.isPresent()){
                        stb.setCategory(sbs.getChannelCategoriesByName(cat.get().getName()).get(0));
                    }
                }
                stb.setName(c.getName());
                stb.setSlowmodeDelayInSeconds(c.asServerTextChannel().get().getSlowmodeDelayInSeconds());
                stb.setTopic(c.asServerTextChannel().get().getTopic());
                TextChannel tc = stb.create().get();
                TchannelMap.put(c.getId(),tc.getId());
            }else {
                if (c.asServerVoiceChannel().isPresent()) {
                    ServerVoiceChannelBuilder stb = sbs.createVoiceChannelBuilder();
                    stb.setAuditLogReason("Backup");
                    Map<Permissionable, Permissions> perm = c.getOverwrittenPermissions();
                    if (!perm.isEmpty()) {
                        for (Permissionable p : perm.keySet()) {
                            stb.addPermissionOverwrite((Permissionable & DiscordEntity) p, perm.get(p));
                        }
                    }
                    if (c.asCategorizable().isPresent()) {
                        Optional<ChannelCategory> cat = c.asCategorizable().get().getCategory();
                        if (cat.isPresent()) {
                            stb.setCategory(sbs.getChannelCategoriesByName(cat.get().getName()).get(0));
                        }
                    }
                    stb.setName(c.getName());
                    if (c.asServerVoiceChannel().isPresent())
                        stb.setBitrate(c.asServerVoiceChannel().get().getBitrate());
                    if (c.asServerVoiceChannel().isPresent())
                        if (c.asServerVoiceChannel().get().getUserLimit().isPresent())
                            stb.setUserlimit(c.asServerVoiceChannel().get().getUserLimit().get());
                    stb.create().get();
                }
            }
        }
        System.out.println("Server ready!");
        sbs = api.getServerById(l.get()).get();
        System.out.println("Getting invite link...");
        for(Channel c:sbs.getChannels()){
            if(c.asServerTextChannel().isPresent()){
                System.out.println(c.asServerTextChannel().get().createInviteBuilder().create().get().getUrl().toString());

            }
        }
        System.out.println("Syncing messages!");
        for(TextChannel tc:s.getTextChannels()){
            MessageSet ms = tc.getMessagesWhile(new Predicate<Message>() {
                @Override
                public boolean test(Message message) {
                    return Duration.between(message.getCreationTimestamp(), Instant.now()).toDays() < 30;
                }
            }).get();
            TextChannel linked = sbs.getTextChannelById(TchannelMap.get(tc.getId())).get();
            if(ms.isEmpty())continue;
            for(Message m:ms){
                if(m.getAuthor().isBotUser())continue;
                EmbedBuilder eb = new EmbedBuilder();
                eb.setFooter(Date.from(Instant.ofEpochSecond(m.getCreationTimestamp().toEpochMilli())).toString());
                eb.setAuthor(m.getAuthor().getDiscriminatedName(),"",m.getAuthor().getAvatar());
                eb.setDescription(m.getContent());
                linked.sendMessage(eb);
            }
        }
        System.out.println("Synced all messages!");
        System.exit(0);

    }
}
