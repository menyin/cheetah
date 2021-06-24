package com.caisheng.cheetah.cacher.redis.connection;

import com.caisheng.cheetah.tools.config.data.RedisNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis连接工厂
 * 大致的使用流程：调用相关set方法，调用init(),调用相关方法获取客户端，用客户端操作数据
 */
public class RedisConnectionFactory {
    private Logger logger = LoggerFactory.getLogger(RedisConnectionFactory.class);

    private String hostName = Protocol.DEFAULT_HOST;
    private int port = Protocol.DEFAULT_PORT;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    private String password;
    private String sentinelMaster;//哨兵监控的master名 在sentinel.conf里配置
    private List<RedisNode> redisServers;
    private boolean isCluster;
    private int dbIndex = 0;
    private JedisShardInfo jedisShardInfo;
    private Pool<Jedis> pool;//连接池，可能是哨兵连接池或普通连接池
    private JedisCluster jedisCluster;
    private JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();//连接池相关参数配置类

    public RedisConnectionFactory() {
    }

    public void init() {
        if (this.jedisShardInfo == null) {
            this.jedisShardInfo = new JedisShardInfo(this.hostName, this.port);
            if (StringUtils.isNotBlank(this.password)) {
                this.jedisShardInfo.setPassword(this.password);
            }
            if (this.timeout > 0) {
                this.jedisShardInfo.setConnectionTimeout(this.timeout);
            }
        }
        if (this.isCluster) {
            this.jedisCluster = createCluster();
        } else {
            this.pool = createPool();
        }
    }

    public void destroy() {
        if (this.pool != null) {
                try {
                    this.pool.destroy();
                } catch (Exception e) {
                    logger.warn("Cannot properly close Jedis pool");
                }
                this.pool=null;
        }
        if (this.jedisCluster != null) {
            try {
                this.jedisCluster.close();
            } catch (IOException ex) {
                logger.warn("Cannot properly close Jedis cluster",ex);
            }
            this.jedisCluster=null;
        }
    }

    private Pool<Jedis> createPool() {
        if (StringUtils.isNotBlank(this.sentinelMaster)) {
            Set<String> sentinels = this.redisServers.stream().map(redisNode -> {
                return redisNode.getHost() + ":" + redisNode.getPort();
            }).collect(Collectors.toSet());
            return new JedisSentinelPool(this.sentinelMaster, sentinels, this.jedisPoolConfig, this.jedisShardInfo.getSoTimeout(), jedisShardInfo.getPassword());
        } else {
            JedisPool jedisPool = new JedisPool(this.jedisPoolConfig, jedisShardInfo.getHost(), jedisShardInfo.getPort(), this.jedisShardInfo.getSoTimeout(), jedisShardInfo.getPassword());
            return jedisPool;
        }
    }

    private JedisCluster createCluster() {
        if (StringUtils.isNotBlank(this.password)) {
            throw new IllegalArgumentException("Jedis does not support password protected Redis Cluster configurations!");
        }
        Set<HostAndPort> redisServersSet = this.redisServers.stream().map(redisNode -> new HostAndPort(redisNode.getHost(), redisNode.getPort())).collect(Collectors.toSet());
        return new JedisCluster(redisServersSet, this.timeout, 5, this.jedisPoolConfig);//maxAttempts是最大重试次数
    }




    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSentinelMaster() {
        return sentinelMaster;
    }

    public void setSentinelMaster(String sentinelMaster) {
        this.sentinelMaster = sentinelMaster;
    }

    public List<RedisNode> getRedisServers() {
        return redisServers;
    }

    public void setRedisServers(List<RedisNode> redisServers) {
        this.redisServers = redisServers;
    }

    public boolean isCluster() {
        return isCluster;
    }

    public void setCluster(boolean cluster) {
        isCluster = cluster;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public JedisShardInfo getJedisShardInfo() {
        return jedisShardInfo;
    }

    public void setJedisShardInfo(JedisShardInfo jedisShardInfo) {
        this.jedisShardInfo = jedisShardInfo;
    }

    public Pool<Jedis> getPool() {
        return pool;
    }

    public void setPool(Pool<Jedis> pool) {
        this.pool = pool;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public Jedis getJedis() {
        Jedis jedis = fetchJedis();
        if (this.dbIndex>0) {
            jedis.select(this.dbIndex);
        }
        return jedis;
    }

    protected Jedis fetchJedis() {
        try {
            if (this.pool != null) {
                return this.pool.getResource();
            }
            Jedis jedis = new Jedis(this.jedisShardInfo);//jedisShardInfo只是作为一些参数信息的收集载体
            jedis.connect();
            return jedis;
        } catch (Exception ex) {
            throw new RuntimeException("Cannot get Jedis connection", ex);
        }

    }

}
