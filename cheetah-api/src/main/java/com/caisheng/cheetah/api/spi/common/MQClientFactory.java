package com.caisheng.cheetah.api.spi.common;

import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface MQClientFactory extends Factory<MQClient> {
    static MQClient create() {
        return SpiLoader.load(MQClientFactory.class).get();
    }
}
