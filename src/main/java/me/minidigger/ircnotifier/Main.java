package me.minidigger.ircnotifier;

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
import java.io.PrintWriter;
import java.io.StringWriter;

import static spark.Spark.exception;
import static spark.Spark.port;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Logger ghLogger = LoggerFactory.getLogger(GitHubListener.class);
    private static final Logger ciLogger = LoggerFactory.getLogger(JenkinsListener.class);

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("name", true, "The name of the bot");
        options.addOption("password", true, "The nickserv password of the bot");
        options.addOption("server", true, "The server the bot will connect to");
        options.addOption("channel", true, "The channel the bot will join");
        options.addOption("apiai", true, "The API.AI key");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        new Main().start(cmd.getOptionValue("name"), cmd.getOptionValue("password"), cmd.getOptionValue("server"), cmd.getOptionValue("channel"), cmd.getOptionValue("apiai"));
    }

    public void start(String name, String password, String server, String channel, String APIAI) {
        logger.info("Starting...");

        //APIAI apiai = new APIAI(APIAI);

        Configuration configuration = new Configuration.Builder()
                .setName(name)
                .addServer(server)
                .setAutoReconnect(true)
                .setNickservNick(name)
                .setNickservPassword(password)
                .addAutoJoinChannel(channel)
                .addListener(new CommandListener())
                //.addListener(apiai)
                .buildConfiguration();

        PircBotX bot = new PircBotX(configuration);

        startThread("Spark", () -> {
            port(3263);

            new GitHubListener((msg) -> {
                ghLogger.info(msg);
                bot.sendIRC().message(channel, msg);
            });

            new JenkinsListener((msg) -> {
                ciLogger.info(msg);
                bot.sendIRC().message(channel, msg);
            });

            // catch exceptions
            exception(Exception.class, (ex, req, res) -> {
                logger.info("exception " + ex.getClass().getName() + ": " + ex.getMessage());
                ex.printStackTrace();
                try {
                    logger.info(req.contextPath() + " " + req.body());
                } catch (NullPointerException e) {
                    logger.info("WTF");
                    return;
                }
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(new StringWriter());
                ex.printStackTrace(pw);
                res.status(500);
                res.body("Well, thats embarrassing<br>"
                        + "exception " + ex.getClass().getName() + ": " + ex.getMessage() + "<br>" +
                        sw.toString());
            });
        });

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
