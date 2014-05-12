package net.app.izot;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

public class SamplePublisher {
    public static final String PUB_KEY = "pub-c-b2a4b991-340e-4168-9961-057d48f97e5c";

    public static final String SUB_KEY = "sub-c-c41ca018-d894-11e3-a226-02ee2ddab7fe";

    public final String channel;

    private final Pubnub pubnub;

    public SamplePublisher(String channel) {
        this.channel = channel;
        pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);
    }

    public void sendMessage(String key, String value) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(key, value);
        pubnub.publish(channel, obj, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println("SENT : " + channel + " : " + message.getClass() + " : " + message.toString());
            }
        });
    }

    public void stop() {
        pubnub.shutdown();
    }
}
