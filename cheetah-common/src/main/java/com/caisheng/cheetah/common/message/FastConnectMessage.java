package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

public class FastConnectMessage extends ByteBufMessage {
    private String sessionId;
    private String deviceId;
    private int minHeartbeat;
    private int maxHeartbeat;

    public FastConnectMessage(Connection connection) {
        super(new Packet(Command.FAST_CONNECT,genSessionId()), connection);
    }

    public FastConnectMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }


    @Override
    protected void decode(ByteBuf byteBuf) {
        this.sessionId = decodeString(byteBuf);
        this.deviceId = decodeString(byteBuf);
        this.minHeartbeat= decodeInt(byteBuf);
        this.maxHeartbeat = decodeInt(byteBuf);

    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeString(byteBuf,this.sessionId);
        encodeString(byteBuf,this.deviceId);
        encodeInt(byteBuf,this.minHeartbeat);
        encodeInt(byteBuf,this.maxHeartbeat);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "FastConnectMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", minHeartbeat=" + minHeartbeat +
                ", maxHeartbeat=" + maxHeartbeat +
                ", packet=" + packet +
                '}';
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getMinHeartbeat() {
        return minHeartbeat;
    }

    public void setMinHeartbeat(int minHeartbeat) {
        this.minHeartbeat = minHeartbeat;
    }

    public int getMaxHeartbeat() {
        return maxHeartbeat;
    }

    public void setMaxHeartbeat(int maxHeartbeat) {
        this.maxHeartbeat = maxHeartbeat;
    }
}
