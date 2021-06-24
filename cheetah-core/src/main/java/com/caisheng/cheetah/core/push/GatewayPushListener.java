package com.caisheng.cheetah.core.push;

import com.caisheng.cheetah.api.CheetahContext;
import com.caisheng.cheetah.api.spi.push.PushListener;
import com.caisheng.cheetah.api.spi.push.PushListenerFactory;
import com.caisheng.cheetah.common.ErrorCode;
import com.caisheng.cheetah.common.message.ErrorMessage;
import com.caisheng.cheetah.common.message.OkMessage;
import com.caisheng.cheetah.common.message.gateway.GatewayPushMessage;
import com.caisheng.cheetah.common.push.GatewayPushResult;
import com.caisheng.cheetah.tools.Jsons;
import com.caisheng.cheetah.tools.log.Logs;

import java.util.concurrent.ScheduledExecutorService;

import static com.caisheng.cheetah.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.caisheng.cheetah.common.push.GatewayPushResult.toJson;

public class GatewayPushListener implements PushListener<GatewayPushMessage>, PushListenerFactory<GatewayPushMessage> {
    private PushCenter pushCenter;

    @Override
    public void init(CheetahContext context) {
//        this.pushCenter=context.getPushCenter();//TODO

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage okMessage = OkMessage.from(message);
                    okMessage.setData(toJson(message,timePoints));
                    okMessage.sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to qishou connect success,but shangjia connect connection be closed. timePoints={},message={}", Jsons.toJson(timePoints),message);
        }
    }

    @Override
    public void onAckSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) { //cny_note message是商家推送给骑手的消息，message里的连接是商家和服务网关的连接
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage okMessage = OkMessage.from(message);
                    okMessage.setData(toJson(message,timePoints));
                    okMessage.sendRaw();
                }
            });

        } else {
            Logs.PUSH.warn("connect ack success, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onBroadcastSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage okMessage = OkMessage.from(message);
                    okMessage.sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("broadcast to connect finish, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onFailure(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage errorMessage = ErrorMessage.from(message);
                    errorMessage.setCode(PUSH_CLIENT_FAILURE.getErrorCode());
                    errorMessage.setData(toJson(message, timePoints));
                    errorMessage.sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to connect failure, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onOffline(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage errorMessage = ErrorMessage.from(message);
                            errorMessage.setCode(ErrorCode.OFFLINE.getErrorCode());
                            errorMessage.setData(toJson(message, timePoints));
                            errorMessage.sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to connect offline, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onRedirect(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            pushCenter.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage errorMessage = ErrorMessage.from(message);
                            errorMessage.setCode(ErrorCode.ROUTER_CHANGE.getErrorCode());
                            errorMessage.setData(toJson(message, timePoints));
                            errorMessage.sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to connect redirect, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onTimeout(GatewayPushMessage message, Object[] timePoints) {
        Logs.PUSH.warn("push message to connect timeout, timePoints={}, message={}"
                , Jsons.toJson(timePoints), message);
    }

    @Override
    public PushListener<GatewayPushMessage> get() {
        return this;
    }
}
