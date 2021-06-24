package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

public class BindUserMessage extends ByteBufMessage{
    private String userId;
    private String tags;
    private String data;

    public BindUserMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public BindUserMessage(Connection connection) {
        super(new Packet(Command.BIND,genSessionId()), connection);
    }

    @Override
    protected void decode(ByteBuf byteBuf) {
        this.userId = decodeString(byteBuf);
        this.tags = decodeString(byteBuf);
        this.data = decodeString(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeString(byteBuf,this.userId);
        encodeString(byteBuf,this.tags);
        encodeString(byteBuf,this.data);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "data='" + data + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", packet=" + packet +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
