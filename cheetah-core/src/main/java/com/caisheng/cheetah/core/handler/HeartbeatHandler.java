package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.tools.log.Logs;

public class HeartbeatHandler implements MessageHandler {

    public HeartbeatHandler(Packet packet, Connection connection) {
    }

    @Override
    public void handle(Packet packet, Connection connection) {
        connection.send(packet);
        Logs.HEARTBEAT.info("ping -> pong {}",connection);
    }
}
