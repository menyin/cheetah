package com.caisheng.cheetah.cacher.redis.manager;

import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;

public class RedisCacheManagerFactory implements CacheManagerFactory {
    @Override
    public CacheManager get() {
        return RedisManager.I;
    }
}
