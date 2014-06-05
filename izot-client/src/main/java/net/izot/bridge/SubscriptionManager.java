package net.izot.bridge;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class SubscriptionManager {
	private static final ConcurrentMap<String, MessageRouter> messageRouters = new ConcurrentHashMap<String, MessageRouter>();
	private static final Object lock = new Object();
	private static Pubnub pubnub = null;

	public static void registerSubscriber(String channelName, String host,
			int port) {
		MessageRouter messageRouter = null;
		synchronized (lock) {
			messageRouter = messageRouters.get(channelName);
			if (messageRouter == null) {
				messageRouter = new MessageRouter();
				PubnubSubscriber subscriber = new PubnubSubscriber(channelName, pubnub, messageRouter);
				try {
					subscriber.start();
				} catch (PubnubException e) {
					System.out.println("Could not start subscriber at channel: " + channelName);
					return;
				}
				messageRouters.put(channelName, messageRouter);
			}
		}

		IzotConnection connection = IzotConnectionFactory.createConnection(host, port);
		if (connection != null) {
			//messageRouter.register(connection);
		}
	}
}
