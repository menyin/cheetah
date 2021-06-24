package com.caisheng.cheetah.api.spi.client;

import com.caisheng.cheetah.api.push.PushSender;
import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface PushSenderFactory extends Factory<PushSender> {
    static PushSender create(){
        return SpiLoader.load(PushSenderFactory.class).get();
    }
}
