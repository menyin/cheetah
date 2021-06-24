package com.caisheng.cheetah.api.spi.core;

import com.caisheng.cheetah.api.connection.Cipher;
import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface RsaCipherFactory extends Factory<Cipher> {
    static Cipher create() {
        return SpiLoader.load(RsaCipherFactory.class).get();
    }
}
