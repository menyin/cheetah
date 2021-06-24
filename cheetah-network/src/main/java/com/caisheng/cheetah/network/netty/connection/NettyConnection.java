package com.caisheng.cheetah.network.netty.connection;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.core.RsaCipherFactory;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO
public class NettyConnection implements Connection ,ChannelFutureListener{
    private Logger logger = LoggerFactory.getLogger(NettyConnection.class);

    private Channel channel;
    private boolean security;
    private long lastReadTime;
    private long lastWriteTime;
    private SessionContext sessionContext;
    private volatile byte status = STATUS_NEW;

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.security = security;
//        this.lastReadTime = System.currentTimeMillis();
        this.status = STATUS_CONNECTED;
        this.sessionContext = new SessionContext();
        this.security = security;
        if (this.security) {
            this.sessionContext.setCipher(RsaCipherFactory.create());
        }
    }


    @Override
    public ChannelFuture send(Packet packet) {
        return send(packet, null);
    }

    @Override
    public ChannelFuture send(Packet packet, ChannelFutureListener channelFutureListener) {
        if (this.channel.isActive()) {
            ChannelFuture channelFuture = channel.writeAndFlush(packet).addListener(this);
            if (channelFutureListener != null) {
                channelFuture.addListener(channelFutureListener);
            }
            if (this.channel.isWritable()) {
                return channelFuture;
            }

            if (!this.channel.eventLoop().inEventLoop()) {
                channelFuture.awaitUninterruptibly(100);
            }
            return channelFuture;
        }else{
            return this.close();
        }
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > sessionContext.getHeartbeat()+ 1000;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > sessionContext.getHeartbeat()- 1000;
    }

    @Override
    public String getId() {
        return this.channel.id().asShortText();
    }

    @Override
    public ChannelFuture close() {
        if (status == STATUS_DISCONNECTED) {
            return null;
        }
        return this.channel.close();
    }

    @Override
    public boolean isConnected() {
        return this.status == STATUS_CONNECTED;
    }

    @Override
    public void updateLastReadTime() {
        this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void updateLastWriteTime() {
        this.lastWriteTime= System.currentTimeMillis();
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public SessionContext getSessionConext() {
        return this.sessionContext;
    }

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            this.updateLastWriteTime();
        }else{
            logger.error("connection send msg error.",channelFuture.cause());
            Logs.CONN.error("connection send msg error ={},conn={}",channelFuture.cause().getMessage(),this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NettyConnection that = (NettyConnection) o;

        return channel.id().equals(that.channel.id());
    }

    @Override
    public int hashCode() {
        return channel.id().hashCode();
    }

    @Override
    public String toString() {
        return "[channel=" + this.channel
                + ", sessionContext=" + this.sessionContext
                + ", status=" + this.status
                + ", lastReadTime=" + this.lastReadTime
                + ", lastWriteTime=" + this.lastWriteTime
                + "]";
    }

}
