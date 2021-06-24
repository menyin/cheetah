package com.caisheng.cheetah.core.ack;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.core.CheetahServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AckTaskQueue extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(AckTaskQueue.class);
    public static final int DEFAULT_TIMEOUT = 3000;
    private final Map<Integer, AckTask> queue = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService;
    private CheetahServer cheetahServer;

    public AckTaskQueue(CheetahServer cheetahServer) {
        this.cheetahServer = cheetahServer;
    }

    @Override
    protected void doStart(Listener listener) {
        //scheduledExecutorService = this.cheetahServer... TODO
        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
        super.doStop(listener);
    }

    public void  add(AckTask ackTask,int timeout){
        this.queue.put(ackTask.getAckMessageId(), ackTask);
        ackTask.setAckTaskQueue(this);
        ScheduledFuture<?> scheduledFuture = this.scheduledExecutorService.schedule(ackTask, timeout > 0 ? timeout : DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        ackTask.setTimeoutFuture(scheduledFuture);
        logger.debug("one ack task add to queue,task={},timeout={}",ackTask,timeout);
    }

    public AckTask getAndRemove(int sessionId) {
        return queue.remove(sessionId);
    }



}
