package com.caisheng.cheetah.tester.push;

import com.caisheng.cheetah.api.push.*;
import com.caisheng.cheetah.common.qps.FlowControl;
import com.caisheng.cheetah.common.qps.GlobalFlowControl;
import com.caisheng.cheetah.tools.log.Logs;

import java.time.LocalTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PushClientTest2 {
    public static void main(String[] args) throws InterruptedException {
        Logs.init();
        PushSender pushSender = PushSender.create();
        pushSender.start().join();
        Thread.sleep(1000);

        Statistics statistics = new Statistics();

        FlowControl flowControl = new GlobalFlowControl(1000);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("time=" + LocalTime.now() +
                    ",flowControl=" + flowControl.report() +
                    ",statistics=" + statistics);
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(4);
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1; j++) {
                while (executorService.getQueue().size() > 1000) {
                    Thread.sleep(1000);
                }//防止内存溢出，做下延时

                //构造发送的消息
                PushMsg pushMsg = PushMsg.build(MsgType.MESSAGE, "hello,this is a push message.");
                pushMsg.setMsgId("msg_id_" + i);
                PushContext pushContext = new PushContext(pushMsg);
                pushContext.setAckModel(AckModel.AUTO_ACK);
                pushContext.setUserId("user-" + i);
                pushContext.setBroadcast(false);
                pushContext.setTimeout(1000);
                pushContext.setPushCallback(new PushCallback() {
                    @Override
                    public void onResult(PushResult pushResult) {
                        statistics.add(pushResult.getResultCode());
                    }
                });

                executorService.execute(new PushTask(pushSender,executorService,flowControl,statistics,pushContext));

            }

        }
    }



    private static class PushTask implements Runnable {
        private PushSender pushSender;
        private ScheduledExecutorService executorService;
        private FlowControl flowControl;
        private Statistics statistics;
        private PushContext pushContext;

        public PushTask(PushSender pushSender, ScheduledExecutorService executorService, FlowControl flowControl, Statistics statistics, PushContext pushContext) {
            this.pushSender = pushSender;
            this.executorService = executorService;
            this.flowControl = flowControl;
            this.statistics = statistics;
            this.pushContext = pushContext;
        }


        @Override
        public void run() {
            if (flowControl.checkQps()) {
                 pushSender.send(pushContext);
            }else{
                executorService.schedule(this, flowControl.getDelay(), TimeUnit.NANOSECONDS);
            }

        }
    }

    private static class Statistics {
        final AtomicInteger successNum = new AtomicInteger();
        final AtomicInteger failureNum = new AtomicInteger();
        final AtomicInteger offlineNum = new AtomicInteger();
        final AtomicInteger timeoutNum = new AtomicInteger();

        final AtomicInteger[] counts = new AtomicInteger[]{successNum, failureNum, offlineNum, timeoutNum};

        public void add(int code) {
            counts[code - 1].incrementAndGet();
        }

        @Override
        public String toString() {
            return "{" +
                    "successNum=" + successNum +
                    ", offlineNum=" + offlineNum +
                    ", timeoutNum=" + timeoutNum +
                    ", failureNum=" + failureNum +
                    '}';
        }
    }
}
