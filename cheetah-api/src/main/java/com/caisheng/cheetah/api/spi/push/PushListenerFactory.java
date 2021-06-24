package com.caisheng.cheetah.api.spi.push;

import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface PushListenerFactory<M extends IPushMessage> extends Factory<PushListener<M>> {
    static <M extends IPushMessage> PushListener<M> create(){
        return (PushListener<M>) SpiLoader.load(PushListenerFactory.class).get();
    }

}
