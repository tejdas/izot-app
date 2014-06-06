package net.izot.bridge;

import com.pubnub.api.Pubnub;

public class PubnubContext {
    public static final String PUB_KEY = "InputPublisherKeyFromPubNub";

    public static final String SUB_KEY = "InputSubscriberKeyFromPubNub";

    private static Pubnub pubnub = null;

    public static void initialize() {
        pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);
    }

    public static void shutdown() {
        if (pubnub != null) {
            pubnub.shutdown();
        }
    }

    public static Pubnub getPubnub() {
        return pubnub;
    }
}
