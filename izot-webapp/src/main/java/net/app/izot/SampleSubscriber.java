package net.app.izot;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class SampleSubscriber {

    public static final String PUB_KEY = "pub-c-b2a4b991-340e-4168-9961-057d48f97e5c";

    public static final String SUB_KEY = "sub-c-c41ca018-d894-11e3-a226-02ee2ddab7fe";

    private final Queue<JSONObject> messages = new ConcurrentLinkedQueue<>();

    public final String channel;

    private final Pubnub pubnub;

    private boolean initialized = false;

    public SampleSubscriber(String channel) {
        this.channel = channel;
        pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);
    }

    public void start() {
        try {
            pubnub.subscribe(channel, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    System.out.println("RECEIVED : " + channel + " : " + message.getClass() + " : " + message.toString());
                    if (message instanceof JSONObject) {
                        messages.offer((JSONObject) message);
                    }
                }
            });
            initialized = true;
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (initialized) {
            pubnub.shutdown();
        }
    }

    public int getMessageCount() {
        return messages.size();
    }

    public JSONObject getMessage() {
        return messages.poll();
    }
}
