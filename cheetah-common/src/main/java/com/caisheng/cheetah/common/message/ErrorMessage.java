package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessage extends ByteBufMessage {
    private byte cmd;
    private byte code;
    private String reason;
    private String data;

    public ErrorMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public ErrorMessage(byte cmd, Packet packet, Connection connection) {
        super(packet, connection);
        this.cmd = cmd;
    }

    /**
     * cny_note 实际上应该是基于一个xxxMessage消息转为ErrorMessage
     * 主要就是保持xxxMessage原有一些属性，只改变packet.cmd为ERROR
     * @param src
     * @return
     */
    public static ErrorMessage from(BaseMessage src) {
        return new ErrorMessage(src.getPacket().getCmd(), src.getPacket().response(Command.ERROR), src.getConnection());
    }
    public static ErrorMessage from(Packet packet,Connection connection) {
        return new ErrorMessage(packet.getCmd(), packet.response(Command.ERROR), connection);
    }



    @Override
    protected void decode(ByteBuf byteBuf) {
        this.cmd = decodeByte(byteBuf);
        this.code = decodeByte(byteBuf);
        this.reason = decodeString(byteBuf);
        this.data = decodeString(byteBuf);
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeByte(byteBuf, cmd);
        encodeByte(byteBuf, code);
        encodeString(byteBuf, reason);
        encodeString(byteBuf, data);
        return new byte[0];
    }

    @Override
    protected Map<String, Object> encodeJsonBody() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("cmd", cmd);
        map.put("code", code);
        if (reason != null) {
            map.put("reason", reason);
        }
        if (data != null) {
            map.put("data", data);
        }
        return map;
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public void close() {
        super.sendRaw(ChannelFutureListener.CLOSE);
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "reason='" + reason + '\'' +
                ", code=" + code +
                ", data=" + data +
                ", packet=" + packet +
                '}';
    }


    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
