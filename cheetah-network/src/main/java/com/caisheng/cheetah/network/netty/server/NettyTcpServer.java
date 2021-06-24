package com.caisheng.cheetah.network.netty.server;

import com.caisheng.cheetah.api.service.*;
import com.caisheng.cheetah.network.netty.codec.PacketDecoder;
import com.caisheng.cheetah.tools.Utils;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NettyTcpServer extends BaseService implements Server {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final int port;//final成员变量：强制必须并只能在构造函数中赋值
    protected final String host;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workGroup;
    private ChannelHandler decoder;
    private ChannelHandler encoder;

    protected int getBossThreadNum() {
        return Runtime.getRuntime().availableProcessors() * 2;//现代计算机基本都是双核+，所以默认设置2个线程，充分利用cpu资源
//        return 1;//
    }

    protected ThreadFactory getBossThreadFactory() {
        return new DefaultThreadFactory(getBossThreadName());
    }

    protected SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }

    protected String getBossThreadName() {
        return ThreadNames.T_BOSS;
    }

    protected int getWorkThreadNum() {
        return 0;
    }

    public ThreadFactory getWorkThreadFactory() {
        return new DefaultThreadFactory(getWorkThreadName());
    }

    protected String getWorkThreadName() {
        return ThreadNames.T_WORKER;
    }

    
    protected ChannelFactory<? extends ServerChannel> getChannelFactory() {
        return NioServerSocketChannel::new;
    }


    protected ChannelHandler getDecoder() {
        return new PacketDecoder();
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    protected abstract ChannelHandler getChannelHandler() ;

    public int getIoRate() {
        return 70;
    }


    public enum State {Created, Initialized, Starting, Started, Shutdown}

    protected final AtomicReference<State> serverState = new AtomicReference<>(State.Created);

    public NettyTcpServer(int port) {
        this(port, null);
    }

    public NettyTcpServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void init() {
        if (!serverState.compareAndSet(State.Created, State.Initialized)) {
            new ServiceException(String.format("service %s is inited", this.getClass().getSimpleName()));
        }
    }

    @Override
    public void start(final Listener listener) {
        this.init();
        if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
            new ServiceException(String.format("service %s is Starting", this.getClass().getSimpleName()));
        }

        if (Utils.useNettyEpoll()) {
            createEpollServer(listener);
        } else {
            createNioServer(listener);
        }
    }

    private void createEpollServer(Listener listener) {
        if (getBossGroup() == null) {
            EpollEventLoopGroup bossEpollEventLoopGroup = new EpollEventLoopGroup(getBossThreadNum(),getBossThreadFactory());
            bossEpollEventLoopGroup.setIoRatio(100);
            this.bossGroup = bossEpollEventLoopGroup;

        }
        if (getWorkGroup() == null) {
            EpollEventLoopGroup workEpollEventLoopGroup = new EpollEventLoopGroup(getBossThreadNum(),getBossThreadFactory());
            workEpollEventLoopGroup.setIoRatio(getIoRate());
            this.workGroup = workEpollEventLoopGroup;
        }

        createServer(listener, this.bossGroup, this.workGroup, getChannelFactory());
    }

    private void createNioServer(Listener listener) {

        if (getBossGroup() == null) {
            NioEventLoopGroup bossNioEventLoopGroup = new NioEventLoopGroup(getBossThreadNum(),getBossThreadFactory(),getSelectorProvider());
            bossNioEventLoopGroup.setIoRatio(100);
            this.bossGroup = bossNioEventLoopGroup;

        }
        if (getWorkGroup() == null) {
            NioEventLoopGroup workNioEventLoopGroup = new NioEventLoopGroup(getWorkThreadNum(),getWorkThreadFactory(),getSelectorProvider());
            workNioEventLoopGroup.setIoRatio(getIoRate());
            this.workGroup = workNioEventLoopGroup;
        }

        createServer(listener, this.bossGroup, this.workGroup, getChannelFactory());
        
        

    }

    private void createServer(Listener listener, EventLoopGroup bossGroup, EventLoopGroup workGroup, ChannelFactory<? extends ServerChannel> channelFactory) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channelFactory(channelFactory);
            serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    initPipLine(channel.pipeline());
                }
            });
            initOptions(serverBootstrap);

            InetSocketAddress inetSocketAddress = StringUtils.isBlank(this.host) ? new InetSocketAddress(this.port) : new InetSocketAddress(this.host, this.port);
            serverBootstrap.bind(inetSocketAddress).addListener(future -> {
                if (future.isSuccess()) {
                    serverState.set(State.Started);
                    logger.info("server {} start success on:{}", this.getClass().getName(), this.port);
                    if (listener != null) {
                        listener.onSuccess(this.port);
                    }
                } else {
                    logger.error("server {} start failure on:{}", this.getClass().getName(), this.port);
                    if (listener != null) {
                        listener.onFailure(future.cause());
                    }
                }
            });
        } catch (Exception e) {
            logger.error("server {} start failure on:{}", this.getClass().getName(), this.port);
            if (listener != null) {
                listener.onFailure(e);
            }
            throw new ServiceException(String.format("server start exception, port =" + this.port, e));
        }
    }

    protected void initOptions(ServerBootstrap serverBootstrap) {
        serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//TODO 带研究
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//TODO 带研究
    }


    protected void initPipLine(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
        pipeline.addLast("handler", getChannelHandler());
    }


    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public AtomicReference<State> getServerState() {
        return serverState;
    }


    /**
     * 可被子类重写
     *
     * @return
     */
    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    /**
     * 可被子类重写
     *
     * @return
     */
    public EventLoopGroup getWorkGroup() {
        return workGroup;
    }
}
