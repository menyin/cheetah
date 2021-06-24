package com.caisheng.cheetah.api.srd;

public interface ServiceNode {
    String getServiceName();
    String getNodeId();
    String getHost();
    int getPort();
    default <T> T getAttr(String name){
        return null;
    }
    default boolean isPeristent(){return false;}
    default String hostAndPort(){return getHost()+":"+getPort();}
    default String nodePath(){return getServiceName()+"/"+getNodeId();}
}
