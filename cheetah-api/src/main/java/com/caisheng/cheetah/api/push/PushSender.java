package com.caisheng.cheetah.api.push;

import com.caisheng.cheetah.api.service.Service;
import com.caisheng.cheetah.api.spi.client.PushSenderFactory;

import java.util.concurrent.FutureTask;

public interface PushSender extends Service {
    static PushSender create(){
        return PushSenderFactory.create();
    }

    FutureTask<PushResult> send(PushContext pushContext);

    default FutureTask<PushResult> send(String content,String userId,PushCallback pushCallback){
        PushContext pushContext = new PushContext(content);
        pushContext.setUserId(userId);
        pushContext.setPushCallback(pushCallback);
        return send(pushContext);
    }

    default FutureTask<PushResult> send(String content, String userId, AckModel ackModel, PushCallback pushCallback) {
        PushContext pushContext = new PushContext(content);
        pushContext.setUserId(userId);
        pushContext.setAckModel(ackModel);
        pushContext.setPushCallback(pushCallback);
        return send(pushContext);
    }

}
