package me.minidigger.ircnotifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import spark.Route;

import static spark.Spark.exception;
import static spark.Spark.halt;
import static spark.Spark.port;
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
                messageHandler.handleMessage("Project " + name + " build #" + build.get("number").getAsInt() + ": " + build.get("phase").getAsString() + " [" + build.get("status") + "] (" + build.get("full_url").getAsString() + ")");
            }

            return "meh";
        });

        // end events

        port(3263);

        // register route
        post("/jenkins", (req, res) -> routes.get("notify").handle(req, res));

        // catch exceptions
        exception(Exception.class, (ex, req, res) -> {
            logger.info("exception " + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();
            logger.info(res.body());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(new StringWriter());
            ex.printStackTrace(pw);
            halt(500, "Well, thats embarrassing<br>"
                    + "exception " + ex.getClass().getName() + ": " + ex.getMessage() + "<br>" +
                    sw.toString());
        });
    }

    @FunctionalInterface
    interface MessageHandler {
        void handleMessage(String message);
    }
}
