package com.caisheng.cheetah.core.server;

import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.common.MessageDispatcher;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.handler.GatewayPushHandler;
import com.caisheng.cheetah.network.netty.server.NettyTcpServer;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.thread.NamedThreadFactory;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.caisheng.cheetah.tools.config.CC.lion.net.gateway_server_bind_ip;
import static com.caisheng.cheetah.tools.config.CC.lion.net.gateway_server_port;
import static com.caisheng.cheetah.tools.config.CC.lion.net.write_buffer_water_mark.*;

public class GatewayServer extends NettyTcpServer {
    private ServerChannelHandler serverChannelHandler;
    private ConnectionManager connectionManager;
    private MessageDispatcher messageDispatcher;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;
    private CheetahServer cheetahServer;

    public GatewayServer(CheetahServer cheetahServer) {
        super(gateway_server_port, gateway_server_bind_ip);
        this.cheetahServer=cheetahServer;
        this.messageDispatcher = new MessageDispatcher();
        this.connectionManager = new ServerConnectionManager(false);
        this.serverChannelHandler = new ServerChannelHandler(false, this.connectionManager, this.messageDispatcher);
    }

    @Override
    public void init() {
        super.init();
        this.messageDispatcher.register(Command.GATEWAY_PUSH,()->new GatewayPushHandler(this.cheetahServer.getPushCenter()));

        if (CC.lion.net.traffic_shaping.connect_server.enabled) {//启用流量整形，限流
            trafficShapingExecutor= Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(ThreadNames.T_TRAFFIC_SHAPING));

            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                    trafficShapingExecutor,
                    CC.lion.net.traffic_shaping.connect_server.write_global_limit,
                    CC.lion.net.traffic_shaping.connect_server.read_global_limit,
                    CC.lion.net.traffic_shaping.connect_server.write_channel_limit,
                    CC.lion.net.traffic_shaping.connect_server.read_channel_limit,
                    CC.lion.net.traffic_shaping.connect_server.check_interval
            );
        }
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
            trafficShapingExecutor.shutdown();
        }
        if (connectionManager != null) {
            connectionManager.destroy();
        }
    }
    @Override
    protected void initOptions(ServerBootstrap serverBootstrap) {
        super.initOptions(serverBootstrap);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        if (CC.lion.net.snd_buf.connect_server > 0) {
            serverBootstrap.option(ChannelOption.SO_SNDBUF, CC.lion.net.snd_buf.connect_server);
        }
        if (CC.lion.net.rcv_buf.connect_server > 0) {
            serverBootstrap.option(ChannelOption.SO_RCVBUF, CC.lion.net.rcv_buf.connect_server);
        }

        //设置高低水位
        if (gateway_server_low > 0 && gateway_server_high > 0) {
            serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                    connect_server_low, connect_server_high
            ));
        }

    }

    @Override
    protected void initPipLine(ChannelPipeline pipeline) {
        super.initPipLine(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addFirst(trafficShapingHandler);
        }
    }


    @Override
    public ChannelFactory<? extends ServerChannel> getChannelFactory() {
        if (CC.lion.net.tcpGateway()) return super.getChannelFactory();
        if (CC.lion.net.udtGateway()) return NioUdtProvider.BYTE_ACCEPTOR;
        if (CC.lion.net.sctpGateway()) return NioSctpServerChannel::new;
        return super.getChannelFactory();
    }

    @Override
    public SelectorProvider getSelectorProvider() {
        if (CC.lion.net.tcpGateway()) return super.getSelectorProvider();
        if (CC.lion.net.udtGateway()) return NioUdtProvider.BYTE_PROVIDER;
        if (CC.lion.net.sctpGateway()) return super.getSelectorProvider();
        return super.getSelectorProvider();
    }
    @Override
    protected ChannelHandler getChannelHandler() {
        return this.serverChannelHandler;
    }
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
