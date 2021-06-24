package com.caisheng.cheetah.common.handler;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.protocol.Packet;
import com.caisheng.cheetah.common.message.BaseMessage;

public abstract class BaseMessageHandler<T extends BaseMessage> implements MessageHandler {

    @Override
    public void handle(Packet packet, Connection connection) {
//        Profiler.enter("time cost on [message decode]");

        T t = decode(packet, connection);
        if (t!=null){
            t.decodeBody();
//            Profiler.release();
//            Profiler.enter("time cost on [handle]");
            handle(t);
//            Profiler.release();
        }
    }

    protected abstract void handle(T t);

    protected abstract T decode(Packet packet, Connection connection);
}
