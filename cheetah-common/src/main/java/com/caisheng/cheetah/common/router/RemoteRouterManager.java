package com.caisheng.cheetah.common.router;

import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.api.router.RouterManager;
import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;
import com.caisheng.cheetah.common.CacheKeys;
import com.caisheng.cheetah.tools.event.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoteRouterManager extends EventConsumer implements RouterManager<RemoteRouter> {
    private final static Logger logger = LoggerFactory.getLogger(RemoteRouterManager.class);
    private final CacheManager cacheManager = CacheManagerFactory.create();

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
        String userRouterKey = CacheKeys.getUserRouterKey(userId);
        String field = Integer.toString(router.getRouterValue().getClientType());
        ClientLocation old = cacheManager.hget(userRouterKey, field, ClientLocation.class);
        cacheManager.hset(userRouterKey, field, router.getRouterValue().toJson());
        logger.info("register remote router success,userId={},oldRouter={},newRouter={}", userId, old, router);
        return old == null ? null : new RemoteRouter(old);
    }

    @Override
    public boolean unRegister(String userId, int clientType) {
        String userRouterKey = CacheKeys.getUserRouterKey(userId);
        String field = Integer.toString(clientType);
        ClientLocation old = cacheManager.hget(userRouterKey, field, ClientLocation.class);
        if (old == null||old.isOffline()) {
            return true;
        }else{
            cacheManager.hset(userRouterKey, field, old.offline().toJson());
            logger.info("unRegister remote router success userId={}, route={}", userId, old);
            return true;
        }
    }

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        String userRouterKey = CacheKeys.getUserRouterKey(userId);
        Map<String, ClientLocation> clientLocationMap = cacheManager.hgetAll(userRouterKey, ClientLocation.class);
        if (clientLocationMap == null||clientLocationMap.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<RemoteRouter> set = clientLocationMap.values().stream().map(RemoteRouter::new).collect(Collectors.toSet());
        return set;
    }

    @Override
    public RemoteRouter lookup(String userId, int clientType) {
        String userRouterKey = CacheKeys.getUserRouterKey(userId);
        ClientLocation clientLocation = cacheManager.hget(userRouterKey, Integer.toString(clientType), ClientLocation.class);
        logger.info("lookup remote router userId={}, router={}", userId, clientLocation);
        return clientLocation == null ? null : new RemoteRouter(clientLocation);
    }
}
