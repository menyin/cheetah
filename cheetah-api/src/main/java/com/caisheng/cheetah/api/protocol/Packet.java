package com.caisheng.cheetah.api.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class Packet {
    //常量
    public static final int HEADER_LEN = 13;

    public static final byte FLAG_CRYPTO = 1;
    public static final byte FLAG_COMPRESS = 2;
    public static final byte FLAG_BIZ_ACK = 4;
    public static final byte FLAG_AUTO_ACK = 8;
    public static final byte FLAG_JSON_BODY = 16;

    public static final byte HB_PACKET_BYTE = -13;
    public static final byte[] HB_PACKET_BYTES = new byte[]{HB_PACKET_BYTE};
    public static final Packet HB_PACKET = new Packet(Command.HEARTBEAT.getCmd());


    //成员变量
    //private int bodyLength; 从字节流解析后就是body.length，所以此处不用一个成员变量
    protected byte cmd;
    protected short checkCode;
    protected byte flags;
    protected int sessionId;
    transient protected byte lrc;
    transient private byte[] body;

    public Packet(byte cmd) {
        this.cmd = cmd;
    }

    public Packet(byte cmd, int sessionId) {
        this.cmd = cmd;
        this.sessionId = sessionId;
    }

    public Packet(Command cmd, int sessionId) {
        this.cmd = cmd.getCmd();
        this.sessionId = sessionId;
    }


    public int getBodyLength() {
        return body == null ? 0 : body.length;
    }

    /**
     * cny_note 只能用于0置为1操作，如果要1置为0 则需要用removeFlag
     *
     * @param flag
     */
    public void addFlag(byte flag) {
        flags |= flag;
    }

    /**
     * cny_note 只能用于1置为0操作，如果要0置为1 则需要用addFlag
     *
     * @param flag
     */
    public void removeFlag(byte flag) {
        this.flags &= ~flag;
    }

    public boolean hasFlag(byte flag) {
        return (this.flags & flag) != 0;
    }

    public short calcCheckCode() {
        short checkCode = 0;
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                checkCode += (body[i] & 0x0ff); //cny_note
            }
        }
        return checkCode;
    }

    public boolean validCheckCode() {
        return calcCheckCode() == this.checkCode;
    }

    public byte calcLrc() {
        byte[] data = Unpooled.buffer(HEADER_LEN - 1)
                .writeInt(getBodyLength())
                .writeByte(cmd)
                .writeShort(checkCode)
                .writeByte(flags)
                .writeInt(sessionId)
                .array();
        byte lrc = 0;
        for (int i = 0; i < data.length; i++) {
            lrc ^= data[i];
        }
        return lrc;
    }

    public boolean validLrc() {
        return (lrc ^ calcLrc()) == 0;
    }

    public InetSocketAddress getRecipient() {
        return null;
    }

    public void setRecipient(InetSocketAddress recipient) {
    }


    public static Packet decodePacket(Packet packet, ByteBuf byteBuf, int bodyLength) {
        packet.setCheckCode(byteBuf.readShort());
        packet.setFlags(byteBuf.readByte());
        packet.setSessionId(byteBuf.readInt());
        packet.setLrc(byteBuf.readByte());
        if (bodyLength > 0) {
            packet.setBody(byteBuf.readBytes(bodyLength).array());
        }
        return packet;
    }

    public static void encodePacket(Packet packet, ByteBuf byteBuf) {
        if (packet.getCmd() == Packet.HB_PACKET_BYTE) {
            byteBuf.writeByte(Packet.HB_PACKET_BYTE);
        } else {
            byteBuf.writeInt(packet.getBodyLength());
            byteBuf.writeByte(packet.getCmd());
            byteBuf.writeShort(packet.getCheckCode());
            byteBuf.writeByte(packet.getFlags());
            byteBuf.writeInt(packet.getSessionId());
            byteBuf.writeByte(packet.getLrc());
            if (packet.getBodyLength() > 0) {
                byteBuf.writeBytes((byte[]) packet.getBody());//此处
            }
        }
        packet.body = null;

    }

    public Object toFrame(Channel channel) {//？？为什么要传递参数 ！给子类继承，参数只为兼容适配
        return this;
    }

    /**
     * cny_note 返回携带cmd和sessionId的响应Packet
     * @param command
     * @return
     */
    public Packet response(Command command) {
        return new Packet(command, sessionId);
    }

    @Override
    public String toString() {
        return "{cmd=" + cmd +
                ",checkCode=" + checkCode +
                ",flags=" + flags +
                ",sessionId=" + sessionId +
                ",lrc=" + lrc +
                "}";
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public short getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(short checkCode) {
        this.checkCode = checkCode;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public byte getLrc() {
        return lrc;
    }

    public void setLrc(byte lrc) {
        this.lrc = lrc;
    }

    public <T> T getBody() {
        return (T)body;
    }

    public <T> void setBody(T body) {
        this.body = (byte[]) body;
    }

    public InetSocketAddress sender() {
        return null;
    }
}
