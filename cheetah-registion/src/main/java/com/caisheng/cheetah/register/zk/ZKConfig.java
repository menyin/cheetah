package com.caisheng.cheetah.register.zk;

import com.caisheng.cheetah.tools.config.CC;

public class ZKConfig {
    public static final int ZK_MAX_RETRY = 3;
    public static final int ZK_MIN_TIME = 5000;
    public static final int ZK_MAX_TIME = 5000;
    public static final int ZK_SESSION_TIMEOUT = 5000;
    public static final int ZK_CONNECTION_TIMEOUT = 5000;
    public static final String ZK_DEFAULT_CACHE_PATH = "/";

    private String hosts;
    private String digest;
    private String namespace;
    private int maxRetries = ZK_MAX_RETRY;
    private int baseSleepTimeMs = ZK_MIN_TIME;
    private int maxSleepMs = ZK_MAX_TIME;
    private int sessionTimeout = ZK_SESSION_TIMEOUT;
    private int connectionTimeout = ZK_CONNECTION_TIMEOUT;
    private String watchPath = ZK_DEFAULT_CACHE_PATH;

    public ZKConfig(String hosts) {
        this.hosts = hosts;
    }

    public static ZKConfig build() {
        ZKConfig zkConfig = new ZKConfig(CC.lion.zk.server_address);
        zkConfig.setConnectionTimeout(CC.lion.zk.connectionTimeoutMs);
        zkConfig.setDigest(CC.lion.zk.digest);
        zkConfig.setWatchPath(CC.lion.zk.watch_path);
        zkConfig.setMaxRetries(CC.lion.zk.retry.maxRetries);
        zkConfig.setMaxSleepMs(CC.lion.zk.retry.maxSleepMs);
        zkConfig.setBaseSleepTimeMs(CC.lion.zk.retry.baseSleepTimeMs);
        zkConfig.setNamespace(CC.lion.zk.namespace);
        zkConfig.setSessionTimeout(CC.lion.zk.sessionTimeoutMs);
        return zkConfig;
    }


    @Override
    public String toString() {
        return "ZKConfig{" +
                "hosts='" + hosts + '\'' +
                ", digest='" + digest + '\'' +
                ", namespace='" + namespace + '\'' +
                ", maxRetries=" + maxRetries +
                ", baseSleepTimeMs=" + baseSleepTimeMs +
                ", maxSleepMs=" + maxSleepMs +
                ", sessionTimeout=" + sessionTimeout +
                ", connectionTimeout=" + connectionTimeout +
                ", watchPath='" + watchPath + '\'' +
                '}';
    }



    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getWatchPath() {
        return watchPath;
    }

    public void setWatchPath(String watchPath) {
        this.watchPath = watchPath;
    }
}
