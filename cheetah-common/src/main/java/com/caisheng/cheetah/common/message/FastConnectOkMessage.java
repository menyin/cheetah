package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;


public class FastConnectOkMessage extends ByteBufMessage {
    private int heartbeat;

    public FastConnectOkMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public static FastConnectOkMessage from(BaseMessage src) {
        return new FastConnectOkMessage(src.packet.response(Command.FAST_CONNECT), src.connection);
    }

    @Override
    protected void decode(ByteBuf byteBuf) {
        this.heartbeat = decodeInt(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeInt(byteBuf, this.heartbeat);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "FastConnectOkMessage{" +
                "heartbeat=" + heartbeat +
                ", packet=" + packet +
                '}';
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }
}
