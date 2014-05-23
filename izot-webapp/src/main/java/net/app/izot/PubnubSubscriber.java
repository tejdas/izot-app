package net.app.izot;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class PubnubSubscriber {
    public static final String PUB_KEY = "InputPublisherKeyFromPubNub";

    public static final String SUB_KEY = "InputSubscriberKeyFromPubNub";

    private final Queue<JSONObject> messages = new ConcurrentLinkedQueue<>();

    public final String channel;

    private final Pubnub pubnub;

    public PubnubSubscriber(String channel, Pubnub pubnub) {
        this.channel = channel;
        this.pubnub = pubnub;
    }

    public void start() throws PubnubException {
        pubnub.subscribe(channel, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println("RECEIVED : " + channel + " : " + message.getClass() + " : " + message.toString());
                if (message instanceof JSONObject) {
                    messages.offer((JSONObject) message);
                }
            }
        });
    }

    public int getMessageCount() {
        return messages.size();
    }

    public JSONObject getMessage() {
        return messages.poll();
    }
}
