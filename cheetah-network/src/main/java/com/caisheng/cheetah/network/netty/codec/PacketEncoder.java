package com.caisheng.cheetah.network.netty.codec;


import com.caisheng.cheetah.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * TODO
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {
    public static final PacketEncoder INSTANCE = new PacketEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        Packet.encodePacket(msg, out);
    }
}
