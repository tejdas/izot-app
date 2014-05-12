package net.client.izot;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class PublisherClient {
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

    public static final String PUB_KEY = "pub-c-b2a4b991-340e-4168-9961-057d48f97e5c";

    public static final String SUB_KEY = "sub-c-c41ca018-d894-11e3-a226-02ee2ddab7fe";

    public static void main(String[] args) throws IOException, JSONException {
        if (args==null || args.length == 0) {
            System.out.println("Specify PubNub channel");
            return;
        }
        String channelName = args[0];
        Pubnub pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);

        TestCallback callback = new TestCallback();
        pubnub.publish(channelName, "Hello World " + new Date().toString(), callback);

        JSONObject obj = new JSONObject();
        obj.put("name", "VMWare");
        obj.put("symbol", "VMW");
        obj.put("stockPrice", 92.53);
        obj.put("Date", new Date().toString());
        pubnub.publish(channelName, obj, callback);
        try {
            System.out.println("Sleeping for 10 seconds");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        pubnub.shutdown();
    }
}
