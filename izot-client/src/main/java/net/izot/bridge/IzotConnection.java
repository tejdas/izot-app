package net.izot.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;

public class IzotConnection {
    private final Channel channel;

    private volatile boolean isClosed = false;

    public void disconnected() {
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public IzotConnection(Channel channel) {
        this.channel = channel;
    }

    public void initialize() {
        IzotConnectionHandler connectionHandler = channel.pipeline().get(
                IzotConnectionHandler.class);
        connectionHandler.setConnection(this);
    }

    public void close() {
        isClosed = true;
        ChannelFuture cf = channel.close();
        try {
            cf.sync();
            System.out.println("Connection closed");
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
                channel.writeAndFlush(buf).sync();
                System.out.println("Sent message: " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("InterruptedException while sending message: " + message);
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
}
