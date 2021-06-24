package com.caisheng.cheetah.api.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;

public interface PacketReceiver {
    void onReceive(Packet packet, Connection connection);
}
