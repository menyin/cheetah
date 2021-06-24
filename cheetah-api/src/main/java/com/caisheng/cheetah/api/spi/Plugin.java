
package com.caisheng.cheetah.api.spi;

import com.caisheng.cheetah.api.CheetahContext;

/**
 *
 *
 *
 */
public interface Plugin {

    default void init(CheetahContext context) {

    }

    default void destroy() {

    }
}
