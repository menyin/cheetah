package com.caisheng.cheetah.client.gateway.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.client.push.PushRequest;
import com.caisheng.cheetah.client.push.PushRequestBus;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.OkMessage;
import com.caisheng.cheetah.common.push.GatewayPushResult;
import com.caisheng.cheetah.tools.log.Logs;

import java.util.function.Supplier;

public class GatewayOkHandler extends BaseMessageHandler<OkMessage> {
    private PushRequestBus pushRequestBus;
    public GatewayOkHandler(CheetahClient cheetahClient) {
        this.pushRequestBus=cheetahClient.getPushRequestBus();

    }

    @Override
    protected void handle(OkMessage okMessage) {
        if (okMessage.getCmd()== Command.GATEWAY_PUSH.getCmd()) {
            PushRequest pushRequest = this.pushRequestBus.getAndRemove(okMessage.getPacket().getSessionId());
            if (pushRequest == null) {
                Logs.PUSH.warn("receive a gateway response,but request has timeout. message={}",okMessage);
                return;
            }
            pushRequest.onSuccess(GatewayPushResult.fromJson(okMessage.getData()));
        }
    }

    @Override
    protected OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet,connection);
    }
}
