package net.izot.bridge;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriptionManager {
    private static final ConcurrentMap<String, MessageRouter> messageRouters = new ConcurrentHashMap<String, MessageRouter>();

    private static final Object connectionsLock = new Object();

    private static final ConcurrentMap<String, IzotConnection> connections = new ConcurrentHashMap<String, IzotConnection>();

    private static final Object routerssLock = new Object();

    public static void registerSubscriber(String pubnubChannel, String host,
            int port) {
        String address = String.format("%s:%d", host, port);

        IzotConnection connection;
        synchronized (connectionsLock) {
            connection = connections.get(address);
            if (connection == null) {
                connection = IzotConnectionFactory.createConnection(host, port, address);
                if (connection == null) {
                    System.out.println("Cannot created connection to : "
                            + address);
                    return;
                }
                connections.put(address, connection);
            }
            connection.register(pubnubChannel);
        }

        MessageRouter messageRouter;
        boolean doInititialize = false;
        synchronized (routerssLock) {
            messageRouter = messageRouters.get(pubnubChannel);
            if (messageRouter == null) {
                doInititialize = true;
                messageRouter = new MessageRouter(pubnubChannel);
                messageRouters.put(pubnubChannel, messageRouter);
            }
            messageRouter.register(address);
        }

        if (doInititialize) {
            if (!messageRouter.initialize()) {
                synchronized (routerssLock) {
                    messageRouters.remove(pubnubChannel);
                }
                return;
            }
        }
        System.out.println("Message router for channel: " + pubnubChannel
                + " and endpoint: " + address);
    }

    public static void unregisterSubscriber(String pubnubChannel, String host,
            int port) {
        String address = String.format("%s:%d", host, port);
        IzotConnection connection;
        synchronized (connectionsLock) {
            connection = connections.get(address);
            if (connection != null) {
                connection.unregister(pubnubChannel);
                if (!connection.hasPubnubSubscribers()) {
                    connections.remove(address);
                    connection.close();
                }
            }
        }

        MessageRouter messageRouter;
        boolean doCleanup = false;
        synchronized (routerssLock) {
            messageRouter = messageRouters.get(pubnubChannel);
            if (messageRouter != null) {
                messageRouter.unregister(address);
                if (!messageRouter.hasSubscribers()) {
                    messageRouter = messageRouters.remove(pubnubChannel);
                    doCleanup = true;
                }
            }
        }

        if (doCleanup) {
            messageRouter.cleanup();
        }
        System.out.println("Endpoint: " + address
                + " unsubscribed from channel: " + pubnubChannel);
    }

    public static IzotConnection getConnection(String address) {
        return connections.get(address);
    }

    public static void unregisterConnection(String address, IzotConnection connection) {
        synchronized (connectionsLock) {
            connections.remove(address, connection);
        }
    }
}
