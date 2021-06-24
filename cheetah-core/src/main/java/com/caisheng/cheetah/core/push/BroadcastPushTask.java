package com.caisheng.cheetah.core.push;

import com.caisheng.cheetah.api.common.Condition;
import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.connection.SessionContext;
import com.caisheng.cheetah.api.spi.push.IPushMessage;
import com.caisheng.cheetah.common.TimeLine;
import com.caisheng.cheetah.common.condition.AwaysPassCondition;
import com.caisheng.cheetah.common.condition.ScriptCondition;
import com.caisheng.cheetah.common.condition.TagsCondition;
import com.caisheng.cheetah.common.message.PushMessage;
import com.caisheng.cheetah.common.qps.FlowControl;
import com.caisheng.cheetah.common.qps.OverFlowException;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.router.LocalRouter;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.ChannelFuture;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BroadcastPushTask implements PushTask {
    private final long begin = System.currentTimeMillis();
    private final AtomicInteger finishTasks = new AtomicInteger(0);
    private final TimeLine timeline = new TimeLine();
    private final Set<String> successUserIds = new HashSet<>(1024);
    private final FlowControl flowControl;
    private final IPushMessage pushMessage;
    private final Condition condition;
    private final CheetahServer cheetahServer;
    private final Iterator<Map.Entry<String, Map<Integer, LocalRouter>>> iterator;


    public BroadcastPushTask(CheetahServer cheetahServer, IPushMessage pushMessage, FlowControl flowControl) {
        this.cheetahServer = cheetahServer;
        this.pushMessage = pushMessage;
        this.flowControl = flowControl;
        this.condition=pushMessage.getCondition();
        this.iterator=cheetahServer.getRouterCenter().getLocalRouterManager().getRouters().entrySet().iterator();
        this.timeline.begin("push-center-begin");

    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return this.pushMessage.getConnection().getChannel().eventLoop();
    }

    @Override
    public void run() {
        flowControl.reset();
        boolean done=broadcast();
        if (done) {
            if (finishTasks.addAndGet(flowControl.total())==0) {
                report();
            }
        }else{
            cheetahServer.getPushCenter().delayTask(flowControl.getDelay(), this);
        }
        flowControl.end(successUserIds.toArray(new String[successUserIds.size()]));
    }

    private void report() {
        Logs.PUSH.info("[broadcast] task finished,cost={},message={}",System.currentTimeMillis()-begin,pushMessage);
        //cheetahServer.getPushCenter().getPushListener().onBroadcastSuccess(pushMessage,timeline.end().getTimePoints());//TODO
    }

    private boolean broadcast() {
        try {
            this.iterator.forEachRemaining(entry->{
                String userId = entry.getKey();
                entry.getValue().forEach((clientType,localRouter)->{
                    Connection connection = localRouter.getRouterValue();
                    if (checkCondition(this.condition, connection)) {
                        if (connection.isConnected()) {
                            PushMessage pushMessageSwitch = PushMessage.build(connection);
                            pushMessageSwitch.setContent(pushMessage.getContent());
                            pushMessageSwitch.send(channelFuture -> {
                                operationComplete(channelFuture, userId);
                            });
                            if(!flowControl.checkQps()){
                                throw new OverFlowException(false);
                            }
                        } else {
                            Logs.PUSH.warn("[broadcast] find router in local but connection disconnect,message={},connection={}", pushMessage, connection);
                            cheetahServer.getRouterCenter().getLocalRouterManager().unRegister(userId, clientType);
                        }
                    }
                });
            });
        } catch (OverFlowException ex) {
            //超出最大限制，或者遍历完毕，结束广播
            return ex.isOverMaxLimit() || !iterator.hasNext();
        }
        return this.iterator.hasNext();
    }

    private void operationComplete(ChannelFuture channelFuture, String userId) {
        if (channelFuture.isSuccess()) {
            successUserIds.add(userId);
            Logs.PUSH.warn("[broadcast] push message to connect success.userId={},message={}", userId, pushMessage);
        } else {
            Logs.PUSH.warn("[broadcast] push message to connect failure.userId={},message={},connection={}", userId, pushMessage, channelFuture.channel());
        }
    }

    private boolean checkCondition(Condition condition, Connection connection) {
        SessionContext sessionConext = connection.getSessionConext();
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("userId", sessionConext.getUserId());
        env.put("clientType", sessionConext.getClientType());
        env.put("tags", sessionConext.getTags());
        env.put("clientVersion", sessionConext.getClientVersion());
        env.put("osName", sessionConext.getOsName());
        env.put("osVersion", sessionConext.getOsVersion());
        return this.condition.test(env);
    }
}
