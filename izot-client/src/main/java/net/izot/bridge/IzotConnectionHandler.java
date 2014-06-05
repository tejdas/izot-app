package net.izot.bridge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.json.JSONObject;

public class IzotConnectionHandler extends SimpleChannelInboundHandler<String> {
	private final PubnubPublisher pubnubPublisher = new PubnubPublisher();
	private IzotConnection connection = null;

	protected void setConnection(IzotConnection connection) {
		this.connection = connection;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Channel active");
		super.channelActive(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Channel unregistered");
		if (connection != null) {
			connection.disconnected();
		}
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		System.out.println("Exception caught: " + cause.toString());
		if (connection != null) {
			connection.disconnected();
		}
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		System.out.println("Received message: " + msg);

		if (msg.startsWith("KEYS ")) {
			LedControllerDataParser.buildKeys(msg.replaceFirst("KEYS ", ""));
		} else {
			JSONObject jsonObj = LedControllerDataParser.toJson(msg);
			pubnubPublisher.publish(IzotBridgeDriver.publishChannel, jsonObj);
		}
		ctx.write(msg);
		ctx.flush();
	}
}
