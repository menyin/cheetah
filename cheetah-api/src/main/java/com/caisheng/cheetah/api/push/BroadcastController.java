package com.caisheng.cheetah.api.push;

import java.util.List;

public interface BroadcastController {
    String taskId();
    int qps();
    void updateQps(int qps);
    boolean isDone();
    int sendCount();
    void cancel();
    boolean isCanceled();
    int incSendCount(int count);
    void success(String... userIds);
    List<String> successUserIds();
}
