package com.caisheng.cheetah.network.netty.client;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Client;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.network.netty.codec.PacketDecoder;
import com.caisheng.cheetah.network.netty.codec.PacketEncoder;
import com.caisheng.cheetah.tools.Utils;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;

public abstract class NettyTCPClient extends BaseService implements Client {
    private static final Logger logger = LoggerFactory.getLogger(NettyTCPClient.class);

    private EventLoopGroup workGroup;
    protected Bootstrap bootstrap;

    /***************** 启动逻辑 begin *****************/
    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (Utils.useNettyEpoll()) {
            createEpollClient(listener);
        } else {
            createNioClient(listener);
        }
    }

    private void createNioClient(Listener listener) {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(getWorkThreadNum(), new DefaultThreadFactory(ThreadNames.T_TCP_CLIENT), getSelectorProvider());
        nioEventLoopGroup.setIoRatio(getIoRate());
        createClient(listener, nioEventLoopGroup, getChannelFactory());
    }

    private void createClient(Listener listener, EventLoopGroup workGroup, ChannelFactory<? extends Channel> channelFactory) {
        this.workGroup = workGroup;
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(workGroup)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .channelFactory(channelFactory)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        initPipeline(ch.pipeline());
                    }
                });
        initOptions(this.bootstrap);
        listener.onSuccess();

    }
    /***************** 启动逻辑 end *****************/

    /***************** 使用逻辑 begin *****************/
    public ChannelFuture connect(String host,int port) {
        return this.bootstrap.connect(new InetSocketAddress(host,port));
    }
    public ChannelFuture connect(String host,int port,Listener listener) {
        return this.bootstrap.connect(new InetSocketAddress(host,port)).addListener(future -> {
            if (future.isSuccess()) {
                if (listener!=null) {
                    listener.onSuccess(port);
                }
                logger.info("start netty connect success,host={},port={}",host,port);

            } else {
                if (listener!=null) {
                    listener.onFailure(future.cause());
                }
                logger.info("start netty connect failure,host={},port={}",host,port);

            }
        });
    }
    /***************** 使用逻辑 end *****************/


    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("decoder", getEncoder());
        pipeline.addLast("handler", getChannelHandler());
    }

    protected void initOptions(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }

    private void createEpollClient(Listener listener) {
        EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(getWorkThreadNum(),new DefaultThreadFactory(ThreadNames.T_TCP_CLIENT));
        epollEventLoopGroup.setIoRatio(getIoRate());
        createClient(listener,epollEventLoopGroup, EpollSocketChannel::new);
    }


    protected int getWorkThreadNum() {
        return 1;
    }

    public SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }

    public int getIoRate() {
        return 50;
    }

    public ChannelFactory<? extends Channel> getChannelFactory() {
        return NioSocketChannel::new;
    }

    public ChannelHandler getDecoder() {
        return new PacketDecoder();
    }

    public ChannelHandler getEncoder() {
        return new PacketEncoder();
    }

    public abstract ChannelHandler getChannelHandler();
}
