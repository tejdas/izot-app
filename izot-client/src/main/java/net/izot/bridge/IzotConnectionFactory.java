package net.izot.bridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class IzotConnectionFactory {
    private static final class IzotChannelInitializer extends
            ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("handler", new IzotConnectionHandler());
        }
    }

    private static EventLoopGroup group = null;

    private static final Bootstrap bootStrap = new Bootstrap();

    public static void initialize() {
        group = new NioEventLoopGroup();
        try {
            bootStrap.group(group).channel(NioSocketChannel.class);
            bootStrap.handler(new IzotChannelInitializer());
        } finally {
        }
    }

    public static void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public static IzotConnection createConnection(String host, int port) {
        try {
            ChannelFuture f = bootStrap.connect(host, port).sync();
            Channel ch = f.sync().channel();
            IzotConnection connection = new IzotConnection(ch);
            connection.initialize();
            System.out.println(String.format("Connection created to %s:%d",
                    host, port));
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
