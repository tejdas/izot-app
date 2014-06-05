package net.izot.bridge;

import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class PubnubSubscriber {
    public final String channel;
    private final Pubnub pubnub;
    private final MessageRouter messageRouter;

    public PubnubSubscriber(String channel, Pubnub pubnub, MessageRouter messageRouter) {
        this.channel = channel;
        this.pubnub = pubnub;
        this.messageRouter = messageRouter;
    }

    public void start() throws PubnubException {
        pubnub.subscribe(channel, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println("RECEIVED message on channel: " + channel);
                if (message instanceof JSONObject) {
                	messageRouter.routeMessage((JSONObject)message);
                }
            }
        });
    }
}
