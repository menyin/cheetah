package com.caisheng.cheetah.core.push;

import java.util.concurrent.ScheduledExecutorService;

public interface PushTask extends Runnable {
    ScheduledExecutorService getExecutor();
}
