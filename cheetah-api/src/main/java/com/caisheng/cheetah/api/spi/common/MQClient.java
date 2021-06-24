package com.caisheng.cheetah.api.spi.common;

import com.caisheng.cheetah.api.spi.Plugin;

public interface MQClient extends Plugin{
    void subscribe(String topic,MQMessageReceiver mqMessageReceiver);
    void publish(String topic,Object message);
}
