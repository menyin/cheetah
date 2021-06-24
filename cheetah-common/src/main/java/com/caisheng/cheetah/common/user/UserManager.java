package com.caisheng.cheetah.common.user;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.api.spi.common.CacheManager;
import com.caisheng.cheetah.api.spi.common.CacheManagerFactory;
import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQClientFactory;
import com.caisheng.cheetah.common.CacheKeys;
import com.caisheng.cheetah.common.router.MQKickRemoteMsg;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.common.router.RemoteRouterManager;
import com.caisheng.cheetah.tools.config.ConfigTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;


public class UserManager {
    private final static Logger logger = LoggerFactory.getLogger(UserManager.class);
    private final static String onlineUserListKey = CacheKeys.getOnlineUserListKey(ConfigTools.getPublicIp());
    private final CacheManager cacheManager = CacheManagerFactory.create();
    private final MQClient mqClient = MQClientFactory.create();
    private final RemoteRouterManager remoteRouterManager;

    public UserManager(RemoteRouterManager remoteRouterManager) {
        this.remoteRouterManager = remoteRouterManager;
    }

    public void kickUser(String userId) {
        kickUser(userId, -1);
    }
    public void kickUser(String userId,int clientType) {
        Set<RemoteRouter> remoteRouters = this.remoteRouterManager.lookupAll(userId);
        if (remoteRouters != null) {
            remoteRouters.forEach(remoteRouter -> {
                ClientLocation clientLocation = remoteRouter.getRouterValue();
                if (clientType == -1 || clientType == clientLocation.getClientType()) {
                    MQKickRemoteMsg mqKickRemoteMsg = new MQKickRemoteMsg();
                    mqKickRemoteMsg.setUserId(userId);
                    mqKickRemoteMsg.setClientType(clientLocation.getClientType());
                    mqKickRemoteMsg.setDeviceId(clientLocation.getDeviceId());
                    mqKickRemoteMsg.setConnId(clientLocation.getConnId());
                    mqKickRemoteMsg.setTargetServer(clientLocation.getHost());
                    mqKickRemoteMsg.setTargetPort(clientLocation.getPort());
                    //发布消息到MQ上
                    mqClient.publish(Constants.getKickChannel(clientLocation.getHostAndPort()),mqKickRemoteMsg);
                }
            });
        }
    }

    public void cleanOnlineUserList(){
        cacheManager.del(onlineUserListKey);
    }

    public void addToOnlineUserList(String userId){
        cacheManager.zAdd(onlineUserListKey,userId);
        logger.info("user online {}", userId);
    }
    public void remFromOnlineUserList(String userId){
        cacheManager.zRem(onlineUserListKey,userId);
        logger.info("user offline {}", userId);
    }
    //在线用户数量
    public long getOnlineUserNum(){
        Long num = cacheManager.zCard(onlineUserListKey);
        return num==null?0:num;
    }

    //指定公网IP内的在线用户数量
    public long getOnlineUserNum(String publicIP) {
        String online_key = CacheKeys.getOnlineUserListKey(publicIP);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    //部分在线用户列表
    public List<String> getOnlineUserList(String publicIP, int start, int end) {
        String key = CacheKeys.getOnlineUserListKey(publicIP);
        return cacheManager.zRange(key, start, end, String.class);
    }


}
