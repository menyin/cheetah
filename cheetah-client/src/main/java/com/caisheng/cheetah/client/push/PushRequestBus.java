package com.caisheng.cheetah.client.push;

import com.caisheng.cheetah.api.service.BaseService;
import com.caisheng.cheetah.api.service.Listener;
import com.caisheng.cheetah.client.CheetahClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PushRequestBus extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(PushRequestBus.class);
    private final Map<Integer, PushRequest> requestQueue = new ConcurrentHashMap<>(1024);
    private ScheduledExecutorService executorService;
    private CheetahClient cheetahClient;

    public PushRequestBus(CheetahClient cheetahClient) {
        this.cheetahClient = cheetahClient;
    }

    public Future<?> put(int sessionId, PushRequest pushRequest) {
        requestQueue.put(sessionId, pushRequest);
        return executorService.schedule(pushRequest, pushRequest.getTimeout(), TimeUnit.MILLISECONDS);
    }

    public PushRequest getAndRemove(int sessionId) {
        return requestQueue.remove(sessionId);
    }

    public void syncCall(Runnable runnable) {
        executorService.execute(runnable);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
//        this.executorService=cheetahClient.getThreadPoolManager().getPushClientTimer();//线程池  TODO
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
        listener.onSuccess();

    }

}
