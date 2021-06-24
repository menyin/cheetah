package com.caisheng.cheetah.core.ack;

public interface AckCallback {
    void onSuccess(AckTask ackTask);
    void onTimeout(AckTask ackTask);
}
