package com.caisheng.cheetah.common.qps;

import com.caisheng.cheetah.api.push.BroadcastController;
import com.caisheng.cheetah.common.push.RedisBroadcastController;

import java.util.concurrent.TimeUnit;

public class RedisFlowControl implements FlowControl {
    private BroadcastController controller;
    private final long start0 = System.nanoTime();
    private final long duration = TimeUnit.SECONDS.toNanos(1);
    private final int maxLimit;
    private int limit;
    private int count;
    private int total;
    private long start;

    public RedisFlowControl(String taskId) {
        this(taskId, Integer.MAX_VALUE);
    }

    public RedisFlowControl(String taskId, int maxLimit) {
        this.controller = new RedisBroadcastController(taskId);
        this.maxLimit = maxLimit;
        this.limit = controller.qps();
    }

    @Override
    public void reset() {
        count = 0;
        start = System.nanoTime();
    }

    @Override
    public int total() {
        return total;
    }

    @Override
    public boolean checkQps() throws OverFlowException {
        if (count < limit) {
            count++;
            total++;
            return true;
        }
        if (total() > maxLimit) {
            throw new OverFlowException(true);
        }
        if (System.nanoTime() - start > duration) {
            reset();
            total++;
            return true;
        }
        if (controller.isCanceled()) {
            throw new OverFlowException(true);
        } else {
            limit = controller.qps();
        }
        return false;

    }

    @Override
    public long getDelay() {
        return duration-(System.nanoTime()-start);
    }

    @Override
    public int qps() {
        return (int) (TimeUnit.SECONDS.toNanos(total) / (System.nanoTime() - start0));
    }

    @Override
    public void end(Object results) {
        int t=total;
        if (total > 0) {
            total=0;
            controller.incSendCount(t);
        }
        if (results != null&&results instanceof String[]) {
            controller.success((String[]) results);

        }
    }

    @Override
    public String report() {
        return String.format("total:%d, count:%d, qps:%d", total, count, qps());
    }
    public BroadcastController getController() {
        return controller;
    }
}
