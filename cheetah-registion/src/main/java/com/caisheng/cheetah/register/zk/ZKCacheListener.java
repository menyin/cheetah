package com.caisheng.cheetah.register.zk;

import com.caisheng.cheetah.api.srd.CommonServiceNode;
import com.caisheng.cheetah.api.srd.ServiceListener;
import com.caisheng.cheetah.api.srd.ServiceNode;
import com.caisheng.cheetah.tools.Jsons;
import com.caisheng.cheetah.tools.log.Logs;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public class ZKCacheListener implements TreeCacheListener {
    private String watchPath;
    private ServiceListener serviceListener;

    public ZKCacheListener(String watchPath, ServiceListener serviceListener) {
        this.watchPath = watchPath;
        this.serviceListener = serviceListener;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
        ChildData data = treeCacheEvent.getData();
        if (data == null) {
            return;
        }
        String path = data.getPath();
        if (StringUtils.isBlank(path)) {
            return;
        }
        if (path.startsWith(watchPath)) {
            switch (treeCacheEvent.getType()) {
                case NODE_ADDED:
                    serviceListener.onServiceAdd(path, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_REMOVED:
                    serviceListener.onServiceRemove(path, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_UPDATED:
                    serviceListener.onServiceUpdate(path, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
            }

            Logs.SRD.info("ZK node data change={},nodePath={},watchPath,ns={}",data,path,watchPath,"");
        }
    }
}
