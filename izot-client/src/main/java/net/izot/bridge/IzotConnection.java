package net.izot.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class IzotConnection {
    private final String address;
    private final Channel channel;
    private final Set<String> registeredPubnubSubscribers = new CopyOnWriteArraySet<String>();
    private volatile boolean isClosed = false;

    public void disconnected() {
        System.out.println("Connection disconnected: " + address);
        isClosed = true;
        SubscriptionManager.unregisterConnection(address, this);
    }

    public boolean isClosed() {
        return isClosed;
    }

    public IzotConnection(Channel channel, String address) {
        this.channel = channel;
        this.address = address;
    }

    public void initialize() {
        IzotConnectionHandler connectionHandler = channel.pipeline().get(
                IzotConnectionHandler.class);
        connectionHandler.setConnection(this);
    }

    public void close() {
        if (isClosed) {
            return;
        }

        isClosed = true;
        ChannelFuture cf = channel.close();
        try {
            cf.sync();
            System.out.println("Connection closed to: " + address);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendMessage(String message) {
        if (!isClosed) {
            ByteBuf buf = Unpooled.buffer();
            ByteBufOutputStream stream = new ByteBufOutputStream(buf);

            try {
                stream.writeUTF(message);
                channel.writeAndFlush(buf);
            } catch (IOException e) {
                System.out.println("IOException while sending message: " + message);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.out.println("IOException while sending message: " + message);
                }
            }
        }
    }

    public void register(String channel) {
        registeredPubnubSubscribers.add(channel);
    }

    public void unregister(String channel) {
        registeredPubnubSubscribers.remove(channel);
    }

    public boolean hasPubnubSubscribers() {
        return (registeredPubnubSubscribers.size() > 0);
    }
}
