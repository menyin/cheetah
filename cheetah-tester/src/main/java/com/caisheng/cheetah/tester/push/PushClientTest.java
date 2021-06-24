package com.caisheng.cheetah.tester.push;

import com.caisheng.cheetah.api.push.*;
import com.caisheng.cheetah.tools.log.Logs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class PushClientTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Logs.init();
        PushSender pushSender = PushSender.create();
        //pushSender.init();
        pushSender.start().join();
        Thread.sleep(1000);
        for (int i = 0; i < 1; i++) {
            PushMsg pushMsg = PushMsg.build(MsgType.MESSAGE, "hello,this is a push message.");
            pushMsg.setMsgId("msg_id_"+i);
            PushContext pushContext = new PushContext(pushMsg);
            pushContext.setAckModel(AckModel.AUTO_ACK);
            pushContext.setUserId("user-" + i);
            pushContext.setBroadcast(false);
            pushContext.setTimeout(1000);
            pushContext.setPushCallback(new PushCallback() {
                @Override
                public void onResult(PushResult pushResult) {//
                    System.out.println(pushResult);
                }
            });

            FutureTask<PushResult> futureTask = pushSender.send(pushContext);
            PushResult pushResult = futureTask.get();
            System.out.println(pushResult);
        }

    }
}
