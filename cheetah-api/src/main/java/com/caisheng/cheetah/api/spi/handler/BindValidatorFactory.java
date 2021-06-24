
package com.caisheng.cheetah.api.spi.handler;


import com.caisheng.cheetah.api.spi.Factory;
import com.caisheng.cheetah.api.spi.SpiLoader;

public interface BindValidatorFactory extends Factory<BindValidator> {
    static BindValidator create() {
        return SpiLoader.load(BindValidatorFactory.class).get();
    }
}
