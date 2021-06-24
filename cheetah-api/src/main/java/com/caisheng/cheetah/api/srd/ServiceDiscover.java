package com.caisheng.cheetah.api.srd;

import com.caisheng.cheetah.api.service.Service;

import java.util.List;

public interface ServiceDiscover extends Service {
    List<ServiceNode> lookup(String path);
    void subscribe(String path,ServiceListener serviceListener);
    void unsubscribe(String path,ServiceListener serviceListener);
}
