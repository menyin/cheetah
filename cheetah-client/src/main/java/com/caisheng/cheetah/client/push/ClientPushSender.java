package com.caisheng.cheetah.client.push;

import com.caisheng.cheetah.api.CheetahContext;
import com.caisheng.cheetah.api.push.*;
import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;
import com.caisheng.cheetah.api.spi.common.ServiceDiscoverFactory;
import com.caisheng.cheetah.api.srd.ServiceDiscover;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.common.router.CacheRemoteRouterManager;

import java.util.Set;
import java.util.concurrent.FutureTask;

import com.caisheng.cheetah.client.gateway.connection.GatewayConnectionFactory;
import com.caisheng.cheetah.common.router.RemoteRouter;

/**
 * look >>> PushClient
 */
public class ClientPushSender extends BaseService implements PushSender {

    private CheetahClient cheetahClient;
    private PushRequestBus pushRequestBus;
    private CacheRemoteRouterManager cacheRemoteRouterManager;
    private GatewayConnectionFactory gatewayConnetionFactory;

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (cheetahClient == null) {
            cheetahClient = new CheetahClient();
        }

        pushRequestBus = cheetahClient.getPushRequestBus();
        cacheRemoteRouterManager = cheetahClient.getCacheRemoteRouterManager();
        gatewayConnetionFactory = cheetahClient.getGatewayConnectionFactory();

        ServiceDiscover serviceDiscover = ServiceDiscoverFactory.create();
        serviceDiscover.syncStart();

        CacheManager cacheManager = CacheManagerFactory.create();
        cacheManager.init();

        pushRequestBus.syncStart();

        gatewayConnetionFactory.start(listener);

    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ServiceDiscoverFactory.create().syncStop();
        CacheManagerFactory.create().destroy();
        pushRequestBus.syncStop();
        gatewayConnetionFactory.stop(listener);
    }

    /******** cny_note PushSender Methods begin *********/
    @Override
    public FutureTask<PushResult> send(PushContext pushContext) {
        if (pushContext.isBroadcast()) {
            pushContext.setUserId(null);
            return send0(pushContext);
        } else if (pushContext.getUserId() != null) {
            return send0(pushContext);
        } else if (pushContext.getUserIds() != null) {
            FutureTask<PushResult> task = null;
            for (String userId : pushContext.getUserIds()) {
                pushContext.setUserId(userId);
                task = send0(pushContext);
            }
            return task;
        } else {
            throw new PushException("param error.");
        }

    }

    private FutureTask<PushResult> send0(PushContext pushContext) {
        if (pushContext.isBroadcast()) {
            return PushRequest.build(cheetahClient, pushContext).broadcast();
        } else {
            Set<RemoteRouter> remoteRouters = this.cacheRemoteRouterManager.lookupAll(pushContext.getUserId());
            if (remoteRouters == null || remoteRouters.isEmpty()) {
                return PushRequest.build(cheetahClient, pushContext).onOffline();
            }
            FutureTask<PushResult> task=null;
            for (RemoteRouter remoteRouter : remoteRouters) {
                 task = PushRequest.build(cheetahClient, pushContext).send(remoteRouter);
            }
            return task;
        }
    }

    /******** cny_note PushSender Methods end *********/

    @Override
    public boolean isRunning() {
        return started.get();
    }

}
