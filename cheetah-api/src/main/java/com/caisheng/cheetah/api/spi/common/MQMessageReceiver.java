package com.caisheng.cheetah.api.spi.common;

import java.util.Objects;

public interface MQMessageReceiver {
    void receive(String topic, Object message);
}
