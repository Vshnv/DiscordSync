package com.rocketmail.vaishnavanil.discordsync.utils;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Permissions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class BotUtil {
    public static DiscordApi authenticate(String token) throws ExecutionException, InterruptedException, MalformedURLException {
        DiscordApi api =
        new DiscordApiBuilder().setToken(token).login().get();
        System.out.println("Bot Invite : " + api.createBotInvite(Permissions.fromBitmask(8)));
        //if(!api.getYourself().getAvatar().getUrl().sameFile(new URL("https://preview.redd.it/5x8c8pqf9co41.png?width=640&format=png&auto=webp&s=33ee59816d5a6bed748285cf44a40b22cc0aceee"))){
            //api.createAccountUpdater().setAvatar(new URL("https://preview.redd.it/5x8c8pqf9co41.png?width=640&format=png&auto=webp&s=33ee59816d5a6bed748285cf44a40b22cc0aceee")).update().get();

        //}
        return api;
    }
}
