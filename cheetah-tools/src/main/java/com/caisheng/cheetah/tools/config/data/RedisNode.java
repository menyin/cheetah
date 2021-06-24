package com.caisheng.cheetah.tools.config.data;

public class RedisNode {
    private int port;
    private String host;

    public RedisNode(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public static RedisNode from(String config) {
        String[] split = config.split(":");
        if (split.length > 1) {
            return new RedisNode(Integer.parseInt(split[1]),split[0]);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = host.hashCode();
        return hash * 31 + port;
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj)return true;
        if(obj==null||obj.getClass()!=this.getClass())return false;
        RedisNode redisNode=(RedisNode)obj;
        if(redisNode.getHost()!=this.getHost())return false;
        return redisNode.getPort()==this.getPort();
    }
}
