package com.caisheng.cheetah.common.qps;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalFlowControl implements FlowControl {
    private final int limit;
    private final int maxLimit;
    private final long duration;
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger total = new AtomicInteger();
    private final long start0 = System.nanoTime();
    private volatile long start;

    public GlobalFlowControl(int qps) {
        this(qps, Integer.MAX_VALUE, 1000);
    }

    public GlobalFlowControl(int limit, int maxLimit, int duration) {
        this.limit = limit;
        this.maxLimit = maxLimit;
        this.duration = TimeUnit.MICROSECONDS.toNanos(duration);
    }

    @Override
    public void reset() {
        this.count.set(0);
        this.start = System.nanoTime();

    }

    @Override
    public int total() {
        return this.total.get();
    }

    @Override
    public boolean checkQps() throws OverFlowException {
        if (count.incrementAndGet() < this.limit) {
            total.incrementAndGet();
            return true;
        }
        if (maxLimit > 0 && total.get() > maxLimit) {
            throw new OverFlowException(true);
        }

        if (System.nanoTime() - start > duration) {
            reset();
            total.incrementAndGet();
            return true;
        }

        return false;
    }

    @Override
    public long getDelay() {
        return duration-(System.nanoTime()-start);
    }

    @Override
    public int qps() {
        return (int) (TimeUnit.SECONDS.toNanos(total.get()) / (System.nanoTime() - start0));
    }

    @Override
    public String report() {
        return String.format("total:%d, count:%d, qps:%d", total.get(), count.get(), qps());
    }
}
