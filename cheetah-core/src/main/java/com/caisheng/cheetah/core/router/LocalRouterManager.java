package com.caisheng.cheetah.core.router;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.event.ConnectionCloseEvent;
import com.caisheng.cheetah.api.event.UserOfflineEvent;
import com.caisheng.cheetah.api.router.Router;
import com.caisheng.cheetah.api.router.RouterManager;
import com.caisheng.cheetah.tools.event.EventBus;
import com.caisheng.cheetah.tools.event.EventConsumer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRouterManager extends EventConsumer implements RouterManager<LocalRouter> {
    private final static Logger logger = LoggerFactory.getLogger(LocalRouterManager.class);
    private final static Map<Integer, LocalRouter> EMPTY = new HashMap<>(0);

    //本地路由列表
    private final static Map<String, Map<Integer, LocalRouter>> routers = new ConcurrentHashMap<>();


    @Override
    public LocalRouter register(String userId, LocalRouter localRouter) {
        logger.info("register local router success userId={},localrouter={}", userId, localRouter);
        return routers.computeIfAbsent(userId, s -> new HashMap<>(0)).put(localRouter.getClientType(), localRouter);
    }

    @Override
    public boolean unRegister(String userId, int clientType) {
        LocalRouter localRouter = routers.getOrDefault(userId, EMPTY).remove(userId);
        logger.info("remove local router success userId={},localrouter={}", userId, localRouter);
        return true;
    }

    @Override
    public Set<LocalRouter> lookupAll(String userId) {
        Collection<LocalRouter> values = routers.getOrDefault(userId, EMPTY).values();
        return new HashSet<>(values);
    }

    @Override
    public LocalRouter lookup(String userId, int clientType) {
        LocalRouter localRouter = routers.getOrDefault(userId, EMPTY).get(clientType);
        logger.info("lookup local router userId={},clientType={},localRouter={}", userId, clientType, localRouter);
        return localRouter;
    }

    /**
     * 监听连接关闭，清理相关路由
     *
     * @param connectionCloseEvent
     */
    @Subscribe
    @AllowConcurrentEvents
    private void on(ConnectionCloseEvent connectionCloseEvent) {
        Connection connection = connectionCloseEvent.getConnection();
        if (connection == null) {
            return;
        }

        SessionContext sessionConext = connection.getSessionConext();
        String userId = sessionConext.getUserId();
        if (userId == null) {
            return;
        }
        byte clientType = sessionConext.getClientType();

        LocalRouter localRouter = lookup(userId, clientType);
        if (localRouter == null) {
            return;
        }

        String id = connection.getId();
        if (id.equals(localRouter.getRouterValue().getId())) {
            unRegister(userId, clientType);
            EventBus.post(new UserOfflineEvent(connection, userId));//触发用户下线事件
            logger.info("clean disconnected local router,userId={},clientType={},localRouter={}", userId, clientType, localRouter);
        } else {
            logger.info("clean disconnected local route, not clean:userId={}, localRouter={}", userId, localRouter);
        }




    }

    public static Map<String, Map<Integer, LocalRouter>> getRouters() {
        return routers;
    }
}
