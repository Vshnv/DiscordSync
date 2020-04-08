package com.rocketmail.vaishnavanil.discordsync;

import com.rocketmail.vaishnavanil.discordsync.bot.BackupBot;
import com.rocketmail.vaishnavanil.discordsync.bot.SyncBot;
import com.rocketmail.vaishnavanil.discordsync.utils.BotUtil;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.javacord.api.DiscordApi;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class DiscordSync {
    public static void main(String[] args) throws IOException {
        String token = null,mode = null,ch = null,server = null;
        args = new String[]{"-t", "MzUzMzkwMDcxMTI0MTk3Mzg2.Xoyg5w.h0EYniiFBjsZZwSb6nsI2ej9H3Q","-m","backup","-s","652375919327182849"};
        for(int i = 0 ; i<args.length;i++){
            switch (args[i]){
                case "-t":
                    token = args[i+1];
                    break;
                case "-m":
                    mode = args[i+1];
                    break;
                case "-c":
                    ch = args[i+1];
                    break;
                case "-s":
                    server = args[i+1];
                    break;
                default:
                    continue;
            }
        }
        token = token.trim();
        mode = mode.trim().toLowerCase();
        if(mode.equals("backup")){
            if(server == null){
                System.out.println("For backup you need to provide the server to be backedup using -s <ID>");return;
            }
            DiscordApi apiB;
            try {

                apiB = BotUtil.authenticate(token);
            } catch (Exception e) {
                System.out.println("Invalid token / Bad Connection! Recheck your internet connection and token!");
                return;
            }
            long l = Long.parseLong(server);
            BackupBot bb = new BackupBot(apiB,l);
            try {
                bb.begin();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

        ch = ch.replace("{","").replaceAll("}","").trim();
        String[] chA = ch.split(",");
        if(chA[0].equals("")){
            System.out.println("Could not find any channels in channel section of config!");
            return;
        }
        long[] channels = new long[chA.length];
        for(int i = 0; i<chA.length;i++)channels[i]=Long.valueOf(chA[i].trim());



        if(!mode.equalsIgnoreCase("sync") && !mode.equalsIgnoreCase("backup")){
            System.out.println("Invalid mode! Please use sync or backup");
            return;
        }

        DiscordApi api;
        try {

            api = BotUtil.authenticate(token);
        } catch (Exception e) {
            System.out.println("Invalid token / Bad Connection! Recheck your internet connection and token!");
            return;
        }
        switch (mode.toLowerCase()){
            case "sync":
                SyncBot sb = new SyncBot(api,channels);
                sb.begin();
                break;
            default:
                System.out.println("Invalid mode!");
                return;
        }
    }

    private static void generateDefaultConfig(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream buf = new BufferedOutputStream(fos);
        String token = "token: \n";
        String mode = "mode: sync/backup\n";
        String channels = "channels: {}\n";
        buf.write(token.getBytes());
        buf.write(mode.getBytes());
        buf.write(channels.getBytes());
        buf.flush();
        buf.close();
    }
}
