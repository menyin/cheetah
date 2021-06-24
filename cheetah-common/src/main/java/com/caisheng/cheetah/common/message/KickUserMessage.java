package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.JsonPacket;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class KickUserMessage extends ByteBufMessage {
    private String deviceId;
    private String userId;

    public KickUserMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public static KickUserMessage build(Connection connection){
        if (connection.getSessionConext().isSecurity()) {
            return new KickUserMessage(new Packet(Command.KICK), connection);
        } else {
            return new KickUserMessage(new JsonPacket(Command.KICK), connection);
        }
    }

    @Override
    protected void decode(ByteBuf byteBuf) {
        this.deviceId = decodeString(byteBuf);
        this.userId = decodeString(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeString(byteBuf, this.deviceId);
        encodeString(byteBuf, this.userId);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    protected Map<String, Object> encodeJsonBody() {
        Map<String, Object> body = new HashMap<>(2);
        body.put("deviceId", deviceId);
        body.put("userId", userId);
        return body;
    }

    @Override
    public String toString() {
        return "KickUserMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
