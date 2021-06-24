package com.caisheng.cheetah.register.zk;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.tools.log.Logs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ZKClient extends BaseService {
    public static final ZKClient I = I();
    private ZKConfig zkConfig;
    private CuratorFramework client;
    private TreeCache treeCache;
    private Map<String, String> ephemeralNodes = new LinkedHashMap<>(4);
    private Map<String, String> ephemeralSequentialNodes = new LinkedHashMap<>(1);


    private synchronized static ZKClient I() {
        return I == null ? new ZKClient() : I;
    }

    private ZKClient() {
    }

    @Override
    public void start(Listener listener) {//cny_note 这个listenter通过一系列调用最终会被包装成FutureListener实例，具备CompletableFuture的特性
        if (isRunning()) {
            listener.onSuccess();
        } else {
            super.start(listener);//调用父类BaseService#start(Listener listener)
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
        client.start();
        Logs.SRD.info("init zk connect waitting for connected....");
        if (!client.blockUntilConnected(1, TimeUnit.MINUTES)) {
            throw new ZKException("init zk error,config=" + zkConfig);
        }
        initLocalCache(zkConfig.getWatchPath());
        addConnectionStateListener();

    }

    private void addConnectionStateListener() {
        this.client.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                ephemeralNodes.forEach(this::reRegisterEphemeral);
                ephemeralSequentialNodes.forEach(this::reRegisterEphemeralSequential);
            }
        });
    }

    private void reRegisterEphemeralSequential(String key, String value) {
        registerEphemeralSequential(key, value, false);
    }

    private void registerEphemeralSequential(String key, String value, boolean cacheNode) {
        try {
//            if (isExisted(key)) {
//                connect.delete().deletingChildrenIfNeeded().forPath(key);
//            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key, value.getBytes(Constants.UTF_8));
            if (cacheNode) {
                ephemeralSequentialNodes.put(key, value);
            }
        } catch (Exception ex) {
            Logs.SRD.error("register ephemeral sequential failure,key={},value={}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    public void registerEphemeralSequential(String key, String value) {
        registerEphemeralSequential(key, value, true);
    }

    public void registerEphemeralSequential(String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (Exception ex) {
            Logs.SRD.error("register ephemeral sequential failure,key={}", key, ex);
            throw new ZKException(ex);
        }
    }

    private void reRegisterEphemeral(String key, String value) {
        registerEphemeral(key, value, false);
    }

    private void registerEphemeral(String key, String value, boolean cacheNode) {

        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Constants.UTF_8));
            if (cacheNode) {
                ephemeralNodes.put(key, value);
            }
        } catch (Exception ex) {
            Logs.SRD.error("registerEphemeral failure,key={},value={}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    public void registerEphemeral(String key, String value) {
        registerEphemeral(key, value, true);
    }


    public void remove(String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception ex) {
            Logs.SRD.error("delete node failure,key={}", key, ex);
            throw new ZKException(ex);
        }
    }

    private boolean isExisted(String key) {
        try {
            Stat stat = client.checkExists().forPath(key);
            return stat != null;
        } catch (Exception ex) {
            Logs.SRD.error("checkExists failure,key={}", key, ex);
            return false;
        }
    }

    private void initLocalCache(String watchPath) throws Exception {
        treeCache = new TreeCache(client, watchPath);
        treeCache.start();
    }

    public void registerListener(TreeCacheListener treeCacheListener) {
        treeCache.getListenable().addListener(treeCacheListener);
    }


    public String get(String key) {
        if (treeCache == null) {
            return null;
        }
        ChildData currentData = treeCache.getCurrentData(key);
        if (currentData != null) {
            return currentData.getData() == null ? null : new String(currentData.getData(), Constants.UTF_8);
        }
        return getFromRemote(key);
    }


    public String getFromRemote(String key) {
        if (isExisted(key)) {
            try {
                byte[] bytes = client.getData().forPath(key);
                if (bytes != null) {
                    return new String(bytes, Constants.UTF_8);
                }
                return null;
            } catch (Exception ex) {
                Logs.SRD.error("getData from remote failure,key={}", key, ex);
                return null;
            }
        }
        return null;
    }

    public List<String> getChildrenKeys(String key) {
        if (isExisted(key)) {
            return Collections.EMPTY_LIST;
        }
        try {
            List<String> childrens = client.getChildren().forPath(key);
            childrens.sort(Comparator.reverseOrder());
            return childrens;
        } catch (Exception ex) {
            Logs.SRD.error("get childrens failure,key={}",key,ex);
            return Collections.EMPTY_LIST;
        }

    }

    public void update(String key,String value){
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key,value.getBytes(Constants.UTF_8)).and().commit();

        } catch (Exception ex) {
            Logs.SRD.error("update key failure,key={},value={}",key,value,ex);
            throw new ZKException(ex);
        }

    }

    public ZKConfig getZkConfig() {
        return zkConfig;
    }

    public void setZkConfig(ZKConfig zkConfig) {
        this.zkConfig = zkConfig;
    }
}

