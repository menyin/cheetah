package com.caisheng.cheetah.api.message;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;

public interface MessageHandler {
    void handle(Packet packet, Connection connection);
}
