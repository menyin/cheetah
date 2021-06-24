package com.caisheng.cheetah.cacher.redis.mq;

import com.caisheng.cheetah.api.CheetahContext;
import com.caisheng.cheetah.api.spi.common.MQClient;
import com.caisheng.cheetah.api.spi.common.MQMessageReceiver;
import com.caisheng.cheetah.cacher.redis.manager.RedisManager;
import com.caisheng.cheetah.tools.log.Logs;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class ListenerDispatcher implements MQClient {
    private final Map<String, List<MQMessageReceiver>> subscribes = Maps.newTreeMap();
    private final Subscriber subscriber;
    private Executor executor;

    public ListenerDispatcher() {
        subscriber = new Subscriber(this);
    }

    @Override
    public void init(CheetahContext context) {
        //TODO 从CheetahContext中获取一个线程池给this.executor赋值
    }

    public void onMessage(String channel, String message){
        List<MQMessageReceiver> mqMessageReceivers = subscribes.get(channel);
        if (mqMessageReceivers != null&&!mqMessageReceivers.isEmpty()) {
            mqMessageReceivers.forEach(mqMessageReceiver -> {
                executor.execute(()->{
                    mqMessageReceiver.receive(channel,message);//用线程池来执行任务，提高性能
                });
            });
        }
    }

    @Override
    public void subscribe(String topic, MQMessageReceiver mqMessageReceiver) {
        List<MQMessageReceiver> mqMessageReceivers = subscribes.get(topic);
        boolean topicIsSubscribed= mqMessageReceivers!=null;
        mqMessageReceivers=topicIsSubscribed?mqMessageReceivers:new ArrayList<MQMessageReceiver>();
        mqMessageReceivers.add(mqMessageReceiver);
        if(!topicIsSubscribed){//防止重复订阅redisqMq消息
            RedisManager.I.subscribe(this.subscriber,topic);
        }
    }

    @Override
    public void publish(String topic, Object message) {
        RedisManager.I.publish(topic,message);
    }
}
