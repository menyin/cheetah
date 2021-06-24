package com.caisheng.cheetah.tools.event;

public class EventConsumer {
    public EventConsumer() {
        EventBus.register(this);
    }
}
