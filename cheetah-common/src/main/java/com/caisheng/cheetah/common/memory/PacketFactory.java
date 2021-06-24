package com.caisheng.cheetah.common.memory;

import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.protocol.UDPPacket;
import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.tools.config.CC;

public interface PacketFactory {
    PacketFactory FACTORY= CC.lion.net.udpGateway()? UDPPacket::new: Packet::new;

    static Packet get(Command command) {
        return FACTORY.create(command);
    }
    Packet create(Command command);
}
