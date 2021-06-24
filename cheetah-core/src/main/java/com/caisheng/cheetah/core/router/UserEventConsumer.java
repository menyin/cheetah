package com.caisheng.cheetah.core.router;

import com.caisheng.cheetah.api.event.Topics;
import com.caisheng.cheetah.api.event.UserOfflineEvent;
import com.caisheng.cheetah.api.event.UserOnlineEvent;
import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQClientFactory;
import com.caisheng.cheetah.common.router.RemoteRouterManager;
import com.caisheng.cheetah.common.user.UserManager;
import com.caisheng.cheetah.tools.event.EventConsumer;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public class UserEventConsumer extends EventConsumer {
    private final MQClient mqClient = MQClientFactory.create();//
    private final UserManager userManager;

    public UserEventConsumer(RemoteRouterManager remoteRouterManager) {
        userManager = new UserManager(remoteRouterManager);
    }

    @Subscribe
    @AllowConcurrentEvents
    private void on(UserOfflineEvent userOfflineEvent) {
        userManager.addToOnlineUserList(userOfflineEvent.getUserId());
        mqClient.publish(Topics.OFFLINE_CHANNEL, userOfflineEvent.getUserId());
    }

    @Subscribe
    @AllowConcurrentEvents
    private void on(UserOnlineEvent userOnlineEvent) {
        userManager.remFromOnlineUserList(userOnlineEvent.getUserId());
        mqClient.publish(Topics.ONLINE_CHANNEL, userOnlineEvent.getUserId());
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
