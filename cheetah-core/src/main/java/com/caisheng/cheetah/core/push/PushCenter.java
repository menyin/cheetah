package com.caisheng.cheetah.core.push;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.api.spi.push.IPushMessage;
import com.caisheng.cheetah.api.spi.push.MessagePusher;
import com.caisheng.cheetah.api.spi.push.PushListener;
import com.caisheng.cheetah.api.spi.push.PushListenerFactory;
import com.caisheng.cheetah.common.qps.FastFlowControl;
import com.caisheng.cheetah.common.qps.FlowControl;
import com.caisheng.cheetah.common.qps.GlobalFlowControl;
import com.caisheng.cheetah.common.qps.RedisFlowControl;
import com.caisheng.cheetah.core.CheetahServer;
import com.caisheng.cheetah.core.ack.AckTaskQueue;
import com.caisheng.cheetah.tools.config.CC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PushCenter extends BaseService implements MessagePusher {
    private final Logger logger = LoggerFactory.getLogger(PushCenter.class);
    private GlobalFlowControl globalFlowControl = new GlobalFlowControl(
            CC.lion.push.flow_control.global.limit, CC.lion.push.flow_control.global.max, CC.lion.push.flow_control.global.duration);
    private AtomicLong taskNum = new AtomicLong();
    private AckTaskQueue ackTaskQueue;
    private CheetahServer cheetahServer;
    private PushListener<IPushMessage> pushListener;
    private PushTaskExecutor executor;

    public PushCenter(CheetahServer cheetahServer) {
        this.cheetahServer = cheetahServer;
        this.ackTaskQueue = new AckTaskQueue(cheetahServer);
    }


    @Override
    public void push(IPushMessage pushMessage) {
        if (pushMessage.isBroadcast()) {
            int limit = CC.lion.push.flow_control.broadcast.limit;
            int max = CC.lion.push.flow_control.broadcast.max;
            int duration = CC.lion.push.flow_control.broadcast.duration;
            FlowControl flowControl = pushMessage.getTaskId() == null ? new FastFlowControl(limit, max, duration) : new RedisFlowControl(pushMessage.getTaskId());
            addTask(new BroadcastPushTask(cheetahServer, pushMessage, flowControl));
        } else {
            addTask(new SingleUserPushTask(cheetahServer, pushMessage, globalFlowControl));
        }
    }

    public void addTask(PushTask pushTask) {
        this.executor.addTask(pushTask);
    }

    public void delayTask(long delay, PushTask pushTask) {
        this.executor.delayTask(delay, pushTask);
    }

    @Override
    public void doStart(Listener listener) throws Throwable{
       this.pushListener= PushListenerFactory.create();
        this.pushListener.init(cheetahServer);
        if (CC.lion.net.udpGateway() || CC.lion.thread.pool.push_task > 0) {
//            executor=new CustomJDKExecutor(cheetahServer.getMonitor().getThreadPoolManager().getPushTaskTime());//TODO
        }else{
            executor=new NettyEventLoopExecutor();
        }
        //MBeanRegistry.getInstance().register(new PushCenterBean(taskNum), null); TODO
        this.ackTaskQueue.start();
        logger.info("push center start success.");
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) {
        this.executor.shutdown();
        this.ackTaskQueue.stop();
        logger.info("push center stop success.");
        listener.onSuccess();
    }

    public PushListener<IPushMessage> getPushListener() {
        return pushListener;
    }


    public interface PushTaskExecutor {
        void shutdown();

        void addTask(PushTask pushTask);

        void delayTask(long delay, PushTask pushTask);
    }

    private class CustomJDKExecutor implements PushTaskExecutor {
        private final ScheduledExecutorService executorService;
        public CustomJDKExecutor(ScheduledExecutorService executorService) {
            this.executorService=executorService;
        }

        @Override
        public void shutdown() {
            this.executorService.shutdown();
        }

        @Override
        public void addTask(PushTask pushTask) {
            this.executorService.execute(pushTask);
        }

        @Override
        public void delayTask(long delay, PushTask pushTask) {
            this.executorService.schedule(pushTask, delay, TimeUnit.MILLISECONDS);
        }
    }


    private class NettyEventLoopExecutor implements PushTaskExecutor {
        @Override
        public void shutdown() {
            //无法关闭Netty的线程池，不属于管辖范围
        }

        @Override
        public void addTask(PushTask pushTask) {
            ScheduledExecutorService executor = pushTask.getExecutor();
            executor.execute(pushTask);
        }

        @Override
        public void delayTask(long delay, PushTask pushTask) {
            ScheduledExecutorService executor = pushTask.getExecutor();
            executor.schedule(pushTask, delay, TimeUnit.MILLISECONDS);
        }
    }
}
