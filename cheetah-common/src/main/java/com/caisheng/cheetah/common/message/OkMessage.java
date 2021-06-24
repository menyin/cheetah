package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class OkMessage extends ByteBufMessage {
    private byte cmd;
    private byte code;
    private String data;

    public OkMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    public OkMessage(byte cmd, Packet packet, Connection connection) {
        super(packet, connection);
        this.cmd=cmd;
    }


    @Override
    protected void decode(ByteBuf byteBuf) {
        this.cmd=decodeByte(byteBuf);
        this.code=decodeByte(byteBuf);
        this.data=decodeString(byteBuf);

    }

    @Override
    protected Map<String, Object> encodeJsonBody() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.cmd>0) {
            map.put("cmd", cmd);
        }
        if (this.code>0) {
            map.put("code", code);
        }
        if (StringUtils.isNotBlank(this.data)) {
            map.put("data", data);
        }

        return map;
    }

    public static OkMessage from(BaseMessage src) {
        return new OkMessage(src.getPacket().getCmd(), src.getPacket().response(Command.OK), src.getConnection());
    }

    @Override
    protected byte[] encode(ByteBuf byteBuf) {
        encodeByte(byteBuf, this.cmd);
        encodeByte(byteBuf, this.code);
        encodeString(byteBuf, this.data);
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public String toString() {
        return "OkMessage{" +
                "data='" + data + '\'' +
                "packet='" + packet + '\'' +
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
