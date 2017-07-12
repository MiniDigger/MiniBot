package me.minidigger.ircnotifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.pircbotx.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import spark.Route;

import static spark.Spark.post;

public class JenkinsListener {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsListener.class);

    private Map<String, Route> routes = new HashMap<>();
    private JenkinsListener.MessageHandler messageHandler;

    public JenkinsListener(JenkinsListener.MessageHandler handler) {
        messageHandler = handler;

        // handle events

        // notify
        routes.put("notify", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();

            String name = object.get("name").getAsString();
            JsonObject build = object.get("build").getAsJsonObject();

            // ignore all other projects
            if (name.equalsIgnoreCase("VoxelGamesLibv2")) {
                String phase = build.get("phase").getAsString();
                if (phase.equalsIgnoreCase("QUEUED") || phase.equalsIgnoreCase("FINALIZED"))
                    return "meh";
                String status = build.get("status") == null ? "undefined" : build.get("status").getAsString();
                if (status.equalsIgnoreCase("SUCCESS")) {
                    status = Colors.set(status, Colors.GREEN);
                } else if (status.equalsIgnoreCase("failure")) {
                    status = Colors.set(status, Colors.RED);
                } else {
                    status = Colors.set(status, Colors.YELLOW);
                }

                messageHandler.handleMessage("Project " + name + " build #" + build.get("number").getAsInt() + ": " + phase + " [" + status + "] (" + build.get("full_url").getAsString() + ")");
            }

            return "meh";
        });

        // end events

        // register route
        post("/jenkins", (req, res) -> routes.get("notify").handle(req, res));
    }

    @FunctionalInterface
    interface MessageHandler {
        void handleMessage(String message);
    }
}
