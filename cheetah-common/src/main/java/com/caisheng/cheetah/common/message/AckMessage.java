package com.caisheng.cheetah.common.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;

public class AckMessage extends BaseMessage {

    public AckMessage(Packet packet, Connection connection) {
        super(packet, connection);
    }
    public static AckMessage from(BaseMessage src) {
        return new AckMessage(new Packet(Command.ACK,src.getPacket().getSessionId()),src.getConnection());
    }
    @Override
    protected void decode(byte[] body) {

    }

    @Override
    protected byte[] encode() {
        return null;
    }



    @Override
    public String toString() {
        return "AckMessage{" +
                "packet=" + packet +
                '}';
    }
}
