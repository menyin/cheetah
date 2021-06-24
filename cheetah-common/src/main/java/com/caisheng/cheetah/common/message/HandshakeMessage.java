package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Cipher;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.core.RsaCipherFactory;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class HandshakeMessage extends ByteBufMessage {
    private String deviceId;
    private String osName;
    private String osVersion;
    private String clientVersion;
    private byte[] iv;//AES密钥向量16位
    private byte[] clientKey;//16位客户端随机数
    private int minHeartbeat;
    private int maxHeartbeat;
    private long timestamp;


    public HandshakeMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public HandshakeMessage from(BaseMessage baseMessage) {

        return null;
    }

    @Override
    protected void decode(ByteBuf byteBuf) {
        this.deviceId = decodeString(byteBuf);
        this.osName = decodeString(byteBuf);
        this.osVersion = decodeString(byteBuf);
        this.clientVersion = decodeString(byteBuf);
        this.iv = decodeBytes(byteBuf);
        this.clientKey = decodeBytes(byteBuf);
        this.minHeartbeat = decodeInt(byteBuf);
        this.maxHeartbeat = decodeInt(byteBuf);
        this.timestamp = decodeLong(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeString(byteBuf, deviceId);
        encodeString(byteBuf, osName);
        encodeString(byteBuf, osVersion);
        encodeString(byteBuf, clientVersion);
        encodeBytes(byteBuf, iv);
        encodeBytes(byteBuf, clientKey);
        encodeInt(byteBuf, minHeartbeat);
        encodeInt(byteBuf, maxHeartbeat);
        encodeLong(byteBuf, timestamp);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        deviceId = (String) body.get("deviceId");
        osName = (String) body.get("osName");
        osVersion = (String) body.get("osVersion");
        clientVersion = (String) body.get("clientVersion");
    }
    @Override
    protected Cipher getCipher() {
        return RsaCipherFactory.create();
    }

    @Override
    public String toString() {
        return null;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getClientKey() {
        return clientKey;
    }

    public void setClientKey(byte[] clientKey) {
        this.clientKey = clientKey;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
