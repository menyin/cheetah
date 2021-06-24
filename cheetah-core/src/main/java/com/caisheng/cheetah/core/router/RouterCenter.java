package com.caisheng.cheetah.core.router;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.event.RouterChangeEvent;
import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.api.router.Router;
import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.common.router.RemoteRouterManager;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.tools.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterCenter extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(RouterCenter.class);

    private LocalRouterManager localRouterManager;
    private RemoteRouterManager remoteRouterManager;
    private UserEventConsumer userEventConsumer;
    private RouterChangeListener routerChangeListener;
    private CheetahServer cheetahServer;

    public RouterCenter(CheetahServer cheetahServer) {
        this.cheetahServer = cheetahServer;
    }

    @Override
    protected void doStart(Listener listener) {
        localRouterManager = new LocalRouterManager();
        remoteRouterManager = new RemoteRouterManager();
        routerChangeListener = new RouterChangeListener(this.cheetahServer);
        userEventConsumer = new UserEventConsumer(remoteRouterManager);
        userEventConsumer.getUserManager().cleanOnlineUserList();
        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) {
        userEventConsumer.getUserManager().cleanOnlineUserList();
        super.doStop(listener);
    }

    public boolean register(String userId, Connection connection) {
        ClientLocation clientLocation = ClientLocation.from(connection);
        //cny_note 注意这里远程路由的ClientLocation信息是连接服务器节点的网关节点ip+port
        //TODO clientLocation.setHost(cheetahServer.getGatewayServerNode().getHost());
        //TODO clientLocation.setPort(cheetahServer.getGatewayServerNode().getPort());
        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(clientLocation);
        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;
        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        } catch (Exception e) {
            logger.error("register router ex,userId={},connection={}", userId, connection, e);
        }
        if (oldLocalRouter != null) {
            EventBus.post(new RouterChangeEvent(userId, oldLocalRouter));
            logger.info("register router success, find old local router={}, userId={}", oldLocalRouter, userId);
        }
        if (oldRemoteRouter != null) {
            EventBus.post(new RouterChangeEvent(userId, oldRemoteRouter));
            logger.info("register router success, find old remote router={}, userId={}", oldRemoteRouter, userId);
        }
        return true;
    }

    public boolean unRegister(String userId,int clientType){
        localRouterManager.unRegister(userId, clientType);
        remoteRouterManager.unRegister(userId, clientType);
        logger.info("unregister router success,userId={},clientType={}",userId,clientType);
        return true;
    }

    public Router<?> lookup(String userId, int clientType) {
        LocalRouter local = localRouterManager.lookup(userId, clientType);
        if (local != null) return local;
        RemoteRouter remote = remoteRouterManager.lookup(userId, clientType);
        return remote;
    }
    public LocalRouterManager getLocalRouterManager() {
        return localRouterManager;
    }

    public RemoteRouterManager getRemoteRouterManager() {
        return remoteRouterManager;
    }
}
