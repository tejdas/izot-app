package net.izot.bridge;

import com.pubnub.api.Pubnub;

public class PubnubContext {
	public static final String PUB_KEY = "pub-c-b2a4b991-340e-4168-9961-057d48f97e5c";

	public static final String SUB_KEY = "sub-c-c41ca018-d894-11e3-a226-02ee2ddab7fe";

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
