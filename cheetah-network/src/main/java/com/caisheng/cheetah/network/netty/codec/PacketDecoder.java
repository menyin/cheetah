package com.caisheng.cheetah.network.netty.codec;

import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.tools.config.CC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;


/**
 * TODO
 */
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        decodeHeartBeat(byteBuf, list);
        decodeFrames(byteBuf, list);
    }

    private void decodeHeartBeat(ByteBuf byteBuf, List<Object> list) {
        while (byteBuf.isReadable()) {
            if (byteBuf.readByte() == Packet.HB_PACKET_BYTE) {
                list.add(Packet.HB_PACKET);
            } else {
                byteBuf.readerIndex(byteBuf.readerIndex() - 1);
            }
        }
    }
    private void decodeFrames(ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() > Packet.HEADER_LEN) {
            byteBuf.markReaderIndex();
            Packet packet=decodeFrame(byteBuf);
            if (packet != null) {
                list.add(packet);
            }else{
                byteBuf.resetReaderIndex();
            }

        }
    }

    private Packet decodeFrame(ByteBuf byteBuf) {
        int readableBytes = byteBuf.readableBytes();
        int bodyLength = byteBuf.readInt();
        if (readableBytes < bodyLength + Packet.HEADER_LEN) {
            return null;
        }
        if (bodyLength> CC.lion.core.max_packet_size){
            throw new TooLongFrameException("packet body length over limit " + bodyLength);
        }
        return Packet.decodePacket(new Packet(byteBuf.readByte()),byteBuf,bodyLength);
    }

}
