package net.client.izot;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class SubscriberClient {
    public static final String PUB_KEY = "InputPublisherKeyFromPubNub";

    public static final String SUB_KEY = "InputSubscriberKeyFromPubNub";

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Specify PubNub channel");
            return;
        }
        String channelName = args[0];
        final Pubnub pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);

        try {
            pubnub.subscribe(channelName, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    System.out.println("RECEIVED : " + channel + " : " + message.getClass() + " : " + message.toString());

                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down PubNub");
                pubnub.shutdown();
                System.out.println("Shutdown PubNub");
            }
        });
    }
}
