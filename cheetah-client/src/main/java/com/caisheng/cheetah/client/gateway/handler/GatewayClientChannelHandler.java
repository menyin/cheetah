package com.caisheng.cheetah.client.gateway.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.api.event.ConnectionCloseEvent;
import com.caisheng.cheetah.api.event.ConnectionConnectEvent;
import com.caisheng.cheetah.api.message.PacketReceiver;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.MessageDispatcher;
import com.caisheng.cheetah.network.netty.connection.NettyConnection;
import com.caisheng.cheetah.tools.event.EventBus;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayClientChannelHandler extends ChannelInboundHandlerAdapter{
    private static final Logger logger = LoggerFactory.getLogger(GatewayClientChannelHandler.class);
    private final ConnectionManager connectionManager;
    private final PacketReceiver packetReceiver;

    public GatewayClientChannelHandler(ConnectionManager connectionManager, PacketReceiver packetReceiver) {
        this.connectionManager = connectionManager;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("connect connected conn={}",ctx.channel());
        NettyConnection nettyConnection = new NettyConnection();
        nettyConnection.init(ctx.channel(),false);
        this.connectionManager.add(nettyConnection);
        EventBus.post(new ConnectionConnectEvent(nettyConnection));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Logs.CONN.info("receive gateway packet={},channel={}",msg,ctx.channel());
        Packet packet = (Packet)msg;
        packetReceiver.onReceive(packet,this.connectionManager.get(ctx.channel()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = this.connectionManager.get(ctx.channel());
        Logs.CONN.error("connect cause ex ,connection={}",connection);
        logger.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("connect disconnected conn={}", connection);
    }
}
