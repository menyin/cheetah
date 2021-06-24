package com.caisheng.cheetah.api.spi.common;

import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface CacheManagerFactory extends Factory<CacheManager> {
    static CacheManager create() {
        return SpiLoader.load(CacheManagerFactory.class).get();
    }
}
