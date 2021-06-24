package com.caisheng.cheetah.core.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.handler.BaseMessageHandler;
import com.caisheng.cheetah.common.message.AckMessage;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.ack.AckTask;
import com.caisheng.cheetah.core.ack.AckTaskQueue;
import com.caisheng.cheetah.tools.log.Logs;

public class AckHandler extends BaseMessageHandler<AckMessage> {
    private final AckTaskQueue ackTaskQueue;

    public AckHandler(CheetahServer cheetahServer) {
        this.ackTaskQueue = null;//TODO
        //this.ackTaskQueue = cheetahServer.getPushCenter().getAckTaskQueue();TODO
    }

    @Override
    protected void handle(AckMessage ackMessage) {

        AckTask ackTask =this.ackTaskQueue.getAndRemove(ackMessage.getPacket().getSessionId());
        if (ackTask  != null) {
            Logs.PUSH.info("receive qishou connect ack,but task timeout message={}",ackMessage);
            return;
        }

        ackTask.onResponse();

    }

    @Override
    protected AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }
}
