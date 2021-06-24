package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.api.spi.handler.PushHandlerFactory;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.AckMessage;
import com.caisheng.cheetah.common.message.PushMessage;
import com.caisheng.cheetah.tools.log.Logs;

public class ClientPushHandler extends BaseMessageHandler<PushMessage> implements PushHandlerFactory {
    @Override
    protected void handle(PushMessage pushMessage) {
        Logs.PUSH.info("receive connect push message={}", pushMessage);
        if (pushMessage.autoAck()) {
            AckMessage ackMessage = AckMessage.from(pushMessage);
            ackMessage.sendRaw();
            Logs.PUSH.info("send ack for push message={}",pushMessage);
        }
        //根据业务进行应答，可以作为一个扩展点进行注入
    }

    @Override
    protected PushMessage decode(Packet packet, Connection connection) {
        return new PushMessage(packet,connection);
    }

    @Override
    public MessageHandler get() {
        return this;
    }
}
