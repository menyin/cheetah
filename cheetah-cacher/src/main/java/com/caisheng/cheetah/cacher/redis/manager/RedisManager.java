package com.caisheng.cheetah.cacher.redis.manager;

import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.cacher.redis.connection.RedisConnectionFactory;
import com.caisheng.cheetah.tools.Jsons;
import com.caisheng.cheetah.tools.Utils;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.log.Logs;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RedisManager implements CacheManager {
    public static final RedisManager I = new RedisManager();
    private final RedisConnectionFactory redisConnectionFactory = new RedisConnectionFactory();//连接工厂，仿造JedisConnectionFactory


    @Override
    public void init() {
        redisConnectionFactory.setPassword(CC.lion.redis.password);
        redisConnectionFactory.setJedisPoolConfig(CC.lion.redis.getPoolConfig(JedisPoolConfig.class));
        redisConnectionFactory.setRedisServers(CC.lion.redis.nodes);
        redisConnectionFactory.setCluster(CC.lion.redis.isCluster());
        if (CC.lion.redis.isSentinel()) {
            redisConnectionFactory.setSentinelMaster(CC.lion.redis.sentinelMaster);
        }
        redisConnectionFactory.init();
        test();
        Logs.CACHE.info("init redis success...");
    }

    private <R> R apply(Function<JedisCommands, R> function, R r) {
        if (redisConnectionFactory.isCluster()) {
            JedisCluster jedisCluster = null;
            try {
                jedisCluster = redisConnectionFactory.getJedisCluster();
                R apply = function.apply(jedisCluster);
                return apply;
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            }

        } else {
            Jedis jedis = null;
            try {
                jedis = redisConnectionFactory.getJedis();
                R apply = function.apply(jedis);
                return apply;
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return null;
    }

    private <R> R apply(Function<JedisCommands, R> function) {
        if (redisConnectionFactory.isCluster()) {
            JedisCluster jedisCluster = null;
            try {
                jedisCluster = redisConnectionFactory.getJedisCluster();
                R apply = function.apply(jedisCluster);
                return apply;
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            }
        } else {
            Jedis jedis = null;
            try {
                jedis = redisConnectionFactory.getJedis();
                R apply = function.apply(jedis);
                return apply;
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }

        }
        return null;
    }

    /**
     * 类似于js的call
     *
     * @param consumer
     */
    private void call(Consumer<JedisCommands> consumer) {
        if (redisConnectionFactory.isCluster()) {

            JedisCluster jedisCluster = null;
            try {
                jedisCluster = redisConnectionFactory.getJedisCluster();
                consumer.accept(jedisCluster);
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            }

        } else {
            Jedis jedis = null;
            try {
                jedis = redisConnectionFactory.getJedis();
                consumer.accept(jedis);
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    /**
     * 测试
     */
    private void test() {
        if (redisConnectionFactory.isCluster()) {
            JedisCluster jedisCluster = redisConnectionFactory.getJedisCluster();
            if (jedisCluster == null) {
                throw new RuntimeException("init redis cluster error...");
            }
        } else {
            Jedis jedis = redisConnectionFactory.getJedis();
            if (jedis == null) {
                throw new RuntimeException("init redis error...");
            }
            jedis.close();
        }
    }

    @Override
    public void destroy() {
        if (redisConnectionFactory != null) {
            redisConnectionFactory.destroy();
        }
    }
    /**** CacheManager接口方法 begin ****/
    @Override
    public void del(String key) {
        this.call(jedis -> {
            jedis.del(key);
        });
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return apply(jedis -> jedis.hincrBy(key, field, value));
    }

    @Override
    public void set(String key, String value) {
        call(jedis -> jedis.set(key, value));
    }

    @Override
    public void set(String key, String value, int expireTime) {//expireTime单位是秒
        call(jedis -> {
            jedis.set(key, value);
            if (expireTime > 0) {
                jedis.expire(key, expireTime);
            }
        });
    }

    @Override
    public void set(String key, Object value, int expireTime) {
        call(jedis -> {
            jedis.set(key, value.toString());
            if (expireTime > 0) {
                jedis.expire(key, expireTime);
            }
        });
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return apply(jedis -> {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }
            return Jsons.fromJson(json, clazz);
        });
    }

    @Override
    public void hset(String key, String field, String value) {
        call(jedes -> jedes.hset(key, field, value));
    }

    @Override
    public void hset(String key, String field, Object value) {
        call(jedes -> {
            String json = Jsons.toJson(value);
            jedes.hset(key, field, json);
        });

    }

    @Override
    public <T> T hget(String key, String field, Class<T> clazz) {
        return apply(jedis -> {
            String json = jedis.hget(key, field);
            return Jsons.fromJson(json, clazz);
        });
    }

    @Override
    public void hdel(String key, String field) {
        call(jedis -> jedis.hdel(key, field));
    }

    public Map<String, String> hgetAll(String key) {
        return apply(jedis -> jedis.hgetAll(key), Collections.<String, String>emptyMap());
    }

    @Override
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        return apply(jedis -> {
            Map<String, String> map = jedis.hgetAll(key);
            if (map.isEmpty()) {
                return (Map<String, T>)Collections.EMPTY_MAP;
            }
            HashMap<String, T> stringTHashMap = new HashMap<String, T>();
            map.forEach((k, v) -> {
                stringTHashMap.put(k, Jsons.fromJson(v, clazz));
            });
            return stringTHashMap;
        });
    }

    /**
     * SortedSet 有序的set
     * 添加一个值，score为分数，分数越低越前面
     *
     * @param key
     * @param value
     */
    @Override
    public void zAdd(String key, String value) {
        call(jedis -> jedis.zadd(key, 0, value));
    }

    /**
     * SortedSet 有序的set
     * 得到set里元素的总个数
     *
     * @param key
     */
    @Override
    public Long zCard(String key) {
        return apply(jedis -> jedis.zcard(key));
    }

    @Override
    public void zRem(String key, String value) {
        call(jedis -> jedis.zrem(key, value));//移除指定SortedSet指定元素
    }

    @Override
    public <T> List<T> zRange(String key, int start, int end, Class<T> clazz) {
        return apply(jedis -> {
            Set<String> set = jedis.zrange(key, start, end);
            if (set == null) {
                return null;
            }
            if (clazz == String.class) {
                return (List<T>) set.stream().collect(Collectors.toList());
            }
            return set.stream().map(s -> Jsons.fromJson(s, clazz)).collect(Collectors.toList());
        });
    }

    @Override
    public void lpush(String key, String... value) {
        call(jedis -> jedis.lpush(key, value));
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {

        return apply(jedis -> {
            List<String> lrange = jedis.lrange(key, start, end);
            if (lrange == null) {
                return null;
            }
            if (clazz == String.class){
                return (List<T>) lrange;
            }
            return lrange.stream().map(s -> Jsons.fromJson(s, clazz)).collect(Collectors.toList());
        });
    }

    /**** CacheManager接口方法 end ****/



    /**** redis MQ 相关方法 begin ****/
    public void publish(String channel,Object message){
        String msg = message.getClass() == String.class ? (String)message: Jsons.toJson(message);
        call(jedis->{
            if(jedis instanceof MultiKeyCommands){
                ((MultiKeyCommands) jedis).publish(channel,msg);
            } else if (jedis instanceof MultiKeyJedisClusterCommands) {
                ((MultiKeyJedisClusterCommands) jedis).publish(channel,msg);
            }
        });

    }

    /**
     *
     * @param jedisPubSub
     * @param channel
     */
    public void subscribe(JedisPubSub jedisPubSub,String channel){
        Utils.newThread(channel, () -> {
            call(jedis->{
                if(jedis instanceof MultiKeyCommands){
                    ((MultiKeyCommands) jedis).subscribe(jedisPubSub,channel);
                } else if (jedis instanceof MultiKeyJedisClusterCommands) {
                    ((MultiKeyJedisClusterCommands) jedis).subscribe(jedisPubSub,channel);
                }
            });
        });

    }
    /**** redis MQ 相关方法 end ****/


}
