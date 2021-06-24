package com.caisheng.cheetah.common.message.gateway;

import com.alibaba.fastjson.TypeReference;
import com.caisheng.cheetah.api.common.Condition;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.push.IPushMessage;
import com.caisheng.cheetah.common.condition.AwaysPassCondition;
import com.caisheng.cheetah.common.condition.ScriptCondition;
import com.caisheng.cheetah.common.condition.TagsCondition;
import com.caisheng.cheetah.common.memory.PacketFactory;
import com.caisheng.cheetah.common.message.ByteBufMessage;
import com.caisheng.cheetah.tools.Jsons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

import java.util.Set;

public class GatewayPushMessage extends ByteBufMessage implements IPushMessage {
    private String userId;
    private int clientType;
    private int timeout;
    private byte[] content;

    private String taskId;
    private Set<String> tags;
    private String condition;

    public GatewayPushMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }


    public static GatewayPushMessage build(Connection connection) {
        Packet packet= PacketFactory.get(Command.GATEWAY_PUSH);
        packet.setSessionId(genSessionId());
        return new GatewayPushMessage(packet,connection);
    }


    @Override
    protected void decode(ByteBuf byteBuf) {
        this.userId = decodeString(byteBuf);
        this.clientType = decodeInt(byteBuf);
        this.timeout = decodeInt(byteBuf);
        this.content = decodeBytes(byteBuf);
        this.taskId = decodeString(byteBuf);
        this.tags = decodeSet(byteBuf);
        this.condition = decodeString(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeString(byteBuf, this.userId);
        encodeInt(byteBuf, this.clientType);
        encodeInt(byteBuf, this.timeout);
        encodeBytes(byteBuf, this.content);
        encodeString(byteBuf, this.taskId);
        encodeSet(byteBuf, this.tags);
        encodeString(byteBuf, this.condition);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "GatewayPushMessage{" +
                "userId='" + userId + '\'' +
                ", clientType='" + clientType + '\'' +
                ", timeout='" + timeout + '\'' +
                ", content='" + (content == null ? 0 : content.length) + '\'' +
                '}';
    }

    @Override
    public boolean isBroadcast() {
        return this.userId==null;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public int getClientType() {
        return this.clientType;
    }

    @Override
    public byte[] getContent() {
        return this.content;
    }

    @Override
    public boolean isNeedAck() {
        return this.packet.hasFlag(Packet.FLAG_AUTO_ACK)||this.packet.hasFlag(Packet.FLAG_BIZ_ACK);
    }

    @Override
    public byte getFlags() {
        return this.packet.getFlags();
    }

    @Override
    public int getTimeoutMills() {
        return this.timeout;
    }

    @Override
    public String getTaskId() {
        return this.taskId;
    }

    @Override
    public Condition getCondition() {
        if (this.condition != null) {
            return new ScriptCondition(this.condition);
        }
        if (this.tags != null) {
            return new TagsCondition(this.tags);
        }
        return AwaysPassCondition.I;
    }

    @Override
    public void finalized() {
        this.content=null;
        this.condition=null;
        this.tags=null;

    }
    private Set<String> decodeSet(ByteBuf body) {
        String json = decodeString(body);
        if (json == null) return null;
        return Jsons.fromJson(json, new TypeReference<Set<String>>() {
        }.getType());
    }

    private void encodeSet(ByteBuf body, Set<String> field) {
        String json = field == null ? null : Jsons.toJson(field);
        encodeString(body, json);
    }


    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public void send(ChannelFutureListener channelFutureListener) {
        super.sendRaw(channelFutureListener);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void addFlag(byte flag) {
        packet.addFlag(flag);
    }
}
