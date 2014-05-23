package net.client.izot;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class PubnubPublisher {
    static final class TestCallback extends Callback {
        @Override
        public void successCallback(String channel, Object message) {
            System.out.println("SENT : " + channel + " : " + message.getClass() + " : " + message.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            System.out.println("SENT : " + channel + " : " + error.toString());
        }
    }

    public static final String PUB_KEY = "InputPublisherKeyFromPubNub";

    public static final String SUB_KEY = "InputSubscriberKeyFromPubNub";

    private Pubnub pubnub = null;

    public void initialize() {
        pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);
    }

    public void shutdown() {
        if (pubnub != null) {
            pubnub.shutdown();
        }
    }
    public void publish(String channelName, String jsonString) throws IOException, JSONException {
        TestCallback callback = new TestCallback();
        pubnub.publish(channelName, "Hello World " + new Date().toString(), callback);

        JSONObject obj = new JSONObject(jsonString);
        pubnub.publish(channelName, obj, callback);
        try {
            System.out.println("Sleeping for 10 seconds");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
    }
}
