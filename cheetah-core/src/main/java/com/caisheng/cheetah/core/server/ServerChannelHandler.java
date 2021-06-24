package com.caisheng.cheetah.core.server;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.ConnectionManager;
import com.caisheng.cheetah.api.message.PacketReceiver;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.network.netty.connection.NettyConnection;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter{
    private Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);
    private boolean security;
    private ConnectionManager connectionManager;
    private PacketReceiver packetReceiver;

    public ServerChannelHandler(boolean security, ConnectionManager connectionManager, PacketReceiver packetReceiver) {
        this.security = security;
        this.connectionManager = connectionManager;
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("connect connected conne={}",ctx.channel());
        Connection connection= new NettyConnection();
        connection.init(ctx.channel(), true);
        this.connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = this.connectionManager.get(ctx.channel());
        this.connectionManager.removeAndClose(ctx.channel());
        Logs.CONN.info("connect disconnected conn={}",connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        try{
//            Profiler.start("time cost on [channel read]: ", packet.toString());//TODO
            Connection connection = this.connectionManager.get(ctx.channel());
            connection.updateLastReadTime();
            logger.debug("channelRead conn={},packet={}",connection,packet);
            this.packetReceiver.onReceive(packet, connection);
        }finally {
            /*Profiler.release();//TODO
            if (Profiler.getDuration() > profile_slowly_limit) {
                Logs.PROFILE.info("Read Packet[cmd={}] Slowly: \n{}", Command.toCMD(cmd), Profiler.dump());
            }
            Profiler.reset();*/
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = this.connectionManager.get(ctx.channel());
        Logs.CONN.error("connect caught ex,conn={}", connection);
        logger.error("caught an ex,channel={},conn={}",ctx.channel(),connection,cause);
        ctx.close();
    }
}
