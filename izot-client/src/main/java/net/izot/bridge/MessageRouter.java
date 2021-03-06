package net.izot.bridge;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONObject;

import com.pubnub.api.PubnubException;

public class MessageRouter {
    private final String channel;

    private final Set<String> registeredTCPSubscribers = new CopyOnWriteArraySet<String>();

    private PubnubSubscriber subscriber = null;

    public MessageRouter(String channel) {
        this.channel = channel;
    }

    public void setSubscriber(PubnubSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    public PubnubSubscriber getSubscriber() {
        return subscriber;
    }

    public void register(String address) {
        registeredTCPSubscribers.add(address);
        System.out.println("Endpoint: " + address + " subscribed on Pubnub channel: " + channel);
    }

    public void unregister(String address) {
        registeredTCPSubscribers.remove(address);
        System.out.println("Endpoint: " + address + " unsubscribed from Pubnub channel: " + channel);
    }

    public boolean hasSubscribers() {
        return (registeredTCPSubscribers.size() > 0);
    }

    public boolean initialize() {
        PubnubSubscriber subscriber = new PubnubSubscriber(channel, PubnubContext.getPubnub(), this);
        setSubscriber(subscriber);
        try {
            subscriber.start();
            return true;
        } catch (PubnubException e) {
            System.out.println("Could not start subscriber at channel: " + channel);
            return false;
        }
    }

    public void cleanup() {
        try {
            subscriber.stop();
        } catch (PubnubException e) {
            System.out.println("Could not stop subscriber at channel: " + channel);
        }
    }

    public void routeMessage(JSONObject message) {
        String messageStr = message.toString();
        for (String address : registeredTCPSubscribers) {
            IzotConnection connection = SubscriptionManager.getConnection(address);
            if (connection != null) {
                if (connection.isClosed()) {
                    registeredTCPSubscribers.remove(address);
                    System.out.println("Message could not be routed. Connection to: " + address + " is already closed");
                } else {
                    System.out.println("Message being routed to: " + address);
                    connection.sendMessage(messageStr);
                }
            } else {
                System.out.println("Message could not be routed. No connection to: " + address);
                registeredTCPSubscribers.remove(address);
            }
        }
    }
}
