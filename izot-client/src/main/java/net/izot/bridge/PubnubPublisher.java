package net.izot.bridge;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
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
            System.out.println("Error details: " + error.errorCode + "  " + error.getErrorString());
        }
    }

    private final PublisherCallback callback = new PublisherCallback();

    public void publish(String channelName, String jsonString) throws IOException, JSONException {
        publish(jsonString, new JSONObject(jsonString));
    }

    public void publish(String channelName, JSONObject jsonObj) throws IOException, JSONException {
        PubnubContext.getPubnub().publish(channelName, jsonObj, callback);
    }
}
