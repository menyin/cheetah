package com.caisheng.cheetah.api.spi.push;

public interface MessagePusher {
    void push(IPushMessage pushMessage);
}
