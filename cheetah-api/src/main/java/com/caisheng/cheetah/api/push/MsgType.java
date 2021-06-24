package com.caisheng.cheetah.api.push;

public enum MsgType {
    NOTIFICATION("提醒", 1),
    MESSAGE("消息", 2),
    NOTIFICATION_AND_MESSAGE("提醒+消息", 3);

    private String desc;
    private int value;

    MsgType(String desc, int value) {
        this.desc = desc;
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }
}
