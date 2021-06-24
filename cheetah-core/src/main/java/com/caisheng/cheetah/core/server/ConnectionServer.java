package com.caisheng.cheetah.core.server;

import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.spi.handler.PushHandlerFactory;
import com.caisheng.cheetah.common.MessageDispatcher;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.handler.*;
import com.caisheng.cheetah.network.netty.server.NettyTcpServer;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.thread.NamedThreadFactory;
import com.caisheng.cheetah.tools.thread.ThreadNames;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.caisheng.cheetah.tools.config.CC.lion.net.write_buffer_water_mark.connect_server_high;
import static com.caisheng.cheetah.tools.config.CC.lion.net.write_buffer_water_mark.connect_server_low;

public class ConnectionServer extends NettyTcpServer {
    private ChannelHandler channelHandler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;//netty自带的限流整形工具
    private ScheduledExecutorService trafficShapingExecutor;
    private MessageDispatcher messageDispatcher;//消息分发器，类似于SpringMvc那个dispatcher
    private ConnectionManager connectionManager;
    private CheetahServer cheetahServer;


    public ConnectionServer(CheetahServer cheetahServer) {
        super(CC.lion.net.connect_server_port, CC.lion.net.connect_server_bind_ip);//
        this.cheetahServer = cheetahServer;
        //cny_wait
        this.connectionManager = new ServerConnectionManager(true);
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new ServerChannelHandler(true, this.connectionManager, this.messageDispatcher);
    }

    @Override
    public void init() {
        super.init();
        messageDispatcher.register(Command.HEARTBEAT, HeartbeatHandler::new);//？？这个应该是给骑手客户端做ping-pong检查的
        messageDispatcher.register(Command.HANDSHAKE, () -> new HandshakeHandler(this.cheetahServer));
        messageDispatcher.register(Command.BIND, () -> new BindUserHandler(this.cheetahServer));
        messageDispatcher.register(Command.UNBIND, () -> new BindUserHandler(this.cheetahServer));
        messageDispatcher.register(Command.FAST_CONNECT, () -> new FastConnectHandler(this.cheetahServer));
        messageDispatcher.register(Command.PUSH, PushHandlerFactory::create); //？？接收骑手用户推送的消息 ？这种消息应该是商家推送过来的
        messageDispatcher.register(Command.ACK, () -> new AckHandler(this.cheetahServer)); //？？这个消息类型是做什么用的 ！骑手客户端接收到推送消息后给服务端返回的应答消息
        //messageDispatcher.register(Command.HTTP_PROXY, () -> new HttpProxyHandler(this.cheetahServer), CC.lion.http.proxy_enabled);//？？ TODO
        if (CC.lion.net.traffic_shaping.connect_server.enabled) {//启用流量整形，限流
            trafficShapingExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(ThreadNames.T_TRAFFIC_SHAPING));

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
    protected void initPipLine(ChannelPipeline pipeline) {
        super.initPipLine(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addLast(trafficShapingHandler);
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
        serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                connect_server_low, connect_server_high
        ));

    }

    @Override
    protected ChannelHandler getChannelHandler() {
        return this.channelHandler;
    }
}
