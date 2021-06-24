package com.caisheng.cheetah.core.push;

import com.caisheng.cheetah.api.spi.push.IPushMessage;
import com.caisheng.cheetah.common.TimeLine;
import com.caisheng.cheetah.core.ack.AckCallback;
import com.caisheng.cheetah.core.ack.AckTask;
import com.caisheng.cheetah.tools.log.Logs;

public class PushAckCallback implements AckCallback {
    private final IPushMessage pushMessage;
    private final TimeLine timeLine;
    private final PushCenter pushCenter;
    public PushAckCallback(IPushMessage pushMessage, TimeLine timeLine, PushCenter pushCenter) {
        this.pushMessage=pushMessage;
        this.timeLine=timeLine;
        this.pushCenter=pushCenter;
    }

    @Override
    public void onSuccess(AckTask ackTask) {
        this.pushCenter.getPushListener().onAckSuccess(pushMessage,this.timeLine.successEnd().getTimePoints());
        Logs.PUSH.info("[SingleUserPush] connect ack success, timeLine={}, task={}", timeLine, ackTask);
    }

    @Override
    public void onTimeout(AckTask ackTask) {
        pushCenter.getPushListener().onTimeout(pushMessage, timeLine.timeoutEnd().getTimePoints());
        Logs.PUSH.warn("[SingleUserPush] connect ack timeout, timeLine={}, task={}", timeLine, ackTask);

    }
}
