package com.caisheng.cheetah.tools.event;

import com.caisheng.cheetah.api.event.Event;
import com.google.common.eventbus.AsyncEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class EventBus {
    private final static Logger logger = LoggerFactory.getLogger(EventBus.class);
    private static com.google.common.eventbus.EventBus eventBus;
    public static void create(Executor executor){
        eventBus = new AsyncEventBus(executor,(exception,context)->{
            logger.error("event bus subscriber ex", exception);
        });
    }

    public static void post(Event event) {
        eventBus.post(event);
    }
    public static void register(Object bean){
        eventBus.register(bean);
    }

    public static void unregister(Object bean){
        eventBus.unregister(bean);
    }

}
