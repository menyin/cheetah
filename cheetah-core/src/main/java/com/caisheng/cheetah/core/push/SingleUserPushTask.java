package com.caisheng.cheetah.core.push;

import com.caisheng.cheetah.api.connection.Connection;
import com.caisheng.cheetah.api.spi.push.IPushMessage;
import com.caisheng.cheetah.common.TimeLine;
import com.caisheng.cheetah.common.message.PushMessage;
import com.caisheng.cheetah.common.qps.FlowControl;
import com.caisheng.cheetah.common.qps.GlobalFlowControl;
import com.caisheng.cheetah.common.router.RemoteRouter;
import com.caisheng.cheetah.common.router.RemoteRouterManager;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.ack.AckTask;
import com.caisheng.cheetah.core.router.LocalRouter;
import com.caisheng.cheetah.core.router.LocalRouterManager;
import com.caisheng.cheetah.tools.log.Logs;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ScheduledExecutorService;

public class SingleUserPushTask implements PushTask, ChannelFutureListener {
    private final FlowControl flowControl;
    private IPushMessage pushMessage;
    private int messageId;
    private long start;
    private final TimeLine timeLine = new TimeLine();
    private final CheetahServer cheetahServer;


    public SingleUserPushTask(CheetahServer cheetahServer, IPushMessage pushMessage, GlobalFlowControl globalFlowControl) {
        this.cheetahServer = cheetahServer;
        this.pushMessage = pushMessage;
        this.flowControl = globalFlowControl;
        this.timeLine.begin("push-center-begin");
    }


    @Override
    public ScheduledExecutorService getExecutor() {
        return this.pushMessage.getConnection().getChannel().eventLoop();
    }

    @Override
    public void run() {
        if (checkTimeout()) {
            return;
        }
        if (checkLocal(pushMessage)) return;
        checkRemote(pushMessage);
    }

    private void checkRemote(IPushMessage pushMessage) {
        String userId = pushMessage.getUserId();
        int clientType = pushMessage.getClientType();

        RemoteRouterManager remoteRouterManager = cheetahServer.getRouterCenter().getRemoteRouterManager();
        RemoteRouter remoteRouter = remoteRouterManager.lookup(userId, clientType);
        if (remoteRouter == null || remoteRouter.isOffline()) {
            cheetahServer.getPushCenter().getPushListener().onOffline(pushMessage, timeLine.end("offline-end").getTimePoints());
            Logs.PUSH.info("[SingleUserPush] remote router not exists or qishou user offline ,message={}",pushMessage);
            return;
        }
        if (remoteRouter.getRouterValue().isThisMachine(cheetahServer.getGatewayServerNode().getHost(), cheetahServer.getGatewayServerNode().getPort())) {

            cheetahServer.getPushCenter().getPushListener().onOffline(pushMessage, timeLine.end("offline-end").getTimePoints());

            //删除失效的远程缓存
            cheetahServer.getRouterCenter().getRemoteRouterManager().unRegister(userId, clientType);

            Logs.PUSH.info("[SingleUserPush] find remote router in this pc, but local router not exists, userId={}, clientType={}, router={}"
                    , userId, clientType, remoteRouter);

            return;
        }
        lionServer.getPushCenter().getPushListener().onRedirect(message, timeLine.end("redirect-end").getTimePoints());
        Logs.PUSH.info("[SingleUserPush] find router in another pc, userId={}, clientType={}, router={}", userId, clientType, remoteRouter);

    }

    private boolean checkLocal(IPushMessage pushMessage) {
        String userId = pushMessage.getUserId();
        int clientType = pushMessage.getClientType();
        LocalRouterManager localRouterManager = this.cheetahServer.getRouterCenter().getLocalRouterManager();
        LocalRouter localRouter = localRouterManager.lookup(userId, clientType);

        if (localRouter == null) return false;
        Connection connection = localRouter.getRouterValue();
        if (!connection.isConnected()) {
            Logs.PUSH.warn("[SingleUserPush] find local router but conn disconnected, message={}, conn={}", message, connection);
            //删除已经失效的本地路由
            cheetahServer.getRouterCenter().getLocalRouterManager().unRegister(userId, clientType);
            return false;
        }
        if (!connection.getChannel().isWritable()) {
            cheetahServer.getPushCenter().getPushListener().onFailure(pushMessage, timeLine.failureEnd().getTimePoints());
            Logs.PUSH.error("[SingleUserPush] push message to connect failure,tcp sender too busy,message={},conn={}", pushMessage, connection);
            return true;
        }
        if (flowControl.checkQps()) {
            this.timeLine.addTimePoint("before-send");
            PushMessage pushmessageSwitch = PushMessage.build(connection);
            pushmessageSwitch.getPacket().addFlag(pushMessage.getFlags());
            this.messageId = pushmessageSwitch.getPacket().getSessionId();
            pushmessageSwitch.send(this);
        } else {
            cheetahServer.getPushCenter().delayTask(flowControl.getDelay(), this);
        }
        return true;
    }

    private boolean checkTimeout() {
        if (start > 0) {
            if (System.currentTimeMillis() - start > pushMessage.getTimeoutMills()) {
                //this.cheetahServer.getPushCenter().getPushListener().onTimeout(pushMessage,timeLine.timeoutEnd().getTimePoints());
                Logs.PUSH.info("[SingleUserPush] push message to connect timeout,timeline={},message={}", timeLine, pushMessage);
                return true;
            }
        } else {
            start = System.currentTimeMillis();
        }
        return false;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (checkTimeout()) {
            return;
        }
        if (future.isSuccess()) {//推送成功

            if (pushMessage.isNeedAck()) {//需要客户端ACK, 添加等待客户端响应ACK的任务
                addAckTask(messageId);//cny_note messageId是骑手与服务端连接的session
            } else {
                cheetahServer.getPushCenter().getPushListener().onSuccess(pushMessage, timeLine.successEnd().getTimePoints());
            }

            Logs.PUSH.info("[SingleUserPush] push message to connect success, timeLine={}, message={}", timeLine, pushMessage);

        } else {//推送失败

            cheetahServer.getPushCenter().getPushListener().onFailure(pushMessage, timeLine.failureEnd().getTimePoints());

            Logs.PUSH.error("[SingleUserPush] push message to connect failure, message={}, conn={}", pushMessage, future.channel());
        }
    }

    private void addAckTask(int messageId) {
        this.timeLine.addTimePoint("waiting-ack");
        pushMessage.finalized();
        AckTask ackTask = AckTask.from(messageId);
        ackTask.setAckCallback(new PushAckCallback(pushMessage,timeLine,cheetahServer.getPushCenter()));
        cheetahServer.getPushCenter().getAckTaskQueue().add(task, message.getTimeoutMills() - (int) (System.currentTimeMillis() - start));
    }
}
