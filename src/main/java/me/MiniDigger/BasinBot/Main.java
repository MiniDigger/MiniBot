package me.MiniDigger.BasinBot;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Logger ghLogger = LoggerFactory.getLogger(GitHubListener.class);
    
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("name", true, "The name of the bot");
        options.addOption("server", true, "The server the bot will connect to");
        options.addOption("channel", true, "The channel the bot will join");
        options.addOption("apiai", true, "The API.AI key");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        
        new Main().start(cmd.getOptionValue("name"), cmd.getOptionValue("server"), cmd.getOptionValue("channel"), cmd.getOptionValue("apiai"));
    }
    
    public void start(String name, String server, String channel, String APIAI) {
        logger.info("Starting...");
        
        //APIAI apiai = new APIAI(APIAI);
        
        Configuration configuration = new Configuration.Builder()
                .setName(name)
                .addServer(server)
                .addAutoJoinChannel(channel)
                //.addListener(apiai)
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
    
    private Thread startThread(String name, Runnable run) {
        Thread thread = new Thread(run);
        thread.setName(name);
        thread.start();
        return thread;
    }
}
