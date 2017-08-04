package me.minidigger.ircnotifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.pircbotx.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import spark.Route;

import static spark.Spark.post;

/**
 * Created by Martin on 23.12.2016.
 */
public class GitHubListener {

    private static final Logger logger = LoggerFactory.getLogger(GitHubListener.class);

    private Map<String, Route> routes = new HashMap<>();
    private MessageHandler messageHandler;

    public GitHubListener(MessageHandler handler) {
        messageHandler = handler;

        // handle events

        // ping
        routes.put("ping", (req, res) -> "Ehy yo, github, we are alive!");

        // issue_comment
        routes.put("issue_comment", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();

            String action = object.get("action").getAsString();

            int issueId = object.get("issue").getAsJsonObject().get("number").getAsInt();
            String repo = object.get("repository").getAsJsonObject().get("name").getAsString();
            String url = object.get("comment").getAsJsonObject().get("html_url").getAsString();
            String username = object.get("comment").getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();
            String body = object.get("comment").getAsJsonObject().get("body").getAsString();

            switch (action) {
                case "created":
                    messageHandler.handleMessage(username + " commented on " + repo + "#" + issueId + ": " + body + " (" + url + ")");
                    break;
            }
            return "Cool story bro";
        });

        // issues
        routes.put("issues", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();
            JsonObject issue = object.getAsJsonObject("issue").getAsJsonObject();

            String action = object.get("action").getAsString();

            int issueId = issue.get("number").getAsInt();
            String url = issue.get("html_url").getAsString();
            String title = issue.get("title").getAsString();
            String username = object.get("sender").getAsJsonObject().get("login").getAsString();
            String repo = object.get("repository").getAsJsonObject().get("name").getAsString();

            switch (action) {
                case "opened":
                    messageHandler.handleMessage(username + " opened issue " + repo + "#" + issueId + ": " + title + " (" + url + ")");
                    break;
                case "closed":
                    messageHandler.handleMessage(username + " closed issue " + repo + "#" + issueId + ": " + title + " (" + url + ")");
                    break;
                case "reopened":
                    messageHandler.handleMessage(username + " reopened issue " + repo + "#" + issueId + ": " + title + " (" + url + ")");
                    break;
            }
            return "That happened";
        });

        // create
        routes.put("create", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();

            String type = object.get("ref_type").getAsString();

            String repo = object.get("repository").getAsJsonObject().get("name").getAsString();
            String tag = object.get("ref").getAsString();
            String username = object.get("sender").getAsJsonObject().get("login").getAsString();

            switch (type) {
                case "tag":
                    messageHandler.handleMessage(username + " created tag " + tag + " on repo " + repo); // TODO can we get an url here?
                    break;
            }
            return "Cool stuff";
        });

        // push
        routes.put("push", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();
            JsonArray commits = object.get("commits").getAsJsonArray();

            String ref = object.get("ref").getAsString().replace("refs/heads/", "");
            String url = object.get("compare").getAsString();
            String user = object.get("pusher").getAsJsonObject().get("name").getAsString();
            String repo = object.get("repository").getAsJsonObject().get("name").getAsString();
            boolean force = object.get("forced").getAsBoolean();

            String pushed = force ? Colors.set(" force pushed ", Colors.RED) : " pushed ";

            messageHandler.handleMessage(user + pushed + commits.size() + " commit(s) to " + ref + "@"
                    + Colors.set(repo, Colors.YELLOW) + " (" + url + ")");

            for (JsonElement elem : commits) {
                JsonObject obj = elem.getAsJsonObject();

                String username = obj.get("author").getAsJsonObject().get("username").getAsString();
                String message = obj.get("message").getAsString();
                int added = obj.get("added").getAsJsonArray().size();
                int modified = obj.get("modified").getAsJsonArray().size();
                int removed = obj.get("removed").getAsJsonArray().size();

                messageHandler.handleMessage(username + ": " + message + " (" + Colors.set("+" + added, Colors.GREEN)
                        + "/" + Colors.set("-" + removed, Colors.RED) + "/" + Colors.set("~" + modified, Colors.YELLOW) + ")");
            }
            return "Push harder pls";
        });

        // pull request
        routes.put("pull_request", (req, res) -> {
            JsonObject object = new JsonParser().parse(req.body()).getAsJsonObject();
            JsonObject pr = object.getAsJsonObject("pull_request").getAsJsonObject();

            String action = object.get("action").getAsString();

            int id = object.get("number").getAsInt();
            String repo = object.get("repository").getAsJsonObject().get("name").getAsString();
            String url = pr.get("html_url").getAsString();
            String title = pr.get("title").getAsString();
            String user = pr.get("user").getAsJsonObject().get("login").getAsString();

            switch (action) {
                case "opened":
                    messageHandler.handleMessage(user + " opened pr " + repo + "#" + id + ":  " + title + " (" + url + ")");
                    break;
                case "closed":
                    messageHandler.handleMessage(user + " closed pr " + repo + "#" + id + ":  " + title + " (" + url + ")");
                    break;
                case "reopened":
                    messageHandler.handleMessage(user + " reopened pr " + repo + "#" + id + ":  " + title + " (" + url + ")");
                    break;
            }

            return "Pull me closer";
        });

        // end events

        // register route
        post("/github", (req, res) -> {
            String type = req.headers("X-GitHub-Event");
            logger.debug("got " + type + ": " + req.body());
            if (routes.containsKey(type)) {
                return routes.get(type).handle(req, res);
            } else {
                logger.info("did not handle " + type);
                return "WTF do you want from me?!";
            }
        });
    }

    @FunctionalInterface
    interface MessageHandler {
        void handleMessage(String message);
    }
}
