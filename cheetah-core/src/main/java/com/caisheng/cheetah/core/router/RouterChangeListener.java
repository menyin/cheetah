package com.caisheng.cheetah.core.router;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.event.RouterChangeEvent;
import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.api.router.Router;
import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQClientFactory;
import com.caisheng.cheetah.api.spi.common.MQMessageReceiver;
import com.caisheng.cheetah.common.message.KickUserMessage;
import com.caisheng.cheetah.common.router.KickRemoteMsg;
import com.caisheng.cheetah.common.router.MQKickRemoteMsg;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.tools.Jsons;
import com.caisheng.cheetah.tools.config.CC;
import com.caisheng.cheetah.tools.config.ConfigTools;
import com.caisheng.cheetah.tools.event.EventConsumer;
import com.caisheng.cheetah.tools.log.Logs;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class RouterChangeListener extends EventConsumer implements MQMessageReceiver {
    private boolean udpGateway = CC.lion.net.udpGateway();
    private CheetahServer cheetahServer;
    private MQClient mqClient;
    private String kick_channel;

    public RouterChangeListener(CheetahServer cheetahServer) {
        this.cheetahServer = cheetahServer;
//        this.kick_channel= Constants.KICK_CHANNEL_PREFIX+this.cheetahServer.getGatewayServerNode().hostAndPort(); TODO
        mqClient = MQClientFactory.create();
        mqClient.init(this.cheetahServer);
        mqClient.subscribe("", this);


    }

    @Subscribe
    @AllowConcurrentEvents
    private void on(RouterChangeEvent routerChangeEvent) {
        String userId = routerChangeEvent.getUserId();
        Router<?> router = routerChangeEvent.getRouter();
        if (router.getRouterType() == Router.RouterType.LOCAL) {
            sendKickUserMessage2Client(userId, (LocalRouter) router);
        }
        if (router.getRouterType() == Router.RouterType.REMOTE) {
            sendKickUserMessage2MQ(userId, (RemoteRouter) router);
        }

    }

    private void sendKickUserMessage2Client(String userId, LocalRouter router) {
        Connection connection = router.getRouterValue();
        SessionContext sessionConext = connection.getSessionConext();
        KickUserMessage kickUserMessage = KickUserMessage.build(connection);
        kickUserMessage.setDeviceId(sessionConext.getDeviceId());
        kickUserMessage.setUserId(sessionConext.getUserId());
        kickUserMessage.send(future -> {
            if (future.isSuccess()) {
                Logs.CONN.info("kick local connection success, userId={}, router={}, conn={}", userId, router, connection);
            } else {
                Logs.CONN.warn("kick local connection failure, userId={}, router={}, conn={}", userId, router, connection);
            }
        });

    }

    private void sendKickUserMessage2MQ(String userId, RemoteRouter router) {
        ClientLocation clientLocation = router.getRouterValue();
        if (cheetahServer.isTargetMachine(clientLocation.getHost(), clientLocation.getPort())) {
            Logs.CONN.info("kick remote router in local pc,ignore remote broadcast,userId={}", userId);
        }

        if (this.udpGateway) {//TODO

        } else {
            MQKickRemoteMsg mqKickRemoteMsg = new MQKickRemoteMsg();
            mqKickRemoteMsg.setUserId(userId);
            mqKickRemoteMsg.setClientType(clientLocation.getClientType());
            mqKickRemoteMsg.setConnId(clientLocation.getConnId());
            mqKickRemoteMsg.setDeviceId(clientLocation.getDeviceId());
            mqKickRemoteMsg.setTargetServer(clientLocation.getHost());
            mqKickRemoteMsg.setTargetPort(clientLocation.getPort());
            mqClient.publish(Constants.getKickChannel(clientLocation.getHostAndPort()), mqKickRemoteMsg);
        }

    }

    @Override
    public void receive(String topic, Object message) {
        if (this.kick_channel.equals(topic)) {//
            KickRemoteMsg kickRemoteMsg = Jsons.fromJson(message.toString(), MQKickRemoteMsg.class);
            if (kickRemoteMsg != null) {
                onReceiveKickRemoteMsg(kickRemoteMsg);
            } else {
                Logs.CONN.warn("receive error kick message ={}", kickRemoteMsg);
            }
        } else {
            Logs.CONN.warn("receive error redis channel ={}", topic);
        }
    }

    private void onReceiveKickRemoteMsg(KickRemoteMsg kickRemoteMsg) {
        //1.如果当前机器不是目标机器，直接忽略
        if (!cheetahServer.isTargetMachine(kickRemoteMsg.getTargetServer(), kickRemoteMsg.getTargetPort())) {
            Logs.CONN.error("receive kick remote msg, target server error, localIp={}, msg={}", ConfigTools.getLocalIp(), kickRemoteMsg);
            return;
        }
        String userId = kickRemoteMsg.getUserId();
        int clientType = kickRemoteMsg.getClientType();
        LocalRouterManager localRouterManager = cheetahServer.getRouterCenter().getLocalRouterManager();
        LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
        if (localRouter != null) {
            Logs.CONN.info("receive kick remote msg, msg={}", kickRemoteMsg);
            if (localRouter.getRouterValue().getId().equals(kickRemoteMsg.getConnId())) {
                sendKickUserMessage2Client(userId, localRouter);
            }

        } else {
            Logs.CONN.warn("kick router failure can't find local router,msg={}",kickRemoteMsg);
        }



    }
}
