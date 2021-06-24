package com.caisheng.cheetah.api.srd;

import java.util.Map;
import java.util.UUID;

public class CommonServiceNode implements ServiceNode {
    private String host;
    private int port;
    private Map<String, Object> attrs;
    private transient String name;
    private transient String nodeId;
    private transient boolean persistent;

    public CommonServiceNode() {
    }

    public CommonServiceNode(String host, int port, Map<String, Object> attrs, String name, String nodeId, boolean persistent) {
        this.host = host;
        this.port = port;
        this.attrs = attrs;
        this.name = name;
        this.nodeId = nodeId;
        this.persistent = persistent;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @Override
    public String getServiceName() {
        return this.name;
    }

    @Override
    public String getNodeId() {
        if (this.nodeId==null) {
            this.nodeId = UUID.randomUUID().toString();
        }
        return this.nodeId;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public <T> T getAttr(String name) {
        if (this.attrs == null || this.attrs.isEmpty()) {
            return null;
        }
        return (T)this.attrs.get(name);
    }

    @Override
    public boolean isPeristent() {
        return this.persistent;
    }

    @Override
    public String hostAndPort() {
        return this.host + ":" + this.port;
    }


    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", attrs=" + attrs +
                ", persistent=" + persistent +
                '}';
    }
}
