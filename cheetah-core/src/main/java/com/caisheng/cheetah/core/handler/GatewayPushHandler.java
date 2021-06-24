package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.gateway.GatewayPushMessage;
import com.caisheng.cheetah.core.push.PushCenter;

public class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {
    private PushCenter pushCenter;

    public GatewayPushHandler(PushCenter pushCenter) {
        this.pushCenter = pushCenter;
    }

    @Override
    protected void handle(GatewayPushMessage gatewayPushMessage) {
        this.pushCenter.push(gatewayPushMessage);
    }

    @Override
    protected GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet,connection);
    }
}
