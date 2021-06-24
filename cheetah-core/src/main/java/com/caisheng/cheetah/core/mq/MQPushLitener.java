package com.caisheng.cheetah.core.mq;

import com.caisheng.cheetah.api.CheetahContext;
import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQClientFactory;
import com.caisheng.cheetah.api.spi.common.MQMessageReceiver;
import com.caisheng.cheetah.api.spi.push.PushListener;
import com.caisheng.cheetah.tools.config.ConfigTools;

public class MQPushLitener implements PushListener<MQPushMessage> ,MQMessageReceiver{
    private MQClient mqClient= MQClientFactory.create();
    private final static String TOPIC = "/lion/push/" + ConfigTools.getLocalIp();

    @Override
    public void init(CheetahContext context) {
        mqClient.init(context);
        mqClient.subscribe(TOPIC,this);
    }

    @Override
    public void receive(String topic, Object message) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onSuccess(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onAckSuccess(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onBroadcastSuccess(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onFailure(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onOffline(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onRedirect(MQPushMessage message, Object[] timePoints) {

    }

    @Override
    public void onTimeout(MQPushMessage message, Object[] timePoints) {

    }


}
