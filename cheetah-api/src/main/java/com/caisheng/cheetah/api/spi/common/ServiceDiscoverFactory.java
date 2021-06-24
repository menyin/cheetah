package com.caisheng.cheetah.api.spi.common;

import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;
import com.caisheng.cheetah.api.srd.ServiceDiscover;

public interface ServiceDiscoverFactory extends Factory<ServiceDiscover> {
    static ServiceDiscover create(){
        return SpiLoader.load(ServiceDiscoverFactory.class).get();
    }
}
