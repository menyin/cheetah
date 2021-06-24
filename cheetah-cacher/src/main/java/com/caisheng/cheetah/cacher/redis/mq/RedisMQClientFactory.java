package com.caisheng.cheetah.cacher.redis.mq;

import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQClientFactory;

public class RedisMQClientFactory implements MQClientFactory {
    private ListenerDispatcher listenerDispatcher = new ListenerDispatcher();

    @Override
    public MQClient get() {
        return null;
    }
}
