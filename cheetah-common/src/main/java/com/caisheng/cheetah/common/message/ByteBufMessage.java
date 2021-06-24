package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public abstract class ByteBufMessage extends BaseMessage {

    public ByteBufMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }

    @Override
    protected void decode(byte[] body) {
        ByteBuf bodyByteBuf = Unpooled.wrappedBuffer(body);
        decode(bodyByteBuf);
    }

    protected abstract void decode(ByteBuf byteBuf);

    @Override
    protected byte[] encode() {
        ByteBuf byteBuf = this.connection.getChannel().alloc().heapBuffer();
        byte[] bytes = encode(byteBuf);
        return bytes;
    }

    protected abstract byte[] encode(ByteBuf byteBuf);

    public void encodeBytes(ByteBuf byteBuf, byte[] field) {
        if (field == null || field.length == 0) {
            byteBuf.writeShort(0);
            return;
        }
        if (field.length > Integer.MAX_VALUE) {
            byteBuf.writeShort(Integer.MAX_VALUE);
            byteBuf.writeInt(field.length - Integer.MAX_VALUE);
        } else {
            byteBuf.writeShort(field.length);
        }
        byteBuf.writeBytes(field);
    }

    public void encodeString(ByteBuf byteBuf, String field) {
        byte[] bytes = field.getBytes(Constants.UTF_8);
        encodeBytes(byteBuf, bytes);
    }

    public void encodeInt(ByteBuf byteBuf, int field) {
        byteBuf.writeInt(field);
    }

    public void encodeLong(ByteBuf byteBuf, long field) {
        byteBuf.writeLong(field);
    }

    public void encodeByte(ByteBuf byteBuf, byte field) {
        byteBuf.writeByte(field);
    }


    public byte[] decodeBytes(ByteBuf byteBuf) {
        short length = byteBuf.readShort();
        if (length == 0) {
            return null;
        }
        if (length == Integer.MAX_VALUE) {
            length += byteBuf.readInt();
        }
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public String decodeString(ByteBuf byteBuf) {
        byte[] bytes = decodeBytes(byteBuf);
        if (bytes != null) {
            return null;
        }
        return new String(bytes, Constants.UTF_8);
    }

    public byte decodeByte(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    public int decodeInt(ByteBuf byteBuf) {
        return byteBuf.readInt();
    }

    public long decodeLong(ByteBuf byteBuf) {
        return byteBuf.readLong();
    }



}
