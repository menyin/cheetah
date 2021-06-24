package com.caisheng.cheetah.core.ack;

import java.util.concurrent.Future;

public final class AckTask implements Runnable {
    private final int ackMessageId;
    private AckTaskQueue ackTaskQueue;
    private AckCallback ackCallback;
    private Future<?> timeoutFuture;


    public AckTask(int ackMessageId) {
        this.ackMessageId = ackMessageId;
    }

    public static AckTask from(int ackMessageId) {
        return new AckTask(ackMessageId);
    }

    public Integer getAckMessageId() {
        return this.ackMessageId;
    }

    private boolean tryDone() {
        return this.timeoutFuture.cancel(true);
    }

    public void setAckCallback(AckCallback ackCallback) {
        this.ackCallback = ackCallback;
    }

    public void setAckTaskQueue(AckTaskQueue ackTaskQueue) {
        this.ackTaskQueue = ackTaskQueue;
    }

    public void onResponse() {
        if(tryDone()){
            ackCallback.onSuccess(this);
            ackCallback=null;
        }
    }

    @Override
    public void run() {
        onTimeout();
    }

    private void onTimeout() {
        AckTask ackTask = this.ackTaskQueue.getAndRemove(this.ackMessageId);
        if (ackTask != null&&tryDone()) {
            ackCallback.onTimeout(this);
            ackCallback=null;
        }

    }

    public void setTimeoutFuture(Future<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    @Override
    public String toString() {
        return "{" +
                ", ackMessageId=" + ackMessageId +
                '}';
    }
}
