package com.caisheng.cheetah.client.gateway;

import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.client.gateway.handler.GatewayClientChannelHandler;
import com.caisheng.cheetah.client.gateway.handler.GatewayErrorHandler;
import com.caisheng.cheetah.client.gateway.handler.GatewayOkHandler;
import com.caisheng.cheetah.common.MessageDispatcher;
import com.caisheng.cheetah.network.netty.client.NettyTCPClient;
import com.caisheng.cheetah.network.netty.connection.NettyConnectionManager;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.thread.NamedPoolThreadFactory;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class GatewayClient extends NettyTCPClient {
    private final GatewayClientChannelHandler handler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;
    private final ConnectionManager connectionManager;
    private final MessageDispatcher messageDispatcher;

    public GatewayClient(CheetahClient cheetahClient) {
        this.messageDispatcher = new MessageDispatcher();
        this.messageDispatcher.register(Command.OK, new GatewayOkHandler(cheetahClient));
        this.messageDispatcher.register(Command.ERROR, new GatewayErrorHandler(cheetahClient));
        this.connectionManager = new NettyConnectionManager();
        this.handler = new GatewayClientChannelHandler(this.connectionManager, this.messageDispatcher);
        if (CC.lion.net.traffic_shaping.gateway_client.enabled) {
            this.trafficShapingExecutor = Executors.newSingleThreadScheduledExecutor(new NamedPoolThreadFactory(ThreadNames.T_TRAFFIC_SHAPING));
            this.trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                    trafficShapingExecutor,
                    CC.lion.net.traffic_shaping.gateway_client.write_global_limit,
                    CC.lion.net.traffic_shaping.gateway_client.read_global_limit,
                    CC.lion.net.traffic_shaping.gateway_client.write_channel_limit,
                    CC.lion.net.traffic_shaping.gateway_client.read_channel_limit,
                    CC.lion.net.traffic_shaping.gateway_client.check_interval);
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.handler;
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (this.trafficShapingHandler != null) {
            pipeline.addLast(this.trafficShapingHandler);
        }
    }

    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        if (CC.lion.net.snd_buf.gateway_client > 0) b.option(ChannelOption.SO_SNDBUF, CC.lion.net.snd_buf.gateway_client);//cny_note 设置发送缓冲区大小
        if (CC.lion.net.rcv_buf.gateway_client > 0) b.option(ChannelOption.SO_RCVBUF, CC.lion.net.rcv_buf.gateway_client);//cny_note 设置接收缓冲区大小
    }

    @Override
    public SelectorProvider getSelectorProvider() {
        if (CC.lion.net.tcpGateway()) return super.getSelectorProvider();
        if (CC.lion.net.udtGateway()) return NioUdtProvider.BYTE_PROVIDER;
        if (CC.lion.net.sctpGateway()) return super.getSelectorProvider();
        return super.getSelectorProvider();
    }

    @Override
    protected int getWorkThreadNum() {
        return CC.lion.thread.pool.gateway_client_work;
    }



    @Override
    public void stop(Listener listener) {
        if (this.trafficShapingHandler != null) {
            this.trafficShapingHandler.release();
            this.trafficShapingExecutor.shutdown();
        }
        super.stop(listener);
    }
}
