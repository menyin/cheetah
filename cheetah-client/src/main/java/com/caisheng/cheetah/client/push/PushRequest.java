package com.caisheng.cheetah.client.push;

import com.caisheng.cheetah.api.Constants;
import com.caisheng.cheetah.api.push.*;
import com.caisheng.cheetah.api.router.ClientLocation;
import com.caisheng.cheetah.client.CheetahClient;
import com.caisheng.cheetah.common.TimeLine;
import com.caisheng.cheetah.common.message.gateway.GatewayPushMessage;
import com.caisheng.cheetah.common.push.GatewayPushResult;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.tools.Jsons;
import javafx.animation.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public class PushRequest extends FutureTask<PushResult> {
    private final static Logger logger = LoggerFactory.getLogger(PushRequest.class);
    private final static Callable<PushResult> NONE = () -> new PushResult(PushResult.CODE_FAILURE);
    private final AtomicReference<Status> status = new AtomicReference<>(Status.init);
    private final TimeLine timeLine = new TimeLine();
    private final CheetahClient cheetahClient;

    private AckModel ackModel;
    private Set<String> tags;
    private String condition;
    private PushCallback pushCallback;
    private String userId;
    private byte[] content;
    private int timeout;
    private ClientLocation clientLocation;
    private int sessionId;
    private String taskId;
    private Future<?> future;
    private PushResult pushResult;


    public PushRequest(CheetahClient cheetahClient) {
        super(NONE);
        this.cheetahClient = cheetahClient;
    }

    public static PushRequest build(CheetahClient cheetahClient, PushContext pushContext) {
        byte[] content = pushContext.getContent();
        PushMsg pushMsg = pushContext.getPushMsg();
        if (pushMsg != null) {
            String json = Jsons.toJson(pushMsg);
            if (json != null) {
                content = json.getBytes(Constants.UTF_8);
            }
        }
        Objects.requireNonNull(content, "push content can not be null");
        PushRequest pushRequest = new PushRequest(cheetahClient);
        pushRequest.setAckModel(pushContext.getAckModel());
        pushRequest.setUserId(pushContext.getUserId());
        pushRequest.setTags(pushContext.getTags());
        pushRequest.setCondition(pushContext.getCondition());
        pushRequest.setTaskId(pushContext.getTaskId());
        pushRequest.setContent(content);
        pushRequest.setTimeout(pushContext.getTimeout());
        pushRequest.setPushCallback(pushContext.getPushCallback());
        return pushRequest;
    }


    public FutureTask<PushResult> send(RemoteRouter remoteRouter) {
        this.timeLine.begin();
        sendToConnectionServer(remoteRouter);
        return this;
    }

    public FutureTask<PushResult> broadcast() {
        timeLine.begin();

        boolean success = cheetahClient.getGatewayConnectionFactory()
                .broadcast(
                        connection -> {
                            GatewayPushMessage gatewayPushMessage = GatewayPushMessage.build(connection);
                            gatewayPushMessage.setUserId(this.userId);
                            gatewayPushMessage.setContent(this.content);
                            gatewayPushMessage.setTags(this.tags);
                            gatewayPushMessage.addFlag(ackModel.getFlag());
                            gatewayPushMessage.setCondition(condition);
                            gatewayPushMessage.addFlag(ackModel.getFlag());
                            return gatewayPushMessage;
                        },
                        gatewayPushMessage -> {
                            gatewayPushMessage.sendRaw(f -> {
                                if (f.isSuccess()) {
                                    logger.debug("send broadcast to gateway server success, userId={}, conn={}", userId, f.channel());
                                } else {
                                    failure();
                                    logger.error("send broadcast to gateway server failure, userId={}, conn={}", userId, f.channel(), f.cause());
                                }
                            });

                            if (gatewayPushMessage.getTaskId() == null) {
                                sessionId = gatewayPushMessage.getPacket().getSessionId();
                                future = cheetahClient.getPushRequestBus().put(sessionId, PushRequest.this);
                            } else {
                                success();
                            }
                        }
                );

        if (!success) {
            logger.error("get gateway connection failure when broadcast.");
            failure();
        }

        return this;
    }


    private void sendToConnectionServer(RemoteRouter remoteRouter) {
        this.timeLine.addTimePoint("lookup-remote");
        if (remoteRouter != null) {
            this.clientLocation = remoteRouter.getRouterValue();
        }
        if (remoteRouter == null || remoteRouter.isOffline()) {
            offline();
            return;
        }

        this.timeLine.addTimePoint("check-gateway-connection");
        boolean success = cheetahClient.getGatewayConnectionFactory().send(
                clientLocation.getHostAndPort(),
                connection -> {
                    GatewayPushMessage gatewayPushMessage = GatewayPushMessage.build(connection);
                    gatewayPushMessage.setUserId(this.userId);
                    gatewayPushMessage.setContent(this.content);
                    gatewayPushMessage.setClientType(this.clientLocation.getClientType());
                    gatewayPushMessage.setTimeout(this.timeout - 500);
                    gatewayPushMessage.setTags(this.tags);
                    gatewayPushMessage.addFlag(ackModel.getFlag());
                    return gatewayPushMessage;

                },
                pushMessage -> {
                    this.timeLine.addTimePoint("send-to-gateway-begin");
                    pushMessage.sendRaw(future -> {
                        this.timeLine.addTimePoint("send-to-gateway-end");
                        if (future.isSuccess()) {
                            logger.debug("send to gateway server success,clientLocation={},channel={}", clientLocation, future.channel());
                        } else {
                            logger.debug("send to gateway server failure,clientLocation={},channel={}", clientLocation, future.channel(), future.cause());
                        }
                        if (pushMessage.getTaskId() == null) {
                            this.sessionId = pushMessage.getPacket().getSessionId();
                            this.cheetahClient.getPushRequestBus().put(sessionId, this);
                        } else {
                            success();
                        }

                    });
                }
        );
        if (!success) {
            logger.error("get gateway connection failure,clientLocation={}", clientLocation);
            failure();
        }

    }

    private void success() {
        submit(Status.success);
    }

    private void failure() {
        submit(Status.failure);
    }


    private void offline() {
        submit(Status.offline);
    }

    private void timeout() {
        submit(Status.timeout);
    }

    public void onFailure() {
        failure();
    }

    public void onRedirect() {
        this.timeLine.addTimePoint("redirect");
        logger.warn("user remote router changed,userId={},clienLocation={}", userId, clientLocation);
        cheetahClient.getCacheRemoteRouterManager().invalidateLocalCache(userId);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            send(this.cheetahClient.getCacheRemoteRouterManager().lookup(userId, clientLocation.getClientType()));
        }
    }

    public void onSuccess(GatewayPushResult result) {
        if (result != null) timeLine.addTimePoints(result.timePoints);
        submit(Status.success);
    }

    public FutureTask<PushResult> onOffline() {
        offline();
        return this;
    }

    private void submit(Status status) {
        if (this.status.compareAndSet(Status.init, status)) {
            boolean isTimeout = status == Status.timeout;
            if (future != null & !isTimeout) {
                future.cancel(true);
            }

            this.timeLine.end();

            super.set(getPushResult());

            if (this.pushCallback != null) {
                if (isTimeout) {
                    this.pushCallback.onResult(getPushResult());
                } else {
                    cheetahClient.getPushRequestBus().syncCall(this);
                }
            }

        }
        logger.info("push request {} end,{},{},{}", status, userId, clientLocation, timeLine);
    }

    @Override
    public void run() {
        if (this.status.get() == Status.init) {
            timeout();
        } else {
            this.pushCallback.onResult(getPushResult());
        }

    }


    private enum Status {
        init,
        success,
        failure,
        offline,
        timeout
    }


    /*******  get set ********/

    public AckModel getAckModel() {
        return ackModel;
    }

    public void setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String conditions) {
        this.condition = conditions;
    }

    public PushCallback getPushCallback() {
        return pushCallback;
    }

    public void setPushCallback(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public ClientLocation getClientLocation() {
        return clientLocation;
    }

    public void setClientLocation(ClientLocation clientLocation) {
        this.clientLocation = clientLocation;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    private PushResult getPushResult() {
        if (this.pushResult == null) {
            this.pushResult = new PushResult(this.status.get().ordinal());
            this.pushResult.setUserId(this.userId);
            this.pushResult.setClientLocation(this.clientLocation);
            this.pushResult.setTimeLine(this.timeLine.getTimePoints());
        }
        return pushResult;
    }

    public void setPushResult(PushResult pushResult) {
        this.pushResult = pushResult;
    }

    @Override
    public String toString() {
        return "PushRequest{" +
                "content='" + (content == null ? -1 : content.length) + '\'' +
                ", userId='" + userId + '\'' +
                ", timeout=" + timeout +
                ", clientLocation=" + clientLocation +
                '}';
    }
}
