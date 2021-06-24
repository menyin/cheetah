package com.caisheng.cheetah.tester.client;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.client.connect.ClientConfig;
import com.caisheng.cheetah.client.connect.ConnectClientChannelHandler;
import com.caisheng.cheetah.network.netty.codec.PacketDecoder;
import com.caisheng.cheetah.network.netty.codec.PacketEncoder;
import com.caisheng.cheetah.tools.event.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.caisheng.cheetah.client.connect.ConnectClientChannelHandler.CONFIG_KEY;

public class ConnectClientBoot extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectClientBoot.class);

    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup;
//    private MonitorService monitorService;//TODO

    @Override
    protected void doStart(Listener listener) throws Throwable {
//        this.monitorService = new MonitorService(); //TODO
//        EventBus.create(monitorService.getThreadPoolManager().getEventBusExecutor());//TODO
        this.workGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(this.workGroup)//
                .option(ChannelOption.TCP_NODELAY, true)//
                .option(ChannelOption.SO_REUSEADDR, true)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60 * 1000)
                .option(ChannelOption.SO_RCVBUF, 5 * 1024 * 1024)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new PacketDecoder());
                        pipeline.addLast("encoder", PacketEncoder.INSTANCE);
                        pipeline.addLast("handler", new ConnectClientChannelHandler());
                    }
                });

        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (this.workGroup != null) {
            this.workGroup.shutdownGracefully();
        }
        listener.onSuccess();
    }

    public ChannelFuture connect(InetSocketAddress remote, InetSocketAddress local, ClientConfig clientConfig) {
        ChannelFuture channelFuture = local != null ? this.bootstrap.connect(remote, local) : this.bootstrap.connect(remote);
        if (channelFuture != null) {
            channelFuture.channel().attr(CONFIG_KEY).set(clientConfig);
        }
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                channelFuture.channel().attr(CONFIG_KEY).set(clientConfig);
                logger.info("start netty client success,remote={},local={}", remote, local);
            } else {
                logger.error("start netty client failure,remote={},local={}", remote, local, future.cause());
            }
        });
        return channelFuture;

    }

    public ChannelFuture connect(String host, int port, ClientConfig clientConfig) {
        return connect(new InetSocketAddress(host, port), null, clientConfig);
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public NioEventLoopGroup getWorkerGroup() {
        return workGroup;
    }

}
