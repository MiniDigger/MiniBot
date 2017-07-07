package me.minidigger.ircnotifier;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mbenndorf on 07.07.2017.
 */
public class CommandListener extends ListenerAdapter {

    private Map<String, Command> commands = new HashMap<>();

    public CommandListener() {
        commands.put("ping", (channel, user, args) -> channel.send().message(
                Color.color(Color.AQUA) + user.getNick() + ": " + Color.color(Color.RED, Color.BLUE) + " pong!"));
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().startsWith("!")) {
            String[] args = event.getMessage().split(" ");
            String cmd = args[0].replaceFirst("!", "");
            args = Arrays.copyOfRange(args, 1, args.length);
            if (commands.containsKey(cmd)) {
                commands.get(cmd).execute(event.getChannel(), event.getUser(), args);
            }
        }
    }


    @FunctionalInterface
    interface Command {
        void execute(Channel channel, User user, String[] args);
    }
}
