package com.caisheng.cheetah.client.gateway.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.protocol.Command;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.client.push.PushRequest;
import com.caisheng.cheetah.client.push.PushRequestBus;
import com.caisheng.cheetah.common.ErrorCode;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.tools.log.Logs;

import java.util.function.Supplier;

public class GatewayErrorHandler extends BaseMessageHandler<ErrorMessage> {
    private final PushRequestBus pushRequestBus;

    public GatewayErrorHandler(CheetahClient cheetahClient) {
        this.pushRequestBus = cheetahClient.getPushRequestBus();
    }

    @Override
    protected void handle(ErrorMessage errorMessage) {
        if (errorMessage.getCmd() == Command.GATEWAY_PUSH.getCmd()) {
            PushRequest pushRequest = pushRequestBus.getAndRemove(errorMessage.getPacket().getSessionId());
            if (pushRequest == null) {
                Logs.PUSH.warn("receive a gateway response,but request has timeout,message={}", errorMessage);
                return;
            }
            Logs.PUSH.warn("receive an error gateway response,message={}", errorMessage);
            if (errorMessage.getCode()== ErrorCode.OFFLINE.getErrorCode()) {
                pushRequest.onOffline();
            } else if (errorMessage.getCode()==ErrorCode.PUSH_CLIENT_FAILURE.getErrorCode()) {
                pushRequest.onFailure();
            } else if (errorMessage.getCode()==ErrorCode.ROUTER_CHANGE.getErrorCode()) {
                pushRequest.onRedirect();
            }
        }


    }

    @Override
    protected ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }
}
