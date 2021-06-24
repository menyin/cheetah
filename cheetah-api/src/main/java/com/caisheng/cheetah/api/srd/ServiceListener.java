package com.caisheng.cheetah.api.srd;

public interface ServiceListener {
    void onServiceAdd(String path,ServiceNode serviceNode);
    void onServiceUpdate(String path,ServiceNode serviceNode);
    void onServiceRemove(String path,ServiceNode serviceNode);
}
