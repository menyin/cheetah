package com.caisheng.cheetah.api.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public interface Message {
    Connection getConnection();
    void decodeBody();
    void encodeBody();
    void send(ChannelFutureListener channelFutureListener);//会对Packet.body做压缩加密
    void sendRaw(ChannelFutureListener channelFutureListener);//不会对Packet.body做压缩加密
    Packet getPacket();
}
