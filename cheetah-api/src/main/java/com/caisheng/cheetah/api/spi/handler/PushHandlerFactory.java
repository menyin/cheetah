package com.caisheng.cheetah.api.spi.handler;

import com.caisheng.cheetah.api.message.MessageHandler;
import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface PushHandlerFactory extends Factory<MessageHandler> {

    static MessageHandler create(){
        return SpiLoader.load(PushHandlerFactory.class).get();
    }
}
