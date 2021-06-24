package com.caisheng.cheetah.api.spi.router;

import com.caisheng.cheetah.api.router.ClientClassifier;
import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface ClientClassifierFactory extends Factory<ClientClassifier>{
     static ClientClassifier create(){
        return SpiLoader.load(ClientClassifierFactory.class).get();
    }
}
