package net.izot.bridge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Exception caught: " + cause.toString());
        if (connection != null) {
            connection.disconnected();
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Received message: " + msg);

        if (msg.startsWith("KEYS ")) {
            LedControllerDataParser.buildKeys(msg.replaceFirst("KEYS ", ""));
        } else if (msg.startsWith("SUBSCRIBE ") || msg.startsWith("UNSUBSCRIBE ")) {
            StringTokenizer tokenizer = new StringTokenizer(msg, " ");
            String command = tokenizer.nextToken().trim();
            Map<String, String> keyValuePairs = new HashMap<String, String>();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                int index = token.indexOf('=');
                if (index == -1)
                    continue;
                String key = token.substring(0, index);
                String value = token.substring(index + 1);
                keyValuePairs.put(key, value);
            }

            String channel = keyValuePairs.get("channel");
            String host = keyValuePairs.get("host");
            String portStr = keyValuePairs.get("port");

            if (StringUtils.isEmpty(channel) || StringUtils.isEmpty(host) || StringUtils.isEmpty(portStr)) {
                System.out.println("Malformed command");
                return;
            }

            int port = Integer.parseInt(portStr);
            if (command.equalsIgnoreCase("SUBSCRIBE")) {
                SubscriptionManager.registerSubscriber(channel, host, port);
            } else {
                SubscriptionManager.unregisterSubscriber(channel, host, port);
            }

        } else {
            JSONObject jsonObj = LedControllerDataParser.toJson(msg);
            pubnubPublisher.publish(IzotBridgeDriver.publishChannel, jsonObj);
        }
        ctx.write(msg);
        ctx.flush();
    }
}
