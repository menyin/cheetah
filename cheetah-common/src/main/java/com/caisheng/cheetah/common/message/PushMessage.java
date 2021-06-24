package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.JsonPacket;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.channel.ChannelFutureListener;

import java.util.Collections;
import java.util.Map;

public class PushMessage extends BaseMessage {
    private byte[] content;

    public PushMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public static PushMessage build(Connection connection) {
        if (connection.getSessionConext().isSecurity()) {
            return new PushMessage(new Packet(Command.PUSH, genSessionId()), connection);
        } else {
            return new PushMessage(new JsonPacket(Command.PUSH, genSessionId()), connection);
        }
    }

    @Override
    protected void decode(byte[] body) {
        this.content =body;
    }

    @Override
    protected byte[] encode() {
        return this.content;
    }


    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        String content = (String) body.get("content");
        if (content != null) {
            this.content = content.getBytes(Constants.UTF_8);
        }
    }

    @Override
    public Map<String, Object> encodeJsonBody() {
        if (content != null) {
            return Collections.singletonMap("content", new String(content, Constants.UTF_8));
        }
        return null;
    }

    public boolean autoAck() {
        return packet.hasFlag(Packet.FLAG_AUTO_ACK);
    }
    public boolean needAck() {
        return packet.hasFlag(Packet.FLAG_BIZ_ACK) || packet.hasFlag(Packet.FLAG_AUTO_ACK);
    }

    @Override
    public void send(ChannelFutureListener listener) {
        super.send(listener);
        this.content = null;//释放内存
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "content='" + content.length + '\'' +
                ", packet=" + packet +
                '}';
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }
}
