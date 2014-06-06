package net.izot.bridge;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pubnub.api.PubnubException;

public class SubscriptionManager {
    private static final ConcurrentMap<String, MessageRouter> messageRouters = new ConcurrentHashMap<String, MessageRouter>();

    private static final ConcurrentMap<String, IzotConnection> connections = new ConcurrentHashMap<String, IzotConnection>();

    private static final Object lock = new Object();

    public static void registerSubscriber(String channelName, String host,
            int port) {
        String address = String.format("%s:%d", host, port);
        IzotConnection connection = getConnection(address);
        if (connection == null) {
            connection = IzotConnectionFactory.createConnection(host, port);
            if (connection == null) {
                System.out.println("Cannot created connection to : " + address);
            }
            connection = registerConnection(address, connection);
        }

        MessageRouter messageRouter = null;
        synchronized (lock) {
            messageRouter = messageRouters.get(channelName);
            if (messageRouter == null) {
                messageRouter = new MessageRouter(channelName);
                PubnubSubscriber subscriber = new PubnubSubscriber(channelName, PubnubContext.getPubnub(), messageRouter);
                messageRouter.setSubscriber(subscriber);
                try {
                    subscriber.start();
                } catch (PubnubException e) {
                    System.out.println("Could not start subscriber at channel: " + channelName);
                    return;
                }
                messageRouters.put(channelName, messageRouter);
                System.out.println("Message router created for channel: " + channelName);
            }
        }
        messageRouter.register(address);
        System.out.println("Message router for channel: " + channelName + " and endpoint: " + address);
    }

    public static void unregisterSubscriber(String channelName, String host,
            int port) {
        String address = String.format("%s:%d", host, port);
        IzotConnection connection = unregisterConnection(address);
        if (connection != null) {
            connection.close();
        }

        MessageRouter messageRouter = messageRouters.get(channelName);
        if (messageRouter != null) {
            messageRouter.unregister(address);
            if (!messageRouter.hasSubscribers()) {
                messageRouters.remove(channelName);
                try {
                    messageRouter.getSubscriber().stop();
                } catch (PubnubException e) {
                    System.out.println("Could not stop subscriber at channel: " + channelName);
                }
            }
        }
        System.out.println("Endpoint: " + address + " unsubscribed from channel: " + channelName);
    }

    public static IzotConnection registerConnection(String address,
            IzotConnection connection) {
        IzotConnection conn = connections.putIfAbsent(address, connection);
        if (conn == null) {
            return connection;
        }
        if (conn != connection) {
            connection.close();
        }
        return conn;
    }

    public static IzotConnection getConnection(String address) {
        return connections.get(address);
    }

    public static IzotConnection unregisterConnection(String address) {
        return connections.remove(address);
    }
}
