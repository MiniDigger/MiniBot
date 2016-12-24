package me.MiniDigger.BasinBot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Martin on 23.12.2016.
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Logger ghLogger = LoggerFactory.getLogger(GitHubListener.class);
    
    private static String channel;
    
    public static void main(String[] args) {
        logger.info("Starting...");
        
        //TODO get irc connection stuff via args
        channel = "#bot";
        
        Configuration configuration = new Configuration.Builder()
                .setName("BasinBot")
                .addServer("irc.basinmc.org")
                .addAutoJoinChannel(channel)
                .addListener(new BotListener())
                .buildConfiguration();
        
        PircBotX bot = new PircBotX(configuration);
        
        startThread("GitHub Listener", () -> new GitHubListener((msg) -> {
            ghLogger.info(msg);
            bot.sendIRC().message(channel, msg);
        }));
        
        startThread("PircBot", () -> {
            try {
                bot.startBot();
            } catch (IOException | IrcException e) {
                e.printStackTrace();
            }
        });
    }
    
    private static Thread startThread(String name, Runnable run) {
        Thread thread = new Thread(run);
        thread.setName(name);
        thread.start();
        return thread;
    }
}
