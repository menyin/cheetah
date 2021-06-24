package com.caisheng.cheetah.register.zk;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.api.srd.*;
import com.caisheng.cheetah.tools.Jsons;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZKServiceRegistryAndDiscovery extends BaseService implements ServiceRegistry, ServiceDiscover {

    public static final ZKServiceRegistryAndDiscovery I = new ZKServiceRegistryAndDiscovery();
    private static final String PATH_SPARATOR = "/";
    private ZKClient zkClient;

    public ZKServiceRegistryAndDiscovery() {
        this.zkClient = ZKClient.I;
    }

    @Override
    public void start(Listener listener) {
        if (isRunning()) {
            listener.onSuccess();
        } else {
            super.start(listener);
        }
    }

    @Override
    public void stop(Listener listener) {
        if (isRunning()) {
            super.stop(listener);
        } else {
            listener.onSuccess();
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        zkClient.start(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        zkClient.stop(listener);
    }

    /**
     * 服务发现
     **/
    @Override
    public List<ServiceNode> lookup(String serviceName) {
        List<String> childrenKeys = zkClient.getChildrenKeys(serviceName);
        if (childrenKeys == null || childrenKeys.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return childrenKeys.stream()
                .map(key -> {
                    return serviceName + PATH_SPARATOR + key;
                })
                .map(zkClient::get)
                .filter(Objects::nonNull)
                .map(value -> {
                    return Jsons.fromJson(value, CommonServiceNode.class);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(String watchPath, ServiceListener serviceListener) {
this.zkClient.registerListener(new ZKCacheListener(watchPath,serviceListener));
    }

    @Override
    public void unsubscribe(String path, ServiceListener serviceListener) {

    }

    /**
     * 服务注册
     **/
    @Override
    public void register(ServiceNode serviceNode) {

    }

    @Override
    public void deregister(ServiceNode serviceNode) {

    }
}
