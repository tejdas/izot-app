package net.izot.bridge;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class Listener {
    private static final class IzotChannelInitializer extends
            ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(256));
            pipeline.addLast("stringDecoder",
                    new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast("stringEncoder",
                    new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast("handler", new IzotConnectionHandler());
        }
    }

    public static final String LISTEN_ADDRESS = "0.0.0.0";

    private boolean hasShutdown = false;

    private Channel serverChannel = null;

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start(int listenPort) {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(
                NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        bootstrap.childHandler(new IzotChannelInitializer());

        ChannelFuture f = bootstrap.bind(LISTEN_ADDRESS, listenPort);
        try {
            f.sync();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        serverChannel = f.channel();
        System.out.println(String.format("Izot Listener on %s:%s",
                LISTEN_ADDRESS, listenPort));
    }

    public void shutdown() {
        if (hasShutdown) {
            return;
        }

        hasShutdown = true;
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
                serverChannel = null;
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        System.out.println("Izot Listener shut down");
    }
}