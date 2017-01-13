package me.MiniDigger.BasinBot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * Created by Martin on 13.01.2017.
 */
public class APIAI extends ListenerAdapter {
    
    private AIDataService dataService;
    private List<String> stillInAction;
    
    public APIAI(String apiai) {
        AIConfiguration configuration = new AIConfiguration(apiai);
        dataService = new AIDataService(configuration);
        stillInAction = new ArrayList<>();
    }
    
    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (isTriggered(event.getMessage()) || stillInAction.contains(event.getUser().getNick())) {
            try {
                AIRequest request = new AIRequest(event.getMessage());
                AIResponse response = dataService.request(request);
                
                if (response.getStatus().getCode() == 200) {
                    event.respond(response.getResult().getFulfillment().getSpeech());
                    if (response.getResult().isActionIncomplete()) {
                        if (!stillInAction.contains(event.getUser().getNick())) {
                            stillInAction.add(event.getUser().getNick());
                        }
                    } else {
                        stillInAction.remove(event.getUser().getNick());
                    }
                } else {
                    event.respond(response.getStatus().getErrorDetails());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private boolean isTriggered(String message) {
        return message.startsWith("bot");
    }
}
