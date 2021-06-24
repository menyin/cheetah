package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class HandshakeOkMessage extends ByteBufMessage {
    private byte[] serverKey;
    private int heartbeat;
    private String sessionId;
    private long expireTime;

    public HandshakeOkMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }


    @Override
    protected void decode(ByteBuf byteBuf) {
        this.serverKey = decodeBytes(byteBuf);
        this.heartbeat = decodeInt(byteBuf);
        this.sessionId = decodeString(byteBuf);
        this.expireTime = decodeLong(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeBytes(byteBuf,this.serverKey);
        encodeInt(byteBuf,this.heartbeat);
        encodeString(byteBuf,this.sessionId);
        encodeLong(byteBuf,this.expireTime);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "HandshakeOkMessage{" +
                "expireTime=" + expireTime +
                ", serverKey=" + Arrays.toString(serverKey) +
                ", heartbeat=" + heartbeat +
                ", sessionId='" + sessionId + '\'' +
                ", packet=" + packet +
                '}';
    }



    public byte[] getServerKey() {
        return serverKey;
    }

    public void setServerKey(byte[] serverKey) {
        this.serverKey = serverKey;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
