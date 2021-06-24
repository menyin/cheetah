package com.caisheng.cheetah.cacher.redis.mq;

import com.caisheng.cheetah.tools.Jsons;
import com.caisheng.cheetah.tools.log.Logs;
import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {
    private final ListenerDispatcher listenerDispatcher;

    public Subscriber(ListenerDispatcher listenerDispatcher) {
        this.listenerDispatcher = listenerDispatcher;
    }

    @Override
    public void onMessage(String channel, String message) {
        this.listenerDispatcher.onMessage(channel,message);
        Logs.CACHE.info("onMessage:{},{}",channel,message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        Logs.CACHE.info("onPMessage:{},{},{}",pattern,channel,message);

    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        Logs.CACHE.info("onSubscribe:{},{}",channel,subscribedChannels);

    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        Logs.CACHE.info("onUnsubscribe:{},{}",channel,subscribedChannels);

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        Logs.CACHE.info("onPUnsubscribe:{},{}",pattern,subscribedChannels);

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        Logs.CACHE.info("onPSubscribe:{},{}",pattern,subscribedChannels);

    }

    @Override
    public void unsubscribe() {
        Logs.CACHE.info("unsubscribe");

    }

    @Override
    public void unsubscribe(String... channels) {
        Logs.CACHE.info("unsubscribe：{}", Jsons.toJson(channels));
    }
}
