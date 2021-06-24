package com.caisheng.cheetah.common.router;

import com.caisheng.cheetah.api.router.ClientLocation;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CacheRemoteRouterManager extends RemoteRouterManager {
    private final Cache<String, Set<RemoteRouter>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        Set<RemoteRouter> cached = cache.getIfPresent(userId);
        if (cached != null) {
            return cached;
        }
        Set<RemoteRouter> remoteRouters = super.lookupAll(userId);
        if (remoteRouters != null) {
            cache.put(userId, remoteRouters);
        }
        return remoteRouters;
    }

    @Override
    public RemoteRouter lookup(String userId, int clientType) {
        Set<RemoteRouter> remoteRouters = this.lookupAll(userId);
        for (RemoteRouter remoteRouter : remoteRouters) {
            ClientLocation clientLocation = remoteRouter.getRouterValue();
            if (clientLocation.getClientType() == clientType) {
                return remoteRouter;
            }
        }
        return null;
    }

    public void invalidateLocalCache(String userId){
        if (userId != null) {
            cache.invalidate(userId);
        }
    }
}
