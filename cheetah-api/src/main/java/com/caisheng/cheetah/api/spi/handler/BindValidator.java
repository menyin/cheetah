
package com.caisheng.cheetah.api.spi.handler;


import com.caisheng.cheetah.api.spi.Plugin;

public interface BindValidator extends Plugin {
    boolean validate(String userId, String data);
}
