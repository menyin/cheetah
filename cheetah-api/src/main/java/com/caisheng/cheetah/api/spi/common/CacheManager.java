package com.caisheng.cheetah.api.spi.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface CacheManager {
    void init();
    void destroy();
    void del(String key);

    long hincrBy(String key, String field, long value);

    void set(String key , String value);
    void set(String key , String value,int expireTime);
    void set(String key , Object value,int expireTime);

    <T> T get(String key ,Class<T> clazz);

    void hset(String key ,String field,String value);
    void hset(String key ,String field,Object value);

    <T> T hget(String key, String field, Class<T> clazz);

    void hdel(String key, String field);

    <T> Map<String, T> hgetAll(String key, Class<T> clazz);

    void zAdd(String key, String value);

    Long zCard(String key);

    void zRem(String key, String value);

    <T> List<T> zRange(String key, int start, int end, Class<T> clazz);

    void lpush(String key ,String ... value);

    <T> List<T> lrange(String key, int start, int end, Class<T> clazz);
}
