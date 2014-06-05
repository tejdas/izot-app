package net.izot.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

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
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void sendMessage(String message) {
		if (!isClosed) {
			ByteBuf buf = Unpooled.buffer();
			channel.writeAndFlush(buf);
		}
	}
}
