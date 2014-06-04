package net.client.izot;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class PubnubPublisher {
    static final class PublisherCallback extends Callback {
        @Override
        public void successCallback(String channel, Object message) {
            System.out.println("Published message on channel : " + channel);
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            System.out.println("Error while publishing message on channel : " + channel);
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
    	publish(jsonString, new JSONObject(jsonString));
    }

    public void publish(String channelName, JSONObject jsonObj) throws IOException, JSONException {
        PublisherCallback callback = new PublisherCallback();
        pubnub.publish(channelName, jsonObj, callback);
        try {
            System.out.println("Sleeping for 1 second");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }
}
