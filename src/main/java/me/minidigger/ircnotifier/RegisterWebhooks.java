package me.minidigger.ircnotifier;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

public class RegisterWebhooks {
    public static void main(String[] args) throws IOException {
        GitHubClient client = new GitHubClient();
        client.setCredentials("MiniDigger", "sssss");

        RepositoryService service = new RepositoryService();
        for (Repository repo : service.getRepositories("MiniDigger")) {
            RepositoryHook hook = new RepositoryHook();
            hook.setActive(true);
            hook.getConfig().put("url", "http://bender.minidigger.me:3263/github");
            hook.getConfig().put("content_type", "json");
//            hook.setEvent()
            hook.setName("web");
            service.createHook(repo, hook);
        }
    }
}
