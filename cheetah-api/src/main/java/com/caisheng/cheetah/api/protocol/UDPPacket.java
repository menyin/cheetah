package com.caisheng.cheetah.api.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class UDPPacket extends Packet{
    private InetSocketAddress address;
    public UDPPacket(byte cmd,InetSocketAddress address) {
        super(cmd);
        this.address=address;
    }
    public UDPPacket(byte cmd,int sessionId,InetSocketAddress address) {
        super(cmd,sessionId);
        this.address=address;
    }

    public UDPPacket(Command cmd) {
        super(cmd);
    }

    public UDPPacket(Command cmd,int sessionId) {
        super(cmd,sessionId);
    }
    public UDPPacket(Command cmd,int sessionId,InetSocketAddress address) {
        super(cmd,sessionId);
    }

    @Override
    public InetSocketAddress sender() {
        return address;
    }

    @Override
    public void setRecipient(InetSocketAddress recipient) {
        this.address = recipient;
    }

    @Override
    public Packet response(Command command) {
        return new UDPPacket(command, sessionId, address);
    }
    @Override
    public Object toFrame(Channel channel) {//？？为什么要传递参数 ！给子类继承，参数只为兼容适配
        int capacity= cmd == Command.HEARTBEAT.getCmd()? 1: HEADER_LEN+this.getBodyLength();
        ByteBuf buffer = channel.alloc().buffer(capacity, capacity);
        encodePacket(this, buffer);
        return new DatagramPacket(buffer,sender());
    }
}
