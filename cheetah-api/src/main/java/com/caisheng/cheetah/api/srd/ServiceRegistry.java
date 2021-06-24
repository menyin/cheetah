package com.caisheng.cheetah.api.srd;

public interface ServiceRegistry {
    void register(ServiceNode serviceNode);
    void deregister(ServiceNode serviceNode);
}
